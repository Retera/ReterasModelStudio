package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.AddSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.selection.RemoveSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ViewBox;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;

public abstract class AbstractSelectionManager {
	protected ModelView modelView;
	protected SelectionItemTypes selectionMode;
	protected RenderModel editorRenderModel;

	public AbstractSelectionManager(RenderModel editorRenderModel, ModelView modelView, SelectionItemTypes selectionMode) {
		this.modelView = modelView;
		this.selectionMode = selectionMode;
		this.editorRenderModel = editorRenderModel;
	}

	public AbstractSelectionManager setSelectionMode(SelectionItemTypes selectionMode) {
		this.selectionMode = selectionMode;
		return this;
	}

	public Collection<GeosetVertex> getSelectedVertices() {
		return modelView.getSelectedVertices();
	}

	public abstract SelectionBundle getSelectionBundle(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem);

	public abstract SelectionBundle getSelectionBundle(Vec2 min, Vec2 max, Mat4 viewPortAntiRotMat, double sizeAdj);

	public abstract SelectionBundle getSelectionBundle(Vec2 min, Vec2 max, ViewBox viewBox, double sizeAdj);

	public abstract SelectionBundle getSelectionBundle(Vec3 min, Vec3 max, Mat4 viewPortAntiRotMat, double sizeAdj);

	public UndoAction selectStuff(Vec2 min, Vec2 max, SelectionMode selectionMode, CoordinateSystem coordinateSystem) {
		SelectionBundle newSelection = getSelectionBundle(min, max, coordinateSystem);
		return getUndoAction(selectionMode, newSelection);
	}

	public UndoAction selectStuff(Vec2 min, Vec2 max, SelectionMode selectionMode, Mat4 viewPortAntiRotMat, double sizeAdj) {
		SelectionBundle newSelection = getSelectionBundle(min, max, viewPortAntiRotMat, sizeAdj);
		return getUndoAction(selectionMode, newSelection);
	}

	public UndoAction selectStuff(Vec2 min, Vec2 max, SelectionMode selectionMode, ViewBox viewBox, double sizeAdj) {
		SelectionBundle newSelection = getSelectionBundle(min, max, viewBox, sizeAdj);
		return getUndoAction(selectionMode, newSelection);
	}

	public UndoAction selectStuff(Vec3 min, Vec3 max, SelectionMode selectionMode, Mat4 viewPortAntiRotMat, double sizeAdj) {
		SelectionBundle newSelection = getSelectionBundle(min, max, viewPortAntiRotMat, sizeAdj);
		return getUndoAction(selectionMode, newSelection);
	}

	private UndoAction getUndoAction(SelectionMode selectionMode, SelectionBundle newSelection) {
		if (selectionMode == null) {
			if (modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameraNodes())) {
				return null;
			}
			return new SetSelectionUggAction(newSelection, modelView, "select", ModelStructureChangeListener.changeListener);
		}
		return switch (selectionMode) {
			case ADD -> {
				if (newSelection.isEmpty()) {
					yield null;
				}
				yield new AddSelectionUggAction(newSelection, modelView, ModelStructureChangeListener.changeListener);
			}
			case DESELECT -> {
				if (newSelection.isEmpty() || modelView.isEmpty()) {
					yield null;
				}
				yield new RemoveSelectionUggAction(newSelection, modelView, ModelStructureChangeListener.changeListener);
			}
			case SELECT -> {
				if (modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameraNodes())) {
					yield null;
				}
				yield new SetSelectionUggAction(newSelection, modelView, "select", ModelStructureChangeListener.changeListener);
			}
		};
	}

	public boolean isEmpty() {
		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.GROUP
				|| selectionMode == SelectionItemTypes.CLUSTER) {
			return modelView.getSelectedVertices().isEmpty()
					&& modelView.getSelectedIdObjects().isEmpty()
					&& modelView.getSelectedCameraNodes().isEmpty();
		}
		if (selectionMode == SelectionItemTypes.ANIMATE
				|| selectionMode == SelectionItemTypes.TPOSE) {
			return modelView.getSelectedIdObjects().isEmpty() && modelView.getEditableCameraNodes().isEmpty();
		}
		return false;
	}

	public abstract double getCircumscribedSphereRadius(Vec3 sphereCenter, int tvertexLayerId);

	public abstract double getCircumscribedSphereRadius(Vec2 center, int tvertexLayerId);

	public abstract Vec3 getCenter();

	public abstract Vec2 getUVCenter(int tvertexLayerId);

	public abstract Collection<? extends Vec2> getSelectedTVertices(int tvertexLayerId);

	public abstract boolean selectableUnderCursor(Vec2 point, CoordinateSystem axes);

	public abstract boolean selectableUnderCursor(Vec2 point, Mat4 viewPortAntiRotMat, double sizeAdj);

}
