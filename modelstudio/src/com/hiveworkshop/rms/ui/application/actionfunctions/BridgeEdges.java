package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.BridgeEdgeAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.util.Set;

public class BridgeEdges extends ActionFunction {

	public BridgeEdges() {
		super(TextKey.BRIDGE_EDGES, BridgeEdges::doStuff);
	}

	private static void doStuff(ModelHandler modelHandler) {
		Set<GeosetVertex> selectedVertices = modelHandler.getModelView().getSelectedVertices();
		modelHandler.getUndoManager().pushAction(new BridgeEdgeAction(selectedVertices, ModelStructureChangeListener.changeListener).redo());
	}
}
