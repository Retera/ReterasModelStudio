package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.browsers.mpq.MPQBrowser;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;

public class MPQBrowserView {

	public static View createMPQBrowser(final ImageIcon imageIcon) {
		final MPQBrowser mpqBrowser = new MPQBrowser(GameDataFileSystem.getDefault(),
				filepath -> loadFileByType(filepath),
				path -> fetchModelTexture(path));
		final View view = new View("Data Browser", imageIcon, mpqBrowser);
		view.getWindowProperties().setCloseEnabled(true);
		return view;
	}

	public static void openMPQViewer() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		final View view = createMPQBrowser(new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)));
		mainPanel.rootWindow.setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(), view));
	}

	private static void loadFileByType(String filepath) {
		ModelLoader.loadFile(GameDataFileSystem.getDefault().getFile(filepath), true);
	}

	private static void fetchModelTexture(String path) {
		final int modIndex = Math.max(path.lastIndexOf(".w3mod/"), path.lastIndexOf(".w3mod\\"));
		String finalPath;
		if (modIndex == -1) {
			finalPath = path;
		} else {
			finalPath = path.substring(modIndex + ".w3mod/".length());
		}
		final ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			if (modelPanel.getModel().getFormatVersion() > 800) {
				finalPath = finalPath.replace("\\", "/"); // Reforged prefers forward slash
			}
			modelPanel.getModel().add(new Bitmap(finalPath));
			ModelStructureChangeListener.changeListener.texturesChanged();
		}
    }
}
