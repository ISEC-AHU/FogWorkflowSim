package org.fog.entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Consts;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.utils.Config;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.workflowsim.CondorVM;
import org.workflowsim.FileItem;
import org.workflowsim.Job;
import org.workflowsim.Task;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.Parameters.ClassType;
import org.workflowsim.utils.Parameters.FileType;
import org.workflowsim.utils.ReplicaCatalog;

public class FogDevice extends PowerDatacenter {
	
	protected double lockTime;
	
	/**	
	 * ID of the parent Fog Device
	 */
	protected int parentId;
	
	/**
	 * ID of the Controller
	 */
	protected int controllerId;
	/**
	 * IDs of the children Fog devices
	 */
	protected List<Integer> childrenIds;
	
	protected double uplinkBandwidth;
	protected double downlinkBandwidth;
	protected double uplinkLatency;
	
	protected double energyConsumption;
	protected double lastUtilizationUpdateTime;
	protected double lastUtilization;
	
	public double ExecutionTime;
	private int level;
	
	/**
	 * the cost of the computing resource
	 */
	protected double ratePerMips;
	
	protected double totalCost;
	
	public FogDevice(
			String name, 
			FogDeviceCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval,
			double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
		setCharacteristics(characteristics);
		setVmAllocationPolicy(vmAllocationPolicy);
		setLastProcessTime(0.0);
		setStorageList(storageList);
		setVmList(new ArrayList<Vm>());
		setSchedulingInterval(schedulingInterval);
		setUplinkBandwidth(uplinkBandwidth);
		setDownlinkBandwidth(downlinkBandwidth);
		setUplinkLatency(uplinkLatency);
		setRatePerMips(ratePerMips);
		for (Host host : getCharacteristics().getHostList()) {
			host.setDatacenter(this);
		}
		// If this resource doesn't have any PEs then no useful at all
		if (getCharacteristics().getNumberOfPes() == 0) {
			throw new Exception(super.getName()
					+ " : Error - this entity has no PEs. Therefore, can't process any Cloudlets.");
		}
		// stores id of this class
		getCharacteristics().setId(super.getId());
		
		setChildrenIds(new ArrayList<Integer>());
		
		this.lockTime = 0;
		this.lastUtilization = 0;
		setEnergyConsumption(0.0);
		setExecutionTime(0.0);
		setTotalCost(0);
	}

	public FogDevice(
			String name, long mips, int ram, 
			double uplinkBandwidth, double downlinkBandwidth, double ratePerMips, PowerModel powerModel) throws Exception {
		super(name, null, null, new LinkedList<Storage>(), 0);
		
		List<Pe> peList = new ArrayList<Pe>();

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

		int hostId = FogUtils.generateEntityId();
		long storage = 1000000; // host storage
		int bw = 10000;
		double costPerMips = 0.1;

		PowerHost host = new PowerHost(
				hostId,
				costPerMips,
				new RamProvisionerSimple(ram),
				new BwProvisionerOverbooking(bw),
				storage,
				peList,
				new VmSchedulerTimeShared(peList),
				powerModel
			);

		List<Host> hostList = new ArrayList<Host>();
		hostList.add(host);

		setVmAllocationPolicy(new VmAllocationPolicySimple(hostList));
		
		String arch = Config.FOG_DEVICE_ARCH; 
		String os = Config.FOG_DEVICE_OS; 
		String vmm = Config.FOG_DEVICE_VMM;
		double time_zone = Config.FOG_DEVICE_TIMEZONE;
		double cost = Config.FOG_DEVICE_COST; 
		double costPerMem = Config.FOG_DEVICE_COST_PER_MEMORY;
		double costPerStorage = Config.FOG_DEVICE_COST_PER_STORAGE;
		double costPerBw = Config.FOG_DEVICE_COST_PER_BW;

		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
				arch, os, vmm, host, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		setCharacteristics(characteristics);
		
		setLastProcessTime(0.0);
		setVmList(new ArrayList<Vm>());
		setUplinkBandwidth(uplinkBandwidth);
		setDownlinkBandwidth(downlinkBandwidth);
		setUplinkLatency(uplinkLatency);
		for (Host host1 : getCharacteristics().getHostList()) {
			host1.setDatacenter(this);
		}
		if (getCharacteristics().getNumberOfPes() == 0) {
			throw new Exception(super.getName()
					+ " : Error - this entity has no PEs. Therefore, can't process any Cloudlets.");
		}
		
		getCharacteristics().setId(super.getId());
		setChildrenIds(new ArrayList<Integer>());
		
		this.lockTime = 0;
		this.lastUtilization = 0;
		setEnergyConsumption(0.0);
		setExecutionTime(0.0);
		setTotalCost(0);
	}
	
