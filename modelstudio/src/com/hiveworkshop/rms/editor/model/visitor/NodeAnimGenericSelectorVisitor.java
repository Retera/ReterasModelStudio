package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;

import java.awt.geom.Rectangle2D;
import java.util.List;

public class NodeAnimGenericSelectorVisitor implements IdObjectVisitor {
	private List<IdObject> selectedItems;
	private Rectangle2D area;
	private CoordinateSystem coordinateSystem;
	private RenderModel renderModel;

	public NodeAnimGenericSelectorVisitor reset(RenderModel renderModel, List<IdObject> selectedItems, Rectangle2D area, CoordinateSystem coordinateSystem) {
		this.renderModel = renderModel;
		this.selectedItems = selectedItems;
		this.area = area;
		this.coordinateSystem = coordinateSystem;
		return this;
	}

	@Override
	public void visitIdObject(IdObject object) {
		double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.getZoom(coordinateSystem) * 2;
		NodeAnimationModelEditor.hitTest(selectedItems, area, object.getPivotPoint(), coordinateSystem, vertexSize, object, renderModel);
	}

	@Override
	public void camera(Camera camera) {
		System.err.println("Attempted to process camera with Node Animation Editor generic selector!!!");
	}

}
