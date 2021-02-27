package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv;

import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;

import java.awt.geom.Point2D.Double;

public final class MoveXTVertexManipulator extends AbstractMoveTVertexManipulator {

	public MoveXTVertexManipulator(final TVertexEditor modelEditor) {
		super(modelEditor);
	}

	@Override
	protected void buildMoveVector(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		moveVector.setCoord(dim1, mouseEnd.x - mouseStart.x);
	}

}
