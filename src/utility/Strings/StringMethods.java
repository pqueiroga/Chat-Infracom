package utility.Strings;

/**
 * Classe para facilitar opera��es sobre Strings do projeto.
 * @author Gabriel Barbosa
 */
public class StringMethods {
	/**
	 * Prefixa String representante de um n�mero com (size - (comprimento de number)) zeros.
	 * @param number String representante de n�mero.
	 * @param size Tamanho desejado da String final.
	 * @return Retorna String original prefixada com zeros se size > (comprimento de number), caso contr�rio,
	 * retorna String original.
	 */
	public static String zeroPrefixTo(String number, int size) {
		if (size > number.length()) {
			String newString;
			if (Integer.parseInt(number) >= 0)
				newString = (new String(new char[size - number.length()]).replace('\0', '0')) + number;
			else
				newString = "-" + (new String(new char[size - (number.length() + 1)]).replace('\0', '0')) + number.substring(1);
			return newString;
		} else return number;
	}
}