	/**
	 * Overrides this method when making a new and different type of resource. <br>
	 * <b>NOTE:</b> You do not need to override {@link #body()} method, if you use this method.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void registerOtherEntity() {
		
	}
	@Override
	protected void processOtherEvent(SimEvent ev) {
		switch(ev.getTag()){
		case FogEvents.RELEASE_OPERATOR:
			processOperatorRelease(ev);
			break;
		case FogEvents.RESOURCE_MGMT:
			manageResources(ev);
		default:
			break;
		}
	}
	
	/**
	 * Perform miscellaneous resource management tasks
	 * @param ev
	 */
	private void manageResources(SimEvent ev) {
		//updateEnergyConsumption();
		//send(getId(), Config.RESOURCE_MGMT_INTERVAL, FogEvents.RESOURCE_MGMT);
	}
	
	
    @Override
    protected void updateCloudletProcessing() {
        // if some time passed since last processing
        // R: for term is to allow loop at simulation start. Otherwise, one initial
        // simulation step is skipped and schedulers are not properly initialized
        //this is a bug of CloudSim if the runtime is smaller than 0.1 (now is 0.01) it doesn't work at all
    	if (CloudSim.clock() < 0.111 || CloudSim.clock() > getLastProcessTime() + 0.01) {
            List<? extends Host> list = getVmAllocationPolicy().getHostList();
            double smallerTime = Double.MAX_VALUE;
            // for each host...
            for (Host host : list) {
                // inform VMs to update processing
                double time = host.updateVmsProcessing(CloudSim.clock());
                // what time do we expect that the next cloudlet will finish?
                if (time < smallerTime) {
                    smallerTime = time;
                }
            }
            // gurantees a minimal interval before scheduling the event
            if (smallerTime < CloudSim.clock() + 0.11) {
                smallerTime = CloudSim.clock() + 0.11;
            }
            if (smallerTime != Double.MAX_VALUE) {
                schedule(getId(), (smallerTime - CloudSim.clock()), CloudSimTags.VM_DATACENTER_EVENT);
            }
            setLastProcessTime(CloudSim.clock());
        }
    }
	
	/**
	 * Update cloudet processing without scheduling future events.
	 * 
	 * @return the double
	 */
	protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
		double currentTime = CloudSim.clock();
		double minTime = Double.MAX_VALUE;
		double timeDiff = currentTime - getLastProcessTime();
		double timeFrameDatacenterEnergy = 0.0;

		for (PowerHost host : this.<PowerHost> getHostList()) {
			Log.printLine();

			double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
			if (time < minTime) {
				minTime = time;
			}

			Log.formatLine(
					"%.2f: [Host #%d] utilization is %.2f%%",
					currentTime,
					host.getId(),
					host.getUtilizationOfCpu() * 100);
		}

		if (timeDiff > 0) {
			Log.formatLine(
					"\nEnergy consumption for the last time frame from %.2f to %.2f:",
					getLastProcessTime(),
					currentTime);

			for (PowerHost host : this.<PowerHost> getHostList()) {
				double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
				double utilizationOfCpu = host.getUtilizationOfCpu();
				double timeFrameHostEnergy = host.getEnergyLinearInterpolation(
						previousUtilizationOfCpu,
						utilizationOfCpu,
						timeDiff);
				timeFrameDatacenterEnergy += timeFrameHostEnergy;

				Log.printLine();
				Log.formatLine(
						"%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%",
						currentTime,
						host.getId(),
						getLastProcessTime(),
						previousUtilizationOfCpu * 100,
						utilizationOfCpu * 100);
				Log.formatLine(
						"%.2f: [Host #%d] energy is %.2f W*sec",
						currentTime,
						host.getId(),
						timeFrameHostEnergy);
			}

			Log.formatLine(
					"\n%.2f: Data center's energy is %.2f W*sec\n",
					currentTime,
					timeFrameDatacenterEnergy);
		}

