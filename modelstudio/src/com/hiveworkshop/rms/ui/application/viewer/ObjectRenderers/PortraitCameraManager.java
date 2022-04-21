package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNodeCamera;

import java.awt.*;

public class PortraitCameraManager extends CameraManager {
	protected ViewerCamera camera;
	private RenderNodeCamera renderCamera;

	public PortraitCameraManager(Component viewport) {
		super(viewport);
		camera = new ViewerCamera();
	}

	public void updateCamera() {
		vec4Heap.set(1, 0, 0, this.upAngle);
		upRot.setFromAxisAngle(vec4Heap);
		vec4Heap.set(0, 0, 1, this.sideAngle);
		sideRot.setFromAxisAngle(vec4Heap);
		upRot.mulLeft(sideRot);
//		System.out.println();

		camPosition.set(0, 0, 1);
		camPosition.transform(upRot);
		camPosition.scale(this.distance);
		camPosition.add(target);

		if (this.renderCamera != null) {
			camPosition.set(renderCamera.getRenderPivot());
			target.set(renderCamera.getTarget());

			camera.perspective((float) Math.toDegrees(renderCamera.getFoV()), camera.getAspect(),
					(float) renderCamera.getNearClip(), (float) renderCamera.getFarClip());
		}
		else {
			camera.perspective(70, camera.getAspect(), 1, 5000);
		}

		camera.moveToAndFace(camPosition, target, worldUp);
	}

	public void setModelInstance(final RenderModel modelInstance, final Camera camera) {
		if (modelInstance != null && camera != null) {
			renderCamera = modelInstance.getRenderNode(camera);
		} else {
			renderCamera = null;
		}
	}

}