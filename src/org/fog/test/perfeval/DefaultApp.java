package org.fog.test.perfeval;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.fog.entities.Controller;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.offloading.OffloadingStrategyAllinCloud;
import org.fog.offloading.OffloadingStrategyAllinFog;
import org.fog.offloading.OffloadingStrategySimple;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.workflowsim.*;
import org.workflowsim.utils.ClusteringParameters;
import org.workflowsim.utils.OverheadParameters;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.ReplicaCatalog;

import java.io.File;
import java.util.*;

//Baseline paper http://www.csjournals.com/IJEE/PDF10-2/107.%20Sanju.pdf

public class DefaultApp {

    static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
    static List<Double[]> record=new ArrayList<Double[]>();
    final static int numOfDepts = 1;
    final static int numOfMobilesPerDept = 1;

    final static String[] algrithmStr = new String[]{"MINMIN","MAXMIN","FCFS","ROUNDROBIN","PSO","GA","TSP"};
    final static String scheduler_method="TSP";

    final static String StrategyCb="All-in-Fog"; //"All-in-Fog","All-in-Cloud","Simple"
    final static String optimize_objective="Time"; //"Time","Energy","Cost"

    final static int numCloudDevices = 10;
    final static int numFogDevices = 2;
    final static int numMobileDevices = 3;



    private static WorkflowEngine wfEngine;
    private static Controller controller;

    final static String daxPath ="config/dax/"+"Montage"+"_"+"40"+".xml";

