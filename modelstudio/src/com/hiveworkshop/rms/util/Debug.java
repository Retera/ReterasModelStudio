package com.hiveworkshop.rms.util;

public class Debug {
	static boolean debug = true;
	static boolean inform = true;
	public static void print(String... s) {
		if(debug){
			System.out.println(String.join(", ", s));
		}
	}
	public static void print(Byte... v) {
		if(debug){
			StringBuilder sb = new StringBuilder();
			for (Byte i : v){
				sb.append(i);
				sb.append(", ");
			}
			sb.delete(sb.length()-2, sb.length());
			System.out.println(sb);
		}
	}
	public static void print(Short... v) {
		if(debug){
			StringBuilder sb = new StringBuilder();
			for (Short i : v){
				sb.append(i);
				sb.append(", ");
			}
			sb.delete(sb.length()-2, sb.length());
			System.out.println(sb);
		}
	}
	public static void print(Integer... v) {
		if(debug){
			StringBuilder sb = new StringBuilder();
			for (Integer i : v){
				sb.append(i);
				sb.append(", ");
			}
			sb.delete(sb.length()-2, sb.length());
			System.out.println(sb);
		}
	}
	public static void print(Long... v) {
		if(debug){
			StringBuilder sb = new StringBuilder();
			for (Long i : v){
				sb.append(i);
				sb.append(", ");
			}
			sb.delete(sb.length()-2, sb.length());
			System.out.println(sb);
		}
	}
	public static void print(Float... v) {
		if(debug){
			StringBuilder sb = new StringBuilder();
			for (Float i : v){
				sb.append(i);
				sb.append(", ");
			}
			sb.delete(sb.length()-2, sb.length());
			System.out.println(sb);
		}
	}
	public static void print(Double... v) {
		if(debug){
			StringBuilder sb = new StringBuilder();
			for (Double i : v){
				sb.append(i);
				sb.append(", ");
			}
			sb.delete(sb.length()-2, sb.length());
			System.out.println(sb);
		}
	}

	public static void inform(String s){
		if(inform){
			System.out.println(s);
		}
	}
	public static void tempPrint(String s){
		// To use for temp info (that should be removed in the end) when developing;
		// might be a bit easier to find and remove
		if(debug){
			System.out.println(s);
		}
	}
}
