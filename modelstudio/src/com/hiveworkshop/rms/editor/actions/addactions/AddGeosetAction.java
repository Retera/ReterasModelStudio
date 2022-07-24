package com.hiveworkshop.rms.editor.actions.addactions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class AddGeosetAction implements UndoAction {
	private final Geoset geoset;
	private final GeosetAnim geosetAnim;
	private final ModelStructureChangeListener changeListener;
	private final EditableModel model;

	public AddGeosetAction(Geoset geoset, ModelView modelView, ModelStructureChangeListener changeListener) {
		this.geoset = geoset;
		this.geosetAnim = geoset.getGeosetAnim();
		this.model = modelView.getModel();
		this.changeListener = changeListener;
	}

	public AddGeosetAction(Geoset geoset, EditableModel model, ModelStructureChangeListener changeListener) {
		this.geoset = geoset;
		this.geosetAnim = geoset.getGeosetAnim();
		this.model = model;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		model.remove(geoset);
		model.remove(geosetAnim);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.add(geoset);
		if(geosetAnim != null){
			model.add(geosetAnim);
		}
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
