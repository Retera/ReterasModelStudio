package com.hiveworkshop.rms.editor.actions.addactions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.Collections;
import java.util.List;

public class NewGeosetAction implements UndoAction {
	private final Geoset geoset;
	private final ModelStructureChangeListener changeListener;
	private final List<Geoset> geosetAsList;
	private final ModelView modelView;

	public NewGeosetAction(Geoset geoset,
	                       ModelView modelView,
	                       ModelStructureChangeListener changeListener) {
		this.geoset = geoset;
		this.modelView = modelView;
		this.changeListener = changeListener;
		geosetAsList = Collections.singletonList(geoset);
	}

	@Override
	public UndoAction undo() {
		modelView.getModel().remove(geoset);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		modelView.getModel().add(geoset);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "create geoset";
	}

}
