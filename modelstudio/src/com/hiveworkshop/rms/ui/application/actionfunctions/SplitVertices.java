package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.SplitVertexAction;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;

public class SplitVertices extends ActionFunction{

	public SplitVertices(){
		super(TextKey.SPLIT_VERTICES, SplitVertices::snapVertices);
	}

	public static void snapVertices(ModelHandler modelHandler) {
		SplitVertexAction splitAction = new SplitVertexAction(modelHandler.getModelView().getSelectedVertices(), ModelStructureChangeListener.changeListener);
		modelHandler.getUndoManager().pushAction(splitAction.redo());
		ProgramGlobals.getMainPanel().repaint();
	}
}
