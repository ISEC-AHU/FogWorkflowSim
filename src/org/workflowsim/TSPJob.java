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
 The Job concept in the TSP extension is far from the one used in WorkflowSim.
 In this case, it refers to the restrictions for the execution of the tasks that are
 defined in the jobs dataset. This class represents a job.
 *
 * @author Julio Corona
 * @since WorkflowSim Toolkit 1.0 's TSP extension
 */
public class TSPJob {
    /**
     * Maximum number of tasks can be executed simultaneously
     */
    private int max_parallel_executable_tasks;

    /**
     * Task list can be executed simultaneously
     */
    private ArrayList<ArrayList<Integer>> tasks_which_can_run_in_parallel;

    /**
     * List of tasks that are running at a certain time
     */
    private LinkedList<TSPTask> tasks_running;

    /**
     * Quantity of tasks that are running at a certain time
     */
    public int tasks_running_quantity;

    /**
     * Creates a new entity
     * @param max_parallel_executable_tasks Maximum number of tasks can be executed simultaneously
     * @param tasks_which_can_run_in_parallel Task list can be executed simultaneously
     */
    public TSPJob(int max_parallel_executable_tasks, ArrayList<ArrayList<Integer>> tasks_which_can_run_in_parallel) {
        this.max_parallel_executable_tasks = max_parallel_executable_tasks;
        this.tasks_which_can_run_in_parallel = tasks_which_can_run_in_parallel;
        this.tasks_running = new LinkedList<>();
        this.tasks_running_quantity = 0;
    }

    /**
     * Add a task to the list of running tasks
     * @param task the task to be added
     */
    public void addTasksRunning(TSPTask task) {
        this.tasks_running.add(task);
        this.tasks_running_quantity+=1;
    }

    /**
     * Remove a task to the list of running tasks
     * @param task the task to be removed
     */
    public void removeTasksRunning(TSPTask task) {
        this.tasks_running.remove(task);
        this.tasks_running_quantity-=1;
    }

    /**
     * It determines if a task can be executed or not taking into account the number of tasks that are being executed
     * and the parallelism restrictions between them.
     *
     * @param id the id of the task to check if it can be executed
     * @return true in the task can be executed, else false
     */
    public boolean canRunTask(int id) {//[[0],[1,2],[3,4,5,8,11],[6,9,10],[7,17],[12],[13,15],[14,19],[16],[18],[20]]
        if (this.tasks_running_quantity >= this.max_parallel_executable_tasks) {
            return false;
        }

        for (ArrayList<Integer> tasks: this.tasks_which_can_run_in_parallel) {
            if (tasks.contains(id)){
                for (TSPTask task_running: this.tasks_running) {
                    if (!tasks.contains(task_running.getTaskId())){
                        return false;
                    }
                }
                return true;
            }
        }

        return true;
    }
}
