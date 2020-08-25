package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Matrix4;
import com.hiveworkshop.rms.editor.model.QuaternionRotation;
import com.hiveworkshop.rms.editor.model.Vertex;
import com.hiveworkshop.rms.util.MathUtils;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public final class RenderNode {
	private final AnimatedNode idObject;

	private boolean dontInheritScaling = false;
	boolean billboarded;
	boolean billboardedX;
	boolean billboardedY;
	boolean billboardedZ;
	private static final Vector3f locationHeap = new Vector3f();
	private static final Vector3f scalingHeap = new Vector3f();
	private static final Vector3f pivotHeap = new Vector3f();
	private static final Vector4f vector4Heap = new Vector4f();

	protected final Vector3f localLocation = new Vector3f();
	protected final Quaternion localRotation = new Quaternion();
	protected final Vector3f localScale = new Vector3f(1, 1, 1);
	private final Matrix4 localMatrix = new Matrix4();

	private final Vector3f worldLocation = new Vector3f();
	private final Quaternion worldRotation = new Quaternion();
	private final Vector3f worldScale = new Vector3f(1, 1, 1);
	private final Matrix4 worldMatrix = new Matrix4();
	private final Matrix4 finalMatrix;
	private Matrix4 bindPose;

	protected final Vector3f inverseWorldLocation = new Vector3f();
	protected final Quaternion inverseWorldRotation = new Quaternion();
	protected final Vector3f inverseWorldScale = new Vector3f();

	protected boolean visible;

	private final RenderModel model;

	public RenderNode(final RenderModel model, final AnimatedNode idObject) {
		this.model = model;
		this.idObject = idObject;
		if (idObject instanceof IdObject) {
			final float[] bindPose = ((IdObject) idObject).getBindPose();
			if (bindPose != null) {
				finalMatrix = new Matrix4();
				this.bindPose = new Matrix4();
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
				final Vector3f computedLocation = locationHeap;
				final Vector3f computedScaling;
				computedLocation.x = localLocation.x;// + (float) parent.pivotPoint.x;
				computedLocation.y = localLocation.y;// + (float) parent.pivotPoint.y;
				computedLocation.z = localLocation.z;// + (float) parent.pivotPoint.z;

				if (dontInheritScaling) {
					computedScaling = scalingHeap;

					final Vector3f parentInverseScale = model.getRenderNode(idObject.getParent()).inverseWorldScale;
					computedScaling.x = parentInverseScale.x * localScale.x;
					computedScaling.y = parentInverseScale.y * localScale.y;
					computedScaling.z = parentInverseScale.z * localScale.z;

					worldScale.x = localScale.x;
					worldScale.y = localScale.y;
					worldScale.z = localScale.z;
				} else {
					computedScaling = localScale;

					final Vector3f parentScale = model.getRenderNode(idObject.getParent()).worldScale;
					worldScale.x = parentScale.x * localScale.x;
					worldScale.y = parentScale.y * localScale.y;
					worldScale.z = parentScale.z * localScale.z;
				}

				pivotHeap.x = (float) idObject.getPivotPoint().x;
				pivotHeap.y = (float) idObject.getPivotPoint().y;
				pivotHeap.z = (float) idObject.getPivotPoint().z;
				MathUtils.fromRotationTranslationScaleOrigin(localRotation, computedLocation, computedScaling,
						localMatrix, pivotHeap);

				Matrix4.mul(model.getRenderNode(idObject.getParent()).worldMatrix, localMatrix, worldMatrix);

				Quaternion.mul(model.getRenderNode(idObject.getParent()).worldRotation, localRotation, worldRotation);
			} else {

				pivotHeap.x = (float) idObject.getPivotPoint().x;
				pivotHeap.y = (float) idObject.getPivotPoint().y;
				pivotHeap.z = (float) idObject.getPivotPoint().z;
				MathUtils.fromRotationTranslationScaleOrigin(localRotation, localLocation, localScale, localMatrix,
						pivotHeap);
				worldMatrix.set(localMatrix);
				worldRotation.set(localRotation);
				worldScale.set(localScale);
			}
			if (worldMatrix != finalMatrix) {
				Matrix4.mul(worldMatrix, bindPose, finalMatrix);
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

	public void setTransformation(final Vertex location, final QuaternionRotation rotation, final Vertex scale) {
		localLocation.x = (float) location.x;
		localLocation.y = (float) location.y;
		localLocation.z = (float) location.z;

		localRotation.x = (float) rotation.x;
		localRotation.y = (float) rotation.y;
		localRotation.z = (float) rotation.z;
		localRotation.w = (float) rotation.w;

		localScale.x = (float) scale.x;
		localScale.y = (float) scale.y;
		localScale.z = (float) scale.z;

		dirty = true;
	}

	public Matrix4 getWorldMatrix() {
		return worldMatrix;
	}

	/**
	 * Supposedly returns final matrix based on bind pose, but don't actually use
	 * this yet, I'm not even sure it's computed correctly. Graphically, based on my
	 * tests, it looked like maybe we do not need it.
	 * 
	 * @return
	 */
	public Matrix4 getFinalMatrix() {
		return finalMatrix;
	}

	public Quaternion getInverseWorldRotation() {
		return inverseWorldRotation;
	}

	public Vector3f getInverseWorldLocation() {
		return inverseWorldLocation;
	}

	public Vector3f getInverseWorldScale() {
		return inverseWorldScale;
	}

	public Vector3f getWorldLocation() {
		return worldLocation;
	}

	public Vector3f getLocalLocation() {
		return localLocation;
	}

	public Vector3f getLocalScale() {
		return localScale;
	}

	public Matrix4 getLocalMatrix() {
		return localMatrix;
	}

	public Quaternion getLocalRotation() {
		return localRotation;
	}

	public Quaternion getWorldRotation() {
		return worldRotation;
	}

	public Vector3f getWorldScale() {
		return worldScale;
	}

	public Vector3f getPivot() {
		vector4Heap.x = (float) idObject.getPivotPoint().x;
		vector4Heap.y = (float) idObject.getPivotPoint().y;
		vector4Heap.z = (float) idObject.getPivotPoint().z;
		vector4Heap.w = 1;
		Matrix4.transform(worldMatrix, vector4Heap, vector4Heap);
		pivotHeap.x = vector4Heap.x;
		pivotHeap.y = vector4Heap.y;
		pivotHeap.z = vector4Heap.z;
		return pivotHeap;
	}
}