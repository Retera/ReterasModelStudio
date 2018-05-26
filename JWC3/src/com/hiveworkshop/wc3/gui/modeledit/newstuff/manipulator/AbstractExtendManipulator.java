package com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator;

import java.awt.geom.Point2D.Double;

import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.CompoundAction;

public abstract class AbstractExtendManipulator extends AbstractMoveManipulator {

	private UndoAction extrudeAction;

	public AbstractExtendManipulator(final ModelEditor modelEditor) {
		super(modelEditor);
	}

	@Override
	protected void onStart(final Double mouseStart, final byte dim1, final byte dim2) {
		super.onStart(mouseStart, dim1, dim2);
		extrudeAction = modelEditor.beginExtendingSelection();
	}

	@Override
	public UndoAction finish(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		return new CompoundAction("extend",
				ListView.Util.of(extrudeAction, super.finish(mouseStart, mouseEnd, dim1, dim2)));
	}

	@Override
	protected void buildMoveVector(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		moveVector.setCoord(dim1, mouseEnd.x - mouseStart.x);
		moveVector.setCoord(dim2, mouseEnd.y - mouseStart.y);
	}

}
