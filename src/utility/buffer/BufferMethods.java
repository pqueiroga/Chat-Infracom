package utility.buffer;

import java.io.IOException;
import java.net.SocketException;

import protocol.DGSocket;

/**
 * classe com métodos pra transferência de mensagem
 * @author Pedro Queiroga <psq@cin.ufpe.br>
 *
 */
public class BufferMethods {
	/**
	 * Coloca uma cadeia num arranjo de bytes.
	 * @param buf Arranjo de bytes que deve receber a String str.
	 * @param str String que deve ser colocada no arranjo buf.
	 */
	public static void toByteArray(byte[] buf, String str) {
		for (int i = 0; i < str.length(); i++) {
			buf[i] = (byte) str.charAt(i);
		}
	}
	
	/**
	 * Coloca um int num arranjo de bytes.
	 * @param buf Arranjo de bytes com 5 posições que deve receber o inteiro.
	 * @param inteiro int que deve ser colocada no arranjo buf.
	 */
	public static void toByteArray(byte[] buf, int inteiro) {
		String str = inteiro + "";
		while (str.length() < 5) {
			str = "0" + str;
		}
		for (int i = 0; i < str.length(); i++) {
			buf[i] = (byte) str.charAt(i);
		}
	}
	
	/**
	 * Cria uma cadeia de caracteres a partir de um arranjo de bytes.
	 * @param buf Buffer que deve ser transformado em uma cadeia de caracteres.
	 * @param strlen Tamanho da cadeia de caracteres.
	 * @return Uma cadeia composta pelos caracteres de buf.
	 */
	public static String byteArraytoString(byte[] buf, int strlen) {
		String retorno = "";
		for (int i = 0; i < strlen; i++) {
			retorno += (char) buf[i];
		}
		return retorno;
	}
	
	/**
	 * Escreve string na output stream, já dizendo o  tamanho de antemão
	 * @param str String a ser enviada
	 * @param os OuputStream
	 * @throws IOException
	 */
	public static void writeString(String str, DGSocket dgs) throws IOException {
		byte[] buffer = new byte[256];
//		byte[] lenByte = new byte[5];
//		BufferMethods.toByteArray(lenByte, str.length());
//		os.write(str.length());
//		dgs.send(lenByte, 5); // diz tamanho da string que estarei enviando
		sendInt(str.length(), dgs);
		BufferMethods.toByteArray(buffer, str);
//		os.write(buffer, 0, str.length());
		dgs.send(buffer, str.length()); // envia string
	}
	
	/**
	 * Lê uma string da input stream. antes lê o tamanho.
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String readString(DGSocket dgs) throws IOException {
		byte[] buffer = new byte[256];
//		byte[] lenByte = new byte[5]; // sempre 5 dígitos pra usar o método que já temos (int to byte array)
//		int strlen = is.read();
//		dgs.receive(lenByte, 5);
//		int strlen = Integer.parseInt(BufferMethods.byteArraytoString(lenByte, 5));
		int strlen = receiveInt(dgs);
//		is.read(buffer, 0, strlen);
		dgs.receive(buffer, strlen);
		String str = BufferMethods.byteArraytoString(buffer, strlen);
		return str;
	}
	
	public static String readChatString(DGSocket dgs) throws IOException {
		byte[] buffer = new byte [1024];
//		int strlen = is.read();
//		is.read(buffer, 0, strlen);
		int strlen = receiveInt(dgs);
		dgs.receive(buffer, strlen);
		return new String(buffer, 0, strlen, "UTF-8");
	}
	
	public static void writeChatString(String str, DGSocket dgs) throws IOException {
		byte[] buffer = str.getBytes("UTF-8");
//		os.write(buffer.length);
//		os.write(buffer, 0, buffer.length);
//		byte[] lenByte = new byte[5];
//		BufferMethods.toByteArray(lenByte, buffer.length);
//		dgs.send(lenByte, 5);
		sendInt(buffer.length, dgs);
		dgs.send(buffer, buffer.length);
	}
	
	public static int receiveInt(DGSocket dgs) throws SocketException {
		byte[] intByte = new byte[5];
		dgs.receive(intByte, 5); // recebe inteiro com sempre 5 dígitos
		return Integer.parseInt(byteArraytoString(intByte, 5)); // transforma num inteiro
	}
	
	public static void sendInt(int inteiro, DGSocket dgs) throws IOException {
		byte[] intByte = new byte[5];
		toByteArray(intByte, inteiro); 
		dgs.send(intByte, 5); // envia o inteiro com sempre 5 dígitos
	}
	
	public static void sendFeedBack(int feedback, DGSocket dgs) throws IOException {
		byte[] feedBackByte = {(byte) feedback};
		dgs.send(feedBackByte, 1);
	}
	
	public static int receiveFeedBack(DGSocket dgs) throws SocketException {
		byte[] feedBackByte = new byte[1];
		dgs.receive(feedBackByte, 1);
		return feedBackByte[0];
	}
}
