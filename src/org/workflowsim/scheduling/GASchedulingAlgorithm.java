package org.workflowsim.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * The GA Algorithm
 * 
 * @since FogWorkflowSim Toolkit 1.0
 * @author Lina Gong
 */
public class GASchedulingAlgorithm {
	
	public static int popsize;//子代个数
	public static int gmax;//迭代次数
	public static double crossoverProb;//交叉概率
	public static double mutationRate;//变异概率
	
	public static int taskNum;//任务数
	public static int vmNum;//虚拟机数
	public static ArrayList<int[]> schedules=new ArrayList<int[]>();
	public static double totalFitness=0;	
	public static HashMap<Integer,Double> probs=new HashMap<Integer,Double>();
	public static HashMap<Integer,double[]> probSegments=new HashMap<Integer,double[]>();
	public static ArrayList<int[]> children=new ArrayList<int[]>();
	public static ArrayList<int[]> tempParents=new ArrayList<int[]>();
	public static ArrayList<int[]> tempChildren=new ArrayList<int[]>();
	public static double bestFitness=Double.MAX_VALUE;
	public static double bestParentFitness=Double.MAX_VALUE;
	//public static ArrayList<int[]> bestSchedule=new ArrayList<int[]>();
	public static int[] gbestSchedule;
	public static int[] bestParent;
	public static int initFlag=0;
	public static List<Double> fitness=new ArrayList<Double>();
	//public static List<Double> best_fitness=new ArrayList<Double>();
	
	public  static void initPopsRandomly(int taskNum1,int vmNum1)
	{
		taskNum=taskNum1;
		vmNum=vmNum1;
		gbestSchedule=new int[taskNum];
		bestParent=new int[taskNum];
		
		for(int i=0;i<popsize;i++)
		{
			int[] schedule=new int[taskNum];
			for(int j=0;j<taskNum;j++)
			{
				schedule[j]=new Random().nextInt(vmNum);
			}
			schedules.add(schedule);
		}
		initFlag=1;
	}
	
	public static void GA()
	{
		if(children.size()<schedules.size()) {//对每个pop[i]进行交叉变异操作
			//selection phase:select two parents each time.
			for(int i=0;i<2;i++)
			{
				double prob = new Random().nextDouble();
				for (int j = 0; j < schedules.size(); j++)
				{
 					if (isBetween(prob, probSegments.get(j)))
					{
						tempParents.add(schedules.get(j));
						break;
					}
				}
			}
			//cross-over phase.
			int[] p1,p2,p1temp,p2temp;
			p1= tempParents.get(tempParents.size() - 2).clone();
			p1temp= tempParents.get(tempParents.size() - 2).clone();
			p2 = tempParents.get(tempParents.size() -1).clone();
			p2temp = tempParents.get(tempParents.size() -1).clone();
			
			if(new Random().nextDouble()<crossoverProb)
			{
				int crossPosition = new Random().nextInt(taskNum+1);//生成的数字：0-->任务数-1
				//cross-over operation
				for (int i = crossPosition; i < taskNum; i++)
				{
					int temp = p1temp[i];
					p1temp[i] = p2temp[i];
					p2temp[i] = temp;
				}
			}
			tempChildren.add(p1);
			tempChildren.add(p1temp);
			tempChildren.add(p2);
			tempChildren.add(p2temp);
			
			//choose the children if they are better,else keep parents in next iteration.
			//children.add(getFitness(p1temp) < getFitness(p1) ? p1temp : p1);//适应度低的作为children
			//children.add(getFitness(p2temp) < getFitness(p2) ? p2temp : p2);	
			// mutation phase.
			if (new Random().nextDouble() < mutationRate&&children.size()>0)
			{
				// mutation operations bellow.
				int maxIndex = children.size() - 1;

				for (int i = maxIndex - 1; i <= maxIndex; i++)
				{
					operateMutation(children.get(i));//对每个children进行变异
				}
			}
		}
	}
	
	public static void operateMutation(int []child) //变异
	{
		int mutationIndex = new Random().nextInt(taskNum);
		int newVmId = new Random().nextInt(vmNum);
		while (child[mutationIndex] == newVmId)
		{
			newVmId = new Random().nextInt(vmNum);
		}

		child[mutationIndex] = newVmId;
	}
	
	private static boolean isBetween(double prob,double[]segment)
	{
		if(segment[0]<=prob&&prob<=segment[1])
			return true;
		return false;	
	}
	
	public static void getSegments()
	{
		int size=probs.size();
		double start=0;
		double end=0;
		for(int i=0;i<size;i++)
		{
			end=start+probs.get(i);
			double[]segment=new double[2];
			segment[0]=start;
			segment[1]=end;
			probSegments.put(i, segment);
			start=end;
		}
	}

	/**
	 * 初始化所有对象，为了反复实现GA调度算法
	 */
	public static void clear() {
		schedules.removeAll(schedules);
        children.removeAll(children);
        tempParents.removeAll(tempParents);
        tempChildren.removeAll(tempChildren);
        fitness.removeAll(fitness);
        probSegments=new HashMap<Integer,double[]>();
		probs=new HashMap<Integer,Double>();
		initFlag=0;
	}
}
