package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.editor;

import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class StaticMeshRotateAction implements GenericRotateAction {
	private final ModelEditor modelEditor;
	private final Vertex center;
	private double radians;
	private final byte dim1;
	private final byte dim2;

	public StaticMeshRotateAction(final ModelEditor modelEditor, final Vertex center, final byte dim1,
			final byte dim2) {
		this.modelEditor = modelEditor;
		this.center = center;
		this.radians = 0;
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

	@Override
	public GenericRotateAction updateRotation(final double radians) {
		this.radians += radians;
		modelEditor.rawRotate2d(center.x, center.y, center.z, radians, dim1, dim2);
		return this;
	}

}
