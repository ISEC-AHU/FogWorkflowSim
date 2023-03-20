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

    public List getReadyForScheduleCloudletList(){
        List cloudletList = getCloudletList();
        List<Job> filteredCloudletList = new ArrayList<>();

        for (Iterator it = cloudletList.iterator(); it.hasNext();) { //Task list

            Job next=(Job)it.next();

            //if the job has a TSP task
            if (next.getTaskList().size() > 0){
                TSPTask tsp_task = (TSPTask)next.getTaskList().get(0);
                if (CloudSim.clock() >= tsp_task.getTimeSubmission() && (!Parameters.getConsiderTasksParallelismRestrictions() || TSPJobManager.canRunTask(tsp_task.getJobId(), tsp_task.getTaskId()))) {
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
                    break;
                }
                return new ArrayList<>();
            }
        }

        if (filteredCloudletList.isEmpty()){
            Object[] next_executable_job = TSPJobManager.getNextAvailableJobs(cloudletList, CloudSim.clock());
            CloudSim.clock = (Double)next_executable_job[0];
            for (Iterator it = ((ArrayList<Job>)next_executable_job[1]).iterator(); it.hasNext();) {
                Job next=(Job)it.next();
                TSPTask tsp_task = (TSPTask)next.getTaskList().get(0);
                if (CloudSim.clock() >= tsp_task.getTimeSubmission() && (!Parameters.getConsiderTasksParallelismRestrictions() || TSPJobManager.canRunTask(tsp_task.getJobId(), tsp_task.getTaskId()))) {
                    filteredCloudletList.add(next);
                }
            }
        }
        return filteredCloudletList;
    }

    /**
     * Compute the strategy's action reward for a given placement action
     * @param vm the vm for allocating the task
     * @param tsp_task the task to be allocated
     * @return
     */
    public double getReward(TSPTask tsp_task, CondorVM vm){

        //compute the task's processing time and save it
        double task_running_time = tsp_task.getMi() / vm.getMips();

        double cost;

        if (Parameters.getOptimization() == Parameters.Optimization.Energy || Parameters.getOptimization() == Parameters.Optimization.TaskCompletionTimeAndEnergy){
            TSPJobManager.updateDeviceBusyTime(vm.getHost().getId(), task_running_time);
        }

        if (Parameters.isDeadlinePenalizationEnabled() &&  task_running_time + CloudSim.clock() > tsp_task.getTimeDeadlineFinal()){
            cost = Float.MAX_VALUE / 2; //penalization
            TSPJobManager.registerTaskExceedingDeadline(tsp_task.getPriority());

            if (Parameters.getPrioritiesQuantity() > 0){
                cost *= tsp_task.getPriority() * 1.0 / Parameters.getPrioritiesQuantity();
            }

        }else {
            switch (Parameters.getOptimization()) {
                case AvgTaskCompletionTime:
                    cost = TSPJobManager.getAvgTaskCompletionTime(tsp_task.getTimeSubmission() + task_running_time);
                    break;
                case TaskRunningTime:
                    cost = TSPJobManager.getAvgTaskRunningTime(task_running_time);
                    break;
                case Energy:
                    cost = TSPJobManager.getEnergyConsumption(getNotMobileVmList());
                    break;
                case TaskEnergy:
                    PowerHost host = (PowerHost)vm.getHost();
                    cost = task_running_time * host.getPowerModel().getPower(vm.getMips()/(host).getTotalMips());
                    break;
                case TaskCompletionTimeAndEnergy:
                    double w1 = 0.5;
                    double w2 = 0.5;
                    cost = (tsp_task.getTimeSubmission() + task_running_time) * w1;
                    cost += TSPJobManager.getEnergyConsumption(getNotMobileVmList()) * w2;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid optimization parameter value");
            }
        }
        return - cost;
    }
}
