package org.fog.test.perfeval;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
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
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

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
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.workflowsim.CondorVM;
import org.workflowsim.Job;
import org.workflowsim.Task;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.WorkflowPlanner;
import org.workflowsim.scheduling.GASchedulingAlgorithm;
import org.workflowsim.scheduling.PsoScheduling;
import org.workflowsim.utils.ClusteringParameters;
import org.workflowsim.utils.OverheadParameters;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.Parameters.ClassType;
import org.workflowsim.utils.ReplicaCatalog;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.sun.javafx.collections.MappingChange.Map;

import drawbar.DrawBar;
import drawplot2.DrawPicture;

/**
 * The UI frame of FogWorkflowSim 1.0
 * 
 * @since FogWorkflowSim Toolkit 1.0
 * @author Lingmin Fan
 * @author Lina Gong
 */
@SuppressWarnings("serial")
public class UI2 extends JFrame {
	String [] algrithmStr=new String[]{"MINMIN","MAXMIN","FCFS","ROUNDROBIN","PSO","GA"};
	String [] objectiveStr=new String[]{"Time","Energy","Cost"};
	String [] inputTypeStr=new String[]{"Montage","CyberShake","Epigenomics","Inspiral","Sipht"};
	String [] nodeSizeStr=new String[]{};
	String [] cloudNumStr=new String[]{"1","2","3","4","5"};
	String [] edgeNumStr=new String[]{"1","2","3"};
	String [] mobileNumStr=new String[]{"1","2","3"};
	static String[] columnNames = {"Job ID", "Task ID", "STATUS", "Data center ID", "VM ID", 
			"Time","Start Time","Finish Time","Depth","Cost","Parents"};//表头元素
	private JComboBox inputTypeCb = new JComboBox(inputTypeStr);
	private JComboBox nodeSizeCb = new JComboBox(nodeSizeStr);//任务个数
	private JComboBox cloudNumCb = new JComboBox(cloudNumStr);
	private JComboBox edgeNumCb = new JComboBox(edgeNumStr);
	private JComboBox mobileNumCb = new JComboBox(mobileNumStr);
	private JButton stnBtn;
	private JButton cmpBtn;
	private JPanel cloudPanel = new JPanel();
	private JPanel fogPanel = new JPanel();
	private JPanel mobilePanel = new JPanel();
	private JLabel jLabel;

	private JPanel contentPane;
	private JPanel settingPane;
	private static JScrollPane scrollPane = new JScrollPane();
	
	private File XMLFile;
	private File psosetting;
	private File gasetting;
	private File AlgorithmFile;
	
	private String scheduler_method;
	private String optimize_objective;
	private String daxPath;
	
	private int cloudNum;
	private int fogServerNum;
	private int mobileNum;
	
	static boolean Flag = true;//表示需要画图
	
    static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
    List<Double[]> record=new ArrayList<Double[]>();
	
	static int numOfDepts = 1;
	static int numOfMobilesPerDept = 1;
	static int nodeSize;
	private final JLabel lblNumberOfCloud = new JLabel("Number of Cloud Servers:");
	private final JLabel lblNumberOfEdge = new JLabel("Number of Fog Servers:");
	private final JLabel lblNumberOfMobile = new JLabel("Number of Mobiles:");
	private static WorkflowEngine wfEngine;
	private static Controller controller;
	private final JPanel panel = new JPanel();
	private final JPanel panel_1 = new JPanel();
	private final JLabel taskTypeLabel = new JLabel("Type:");
	private final JLabel taskNumLabel = new JLabel("Amount:");
	private final JPanel panel_2 = new JPanel();
	private final JLabel lblAlgorithm = new JLabel("Algorithms:");
	private final JLabel lblObjective = new JLabel("Objective:");
	private final JLabel lblEnvironmentSetting = new JLabel("Fog Computing Environment Setting");
	private final JLabel lblWorkflowSetting = new JLabel("Workflow Setting");
	private final JLabel lblAlgorithmSelection = new JLabel("Algorithms & Objective");
	private final JPanel panel_3 = new JPanel();
	private static JTable table;
	private static JComboBox selectdisplay = new JComboBox();
	
	private final JCheckBox chckbxMinmin = new JCheckBox("MINMIN");
	private final JCheckBox chckbxMaxmin = new JCheckBox("MAXMIN");
	private final JCheckBox chckbxFcfs = new JCheckBox("FCFS");
	private final JCheckBox chckbxRoundrobin = new JCheckBox("ROUNDROBIN");
	private final JCheckBox chckbxGa = new JCheckBox("GA");
	private final JCheckBox chckbxPso = new JCheckBox("PSO");
	private List<JCheckBox> CheckBoxList = new ArrayList<JCheckBox>();
	private final JRadioButton rdbtnTime = new JRadioButton("Time",true);
	private final JRadioButton rdbtnEnergy = new JRadioButton("Energy");
	private final JRadioButton rdbtnCost = new JRadioButton("Cost");
	ButtonGroup g1 = new ButtonGroup(); //分组进行单选
	private final JCheckBox userdefined = new JCheckBox("Custom");
	private JTextField filepath = new JTextField();;
	private final JButton selectfile = new JButton("Select File");
	JTabbedPane jp = new JTabbedPane(JTabbedPane.LEFT);//设置选项卡在坐标 
	
	private final JPanel AlgorithmsPanel = new JPanel();
	private final JPanel psoSettingPanel = new JPanel();
	private final JPanel gaSettingPanel = new JPanel();
	private HashMap<JLabel, JPanel> LabeltoPanel = new HashMap<JLabel, JPanel>();
	
	private final JLabel pso1 = new JLabel("Number of Particles:");
	private JTextField particleNum = new JTextField();
	private final JLabel pso2 = new JLabel("Number of Iterations:");
	private JTextField psoiterate = new JTextField();
	private final JLabel pso3 = new JLabel("Learning Factor c1:");
	private final JTextField c1 = new JTextField();
	private final JLabel pso4 = new JLabel("Learning Factor c2:");
	private final JTextField c2 = new JTextField();
	private final JLabel pso5 = new JLabel("Inertia Weight:");
	private final JTextField weight = new JTextField();
	private final JLabel pso6 = new JLabel("Repeated experiment:");
	private final JTextField psoSimNum = new JTextField();
	private final JLabel ga5 = new JLabel("Repeated experiment:");
	private final JTextField gaSimNum = new JTextField();
	private final JButton psoout = new JButton("Export");
	private final JTextField psoxml = new JTextField();
	private final JButton psoselect = new JButton("Select File");
	private final JButton psoin = new JButton("Import");
	private final JButton gaout = new JButton("Export");
	private final JTextField gaxml = new JTextField();
	private final JButton gaselect = new JButton("Select File");
	private final JButton gain = new JButton("Import");
	private final JLabel lblOutputResultDisplay = new JLabel("Output result display area");
	private JTextField populationsize = new JTextField();
	private JTextField gaiterate = new JTextField();
	private JTextField cross = new JTextField();
	private JTextField mutate = new JTextField();
	private static HashMap<String,Object[][]> outputs= new LinkedHashMap<String, Object[][]>();
	private final JLabel lblMaxmin = new JLabel("MaxMin");
	private final JButton btnAddAlgorithm = new JButton("Add Algorithm");
	private final JLabel Customlabel = new JLabel("");
	private final JPanel CustomASettingPanel = new JPanel();
	private final JLabel lblCustomAlgorithmParameter = new JLabel();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UI2 frame = new UI2();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public UI2() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("FogWorkflowSim");
		setBounds(100, 100, 1063, 775);
		jp.setPreferredSize(new Dimension(2000,2000));
		
