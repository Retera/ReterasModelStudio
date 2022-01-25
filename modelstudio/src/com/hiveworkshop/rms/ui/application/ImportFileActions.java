package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanelGui;

import java.io.File;
import java.io.IOException;

public class ImportFileActions {

	public static void repaintModelTrees() {
		if (ProgramGlobals.getCurrentModelPanel() != null) {
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
		}
	}

	public static String convertPathToMDX(String filepath) {
		if (filepath.endsWith(".mdl")) {
			filepath = filepath.replace(".mdl", ".mdx");
		} else if (!filepath.endsWith(".mdx")) {
			filepath = filepath.concat(".mdx");
		}
		return filepath;
	}

	public static void importMdxObject(String path) {
		if (path != null) {
			String filepath = convertPathToMDX(path);
			if (filepath != null) {
				File animationSource = GameDataFileSystem.getDefault().getFile(filepath);
				ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
				if (modelPanel != null && modelPanel.getModel() != null) {
					try {
//                        ImportPanel importPanel = new ImportPanel(modelPanel.getModel(), MdxUtils.loadEditable(animationSource), true);
//                        importPanel.setModelChangeListener(new ModelStructureChangeListener());
						ImportPanelGui importPanel = new ImportPanelGui(modelPanel.getModel(), MdxUtils.loadEditable(animationSource));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		repaintModelTrees();
	}
}
