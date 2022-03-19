package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.WeldVertsAction;
import com.hiveworkshop.rms.editor.actions.selection.AddSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.util.Set;

public class WeldVerts extends ActionFunction {
	public WeldVerts(){
		super(TextKey.WELD_VERTICES, WeldVerts::doWeld);
	}

	public static void doWeld(ModelHandler modelHandler) {
		ModelView modelView = modelHandler.getModelView();
		Set<GeosetVertex> selectedVertices = modelView.getSelectedVertices();
		UndoAction selectionAction = new AddSelectionUggAction(selectedVertices, modelView, null); // to get back selection when undoing
		UndoAction action = new WeldVertsAction(selectedVertices, 1f, ModelStructureChangeListener.changeListener);
		modelHandler.getUndoManager().pushAction(new CompoundAction("Weld Vertices", null, selectionAction, action).redo());
	}
}
