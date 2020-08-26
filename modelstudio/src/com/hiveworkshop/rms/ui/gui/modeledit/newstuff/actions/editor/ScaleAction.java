package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor;

import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class ScaleAction implements UndoAction {
	private final ModelEditor modelEditor;
	private final double scaleX;
	private final double scaleY;
	private final double scaleZ;
	private final Vec3 center;

	public ScaleAction(final ModelEditor modelEditor, final Vec3 center, final double scaleX, final double scaleY,
			final double scaleZ) {
		this.modelEditor = modelEditor;
		this.center = center;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.scaleZ = scaleZ;
	}

	@Override
	public void undo() {
		modelEditor.rawScale(center.x, center.y, center.z, 1 / scaleX, 1 / scaleY, 1 / scaleZ);
	}

	@Override
	public void redo() {
		modelEditor.rawScale(center.x, center.y, center.z, scaleX, scaleY, scaleZ);
	}

	@Override
	public String actionName() {
		return "scale";
	}

}
