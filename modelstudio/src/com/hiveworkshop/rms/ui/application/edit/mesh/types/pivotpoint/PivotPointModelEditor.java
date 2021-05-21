package com.hiveworkshop.rms.ui.application.edit.mesh.types.pivotpoint;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PivotPointModelEditor extends AbstractModelEditor<Vec3> {

	public PivotPointModelEditor(PivotPointSelectionManager selectionManager,
	                             ModelStructureChangeListener structureChangeListener,
	                             ModelHandler modelHandler) {
		super(selectionManager, structureChangeListener, modelHandler);
	}

	@Override
	protected List<Vec3> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<Vec3> selectedItems = new ArrayList<>();

		for (IdObject object : modelView.getEditableIdObjects()) {
			double vertexSize1 = object.getClickRadius(coordinateSystem) * coordinateSystem.getZoom() * 2;
			if (AbstractModelEditor.hitTest(min, max, object.getPivotPoint(), coordinateSystem, vertexSize1)) {
				System.out.println("selected " + object.getName());
				selectedItems.add(object.getPivotPoint());
			}

			if (object instanceof CollisionShape) {
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					int vertexSize = IdObject.DEFAULT_CLICK_RADIUS;
					if (AbstractModelEditor.hitTest(min, max, vertex, coordinateSystem, vertexSize)) {
						selectedItems.add(vertex);
					}
				}
			}
		}
		for (Camera camera : modelView.getEditableCameras()) {
			int vertexSize = ProgramGlobals.getPrefs().getVertexSize();
			if (AbstractModelEditor.hitTest(min, max, camera.getPosition(), coordinateSystem, vertexSize)) {
				selectedItems.add(camera.getPosition());
			}
			if (AbstractModelEditor.hitTest(min, max, camera.getTargetPosition(), coordinateSystem, vertexSize)) {
				selectedItems.add(camera.getTargetPosition());
			}
		}
		return selectedItems;
	}

	@Override
	public boolean canSelectAt(Vec2 point, CoordinateSystem axes) {
		for (IdObject object : modelView.getEditableIdObjects()) {
			double vertexSize1 = object.getClickRadius(axes) * axes.getZoom() * 2;
			if (AbstractModelEditor.hitTest(object.getPivotPoint(), CoordSysUtils.geomV2(axes, point), axes, vertexSize1)) {
				return true;
			}
			if (object instanceof CollisionShape) {
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					int vertexSize = IdObject.DEFAULT_CLICK_RADIUS;
					if (AbstractModelEditor.hitTest(vertex, CoordSysUtils.geomV2(axes, point), axes, vertexSize)) {
						return true;
					}
				}
			}
		}
		for (Camera camera : modelView.getEditableCameras()) {
			int vertexSize = ProgramGlobals.getPrefs().getVertexSize();
			if (AbstractModelEditor.hitTest(camera.getPosition(), CoordSysUtils.geomV2(axes, point), axes, vertexSize)) {
				return true;
			}
			if (AbstractModelEditor.hitTest(camera.getTargetPosition(), CoordSysUtils.geomV2(axes, point), axes, vertexSize)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void selectByVertices(Collection<? extends Vec3> newSelection) {
		List<Vec3> newlySelectedPivots = new ArrayList<>();
		for (IdObject object : modelView.getEditableIdObjects()) {
			if (newSelection.contains(object.getPivotPoint())) {
				newlySelectedPivots.add(object.getPivotPoint());
			}
			if (object instanceof CollisionShape) {
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					if (newSelection.contains(vertex)) {
						newlySelectedPivots.add(vertex);
					}
				}
			}
		}
		for (Camera camera : modelView.getEditableCameras()) {
			if (newSelection.contains(camera.getPosition())) {
				newlySelectedPivots.add(camera.getPosition());
			}
			if (newSelection.contains(camera.getTargetPosition())) {
				newlySelectedPivots.add(camera.getTargetPosition());
			}
		}
		selectionManager.setSelection(newlySelectedPivots);
	}

	@Override
	protected UndoAction buildHideComponentAction(List<? extends CheckableDisplayElement<?>> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		List<Vec3> previousSelection = new ArrayList<>(selectionManager.getSelection());
		List<Vec3> possibleVerticesToTruncate = new ArrayList<>();
		for (CheckableDisplayElement<?> component : selectableComponents) {
			Object item = component.getItem();
			if (item instanceof Camera) {
				possibleVerticesToTruncate.add(((Camera) item).getPosition());
				possibleVerticesToTruncate.add(((Camera) item).getTargetPosition());
			} else if (item instanceof IdObject) {
				possibleVerticesToTruncate.add(((IdObject) item).getPivotPoint());
				if (item instanceof CollisionShape) {
					possibleVerticesToTruncate.addAll(((CollisionShape) item).getVertices());
				}
			}
		}
		Runnable truncateSelectionRunnable = () -> selectionManager.removeSelection(possibleVerticesToTruncate);
		Runnable unTruncateSelectionRunnable = () -> selectionManager.setSelection(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
