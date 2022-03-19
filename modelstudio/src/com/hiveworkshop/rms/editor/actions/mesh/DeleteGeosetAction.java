package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

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
	private final ModelStructureChangeListener changeListener;

	public DeleteGeosetAction(EditableModel model, Geoset geoset, ModelStructureChangeListener changeListener) {
		this.geosets = Collections.singletonList(geoset);
		this.model = model;
		geosetAnims = Collections.singletonList(geoset.getGeosetAnim());
		this.changeListener = changeListener;
	}

	public DeleteGeosetAction(EditableModel model, List<Geoset> geosets, ModelStructureChangeListener changeListener) {
		this.geosets = geosets;
		this.model = model;
		geosetAnims = new ArrayList<>();
		for (Geoset geoset : geosets) {
			geosetAnims.add(geoset.getGeosetAnim());
		}
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction redo() {
		for (Geoset geoset : geosets) {
			model.remove(geoset);
		}
		for (GeosetAnim geosetAnim : geosetAnims) {
			if (geosetAnim != null) {
				model.remove(geosetAnim);
			}
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		for (Geoset geoset : geosets) {
			model.add(geoset);
		}
		for (GeosetAnim geosetAnim : geosetAnims) {
			if (geosetAnim != null) {
				model.add(geosetAnim);
			}
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "delete vertices";
	}
}
