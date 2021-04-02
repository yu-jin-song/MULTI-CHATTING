import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

public class ChattingServer {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ChatServer mainFrame = new ChatServer("Server");
		mainFrame.initForm();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(250, 400);
		mainFrame.setVisible(true);
		
	}

}


class ChatServer extends JFrame {	
	/*
	 ** ���� ����
	 */
	private JPanel pan, topPan, midPan;	// ������ ȭ��
	private JTextField sendTextBox;	// �޽��� �Է� â
	private JTextField serverIp, portNo, serverName;
	private JButton startBt, stopBt;
	private JButton sendButton;	// ���� ��ư
	private SendMsgHandler sendMsgHandler;	// send ��ư�̳� enter ������ �� ó���� Handler reference
	
	private ServerSocket serverSocket;
	JTextArea showDlgBox;
	// ��ü �ܺο��� ���� �ʿ� �� private ���x
	ServerReceiveThread chatTrd;
	Vector <ServerReceiveThread> vClient; // ������ ���� ����
	Socket dataSocket;	// �޽��� ��� â
	boolean listening;
	
	public ChatServer() {	}
	public ChatServer(String str) {
		super(str);
		vClient = new Vector<ServerReceiveThread>();	// ������ ���� ������ vector ����
	}
	
	/*
	 * ȭ�� �����ο� �ʿ��� component ����
	 */
	public void initForm() {
		sendTextBox = new JTextField(30);
		showDlgBox = new JTextArea(20, 40);
		showDlgBox.setLineWrap(true);
		JScrollPane scrollPane11 = new JScrollPane(showDlgBox);
		showDlgBox.setEditable(false);
		
		pan = new JPanel();
		topPan = new JPanel();
		midPan = new JPanel();
		
		InetAddress inet = null;	//IP address Class ��ü ����
		try{
			inet = InetAddress.getLocalHost();	// LocalHost�� IP Address ������
		} catch(UnknownHostException e) {
			e.printStackTrace();
		}
		
		serverIp = new JTextField(inet.getHostAddress(), 10);
		portNo = new JTextField("1234", 10);
		portNo.setEditable(true);
		portNo.selectAll();
		serverName = new JTextField("Server", 10);
		serverName.setEditable(false);
		startBt = new JButton("Server Start");
		startBt.setEnabled(true);
		startBt.addActionListener(new ChatActionHandler());
		stopBt = new JButton("Server Stop");
		stopBt.setEnabled(false);
		stopBt.addActionListener(new ChatActionHandler());
		
		topPan.setLayout(new GridLayout(4, 2, 0, 10));
		
		topPan.add(new Label("Server Ip"));
		topPan.add(serverIp);
		topPan.add(new Label("Port No"));
		topPan.add(portNo);
		topPan.add(new Label("Name"));
		topPan.add(serverName);
		topPan.add(startBt);
		topPan.add(stopBt);
		
		sendButton = new JButton("Send");
		sendButton.setEnabled(false);
		sendMsgHandler = new SendMsgHandler();
		sendButton.addActionListener(sendMsgHandler);
		sendTextBox.addActionListener(sendMsgHandler);
		
		midPan.setLayout(new BorderLayout());
		midPan.add("Center", scrollPane11);
		midPan.add("South", sendTextBox);
		
		pan.setLayout(new BorderLayout());
		pan.add("North", topPan);
		pan.add("Center", midPan);
		pan.add("South", sendButton);
		
		Container cpane;
		cpane = getContentPane();
		cpane.add(pan);
		pack();
		setVisible(true);
	} //----------------------initForm ��
	
	public void broadcast(String msg) throws IOException {
		for(int i=0; i<vClient.size(); i++) {
			ServerReceiveThread trd = ((ServerReceiveThread)vClient.elementAt(i));	//�����ں� Msg ó�� Thread ����
			trd.socketOut.println(msg);	// �� ��ü�� message �����Ͽ� ó��
		}
		
		//server ä��ȭ�鿡 ���
		showDlgBox.append(  msg + "\n");
		showDlgBox.setCaretPosition(showDlgBox.getDocument().getLength()); // ��ũ�ѹ��� ��ġ�� �� �Ʒ���
	}
	
