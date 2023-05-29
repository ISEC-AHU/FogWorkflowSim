package org.workflowsim.scheduling;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.workflowsim.*;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.TSPJobManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Base template for the TSP problem strategies
 *
 * @since TSP Extension 1.0
 * @author Julio Corona
 */
public abstract class TSPBaseStrategyAlgorithm extends BaseSchedulingAlgorithm {

    /**
     * This method will be not used in TSP
     */
    public void run() {
        return;
    }

    /**
     * This method will replace the 'run' method in TSP
     */

    public abstract double runSteep(boolean isRequired) throws Exception;

    /**
     * Return the list of devices available for placement
     * @return the list of fog and cloud devices
     */
    public List<Vm> getNotMobileVmList() {
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
     * Gets the list of tasks that meets the jobs and timestamps requirements
     * @return the list of tasks
     */

    public List getReadyForScheduleCloudletList(){ //temp
        return new ArrayList<>();
    }

    public List getReadyForScheduleCloudletList(boolean isRequired){
        List cloudletList = getCloudletList();
        List<Job> filteredCloudletList = new ArrayList<>();

        for (Iterator it = cloudletList.iterator(); it.hasNext();) { //Task list

            Job next=(Job)it.next();

            //if the job has a TSP task
            if (next.getTaskList().size() > 0){
                TSPTask tsp_task = (TSPTask)next.getTaskList().get(0);
                if (CloudSim.clock() >= tsp_task.getArrivalTime() && (!Parameters.getConsiderTasksParallelismRestrictions() || TSPJobManager.canRunTask(tsp_task.getJobId(), tsp_task.getTaskId()))) {
                    filteredCloudletList.add(next);
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
                }
                return new ArrayList<>();
            }
        }

        if (isRequired && filteredCloudletList.isEmpty() && !cloudletList.isEmpty()){
            Object[] next_executable_job = TSPJobManager.getNextAvailableJobs(cloudletList, CloudSim.clock());
            CloudSim.clock = (Double)next_executable_job[0];
            filteredCloudletList = (ArrayList<Job>)next_executable_job[1];
        }
        return filteredCloudletList;
    }

    /**
     * Compute the strategy's action reward
     * @param vm the vm for allocating the task
     * @param tsp_task the task to be allocated
     * @return
     */
    public double getReward(TSPTask tsp_task, CondorVM vm, boolean deadline_exceeded, double task_start_executing_time){

        //compute the task's processing time and save it
        double task_running_time = tsp_task.getMi() / vm.getMips();

        double task_completion_time = task_start_executing_time + task_running_time - tsp_task.getArrivalTime();

        TSPJobManager.updateDeviceBusyTime(vm.getHost().getId(), task_running_time);

        double reward;
        double energy_consumed;
        PowerHost host;

        switch (Parameters.getSchedulingAlgorithm()) {
            case TSP_Scheduling:
                reward = 1 / task_completion_time;
//                System.out.println(task_completion_time);
                if (deadline_exceeded){
                    reward /= tsp_task.getPriority();
                }
                break;
            case TSP_Placement:
                // weight of the reward function components
                double w1 = 0.5;
                double w2 = 0.5;

                host = (PowerHost)vm.getHost();
                energy_consumed = task_running_time * host.getPowerModel().getPower(vm.getMips()/(host).getTotalMips());

                double r1 = -(2/Math.PI) * Math.atan(task_completion_time - tsp_task.getMi() * energy_consumed / vm.getMips());
                double r2;

                double task_expected_run_time = tsp_task.getTimeDeadlineFinal() - CloudSim.clock();
                if (task_running_time <= task_expected_run_time){
                    r2 = 1;
                }else if(task_running_time > 2 * task_expected_run_time){
                    r2 = 0;
                }else {
                    r2 = -Math.pow(task_completion_time / tsp_task.getTimeDeadlineFinal() - 1, 3) + 1;
                }

                reward = w1 * r1 + w2 * r2;

                break;
            case TSP_Scheduling_Placement:
                reward = 1 / task_completion_time;
                break;
            case TSP_Batch_Schedule_Placement:
                reward = 1 / task_completion_time;
                break;
            //TSP code end
            default:
                reward = 0;
                System.err.println("Not valid option for TSP");
                System.exit(1);
        }

        return reward;
    }
}
