package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNodeCamera;

import java.awt.*;

public class PortraitCameraManager extends CameraManager {
	CameraNode cameraNode;
	RenderModel renderModel;

	public PortraitCameraManager(Component viewport) {
		super(viewport);
	}

	public void updateCamera() {
		useCamera();
		super.updateCamera();
	}

	public void setModelInstance(final RenderModel modelInstance, final Camera camera) {
		if (modelInstance != null && camera != null) {
			this.renderModel = modelInstance;
			this.cameraNode = camera.getSourceNode();
		} else {
			this.renderModel = null;
			this.cameraNode = null;
		}
	}
	public CameraManager setCamera(RenderModel renderModel, CameraNode cameraNode) {
		this.renderModel = renderModel;
		this.cameraNode = cameraNode;
		useCamera();
		return this;
	}

	public void useCamera(){
		if (this.renderModel != null) {
			RenderNodeCamera renderCamera = renderModel.getRenderNode(cameraNode);
			target.set(renderCamera.getTarget());
			distance = renderCamera.getPivot().distance(renderCamera.getTarget());
			fieldOfView = (float) Math.toDegrees(cameraNode.getParent().getFieldOfView());
			farClip = (float) cameraNode.getParent().getFarClip();
			nearClip = (float) cameraNode.getParent().getNearClip();

			vecHeap.set(renderCamera.getPivot());
			vecHeap.sub(target).normalize();

			upAngle = (float) vecHeap.getAngleToZaxis();
			sideAngle = (float) -vecHeap.getZrotToYaxis();
			tiltAngle = renderCamera.getLocalRotationFloat();

			calculateCameraRotation();
		}
	}

}