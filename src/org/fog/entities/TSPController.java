package org.fog.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerHost;
import org.fog.utils.Config;
import org.fog.utils.FogEvents;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.NetworkUsageMonitor;
import org.workflowsim.CondorVM;
import org.workflowsim.FileItem;
import org.workflowsim.Job;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.utils.Parameters.FileType;
import org.workflowsim.utils.TSPEnvHelper;
import org.workflowsim.utils.TSPJobManager;

/**
 * By extending from Controller this class contains the evaluating indicators library, which can calculate each indicator.
 *
 * This extension includes to calculate the indicators also in the fog and cloud layers of the simulation.
 *
 * Due to the presence of several private attributes in the Controller class, this extension preserves the original code
 * and documents the differences starting with "TSP modification".
 *
 * @since TSP Extension 1.0
 * @author Julio Corona
 */
public class TSPController extends Controller{

    private List<FogDevice> fogDevices;

    /**
     * The workflow engine associated with it.
     */
    private WorkflowEngine wfEngine;

    /**
     * The offloading engine associated with it.
     */
    private OffloadingEngine offloadingEngine;

    /**
     * Current time
     */
    public double TotalExecutionTime;

    /**
     * The Energy of mobile
     */
    public double TotalEnergy;

    /**
     * The cost of the usage of fog servers and cloud
     */
    public double TotalCost;

    /**
     * The time of mobile sending data
     */
    public double MSendTime;//移动设备发送数据时间

    /**
     * The time of mobile receiving data
     */
    public double MReceTime;//移动设备接收数据时间

    double LAN_Bandwidth = 100;//Mbps
    double WAN_Bandwidth = 40;//Mbps
    final double parameter = 10000;//计算传输数据的传输时间的调整参数
    int count1=0, count2=0, count3=0;

    /**
     * TSP modification: just the name change
     * Creates a new entity.
     * @param name the name to be associated with this entity
     * @param fogDevices the list of fog devices
     * @param Engine the workflow engine associated with this entity
     */
    public TSPController(String name, List<FogDevice> fogDevices , WorkflowEngine Engine) {
        super(name, fogDevices, Engine);
        for(FogDevice fogDevice : fogDevices){
            fogDevice.setControllerId(getId());
        }
        setFogDevices(fogDevices);
        wfEngine = Engine;
        wfEngine.setcontrollerId(this.getId());
        offloadingEngine = wfEngine.getoffloadingEngine();
        offloadingEngine.setfogDevices(fogDevices);
        TotalExecutionTime=0.0;
        TotalEnergy=0.0;
        TotalCost=0.0;
        MSendTime=0.0;


        FogDevice fog = getFog();
        FogDevice cloud = getCloud();

        TSPEnvHelper.setCloudId(cloud.getId());

        TSPEnvHelper.setUploadRateVariables(fog.getUplinkLatency(), fog.getUplinkBandwidth(), cloud.getUplinkLatency(), cloud.getUplinkBandwidth());

//        System.out.println("Controller created");
    }

    public FogDevice getFogDeviceById(int id){
        for(FogDevice fogDevice : getFogDevices()){
            if(id==fogDevice.getId())
                return fogDevice;
        }
        return null;
    }

    @Override
    public void startEntity() {

//		Log.printLine("Starting FogWorkflowSim version 1.0");
        //send(getId(), Config.RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);

        //send(getId(), Config.MAX_SIMULATION_TIME, FogEvents.STOP_SIMULATION);

        //for(FogDevice dev : getFogDevices())
        //	sendNow(dev.getId(), FogEvents.RESOURCE_MGMT);

    }

    @Override
    public void processEvent(SimEvent ev) {
        switch(ev.getTag()){
            case FogEvents.CONTROLLER_RESOURCE_MANAGE:
                manageResources();
                break;
            case FogEvents.STOP_SIMULATION:
                System.out.println();
                shutdownEntity();
                CloudSim.clearEvent();
                updateExecutionTime();
                CloudSim.stopSimulation();
                break;
        }
    }

    private void printNetworkUsageDetails() {
        System.out.println("Total network usage = "+NetworkUsageMonitor.getNetworkUsage()/Config.MAX_SIMULATION_TIME);
    }

    /**
     * TSP modification: Extended to also show energy and execution time
     */
    private void printCostDetails(){
        System.out.println("Total Energy = "+TotalEnergy);
        System.out.println("Total Cost = "+TotalCost);
    }

