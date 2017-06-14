package utility.arrays;

public class ArrayMethods {
	public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
		byte[] ab = new byte[a.length + b.length];
		
		System.arraycopy(a, 0, ab, 0, a.length);
		System.arraycopy(b, 0, ab, a.length, b.length);

		return ab;
	}
}
