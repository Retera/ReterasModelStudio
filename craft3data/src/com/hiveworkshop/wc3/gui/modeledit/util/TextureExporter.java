package com.hiveworkshop.wc3.gui.modeledit.util;

import java.awt.Component;
import java.io.File;

import javax.swing.filechooser.FileFilter;

public interface TextureExporter {
	void showOpenDialog(final String suggestedName, final TextureExporterClickListener fileHandler,
			final Component parent);

	void exportTexture(final String suggestedName, final TextureExporterClickListener fileHandler,
			final Component parent);

	public static interface TextureExporterClickListener {
		void onClickOK(File selectedFile, FileFilter selectedFilter);
	}
}
