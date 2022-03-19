package com.hiveworkshop.rms.editor.actions.uv;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Vec2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class MirrorTVerticesAction implements UndoAction {
	private final List<Vec2> selection;
	private final String mirrorDim;
	private final Vec2 flipAxis;
	private final Vec2 center;
	private final ModelStructureChangeListener changeListener;

// flipAxis - the axis which values to affect
	public MirrorTVerticesAction(Collection<Vec2> selection, Vec2 center, Vec2 flipAxis,
	                             ModelStructureChangeListener changeListener) {
		this.center = new Vec2(center);
		this.selection = new ArrayList<>(selection);
		this.flipAxis = new Vec2(flipAxis);
		this.changeListener = changeListener;
		mirrorDim = flipAxis.x == 1 ? "X" : "Y";
	}

	@Override
	public UndoAction undo() {
		doMirror();

		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		doMirror();

		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	private void doMirror() {
		Vec2 keepAxis = new Vec2(1,1).sub(flipAxis);
		final Vec2 center = new Vec2(this.center).mul(flipAxis).scale(2);
		Vec2 tempFlipAxis = new Vec2(flipAxis).scale(-1).add(keepAxis);
		for (Vec2 vert : selection) {
			vert.mul(tempFlipAxis).add(center);
		}
	}

	@Override
	public String actionName() {
		return "Mirror UV " + mirrorDim;
	}

}
