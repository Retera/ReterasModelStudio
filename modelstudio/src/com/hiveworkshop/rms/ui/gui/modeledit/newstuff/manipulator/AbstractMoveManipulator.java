package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.util.Vertex3;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;

import java.awt.geom.Point2D.Double;

public abstract class AbstractMoveManipulator extends AbstractManipulator {
	protected final ModelEditor modelEditor;
	protected final Vertex3 moveVector;
	private GenericMoveAction translationAction;

	public AbstractMoveManipulator(final ModelEditor modelEditor) {
		this.modelEditor = modelEditor;
		moveVector = new Vertex3(0, 0, 0);
	}

	@Override
	protected void onStart(final Double mouseStart, final byte dim1, final byte dim2) {
		super.onStart(mouseStart, dim1, dim2);
		translationAction = modelEditor.beginTranslation();
	}

	@Override
	public void update(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		resetMoveVector();
		buildMoveVector(mouseStart, mouseEnd, dim1, dim2);
		translationAction.updateTranslation(moveVector.x, moveVector.y, moveVector.z);
	}

	@Override
	public UndoAction finish(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		update(mouseStart, mouseEnd, dim1, dim2);
		resetMoveVector();
		return translationAction;
	}

	protected abstract void buildMoveVector(final Double mouseStart, final Double mouseEnd, final byte dim1,
			final byte dim2);

	private void resetMoveVector() {
		moveVector.x = 0;
		moveVector.y = 0;
		moveVector.z = 0;
	}
}
