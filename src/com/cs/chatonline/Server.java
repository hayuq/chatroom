package com.cs.chatonline;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * 服务端
 * @author hayuq (https://hayuq.com)
 */
public class Server {

	public static final int PORT = 8021;

	private ServerSocket serverSocket = null;

	private static boolean running = false;

	/**
	 * 消息类型：私聊，系统，广播
	 */
	private enum MessageType {
		PRIVATE, SYSTEM, BROADCAST
	};

	private DataInputStream inStream = null;
	
	private DataOutputStream outStream = null;
	
	private String clientName; //客户端名称
	
	/**
	 * 用于存放客户端消息(名称，输出对象)
	 */
	protected static HashMap<String, DataOutputStream> clients = null;

	public Server(){
		try {
			serverSocket = new ServerSocket(PORT);
			clients = new HashMap<String, DataOutputStream>();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送消息
	 * @param type 消息类型
	 * @param name 私聊时的对象名称
	 * @param msg 消息内容
	 * @throws IOException
	 */
	public synchronized void sendMsg(MessageType type, String name, String msg) {
		switch (type) {
			case SYSTEM:
			case BROADCAST:
				for (DataOutputStream outStream : clients.values()) {
					try {
						outStream.writeUTF(msg);
						outStream.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;
			case PRIVATE:
				if (!name.isEmpty() && (outStream = clients.get(name)) != null) {
					try {
						outStream.writeUTF(msg);
						outStream.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;
			default:
				break;
			}
	}

	/**
	 * 该线程循环接收客户端消息并转发到客户端控制台
	 */
	public class GetClientMsgThread extends Thread {

		private Socket socket = null;
		
		public GetClientMsgThread(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {

			try {
				sendMsg(MessageType.BROADCAST, "", MessageFormat.format("【系统消息】欢迎 {0} 进入聊天室！", clientName));
				String msg;
				//获取消息内容
				while ((msg = inStream.readUTF()) != null) {
					// 判断是否是私聊（私聊格式：@昵称 内容）
					if (msg.startsWith("@")) {
						int index = msg.indexOf(" ");
						if (index >= 0) {
							//获取名称
							String name = msg.substring(1, index);
							msg = MessageFormat.format("{0} 对你说 ({1})：\r\n  {2}", clientName, getTime(), msg.substring(index + 1, msg.length()));
							sendMsg(MessageType.PRIVATE, name, msg);
							continue;
						}
					}
					sendMsg(MessageType.BROADCAST, "", MessageFormat.format("{0} ({1})：\r\n  {2}", clientName, getTime(), msg));
//					sendMsg(MessageType.BROADCAST, "", MessageFormat.format("{0}({1}) {2}：\r\n  {3}", clientName, socket.getLocalSocketAddress(), getTime(), msg));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 下线后将该客户端的信息从Map集合中移除
				clients.remove(clientName);
				String msg = MessageFormat.format("【系统消息】 {0} 已经下线了，当前在线人数：{1}", clientName, clients.size());
				sendMsg(MessageType.SYSTEM, "", msg);
				try {
					if (socket != null){
						socket.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 启动服务端
	 */
	private void start() {
		System.out.println("服务端已启动，等待客户端连接......");
		while (true) { // 循环等待客户端连接
			try {
				Socket socket = serverSocket.accept();// 程序会阻塞
				System.out.println("有新的客户端接入");
				inStream = new DataInputStream(socket.getInputStream());
				outStream = new DataOutputStream(socket.getOutputStream());

				clientName = inStream.readUTF();
				System.out.println("客户端名称："+clientName);
				//上线后将该客户端信息保存到Map集合中
				clients.put(clientName, outStream);
				
				new GetClientMsgThread(socket).start(); // 启动接收消息的线程
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取当前时间
	 * @return yyyy-MM-dd HH:mm:ss格式的时间字符串
	 */
	public static String getTime(){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(new Date());
	}
	
	public static void main(String[] args) {
		if (running) {
			System.out.println("服务端已经在运行，不能重复启动");
			return;
		}
		new Server().start();
		running = true;
	}
}
