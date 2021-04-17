package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;

import java.util.HashSet;
import java.util.Set;

public class CheckableDummyElement extends CheckableDisplayElement<String> {
	Set<Geoset> tempGeosetList = new HashSet<>();
	Set<IdObject> tempIdObjectList = new HashSet<>();

	//	List<Geoset> tempGeosetList = new ArrayList<>();
//	List<IdObject> tempIdObjectList = new ArrayList<>();
	public CheckableDummyElement(final ModelViewManager modelViewManager, final String name) {
		super(modelViewManager, name);
	}

	@Override
	protected void setChecked(final String item, final ModelViewManager modelViewManager, final boolean checked) {
		System.out.println(item + " set checked: " + checked);
		if (item.equals("Mesh")) {
//			updateMeshState(modelViewManager, checked);
		}
		if (item.equals("Nodes")) {
//			updateNodesState(modelViewManager, checked);
		}
	}

	private void updateNodesState(ModelViewManager modelViewManager, boolean checked) {
		System.out.println("1 tempIdObjectList: " + tempIdObjectList.size());
		if (!checked) {
			tempIdObjectList.addAll(modelViewManager.getEditableIdObjects());
		}
		for (IdObject idObject : tempIdObjectList) {
			if (checked) {
				System.out.println("object visible: " + idObject.getName());
				modelViewManager.makeIdObjectVisible(idObject);
			} else {
				modelViewManager.makeIdObjectNotVisible(idObject);
			}
		}
		if (checked) {
			tempIdObjectList.clear();
		}
		System.out.println("2 tempIdObjectList: " + tempIdObjectList.size());
	}

	private void updateMeshState(ModelViewManager modelViewManager, boolean checked) {
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
	protected String getName(final String item, final ModelViewManager modelViewManager) {
		return item;
	}
}