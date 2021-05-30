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
//			Object item = element.getItem();
//			if (item instanceof Geoset){
//				modelView.makeGeosetEditable((Geoset) item);
//			}
//			if (item instanceof IdObject){
//				modelView.makeIdObjectEditable((IdObject) item);
//			}
//			if (item instanceof Camera){
//				modelView.makeCameraEditable((Camera) item);
//			}
		}
	}

	public void makeNotEditable() {
		for (final CheckableDisplayElement<?> element : elements) {
			element.setChecked(false);
//			Object item = element.getItem();
//			if (item instanceof Geoset){
//				modelView.makeGeosetNotEditable((Geoset) item);
//			}
//			if (item instanceof IdObject){
//				modelView.makeIdObjectNotEditable((IdObject) item);
//			}
//			if (item instanceof Camera){
//				modelView.makeCameraNotEditable((Camera) item);
//			}
		}
	}

	public UndoAction hideComponent(ModelView modelView,
	                                Runnable refreshGUIRunnable) {

		List<GeosetVertex> previousVertSelection = new ArrayList<>(modelView.getSelectedVertices());
		List<IdObject> previousObjSelection = new ArrayList<>(modelView.getSelectedIdObjects());
		List<Camera> previousCamSelection = new ArrayList<>(modelView.getSelectedCameras());
		List<GeosetVertex> vertsToHide = new ArrayList<>();
		List<Geoset> geosetsToHide = new ArrayList<>();
		List<IdObject> objsToHide = new ArrayList<>();
		List<Camera> camsToHide = new ArrayList<>();
		for (CheckableDisplayElement<?> component : elements) {
			Object item = component.getItem();
			if (item instanceof Geoset) {
				vertsToHide.addAll(((Geoset) item).getVertices());
				geosetsToHide.add((Geoset) item);
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

//			geosetsToHide.forEach(modelView::makeGeosetNotEditable);
//			objsToHide.forEach(modelView::makeIdObjectNotEditable);
//			camsToHide.forEach(modelView::makeCameraNotEditable);

			geosetsToHide.forEach(modelView::makeGeosetNotVisible);
			objsToHide.forEach(modelView::makeIdObjectNotVisible);
			camsToHide.forEach(modelView::makeCameraNotVisible);

		};
		Runnable unTruncateSelectionRunnable = () -> {
			geosetsToHide.forEach(modelView::makeGeosetVisible);
			objsToHide.forEach(modelView::makeIdObjectVisible);
			camsToHide.forEach(modelView::makeCameraVisible);

//			geosetsToHide.forEach(modelView::makeGeosetEditable);
//			objsToHide.forEach(modelView::makeIdObjectEditable);
//			camsToHide.forEach(modelView::makeCameraEditable);

			modelView.setSelectedVertices(previousVertSelection);
			modelView.setSelectedIdObjects(previousObjSelection);
			modelView.setSelectedCameras(previousCamSelection);
		};

		UndoAction hideComponentAction = new MakeNotEditableAction(this, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
		hideComponentAction.redo();
		return hideComponentAction;
	}

	public UndoAction showComponent(ModelView modelView,
	                                Runnable refreshGUIRunnable) {

//		List<GeosetVertex> previousVertSelection = new ArrayList<>(modelView.getSelectedVertices());
//		List<IdObject> previousObjSelection = new ArrayList<>(modelView.getSelectedIdObjects());
//		List<Camera> previousCamSelection = new ArrayList<>(modelView.getSelectedCameras());

		List<Geoset> geosetsToShow = new ArrayList<>();
		List<IdObject> objsToShow = new ArrayList<>();
		List<Camera> camsToShow = new ArrayList<>();

		for (CheckableDisplayElement<?> component : elements) {
			Object item = component.getItem();
			if (item instanceof Geoset) {
				geosetsToShow.add((Geoset) item);
			}
			if (item instanceof Camera) {
				camsToShow.add((Camera) item);
			} else if (item instanceof IdObject) {
				objsToShow.add((IdObject) item);
//                if (item instanceof CollisionShape) {
//                    vertsToHide.addAll(((CollisionShape) item).getVertices());
//                }
			}
		}
		Runnable truncateSelectionRunnable = () -> {
			geosetsToShow.forEach(modelView::makeGeosetVisible);
			objsToShow.forEach(modelView::makeIdObjectVisible);
			camsToShow.forEach(modelView::makeCameraVisible);

//			geosetsToShow.forEach(modelView::makeGeosetEditable);
//			objsToShow.forEach(modelView::makeIdObjectEditable);
//			camsToShow.forEach(modelView::makeCameraEditable);
		};
		Runnable unTruncateSelectionRunnable = () -> {
			geosetsToShow.forEach(modelView::makeGeosetNotVisible);
			objsToShow.forEach(modelView::makeIdObjectNotVisible);
			camsToShow.forEach(modelView::makeCameraNotVisible);

//			geosetsToShow.forEach(modelView::makeGeosetNotEditable);
//			objsToShow.forEach(modelView::makeIdObjectNotEditable);
//			camsToShow.forEach(modelView::makeCameraNotEditable);

		};
		return new MakeEditableAction(this, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable).redo();
	}
}
