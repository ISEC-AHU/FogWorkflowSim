package org.workflowsim.scheduling;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.utils.FogEvents;
import org.workflowsim.*;
import org.workflowsim.utils.*;

import java.util.Iterator;
import java.util.List;

/**
 * Strategy method for task scheduling
 *
 * @since TSP Extension 1.0
 * @author Julio Corona
 */
public class TSPSchedulingAlgorithm extends TSPBaseStrategyAlgorithm {


    public TSPSchedulingAlgorithm()
    {
        super();
    }

    /**
     * Performs a TSP operation
     * @param isRequired true if the method was called from e processCloudletUpdate. Else the method was called after another TSP operation
     * @return
     */
    public double runSteep(boolean isRequired) {

        List cloudletList = getReadyForScheduleCloudletList(isRequired);

//        System.out.println("runSteep");

        if (cloudletList.isEmpty()){
            return -1;
        }

        TSPJobManager.releaseFinishedTasks(CloudSim.clock());

        //parse the tasks and the devices' status to a vector
        Long[] state = TSPEnvHelper.parseStateWithTasksAndServers(cloudletList, getNotMobileVmList());

        TSPDecisionResult response = TSPSocketClient.askForDecisionWithActionId(TSPJobManager.last_executed_task_no, state);
        int action = response.getAction()[0];

        double decision_time = response.getTime();

//        System.out.println("Action received:" + action);
//        System.out.println("Time:" + response.getTime());

        if (action == -1){ // -1 means there was no server with enough resources for any task
            return -1;
        }

        Cloudlet cloudlet = (Cloudlet) cloudletList.get(action);
        TSPTask tsp_task = (TSPTask) ((Job) cloudlet).getTaskList().get(0);

        CondorVM vm = placerFCFS(cloudlet, tsp_task);

        double task_decision_and_offloading_time = decision_time + TSPEnvHelper.getOffloadingTimeByFogDeviceId(vm.getHost().getDatacenter().getId(), tsp_task.getStorage());
        double task_start_execution_timestamp = CloudSim.clock() + task_decision_and_offloading_time;
        double task_running_time = tsp_task.getMi() / vm.getMips();

        boolean deadline_exceeded = task_start_execution_timestamp + task_running_time > tsp_task.getTimeDeadlineFinal();

        if (deadline_exceeded){
            TSPJobManager.registerTaskExceedingDeadline(tsp_task.getPriority());
            getScheduledList().remove(cloudlet);
            getCloudletList().remove(cloudlet);
            vm.setState(WorkflowSimTags.VM_STATUS_IDLE);
//                    System.out.println("DEADLINE");
        }else {
            TSPJobManager.addTaskRunning(cloudlet, tsp_task, decision_time, task_start_execution_timestamp);
            TSPJobManager.updateDeviceBusyTime(vm.getHost().getId(), tsp_task.getMi() / vm.getMips());
        }

        double reward = getReward(tsp_task, vm, deadline_exceeded, decision_time, task_start_execution_timestamp + task_running_time - tsp_task.getArrivalTime());

        TSPSocketClient.saveReward(TSPJobManager.last_executed_task_no, reward);
        if (TSPJobManager.last_executed_task_no != 0){
            //updating the placer information
            TSPSocketClient.retrain(TSPJobManager.last_executed_task_no - 1, state);
        }
        TSPJobManager.last_executed_task_no += 1;

        cloudletList.remove(cloudlet);

//        System.out.println(deadline_exceeded);

        // if its required to offload something and this was a deadline then go to the next
        if (deadline_exceeded && isRequired){
            return decision_time + runSteep(isRequired);
        }

        return decision_time;
    }

    /**
     * Placer in the next servers with enough resources
     */
    public CondorVM placerFCFS(Cloudlet cloudlet, TSPTask tsp_task){

        for (Iterator itc = getNotMobileVmList().iterator(); itc.hasNext();) {
            CondorVM vm = (CondorVM) itc.next();

            if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE && tsp_task.getRam() <= vm.getRam() && tsp_task.getStorage() <= vm.getSize()) {

                vm.setState(WorkflowSimTags.VM_STATUS_BUSY);
                cloudlet.setVmId(vm.getId());
                //System.out.println("vm"+vm.getId()+".mips: "+vm.getMips()+"  host: "+vm.getHost().getId());
                getScheduledList().add(cloudlet);

                return vm;
            }
        }
        System.err.println("Invalid point reached");
        System.exit(-1);
        return null;
    }

}
