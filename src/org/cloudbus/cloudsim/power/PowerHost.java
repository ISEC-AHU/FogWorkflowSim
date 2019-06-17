/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;

import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.fog.entities.FogDevice;
import org.fog.utils.FogLinearPowerModel;
import org.workflowsim.CondorVM;

/**
 * PowerHost class enables simulation of power-aware hosts.
 * 
 * If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PowerHost extends HostDynamicWorkload {

	/** The power model. */
	private PowerModel powerModel;
	
	protected double lastUtilizationUpdateTime;
	protected double lastUtilization;
	protected double costPerMips;
	public double energyConsumption;
	public double ExecutionTime;
	public double IdleTime;

	/**
	 * Instantiates a new host.
	 * 
	 * @param id the id
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param storage the storage
	 * @param peList the pe list
	 * @param vmScheduler the VM scheduler
	 */
	public PowerHost(
			int id,
			double cost,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler,
			PowerModel powerModel) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		setPowerModel(powerModel);
		costPerMips = cost;
		energyConsumption = 0.0;
		ExecutionTime = 0.0;
		IdleTime = 0.0;
		lastUtilization = 0.0;
		lastUtilizationUpdateTime = 0.0;
	}

	/**
	 * Gets the power. For this moment only consumed by all PEs.
	 * 
	 * @return the power
	 */
	public double getPower() {
		return getPower(getUtilizationOfCpu());
	}

	/**
	 * Gets the power. For this moment only consumed by all PEs.
	 * 
	 * @param utilization the utilization
	 * @return the power
	 */
	protected double getPower(double utilization) {
		double power = 0;
		try {
			power = getPowerModel().getPower(utilization);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}

	/**
	 * Gets the max power that can be consumed by the host.
	 * 
	 * @return the max power
	 */
	public double getMaxPower() {
		double power = 0;
		try {
			power = getPowerModel().getPower(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}

	/**
	 * Gets the energy consumption using linear interpolation of the utilization change.
	 * 
	 * @param fromUtilization the from utilization
	 * @param toUtilization the to utilization
	 * @param time the time
	 * @return the energy
	 */
	public double getEnergyLinearInterpolation(double fromUtilization, double toUtilization, double time) {
		if (fromUtilization == 0) {
			return 0;
		}
		double fromPower = getPower(fromUtilization);
		double toPower = getPower(toUtilization);
		return (fromPower + (toPower - fromPower) / 2) * time;
	}

	/**
	 * Sets the power model.
	 * 
	 * @param powerModel the new power model
	 */
	protected void setPowerModel(PowerModel powerModel) {
		this.powerModel = powerModel;
	}

	/**
	 * Gets the power model.
	 * 
	 * @return the power model
	 */
	public PowerModel getPowerModel() {
		return powerModel;
	}
	
	public double getcostPerMips() {
		return costPerMips;
	}
	
	public double updateEnergyConsumption() {
		double totalMipsAllocated = 0;
		//ExecutionTime = 0.0;
		IdleTime = 0.0;
		//System.out.println("host:"+getId()+"更新能耗");
		List<CondorVM> vmlist = ishost(getVmList());
		for(final CondorVM vm : vmlist){
			vm.updateVmProcessing(CloudSim.clock(),vm.getHost().getVmScheduler().getAllocatedMipsForVm(vm));
			totalMipsAllocated += vm.getHost().getTotalAllocatedMipsForVm(vm);
			if(vm.flag == true){//若vm state更新
				ExecutionTime += vm.ExecutionTime;
				IdleTime += vm.IdleTime;
				vm.flag = false;
				System.out.println("执行时间："+ExecutionTime);
				System.out.println("空闲时间："+IdleTime);
			}
		}
		FogLinearPowerModel powerModel = (FogLinearPowerModel) getPowerModel();
		lastUtilization = Math.min(1, totalMipsAllocated/getTotalMips());
		energyConsumption = ExecutionTime * powerModel.getPower(lastUtilization)
				     + IdleTime * powerModel.getStaticPower();//负载能耗+空闲能耗
		
		return energyConsumption;
	}
	public double updateCost(){
		FogDevice device = (FogDevice) getDatacenter();
		double cost = getTotalMips() * ExecutionTime * device.getRatePerMips();
		System.out.println(" "+getTotalMips() +" "+ ExecutionTime +" "+ device.getRatePerMips());
		return cost;
	}

	private  List<CondorVM> ishost (List<PowerVm> vmList){
		List<CondorVM> vmList2 = new ArrayList<CondorVM>();
		for(PowerVm vm : vmList){
			if(this == vm.getHost())
				vmList2.add((CondorVM)vm);
		}
		return vmList2;
	}
}
