package utility.buffer;

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
}
