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

import org.workflowsim.utils.Parameters.FileType;

import java.util.ArrayList;
import java.util.List;

/**
 * Since the Task class has many private attributes, this class extends from the Task class for TSP problems, adding the
 * new properties derived from the TSP scenario while keeping the rest of the Task code the same.
 *
 * @since TSP Extension 1.0
 * @author Julio Corona
 */
public class TSPTask extends Task {

    /**
     * The job id of this task
     */
    private int job_id;

    /**
     * The id of this task
     */
    private int task_id;

    /**
     * The amount of RAM needed by this task
     */
    private long ram;

    /**
     * The amount of Storage needed by this task
     */
    private long storage;

    /**
     * The arrival time of this task to the simulation
     */
    private long arrival_time;

    /**
     * The maximum time to execute this task
     */
    private long time_deadline_final;

    /**
     * The timestamp when the task started executing
     */

    private double time_start_processing;
    /**
     * Gets the job id of the task
     *
     * @return job id of the task
     */

    /**
     * time used by the agent for the scheduling/placement decision
     */

    private double decision_time;

    /**
     * Gets the decision time of the task
     *
     * @return job id of the task
     */

    public double getDecisionTime() {
        return decision_time;
    }

    /**
     * Sets the decision time of the task
     * @param decision_time amount of RAM needed by the task
     */
    public void setDecisionTime(double decision_time) {
        this.decision_time = decision_time;
    }


    /**
     * Gets the job id of the task
     *
     * @return job id of the task
     */
    public int getJobId() {
        return job_id;
    }

    /**
     * Gets the id of the task
     *
     * @return id of the task
     */
    public int getTaskId() {
        return task_id;
    }

    /**
     * Gets the millions of instructions number of the task
     *
     * @return the millions of instructions number of the task
     */
    public long getMi() {
        return runlength;
    }

    /**
     * Sets the millions of instructions number of the task
     *
     * @param mi the millions of instructions number
     */
    public void setMi(long mi) {
        this.runlength = mi;
    }

    /**
     * Gets the amount of RAM needed by the task
     *
     * @return the amount of RAM needed by the task
     */
    public long getRam() {
        return ram;
    }

    /**
     * Sets the amount of RAM needed by the task
     *
     * @param ram amount of RAM needed by the task
     */
    public void setRam(long ram) {
        this.ram = ram;
    }

    /**
     * Gets the amount of storage needed by the task
     *
     * @return the amount of storage needed by the task
     */
    public long getStorage() {
        return storage;
    }

    /**
     * Sets the amount of storage needed by the task
     *
     * @param storage amount of storage needed by the task
     */
    public void setStorage(long storage) {
        this.storage = storage;
    }

    /**
     * Gets the arrival time of this task
     *
     * @return the arrival time of this task
     */
    public long getArrivalTime() {
        return arrival_time;
    }

    /**
     * Sets the arrival time of this task
     *
     * @param arrival_time arrival time of this task
     */
    public void setArrivalTime(long arrival_time) {
        this.arrival_time = arrival_time;
    }

    /**
     * Gets the maximum time to execute this task
     *
     * @return the maximum time to execute this task
     */
    public long getTimeDeadlineFinal() {
        return time_deadline_final;
    }

    /**
     * Sets the maximum time to execute this task
     *
     * @param time_deadline_final maximum time to execute this task
     */
    public void setTimeDeadlineFinal(long time_deadline_final) {
        this.time_deadline_final = time_deadline_final;
    }

    /**
     * Creates a new entity
     * @param taskId the global id of the task
     * @param job_id job id of the task
     * @param task_id id of the task
     * @param mi millions of instructions number of the task
     * @param ram amount of RAM needed by the task
     * @param storage amount of storage needed by the task
     * @param arrival_time arrival time of this task
     * @param time_deadline_final maximum time to execute this task
     * @param priority priority of this task
     */

    public TSPTask(
            final int taskId,
            final int job_id,
            final int task_id,
            final long mi,
            final long ram,
            final long storage,
            final long arrival_time,
            final long time_deadline_final,
            final int priority) {

        super(taskId, mi);


        this.childList = new ArrayList<>();
        this.parentList = new ArrayList<>();
        this.fileList = new ArrayList<>();
        this.impact = 0.0;
        this.taskFinishTime = -1.0;

        this.runlength = mi;
        this.task_id = task_id;
        this.job_id=job_id;
        this.ram=ram;
        this.storage=storage;
        this.arrival_time = arrival_time;
        this.time_deadline_final=time_deadline_final;
        this.priority=priority;
    }


    /* So far the extension for TSP. From here on, the code specified in FogWorkFlowSim's Task is maintained */

    /*
     * The list of parent tasks.
     */
    private List<Task> parentList;
    /*
     * The list of child tasks.
     */
    private List<Task> childList;
    /*
     * The list of all files (input data and ouput data)
     */
    private List<FileItem> fileList;
    /*
     * The priority used for research. Not used in current version.
     */
    private int priority;
    /*
     * The depth of this task. Depth of a task is defined as the furthest path
     * from the root task to this task. It is set during the workflow parsing
     * stage.
     */
    private int depth;
    /*
     * The impact of a task. It is used in research.
     */
    private double impact;

