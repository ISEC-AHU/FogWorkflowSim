package org.fog.entities;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;
import org.workflowsim.*;
import org.workflowsim.failure.FailureGenerator;
import org.workflowsim.scheduling.*;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.Parameters.SchedulingAlgorithm;
import org.workflowsim.utils.TSPEnvHelper;
import org.workflowsim.utils.TSPJobManager;

import java.util.*;

import static org.workflowsim.utils.Parameters.TSPStrategy.TSP_DRL_BATCH;

/**
 * By extending from FogBroker this class add the TSP schedulers.
 * Due to the presence of several private attributes in the Controller class, this extension preserves the original code
 * and documents the differences starting with "TSP modification".
 *
 * @since TSP Extension 1.0
 * @author Julio Corona
 */
public class TSPFogBroker extends FogBroker {

    /**
     * The workflow engine id associated with this workflow algorithm.
     */
    private int workflowEngineId;

    /**
     * TSP modification: just the name change
     * Creates a new entity.
     * @param name name to be associated with this entity
     */
    public TSPFogBroker(String name) throws Exception {
        super(name);

//        System.out.println("Broker created");
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
                    case TSP_Placement:
                    case TSP_Scheduling:
                    case TSP_Scheduling_Placement:
                    case TSP_Batch_Schedule_Placement:
                    //TSP code end
                    case ROUNDROBIN:
                        processCloudletUpdate(ev);
                        break;

                    default:
                        break;
                }
                break;
            case CloudSimTags.TSP_GATEWAY_IDLE:
                processTSPGatewayFree(ev);
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
            //TSP modification begin
            case TSP_Scheduling:
                algorithm = new TSPSchedulingAlgorithm();
                break;
            case TSP_Placement:
                algorithm = new TSPPlacementAlgorithm();
                break;
            case TSP_Scheduling_Placement:
                algorithm = new TSPSchedulingAndPlacementAlgorithm();
                break;
            case TSP_Batch_Schedule_Placement:
                algorithm = new TSPBatchSchedulingAndPlacementAlgorithm();
                break;
            //TSP code end
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

    private double gatewayNextIdleTime = CloudSim.clock();

    /**
     * Performs one scheduling/placement decision on the gateway
     * @param isRequired
     */
    private void doTSPDecision(boolean isRequired) {

//        System.out.println("doTSPDecision: "+isRequired);

        TSPBaseStrategyAlgorithm scheduler = (TSPBaseStrategyAlgorithm)getScheduler(Parameters.getSchedulingAlgorithm());

        scheduler.setCloudletList(getCloudletList());
        List<? extends Vm> vmlist = getVmsCreatedList();
        scheduler.setVmList(vmlist);

        double decision_time = 0;

        try {
            decision_time = scheduler.runSteep(isRequired);
        } catch (Exception e) {
            Log.printLine("Error in configuring scheduler_method");
            e.printStackTrace();
        }

        if (decision_time == -1){
            if (scheduler.getScheduledList().isEmpty()) {
                //-1 means there was no server with enough resources for any task. And the list if checked because the first simulator task
                return;
            }else
                // in this case it's the first simulator task
                decision_time = 0;
        }

//        System.out.println("decision_time: "+decision_time);
//        System.out.println("scheduledList.size: "+scheduler.getScheduledList().size());

        List<Cloudlet> scheduledList = scheduler.getScheduledList();
        for (Cloudlet cloudlet : scheduledList) {
            int vmId = cloudlet.getVmId();
            double delay=0;

            Job job = (Job) cloudlet;

            if (job.getTaskList().isEmpty()){
                schedule(getVmsToDatacentersMap().get(vmId), delay, CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
            }else {
                //////

                TSPTask tsp_task = (TSPTask) job.getTaskList().get(0);

                delay += TSPEnvHelper.getOffloadingTimeByFogDeviceId(job.getoffloading(), tsp_task.getStorage());

                /////
                //offload the task when the decision time its reached
                schedule(getVmsToDatacentersMap().get(vmId), delay + decision_time, CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
            }

//            TSPEnvHelper.addBusyServer();
        }

        //mark el gateway busy until the end of the decision time
        gatewayNextIdleTime = CloudSim.clock() + decision_time;

        if (Parameters.getTSPStrategy() != TSP_DRL_BATCH) { //Because TSP_DRL_BATCH offloads all at once, there is no need to move to the next offloading.
            schedule(this.getId(), decision_time, CloudSimTags.TSP_GATEWAY_IDLE);
        }


        getCloudletList().removeAll(scheduledList);
        getCloudletSubmittedList().addAll(scheduledList);
        cloudletsSubmitted += scheduledList.size();
    }

    /**
     * @param ev a simEvent object
     */
    protected void processTSPGatewayFree(SimEvent ev) {
        //if the gateway its busy then delays more this event
        if (gatewayNextIdleTime > CloudSim.clock()){
            schedule(this.getId(), gatewayNextIdleTime - CloudSim.clock(), CloudSimTags.TSP_GATEWAY_IDLE);
            return;
        }
        this.doTSPDecision(false);
    }

    /**
     * Update a cloudlet (job)
     *
     * @param ev a simEvent object
     */
    protected void processCloudletUpdate(SimEvent ev) {

        if (gatewayNextIdleTime > CloudSim.clock()){
            schedule(this.getId(), gatewayNextIdleTime - CloudSim.clock(), WorkflowSimTags.CLOUDLET_UPDATE);
            return;
        }
        this.doTSPDecision(cloudletsSubmitted == 0);
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

        schedule(this.workflowEngineId, delay, CloudSimTags.CLOUDLET_RETURN, cloudlet);

        cloudletsSubmitted--;
        //not really update right now, should wait 1 s until many jobs have returned
        schedule(this.getId(), 0.0, WorkflowSimTags.CLOUDLET_UPDATE);

//        System.out.println("Running task quantity: "+ cloudletsSubmitted);
//        TSPEnvHelper.removeBusyServer();
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
