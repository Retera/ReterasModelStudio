package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetGeosetStaticAlphaAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Geoset geoset;
	private final double alpha;
	private final double oldAlpha;

	public SetGeosetStaticAlphaAction(Geoset geoset, double alpha, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.geoset = geoset;
		this.oldAlpha = geoset.getStaticAlpha();
		this.alpha = alpha;
	}

	@Override
	public UndoAction redo() {
		geoset.setStaticAlpha(alpha);
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		geoset.setStaticAlpha(oldAlpha);
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Set Geoset Alpha";
	}
}