	public class ChatActionHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand().equals("Server Start")) {	// start ��ư ������ ��
				showDlgBox.append("������ �����մϴ�. \n");
				ChatAcceptThread acceptThread = new ChatAcceptThread();
				acceptThread.start();
					
				startBt.setEnabled(false);
				stopBt.setEnabled(true);
				sendButton.setEnabled(true);
				sendTextBox.requestFocus();	// �޽��� �Է�â���� focus ������
			}
			else {	// stop ��ư ������ ��
				showDlgBox.append("������ �����մϴ�. \n");
				startBt.setEnabled(true);
				stopBt.setEnabled(false);
				sendButton.setEnabled(false);
				listening = false;
					
				try {
					serverSocket.close();	// server socket ����
				} catch(IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	} //-------- ActionHandler �� 
	
	class SendMsgHandler implements ActionListener {	// �޽��� ���� Handler
		public void actionPerformed(ActionEvent evt) {
			String strMsg;
			
			try {
				strMsg = sendTextBox.getText();
				if(!strMsg.isEmpty()) {	// ���۸޽����� �ִ� ���
					String sendMsg = "[Server] " + strMsg;
					broadcast(sendMsg);
					System.out.println(strMsg);	// server�� ����
					sendTextBox.setText("");	// �޽��� ���� �� �޽��� �Է�â �ʱ�ȭ
					sendTextBox.requestFocus();	// �޽��� �Է�â���� focus
				}
			} catch(Exception e) {
				showDlgBox.append("���� ���� \n");
			}
		}
	}//-------- Message �ְ�ޱ� ó�� ��
	
	public class ChatAcceptThread extends Thread {	// ä�� ����
		ServerReceiveThread chatTrd;
		PrintWriter socketOut;
		
		ChatAcceptThread() {	}
		
		public void run() {
			int port = Integer.parseInt(portNo.getText());
			listening = true;
			
			try {
				serverSocket = new ServerSocket(port);
			} catch(IOException e) {
				showDlgBox.append("Server Socket �ʱ�ȭ Error \n");
				return;
			}
			
			startBt.setEnabled(false);
			stopBt.setEnabled(true);
			showDlgBox.append(port + " Port ���� ������ ��ٸ��ϴ�. \n");
			
			try {
				while(listening) {	// start ��ư�� ���� ������ ����
					dataSocket = serverSocket.accept();
					chatTrd = new ServerReceiveThread(ChatServer.this);	// server�� reference ����
					chatTrd.start();
					vClient.addElement(chatTrd);	// ������ �߰�
				}
				serverSocket.close();
			}
			catch(IOException e) {	}
			
			showDlgBox.append("������ �����մϴ�. \n");
		}//--------- run ��
	}//-------------SjChatAcceptThread ��
}//-------------ChatServer class ��


class ServerReceiveThread extends Thread {	// �޽��� ���� ó�� thread
	Socket clientSocket = null;
	PrintWriter socketOut;	// server �� client
	BufferedReader socketIn;	// client �� server
	String strInput, strName;
	ChatServer chatServer;	// server ��ü
	
	public ServerReceiveThread() {	}
	public ServerReceiveThread(ChatServer chatServer) {	// server�� reference ���޹ޱ�
		clientSocket = chatServer.dataSocket;
		this.chatServer = chatServer;
	}
	
	public void removeClient() throws IOException {
		String strMsg;
		
		chatServer.vClient.removeElement(this);
		for(int i=0; i<chatServer.vClient.size(); i++) {
			ServerReceiveThread trd = ((ServerReceiveThread)chatServer.vClient.elementAt(i));
			socketOut.println( trd.strName );
		}
		strMsg = "[" + strName + "] ���� �����ϼ̽��ϴ�.";
		chatServer.broadcast(strMsg);
	}
	
	public void sendUserList() throws IOException {
		int cnt = chatServer.vClient.size() + 1;
		socketOut.println("< ���� ������ (" + cnt + "��)>");
		socketOut.println( "Server " );
		for(int i=0; i<chatServer.vClient.size(); i++) {
			ServerReceiveThread trd = ((ServerReceiveThread)chatServer.vClient.elementAt(i));
			socketOut.println( trd.strName );
		}
	}
	
	public void run() {
		try {
			chatServer.showDlgBox.append("Client: " + clientSocket.toString() + "���� �����Ͽ����ϴ�. \n");
			socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
			socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			socketOut.println("ChatServer");	// server Ȯ�� �޽��� ����
			strInput = socketIn.readLine();
			
			if(strInput.equals("ChatClient")) {	// client Ȯ��
				socketOut.println(  "<����Ű> /h(����), /u(�����ڸ��)");
				strName = socketIn.readLine();
				
				chatServer.broadcast("[" + strName + "] ���� �����ϼ̽��ϴ�.");
				
				while((strInput = socketIn.readLine()) != null) {	// client���� ������ ������ �����ϴ� ����
					if(strInput.equals("/h")) {
						socketOut.println(  "<����Ű> /h(����), /u(�����ڸ��)");
					}
					else if(strInput.equals("/u")) {
						sendUserList();
					}
					else {	// �޽��� ���
						chatServer.broadcast("[" + strName + "] " + strInput);
					}
				}
			}
			else {
				socketOut.println("�߸��� Client�Դϴ�.");
			}
			socketOut.close();
			socketIn.close();
			clientSocket.close();
			removeClient();
		}
		catch(IOException e) {
			try {
				removeClient();
			}
			catch(IOException e1) {
				chatServer.showDlgBox.append(" " + strName + "�� ������ ������ϴ�.");
			}
		}
	}//---------------run ��
}//-------------------ChatReceiveThread class ��