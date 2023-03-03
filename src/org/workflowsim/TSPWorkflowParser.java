/**
 * TSP: to be defined
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

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.TSPJobManager;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Since the WorkflowParser is defined as "final" this class replaces the WorkflowParser class for TSP problems.
 * This extension preserves the original code and documents the differences starting with "TSP modification".
 *
 * @since TSP Extension 1.0
 * @author Julio Corona
 */
public final class TSPWorkflowParser {

    /**
     * The path to DAX file.
     */
    private final String daxPath;
    /**
     * The path to DAX files.
     */
    private final List<String> daxPaths;
    /**
     * All tasks.
     */
    private List<Task> taskList;
    /**
     * User id. used to create a new task.
     */
    private final int userId;

    /**
     * current job id. In case multiple workflow submission
     */
    private int jobIdStartsFrom;

    /**
     * Gets the task list
     *
     * @return the task list
     */
    @SuppressWarnings("unchecked")
    public List<Task> getTaskList() {
        return taskList;
    }

    /**
     * Sets the task list
     *
     * @param taskList the task list
     */
    protected void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }
    /**
     * Map from task name to task.
     */
    protected Map<String, Task> mName2Task;

    /**
     * Initialize a WorkflowParser
     *
     * @param userId the user id. Currently we have just checked single user
     * mode
     */
    public TSPWorkflowParser(int userId) {
        this.userId = userId;
        this.mName2Task = new HashMap<>();
        this.daxPath = Parameters.getDaxPath();
        this.daxPaths = Parameters.getDAXPaths();
        this.jobIdStartsFrom = 1;

        setTaskList(new ArrayList<>());
    }

    /**
     * Start to parse a workflow which is a xml file(s).
     */
    public void parse() {
        if (this.daxPath != null) {
            parseCsvFile(this.daxPath);
        } else if (this.daxPaths != null) {
            for (String path : this.daxPaths) {
                parseCsvFile(path);
            }
        }
    }

    /**
     * Sets the depth of a task
     *
     * @param task the task
     * @param depth the depth
     */
    private void setDepth(Task task, int depth) {
        if (depth > task.getDepth()) {
            task.setDepth(depth);
        }
        for (Task cTask : task.getChildList()) {
            setDepth(cTask, task.getDepth() + 1);
        }
    }

    /**
     * TSP modification: This method replaces the "parseXmlFile" method to read the tasks
     * @param path the path where are the job.csv and tasks.csv files
     */
    public void parseCsvFile(String path){
        try (CSVReader job_reader = new CSVReader(new FileReader(path + "/jobs.csv"))) {
            CSVReader task_reader = new CSVReader(new FileReader(path + "/tasks.csv"));

            //skip the headers
            job_reader.readNext();
            task_reader.readNext();

            //reading job information
            String[] job_info;

            int taskId=0;

            while ((job_info = job_reader.readNext()) != null) {
                int job_id = Integer.parseInt(job_info[1]);

                //creating the TSPJob
                TSPJobManager.createTSPJob(job_id, job_info[18], job_info[20]);
                long job_time_submission = Long.parseLong(job_info[4]);

                //reading job's tasks
                String[] task_info = task_reader.peek();

                int task_job_id = Integer.parseInt(task_info[2]);

                while (task_job_id == job_id){

                    task_job_id = Integer.parseInt(task_info[2]);
                    int task_id = Integer.parseInt(task_info[1]);
                    long mi = Long.parseLong(task_info[3]) * (long)Parameters.getRuntimeScale();
                    long ram = Long.parseLong(task_info[4]);
                    long storage = Long.parseLong(task_info[5]);
                    long time_submission = job_time_submission + Long.parseLong(task_info[7]);
                    long time_deadline_final = Long.parseLong(task_info[9]);
                    int priority_no = Integer.parseInt(task_info[17]);

                    //task creation
                    TSPTask task = new TSPTask(taskId, task_job_id, task_id, mi, ram, storage, time_submission, time_deadline_final, priority_no);
                    task.setUserId(userId);

                    //searching the position for keeping the submission order
                    int i=0;
                    while (i < this.getTaskList().size() && task.getTimeSubmission() > ((TSPTask)this.getTaskList().get(i)).getTimeSubmission()){
                        i++;
                    }

                    this.getTaskList().add(i, task);

                    //going to th next task
                    task_info = task_reader.readNext();

                    taskId+=1;
                    if (task_info == null){
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
    }
}
