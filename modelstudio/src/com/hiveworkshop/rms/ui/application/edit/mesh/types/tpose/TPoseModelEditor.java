package com.hiveworkshop.rms.ui.application.edit.mesh.types.tpose;

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
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class TPoseModelEditor extends AbstractModelEditor<IdObject> {

	public TPoseModelEditor(SelectionManager<IdObject> selectionManager,
	                        ModelStructureChangeListener structureChangeListener,
	                        ModelHandler modelHandler) {
		super(selectionManager, structureChangeListener, modelHandler);
	}

	@Override
	public void selectByVertices(Collection<? extends Vec3> newSelection) {
		Set<IdObject> newlySelectedPivots = new HashSet<>();
		for (IdObject object : modelView.getEditableIdObjects()) {
			if (newSelection.contains(object.getPivotPoint())) {
				newlySelectedPivots.add(object);
			}
			if (object instanceof CollisionShape) {
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					if (newSelection.contains(vertex)) {
						newlySelectedPivots.add(object);
					}
				}
			}
		}
		modelView.setSelectedIdObjects(newlySelectedPivots);
	}

	@Override
	protected List<IdObject> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<IdObject> selectedItems = new ArrayList<>();

		for (IdObject object : modelView.getEditableIdObjects()) {
			double vertexSize = object.getClickRadius(coordinateSystem) * coordinateSystem.getZoom();
			if (AbstractModelEditor.hitTest(min, max, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object);
			}
			if (object instanceof CollisionShape) {
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					if (AbstractModelEditor.hitTest(min, max, vertex, coordinateSystem, IdObject.DEFAULT_CLICK_RADIUS)) {
						selectedItems.add(object);
					}
				}
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
	protected UndoAction buildHideComponentAction(List<? extends CheckableDisplayElement<?>> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		List<IdObject> previousSelection = new ArrayList<>(modelView.getSelectedIdObjects());
		Runnable truncateSelectionRunnable = () -> modelView.removeSelectedIdObjects(modelView.getModel().getIdObjects());
		Runnable unTruncateSelectionRunnable = () -> modelView.setSelectedIdObjects(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
