package org.workflowsim.scheduling;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.workflowsim.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Temporal template with FCFS algorithm for the TSP problem. In the next release this will be updated to be a Reinforcement Learning Agent.
 *
 * @since TSP Extension 1.0
 * @author Julio Corona
 */
public class RL1SchedulingAlgorithm  extends BaseSchedulingAlgorithm {


    public RL1SchedulingAlgorithm()
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
    public boolean placer(Cloudlet cloudlet, TSPTask task_info){
        boolean stillHasVm = false;

        for (Iterator itc = getNotMobileVmList().iterator(); itc.hasNext(); ) { //VM list
            CondorVM vm = (CondorVM) itc.next();
            if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE) {
                stillHasVm = true;
                vm.setState(WorkflowSimTags.VM_STATUS_BUSY);
                cloudlet.setVmId(vm.getId());
                getScheduledList().add(cloudlet);
                TSPJobManager.addTaskRunning(task_info);
                break;
            }
        }
        return stillHasVm;
    }
}
