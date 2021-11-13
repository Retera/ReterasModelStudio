package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public final class RenderNode {
	private final AnimatedNode idObject;

	private boolean dontInheritScaling = false;
	boolean billboarded;
	boolean billboardedX;
	boolean billboardedY;
	boolean billboardedZ;

	private final Vec3 localLocation = new Vec3(0, 0, 0);
	private final Quat localRotation = new Quat(0, 0, 0, 1);
	private final Vec3 localScale = new Vec3(1, 1, 1);
	private final Mat4 localMatrix = new Mat4();

	private final Vec3 worldLocation = new Vec3();
	private final Quat worldRotation = new Quat();
	private final Vec3 worldScale = new Vec3(1, 1, 1);
	private final Mat4 worldMatrix = new Mat4();

	private final Vec3 renderPivot = new Vec3(0, 0, 0);
//	private final Mat4 finalMatrix;
//	private Mat4 bindPose;

	private final Vec3 inverseWorldLocation = new Vec3();
	private final Quat inverseWorldRotation = new Quat();
	private final Vec3 inverseWorldScale = new Vec3();

	protected boolean visible;

	private final RenderModel renderModel;

	boolean dirty = false;
	boolean wasDirty = false;

	public RenderNode(final RenderModel renderModel, final AnimatedNode idObject) {
		this.renderModel = renderModel;
		this.idObject = idObject;
		renderPivot.set(idObject.getPivotPoint());
//		if (idObject instanceof IdObject) {
//			final float[] bindPose = ((IdObject) idObject).getBindPose();
//			if (bindPose != null) {
////				finalMatrix = new Mat4();
//				this.bindPose = new Mat4().setFromBindPose(bindPose);
////				this.bindPose.m00 = bindPose[0];
////				this.bindPose.m01 = bindPose[1];
////				this.bindPose.m02 = bindPose[2];
////
////				this.bindPose.m10 = bindPose[3];
////				this.bindPose.m11 = bindPose[4];
////				this.bindPose.m12 = bindPose[5];
////
////				this.bindPose.m20 = bindPose[6];
////				this.bindPose.m21 = bindPose[7];
////				this.bindPose.m22 = bindPose[8];
////
////				this.bindPose.m30 = bindPose[9];
////				this.bindPose.m31 = bindPose[10];
////				this.bindPose.m32 = bindPose[11];
////				this.bindPose.m33 = 1;
//			} else {
////				finalMatrix = worldMatrix;
//			}
//		} else {
////			finalMatrix = worldMatrix;
//		}
	}

	public void refreshFromEditor() {
		if (idObject instanceof IdObject) {
			final IdObject actualIdObject = (IdObject)idObject;

			dontInheritScaling = actualIdObject.getDontInheritScaling();
			billboarded = actualIdObject.getBillboarded();
			billboardedX = actualIdObject.getBillboardLockX();
			billboardedY = actualIdObject.getBillboardLockY();
			billboardedZ = actualIdObject.getBillboardLockZ();
		}
	}

	public void recalculateTransformation() {
		if (dirty) {
//			dirty = false;
//			if (idObject instanceof IdObject && ((IdObject) idObject).getParent() != null) {
//				worldLocation.set(localLocation);
//				Vec3 computedLocation = new Vec3(localLocation);
//				Vec3 computedScaling = new Vec3();
//
//				if (dontInheritScaling) {
//					Vec3 parentInverseScale = renderModel.getRenderNode(((IdObject) idObject).getParent()).inverseWorldScale;
//					computedScaling.set(parentInverseScale).multiply(localScale);
//
//					worldScale.set(localScale);
//				} else {
//					computedScaling = localScale;
//
//					Vec3 parentScale = renderModel.getRenderNode(((IdObject) idObject).getParent()).worldScale;
//					worldScale.set(parentScale).multiply(localScale);
//				}
//
//				localMatrix.fromRotationTranslationScaleOrigin(localRotation, computedLocation, computedScaling, idObject.getPivotPoint());
//
//				Mat4 parentWorldMatrix = renderModel.getRenderNode(((IdObject) idObject).getParent()).worldMatrix;
//				this.worldMatrix.set(parentWorldMatrix).mul(localMatrix);
//
//				Quat parentWorldRotation = renderModel.getRenderNode(((IdObject) idObject).getParent()).worldRotation;
//				this.worldRotation.set(parentWorldRotation).mul(localRotation);
//			} else {
//
//				localMatrix.fromRotationTranslationScaleOrigin(localRotation, localLocation, localScale, idObject.getPivotPoint());
//				worldMatrix.set(localMatrix);
//				worldRotation.set(localRotation);
//				worldScale.set(localScale);
//			}
////			if (worldMatrix != finalMatrix) {
////				finalMatrix.set(worldMatrix).mul(bindPose);
////			}
//
//			// Inverse world rotation
//			inverseWorldRotation.set(worldRotation).invertRotation();
//
//			// Inverse world scale
//			inverseWorldScale.set(1, 1, 1).divide(worldScale);
//
//			// World location
//			worldLocation.set(worldMatrix.m30, worldMatrix.m31, worldMatrix.m32);
//
//			// Inverse world location
//			inverseWorldLocation.set(worldLocation).negate();
//
//			renderPivot.set(idObject.getPivotPoint()).transform(worldMatrix);
		}
	}

	public void update() {
		if (idObject instanceof IdObject) {
			final AnimatedNode parent = ((IdObject) idObject).getParent();
			if (dirty || ((parent != null) && renderModel.getRenderNode(((IdObject) idObject).getParent()).wasDirty)) {
				dirty = true;
				wasDirty = true;
				recalculateTransformation();
			} else {
				wasDirty = false;
			}

			updateChildren();
		}
	}

	public void updateChildren() {
//		for (final AnimatedNode childNode : idObject.getChildrenNodes()) {
//			if (renderModel.getRenderNode(childNode) == null) {
//				throw new NullPointerException(
//						"Cannot find child \"" + childNode.getName() + "\" of \"" + idObject.getName() + "\"");
//			}
//			renderModel.getRenderNode(childNode).update();
//		}
	}

	public void resetTransformation() {
		localLocation.set(0, 0, 0);
		localRotation.set(0, 0, 0, 1);
		localScale.set(1, 1, 1);
		worldMatrix.setIdentity();

		renderPivot.set(idObject.getPivotPoint()).transform(worldMatrix);

		dirty = true;
	}

	public void setTransformation(final Vec3 location, final Quat rotation, final Vec3 scale) {
		localLocation.set(location);
		localRotation.set(rotation);
		localScale.set(scale);

		dirty = true;
	}

	public void fetchTransformation(TimeEnvironmentImpl timeEnvironment) {
		setLocation(idObject.getRenderTranslation(timeEnvironment));
		setRotation(idObject.getRenderRotation(timeEnvironment));
		setScale(idObject.getRenderScale(timeEnvironment));
//		localLocation.set(idObject.getRenderTranslation(timeEnvironment));
//		localRotation.set(idObject.getRenderRotation(timeEnvironment));
//		localScale.set(idObject.getRenderScale(timeEnvironment));

		dirty = true;
	}

//	public void setRotation(Quat rotation) {
//		localRotation.set(rotation);
//		dirty = true;
//	}

	public Quat setRotation(Quat rotation) {
		if(rotation == null){
			localRotation.set(0, 0, 0, 1);
		}else {
			localRotation.set(rotation);
		}
//		dirty = true;
		return localRotation;
	}

	public Vec3 setLocation(Vec3 location) {
		if(location == null){
			localLocation.set(0, 0, 0);
		}else {
			localLocation.set(location);
		}
//		dirty = true;
		return localLocation;
	}
//	public Vec3 setTranslation(Vec3 translation) {
//		if(translation == null){
//			localTranslation.set(0, 0, 0);
//		}else {
//			localTranslation.set(translation);
//		}
////		dirty = true;
//		return localTranslation;
//	}

	public Vec3 setScale(Vec3 scale) {
		if(scale == null){
			localScale.set(1, 1, 1);
		}else {
			localScale.set(scale);
		}
//		dirty = true;
		return localScale;
	}

	public RenderNode setVisible(boolean visible) {
		this.visible = visible;
		return this;
	}

	public RenderNode setDirty(boolean dirty) {
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
		return idObject.getPivotPoint();
//		return Vec3.getTransformed(idObject.getPivotPoint(), worldMatrix);

//		Vec4 vector4Heap = new Vec4(idObject.getPivotPoint(), 1);
//		vector4Heap.transform(worldMatrix);
//		return vector4Heap.getVec3();
	}
}