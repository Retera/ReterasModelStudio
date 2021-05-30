package com.hiveworkshop.rms.ui.gui.modeledit.creator.actions;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.Collections;
import java.util.List;

public class NewGeosetAction implements UndoAction {
	private final Geoset geoset;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final List<Geoset> geosetAsList;
	private final ModelView modelView;

	public NewGeosetAction(Geoset geoset,
	                       ModelView modelView,
	                       ModelStructureChangeListener modelStructureChangeListener) {
		this.geoset = geoset;
		this.modelView = modelView;
		this.modelStructureChangeListener = modelStructureChangeListener;
		geosetAsList = Collections.singletonList(geoset);
	}

	@Override
	public UndoAction undo() {
		modelView.getModel().remove(geoset);
		modelView.updateElements();
		modelStructureChangeListener.geosetsUpdated();
		return this;
	}

	@Override
	public UndoAction redo() {
		modelView.getModel().add(geoset);
		modelView.updateElements();
		modelStructureChangeListener.geosetsUpdated();
		return this;
	}

	@Override
	public String actionName() {
		return "create geoset";
	}

}
