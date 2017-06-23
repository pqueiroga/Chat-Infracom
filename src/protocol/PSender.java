package protocol;

import utility.Strings.StringMethods;
import utility.arrays.ArrayMethods;
import utility.Exceptions.UnexistantFlagException;
import java.util.LinkedList;
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
	private LinkedList<TimerTask> timerTasks;
	private PReceiver receiverSide;
	private PipedInputStream dataIn;
	private DataInputStream dis;
	private int lastReadFromStream, lastSent, sentUnacked;
	private long packetTimeout;
	private byte[] buf;
	private boolean alive, inWriteMode;
	private TimerTask timerTask;
	private Timer timer;
	
	/**
	 * Cria parte remetente de datagramas de uma Socket.
	 * @param datagramSocket Socket de datagramas que enviar� dados.
	 * @param bufferSize Tamanho do buffer para datagramas.
	 */
	public PSender(DatagramSocket datagramSocket, int bufferSize, long packetTimeout) {
		this.datagramSocket = datagramSocket;
		dataIn = new PipedInputStream();
		dis = new DataInputStream(dataIn);
		buf = new byte[bufferSize];
		lastReadFromStream = -1;
		sentUnacked = 0;
		lastSent = -1;
		this.packetTimeout = packetTimeout;
		inWriteMode = true;
		alive = true;
		timer = new Timer();
		timerTasks = new LinkedList<TimerTask>();

		timerTask = new TimerTask() { 
			public void run() {
				if (alive) {
					int leftToRead = 0;
					
					try {leftToRead = dis.available();} catch (IOException ioe) {}
					
					if ((leftToRead > 0) && (sentUnacked < receiverSide.getTheirWindowSize())) {
						int lenRead = 0;
						
						try {lenRead = dis.read(buf);} catch (IOException ioe) {}
						
						timerTasks.add(sendData(encapsulateDatagram(buf, 0, lenRead), lastSent + lenRead));
						
						lastSent += lenRead;
					}
				} else {
					try {
						dataIn.close();
						dis.close();
					} catch (IOException ioe) {}
					finally {
						timer.cancel();
					}
				}
			}
		};
	}

	/**
	 * A classe PSender precisa de uma refer�ncia � sua parte receptora correspondente,
	 * o m�todo setReceiver configura essa refer�ncia para o PReceiver especificado.
	 * @param receiver PReceiver a ser referenciado.
	 */
	public void setReceiver(PReceiver receiver) {
		receiverSide = receiver;
	}

	/**
	 * Transforma mensagem em segmento.
	 * @param buf Array de bytes com dados a serem enviados
	 * @param offset In�cio dos dados a serem copiados do array.
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
	 * @param buf ByteBuffer que guarda as informa��es a serem encapsuladas.
	 * @param len Quanta informa��o do ByteBuffer encapsular.
	 * @return DatagramPacket com informa��es encapsuladas.
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
	 * @param seqnum N�mero de sequ�ncia do segmento.
	 * @param acknum N�mero confirmado.
	 * @param flagSettings byte de flags.
	 * @return Mensagem com cabe�alhos (segmento) em forma de array de bytes.
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
	 * Cria byte que representa flags setadas do cabe�alho.
	 * @param flagSettings Array de Strings, cada uma deve conter o nome da flag a ser setada.
	 * @return byte representante das flags setadas do cabe�alho.
	 * @throws UnexistantFlagException Caso uma flag especificada em flagSettings n�o exista.
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
	 * Envia dados a destinat�rio.
	 * @param toSend DatagramPacket a ser enviado.
	 * @param lastByte �ltimo byte da mensagem enviada por este segmento.
	 */
	private TimerTask sendData(DatagramPacket toSend, int lastByte) {
		try {datagramSocket.send(toSend);} catch (IOException ioe) {}
		
		TimerTask task = new TimerTask() {
			public void run() {
				if (receiverSide.getLastAcked() <= lastByte) {
					reSendData(toSend);
				} else this.cancel();
			}
		};
		
		timer.schedule(task, packetTimeout, packetTimeout);
		
		return task;
	}
	
	private void reSendData(DatagramPacket toSend) {
		try {datagramSocket.send(toSend);} catch (IOException ioe) {}
	}

	/**
	 * Para conex�o com PipedOutputStream da Thread escritora.
	 * @return PipedInputStream do PacketSender.
	 */
	public PipedInputStream getPipedInputStream() {
		return dataIn;
	}
	
	/**
	 * Para execu��o da thread
	 */
	public void kill() {
		alive = false;
	}
	
	public void updateAckedNumber() {
		sentUnacked += -1;
	}
}