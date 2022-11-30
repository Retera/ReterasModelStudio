package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Vec3;

public class SetGeosetStaticColorAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Geoset geoset;
	private final Vec3 color;
	private final Vec3 oldColor;

	public SetGeosetStaticColorAction(Geoset geoset, Vec3 color, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.geoset = geoset;
		this.oldColor = new Vec3(geoset.getStaticColor());
		this.color = color;
	}

	@Override
	public UndoAction redo() {
		geoset.setStaticColor(color);
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		geoset.setStaticColor(oldColor);
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Set Geoset Color";
	}
}