    public static void simulate(double deadline) {
        System.out.println("Starting Task...");

        try {
            //Log.disable();
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            CloudSim.init(num_user, calendar, trace_flag);

            String appId = "workflow"; // identifier of the application

            createFogDevices(1,appId);//(broker.getId(), appId);

            List<? extends Host> hostlist = new ArrayList<Host>();
            int hostnum = 0;
            for(FogDevice device : fogDevices){
                hostnum += device.getHostList().size();
                hostlist.addAll(device.getHostList());
            }
            int vmNum = hostnum;//number of vms;

            File daxFile = new File(daxPath);
            if (!daxFile.exists()) {
                System.out.println("Warning: Please replace daxPath with the physical path in your working environment!");
                return;
            }

            /**
             * Since we are using MINMIN scheduling algorithm, the planning
             * algorithm should be INVALID such that the planner would not
             * override the result of the scheduler
             */
            Parameters.SchedulingAlgorithm sch_method =Parameters.SchedulingAlgorithm.valueOf(scheduler_method);
            Parameters.Optimization opt_objective = Parameters.Optimization.valueOf(optimize_objective);
            Parameters.PlanningAlgorithm pln_method = Parameters.PlanningAlgorithm.INVALID;
            ReplicaCatalog.FileSystem file_system = ReplicaCatalog.FileSystem.SHARED;
            /**
             * No overheads
             */
            OverheadParameters op = new OverheadParameters(0, null, null, null, null, 0);

            /**
             * No Clustering
             */
            ClusteringParameters.ClusteringMethod method = ClusteringParameters.ClusteringMethod.NONE;
            ClusteringParameters cp = new ClusteringParameters(0, 0, method, null);

            /**
             * Initialize static parameters
             */
            Parameters.init(vmNum, daxPath, null,
                    null, op, cp, sch_method, opt_objective,
                    pln_method, null, 0);
            ReplicaCatalog.init(file_system);

            /**
             * Create a WorkflowPlanner with one schedulers.
             */
            WorkflowPlanner wfPlanner = new WorkflowPlanner("planner_0", 1);
            /**
             * Create a WorkflowEngine.
             */
            wfEngine = wfPlanner.getWorkflowEngine();

            /**
             * Set a offloading Strategy for OffloadingEngine
             */

            switch (StrategyCb) {
                case "All-in-Fog":
                    wfEngine.getoffloadingEngine().setOffloadingStrategy(new OffloadingStrategyAllinFog());
                    break;
                case "All-in-Cloud":
                    wfEngine.getoffloadingEngine().setOffloadingStrategy(new OffloadingStrategyAllinCloud());
                    break;
                case "Simple":
                    wfEngine.getoffloadingEngine().setOffloadingStrategy(new OffloadingStrategySimple());
                    break;
                default:
                    wfEngine.getoffloadingEngine().setOffloadingStrategy(null);
                    break;
            }

            /**
             * Set a deadline of workflow for WorkflowEngine
             */
            wfEngine.setDeadLine(deadline);
            /**
             * Create a list of VMs.The userId of a vm is basically the id of
             * the scheduler that controls this vm.
             */
            List<CondorVM> vmlist0 = createVM(wfEngine.getSchedulerId(0), Parameters.getVmNum(), hostlist);
            hostlist = null;//清空，释放内存
            /**
             * Submits this list of vms to this WorkflowEngine.
             */
            wfEngine.submitVmList(vmlist0, 0);
            vmlist0 = null;

            controller = new Controller("master-controller", fogDevices, wfEngine);

            /**
             * Binds the data centers with the scheduler.
             */
            List<PowerHost> list;
            for(FogDevice fogdevice:controller.getFogDevices()){
                wfEngine.bindSchedulerDatacenter(fogdevice.getId(), 0);
                list = fogdevice.getHostList();  //输出设备上的主机
                System.out.println(fogdevice.getName()+": ");
                for (PowerHost host : list){
                    System.out.print(host.getId()+":Mips("+host.getTotalMips()+"),"+"cost("+host.getcostPerMips()+")  ");
                }
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unwanted errors happen");
        }
    }

    public static Double getAlgorithm(String scheduler_method) {
        if(scheduler_method.equals(algrithmStr[0]))
            return 1.0;
        else if(scheduler_method.equals(algrithmStr[1]))
            return 2.0;
        else if(scheduler_method.equals(algrithmStr[2]))
            return 3.0;
        else if(scheduler_method.equals(algrithmStr[3]))
            return 4.0;
        else if(scheduler_method.equals(algrithmStr[4]))
            return 5.0;
        else if(scheduler_method.equals(algrithmStr[5]))
            return 6.0;
        else if(scheduler_method.equals(algrithmStr[6]))
            return 7.0;
        return null;
    }

    private static void createFogDevices(int userId, String appId) {

        double ratePerMips = 0.96;
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.1; // the cost of using storage in this resource
        double costPerBw = 0.2;//

        List<Long> GHzList = new ArrayList<>();
        List<Double> CostList = new ArrayList<>();

        for (int i = 0; i < numCloudDevices; i++){ //setup cloud capacities
            GHzList.add((long)1600);
            CostList.add(0.96);
        }

        System.out.println();

        System.out.println("GHzList.size()");
        System.out.println(GHzList.size());

        FogDevice cloud = createFogDevice("cloud", GHzList.size(), GHzList, CostList,
                40000, 100, 10000, 0, ratePerMips, 16*103, 16*83.25,costPerMem,costPerStorage,costPerBw); // creates the fog device Cloud at the apex of the hierarchy with level=0
        cloud.setParentId(-1);

        fogDevices.add(cloud);
        for(int i=0;i<numOfDepts;i++){
            addFogNode(i+"", userId, appId, fogDevices.get(0).getId()); // adding a fog device for every Gateway in physical topology. The parent of each gateway is the Proxy Server
        }
    }

    private static FogDevice addFogNode(String id, int userId, String appId, int parentId){

        double ratePerMips = 0.48;
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.1; // the cost of using storage in this resource
        double costPerBw = 0.1;

        List<Long> GHzList = new ArrayList<>();
        List<Double> CostList = new ArrayList<>();

        for (int i = 0; i < numFogDevices; i++){ //setup cloud capacities
            GHzList.add((long)1300);
            CostList.add(0.48);
        }

        FogDevice dept = createFogDevice("f-"+id, GHzList.size(), GHzList, CostList,
                4000, 10000, 10000, 1, ratePerMips, 700, 30,costPerMem,costPerStorage,costPerBw);
        fogDevices.add(dept);
        dept.setParentId(parentId);
        dept.setUplinkLatency(4); // latency of connection between gateways and server is 4 ms
        for(int i=0;i<numOfMobilesPerDept;i++){
            String mobileId = id+"-"+i;
            FogDevice mobile = addMobile(mobileId, userId, appId, dept.getId()); // adding mobiles to the physical topology. Smartphones have been modeled as fog devices as well.
            mobile.setUplinkLatency(2); // latency of connection between the smartphone and proxy server is 4 ms
            fogDevices.add(mobile);
        }
        return dept;
    }

    private static FogDevice addMobile(String id, int userId, String appId, int parentId){
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.1; // the cost of using storage in this resource
        double costPerBw = 0.3;//每带宽的花费

        List<Long> GHzList = new ArrayList<>();
        List<Double> CostList = new ArrayList<>();

        for (int i = 0; i < numMobileDevices; i++){ //setup cloud capacities
            CostList.add(0.0);
            GHzList.add((long)1000);
        }

        FogDevice mobile = createFogDevice("m-"+id, GHzList.size(), GHzList, CostList,
                10000, 20*1024, 40*1024, 3, 0, 700, 30,costPerMem,costPerStorage,costPerBw);
        mobile.setParentId(parentId);
        return mobile;
    }

    /**
     * Creates a vanilla fog device
     * @param nodeName name of the device to be used in simulation
     * @param hostnum the number of the host of device
     * @param mips the list of host'MIPS
     * @param costPerMips the list of host'cost per mips
     * @param ram RAM
     * @param upBw uplink bandwidth (Kbps)
     * @param downBw downlink bandwidth (Kbps)
     * @param level hierarchy level of the device
     * @param ratePerMips cost rate per MIPS used
     * @param busyPower(mW)
     * @param idlePower(mW)
     * @return
     */
    private static FogDevice createFogDevice(String nodeName, int hostnum, List<Long> mips, List<Double> costPerMips,
                                      int ram, long upBw, long downBw, int level, double ratePerMips,
                                      double busyPower, double idlePower,
                                      double costPerMem,double costPerStorage,double costPerBw) {

        List<Host> hostList = new ArrayList<Host>();

        for ( int i = 0 ;i < hostnum; i++ )
        {
            List<Pe> peList = new ArrayList<Pe>();
            // Create PEs and add these into a list.
            peList.add(new Pe(0, new PeProvisionerSimple(mips.get(i)))); // need to store Pe id and MIPS Rating
            int hostId = FogUtils.generateEntityId();
            long storage = 1000000; // host storage
            int bw = 10000;

            PowerHost host = new PowerHost(
                    hostId,
                    costPerMips.get(i),
                    new RamProvisionerSimple(ram),
                    new BwProvisionerSimple(bw),
                    storage,
                    peList,
                    new VmSchedulerTimeShared(peList),
                    new FogLinearPowerModel(busyPower, idlePower)//默认发送功率100mW 接收功率25mW
            );

            hostList.add(host);
        }

        // Create a DatacenterCharacteristics object
        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource每秒的花费
			/*double costPerMem = 0.05; // the cost of using memory in this resource
			double costPerStorage = 0.1; // the cost of using storage in this resource
			double costPerBw = 0.1; // the cost of using bw in this resource每带宽的花费*/
        LinkedList<Storage> storageList = new LinkedList<Storage>();

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        FogDevice fogdevice = null;

        // Finally, we need to create a storage object.
        try {
            HarddriveStorage s1 = new HarddriveStorage(nodeName, 1e12);
            storageList.add(s1);
            fogdevice = new FogDevice(nodeName, characteristics,
                    new VmAllocationPolicySimple(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
        } catch (Exception e) {
            e.printStackTrace();
        }

        fogdevice.setLevel(level);
        return fogdevice;
    }

    protected static List<CondorVM> createVM(int userId, int vms, List<? extends Host> devicelist) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<CondorVM> list = new LinkedList<>();

        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        long bw = 1;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs
        CondorVM[] vm = new CondorVM[vms];
        for (int i = 0; i < vms; i++) {
            double ratio = 1.0;
            int mips = devicelist.get(i).getTotalMips();
            vm[i] = new CondorVM(i, userId, mips * ratio, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }
        return list;
    }

    public static void main(String[] args) {
        System.out.println("Starting TestApplication...");

        System.out.println("Optimization objective : "+optimize_objective);

        double deadline = Double.MAX_VALUE;
        simulate(deadline);
        CloudSim.startSimulation();
        List<Job> outputList0 = wfEngine.getJobsReceivedList();
        CloudSim.stopSimulation();
        Log.enable();
        controller.print();
        Double[] a = {getAlgorithm(scheduler_method),controller.TotalExecutionTime,controller.TotalEnergy,controller.TotalCost};
        record.add(a);
        long time = wfEngine.algorithmTime;

        System.out.println("Algorithm Time: "+time);
    }


}
