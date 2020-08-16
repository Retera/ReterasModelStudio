package com.matrixeater.hacks;

public class Testest {

	public static void main(final String[] args) {
		String path = "junk.tif";
		if ((path != null) && !path.isEmpty()) {
			final int dotIndex = path.lastIndexOf('.');
			if ((dotIndex != -1) && !path.endsWith(".blp")) {
				path = (path.substring(0, dotIndex));
			}
			if (!path.endsWith(".blp")) {
				path += ".blp";
			}
			System.out.println(path);
		}
	}

}
