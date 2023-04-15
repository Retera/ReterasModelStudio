package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.ImageUtils.ImageCreator;

import java.awt.image.BufferedImage;

public class File {
	private static final FileDialog fileDialog = new FileDialog();

	public static class Save extends ActionFunction{
		public Save(){
			super(TextKey.SAVE, () -> onClickSave(ProgramGlobals.getCurrentModelPanel()), "control S");
		}
	}

	public static class Open extends ActionFunction{
		public Open(){
			super(TextKey.OPEN, () -> onClickOpen(FileDialog.OPEN_FILE), "control O");
		}
	}


	public static class SaveAs extends ActionFunction{
		public SaveAs(){
			super(TextKey.SAVE_AS, () -> onClickSaveAs(ProgramGlobals.getCurrentModelPanel(), FileDialog.SAVE), "control Q");
		}
	}


	public static void onClickOpen(int operationType) {
		java.io.File file = fileDialog.openFile(operationType);

		if (file != null) {
			openFile(file);
		}
	}
	public static void openFile(final java.io.File file) {
		if (file != null) {
			SaveProfile.get().setPath(file.getParent());

			SaveProfile.get().addRecent(file.getPath());
			ProgramGlobals.getMenuBar().updateRecent();
			ModelLoader.loadFile(file);
		}
	}

	public static void onClickSave(ModelPanel modelPanel) {
		System.out.println("saving");
		EditableModel model = modelPanel.getModel();
		onClickSave(modelPanel, model);
	}

	private static void onClickSave(ModelPanel modelPanel, EditableModel model) {
		if (model != null && !model.isTemp() && model.getFile() != null) {
			saveModel(model, model.getFile(), modelPanel);
		} else {
			onClickSaveAs(modelPanel, FileDialog.SAVE);
		}
	}


	private static void saveModel(EditableModel model, java.io.File modelFile, ModelPanel modelPanel) {
		String ext = fileDialog.getExtension(modelFile);

		try {
			if (ext.equals("mdl")) {
				MdxUtils.saveMdl(model, modelFile);
			} else {
				MdxUtils.saveMdx(model, modelFile);
			}
			model.setFileRef(modelFile);
			model.setTemp(false);

			if (modelPanel != null) {
				modelPanel.getModelHandler().getUndoManager().resetActionsSinceSave();
				modelPanel.updateMenuItem();
			}
			SaveProfile.get().addRecent(modelFile.getPath());
			ProgramGlobals.getMenuBar().updateRecent();
		} catch (final Exception exc) {
			exc.printStackTrace();
			ExceptionPopup.display(exc);
		}
	}

	public static boolean onClickSaveAs(ModelPanel modelPanel, int operationType) {
		EditableModel model = modelPanel.getModel();
		return onClickSaveAs(modelPanel, operationType, model);
	}

	public static boolean onClickSaveAs(ModelPanel modelPanel, int operationType, EditableModel model) {
		String fileName = "";
		if (model != null) {
			if (model.getFile() == null) {
				fileName = model.getName();
			} else {
				fileName = model.getFile().getName();
			}
		}

		java.io.File file = fileDialog.getSaveFile(operationType, fileName);

		if (file != null) {
			String ext = fileDialog.getExtension(file).toLowerCase();
			if (fileDialog.isSavableModelExt(ext)) {
				saveModel(model, file, modelPanel);
				return true;
			} else if (fileDialog.isSavableTextureExt(ext)) {
				if (model != null && model.getMaterial(0) != null) {
					BufferedImage bufferedImage = ImageCreator.getBufferedImage(model.getMaterial(0), model.getWrappedDataSource());
					if (bufferedImage != null) {
						return ExportTexture.saveTexture(bufferedImage, file, ext, ProgramGlobals.getMainPanel());
					}
				}
			}
		}
		return false;
	}
}
