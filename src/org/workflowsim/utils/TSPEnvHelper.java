package org.workflowsim.utils;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.fog.entities.FogDevice;
import org.fog.utils.FogLinearPowerModel;
import org.workflowsim.CondorVM;
import org.workflowsim.Job;
import org.workflowsim.TSPTask;
import org.workflowsim.WorkflowSimTags;

import java.util.ArrayList;
import java.util.Iterator;
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
    public static Long[] parseState(TSPTask task, List notMobileVmList){
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
}
