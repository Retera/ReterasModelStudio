package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Mat4;

import java.awt.*;

public class NodeAnimSelectionAtPointTester implements IdObjectVisitor {
	private CoordinateSystem axes;
	private Point point;
	private boolean mouseOverVertex;
	private RenderModel renderModel;

	public NodeAnimSelectionAtPointTester reset(RenderModel renderModel, CoordinateSystem axes, Point point) {
		this.renderModel = renderModel;
		this.axes = axes;
		this.point = point;
		mouseOverVertex = false;
		return this;
	}

	private void handleDefaultNode(Point point, CoordinateSystem axes, IdObject node) {
		Mat4 worldMatrix = renderModel.getRenderNode(node).getWorldMatrix();
		double vertexSize = node.getClickRadius(axes) * CoordSysUtils.getZoom(axes) * 2;
		if (NodeAnimationModelEditor.hitTest(node.getPivotPoint(), CoordSysUtils.geom(axes, point), axes, vertexSize, worldMatrix)) {
			mouseOverVertex = true;
		}
	}

	public boolean isMouseOverVertex() {
		return mouseOverVertex;
	}

	@Override
	public void visitIdObject(IdObject object) {
		handleDefaultNode(point, axes, object);
	}

	@Override
	public void camera(Camera camera) {
		System.err.println("CAMERA processed in NodeAnimationModelEditor!!!");
		// if (hitTest(camera.getPosition(), CoordinateSystem.Util.geom(axes, point),
		// axes,
		// programPreferences.getVertexSize(), worldMatrix)) {
		// mouseOverVertex = true;
		// }
		// if (hitTest(camera.getTargetPosition(), CoordinateSystem.Util.geom(axes,
		// point), axes,
		// programPreferences.getVertexSize(), worldMatrix)) {
		// mouseOverVertex = true;
		// }
	}
}
