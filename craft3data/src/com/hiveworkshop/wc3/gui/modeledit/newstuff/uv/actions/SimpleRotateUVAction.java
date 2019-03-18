package com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.actions;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.wc3.mdl.TVertex;

public final class SimpleRotateUVAction implements UndoAction {
	private final TVertexEditor modelEditor;
	private final TVertex center;
	private final double radians;

	public SimpleRotateUVAction(final TVertexEditor modelEditor, final TVertex center, final double radians) {
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
