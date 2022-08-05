package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetGeosetAnimStaticAlphaAction implements UndoAction {
	private final GeosetAnim geosetAnim;
	private final double alpha;
	private final double oldAlpha;
	private final ModelStructureChangeListener changeListener;

	public SetGeosetAnimStaticAlphaAction(GeosetAnim geosetAnim, double alpha, ModelStructureChangeListener changeListener) {
		this.geosetAnim = geosetAnim;
		oldAlpha = geosetAnim.getStaticAlpha();
		this.alpha = alpha;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction redo() {
		geosetAnim.setStaticAlpha(alpha);
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		geosetAnim.setStaticAlpha(oldAlpha);
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Set GeosetAnim Alpha";
	}
}
