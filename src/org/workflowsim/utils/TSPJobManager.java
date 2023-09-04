/**
 * Copyright 2012-2013 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.workflowsim.utils;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.fog.entities.FogDevice;
import org.fog.utils.FogLinearPowerModel;
import org.workflowsim.Job;
import org.workflowsim.TSPJob;
import org.workflowsim.TSPTask;

import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;

/**
 This class allows dealing with the task execution restrictions specified on the jobs
 *
 * @author Julio Corona
 * @since WorkflowSim Toolkit 1.0 's TSP extension
 */
public class TSPJobManager {

    /**
     * Dictionary to store the execution status of each job
     */
    private static Map<Integer, TSPJob> jobs;

    /**
     * Dictionary to store the quantity of task exceeding the deadline
     */
    private static Map<Integer, Integer> deadline_exceeded;

    /**
     * Auxiliary variable for storing the sum of the tasks' completion time
    */

    private static double total_task_completion_time;

    /**
     * Auxiliary variable for storing the sum of the tasks' completion time including the decision time
    */

    private static double total_task_completion_time_with_decision;

    public static ArrayList<Double> getTaskCompletionTimeAvgHistory() {
        return task_completion_time_avg_history;
    }

    public static double getTaskCompletionTimeAvg() {
        return task_completion_time_avg_history.get(task_completion_time_avg_history.size()-1);
    }

    /**
     * Auxiliary variable for storing the tasks' completion time average
    */

    private static ArrayList<Double> task_completion_time_avg_history;

    /**
     * Auxiliary variable for storing the sum of the tasks' running time
    */

    private static double total_task_running_time;

    /**
     * Auxiliary variable for storing quantity of tasks' completed
    */

    private static double quantity_task_completed;

    /**
     * Create a new job
     * @param job_id the job id
     * @param max_parallel_executable_tasks the maximum number of tasks can be executed simultaneously in this job
     * @param tasks_which_can_run_in_parallel the task list can be executed simultaneously in this job
     */

    public static void createTSPJob(Integer job_id, String max_parallel_executable_tasks,  String tasks_which_can_run_in_parallel){
        String[] task_id_list = tasks_which_can_run_in_parallel.substring(2, tasks_which_can_run_in_parallel.length() - 2).split(Pattern.quote("],["));

        ArrayList<ArrayList<Integer>> tasks_which_can_run_in_parallel_parsed = new ArrayList<ArrayList<Integer>>(task_id_list.length);
        for (int i=0; i<task_id_list.length; i++){
            String[] task = task_id_list[i].split(",");
            ArrayList<Integer> tasks_parsed = new ArrayList<Integer>(task.length);

            for (int j=0; j<task.length; j++){
                tasks_parsed.add(Integer.parseInt(task[j]));
            }
            tasks_which_can_run_in_parallel_parsed.add(tasks_parsed);
        }

        //creating the job
        TSPJob tsp_job = new TSPJob(Integer.parseInt(max_parallel_executable_tasks), tasks_which_can_run_in_parallel_parsed);

        jobs.put(job_id, tsp_job);
    }

    /**
     * Check if a task can be executed
     * @param job_id the task's job id
     * @param task_id the task id
     * @return true if it can be executed, false otherwise
     */
    public static boolean canRunTask(Integer job_id, Integer task_id){
        return jobs.get(job_id).canRunTask(task_id);
    }


    /**
     * List of all running tasks
     */
    private static ArrayList<TSPTask> executing_task;

    /**
     * Add a task to the list of running tasks
     * @param task the task to be added
     */

    public static void addTaskRunning(Cloudlet cloudlet, TSPTask task, double decision_time, double task_start_execution_timestamp){
        //restringing the execution for considering scheduling restrictions
        jobs.get(task.getJobId()).addTasksRunning(task);

        //restringing the execution for the end of the scheduling restrictions
        task.setDecisionTime(decision_time);
        cloudlet.setExecStartTime(task_start_execution_timestamp);
        task.setTimeStartProcessing(task_start_execution_timestamp);

        executing_task.add(task);
    }

    /**
     * Review the list of tasks that have already finished and delete them from the list of tasks in progress
     * @param time the simulation time
     */
    public static void releaseFinishedTasks(double time){
        Stack<TSPTask> finished_tasks = new Stack<TSPTask>();

        for (TSPTask task: executing_task) {
            if (task.getTaskFinishTime() != -1 && task.getTaskFinishTime() >= time){
                jobs.get(task.getJobId()).removeTasksRunning(task);
                finished_tasks.push(task);

                total_task_completion_time += task.getTaskFinishTime() - task.getArrivalTime();

                total_task_running_time += task.getTaskFinishTime() - task.getTimeStartProcessing();
                quantity_task_completed += 1;

                task_completion_time_avg_history.add(getAvgTaskCompletionTime());
//                task_completion_time_avg_history.add(task.getTaskFinishTime() - task.getArrivalTime() + task.getDecisionTime());

            }
        }

        while (!finished_tasks.isEmpty()){
            executing_task.remove(finished_tasks.pop());
        }
    }

