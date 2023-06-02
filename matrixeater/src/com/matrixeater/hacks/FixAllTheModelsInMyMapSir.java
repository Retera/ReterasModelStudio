package com.matrixeater.hacks;

import java.io.File;

import com.hiveworkshop.wc3.mdl.EditableModel;

public class FixAllTheModelsInMyMapSir {
	public static void main(final String[] args) {
		final File mapFolder = new File(
				"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III Beta\\Maps\\TheSheepAttackFolderFixing.w3x");
		traverse(mapFolder);
	}

	public static void traverse(final File file) {
		if (file.isDirectory()) {
			for (final File subFile : file.listFiles()) {
				traverse(subFile);
			}
		} else if (file.getName().toLowerCase().endsWith(".mdx")) {
			final EditableModel model = EditableModel.read(file);
			System.out.println(file.getPath());
			if (model.getFormatVersion() != 900) {
				model.setFormatVersion(900);
				model.printTo(file, false);
			}
		}
	}
}
