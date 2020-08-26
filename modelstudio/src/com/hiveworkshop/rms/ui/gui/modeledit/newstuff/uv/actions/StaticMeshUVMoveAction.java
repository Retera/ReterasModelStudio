package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.rms.util.Vertex2;

public final class StaticMeshUVMoveAction implements UndoAction, GenericMoveAction {
	private final TVertexEditor modelEditor;
	private final Vertex2 moveVector;

	public StaticMeshUVMoveAction(final TVertexEditor modelEditor, final Vertex2 moveVector) {
		this.modelEditor = modelEditor;
		this.moveVector = new Vertex2(moveVector);
	}

	@Override
	public void undo() {
		modelEditor.rawTranslate(-moveVector.x, -moveVector.y);
	}

	@Override
	public void redo() {
		modelEditor.rawTranslate(moveVector.x, moveVector.y);
	}

	@Override
	public String actionName() {
		return "move UV";
	}

	@Override
	public void updateTranslation(final double deltaX, final double deltaY, final double deltaZ) {
		moveVector.x += deltaX;
		moveVector.y += deltaY;
		modelEditor.rawTranslate(deltaX, deltaY);
	}

}
