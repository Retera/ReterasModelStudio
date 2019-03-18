package com.matrixeater.hacks;

import java.io.File;

public class ListFileComparingGuy {
	private static String zezulaPath = "C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Maps\\Download\\TemplarNaruto\\Archive";
	private static String myPath = "C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Maps\\Download\\TemplarNaruto\\JavaArchive";

	public static void main(final String[] args) {
		traverseMyPath(new File(myPath));
		traverseZPath(new File(zezulaPath));
	}

	public static void traverseMyPath(final File file) {
		if (file.isDirectory()) {
			for (final File subFile : file.listFiles()) {
				traverseMyPath(subFile);
			}
		} else {
			final String partialPathEnding = file.getAbsolutePath().substring(myPath.length());
			final String zezulaPathOfFile = zezulaPath + partialPathEnding;
			if (!new File(zezulaPathOfFile).exists()) {
				System.out.println("Not in MPQ Editor out: " + partialPathEnding);
			}
		}
	}

	public static void traverseZPath(final File file) {
		if (file.isDirectory()) {
			for (final File subFile : file.listFiles()) {
				traverseZPath(subFile);
			}
		} else {
			final String partialPathEnding = file.getAbsolutePath().substring(zezulaPath.length());
			final String myPathOfFile = myPath + partialPathEnding;
			if (!new File(myPathOfFile).exists()) {
				System.out.println("Not in My Java code out: " + partialPathEnding);
			}
		}
	}

}
