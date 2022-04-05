package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.util.Vec3;

public class PortraitCameraManager extends CameraManager {
	public Camera modelCamera;
	protected RenderModel modelInstance;

	public void updateCamera() {
		vec4Heap.set(1, 0, 0, this.horizontalAngle);
		quatHeap.setFromAxisAngle(vec4Heap);
		vec4Heap.set(0, 0, 1, this.verticalAngle);
		quatHeap2.setFromAxisAngle(vec4Heap);
		quatHeap.mulLeft(quatHeap2);
//		System.out.println();

		this.position.set(0, 0, 1);
		position.transform(quatHeap);
		this.position.scale(this.distance);
		position.add(target);
		if (this.modelCamera != null) {
			Vec3 sourceTranslation = modelCamera.getSourceNode().getRenderTranslation(modelInstance.getTimeEnvironment());
			if (sourceTranslation == null) {
				sourceTranslation = Vec3.ZERO;
			}
			Vec3 targetTranslation = modelCamera.getTargetNode().getRenderTranslation(modelInstance.getTimeEnvironment());
			if (targetTranslation == null) {
				targetTranslation = Vec3.ZERO;
			}

			Vec3 cameraPosition = this.modelCamera.getPosition();
			Vec3 targetPosition = this.modelCamera.getTargetPosition();
			this.position.set((cameraPosition.x + sourceTranslation.x), (cameraPosition.y + sourceTranslation.y), (cameraPosition.z + sourceTranslation.z));
			this.target.set((targetPosition.x + targetTranslation.x), (targetPosition.y + targetTranslation.y), (targetPosition.z + targetTranslation.z));

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