package com.hiveworkshop.wc3.gui.modelviewer.camera;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.util.MathUtils;

public final class PortraitCameraManager extends CameraManager {
	public com.hiveworkshop.wc3.mdl.Camera modelCamera;
	protected RenderModel modelInstance;

	@Override
	public void updateCamera() {
		vec4Heap.set(1, 0, 0, this.horizontalAngle);
		quatHeap.setFromAxisAngle(vec4Heap);
		vec4Heap.set(0, 0, 1, this.verticalAngle);
		quatHeap2.setFromAxisAngle(vec4Heap);
		Quaternion.mul(quatHeap2, quatHeap, quatHeap);
		System.out.println();

		this.position.set(0, 0, 1);
		MathUtils.transform(this.quatHeap, this.position);
		this.position.scale(this.distance);
		Vector3f.add(this.position, this.target, this.position);
		if (this.modelCamera != null) {
			Vertex sourceTranslation = modelCamera.getSourceNode()
					.getRenderTranslation(modelInstance.getAnimatedRenderEnvironment());
			if (sourceTranslation == null) {
				sourceTranslation = Vertex.ORIGIN;
			}
			Vertex targetTranslation = modelCamera.getTargetNode()
					.getRenderTranslation(modelInstance.getAnimatedRenderEnvironment());
			if (targetTranslation == null) {
				targetTranslation = Vertex.ORIGIN;
			}

			final Vertex cameraPosition = this.modelCamera.getPosition();
			final Vertex targetPosition = this.modelCamera.getTargetPosition();
			this.position.set((float) (cameraPosition.x + sourceTranslation.x),
					(float) (cameraPosition.y + sourceTranslation.y), (float) (cameraPosition.z + sourceTranslation.z));
			this.target.set((float) (targetPosition.x + targetTranslation.x),
					(float) (targetPosition.y + targetTranslation.y), (float) (targetPosition.z + targetTranslation.z));

			this.camera.perspective((float) this.modelCamera.getFieldOfView() * 0.75f, this.camera.getAspect(),
					(float) this.modelCamera.getNearClip(), (float) this.modelCamera.getFarClip());
		}
		else {
			this.camera.perspective(70, this.camera.getAspect(), 100, 5000);
		}

		this.camera.moveToAndFace(this.position, this.target, this.worldUp);
	}

	public void setModelInstance(final RenderModel modelInstance, final Camera camera) {
		this.modelInstance = modelInstance;
		if (modelInstance == null) {
			this.modelCamera = null;
		}
		else if (camera != null) {
			this.modelCamera = camera;
		}
	}

}