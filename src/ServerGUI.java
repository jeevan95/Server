

import javax.swing.*;

import com.example.jeevan.myapplication.Message;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;



public class ServerGUI extends JFrame implements ActionListener, WindowListener{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Launch the application.
	 */
	Boolean isOnline;
	ServerSocket svSock;
	ArrayList<Client> clients = new ArrayList<Client>(); 
	JTextArea txtLog;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerGUI server = new ServerGUI();
					server.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ServerGUI() {
		initialize();
		new ServerRunning().start();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	class ServerRunning extends Thread {
		public void run() {
			startServer();

		}
	}

	private void initialize() {
		setBounds(100, 100, 575, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);

		txtLog = new JTextArea();
		txtLog.setBounds(10, 63, 539, 330);
		getContentPane().add(txtLog);
		txtLog.setEditable(false);
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub

		try {
			svSock.close();
			appendLog("Server stopped");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		try {
			svSock.close();
			appendLog("Server stopped");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}
	void appendLog(String lg) {
		txtLog.append(lg + '\n');
	}
	public void startServer(){

		isOnline = true;

		try {
			svSock = new ServerSocket(3223);
			appendLog("Server Runing");
			while (isOnline) {
				Socket sock = svSock.accept();
				appendLog("new conn");
				Client cl = new Client(sock);
				clients.add(cl);
				cl.start();
			}
			svSock.close();
		}
		catch (IOException e ){
			e.printStackTrace();
		}

	}
	void stopServer(){
		isOnline = false;
	}

	class Client extends Thread {
		Socket sock; 
		ObjectInputStream istrm;
		ObjectOutputStream ostrm;

		Client(Socket sc) throws IOException {
			sock = sc;
			ostrm = new ObjectOutputStream(sock.getOutputStream());
			istrm = new ObjectInputStream(sock.getInputStream());
		}
		public void run(){
			Message msg = receive();
			if(msg!=null){
				appendLog("Message recied = "+msg.st);
				send(new Message(1,"dddddddd"));
			}
		}
		public void stopConnection() {
			try {
				sock.close();
				ostrm.close();
				istrm.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		public Message receive(){
			try {
				return (Message) istrm.readObject();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		public void send(Message msg) {
			try {
				ostrm.writeObject(msg);
				ostrm.flush();
				ostrm.reset();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}


}
