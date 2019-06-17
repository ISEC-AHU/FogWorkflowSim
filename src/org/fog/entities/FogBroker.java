package org.fog.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.workflowsim.CondorVM;
import org.workflowsim.Job;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.WorkflowSimTags;
import org.workflowsim.failure.FailureGenerator;
import org.workflowsim.scheduling.BaseSchedulingAlgorithm;
import org.workflowsim.scheduling.DataAwareSchedulingAlgorithm;
import org.workflowsim.scheduling.FCFSSchedulingAlgorithm;
import org.workflowsim.scheduling.GASchedulingAlgorithm;
import org.workflowsim.scheduling.MCTSchedulingAlgorithm;
import org.workflowsim.scheduling.MaxMinSchedulingAlgorithm;
import org.workflowsim.scheduling.MinMinSchedulingAlgorithm;
import org.workflowsim.scheduling.PsoScheduling;
import org.workflowsim.scheduling.RoundRobinSchedulingAlgorithm;
import org.workflowsim.scheduling.StaticSchedulingAlgorithm;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.Parameters.SchedulingAlgorithm;

public class FogBroker extends PowerDatacenterBroker{
	
    /**
     * The workflow engine id associated with this workflow algorithm.
     */
    private int workflowEngineId;
    /**
     * the start time of algorithm
     */
    public long startTime;
    public static int count=0;//初始化时，计数当前根据哪个粒子来为job分配虚拟机
    public static int count2=0;//更新粒子时，计数当前根据哪个粒子来为job分配虚拟机
    public static int initIndexForGA=0;
    public static int tempChildrenIndex=0;
    private static List<CondorVM> scheduledVmList;
    /**
     * Created a new WorkflowScheduler object.
     *
     * @param name name to be associated with this entity (as required by
     * Sim_entity class from simjava package)
     * @throws Exception the exception
     * @pre name != null
     * @post $none
     */
	public FogBroker(String name) throws Exception {
		super(name);
		scheduledVmList = new ArrayList<CondorVM>();
	}

    /**
     * Binds this scheduler to a datacenter
     *
     * @param datacenterId data center id
     */
    public void bindSchedulerDatacenter(int datacenterId) {
        if (datacenterId <= 0) {
            Log.printLine("Error in data center id");
            return;
        }
        this.datacenterIdsList.add(datacenterId);
    }

    /**
     * Sets the workflow engine id
     *
     * @param workflowEngineId the workflow engine id
     */
    public void setWorkflowEngineId(int workflowEngineId) {
    	
        this.workflowEngineId = workflowEngineId;
    }

