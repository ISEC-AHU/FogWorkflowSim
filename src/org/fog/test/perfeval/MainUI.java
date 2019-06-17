package org.fog.test.perfeval;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.HarddriveStorage;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
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
import org.workflowsim.CondorVM;
import org.workflowsim.Job;
import org.workflowsim.Task;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.WorkflowPlanner;
import org.workflowsim.utils.ClusteringParameters;
import org.workflowsim.utils.OverheadParameters;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.Parameters.ClassType;
import org.workflowsim.utils.ReplicaCatalog;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import drawbar.DrawBar;
import drawplot2.DrawPicture;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * The MainUI of FogWorkflowSim 2.0
 * 
 * @since FogWorkflowSim Toolkit 1.0
 * @author Lingmin Fan
 * @author Lina Gong
 */

@SuppressWarnings("serial")
public class MainUI extends JFrame {
	final static String[] algrithmStr = new String[]{"MINMIN","MAXMIN","FCFS","ROUNDROBIN","PSO","GA"};
	final static String[] objectiveStr = new String[]{"Time","Energy","Cost"};
	final static String[] inputTypeStr = new String[]{"Montage","CyberShake","Epigenomics","Inspiral","Sipht"};
	final static String[] nodeSizeStr = new String[]{};
	final static String[] cloudNumStr = new String[]{null,"1","2","3","4","5"};
	final static String[] edgeNumStr = new String[]{null,"1","2","3","4","5"};
	final static String[] mobileNumStr = new String[]{null,"1","2","3","4","5"};
	final static String[] strategyStr = new String[]{null,"All-in-Fog","All-in-Cloud","Simple"};
	final static String[] columnNames = {"Job ID", "Task ID", "STATUS", "Data center ID", "VM ID", 
			"Time","Start Time","Finish Time","Depth","Cost","Parents"};//表头元素
	private static JComboBox inputTypeCb = new JComboBox(inputTypeStr);
	private static JComboBox nodeSizeCb = new JComboBox(nodeSizeStr);//任务个数
	private static JComboBox cloudNumCb = new JComboBox(cloudNumStr);
	private static JComboBox edgeNumCb = new JComboBox(edgeNumStr);
	private static JComboBox mobileNumCb = new JComboBox(mobileNumStr);
	private static JComboBox StrategyCb = new JComboBox(strategyStr);
	
	private final static JButton stnBtn = new JButton("Start Simulation");
	private final static JButton cmpBtn = new JButton("Compare");
	
	static boolean Flag = true;//表示需要画图
	static boolean Flag1 = true;//判断FogEnvironmentUI需不需要重新绘制
    static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
    static List<Double[]> record=new ArrayList<Double[]>();
	final static int numOfDepts = 1;
	final static int numOfMobilesPerDept = 1;
	static int nodeSize;

	private static WorkflowEngine wfEngine;
	private static Controller controller;
	
	/**
	 * Main Panel
	 */
	private static JPanel contentPane;

	/**
	 * Fog Computing environment Panel
	 */
	private final static JPanel panel_3 = new JPanel();
	private final static JPanel panel = new JPanel();
	private final static JPanel cloudPanel = new JPanel();
	private final static JPanel fogPanel = new JPanel();
	private final static JPanel mobilePanel = new JPanel();
	private static int cloudNum;
	private static int fogServerNum;
	private static int mobileNum;
	
	/**
	 * Algorithm & Objective Panel
	 */
	final static JPanel panel_2 = new JPanel();
	private final JCheckBox chckbxMinmin = new JCheckBox("MINMIN");
	private final JCheckBox chckbxMaxmin = new JCheckBox("MAXMIN");
	private final JCheckBox chckbxFcfs = new JCheckBox("FCFS");
	private final JCheckBox chckbxRoundrobin = new JCheckBox("ROUNDROBIN");
	private final JCheckBox chckbxGa = new JCheckBox("GA");
	private final JCheckBox chckbxPso = new JCheckBox("PSO");
	static List<JCheckBox> CheckBoxList = new ArrayList<JCheckBox>();
	private final JRadioButton rdbtnTime = new JRadioButton("Time",true);
	private final JRadioButton rdbtnEnergy = new JRadioButton("Energy");
	private final JRadioButton rdbtnCost = new JRadioButton("Cost");
	static ButtonGroup g1 = new ButtonGroup(); //分组进行单选
	private static String scheduler_method;
	private static String optimize_objective;
	
	/**
	 * Workflow Panel
	 */
	private final JPanel panel_1 = new JPanel();
	private final JCheckBox userdefined = new JCheckBox("Custom");
	private static JTextField filepath = new JTextField();
	private final JButton selectfile = new JButton("Select File");
	private static String daxPath;
	private static File XMLFile;
	public static JTextField inputDL;
	
