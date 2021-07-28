package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.SimplifyGeometryAction2;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

public class SimplifyGeometry extends ActionFunction {
	public SimplifyGeometry(){
		super(TextKey.SIMPLIFY_SELECTED_GEOMETRY, () -> simplifyGeometry());

	}

	public static void simplifyGeometry() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();

		UndoAction action = new SimplifyGeometryAction2(modelPanel.getModelView().getSelectedVertices());
		modelPanel.getUndoManager().pushAction(action.redo());
		ModelStructureChangeListener.changeListener.geosetsUpdated();
	}
}
