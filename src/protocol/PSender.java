package protocol;

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
public class PSender implements Runnable {
	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacket;
	private Vector<DatagramPacket> packetBuffer;
	private PipedInputStream dataIn;
	private DataInputStream dis;
	private int windowSize, lastAcked;
	private byte[] buf;
	
	/**
	 * Cria parte remetente de datagramas de uma Socket.
	 * @param datagramSocket Socket de datagramas que enviar� dados.
	 * @param bufferSize Tamanho do buffer para datagramas.
	 */
	public PSender(DatagramSocket datagramSocket, int bufferSize) {
		this.datagramSocket = datagramSocket;
		packetBuffer = new Vector<DatagramPacket>();
		dis = new DataInputStream(dataIn);
		buf = new byte[bufferSize];
		windowSize = 1;
		lastAcked = -1;
	}
	
	public void run() {
		int len;
		while (true) {
			len = -1;
			try {len = dis.read(buf);} catch (IOException ioe) {}
			if (len != -1) {
				packetBuffer.addElement(encapsulateDatagram(buf, len));
			}
		}
	}
	
	/**
	 * Recebe mensagem e transforma em segmento.
	 * @param data Mensagem(String) a ser encapsulada.
	 * @return Datagrama pronto para envio.
	 */
	private DatagramPacket encapsulateDatagram(byte[] data, int len) {
		return new DatagramPacket(data, len);
	}
	
	/**
	 * Envia segmentos ao destinat�rio. 
	 */
	public void sendData() {
		for (int i = 0; i < windowSize;i++) {
			try {datagramSocket.send(packetBuffer.elementAt(lastAcked + i + 1));} catch (IOException ioe) {}
		}
	}
	
	/**
	 * Atualiza n�mero do �ltimo segmento recebido pelo destinat�rio.
	 * @param ackedNum N�mero do �ltimo segmento recebido pelo destinat�rio.
	 */
	public void updatePacketBuffer(int ackedNum) {
		for (int i = 0;i < ackedNum - lastAcked;i++) {
			packetBuffer.remove(0);
		}
		lastAcked = ackedNum;
	}

	/**
	 * Para conex�o com PipedOutputStream da Thread escritora.
	 * @return PipedInputStream do PacketSender.
	 */
	public PipedInputStream getPipedInputStream() {
		return dataIn;
	}
}
