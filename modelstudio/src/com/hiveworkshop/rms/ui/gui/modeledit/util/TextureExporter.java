package com.hiveworkshop.rms.ui.gui.modeledit.util;

import java.awt.Component;
import java.io.File;

import javax.swing.filechooser.FileFilter;

public interface TextureExporter {
	void showOpenDialog(final String suggestedName, final TextureExporterClickListener fileHandler,
			final Component parent);

	void exportTexture(final String suggestedName, final TextureExporterClickListener fileHandler,
			final Component parent);

	interface TextureExporterClickListener {
		void onClickOK(File selectedFile, FileFilter selectedFilter);
	}
}
