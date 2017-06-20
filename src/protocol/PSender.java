package protocol;

import utility.Strings.StringMethods;
import utility.arrays.ArrayMethods;
import java.util.Vector;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Implementa parte remetente de datagramas da Socket.
 * @author Gabriel Barbosa
 *
 */
public class PSender {
	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacket;
	private PReceiver receiverSide;
	private PipedInputStream dataIn;
	private DataInputStream dis;
	private int windowSize, lastAcked;
	private byte[] buf;
	
	/**
	 * Cria parte remetente de datagramas de uma Socket.
	 * @param datagramSocket Socket de datagramas que enviará dados.
	 * @param bufferSize Tamanho do buffer para datagramas.
	 */
	public PSender(DatagramSocket datagramSocket, int bufferSize) {
		this.datagramSocket = datagramSocket;
		dis = new DataInputStream(dataIn);
		buf = new byte[bufferSize];
		windowSize = 1;
		lastAcked = -1;
	}

	/**
	 * A classe PSender precisa de uma referência à sua parte receptora correspondente,
	 * o método setReceiver configura essa referência para o PReceiver especificado.
	 * @param receiver PReceiver a ser referenciado.
	 */
		public void setReceiver(PReceiver receiver) {
		receiverSide = receiver;
	}

	/**
	 * Recebe mensagem e transforma em segmento.
	 * @param data Mensagem(String) a ser encapsulada.
	 * @return Datagrama pronto para envio.
	 */
	private DatagramPacket encapsulateDatagram(byte[] data, int len) {
		byte array[] = new byte[len];
		int lenRead = 0;
		try {if (dis.available() > 0) lenRead = dis.read(array, 0, len);} catch (IOException ioe) {}
		
		int seqnum = receiverSide.getLastReceived();
		
		return new DatagramPacket(data, len);
	}
	
	/**
	 * Envia segmentos ao destinatário. 
	 */
	public void sendData() {

	}
	
	/**
	 * Atualiza número do último segmento recebido pelo destinatário.
	 * @param ackedNum Número do último segmento recebido pelo destinatário.
	 */
	public void updatePacketBuffer(int ackedNum) {
		lastAcked = ackedNum;
	}

	/**
	 * Para conexão com PipedOutputStream da Thread escritora.
	 * @return PipedInputStream do PacketSender.
	 */
	public PipedInputStream getPipedInputStream() {
		return dataIn;
	}
}