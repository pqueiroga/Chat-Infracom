package protocol;

import utility.Strings.StringMethods;
import utility.arrays.ArrayMethods;
import utility.Exceptions.UnexistantFlagException;
import java.util.Timer;
import java.util.TimerTask;
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
	private int lastReadFromStream, lastSent, sentUnacked, byteBufferSeqnum;
	private ByteBuffer byteBuffer;
	private byte[] buf;
	private boolean alive;
	private TimerTask timerTask;
	private Timer timer;
	
	/*
	public void run() {
		while (alive) {
			int leftToRead = 0;
			
			try {leftToRead = dis.available();} catch (IOException ioe) {}

			if ((leftToRead > 0) && (byteBuffer.remaining() > 0) &&) {
				
			}
		}
	}
	*/

	/**
	 * Cria parte remetente de datagramas de uma Socket.
	 * @param datagramSocket Socket de datagramas que enviará dados.
	 * @param bufferSize Tamanho do buffer para datagramas.
	 */
	public PSender(DatagramSocket datagramSocket, int bufferSize) {
		byteBuffer = ByteBuffer.allocate(8*bufferSize);
		this.datagramSocket = datagramSocket;
		dis = new DataInputStream(dataIn);
		buf = new byte[bufferSize];
		lastReadFromStream = -1;
		byteBufferSeqnum = 0;
		sentUnacked = 0;
		lastSent = -1;
		alive = true;
		timerTask = new TimerTask() { 
			public void run() {
				if (alive) {
					int leftToRead = 0;
					
					try {leftToRead = dis.available();} catch (IOException ioe) {}
					
					if ((leftToRead > 0) && (byteBuffer.remaining() > 0) && (sentUnacked <= receiverSide.getTheirWindowSize())) {
						dis.read()
					}
				}
			}
		};
		timer = new Timer();
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
	 * Transforma mensagem em segmento.
	 * @param buf Array de bytes com dados a serem enviados
	 * @param offset Início dos dados a serem copiados do array.
	 * @param len Quantidade de bytes a serem copiados.
	 * @return Datagrama pronto para envio.
	 */
	private DatagramPacket encapsulateDatagram(byte[] buf, int offset, int len) {
		String flags[] = {"ACK"};
		
		byte data[] = null;

		try {
			data =
				ArrayMethods.concatenateByteArrays(
						makeHeader(12, lastSent + 1, receiverSide.getLastReceived() + 1,
								receiverSide.getOurWindowSize(), makeFlagByte(flags)),
						ArrayMethods.byteArraySubset(buf, offset, len));
		} catch (UnexistantFlagException ufe) {}
		
		return new DatagramPacket(data, 0, data.length,
				datagramSocket.getInetAddress(), datagramSocket.getLocalPort());
	}

	/**
	 * Transforma mensagem em segmento.
	 * @param buf ByteBuffer que guarda as informações a serem encapsuladas.
	 * @param len Quanta informação do ByteBuffer encapsular.
	 * @return DatagramPacket com informações encapsuladas.
	 */
	private DatagramPacket encapsulateDatagram(ByteBuffer buf, int len) {
		int realLen = (buf.remaining() > len) ? len : buf.remaining();

		byte data[] = null, tempByteBuf[] = new byte[realLen];

		int seqnum = receiverSide.getLastAcked() + buf.position();
		
		buf.get(tempByteBuf);

		data =
				ArrayMethods.concatenateByteArrays(
						makeHeader(12, seqnum, 0, receiverSide.getOurWindowSize(), (byte) 0), 
						tempByteBuf);
		
		return new DatagramPacket(data, 0, data.length, datagramSocket.getInetAddress(),
				datagramSocket.getLocalPort());
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
	
	/**
	 * Cria byte que representa flags setadas do cabeçalho.
	 * @param flagSettings Array de Strings, cada uma deve conter o nome da flag a ser setada.
	 * @return byte representante das flags setadas do cabeçalho.
	 * @throws UnexistantFlagException Caso uma flag especificada em flagSettings não exista.
	 */
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
	 * Para conexão com PipedOutputStream da Thread escritora.
	 * @return PipedInputStream do PacketSender.
	 */
	public PipedInputStream getPipedInputStream() {
		return dataIn;
	}
	
	/**
	 * Para execução da thread
	 */
	public void kill() {
		alive = false;
	}
	
	public void updateAckedNumber() {
		sentUnacked += -1;
	}
}