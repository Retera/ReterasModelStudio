package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions;

import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexEditor;

public class StaticMeshUVScaleAction implements GenericScaleAction {
	private final TVertexEditor modelEditor;
	private double scaleX;
	private double scaleY;
	private final double centerX;
	private final double centerY;

	public StaticMeshUVScaleAction(final TVertexEditor modelEditor, final double centerX, final double centerY) {
		this.modelEditor = modelEditor;
		this.centerX = centerX;
		this.centerY = centerY;
		this.scaleX = 1;
		this.scaleY = 1;
	}

	@Override
	public void undo() {
		modelEditor.rawScale(centerX, centerY, 1 / scaleX, 1 / scaleY);
	}

	@Override
	public void redo() {
		modelEditor.rawScale(centerX, centerY, scaleX, scaleY);
	}

	@Override
	public String actionName() {
		return "scale";
	}

	@Override
	public void updateScale(final double scaleX, final double scaleY, final double scaleZ) {
		this.scaleX *= scaleX;
		this.scaleY *= scaleY;
		modelEditor.rawScale(centerX, centerY, scaleX, scaleY);
	}

}
