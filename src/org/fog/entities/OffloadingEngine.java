package org.fog.entities;

import java.util.List;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.offloading.OffloadingStrategy;
import org.workflowsim.Job;
import org.workflowsim.WorkflowEngine;

public class OffloadingEngine extends SimEntity{

	private List<Job> jobList;
	private List<FogDevice> fogDevices;
	/**
     * The workflow engine associated with it.
     */
	private WorkflowEngine wfEngine;
	
	/**
	 * Current time
	 */
	public double offloadingTime;
	
	/**
	 * The Energy of mobile
	 */
	public double TotalEnergy;
	
	public OffloadingStrategy offloadingStrategy;
	
	public OffloadingEngine(String name)throws Exception{
		super(name);
	}
	public OffloadingEngine(String name, OffloadingStrategy offloadingStrategy)throws Exception{
		super(name);
//		offloadingStrategy.setOffloadingEngine(this);
		this.offloadingStrategy = offloadingStrategy;
	}
	
	public void run(){
		// TODO Auto-generated method stub
	}
	
	public long run(List<Job> list, double DeadLine){
		setjobList(list);
		long startoffloading = System.currentTimeMillis();
		getOffloadingStrategy().BeforeOffloading(DeadLine);
		double sum = 0;
		for(Job job : list){
//			for(FileItem file : job.getFileList()){
//				if(file.getType() == FileType.INPUT)
//					sum += file.getSize() / 1000;
//			}
			sum += job.getCloudletLength();
		}
		System.out.println(sum);
		double time = 0;
		if(getOffloadingStrategy() != null){
			for(Job job : list){
				double deadline = DeadLine;
//				double deadline = job.getCloudletLength() / sum * DeadLine;// * 10;
				time += getOffloadingStrategy().SelectDatacenter(job, deadline);
			}
		}
		System.out.println("手机运行时间之和 = "+time);
		long endoffloading = System.currentTimeMillis();
		System.out.println("卸载策略运行时间 = " + (endoffloading - startoffloading));
		return endoffloading - startoffloading;
	}
	
	public void setfogDevices(List list){
		this.fogDevices = list;
		if(getOffloadingStrategy() != null){
			getOffloadingStrategy().setFogDeviceLists(list);
		}
	}
	
	public void setjobList(List list){
		this.jobList = list;
		if(getOffloadingStrategy() != null){
			getOffloadingStrategy().setjobList(list);
		}
	}
	
	public void setWorkflowEngine(WorkflowEngine workflowEngine){
		this.wfEngine = workflowEngine;
	}
	
	public WorkflowEngine getWorkflowEngine(){
		return wfEngine;
	}
	
	public OffloadingStrategy getOffloadingStrategy(){
		return this.offloadingStrategy;
	}

	public void setOffloadingStrategy(OffloadingStrategy offloadingStrategy) {
		this.offloadingStrategy = offloadingStrategy;
	}
	
	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		System.out.println(getName()+" is starting...");
	}

	@Override
	public void processEvent(SimEvent ev) {
		// TODO Auto-generated method stub
//		if(CloudSim.clock()>40)
//	    	System.out.println(CloudSim.clock()+":"+CloudSim.getEntityName(ev.getSource())+"-> OffloadingEngine = "+ev.getTag());
	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		System.out.println(getName()+" is shutting down...");
	}
}
