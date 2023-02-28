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
package org.workflowsim;

import org.cloudbus.cloudsim.core.CloudSim;

import java.util.*;
import java.util.regex.Pattern;

/**
 The Job concept in the TSP extension is very far from the one used in WorkflowSim.
 In this case, it refers to the restrictions for the execution of the tasks that are
 defined in the jobs dataset. This class helps to control the jobs execution
 *
 * @author Julio Corona
 * @since WorkflowSim Toolkit 1.0 's TSP extension
 * @date Jan 24, 2023
 */
public class TSPJobManager {

    private static Map<Integer, TSPJob> jobs = new HashMap<Integer, TSPJob>();

    public static void addTSPJob(Integer job_id, TSPJob tsp_job){
        jobs.put(job_id, tsp_job);
    }

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

    public static TSPJob getTSPJob(Integer job_id){
        return jobs.get(job_id);
    }

    public static int getTSPJobQuantity(){
        return jobs.size();
    }

    public static boolean canRunTask(Integer job_id, Integer task_id){
        return jobs.get(job_id).canRunTask(task_id);
    }


    // for task finishing control
    private static ArrayList<TSPTask> executing_task = new ArrayList<>();

    public static void addTaskRunning(TSPTask task){
        //restringing the execution for considering scheduling restrictions
        jobs.get(task.getJob_id()).addTasksRunning(task);

        //restringing the execution for the end of the scheduling restrictions
        executing_task.add(task);
    }

    public static void releaseFinishedTasks(double time){
        Stack<TSPTask> finished_tasks = new Stack<TSPTask>();

        for (TSPTask task: executing_task) {
            if (task.getTaskFinishTime() != -1 && task.getTaskFinishTime() >= time){
                jobs.get(task.getJob_id()).removeTasksRunning(task);
                finished_tasks.push(task);
            }
        }

        while (!finished_tasks.isEmpty()){
            executing_task.remove(finished_tasks.pop());
        }
    }

    private static double getNextFinishTime(){
        double next_finish_time = Double.MAX_VALUE;
        if (executing_task.size() > 0){
            for (int i=0; i < executing_task.size(); i++){
                if (executing_task.get(i).getTaskFinishTime() != -1 &&  executing_task.get(i).getTaskFinishTime() < next_finish_time){
                    next_finish_time = executing_task.get(i).getTaskFinishTime();
                }
            }
        }
        return next_finish_time;
    }

    private static ArrayList<Job> getAvailableJobs(List cloudletList, double clock){
        ArrayList<Job> jobs = new ArrayList<>();

        for (int i=0; i < cloudletList.size(); i++){
            Job job = (Job)cloudletList.get(i);
            TSPTask task_info = (TSPTask)job.getTaskList().get(0);
            if (task_info.getTimeSubmission() <= clock && canRunTask(task_info.getJob_id(), task_info.getTask_id())){
                jobs.add((Job)cloudletList.get(i));
            }
        }
        return jobs;
    }

    public static Object[] getNextAvailableJobs(List cloudletList, double clock){
        ArrayList<Job> next_available_jobs_to_income = getAvailableJobs(cloudletList, clock);

        if (!next_available_jobs_to_income.isEmpty()){
            return new Object[]{clock, next_available_jobs_to_income};
        }

        double next_finish_time =  getNextFinishTime();
        releaseFinishedTasks(next_finish_time);

        return getNextAvailableJobs(cloudletList, next_finish_time);
    }
}
