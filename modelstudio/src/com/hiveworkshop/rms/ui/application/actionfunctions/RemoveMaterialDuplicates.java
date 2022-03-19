package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoveMaterialDuplicates extends ActionFunction {
	public RemoveMaterialDuplicates(){
		super(TextKey.REMOVE_MATERIALS_DUPLICATES, () -> removeMaterialDuplicates());
		setMenuItemMnemonic(KeyEvent.VK_S);

	}

	public static void removeMaterialDuplicates() {
		EditableModel model = ProgramGlobals.getCurrentModelPanel().getModel();
		List<Material> materials = model.getMaterials();

		Map<Material, Material> sameMaterialMap = new HashMap<>();
		for (int i = 0; i < materials.size(); i++) {
			Material material1 = materials.get(i);
			for (int j = i + 1; j < materials.size(); j++) {
				Material material2 = materials.get(j);
				System.out.println(material1.getName() + " == " + material2.getName());
				if (material1.equals(material2)) {
					if (!sameMaterialMap.containsKey(material2)) {
						sameMaterialMap.put(material2, material1);
					}
				}
			}
		}

		List<Geoset> geosets = model.getGeosets();
		for (Geoset geoset : geosets) {
			if (sameMaterialMap.containsKey(geoset.getMaterial())) {
				geoset.setMaterial(sameMaterialMap.get(geoset.getMaterial()));
			}
		}

		materials.removeAll(sameMaterialMap.keySet());
		ModelStructureChangeListener.changeListener.materialsListChanged();
	}

}
