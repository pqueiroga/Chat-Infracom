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

public class PReceiver {
	/*	time de timeout
	 * 	DatagramSocket para receber datagramas
	 * 	PipedOutputStream para enviar dados à aplicação */
	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacket;
	private PipedOutputStream dataOut;
	
	/**
	 * Cria parte receptora de datagramas de uma Socket.
	 * @param dataReader PipedInputStream da thread que receberá os dados da Socket.
	 * @param datagramSocket Socket de datagramas que receberá dados.
	 * @param datagramPacket DatagramPacket com buffer para receber datagramas.
	 */
	public PReceiver(PipedInputStream dataReader, DatagramSocket datagramSocket, DatagramPacket datagramPacket) {
		dataOut = null;
		this.datagramPacket = datagramPacket;
		this.datagramSocket = datagramSocket;
		try {
			dataOut = new PipedOutputStream(dataReader);
		} catch (IOException ioe) {}

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
	
	/*	Método para julgar datagrama e decidir como proceder */
	/**
	 * Método para julgar segmentos e decidir como proceder.
	 * @param toJudge Segmento a ser julgado.
	 * @return Integer representando possibilidade de resposta.
	 */
	private int judgeDatagram(DatagramPacket toJudge) {
		return 0;
	}
}
