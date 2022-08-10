package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;

import java.awt.*;
import java.io.File;

public class OpenImages {

	public static Bitmap importImage(EditableModel model, Component parent) {
		FileDialog fileDialog = new FileDialog(parent);
		File selectedFile = fileDialog.openFile(FileDialog.OPEN_TEXTURE);
		if (selectedFile != null) {
			if (model != null && model.getFile() != null) {
				File modelDirectory = model.getFile().getParentFile();
				return new Bitmap(modelDirectory.toPath().relativize(selectedFile.toPath()).toString());
			}
			return new Bitmap(selectedFile.toPath().toString());
		}
		return null;
	}
	public static Bitmap[] importImages(EditableModel model, Component parent) {
		FileDialog fileDialog = new FileDialog(parent);
		File[] selectedFiles = fileDialog.openFiles(FileDialog.OPEN_TEXTURE);
		if (selectedFiles != null) {
			Bitmap[] bitmaps = new Bitmap[selectedFiles.length];
			for(int i = 0; i<selectedFiles.length; i++){
				if (model != null && model.getFile() != null) {
					File modelDirectory = model.getFile().getParentFile();
					bitmaps[i] = new Bitmap(modelDirectory.toPath().relativize(selectedFiles[i].toPath()).toString());
				} else {
					bitmaps[i] = new Bitmap(selectedFiles[i].toPath().toString());
				}
			}
			return bitmaps;
		}

		return null;
	}
}
