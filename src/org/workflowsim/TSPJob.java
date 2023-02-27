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

import java.util.ArrayList;
import java.util.LinkedList;

/**
 The Job concept in the TSP extension is very far from the one used in WorkflowSim.
 In this case, it refers to the restrictions for the execution of the tasks that are
 defined in the jobs dataset. This class represents a job.
 *
 * @author Julio Corona
 * @since WorkflowSim Toolkit 1.0 's TSP extension
 * @date Jan 24, 2023
 */
public class TSPJob {
    /*
     * The list of tasks a job has. It is the only difference between Job and Task.
     */
    private int max_parallel_executable_tasks;
    private ArrayList<ArrayList<Integer>> tasks_which_can_run_in_parallel;

    public LinkedList<Integer> tasks_running;
    public int tasks_running_quantity;

    public TSPJob(int max_parallel_executable_tasks, ArrayList<ArrayList<Integer>> tasks_which_can_run_in_parallel) {
        this.max_parallel_executable_tasks = max_parallel_executable_tasks;
        this.tasks_which_can_run_in_parallel = tasks_which_can_run_in_parallel;
        this.tasks_running = new LinkedList<Integer>();
        this.tasks_running_quantity = 0;
    }

    public int getMaxParallelExecutableTasks() {
        return max_parallel_executable_tasks;
    }

    public void setMaxParallelExecutableTasks(int max_parallel_executable_tasks) {
        this.max_parallel_executable_tasks = max_parallel_executable_tasks;
    }

    public ArrayList<ArrayList<Integer>> getTasksWhichCanRunInParallel() {
        return tasks_which_can_run_in_parallel;
    }

    public void setTasksWhichCanRunInParallel(ArrayList<ArrayList<Integer>> tasks_which_can_run_in_parallel) {
        this.tasks_which_can_run_in_parallel = tasks_which_can_run_in_parallel;
    }

    public LinkedList<Integer> getTasks_running() {
        return tasks_running;
    }

    public void addTasksRunning(int id) {
        this.tasks_running.add(id);
        this.tasks_running_quantity+=1;
    }

    public void removeTasksRunning(int id) {
        this.tasks_running.remove(id);
        this.tasks_running_quantity-=1;
    }

    public boolean checkTasksRunning(int id) {
        return this.tasks_running.contains(id);
    }

    public boolean canRunTask(int id) {//[[0],[1,2],[3,4,5,8,11],[6,9,10],[7,17],[12],[13,15],[14,19],[16],[18],[20]]
        if (this.tasks_running_quantity >= this.max_parallel_executable_tasks) {
            return false;
        }

        for (ArrayList<Integer> tasks: this.tasks_which_can_run_in_parallel) {
            if (tasks.contains(id)){
                for (Integer task_running_id: this.tasks_running) {
                    if (!tasks.contains(task_running_id)){
                        return false;
                    }
                }
                return true;
            }
        }

        return true;
    }
}
