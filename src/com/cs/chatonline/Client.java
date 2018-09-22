package com.cs.chatonline;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * 客户端程序
 * @author hayuq (https://hayuq.com)
 */
public class Client extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;

	private static Socket socket = null;
	
	private String name = null; //名称

	private static JTextArea receiveArea = null;
	private JTextArea sendArea = null;
	private Container container = null;
	private JScrollPane pane1 = null;
	private JScrollPane pane2 = null;
	private JPanel panel = null;
	private JButton btn_send = null; //发送消息按钮
	private JButton btn_exit = null; //退出按钮
	private JButton btn_save = null; //保存聊天记录按钮
	
	static{
		try {
			socket = new Socket("127.0.0.1", Server.PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Client(String name) {
		this.name = name;
		
		this.setTitle(MessageFormat.format("MyChat - 即时聊天工具（{0}）", name));
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				int op = JOptionPane.showConfirmDialog(null, "确定退出聊天程序吗？", "提示", JOptionPane.YES_NO_OPTION);
				if (op == 0)
					System.exit(0);
			}
		});
		this.setLayout(new BorderLayout());
		this.setSize(500, 400);
		this.setLocationRelativeTo(null); // 居中
		this.setResizable(false);
		init();
		this.setVisible(true);
	}

	/**
	 * 初始化组件
	 */
	private void init() {
		receiveArea = new JTextArea();
		receiveArea.setRows(12);
		receiveArea.setEditable(false);
		receiveArea.setFocusable(false);
		receiveArea.setLineWrap(true);
		receiveArea.setFont(new Font(Font.SANS_SERIF, Font.TRUETYPE_FONT, 13));

		sendArea = new JTextArea();
		sendArea.setLineWrap(true);
		sendArea.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						sendMsg(sendArea.getText().trim());
						sendArea.setText("");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
			
		});
		
		Border etched = BorderFactory.createEtchedBorder();
	    Border tbdReciever = BorderFactory.createTitledBorder(etched, "收到的消息");
	    Border tbdSend = BorderFactory.createTitledBorder(etched, "发送消息");
		receiveArea.setBorder(tbdReciever);
		sendArea.setBorder(tbdSend);
	    
		container = this.getContentPane();
		((JComponent) container).setBorder(new EmptyBorder(5, 5, 5, 5));
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		btn_send = new JButton("发送");
		btn_save = new JButton("保存聊天记录");
		btn_exit = new JButton("退出");
		btn_send.addActionListener(this);
		btn_save.addActionListener(this);
		btn_exit.addActionListener(this);
		
		pane1 = new JScrollPane();
		pane2 = new JScrollPane();
		pane1.setViewportView(receiveArea);
		pane2.setViewportView(sendArea);
		panel.add(btn_send);
		panel.add(btn_save);
		panel.add(btn_exit);
		container.add(pane1,BorderLayout.NORTH);
		container.add(pane2,BorderLayout.CENTER);
		container.add(panel,BorderLayout.SOUTH);
	}
	
	/**
	 * 发送消息方法
	 * @throws IOException
	 */
	public void sendMsg(String message) throws IOException {
		DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
		if(message.isEmpty()) {
			JOptionPane.showMessageDialog(null, "不能发送空的内容");
			return;
		}
		outStream.writeUTF(message);
		outStream.flush();
	}

	/**
	 * 该线程只负责循环接收服务端消息并输出到控制台
	 */
	public class GetServerMsgThread extends Thread {

		@Override
		public void run() {
			try {
				DataInputStream inStream = new DataInputStream(socket.getInputStream());
				while(true) {
					receiveArea.append("\r\n" + inStream.readUTF());
				}
			} catch (IOException e) {
				receiveArea.append("\r\n与服务器的连接已断开......");
			}finally {
				try {
					if (socket != null)
						socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 启动客户端
	 */
	public void start() {
		try {
			sendMsg(name); //发送当前客户端名称给服务端
			new GetServerMsgThread().start(); // 启动接收服务端消息的线程
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj.equals(btn_exit)) {
			int op = JOptionPane.showConfirmDialog(null, "确定退出聊天程序吗？","提示",JOptionPane.YES_NO_OPTION);
			switch (op) {
				case 0: System.exit(0);
				case 1:
				default: break;
			}
		}
		else if (obj.equals(btn_send)) {
			String msgStr = sendArea.getText().trim();
			try {
				sendMsg(msgStr);
				sendArea.setText("");//清空消息发送区
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else if (obj.equals(btn_save)) {
			String msg = receiveArea.getText().trim();
			if (msg.isEmpty()) {
				JOptionPane.showMessageDialog(null, "当前不存在聊天记录");
				return;
			}
			try {
				FileOutputStream outputStream = new FileOutputStream(name + "-records.txt");
				outputStream.write(msg.getBytes());
				outputStream.flush();
				outputStream.close();
				JOptionPane.showMessageDialog(null, "保存成功");
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(null, "保存失败");
				e1.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		
		//启动客户端后首先进入登录界面
		new LoginUI(); 
	}

}
