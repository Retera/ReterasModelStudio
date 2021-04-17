package com.hiveworkshop.rms.ui.application.actions.model.material;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.ArrayList;
import java.util.List;

public class RemoveMaterialAction implements UndoAction {
	private final Material material;
	private final ModelViewManager modelViewManager;
	private final ModelStructureChangeListener structureChangeListener;
	private int index;
	private final List<Geoset> affectedGeosets;

	public RemoveMaterialAction(final Material material,
	                            final ModelViewManager modelViewManager,
	                            final ModelStructureChangeListener modelStructureChangeListener) {
		this.material = material;
		this.modelViewManager = modelViewManager;
		this.structureChangeListener = modelStructureChangeListener;
		affectedGeosets = new ArrayList<>();
	}

	@Override
	public void undo() {
		modelViewManager.getModel().getMaterials().add(index, material);
		setMaterialForAffectedGeosets(material);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public void redo() {
		// To remove the chosen instance of the material, if clones exists.
		// This keeps the expected order of the material list in the model view
		List<Material> materials = modelViewManager.getModel().getMaterials();
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
			index = modelViewManager.getModel().getMaterials().indexOf(material);
			modelViewManager.getModel().getMaterials().remove(material);
		}

		removeGeosetUsers();

		structureChangeListener.materialsListChanged();
	}

	private void getAffectedGeosets() {
		List<Geoset> geosets = modelViewManager.getModel().getGeosets();
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
		int materialCopyIndex = modelViewManager.getModel().getMaterials().indexOf(material);
		if (materialCopyIndex != -1) {
			replacementMaterial = modelViewManager.getModel().getMaterials().get(materialCopyIndex);
		} else {
			replacementMaterial = modelViewManager.getModel().getMaterials().get(0);
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