    /**
     * Process an event
     *
     * @param ev a simEvent obj
     */
    @Override
    public void processEvent(SimEvent ev) {
//    	if(CloudSim.clock()>40)
//    	System.out.println(CloudSim.clock()+":"+CloudSim.getEntityName(ev.getSource())+"-> FogScheduler = "+ev.getTag());
        switch (ev.getTag()) {
            // Resource characteristics request
            case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
                processResourceCharacteristicsRequest(ev);
                break;
            // Resource characteristics answer
            case CloudSimTags.RESOURCE_CHARACTERISTICS:
                processResourceCharacteristics(ev);
                break;
            // VM Creation answer
            case CloudSimTags.VM_CREATE_ACK:
                processVmCreate(ev);
                break;
            // A finished cloudlet returned
            case WorkflowSimTags.CLOUDLET_CHECK:
                processCloudletReturn(ev);
                break;
            case CloudSimTags.CLOUDLET_RETURN:
                processCloudletReturn(ev);
                break;
            case CloudSimTags.END_OF_SIMULATION:
                shutdownEntity();
                break;
            case CloudSimTags.CLOUDLET_SUBMIT:
                processCloudletSubmit(ev);
                break;
            case CloudSimTags.CLEAR:
            	clearVmProcessing(ev);
                break;
            case CloudSimTags.CLEARCONSUMPTION:
            	clearConsumption(ev);
                break;
            case WorkflowSimTags.CLOUDLET_UPDATE:
            	switch (Parameters.getSchedulingAlgorithm()) {
				case PSO:
					if(WorkflowEngine.updateFlag==0&&WorkflowEngine.startlastSchedule==0) {
						processCloudletUpdateForPSOInit(ev);
	            	}else if(WorkflowEngine.startlastSchedule==0){
	            		processCloudletUpdateForPSOUpdate(ev);
					}else {
						processCloudletUpdateForPSOGbest(ev);
					}
				  break;
				case GA:
					if(WorkflowEngine.gaFlag==0&&WorkflowEngine.findBestSchedule==0) {
						processCloudletUpdateForGAInit(ev);
					}else if(WorkflowEngine.gaFlag==1&&WorkflowEngine.findBestSchedule==0){
						processCloudletUpdateForGA(ev);
					}
					if(WorkflowEngine.findBestSchedule==1)
						processCloudletUpdateForGABest(ev);
					
					break;
				case MINMIN:
				case MAXMIN:
				case FCFS:
				case MCT:
				case STATIC:
				case DATA:
				case ROUNDROBIN:
					processCloudletUpdate(ev);
					break;

				default:
					break;
				}
            break;	
            default:
                processOtherEvent(ev);
                break;
        }
    }

    private void clearConsumption(SimEvent ev) {
		// TODO Auto-generated method stub
    	for(int datacenterId:this.datacenterIdsList) {
    		schedule(datacenterId, 0, CloudSimTags.CLEARCONSUMPTION, null);
    	}
		
	}

	private void clearVmProcessing(SimEvent ev) {
    	for(int datacenterId:this.datacenterIdsList) {
    		schedule(datacenterId, 0, CloudSimTags.CLEAR, null);
    	}
    	//schedule(2, 0, CloudSimTags.CLEAR, null);
    	//schedule(3, 0, CloudSimTags.CLEAR, null);
    	//schedule(4, 0, CloudSimTags.CLEAR, null);
		
	}

	/**
     * Switch between multiple schedulers. Based on algorithm.method
     *
     * @param name the SchedulingAlgorithm name
     * @return the algorithm that extends BaseSchedulingAlgorithm
     */
    private BaseSchedulingAlgorithm getScheduler(SchedulingAlgorithm name) {
        BaseSchedulingAlgorithm algorithm;

        // choose which algorithm to use. Make sure you have add related enum in
        //Parameters.java
        switch (name) {
            //by default it is Static
            case FCFS:
                algorithm = new FCFSSchedulingAlgorithm();
                break;
            case MINMIN:
                algorithm = new MinMinSchedulingAlgorithm();
                break;
            case MAXMIN:
                algorithm = new MaxMinSchedulingAlgorithm();
                break;
            case MCT:
                algorithm = new MCTSchedulingAlgorithm();
                break;
            case DATA:
                algorithm = new DataAwareSchedulingAlgorithm();
                break;
            case STATIC:
                algorithm = new StaticSchedulingAlgorithm();
                break;
            case ROUNDROBIN:
                algorithm = new RoundRobinSchedulingAlgorithm();
                break;
            default:
                algorithm = new StaticSchedulingAlgorithm();
                break;

        }
        return algorithm;
    }

    /**
     * Process the ack received due to a request for VM creation.
     *
     * @param ev a SimEvent object
     * @pre ev != null
     * @post $none
     */
    @Override
    protected void processVmCreate(SimEvent ev) {
        int[] data = (int[]) ev.getData();
        int datacenterId = data[0];
        int vmId = data[1];
        int result = data[2];

        if (result == CloudSimTags.TRUE) {
            getVmsToDatacentersMap().put(vmId, datacenterId);
            /**
             * Fix a bug of cloudsim Don't add a null to getVmsCreatedList()
             * June 15, 2013
             */
            if (VmList.getById(getVmList(), vmId) != null) {
                getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
                Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
                        + " has been created in Datacenter #" + datacenterId + ", Host #"
                        + VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
            }
        } else {
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
                    + " failed in Datacenter #" + datacenterId);
        }

