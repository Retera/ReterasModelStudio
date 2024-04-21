package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.util.Vec3;

public class ScaleExtentAction extends AbstractTransformAction {
	private final ExtLog extLog;
	private final ExtLog oldLog;
	private final Vec3 center;
	private final Vec3 scale;

	public ScaleExtentAction(ExtLog extLog, Vec3 center, Vec3 scale) {
		this.extLog = extLog;
		this.oldLog = extLog.deepCopy();
		this.center = center;
		this.scale = scale;
	}


	@Override
	public ScaleExtentAction undo() {
		extLog.set(oldLog);
		return this;
	}

	@Override
	public ScaleExtentAction redo() {
		return this;
	}


	@Override
	public ScaleExtentAction updateScale(Vec3 scale) {
		this.scale.multiply(scale);
		resetScale();
		doScale(center, this.scale);
		return this;
	}
	@Override
	public ScaleExtentAction setScale(Vec3 scale) {
		this.scale.set(scale);
		resetScale();
		doScale(center, scale);
		return this;
	}

	private void resetScale() {
		extLog.set(oldLog);
	}

	private void doScale(Vec3 center, Vec3 scale) {
		extLog.getMinimumExtent().scale(center, scale);
		extLog.getMaximumExtent().scale(center, scale);
		double avgScale = (scale.x + scale.y + scale.z) / 3;
		extLog.setBoundsRadius(extLog.getBoundsRadius() * avgScale);
	}

	@Override
	public String actionName() {
		return "Scale Extent";
	}
}
