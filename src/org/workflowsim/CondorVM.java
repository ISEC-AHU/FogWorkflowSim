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

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerVm;

/**
 * Condor Vm extends a VM: the difference is it has a locl storage system and it
 * has a state to indicate whether it is busy or not
 *
 * @author Weiwei Chen
 * @since WorkflowSim Toolkit 1.0
 * @date Apr 9, 2013
 */
public class CondorVM extends PowerVm {

    /*
     * The state of a vm. It should be either WorkflowSimTags.VM_STATUS_IDLE
     * or VM_STATUS_READY (not used in workflowsim) or VM_STATUS_BUSY
     */
    private int state;

    /**
     * the cost of using memory in this resource
     */
    private double costPerMem = 0.0;

    /**
     * the cost of using bandwidth in this resource
     */
    private double costPerBW = 0.0;

    /**
     * the cost of using storage in this resource
     */
    private double costPerStorage = 0.0;

    /**
     * the cost of using CPU in this resource
     */
    private double cost = 0.0;
    
	protected double energyConsumption = 0.0;
	protected double lastUtilizationUpdateTime = 0.0;
	protected double lastIdleTime = 0.0;
	public double ExecutionTime = 0.0;
	public double IdleTime = 0.0;
	public boolean flag; //状态是否变更的标志

    /**
     * Creates a new CondorVM object.
     *
     * @param id unique ID of the VM
     * @param userId ID of the VM's owner
     * @param mips the mips
     * @param numberOfPes amount of CPUs
     * @param ram amount of ram
     * @param bw amount of bandwidth
     * @param size amount of storage
     * @param vmm virtual machine monitor
     * @param cloudletScheduler cloudletScheduler policy for cloudlets
     * @pre id >= 0
     * @pre userId >= 0
     * @pre size > 0
     * @pre ram > 0
     * @pre bw > 0
     * @pre cpus > 0
     * @pre priority >= 0
     * @pre cloudletScheduler != null
     * @post $none
     */
    public CondorVM(
            int id,
            int userId,
            double mips,
            int numberOfPes,
            int ram,
            long bw,
            long size,
            String vmm,
            CloudletScheduler cloudletScheduler) {
        super(id, userId, mips, numberOfPes, ram, bw, size, 0, vmm, cloudletScheduler, 0);
        /*
         * At the beginning all vm status is idle. 
         */
        setState(WorkflowSimTags.VM_STATUS_IDLE);
    }

    /**
     * Creates a new CondorVM object.
     *
     * @param id unique ID of the VM
     * @param userId ID of the VM's owner
     * @param mips the mips
     * @param numberOfPes amount of CPUs
     * @param ram amount of ram
     * @param bw amount of bandwidth
     * @param size amount of storage
     * @param vmm virtual machine monitor
     * @param cost cost for CPU
     * @param costPerBW cost for using bandwidth
     * @param costPerStorage cost for using storage
     * @param costPerMem cost for using memory
     * @param cloudletScheduler cloudletScheduler policy for cloudlets
     * @pre id >= 0
     * @pre userId >= 0
     * @pre size > 0
     * @pre ram > 0
     * @pre bw > 0
     * @pre cpus > 0
     * @pre priority >= 0
     * @pre cloudletScheduler != null
     * @post $none
     */
    public CondorVM(
            int id,
            int userId,
            double mips,
            int numberOfPes,
            int ram,
            long bw,
            long size,
            String vmm,
            double cost,
            double costPerMem,
            double costPerStorage,
            double costPerBW,
            CloudletScheduler cloudletScheduler) {
        this(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
        this.cost = cost;
        this.costPerBW = costPerBW;
        this.costPerMem = costPerMem;
        this.costPerStorage = costPerStorage;
    }
    
    public double lastUtilizationUpdateTime(){
    	if(getState() == WorkflowSimTags.VM_STATUS_IDLE)
    		return 0;
		return ExecutionTime;
    }

    /**
     * Gets the CPU cost
     *
     * @return the cost
     */
    public double getCost() {
        return this.cost;
    }

    /**
     * Gets the cost per bw
     *
     * @return the costPerBW
     */
    public double getCostPerBW() {
        return this.costPerBW;
    }

    /**
     * Gets the cost per storage
     *
     * @return the costPerStorage
     */
    public double getCostPerStorage() {
        return this.costPerStorage;
    }

    /**
     * Gets the cost per memory
     *
     * @return the costPerMem
     */
    public double getCostPerMem() {
        return this.costPerMem;
    }

    /**
     * Sets the state of the task
     *
     * @param tag
     */
    public final void setState(int tag) {
        this.state = tag;
//        double timenow = CloudSim.clock();
//        double idletime = 0.0 , executiontime = 0.0;
//        flag = true;
//        if(timenow !=0 ){//&& getHost().getDatacenter().getName().contains("m")){
//        	//System.out.println("检测手机上虚拟机是空闲还是负载状态");
//        	//System.out.println("timenow "+timenow+" Vm #"+getId()); 
//        	if(tag == WorkflowSimTags.VM_STATUS_IDLE){
//        		 setlastUtilizationUpdateTime(timenow);
//        		 executiontime = getExecutionTime() + timenow - getlastIdleTime();
//        		 setExecutionTime(executiontime);
//        		 //System.out.println("此时状态更新为空闲，执行时间更新为："+executiontime);
//        	 }
//        	 else if(tag == WorkflowSimTags.VM_STATUS_BUSY){
//        		 setlastIdleTime(timenow);
//        		 idletime = getIdleTime() + timenow - getlastUtilizationUpdateTime();
//        		 setIdleTime(idletime);
//        		 //System.out.println("此时状态更新为负载，空闲时间更新为："+idletime);
//        	 }
//        }
    }

    /**
     * Gets the state of the task
     *
     * @return the state of the task
     * @pre $none
     * @post $none
     */
    public final int getState() {
        return this.state;
    }
    
	public double getlastUtilizationUpdateTime() {
		return lastUtilizationUpdateTime;
	}

	public void setlastUtilizationUpdateTime(double lastUtilizationUpdateTime) {
		this.lastUtilizationUpdateTime = lastUtilizationUpdateTime;
	}
	
	public double getlastIdleTime() {
		return lastIdleTime;
	}

	public void setlastIdleTime(double lastIdleTime) {
		this.lastIdleTime = lastIdleTime;
	}
    
	public double getExecutionTime() {
		return ExecutionTime;
	}

	public void setExecutionTime(double ExecutionTime) {
		this.ExecutionTime = ExecutionTime;
	}
	
	public double getIdleTime() {
		return IdleTime;
	}

	public void setIdleTime(double IdleTime) {
		this.IdleTime = IdleTime;
	}
	
	public double getEnergyConsumption() {
		return energyConsumption;
	}

	public void setEnergyConsumption(double energyConsumption) {
		this.energyConsumption = energyConsumption;
	}
}
