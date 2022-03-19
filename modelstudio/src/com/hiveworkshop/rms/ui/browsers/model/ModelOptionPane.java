package com.hiveworkshop.rms.ui.browsers.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.MainFrame;

import javax.swing.*;
import java.awt.*;

public class ModelOptionPane {
//	public static ModelElement showAndLogIcon(Component component) {
//		ModelOptionPanel uop = getModelOptionPanel(component);
//		if (uop != null) {
//			return new ModelElement(uop.getSelection(), uop.getCachedIconPath());
//		}
//		return null;
//	}

//	public static String show(Component component) {
//		ModelOptionPanel uop = getModelOptionPanel(component);
//		if (uop != null) {
//			return uop.getSelection();
//		}
//		return null;
//	}
//
//	public static String show(Component component, String startingFile) {
//		ModelOptionPanel uop = new ModelOptionPanel().setSelection(startingFile);
//		final int x = JOptionPane.showConfirmDialog(component, uop, "Choose Model", JOptionPane.OK_CANCEL_OPTION,
//				JOptionPane.PLAIN_MESSAGE);
//		if (x == JOptionPane.OK_OPTION) {
//			return uop.getSelection();
//		}
//		return null;
//	}

	public static ModelElement fetchModelElement(Component component) {
		ModelOptionPanel uop = getModelOptionPanel(component);
		if (uop != null) {
			ModelElement model = new ModelElement(uop.getSelection(), uop.getCachedIconPath());
			String filepath = model.getFilepath();
			if (isValidFilepath(filepath)) return model;
		}
		return null;
	}

	public static String fetchModelPath(Component component) {
		ModelOptionPanel uop = getModelOptionPanel(component);
		if (uop != null) {
			if (isValidFilepath(uop.getSelection())) return uop.getSelection();
//			ModelElement model =  new ModelElement(uop.getSelection(), uop.getCachedIconPath());
//			String filepath = model.getFilepath();
//			if (isValidFilepath(filepath)) return filepath;
		}
		return null;
	}

	public static EditableModel fetchModel(Component component) {
		ModelOptionPanel uop = getModelOptionPanel(component);
		if (uop != null) {
			if (isValidFilepath(uop.getSelection())) return uop.getSelectedModel();
		}
		return null;
	}

	private static ModelOptionPanel getModelOptionPanel(Component component) {
		ModelOptionPanel uop = new ModelOptionPanel();
		int x = JOptionPane.showConfirmDialog(component, uop, "Choose Model", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (x == JOptionPane.OK_OPTION) {
			return uop;
		} else {
			return null;
		}
	}

	private static boolean isValidFilepath(String filepath) {
		try {
			//check model by converting its path
			ImportFileActions.convertPathToMDX(filepath);
		} catch (final Exception exc) {
			exc.printStackTrace();
			JOptionPane.showMessageDialog(MainFrame.frame,
					"The chosen model could not be used.",
					"Program Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public static final class ModelElement {
		private final String filepath;
		private final String cachedIconPath;

		public ModelElement(String filepath, String cachedIconPath) {
			this.filepath = filepath;
			this.cachedIconPath = cachedIconPath;
		}

		public String getFilepath() {
			return filepath;
		}

		public String getCachedIconPath() {
			return cachedIconPath;
		}

		public boolean hasCachedIconPath() {
			return cachedIconPath != null && cachedIconPath.length() > 0;
		}
	}
}
