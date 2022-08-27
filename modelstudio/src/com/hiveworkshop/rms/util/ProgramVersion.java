package com.hiveworkshop.rms.util;

import java.io.InputStream;

public class ProgramVersion {
	private static String version;
	/**
	 * Returns the content of version.txt which is generated
	 * by modelstudio/build.gradle
	 * */
	public static String get() {
		if(version == null){
			try (InputStream inputStream = ProgramVersion.class.getClassLoader().getResourceAsStream("version.txt")) {
				if (inputStream != null) {
					version = new String(inputStream.readAllBytes());
				}
			} catch (Exception exception) {
				version = "v0.05 Beta Build"; // In case the file doesn't exists, and during development
				exception.printStackTrace();
			}
		}
		return version;
	}
	public static String getSurrounded() {
		return "(" + get() + ")";
	}
}
