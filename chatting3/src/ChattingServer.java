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
	 ** 변수 정의
	 */
	private JPanel pan, topPan, midPan;	// 오른쪽 화면
	private JTextField sendTextBox;	// 메시지 입력 창
	private JTextField serverIp, portNo, serverName;
	private JButton startBt, stopBt;
	private JButton sendButton;	// 전송 버튼
	private SendMsgHandler sendMsgHandler;	// send 버튼이나 enter 눌렀을 때 처리할 Handler reference
	
	private ServerSocket serverSocket;
	JTextArea showDlgBox;
	// 객체 외부에서 접속 필요 → private 사용x
	ServerReceiveThread chatTrd;
	Vector <ServerReceiveThread> vClient; // 접속자 정보 저장
	Socket dataSocket;	// 메시지 출력 창
	boolean listening;
	
	public ChatServer() {	}
	public ChatServer(String str) {
		super(str);
		vClient = new Vector<ServerReceiveThread>();	// 접속자 정보 저장할 vector 생성
	}
	
	/*
	 * 화면 디자인에 필요한 component 생성
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
		
		InetAddress inet = null;	//IP address Class 객체 생성
		try{
			inet = InetAddress.getLocalHost();	// LocalHost의 IP Address 얻어오기
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
	} //----------------------initForm 끝
	
	public void broadcast(String msg) throws IOException {
		for(int i=0; i<vClient.size(); i++) {
			ServerReceiveThread trd = ((ServerReceiveThread)vClient.elementAt(i));	//접속자별 Msg 처리 Thread 생성
			trd.socketOut.println(msg);	// 각 객체로 message 전달하여 처리
		}
		
		//server 채팅화면에 출력
		showDlgBox.append(  msg + "\n");
		showDlgBox.setCaretPosition(showDlgBox.getDocument().getLength()); // 스크롤바의 위치를 맨 아래로
	}
	
	public class ChatActionHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand().equals("Server Start")) {	// start 버튼 눌렀을 때
				showDlgBox.append("서버를 시작합니다. \n");
				ChatAcceptThread acceptThread = new ChatAcceptThread();
				acceptThread.start();
					
				startBt.setEnabled(false);
				stopBt.setEnabled(true);
				sendButton.setEnabled(true);
				sendTextBox.requestFocus();	// 메시지 입력창으로 focus 보내기
			}
			else {	// stop 버튼 눌렀을 때
				showDlgBox.append("서버를 종료합니다. \n");
				startBt.setEnabled(true);
				stopBt.setEnabled(false);
				sendButton.setEnabled(false);
				listening = false;
					
				try {
					serverSocket.close();	// server socket 종료
				} catch(IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	} //-------- ActionHandler 끝 
	
	class SendMsgHandler implements ActionListener {	// 메시지 전송 Handler
		public void actionPerformed(ActionEvent evt) {
			String strMsg;
			
			try {
				strMsg = sendTextBox.getText();
				if(!strMsg.isEmpty()) {	// 전송메시지가 있는 경우
					String sendMsg = "[Server] " + strMsg;
					broadcast(sendMsg);
					System.out.println(strMsg);	// server로 전송
					sendTextBox.setText("");	// 메시지 전송 후 메시지 입력창 초기화
					sendTextBox.requestFocus();	// 메시지 입력창으로 focus
				}
			} catch(Exception e) {
				showDlgBox.append("전송 오류 \n");
			}
		}
	}//-------- Message 주고받기 처리 끝
	
	public class ChatAcceptThread extends Thread {	// 채팅 수락
		ServerReceiveThread chatTrd;
		PrintWriter socketOut;
		
		ChatAcceptThread() {	}
		
		public void run() {
			int port = Integer.parseInt(portNo.getText());
			listening = true;
			
			try {
				serverSocket = new ServerSocket(port);
			} catch(IOException e) {
				showDlgBox.append("Server Socket 초기화 Error \n");
				return;
			}
			
			startBt.setEnabled(false);
			stopBt.setEnabled(true);
			showDlgBox.append(port + " Port 에서 접속을 기다립니다. \n");
			
			try {
				while(listening) {	// start 버튼이 눌린 상태인 동안
					dataSocket = serverSocket.accept();
					chatTrd = new ServerReceiveThread(ChatServer.this);	// server의 reference 전송
					chatTrd.start();
					vClient.addElement(chatTrd);	// 접속자 추가
				}
				serverSocket.close();
			}
			catch(IOException e) {	}
			
			showDlgBox.append("서버를 종료합니다. \n");
		}//--------- run 끝
	}//-------------SjChatAcceptThread 끝
}//-------------ChatServer class 끝


class ServerReceiveThread extends Thread {	// 메시지 전달 처리 thread
	Socket clientSocket = null;
	PrintWriter socketOut;	// server → client
	BufferedReader socketIn;	// client → server
	String strInput, strName;
	ChatServer chatServer;	// server 객체
	
	public ServerReceiveThread() {	}
	public ServerReceiveThread(ChatServer chatServer) {	// server의 reference 전달받기
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
		strMsg = "[" + strName + "] 님이 퇴장하셨습니다.";
		chatServer.broadcast(strMsg);
	}
	
	public void sendUserList() throws IOException {
		int cnt = chatServer.vClient.size() + 1;
		socketOut.println("< 현재 접속자 (" + cnt + "명)>");
		socketOut.println( "Server " );
		for(int i=0; i<chatServer.vClient.size(); i++) {
			ServerReceiveThread trd = ((ServerReceiveThread)chatServer.vClient.elementAt(i));
			socketOut.println( trd.strName );
		}
	}
	
	public void run() {
		try {
			chatServer.showDlgBox.append("Client: " + clientSocket.toString() + "에서 접속하였습니다. \n");
			socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
			socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			socketOut.println("ChatServer");	// server 확인 메시지 전송
			strInput = socketIn.readLine();
			
			if(strInput.equals("ChatClient")) {	// client 확인
				socketOut.println(  "<단축키> /h(도움말), /u(접속자목록)");
				strName = socketIn.readLine();
				
				chatServer.broadcast("[" + strName + "] 님이 입장하셨습니다.");
				
				while((strInput = socketIn.readLine()) != null) {	// client에서 가져올 정보가 존재하는 동안
					if(strInput.equals("/h")) {
						socketOut.println(  "<단축키> /h(도움말), /u(접속자목록)");
					}
					else if(strInput.equals("/u")) {
						sendUserList();
					}
					else {	// 메시지 출력
						chatServer.broadcast("[" + strName + "] " + strInput);
					}
				}
			}
			else {
				socketOut.println("잘못된 Client입니다.");
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
				chatServer.showDlgBox.append(" " + strName + "의 접속이 끊겼습니다.");
			}
		}
	}//---------------run 끝
}//-------------------ChatReceiveThread class 끝