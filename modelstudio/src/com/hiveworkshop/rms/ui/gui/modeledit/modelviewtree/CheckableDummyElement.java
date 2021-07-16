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
			case "Mesh" -> modelView.setGeosetsEditable(editable);
			case "Nodes" -> modelView.setIdObjectsVisible(editable);
			case "Cameras" -> modelView.setCamerasVisible(editable);
		}
	}
	@Override
	public void setVisible(boolean visible){
		switch (item) {
			case "Mesh" -> modelView.setGeosetsVisible(visible);
			case "Nodes" -> modelView.setIdObjectsVisible(visible);
			case "Cameras" -> modelView.setCamerasVisible(visible);
		}
	}

	@Override
	public boolean isEditable(){
		return switch (item) {
			case "Mesh" -> modelView.isGeosetsEditable();
			case "Nodes" -> modelView.isIdObjectsVisible();
			case "Cameras" -> modelView.isCamerasVisible();
			default -> false;
		};
	}
	@Override
	public boolean isVisible(){
		return switch (item) {
			case "Mesh" -> modelView.isGeosetsVisible();
			case "Nodes" -> modelView.isIdObjectsVisible();
			case "Cameras" -> modelView.isCamerasVisible();
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
		for (Geoset geoset : tempGeosetList) {
			if (checked) {
				modelViewManager.makeGeosetEditable(geoset);
			} else {
				modelViewManager.makeGeosetNotEditable(geoset);
			}
		}
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