package utility.arrays;

/**
 * Classe com m�todos para facilitar opera��es sobre arrays do projeto.
 * @author Gabriel Barbosa
 */
public class ArrayMethods {
	/**
	 * Concatena dois arrays de bytes.
	 * @param a Primeiro array.
	 * @param b Segundo array.
	 * @return Arrays concatenados.
	 */
	public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
		byte[] ab = new byte[a.length + b.length];
		
		System.arraycopy(a, 0, ab, 0, a.length);
		System.arraycopy(b, 0, ab, a.length, b.length);

		return ab;
	}
	
	/**
	 * Adiciona byte ao fim de um array de bytes.
	 * @param a Array de bytes.
	 * @param b Byte a ser adicionado ao fim do array.
	 * @return Array com novo byte ao fim.
	 */
	public static byte[] concatenateByteArrays(byte[] a, byte b) {
		byte[] ab = new byte[a.length + 1];
		
		System.arraycopy(a, 0, ab, 0, a.length);
		ab[ab.length - 1] = b;
		
		return ab;
	}
	
	/**
	 * Prefixa array de bytes com um byte especificado.
	 * @param a Byte a ser adicionado � frente do array.
	 * @param b Array a ser prefixado com byte.
	 * @return Array com novo byte ao in�cio.
	 */
	public static byte[] concatenateByteArrays(byte a, byte[] b) {
		byte[] ab = new byte[b.length + 1];
		
		System.arraycopy(b, 0, ab, 1, b.length);
		ab[0] = a;
		
		return ab;
	}
	
	/**
	 * Destaca subarray de um array de bytes fornecidos.
	 * @param array Array de que se destacar� subarray.
	 * @param len Tamanho do subarray.
	 * @param offset Ponto inicial do destaque do array.
	 * @return Subarray do array especificado.
	 */
	public static byte[] byteArraySubset(byte[] array, int offset, int len) {
		byte[] newArray = new byte[len];
		
		System.arraycopy(array, offset, newArray, 0, len);
		
		return newArray;
	}
}
