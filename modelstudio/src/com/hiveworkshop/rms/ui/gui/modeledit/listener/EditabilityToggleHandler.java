//package com.hiveworkshop.rms.ui.gui.modeledit.listener;
//
//import com.hiveworkshop.rms.editor.actions.UndoAction;
//import com.hiveworkshop.rms.editor.actions.selection.SetEditableMultipleAction;
//import com.hiveworkshop.rms.editor.model.*;
//import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
//import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
//import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//public class EditabilityToggleHandler {
//	private final Collection<CheckableDisplayElement<?>> elements;
//
//	public EditabilityToggleHandler(final Collection<CheckableDisplayElement<?>> elements) {
//		this.elements = elements;
//	}
//
//	public void makeEditable() {
//		for (final CheckableDisplayElement<?> element : elements) {
//			element.setChecked(true);
////			Object item = element.getItem();
////			if (item instanceof Geoset){
////				modelView.makeGeosetEditable((Geoset) item);
////			}
////			if (item instanceof IdObject){
////				modelView.makeIdObjectEditable((IdObject) item);
////			}
////			if (item instanceof Camera){
////				modelView.makeCameraEditable((Camera) item);
////			}
//		}
//	}
//
//	public void makeNotEditable() {
//		for (final CheckableDisplayElement<?> element : elements) {
//			element.setChecked(false);
////			Object item = element.getItem();
////			if (item instanceof Geoset){
////				modelView.makeGeosetNotEditable((Geoset) item);
////			}
////			if (item instanceof IdObject){
////				modelView.makeIdObjectNotEditable((IdObject) item);
////			}
////			if (item instanceof Camera){
////				modelView.makeCameraNotEditable((Camera) item);
////			}
//		}
//	}
//
//	public UndoAction hideComponent(ModelView modelView,
//	                                Runnable refreshGUIRunnable) {
//
//		List<GeosetVertex> previousVertSelection = new ArrayList<>(modelView.getSelectedVertices());
//		List<IdObject> previousObjSelection = new ArrayList<>(modelView.getSelectedIdObjects());
//		List<Camera> previousCamSelection = new ArrayList<>(modelView.getSelectedCameras());
//		List<GeosetVertex> vertsToHide = new ArrayList<>();
//		List<Geoset> geosetsToHide = new ArrayList<>();
//		List<IdObject> objsToHide = new ArrayList<>();
//		List<Camera> camsToHide = new ArrayList<>();
//		List<Named> objects = new ArrayList<>();
//		for (CheckableDisplayElement<?> component : elements) {
//			Object item = component.getItem();
//			if (item instanceof Geoset) {
//				vertsToHide.addAll(((Geoset) item).getVertices());
//				geosetsToHide.add((Geoset) item);
//				objects.add((Geoset) item);
//			} else if (item instanceof Camera) {
//				camsToHide.add((Camera) item);
//				objects.add((Camera) item);
//			} else if (item instanceof IdObject) {
//				objsToHide.add((IdObject) item);
//				objects.add((IdObject) item);
////                if (item instanceof CollisionShape) {
////                    vertsToHide.addAll(((CollisionShape) item).getVertices());
////                }
//			}
//		}
//		Runnable truncateSelectionRunnable = () -> {
//			modelView.removeSelectedVertices(vertsToHide);
//			modelView.removeSelectedIdObjects(objsToHide);
//			modelView.removeSelectedCameras(camsToHide);
//
////			geosetsToHide.forEach(modelView::makeGeosetNotEditable);
////			objsToHide.forEach(modelView::makeIdObjectNotEditable);
////			camsToHide.forEach(modelView::makeCameraNotEditable);
//
////			geosetsToHide.forEach(modelView::makeGeosetNotVisible);
////			objsToHide.forEach(modelView::makeIdObjectNotVisible);
////			camsToHide.forEach(modelView::makeCameraNotVisible);
//
//			geosetsToHide.forEach(o -> modelView.makeVisible(o, false));
//			objsToHide.forEach(o -> modelView.makeVisible(o, false));
//			camsToHide.forEach(o -> modelView.makeVisible(o, false));
//
//		};
//		Runnable unTruncateSelectionRunnable = () -> {
////			geosetsToHide.forEach(modelView::makeGeosetEditable);
////			objsToHide.forEach(modelView::makeIdObjectEditable);
////			camsToHide.forEach(modelView::makeCameraEditable);
//
////			geosetsToHide.forEach(modelView::makeGeosetVisible);
////			objsToHide.forEach(modelView::makeIdObjectVisible);
////			camsToHide.forEach(modelView::makeCameraVisible);
//			geosetsToHide.forEach(o -> modelView.makeVisible(o, true));
//			objsToHide.forEach(o -> modelView.makeVisible(o, true));
//			camsToHide.forEach(o -> modelView.makeVisible(o, true));
//
////			geosetsToHide.forEach(modelView::makeGeosetEditable);
////			objsToHide.forEach(modelView::makeIdObjectEditable);
////			camsToHide.forEach(modelView::makeCameraEditable);
//
//			modelView.setSelectedVertices(previousVertSelection);
//			modelView.setSelectedIdObjects(previousObjSelection);
//			modelView.setSelectedCameras(previousCamSelection);
//		};
//
////		UndoAction hideComponentAction = new MakeNotEditableAction(this, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
////		hideComponentAction.redo();
////		return hideComponentAction;
//
//		return new SetEditableMultipleAction(objects, true, modelView, ModelStructureChangeListener.changeListener).redo();
//	}
//
//	public UndoAction showComponent(ModelView modelView,
//	                                Runnable refreshGUIRunnable) {
//
////		List<GeosetVertex> previousVertSelection = new ArrayList<>(modelView.getSelectedVertices());
////		List<IdObject> previousObjSelection = new ArrayList<>(modelView.getSelectedIdObjects());
////		List<Camera> previousCamSelection = new ArrayList<>(modelView.getSelectedCameras());
//
//		List<Geoset> geosetsToShow = new ArrayList<>();
//		List<IdObject> objsToShow = new ArrayList<>();
//		List<Camera> camsToShow = new ArrayList<>();
//		List<Named> objects = new ArrayList<>();
//
//		for (CheckableDisplayElement<?> component : elements) {
//			Object item = component.getItem();
//			if (item instanceof Geoset) {
//				geosetsToShow.add((Geoset) item);
//				objects.add((Geoset) item);
//			} else if (item instanceof Camera) {
//				camsToShow.add((Camera) item);
//				objects.add((Camera) item);
//			} else if (item instanceof IdObject) {
//				objsToShow.add((IdObject) item);
//				objects.add((IdObject) item);
////                if (item instanceof CollisionShape) {
////                    vertsToHide.addAll(((CollisionShape) item).getVertices());
////                }
//			}
//		}
//		Runnable truncateSelectionRunnable = () -> {
////			geosetsToShow.forEach(modelView::makeGeosetEditable);
////			objsToShow.forEach(modelView::makeIdObjectEditable);
////			camsToShow.forEach(modelView::makeCameraEditable);
//
//			geosetsToShow.forEach(o -> modelView.makeVisible(o, true));
//			objsToShow.forEach(o -> modelView.makeVisible(o, true));
//			camsToShow.forEach(o -> modelView.makeVisible(o, true));
//
////			geosetsToShow.forEach(modelView::makeGeosetEditable);
////			objsToShow.forEach(modelView::makeIdObjectEditable);
////			camsToShow.forEach(modelView::makeCameraEditable);
//		};
//		Runnable unTruncateSelectionRunnable = () -> {
//			geosetsToShow.forEach(o -> modelView.makeVisible(o, false));
//			objsToShow.forEach(o -> modelView.makeVisible(o, false));
//			camsToShow.forEach(o -> modelView.makeVisible(o, false));
//
////			geosetsToShow.forEach(modelView::makeGeosetNotEditable);
////			objsToShow.forEach(modelView::makeIdObjectNotEditable);
////			camsToShow.forEach(modelView::makeCameraNotEditable);
//
//		};
////		return new MakeEditableAction(this, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable).redo();
//		return new SetEditableMultipleAction(objects, true, modelView, ModelStructureChangeListener.changeListener).redo();
//	}
//}
