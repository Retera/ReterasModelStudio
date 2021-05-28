package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

public final class SimpleRotateAction implements UndoAction {
	private final ModelView modelView;
	private final Vec3 center;
	private final double radians;
	private final byte dim1;
	private final byte dim2;

	public SimpleRotateAction(ModelView modelView, Vec3 center, double radians, byte dim1, byte dim2) {
		this.modelView = modelView;
		this.center = center;
		this.radians = radians;
		this.dim1 = dim1;
		this.dim2 = dim2;
	}

	@Override
	public UndoAction undo() {
		for (Vec3 vertex : modelView.getSelectedVertices()) {
			vertex.rotate(center, -radians, dim1, dim2);
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Vec3 vertex : modelView.getSelectedVertices()) {
			vertex.rotate(center, radians, dim1, dim2);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "rotate";
	}

}
