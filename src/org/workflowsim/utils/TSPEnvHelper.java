package org.workflowsim.utils;

import org.workflowsim.*;

import java.util.List;

/**
 This class helps to represent the environment information for Reinforcement Learning agents
 *
 * @author Julio Corona
 * @since WorkflowSim Toolkit 1.0 's TSP extension
 */
public class TSPEnvHelper {
    /**
     * Return one array containing the environment information regarding task, fog and cloud servers
     * format its: [T.mi, T.ram, T.storage, T.priority, S1, S2, .., Sn] where Sn is the server's state, and T refers to the task to be placed
     * @return array with the environment information
     */
    public static Long[] parseStateWithTaskAndServers(TSPTask task, List notMobileVmList){
        Long[] state = new Long[4 + notMobileVmList.size()];

        state[0] = task.getMi();
        state[1] = task.getRam();
        state[2] = task.getStorage();
        state[3] = (long) task.getPriority();

        for (int i=0; i < notMobileVmList.size(); i++) {
            CondorVM vm = (CondorVM) notMobileVmList.get(i);
            if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE) {
                state[4 + i] = 1L;
            } else {
                state[4 + i] = 2L;
            }
        }
        return state;
    }

    /**
     * Return one array containing the environment information regarding task, fog and cloud servers
     * format its: [T1.mi, T1.ram, T1.storage, T1.priority, ... ,Tn.mi, Tn.ram, Tn.storage, Tn.priority, S1, S2, .., Sn] where Sn is the server's state, and T refers to the task to be placed
     * @return array with the environment information
     */
    public static Long[] parseStateWithTasksAndServers(List<Job> jobs, List notMobileVmList){
        Long[] state = new Long[4 * jobs.size() + notMobileVmList.size()];

        int i;
        for (i=0; i < jobs.size(); i++){
            TSPTask tsp_task = (TSPTask)jobs.get(i).getTaskList().get(0);
            state[i * 4] = tsp_task.getMi();
            state[i * 4 + 1] = tsp_task.getRam();
            state[i * 4 + 2] = tsp_task.getStorage();
            state[i * 4 + 3] = (long) tsp_task.getPriority();
        }

        int used_positions = i * 4;

        for (i=0; i < notMobileVmList.size(); i++) {
            CondorVM vm = (CondorVM) notMobileVmList.get(i);
            if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE) {
                state[used_positions + i] = 1L;
            } else {
                state[used_positions + i] = 2L;
            }
        }

        return state;
    }

    /**
     * Parse a string to an array of int
     * @param str the string to be parsed
     * @return the resulting array of int
     */
    public static int[] parseStrArrayToIntArray(String str){
        String[] string = str.split(",");

        int size = string.length;
        int [] arr = new int [size];
        for(int i=0; i<size; i++) {
            arr[i] = Integer.parseInt(string[i]);
        }
        return arr;
    }

    /**
     * Parse a string to a float an array of int
     * @param str the string to be parsed
     * @return the resulting array of int
     */
    public static TSPDecisionResult parseStrArrayToTSPDecisionResult(String str){
        String[] string = str.split("d");
        double computation_time;

        //parsing the computation cost
        String[] computing_cost = string[0].split(",");

        if (Parameters.getConsiderGatewayComputationTime()){
            computation_time = Double.parseDouble(computing_cost[0]);
        }else {
            computation_time = 0;
        }

        computation_time = TSPJobManager.parseComputationTime(computation_time);

        TSPJobManager.registerGatewayBusyTimes(computation_time, Double.parseDouble(computing_cost[1]));

        //parsing the decision result
        string = string[1].split(",");

        int size = string.length;
        int [] arr = new int [size];
        for(int i=0; i<size; i++) {
            arr[i] = Integer.parseInt(string[i]);
        }
        return new TSPDecisionResult(computation_time, arr);
    }

    private static double fog_latency;
    private static double fog_upload_bandwidth_MB;
    private static double cloud_latency;
    private static double cloud_upload_bandwidth_MB;

    public static void setUploadRateVariables(double f_latency, double f_upload_bandwidth_Mb, double c_latency, double c_upload_bandwidth_Mb){
        fog_latency=f_latency;
        fog_upload_bandwidth_MB=f_upload_bandwidth_Mb / 8;
        cloud_latency=c_latency;
        cloud_upload_bandwidth_MB=c_upload_bandwidth_Mb / 8;
    }

    private static int cloud_fog_device_id;

    public static void setCloudId(int cloudFDid){
        cloud_fog_device_id = cloudFDid;
    }

    public static double getOffloadingTimeByFogDeviceId(int FDid, double taskSize){
        if (FDid == cloud_fog_device_id) {
            return cloud_latency + taskSize / cloud_upload_bandwidth_MB;
        }
        return fog_latency + taskSize / fog_upload_bandwidth_MB;
    }
//
//    private static int busy_servers_quantity = 0;
//
//    public static void addBusyServer(){
//        busy_servers_quantity +=1 ;
//    }
//
//    public static void removeBusyServer(){
//        busy_servers_quantity -=1 ;
//    }

}
