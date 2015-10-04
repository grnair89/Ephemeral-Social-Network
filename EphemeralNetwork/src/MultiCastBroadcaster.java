
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;


/**
 * 
 */

/**
 * @author Ganesh Rajasekharan
 * 
 * This class multicasts heartbeat messgaes to all the members of the multicast group
 *
 */
public class MultiCastBroadcaster extends Server_Client implements Runnable {

	public MulticastSocket socket;

	final static String INET_ADDR = "224.1.2.1";
	
	public HashMap<String, String> mapIP;

	public static void startMulticastBroadcast(){
		
		System.out.println("Multicast sender Thread has started");

		new Thread(new MultiCastBroadcaster()).start();


	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub

		try {
	
		while(true){									
			socket = new MulticastSocket();

			InetAddress address = InetAddress.getByName(INET_ADDR);
			
			//System.out.println("Hearbeat message sent to: "+address.getHostAddress());
			ByteArrayOutputStream byteopstream = new ByteArrayOutputStream(3000);
			ObjectOutputStream dataopStream = new ObjectOutputStream(new BufferedOutputStream(byteopstream));
			dataopStream.flush();
			dataopStream.writeObject(listIP);
			dataopStream.flush();
			
			byte[] byteBuffer = byteopstream.toByteArray();
			DatagramPacket sendPacket = new DatagramPacket(byteBuffer, byteBuffer.length,address,8427);
			socket.send(sendPacket);
			dataopStream.close();
			byteopstream.close();
			socket.close();
			
			
			Thread.sleep(3000);

			
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}





}
