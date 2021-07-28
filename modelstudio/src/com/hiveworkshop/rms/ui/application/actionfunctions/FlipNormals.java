package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.FlipNormalsAction;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

public class FlipNormals extends ActionFunction {
	public FlipNormals(){
		super(TextKey.FLIP_NORMALS, () -> insideOutNormals());
	}


	public static void insideOutNormals() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			FlipNormalsAction flipNormalsAction = new FlipNormalsAction(modelPanel.getModelView().getSelectedVertices());
			modelPanel.getUndoManager().pushAction(flipNormalsAction.redo());
		}
		ProgramGlobals.getMainPanel().repaint();
	}
}
