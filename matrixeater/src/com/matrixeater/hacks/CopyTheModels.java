package com.matrixeater.hacks;

import java.io.File;

import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.MDL;

public class CopyTheModels {
	private static final File input = new File("C:\\Temp\\WarcraftIII\\war3.w3mod");
	private static final File output = new File("C:\\Temp\\WarcraftIII\\output");

	public static void main(final String[] args) {
		traverse(input);
	}

	public static void traverse(final File file) {
		if (file.isDirectory()) {
			for (final File subFile : file.listFiles()) {
				traverse(subFile);
			}
		} else {
			if (file.getPath().endsWith(".mdx")) {
				System.out.println(file);
				final MDL model = MDL.read(file);
				for (final Bitmap b : model.getTextures()) {
					b.setPath("Textures\\White.blp");
				}
				final String wc3Path = file.getPath().substring(input.getPath().length());
				final File outputMDX = new File(output.getPath() + wc3Path);
				outputMDX.getParentFile().mkdirs();
				model.printTo(outputMDX);
			}
		}
	}
}
