package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.util.Vec3;

public final class StaticMeshRotateAction implements GenericRotateAction {
	private final ModelView modelView;
	private final Vec3 center;
	private double radians;
	private final byte dim1;
	private final byte dim2;

	public StaticMeshRotateAction(ModelView modelView, Vec3 center, byte dim1, byte dim2) {
		this.modelView = modelView;
		this.center = center;
		radians = 0;
		this.dim1 = dim1;
		this.dim2 = dim2;
	}

	@Override
	public void undo() {
		for (Vec3 vertex : modelView.getSelectedVertices()) {
			vertex.rotate(center, -radians, dim1, dim2);
		}
	}

	@Override
	public void redo() {
		for (Vec3 vertex : modelView.getSelectedVertices()) {
			vertex.rotate(center, radians, dim1, dim2);
		}
	}

	@Override
	public String actionName() {
		return "rotate";
	}

	@Override
	public void updateRotation(double radians) {
		this.radians += radians;
		for (Vec3 vertex : modelView.getSelectedVertices()) {
			vertex.rotate(center, radians, dim1, dim2);
		}
	}

}
