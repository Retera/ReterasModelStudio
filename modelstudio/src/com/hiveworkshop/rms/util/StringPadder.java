package com.hiveworkshop.rms.util;

public class StringPadder {

	public static String padStringStart(String string, String padding, int newLength){
		int paddingSize = newLength - string.length();
		String repeat = padding.repeat(Math.max(0, paddingSize)).substring(0,paddingSize);
		return repeat + string;
	}

	public static String padStringEnd(String string, String padding, int newLength){
		int paddingSize = newLength - string.length();
		String repeat = padding.repeat(Math.max(0, paddingSize)).substring(0,paddingSize);
		return string + repeat;
	}

	public static String[] padStringStart(String padding, int newLength, String... strings){
		String[] newStrings = new String[strings.length];
		for (int i = 0; i < strings.length; i++){
			String string = strings[i];
			int paddingSize = newLength - string.length();
			String repeat = padding.repeat(Math.max(0, paddingSize)).substring(0,paddingSize);

			newStrings[i] = repeat + string;
		}
		return newStrings;
	}

	public static String[] padStringEnd(String padding, int newLength, String... strings){
		String[] newStrings = new String[strings.length];
		for (int i = 0; i < strings.length; i++){
			String string = strings[i];
			int paddingSize = newLength - string.length();
			String repeat = padding.repeat(Math.max(0, paddingSize)).substring(0,paddingSize);

			newStrings[i] = repeat + string;
		}
		return newStrings;
	}
}
