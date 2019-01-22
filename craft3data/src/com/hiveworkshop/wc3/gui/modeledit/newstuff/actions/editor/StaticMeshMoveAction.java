package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.editor;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class StaticMeshMoveAction implements UndoAction, GenericMoveAction {
	private final ModelEditor modelEditor;
	private final Vertex moveVector;

	public StaticMeshMoveAction(final ModelEditor modelEditor, final Vertex moveVector) {
		this.modelEditor = modelEditor;
		this.moveVector = new Vertex(moveVector);
	}

	@Override
	public void undo() {
		modelEditor.rawTranslate(-moveVector.x, -moveVector.y, -moveVector.z);
	}

	@Override
	public void redo() {
		modelEditor.rawTranslate(moveVector.x, moveVector.y, moveVector.z);
	}

	@Override
	public String actionName() {
		return "move";
	}

	@Override
	public void updateTranslation(final double deltaX, final double deltaY, final double deltaZ) {
		moveVector.x += deltaX;
		moveVector.y += deltaY;
		moveVector.z += deltaZ;
		modelEditor.rawTranslate(deltaX, deltaY, deltaZ);
	}

}
