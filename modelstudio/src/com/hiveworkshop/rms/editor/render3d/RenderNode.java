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

	protected final Vec3 localLocation = new Vec3(0, 0, 0);
	protected final Quat localRotation = new Quat(0, 0, 0, 1);
	protected final Vec3 localScale = new Vec3(1, 1, 1);
	private final Mat4 localMatrix = new Mat4();

	private final Vec3 worldLocation = new Vec3();
	private final Quat worldRotation = new Quat();
	private final Vec3 worldScale = new Vec3(1, 1, 1);
	private final Mat4 worldMatrix = new Mat4();
	private final Mat4 finalMatrix;
	private Mat4 bindPose;

	protected Vec3 inverseWorldLocation = new Vec3();
	protected Quat inverseWorldRotation = new Quat();
	protected Vec3 inverseWorldScale = new Vec3();

	protected boolean visible;

	private final RenderModel model;

	boolean dirty = false;
	boolean wasDirty = false;

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

	public void recalculateTransformation() {
		if (dirty) {
//			dirty = false;
			if (idObject instanceof IdObject && ((IdObject) idObject).getParent() != null) {
				Vec3 computedLocation = new Vec3(localLocation);
				Vec3 computedScaling = new Vec3();

				if (dontInheritScaling) {
					final Vec3 parentInverseScale = model.getRenderNode(((IdObject) idObject).getParent()).inverseWorldScale;
					computedScaling.set(parentInverseScale);
					computedScaling.multiply(localScale);

					worldScale.set(localScale);
				} else {
					computedScaling = localScale;

					final Vec3 parentScale = model.getRenderNode(((IdObject) idObject).getParent()).worldScale;
					worldScale.set(parentScale);
					worldScale.multiply(localScale);
				}

				localMatrix.fromRotationTranslationScaleOrigin(localRotation, computedLocation, computedScaling, idObject.getPivotPoint());

				worldMatrix.set(Mat4.getProd(model.getRenderNode(((IdObject) idObject).getParent()).worldMatrix, localMatrix));
				worldRotation.set(Quat.getProd(model.getRenderNode(((IdObject) idObject).getParent()).worldRotation, localRotation));
			} else {

				localMatrix.fromRotationTranslationScaleOrigin(localRotation, localLocation, localScale, idObject.getPivotPoint());
				worldMatrix.set(localMatrix);
				worldRotation.set(localRotation);
				worldScale.set(localScale);
			}
			if (worldMatrix != finalMatrix) {
				finalMatrix.set(Mat4.getProd(worldMatrix, bindPose));
			}

			// Inverse world rotation
			inverseWorldRotation.set(Quat.getInverseRotation(worldRotation));

			// Inverse world scale
			inverseWorldScale = Vec3.getQuotient(new Vec3(1, 1, 1), worldScale);

			// World location
			worldLocation.set(worldMatrix.m30, worldMatrix.m31, worldMatrix.m32);

			// Inverse world location
			inverseWorldLocation.set(worldLocation).negate();
		}
	}

	public void update() {
		if (idObject instanceof IdObject) {
			final AnimatedNode parent = ((IdObject) idObject).getParent();
			if (dirty || ((parent != null) && model.getRenderNode(((IdObject) idObject).getParent()).wasDirty)) {
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
		for (final AnimatedNode childNode : idObject.getChildrenNodes()) {
			if (model.getRenderNode(childNode) == null) {
				if (childNode instanceof IdObject) {
//					throw new NullPointerException("Cannot find child \"" + childNode.getName() + ":"
//							+ ((IdObject) childNode).getObjectId() + "\" of \"" + idObject.getName() + "\"");
					throw new NullPointerException("Cannot find child \"" + childNode.getName()
							+ "\" of \"" + idObject.getName() + "\"");
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
		localLocation.set(location);

		localRotation.set(rotation);

		localScale.set(scale);

		dirty = true;
	}

	public Mat4 getWorldMatrix() {
		return worldMatrix;
	}

	/**
	 * Supposedly returns final matrix based on bind pose, but don't actually use
	 * this yet, I'm not even sure it's computed correctly. Graphically, based on my
	 * tests, it looked like maybe we do not need it.
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
		Vec4 vector4Heap = new Vec4(idObject.getPivotPoint(), 1);
		vector4Heap.transform(worldMatrix);
		return vector4Heap.getVec3();
	}
}