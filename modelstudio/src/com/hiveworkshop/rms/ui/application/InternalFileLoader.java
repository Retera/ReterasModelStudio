package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class InternalFileLoader {
	public static void loadStreamMdx(InputStream f, boolean temporary, boolean showModel, ImageIcon icon) {
		ModelPanel temp;
		try {
			final EditableModel model = MdxUtils.loadEditable(f);
			model.setFileRef(null);
			temp = ModelLoader.newTempModelPanel(icon, model);
		} catch (final IOException e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
			throw new RuntimeException("Reading mdx failed");
		}

		ModelLoader.loadModel(temporary, showModel, temp);
	}

	public static void loadMdxStream(MutableGameObject obj, String prePath, boolean showModel) {
		String path = ImportFileActions.convertPathToMDX(prePath);
		ImageIcon icon = new ImageIcon(IconUtils
				.getIcon(obj)
				.getScaledInstance(16, 16, Image.SCALE_DEFAULT));

		loadFromStream(path, icon, showModel);
	}

	public static void loadFromStream(String filepath, ImageIcon icon) {
		loadFromStream(filepath, icon, true);
	}

	public static void loadFromStream(String filepath, String iconPath) {
		ImageIcon icon;
		if(iconPath != null && iconPath.length() > 0){
			Image scaledInstance = BLPHandler.getGameTex(iconPath).getScaledInstance(16, 16, Image.SCALE_FAST);
			icon = new ImageIcon(scaledInstance);
		} else {
			icon = ModelLoader.MDLIcon;
		}
		loadFromStream(filepath, icon, true);
	}

	public static void loadFromStream(String filepath, ImageIcon icon, boolean showModel) {
		if (filepath != null) {
			loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(filepath), true, showModel, icon);

			String portrait = ModelUtils.getPortrait(filepath);;
			if (ProgramGlobals.getPrefs().isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
				loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false, icon);
			}
		}
	}
}
