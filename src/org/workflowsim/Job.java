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
import java.util.List;

/**
 * Job is an extention to Task. It is basically a group of tasks. In
 * WorkflowSim, the ClusteringEngine merges tasks into jobs (group of tasks) and
 * the overall runtime of a job is the sum of the task runtime.
 *
 *
 * @author Weiwei Chen
 * @since WorkflowSim Toolkit 1.0
 * @date Apr 9, 2013
 */
public class Job extends Task {

    /*
     * The list of tasks a job has. It is the only difference between Job and Task. 
     */
    private List<Task> taskList;
    private double Processingcost;
    private int offloading;// 数字代表数据中心编号,-1表示未进行卸载决策
    private double Inputsize;//需要终端设备发送的输入文件大小
    private double Outputsize;//输出到终端设备的文件大小
    /**
     * by Fan
     */
    private double FileSize;

    /**
     * Allocates a new Job object. The job length should be greater than or
     * equal to 1.
     *
     * @param jobId the unique ID of this job
     * @param jobLength the length or size (in MI) of this task to be executed
     * in a PowerDatacenter
     * @pre jobId >= 0
     * @pre jobLength >= 0.0
     * @post $none
     */
    public Job(
            final int jobId,
            final long jobLength) {

        super(jobId, jobLength);
        this.taskList = new ArrayList<>();
        setProcessingCost(0.0);
        setoffloading(-1);//-1表示未进行卸载决策
        setInputsize(0);
        setOutputsize(0);
    }

    /**
     * Gets the list of tasks in this job
     *
     * @return the list of the tasks
     * @pre $none
     * @post $none
     */
    public List<Task> getTaskList() {
        return this.taskList;
    }

    /**
     * Sets the list of the tasks
     *
     * @param list, list of the tasks
     */
    public void setTaskList(List list) {
        this.taskList = list;
    }

    /**
     * Adds a task list to the existing task list
     *
     * @param list, task list to be added
     */
    public void addTaskList(List list) {
        this.taskList.addAll(list);
    }

    /**
     * Gets the list of the parent tasks and override its super function
     *
     * @return the list of the parents
     * @pre $none
     * @post $none
     */
    @Override
    public List getParentList() {
        return super.getParentList();
    }
    
    public double getProcessingCost(){
    	return Processingcost;
    }
    public void setProcessingCost(double cost){
    	this.Processingcost = cost;
    }
    public int getoffloading(){
    	return offloading;
    }
    public void setoffloading(int id){
    	this.offloading = id;
    }
    public double getInputsize(){
    	return Inputsize;
    }
    public void setInputsize(double size){
    	this.Inputsize = size;
    }
    public double getOutputsize(){
    	return Outputsize;
    }
    public void setOutputsize(double size){
    	this.Outputsize = size;
    }
    public double getFileSize(){
    	return FileSize;
    }
    public void setFileSize(double size){
    	this.FileSize = size;
    }
}
