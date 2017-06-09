package protocol;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.io.IOException;

public class PReceiver {
	private int time;
	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacket;
	
	//boolean determinará se thread deve continuar rodando
	public boolean alive;
	
	public PReceiver(int port, int time) {
		this.time = time;
		alive = true;
		datagramSocket = null;
		try {
			datagramSocket = new DatagramSocket(port);
		} catch (SocketException se) {}
	}
	
	String getMessage() {
		return message;
	}
}