        incrementVmsAcks();

        // all the requested VMs have been created
        if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
            submitCloudlets();
        } else {
            // all the acks received, but some VMs were not created
            if (getVmsRequested() == getVmsAcks()) {
                // find id of the next datacenter that has not been tried
                for (int nextDatacenterId : getDatacenterIdsList()) {
                    if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
                        createVmsInDatacenter(nextDatacenterId);
                        return;
                    }
                }

                // all datacenters already queried
                if (getVmsCreatedList().size() > 0) { // if some vm were created
                    submitCloudlets();
                } else { // no vms created. abort
                    Log.printLine(CloudSim.clock() + ": " + getName()
                            + ": none of the required VMs could be created. Aborting");
                    finishExecution();
                }
            }
        }
    }

    /**
     * Update a cloudlet (job)
     *
     * @param ev a simEvent object
     */
    protected void processCloudletUpdate(SimEvent ev) {

        BaseSchedulingAlgorithm scheduler = getScheduler(Parameters.getSchedulingAlgorithm());
        scheduler.setCloudletList(getCloudletList());
        List<? extends Vm> vmlist = getVmsCreatedList();
        Collections.reverse(vmlist);
        scheduler.setVmList(vmlist);

        try {
            scheduler.run();
        } catch (Exception e) {
            Log.printLine("Error in configuring scheduler_method");
            e.printStackTrace();
        }

        WorkflowEngine wfEngine = (WorkflowEngine) CloudSim.getEntity(workflowEngineId);
        Controller controller = wfEngine.getController();
        List<Cloudlet> scheduledList = scheduler.getScheduledList();
        for (Cloudlet cloudlet : scheduledList) {
            int vmId = cloudlet.getVmId();
            double delay = 0.0;
            if (Parameters.getOverheadParams().getQueueDelay() != null) {
                delay = Parameters.getOverheadParams().getQueueDelay(cloudlet);
            }
//            Job job = (Job) cloudlet;
//            if(job.getoffloading() != controller.getmobile().getId()){
//            	if(job.getoffloading() != controller.getcloud().getId())
//            		delay = job.getInputsize() / controller.parameter / controller.WAN_Bandwidth;
//            	else
//            		delay = job.getInputsize() / controller.parameter / controller.LAN_Bandwidth;
//            }
//            System.out.println(cloudlet.getCloudletId()+".submit delay : "+delay);
            schedule(getVmsToDatacentersMap().get(vmId), delay, CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
        }
        getCloudletList().removeAll(scheduledList);
        getCloudletSubmittedList().addAll(scheduledList);
        cloudletsSubmitted += scheduledList.size();
    }

    /**
     * Update a cloudlet (job)
     * 每接收到一个任务以后，调用此方法，设置提交到调度机上的任务，以及调度机绑定的虚拟机，并为cloudlets分配虚拟机
     *每处理完一个任务以后，调用此方法，更新提交到调度机上的任务，以及调度机绑定的虚拟机
     * @param ev a simEvent object
     */
    protected void processCloudletUpdateForPSOInit(SimEvent ev) {
    	List<Cloudlet> cloudletList=getCloudletList();
    	List<CondorVM> vmList=getVmsCreatedList();
    	if(PsoScheduling.initFlag==0) {
    		startTime = System.currentTimeMillis();
    		WorkflowEngine engine = (WorkflowEngine)CloudSim.getEntity(workflowEngineId);
    		PsoScheduling.init(engine.jobList.size(),getVmList().size());
    	}
    	List<Cloudlet> scheduledList =new ArrayList<Cloudlet>();
    	List<int[]> schedules=PsoScheduling.schedules;
    	for(int i=0;i<cloudletList.size();i++) {
    		int cloudletId=cloudletList.get(i).getCloudletId();
    		int vmId=schedules.get(count)[cloudletId];
    		cloudletList.get(i).setVmId(vmId);
    		//setVmState(vmId);
    		scheduledList.add(cloudletList.get(i));
    	}
    	for (Cloudlet cloudlet : scheduledList) {
            int vmId = cloudlet.getVmId();
            double delay = 0.0;
            if (Parameters.getOverheadParams().getQueueDelay() != null) {
                delay = Parameters.getOverheadParams().getQueueDelay(cloudlet);
            }
           // System.out.println("delay:"+delay);
           // System.out.println("FogBroker.processCloudletUpdateForPSOInit提交给"+getVmsToDatacentersMap().get(vmId)+"号数据中心"+vmId+"号虚拟机的任务："+cloudlet.getCloudletId());
            
            schedule(getVmsToDatacentersMap().get(vmId), delay, CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
        }
        //把Cloudlets交由数据中心处理以后，从CloudletList中移除这些任务，并向CloudletSubmittedList中添加这些任务
        getCloudletList().removeAll(scheduledList);
        getCloudletSubmittedList().addAll(scheduledList);
        cloudletsSubmitted += scheduledList.size();
    }

    protected void processCloudletUpdateForPSOUpdate(SimEvent ev) {
    	List<Cloudlet> cloudletList=getCloudletList();
    	List<CondorVM> vmList=getVmsCreatedList();
    	if(WorkflowEngine.updateFlag2==1&&cloudletList.size()!=0) {
    		PsoScheduling.updateParticles();
    	}
    	List<Cloudlet> scheduledList =new ArrayList<Cloudlet>();
    	List<int[]> newSchedules=PsoScheduling.newSchedules;
    	for(int i=0;i<cloudletList.size();i++) {
    		int cloudletId=cloudletList.get(i).getCloudletId();
    		int vmId=newSchedules.get(count2)[cloudletId];
    		cloudletList.get(i).setVmId(vmId);
    		//setVmState(vmId);
    		scheduledList.add(cloudletList.get(i));
    	}
    	for (Cloudlet cloudlet : scheduledList) {
            int vmId = cloudlet.getVmId();
            double delay = 0.0;
            if (Parameters.getOverheadParams().getQueueDelay() != null) {
                delay = Parameters.getOverheadParams().getQueueDelay(cloudlet);
            }
            schedule(getVmsToDatacentersMap().get(vmId), delay, CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
        }
        //把Cloudlets交由数据中心处理以后，从CloudletList中移除这些任务，并向CloudletSubmittedList中添加这些任务
        getCloudletList().removeAll(scheduledList);
        getCloudletSubmittedList().addAll(scheduledList);
        cloudletsSubmitted += scheduledList.size();
    }
    
    protected void processCloudletUpdateForPSOGbest(SimEvent ev) {
    	List<Cloudlet> cloudletList=getCloudletList();
    	List<CondorVM> vmList=getVmsCreatedList();
    	List<Cloudlet> scheduledList =new ArrayList<Cloudlet>();
    	for(int i=0;i<cloudletList.size();i++) {
    		int cloudletId=cloudletList.get(i).getCloudletId();
    		int vmId=PsoScheduling.gbest_schedule[cloudletId];
    		cloudletList.get(i).setVmId(vmId);
    		//setVmState(vmId);
    		scheduledList.add(cloudletList.get(i));
    	}
    	for (Cloudlet cloudlet : scheduledList) {
            int vmId = cloudlet.getVmId();
            double delay = 0.0;
            if (Parameters.getOverheadParams().getQueueDelay() != null) {
                delay = Parameters.getOverheadParams().getQueueDelay(cloudlet);
            }
            schedule(getVmsToDatacentersMap().get(vmId), delay, CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
        }
        //把Cloudlets交由数据中心处理以后，从CloudletList中移除这些任务，并向CloudletSubmittedList中添加这些任务
        getCloudletList().removeAll(scheduledList);
        getCloudletSubmittedList().addAll(scheduledList);
        cloudletsSubmitted += scheduledList.size();
    }
    
    protected void processCloudletUpdateForGAInit(SimEvent ev) {
    	List<Cloudlet> cloudletList=getCloudletList();
    	List<CondorVM> vmList=getVmsCreatedList();
    	if(GASchedulingAlgorithm.initFlag==0) {
    		startTime = System.currentTimeMillis();
    		WorkflowEngine engine = (WorkflowEngine)CloudSim.getEntity(workflowEngineId);
    		GASchedulingAlgorithm.initPopsRandomly(engine.jobList.size(),getVmList().size());
    	}
    	List<Cloudlet> scheduledList =new ArrayList<Cloudlet>();
    	List<int[]> schedules=GASchedulingAlgorithm.schedules;
    	for(int i=0;i<cloudletList.size();i++) {
    		int cloudletId=cloudletList.get(i).getCloudletId();
    		int vmId=schedules.get(initIndexForGA)[cloudletId];
    		int scheduledVmId = ChooseVm(cloudletList.get(i), vmId);
    		cloudletList.get(i).setVmId(scheduledVmId);
    		//setVmState(vmId);
    		scheduledList.add(cloudletList.get(i));
    	}
    	for (Cloudlet cloudlet : scheduledList) {
            int vmId = cloudlet.getVmId();
            double delay = 0.0;
            if (Parameters.getOverheadParams().getQueueDelay() != null) {
                delay = Parameters.getOverheadParams().getQueueDelay(cloudlet);
            }
            schedule(getVmsToDatacentersMap().get(vmId), delay, CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
        }
        //把Cloudlets交由数据中心处理以后，从CloudletList中移除这些任务，并向CloudletSubmittedList中添加这些任务
        getCloudletList().removeAll(scheduledList);
        getCloudletSubmittedList().addAll(scheduledList);
        cloudletsSubmitted += scheduledList.size();
    }
    
    protected void processCloudletUpdateForGA(SimEvent ev) {
    	List<Cloudlet> cloudletList=getCloudletList();
    	List<CondorVM> vmList=getVmsCreatedList();
    	if(WorkflowEngine.gaFlag2==0&&cloudletList.size()!=0) {
    		GASchedulingAlgorithm.GA();
    	}
    	List<Cloudlet> scheduledList =new ArrayList<Cloudlet>();
    	List<int[]> schedules=GASchedulingAlgorithm.tempChildren;
    	for(int i=0;i<cloudletList.size();i++) {
    		int cloudletId=cloudletList.get(i).getCloudletId();
    		int vmId=schedules.get(tempChildrenIndex)[cloudletId];
    		int scheduledVmId = ChooseVm(cloudletList.get(i), vmId);
    		cloudletList.get(i).setVmId(scheduledVmId);
    		//setVmState(vmId);
    		scheduledList.add(cloudletList.get(i));
    	}
    	for (Cloudlet cloudlet : scheduledList) {
            int vmId = cloudlet.getVmId();
            double delay = 0.0;
            if (Parameters.getOverheadParams().getQueueDelay() != null) {
                delay = Parameters.getOverheadParams().getQueueDelay(cloudlet);
            }
            schedule(getVmsToDatacentersMap().get(vmId), delay, CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
        }
        //把Cloudlets交由数据中心处理以后，从CloudletList中移除这些任务，并向CloudletSubmittedList中添加这些任务
        getCloudletList().removeAll(scheduledList);
        getCloudletSubmittedList().addAll(scheduledList);
        cloudletsSubmitted += scheduledList.size();
    }
    
    protected void processCloudletUpdateForGABest(SimEvent ev) {
    	List<Cloudlet> cloudletList=getCloudletList();
    	List<CondorVM> vmList=getVmsCreatedList();
    	List<Cloudlet> scheduledList =new ArrayList<Cloudlet>();
    	for(int i=0;i<cloudletList.size();i++) {
    		int cloudletId=cloudletList.get(i).getCloudletId();
    		int vmId=GASchedulingAlgorithm.gbestSchedule[cloudletId];
    		int scheduledVmId = ChooseVm(cloudletList.get(i), vmId);
    		cloudletList.get(i).setVmId(scheduledVmId);
    		//setVmState(vmId);
    		scheduledList.add(cloudletList.get(i));
    	}
    	for (Cloudlet cloudlet : scheduledList) {
            int vmId = cloudlet.getVmId();
            double delay = 0.0;
            if (Parameters.getOverheadParams().getQueueDelay() != null) {
                delay = Parameters.getOverheadParams().getQueueDelay(cloudlet);
            }
            schedule(getVmsToDatacentersMap().get(vmId), delay, CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
        }
        //把Cloudlets交由数据中心处理以后，从CloudletList中移除这些任务，并向CloudletSubmittedList中添加这些任务
        getCloudletList().removeAll(scheduledList);
        getCloudletSubmittedList().addAll(scheduledList);
        cloudletsSubmitted += scheduledList.size();
    }
    
    /**
     * Process a cloudlet (job) return event.
     *
     * @param ev a SimEvent object
     * @pre ev != $null
     * @post $none
     */
    @Override
    protected void processCloudletReturn(SimEvent ev) {
        Cloudlet cloudlet = (Cloudlet) ev.getData();
        Job job = (Job) cloudlet;

        /**
         * Generate a failure if failure rate is not zeros.
         */
        FailureGenerator.generate(job);

        getCloudletReceivedList().add(cloudlet);
        getCloudletSubmittedList().remove(cloudlet);

        CondorVM vm = (CondorVM) getVmsCreatedList().get(cloudlet.getVmId());
        //so that this resource is released
        vm.setState(WorkflowSimTags.VM_STATUS_IDLE);
        vm.setlastUtilizationUpdateTime(CloudSim.clock());

        WorkflowEngine wfEngine = (WorkflowEngine) CloudSim.getEntity(workflowEngineId);
        Controller controller = wfEngine.getController();
        double delay = 0.0;
        if (Parameters.getOverheadParams().getPostDelay() != null) {
            delay = Parameters.getOverheadParams().getPostDelay(job);
        }
//        if(job.getoffloading() != controller.getmobile().getId()){
//        	if(job.getoffloading() != controller.getcloud().getId())
//        		delay = job.getOutputsize() / controller.parameter / controller.WAN_Bandwidth;
//        	else
//        		delay = job.getOutputsize() / controller.parameter / controller.LAN_Bandwidth;
//        }
//        System.out.println(cloudlet.getCloudletId()+".return delay : "+delay);
        schedule(this.workflowEngineId, delay, CloudSimTags.CLOUDLET_RETURN, cloudlet);

        cloudletsSubmitted--;
        //not really update right now, should wait 1 s until many jobs have returned
        schedule(this.getId(), 0.0, WorkflowSimTags.CLOUDLET_UPDATE);

    }

    /**
     * Start this entity (WorkflowScheduler)
     */
    @Override
    public void startEntity() {
        Log.printLine(getName() + " is starting...");
        // this resource should register to regional GIS.
        // However, if not specified, then register to system GIS (the
        // default CloudInformationService) entity.
        //int gisID = CloudSim.getEntityId(regionalCisName);
        int gisID = -1;
        if (gisID == -1) {
            gisID = CloudSim.getCloudInfoServiceEntityId();
        }

        // send the registration to GIS
        sendNow(gisID, CloudSimTags.REGISTER_RESOURCE, getId());
    }

    /**
     * Terminate this entity (WorkflowScheduler)
     */
    @Override
    public void shutdownEntity() {
        clearDatacenters();
        Log.printLine(getName() + " is shutting down...");
    }

    /**
     * Submit cloudlets (jobs) to the created VMs. Scheduling is here
     */
    @Override
    protected void submitCloudlets() {
        sendNow(this.workflowEngineId, CloudSimTags.CLOUDLET_SUBMIT, null);
    }
    /**
     * A trick here. Assure that we just submit it once
     */
    private boolean processCloudletSubmitHasShown = false;

    /**
     * Submits cloudlet (job) list
     *
     * @param ev a simEvent object
     */
    protected void processCloudletSubmit(SimEvent ev) {
        List<Job> list = (List) ev.getData();
        getCloudletList().addAll(list);

        sendNow(this.getId(), WorkflowSimTags.CLOUDLET_UPDATE);
        if (!processCloudletSubmitHasShown) {
            processCloudletSubmitHasShown = true;
        }
    }

    /**
     * Process a request for the characteristics of a PowerDatacenter.
     *
     * @param ev a SimEvent object
     * @pre ev != $null
     * @post $none
     */
    @Override
    protected void processResourceCharacteristicsRequest(SimEvent ev) {
        setDatacenterCharacteristicsList(new HashMap<>());
        Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with "
                + getDatacenterIdsList().size() + " resource(s)");
        for (Integer datacenterId : getDatacenterIdsList()) {
            sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
        }
    }
    
    private void setVmState(int id) {
		for(Vm vm : getVmList()){
			if(vm.getId()==id){
				CondorVM vm2 = (CondorVM) vm;
				vm2.setState(WorkflowSimTags.VM_STATUS_BUSY);
			}
		}
	}
    
    /**
     * 根据任务卸载决策的结果，对智能算法所生成的虚拟机编号进行转换
     * @param cloudlet 任务
     * @param vmId 智能算法生成的虚拟机编号
     * @return 根据卸载决策结果所选择的虚拟机编号
     */
    private int ChooseVm(Cloudlet cloudlet, int vmId) {
    	List<CondorVM> list = new ArrayList<CondorVM>();
    	int chooseVmId = -1;
    	Job job = (Job) cloudlet;
    	if(job.getoffloading() == -1){
//    		Log.printLine("没有进行卸载决策");
    		return vmId;
    	}
    	else{
    		for(Vm vm : getVmList()){
        		CondorVM cvm = (CondorVM) vm;
            	if(job.getoffloading() == cvm.getHost().getDatacenter().getId())
            		list.add(cvm);
            }
    		for(Vm vm : list){
    			if(vmId == vm.getId()){
    				//Log.printLine("GA----job"+job.getCloudletId()+":"+vmId+"=>"+CloudSim.getEntityName(job.getoffloading())+":"+vmId);
    				return vmId;
    			}
    		}
    		chooseVmId = list.get(0).getId() + vmId % list.size();
//    		System.out.println("GA----job"+job.getCloudletId()+":"+vmId+"=>"+CloudSim.getEntityName(job.getoffloading())+":"+chooseVmId);
    	}
    	list.clear();
		return chooseVmId;
	}
    
    /**
     * 根据任务卸载决策的结果得到相应数据中心的虚拟机列表
     * @param cloudlet 任务
     * @return 决策后相应数据中心的虚拟机列表
     */
    private List<CondorVM> getScheduledVmList(Cloudlet cloudlet) {
    	Job job = (Job) cloudlet;
    	if(job.getoffloading() == -1){
    		Log.printLine("没有进行卸载决策");
    		return getVmList();
    	}
    	List<CondorVM> list = new ArrayList<CondorVM>();
    	for(Vm vm : getVmList()){
    		CondorVM cvm = (CondorVM) vm;
        	if(job.getoffloading() == cvm.getHost().getDatacenter().getId())
        		list.add(cvm);
        }
    	return list;
	}
}