		setPower(getPower() + timeFrameDatacenterEnergy);

		checkCloudletCompletion();

		/** Remove completed VMs **/
		/**
		 * Change made by HARSHIT GUPTA
		 */
		/*for (PowerHost host : this.<PowerHost> getHostList()) {
			for (Vm vm : host.getCompletedVms()) {
				getVmAllocationPolicy().deallocateHostForVm(vm);
				getVmList().remove(vm);
				Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
			}
		}*/
		
		Log.printLine();

		setLastProcessTime(currentTime);
		return minTime;
	}


	protected void checkCloudletCompletion() {  //有修改
		//boolean cloudletCompleted = false;
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			for (Vm vm : host.getVmList()) {
				while (vm.getCloudletScheduler().isFinishedCloudlets()) {
					Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
					if (cl != null) {
						sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
						register(cl);
					}
				}
			}
		}
		//if(cloudletCompleted)
		//	updateAllocatedMips(null);
	}
	
	private void register(Cloudlet cl) {
        Task tl = (Task) cl;
        List<FileItem> fList = tl.getFileList();
        for (FileItem file : fList) {
            if (file.getType() == FileType.OUTPUT)//output file
            {
                switch (ReplicaCatalog.getFileSystem()) {
                    case SHARED:
                        ReplicaCatalog.addFileToStorage(file.getName(), this.getName());
                        break;
                    case LOCAL:
                        int vmId = cl.getVmId();
                        int userId = cl.getUserId();
                        Host host = getVmAllocationPolicy().getHost(vmId, userId);
                        /**
                         * Left here for future work
                         */
                        CondorVM vm = (CondorVM) host.getVm(vmId, userId);
                        ReplicaCatalog.addFileToStorage(file.getName(), Integer.toString(vmId));
                        break;
                }
            }
        }
    }

	protected int getChildIdWithRouteTo(int targetDeviceId){
		for(Integer childId : getChildrenIds()){
			if(targetDeviceId == childId)
				return childId;
			if(((FogDevice)CloudSim.getEntity(childId)).getChildIdWithRouteTo(targetDeviceId) != -1)
				return childId;
		}
		return -1;
	}
	
