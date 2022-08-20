package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Vec3;

public class SetGeosetAnimStaticColorAction implements UndoAction {
	private final GeosetAnim geosetAnim;
	private final Vec3 color;
	private final Vec3 oldColor;
	private final ModelStructureChangeListener changeListener;

	public SetGeosetAnimStaticColorAction(GeosetAnim geosetAnim, Vec3 color, ModelStructureChangeListener changeListener) {
		this.geosetAnim = geosetAnim;
		oldColor = geosetAnim.getStaticColor();
		this.color = color;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction redo() {
		geosetAnim.setStaticColor(color);
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		geosetAnim.setStaticColor(oldColor);
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Set GeosetAnim Color";
	}
}
