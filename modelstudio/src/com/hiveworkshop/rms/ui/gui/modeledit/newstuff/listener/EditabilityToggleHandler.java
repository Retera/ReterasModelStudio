package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EditabilityToggleHandler {
	private final Collection<CheckableDisplayElement<?>> elements;

	public EditabilityToggleHandler(final Collection<CheckableDisplayElement<?>> elements) {
		this.elements = elements;
	}

	public void makeEditable() {
		for (final CheckableDisplayElement<?> element : elements) {
			element.setChecked(true);
		}
	}

	public void makeNotEditable() {
		for (final CheckableDisplayElement<?> element : elements) {
			element.setChecked(false);
		}
	}

	public UndoAction hideComponent(ModelView modelView,
	                                Runnable refreshGUIRunnable) {

		List<GeosetVertex> previousVertSelection = new ArrayList<>(modelView.getSelectedVertices());
		List<IdObject> previousObjSelection = new ArrayList<>(modelView.getSelectedIdObjects());
		List<Camera> previousCamSelection = new ArrayList<>(modelView.getSelectedCameras());
		List<GeosetVertex> vertsToHide = new ArrayList<>();
		List<IdObject> objsToHide = new ArrayList<>();
		List<Camera> camsToHide = new ArrayList<>();
		for (CheckableDisplayElement<?> component : elements) {
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

		UndoAction hideComponentAction = new MakeNotEditableAction(this, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
		hideComponentAction.redo();
		return hideComponentAction;
	}

	public UndoAction showComponent() {
		return new MakeEditableAction(this).redo();
	}
}
