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

public class TPoseGenericSelectorVisitor implements IdObjectVisitor {
	ProgramPreferences programPreferences;
	private List<IdObject> selectedItems;
	private Rectangle2D area;
	private CoordinateSystem coordinateSystem;

	public TPoseGenericSelectorVisitor reset(ProgramPreferences programPreferences, List<IdObject> selectedItems, Rectangle2D area, CoordinateSystem coordinateSystem) {
		this.programPreferences = programPreferences;
		this.selectedItems = selectedItems;
		this.area = area;
		this.coordinateSystem = coordinateSystem;
		return this;
	}

	@Override
	public void visitIdObject(IdObject object) {
		double vertexSize = object.getClickRadius(coordinateSystem) * CoordSysUtils.getZoom(coordinateSystem);
		if (AbstractModelEditor.hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
			selectedItems.add(object);
		}
		if (object instanceof CollisionShape) {
			for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
				if (AbstractModelEditor.hitTest(area, vertex, coordinateSystem, IdObject.DEFAULT_CLICK_RADIUS)) {
					selectedItems.add(object);
				}
			}
		}
	}

	@Override
	public void camera(Camera object) {
	}
}
