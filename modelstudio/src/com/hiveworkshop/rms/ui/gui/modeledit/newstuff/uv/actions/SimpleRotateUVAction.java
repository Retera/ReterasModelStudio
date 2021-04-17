package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions;

import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec2;

public final class SimpleRotateUVAction implements UndoAction {
	private final TVertexEditor modelEditor;
	private final Vec2 center;
	private final double radians;

	public SimpleRotateUVAction(final TVertexEditor modelEditor, final Vec2 center, final double radians) {
		this.modelEditor = modelEditor;
		this.center = center;
		this.radians = radians;
	}

	@Override
	public void undo() {
		modelEditor.rawRotate2d(center.x, center.y, -radians, (byte) 0, (byte) 1);
	}

	@Override
	public void redo() {
		modelEditor.rawRotate2d(center.x, center.y, radians, (byte) 0, (byte) 1);
	}

	@Override
	public String actionName() {
		return "rotate UV";
	}

}
