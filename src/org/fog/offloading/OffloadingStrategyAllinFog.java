package org.fog.offloading;

import java.util.List;
import org.fog.entities.FogDevice;
import org.workflowsim.Job;

public class OffloadingStrategyAllinFog extends OffloadingStrategy{
	
	
	public OffloadingStrategyAllinFog(List<FogDevice> fogdevices) {
		super(fogdevices);
		// TODO Auto-generated constructor stub
	}
	public OffloadingStrategyAllinFog() {
		
	}

	public double SelectDatacenter(Job job, double deadline) {
		// TODO Auto-generated method stub
		job.setoffloading(getFogNode().getId());
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
