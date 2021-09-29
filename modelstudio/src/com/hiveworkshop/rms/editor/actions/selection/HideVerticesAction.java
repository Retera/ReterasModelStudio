package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.ArrayList;
import java.util.List;

public class HideVerticesAction implements UndoAction {
	private final ModelView modelView;
	private final ModelStructureChangeListener changeListener;
	private final List<GeosetVertex> vertsToHide;

	public HideVerticesAction(ModelView modelView, ModelStructureChangeListener changeListener) {
		this.modelView = modelView;
		this.changeListener = changeListener;
		vertsToHide = new ArrayList<>(modelView.getSelectedVertices());
	}

	@Override
	public UndoAction undo() {
		modelView.showVertices(vertsToHide);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		modelView.hideVertices(vertsToHide);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Make Vertices Visible";
	}
}
