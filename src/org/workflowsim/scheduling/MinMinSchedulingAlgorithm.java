/**
 * Copyright 2012-2013 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.workflowsim.scheduling;

import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.basic.BasicScrollPaneUI.VSBChangeListener;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.workflowsim.CondorVM;
import org.workflowsim.Job;
import org.workflowsim.WorkflowSimTags;

import sun.misc.VM;

/**
 * MinMin algorithm.
 *
 * @author Weiwei Chen
 * @since WorkflowSim Toolkit 1.0
 * @date Apr 9, 2013
 */
public class MinMinSchedulingAlgorithm extends BaseSchedulingAlgorithm {

    public MinMinSchedulingAlgorithm() {
        super();
    }
    private final List<Boolean> hasChecked = new ArrayList<>();

    @Override
    public void run() {

        int size = getCloudletList().size();
        List<Cloudlet> cloudlets = getCloudletList();
        hasChecked.clear();
        for (int t = 0; t < size; t++) {
            hasChecked.add(false);
        }
//        for (int i = 0; i < size; i++) 
        while(!cloudlets.isEmpty()){
            int minIndex = 0;
            Cloudlet minCloudlet = null;
            for (int j = 0; j < size; j++) {
                Cloudlet cloudlet = (Cloudlet) cloudlets.get(j);
                if (!hasChecked.get(j)) {
                    minCloudlet = cloudlet;
                    minIndex = j;
                    break;
                }
            }
            if (minCloudlet == null) {
                break;
            }


            for (int j = 0; j < size; j++) {
                Cloudlet cloudlet = (Cloudlet) cloudlets.get(j);
                if (hasChecked.get(j)) {
                    continue;
                }
                long length = cloudlet.getCloudletLength();
                if (length < minCloudlet.getCloudletLength()) {
                    minCloudlet = cloudlet;
                    minIndex = j;
                }
            }
            hasChecked.set(minIndex, true);

//            int vmSize = getVmList().size();
            Job job = (Job) minCloudlet;
            List<CondorVM> vlist = getVmList();
            List<CondorVM> schedulableVmList = new ArrayList<CondorVM>();
            if(job.getoffloading() == -1){
            	schedulableVmList.addAll(vlist);
//            	System.out.println("没有进行卸载决策");
            }
            else{
            	for(CondorVM vm : vlist){
                	if(job.getoffloading() == vm.getHost().getDatacenter().getId())
                		schedulableVmList.add(vm);
                }
			}
//            System.out.print("  job"+job.getCloudletId()+"卸载到"+CloudSim.getEntityName(job.getoffloading())+", 可调度的虚拟机有: ");
//            for(CondorVM v2 : schedulableVmList)
//            	System.out.print(v2.getId()+",");
            int vmSize = schedulableVmList.size();
            CondorVM firstIdleVm = null;//(CondorVM)getVmList().get(0);
            for (int j = 0; j < vmSize; j++) {
                CondorVM vm = schedulableVmList.get(j);
                if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE) {
                    firstIdleVm = vm;
                    break;
                }
            }
            if (firstIdleVm == null) {
//                break;
            	CondorVM fast = schedulableVmList.get(0);
            	for(CondorVM vm : schedulableVmList){
            		if(vm.getMips() > fast.getMips())
            			fast = vm;
            	}
            	firstIdleVm = fast;
            }
            else{
            for (int j = 0; j < vmSize; j++) {
                CondorVM vm = schedulableVmList.get(j);
                if ((vm.getState() == WorkflowSimTags.VM_STATUS_IDLE)
                        && vm.getCurrentRequestedTotalMips() > firstIdleVm.getCurrentRequestedTotalMips()) {
                    firstIdleVm = vm;
                }
            }
            }
            firstIdleVm.setState(WorkflowSimTags.VM_STATUS_BUSY);
            minCloudlet.setVmId(firstIdleVm.getId());
//            System.out.println("调度到vm"+firstIdleVm.getId());
            getScheduledList().add(minCloudlet);
            cloudlets.remove(minCloudlet);
            size = cloudlets.size();
        }
    }
}
