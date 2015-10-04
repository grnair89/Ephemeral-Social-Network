
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashSet;


/**
 * 
 */

/**
 * @author Ganesh Rajasekharan
 * 
 * This class receives a heartbeat messages with a serialised linkedhashset of IPs of all the active devices
 *
 */
public class MulticastReceiver extends Server_Client implements Runnable{

	final static String INET_ADDR = "224.1.2.1";
	final static int PORT = 8427;

	public static void startMulticastReception(){

		new Thread(new MulticastReceiver()).start();;

	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run() {

		System.out.println("Multicast receiver Thread has started");

		String ipRemove= "";

		try(MulticastSocket clientSocket = new MulticastSocket(PORT)) {


			DatagramPacket receivePacket;
			InetAddress address = InetAddress.getByName(INET_ADDR);
			clientSocket.joinGroup(address);
			byte[] messageBuffer = new byte[3000];
			ObjectInputStream dataInputStream;

			while (true) {


				clientSocket.setSoTimeout(8000);
				receivePacket = new DatagramPacket( messageBuffer, messageBuffer.length);
				clientSocket.receive(receivePacket);
				ByteArrayInputStream newMsgByteInputStream = new ByteArrayInputStream( messageBuffer );
				dataInputStream = new ObjectInputStream(new BufferedInputStream(newMsgByteInputStream));

				listIP = (LinkedHashSet<String>)dataInputStream.readObject();	
				ipRemove = receivePacket.getAddress().getHostAddress();
				messageBuffer = new byte[4000];

			}

		}
		//on heart beat timeout elect a new server
		catch(SocketTimeoutException e) {
			System.out.println("Heartbeat: "+e.getMessage());

			//remove the failed ip from the election list
			listIP.remove(ipRemove);
			System.out.println("Receivers IPList after removing failed IP: "+listIP.toString());


			//Handle server failure for clients
			startelection();

		}

		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	/**
	 * @throws UnknownHostException 
	 * @throws InterruptedException 
	 * 
	 */
	private static void startelection(){

		if(!listIP.isEmpty()){
			String electedServerIP, hostName="";

			ArrayList<String> list = new ArrayList<String>();
			list.clear();
			list.addAll(listIP);
			int size = list.size()-1;		

			//the last peer to have joined the network would be elected
			electedServerIP = list.get(size);

			System.out.println("Elected server is: "+electedServerIP);

			System.out.println("IPs in election list :"+ listIP.toString());
			try {
				hostName = InetAddress.getLocalHost().getHostAddress();

				if(electedServerIP.equals(hostName)){


					//start UDP server to service future clients

					System.out.println("New startIPBroadcastServer started");
					IPBroadcastServer.startIPBroadcastServer();

					//start TCP server and the MulticastBroadcast Server
					System.out.println("New startTCPServer started");
					TCPServerService.startTCPServer();

					//Thread.sleep(5000);

					sendIPtoClients(electedServerIP);

				}

				//others should call a method which will listen for a serverIP from the elected server.

				else{	

					//send a udp discovery packet to the elected servcer's ip.

					System.out.println("In startElectedUDPReceiver: Receive elected IP from server");
					startElectedUDPReceiver(electedServerIP);

				}
				System.out.println(">> New server: Sent IP to all the IPs in list");


			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			finally{
				System.out.println(">>> Exiting TCPClient..");
			}

		}
	}

	/**
	 * This method sends the elected IP to other clients
	 * 
	 * @param electedServerIP 
	 * 
	 */
	private static void sendIPtoClients(String electedServerIP) {
		
		try(DatagramSocket udpclientSocket = new DatagramSocket()){
			byte[] sendData = ("SENDING_MY_IP_"+electedServerIP).getBytes();

			//send elected server IP to all the remaining IP
			for(String destination : listIP){
				if(destination.equals(electedServerIP)){
					continue;
				}
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(destination), 8900);
				udpclientSocket.send(sendPacket);
				System.out.println(">>> Elected server info packet sent to: "+destination);
			}
		}catch(IOException e){


		}



	}

	/**
	 * This method receives a UDP packet containing the elected server's IP and starts TCP client on the IP.
	 * 
	 */
	private static void startElectedUDPReceiver(String electedServer) {

		try(DatagramSocket udpclientSocket = new DatagramSocket(8900)){

			
			byte[] recvBuf = new byte[15000];
			DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

			udpclientSocket.receive(receivePacket);

			System.out.println(">>> Response from elected server: " + receivePacket.getAddress().getHostAddress());

			//Check if the message is correct
			String message = new String(receivePacket.getData()).trim();
			if (message.contains("SENDING_MY_IP_")) {

				//store server ip for TCP clients to connect to.
				serverIP = receivePacket.getAddress().getHostAddress();

				System.out.println("New server IP is :"+serverIP);

				//spawns a TCP client thread which creates a socket to the tcp server socket and pops up a new chat window.
				TCPClient.startTCPclient();				
			}


		}catch(IOException e){


		}

	}


}
