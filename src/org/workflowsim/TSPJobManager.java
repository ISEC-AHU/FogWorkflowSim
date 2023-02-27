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

    public static void addTaskRunning(Integer job_id, Integer task_id){
        jobs.get(job_id).addTasksRunning(task_id);
    }

    public static void removeTaskRunning(Integer job_id, Integer task_id){
        jobs.get(job_id).removeTasksRunning(task_id);
    }

    public static boolean canRunTask(Integer job_id, Integer task_id){
        return jobs.get(job_id).canRunTask(task_id);
    }




}
