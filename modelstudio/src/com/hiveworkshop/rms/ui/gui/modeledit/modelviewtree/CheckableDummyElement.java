package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import java.util.HashSet;
import java.util.Set;

public class CheckableDummyElement extends CheckableDisplayElement<String> {
	Set<Geoset> tempGeosetList = new HashSet<>();
	Set<IdObject> tempIdObjectList = new HashSet<>();

	private static final String MESH = "Mesh";
	private static final String NODES = "Nodes";
	private static final String CAMERAS = "Cameras";

	//	List<Geoset> tempGeosetList = new ArrayList<>();
//	List<IdObject> tempIdObjectList = new ArrayList<>();
	public CheckableDummyElement(ModelHandler modelHandler, String name) {
		super(modelHandler.getModelView(), name);
	}

	@Override
	protected void setChecked(String item, ModelView modelView, boolean checked) {
		System.out.println(item + " set checked: " + checked);
		//			updateMeshState(modelViewManager, checked);
		if (item.equals("Nodes")) {
//			updateNodesState(modelViewManager, checked);
		}
	}
	@Override
	public void setEditable(boolean editable){
		switch (item) {
			case MESH -> modelView.setGeosetsEditable(editable);
			case NODES -> modelView.setIdObjectsVisible(editable);
			case CAMERAS -> modelView.setCamerasVisible(editable);
		}
	}
	@Override
	public void setVisible(boolean visible){
		switch (item) {
			case MESH -> modelView.setGeosetsVisible(visible);
			case NODES -> modelView.setIdObjectsVisible(visible);
			case CAMERAS -> modelView.setCamerasVisible(visible);
		}
	}

	@Override
	public boolean isEditable(){
		return switch (item) {
			case MESH -> modelView.isGeosetsEditable();
			case NODES -> modelView.isIdObjectsVisible();
			case CAMERAS -> modelView.isCamerasVisible();
			default -> false;
		};
	}
	@Override
	public boolean isVisible(){
		return switch (item) {
			case MESH -> modelView.isGeosetsVisible();
			case NODES -> modelView.isIdObjectsVisible();
			case CAMERAS -> modelView.isCamerasVisible();
			default -> false;
		};
	}

	private void updateNodesState(ModelView modelViewManager, boolean checked) {
		System.out.println("1 tempIdObjectList: " + tempIdObjectList.size());
		if (!checked) {
			tempIdObjectList.addAll(modelViewManager.getEditableIdObjects());
		}
		for (IdObject idObject : tempIdObjectList) {
			if (checked) {
				System.out.println("object visible: " + idObject.getName());
				modelViewManager.makeIdObjectEditable(idObject);
			} else {
				modelViewManager.makeIdObjectNotVisible(idObject);
			}
		}
		if (checked) {
			tempIdObjectList.clear();
		}
		System.out.println("2 tempIdObjectList: " + tempIdObjectList.size());
	}

	private void updateMeshState(ModelView modelViewManager, boolean checked) {
		System.out.println("1 tempGeosetList: " + tempGeosetList.size());
		if (!checked) {
			tempGeosetList.addAll(modelViewManager.getEditableGeosets());
		}
		modelViewManager.makeGeosetEditable(checked, tempGeosetList);
//		for (Geoset geoset : tempGeosetList) {
//			modelViewManager.makeGeosetEditable(checked, geoset);
////			if (checked) {
////				modelViewManager.makeGeosetEditable(geoset);
////			} else {
////				modelViewManager.makeGeosetNotEditable(geoset);
////			}
//		}
		if (checked) {
			tempGeosetList.clear();
		}
		System.out.println("2 tempGeosetList: " + tempGeosetList.size());
	}

	//	public void removeElement(Named element){
	public void removeElement(Object element) {
		if (element instanceof Geoset) {
			tempGeosetList.remove(element);
		} else if (element instanceof IdObject) {
			tempIdObjectList.remove(element);
		}
	}

	public void clearElementList(Object element) {
		if (element instanceof Geoset) {
			tempGeosetList.clear();
		} else if (element instanceof IdObject) {
			tempIdObjectList.clear();
		}
	}

	@Override
	protected String getName(String item, ModelView modelView) {
		return item;
	}
}