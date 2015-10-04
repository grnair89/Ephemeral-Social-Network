import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * This class is the entry point of the application. After starting the program can enter either server or the client mode.
 * 
 * @author Ganesh Rajasekharan
 *
 */
public class Server_Client {


	public static int PORT_NO = 8883;

	public static List<String> clntmessageList;

	public static LinkedHashSet<String> listIP; 

	private static Server_Client instance;

	public DatagramSocket  udpclientSocket;

	public Socket  tcpClientSocket;

	public Socket tcpServerSocket;

	public DatagramSocket udpServerSocket;

	public static String serverIP;

	public static Server_Client createInstance() {

		if (instance == null) {
			instance = new Server_Client();
		}
		return instance;

	}
	
	//main method no arguments expected
	public static void main(String args[]){

		instance = createInstance();
		instance.createUDPClient();
	}


	/**
	 * Creates a method which sends a discovery packet to the default gateway IP of the network for server discovery.
	 */
	protected  void createUDPClient() {	
		try {
			//store all the connected peers IPs
			listIP = new LinkedHashSet<String>();
			
			//store the messages to be transferred to other peers.
			clntmessageList = new ArrayList<String>();

			udpclientSocket = new DatagramSocket();
			udpclientSocket.setBroadcast(true);
			byte[] sendData = "SERVER_SEARCH_CODE_K213JKH12UJGH1".getBytes();

			try {
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), PORT_NO);
				udpclientSocket.send(sendPacket);
				System.out.println(">>> Sent a broadcast packet to: 255.255.255.255");
			} catch (Exception e) {
			}


			//Wait for a response
			byte[] receiveBuffer = new byte[15000];
			DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

			udpclientSocket.setSoTimeout(5000);
			udpclientSocket.receive(receivePacket);

			//We have a response
			System.out.println(">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

			//verify if the actual server sent the packet
			String message = new String(receivePacket.getData()).trim();
			if (message.equals("REPLY_CODE_94HDKD83BSKD9365JSDMN0")) {

				serverIP = receivePacket.getAddress().getHostName();
				
				System.out.println("Started in client mode");
				//start client service
				startTCPClientService();
				//start the heartbeat messaging
				MulticastReceiver.startMulticastReception();
				
			}


		} catch (SocketTimeoutException  ex) {

			udpclientSocket.close();
			System.out.println(">>> No active servers found: Switching to server mode.");
			
			//add its own IP to the IP list for failure handling
			String selfIP;
			try {
				selfIP = InetAddress.getLocalHost().getHostAddress();
				if(!listIP.contains(selfIP)){
				    listIP.add(selfIP);		
					}
			} catch (UnknownHostException e) {
				
				e.printStackTrace();
			}			
			//start UDP thread broadcast servers
			IPBroadcastServer.startIPBroadcastServer();
			
			//start Multicast UDP server thread for sending heartbeat messages.
			MultiCastBroadcaster.startMulticastBroadcast();
			
			//start TCP server thread for sending messages.
			TCPServerService.startTCPServer();
			

		}
		catch(ConnectException ce){

		}
		catch (IOException ex) {
			if(udpclientSocket!=null){
			udpclientSocket.close();}	
			//IPBroadcastServer.startIPBroadcastServer();
		}

		finally{
			//after determining the server mode or client mode close UDP client socket.
			if(udpclientSocket!=null)
				udpclientSocket.close();
		}
	}

	/**
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * 
	 */
	private void startTCPClientService() throws IOException {
		
	TCPClient.startTCPClientThread();
	
	}
}