		contentPane = new JPanel();
		contentPane.setLayout(null);
		jp.add("Main", contentPane);//添加子容器  并且为选项卡添加名字
		
		settingPane=new JPanel();
		settingPane.setLayout(null);
		jp.add("Setting", settingPane);
		
		jp.setEnabledAt(1, true);
		
		getContentPane().add(jp,BorderLayout.CENTER);//将选项卡窗体添加到主窗体上去
		
		this.initUI();
	}
	
	public void initUI() {
		
		stnBtn = new JButton("Start Simulation");
		stnBtn.setFont(new Font("Consolas", Font.PLAIN, 12));
		stnBtn.setBounds(656, 289, 153, 45);
		stnBtn.addActionListener(new JMHandler());
		contentPane.add(stnBtn);
		ImageIcon icon =new ImageIcon(getClass().getResource("/images/cloudServer2.jpg"));
		icon.setImage(icon.getImage().getScaledInstance(60, 70,
				Image.SCALE_DEFAULT));
		validate();
		repaint();
		ImageIcon icon2 =new ImageIcon(getClass().getResource("/images/fogServer.jpg"));
		icon.setImage(icon.getImage().getScaledInstance(60, 70,
				Image.SCALE_DEFAULT));
		validate();
		repaint();
		ImageIcon icon3 =new ImageIcon(getClass().getResource("/images/mobile.jpg"));
		icon.setImage(icon.getImage().getScaledInstance(60, 70,
				Image.SCALE_DEFAULT));
		validate();
		repaint();
	    
	    Object[][] rowData = {};
	    
	    // 创建一个表格，指定 所有行数据 和 表头
		table = new JTable(rowData, columnNames);
		
		// 设置表格内容颜色
		table.setForeground(Color.BLACK);                   // 字体颜色
		table.setFont(new Font("Consolas", Font.PLAIN, 14));      // 字体样式
		table.setSelectionForeground(Color.DARK_GRAY);      // 选中后字体颜色
		table.setSelectionBackground(Color.LIGHT_GRAY);     // 选中后字体背景
		table.setGridColor(Color.GRAY);                     // 网格颜色

        // 设置表头
        table.getTableHeader().setForeground(Color.black);                // 设置表头名称字体颜色
        table.getTableHeader().setResizingAllowed(false);               // 设置不允许手动改变列宽
        table.getTableHeader().setReorderingAllowed(false);             // 设置不允许拖动重新排序各列
        
        table.setFillsViewportHeight(true);
		table.setCellSelectionEnabled(true);
		table.setColumnSelectionAllowed(true);
		table.setPreferredScrollableViewportSize(new Dimension(0, 280));
//		FitTableColumns(table);
		
		scrollPane.setBounds(10, 358, 964, 367);
		contentPane.add(scrollPane);
		scrollPane.setColumnHeaderView(table);
		scrollPane.setViewportView(table);
		panel_1.setBackground(Color.WHITE);
		panel_1.setBounds(656, 173, 318, 106);
		contentPane.add(panel_1);
		panel_1.setLayout(null);
		
		taskTypeLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
		taskTypeLabel.setBounds(10, 42, 73, 20);
		taskTypeLabel.setForeground(Color.BLACK);
		panel_1.add(taskTypeLabel);
		
		inputTypeCb.setFont(new Font("Consolas", Font.PLAIN, 12));
		inputTypeCb.setBounds(75, 42, 109, 21);
		panel_1.add(inputTypeCb);
		for(String str : getFiles((String)inputTypeCb.getSelectedItem()))
			nodeSizeCb.addItem(str);
		
		taskNumLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
		taskNumLabel.setBounds(190, 42, 60, 20);
		taskNumLabel.setForeground(Color.BLACK);
		panel_1.add(taskNumLabel);
		
		lblWorkflowSetting.setFont(new Font("Consolas", Font.BOLD, 16));
		lblWorkflowSetting.setBounds(86, 10, 159, 21);
		panel_1.add(lblWorkflowSetting);
		nodeSizeCb.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		nodeSizeCb.setBounds(252, 42, 56, 21);
		panel_1.add(nodeSizeCb);
		userdefined.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		userdefined.setBackground(Color.WHITE);
		userdefined.setBounds(6, 72, 67, 23);
		panel_1.add(userdefined);
		
		filepath.setColumns(10);
		filepath.setBounds(75, 72, 138, 23);
		filepath.setEditable(false);
		panel_1.add(filepath);
		
		selectfile.setBounds(218, 72, 98, 23);
		selectfile.setEnabled(false);
		panel_1.add(selectfile);
		
		panel_2.setBackground(Color.WHITE);
		panel_2.setBounds(656, 9, 318, 154);
		
		contentPane.add(panel_2);
		panel_2.setLayout(null);
		lblAlgorithm.setFont(new Font("Consolas", Font.PLAIN, 12));
		lblAlgorithm.setBounds(10, 46, 78, 20);
		lblAlgorithm.setForeground(Color.BLACK);
		
		panel_2.add(lblAlgorithm);
		lblObjective.setFont(new Font("Consolas", Font.PLAIN, 12));
		lblObjective.setBounds(10, 127, 78, 20);
		lblObjective.setForeground(Color.BLACK);
		
		panel_2.add(lblObjective);
		lblAlgorithmSelection.setFont(new Font("Consolas", Font.BOLD, 16));
		lblAlgorithmSelection.setBounds(70, 10, 210, 25);
		panel_2.add(lblAlgorithmSelection);
		
		chckbxMinmin.setFont(new Font("Consolas", Font.PLAIN, 12));
		chckbxMinmin.setBackground(Color.WHITE);
		chckbxMinmin.setBounds(6, 73, 68, 23);
		CheckBoxList.add(chckbxMinmin);
		panel_2.add(chckbxMinmin);
		
		chckbxMaxmin.setFont(new Font("Consolas", Font.PLAIN, 12));
		chckbxMaxmin.setBackground(Color.WHITE);
		chckbxMaxmin.setBounds(78, 73, 75, 23);
		CheckBoxList.add(chckbxMaxmin);
		panel_2.add(chckbxMaxmin);
		
		chckbxFcfs.setFont(new Font("Consolas", Font.PLAIN, 12));
		chckbxFcfs.setBackground(Color.WHITE);
		chckbxFcfs.setBounds(150, 73, 56, 23);
		CheckBoxList.add(chckbxFcfs);
		panel_2.add(chckbxFcfs);
		
		chckbxRoundrobin.setFont(new Font("Consolas", Font.PLAIN, 12));
		chckbxRoundrobin.setBackground(Color.WHITE);
		chckbxRoundrobin.setBounds(208, 73, 105, 23);
		CheckBoxList.add(chckbxRoundrobin);
		panel_2.add(chckbxRoundrobin);
		
		chckbxPso.setFont(new Font("Consolas", Font.PLAIN, 12));
		chckbxPso.setBackground(Color.WHITE);
		chckbxPso.setBounds(6, 98, 56, 23);
		CheckBoxList.add(chckbxPso);
		panel_2.add(chckbxPso);
		
		chckbxGa.setFont(new Font("Consolas", Font.PLAIN, 12));
		chckbxGa.setBackground(Color.WHITE);
		chckbxGa.setBounds(78, 98, 68, 23);
		CheckBoxList.add(chckbxGa);
		panel_2.add(chckbxGa);
		
		rdbtnTime.setFont(new Font("Consolas", Font.PLAIN, 12));
		rdbtnTime.setBackground(Color.WHITE);
		rdbtnTime.setBounds(85, 126, 68, 23);
		panel_2.add(rdbtnTime);
		
		rdbtnEnergy.setFont(new Font("Consolas", Font.PLAIN, 12));
		rdbtnEnergy.setBackground(Color.WHITE);
		rdbtnEnergy.setBounds(152, 126, 68, 23);
		panel_2.add(rdbtnEnergy);
		
		rdbtnCost.setFont(new Font("Consolas", Font.PLAIN, 12));
		rdbtnCost.setBackground(Color.WHITE);
		rdbtnCost.setBounds(222, 126, 68, 23);
		panel_2.add(rdbtnCost);
		
		g1.add(rdbtnTime);
		g1.add(rdbtnEnergy);
		g1.add(rdbtnCost);
		
		panel_3.setBackground(new Color(220, 220, 220));
		panel_3.setBounds(10, 9, 638, 325);
		panel_3.setLayout(null);
		contentPane.add(panel_3);
		
		lblEnvironmentSetting.setBounds(5, 10, 305, 26);
		lblEnvironmentSetting.setFont(new Font("Consolas", Font.BOLD, 16));
		panel_3.add(lblEnvironmentSetting);
		panel.setBounds(10, 45, 293, 270);
		panel_3.add(panel);
		
		panel.setBackground(Color.WHITE);
		panel.setLayout(null);
		lblNumberOfCloud.setFont(new Font("Consolas", Font.PLAIN, 12));
		lblNumberOfCloud.setBounds(10, 14, 188, 20);
		panel.add(lblNumberOfCloud);
		lblNumberOfCloud.setForeground(Color.BLACK);
		cloudNumCb.setFont(new Font("Consolas", Font.PLAIN, 12));
		cloudNumCb.setBounds(200, 10, 75, 28);
		panel.add(cloudNumCb);
		lblNumberOfEdge.setFont(new Font("Consolas", Font.PLAIN, 12));
		lblNumberOfEdge.setBounds(10, 108, 188, 20);
		panel.add(lblNumberOfEdge);
		lblNumberOfEdge.setForeground(Color.BLACK);
		edgeNumCb.setFont(new Font("Consolas", Font.PLAIN, 12));
		edgeNumCb.setBounds(200, 104, 75, 28);
		panel.add(edgeNumCb);
		lblNumberOfMobile.setFont(new Font("Consolas", Font.PLAIN, 12));
		lblNumberOfMobile.setBounds(10, 210, 188, 20);
		panel.add(lblNumberOfMobile);
		lblNumberOfMobile.setForeground(Color.BLACK);
		mobileNumCb.setFont(new Font("Consolas", Font.PLAIN, 12));
		mobileNumCb.setBounds(200, 206, 75, 28);
		panel.add(mobileNumCb);
		cloudPanel.setBounds(311, 10, 317, 95);
		panel_3.add(cloudPanel);
		
		cloudPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		cloudPanel.setLayout(new FlowLayout());
		cloudPanel.setBackground(Color.WHITE);
		jLabel=new JLabel(icon); 
		cloudPanel.add(jLabel);
		fogPanel.setBounds(311, 115, 317, 95);
		panel_3.add(fogPanel);
		
		fogPanel.setLayout(new FlowLayout());
		fogPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		fogPanel.setBackground(Color.WHITE);
		jLabel=new JLabel(icon2); 
		fogPanel.add(jLabel);
		mobilePanel.setBounds(311, 220, 317, 95);
		panel_3.add(mobilePanel);
		
		mobilePanel.setLayout(new FlowLayout());
		mobilePanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		mobilePanel.setBackground(Color.WHITE);
		jLabel=new JLabel(icon3); 
		mobilePanel.add(jLabel);
		
		cmpBtn = new JButton("Compare");
		cmpBtn.setFont(new Font("Consolas", Font.PLAIN, 12));
		cmpBtn.setBounds(821, 289, 153, 45);
		cmpBtn.addActionListener(new JMHandler());
		contentPane.add(cmpBtn);
		
		lblOutputResultDisplay.setFont(new Font("Consolas", Font.BOLD, 13));
		lblOutputResultDisplay.setBounds(300, 340, 202, 15);
		contentPane.add(lblOutputResultDisplay);
		selectdisplay.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		selectdisplay.setBounds(520, 336, 100, 20);
		contentPane.add(selectdisplay);
		
		psoSettingPanel.setBackground(Color.WHITE);
		settingPane.add(psoSettingPanel);
		psoSettingPanel.setLayout(null);
		psoSettingPanel.setBounds(228, 27, 309, 409);
		
		JLabel lblPso = new JLabel("PSO Algorithm Parameter Setting");
		lblPso.setFont(new Font("Consolas", Font.BOLD, 16));
		lblPso.setBounds(12, 10, 289, 25);
		psoSettingPanel.add(lblPso);
		
		pso1.setFont(new Font("Consolas", Font.PLAIN, 12));
		pso1.setBounds(42, 48, 148, 15);
		psoSettingPanel.add(pso1);
		
		particleNum.setColumns(10);
		particleNum.setBounds(194, 45, 76, 22);
		psoSettingPanel.add(particleNum);
		
		pso2.setFont(new Font("Consolas", Font.PLAIN, 12));
		pso2.setBounds(42, 86, 148, 15);
		psoSettingPanel.add(pso2);
		
		psoiterate.setColumns(10);
		psoiterate.setBounds(194, 83, 76, 22);
		psoSettingPanel.add(psoiterate);
		
		pso3.setFont(new Font("Consolas", Font.PLAIN, 12));
		pso3.setBounds(42, 124, 165, 15);
		psoSettingPanel.add(pso3);
		
		c1.setColumns(10);
		c1.setBounds(194, 121, 76, 22);
		psoSettingPanel.add(c1);
		
		pso4.setFont(new Font("Consolas", Font.PLAIN, 12));
		pso4.setBounds(42, 163, 165, 15);
		psoSettingPanel.add(pso4);
		
		c2.setColumns(10);
		c2.setBounds(194, 160, 76, 22);
		psoSettingPanel.add(c2);
		
		pso5.setFont(new Font("Consolas", Font.PLAIN, 12));
		pso5.setBounds(42, 201, 148, 15);
		psoSettingPanel.add(pso5);
		
		weight.setColumns(10);
		weight.setBounds(194, 198, 76, 22);
		psoSettingPanel.add(weight);
		psoSettingPanel.setVisible(false);
		
		psoout.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		psoout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Element root = new Element("PSO");
				Document dom = new Document(root);
				Element element1 = new Element(pso1.getText().substring(0, pso1.getText().length()-1).replaceAll(" ", "_"));
				element1.setText(particleNum.getText());
				root.addContent(element1);
				Element element2 = new Element(pso2.getText().substring(0, pso2.getText().length()-1).replaceAll(" ", "_"));
				element2.setText(psoiterate.getText());
				root.addContent(element2);
				Element element3 = new Element(pso3.getText().substring(0, pso3.getText().length()-1).replaceAll(" ", "_"));
				element3.setText(c1.getText());
				root.addContent(element3);
				Element element4 = new Element(pso4.getText().substring(0, pso4.getText().length()-1).replaceAll(" ", "_"));
				element4.setText(c2.getText());
				root.addContent(element4);
				Element element5 = new Element(pso5.getText().substring(0, pso5.getText().length()-1).replaceAll(" ", "_"));
				element5.setText(weight.getText());
				root.addContent(element5);
				Element element6 = new Element(pso6.getText().substring(0, pso6.getText().length()-1).replaceAll(" ", "_"));
				element6.setText(psoSimNum.getText());
				root.addContent(element6);
				Format format = Format.getCompactFormat();
	            format.setIndent(" ");
				XMLOutputter outputter=new XMLOutputter(format);
				Date date = new Date();
				String d = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
				try {
					File file = new File("parameter-setting/PSO/psosetting"+d+".xml");
					outputter.output(dom, new FileOutputStream(file));
					System.out.println("生成xml成功");
					//打开文件所在目录并选中该文件
					Runtime.getRuntime().exec(
							"rundll32 SHELL32.DLL,ShellExec_RunDLL "
							+ "Explorer.exe /select," + file.getAbsolutePath());
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		psoout.setBounds(112, 277, 93, 23);
		psoSettingPanel.add(psoout);
		psoxml.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		psoxml.setColumns(10);
		psoxml.setBounds(12, 310, 289, 23);
		psoSettingPanel.add(psoxml);
		psoselect.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		psoselect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
		        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		        jfc.setCurrentDirectory(new File("parameter-setting/PSO"));// 文件选择器的初始目录
		        FileNameExtensionFilter filter = new FileNameExtensionFilter("xml文件(*.xml)", "xml");
		        jfc.setFileFilter(filter);
		        jfc.showDialog(new JLabel(), "select");
		        if(jfc.getSelectedFile()!=null){
		        	psosetting = jfc.getSelectedFile();
		        	psoxml.setText(psosetting.getPath());
		        }
		        psoin.setEnabled(true);
			}
		});
		psoselect.setBounds(100, 343, 115, 23);
		psoSettingPanel.add(psoselect);
		psoin.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		psoin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SAXBuilder builder = new SAXBuilder();
				Document dom = null;
				try {
					dom = builder.build(psosetting);
				} catch (JDOMException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
	            Element root = dom.getRootElement();
	            List<Element> list = root.getChildren();
	            for (Element node : list) {
	            	if(pso1.getText().substring(0, pso1.getText().length()-1).replaceAll(" ", "_").equals(node.getName()))
	            		particleNum.setText(node.getText());
	            	else if(pso2.getText().substring(0, pso2.getText().length()-1).replaceAll(" ", "_").equals(node.getName()))
	            		psoiterate.setText(node.getText());
	            	else if(pso3.getText().substring(0, pso3.getText().length()-1).replaceAll(" ", "_").equals(node.getName()))
	            		c1.setText(node.getText());
	            	else if(pso4.getText().substring(0, pso4.getText().length()-1).replaceAll(" ", "_").equals(node.getName()))
	            		c2.setText(node.getText());
	            	else if(pso5.getText().substring(0, pso5.getText().length()-1).replaceAll(" ", "_").equals(node.getName()))
	            		weight.setText(node.getText());
	            	else if(pso6.getText().substring(0, pso6.getText().length()-1).replaceAll(" ", "_").equals(node.getName()))
	            		psoSimNum.setText(node.getText());
	            }
			}
		});
		psoin.setBounds(112, 376, 93, 23);
		psoin.setEnabled(false);
		psoSettingPanel.add(psoin);
		
		
		pso6.setFont(new Font("Consolas", Font.PLAIN, 12));
		pso6.setBounds(42, 239, 148, 15);
		psoSettingPanel.add(pso6);
		
		
		psoSimNum.setColumns(10);
		psoSimNum.setBounds(194, 236, 76, 22);
		psoSettingPanel.add(psoSimNum);
		
		gaSettingPanel.setLayout(null);
		gaSettingPanel.setBackground(Color.WHITE);
