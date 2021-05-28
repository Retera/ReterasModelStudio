package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSelectingEditor implements ModelEditor {
	protected final ModelView modelView;
	protected final SelectionView selectionManager;

	public AbstractSelectingEditor(SelectionView selectionManager, ModelView modelView) {
		this.selectionManager = selectionManager;
		this.modelView = modelView;
	}

	@Override
	public final UndoAction setSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		return selectionManager.setSelectedRegion(min, max, coordinateSystem);
	}

	@Override
	public final UndoAction removeSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		return selectionManager.removeSelectedRegion(min, max, coordinateSystem);
	}

	@Override
	public final UndoAction addSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		return selectionManager.addSelectedRegion(min, max, coordinateSystem);
	}

	@Override
	public UndoAction hideComponent(List<? extends CheckableDisplayElement<?>> selectableComponents,
	                                EditabilityToggleHandler editabilityToggleHandler,
	                                Runnable refreshGUIRunnable) {

		List<GeosetVertex> previousVertSelection = new ArrayList<>(modelView.getSelectedVertices());
		List<IdObject> previousObjSelection = new ArrayList<>(modelView.getSelectedIdObjects());
		List<Camera> previousCamSelection = new ArrayList<>(modelView.getSelectedCameras());
		List<GeosetVertex> vertsToHide = new ArrayList<>();
		List<IdObject> objsToHide = new ArrayList<>();
		List<Camera> camsToHide = new ArrayList<>();
		for (CheckableDisplayElement<?> component : selectableComponents) {
			Object item = component.getItem();
			if (item instanceof Geoset) {
				vertsToHide.addAll(((Geoset) item).getVertices());
			}
			if (item instanceof Camera) {
				camsToHide.add((Camera) item);
			} else if (item instanceof IdObject) {
				objsToHide.add((IdObject) item);
//                if (item instanceof CollisionShape) {
//                    vertsToHide.addAll(((CollisionShape) item).getVertices());
//                }
			}
		}
		for (CheckableDisplayElement<?> component : selectableComponents) {
			Object item = component.getItem();
		}
		Runnable truncateSelectionRunnable = () -> {
			modelView.removeSelectedVertices(vertsToHide);
			modelView.removeSelectedIdObjects(objsToHide);
			modelView.removeSelectedCameras(camsToHide);
		};
		Runnable unTruncateSelectionRunnable = () -> {
			modelView.setSelectedVertices(previousVertSelection);
			modelView.setSelectedIdObjects(previousObjSelection);
			modelView.setSelectedCameras(previousCamSelection);
		};

		UndoAction hideComponentAction = new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
		hideComponentAction.redo();
		return hideComponentAction;
	}

	@Override
	public UndoAction showComponent(EditabilityToggleHandler editabilityToggleHandler) {
		editabilityToggleHandler.makeEditable();
		return new MakeEditableAction(editabilityToggleHandler);
	}
}
