package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ModelEditActions;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.awt.event.KeyEvent;

public class RigSelection extends ActionFunction {
	public RigSelection(){
		super(TextKey.RIG_ACTION, () -> rigActionRes(), "control W");
		setMenuItemMnemonic(KeyEvent.VK_R);
	}



	public static void rigActionRes() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			ModelView modelView = modelPanel.getModelView();
			if (!modelView.getSelectedIdObjects().isEmpty() && !modelView.getSelectedVertices().isEmpty()) {
				modelPanel.getUndoManager().pushAction(ModelEditActions.rig(modelView));
			} else {
				System.err.println("NOT RIGGING, NOT VALID: " + modelView.getSelectedIdObjects().size() + " idObjects and " + modelView.getSelectedVertices() + " vertices selected");
			}
		}
	}
}
