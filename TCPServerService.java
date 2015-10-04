import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * @author Ganesh Rajasekharan
 * 
 * This class starts a threads to accept incoming socket connection to the serversocket
 */
public class TCPServerService extends Server_Client implements Runnable{

	public static JFrame frame;
	public static JTextField textField;
	public static JTextArea messageArea;

	public static ServerSocket serverSocket;

	private BufferedReader in;
	private PrintWriter out;
	public static int PORT_NO = 9005;

	private static HashSet<PrintWriter> printWriters = new HashSet<PrintWriter>();

	public static String serverName ="";

	public static ArrayList<String> messageList;

	public static void startTCPServer() {

		try {
			messageList = new ArrayList<String>();
			serverName = InetAddress.getLocalHost().getHostName();

			initialiseFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			System.out.println("<<< TCPServer is running: IP"+InetAddress.getLocalHost().getHostAddress()+" >>>");

			new Thread(new TCPServerService()).start();

			//serverSocket.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				if(serverSocket!=null){
					serverSocket.close();}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//}


	}

	/**
	 * 
	 */
	private static void initialiseFrame() {

		frame = new JFrame("Ask something!");
		textField = new JTextField(40);
		messageArea = new JTextArea(25, 40);

		textField.setEditable(false);
		messageArea.setEditable(false);
		frame.getContentPane().add(textField, "South");
		frame.getContentPane().add(new JScrollPane(messageArea), "Center");
		frame.pack();
		frame.setVisible(true);
		textField.setEditable(true);

		textField.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String input = "";

				input = textField.getText();


				if(!"".equals(input)){

					messageList.add(serverName+": "+input);	

					for (PrintWriter writer : printWriters) {
						writer.println("MESSAGE "+serverName+": "+input);
					}
					messageArea.append(serverName+": "+input+"\n");
					textField.setText("");
				}

			}
		});
	}

	public void run(){

		ServerSocket socket = null;
		Socket recvSocket = null;

		try {
			socket = new ServerSocket(PORT_NO);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
		while(true){
			try {

				recvSocket = socket.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			(new Thread( this.new HandleClient(recvSocket))).start();

		}
	}
	//handler class to create threads to service each client connection
	class HandleClient implements Runnable{
		Socket clientSocket;
		HandleClient(Socket recvSocket){
			this.clientSocket = recvSocket;
		}
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {

			try{
				String chatHistory = "HISTORY";	
				String ipdata =  "IPDATA";

				String extractIP = "";
				extractIP = clientSocket.getLocalSocketAddress().toString();
				for(int i=0;i<extractIP.length();i++){
					if(extractIP.charAt(i)==':'){
						extractIP = extractIP.substring(1, i);
					}

				}
				System.out.println(">>> Conencted to a client: "+extractIP);

				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				out = new PrintWriter(clientSocket.getOutputStream(), true);


				printWriters.add(out);

				if(!messageList.isEmpty()){

					for(String s:messageList){
						chatHistory  += s+"~";					
					}
				}
				out.println(chatHistory);

				if(!listIP.isEmpty()){

					for(String s1:listIP){
						ipdata  += s1+"~";					
					}				
				}

				System.out.println("chatHistory string: "+chatHistory);
				System.out.println("ipdata string: "+ipdata);

				//protocol for sending bytes through the socket streams to the clients. 
				//get printwriters to all the clients except the one which sent it and write the messages.
				while (true) {

					String inputText = "";
					inputText = textField.getText();

					if(!"".equals(inputText)){
						messageList.add(serverName+": "+inputText);	

						for (PrintWriter writer : printWriters) {
							writer.println("MESSAGE "+serverName+": "+inputText);
						}
						messageArea.append(serverName+": "+inputText+"\n");
					}

					String input = in.readLine();
					if (input == null) {
						return;
					}

					for (PrintWriter writer : printWriters) {
						if(writer.equals(out)){
							continue;
						}

						System.out.println("IPDATA is :"+ipdata);
						writer.println("MESSAGE " +input);

					}
					messageList.add(input);					
					messageArea.append(input+"\n");
				}
			}catch(IOException e){

			}
			finally{
				System.out.println("Messages List: "+messageList.toString());
				System.out.println("IPs collected: "+listIP.toString());
				frame.dispose();
				if (out != null) {
					printWriters.remove(out);
				}

				try {
					clientSocket.close();
					printWriters.remove(out);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
	}
}