//		gaSettingPanel.setBounds(575, 27, 309, 409);
		gaSettingPanel.setBounds(228, 27, 309, 409);
		settingPane.add(gaSettingPanel);
		
		JLabel lblGa = new JLabel("GA Algorithm Parameter Setting");
		lblGa.setFont(new Font("Consolas", Font.BOLD, 16));
		lblGa.setBounds(15, 10, 278, 25);
		gaSettingPanel.add(lblGa);
		
		JLabel ga1 = new JLabel("Population Size:");
		ga1.setFont(new Font("Consolas", Font.PLAIN, 12));
		ga1.setBounds(37, 48, 148, 15);
		gaSettingPanel.add(ga1);
		
		populationsize.setColumns(10);
		populationsize.setBounds(189, 45, 76, 22);
		gaSettingPanel.add(populationsize);
		
		JLabel ga2 = new JLabel("Number of Iterations:");
		ga2.setFont(new Font("Consolas", Font.PLAIN, 12));
		ga2.setBounds(37, 94, 148, 15);
		gaSettingPanel.add(ga2);
		
		gaiterate.setColumns(10);
		gaiterate.setBounds(189, 91, 76, 22);
		gaSettingPanel.add(gaiterate);
		
		JLabel ga3 = new JLabel("Cross Probability:");
		ga3.setFont(new Font("Consolas", Font.PLAIN, 12));
		ga3.setBounds(37, 144, 165, 15);
		gaSettingPanel.add(ga3);
		
		cross.setColumns(10);
		cross.setBounds(189, 141, 76, 22);
		gaSettingPanel.add(cross);
		
		JLabel ga4 = new JLabel("Mutation Probability:");
		ga4.setFont(new Font("Consolas", Font.PLAIN, 12));
		ga4.setBounds(37, 191, 165, 15);
		gaSettingPanel.add(ga4);
		
		mutate.setColumns(10);
		mutate.setBounds(189, 188, 76, 22);
		gaSettingPanel.add(mutate);
		gaSettingPanel.setVisible(false);
		
		gaout.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		gaout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Element root = new Element("GA");
				Document dom = new Document(root);
				Element element1 = new Element(ga1.getText().substring(0, ga1.getText().length()-1).replaceAll(" ", "_"));
				element1.setText(populationsize.getText());
				root.addContent(element1);
				Element element2 = new Element(ga2.getText().substring(0, ga2.getText().length()-1).replaceAll(" ", "_"));
				element2.setText(gaiterate.getText());
				root.addContent(element2);
				Element element3 = new Element(ga3.getText().substring(0, ga3.getText().length()-1).replaceAll(" ", "_"));
				element3.setText(cross.getText());
				root.addContent(element3);
				Element element4 = new Element(ga4.getText().substring(0, ga4.getText().length()-1).replaceAll(" ", "_"));
				element4.setText(mutate.getText());
				root.addContent(element4);
				Element element5 = new Element(ga5.getText().substring(0, ga5.getText().length()-1).replaceAll(" ", "_"));
				element5.setText(gaSimNum.getText());
				root.addContent(element5);
				Format format = Format.getCompactFormat();
	            format.setIndent(" ");
				XMLOutputter outputter=new XMLOutputter(format);
				Date date = new Date();
				String d = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
				try {
					File file = new File("parameter-setting/GA/gasetting"+d+".xml");
					outputter.output(dom, new FileOutputStream(file));
					System.out.println("生成xml成功");
					//打开文件所在目录并选中该文件
					Runtime.getRuntime().exec(
							"rundll32 SHELL32.DLL,ShellExec_RunDLL "
							+ "Explorer.exe /select," + file.getAbsolutePath());
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		gaout.setBounds(115, 277, 93, 23);
		gaSettingPanel.add(gaout);
		gaxml.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		gaxml.setColumns(10);
		gaxml.setBounds(15, 310, 289, 23);
		gaSettingPanel.add(gaxml);
		gaselect.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		gaselect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
		        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		        jfc.setCurrentDirectory(new File("parameter-setting/GA"));// 文件选择器的初始目录定为e盘
		        FileNameExtensionFilter filter = new FileNameExtensionFilter("xml文件(*.xml)", "xml");
		        jfc.setFileFilter(filter);
		        jfc.showDialog(new JLabel(), "select");
		        if(jfc.getSelectedFile()!=null){
		        	gasetting = jfc.getSelectedFile();
		        	gaxml.setText(gasetting.getPath());
		        }
		        gain.setEnabled(true);
			}
		});
		gaselect.setBounds(103, 343, 115, 23);
		gaSettingPanel.add(gaselect);
		gain.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		gain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SAXBuilder builder = new SAXBuilder();
				Document dom = null;
				try {
					dom = builder.build(gasetting);
				} catch (JDOMException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
	            Element root = dom.getRootElement();
	            List<Element> list = root.getChildren();
	            for (Element node : list) {
	            	if(ga1.getText().substring(0, ga1.getText().length()-1).replaceAll(" ", "_").equals(node.getName()))
	            		populationsize.setText(node.getText());
	            	else if(ga2.getText().substring(0, ga2.getText().length()-1).replaceAll(" ", "_").equals(node.getName()))
	            		gaiterate.setText(node.getText());
	            	else if(ga3.getText().substring(0, ga3.getText().length()-1).replaceAll(" ", "_").equals(node.getName()))
	            		cross.setText(node.getText());
	            	else if(ga4.getText().substring(0, ga4.getText().length()-1).replaceAll(" ", "_").equals(node.getName()))
	            		mutate.setText(node.getText());
	            	else if(ga5.getText().substring(0, ga5.getText().length()-1).replaceAll(" ", "_").equals(node.getName()))
	            		gaSimNum.setText(node.getText());
	            }
			}
		});
		gain.setEnabled(false);
		gain.setBounds(115, 376, 93, 23);
		gaSettingPanel.add(gain);
		
		ga5.setFont(new Font("Consolas", Font.PLAIN, 12));
		ga5.setBounds(37, 237, 165, 15);
		gaSettingPanel.add(ga5);
		
		gaSimNum.setColumns(10);
		gaSimNum.setBounds(189, 234, 76, 22);
		gaSettingPanel.add(gaSimNum);
		
		CustomASettingPanel.setLayout(null);
		CustomASettingPanel.setBackground(Color.WHITE);
