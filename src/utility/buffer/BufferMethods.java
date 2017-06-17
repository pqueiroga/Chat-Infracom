package utility.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
	 * Coloca uma porta num arranjo de bytes.
	 * @param buf Arranjo de bytes que deve receber a String str.
	 * @param str String que deve ser colocada no arranjo buf.
	 */
	public static void toByteArray(byte[] buf, int port) {
		String str = port + "";
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
	public static void writeString(String str, OutputStream os) throws IOException {
		byte[] buffer = new byte[256];
		os.write(str.length());
		BufferMethods.toByteArray(buffer, str);
		os.write(buffer, 0, str.length());
	}
	
	/**
	 * Lê uma string da input stream. antes lê o tamanho.
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String readString(InputStream is) throws IOException {
		byte[] buffer = new byte[256];
		int strlen = is.read();
		is.read(buffer, 0, strlen);
		String str = BufferMethods.byteArraytoString(buffer, strlen);
		return str;
	}
}
