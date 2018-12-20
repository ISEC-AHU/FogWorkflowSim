package org.workflowsim.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The PSO Algorithm
 * 
 * @since FogWorkflowSim Toolkit 1.0
 * @author Lina Gong
 */
public class PsoScheduling {
	
	public static int particleNum;//粒子数
	public static int iterateNum;//迭代次数
	public static double c1;//学习因子c1
	public static double c2;//学习因子c2
	public static double w;//惯性权重
	
	public static int initFlag=0;
	public static List<int[]> schedules=new ArrayList<int[]>();//更新前
	public static List<int[]> newSchedules=new ArrayList<int[]>();//更新后
	public static List<int[]> pbest_schedule=new ArrayList<int[]>();
	public static int[] gbest_schedule;
	public static double[] pbest_fitness;
	public static List<double[]> velocity=new ArrayList<double[]>();
	public static double gbest_fitness=Double.MAX_VALUE;
	public static int[] x;//更新前
	public static int[] pbestSchedule;
	public static double[] v;
	public static int taskNum;
	public static int vmNum;
	
	public static void init(int jobNum,int maxVmNum) {
		pbest_fitness=new double[particleNum];
		  taskNum=jobNum;
		  vmNum=maxVmNum;
		  gbest_schedule=new int[taskNum];
			for(int i=0;i<particleNum;i++)
			{
				x=new int[taskNum];
				pbestSchedule=new int[taskNum];
				double[] v=new double[taskNum];
				for(int j=0;j<taskNum;j++)
				{
					x[j]=new Random().nextInt(vmNum);
					pbestSchedule[j]=x[j];
					v[j]=new Random().nextDouble();
				}
				schedules.add(x);
				pbest_schedule.add(pbestSchedule);
				velocity.add(v);
			}
			initFlag=1;
		}
		
	public static void updateParticles()
	{
		for(int i=0;i<particleNum;i++) {
			int x[]=schedules.get(i);
			double v[]=velocity.get(i);
			int pbest[]=new int[taskNum];
			int temp1[]=new int[taskNum];
			int temp2[]=new int[taskNum];
			double sum[]=new double[taskNum];
			//更新每种调度方案
			pbest=pbest_schedule.get(i);
			for(int j=0;j<x.length;j++) {
				temp1[j] = pbest[j]-x[j];
				temp2[j] = gbest_schedule[j]-x[j];
				double r1 = new Random().nextDouble();
				double r2 = new Random().nextDouble();
				sum[j] = c1*r1*temp1[j]+c2*r2*temp2[j];
				v[j] = w*v[j]+sum[j];
				x[j] = x[j]+(int)v[j];
				if(x[j]>vmNum-1)
					x[j]=vmNum-1;
				if(x[j]<0)
					x[j]=0;
			}
			newSchedules.add(x);
		}
	}
	
	/**
	 * 初始化所有对象，为了反复实现pso调度算法
	 */
	public static void clear() {
		gbest_fitness = Double.MAX_VALUE;
	    initFlag = 0;
        schedules.removeAll(schedules);
        pbest_schedule.removeAll(pbest_schedule);
        velocity.removeAll(velocity);
        newSchedules.removeAll(newSchedules);
        pbest_schedule.removeAll(pbest_schedule);
	}
}