/*	public void updateEnergyConsumption() {
		double totalMipsAllocated = 0;
		double Energy = 0;//getEnergyConsumption();
		double ExecutionTime = 0;
		double Cost = 0;
		//System.out.println(getName()+"能耗:"+Energy);
		List<PowerHost> hostlist = getHostList();
		for(PowerHost host : hostlist){
			//double newenergy = host.updateEnergyConsumption();
			if(getName().contains("m")){//计算手机能耗
				//Energy += newenergy;
				ExecutionTime += host.ExecutionTime;
			}
			else {//计算服务器费用
				Cost += host.updateCost();
			}
		}

		double timeNow = CloudSim.clock();
		//System.out.println("---------------------------------------------time now: "+timeNow);
		//double currentEnergyConsumption = getEnergyConsumption();
		double usetime=getExecutionTime()+timeNow-lastUtilizationUpdateTime;  //求该设备执行时间之和
		//double newEnergyConsumption = currentEnergyConsumption + (timeNow-lastUtilizationUpdateTime)*getHost().getPowerModel().getPower(lastUtilization);
		//setEnergyConsumption(Energy);
		setExecutionTime(ExecutionTime);
	
		if(getName().equals("d-0")){
			System.out.println("------------------------");
			System.out.println("Utilization = "+lastUtilization);
			System.out.println("Power = "+getHost().getPowerModel().getPower(lastUtilization));
			System.out.println(timeNow-lastUtilizationUpdateTime);
		}
		
		double currentCost = getTotalCost();
		double newcost = currentCost + (timeNow-lastUtilizationUpdateTime)*getRatePerMips()*lastUtilization*getHost().getTotalMips();
		//setTotalCost(Cost);
		
		
		lastUtilization = Math.min(1, totalMipsAllocated/getHost().getTotalMips());
		lastUtilizationUpdateTime = timeNow;
	}*/

    /**
     * Processes a Cloudlet submission. The cloudlet is actually a job which can
     * be cast to org.workflowsim.Job
     *
     * @param ev a SimEvent object
     * @param ack an acknowledgement
     * @pre ev != null
     * @post $none
     */
    @Override
    protected void processCloudletSubmit(SimEvent ev, boolean ack) {
        updateCloudletProcessing();
       
        try {
            /**
             * cl is actually a job but it is not necessary to cast it to a job
             */
            Job job = (Job) ev.getData();
           // System.out.println(job.getCloudletId());

            if (job.isFinished()) {
                String name = CloudSim.getEntityName(job.getUserId());
                Log.printLine(getName() + ": Warning - Cloudlet #" + job.getCloudletId() + " owned by " + name
                        + " is already completed/finished.");
                Log.printLine("Therefore, it is not being executed again");
                Log.printLine();

                // NOTE: If a Cloudlet has finished, then it won't be processed.
                // So, if ack is required, this method sends back a result.
                // If ack is not required, this method don't send back a result.
                // Hence, this might cause CloudSim to be hanged since waiting
                // for this Cloudlet back.
                if (ack) {
                    int[] data = new int[3];
                    data[0] = getId();
                    data[1] = job.getCloudletId();
                    data[2] = CloudSimTags.FALSE;

                    // unique tag = operation tag
                    int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
                    sendNow(job.getUserId(), tag, data);
                }
                sendNow(job.getUserId(), CloudSimTags.CLOUDLET_RETURN, job);

                return;
            }

            int userId = job.getUserId();
            int vmId = job.getVmId();
            Host host = getVmAllocationPolicy().getHost(vmId, userId);
//            System.out.println("host.id:"+host.getId()+" vmId:"+vmId);
            CondorVM vm = (CondorVM) host.getVm(vmId, userId);

            switch (Parameters.getCostModel()) {
                case DATACENTER:
                    // process this Cloudlet to this CloudResource
                    job.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(),
                            getCharacteristics().getCostPerBw());
                    break;
                case VM:
                    job.setResourceParameter(getId(), vm.getCost(), vm.getCostPerBW());
                    break;
                default:
                    break;
            }

            /**
             * Stage-in file && Shared based on the file.system
             */
            if (job.getClassType() == ClassType.STAGE_IN.value) {
                stageInFile2FileSystem(job);
            }

            /**
             * Add data transfer time (communication cost)
             */
            double fileTransferTime = 0.0;
            if (job.getClassType() == ClassType.COMPUTE.value) {
                fileTransferTime = processDataStageInForComputeJob(job.getFileList(), job);
            }

            CloudletScheduler scheduler = vm.getCloudletScheduler();
            double estimatedFinishTime = scheduler.cloudletSubmit(job, fileTransferTime);
            updateTaskExecTime(job, vm);

            // if this cloudlet is in the exec queue
            if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
                send(getId(), estimatedFinishTime, CloudSimTags.VM_DATACENTER_EVENT);
            } else {
                //Log.printLine("Warning: You schedule cloudlet to a busy VM");
            }

            if (ack) {
                int[] data = new int[3];
                data[0] = getId();
                data[1] = job.getCloudletId();
                data[2] = CloudSimTags.TRUE;

                int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
                sendNow(job.getUserId(), tag, data);
            }
        } catch (ClassCastException c) {
            Log.printLine(getName() + ".processCloudletSubmit(): " + "ClassCastException error.");
        } catch (Exception e) {
            Log.printLine(getName() + ".processCloudletSubmit(): " + "Exception error.");
            e.printStackTrace();
        }
        checkCloudletCompletion();
    }
    
    /**
     * Update the submission time/exec time of a job
     *
     * @param job
     * @param vm
     */
    private void updateTaskExecTime(Job job, Vm vm) {
        double start_time = job.getExecStartTime();
        for (Task task : job.getTaskList()) {
            task.setExecStartTime(start_time);
            double task_runtime = task.getCloudletLength() / vm.getMips();
            start_time += task_runtime;
            //Because CloudSim would not let us update end time here
            task.setTaskFinishTime(start_time);
        }
    }

    /**
     * Stage in files for a stage-in job. For a local file system (such as
     * condor-io) add files to the local storage; For a shared file system (such
     * as NFS) add files to the shared storage
     *
     * @param cl, the job
     * @pre $none
     * @post $none
     */
    private void stageInFile2FileSystem(Job job) {
        List<FileItem> fList = job.getFileList();

        for (FileItem file : fList) {
            switch (ReplicaCatalog.getFileSystem()) {
                /**
                 * For local file system, add it to local storage (data center
                 * name)
                 */
                case LOCAL:
                    ReplicaCatalog.addFileToStorage(file.getName(), this.getName());
                    /**
                     * Is it not really needed currently but it is left for
                     * future usage
                     */
                    //ClusterStorage storage = (ClusterStorage) getStorageList().get(0);
                    //storage.addFile(file);
                    break;
                /**
                 * For shared file system, add it to the shared storage
                 */
                case SHARED:
                    ReplicaCatalog.addFileToStorage(file.getName(), this.getName());
                    break;
                default:
                    break;
            }
        }
    }

    /*
     * Stage in for a single job (both stage-in job and compute job)
     * @param requiredFiles, all files to be stage-in
     * @param job, the job to be processed
     * @pre  $none
     * @post $none
     */
    protected double processDataStageInForComputeJob(List<FileItem> requiredFiles, Job job) throws Exception {
        double time = 0.0;
        Controller controller = (Controller)CloudSim.getEntity(controllerId);
        for (FileItem file : requiredFiles) {
            //The input file is not an output File 
            if (file.isRealInputFile(requiredFiles)) {
                double maxBwth = 0.0;
				List siteList = ReplicaCatalog.getStorageList(file.getName());
                if (siteList.isEmpty()) {
                    throw new Exception(file.getName() + " does not exist");
                }
                switch (ReplicaCatalog.getFileSystem()) {
                    case SHARED: //进入share
                        //stage-in job
                        /**
                         * Picks up the site that is closest
                         */
                        /*double maxRate = Double.MIN_VALUE;
                        for (Storage storage : getStorageList()) {
                            double rate = storage.getMaxTransferRate();
                            if (rate > maxRate) {
                                maxRate = rate;
                            }
                        }
                        //Storage storage = getStorageList().get(0);
                        
                        time += file.getSize() / (double) Consts.MILLION / maxRate;*/
                        if(getId() == controller.getcloud().getId())
                        	time += file.getSize() / controller.parameter / controller.WAN_Bandwidth;
                        else if(getId() == controller.getFogNode().getId())
                        	time += file.getSize() / controller.parameter / controller.LAN_Bandwidth;
                        break;
                    case LOCAL:
                        int vmId = job.getVmId();
                        int userId = job.getUserId();
                        Host host = getVmAllocationPolicy().getHost(vmId, userId);
                        Vm vm = host.getVm(vmId, userId);

                        boolean requiredFileStagein = true;
                        for (Iterator it = siteList.iterator(); it.hasNext();) {
                            //site is where one replica of this data is located at
                            String site = (String) it.next();
                            if (site.equals(this.getName())) {
                                continue;
                            }
                            /**
                             * This file is already in the local vm and thus it
                             * is no need to transfer
                             */
                            if (site.equals(Integer.toString(vmId))) {
                                requiredFileStagein = false;
                                break;
                            }
                            double bwth;
                            if (site.equals(Parameters.SOURCE)) {
                                //transfers from the source to the VM is limited to the VM bw only
                                bwth = vm.getBw();
                                //bwth = dcStorage.getBaseBandwidth();
                            } else {
                                //transfers between two VMs is limited to both VMs
                                bwth = Math.min(vm.getBw(), getVmAllocationPolicy().getHost(Integer.parseInt(site), userId).getVm(Integer.parseInt(site), userId).getBw());
                                //bwth = dcStorage.getBandwidth(Integer.parseInt(site), vmId);
                            }
                            if (bwth > maxBwth) {
                                maxBwth = bwth;
                            }
                        }
                        if (requiredFileStagein && maxBwth > 0.0) {
                            time += file.getSize() / (double) Consts.MILLION / maxBwth;
                        }

                        /**
                         * For the case when storage is too small it is not
                         * handled here
                         */
                        //We should add but since CondorVm has a small capability it often fails
                        //We currently don't use this storage to do anything meaningful. It is left for future. 
                        //condorVm.addLocalFile(file);
                        ReplicaCatalog.addFileToStorage(file.getName(), Integer.toString(vmId));
                        break;
                }
            }
        }
        if(getName().contains("m"))//如果该雾设备是手机，则不考虑文件传输时间
			return 0;
		else
			return time;
    }

	protected void processOperatorRelease(SimEvent ev){
		this.processVmMigrate(ev, false);
	}
	
	public PowerHost getHost(){
		return (PowerHost) getHostList().get(0);
	}
	
	public PowerHost getHost(int i){
		return (PowerHost) getHostList().get(i);
	}

	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public List<Integer> getChildrenIds() {
		return childrenIds;
	}
	public void setChildrenIds(List<Integer> childrenIds) {
		this.childrenIds = childrenIds;
	}
	public double getUplinkBandwidth() {
		return uplinkBandwidth;
	}
	public void setUplinkBandwidth(double uplinkBandwidth) {
		this.uplinkBandwidth = uplinkBandwidth;
	}
	public double getUplinkLatency() {
		return uplinkLatency;
	}
	public void setUplinkLatency(double uplinkLatency) {
		this.uplinkLatency = uplinkLatency;
	}
	public int getControllerId() {
		return controllerId;
	}
	public void setControllerId(int controllerId) {
		this.controllerId = controllerId;
	}
	
	public double getDownlinkBandwidth() {
		return downlinkBandwidth;
	}

	public void setDownlinkBandwidth(double downlinkBandwidth) {
		this.downlinkBandwidth = downlinkBandwidth;
	}
	
	public double getEnergyConsumption() {
		return energyConsumption;
	}

	public void setEnergyConsumption(double energyConsumption) {
		this.energyConsumption = energyConsumption;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public double getRatePerMips() {
		return ratePerMips;
	}

	public void setRatePerMips(double ratePerMips) {
		this.ratePerMips = ratePerMips;
	}
	public double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}
	
	public double getExecutionTime() {
		return ExecutionTime;
	}

	public void setExecutionTime(double ExecutionTime) {
		this.ExecutionTime = ExecutionTime;
	}

	public void clear() throws Throwable {
		// TODO Auto-generated method stub
		this.finalize();
		this.ExecutionTime=0;
		this.getVmList().clear();
		this.getHostList().clear();
	}
	
	protected void clearVmProcessing(SimEvent ev) {
		// TODO Auto-generated method stub
		setLastProcessTime(0.1);
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		for (Host host : list) {
            double time = host.updateVmsProcessing(CloudSim.clock());
		}
	}

	public void clearConsumption() {
		setEnergyConsumption(0);
		setTotalCost(0);
		setExecutionTime(0.0);
		List<PowerHost> hostlist = getHostList();
		for(PowerHost host : hostlist){
			host.ExecutionTime=0;
			host.IdleTime=0;
			host.energyConsumption=0.0;
			List<CondorVM> vmlist = host.getVmList();
			for(CondorVM vm : vmlist){
				vm.IdleTime=0.0;
				vm.ExecutionTime=0.0;
			}
		}
	}
	
	public double getAverageMips(){
		double total = 0;
		for(Host host : getHostList())
			total += host.getTotalMips();
		return total / getHostList().size();
	}
}