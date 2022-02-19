package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class InternalFileLoader {

	public static void loadMdxStream(MutableGameObject obj, String filepath, boolean showModel) {
		ImageIcon icon = new ImageIcon(IconUtils
				.getIcon(obj)
				.getScaledInstance(16, 16, Image.SCALE_DEFAULT));

		loadFromStream(filepath, icon, showModel);
	}

	public static void loadFromStream(String filepath, ImageIcon icon, boolean showModel) {
		if (filepath != null) {
			loadModelPanel(true, showModel, icon, getEditableModel(filepath, false));

			if (ProgramGlobals.getPrefs().isLoadPortraits()) {
				String portrait = ModelUtils.getPortrait(filepath);;
				loadModelPanel(true, false, icon, getEditableModel(portrait, true));
			}
		}
	}

	public static void loadStreamMdx(InputStream f, boolean temporary, boolean showModel, ImageIcon icon) {
		EditableModel model = getEditableModel(f);
		loadModelPanel(temporary, showModel, icon, model);
	}

	public static void loadStreamMdx(String filepath, boolean temporary, boolean showModel, ImageIcon icon) {
		EditableModel model = getEditableModel(filepath, false);
		loadModelPanel(temporary, showModel, icon, model);

	}

	public static void loadFilepathMdx(String filepath, boolean temporary, boolean showModel, ImageIcon icon) {
		EditableModel model = getEditableModel(filepath, false);
		loadModelPanel(temporary, showModel, icon, model);

	}

	private static void loadModelPanel(boolean temporary, boolean showModel, ImageIcon icon, EditableModel model) {
		if(model != null){
			ModelPanel temp = new ModelPanel(new ModelHandler(model, icon));
			ModelLoader.loadModel(temporary, showModel, temp);
		}
	}

	public static EditableModel getEditableModel(String filepath, boolean doCheckExist) {
		String path = ImportFileActions.convertPathToMDX(filepath);
//		if (!doCheckExist || GameDataFileSystem.getDefault().has(filepath)) {
		if (!doCheckExist || GameDataFileSystem.getDefault().has(path)) {
			System.err.println("loading: " + path);
			InputStream resourceAsStream = GameDataFileSystem.getDefault().getResourceAsStream(path);
			return getEditableModel(resourceAsStream);
		}
		return null;
	}

	public static EditableModel getEditableModel(InputStream f) {
		try {
			EditableModel model = MdxUtils.loadEditable(f);
			model.setFileRef(null);
			return model;
		} catch (final IOException e) {
			e.printStackTrace();
			ExceptionPopup.display("Reading mdx failed", e);
			throw new RuntimeException("Reading mdx failed");
		}
	}
}
