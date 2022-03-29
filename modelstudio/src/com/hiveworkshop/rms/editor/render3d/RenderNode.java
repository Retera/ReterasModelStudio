package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public abstract class RenderNode<T extends AnimatedNode> {
	protected final T animatedNode;

	protected final Vec3 localLocation = new Vec3(0, 0, 0);
	protected final Quat localRotation = new Quat(0, 0, 0, 1);
	protected final Vec3 localScale = new Vec3(1, 1, 1);
	protected final Mat4 localMatrix = new Mat4();

	protected final Vec3 worldLocation = new Vec3();
	protected final Quat worldRotation = new Quat();
	protected final Vec3 worldScale = new Vec3(1, 1, 1);
	protected final Mat4 worldMatrix = new Mat4();

	protected final Vec3 renderPivot = new Vec3(0, 0, 0);

	protected final Vec3 inverseWorldLocation = new Vec3();
	protected final Quat inverseWorldRotation = new Quat();
	protected final Vec3 inverseWorldScale = new Vec3();

	protected boolean visible;

	protected final RenderModel renderModel;

	boolean dirty = false;
	boolean wasDirty = false;

	public RenderNode(RenderModel renderModel, T animatedNode) {
		this.renderModel = renderModel;
		this.animatedNode = animatedNode;
		renderPivot.set(animatedNode.getPivotPoint());
	}

	public abstract void refreshFromEditor();

	public abstract void recalculateTransformation();

	public abstract void update();

	public abstract void resetTransformation();

	public abstract void fetchTransformation(TimeEnvironmentImpl timeEnvironment);

	public Quat setRotation(Quat rotation) {
		if(rotation == null){
			localRotation.set(0, 0, 0, 1);
		}else {
			localRotation.set(rotation);
		}
		return localRotation;
	}

	public Vec3 setLocation(Vec3 location) {
		if(location == null){
			localLocation.set(0, 0, 0);
		}else {
			localLocation.set(location);
		}
		return localLocation;
	}


	public Vec3 setScale(Vec3 scale) {
		if(scale == null){
			localScale.set(1, 1, 1);
		}else {
			localScale.set(scale);
		}
		return localScale;
	}

	public RenderNode<T> setVisible(boolean visible) {
		this.visible = visible;
		return this;
	}

	public RenderNode<T> setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}

	public Mat4 getWorldMatrix() {
		return worldMatrix;
	}

	/**
	 * Supposedly returns final matrix based on bind pose, but don't actually use
	 * this yet, I'm not even sure it's computed correctly. Graphically, based on my
	 * tests, it looked like maybe we do not need it.
	 */

	public Quat getInverseWorldRotation() {
		return inverseWorldRotation;
	}

	public Vec3 getInverseWorldLocation() {
		return inverseWorldLocation;
	}

	public Vec3 getInverseWorldScale() {
		return inverseWorldScale;
	}

	public Vec3 getWorldLocation() {
		return worldLocation;
	}

	public Vec3 getLocalLocation() {
		return localLocation;
	}

	public Vec3 getLocalScale() {
		return localScale;
	}

	public Mat4 getLocalMatrix() {
		return localMatrix;
	}

	public abstract Mat4 getParentWorldMatrix();

	public Quat getLocalRotation() {
		return localRotation;
	}

	public Quat getWorldRotation() {
		return worldRotation;
	}

	public Vec3 getWorldScale() {
		return worldScale;
	}

	public Vec3 getPivot() {
		if (renderModel.getTimeEnvironment().isLive() || ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE) {
			return renderPivot;
		}
		return animatedNode.getPivotPoint();
//		return Vec3.getTransformed(idObject.getPivotPoint(), worldMatrix);

//		Vec4 vector4Heap = new Vec4(idObject.getPivotPoint(), 1);
//		vector4Heap.transform(worldMatrix);
//		return vector4Heap.getVec3();
	}
}