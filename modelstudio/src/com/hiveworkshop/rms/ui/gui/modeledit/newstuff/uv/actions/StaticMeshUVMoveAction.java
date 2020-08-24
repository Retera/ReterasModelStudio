package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.rms.editor.model.TVertex;

public final class StaticMeshUVMoveAction implements UndoAction, GenericMoveAction {
	private final TVertexEditor modelEditor;
	private final TVertex moveVector;

	public StaticMeshUVMoveAction(final TVertexEditor modelEditor, final TVertex moveVector) {
		this.modelEditor = modelEditor;
		this.moveVector = new TVertex(moveVector);
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
