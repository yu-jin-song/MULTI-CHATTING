// Swing 사용 Chatting 구현
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;

public class ChattingClient extends JFrame {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ChattingClient("Client").setVisible(true);
	}
	
	ChattingClient() {	}
	ChattingClient(final String str) {
		super(str);
		initForm();
	}
	
	void initForm() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		pan = new JPanel();
		topPan = new JPanel();
		midPan = new JPanel();
		
		serverIp = new JTextField("localhost", 10);
		portNo = new JTextField("1234", 10);
		userName = new JTextField("손님",10);
		sendTextBox = new JTextField(21);
		sendTextBox.setText("대화할 수 없습니다.");
		sendTextBox.setEnabled(false);
		
		connectBt = new JButton("Connect");
		disconnectBt = new JButton("Disconnect");
		sendButton = new JButton("Send");
		
		showDlgBox = new JTextArea(10, 10);
		showDlgBox.setLineWrap(true);
		JScrollPane scrollPane11 = new JScrollPane(showDlgBox);
		
		midPan.setLayout(new BorderLayout());
		midPan.add("Center", scrollPane11);
		midPan.add("South", sendTextBox);
		
		topPan.setLayout(new GridLayout(4, 2, 0, 10));
		topPan.add(new Label("Server Ip"));
		topPan.add(serverIp);
		topPan.add(new Label("Port No"));
		topPan.add(portNo);
		topPan.add(new Label("Name"));
		topPan.add(userName);
		topPan.add(connectBt);
		topPan.add(disconnectBt);
		
		pan.setLayout(new BorderLayout());
		pan.add("North", topPan);
		pan.add("Center", midPan);
		pan.add("South", sendButton);
		
		showDlgBox.setEditable(false);
		connectBt.setEnabled(true);
		disconnectBt.setEnabled(false);
		sendButton.setEnabled(false);
		
		// main 화면에 추가
		getContentPane().add(pan);
		setSize(250, 400);
		setVisible(true);
		
		// Event Handler 구현
		connectBt.addActionListener(
			new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt){
					connectBtActionPerformed(evt);
				}
			}
		);
		disconnectBt.addActionListener(new disConnectHandler());
		sendMsgHandler = new sendMsgHandler();
		sendButton.addActionListener(sendMsgHandler);	// send 버튼 눌렀을 때 message 전송
		sendTextBox.addActionListener(sendMsgHandler);	// enter 눌렀을 때 message 전송
		userName.requestFocus();	// user 이름에 focus
	}	//--------initForm() 끝
	
	private void connectBtActionPerformed(java.awt.event.ActionEvent evt) {
		String strMsg;
		int cnt = 0;
		
		try {
			echoSocket = new Socket(serverIp.getText(), Integer.parseInt(portNo.getText()));	// TextField에 입력된 주소로 접속
			
			// 입출력용 스트림 생성
			socketOut = new PrintStream(echoSocket.getOutputStream(), true);
			socketIn = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
			
			strMsg = socketIn.readLine();	// 접속할 server 확인
			
			if(strMsg.equals("ChatServer"))	{	// 접속하려는 server가 맞다면
				socketOut.println("ChatClient");	// client 확인 메시지 전송
				socketOut.println(userName.getText());	// 대화명 전송
				
				// 전송되어온 메시지 처리할 Thread 생성, 메시지 출력 위해 입력 스트림과 메시지 창 전송
				chatTrd = new ChatReceiveThread(socketIn, showDlgBox);
				chatTrd.start();
				
				sendTextBox.requestFocus();	// 메시지 입력 창으로 focus
				connectBt.setEnabled(false);
				disconnectBt.setEnabled(true);
				sendButton.setEnabled(true);
				sendTextBox.setText("");
				sendTextBox.setEnabled(true);
			}
			else {
				showDlgBox.append("잘못된 Server 입니다.\n");
			}
		}
		catch(UnknownHostException e) {
			showDlgBox.append("존재하지 않는 Server 입니다.\n");
		}
		catch(IOException e) {
			showDlgBox.append("입출력 Error\n");
		}
		catch(Exception e) {
			showDlgBox.append("연결이 끊겼습니다.\n");
		}
		
	}	//----------------connectBtActionPerformed() 끝
	
	class disConnectHandler implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				socketOut.close();
				socketIn.close();
				echoSocket.close();
				connectBt.setEnabled(true);
				disconnectBt.setEnabled(false);
				sendButton.setEnabled(false);
				sendTextBox.setText("대화할 수 없습니다.");
				sendTextBox.setEnabled(false);
			} catch(IOException e) {
				showDlgBox.append("입출력 Error\n");
			}
		}
	}
	
	class sendMsgHandler implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			String strMsg;
			
			try {
				strMsg = sendTextBox.getText();
				if(!strMsg.isEmpty()) {	// 전송하려는 메시지가 존재하는 경우
					socketOut.println(strMsg);	// server로 전송
					sendTextBox.setText("");	// 메시지 입력창 초기화
					sendTextBox.requestFocus();	// 메시지 입력창에 focus
				}
			}
			catch(Exception e) {
				showDlgBox.append("전송 오류\n");
			}
		}
	}
	
	// 변수 정의
	private Socket echoSocket;	// socket reference variable
	private PrintStream socketOut;	// client → server
	private BufferedReader socketIn;	// server → client
	private sendMsgHandler sendMsgHandler;	// send 버튼 혹은 enter 눌렀을 경우 처리 Handler reference
	private ChatReceiveThread chatTrd;	// 전송되어 온 메시지 처리 thread
	
	private JPanel pan, topPan, midPan;
	private JTextArea showDlgBox;	// 채팅 내용 출력 창
	private JTextField sendTextBox;	// 전송할 메시지 입력 창
	private JTextField serverIp, portNo, userName;	// server IP, port 번호, 대화명
	private JButton connectBt, disconnectBt, sendButton;	// 접속, 접속중단, 전송 버튼
	
	
	
	class ChatReceiveThread extends Thread {	// 전송되어 온 메시지 처리 Thread
		BufferedReader socketIn = null;
		JTextArea showDlgBox;
		String strSocket;
		
		ChatReceiveThread() {	}
		ChatReceiveThread(BufferedReader socketIn, JTextArea showDlgBox) {
			this.socketIn = socketIn;
			this.showDlgBox = showDlgBox;
		}
		
		public void run() {
			try {
				while((strSocket = socketIn.readLine()) != null) {
					if (strSocket.equals("SERVER-TERMINATED")) {
						disconnectBt.doClick();
	                    throw new Exception();
	                }
					
					showDlgBox.append(strSocket + "\n");
					showDlgBox.setCaretPosition(showDlgBox.getDocument().getLength());
				}	// server를 통하여 출력하므로 직접 출력x
			}
			catch(Exception e) {
				showDlgBox.append("연결이 끊겼습니다. \n");
			}
		}
	}	//-------------------ChatReceiveThread class 끝
}	//-------------------ChattingClient class 끝