    /**
     * TSP modification: Updated to calculate the energy by layers (not only mobile)
     */
    public void updateExecutionTime() {
        double time = 0.0;
        double energy = 0.0;
        double cost = 0.0;
        double WAN_sendInput = 0, WAN_sendOutput = 0;
        double LAN_sendInput = 0, LAN_sendOutput = 0;

        //TSP modification begin
        //getmobile().setEnergyConsumption(getMobileEnergy());
        for(FogDevice dev : getFogDevices()) {
            dev.setEnergyConsumption(getDeviceEnergy(dev));//updating the energy consumption
        }
        //TSP code end

        List<Job> jobList = wfEngine.getJobsReceivedList();

        for(Job job : jobList){
            if(getDC(job.getVmId()) == getcloud().getId()){

                count1++;
                WAN_sendInput += job.getInputsize();
//				WAN_sendOutput += job.getOutputsize();
            }
            else if(getDC(job.getVmId()) == getFogNode().getId()){
                count2++;
                LAN_sendInput += job.getInputsize();
//				LAN_sendOutput += job.getOutputsize();
            }
            else if(getDC(job.getVmId()) == getmobile().getId()){
                count3++;
            }
        }

        Job Lastjob = jobList.get(jobList.size()-1);//获取到最后一个执行的job
        for(FileItem file : Lastjob.getFileList()){
            if(file.getType() == FileType.OUTPUT)
                if(getDC(Lastjob.getVmId()) == getcloud().getId())
                    WAN_sendOutput += Lastjob.getOutputsize();
                else if(getDC(Lastjob.getVmId()) == getFogNode().getId())
                    LAN_sendOutput += Lastjob.getOutputsize();
        }

        for(FogDevice fogDevice : getFogDevices())
        {
            String name=fogDevice.getName();
            time += fogDevice.getExecutionTime();

            if(name.contains("m")){
                fogDevice.setEnergyConsumption(TSPJobManager.getGatewayEnergyConsumption(CloudSim.clock(), fogDevice.getHost()));//updating the energy consumption
            }
            else{
                fogDevice.setTotalCost(getDatacenterCost(fogDevice.getId()));
                cost += getDatacenterCost(fogDevice.getId());
            }
        }
        TotalExecutionTime = CloudSim.clock();
        TotalEnergy = getTotalEnergyConsumption();  // TSP Change. Updated from: getmobile().getEnergyConsumption();
        TotalCost = cost;
    }

    /**
     * TSP modification: new function to calculate the total energy consumption
     * @return the total energy consumed
     */
    public double getTotalEnergyConsumption() {
        double totalEnergy = 0;
        for(FogDevice dev : getFogDevices()) {
            totalEnergy+=dev.getEnergyConsumption();
        }
        return totalEnergy;
    }

    private void printPowerDetails() {
        for(FogDevice dev : getFogDevices()) {
            Log.printLine(dev.getName() +" : Energy Consumed = "+dev.getEnergyConsumption() +" J");
        }
    }

    protected void manageResources(){
        for(FogDevice dev : getFogDevices())
            sendNow(dev.getId(), FogEvents.RESOURCE_MGMT);
    }

    @Override
    public void shutdownEntity() {
        Log.printLine(getName() + " is shutting down...");
    }

    public List<FogDevice> getFogDevices() {
        return fogDevices;
    }

    public void setFogDevices(List<FogDevice> fogDevices) {
        this.fogDevices = fogDevices;
    }

    public void print()
    {
        Log.printLine("\n===================== Simulation results =====================");
        Log.printLine("Workflow Makespan = "+TotalExecutionTime);
        Log.printLine("Task time AVG: " + TSPJobManager.getTaskCompletionTimeAvg());
        printPowerDetails();
//        printCostDetails();
//        System.out.println("Offloading to Cloud: "+count1+", to Fog: "+count2+".");

        Log.printLine("Gateway Idle Energy Consumed = " + TSPJobManager.getGatewayIdleEnergyConsumption());
        Log.printLine("Gateway Busy Energy Consumed = " + TSPJobManager.getGatewayBusyEnergyConsumption());
        TSPJobManager.printTaskExceededDeadlineQuantities();
        Log.printLine("===============================================================");
        Log.printLine();
    }

