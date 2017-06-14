package protocol;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PSender implements Runnable {
	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacket;
	private PipedInputStream dataIn;
	
	public PSender(DatagramSocket datagramSocket, DatagramPacket datagramPacket) {
		this.datagramSocket = datagramSocket;
		this.datagramPacket = datagramPacket;
	}
	
	public void run() {
		
	}
	
	public DatagramPacket encapsulateDatagram(String data) {
		DatagramPacket datagramPacket = null;
		
		byte dataByteArray[] = data.getBytes();
		
		return datagramPacket;
	}
}
