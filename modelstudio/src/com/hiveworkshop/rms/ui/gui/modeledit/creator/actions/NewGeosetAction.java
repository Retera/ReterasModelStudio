package com.hiveworkshop.rms.ui.gui.modeledit.creator.actions;

import java.util.Collections;
import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.EditableModel;

public class NewGeosetAction implements UndoAction {
	private final Geoset geoset;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final List<Geoset> geosetAsList;
	private final EditableModel model;

	public NewGeosetAction(final Geoset geoset, final EditableModel model,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.geoset = geoset;
		this.model = model;
		this.modelStructureChangeListener = modelStructureChangeListener;
		geosetAsList = Collections.singletonList(geoset);
	}

	@Override
	public void undo() {
		model.remove(geoset);
		modelStructureChangeListener.geosetsRemoved(geosetAsList);
	}

	@Override
	public void redo() {
		model.add(geoset);
		modelStructureChangeListener.geosetsAdded(geosetAsList);
	}

	@Override
	public String actionName() {
		return "create geoset";
	}

}