    /**
     * Returns the average time to complete the tasks
     */

    static public double getAvgTaskCompletionTime(){
        return total_task_completion_time / quantity_task_completed;
    }

    /**
     * Returns the average time to complete the tasks, when the last task is going to be assigned
     */

    static public double getAvgTaskCompletionTime(double task_completion_time){
        return (total_task_completion_time + task_completion_time) / (quantity_task_completed + 1);
    }

    /**
     * Returns the average time to run the tasks
     */

    static public double getAvgTaskRunningTime(){
        return total_task_running_time / quantity_task_completed;
    }

    /**
     * Returns the average time to run the tasks, when the last task is going to be assigned
     */

    static public double getAvgTaskRunningTime(double task_running_time){
        return (total_task_running_time + task_running_time) / (quantity_task_completed + 1);
    }

    /**
     * Returns the next time when one of the tasks that are running will end
     */
    private static double getNextFinishTime(){
        double next_finish_time = Double.MAX_VALUE;
        if (executing_task.size() > 0){
            for (int i=0; i < executing_task.size(); i++){
                if (executing_task.get(i).getTaskFinishTime() != -1 &&  executing_task.get(i).getTaskFinishTime() < next_finish_time){
                    next_finish_time = executing_task.get(i).getTaskFinishTime();
                }
            }
            return next_finish_time;
        }
        return next_finish_time;
    }

    /**
     * Returns the next list of jobs that will be ready to be executed at a given clock time
     * @param cloudletList The list of jobs pending to be executed
     * @param clock the clock time
     * @return the list of jobs ready to be executed
     */
    private static ArrayList<Job> getAvailableJobs(List cloudletList, double clock){
        ArrayList<Job> jobs = new ArrayList<>();

        for (int i=0; i < cloudletList.size(); i++){
            Job job = (Job)cloudletList.get(i);
            TSPTask tsp_task = (TSPTask)job.getTaskList().get(0);
            if (tsp_task.getArrivalTime() <= clock  && (!Parameters.getConsiderTasksParallelismRestrictions() || TSPJobManager.canRunTask(tsp_task.getJobId(), tsp_task.getTaskId()))){
                jobs.add((Job)cloudletList.get(i));
            }
        }

        return jobs;
    }

    /**
     * Returns the next time when one of the pending jobs will arrive
     * @param cloudletList the list of pending jobs
     */
    private static double getFutureJobsTime(List cloudletList){
        double time = Double.MAX_VALUE;

        for (int i=0; i < cloudletList.size(); i++){
            Job job = (Job)cloudletList.get(i);
            TSPTask tsp_task = (TSPTask)job.getTaskList().get(0);
            if (tsp_task.getArrivalTime() <= time  && (!Parameters.getConsiderTasksParallelismRestrictions() || TSPJobManager.canRunTask(tsp_task.getJobId(), tsp_task.getTaskId()))){
                time = tsp_task.getArrivalTime();
            }
        }
        return time;
    }

    /**
     * Returns the next jobs that will be able to be executed either due to their submission time or due to simulation
     * restrictions
     * @param cloudletList the list of pending jobs
     * @param clock the clock time
     * @return the time of the next availability and the list of available jobs
     */
    public static Object[] getNextAvailableJobs(List cloudletList, double clock){
        ArrayList<Job> next_available_jobs_to_income = getAvailableJobs(cloudletList, clock);

        if (clock == Double.MAX_VALUE){
            return new Object[]{clock, new ArrayList<>()};
        }

        if (!next_available_jobs_to_income.isEmpty()){
            return new Object[]{clock, next_available_jobs_to_income};
        }

        double next_finish_time =  getNextFinishTime();
        double future_jobs_time = getFutureJobsTime(cloudletList);

        double new_time = Math.min(next_finish_time, future_jobs_time);

        releaseFinishedTasks(new_time);

        return getNextAvailableJobs(cloudletList, new_time);
    }

    /**
     * Count the tasks that exceeded its deadline
     * @param taskPriority the task priority
     */

    public static void registerTaskExceedingDeadline(int taskPriority){
        if (deadline_exceeded.containsKey(taskPriority)){
            deadline_exceeded.replace(taskPriority, deadline_exceeded.get(taskPriority) + 1);
        }else
        {
            deadline_exceeded.put(taskPriority, 1);
        }
    }

    public static int getTaskExceedingDeadlineQuantity(){
        int v=0;
        for ( Integer value : deadline_exceeded.values() ) {
            v+=value;
        }
        return v;
    }

