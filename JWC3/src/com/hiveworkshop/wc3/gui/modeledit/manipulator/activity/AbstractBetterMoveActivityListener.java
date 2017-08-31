package com.hiveworkshop.wc3.gui.modeledit.manipulator.activity;

import java.awt.geom.Point2D.Double;

import com.hiveworkshop.wc3.gui.modeledit.manipulator.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.actions.MoveAction;
import com.hiveworkshop.wc3.gui.modeledit.useractions.UndoActionListener;
import com.hiveworkshop.wc3.mdl.Vertex;

public abstract class AbstractBetterMoveActivityListener extends AbstractBetterActivityListener {
	private final ModelEditor modelEditor;
	private final UndoActionListener undoManager;
	protected final Vertex moveVector;

	public AbstractBetterMoveActivityListener(final ModelEditor modelEditor, final UndoActionListener undoManager) {
		this.modelEditor = modelEditor;
		this.undoManager = undoManager;
		moveVector = new Vertex(0, 0, 0);
	}

	@Override
	public void update(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		resetMoveVector();
		buildMoveVector(mouseStart, mouseEnd, dim1, dim2);
		modelEditor.translate(moveVector.x, moveVector.y, moveVector.z);
	}

	@Override
	public void finish(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		update(mouseStart, mouseEnd, dim1, dim2);
		resetMoveVector();
		buildMoveVector(activityStart, mouseEnd, dim1, dim2);
		undoManager.pushAction(new MoveAction(modelEditor, moveVector));
	}

	protected abstract void buildMoveVector(final Double mouseStart, final Double mouseEnd, final byte dim1,
			final byte dim2);

	private void resetMoveVector() {
		moveVector.x = 0;
		moveVector.y = 0;
		moveVector.z = 0;
	}
}
