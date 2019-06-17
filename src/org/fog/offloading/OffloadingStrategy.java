package org.fog.offloading;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.fog.entities.FogDevice;
import org.fog.entities.OffloadingEngine;
import org.workflowsim.Job;

public abstract class OffloadingStrategy {

	private List<Job> jobList;
	private List<FogDevice> fogDevices;
	private OffloadingEngine offloadingEngine;
	
	/** The host list. */
	private List<? extends Host> hostList;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public OffloadingStrategy () {
	}
	
	public OffloadingStrategy (List<FogDevice> fogdevices) {
		this.fogDevices = fogdevices;
//		this.hostList = list;
	}
	
	public abstract void BeforeOffloading(double deadline);
	public abstract void SelectDatacenter(Job job);
	public abstract double SelectDatacenter(Job job, double deadline);
	
	public FogDevice getmobile(){
		for(FogDevice dev : getFogDeviceLists())
			if(dev.getName().contains("m"))
				return dev;
		return null;
	}
	
	public FogDevice getcloud(){
		for(FogDevice dev : getFogDeviceLists())
			if(dev.getName().equalsIgnoreCase("cloud"))
				return dev;
		return null;
	}
	
	public FogDevice getFogNode(){
		for(FogDevice dev : getFogDeviceLists())
			if(dev.getName().contains("f"))
				return dev;
		return null;
	}

	public void setFogDeviceLists(List list){
		this.fogDevices = list;
	}
	public List<FogDevice> getFogDeviceLists(){
		return fogDevices;
	}
	public void setjobList(List list){
		this.jobList = list;
	}
	public List<Job> getjobList(){
		return jobList;
	}
	public OffloadingEngine getOffloadingEngine(){
		return offloadingEngine;
	}
	public void setOffloadingEngine(OffloadingEngine offloadingEngine) {
		this.offloadingEngine = offloadingEngine;
	}
}
