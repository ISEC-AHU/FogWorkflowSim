package org.fog.offloading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.entities.FogDevice;
import org.fog.utils.FogLinearPowerModel;
import org.workflowsim.FileItem;
import org.workflowsim.Job;
import org.workflowsim.utils.Parameters.FileType;

public class OffloadingStrategySimple extends OffloadingStrategy{
	
	double LAN_Bandwidth = 100;//Mbps
	double WAN_Bandwidth = 40;//Mbps
	final double parameter = 10000;//计算传输数据的传输时间的调整参数
	double deadline;
	private HashMap<String, List<Integer>> fileToDatacenter;
	private static FogLinearPowerModel powerModel;
	List<Integer> IdList = new ArrayList<Integer>();
	
	public OffloadingStrategySimple(List<FogDevice> fogdevices) {
		super(fogdevices);
		// TODO Auto-generated constructor stub
		deadline = getOffloadingEngine().getWorkflowEngine().DeadLine;
		fileToDatacenter = new HashMap<String, List<Integer>>();
	}
	public OffloadingStrategySimple() {
		fileToDatacenter = new HashMap<String, List<Integer>>();
	}

	public double SelectDatacenter(Job job, double deadline) {
		// TODO Auto-generated method stub
		double time1 = 0, time2 = 0, time3 = 0;
		double energy1 = 0, energy2 = 0, energy3 = 0;
		powerModel = (FogLinearPowerModel) getmobile().getHost().getPowerModel();
		
		for(FogDevice fd: getFogDeviceLists()){
			if(fd.getName().equalsIgnoreCase("cloud")){ //计算卸载到云所需时间
				time1 = job.getCloudletLength() / fd.getAverageMips()
						                  + getJobFileSize(job) / parameter / WAN_Bandwidth;
				//卸载所需能耗 = 空闲功率 * 云执行时间 + 传输功率 * (发送数据大小 + 接收数据大小 ) / WAN带宽
				energy1 = powerModel.getStaticPower() * job.getCloudletLength() / fd.getAverageMips()
						     + powerModel.getSendPower() * getJobFileSize(job) / parameter / WAN_Bandwidth;
			}
			else if(fd.getName().contains("f")){ //计算卸载到雾所需时间
				time2 = job.getCloudletLength() / fd.getAverageMips()
		                  + getJobFileSize(job) / parameter / LAN_Bandwidth;
				//卸载所需能耗 = 空闲功率 * 雾执行时间 + 传输功率 * (发送数据大小 + 接收数据大小 ) / LAN带宽
				energy2 = powerModel.getStaticPower() * job.getCloudletLength() / fd.getAverageMips()
						+ powerModel.getSendPower() * getJobFileSize(job) / parameter / LAN_Bandwidth;
//				System.out.print("energy2 = ");
//				System.out.print(powerModel.getStaticPower()+" * "+(job.getCloudletLength() / fd.getHost().getTotalMips()));
//				System.out.print(" + "+ powerModel.getSendPower() +" * ("); 
//				System.out.print(getJobInputFileSize(fd, job));
//				System.out.print(" + "+getJobOutputFileSize(job)+" ) * 8/ 100 /");
//                System.out.println(LAN_Bandwidth);
//                System.out.println("energy2 = "+powerModel.getStaticPower() * job.getCloudletLength() / fd.getHost().getTotalMips() + " + "
//                                                  +powerModel.getSendPower() +" * "+(getJobInputFileSize(fd, job) + getJobOutputFileSize(job)) * 8 /100 / LAN_Bandwidth);
			}
			else{//不卸载
				time3 = job.getCloudletLength() / fd.getAverageMips();
				energy3 = powerModel.getMaxPower() * job.getCloudletLength() / fd.getAverageMips();
			}
		}
		System.out.println("deadline : "+deadline+"; cloud : "+time1+"; fog : "+time2+"; mobile : "+time3);
//		System.out.println(energy1+",  "+energy2+",  "+energy3);
		if(deadline < Math.min(time1, time2)){//都不满足时间约束
			System.out.println("卸载不满足时间约束");
			job.setoffloading(getmobile().getId());//不卸载
		}
		else if(deadline > Math.max(time1, time2)){//都满足时间约束
			if(Math.min(energy3, Math.min(energy1, energy2)) == energy1){
				System.out.println("都满足时间约束，且卸载到云能耗最小");
				job.setoffloading(getcloud().getId());//卸载到云
			}
			else if(Math.min(energy3, Math.min(energy1, energy2)) == energy2){
				System.out.println("都满足时间约束，且卸载到雾能耗最小");
				job.setoffloading(getFogNode().getId());//卸载到雾节点
			}
			else{
				System.out.println("都满足时间约束，但不卸载能耗最小");
				job.setoffloading(getmobile().getId());//不卸载
			}
		}
		else{//deadline介于time1和time2之间
			if(time1 < time2 && energy1 < energy3){//卸载到云满足时间约束并且能耗较小
				System.out.println("云满足时间约束，且能耗小");
				job.setoffloading(getcloud().getId());//卸载到云
			}
			else if(time1 > time2 && energy2 < energy3){//卸载到雾节点满足时间约束并且能耗较小
				System.out.println("雾满足时间约束，且能耗小");
				job.setoffloading(getFogNode().getId());//卸载到雾节点
			}
			else{
				System.out.println("有一个满足时间约束，但不卸载能耗最小");
				job.setoffloading(getmobile().getId());//不卸载
			}
		}
//		if(deadline < Math.min(time1, time2))
//			job.setoffloading(getmobile().getId());//不卸载
//		else{
//			if(time1 < time2){
//				job.setoffloading(getcloud().getId());//卸载到云
//				addFileToDatacenter(getcloud(), job);
//			}
//			else{
//				job.setoffloading(getFogNode().getId());//卸载到雾节点
//				addFileToDatacenter(getFogNode(), job);
//			}
//		}
		System.out.println("job"+job.getCloudletId()+"卸载决策结果: "+job.getoffloading()+":"+CloudSim.getEntityName(job.getoffloading()));//输出卸载决策结果
		return time3;
	}
	