    public static void printTaskExceededDeadlineQuantities(){
        Log.printLine("Exceeded deadlines by priorities:");
        for (Integer priority: deadline_exceeded.keySet()) {
            Log.printLine("Priority "+ priority + ": " + deadline_exceeded.get(priority));
        }
    }

    public static int getQuantityOfExceededDeadline(int priority){
        if (deadline_exceeded.containsKey(priority)){
            return deadline_exceeded.get(priority);
        }
        return 0;
    }

    /**
     * Auxiliary attribute for know the last executed task
     */
    public static int last_executed_task_no;


    /**
     * List of device's busy time
     */
    private static Map<Integer, Double> device_host_busy_time;

    /**
     * Clean the simulation auxiliary variables before each simulation
     */
    public static void initSimulationVariables(double myRealGatewayMIPS, double simulatedGatewayMIPS, List<FogDevice> fogDevices){

        //variables to be used in the simulation
        jobs = new HashMap<>();
        deadline_exceeded = new HashMap<>();
        total_task_completion_time = 0;
        total_task_completion_time_with_decision = 0;
        task_completion_time_avg_history = new ArrayList<>();
        total_task_running_time = 0;
        quantity_task_completed = 0;
        executing_task = new ArrayList<>();
        last_executed_task_no = 0;
        device_host_busy_time = new HashMap<>();
        gatewayBusyTimes = new ArrayList<>();

        //variables to be used in the simulation
        gateway_idle_energy_consumption = -1;
        gateway_busy_energy_consumption = -1;
        my_real_gateway_mips = -1;
        simulated_gateway_mips = -1;

        // init the device's busy time list
        for (FogDevice fogDevice: fogDevices) {
            for (Host host: fogDevice.getHostList()) {
                device_host_busy_time.put(host.getId(), 0.0);
            }
        }
    }

    /**
     * Update the device's busy time list
     * @param host_id the device's host id
     * @param time the task execution time
     */
    public static void updateDeviceBusyTime(int host_id, double time){
        device_host_busy_time.replace(host_id, device_host_busy_time.get(host_id) + time);
    }

    public static double getEnergyConsumption(List<Vm> vmList){
        double energy = 0;
        for (Vm vm: vmList){
            double busy_time = device_host_busy_time.get(vm.getHost().getId());
            PowerHost host = (PowerHost)vm.getHost();
            FogLinearPowerModel powerModel = (FogLinearPowerModel) host.getPowerModel();
            energy += busy_time * powerModel.getPower(vm.getMips()/host.getTotalMips());

            double idle_time = CloudSim.clock() - busy_time;
            energy += idle_time * powerModel.getStaticPower();
        }

        return energy;
    }

    /**
     * Auxiliary var to storing the Gateway utilization for its final consumption
     */
    private static ArrayList<Double[]> gatewayBusyTimes;

    public static void registerGatewayBusyTimes(double computation_time, double cpu_percent){
        gatewayBusyTimes.add(new Double[]{computation_time, cpu_percent});
    }

    private static double gateway_idle_energy_consumption;
    private static double gateway_busy_energy_consumption;

    public static double getGatewayEnergyConsumption(double simulationFinalClock, PowerHost host){
        gateway_busy_energy_consumption = 0;
        double busy_time = 0;

        FogLinearPowerModel powerModel = (FogLinearPowerModel) host.getPowerModel();

        for (Double[] busyTime: gatewayBusyTimes){
            double time = busyTime[0];
            double cpu_percent = busyTime[1];

            if (cpu_percent > 100){ //if there was cpu overclocking, will be considered only at 100%
                cpu_percent = 100;
            }

            busy_time+=time;
            gateway_busy_energy_consumption += time * powerModel.getPower(cpu_percent/100);
        }

        double idle_time = simulationFinalClock - busy_time;

        gateway_idle_energy_consumption = idle_time * powerModel.getStaticPower();

        return gateway_busy_energy_consumption + gateway_idle_energy_consumption;
    }

    public static void writeToTxtGatewayBusyTimes(String filePath){
        try
        {
            PrintWriter pr = new PrintWriter(filePath);

            for (Double[] busyTime: gatewayBusyTimes)
            {
                pr.println(busyTime[0]);
            }
            pr.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("No such file exists.");
        }
    }

    public static double getGatewayIdleEnergyConsumption() {
        return gateway_idle_energy_consumption;
    }

    public static double getGatewayBusyEnergyConsumption() {
        return gateway_busy_energy_consumption;
    }

    private static double my_real_gateway_mips;
    private static double simulated_gateway_mips;

    public static double parseComputationTime(double computation_time) {
        return computation_time * my_real_gateway_mips / simulated_gateway_mips;
    }
}
