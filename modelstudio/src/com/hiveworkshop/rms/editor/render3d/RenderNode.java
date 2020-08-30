package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

public final class RenderNode {
	private final AnimatedNode idObject;

	private boolean dontInheritScaling = false;
	boolean billboarded;
	boolean billboardedX;
	boolean billboardedY;
	boolean billboardedZ;
	private static final Vec3 locationHeap = new Vec3();
	private static final Vec3 scalingHeap = new Vec3();
	private static final Vec3 pivotHeap = new Vec3();
	private static final Vec4 vector4Heap = new Vec4();

	protected final Vec3 localLocation = new Vec3();
	protected final Quat localRotation = new Quat();
	protected final Vec3 localScale = new Vec3(1, 1, 1);
	private final Mat4 localMatrix = new Mat4();

	private final Vec3 worldLocation = new Vec3();
	private final Quat worldRotation = new Quat();
	private final Vec3 worldScale = new Vec3(1, 1, 1);
	private final Mat4 worldMatrix = new Mat4();
	private final Mat4 finalMatrix;
	private Mat4 bindPose;

	protected final Vec3 inverseWorldLocation = new Vec3();
	protected final Quat inverseWorldRotation = new Quat();
	protected final Vec3 inverseWorldScale = new Vec3();

	protected boolean visible;

	private final RenderModel model;

