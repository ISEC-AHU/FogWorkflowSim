package org.workflowsim.scheduling;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.workflowsim.*;
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

    /**
     * Scheduler regarding the constraints
     */
    @Override
    public void run() {

        System.out.println("Batch scheduling and Placing...");

        TSPJobManager.releaseFinishedTasks(CloudSim.clock);

        List cloudletList = getReadyForScheduleCloudletList();
        List vmList = getNotMobileVmList();

        if (!cloudletList.isEmpty()){
            //parse the tasks and the devices' status to a vector
            Long[] state = TSPEnvHelper.parseStateWithTasksAndServers(cloudletList, vmList);

            int[] action = TSPSocketClient.askForDecision(null, state);
            System.out.println("Action received from Python:" + Arrays.toString(action));

            double reward_total = 0;
            int reward_quantity = 0;

            for (int server_no=0; server_no < action.length; server_no++){
                int task_no = action[server_no];
                if (task_no != -1){
                    Cloudlet cloudlet = (Cloudlet) cloudletList.get(task_no);
                    CondorVM vm = (CondorVM) vmList.get(server_no);

                    //place the task in the vm
                    place(cloudlet, vm);
                    TSPTask tsp_task = (TSPTask) ((Job) cloudlet).getTaskList().get(0);
                    TSPJobManager.addTaskRunning(tsp_task);

                    double reward = getReward(tsp_task, vm);

                    reward_total += reward;
                    reward_quantity += 1;
                }
            }

            if (reward_quantity == 0){
                return;
            }

            double reward_avg = reward_total / reward_quantity;

            System.out.println("Reward " + reward_avg);

            TSPJobManager.last_executed_cloudlet_id += 1;

            TSPSocketClient.saveReward(TSPJobManager.last_executed_cloudlet_id, reward_avg);
            if (TSPJobManager.last_executed_cloudlet_id != 0){
                //updating the placer information
                TSPSocketClient.retrain(TSPJobManager.last_executed_cloudlet_id - 1, state);
            }

        }
    }

    /**
     * Placer in the next servers with enough resources
     */
    public void place(Cloudlet cloudlet, CondorVM vm){
        vm.setState(WorkflowSimTags.VM_STATUS_BUSY);
        cloudlet.setVmId(vm.getId());
        System.out.println("vm"+vm.getId()+".mips: "+vm.getMips()+"  host: "+vm.getHost().getId());
        getScheduledList().add(cloudlet);
    }

}
