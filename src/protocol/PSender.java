package protocol;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
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
	private PipedInputStream dataIn;
	private byte[] buf;
	
	/*	boolean determinará se thread deve continuar rodando */
	public boolean alive;
	
	/**
	 * Cria parte remetente de datagramas de uma Socket.
	 * @param datagramSocket Socket de datagrams que enviará dados.
	 * @param datagramPacket DatagramPacket com buffer para enviar datagramas.
	 */
	public PSender(DatagramSocket datagramSocket, DatagramPacket datagramPacket) {
		this.datagramSocket = datagramSocket;
		this.datagramPacket = datagramPacket;
		alive = true;
		buf = new byte[1480];
	}
	
	public void run() {
		DataInputStream receivedData = new DataInputStream(dataIn);
		
		while (alive) {
			datagramSocket.send(encapsulateDatagram(receivedData.));
		}
	}
	
	/**
	 * Recebe mensagem e transforma em segmento.
	 * @param data Mensagem(String) a ser encapsulada.
	 * @return Datagrama pronto para envio.
	 */
	public DatagramPacket encapsulateDatagram(byte[] data) {
		DatagramPacket datagramPacket = null;
		
		return datagramPacket;
	}
	
	/**
	 * Para conexão com PipedOutputStream da Thread escritora.
	 * @return PipedInputStream do PacketSender.
	 */
	public PipedInputStream getPipedInputStream() {
		return dataIn;
	}
}
