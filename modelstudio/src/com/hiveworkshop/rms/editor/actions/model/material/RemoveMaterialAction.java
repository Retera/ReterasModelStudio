package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.ArrayList;
import java.util.List;

public class RemoveMaterialAction implements UndoAction {
	private final Material material;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;
	private int index;
	private final List<Geoset> affectedGeosets;

	public RemoveMaterialAction(final Material material,
	                            final EditableModel model,
	                            final ModelStructureChangeListener changeListener) {
		this.material = material;
		this.model = model;
		this.changeListener = changeListener;
		affectedGeosets = new ArrayList<>();
	}

	@Override
	public UndoAction undo() {
		model.getMaterials().add(index, material);
		setMaterialForAffectedGeosets(material);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		// To remove the chosen instance of the material, if clones exists.
		// This keeps the expected order of the material list in the model view
		List<Material> materials = model.getMaterials();
		getAffectedGeosets();

		boolean materialRemoved = false;
		for (int i = 0; i < materials.size(); i++) {
			if (materials.get(i) == material) {
				index = i;
				materials.remove(i);
				materialRemoved = true;
				break;
			}
		}
		if (!materialRemoved) {
			index = model.getMaterials().indexOf(material);
			model.getMaterials().remove(material);
		}

		removeGeosetUsers();

		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	private void getAffectedGeosets() {
		List<Geoset> geosets = model.getGeosets();
		for (Geoset geoset : geosets) {
			if (geoset.getMaterial() == material) {
				affectedGeosets.add(geoset);
			}
		}
	}

	private void removeGeosetUsers() {
		// checks for a duplicate instance of the material.
		// if found sets material of affected geosets to the duplicate material,
		// else sets geoset materials to the first material
		Material replacementMaterial;
		int materialCopyIndex = model.getMaterials().indexOf(material);
		if (materialCopyIndex != -1) {
			replacementMaterial = model.getMaterials().get(materialCopyIndex);
		} else {
			replacementMaterial = model.getMaterials().get(0);
		}
		setMaterialForAffectedGeosets(replacementMaterial);
	}

	private void setMaterialForAffectedGeosets(Material material) {
		for (Geoset geoset : affectedGeosets) {
			geoset.setMaterial(material);
		}
	}

	@Override
	public String actionName() {
		return "remove Material";
	}

}
