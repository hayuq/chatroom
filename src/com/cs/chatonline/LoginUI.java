package com.cs.chatonline;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * 用户登录界面
 * @author hayuq (https://hayuq.com)
 */
public class LoginUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static final String USER_FILE = "user.properties";

	private JTextField txt_name = null;
	private JPasswordField txt_pwd = null;
	private JButton btn_login = null;
	private JButton btn_register = null;
	private JButton btn_reset = null;
	private Box b = null;
	private Box b1 = null;
	private Box b2 = null;
	private JPanel panel = null;
	private Container container = null;

	public LoginUI() {
		this.setTitle("MyChat 用户登录或注册");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.setSize(150, 100);
		this.setResizable(false);
		this.setLocationRelativeTo(null);// 窗口在屏幕中间显示
		init();
		this.pack();
		this.setVisible(true);
	}

	/**
	 * 初始化组件
	 */
	private void init() {
		container = this.getContentPane();
		b = Box.createHorizontalBox();
		b1 = Box.createVerticalBox();
		b2 = Box.createVerticalBox();

		txt_name = new JTextField(20);
		txt_pwd = new JPasswordField(20);
		txt_pwd.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					login();
				}
			}
			
		});

		b1.add(Box.createVerticalStrut(5));
		b1.add(new JLabel("用户名"));
		b1.add(Box.createVerticalStrut(5));
		b1.add(new JLabel("密    码"));
		b2.add(Box.createVerticalStrut(5));
		b2.add(txt_name);
		b2.add(Box.createVerticalStrut(5));
		b2.add(txt_pwd);
		btn_login = new JButton("登录");
		btn_reset = new JButton("重置");
		btn_register = new JButton("注册");
		btn_login.addActionListener(this);
		btn_reset.addActionListener(this);
		btn_register.addActionListener(this);

		b.add(Box.createHorizontalStrut(5));
		b.add(b1);
		b.add(Box.createHorizontalStrut(5));
		b.add(b2);
		b.add(Box.createHorizontalStrut(5));
		panel = new JPanel();
		panel.add(btn_login);
		panel.add(btn_reset);
		panel.add(btn_register);
		container.add(b, BorderLayout.NORTH);
		container.add(panel, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj.equals(btn_login)) {
			login();
		} else if (obj.equals(btn_reset)) {
			txt_name.setText("");
			txt_pwd.setText("");
		} else if (obj.equals(btn_register)) {
			register();
		}
	}

	/**
	 * 注册
	 */
	private void register() {
		OutputStream outStream = null;
		Properties properties = new Properties();
		String name = txt_name.getText().trim();
		String pwd = new String(txt_pwd.getPassword());
		try {
			if (name.isEmpty() || pwd.isEmpty()) {
				JOptionPane.showMessageDialog(null, "用户名或密码不能为空");
				return;
			}
			// 向properties文件写入注册信息
			outStream = new FileOutputStream(USER_FILE, true);
			properties.setProperty(name, pwd);
			properties.store(outStream, null);
			JOptionPane.showMessageDialog(null, "注册成功");
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, "注册失败");
			e1.printStackTrace();
		}
	}

	/**
	 * 登录
	 */
	private void login() {
		InputStream inStream = null;
		Properties properties = new Properties();
		try {
			File file = new File(USER_FILE);
			if (!file.exists()) {
				file.createNewFile();
			}
			inStream = new FileInputStream(file);
			properties.load(inStream);
			String name = txt_name.getText().trim();
			if (!properties.containsKey(name)) {
				JOptionPane.showMessageDialog(null, "该用户不存在，请先注册");
			}
			else if (properties.getProperty(name).equals(new String(txt_pwd.getPassword()))) {
				new Client(name).start(); // 登录成功后启动客户端
				dispose(); //释放当前资源
			} 
			else {
				JOptionPane.showMessageDialog(null, "用户名或密码错误，登录失败");
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, "登录失败");
			e1.printStackTrace();
		}
	}
}
