package com.hiveworkshop.wc3.mdl.render3d;

import com.hiveworkshop.wc3.mdl.AnimatedNode;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.Vertex;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import com.hiveworkshop.wc3.mdl.IdObject.NodeFlags;
import com.hiveworkshop.wc3.util.MathUtils;
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
	private final Matrix4f localMatrix = new Matrix4f();

	private final Vector3f worldLocation = new Vector3f();
	private final Quaternion worldRotation = new Quaternion();
	private final Vector3f worldScale = new Vector3f(1, 1, 1);
	private final Matrix4f worldMatrix = new Matrix4f();

	protected final Vector3f inverseWorldLocation = new Vector3f();
	protected final Quaternion inverseWorldRotation = new Quaternion();
	protected final Vector3f inverseWorldScale = new Vector3f();

	protected boolean visible;

	private final RenderModel model;

	public RenderNode(final RenderModel model, final AnimatedNode idObject) {
		this.model = model;
		this.idObject = idObject;
	}

	public void refreshFromEditor() {
		dontInheritScaling = idObject.hasFlag(NodeFlags.DONTINHERIT_SCALING); // hasFlag is idiot code with string
																				// compare
		billboarded = idObject.hasFlag(NodeFlags.BILLBOARDED);
		billboardedX = idObject.hasFlag(NodeFlags.BILLBOARD_LOCK_X);
		billboardedY = idObject.hasFlag(NodeFlags.BILLBOARD_LOCK_Y);
		billboardedZ = idObject.hasFlag(NodeFlags.BILLBOARD_LOCK_Z);
	}

	boolean dirty = false;
	boolean wasDirty = false;

	public void recalculateTransformation() {
		if (this.dirty) {
			this.dirty = false;
			if (idObject.getParent() != null) {
				final Vector3f computedLocation = locationHeap;
				final Vector3f computedScaling;
				computedLocation.x = localLocation.x;// + (float) parent.pivotPoint.x;
				computedLocation.y = localLocation.y;// + (float) parent.pivotPoint.y;
				computedLocation.z = localLocation.z;// + (float) parent.pivotPoint.z;

				if (this.dontInheritScaling) {
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

				Matrix4f.mul(model.getRenderNode(idObject.getParent()).worldMatrix, localMatrix, worldMatrix);

				Quaternion.mul(model.getRenderNode(idObject.getParent()).worldRotation, localRotation, worldRotation);
			} else {

				pivotHeap.x = (float) idObject.getPivotPoint().x;
				pivotHeap.y = (float) idObject.getPivotPoint().y;
				pivotHeap.z = (float) idObject.getPivotPoint().z;
				MathUtils.fromRotationTranslationScaleOrigin(localRotation, localLocation, localScale, localMatrix,
						pivotHeap);
				worldMatrix.load(localMatrix);
				worldRotation.set(localRotation);
				worldScale.set(localScale);
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
		if (this.dirty || (parent != null && model.getRenderNode(idObject.getParent()).wasDirty)) {
			this.dirty = true;
			this.wasDirty = true;
			this.recalculateTransformation();
		} else {
			this.wasDirty = false;
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

		localRotation.x = (float) rotation.a;
		localRotation.y = (float) rotation.b;
		localRotation.z = (float) rotation.c;
		localRotation.w = (float) rotation.d;

		localScale.x = (float) scale.x;
		localScale.y = (float) scale.y;
		localScale.z = (float) scale.z;

		this.dirty = true;
	}

	public Matrix4f getWorldMatrix() {
		return worldMatrix;
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

	public Matrix4f getLocalMatrix() {
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
		Matrix4f.transform(worldMatrix, vector4Heap, vector4Heap);
		pivotHeap.x = vector4Heap.x;
		pivotHeap.y = vector4Heap.y;
		pivotHeap.z = vector4Heap.z;
		return pivotHeap;
	}
}