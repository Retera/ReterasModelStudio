package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D.Double;
import java.util.Arrays;

public final class ExtendManipulator extends AbstractMoveManipulator {

	private UndoAction extrudeAction;

	public ExtendManipulator(final ModelEditor modelEditor, MoveDimension dir) {
		super(modelEditor, dir);
	}

	@Override
	protected void onStart(MouseEvent e, final Double mouseStart, final byte dim1, final byte dim2) {
		super.onStart(e, mouseStart, dim1, dim2);
		extrudeAction = modelEditor.beginExtendingSelection();
	}

	@Override
	public UndoAction finish(MouseEvent e, final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		return new CompoundAction("extend", Arrays.asList(extrudeAction, super.finish(e, mouseStart, mouseEnd, dim1, dim2)));
	}

//	@Override
//	protected void buildMoveVector(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
//		moveVector.setCoord(dim1, mouseEnd.x - mouseStart.x);
//		moveVector.setCoord(dim2, mouseEnd.y - mouseStart.y);
//	}

	@Override
	protected void buildMoveVector(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		if (dir.containDirection(dim1)) {
			moveVector.setCoord(dim1, mouseEnd.x - mouseStart.x);
		}
		if (dir.containDirection(dim2)) {
			moveVector.setCoord(dim2, mouseEnd.y - mouseStart.y);
		}
	}

}
