package protocol;

import utility.arrays.*;
import java.time.LocalTime;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;
import java.util.Vector;
import java.io.IOException;

/**
 * 	PacketReceiver, implementa parte receptora de datagramas da Socket.	
 * @author Gabriel Barbosa
 * */

public class PReceiver {
	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacket;
	private PSender senderSide;
	private PipedOutputStream dataOut;
	private Vector<LocalTime> sentTimes;
	private Vector<Integer> expectedAcks;
	private int theirWindowSize, ourWindowSize, lastReceived, lastAcked, threshold;
	
	/**
	 * Cria parte receptora de datagramas de uma Socket.
	 * @param dataReader PipedInputStream da thread que receberÃ¡ os dados da Socket.
	 * @param datagramSocket Socket de datagramas que receberÃ¡ dados.
	 * @param datagramPacket DatagramPacket com buffer para receber datagramas.
	 */
	public PReceiver(PipedInputStream dataReader, DatagramSocket datagramSocket, DatagramPacket datagramPacket) {
		lastReceived = -1;
		lastAcked = -1;
		ourWindowSize = 1;
		threshold = 6;
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
	
	/**
	 * @return Último byte recebido.
	 */
	public int getLastReceived() {
		return lastReceived;
	}
	
	/**
	 * @return Último byte ackeado.
	 */
	public int getLastAcked() {
		return lastAcked;
	}
	
	/**
	 * @return Tamanho da janela deste lado da conexão.
	 */
	public int getOurWindowSize() {
		return ourWindowSize;
	}
	
	/**
	 * Seta tamanho da janela de congestionamento.
	 * @param newSize
	 */
	public void setOurWindowSize(int newSize) {
		ourWindowSize = newSize;
	}
	
	/** 
	 * Determina novo limite inferior para janela de congestionamento.
	 * @param newThreshold
	 */
	public void setThreshold(int newThreshold) {
		threshold = newThreshold;
	}

	/**
	 * @return Tamanho do limite inferior da janela de congestionamento.
	 */
	public int getThreshold() {
		return threshold;
	}
	
	/**
	 * Adiciona momento de envio de um pacote pelo lado remetente da aplicação para cálculo de RTT.
	 * @param time Momento do envio de pacote.
	 */
	public void addSentTime(LocalTime time, int expectedAcknum) {
		sentTimes.add(time);
		expectedAcks.add(expectedAcknum);
	}
	
	public void estimateNewRTT(int acknum) {
		int position = expectedAcks.indexOf(acknum);
		LocalTime temp = sentTimes.elementAt(position);
	}

	/**
	 * @return Tamanho da janela do destinatário.
	 */
	public int getTheirWindowSize() {
		return theirWindowSize;
	}

	/**
	 * Recebe segmento e transorma em mensagem.
	 * @param datagram Segmento a ser desencapsulado.
	 * @return Array de bytes contendo mensagem.
	 */
	private byte[] decapsulateDatagram(DatagramPacket datagram) {
		byte header[] = ArrayMethods.byteArraySubset(datagram.getData(), 0, 37);
		byte message[] = ArrayMethods.byteArraySubset(datagram.getData(), 37, datagram.getData().length - 37);
		
		return message;
	}
	
	/**
	 * Mï¿½todo para julgar segmentos e decidir como proceder.
	 * @param header header a ser julgado.
	 * @return Integer representando possibilidade de resposta.
	 */
	private int judgeDatagram(byte[] header) {
		return 0;
	}
}