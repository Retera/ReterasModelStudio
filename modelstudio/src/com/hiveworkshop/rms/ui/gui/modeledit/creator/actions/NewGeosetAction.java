package com.hiveworkshop.rms.ui.gui.modeledit.creator.actions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.Collections;
import java.util.List;

public class NewGeosetAction implements UndoAction {
	private final Geoset geoset;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final List<Geoset> geosetAsList;
	private final EditableModel model;

	public NewGeosetAction(Geoset geoset,
	                       EditableModel model,
	                       ModelStructureChangeListener modelStructureChangeListener) {
		this.geoset = geoset;
		this.model = model;
		this.modelStructureChangeListener = modelStructureChangeListener;
		geosetAsList = Collections.singletonList(geoset);
	}

	@Override
	public UndoAction undo() {
		model.remove(geoset);
		modelStructureChangeListener.geosetsUpdated();
		return this;
	}

	@Override
	public UndoAction redo() {
		model.add(geoset);
		modelStructureChangeListener.geosetsUpdated();
		return this;
	}

	@Override
	public String actionName() {
		return "create geoset";
	}

}
