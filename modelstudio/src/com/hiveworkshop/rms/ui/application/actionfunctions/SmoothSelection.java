package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.SmoothVertsAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

public class SmoothSelection extends ActionFunction {
	public SmoothSelection(){
		super(TextKey.SMOOTH_VERTS, () -> smoothSelection());
	}

	public static void smoothSelection() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if(modelPanel != null){
			ModelView modelView = modelPanel.getModelView();
			SmoothVertsAction action = new SmoothVertsAction(modelView.getSelectedVertices(), false, 0.5f);
			modelPanel.getUndoManager().pushAction(action.redo());
		}
	}
}
