package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.SnapAction;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;

public class SnapVertices extends ActionFunction{

	public SnapVertices(){
		super(TextKey.SNAP_VERTICES, SnapVertices::snapVertices);
		setKeyStroke(KeyStroke.getKeyStroke("control shift W"));
	}

	public static void snapVertices(ModelHandler modelHandler) {

		SnapAction snapAction = new SnapAction(modelHandler.getModelView().getSelectedVertices());
		modelHandler.getUndoManager().pushAction(snapAction.redo());
		ProgramGlobals.getMainPanel().repaint();
	}
}