	/*
	@Override
	public double SelectDatacenter(Job job, double deadline) {
		// TODO Auto-generated method stub
		double time1 = 0, time2 = 0, time3 = 0;
		double energy1 = 0, energy2 = 0, energy3 = 0;
		powerModel = (FogLinearPowerModel) getmobile().getHost().getPowerModel();
		
		for(FogDevice fd: getFogDeviceLists()){
			if(fd.getName().equalsIgnoreCase("cloud")){ //计算卸载到云所需时间
				time1 = job.getCloudletLength() / fd.getAverageMips()
						                  + getJobInputFileSize(fd, job) / parameter / WAN_Bandwidth;
				//卸载所需能耗 = 空闲功率 * 云执行时间 + 传输功率 * (发送数据大小 + 接收数据大小 ) / WAN带宽
				energy1 = powerModel.getStaticPower() * job.getCloudletLength() / fd.getAverageMips()
						     + powerModel.getSendPower() * (getJobInputFileSize(fd, job) + getJobOutputFileSize(job)) 
						                                                     / parameter / WAN_Bandwidth;
			}
			else if(fd.getName().contains("f")){ //计算卸载到雾所需时间
				time2 = job.getCloudletLength() / fd.getAverageMips()
		                  + getJobInputFileSize(fd, job) / parameter / LAN_Bandwidth;
				//卸载所需能耗 = 空闲功率 * 雾执行时间 + 传输功率 * (发送数据大小 + 接收数据大小 ) / LAN带宽
				energy2 = powerModel.getStaticPower() * job.getCloudletLength() / fd.getAverageMips()
						+ powerModel.getSendPower() * (getJobInputFileSize(fd, job) + getJobOutputFileSize(job)) 
                                                                    / parameter / LAN_Bandwidth;
//				System.out.print("energy2 = ");
//				System.out.print(powerModel.getStaticPower()+" * "+(job.getCloudletLength() / fd.getHost().getTotalMips()));
//				System.out.print(" + "+ powerModel.getSendPower() +" * ("); 
//				System.out.print(getJobInputFileSize(fd, job));
//				System.out.print(" + "+getJobOutputFileSize(job)+" ) * 8/ 100 /");
//                System.out.println(LAN_Bandwidth);
//                System.out.println("energy2 = "+powerModel.getStaticPower() * job.getCloudletLength() / fd.getHost().getTotalMips() + " + "
//                                                  +powerModel.getSendPower() +" * "+(getJobInputFileSize(fd, job) + getJobOutputFileSize(job)) * 8 /100 / LAN_Bandwidth);
			}
			else{//不卸载
				time3 = job.getCloudletLength() / fd.getAverageMips();
				energy3 = powerModel.getMaxPower() * job.getCloudletLength() / fd.getAverageMips();
			}
		}
		System.out.println("deadline : "+deadline+"; cloud : "+time1+"; fog : "+time2+"; mobile : "+time3);
//		System.out.println(energy1+",  "+energy2+",  "+energy3);
		if(deadline < Math.min(time1, time2)){//都不满足时间约束
			System.out.println("卸载不满足时间约束");
			job.setoffloading(getmobile().getId());//不卸载
		}
		else if(deadline > Math.max(time1, time2)){//都满足时间约束
			if(Math.min(energy3, Math.min(energy1, energy2)) == energy1){
				System.out.println("都满足时间约束，且卸载到云能耗最小");
				job.setoffloading(getcloud().getId());//卸载到云
				addFileToDatacenter(getcloud(), job);
			}
			else if(Math.min(energy3, Math.min(energy1, energy2)) == energy2){
				System.out.println("都满足时间约束，且卸载到雾能耗最小");
				job.setoffloading(getFogNode().getId());//卸载到雾节点
				addFileToDatacenter(getFogNode(), job);
			}
			else{
				System.out.println("都满足时间约束，但不卸载能耗最小");
				job.setoffloading(getmobile().getId());//不卸载
			}
		}
		else{//deadline介于time1和time2之间
			if(time1 < time2 && energy1 < energy3){//卸载到云满足时间约束并且能耗较小
				System.out.println("云满足时间约束，且能耗小");
				job.setoffloading(getcloud().getId());//卸载到云
				addFileToDatacenter(getcloud(), job);
			}
			else if(time1 > time2 && energy2 < energy3){//卸载到雾节点满足时间约束并且能耗较小
				System.out.println("雾满足时间约束，且能耗小");
				job.setoffloading(getFogNode().getId());//卸载到雾节点
				addFileToDatacenter(getFogNode(), job);
			}
			else{
				System.out.println("有一个满足时间约束，但不卸载能耗最小");
				job.setoffloading(getmobile().getId());//不卸载
			}
		}
//		if(deadline < Math.min(time1, time2))
//			job.setoffloading(getmobile().getId());//不卸载
//		else{
//			if(time1 < time2){
//				job.setoffloading(getcloud().getId());//卸载到云
//				addFileToDatacenter(getcloud(), job);
//			}
//			else{
//				job.setoffloading(getFogNode().getId());//卸载到雾节点
//				addFileToDatacenter(getFogNode(), job);
//			}
//		}
		System.out.println("job"+job.getCloudletId()+"卸载决策结果: "+job.getoffloading()+":"+CloudSim.getEntityName(job.getoffloading()));//输出卸载决策结果
		return time3;
	}*/
	
