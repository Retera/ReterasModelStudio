package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor;

import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;

public final class StaticMeshMoveAction implements GenericMoveAction {
	private final ModelEditor modelEditor;
	private final Vec3 moveVector;

	public StaticMeshMoveAction(final ModelEditor modelEditor, final Vec3 moveVector) {
		this.modelEditor = modelEditor;
		this.moveVector = new Vec3(moveVector);
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
