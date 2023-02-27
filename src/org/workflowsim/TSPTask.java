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

import org.workflowsim.utils.Parameters.FileType;

import java.util.ArrayList;
import java.util.List;

/**
 * Task is an extention to Cloudlet in CloudSim. It supports the implementation
 * of dependencies between tasks, which includes a list of parent tasks and a
 * list of child tasks that it has. In WorkflowSim, the Workflow Engine assure
 * that a task is released to the scheduler (ready to run) when all of its
 * parent tasks have completed successfully
 *
 * @author Weiwei Chen
 * @since WorkflowSim Toolkit 1.0
 * @date Apr 9, 2013
 */
public class TSPTask extends Task {



    /*
     * The list of TSP tasks properties
     */
    private int job_id;
    private int task_id; //taskId
    private long mi;    //taskLength
    private long ram;
    private long storage;
    private long time_submission;
    private long time_deadline_final;

    public int getJob_id() {
        return job_id;
    }

    public void setJob_id(int job_id) {
        this.job_id = job_id;
    }

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public long getMi() {
        return mi;
    }

    public void setMi(long mi) {
        this.mi = mi;
    }

    public long getRam() {
        return ram;
    }

    public void setRam(long ram) {
        this.ram = ram;
    }

    public long getStorage() {
        return storage;
    }

    public void setStorage(long storage) {
        this.storage = storage;
    }

    public long getTimeSubmission() {
        return time_submission;
    }

    public void setTimeSubmission(long time_submission) {
        this.time_submission = time_submission;
    }

    public long getTimeDeadlineFinal() {
        return time_deadline_final;
    }

    public void setTimeDeadlineFinal(long time_deadline_final) {
        this.time_deadline_final = time_deadline_final;
    }

    public int getPriorityNo() {
        return priority_no;
    }

    public void setPriorityNo(int priority_no) {
        this.priority_no = priority_no;
    }

    public long getTimeSp() {
        return time_sp;
    }

    public void setTimeSp(long time_sp) {
        this.time_sp = time_sp;
    }

    private int priority_no;
    private long time_sp;

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
     * Allocates a new Task object. The task length should be greater than or
     * equal to 1.
     *
     * @param taskId the unique ID of this Task
     * @param taskLength the length or size (in MI) of this task to be executed
     * in a PowerDatacenter
     * @pre taskId >= 0
     * @pre taskLength >= 0.0
     * @post $none
     */
    public TSPTask(
            final int taskId,
            final long taskLength) {
        /**
         * We do not use cloudletFileSize and cloudletOutputSize here. We have
         * added a list to task and thus we don't need a cloudletFileSize or
         * cloudletOutputSize here The utilizationModelCpu, utilizationModelRam,
         * and utilizationModelBw are just set to be the default mode. You can
         * change it for your own purpose.
         */
        super(taskId, taskLength);

        this.runlength = taskLength;
        this.childList = new ArrayList<>();
        this.parentList = new ArrayList<>();
        this.fileList = new ArrayList<>();
        this.impact = 0.0;
        this.taskFinishTime = -1.0;
    }

    public TSPTask(
            final int taskId,
            final int job_id,
            final int task_id,
            final long mi,
            final long ram,
            final long storage,
            final long time_submission,
            final long time_deadline_final,
            final int priority_no) {
        /**
         * We do not use cloudletFileSize and cloudletOutputSize here. We have
         * added a list to task and thus we don't need a cloudletFileSize or
         * cloudletOutputSize here The utilizationModelCpu, utilizationModelRam,
         * and utilizationModelBw are just set to be the default mode. You can
         * change it for your own purpose.
         */
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
        this.time_submission=time_submission;
        this.time_deadline_final=time_deadline_final;
        this.priority_no=priority_no;
    }

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
