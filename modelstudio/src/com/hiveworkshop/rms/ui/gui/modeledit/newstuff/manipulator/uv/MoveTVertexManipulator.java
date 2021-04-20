package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv;

import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.Vec2;

public final class MoveTVertexManipulator extends AbstractMoveTVertexManipulator {

	public MoveTVertexManipulator(TVertexEditor modelEditor, MoveDimension dir) {
		super(modelEditor, dir);
	}

	@Override
	protected void buildMoveVector(Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		if (dir.containDirection(dim1)) {
			moveVector.setCoord(dim1, mouseEnd.x - mouseStart.x);
		}
		if (dir.containDirection(dim2)) {
			moveVector.setCoord(dim2, mouseEnd.y - mouseStart.y);
		}
	}

}
