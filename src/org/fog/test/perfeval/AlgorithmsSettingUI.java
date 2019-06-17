package org.fog.test.perfeval;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.scheduling.GASchedulingAlgorithm;
import org.workflowsim.scheduling.PsoScheduling;

/**
 * The SettingUI of FogWorkflowSim 2.0
 * 
 * @since FogWorkflowSim Toolkit 1.0
 * @author Lingmin Fan
 */

@SuppressWarnings("serial")
public class AlgorithmsSettingUI extends JFrame {

	private static WorkflowEngine wfEngine;
	private static File psosetting;
	private static File gasetting;
	private static File AlgorithmFile;
	
	private final static JPanel AlgorithmsPanel = new JPanel();
	private final static JPanel psoSettingPanel = new JPanel();
	private final static JPanel gaSettingPanel = new JPanel();
	private static HashMap<JLabel, JPanel> LabeltoPanel = new HashMap<JLabel, JPanel>();
	
	private static JPanel SettingPane;
	
	private final static JLabel pso1 = new JLabel("Number of Particles:");
	private static JTextField particleNum = new JTextField();
	private final static JLabel pso2 = new JLabel("Number of Iterations:");
	private static JTextField psoiterate = new JTextField();
	private final static JLabel pso3 = new JLabel("Learning Factor c1:");
	private final static JTextField c1 = new JTextField();
	private final static JLabel pso4 = new JLabel("Learning Factor c2:");
	private final static JTextField c2 = new JTextField();
	private final static JLabel pso5 = new JLabel("Inertia Weight:");
	private final static JTextField weight = new JTextField();
	private final static JLabel pso6 = new JLabel("Repeated experiment:");
	private final static JTextField psoSimNum = new JTextField();
	private final static JLabel ga5 = new JLabel("Repeated experiment:");
	private final static JTextField gaSimNum = new JTextField();
	private final static JButton psoout = new JButton("Export");
	private final static JTextField psoxml = new JTextField();
	private final static JButton psoselect = new JButton("Select File");
	private final static JButton psoin = new JButton("Import");
	private final static JButton gaout = new JButton("Export");
	private final static JTextField gaxml = new JTextField();
	private final static JButton gaselect = new JButton("Select File");
	private final static JButton gain = new JButton("Import");
	private static JTextField populationsize = new JTextField();
	private static JTextField gaiterate = new JTextField();
	private static JTextField cross = new JTextField();
	private static JTextField mutate = new JTextField();
	private static HashMap<String,Object[][]> outputs= new LinkedHashMap<String, Object[][]>();
	private final static JLabel lblMaxmin = new JLabel("MaxMin");
	private final static JButton btnAddAlgorithm = new JButton("Add Algorithm");
	private final static JLabel Customlabel = new JLabel("");
	private final static JPanel CustomASettingPanel = new JPanel();
	private final static JLabel lblCustomAlgorithmParameter = new JLabel();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AlgorithmsSettingUI frame = new AlgorithmsSettingUI();
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
	public AlgorithmsSettingUI() {
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Algorithms Setting");
		setBounds(100, 100, 512, 465);
		SettingPane = new JPanel();
		SettingPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(SettingPane);
		SettingPane.setLayout(null);
		
		this.initUI();
	}
	
