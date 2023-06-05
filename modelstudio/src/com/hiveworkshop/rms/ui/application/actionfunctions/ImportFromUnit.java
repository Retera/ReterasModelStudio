package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanelGui;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.awt.*;

public class ImportFromUnit extends ActionFunction{
	public ImportFromUnit(){
		super(TextKey.IMPORT_FROM_UNIT, ImportFromUnit::importUnitActionRes, "control shift U");
	}

	public static void importUnitActionRes(ModelHandler modelHandler){
		EditableModel animationSource = getFileModel();
		if (animationSource != null) {
			new ImportPanelGui(modelHandler.getModel(), animationSource, ModelLoader::loadModel);
		}
		if (ProgramGlobals.getCurrentModelPanel() != null) {
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
		}
	}

	public static EditableModel getFileModel(){
		String filepath = fetchUnitPath(ProgramGlobals.getMainPanel());
		if (filepath != null) {
			return InternalFileLoader.getEditableModel(filepath, true);
		}
		return null;
	}

	public static String fetchUnitPath(Component component) {
		GameObject choice = UnitOptionPanel.getGameObject(component);
		if (choice != null) {
			return choice.getField("file");
		}
		return null;
	}
}
