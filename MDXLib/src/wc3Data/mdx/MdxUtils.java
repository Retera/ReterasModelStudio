package wc3Data.mdx;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MdxUtils {

	/*
	 * private static Set<MdxComponent> optionalPool = new HashSet();
	 * 
	 * public void addOptionalPool(MdxComponent component){
	 * optionalPool.add(component); }
	 * 
	 * 
	 * public void executeOptionalPool(){
	 * 
	 * }
	 */

	public static MdxModel loadModel(BlizzardDataInputStream in)
			throws IOException {
		MdxModel model = new MdxModel();
		model.load(in);
		return model;
	}

	public static boolean checkOptionalId(BlizzardDataInputStream in,
			String name) throws IOException {

		in.mark(8);

		if (name.equals(in.readCharsAsString(4))) {
			in.reset();
			return true;
		}

		in.reset();
		return false;
	}

	public static void checkId(BlizzardDataInputStream in, String name)
			throws IOException {
		String found = in.readCharsAsString(4);
		if (!found.equals(name)) {
			throw new IOException(
					"Error loading model: CheckID failed, required " + name
							+ " found " + found);
		}
	}

	/*
	 * public static boolean checkOptionalId(BlizzardDataInputStream in, String
	 * name) throws IOException { if(lastCheck == null){ lastCheck =
	 * in.readCharsAsString(4); }
	 * 
	 * return lastCheck.equals(name); }
	 * 
	 * public static void checkId(BlizzardDataInputStream in, String name)
	 * throws IOException {
	 * 
	 * if(lastCheck != null){
	 * 
	 * if(!name.equals(lastCheck)){ throw new IOException(
	 * "Error loading model: CheckID failed after optinal check, required " +
	 * name + " found " + lastCheck); }
	 * 
	 * lastCheck=null;
	 * 
	 * }else{
	 * 
	 * String found = in.readCharsAsString(4); if (!found.equals(name)) { throw
	 * new IOException("Error loading model: CheckID failed, required " + name +
	 * " found " + found); } } }
	 */

	public static float[] loadFloatArray(BlizzardDataInputStream in, int size)
			throws IOException {
		float array[] = new float[size];

		for (int i = 0; i < size; i++) {
			array[i] = in.readFloat();
		}
		return array;
	}

	public static int[] loadIntArray(BlizzardDataInputStream in, int size)
			throws IOException {
		int array[] = new int[size];

		for (int i = 0; i < size; i++) {
			array[i] = in.readInt();
		}

		return array;
	}

	public static short[] loadShortArray(BlizzardDataInputStream in, int size)
			throws IOException {
		short array[] = new short[size];

		for (int i = 0; i < size; i++) {
			array[i] = (short) (in.readShort() & 0xFFFF);
		}

		return array;
	}

	public static byte[] loadByteArray(BlizzardDataInputStream in, int size)
			throws IOException {
		byte array[] = new byte[size];

		for (int i = 0; i < size; i++) {
			array[i] = in.readByte();
		}
		return array;
	}

	public static char[] loadCharArray(BlizzardDataInputStream in, int size)
			throws IOException {
		char array[] = new char[size];

		for (int i = 0; i < size; i++) {
			array[i] = in.readChar();
		}
		return array;
	}

	public static void saveFloatArray(BlizzardDataOutputStream out,
			float[] array) throws IOException {
		for (int i = 0; i < array.length; i++) {
			out.writeFloat(array[i]);
		}
	}

	public static void saveIntArray(BlizzardDataOutputStream out, int[] array)
			throws IOException {
		for (int i = 0; i < array.length; i++) {
			out.writeInt(array[i]);
		}
	}

	public static void saveShortArray(BlizzardDataOutputStream out,
			short[] array) throws IOException {
		for (int i = 0; i < array.length; i++) {
			out.writeNByteInt(array[i], 2);
		}
	}

	public static void saveByteArray(BlizzardDataOutputStream out, byte[] array)
			throws IOException {
		for (int i = 0; i < array.length; i++) {
			out.writeByte(array[i]);
		}
	}

	public static void saveCharArray(BlizzardDataOutputStream out, char[] array)
			throws IOException {
		out.writeChars(array);
	}
}
