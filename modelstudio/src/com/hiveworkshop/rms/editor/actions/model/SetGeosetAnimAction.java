package com.hiveworkshop.rms.editor.actions.model;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetGeosetAnimAction implements UndoAction {
	private final Geoset geoset;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;
	private final GeosetAnim geosetAnim;
	private final GeosetAnim oldGeosetAnim;


	public SetGeosetAnimAction(EditableModel model, Geoset geoset, ModelStructureChangeListener changeListener) {
		this.geoset = geoset;
		this.model = model;
		this.changeListener = changeListener;
		oldGeosetAnim = geoset.getGeosetAnim();
		geosetAnim = new GeosetAnim(geoset);
	}

	public SetGeosetAnimAction(EditableModel model, Geoset geoset, GeosetAnim geosetAnim, ModelStructureChangeListener changeListener) {
		this.geoset = geoset;
		this.model = model;
		this.changeListener = changeListener;
		oldGeosetAnim = geoset.getGeosetAnim();
		this.geosetAnim = geosetAnim;
	}

	@Override
	public UndoAction undo() {
		geoset.setGeosetAnim(oldGeosetAnim);
		model.remove(geosetAnim);
		if (oldGeosetAnim != null) {
			model.add(oldGeosetAnim);
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		geoset.setGeosetAnim(geosetAnim);
		if (oldGeosetAnim != null) {
			model.remove(oldGeosetAnim);
		}
		model.add(geosetAnim);
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Add GeosetAnim";
	}
}
