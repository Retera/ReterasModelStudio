package com.hiveworkshop.wc3.jworldedit.wipdesign.test;

import java.io.File;

public class LSOfFolder {
	public static void main(final String[] args) {
		final File folder = new File("F:\\Users\\Eric\\Downloads\\mdx-m3-viewer-master\\ericTest/");
		recurse(folder, folder);
	}

	public static void recurse(final File parent, final File current) {
		for (final File file : current.listFiles()) {
			if (file.isDirectory()) {
				recurse(parent, file);
			} else if (file.getPath().endsWith(".js")) {
				System.out.println(stmtFor(parent, file));
			}
		}
	}

	public static String stmtFor(final File parent, final File file) {
		return "    <script src=\"" + file.getPath().substring(parent.getPath().length() + 1).replace("\\", "/")
				+ "\"></script>";
	}
}
