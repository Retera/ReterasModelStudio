package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.ArrayList;
import java.util.List;

public class ShowVerticesAction implements UndoAction {
	private final ModelView modelView;
	private final ModelStructureChangeListener changeListener;
	private final List<GeosetVertex> hiddenVerts;

	public ShowVerticesAction(ModelView modelView, ModelStructureChangeListener changeListener) {
		this.modelView = modelView;
		this.changeListener = changeListener;
		hiddenVerts = new ArrayList<>(modelView.getHiddenVertices());
	}

	@Override
	public UndoAction undo() {
		modelView.hideVertices(hiddenVerts);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		modelView.unHideAllVertices();
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Show Hidden Vertices";
	}
}
