package protocol;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;
import java.io.IOException;

/*	PacketReceiver, implementa parte receptora de datagramas da Socket	*/

public class PReceiver {
	/*	time de timeout
	 * 	DatagramSocket para receber datagramas
	 * 	PipedOutputStream para enviar dados à aplicação */
	private int time;
	private DatagramSocket datagramSocket;
	private PipedOutputStream dataOut;
	
	//boolean determinará se thread deve continuar rodando
	public boolean alive;
	
	public PReceiver(int port, int time, PipedInputStream dataReader) {
		this.time = time;
		alive = true;
		datagramSocket = null;
		dataOut = null;
		try {
			dataOut = new PipedOutputStream(dataReader);
			datagramSocket = new DatagramSocket(port);
			datagramSocket.setSoTimeout(time);
		} catch (SocketException se) {

		} catch (IOException ioe) {}
	}
	
	private String decapsulateDatagram(DatagramPacket datagram) {
		String message = null;
		
		return message;
	}
	
	/*	Método para julgar datagrama e decidir como proceder */
	
	private int judgeDatagram(DatagramPacket toJudge) {
		return 0;
	}
	
	private void getMessage() {
		DatagramPacket temp = null;
		try {
			datagramSocket.receive(temp);
		} catch (IOException ioe) {}
		
	}
}
