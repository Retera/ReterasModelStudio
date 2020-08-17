package com.matrixeater.hacks;

import java.io.File;
import java.io.IOException;

import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;

public class CopyTheModels {
	private static final File input = new File("C:\\Temp\\WarcraftIII\\war3.w3mod");
	private static final File output = new File("C:\\Temp\\WarcraftIII\\output");

	public static void main(final String[] args) throws IOException {
		traverse(input);
	}

	public static void traverse(final File file) throws IOException {
		if (file.isDirectory()) {
			for (final File subFile : file.listFiles()) {
				traverse(subFile);
			}
		} else {
			if (file.getPath().endsWith(".mdx")) {
				System.out.println(file);
				final EditableModel model = MdxUtils.loadEditableModel(file);
				for (final Bitmap b : model.getTextures()) {
					b.setPath("Textures\\White.blp");
				}
				final String wc3Path = file.getPath().substring(input.getPath().length());
				final File outputMDX = new File(output.getPath() + wc3Path);
				outputMDX.getParentFile().mkdirs();
				MdxUtils.saveEditableModel(model, outputMDX);
			}
		}
	}
}
