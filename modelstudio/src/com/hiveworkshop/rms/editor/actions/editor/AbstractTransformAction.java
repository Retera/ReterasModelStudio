package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

public abstract class AbstractTransformAction implements UndoAction {
	public AbstractTransformAction doSetup() {
		return this;
	}
	public AbstractTransformAction updateTranslation(double deltaX, double deltaY, double deltaZ) {
		return this;
	}
	public AbstractTransformAction updateTranslation(Vec3 delta) {
		return this;
	}
	public AbstractTransformAction setTranslation(Vec3 delta) {
		return this;
	}
	public AbstractTransformAction setScale(Vec3 scale) {
		return this;
	}
	public AbstractTransformAction updateScale(Vec3 scale) {
		return this;
	}
	public AbstractTransformAction setRotation(double radians) {
		return this;
	}
	public AbstractTransformAction updateRotation(double radians) {
		return this;
	}
	protected AbstractTransformAction updateTransform() {
		return this;
	}

}