	public void initUI(){
		
		AlgorithmsPanel.setBackground(SystemColor.controlHighlight);
		AlgorithmsPanel.setBounds(10, 10, 161, 409);
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
		lblPso_1.setForeground(Color.blue);
		AlgorithmsPanel.add(lblPso_1);
		LabeltoPanel.put(lblPso_1, psoSettingPanel);
		lblPso_1.addMouseListener(new MouseListener1(){});
		
		JLabel lblGa_1 = new JLabel("GA");
		lblGa_1.setFont(new Font("Consolas", Font.PLAIN, 16));
		lblGa_1.setBounds(28, 247, 95, 15);
		lblGa_1.setForeground(Color.blue);
		AlgorithmsPanel.add(lblGa_1);
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
		            		MainUI.CheckBoxList.add(customchk);
		            		MainUI.panel_2.add(customchk);
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
		SettingPane.add(AlgorithmsPanel);
		
		psoSettingPanel.setBackground(Color.WHITE);
		SettingPane.add(psoSettingPanel);
		psoSettingPanel.setLayout(null);
		psoSettingPanel.setBounds(181, 10, 309, 409);
		
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
		LabeltoPanel.put(lblGa_1, gaSettingPanel);
		gaSettingPanel.setBounds(181, 10, 309, 409);
		SettingPane.add(gaSettingPanel);
		gaSettingPanel.setVisible(false);
		
		gaSettingPanel.setLayout(null);
		gaSettingPanel.setBackground(Color.WHITE);
		
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
		
		JLabel ga3 = new JLabel("Cross Rate:");
		ga3.setFont(new Font("Consolas", Font.PLAIN, 12));
		ga3.setBounds(37, 144, 165, 15);
		gaSettingPanel.add(ga3);
		
		cross.setColumns(10);
		cross.setBounds(189, 141, 76, 22);
		gaSettingPanel.add(cross);
		
		JLabel ga4 = new JLabel("Mutation Rate:");
		ga4.setFont(new Font("Consolas", Font.PLAIN, 12));
		ga4.setBounds(37, 191, 165, 15);
		gaSettingPanel.add(ga4);
		
		mutate.setColumns(10);
		mutate.setBounds(189, 188, 76, 22);
		gaSettingPanel.add(mutate);
		
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
		
		CustomASettingPanel.setBounds(181, 10, 309, 409);
		SettingPane.add(CustomASettingPanel);
		
		CustomASettingPanel.setLayout(null);
		CustomASettingPanel.setBackground(Color.WHITE);
		
		lblCustomAlgorithmParameter.setFont(new Font("Consolas", Font.BOLD, 16));
		lblCustomAlgorithmParameter.setBounds(0, 10, 319, 25);
		CustomASettingPanel.add(lblCustomAlgorithmParameter);
		CustomASettingPanel.setVisible(false);
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
					label.setForeground(Color.blue);
			}
		}	
	 }
	
	public void bindEngine(WorkflowEngine Engine) {
		this.wfEngine = Engine;
	}
	
	/**
	 * 显示名为string的算法参数设置
	 * @param string
	 */
	public void display(String string){
		for(JLabel label : LabeltoPanel.keySet()){
			if(string.equals(label.getText()))
				LabeltoPanel.get(label).setVisible(true);
			else
				LabeltoPanel.get(label).setVisible(false);
		}
		this.setVisible(true);
	}
	/**
	 * 获得所输入的pso参数设置
	 * @return 获取成功返回true 未输入返回false
	 */
	public int getpsosetting(WorkflowEngine wfEngine){
		int repeat = 0;
		try{
			PsoScheduling.particleNum = Integer.valueOf(particleNum.getText());
			PsoScheduling.iterateNum = Integer.valueOf(psoiterate.getText());
			PsoScheduling.c1 = Double.valueOf(c1.getText());
			PsoScheduling.c2 = Double.valueOf(c2.getText());
			PsoScheduling.w = Double.valueOf(weight.getText());
			if(!psoSimNum.getText().isEmpty())
				repeat = Integer.valueOf(psoSimNum.getText());
			wfEngine.fitness = new double[PsoScheduling.particleNum];
			wfEngine.fitness2 = new double[PsoScheduling.particleNum];
		}catch (Exception e) {
			return -1;
		}
		return repeat;
	}
	
	/**
	 * 获得所输入的ga参数设置
	 * @return 获取成功返回true 未输入返回false
	 */
	public int getgasetting(WorkflowEngine wfEngine){
		int repeat = 0;
		try{
			GASchedulingAlgorithm.popsize = Integer.valueOf(populationsize.getText());
			GASchedulingAlgorithm.gmax = Integer.valueOf(gaiterate.getText());
			GASchedulingAlgorithm.crossoverProb = Double.valueOf(cross.getText());
			GASchedulingAlgorithm.mutationRate = Double.valueOf(mutate.getText());
			if(!gaSimNum.getText().isEmpty())
				repeat = Integer.valueOf(gaSimNum.getText());
			wfEngine.fitnessForGA = new double[GASchedulingAlgorithm.popsize];
		}catch (Exception e) {
			return -1;
		}
		return repeat;
	}
}
