package org.fog.offloading;

import java.util.List;
import org.fog.entities.FogDevice;
import org.workflowsim.Job;

public class OffloadingStrategyAllinCloud extends OffloadingStrategy{
	
	
	public OffloadingStrategyAllinCloud(List<FogDevice> fogdevices) {
		super(fogdevices);
		// TODO Auto-generated constructor stub
	}
	public OffloadingStrategyAllinCloud() {
		
	}

	public double SelectDatacenter(Job job, double deadline) {
		// TODO Auto-generated method stub
		job.setoffloading(getcloud().getId());
		return 0;
	}
	
	@Override
	public void SelectDatacenter(Job job) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void BeforeOffloading(double deadline) {
		// TODO Auto-generated method stub
		
	}
}
