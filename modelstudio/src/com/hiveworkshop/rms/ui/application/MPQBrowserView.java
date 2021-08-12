package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.browsers.mpq.MPQBrowser;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;

public class MPQBrowserView extends View {

	static ImageIcon imageIcon = new ImageIcon(MainFrame.MAIN_PROGRAM_ICON.getScaledInstance(16, 16, Image.SCALE_FAST));
	MPQBrowser mpqBrowser;

	public MPQBrowserView() {
		super("Data Browser", imageIcon, null);
		mpqBrowser = new MPQBrowser();
		getWindowProperties().setCloseEnabled(true);
		setComponent(mpqBrowser);
	}

	public MPQBrowserView prefsUpdated(){
		mpqBrowser.reloadFileSystem();
		return this;
	}

	private static void loadFileByType(String filepath) {
		ModelLoader.loadFile(GameDataFileSystem.getDefault().getFile(filepath), true);
	}

	private static void fetchModelTexture(String path) {
		int modIndex = Math.max(path.lastIndexOf(".w3mod/"), path.lastIndexOf(".w3mod\\"));
		String finalPath;
		if (modIndex == -1) {
			finalPath = path;
		} else {
			finalPath = path.substring(modIndex + ".w3mod/".length());
		}
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			if (modelPanel.getModel().getFormatVersion() > 800) {
				finalPath = finalPath.replace("\\", "/"); // Reforged prefers forward slash
			}
			modelPanel.getModel().add(new Bitmap(finalPath));
			ModelStructureChangeListener.changeListener.texturesChanged();
		}
    }
}
