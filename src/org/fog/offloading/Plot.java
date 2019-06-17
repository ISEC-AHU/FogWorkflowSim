package org.fog.offloading;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import plottest.PlotTest;

public class Plot implements Runnable {
	
	double[][] points;
	int[] labels;
	
	public Plot(double[][] points, int[] labels) {
		// TODO Auto-generated constructor stub
		this.points = points;
		this.labels = labels;
	}

	@SuppressWarnings("finally")
	protected static int plot(double[][] points, int[] labels) {
        MWNumericArray x = null; // 存放x值的数组
        MWNumericArray y = null; // 存放y值的数组
        MWNumericArray z = null; // 存放labels值的数组
        PlotTest plot = null; // 自定义plotter实例，即打包时所指定的类名，根据实际情况更改
         
        int n = points.length;//做图点数  横坐标
        try {
            int[] dims = {n, 1};//几行几列
            x = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            y = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            z = MWNumericArray.newInstance(dims, MWClassID.INT16, MWComplexity.REAL);
             
            for(int i = 1; i <= n; i++) {
                x.set(i, points[i-1][0]);//System.out.print(points[i-1][0]+",");
                y.set(i, points[i-1][1]);//System.out.println(points[i-1][1]);
                z.set(i, labels[i-1]);
            }
             
            //初始化plotter
            plot = new PlotTest();
             
            //做图
            plot.plottest(x, y, z);// 在脚本文件中的函数名，根据实际情更改
            plot.waitForFigures();// 不调用该句，无法弹出绘制图形窗口
            
        } catch (Exception e1) {
            // TODO: handle exception
        } finally {
            MWArray.disposeArray(x);
            MWArray.disposeArray(y);
            if(plot != null) {
                plot.dispose();
            }
            return 1;
        }
   }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		plot(points, labels);
	}
}
