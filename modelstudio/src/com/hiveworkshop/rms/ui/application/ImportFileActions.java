package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanelGui;

import java.io.File;
import java.io.IOException;

public class ImportFileActions {

	public static String convertPathToMDX(String filepath) {
		if (filepath != null && filepath.toLowerCase().endsWith(".mdl")) {
			filepath = filepath.replaceAll("\\.(M|m)(D|d)(L|l)", ".mdx");
//			filepath = filepath.replaceAll("(?-i)\\.mdl", ".mdx");
			System.out.println("to: " + filepath);
		} else if (filepath != null && !filepath.toLowerCase().endsWith(".mdx")) {
			filepath = filepath.concat(".mdx");
		}
		return filepath;
	}

	public static void importMdxObject(String path) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		String filepath = convertPathToMDX(path);
		if (filepath != null && modelPanel != null && modelPanel.getModel() != null) {
			File animationSource = GameDataFileSystem.getDefault().getFile(filepath);
			try {
				ImportPanelGui importPanel = new ImportPanelGui(modelPanel.getModel(), MdxUtils.loadEditable(animationSource));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		repaintModelTrees();
	}
	public static void importMdxObject(ModelHandler modelHandler, EditableModel model) {
		if (modelHandler != null && model != null) {
			ImportPanelGui importPanel = new ImportPanelGui(modelHandler.getModel(), model);
		}
		repaintModelTrees();
	}

	public static void repaintModelTrees() {
		if (ProgramGlobals.getCurrentModelPanel() != null) {
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
		}
	}
}
