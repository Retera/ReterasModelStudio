package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.geom.Rectangle2D;
import java.util.List;

public class PivPGenericSelectorVisitor implements IdObjectVisitor {
	private List<Vec3> selectedItems;
	private Rectangle2D area;
	private CoordinateSystem coordinateSystem;
	private ProgramPreferences programPreferences;

	public PivPGenericSelectorVisitor reset(ProgramPreferences programPreferences, List<Vec3> selectedItems, Rectangle2D area, CoordinateSystem coordinateSystem) {
		this.programPreferences = programPreferences;
		this.selectedItems = selectedItems;
		this.area = area;
		this.coordinateSystem = coordinateSystem;
		return this;
	}

	private void handleDefaultNode(IdObject object) {
		double vertexSize = object.getClickRadius(coordinateSystem) * CoordSysUtils.getZoom(coordinateSystem) * 2;
		if (AbstractModelEditor.hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
			selectedItems.add(object.getPivotPoint());
		}
	}

	@Override
	public void visitIdObject(IdObject object) {
		handleDefaultNode(object);

		if (object instanceof CollisionShape) {
			for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
				int vertexSize = IdObject.DEFAULT_CLICK_RADIUS;
				if (AbstractModelEditor.hitTest(area, vertex, coordinateSystem, vertexSize)) {
					selectedItems.add(vertex);
				}
			}
		}
	}

	@Override
	public void camera(Camera object) {
		int vertexSize = programPreferences.getVertexSize();
		if (AbstractModelEditor.hitTest(area, object.getPosition(), coordinateSystem, vertexSize)) {
			selectedItems.add(object.getPosition());
		}
		if (AbstractModelEditor.hitTest(area, object.getTargetPosition(), coordinateSystem, vertexSize)) {
			selectedItems.add(object.getTargetPosition());
		}
	}
}
