
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * @author Ganesh Rajasekharan
 * 
 * This class starts a TCP client which exchanges messages with the TCP server 
 *
 */
public class TCPClient extends Server_Client implements Runnable {

	/**
	 * 
	 */

	static BufferedReader in;
	static PrintWriter out;
	static JFrame frame = new JFrame("Ask Something(Client)");
	static JTextField textField = new JTextField(40);
	static JTextArea messageArea = new JTextArea(25, 40);

	public static int PORT_NO = 9005;

	public static String hostName = "";
	public static ArrayList<String> clntmessageList;
	public static HashSet<String> ipSet;

	public static Server_Client instance;

	public static void startTCPClientThread(){
		new Thread(new TCPClient()).start();

	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		try {
			startTCPclient();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public static void initialiseClient(){

		try {
			clntmessageList= new ArrayList<String>();
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Layout GUI
		textField.setEditable(false);
		messageArea.setEditable(false);
		frame.getContentPane().add(textField, "South");
		frame.getContentPane().add(new JScrollPane(messageArea), "Center");
		frame.pack();
		textField.setEditable(true);

		// Add Listeners
		textField.addActionListener(new ActionListener() {
			/**
			 * 
			 */
			public void actionPerformed(ActionEvent e) {
				String s,m = "";
				s=textField.getText();
				if(!"".equals(s)){
					m = hostName+": "+s;
					out.println(m);
					clntmessageList.add(m);
					messageArea.append(m+"\n");
					textField.setText("");
				}
			}
		});


	}


	public static void startTCPclient() throws IOException{
	
		initialiseClient();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		Socket tcpClientSocket = null;

		// create socket connections and initialise streams

		try{

			tcpClientSocket = new Socket(serverIP, PORT_NO);


			in = new BufferedReader(new InputStreamReader(
					tcpClientSocket.getInputStream()));
			out = new PrintWriter(tcpClientSocket.getOutputStream(), true);



			String chatPast,ipList = "";

			chatPast = in.readLine();

			if(!"".equals(chatPast)){


				if(chatPast.startsWith("HISTORY")){

					chatPast = chatPast.substring(7);
					String[] chats = chatPast.split("~");

					//System.out.println("Chats [] values after splitting: ");
					for(String s :chats){

						if(!"".equals(s)){
							System.out.println("value : "+s);

							String m=new String(s);
							messageArea.append(m+"\n");						

						}


					}

				}

			}
			
			// Process all messages from server, according to the protocol.
			while (true) {
				if(in!=null){
					String line = in.readLine();
					if(line == null){ return;}

					if(line.contains("IPDATA")){


						ipList = line.substring(6);
						String[] ips = ipList.split("~");

						System.out.println("Ips [] values after splitting: ");
						
						
						for(String s :ips){

							if(!"".equals(s)){

								listIP.add(s);
								System.out.println("value : "+s);

								String m=new String(s);


								messageArea.append(m+"\n");		
							}
						}
					}
					
					System.out.println("Clients IP copy :"+listIP.toString());


					if (line.contains("MESSAGE")) {
						clntmessageList.add(line.substring(8));
						messageArea.append(line.substring(8) + "\n");
					}
					line = "";
				}
			}

		}catch (UnknownHostException e) {
			//e.printStackTrace();

		} catch(ConnectException ce){
			
			//ce.printStackTrace();

		} catch (SocketException se) {
			
			//se.printStackTrace();

		} catch (IOException e) {
			
			e.printStackTrace();

		}
		finally{
			
			frame.dispose();
			if(tcpClientSocket!=null){
				tcpClientSocket.close();}


		}



	}

}