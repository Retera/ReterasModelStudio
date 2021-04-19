package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Something to undo when you deleted something important.
 *
 * Eric Theller 6/11/2012
 */
public class DeleteGeosetAction implements UndoAction {
	private final List<Geoset> geosets;
	private final EditableModel model;
	private final List<GeosetAnim> geosetAnims;
	ModelStructureChangeListener modelStructureChangeListener;

	public DeleteGeosetAction(final Geoset geoset, ModelStructureChangeListener modelStructureChangeListener) {
		this.geosets = Collections.singletonList(geoset);
		model = geoset.getParentModel();
		geosetAnims = Collections.singletonList(geoset.getGeosetAnim());
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	public DeleteGeosetAction(List<Geoset> geosets, ModelStructureChangeListener modelStructureChangeListener) {
		this.geosets = geosets;
		model = geosets.get(0).getParentModel();
		geosetAnims = new ArrayList<>();
		for (Geoset geoset : geosets) {
			geosetAnims.add(geoset.getGeosetAnim());
		}
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void redo() {
		for (Geoset geoset : geosets) {
			model.remove(geoset);
		}
		for (GeosetAnim geosetAnim : geosetAnims) {
			if (geosetAnim != null) {
				model.remove(geosetAnim);
			}
		}
		modelStructureChangeListener.geosetsRemoved(geosets);
	}

	@Override
	public void undo() {
		for (Geoset geoset : geosets) {
			model.add(geoset);
		}
		for (GeosetAnim geosetAnim : geosetAnims) {
			if (geosetAnim != null) {
				model.add(geosetAnim);
			}
		}
		modelStructureChangeListener.geosetsAdded(geosets);
	}

	@Override
	public String actionName() {
		return "delete vertices";
	}
}
