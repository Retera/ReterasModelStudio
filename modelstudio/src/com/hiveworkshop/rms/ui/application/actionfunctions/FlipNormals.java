package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.FlipNormalsAction;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;

public class FlipNormals extends ActionFunction {
	public FlipNormals(){
		super(TextKey.FLIP_NORMALS, FlipNormals::insideOutNormals);
	}


	public static void insideOutNormals(ModelHandler modelHandler) {
		FlipNormalsAction flipNormalsAction = new FlipNormalsAction(modelHandler.getModelView().getSelectedVertices());
		modelHandler.getUndoManager().pushAction(flipNormalsAction.redo());
		ProgramGlobals.getMainPanel().repaint();
	}
}
