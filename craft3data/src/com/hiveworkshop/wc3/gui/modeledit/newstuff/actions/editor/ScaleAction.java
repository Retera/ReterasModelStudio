package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.editor;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.mdl.Vertex;

public class ScaleAction implements UndoAction {
	private final ModelEditor modelEditor;
	private final double scaleX;
	private final double scaleY;
	private final double scaleZ;
	private final Vertex center;

	public ScaleAction(final ModelEditor modelEditor, final Vertex center, final double scaleX, final double scaleY,
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
