import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
/**
 * 
 */

/**
 * @author Ganesh
 * 
 * This class receives broadcasts packets from the application on start and sends a message to add new peers to the network
 *
 */
public class IPBroadcastServer extends Server_Client implements Runnable {

	public static DatagramSocket socket;

	public static void startIPBroadcastServer(){

		Thread serverthread = new Thread(new IPBroadcastServer());
		serverthread.start();

	}


	@Override
	public void run() {
		try {
			//Listen to incoming UDP packets.
			socket = new DatagramSocket(PORT_NO, InetAddress.getByName("0.0.0.0"));
			socket.setBroadcast(true);

			while (true) {
				System.out.println("<<< IPBroadcastServer >>> Ready to receive broadcast packets!");

				//Receive a packet
				byte[] recvBuf = new byte[15000];
				DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
				socket.receive(packet);

				//Packet received
				System.out.println(">>>Received packet from client: " + packet.getAddress().getHostAddress());
				System.out.println(getClass().getName() + ">>>Packet received data: " + new String(packet.getData()));
				String IPsender = "";
				IPsender = packet.getAddress().getHostAddress();

				listIP.add(IPsender);


				//check the agreed handshake content of the message
				String message = new String(packet.getData()).trim();
				if (message.equals("SERVER_SEARCH_CODE_K213JKH12UJGH1")) {
					byte[] sendData ="REPLY_CODE_94HDKD83BSKD9365JSDMN0".getBytes();

					//Send a response
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
					socket.send(sendPacket);

					System.out.println(">>>Packet sent to: " + sendPacket.getAddress().getHostAddress());
				}
			}


		} catch (IOException ex) {
		}
	}



}
