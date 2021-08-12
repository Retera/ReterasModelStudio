package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;

public abstract class AbstractSelectionManager {
	protected ModelView modelView;
	protected SelectionItemTypes selectionMode;

	public AbstractSelectionManager(ModelView modelView, SelectionItemTypes selectionMode) {
		this.modelView = modelView;
		this.selectionMode = selectionMode;
	}

	public AbstractSelectionManager setSelectionMode(SelectionItemTypes selectionMode) {
		this.selectionMode = selectionMode;
		return this;
	}

	public Collection<GeosetVertex> getSelectedVertices() {
		return modelView.getSelectedVertices();
	}

	public abstract Vec3 getCenter();

	public abstract SelectoinUgg genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem);

	public abstract UndoAction setSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem);

	public abstract UndoAction removeSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem);

	public abstract UndoAction addSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem);

	public abstract UndoAction setSelectedRegion(Vec2 min, Vec2 max, Mat4 viewPortMat, double zoom);

	public abstract UndoAction removeSelectedRegion(Vec2 min, Vec2 max, Mat4 viewPortMat, double zoom);

	public abstract UndoAction addSelectedRegion(Vec2 min, Vec2 max, Mat4 viewPortMat, double zoom);

	public void setSelection(SelectoinUgg selectionItem) {
		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.GROUP
				|| selectionMode == SelectionItemTypes.CLUSTER) {
			modelView.setSelectedVertices(selectionItem.getSelectedVertices());
			modelView.setSelectedIdObjects(selectionItem.getSelectedIdObjects());
			modelView.setSelectedCameras(selectionItem.getSelectedCameras());
		}
		if (selectionMode == SelectionItemTypes.ANIMATE
				|| selectionMode == SelectionItemTypes.TPOSE) {
			modelView.setSelectedIdObjects(selectionItem.getSelectedIdObjects());
		}
	}


	public void addSelection(SelectoinUgg selectionItem) {
		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.GROUP
				|| selectionMode == SelectionItemTypes.CLUSTER) {
			modelView.addSelectedVertices(selectionItem.getSelectedVertices());
			modelView.addSelectedIdObjects(selectionItem.getSelectedIdObjects());
			modelView.addSelectedCameras(selectionItem.getSelectedCameras());
		}
		if (selectionMode == SelectionItemTypes.ANIMATE
				|| selectionMode == SelectionItemTypes.TPOSE) {
			modelView.addSelectedIdObjects(selectionItem.getSelectedIdObjects());
		}
	}

	public void removeSelection(SelectoinUgg selectionItem) {
		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.GROUP
				|| selectionMode == SelectionItemTypes.CLUSTER) {
			modelView.removeSelectedVertices(selectionItem.getSelectedVertices());
			modelView.removeSelectedIdObjects(selectionItem.getSelectedIdObjects());
			modelView.removeSelectedCameras(selectionItem.getSelectedCameras());
		}
		if (selectionMode == SelectionItemTypes.ANIMATE
				|| selectionMode == SelectionItemTypes.TPOSE) {
			modelView.removeSelectedIdObjects(selectionItem.getSelectedIdObjects());
		}
	}

	public boolean isEmpty() {
		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.GROUP
				|| selectionMode == SelectionItemTypes.CLUSTER) {
			return modelView.getSelectedVertices().isEmpty()
					&& modelView.getSelectedIdObjects().isEmpty()
					&& modelView.getSelectedCameras().isEmpty();
		}
		if (selectionMode == SelectionItemTypes.ANIMATE
				|| selectionMode == SelectionItemTypes.TPOSE) {
			return modelView.getSelectedIdObjects().isEmpty();
		}
		return false;
	}

	public abstract double getCircumscribedSphereRadius(Vec3 sphereCenter);

	public abstract double getCircumscribedSphereRadius(Vec2 center, int tvertexLayerId);

	public abstract Vec2 getUVCenter(int tvertexLayerId);

	public abstract Collection<? extends Vec2> getSelectedTVertices(int tvertexLayerId);

	public abstract boolean selectableUnderCursor(Vec2 point, CoordinateSystem axes);
	public abstract boolean selectableUnderCursor(Vec2 point, CameraHandler axes);

}
