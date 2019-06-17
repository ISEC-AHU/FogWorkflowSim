package org.fog.test.perfeval;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
/**
 * The FogEnvironmentUI of FogWorkflowSim 2.0
 * 
 * @since FogWorkflowSim Toolkit 2.0
 * @author Lingmin Fan
 */

@SuppressWarnings("serial")
public class FogEnvironmentUI extends JFrame {

	public HashMap<String, List<JTextField>> DCMipsMap = new LinkedHashMap<String, List<JTextField>>();
	public HashMap<String, List<JTextField>> DCCostMap = new LinkedHashMap<String, List<JTextField>>();
	private HashMap<String, List<String>> NewDCHostMipsMap = new LinkedHashMap<String, List<String>>();
	private HashMap<String, List<String>> OldDCHostMipsMap = new LinkedHashMap<String, List<String>>();
	private HashMap<String, List<String>> XMLDCHostMipsMap = new LinkedHashMap<>();
	private HashMap<String, List<String>> XMLDCHostCostMap = new LinkedHashMap<>();
	private HashMap<String, List<String>> NewDCHostCostMap = new LinkedHashMap<String, List<String>>();
	private HashMap<String, List<String>> OldDCHostCostMap = new LinkedHashMap<String, List<String>>();
	private JPanel SettingPane;
	private File XMLFile;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FogEnvironmentUI frame = new FogEnvironmentUI();
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
	public FogEnvironmentUI() {
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("FogEnvironment Setting");
		setBounds(100, 100, 601, 619);
		
		JPanel MainPane = new JPanel();
		SettingPane = new JPanel();
		SettingPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(MainPane);
		MainPane.setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBar = new JToolBar();
		toolBar.setBackground(SystemColor.menu);
		toolBar.setFont(new Font("Times New Roman", Font.PLAIN, 20));
		MainPane.add(toolBar, BorderLayout.NORTH);
		
		JButton importButton = new JButton(" Import ");
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getEnvironmentSetting();
			}
		});
		importButton.setFont(new Font("Times New Roman", Font.PLAIN, 18));
		toolBar.add(importButton);
		MainPane.add(SettingPane);
		SettingPane.setLayout(new GridLayout(3, 1, 0, 0));
		initPanel_Cloud(1);
		initPanel_FogNode(1);
		initPanel_EndDevice(1);
	}
	
	protected void getEnvironmentSetting() {
		JFileChooser jfc=new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setCurrentDirectory(new File("environment-setting"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("xml文件(*.xml)", "xml");
        jfc.setFileFilter(filter);
        jfc.showDialog(new JLabel(), "select");
        if(jfc.getSelectedFile() != null){
        	XMLFile = jfc.getSelectedFile();
        	SAXBuilder builder = new SAXBuilder();
    		Document dom = null;
    		try {
    			dom = builder.build(XMLFile);
    		} catch (JDOMException e1) {
    			e1.printStackTrace();
    		} catch (IOException e1) {
    			e1.printStackTrace();
    		}
            Element root = dom.getRootElement();//文档根节点
            List<Element> list = root.getChildren();
            List<Element> hostList;
    		List<String> mipsList = new ArrayList<>();
    		List<String> costList = new ArrayList<>();
            for (Element node : list) {
            	switch (node.getName().toLowerCase()) {
    			case "cloud":
    				int hostnum1 = Integer.valueOf(node.getAttributeValue("hostnum"));
    				double WAN_bandwidth = Double.valueOf(node.getAttributeValue("bandwidth"));
    				hostList = node.getChildren();
    				mipsList = new ArrayList<>();
    				costList = new ArrayList<>();
    				for (Element host : hostList) {
    					String mips = host.getAttributeValue("MIPS");
    					String cost = host.getAttributeValue("cost");
    					mipsList.add(mips);
    					costList.add(cost);
    				}
    				XMLDCHostMipsMap.put("cloud", mipsList);
    				XMLDCHostCostMap.put("cloud", costList);
    				break;
    			case "fognode":
    				int hostnum2 = Integer.valueOf(node.getAttributeValue("hostnum"));
    				double LAN_bandwidth = Double.valueOf(node.getAttributeValue("bandwidth"));
    				hostList = node.getChildren();
    				mipsList = new ArrayList<>();
    				costList = new ArrayList<>();
    				for (Element host : hostList) {
    					String mips = host.getAttributeValue("MIPS");
    					String cost = host.getAttributeValue("cost");
    					mipsList.add(mips);
    					costList.add(cost);
    				}
    				XMLDCHostMipsMap.put("fog", mipsList);
    				XMLDCHostCostMap.put("fog", costList);
    				break;
    			case "enddevice":
    				int hostnum3 = Integer.valueOf(node.getAttributeValue("hostnum"));
    				hostList = node.getChildren();
    				mipsList = new ArrayList<>();
    				costList = new ArrayList<>();
    				for (Element host : hostList) {
    					String mips = host.getAttributeValue("MIPS");
    					String cost = host.getAttributeValue("cost");
    					mipsList.add(mips);
    					costList.add(cost);
    				}
    				XMLDCHostMipsMap.put("mobile", mipsList);
    				XMLDCHostCostMap.put("mobile", costList);
    				break;
    			default:
    				break;
    			}
            }
            NewDCHostMipsMap.clear();
            NewDCHostMipsMap.putAll(XMLDCHostMipsMap);
            NewDCHostCostMap.clear();
            NewDCHostCostMap.putAll(XMLDCHostCostMap);
            initUI(NewDCHostMipsMap.get("cloud").size(), NewDCHostMipsMap.get("fog").size(), NewDCHostMipsMap.get("mobile").size());
        	mipsList.clear();
        	costList.clear();
        }
	}

	public void initUI(int CloudNum, int FogNodeNum, int EndDeviceNum){
		SettingPane.removeAll();
		OldDCHostMipsMap.clear();
		OldDCHostMipsMap.putAll(NewDCHostMipsMap);
		NewDCHostMipsMap.clear();
		OldDCHostCostMap.clear();
		OldDCHostCostMap.putAll(NewDCHostCostMap);
		NewDCHostCostMap.clear();
		initPanel_Cloud(CloudNum);
		initPanel_FogNode(FogNodeNum);
		initPanel_EndDevice(EndDeviceNum);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				List<String> list1 = new ArrayList<String>();
				List<String> list2 = new ArrayList<String>();
				for(int i = 0; i < DCMipsMap.get("cloud").size(); i++){
					list1.add(DCMipsMap.get("cloud").get(i).getText());
					list2.add(DCCostMap.get("cloud").get(i).getText());
				}
				NewDCHostMipsMap.put("cloud", list1);
				NewDCHostCostMap.put("cloud", list2);
				
				List<String> list3 = new ArrayList<String>();
				List<String> list4 = new ArrayList<String>();
				for(int i = 0; i < DCMipsMap.get("fog").size(); i++){
					list3.add(DCMipsMap.get("fog").get(i).getText());
					list4.add(DCCostMap.get("fog").get(i).getText());
				}
				NewDCHostMipsMap.put("fog", list3);
				NewDCHostCostMap.put("fog", list4);

				List<String> list5 = new ArrayList<String>();
				for(int i = 0; i < DCMipsMap.get("mobile").size(); i++){
					list5.add(DCMipsMap.get("mobile").get(i).getText());
				}
				NewDCHostMipsMap.put("mobile", list5);
				
//				System.out.println(NewDCHostMipsMap.entrySet());
			}
		});
	}
	
	private void initPanel_Cloud(int num) {
		List<JTextField> tfmipsList = new ArrayList<JTextField>();
		List<JTextField> tfcostList = new ArrayList<JTextField>();
		List<String> mipsList = new ArrayList<String>();
		List<String> costList = new ArrayList<String>();
		JPanel panel_Cloud = new JPanel();
		SettingPane.add(panel_Cloud);
		panel_Cloud.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane_Cloud = new JScrollPane();
		scrollPane_Cloud.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		panel_Cloud.add(scrollPane_Cloud);
		
		JPanel panel = new JPanel(), panel_Host, panel_parameters, panel_mips, panel_cost;
		panel.setBackground(new Color(135, 206, 250));
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JLabel jLabel1, jLabel2, jLabel3, jLabel4;
		JTextField jtext1, jtext2;
		for(int i = 0; i< num; i++){
			panel_Host = new JPanel();
			ImageIcon icon =new ImageIcon(getClass().getResource("/images/cloudServer.jpg"));
			icon.setImage(icon.getImage());
			jLabel1 = new JLabel(icon);
			panel_Host.add(jLabel1);
			
			panel_parameters = new JPanel();
			panel_parameters.setLayout(new GridLayout(3, 1, 0, 0));
			panel_Host.add(panel_parameters);
			
			jLabel2 = new JLabel("Host-"+(tfmipsList.size()+1));
			jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel2.setFont(new Font("Times New Roman", Font.PLAIN, 18));
			panel_parameters.add(jLabel2);
			
			panel_mips = new JPanel();
			jLabel3 = new JLabel("Mips : ");
			panel_mips.add(jLabel3);
			if(OldDCHostMipsMap.size() == 0 || i >= OldDCHostMipsMap.get("cloud").size())
				jtext1 = new JTextField("1600");
			else 
				jtext1 = new JTextField(OldDCHostMipsMap.get("cloud").get(i));
			jtext1.setName("Host-"+(tfmipsList.size()+1));
			tfmipsList.add(jtext1);
			mipsList.add(jtext1.getText());
			panel_mips.add(jtext1);
			panel_parameters.add(panel_mips);
			
			panel_cost = new JPanel();
			jLabel4 = new JLabel("Cost : ");
			panel_cost.add(jLabel4);
			if(OldDCHostCostMap.size() == 0 || i >= OldDCHostCostMap.get("cloud").size())
				jtext2 = new JTextField("0.96");
			else 
				jtext2 = new JTextField(OldDCHostCostMap.get("cloud").get(i));
			jtext2.setName("Host-"+(tfcostList.size()+1));
			tfcostList.add(jtext2);
			costList.add(jtext2.getText());
			panel_cost.add(jtext2);
			panel_parameters.add(panel_cost);
			
			jtext1.setPreferredSize(new Dimension(50, 22));
			jtext2.setPreferredSize(new Dimension(50, 22));
			panel.add(panel_Host);
		}
		DCMipsMap.put("cloud", tfmipsList);
		DCCostMap.put("cloud", tfcostList);
		
		NewDCHostMipsMap.put("cloud", mipsList);
		NewDCHostCostMap.put("cloud", costList);
		scrollPane_Cloud.setViewportView(panel);
		JLabel lblCloudServers = new JLabel("Cloud Servers");
		lblCloudServers.setHorizontalAlignment(SwingConstants.CENTER);
		lblCloudServers.setFont(new Font("Times New Roman", Font.PLAIN, 20));
		scrollPane_Cloud.setColumnHeaderView(lblCloudServers);
		validate();
		repaint();
	}
	
	private void initPanel_FogNode(int num) {
		List<JTextField> tfmipsList = new ArrayList<JTextField>();
		List<JTextField> tfcostList = new ArrayList<JTextField>();
		List<String> mipsList = new ArrayList<String>();
		List<String> costList = new ArrayList<String>();
		JPanel panel_FogNode = new JPanel();
		SettingPane.add(panel_FogNode);
		panel_FogNode.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane_FogNode = new JScrollPane();
		scrollPane_FogNode.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		panel_FogNode.add(scrollPane_FogNode);
		
		JPanel panel = new JPanel(), panel_Host, panel_parameters, panel_mips, panel_cost;
		panel.setBackground(new Color(255, 248, 220));
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JLabel jLabel1, jLabel2, jLabel3, jLabel4;
		JTextField jtext1, jtext2;
		for(int i = 0; i< num; i++){
			panel_Host = new JPanel();
			ImageIcon icon =new ImageIcon(getClass().getResource("/images/fogServer.jpg"));
			icon.setImage(icon.getImage());
			jLabel1 = new JLabel(icon);
			panel_Host.add(jLabel1);
			
			panel_parameters = new JPanel();
			panel_parameters.setLayout(new GridLayout(3, 1, 0, 0));
			panel_Host.add(panel_parameters);
			
			jLabel2 = new JLabel("Host-"+(tfmipsList.size()+1));
			jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel2.setFont(new Font("Times New Roman", Font.PLAIN, 18));
			panel_parameters.add(jLabel2);
			
			panel_mips = new JPanel();
			jLabel3 = new JLabel("Mips : ");
			panel_mips.add(jLabel3);
			if(OldDCHostMipsMap.size() == 0 || i >= OldDCHostMipsMap.get("fog").size())
				jtext1 = new JTextField("1300");
			else 
				jtext1 = new JTextField(OldDCHostMipsMap.get("fog").get(i));
			jtext1.setName("Host-"+(tfmipsList.size()+1));
			tfmipsList.add(jtext1);
			mipsList.add(jtext1.getText());
			panel_mips.add(jtext1);
			panel_parameters.add(panel_mips);
			
			panel_cost = new JPanel();
			jLabel4 = new JLabel("Cost : ");
			panel_cost.add(jLabel4);
			if(OldDCHostCostMap.size() == 0 || i >= OldDCHostCostMap.get("fog").size())
				jtext2 = new JTextField("0.48");
			else 
				jtext2 = new JTextField(OldDCHostCostMap.get("fog").get(i));
			jtext2.setName("Host-"+(tfcostList.size()+1));
			tfcostList.add(jtext2);
			costList.add(jtext2.getText());
			panel_cost.add(jtext2);
			panel_parameters.add(panel_cost);
			
			jtext1.setPreferredSize(new Dimension(50, 22));
			jtext2.setPreferredSize(new Dimension(50, 22));
			panel.add(panel_Host);
		}
		DCMipsMap.put("fog", tfmipsList);
		DCCostMap.put("fog", tfcostList);
		
		NewDCHostMipsMap.put("fog", mipsList);
		NewDCHostCostMap.put("fog", costList);
		scrollPane_FogNode.setViewportView(panel);
		JLabel lblFogNodes = new JLabel("Fog Nodes");
		lblFogNodes.setHorizontalAlignment(SwingConstants.CENTER);
		lblFogNodes.setFont(new Font("Times New Roman", Font.PLAIN, 20));
		scrollPane_FogNode.setColumnHeaderView(lblFogNodes);
		validate();
		repaint();
	}
	
	private void initPanel_EndDevice(int num) {
		List<JTextField> hostList = new ArrayList<JTextField>();
		List<String> mipsList = new ArrayList<String>();
		JPanel panel_EndDevice = new JPanel();
		SettingPane.add(panel_EndDevice);
		panel_EndDevice.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane_EndDevice = new JScrollPane();
		scrollPane_EndDevice.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		panel_EndDevice.add(scrollPane_EndDevice);
		
		JPanel panel = new JPanel(), panel_Host, panel_parameters, panel_mips;
		panel.setBackground(new Color(230, 230, 250));
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JLabel jLabel1, jLabel2, jLabel3;
		JTextField jtext1;
		for(int i = 0; i< num; i++){
			panel_Host = new JPanel();
			ImageIcon icon =new ImageIcon(getClass().getResource("/images/mobile.jpg"));
			icon.setImage(icon.getImage());
			jLabel1 = new JLabel(icon);
			panel_Host.add(jLabel1);
			
			panel_parameters = new JPanel();
			panel_parameters.setLayout(new GridLayout(2, 1, 0, 0));
			panel_Host.add(panel_parameters);
			
			jLabel2 = new JLabel("Host-"+(hostList.size()+1));
			jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel2.setFont(new Font("Times New Roman", Font.PLAIN, 18));
			panel_parameters.add(jLabel2);
			
			panel_mips = new JPanel();
			jLabel3 = new JLabel("Mips : ");
			panel_mips.add(jLabel3);
			if(OldDCHostMipsMap.size() == 0 || i >= OldDCHostMipsMap.get("mobile").size())
				jtext1 = new JTextField("1000");
			else 
				jtext1 = new JTextField(OldDCHostMipsMap.get("mobile").get(i));
			jtext1.setName("Host-"+(hostList.size()+1));
			jtext1.setPreferredSize(new Dimension(50, 22));
			hostList.add(jtext1);
			mipsList.add(jtext1.getText());
			panel_mips.add(jtext1);
			panel_parameters.add(panel_mips);
			panel.add(panel_Host);
		}
		DCMipsMap.put("mobile", hostList);
		NewDCHostMipsMap.put("mobile", mipsList);
		scrollPane_EndDevice.setViewportView(panel);
		JLabel lblEndDevices = new JLabel("End Devices");
		lblEndDevices.setHorizontalAlignment(SwingConstants.CENTER);
		lblEndDevices.setFont(new Font("Times New Roman", Font.PLAIN, 20));
		scrollPane_EndDevice.setColumnHeaderView(lblEndDevices);
		validate();
		repaint();
	}
	
	public void updateMap(JTextField textField, String name){
		List<String> list = new ArrayList<String>();
		List<String> newlist = new ArrayList<String>();
		String s = textField.getText().trim();//trim()方法用于去掉你可能误输入的空格号
		int index = Integer.valueOf(textField.getName().substring(5));
//		System.out.println("FogEnvironmentUI : Host-"+index);
		list = NewDCHostMipsMap.get(name);
		for(int i = 0; i < list.size(); i++)
			if(i == index -1)
				newlist.add(s);
			else
				newlist.add(list.get(i));
		NewDCHostMipsMap.put(name, newlist);
		System.out.println(NewDCHostMipsMap.entrySet());
	}
}
