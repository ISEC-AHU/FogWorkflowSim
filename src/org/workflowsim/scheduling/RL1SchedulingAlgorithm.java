package org.workflowsim.scheduling;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.workflowsim.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RL1SchedulingAlgorithm  extends BaseSchedulingAlgorithm {


    private int v_job_id = -1;
    private Object v_job = null;

    public RL1SchedulingAlgorithm()
    {
        super();
    }

    private final List<Boolean> hasChecked = new ArrayList<>();

    /**
     * Return the list of devices available for placement
     * @return
     */
    public List getNotMobileVmList() {
        Predicate<CondorVM> byLayer = vm -> !vm.getHost().getDatacenter().getName().startsWith("m");
        return (List)getVmList().stream().filter(byLayer).collect(Collectors.toList());
    }

    /**
     * Return the simulated gateway device
     * @return
     */
    public CondorVM getGatewayVm(){
        for (Iterator itc = getVmList().iterator(); itc.hasNext();) { //VM list
            CondorVM vm = (CondorVM) itc.next();
            if (vm.getHost().getDatacenter().getName().startsWith("m")){
                return vm;
            }
        }
        return null;
    }

    @Override
    public void run() {

        for (Iterator it = getCloudletList().iterator(); it.hasNext();) { //Task list

            Job next=(Job)it.next();

            //if the task is a TSP task
            if (next.getTaskList().size() > 0){
                Task cloudlet = (Task)next;
                TSPTask task_info = (TSPTask)next.getTaskList().get(0);

                //the new scheduler begin

//                if (CloudSim.clock() >= task_info.getTimeSubmission()) {
//                if (TSPJobManager.canRunTask(task_info.getJob_id(), task_info.getTask_id())) {
                if (true) {
                    //the new scheduler end
                    boolean stillHasVm = false;

                    for (Iterator itc = getNotMobileVmList().iterator(); itc.hasNext(); ) { //VM list
                        CondorVM vm = (CondorVM) itc.next();
                        if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE) {
                            stillHasVm = true;
                            vm.setState(WorkflowSimTags.VM_STATUS_BUSY);
                            cloudlet.setVmId(vm.getId());
                            getScheduledList().add(cloudlet);
                            TSPJobManager.addTaskRunning(task_info.getJob_id(), task_info.getTask_id());
                            break;
                        }
                    }
                    //no vm available
                    if (!stillHasVm) {
                        break;
                    }
                }
            }
            //else, the job is the empty workflowsim's job, and the allocation it's the default FCFS
            else{

                Task cloudlet = (Task) next;

                boolean stillHasVm = false;

                for (Iterator itc = getNotMobileVmList().iterator(); itc.hasNext();) { //VM list
                    CondorVM vm = (CondorVM) itc.next();
                    if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE) {
                        stillHasVm = true;
                        vm.setState(WorkflowSimTags.VM_STATUS_BUSY);
                        cloudlet.setVmId(vm.getId());
                        getScheduledList().add(cloudlet);
                        break;
                    }
                }

                //no vm available
                if (!stillHasVm) {
                    break;
                }
            }
        }
    }
}
