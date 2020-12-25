package com.hiveworkshop.rms.ui.application.actions.model.material;

import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.List;

public class RemoveMaterialAction implements UndoAction {
	private final Material material;
	private final ModelViewManager modelViewManager;
	private final ModelStructureChangeListener structureChangeListener;
	private int index;

	public RemoveMaterialAction(final Material material,
	                            final ModelViewManager modelViewManager,
	                            final ModelStructureChangeListener modelStructureChangeListener) {
		this.material = material;
		this.modelViewManager = modelViewManager;
		this.structureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void undo() {
		modelViewManager.getModel().getMaterials().add(index, material);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public void redo() {
		// To remove the chosen instance of the material, if clones exists.
		// This keeps the expected order of the material list in the model view
		List<Material> materials = modelViewManager.getModel().getMaterials();
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
		structureChangeListener.materialsListChanged();
	}

	@Override
	public String actionName() {
		return "remove Material";
	}

}
