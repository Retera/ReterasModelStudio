package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.SimplifyGeometryAction;
import com.hiveworkshop.rms.editor.actions.selection.AddSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.util.Set;

public class SimplifyGeometry extends ActionFunction {
	public SimplifyGeometry(){
		super(TextKey.SIMPLIFY_SELECTED_GEOMETRY, SimplifyGeometry::simplifyGeometry);
	}

	public static void simplifyGeometry(ModelHandler modelHandler) {
		ModelView modelView = modelHandler.getModelView();
		Set<GeosetVertex> selectedVertices = modelView.getSelectedVertices();
		UndoAction selectionAction = new AddSelectionUggAction(selectedVertices, modelView, null); // to get back selection when undoing
		UndoAction action = new SimplifyGeometryAction(selectedVertices, ModelStructureChangeListener.changeListener);
		modelHandler.getUndoManager().pushAction(new CompoundAction("Simplify Geometry", null, selectionAction, action).redo());
	}
}
