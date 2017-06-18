package utility.arrays;

import java.util.Arrays;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class ArrayMethods {
	public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
		byte[] ab = new byte[a.length + b.length];
		
		System.arraycopy(a, 0, ab, 0, a.length);
		System.arraycopy(b, 0, ab, a.length, b.length);

		return ab;
	}
	
	public static byte[] byteArraySubset(byte[] array, int len, int offset) {
		byte[] newArray = new byte[len];
		for (int i = offset;i < offset + len;i++) {
			newArray[i - offset] = array[i];
		}
		
		return newArray;
	}
}
