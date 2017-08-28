package com.hiveworkshop.wc3.gui.modeledit.manipulator.actions;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.ModelEditor;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class MoveAction implements UndoAction {
	private final ModelEditor modelEditor;
	private final Vertex moveVector;

	public MoveAction(final ModelEditor modelEditor, final Vertex moveVector) {
		this.modelEditor = modelEditor;
		this.moveVector = new Vertex(moveVector);
	}

	@Override
	public void undo() {
		modelEditor.translate(-moveVector.x, -moveVector.y, -moveVector.z);
	}

	@Override
	public void redo() {
		modelEditor.translate(moveVector.x, moveVector.y, moveVector.z);
	}

	@Override
	public String actionName() {
		return "move";
	}

}