	public RenderNode(final RenderModel model, final AnimatedNode idObject) {
		this.model = model;
		this.idObject = idObject;
		if (idObject instanceof IdObject) {
			final float[] bindPose = ((IdObject) idObject).getBindPose();
			if (bindPose != null) {
				finalMatrix = new Mat4();
				this.bindPose = new Mat4();
				this.bindPose.m00 = bindPose[0];
				this.bindPose.m01 = bindPose[1];
				this.bindPose.m02 = bindPose[2];
				this.bindPose.m10 = bindPose[3];
				this.bindPose.m11 = bindPose[4];
				this.bindPose.m12 = bindPose[5];
				this.bindPose.m20 = bindPose[6];
				this.bindPose.m21 = bindPose[7];
				this.bindPose.m22 = bindPose[8];
				this.bindPose.m30 = bindPose[9];
				this.bindPose.m31 = bindPose[10];
				this.bindPose.m32 = bindPose[11];
				this.bindPose.m33 = 1;
			} else {
				finalMatrix = worldMatrix;
			}
		} else {
			finalMatrix = worldMatrix;
		}
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

	boolean dirty = false;
	boolean wasDirty = false;

	public void recalculateTransformation() {
		if (dirty) {
			dirty = false;
			if (idObject.getParent() != null) {
				final Vec3 computedLocation = locationHeap;
				final Vec3 computedScaling;
				computedLocation.x = localLocation.x;// + (float) parent.pivotPoint.x;
				computedLocation.y = localLocation.y;// + (float) parent.pivotPoint.y;
				computedLocation.z = localLocation.z;// + (float) parent.pivotPoint.z;

				if (dontInheritScaling) {
					computedScaling = scalingHeap;

					final Vec3 parentInverseScale = model.getRenderNode(idObject.getParent()).inverseWorldScale;
					computedScaling.x = parentInverseScale.x * localScale.x;
					computedScaling.y = parentInverseScale.y * localScale.y;
					computedScaling.z = parentInverseScale.z * localScale.z;

					worldScale.x = localScale.x;
					worldScale.y = localScale.y;
					worldScale.z = localScale.z;
				} else {
					computedScaling = localScale;

					final Vec3 parentScale = model.getRenderNode(idObject.getParent()).worldScale;
					worldScale.x = parentScale.x * localScale.x;
					worldScale.y = parentScale.y * localScale.y;
					worldScale.z = parentScale.z * localScale.z;
				}

				pivotHeap.x = idObject.getPivotPoint().x;
				pivotHeap.y = idObject.getPivotPoint().y;
				pivotHeap.z = idObject.getPivotPoint().z;
				localMatrix.fromRotationTranslationScaleOrigin(localRotation, computedLocation, computedScaling, pivotHeap);

				model.getRenderNode(idObject.getParent()).worldMatrix.mul(localMatrix, worldMatrix);
				model.getRenderNode(idObject.getParent()).worldRotation.mul(localRotation, worldRotation);
			} else {

				pivotHeap.x = idObject.getPivotPoint().x;
				pivotHeap.y = idObject.getPivotPoint().y;
				pivotHeap.z = idObject.getPivotPoint().z;
				localMatrix.fromRotationTranslationScaleOrigin(localRotation, localLocation, localScale, pivotHeap);
				worldMatrix.set(localMatrix);
				worldRotation.set(localRotation);
				worldScale.set(localScale);
			}
			if (worldMatrix != finalMatrix) {
				worldMatrix.mul(bindPose, finalMatrix);
			}

			// Inverse world rotation
			inverseWorldRotation.x = -worldRotation.x;
			inverseWorldRotation.y = -worldRotation.y;
			inverseWorldRotation.z = -worldRotation.z;
			inverseWorldRotation.w = worldRotation.w;

			// Inverse world scale
			inverseWorldScale.x = 1 / worldScale.x;
			inverseWorldScale.y = 1 / worldScale.y;
			inverseWorldScale.z = 1 / worldScale.z;

			// World location
			worldLocation.x = worldMatrix.m30;
			worldLocation.y = worldMatrix.m31;
			worldLocation.z = worldMatrix.m32;

			// Inverse world location
			inverseWorldLocation.x = -worldLocation.x;
			inverseWorldLocation.y = -worldLocation.y;
			inverseWorldLocation.z = -worldLocation.z;
		}
	}

	public void update() {
		final AnimatedNode parent = idObject.getParent();
		if (dirty || ((parent != null) && model.getRenderNode(idObject.getParent()).wasDirty)) {
			dirty = true;
			wasDirty = true;
			recalculateTransformation();
		} else {
			wasDirty = false;
		}

		updateChildren();
	}

	public void updateChildren() {
		for (final AnimatedNode childNode : idObject.getChildrenNodes()) {
			if (model.getRenderNode(childNode) == null) {
				if (childNode instanceof IdObject) {
					throw new NullPointerException("Cannot find child \"" + childNode.getName() + ":"
							+ ((IdObject) childNode).getObjectId() + "\" of \"" + idObject.getName() + "\"");
				} else {
					throw new NullPointerException(
							"Cannot find child \"" + childNode.getName() + "\" of \"" + idObject.getName() + "\"");
				}
			}
			model.getRenderNode(childNode).update();
		}
	}

	public void resetTransformation() {
		localLocation.set(0, 0, 0);
		localRotation.set(0, 0, 0, 1);
		localScale.set(1, 1, 1);
		dirty = true;
	}

	public void setTransformation(final Vec3 location, final Quat rotation, final Vec3 scale) {
		localLocation.x = location.x;
		localLocation.y = location.y;
		localLocation.z = location.z;

		localRotation.x = rotation.x;
		localRotation.y = rotation.y;
		localRotation.z = rotation.z;
		localRotation.w = rotation.w;

		localScale.x = scale.x;
		localScale.y = scale.y;
		localScale.z = scale.z;

		dirty = true;
	}

	public Mat4 getWorldMatrix() {
		return worldMatrix;
	}

	/**
	 * Supposedly returns final matrix based on bind pose, but don't actually use
	 * this yet, I'm not even sure it's computed correctly. Graphically, based on my
	 * tests, it looked like maybe we do not need it.
	 * 
	 * @return
	 */
	public Mat4 getFinalMatrix() {
		return finalMatrix;
	}

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
		vector4Heap.x = idObject.getPivotPoint().x;
		vector4Heap.y = idObject.getPivotPoint().y;
		vector4Heap.z = idObject.getPivotPoint().z;
		vector4Heap.w = 1;
		worldMatrix.transform(vector4Heap);
		pivotHeap.x = vector4Heap.x;
		pivotHeap.y = vector4Heap.y;
		pivotHeap.z = vector4Heap.z;
		return pivotHeap;
	}
}