    /*
     * The type of a task.
     */
    private String type;

    /**
     * The finish time of a task (Because cloudlet does not allow WorkflowSim to
     * update finish_time)
     */
    private double taskFinishTime;
    private long runlength;

    /**
     * Sets the type of the task
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the type of the task
     *
     * @return the type of the task
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the priority of the task
     *
     * @param priority the priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Sets the depth of the task
     *
     * @param depth the depth
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Gets the priority of the task
     *
     * @return the priority of the task
     * @pre $none
     * @post $none
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * Gets the depth of the task
     *
     * @return the depth of the task
     */
    public int getDepth() {
        return this.depth;
    }

    /**
     * Gets the child list of the task
     *
     * @return the list of the children
     */
    public List<Task> getChildList() {
        return this.childList;
    }

    /**
     * Sets the child list of the task
     *
     * @param list, child list of the task
     */
    public void setChildList(List<Task> list) {
        this.childList = list;
    }

    /**
     * Sets the parent list of the task
     *
     * @param list, parent list of the task
     */
    public void setParentList(List<Task> list) {
        this.parentList = list;
    }

    /**
     * Adds the list to existing child list
     *
     * @param list, the child list to be added
     */
    public void addChildList(List<Task> list) {
        this.childList.addAll(list);
    }

    /**
     * Adds the list to existing parent list
     *
     * @param list, the parent list to be added
     */
    public void addParentList(List<Task> list) {
        this.parentList.addAll(list);
    }

    /**
     * Gets the list of the parent tasks
     *
     * @return the list of the parents
     */
    public List<Task> getParentList() {
        return this.parentList;
    }

    /**
     * Adds a task to existing child list
     *
     * @param task, the child task to be added
     */
    public void addChild(Task task) {
        this.childList.add(task);
    }

    /**
     * Adds a task to existing parent list
     *
     * @param task, the parent task to be added
     */
    public void addParent(Task task) {
        this.parentList.add(task);
    }

    /**
     * Gets the list of the files
     *
     * @return the list of files
     * @pre $none
     * @post $none
     */
    public List<FileItem> getFileList() {
        return this.fileList;
    }

    /**
     * Adds a file to existing file list
     *
     * @param file, the file to be added
     */
    public void addFile(FileItem file) {
        this.fileList.add(file);
    }

    /**
     * Sets a file list
     *
     * @param list, the file list
     */
    public void setFileList(List<FileItem> list) {
        this.fileList = list;
    }

    /**
     * Sets the impact factor
     *
     * @param impact, the impact factor
     */
    public void setImpact(double impact) {
        this.impact = impact;
    }

    /**
     * Gets the impact of the task
     *
     * @return the impact of the task
     * @pre $none
     * @post $none
     */
    public double getImpact() {
        return this.impact;
    }

    /**
     * Sets the finish time of the task (different to the one used in Cloudlet)
     *
     * @param time finish time
     */
    public void setTaskFinishTime(double time) {
        this.taskFinishTime = time;
    }

    /**
     * Gets the finish time of a task (different to the one used in Cloudlet)
     *
     * @return
     */
    public double getTaskFinishTime() {
        return this.taskFinishTime;
    }
    
    public long initlength() {
    	super.setCloudletLength(runlength);
    	return runlength;
    }
    
    public List<FileItem> getInputFileList() {
    	List<FileItem> InputFileList = new ArrayList<FileItem>();
    	for(FileItem file : getFileList()){
    		if(file.getType() == FileType.INPUT)
    			InputFileList.add(file);
    	}
    	return InputFileList;
    }
    public List<FileItem> getOutputFileList() {
    	List<FileItem> OutputFileList = new ArrayList<FileItem>();
    	for(FileItem file : getFileList()){
    		if(file.getType() == FileType.OUTPUT)
    			OutputFileList.add(file);
    	}
    	return OutputFileList;
    }

    public double getTimeStartProcessing() {
        return time_start_processing;
    }

    public void setTimeStartProcessing(double time_start_processing) {
        this.time_start_processing = time_start_processing;
    }

    /**
     * Gets the total cost of processing or executing this task The original
     * getProcessingCost does not take cpu cost into it also the data file in
     * Task is stored in fileList <tt>Processing Cost = input data transfer +
     * processing cost + output transfer cost</tt> .
     *
     * @return the total cost of processing Cloudlet
     * @pre $none
     * @post $result >= 0.0
     */
/*    @Override
    public double getProcessingCost() {
        // cloudlet cost: execution cost...

        double cost = getCostPerSec() * getActualCPUTime();

        // ...plus input data transfer cost...
        long fileSize = 0;
        for (FileItem file : getFileList()) {
            fileSize += file.getSize() / Consts.MILLION;
        }
        cost += costPerBw * fileSize;
        return cost;
    }*/
}
