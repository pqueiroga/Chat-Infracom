package protocol;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;
import java.io.IOException;

/*	PacketReceiver, implementa parte receptora de datagramas da Socket	*/

public class PReceiver implements Runnable {
	/*	time de timeout
	 * 	DatagramSocket para receber datagramas
	 * 	PipedOutputStream para enviar dados à aplicação */
	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacket;
	private PipedOutputStream dataOut;
	private byte buf[];
	
	//boolean determinará se thread deve continuar rodando
	public boolean alive;
	
	public PReceiver(int port, PipedInputStream dataReader, DatagramSocket datagramSocket) {
		alive = true;
		dataOut = null;
		buf = new byte[1500];
		datagramPacket = new DatagramPacket(buf, buf.length);
		this.datagramSocket = datagramSocket;
		try {
			dataOut = new PipedOutputStream(dataReader);
		} catch (IOException ioe) {}

	}
	
	public void run() {
		try {
			datagramSocket.receive(datagramPacket);
		} catch (IOException ioe) {}
		
		switch (judgeDatagram(datagramPacket)) {
			// 0 - Datagrama OK, entregar mensagem à aplicação
			case (0):
				try {
					dataOut.write(decapsulateDatagram(datagramPacket));
				} catch (IOException ioe) {}
			break;

			default:
			break;
		}
	}
	
	/*	Método para extrair mensagem de segmento */
	private byte[] decapsulateDatagram(DatagramPacket datagram) {
		byte message [] = null;
		
		return message;
	}
	
	/*	Método para julgar datagrama e decidir como proceder */
	private int judgeDatagram(DatagramPacket toJudge) {
		return 0;
	}
}
