package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.SnapNormalsAction;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;

public class SnapNormals extends ActionFunction{

	public SnapNormals(){
		super(TextKey.SNAP_NORMALS, SnapNormals::snapNormals);
		setKeyStroke(KeyStroke.getKeyStroke("control shift L"));
	}

	public static void snapNormals(ModelHandler modelHandler) {
		SnapNormalsAction snapNormalsAction = new SnapNormalsAction(modelHandler.getModelView().getSelectedVertices(), new Vec3(0, 0, 1));
		modelHandler.getUndoManager().pushAction(snapNormalsAction.redo());
		ProgramGlobals.getMainPanel().repaint();
	}
}
