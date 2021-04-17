package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor;

import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public final class SimpleRotateAction implements UndoAction {
	private final ModelEditor modelEditor;
	private final Vec3 center;
	private final double radians;
	private final byte dim1;
	private final byte dim2;

	public SimpleRotateAction(final ModelEditor modelEditor, final Vec3 center, final double radians, final byte dim1,
			final byte dim2) {
		this.modelEditor = modelEditor;
		this.center = center;
		this.radians = radians;
		this.dim1 = dim1;
		this.dim2 = dim2;
	}

	@Override
	public void undo() {
		modelEditor.rawRotate2d(center.x, center.y, center.z, -radians, dim1, dim2);
	}

	@Override
	public void redo() {
		modelEditor.rawRotate2d(center.x, center.y, center.z, radians, dim1, dim2);
	}

	@Override
	public String actionName() {
		return "rotate";
	}

}
