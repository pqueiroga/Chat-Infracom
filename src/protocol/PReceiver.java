package protocol;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;
import java.io.IOException;

/*	PacketReceiver, implementa parte receptora de datagramas da Socket	*/

public class PReceiver implements Runnable {
	/*	time de timeout
	 * 	DatagramSocket para receber datagramas
	 * 	PipedOutputStream para enviar dados � aplica��o */
	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacket;
	private PipedOutputStream dataOut;
	private byte buf[];
	
	//boolean determinar� se thread deve continuar rodando
	public boolean alive;
	
	public PReceiver(PipedInputStream dataReader, DatagramSocket datagramSocket, DatagramPacket datagramPacket) {
		alive = true;
		dataOut = null;
		this.datagramPacket = datagramPacket;
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
			// 0 - Datagrama OK, entregar mensagem � aplica��o
			case (0):
				try {
					dataOut.write(decapsulateDatagram(datagramPacket));
				} catch (IOException ioe) {}
			break;

			default:
			break;
		}
	}
	
	/*	M�todo para extrair mensagem de segmento */
	private byte[] decapsulateDatagram(DatagramPacket datagram) {
		byte message [] = null;
		
		return message;
	}
	
	/*	M�todo para julgar datagrama e decidir como proceder */
	private int judgeDatagram(DatagramPacket toJudge) {
		return 0;
	}
}
