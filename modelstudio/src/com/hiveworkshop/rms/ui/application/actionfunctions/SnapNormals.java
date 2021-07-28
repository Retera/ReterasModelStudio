package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.SnapNormalsAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;

public class SnapNormals extends ActionFunction{
	private static double lastNormalMaxAngle = 90;
	private static boolean useTris = false;

	public SnapNormals(){
		super(TextKey.SNAP_VERTICES, () -> snapNormals());
		setKeyStroke(KeyStroke.getKeyStroke("control L"));
	}

	public static void snapNormals() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			ModelView modelView = modelPanel.getModelView();
			SnapNormalsAction snapNormalsAction = new SnapNormalsAction(modelView.getSelectedVertices(), new Vec3(0, 0, 1));
			modelPanel.getUndoManager().pushAction(snapNormalsAction.redo());

		}
		ProgramGlobals.getMainPanel().repaint();
	}
}