    public void clear()
    {
        for(FogDevice device: fogDevices)
            device.clearConsumption();
        MSendTime = 0.0;
        MReceTime = 0.0;
        count1=0; count2=0; count3=0;
    }

    public double getSendSize(){
        double sendsize=0;
        List<Job> jobList=wfEngine.getJobsReceivedList();
        HashMap<String, List<Integer>> fileToDatacenter= new HashMap<String, List<Integer>>();

        for(Job job:jobList){
            CondorVM vm = getVm(job.getVmId());
            int datacenterId = vm.getHost().getDatacenter().getId();
            if(!CloudSim.getEntity(datacenterId).getName().contains("m")){
                for(FileItem file : job.getFileList()){
                    List<Integer> datacenterIdList = new ArrayList<Integer>();
                    if(file.getType() == FileType.INPUT){
                        if(!fileToDatacenter.containsKey(file.getName())){
                            sendsize += file.getSize();
                        }
                        else if(fileToDatacenter.containsKey(file.getName()) && !fileToDatacenter.get(file.getName()).contains(datacenterId)){
                            datacenterIdList = fileToDatacenter.get(file.getName());
                        }
                        datacenterIdList.add(datacenterId);
                        fileToDatacenter.put(file.getName(), datacenterIdList);
                    }
                }
            }
        }
        return sendsize;
    }

    public CondorVM getVm(int vmId){
        for(Vm vm:wfEngine.getAllVmList()){
            if(vm.getId()==vmId)
                return (CondorVM) vm;
        }
        return null;
    }

    public int getDC(int vmId){
        for(Vm vm:wfEngine.getAllVmList()){
            if(vm.getId()==vmId)
                return vm.getHost().getDatacenter().getId();
        }
        return 0;
    }

    public double getDatacenterCost(int id) {
        double cost = 0;
        List<Job> jobList = wfEngine.getJobsReceivedList();
        for(Job job : jobList){
            CondorVM vm = getVm(job.getVmId());
            PowerHost host = (PowerHost) vm.getHost();
            FogDevice fogdevice = (FogDevice) host.getDatacenter();
            int datacenterId = fogdevice.getId();
            if(!CloudSim.getEntity(datacenterId).getName().contains("m")){
                if(id == datacenterId) {
                    //cost+=job.getProcessingCost();
                    double c = job.getActualCPUTime()* vm.getHost().getTotalMips()/vm.getMips() * host.getcostPerMips();
                    //费用 = 执行时间 * 计算资源使用量 * 每单位计算量每单位时间的计算费用
                    job.setProcessingCost(c);
                    cost += c;
                }
            }
            else {
                job.setProcessingCost(0);
            }
        }
        return cost;
    }

    /**
     * TSP modification: This method replace the "getMobileEnergy" method to calculate the energy for the
     * specified device, instead of only doing it in the mobile layer.
     * @param dev the specified fog device (layer)
     * @return the energy consumed by the specified device
     */
    public double getDeviceEnergy(FogDevice dev){
        double energy = 0;
        double executiontime = 0;
        double idletime = 0;
        List<Job> jobList=wfEngine.getJobsReceivedList();

        for(Job job:jobList){
            CondorVM vm = getVm(job.getVmId());
            PowerHost host = (PowerHost)vm.getHost();
            FogLinearPowerModel powerModel = (FogLinearPowerModel) host.getPowerModel();
            FogDevice fogdevice = (FogDevice) vm.getHost().getDatacenter();
            if(fogdevice.getId() == dev.getId()){
                executiontime += job.getActualCPUTime();
                double  e = job.getActualCPUTime() * powerModel.getPower(vm.getMips()/host.getTotalMips());
                energy += e;
            }
        }
        idletime = CloudSim.clock()-executiontime;
        FogLinearPowerModel powerModel = (FogLinearPowerModel) getmobile().getHost().getPowerModel();
        energy += idletime * powerModel.getStaticPower();//负载能耗+空闲能耗
        return energy;
    }

    public FogDevice getmobile(){
        for(FogDevice dev : getFogDevices()) {
            if (dev.getName().startsWith("m"))
                return dev;
        }
        return null;
    }

    public FogDevice getCloud(){
        for(FogDevice dev : getFogDevices())
            if(dev.getName().startsWith("cloud"))
                return dev;
        return null;
    }

    public FogDevice getFog(){
        for(FogDevice dev : getFogDevices())
            if(dev.getName().startsWith("f"))
                return dev;
        return null;
    }
}
