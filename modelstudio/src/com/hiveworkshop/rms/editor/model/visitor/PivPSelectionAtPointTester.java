package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public class PivPSelectionAtPointTester implements IdObjectVisitor {
	private CoordinateSystem axes;
	private Point point;
	private boolean mouseOverVertex;
	private ProgramPreferences programPreferences;

	public PivPSelectionAtPointTester reset(ProgramPreferences programPreferences, CoordinateSystem axes, Point point) {
		this.programPreferences = programPreferences;
		this.axes = axes;
		this.point = point;
		mouseOverVertex = false;
		return this;
	}

	private void handleDefaultNode(Point point, CoordinateSystem axes, IdObject node) {
		double vertexSize = node.getClickRadius(axes) * CoordSysUtils.getZoom(axes) * 2;
		if (AbstractModelEditor.hitTest(node.getPivotPoint(), CoordSysUtils.geom(axes, point), axes, vertexSize)) {
			mouseOverVertex = true;
		}
	}

	@Override
	public void visitIdObject(IdObject object) {
		handleDefaultNode(point, axes, object);
		if (object instanceof CollisionShape) {
			for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
				int vertexSize = IdObject.DEFAULT_CLICK_RADIUS;
				if (AbstractModelEditor.hitTest(vertex, CoordSysUtils.geom(axes, point), axes, vertexSize)) {
					mouseOverVertex = true;
				}
			}
		}
	}

	@Override
	public void camera(Camera camera) {
		int vertexSize = programPreferences.getVertexSize();
		if (AbstractModelEditor.hitTest(camera.getPosition(), CoordSysUtils.geom(axes, point), axes, vertexSize)) {
			mouseOverVertex = true;
		}
		if (AbstractModelEditor.hitTest(camera.getTargetPosition(), CoordSysUtils.geom(axes, point), axes, vertexSize)) {
			mouseOverVertex = true;
		}
	}

	public boolean isMouseOverVertex() {
		return mouseOverVertex;
	}
}
