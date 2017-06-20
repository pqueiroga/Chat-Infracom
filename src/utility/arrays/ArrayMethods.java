package utility.arrays;

import java.util.Arrays;

//import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

/**
 * Classe com métodos para facilitar operações sobre arrays do projeto.
 * @author Gabriel Barbosa
 */
public class ArrayMethods {
	/**
	 * Concatena dois arrays de bytes.
	 * @param a Primeiro array.
	 * @param b Segundo array.
	 * @return arrays concatenados.
	 */
	public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
		byte[] ab = new byte[a.length + b.length];
		
		System.arraycopy(a, 0, ab, 0, a.length);
		System.arraycopy(b, 0, ab, a.length, b.length);

		return ab;
	}
	
	/**
	 * Destaca subarray de um array de bytes fornecidos.
	 * @param array Array de que se destacará subarray.
	 * @param len Tamanho do subarray.
	 * @param offset Ponto inicial do destaque do array.
	 * @return Subarray do array especificado.
	 */
	public static byte[] byteArraySubset(byte[] array, int len, int offset) {
		byte[] newArray = new byte[len];
		for (int i = offset;i < offset + len;i++) {
			newArray[i - offset] = array[i];
		}
		
		return newArray;
	}
}
