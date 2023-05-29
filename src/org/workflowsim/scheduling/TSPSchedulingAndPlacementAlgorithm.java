package org.workflowsim.scheduling;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.workflowsim.*;
import org.workflowsim.utils.TSPDecisionResult;
import org.workflowsim.utils.TSPEnvHelper;
import org.workflowsim.utils.TSPJobManager;
import org.workflowsim.utils.TSPSocketClient;

import java.util.Arrays;
import java.util.List;

/**
 * Strategy method for scheduling and placing a task
 *
 * @since TSP Extension 1.0
 * @author Julio Corona
 */
public class TSPSchedulingAndPlacementAlgorithm extends TSPBaseStrategyAlgorithm {


    public TSPSchedulingAndPlacementAlgorithm()
    {
        super();
    }


    public double runSteep(boolean isRequired){
        List cloudletList = getReadyForScheduleCloudletList(isRequired);

        if (cloudletList.isEmpty()){
            return -1;
        }

        TSPJobManager.releaseFinishedTasks(CloudSim.clock());

        List vmList = getNotMobileVmList();

        Long[] state = TSPEnvHelper.parseStateWithTasksAndServers(cloudletList, getNotMobileVmList());

        TSPDecisionResult response = TSPSocketClient.askForDecisionWithActionId(TSPJobManager.last_executed_task_no, state);
        int[] action = response.getAction();

        double decision_time = response.getTime();

//        System.out.println("Action received:" + action[0] + "," + action[1]);

        if (action[0] == -1){ // -1 means there was no server with enough resources for any task
            return -1;
        }

        Cloudlet cloudlet = (Cloudlet) cloudletList.get(action[0]);
        TSPTask tsp_task = (TSPTask) ((Job) cloudlet).getTaskList().get(0);

        CondorVM vm = (CondorVM) vmList.get(action[1]);

        double task_start_executing_time = decision_time + TSPEnvHelper.getOffloadingTimeByFogDeviceId(vm.getHost().getDatacenter().getId(), tsp_task.getStorage());

        boolean deadline_exceeded = task_start_executing_time + tsp_task.getMi() / vm.getMips() + CloudSim.clock() > tsp_task.getTimeDeadlineFinal();

        if (deadline_exceeded){
            TSPJobManager.registerTaskExceedingDeadline(tsp_task.getPriority());
            getCloudletList().remove(cloudlet);
        }else {
            //place the task in the vm
            place(cloudlet, vm);
            TSPJobManager.addTaskRunning(cloudlet, tsp_task, decision_time, task_start_executing_time);
        }

        double reward = getReward(tsp_task, vm, deadline_exceeded, task_start_executing_time);

        TSPSocketClient.saveReward(TSPJobManager.last_executed_task_no, reward);
        if (TSPJobManager.last_executed_task_no != 0){
            //updating the placer information
            TSPSocketClient.retrain(TSPJobManager.last_executed_task_no - 1, state);
        }
        TSPJobManager.last_executed_task_no += 1;

//        cloudletList.remove(cloudlet); //Si no funciona descomentar aqui

        // if its required to offload something and this was a deadline then go to the next
        if (deadline_exceeded && isRequired){
            return decision_time + runSteep(isRequired);
        }

        return decision_time;
    }

    /**
     * Placer in the next servers with enough resources
     */
    public void place(Cloudlet cloudlet, CondorVM vm){
        vm.setState(WorkflowSimTags.VM_STATUS_BUSY);
        cloudlet.setVmId(vm.getId());
        getScheduledList().add(cloudlet);
    }

}
