package com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.actions;

import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.wc3.mdl.TVertex;

public final class StaticMeshUVRotateAction implements GenericRotateAction {
	private final TVertexEditor modelEditor;
	private final TVertex center;
	private double radians;

	public StaticMeshUVRotateAction(final TVertexEditor modelEditor, final TVertex center) {
		this.modelEditor = modelEditor;
		this.center = center;
		this.radians = 0;
	}

	@Override
	public void undo() {
		modelEditor.rawRotate2d(center.x, center.y, -radians);
	}

	@Override
	public void redo() {
		modelEditor.rawRotate2d(center.x, center.y, radians);
	}

	@Override
	public String actionName() {
		return "rotate";
	}

	@Override
	public void updateRotation(final double radians) {
		this.radians += radians;
		modelEditor.rawRotate2d(center.x, center.y, radians);
	}

}
