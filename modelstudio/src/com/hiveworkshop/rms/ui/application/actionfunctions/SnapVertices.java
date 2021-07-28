package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.SnapAction;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;

public class SnapVertices extends ActionFunction{
	private static double lastNormalMaxAngle = 90;
	private static boolean useTris = false;

	public SnapVertices(){
		super(TextKey.SNAP_VERTICES, () -> snapVertices());
		setKeyStroke(KeyStroke.getKeyStroke("control shift W"));
	}

	public static void snapVertices() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			SnapAction snapAction = new SnapAction(modelPanel.getModelView().getSelectedVertices());
			snapAction.redo();
			modelPanel.getUndoManager().pushAction(snapAction);
		}
		ProgramGlobals.getMainPanel().repaint();
	}
}
