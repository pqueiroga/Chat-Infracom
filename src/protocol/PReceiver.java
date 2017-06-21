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
	 * 	PipedOutputStream para enviar dados Ã  aplicaÃ§Ã£o */
	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacket;
	private PSender senderSide;
	private PipedOutputStream dataOut;
	/*	theirWindowSize é o tamanho da janela do destinatário
	 * ourWindowSize é o tamanho da janela deste lado
	 * sentUnacked é o número de segmentos enviados que ainda não foram recebidos
	 */
	private int theirWindowSize, ourWindowSize, sentUnacked, lastReceived;
	
	/**
	 * Cria parte receptora de datagramas de uma Socket.
	 * @param dataReader PipedInputStream da thread que receberÃ¡ os dados da Socket.
	 * @param datagramSocket Socket de datagramas que receberÃ¡ dados.
	 * @param datagramPacket DatagramPacket com buffer para receber datagramas.
	 */
	public PReceiver(PipedInputStream dataReader, DatagramSocket datagramSocket, DatagramPacket datagramPacket) {
		lastReceived = -1;
		dataOut = null;
		this.datagramPacket = datagramPacket;
		this.datagramSocket = datagramSocket;
		try {
			dataOut = new PipedOutputStream(dataReader);
		} catch (IOException ioe) {}
	}
	
	/**
	 * PReceiver deve possuir referência a lado enviador, setSender configura essa referência para
	 * o PSender especificado.
	 * @param sender Referência a PSender.
	 */
	public void setSender(PSender sender) {
		senderSide = sender;
	}
	
	public int getLastReceived() {
		return lastReceived;
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
	
	/*	Mï¿½todo para julgar datagrama e decidir como proceder */
	/**
	 * Mï¿½todo para julgar segmentos e decidir como proceder.
	 * @param toJudge Segmento a ser julgado.
	 * @return Integer representando possibilidade de resposta.
	 */
	private int judgeDatagram(DatagramPacket toJudge) {
		return 0;
	}
}