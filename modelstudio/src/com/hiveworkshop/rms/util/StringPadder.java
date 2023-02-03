package com.hiveworkshop.rms.util;

public class StringPadder {

	public static String padStringStart(String string, String padding, int newLength){
		int paddingSize = newLength - string.length();
		if(paddingSize<0){
			System.err.println("Could not calculate padding size:");
			System.err.println("\tstring: \"" + string + "\"");
			System.err.println("\tpadding: \"" + padding + "\"");
			System.err.println("\tnewLength: " + newLength);
			System.err.println("\tpaddingSize: " + paddingSize);
			paddingSize = 0;
		}
		String repeat = padding.repeat(paddingSize).substring(0,paddingSize);
		return repeat + string;
	}

	public static String padStringEnd(String string, String padding, int newLength){
		int paddingSize = Math.max(0, newLength - string.length());
		String repeat = padding.repeat(paddingSize).substring(0,paddingSize);
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
