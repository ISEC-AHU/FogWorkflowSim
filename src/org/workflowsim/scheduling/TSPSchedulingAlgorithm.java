package org.workflowsim.scheduling;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.workflowsim.*;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.TSPEnvHelper;
import org.workflowsim.utils.TSPJobManager;
import org.workflowsim.utils.TSPSocketClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Temporal template with FCFS algorithm for the TSP problem
 *
 * @since TSP Extension 1.0
 * @author Julio Corona
 */
public class TSPSchedulingAlgorithm extends BaseSchedulingAlgorithm {


    public TSPSchedulingAlgorithm()
    {
        super();
    }

    /**
     * Return the list of devices available for placement
     * @return the list of fog and cloud devices
     */
    public List getNotMobileVmList() {
        Predicate<CondorVM> byLayer = vm -> !vm.getHost().getDatacenter().getName().startsWith("m");
        return (List)getVmList().stream().filter(byLayer).collect(Collectors.toList());
    }

    /**
     * Return the simulated gateway device
     * @return the mobile device
     */
    public CondorVM getGatewayVm(){
        for (Iterator itc = getVmList().iterator(); itc.hasNext();) { //VM list
            CondorVM vm = (CondorVM) itc.next();
            if (vm.getHost().getDatacenter().getName().startsWith("m")){
                return vm;
            }
        }
        return null;
    }

    /**
     * Scheduler regarding the constraints
     */
    @Override
    public void run() {

        TSPJobManager.releaseFinishedTasks(CloudSim.clock);

        List cloudletList = getCloudletList();

        //auxiliary variable to know if the clock needs to be moved in case placement is not done
        boolean some_task_was_scheduled = !cloudletList.isEmpty();

        for (Iterator it = cloudletList.iterator(); it.hasNext();) { //Task list

            Job next=(Job)it.next();

            //if the job has a TSP task
            if (next.getTaskList().size() > 0){
                Task cloudlet = (Task)next;
                TSPTask task_info = (TSPTask)next.getTaskList().get(0);

                if (CloudSim.clock() >= task_info.getTimeSubmission() && TSPJobManager.canRunTask(task_info.getJobId(), task_info.getTaskId())) {

                    boolean stillHasVm = placer(cloudlet, task_info);

                    //no vm available
                    if (stillHasVm) {
                        some_task_was_scheduled = false;
                    }else{
                        break;
                    }
                }
            }
            //else, the job is the first WorkflowSim's job, and the allocation is in the gateway device
            else{
                Task cloudlet = (Task) next;

                CondorVM vm = getGatewayVm();
                if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE) {
                    vm.setState(WorkflowSimTags.VM_STATUS_BUSY);
                    cloudlet.setVmId(vm.getId());
                    getScheduledList().add(cloudlet);
                    some_task_was_scheduled = false;
                    break;
                }
            }
        }

        if (some_task_was_scheduled){

            /*
              next_executable_job structure
              [
               double: the clock on which jobs can be executed
               ArrayList<Job>: the list of jobs that can be processed
              ]
              */
            Object[] next_executable_job = TSPJobManager.getNextAvailableJobs(cloudletList, CloudSim.clock());

            CloudSim.clock = (Double)next_executable_job[0];

            for (Iterator it = ((ArrayList<Job>)next_executable_job[1]).iterator(); it.hasNext();) {
                Job next=(Job)it.next();
                Task cloudlet = (Task)next;
                TSPTask task_info = (TSPTask)next.getTaskList().get(0);

                if (CloudSim.clock() >= task_info.getTimeSubmission() && TSPJobManager.canRunTask(task_info.getJobId(), task_info.getTaskId())) {
                    boolean stillHasVm = placer(cloudlet, task_info);

                    //no vm available
                    if (!stillHasVm) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Placer regarding the availability
     */
    public boolean placer(Cloudlet cloudlet, TSPTask taskInfo){

        //list of fog and cloud devices
        List not_mobile_list = getNotMobileVmList();

        //parses the task and the device status to a vector
        Long[] state = TSPEnvHelper.parseState(taskInfo, not_mobile_list);

        //call the placement agent
        int action = TSPSocketClient.askForDecision(taskInfo.getCloudletId(), state);

        System.out.println("Action received from Python:" + action);

        boolean stillHasVm = false;

        if (action != -1){
            stillHasVm = true;
            CondorVM vm = (CondorVM) not_mobile_list.get(action);
            vm.setState(WorkflowSimTags.VM_STATUS_BUSY);
            cloudlet.setVmId(vm.getId());
            getScheduledList().add(cloudlet);
            TSPJobManager.addTaskRunning(taskInfo);

            //compute the task's processing time and save it
            double task_processing_time = taskInfo.getMi() / vm.getMips();

            double reward;

            if (task_processing_time + CloudSim.clock() > taskInfo.getTimeDeadlineFinal()){
                reward = Float.MIN_VALUE / 2; //penalization
                TSPJobManager.registerTaskExceedingDeadline(taskInfo.getPriority());
            }else {


                if (Parameters.getOptimization() == Parameters.Optimization.Time){
                    // for time optimization
                    reward = task_processing_time - taskInfo.getTimeSubmission();
                }else{
                    // for energy optimization
//                    reward = double  e = job.getActualCPUTime() * powerModel.getPower(vm.getMips()/host.getTotalMips());;//ToDo
                }
            }

            TSPSocketClient.saveReward(taskInfo.getCloudletId(), reward);

            if (TSPJobManager.last_executed_cloudlet_id != -1){

                //updating the placer information
                TSPSocketClient.retrain(TSPJobManager.last_executed_cloudlet_id, state);
            }
            TSPJobManager.last_executed_cloudlet_id = taskInfo.getCloudletId();
        }

        return stillHasVm;
    }



}
