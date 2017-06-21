package protocol;

import utility.Strings.StringMethods;
import utility.arrays.ArrayMethods;
import utility.Exceptions.UnexistantFlagException;
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
	/*	windowSize nesta classe é o tamanho da janela do host no outro lado da Socket */
	private int lastAcked;
	private ByteBuffer byteBuffer;
	private byte[] buf;
	
	/**
	 * Cria parte remetente de datagramas de uma Socket.
	 * @param datagramSocket Socket de datagramas que enviará dados.
	 * @param bufferSize Tamanho do buffer para datagramas.
	 */
	public PSender(DatagramSocket datagramSocket, int bufferSize) {
		byteBuffer = ByteBuffer.allocate(5000);
		this.datagramSocket = datagramSocket;
		dis = new DataInputStream(dataIn);
		buf = new byte[bufferSize];
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
	 * @param buf Array de bytes com dados a serem enviados
	 * @param offset Início dos dados a serem copiados do array.
	 * @param len Quantidade de bytes a serem copiados.
	 * @return Datagrama pronto para envio.
	 */
	private DatagramPacket encapsulateDatagram(byte[] buf, int offset, int len) {
		//A completar.
	}
	
	/**
	 * Cria header para mensagem com dados especificados.
	 * @param seqnum Número de sequência do segmento.
	 * @param acknum Número confirmado.
	 * @param flagSettings byte de flags.
	 * @return Mensagem com cabeçalhos (segmento) em forma de array de bytes.
	 */
	private byte[] makeHeader(int numberByteAmount, int seqnum, int acknum, int windowSize, byte flagSettings) {
		return ArrayMethods.concatenateByteArrays(
				ArrayMethods.concatenateByteArrays(
						StringMethods.zeroPrefixTo(Integer.toString(seqnum), numberByteAmount).getBytes(),
						StringMethods.zeroPrefixTo(Integer.toString(acknum), numberByteAmount).getBytes()),
				ArrayMethods.concatenateByteArrays(
						StringMethods.zeroPrefixTo(Integer.toString(windowSize), numberByteAmount).getBytes(),
						flagSettings)
				);
	}
	
	private byte makeFlagByte(String[] flagSettings) throws UnexistantFlagException {
		byte flags = 0;
		for (int i = 0;i < flagSettings.length;i++) {
			if (flagSettings[i].equals("ACK")) {
				flags |= 1;
			} else if (flagSettings[i].equals("SYN")) {
				flags |= 1 << 1;
			} else if (flagSettings[i].equals("SYNACK")) {
				flags |= 1 << 2;
			} else if (flagSettings[i].equals("FIN")) {
				flags |= 1 << 3;
			} else {
				throw new UnexistantFlagException();
			}
		}
		
		return flags;
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