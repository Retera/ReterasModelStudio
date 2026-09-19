package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import com.hiveworkshop.wc3.mdl.Camera;

public class ComponentCameraCard extends ComponentCard<Camera> {
	private final TrackSummaryPanel tracks = new TrackSummaryPanel();

	public ComponentCameraCard() {
		addTextField("Name", Camera::getName, Camera::setName);
		addVertexRow("Position", 1.0, Camera::getPosition, Camera::setPosition);
		addVertexRow("Target", 1.0, Camera::getTargetPosition, Camera::setTargetPosition);
		addDoubleSpinner("Field of View (deg)", 1.0, camera -> Math.toDegrees(camera.getFieldOfView()),
				(camera, degrees) -> camera.setFieldOfView(Math.toRadians(degrees)));
		addDoubleSpinner("Near Clip", 1.0, Camera::getNearClip, Camera::setNearClip);
		addDoubleSpinner("Far Clip", 1.0, Camera::getFarClip, Camera::setFarClip);
		addWide(tracks);
	}

	@Override
	protected void onReload() {
		tracks.clear();
		tracks.addGroup("Source ", item.getAnimFlags(), navigationListener);
		tracks.addGroup("Target ", item.getTargetAnimFlags(), navigationListener);
		tracks.finish();
	}
}
