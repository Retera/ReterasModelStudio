package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.util.Vec3;

public class StaticMeshScaleAction implements GenericScaleAction {
	private final ModelEditor modelEditor;
	private double scaleX;
	private double scaleY;
	private double scaleZ;
	private final double centerX;
	private final double centerY;
	private final double centerZ;

	public StaticMeshScaleAction(final ModelEditor modelEditor, final double centerX, final double centerY,
	                             final double centerZ) {
		this.modelEditor = modelEditor;
		this.centerX = centerX;
		this.centerY = centerY;
		this.centerZ = centerZ;
		scaleX = 1;
		scaleY = 1;
		scaleZ = 1;
	}

	public StaticMeshScaleAction(final ModelEditor modelEditor, final Vec3 center) {
		this.modelEditor = modelEditor;
		this.centerX = center.x;
		this.centerY = center.y;
		this.centerZ = center.z;
		scaleX = 1;
		scaleY = 1;
		scaleZ = 1;
	}

	@Override
	public void undo() {
		modelEditor.rawScale(centerX, centerY, centerZ, 1 / scaleX, 1 / scaleY, 1 / scaleZ);
	}

	@Override
	public void redo() {
		modelEditor.rawScale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
	}

	@Override
	public String actionName() {
		return "scale";
	}

	@Override
	public void updateScale(final double scaleX, final double scaleY, final double scaleZ) {
		this.scaleX *= scaleX;
		this.scaleY *= scaleY;
		this.scaleZ *= scaleZ;
		modelEditor.rawScale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
	}

	@Override
	public void updateScale(final Vec3 scale) {
		this.scaleX *= scale.x;
		this.scaleY *= scale.y;
		this.scaleZ *= scale.z;
		modelEditor.rawScale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
	}

}
