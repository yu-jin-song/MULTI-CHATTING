// Swing ��� Chatting ����
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;

public class ChattingClient extends JFrame {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ChattingClient("Chatting Client").setVisible(true);
	}
	
	ChattingClient() {	}
	ChattingClient(final String str) {
		super(str);
		initForm();
	}
	
	void initForm() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		pan4 = new JPanel();
		pan1 = new JPanel();
		pan11 = new JPanel();
		pan12 = new JPanel();
		
		serverIp = new JTextField("localhost", 10);
		portNo = new JTextField("1234", 10);
		userName = new JTextField("NoName",10);
		sendTextBox = new JTextField(30);
		
		connectBt = new JButton("Connect");
		disconnectBt = new JButton("Disconnect");
		sendButton = new JButton("Send");
		
		connectorListBox = new JTextArea(10, 10);
		showDlgBox = new JTextArea(20, 40);
		JScrollPane scrollPane11 = new JScrollPane(showDlgBox);
		
		pan4.setLayout(new BorderLayout());
		pan4.add("Center", scrollPane11);
		pan4.add("South", sendTextBox);
		
		pan1.setLayout(new BorderLayout());
		
		pan11.setLayout(new GridLayout(4, 2, 0, 10));
		pan11.add(new JLabel("Server Ip"));
		pan11.add(serverIp);
		pan11.add(new JLabel("Port No"));
		pan11.add(portNo);
		pan11.add(new JLabel("Name"));
		pan11.add(userName);
		pan11.add(connectBt);
		pan11.add(disconnectBt);
		
		pan12.setLayout(new BorderLayout());
		pan12.add("North", new JLabel("������"));
		pan12.add("Center", connectorListBox);
		
		pan1.add("North", pan11);
		pan1.add("Center", pan12);
		pan1.add("South", sendButton);
		
		showDlgBox.setEditable(false);
		connectorListBox.setEditable(false);
		connectBt.setEnabled(true);
		disconnectBt.setEnabled(false);
		sendButton.setEnabled(false);
		
		// main ȭ�鿡 �߰�
		getContentPane().add("Center", pan4);
		getContentPane().add("East", pan1);
		pack();
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
				
				connectorListBox.append(userName.getText() + "\n");
				connectorListBox.append("Server \n");
				
				// ���۵Ǿ�� �޽��� ó���� Thread ����, �޽��� ��� ���� �Է� ��Ʈ���� �޽��� â ����
				chatTrd = new ChatReceiveThread(socketIn, showDlgBox);
				chatTrd.start();
				
				sendTextBox.requestFocus();	// �޽��� �Է� â���� focus
				connectBt.setEnabled(false);
				disconnectBt.setEnabled(true);
				sendButton.setEnabled(true);
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
			showDlgBox.append("disconnect \n");
			
			try {
				socketOut.close();
				socketIn.close();
				echoSocket.close();
				connectBt.setEnabled(true);
				disconnectBt.setEnabled(false);
				sendButton.setEnabled(false);
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
					socketOut.println(strMsg );	// server�� ����
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
	
	private JPanel pan4;	// ���� ȭ��
	private JPanel pan1, pan11, pan12;	// ������ ȭ��
	private JTextArea showDlgBox;	// ä�� ���� ��� â
	JTextArea connectorListBox;	// ������ ��� â
	private JTextField sendTextBox;	// ������ �޽��� �Է� â
	private JTextField serverIp, portNo, userName;	// server IP, port ��ȣ, ��ȭ��
	private JButton connectBt, disconnectBt, sendButton;	// ����, �����ߴ�, ���� ��ư

}	//-------------------ChattingClient class ��


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
				showDlgBox.append(strSocket + "\n");
				showDlgBox.setCaretPosition(showDlgBox.getDocument().getLength());
			}	// server�� ���Ͽ� ����ϹǷ� ���� ���x
		}
		catch(Exception e) {
			showDlgBox.append("������ ������ϴ�.");
		}
	}
}	//-------------------ChatReceiveThread class ��