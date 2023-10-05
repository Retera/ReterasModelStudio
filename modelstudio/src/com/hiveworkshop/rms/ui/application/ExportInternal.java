package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.actionfunctions.ExportTexture;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

public class ExportInternal {

	private static void exportConvert(String internalPath, File selectedFile, CompoundDataSource dataSource,
	                                  String expExt, String saveExt, FileDialog fileDialog) {
		if (expExt.equalsIgnoreCase("mdl") || expExt.equalsIgnoreCase("mdx")) {
			try {
				MdlxModel model = MdxUtils.loadMdlx(internalPath, dataSource);
				if (saveExt.equalsIgnoreCase("mdl")) {
					MdxUtils.saveMdl(model, selectedFile);
				} else if (saveExt.equalsIgnoreCase("mdx")) {
					MdxUtils.saveMdx(model, selectedFile);
				}
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		} else if (fileDialog.isSavableTextureExt(saveExt)) {
			BufferedImage gameTex = BLPHandler.getImage(internalPath);
			ExportTexture.saveTexture(gameTex, selectedFile, saveExt, ProgramGlobals.getMainPanel());
		}
	}

	public static void exportInternalFile(String internalPath, String type, JComponent parent) {
		exportInternalFile(internalPath, type, GameDataFileSystem.getDefault(), parent);
	}

	public static void exportInternalFile(String internalPath, String type, CompoundDataSource dataSource, JComponent parent) {
		FileDialog fileDialog = new FileDialog(parent);
		// only filename
		String internalPath2 = internalPath.replaceAll("^(.+[\\\\/])*", "");

		if (dataSource.has(internalPath)) {
			System.out.println("exporting, path: \"" + internalPath + "\"");
			String extension = fileDialog.getExtension(internalPath);
			if (fileDialog.isSupModel(extension)) {
				saveModel(internalPath, fileDialog, dataSource, extension);
			} else if (fileDialog.isSupTexture(extension)) {
				saveTexture(internalPath, fileDialog, dataSource, extension);
			} else {
				saveOther(internalPath, type, fileDialog, dataSource, extension);
			}

		} else if (dataSource.has(internalPath2)) {
			System.out.println("exporting, alt path: \"" + internalPath2 + "\" (org path: \""  + internalPath + "\")");
			String extension = fileDialog.getExtension(internalPath2);
			if (fileDialog.isSupModel(extension)) {
				saveModel(internalPath2, fileDialog, dataSource, extension);
			} else if (fileDialog.isSupTexture(extension)) {
				saveTexture(internalPath2, fileDialog, dataSource, extension);
			} else {
				saveOther(internalPath2, type, fileDialog, dataSource, extension);
			}
		} else {
			System.out.println("Could not find \"" + internalPath + "\" nor \"" + internalPath2 + "\"");
			JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(), "Could not find \"" + internalPath + "\"", "File not found", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void saveOther(String internalPath, String type, FileDialog fileDialog, CompoundDataSource dataSource, String extension) {
		FileNameExtensionFilter filter = new FileNameExtensionFilter(type, extension == null ? "" : extension);
		String fileName = internalPath.replaceAll(".+[\\\\/](?=.+)", "");
		File selectedFile = fileDialog.getSaveFile(fileName, Collections.singletonList(filter));
		if (selectedFile != null) {
			exportExact(internalPath, selectedFile, dataSource);
		}
	}

	private static void saveTexture(String internalPath, FileDialog fileDialog, CompoundDataSource dataSource, String extension) {
		String fileName = internalPath.replaceAll(".+[\\\\/](?=.+)", "");
		File selectedFile = fileDialog.getSaveFile(FileDialog.SAVE_TEXTURE, fileName);
		if (selectedFile != null) {
			String saveExt = fileDialog.getExtensionOrNull(selectedFile);
			if (!extension.equalsIgnoreCase(saveExt) && (fileDialog.isSavableTextureExt(saveExt))) {
				BufferedImage gameTex = BLPHandler.getImage(internalPath);
				ExportTexture.saveTexture(gameTex, selectedFile, saveExt, ProgramGlobals.getMainPanel());
			} else {
				exportExact(internalPath, selectedFile, dataSource);
			}
		}
	}

	private static void saveModel(String internalPath, FileDialog fileDialog, CompoundDataSource dataSource, String extension) {
		String fileName = internalPath.replaceAll(".+[\\\\/](?=.+)", "");
		File selectedFile = fileDialog.getSaveFile(FileDialog.SAVE_MODEL, fileName);
		if (selectedFile != null) {
			String saveExt = fileDialog.getExtensionOrNull(selectedFile);
			if (!extension.equalsIgnoreCase(saveExt) && (fileDialog.isSavableModelExt(saveExt))) {
				try {
					MdlxModel model = MdxUtils.loadMdlx(internalPath, dataSource);
					if (saveExt.equalsIgnoreCase("mdl")) {
						MdxUtils.saveMdl(model, selectedFile);
					} else if (saveExt.equalsIgnoreCase("mdx")) {
						MdxUtils.saveMdx(model, selectedFile);
					}
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			} else {
				exportExact(internalPath, selectedFile, dataSource);
			}
		}
	}

	private static void exportExact(String internalPath, File selectedFile, CompoundDataSource dataSource) {
		System.out.println("Exporting from internal path: " + dataSource.getFile(internalPath).getName());
		InputStream resourceAsStream = dataSource.getResourceAsStream(internalPath);
		if (resourceAsStream != null) {
			try {
				Files.copy(resourceAsStream, selectedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		} else {
			System.err.println("Data source " + dataSource.getClass().getSimpleName() + " returned null instead of an input stream");
			new Exception().printStackTrace();
		}
	}
}
