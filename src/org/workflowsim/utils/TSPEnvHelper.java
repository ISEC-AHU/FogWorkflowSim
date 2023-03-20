package org.workflowsim.utils;

import org.workflowsim.*;

import java.util.ArrayList;
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
            state[i] = tsp_task.getMi();
            state[i + 1] = tsp_task.getRam();
            state[i + 2] = tsp_task.getStorage();
            state[i + 3] = (long) tsp_task.getPriority();
        }

        int next_limit = i + notMobileVmList.size();

        for (; i < next_limit; i++) {
            CondorVM vm = (CondorVM) notMobileVmList.get(i);
            if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE) {
                state[i] = 1L;
            } else {
                state[i] = 2L;
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


}
