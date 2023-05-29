package org.workflowsim.scheduling;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.workflowsim.*;
import org.workflowsim.utils.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    }

    public double runSteep(boolean isRequired){
        List cloudletList = getReadyForScheduleCloudletList(isRequired);

        if (cloudletList.isEmpty()){
            return -1;
        }

        TSPJobManager.releaseFinishedTasks(CloudSim.clock());

        Cloudlet cloudlet = (Cloudlet) cloudletList.get(0);
        TSPTask tsp_task = (TSPTask) ((Job) cloudlet).getTaskList().get(0);

        double decision_time = placer(cloudlet, tsp_task);

        if (decision_time == -1.0){ // -1.0 means there was no server with enough resources for the current task
            return -1;
        }

        cloudletList.remove(0);

        if (isRequired && was_deadline){
            return decision_time + runSteep(isRequired);
        }

        return decision_time;

    }

    /**
     * Placer regarding the availability
     * returns:
     *  -1.0 if there is no allocating available option
     *  else, the agent decision time
     */
    private boolean was_deadline;

    public double placer(Cloudlet cloudlet, TSPTask tsp_task){
        //revisar porque se llama muchas veces al learning o al agente

//        System.out.println("aaa");

        //list of fog and cloud devices
        List<Vm> not_mobile_list = getNotMobileVmList();

        //parses the task and the device status to a vector
        Long[] state = TSPEnvHelper.parseStateWithTaskAndServers(tsp_task, not_mobile_list);

        //call the placement agent
        TSPDecisionResult response = TSPSocketClient.askForDecisionWithActionId(TSPJobManager.last_executed_task_no, state);
        int action = response.getAction()[0];

        double decision_time = response.getTime();

//        System.out.println("Action received from Python:" + action);

        if (action == -1){
            return -1.0;  // -1 means there was no server with enough resources for the current task
        }

        CondorVM vm = (CondorVM) not_mobile_list.get(action);

        double task_start_executing_time = decision_time + TSPEnvHelper.getOffloadingTimeByFogDeviceId(vm.getHost().getDatacenter().getId(), tsp_task.getStorage());

        boolean deadline_exceeded = task_start_executing_time + tsp_task.getMi() / vm.getMips() + CloudSim.clock() > tsp_task.getTimeDeadlineFinal();

        if (deadline_exceeded) {
            TSPJobManager.registerTaskExceedingDeadline(tsp_task.getPriority());
            getCloudletList().remove(cloudlet);
        }else {
            vm.setState(WorkflowSimTags.VM_STATUS_BUSY);
            cloudlet.setVmId(vm.getId());
            getScheduledList().add(cloudlet);
            TSPJobManager.addTaskRunning(cloudlet, tsp_task, decision_time, task_start_executing_time);
        }

        double reward = getReward(tsp_task, vm, deadline_exceeded, task_start_executing_time);

        TSPSocketClient.saveReward(TSPJobManager.last_executed_task_no, reward);
        if (TSPJobManager.last_executed_task_no != 0){
            //updating the placer information
            TSPSocketClient.retrain(TSPJobManager.last_executed_task_no - 1, state);
        }

        TSPJobManager.last_executed_task_no += 1;

        was_deadline = deadline_exceeded;

        return decision_time;
    }

}
