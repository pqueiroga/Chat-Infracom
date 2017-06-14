package protocol;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;
import java.io.IOException;

/**
 * 	PacketReceiver, implementa parte receptora de datagramas da Socket.	
 * @author Gabriel Barbosa
 * */

public class PReceiver implements Runnable {
	/*	time de timeout
	 * 	DatagramSocket para receber datagramas
	 * 	PipedOutputStream para enviar dados � aplica��o */
	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacket;
	private PipedOutputStream dataOut;
	
	//boolean determinar� se thread deve continuar rodando
	public boolean alive;
	
	/**
	 * Cria parte receptora de datagramas de uma Socket.
	 * @param dataReader PipedInputStream da thread que receber� os dados da Socket.
	 * @param datagramSocket Socket de datagramas que receber� dados.
	 * @param datagramPacket DatagramPacket com buffer para receber datagramas.
	 */
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
	
	/**
	 * Recebe segmento e transorma em mensagem.
	 * @param datagram Segmento a ser desencapsulado.
	 * @return Array de bytes contendo mensagem.
	 */
	private byte[] decapsulateDatagram(DatagramPacket datagram) {
		byte message [] = null;
		
		return message;
	}
	
	/*	M�todo para julgar datagrama e decidir como proceder */
	/**
	 * M�todo para julgar segmentos e decidir como proceder.
	 * @param toJudge Segmento a ser julgado.
	 * @return Integer representando possibilidade de resposta.
	 */
	private int judgeDatagram(DatagramPacket toJudge) {
		return 0;
	}
}
