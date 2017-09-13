package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.editor;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class RotateAction implements UndoAction {
	private final ModelEditor modelEditor;
	private final Vertex center;
	private final double radians;
	private final byte dim1;
	private final byte dim2;

	public RotateAction(final ModelEditor modelEditor, final Vertex center, final double radians, final byte dim1,
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
