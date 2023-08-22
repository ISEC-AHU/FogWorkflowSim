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
 * Strategy method for scheduling and placing multiple tasks at the same time
 *
 * @since TSP Extension 1.0
 * @author Julio Corona
 */
public class TSPBatchSchedulingAndPlacementAlgorithm extends TSPBaseStrategyAlgorithm {


    public TSPBatchSchedulingAndPlacementAlgorithm()
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

        double reward_total = 0;
        int reward_quantity = 0;

        for (int server_no=0; server_no < action.length; server_no++){
            int task_no = action[server_no];
            if (task_no != -1){
                Cloudlet cloudlet = (Cloudlet) cloudletList.get(task_no);
                CondorVM vm = (CondorVM) vmList.get(server_no);

                TSPTask tsp_task = (TSPTask) ((Job) cloudlet).getTaskList().get(0);

                double task_decision_and_offloading_time = decision_time + TSPEnvHelper.getOffloadingTimeByFogDeviceId(vm.getHost().getDatacenter().getId(), tsp_task.getStorage());
                double task_start_execution_timestamp = CloudSim.clock() + task_decision_and_offloading_time;
                double task_running_time = tsp_task.getMi() / vm.getMips();

                boolean deadline_exceeded = task_start_execution_timestamp + task_running_time > tsp_task.getTimeDeadlineFinal();

                if (deadline_exceeded){
                    TSPJobManager.registerTaskExceedingDeadline(tsp_task.getPriority());
                    getCloudletList().remove(cloudlet);
                }else {
                    //place the task in the vm
                    place(cloudlet, vm);
                    TSPJobManager.addTaskRunning(cloudlet, tsp_task, decision_time, task_start_execution_timestamp);
                    TSPJobManager.updateDeviceBusyTime(vm.getHost().getId(), tsp_task.getMi() / vm.getMips());
                }

                double reward = getReward(tsp_task, vm, deadline_exceeded, decision_time, task_start_execution_timestamp + task_running_time - tsp_task.getArrivalTime());

                reward_total += reward;
                reward_quantity += 1;
            }
        }

        double reward_avg = reward_total / ((reward_quantity == 0)?1:reward_quantity);

        TSPSocketClient.saveReward(TSPJobManager.last_executed_task_no, reward_avg);
        if (TSPJobManager.last_executed_task_no != 0){
            //updating the placer information
            //last call: getCloudletList().size() == reward_quantity

            TSPSocketClient.retrain(TSPJobManager.last_executed_task_no - 1, state);
        }

        TSPJobManager.last_executed_task_no += 1;

        if (getScheduledList().isEmpty()){
            if (isRequired){
                return decision_time + runSteep(isRequired);
            }else {
                return -1;
            }
        }


        return decision_time;

    }

    /**
     * Placer in the next servers with enough resources
     */
    public void place(Cloudlet cloudlet, CondorVM vm){
        vm.setState(WorkflowSimTags.VM_STATUS_BUSY);
        cloudlet.setVmId(vm.getId());
        //System.out.println("vm"+vm.getId()+".mips: "+vm.getMips()+"  host: "+vm.getHost().getId());
        getScheduledList().add(cloudlet);
    }

}