//		CustomASettingPanel.setBounds(228, 446, 309, 270);
		CustomASettingPanel.setBounds(228, 27, 309, 409);
		settingPane.add(CustomASettingPanel);
		
		lblCustomAlgorithmParameter.setFont(new Font("Consolas", Font.BOLD, 16));
		lblCustomAlgorithmParameter.setBounds(0, 10, 319, 25);
		CustomASettingPanel.add(lblCustomAlgorithmParameter);
		CustomASettingPanel.setVisible(false);
		
		AlgorithmsPanel.setBackground(SystemColor.controlHighlight);
		AlgorithmsPanel.setBounds(30, 27, 161, 409);
		settingPane.add(AlgorithmsPanel);
		AlgorithmsPanel.setLayout(null);
		
		lblMaxmin.setFont(new Font("Consolas", Font.PLAIN, 16));
		lblMaxmin.setBounds(28, 87, 95, 15);
		AlgorithmsPanel.add(lblMaxmin);
		
		JLabel lblMinmin = new JLabel("MinMin");
		lblMinmin.setFont(new Font("Consolas", Font.PLAIN, 16));
		lblMinmin.setBounds(28, 50, 95, 15);
		AlgorithmsPanel.add(lblMinmin);
		
		JLabel lblFcfs = new JLabel("FCFS");
		lblFcfs.setFont(new Font("Consolas", Font.PLAIN, 16));
		lblFcfs.setBounds(28, 128, 95, 15);
		AlgorithmsPanel.add(lblFcfs);
		
		JLabel lblRoundrobin = new JLabel("RoundRobin");
		lblRoundrobin.setFont(new Font("Consolas", Font.PLAIN, 16));
		lblRoundrobin.setBounds(28, 168, 95, 15);
		AlgorithmsPanel.add(lblRoundrobin);
		
		JLabel lblPso_1 = new JLabel("PSO");
		lblPso_1.setFont(new Font("Consolas", Font.PLAIN, 16));
		lblPso_1.setBounds(28, 209, 95, 15);
		AlgorithmsPanel.add(lblPso_1);
		LabeltoPanel.put(lblPso_1, psoSettingPanel);
		lblPso_1.addMouseListener(new MouseListener1(){});
		
		JLabel lblGa_1 = new JLabel("GA");
		lblGa_1.setFont(new Font("Consolas", Font.PLAIN, 16));
		lblGa_1.setBounds(28, 247, 95, 15);
		AlgorithmsPanel.add(lblGa_1);
		LabeltoPanel.put(lblGa_1, gaSettingPanel);
		lblGa_1.addMouseListener(new MouseListener1(){});
		
		JLabel lblAlgorithms = new JLabel("Algorithms");
		lblAlgorithms.setFont(new Font("Consolas", Font.BOLD, 16));
		lblAlgorithms.setBounds(28, 15, 95, 15);
		AlgorithmsPanel.add(lblAlgorithms);
		
		btnAddAlgorithm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int x1 = 42, y1 = 10, width1 = 148, height1 = 15;
				int x2 = 194, y2 = 7, width2 = 76, height2 = 22;
				JFileChooser jfc=new JFileChooser();
		        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		        jfc.setCurrentDirectory(new File("algorithms"));// 文件选择器的初始目录定为e盘
		        FileNameExtensionFilter filter = new FileNameExtensionFilter("xml文件(*.xml)", "xml");
		        jfc.setFileFilter(filter);
		        jfc.showDialog(new JLabel(), "Add Algorithm");
		        if(jfc.getSelectedFile()!=null){
		        	AlgorithmFile = jfc.getSelectedFile();
		        	SAXBuilder builder = new SAXBuilder();
					Document dom = null;
					try {
						dom = builder.build(AlgorithmFile);
					} catch (JDOMException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					for(JLabel label : LabeltoPanel.keySet())
						LabeltoPanel.get(label).setVisible(false);
		            Element root = dom.getRootElement();
		            List<Element> list = root.getChildren();
		            for (int i = 0; i < list.size(); i++) {
		            	Element node = list.get(i);
		            	if("name".equals(node.getName())){
		            		Customlabel.setText(node.getText());
		            		Customlabel.setForeground(Color.red);
		            		LabeltoPanel.put(Customlabel, CustomASettingPanel);
		            		Customlabel.addMouseListener(new MouseListener1(){});
		            		JCheckBox customchk = new JCheckBox(node.getText());
		            		customchk.setFont(new Font("Consolas", Font.PLAIN, 12));
		            		customchk.setBackground(Color.WHITE);
		            		customchk.setBounds(150, 98, 56, 23);
		            		CheckBoxList.add(customchk);
		            		panel_2.add(customchk);
		            		lblCustomAlgorithmParameter.setText(node.getText()+" Algorithm Parameter Setting");
		            	}
		            	else{
		            		y1 += 38;
		            		y2 += 38;
		            		JLabel settinglabel = new JLabel(node.getAttributeValue("name")+":");
		            		settinglabel.setFont(new Font("Consolas", Font.PLAIN, 12));
		            		settinglabel.setBounds(x1, y1, width1, height1);
		            		CustomASettingPanel.add(settinglabel);
		            		JTextField textField = new JTextField(node.getAttributeValue("value"));
		            		textField.setBounds(x2, y2, width2, height2);
		            		CustomASettingPanel.add(textField);
		            		CustomASettingPanel.setVisible(true);
		            	}
		            }
				}
			}
		});
		btnAddAlgorithm.setBounds(20, 376, 126, 23);
		AlgorithmsPanel.add(btnAddAlgorithm);
		
		Customlabel.setFont(new Font("Consolas", Font.PLAIN, 16));
		Customlabel.setBounds(28, 284, 95, 15);
		AlgorithmsPanel.add(Customlabel);
		
		//mobileNumCb.addActionListener(new JMHandler());//添加监听事件
		mobileNumCb.addItemListener(new JMHandler());
		edgeNumCb.addItemListener(new JMHandler());
		cloudNumCb.addItemListener(new JMHandler());
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
					filepath.setEditable(false);;
					selectfile.setEnabled(false);;
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
					Object[] options = {"Yes","No"};
					int a = JOptionPane.showOptionDialog(getContentPane(), "In the drawing, confirm the closing?", 
							"Option",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					if (a == 0) {
						setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					}
					else if(a == 1){
						setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					}
				}
				else{
					Object[] options = {"Yes","No"};
					int a = JOptionPane.showOptionDialog(getContentPane(), "Confirm the closing?", 
							"Option",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
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
	            	Flag = false;
	            	if(GetAlgorithms().size() == 0)
	            		showMessageDialog("Please choose an Alogorithm");
	            	else if(GetAlgorithms().size() > 1)
	            		showMessageDialog("You chose more than one , please click the 'Compare' button");
	            	else{
	            		for(JCheckBox cb : CheckBoxList){
		            		if(cb.isSelected())
		            			scheduler_method=cb.getText();
		            	}
		            	optimize_objective = GetObjective();//获取所选择的优化目标
		            	if(scheduler_method.equals("PSO")){
		            		if(!getpsosetting())     //获取所输入的PSO参数，若无参数则弹出窗口
		            			showMessageDialog("PSO algorithm parameters are not set , please go to the 'Setting' panel");
		            		else{
//		            			int repeat = Integer.valueOf(psoSimNum.getText());
//		            			List<Double[]> r = new ArrayList<Double[]>();
//		            			for(int i = 0; i < repeat; i++){
		            				StartAlgorithm();
//		            			}
		            			System.out.println("画 "+scheduler_method+" 迭代图");
			            		drawplot(wfEngine.iterateNum, wfEngine.updatebest, "Iterations", optimize_objective);
			            		Flag = true;
			            		System.out.println("画完");
		            		}
		            	}
		            	else if(scheduler_method.equals("GA")){
		            		if(!getgasetting())      //获取所输入的GA参数，若无参数则弹出窗口
		            			showMessageDialog("GA algorithm parameters are not set , please go to the 'Setting' panel");
		            		else{
		            			StartAlgorithm();
		            			System.out.println("画 "+scheduler_method+" 迭代图");
			            		drawplot(wfEngine.iterateNum, wfEngine.updatebest, "Iterations", optimize_objective);
			            		Flag = true;
			            		System.out.println("画完");
		            		}
		            	}
		            	else{//其他算法只支持优化时间
			            	if(!optimize_objective.equalsIgnoreCase("Time"))
			            		showMessageDialog(scheduler_method+" doesn't support '"+optimize_objective+"' objective , only support 'Time'");
			            	else
			            		StartAlgorithm();
		            	}
	            	}
	            }
	            
	            if(e.getSource() == cmpBtn){
	            	selectdisplay.removeAllItems();
	            	outputs.clear();
	            	record.clear();
	            	Flag = false;
	            	List<String> aList = GetAlgorithms();
	            	if(aList.size() == 0)
	            		showMessageDialog("Please choose an Alogorithm");
	            	else if(aList.size() == 1)
	            		showMessageDialog("You chose one , please click the 'Start Simulation' button");
	            	else{
	            		optimize_objective = GetObjective();//获取所选择的优化目标
		            	for(String al : aList){
		            		System.out.println(al);
		            		scheduler_method = al;
		            		if(scheduler_method.equals("PSO")){
			            		if(!getpsosetting()){
			            			//获取所输入的PSO参数，若无参数则弹出窗口
			            			showMessageDialog("PSO algorithm parameters are not set , please go to the 'Setting' panel");
			            			record.clear();
			            			break;
			            		}
			            		else{
			            			int repeat = Integer.valueOf(psoSimNum.getText());
			            			List<Double[]> repeats = new ArrayList<Double[]>();
			            			for(int i = 0; i < repeat; i++){
			            				System.out.println("---------------------------第"+(i+1)+"次pso--------------------------");
			            				Double[] b = StartAlgorithm();
			            				record.remove(record.size()-1);
			            				repeats.add(b);
			            			}
			            			Double[] mean = GetMean(repeats);
			            			Double[] algomean = new Double[4];
			            			algomean[0] = getAlgorithm(scheduler_method);
			            			algomean[1] = mean[0];
			            			algomean[2] = mean[1];
			            			algomean[3] = mean[2];
			            			record.add(algomean);
			            		}
			            	}
			            	else if(scheduler_method.equals("GA")){
			            		if(!getgasetting()){
			            			//获取所输入的GA参数，若无参数则弹出窗口
			            			showMessageDialog("GA algorithm parameters are not set , please go to the 'Setting' panel");
			            			record.clear();
			            			break;
			            		}
			            		else{
			            			int repeat = Integer.valueOf(gaSimNum.getText());
			            			List<Double[]> repeats = new ArrayList<Double[]>();
			            			for(int i = 0; i < repeat; i++){
			            				System.out.println("---------------------------第"+(i+1)+"次ga--------------------------");
			            				Double[] b = StartAlgorithm();
			            				record.remove(record.size()-1);
			            				repeats.add(b);
			            			}
			            			Double[] mean = GetMean(repeats);
			            			Double[] algomean = new Double[4];
			            			algomean[0] = getAlgorithm(scheduler_method);
			            			algomean[1] = mean[0];
			            			algomean[2] = mean[1];
			            			algomean[3] = mean[2];
			            			record.add(algomean);
			            		}
			            	}
			            	else{//其他算法只支持优化时间
				            	if(!optimize_objective.equalsIgnoreCase("Time")){
				            		showMessageDialog(scheduler_method+" doesn't support "+optimize_objective+" objective , can't compare");
				            		record.clear();
				            		break;
				            	}
				            	else
				            		StartAlgorithm();
			            	}
		            	}
	            	}
	            	if(!record.isEmpty()){
	            		System.out.println("画算法对比柱状图");
	            		drawbar(record);
	            		Flag = true;
	            		System.out.println("画完");
	            	}
	            }
	        }
	        
	        public void itemStateChanged(ItemEvent e){
	        if(e.getItemSelectable()==mobileNumCb) {
	    		if(e.getStateChange() == ItemEvent.SELECTED){
	    			mobilePanel.removeAll();
	    			String itemSize = (String) e.getItem();
	    			try{
	    				for(int i=0;i<Integer.parseInt(itemSize);i++) {
	    					jLabel=new JLabel(new ImageIcon(getClass().getResource("/images\\mobile.jpg"))); 
	    					mobilePanel.add(jLabel);
	    					validate();
	    					repaint();
	    				}
	    			}catch(Exception ex){
	    				
	    			}
	    		}
	        }else if(e.getItemSelectable()==edgeNumCb) {
	        		if(e.getStateChange() == ItemEvent.SELECTED){
		    			fogPanel.removeAll();
		    			String itemSize = (String) e.getItem();
		    			try{
		    				for(int i=0;i<Integer.parseInt(itemSize);i++) {
		    					ImageIcon icon =new ImageIcon(getClass().getResource("/images/fogServer.jpg"));
		    					icon.setImage(icon.getImage().getScaledInstance(60, 70,
		    							Image.SCALE_DEFAULT));
		    					jLabel=new JLabel(icon); 
		    					fogPanel.add(jLabel);
		    					validate();
		    					repaint();
		    				}
		    			}catch(Exception ex){
		    				
		    			}
		    		}
	        		
	        	}else if(e.getItemSelectable()==cloudNumCb){
	        		if(e.getStateChange() == ItemEvent.SELECTED){
		    			cloudPanel.removeAll();
		    			String itemSize = (String) e.getItem();
		    			try{
		    				for(int i=0;i<Integer.parseInt(itemSize);i++) {
		    					ImageIcon icon =new ImageIcon(getClass().getResource("/images/cloudServer2.jpg"));
		    					icon.setImage(icon.getImage().getScaledInstance(60, 70,
		    							Image.SCALE_DEFAULT));
		    					jLabel=new JLabel(icon); 
		    					cloudPanel.add(jLabel);
		    					validate();
		    					repaint();
		    				}
		    			}catch(Exception ex){
		    				
		    			}
		    		}
	        	}else if(e.getItemSelectable()==selectdisplay){
	        		String algorithm = (String) e.getItem();
	        		Object[][] rowdata = outputs.get(algorithm);
	        		table = new JTable(rowdata, columnNames);
	        		table.getTableHeader().setForeground(Color.RED);
//	    	        FitTableColumns(table);
	        		scrollPane.setViewportView(table);
	        	}
	    	}
	    }
	 
	 private class MouseListener1 implements MouseListener{
		 @Override
		 public void mouseClicked(MouseEvent e) {
			 for(JLabel label : LabeltoPanel.keySet()){
				if(e.getSource()==label)
					LabeltoPanel.get(label).setVisible(true);
				else
					LabeltoPanel.get(label).setVisible(false);
			}
		}
		 public void mousePressed(MouseEvent e) {}
		 public void mouseReleased(MouseEvent e) {}
		 public void mouseEntered(MouseEvent e) {
			for(JLabel label : LabeltoPanel.keySet()){
				if(e.getSource()==label)
					label.setForeground(Color.RED);
			}
		}
		public void mouseExited(MouseEvent e) {
			for(JLabel label : LabeltoPanel.keySet()){
				if(e.getSource()==label)
					label.setForeground(null);
			}
		}	
	 }
	 
	 public void simulate() {
		 System.out.println("Starting Task...");

			try {
				//Log.disable();
				int num_user = 1; // number of cloud users
				Calendar calendar = Calendar.getInstance();
				boolean trace_flag = false; // mean trace events

				CloudSim.init(num_user, calendar, trace_flag);

				String appId = "workflow"; // identifier of the application
								
				createFogDevices(1,appId);//(broker.getId(), appId);
							
				int hostnum = 0;
				for(FogDevice device : fogDevices){
					hostnum += device.getHostList().size();
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
	             * Create a list of VMs.The userId of a vm is basically the id of
	             * the scheduler that controls this vm.
	             */
	            List<CondorVM> vmlist0 = createVM(wfEngine.getSchedulerId(0), Parameters.getVmNum());

	            /**
	             * Submits this list of vms to this WorkflowEngine.
	             */
	            wfEngine.submitVmList(vmlist0, 0);

	            controller = new Controller("master-controller", fogDevices, wfEngine);
	            
	            /**
	             * Binds the data centers with the scheduler.
	             */
	            for(FogDevice fogdevice:controller.getFogDevices()){
	            	wfEngine.bindSchedulerDatacenter(fogdevice.getId(), 0);
	            	List<PowerHost> list = fogdevice.getHostList();  //输出设备上的主机
	            	System.out.println(fogdevice.getName()+": ");
	            	for (PowerHost host : list){
	            		System.out.print(host.getId()+",");
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

	private  void createFogDevices(int userId, String appId) {
			
			double GHzList[] = {1.6};//云中的主机
			double ratePerMips[] = {0.96};
			double cost = 5.0; // the cost of using processing in this resource每秒的花费
			double costPerMem = 0.05; // the cost of using memory in this resource
			double costPerStorage = 0.1; // the cost of using storage in this resource
			double costPerBw = 0.2;//每带宽的花费

			for(int i=0;i<GHzList.length;i++)
			{
				FogDevice cloud = createFogDevice("cloud", cloudNum, GHzToMips(GHzList[i]), 
						40000, 100, 10000, 0, ratePerMips[i], 16*103, 16*83.25,cost,costPerMem,costPerStorage,costPerBw); // creates the fog device Cloud at the apex of the hierarchy with level=0
				cloud.setParentId(-1);
				
				fogDevices.add(cloud);
			}
			for(int i=0;i<numOfDepts;i++){
				addGw(i+"", userId, appId, fogDevices.get(0).getId()); // adding a fog device for every Gateway in physical topology. The parent of each gateway is the Proxy Server
			}
		}


		private  FogDevice addGw(String id, int userId, String appId, int parentId){
			
			double ratePerMips = 0.48;
			double cost = 3.0; // the cost of using processing in this resource每秒的花费
			double costPerMem = 0.05; // the cost of using memory in this resource
			double costPerStorage = 0.1; // the cost of using storage in this resource
			double costPerBw = 0.1;//每带宽的花费
			
			FogDevice dept = createFogDevice("d-"+id, fogServerNum, GHzToMips(1.3), 4000, 10000, 10000, 1, ratePerMips, 700, 30,cost,costPerMem,costPerStorage,costPerBw);
			fogDevices.add(dept);
			dept.setParentId(parentId);
			dept.setUplinkLatency(4); // latency of connection between gateways and proxy server is 4 ms
			for(int i=0;i<numOfMobilesPerDept;i++){
				String mobileId = id+"-"+i;
				FogDevice mobile = addMobile(mobileId, userId, appId, dept.getId()); // adding mobiles to the physical topology. Smartphones have been modeled as fog devices as well.
				mobile.setUplinkLatency(2); // latency of connection between the smartphone and proxy server is 4 ms
				fogDevices.add(mobile);
			}
			return dept;
		}
		
		private  FogDevice addMobile(String id, int userId, String appId, int parentId){
			double cost = 6.0; // the cost of using processing in this resource每秒的花费
			double costPerMem = 0.05; // the cost of using memory in this resource
			double costPerStorage = 0.1; // the cost of using storage in this resource
			double costPerBw = 0.3;//每带宽的花费
			FogDevice mobile = createFogDevice("m-"+id, mobileNum, GHzToMips(1.0), 20*1024, 40*1024,
					                                     270, 3, 0, 700, 30,cost,costPerMem,costPerStorage,costPerBw);
			mobile.setParentId(parentId);
			return mobile;
		}
		
		/**
		 * Creates a vanilla fog device
		 * @param nodeName name of the device to be used in simulation
		 * @param hostnum the number of the host of device
		 * @param mips MIPS
		 * @param ram RAM
		 * @param upBw uplink bandwidth (Kbps)
		 * @param downBw downlink bandwidth (Kbps)
		 * @param level hierarchy level of the device
		 * @param ratePerMips cost rate per MIPS used
		 * @param busyPower(mW)
		 * @param idlePower(mW)
		 * @return
		 */
		private static FogDevice createFogDevice(String nodeName, int hostnum, long mips,
				int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower,double cost,double costPerMem,double costPerStorage,double costPerBw) {
			
			List<Host> hostList = new ArrayList<Host>();

			for ( int i = 0 ;i < hostnum; i++ )
			{
				List<Pe> peList = new ArrayList<Pe>();
				// 3. Create PEs and add these into a list.
				peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
				//peList.add(new Pe(1, new PeProvisionerSimple(mips)));
				
				int hostId = FogUtils.generateEntityId();
				long storage = 1000000; // host storage
				int bw = 10000;

				PowerHost host = new PowerHost(
						hostId,
						new RamProvisionerSimple(ram),
						new BwProvisionerSimple(bw),
						storage,
						peList,
						new VmSchedulerTimeShared(peList),
						new FogLinearPowerModel(busyPower, idlePower)//默认发送功率100mW 接收功率25mW
					);
				
				hostList.add(host);
			}

	        // 4. Create a DatacenterCharacteristics object that stores the
	        //    properties of a data center: architecture, OS, list of
	        //    Machines, allocation policy: time- or space-shared, time zone
	        //    and its price (G$/Pe time unit).
			String arch = "x86"; // system architecture
			String os = "Linux"; // operating system
			String vmm = "Xen";
			double time_zone = 10.0; // time zone this resource located
			/*double cost = 3.0; // the cost of using processing in this resource每秒的花费
			double costPerMem = 0.05; // the cost of using memory in this resource
			double costPerStorage = 0.1; // the cost of using storage in this resource
			double costPerBw = 0.1; // the cost of using bw in this resource每带宽的花费*/
			LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN devices by now

			FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
					arch, os, vmm, hostList, time_zone, cost, costPerMem,
					costPerStorage, costPerBw);

			FogDevice fogdevice = null;
			
			// 5. Finally, we need to create a storage object.
	        /**
	         * The bandwidth within a data center in MB/s.
	         * maxTransferRate MB/s
	         * upBw Kb/s
	         */
	        int maxTransferRate = 0;//没有作用// the number comes from the futuregrid site, you can specify your bw
	        try {
				// Here we set the bandwidth to be 15MB/s
	            HarddriveStorage s1 = new HarddriveStorage(nodeName, 1e12);
	            s1.setMaxTransferRate(maxTransferRate);
	            storageList.add(s1);
				fogdevice = new FogDevice(nodeName, characteristics, 
						new VmAllocationPolicySimple(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			fogdevice.setLevel(level);
			return fogdevice;
		}
		
	    protected static List<CondorVM> createVM(int userId, int vms) {
	        //Creates a container to store VMs. This list is passed to the broker later
	        LinkedList<CondorVM> list = new LinkedList<>();

	        //VM Parameters
	        long size = 10000; //image size (MB)
	        int ram = 512; //vm memory (MB)
	        int mips = 1000;
	        long bw = 1000;
	        int pesNumber = 1; //number of cpus
	        String vmm = "Xen"; //VMM name

	        //create VMs
	        CondorVM[] vm = new CondorVM[vms];
	        for (int i = 0; i < vms; i++) {
	            double ratio = 1.0;
	            vm[i] = new CondorVM(i, userId, mips * ratio, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
	            list.add(vm[i]);
	        }
	        return list;
	    }
	    
	    private static long GHzToMips(double GHz)
	    {
	    	return (long)GHz*1000;//是否除以4
	    }
	    
	    /**
	     * Prints the job objects
	     *
	     * @param list list of jobs
	     */
	    @SuppressWarnings("null")
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
				String[] columnNames = {"Job ID", "Task ID", "STATUS", "Data center ID", "VM ID", "Time", "Start Time", "Finish Time", "Depth", "Cost", "Parents"};
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
		 * 获得所输入的pso参数设置
		 * @return 获取成功返回true 未输入返回false
		 */
		public boolean getpsosetting(){
			try{
				PsoScheduling.particleNum = Integer.valueOf(particleNum.getText());
				PsoScheduling.iterateNum = Integer.valueOf(psoiterate.getText());
				PsoScheduling.c1 = Double.valueOf(c1.getText());
				PsoScheduling.c2 = Double.valueOf(c2.getText());
				PsoScheduling.w = Double.valueOf(weight.getText());
				wfEngine.fitness = new double[PsoScheduling.particleNum];
				wfEngine.fitness2 = new double[PsoScheduling.particleNum];
			}catch (Exception e) {
				return false;
			}
			return true;
		}
		
		/**
		 * 获得所输入的ga参数设置
		 * @return 获取成功返回true 未输入返回false
		 */
		public boolean getgasetting(){
			try{
				GASchedulingAlgorithm.popsize = Integer.valueOf(populationsize.getText());
				GASchedulingAlgorithm.gmax = Integer.valueOf(gaiterate.getText());
				GASchedulingAlgorithm.crossoverProb = Double.valueOf(cross.getText());
				GASchedulingAlgorithm.mutationRate = Double.valueOf(mutate.getText());
				wfEngine.fitnessForGA = new double[GASchedulingAlgorithm.popsize];
			}catch (Exception e) {
				return false;
			}
			return true;
		}
		
		/**
		 * 针对某个算法进行仿真模拟并记录仿真结果
		 */
		private Double[] StartAlgorithm() {
			clear();
        	System.out.println(optimize_objective);
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
        	String cloudNum1=(String)cloudNumCb.getSelectedItem();
        	cloudNum=Integer.parseInt(cloudNum1);
        	String fogServerNum1=(String)edgeNumCb.getSelectedItem();
        	fogServerNum=Integer.parseInt(fogServerNum1);
        	String mobileNum1=(String)mobileNumCb.getSelectedItem();
        	mobileNum=Integer.parseInt(mobileNum1);
        	simulate();
        	CloudSim.startSimulation();
        	List<Job> outputList0 = wfEngine.getJobsReceivedList();
            CloudSim.stopSimulation();
            Log.enable();
            printJobList(scheduler_method, outputList0);
            controller.print();
            Double[] a = {getAlgorithm(scheduler_method),controller.TotalExecutionTime,controller.TotalEnergy,controller.TotalCost};
            record.add(a);
            Double[] b = {controller.TotalExecutionTime,controller.TotalEnergy,controller.TotalCost};
            return b;
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
		 */
		private void showMessageDialog(String string){
			Object[] options = {"OK"};
			JOptionPane.showOptionDialog(getContentPane(), string, "Error",
					JOptionPane.YES_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
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
				a += d[0];
				b += d[1];
				c += d[2];
			}
			a = a / list.size();
			b = b / list.size();
			c = c / list.size();
			Double[] r = {a,b,c};
			return r;
		}
}