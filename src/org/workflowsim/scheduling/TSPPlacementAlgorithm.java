package org.workflowsim.scheduling;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
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
 * Strategy method for task placing
 *
 * @since TSP Extension 1.0
 * @author Julio Corona
 */
public class TSPPlacementAlgorithm extends TSPBaseStrategyAlgorithm {


    public TSPPlacementAlgorithm()
    {
        super();
        System.out.println("CONSTRUCTOR CALLED");
    }

    /**
     * Scheduler regarding the constraints
     */
    @Override
    public void run() {

        System.out.println("Placing...");

        TSPJobManager.releaseFinishedTasks(CloudSim.clock);

        List cloudletList = getCloudletList();

        //auxiliary variable to know if the clock needs to be moved in case placement is not done
        boolean some_task_was_scheduled = !cloudletList.isEmpty();

        for (Iterator it = cloudletList.iterator(); it.hasNext();) { //Task list

            Job next=(Job)it.next();

            //if the job has a TSP task
            if (next.getTaskList().size() > 0){
                Task cloudlet = (Task)next;
                TSPTask tsp_task = (TSPTask)next.getTaskList().get(0);

                if (CloudSim.clock() >= tsp_task.getTimeSubmission() && (!Parameters.getConsiderTasksParallelismRestrictions() || TSPJobManager.canRunTask(tsp_task.getJobId(), tsp_task.getTaskId()))) {

                    boolean stillHasVm = placer(cloudlet, tsp_task);

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
            Object[] next_executable_job = TSPJobManager.getNextAvailableJobs(cloudletList, CloudSim.clock());
            CloudSim.clock = (Double)next_executable_job[0];
            for (Iterator it = ((ArrayList<Job>)next_executable_job[1]).iterator(); it.hasNext();) {
                Job next=(Job)it.next();
                Task cloudlet = (Task)next;
                TSPTask tsp_task = (TSPTask)next.getTaskList().get(0);
                if (CloudSim.clock() >= tsp_task.getTimeSubmission() && (!Parameters.getConsiderTasksParallelismRestrictions() || TSPJobManager.canRunTask(tsp_task.getJobId(), tsp_task.getTaskId()))) {
                    boolean stillHasVm = placer(cloudlet, tsp_task);
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
    public boolean placer(Cloudlet cloudlet, TSPTask tspTask){

        //list of fog and cloud devices
        List<Vm> not_mobile_list = getNotMobileVmList();

        //parses the task and the device status to a vector
        Long[] state = TSPEnvHelper.parseStateWithTaskAndServers(tspTask, not_mobile_list);

        //call the placement agent
        int action = TSPSocketClient.askForDecision(tspTask.getCloudletId(), state)[0];

        System.out.println("Action received from Python:" + action);

        boolean stillHasVm = false;

        if (action != -1){ // -1 means there was no server with enough resources
            stillHasVm = true;
            CondorVM vm = (CondorVM) not_mobile_list.get(action);
            vm.setState(WorkflowSimTags.VM_STATUS_BUSY);
            cloudlet.setVmId(vm.getId());
            getScheduledList().add(cloudlet);
            TSPJobManager.addTaskRunning(tspTask);

            double reward = getReward(tspTask, vm);

            System.out.println("Reward " + reward);

            TSPSocketClient.saveReward(tspTask.getCloudletId(), reward);

            if (TSPJobManager.last_executed_cloudlet_id != -1){

                //updating the placer information
                TSPSocketClient.retrain(TSPJobManager.last_executed_cloudlet_id, state);
            }
            TSPJobManager.last_executed_cloudlet_id = tspTask.getCloudletId();
        }

        return stillHasVm;
    }

}
