package protocol;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.io.ByteArrayInputStream;
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
			datagramSocket.setSoTimeout(time);
		} catch (SocketException se) {}
	}
	
	private String decapsulateDatagram(String datagram) {
		String message = null;
		
		return message;
	}
	
	String getMessage() {
		String message = null;
		
		datagramSocket.receive(datagramPacket);
		
		message = datagramPacket.getData();
		
		
		return message;
	}
}