	public double getJobInputFileSize(FogDevice device, Job job){
		double sendsize = 0;
		for(FileItem file : job.getFileList()){
			if(file.getType() == FileType.INPUT)
				if(!fileToDatacenter.containsKey(file.getName())|| !fileToDatacenter.get(file.getName()).contains(device.getId())){
					sendsize += file.getSize();
				}
		}
		return sendsize;
	}
	
	public double getJobOutputFileSize(Job job){
		double sendsize = 0;
		for(FileItem file : job.getFileList()){
			if(file.getType() == FileType.OUTPUT)
				sendsize += file.getSize();
		}
		return sendsize;
	}
	
	public void addFileToDatacenter(FogDevice device, Job job){
		job.setInputsize(getJobInputFileSize(device, job));
		job.setOutputsize(getJobOutputFileSize(job));
		for(FileItem file : job.getFileList()){
			//job输入输出文件都放入数据中心中
			if(fileToDatacenter.containsKey(file.getName()))
				IdList.addAll(fileToDatacenter.get(file.getName()));
			if(!IdList.contains(device.getId()))
				IdList.add(device.getId());
			fileToDatacenter.put(file.getName(), IdList);
			IdList.clear();
//			if(file.getType() == FileType.INPUT){//job输入文件放入数据中心中
//				list = fileToDatacenter.get(file.getName());
//				if(!list.contains(device.getId()))
//					list.add(device.getId());
//				fileToDatacenter.put(file.getName(), list);
//			}
//			if(file.getType() == FileType.OUTPUT){//job输出文件放入数据中心中
//				fileToDatacenter.put(file.getName(), new ArrayList<Integer>(device.getId()));
//			}
		}
	}
	
	public double getJobFileSize(Job job){
		double sendsize = 0;
		for(FileItem file : job.getFileList()){
			if(file.getType() == FileType.INPUT)
				sendsize += file.getSize();
		}
		return sendsize;
	}
	
	@Override
	public void SelectDatacenter(Job job) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void BeforeOffloading(double deadline) {
		// TODO Auto-generated method stub
		
	}
}
