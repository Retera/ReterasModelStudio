package com.hiveworkshop.wc3.gui.modeledit.creator.actions;

import java.util.Arrays;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.MDL;

public class NewGeosetAction implements UndoAction {
	private final Geoset geoset;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final List<Geoset> geosetAsList;
	private final MDL model;

	public NewGeosetAction(final Geoset geoset, final MDL model,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.geoset = geoset;
		this.model = model;
		this.modelStructureChangeListener = modelStructureChangeListener;
		geosetAsList = Arrays.asList(geoset);
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
