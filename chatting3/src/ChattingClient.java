// Swing ��� Chatting ����
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
		userName = new JTextField("�մ�",10);
		sendTextBox = new JTextField(21);
		sendTextBox.setText("��ȭ�� �� �����ϴ�.");
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
		
		// main ȭ�鿡 �߰�
		getContentPane().add(pan);
		setSize(250, 400);
		setVisible(true);
		
		// Event Handler ����
		connectBt.addActionListener(
			new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt){
					connectBtActionPerformed(evt);
				}
			}
		);
		disconnectBt.addActionListener(new disConnectHandler());
		sendMsgHandler = new sendMsgHandler();
		sendButton.addActionListener(sendMsgHandler);	// send ��ư ������ �� message ����
		sendTextBox.addActionListener(sendMsgHandler);	// enter ������ �� message ����
		userName.requestFocus();	// user �̸��� focus
	}	//--------initForm() ��
	
	private void connectBtActionPerformed(java.awt.event.ActionEvent evt) {
		String strMsg;
		int cnt = 0;
		
		try {
			echoSocket = new Socket(serverIp.getText(), Integer.parseInt(portNo.getText()));	// TextField�� �Էµ� �ּҷ� ����
			
			// ����¿� ��Ʈ�� ����
			socketOut = new PrintStream(echoSocket.getOutputStream(), true);
			socketIn = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
			
			strMsg = socketIn.readLine();	// ������ server Ȯ��
			
			if(strMsg.equals("ChatServer"))	{	// �����Ϸ��� server�� �´ٸ�
				socketOut.println("ChatClient");	// client Ȯ�� �޽��� ����
				socketOut.println(userName.getText());	// ��ȭ�� ����
				
				// ���۵Ǿ�� �޽��� ó���� Thread ����, �޽��� ��� ���� �Է� ��Ʈ���� �޽��� â ����
				chatTrd = new ChatReceiveThread(socketIn, showDlgBox);
				chatTrd.start();
				
				sendTextBox.requestFocus();	// �޽��� �Է� â���� focus
				connectBt.setEnabled(false);
				disconnectBt.setEnabled(true);
				sendButton.setEnabled(true);
				sendTextBox.setText("");
				sendTextBox.setEnabled(true);
			}
			else {
				showDlgBox.append("�߸��� Server �Դϴ�.\n");
			}
		}
		catch(UnknownHostException e) {
			showDlgBox.append("�������� �ʴ� Server �Դϴ�.\n");
		}
		catch(IOException e) {
			showDlgBox.append("����� Error\n");
		}
		catch(Exception e) {
			showDlgBox.append("������ ������ϴ�.\n");
		}
		
	}	//----------------connectBtActionPerformed() ��
	
	class disConnectHandler implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				socketOut.close();
				socketIn.close();
				echoSocket.close();
				connectBt.setEnabled(true);
				disconnectBt.setEnabled(false);
				sendButton.setEnabled(false);
				sendTextBox.setText("��ȭ�� �� �����ϴ�.");
				sendTextBox.setEnabled(false);
			} catch(IOException e) {
				showDlgBox.append("����� Error\n");
			}
		}
	}
	
	class sendMsgHandler implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			String strMsg;
			
			try {
				strMsg = sendTextBox.getText();
				if(!strMsg.isEmpty()) {	// �����Ϸ��� �޽����� �����ϴ� ���
					socketOut.println(strMsg);	// server�� ����
					sendTextBox.setText("");	// �޽��� �Է�â �ʱ�ȭ
					sendTextBox.requestFocus();	// �޽��� �Է�â�� focus
				}
			}
			catch(Exception e) {
				showDlgBox.append("���� ����\n");
			}
		}
	}
	
	// ���� ����
	private Socket echoSocket;	// socket reference variable
	private PrintStream socketOut;	// client �� server
	private BufferedReader socketIn;	// server �� client
	private sendMsgHandler sendMsgHandler;	// send ��ư Ȥ�� enter ������ ��� ó�� Handler reference
	private ChatReceiveThread chatTrd;	// ���۵Ǿ� �� �޽��� ó�� thread
	
	private JPanel pan, topPan, midPan;
	private JTextArea showDlgBox;	// ä�� ���� ��� â
	private JTextField sendTextBox;	// ������ �޽��� �Է� â
	private JTextField serverIp, portNo, userName;	// server IP, port ��ȣ, ��ȭ��
	private JButton connectBt, disconnectBt, sendButton;	// ����, �����ߴ�, ���� ��ư
	
	
	
	class ChatReceiveThread extends Thread {	// ���۵Ǿ� �� �޽��� ó�� Thread
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
				}	// server�� ���Ͽ� ����ϹǷ� ���� ���x
			}
			catch(Exception e) {
				showDlgBox.append("������ ������ϴ�. \n");
			}
		}
	}	//-------------------ChatReceiveThread class ��
}	//-------------------ChattingClient class ��