	/**
	 * Output Area
	 */
	private static JScrollPane scrollPane = new JScrollPane();
	private final JLabel lblOutputResultDisplay = new JLabel("Output result display area");
	private static JLabel lblTime = new JLabel("");//display algorithm time
	private static HashMap<String, Object[][]> outputs= new LinkedHashMap<String, Object[][]>();
	private static HashMap<String, Long> TimeMap= new LinkedHashMap<String, Long>();
	private static JTable table;
	private static JComboBox selectdisplay = new JComboBox();
	private final JButton exresult = new JButton("Export");
	/**
	 * Algorithm parameters setting Frame
	 */
	static AlgorithmsSettingUI Settingframe;//算法参数设置小窗口
	static FogEnvironmentUI FEframe;

	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainUI frame = new MainUI();
					frame.setVisible(true);
					Settingframe = new AlgorithmsSettingUI();
					FEframe = new FogEnvironmentUI();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("FogWorkflowSim");
		setBounds(100, 100, 998, 732);
		contentPane = new JPanel();
		contentPane.setLayout(null);
		setContentPane(contentPane);
		this.initUI();
	}
	
	public void initUI() {
		
		initFogEnvironmentPanel();
		initA_OPanel();
		initWorkflowPanel();
		
		stnBtn.setFont(new Font("Consolas", Font.PLAIN, 12));
		stnBtn.setBounds(10, 245, 153, 45);
		stnBtn.addActionListener(new JMHandler());
		contentPane.add(stnBtn);
	    
	    Object[][] rowData = new Object[30][];
	    List<Integer> data = new ArrayList<Integer>();
	    for(int j = 0; j < columnNames.length; j++)
	    	data.add(null);
	    for(int i = 0; i < 30; i++)
	    	rowData[i] = data.toArray();
	    // 创建一个表格，指定 所有行数据 和 表头
		table = new JTable(rowData, columnNames);
		
		// 设置表格内容颜色
		table.setForeground(Color.BLACK);                   // 字体颜色
		table.setFont(new Font("Consolas", Font.PLAIN, 14));      // 字体样式
		table.setSelectionForeground(Color.DARK_GRAY);      // 选中后字体颜色
		table.setSelectionBackground(Color.LIGHT_GRAY);     // 选中后字体背景
		table.setGridColor(Color.LIGHT_GRAY);                     // 网格颜色

        // 设置表头
        table.getTableHeader().setForeground(Color.black);                // 设置表头名称字体颜色
        table.getTableHeader().setResizingAllowed(false);               // 设置不允许手动改变列宽
        table.getTableHeader().setReorderingAllowed(false);             // 设置不允许拖动重新排序各列
        
        table.setFillsViewportHeight(true);
		table.setCellSelectionEnabled(true);
		table.setColumnSelectionAllowed(true);
		table.setPreferredScrollableViewportSize(new Dimension(0, 280));
//		FitTableColumns(table);
		
		scrollPane.setBounds(10, 320, 964, 367);
		contentPane.add(scrollPane);
		scrollPane.setColumnHeaderView(table);
		scrollPane.setViewportView(table);
		
		g1.add(rdbtnTime);
		g1.add(rdbtnEnergy);
		g1.add(rdbtnCost);
		
		StrategyCb.setBounds(180, 39, 92, 21);
		panel_2.add(StrategyCb);
		
		JLabel lblStrategy = new JLabel("Offloading Strategys:");
		lblStrategy.setForeground(Color.BLACK);
		lblStrategy.setFont(new Font("Consolas", Font.PLAIN, 12));
		lblStrategy.setBounds(10, 40, 147, 20);
		panel_2.add(lblStrategy);
		
		cmpBtn.setFont(new Font("Consolas", Font.PLAIN, 12));
		cmpBtn.setBounds(170, 245, 153, 45);
		cmpBtn.addActionListener(new JMHandler());
		contentPane.add(cmpBtn);
		
		lblOutputResultDisplay.setFont(new Font("Consolas", Font.BOLD, 13));
		lblOutputResultDisplay.setBounds(150, 300, 202, 15);
		contentPane.add(lblOutputResultDisplay);
		selectdisplay.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		selectdisplay.setBounds(350, 297, 100, 20);
		contentPane.add(selectdisplay);
		exresult.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		exresult.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int count = selectdisplay.getItemCount();
				System.out.println(count);
				if(count == 0)
					showDialog("No result", "error");
				else if(count == 1){
					Date date = new Date();
					String d = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
					try {
						exportTable(table, null, selectdisplay.getSelectedItem().toString()+"-result-"+d);//结果导出Excel表格
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				else{
					String path = null;
					for(int i = 0; i < count; i++){
						selectdisplay.setSelectedIndex(i);
						Date date = new Date();
						String d = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
						if(i == 0)
							path = d;
						try {
							exportTable(table, path, selectdisplay.getSelectedItem().toString()+"-result-"+d);//结果导出Excel表格
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					try {
						Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL "+ "Explorer.exe /select," 
								 +new File("results").getAbsolutePath()+"\\"+path);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		exresult.setBounds(505, 245, 138, 45);
//		exresult.setEnabled(false);
		contentPane.add(exresult);
		
		JButton btnNewButton = new JButton("Algorithms Setting");
		btnNewButton.setFont(new Font("Consolas", Font.PLAIN, 12));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settingframe.bindEngine(wfEngine);
				Settingframe.setVisible(true);
			}
		});
		btnNewButton.setBounds(330, 245, 168, 45);
		contentPane.add(btnNewButton);
		
		lblTime.setBounds(460, 297, 188, 18);
		contentPane.add(lblTime);
		
		inputTypeCb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				nodeSizeCb.removeAllItems();
				for(String str : getFiles((String)inputTypeCb.getSelectedItem()))
					nodeSizeCb.addItem(str);
			}
		});
		selectdisplay.addItemListener(new JMHandler());
		userdefined.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(userdefined.isSelected()){
					filepath.setEditable(true);
					selectfile.setEnabled(true);
				}
				else{
					XMLFile = null;
					filepath.setEditable(false);
					selectfile.setEnabled(false);
				}
			}
		});
		selectfile.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent e){
		        JFileChooser jfc=new JFileChooser();
		        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		        jfc.setCurrentDirectory(new File("e://dax"));// 文件选择器的初始目录定为e盘
		        FileNameExtensionFilter filter = new FileNameExtensionFilter("xml文件(*.xml)", "xml");
		        jfc.setFileFilter(filter);
		        jfc.showDialog(new JLabel(), "select");
		        if(jfc.getSelectedFile()!=null){
		        	XMLFile = jfc.getSelectedFile();
		        	filepath.setText(XMLFile.getPath());
		        }
		    }});
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(Flag == false){
					int a = showDialog("In drawing, exit FogWorkflowSim?", "question");
					if (a == 0) {
						setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					}
					else if(a == 1){
						setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					}
				}
				else{
					int a = showDialog("Exit FogWorkflowSim?", "question");
					if (a == 0) {
						setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					}
					else if(a == 1){
						setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					}
				}
			}
		});
	}
	
	private void initFogEnvironmentPanel() {
		JLabel lblEnvironmentSetting = new JLabel("Fog Computing Environment Setting");
		JLabel lblNumberOfCloud = new JLabel("Number of Cloud Servers:");
		JLabel lblNumberOfEdge = new JLabel("Number of Fog Nodes:");
		JLabel lblNumberOfMobile = new JLabel("Number of End Devices:");
		panel_3.setBackground(new Color(220, 220, 220));
		panel_3.setBounds(10, 9, 638, 229);
		panel_3.setLayout(null);
		contentPane.add(panel_3);
		
		lblEnvironmentSetting.setBounds(5, 10, 305, 26);
		lblEnvironmentSetting.setFont(new Font("Consolas", Font.BOLD, 16));
		panel_3.add(lblEnvironmentSetting);
		
		panel.setBounds(10, 45, 293, 176);
		panel_3.add(panel);
		panel.setBackground(Color.WHITE);
		panel.setLayout(null);
		
		lblNumberOfCloud.setFont(new Font("Consolas", Font.PLAIN, 12));
		lblNumberOfCloud.setBounds(10, 14, 188, 20);
		lblNumberOfCloud.setForeground(Color.BLACK);
		panel.add(lblNumberOfCloud);
		
		cloudNumCb.setFont(new Font("Consolas", Font.PLAIN, 12));
		cloudNumCb.setBounds(200, 10, 75, 28);
		panel.add(cloudNumCb);
		cloudNumCb.addItemListener(new JMHandler());
		InputLimit(cloudNumCb);
		cloudNumCb.setSelectedItem("1");
		
		lblNumberOfEdge.setFont(new Font("Consolas", Font.PLAIN, 12));
		lblNumberOfEdge.setBounds(10, 60, 188, 20);
		lblNumberOfEdge.setForeground(Color.BLACK);
		panel.add(lblNumberOfEdge);
		edgeNumCb.setFont(new Font("Consolas", Font.PLAIN, 12));
		edgeNumCb.setBounds(200, 56, 75, 28);
		panel.add(edgeNumCb);
		edgeNumCb.addItemListener(new JMHandler());
		InputLimit(edgeNumCb);
		edgeNumCb.setSelectedItem("1");
		
		lblNumberOfMobile.setFont(new Font("Consolas", Font.PLAIN, 12));
		lblNumberOfMobile.setBounds(10, 106, 188, 20);
		lblNumberOfMobile.setForeground(Color.BLACK);
		panel.add(lblNumberOfMobile);
		mobileNumCb.setFont(new Font("Consolas", Font.PLAIN, 12));
		mobileNumCb.setBounds(200, 102, 75, 28);
		panel.add(mobileNumCb);
		mobileNumCb.addItemListener(new JMHandler());
		InputLimit(mobileNumCb);
		mobileNumCb.setSelectedItem("1");
		
		Button view = new Button("More Details");
		view.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Flag1) {
					FEframe.initUI(cloudNum, fogServerNum, mobileNum);
					Flag1 = false;
				}
				//FEframe.setFogDevices(cloudNum, fogServerNum, mobileNum, DCHostMipsMap);
				FEframe.setVisible(true);
			}
		});
		view.setBounds(10, 135, 100, 35);
		panel.add(view);
		
		cloudPanel.setBounds(311, 10, 317, 65);
		panel_3.add(cloudPanel);
		cloudPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		cloudPanel.setLayout(new FlowLayout());
		cloudPanel.setBackground(Color.WHITE);
		
		fogPanel.setBounds(311, 83, 317, 65);
		panel_3.add(fogPanel);
		fogPanel.setLayout(new FlowLayout());
		fogPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		fogPanel.setBackground(Color.WHITE);
		
		mobilePanel.setBounds(311, 156, 317, 65);
		panel_3.add(mobilePanel);
		mobilePanel.setLayout(new FlowLayout());
		mobilePanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		mobilePanel.setBackground(Color.WHITE);
	}
	
	private void initA_OPanel() {
		JLabel lblAlgorithm = new JLabel("Scheduling Algorithms:");
		JLabel lblObjective = new JLabel("Objective:");
		JLabel lblAlgorithmSelection = new JLabel("Strategy & Algorithms & Objective");
		panel_2.setBackground(Color.WHITE);
		panel_2.setBounds(656, 9, 318, 171);
		
		contentPane.add(panel_2);
		panel_2.setLayout(null);
		lblAlgorithm.setFont(new Font("Consolas", Font.PLAIN, 12));
		lblAlgorithm.setBounds(10, 65, 166, 20);
		lblAlgorithm.setForeground(Color.BLACK);
		
		panel_2.add(lblAlgorithm);
		lblObjective.setFont(new Font("Consolas", Font.PLAIN, 12));
		lblObjective.setBounds(10, 145, 78, 20);
		lblObjective.setForeground(Color.BLACK);
		
		panel_2.add(lblObjective);
		lblAlgorithmSelection.setFont(new Font("Consolas", Font.BOLD, 16));
		lblAlgorithmSelection.setBounds(10, 8, 298, 25);
		panel_2.add(lblAlgorithmSelection);
		
		chckbxMinmin.setFont(new Font("Consolas", Font.PLAIN, 12));
		chckbxMinmin.setBackground(Color.WHITE);
		chckbxMinmin.setBounds(10, 91, 68, 23);
		CheckBoxList.add(chckbxMinmin);
		panel_2.add(chckbxMinmin);
		
		chckbxMaxmin.setFont(new Font("Consolas", Font.PLAIN, 12));
		chckbxMaxmin.setBackground(Color.WHITE);
		chckbxMaxmin.setBounds(82, 91, 75, 23);
		CheckBoxList.add(chckbxMaxmin);
		panel_2.add(chckbxMaxmin);
		
		chckbxFcfs.setFont(new Font("Consolas", Font.PLAIN, 12));
		chckbxFcfs.setBackground(Color.WHITE);
		chckbxFcfs.setBounds(154, 91, 56, 23);
		CheckBoxList.add(chckbxFcfs);
		panel_2.add(chckbxFcfs);
		
		chckbxRoundrobin.setFont(new Font("Consolas", Font.PLAIN, 12));
		chckbxRoundrobin.setBackground(Color.WHITE);
		chckbxRoundrobin.setBounds(212, 91, 105, 23);
		CheckBoxList.add(chckbxRoundrobin);
		panel_2.add(chckbxRoundrobin);
		
		chckbxPso.setFont(new Font("Consolas", Font.PLAIN, 12));
		chckbxPso.setBackground(Color.WHITE);
		chckbxPso.setBounds(10, 116, 56, 23);
		CheckBoxList.add(chckbxPso);
		panel_2.add(chckbxPso);
		
		chckbxGa.setFont(new Font("Consolas", Font.PLAIN, 12));
		chckbxGa.setBackground(Color.WHITE);
		chckbxGa.setBounds(82, 116, 68, 23);
		CheckBoxList.add(chckbxGa);
		panel_2.add(chckbxGa);
		
		rdbtnTime.setFont(new Font("Consolas", Font.PLAIN, 12));
		rdbtnTime.setBackground(Color.WHITE);
		rdbtnTime.setBounds(89, 144, 68, 23);
		panel_2.add(rdbtnTime);
		
		rdbtnEnergy.setFont(new Font("Consolas", Font.PLAIN, 12));
		rdbtnEnergy.setBackground(Color.WHITE);
		rdbtnEnergy.setBounds(154, 144, 68, 23);
		panel_2.add(rdbtnEnergy);
		
		rdbtnCost.setFont(new Font("Consolas", Font.PLAIN, 12));
		rdbtnCost.setBackground(Color.WHITE);
		rdbtnCost.setBounds(226, 144, 68, 23);
		panel_2.add(rdbtnCost);
	}

	private void initWorkflowPanel() {
		JLabel lblWorkflowSetting = new JLabel("Workflow Setting");
		JLabel taskTypeLabel = new JLabel("Type:");
		JLabel taskNumLabel = new JLabel("Amount:");
		JLabel lblDeadline = new JLabel("Deadline:");
		inputDL = new JTextField();
		panel_1.setBackground(Color.WHITE);
		panel_1.setBounds(656, 185, 318, 130);
		contentPane.add(panel_1);
		panel_1.setLayout(null);
		
		taskTypeLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
		taskTypeLabel.setBounds(10, 39, 73, 20);
		taskTypeLabel.setForeground(Color.BLACK);
		panel_1.add(taskTypeLabel);
		
		inputTypeCb.setFont(new Font("Consolas", Font.PLAIN, 12));
		inputTypeCb.setBounds(75, 39, 109, 21);
		panel_1.add(inputTypeCb);
		for(String str : getFiles((String)inputTypeCb.getSelectedItem()))
			nodeSizeCb.addItem(str);
		
		taskNumLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
		taskNumLabel.setBounds(190, 39, 60, 20);
		taskNumLabel.setForeground(Color.BLACK);
		panel_1.add(taskNumLabel);
		
		lblWorkflowSetting.setFont(new Font("Consolas", Font.BOLD, 16));
		lblWorkflowSetting.setBounds(86, 10, 159, 21);
		panel_1.add(lblWorkflowSetting);
		
		nodeSizeCb.setFont(new Font("Consolas", Font.PLAIN, 12));
		nodeSizeCb.setBounds(252, 39, 56, 21);
		panel_1.add(nodeSizeCb);
		
		userdefined.setFont(new Font("Consolas", Font.PLAIN, 12));
		userdefined.setBackground(Color.WHITE);
		userdefined.setBounds(6, 69, 67, 23);
		panel_1.add(userdefined);
		
		filepath.setColumns(10);
		filepath.setBounds(75, 69, 132, 23);
		filepath.setEditable(false);
		panel_1.add(filepath);
		
		selectfile.setBounds(215, 69, 98, 23);
		selectfile.setEnabled(false);
		panel_1.add(selectfile);
		
		lblDeadline.setFont(new Font("Consolas", Font.PLAIN, 12));
		lblDeadline.setBounds(10, 104, 85, 20);
		panel_1.add(lblDeadline);
		
		inputDL.setColumns(10);
		inputDL.setBounds(75, 102, 132, 23);
		panel_1.add(inputDL);
	}
	
	@SuppressWarnings("finally")
	protected static int drawplot(int num, ArrayList<Double> fitness,String xz,String yz) {
        MWNumericArray x = null; // 存放x值的数组
        MWNumericArray y = null; // 存放y值的数组
        DrawPicture plot = null; // 自定义plotter实例，即打包时所指定的类名，根据实际情况更改
         
        int n = num;//做图点数  横坐标
        try {
            int[] dims = {1, n};//几行几列
            x = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            y = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
             
            for(int i = 1; i <= n; i++) {
                x.set(i,i);
                y.set(i, fitness.get(i-1));
            }
             
            //初始化plotter
            plot = new DrawPicture();
             
            //做图
            plot.drawplot(x, y, xz, yz);// 在脚本文件中的函数名，根据实际情更改
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
	
	@SuppressWarnings("finally")
	protected static int drawbar(List<Double[]> record) {
        MWNumericArray x = null; // 存放x值的数组
        MWNumericArray y1 = null; // 存放y1值的数组
        MWNumericArray y2 = null; // 存放y2值的数组
        MWNumericArray y3 = null; // 存放y3值的数组
        DrawBar plot = null; // 自定义plotter实例，即打包时所指定的类名，根据实际情况更改

        int n = record.size();//做图点数  横坐标
        
        try {
            int[] dims = {n, 1};//几行几列
            x = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            y1 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            y2 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            y3 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            
            for(int i = 1; i <= n ; i++){
            	Double[] data = record.get(i-1);
            	x.set(i, data[0]);//Algorithm     将矩阵中第i个数设置成某个值
            	y1.set(i, data[1]);//Time
            	y2.set(i, data[2]);//Energy
            	y3.set(i, data[3]);//Cost
            }
            
            //初始化plotter
            plot = new DrawBar();
            
            //做图
            plot.drawbar(x, y1, y2, y3);// 在脚本文件中的函数名，根据实际情更改
            plot.waitForFigures();// 不调用该句，无法弹出绘制图形窗口
             
        } catch (Exception e1) {
            // TODO: handle exception
        } finally {
            MWArray.disposeArray(x);
            MWArray.disposeArray(y1);
            MWArray.disposeArray(y2);
            MWArray.disposeArray(y3);
            if(plot != null) {
                plot.dispose();
            }
            return 1;
        }
   }
        
	 private class JMHandler implements ActionListener,ItemListener  
	    {
	        public void actionPerformed(ActionEvent e)  
	        {
	            if(e.getSource() == stnBtn){
	            	selectdisplay.removeAllItems();
	            	outputs.clear();
	            	record.clear();
	            	lblTime.setText("");
	            	if(GetAlgorithms().size() == 0)
	            		showDialog("Please choose an Alogorithm", "error");
	            	else if(GetAlgorithms().size() > 1)
	            		showDialog("You chose more than one , please click the 'Compare' button", "error");
	            	else{
	            		for(JCheckBox cb : CheckBoxList){
		            		if(cb.isSelected())
		            			scheduler_method=cb.getText();
		            	}
		            	optimize_objective = GetObjective();//获取所选择的优化目标
		            	if(scheduler_method.equals("PSO")){
		            		if(Settingframe.getpsosetting(wfEngine) < 0){     //获取所输入的PSO参数，若无参数则弹出窗口
		            			showDialog("PSO algorithm parameters are not set , please go to the 'Setting' panel", "error");
		            			Settingframe.display(scheduler_method);
		            		}
		            		else{
		            			int repeat = Math.max(1, Settingframe.getpsosetting(wfEngine));
		            			List<Double[]> repeats = new ArrayList<Double[]>();
		            			List<Long> times = new ArrayList<Long>();
		            			for(int i = 0; i < repeat; i++){
		            				System.out.println("---------------------------For the "+(i+1)+" pso--------------------------");
		            				long time = StartAlgorithm();
		            				repeats.add(record.get((record.size()-1)));
		            				record.remove(record.size()-1);
		            				times.add(time);
		            			}
		            			Double[] mean = GetMean(repeats);
		            			Double[] algomean = new Double[4];
		            			algomean[0] = getAlgorithm(scheduler_method);System.out.println(scheduler_method+":");
		            			algomean[1] = mean[0];System.out.println("Average task execution time = "+mean[0]);
		            			algomean[2] = mean[1];System.out.println("Average energy consumption = "+mean[1]);
		            			algomean[3] = mean[2];System.out.println("Average cost = "+mean[2]);
		            			record.add(algomean);
		            			if(wfEngine.getoffloadingEngine().getOffloadingStrategy() != null)
		            				System.out.println("Average offloading Strategy time = " + wfEngine.getAverageOffloadingTime());
		            			long averageTime = GetAverageTime(times);times=null;
		            			System.out.println("Average "+scheduler_method+" algorithm execution time = " + averageTime);
		            			displayTime(averageTime);
		            			System.out.println("Drawing "+scheduler_method+" iteration figure......");
		            			showDialog("Drawing", "information");
		            			Flag = false;
			            		drawplot(wfEngine.iterateNum, wfEngine.updatebest, "Iterations", optimize_objective);
			            		Flag = true;
			            		System.out.println("Finished drawing");
		            		}
		            	}
		            	else if(scheduler_method.equals("GA")){
		            		if(Settingframe.getgasetting(wfEngine) < 0){      //获取所输入的GA参数，若无参数则弹出窗口
		            			showDialog("GA algorithm parameters are not set , please go to the 'Setting' panel", "error");
		            			Settingframe.display(scheduler_method);
		            		}
		            		else{
		            			int repeat = Math.max(1, Settingframe.getgasetting(wfEngine));
		            			List<Double[]> repeats = new ArrayList<Double[]>();
		            			List<Long> times = new ArrayList<Long>();
		            			for(int i = 0; i < repeat; i++){
		            				System.out.println("---------------------------For the "+(i+1)+" ga--------------------------");
		            				long time = StartAlgorithm();
		            				repeats.add(record.get((record.size()-1)));
		            				record.remove(record.size()-1);
		            				times.add(time);
		            			}
		            			Double[] mean = GetMean(repeats);repeats=null;
		            			Double[] algomean = new Double[4];
		            			algomean[0] = getAlgorithm(scheduler_method);System.out.println(scheduler_method+":");
		            			algomean[1] = mean[0];System.out.println("Average task execution time = "+mean[0]);
		            			algomean[2] = mean[1];System.out.println("Average energy consumption = "+mean[1]);
		            			algomean[3] = mean[2];System.out.println("Average cost = "+mean[2]);
		            			record.add(algomean);
		            			if(wfEngine.getoffloadingEngine().getOffloadingStrategy() != null)
		            				System.out.println("Average offloading Strategy time = " + wfEngine.getAverageOffloadingTime());
		            			long averageTime = GetAverageTime(times);times=null;
		            			System.out.println("Average "+scheduler_method+" algorithm execution time = " + averageTime);
		            			displayTime(averageTime);
		            			System.out.println("Drawing "+scheduler_method+" iteration figure......");
		            			showDialog("Drawing", "information");
		            			Flag = false;
			            		drawplot(wfEngine.iterateNum, wfEngine.updatebest, "Iterations", optimize_objective);
			            		Flag = true;
			            		System.out.println("Finished drawing");
		            		}
		            	}
		            	else{//其他算法只支持优化时间
			            	if(!optimize_objective.equalsIgnoreCase("Time"))
			            		showDialog(scheduler_method+" doesn't support '"+optimize_objective+"' objective , only support 'Time'", "error");
			            	else
			            		StartAlgorithm();
		            	}
	            	}
	            }
	            
	            if(e.getSource() == cmpBtn){
	            	selectdisplay.removeAllItems();
	            	outputs.clear();
	            	lblTime.setText("");
	            	record.clear();
	            	Flag = false;
	            	List<String> aList = GetAlgorithms();
	            	if(aList.size() == 0)
	            		showDialog("Please choose an Alogorithm", "error");
	            	else if(aList.size() == 1)
	            		showDialog("You chose one , please click the 'Start Simulation' button", "error");
	            	else{
	            		optimize_objective = GetObjective();//获取所选择的优化目标
		            	for(String al : aList){
		            		System.out.println(al);
		            		scheduler_method = al;
		            		if(scheduler_method.equals("PSO")){
			            		if(Settingframe.getpsosetting(wfEngine) < 0){
			            			//获取所输入的PSO参数，若无参数则弹出窗口
			            			showDialog("PSO algorithm parameters are not set , please go to the 'Setting' panel", "error");
			            			Settingframe.display(scheduler_method);
			            			record.clear();
			            			break;
			            		}
			            		else{
			            			int repeat = Settingframe.getpsosetting(wfEngine);
			            			List<Double[]> repeats = new ArrayList<Double[]>();
			            			List<Long> times = new ArrayList<Long>();
			            			for(int i = 0; i < repeat; i++){
			            				System.out.println("---------------------------For the "+(i+1)+" pso--------------------------");
			            				long time = StartAlgorithm();
			            				repeats.add(record.get((record.size()-1)));
			            				record.remove(record.size()-1);
			            				times.add(time);
			            			}
			            			Double[] mean = GetMean(repeats);
			            			Double[] algomean = new Double[4];
			            			algomean[0] = getAlgorithm(scheduler_method);System.out.println(scheduler_method+":");
			            			algomean[1] = mean[0];System.out.println("Average task execution time = "+mean[0]);
			            			algomean[2] = mean[1];System.out.println("Average energy consumption = "+mean[1]);
			            			algomean[3] = mean[2];System.out.println("Average cost = "+mean[2]);
			            			record.add(algomean);
			            			long averageTime = GetAverageTime(times);
			            			System.out.println("Average "+scheduler_method+" algorithm execution time = " + averageTime);
			            			displayTime(averageTime);
			            		}
			            	}
			            	else if(scheduler_method.equals("GA")){
			            		if(Settingframe.getgasetting(wfEngine) < 0){
			            			//获取所输入的GA参数，若无参数则弹出窗口
			            			showDialog("GA algorithm parameters are not set , please go to the 'Setting' panel", "error");
			            			Settingframe.display(scheduler_method);
			            			record.clear();
			            			break;
			            		}
			            		else{
			            			int repeat = Settingframe.getgasetting(wfEngine);
			            			List<Double[]> repeats = new ArrayList<Double[]>();
			            			List<Long> times = new ArrayList<Long>();
			            			for(int i = 0; i < repeat; i++){
			            				System.out.println("---------------------------For the "+(i+1)+" ga--------------------------");
			            				long time = StartAlgorithm();
			            				repeats.add(record.get((record.size()-1)));
			            				record.remove(record.size()-1);
			            				times.add(time);
			            			}
			            			Double[] mean = GetMean(repeats);
			            			Double[] algomean = new Double[4];
			            			algomean[0] = getAlgorithm(scheduler_method);System.out.println(scheduler_method+":");
			            			algomean[1] = mean[0];System.out.println("Average task execution time = "+mean[0]);
			            			algomean[2] = mean[1];System.out.println("Average energy consumption = "+mean[1]);
			            			algomean[3] = mean[2];System.out.println("Average cost = "+mean[2]);
			            			record.add(algomean);
			            			long averageTime = GetAverageTime(times);
			            			System.out.println("Average "+scheduler_method+" algorithm execution time = " + averageTime);
			            			displayTime(averageTime);
			            		}
			            	}
			            	else{//其他算法只支持优化时间
				            	if(!optimize_objective.equalsIgnoreCase("Time")){
				            		showDialog(scheduler_method+" doesn't support "+optimize_objective+" objective , can't compare", "error");
				            		record.clear();
				            		break;
				            	}
				            	else
				            		StartAlgorithm();
			            	}
		            	}
	            	}
	            	if(!record.isEmpty()){
	            		System.out.println("Drawing algorithms comparison bar......");
	            		showDialog("Drawing", "information");
	            		drawbar(record);
	            		Flag = true;
	            		System.out.println("Finished drawing");
	            	}
	            }
	        }
	        
	        public void itemStateChanged(ItemEvent e){
	        if(e.getItemSelectable() == mobileNumCb) {
	    		if(e.getStateChange() == ItemEvent.SELECTED){
	    			Flag1 = true;
	    			mobilePanel.removeAll();
	    			String itemSize = (String) e.getItem();
	    			mobileNum = Integer.parseInt(itemSize);
	    			for(int i = 0; i < mobileNum; i++) {
    					ImageIcon icon = new ImageIcon(getClass().getResource("/images/mobile.jpg"));
    					icon.setImage(icon.getImage().getScaledInstance(30, 50, Image.SCALE_DEFAULT));
    					JLabel jLabel = new JLabel(icon); 
    					mobilePanel.add(jLabel);
    					validate();
    					repaint();
    				}
	    		}
	        }else if(e.getItemSelectable() == edgeNumCb) {
	        		if(e.getStateChange() == ItemEvent.SELECTED){
	        			Flag1 = true;
		    			fogPanel.removeAll();
		    			String itemSize = (String) e.getItem();
		    			fogServerNum = Integer.parseInt(itemSize);
		    			for(int i = 0; i < fogServerNum; i++) {
	    					ImageIcon icon = new ImageIcon(getClass().getResource("/images/fogServer.jpg"));
	    					icon.setImage(icon.getImage().getScaledInstance(40, 50, Image.SCALE_DEFAULT));
	    					JLabel jLabel = new JLabel(icon); 
	    					fogPanel.add(jLabel);
	    					validate();
	    					repaint();
	    				}
		    		}
	        		
	        	}else if(e.getItemSelectable() == cloudNumCb){
	        		if(e.getStateChange() == ItemEvent.SELECTED){
	        			Flag1 = true;
		    			cloudPanel.removeAll();
		    			String itemSize = (String) e.getItem();
		    			cloudNum = Integer.parseInt(itemSize);
		    			for(int i = 0; i < cloudNum; i++) {
	    					ImageIcon icon = new ImageIcon(getClass().getResource("/images/cloudServer.jpg"));
	    					icon.setImage(icon.getImage().getScaledInstance(40, 50, Image.SCALE_DEFAULT));
	    					JLabel jLabel = new JLabel(icon);
	    					cloudPanel.add(jLabel);
	    					validate();
	    					repaint();
	    				}	
		    		}
	        	}else if(e.getItemSelectable()==selectdisplay){
	        		exresult.setEnabled(true);
	        		String algorithm = (String) e.getItem();
	        		Object[][] rowdata = outputs.get(algorithm);
	        		table = new JTable(rowdata, columnNames);
	        		table.getTableHeader().setForeground(Color.RED);
//	    	        FitTableColumns(table);
	        		scrollPane.setViewportView(table);
	        	}
	    	}
	    }
	 
	 public void simulate(double deadline) {
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
	            if(StrategyCb.getSelectedItem() == null)
	            	wfEngine.getoffloadingEngine().setOffloadingStrategy(null);
	            else{
	            	switch (StrategyCb.getSelectedItem().toString()) {
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
	 
	 public Double getAlgorithm(String scheduler_method) {
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
		 return null;
	}
	
	private void createFogDevices(int userId, String appId) {
			
			double ratePerMips = 0.96;
			double costPerMem = 0.05; // the cost of using memory in this resource
			double costPerStorage = 0.1; // the cost of using storage in this resource
			double costPerBw = 0.2;//每带宽的花费
			
			List<Long> GHzList = new ArrayList<>();//云中的主机
			List<Double> CostList = new ArrayList<>();
			for(JTextField textField : FEframe.DCMipsMap.get("cloud")){
				if(textField.getText().isEmpty())
					GHzList.add((long)10000);
				else
					GHzList.add(Long.valueOf(textField.getText()));
			}
			for(JTextField textField : FEframe.DCCostMap.get("cloud")){
				if(textField.getText().isEmpty())
					CostList.add(0.96);
				else
					CostList.add(Double.valueOf(textField.getText()));
			}
			cloudNumCb.setSelectedItem(String.valueOf(GHzList.size()));
			FogDevice cloud = createFogDevice("cloud", GHzList.size(), GHzList, CostList,
					40000, 100, 10000, 0, ratePerMips, 16*103, 16*83.25,costPerMem,costPerStorage,costPerBw); // creates the fog device Cloud at the apex of the hierarchy with level=0
			cloud.setParentId(-1);
			
			fogDevices.add(cloud);
			for(int i=0;i<numOfDepts;i++){
				addFogNode(i+"", userId, appId, fogDevices.get(0).getId()); // adding a fog device for every Gateway in physical topology. The parent of each gateway is the Proxy Server
			}
		}


		private  FogDevice addFogNode(String id, int userId, String appId, int parentId){
			
			double ratePerMips = 0.48;
			double costPerMem = 0.05; // the cost of using memory in this resource
			double costPerStorage = 0.1; // the cost of using storage in this resource
			double costPerBw = 0.1;//每带宽的花费
			
			List<Long> GHzList = new ArrayList<>();//雾中的主机
			List<Double> CostList = new ArrayList<>();
			for(JTextField textField : FEframe.DCMipsMap.get("fog")){
				if(textField.getText().isEmpty())
					GHzList.add((long)5000);
				else
					GHzList.add(Long.valueOf(textField.getText()));
			}
			for(JTextField textField : FEframe.DCCostMap.get("fog")){
				if(textField.getText().isEmpty())
					CostList.add(0.48);
				else
					CostList.add(Double.valueOf(textField.getText()));
			}
			edgeNumCb.setSelectedItem(String.valueOf(GHzList.size()));
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
		
		private  FogDevice addMobile(String id, int userId, String appId, int parentId){
			double costPerMem = 0.05; // the cost of using memory in this resource
			double costPerStorage = 0.1; // the cost of using storage in this resource
			double costPerBw = 0.3;//每带宽的花费
			
			List<Long> GHzList = new ArrayList<>();
			List<Double> CostList = new ArrayList<>();
			for(JTextField textField : FEframe.DCMipsMap.get("mobile")){
				CostList.add(0.0);
				if(textField.getText().isEmpty())
					GHzList.add((long)1000);
				else
					GHzList.add(Long.valueOf(textField.getText()));
			}
			mobileNumCb.setSelectedItem(String.valueOf(GHzList.size()));
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
		private FogDevice createFogDevice(String nodeName, int hostnum, List<Long> mips, List<Double> costPerMips,
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
	        long bw = 1000;
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
	    
	    /**
	     * mips
	     * @param GHz
	     * @return GHz*1000
	     */
	    private long GHzToMips(double GHz)
	    {
	    	double mips = GHz*1000;
	    	long Mips = new Double(mips).longValue();
	    	return Mips;
	    }
	    
	    /**
	     * Prints the job objects
	     *
	     * @param list list of jobs
	     */
	    protected static void printJobList(String algorithm, List<Job> list) {
	        DecimalFormat dft = new DecimalFormat("######0.00");
	        Object[][] rowData = new Object[nodeSize+1][];
	        int i = 0 , n = 1;
	        
	        for (Job job : list) {
	        	Collection<String> data =new ArrayList<String>(); 
	        	data.add(Integer.toString(job.getCloudletId()));
	            if (job.getClassType() == ClassType.STAGE_IN.value) {
	            	data.add("Stage-in");
	            }
	            for (Task task : job.getTaskList()) {
	            	data.add(Integer.toString(task.getCloudletId()));
	            }

	            if (job.getCloudletStatus() == Cloudlet.SUCCESS) {          	
	            	data.add("SUCCESS");
	            	data.add(job.getResourceName(job.getResourceId()));
	            	data.add(Integer.toString(job.getVmId()));
	            	data.add(dft.format(job.getActualCPUTime()));
	            	data.add(dft.format(job.getExecStartTime()));
	            	data.add(dft.format(job.getFinishTime()));
	            	data.add(Integer.toString(job.getDepth()));
	            	data.add(dft.format(job.getProcessingCost()));
	            	
					List<Task> l = job.getParentList();
	            	String parents ="";
	            	for(Task task : l)
	            		parents += task.getCloudletId()+",";
	            	data.add(parents);
	            } 
	            else if (job.getCloudletStatus() == Cloudlet.FAILED) {
	                data.add("FAILED");
	                data.add(job.getResourceName(job.getResourceId()));
	            	data.add(Integer.toString(job.getVmId()));
	            	data.add(dft.format(job.getActualCPUTime()));
	            	data.add(dft.format(job.getExecStartTime()));
	            	data.add(dft.format(job.getFinishTime()));
	            	data.add(Integer.toString(job.getDepth()));
	            	data.add(dft.format(job.getProcessingCost()));
	            }
	            rowData[i] = data.toArray();
	            i++;
	        }
	        for(String algo : outputs.keySet()){
	        	if(algo.contains(algorithm)){
	        		if(algo.equals(algorithm)){
	        			n = 2;
	        		}
	        		else{
	        			String s = algo.substring(0, algorithm.length());
	        			s = algo.replace(s, "");
	        			n = Integer.parseInt(s);
	        			n++;
	        		}
	        	}
	        }
	        if(outputs.containsKey(algorithm))
	        	algorithm += n;
	        outputs.put(algorithm, rowData);
	        selectdisplay.addItem(algorithm);
	        selectdisplay.setSelectedItem(algorithm);
	    }
	    
	    protected static void printJobList(List<Job> list) {
	    	@SuppressWarnings("resource")
			Formatter formatter = new Formatter(System.out);
	    	String indent = "    ";
	        Log.printLine();
	        Log.printLine("========== OUTPUT ==========");
	        formatter.format("%-8s\t%-12s\t%-8s\t%-17s\t%-10s\t%-8s\t%-12s\t%-13s\t%-10s\t%-10s\n","Job ID","Task ID","STATUS",
	        		"Data center ID","VM ID","Time","Start Time","Finish Time","Depth","Cost");
	        DecimalFormat dft = new DecimalFormat("###.##");
	        

	        for (Job job : list) {
	        	formatter.format("  %-8d\t",job.getCloudletId());
	            //Log.print(indent + job.getCloudletId() + indent + indent);
	            if (job.getClassType() == ClassType.STAGE_IN.value) {
	            	formatter.format("%-10s\t","Stage-in");
	                //Log.print("Stage-in");
	            }
	            for (Task task : job.getTaskList()) {
	            	formatter.format("%-10d\t",task.getCloudletId());
	                //Log.print(task.getCloudletId() + ",");
	            }
	            //Log.print(indent);

	            if (job.getCloudletStatus() == Cloudlet.SUCCESS) {
	            	formatter.format(" SUCCESS\t%-16s\t%-9d\t%-10.2f\t%-12.2f\t%-13.2f\t%-8d\t%-12.2f\t",
	            			job.getResourceName(job.getResourceId()),job.getVmId(),
	            			job.getActualCPUTime(),job.getExecStartTime(),
	            			job.getFinishTime(),job.getDepth(),job.getProcessingCost());
	            	@SuppressWarnings("unchecked")
					List<Task> l = job.getParentList();
	            	for(Task task : l)
	            		System.out.print(task.getCloudletId()+",");
	            	System.out.println();
	            } else if (job.getCloudletStatus() == Cloudlet.FAILED) {
	                Log.print("FAILED");
	                Log.printLine(indent + indent + job.getResourceId() + indent + indent + indent + job.getVmId()
	                        + indent + indent + indent + dft.format(job.getActualCPUTime())
	                        + indent + indent + dft.format(job.getExecStartTime()) + indent + indent + indent
	                        + dft.format(job.getFinishTime()) + indent + indent + indent + job.getDepth());
	            }
	        }
	    }
	    /**
	     * 导出jtable的model到excel
	     * @param table 要导出的jtable
	     * @param filename 要导出的文件名
	     * @throws IOException IO异常
	     */
	    public static void exportTable(JTable table, String path, String filename) throws IOException {
	        try {
	        	File file;
	        	filename += ".xls";
	        	if(path == null){
	        		file = new File("results/"+filename);
	        		Runtime.getRuntime().exec(
							"rundll32 SHELL32.DLL,ShellExec_RunDLL "
							+ "Explorer.exe /select," + file.getAbsolutePath());
	        	}
	        	else{
	        		file = new File("results/"+path+"/");
	        		if(!file.exists())//判断该文件是否存在
	                    file.mkdir();
	        		file = new File("results/"+path+"/"+filename);
	        	}
	            OutputStream out = new FileOutputStream(file);
	            TableModel model = table.getModel();
	            WritableWorkbook wwb = Workbook.createWorkbook(out);
	            // 创建字表，并写入数据
	            WritableSheet ws = wwb.createSheet("Sheet", 0);
	            // 添加标题
	            for (int i = 0; i < model.getColumnCount(); i++) {
	                jxl.write.Label labelN = new jxl.write.Label(i, 0, model.getColumnName(i));
	                try {
	                    ws.addCell(labelN);
	                } catch (RowsExceededException e) {
	                    e.printStackTrace();
	                } catch (WriteException e) {
	                    e.printStackTrace();
	                }
	            }
	            // 添加列
	            for (int i = 0; i < model.getColumnCount(); i++) {
	                for (int j = 1; j <= model.getRowCount(); j++) {
	                    jxl.write.Label labelN = new jxl.write.Label(i, j, model.getValueAt(j - 1, i).toString());
	                    try {
	                        ws.addCell(labelN);
	                    } catch (RowsExceededException e) {
	                        e.printStackTrace();
	                    } catch (WriteException e) {
	                        e.printStackTrace();
	                    }
	                }
	            }
	            wwb.write();
	            wwb.close();
	            
	            System.out.println("write out to: "+filename);
	        } catch (FileNotFoundException e) {
	            JOptionPane.showMessageDialog(null, "Please close the running excel");
	        }
	    }
                                                                                                                                                                                                                                                                                                                      
	    private static void printdevices() {    //输出设备列表
	    	System.out.println("设备列表：");
	    	for(SimEntity entity:CloudSim.getEntityList())
	    		System.out.println("    "+entity.getId()+"  "+entity.getName());
	    }
	    
	    /**
	     * 使得表格显示完整
	     * @param myTable 输入的表格
	     */
		public static void FitTableColumns(JTable myTable){//使得表格显示完整
	    	  JTableHeader header = myTable.getTableHeader();
	    	     int rowCount = myTable.getRowCount();

	    	     Enumeration columns = myTable.getColumnModel().getColumns();
	    	     while(columns.hasMoreElements()){
	    	         TableColumn column = (TableColumn)columns.nextElement();
	    	         int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
	    	         int width = (int)myTable.getTableHeader().getDefaultRenderer()
	    	                 .getTableCellRendererComponent(myTable, column.getIdentifier()
	    	                         , false, false, -1, col).getPreferredSize().getWidth();
	    	         for(int row = 0; row<rowCount; row++){
	    	             int preferedWidth = (int)myTable.getCellRenderer(row, col).getTableCellRendererComponent(myTable,
	    	               myTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
	    	             width = Math.max(width, preferedWidth);
	    	         }
	    	         header.setResizingColumn(column); // 此行很重要
	    	         column.setWidth(width+myTable.getIntercellSpacing().width+20);
	    	     }
	    }
	    
		/**
		 *  清除上次仿真所有对象及标记
		 */
		public static int clear()
		{
			try {
				if(controller==null)
					return 0;
				wfEngine.jobList.removeAll(wfEngine.jobList);
				controller.clear();
				wfEngine.clearFlag();
				fogDevices.removeAll(fogDevices);  //清除对象列表
				FogUtils.set1();
			    Object[][] rowData = {};
			    
			    // 创建一个表格，指定 所有行数据 和 表头
				table = new JTable(rowData, columnNames);
				table.getTableHeader().setForeground(Color.black);
//				FitTableColumns(table);
		        scrollPane.setViewportView(table);
				
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 1;
		}
		
		/**
		 *  获得系统自带所选工作流结构的相关文件任务个数列表
		 * @param filename 工作流结构名称
		 * @return 自带所选工作流结构的任务个数列表
		 */
		public static ArrayList<String> getFiles(String filename) {
		    ArrayList<String> files = new ArrayList<String>();
		    ArrayList<Integer> node = new ArrayList<Integer>();
		    File path = new File("config/dax");
		    for (File file : path.listFiles()){
		    	String name = file.getName();
		    	if(name.contains(filename)){
		    		name = name.replace(filename+"_", "");
		    		name = name.replace(".xml", "");
		    		node.add(Integer.valueOf(name));
		    	}
		    }
		    Collections.sort(node);
	        for (Integer integer : node) {
	    	    files.add(Integer.toString(integer));
	    	}
		    return files;
		}
		
		/**
		 * 针对某个算法进行仿真模拟并记录仿真结果
		 */
		private long StartAlgorithm() {
			clear();
			Log.printLine("Starting FogWorkflowSim version 1.0");
        	System.out.println("Optimization objective : "+optimize_objective);
        	if(XMLFile!=null){//自定义工作流xml文件
        		daxPath = XMLFile.getPath();
        		String path = XMLFile.getName();
        		String str="";
        		if(path != null && !"".equals(path)){
        			for(int i=0;i<path.length();i++){
        				if(path.charAt(i)>=48 && path.charAt(i)<=57){
        					str+=path.charAt(i);
        				}
        			}
        		}
        		nodeSize = Integer.parseInt(str);
        	}
        	else{//系统自带工作流xml文件
        		daxPath="config/dax/"+inputTypeCb.getSelectedItem()+"_"+nodeSizeCb.getSelectedItem()+".xml";
        		nodeSize = Integer.parseInt((String) nodeSizeCb.getSelectedItem());
        	}
        	double deadline = Double.MAX_VALUE;
        	if(!inputDL.getText().isEmpty())
        		deadline = Double.valueOf(inputDL.getText()).doubleValue();
        	simulate(deadline);
        	CloudSim.startSimulation();
        	List<Job> outputList0 = wfEngine.getJobsReceivedList();
            CloudSim.stopSimulation();
            Log.enable();
            printJobList(scheduler_method, outputList0);
//            printJobList(outputList0);
            controller.print();
            Double[] a = {getAlgorithm(scheduler_method),controller.TotalExecutionTime,controller.TotalEnergy,controller.TotalCost};
            record.add(a);
            return wfEngine.algorithmTime;
		}
		
		/**
		 * 获取所选择的算法列表
		 */
		private List<String> GetAlgorithms(){
			List<String> algorithms = new ArrayList<String>();
			for(JCheckBox cb : CheckBoxList){
        		if(cb.isSelected())
        			algorithms.add(cb.getText());
        	}
			return algorithms;
		}
		
		/**
		 * 获得所选择的优化目标
		 */
		private String GetObjective() {
			String objective = null;
			Enumeration<AbstractButton> radioBtns=g1.getElements();
        	while (radioBtns.hasMoreElements()) {
        	    AbstractButton btn = radioBtns.nextElement();
        	    if(btn.isSelected()){
        	    	objective = btn.getText();
        	    }
        	}
        	return objective;
		}
		
		/**
		 * 弹出内容为String的窗口
		 * @param string 提示内容
		 * @param type 窗口类型
		 * @return int an integer indicating the option chosen by the user,
		 * or CLOSED_OPTION if the user closed the dialog
		 */
		private int showDialog(String string, String type){
			int a = Integer.MAX_VALUE;
			if(type.equalsIgnoreCase("error")){
				Object[] options = {"OK"};
				JOptionPane.showOptionDialog(getContentPane(), string, "Error",
						JOptionPane.YES_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			}
			else if(type.equalsIgnoreCase("information")){
				Object[] options = {"OK"};
    			JOptionPane.showOptionDialog(getContentPane(), string, "Information",JOptionPane.YES_NO_OPTION, 
    					JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
			}
			else if(type.equalsIgnoreCase("question")){
				Object[] options = {"Yes","No"};
				a = JOptionPane.showOptionDialog(getContentPane(), string, "Confirm Exit",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			}
			return a;
		}
		
		/**
		 * 对list中的double类型元素分别求平均值
		 * @param list
		 * @return 求完平均值后的double类型数组
		 */
		private Double[] GetMean(List<Double[]> list) {
			double a = 0;
			double b = 0;
			double c = 0;
			for(Double[] d : list){
				a += d[1];
				b += d[2];
				c += d[3];
			}
			a = a / list.size();
			b = b / list.size();
			c = c / list.size();
			Double[] r = {a,b,c};
			return r;
		}
		
		private long GetAverageTime(List<Long> list){
			long sum = 0;
			for(long i : list){
				sum += i;
			}
			return sum/Math.max(1,list.size());
		}
		
		private void displayTime(long time) {
			String s = lblTime.getText();
            s += "   "+scheduler_method+":"+time+"ms";
            lblTime.setText(s);
		}
		
		/**
		 * 设置 comoBox 可输入并且限制输入只能是数字
		 * @param comboBox
		 */
		private void InputLimit(JComboBox comboBox) {
			comboBox.setEditable(true);
			ComboBoxEditor editor = comboBox.getEditor();
			JTextField textField = (JTextField)editor.getEditorComponent();
			textField.setDocument(new NumericDocument3());
		}
		
		private class NumericDocument3 extends PlainDocument {
			private static final long serialVersionUID = 1L;

			public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
				if (str == null) {
					return;
					}
				if ((getLength() + str.length()) <= 3) {
					int length = 0;
					char[] upper = str.toCharArray();
					for (int i = 0; i < upper.length; i++) {
						if(offs+i==0 && upper[i]=='0'){
							
						}else if (upper[i] >= '0' && upper[i] <= '9') {
							upper[length++] = upper[i];
							}
						}
					super.insertString(offs, new String(upper, 0, length), a);
					}
				}
		}
}