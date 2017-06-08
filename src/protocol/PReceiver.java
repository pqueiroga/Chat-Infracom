package protocol;

import java.net.DatagramSocket;
import java.net.SocketException;

public class PReceiver implements Runnable {
	private DatagramSocket datagramSocket;
	
	public PReceiver(int port) {
		try {
			datagramSocket = new DatagramSocket(port);
		} catch (SocketException se) {
			
		}
	}
	
	public void run() {
		
	}
}
