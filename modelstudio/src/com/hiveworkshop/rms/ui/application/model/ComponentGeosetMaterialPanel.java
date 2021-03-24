package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.actions.model.material.AddMaterialAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ComponentGeosetMaterialPanel extends JPanel {
	Map<String, Material> materialMap = new TreeMap<>();
	int materialNumber = -1;
	private JComboBox<String> materialChooser;
	private UndoActionListener undoActionListener;
	private ModelStructureChangeListener modelStructureChangeListener;
	private boolean listenersEnabled = true;
	private int currentlyDisplayedMaterialCount = 0;
	private JButton cloneMaterial;


	public ComponentGeosetMaterialPanel() {
		final JPanel leftHandSettingsPanel = new JPanel(new MigLayout("ins 0"));

		materialChooser = new JComboBox<>();
		cloneMaterial = new JButton();
		add(materialChooser);
	}

	private void updateMaterialChooserBox(Geoset geoset) {
//		System.out.println("updateChooser");
		remove(materialChooser);
		materialChooser = new JComboBox<>(getMaterials(geoset));
		materialChooser.addActionListener(e -> chooseMaterial(geoset));
		checkIndex(geoset);
		materialChooser.setSelectedIndex(materialNumber);
		add(materialChooser, "wrap");
	}

	private String[] getMaterials(Geoset geoset) {
//		System.out.println("getMaterials");
//		materialMap = new HashMap<>();
		materialMap.clear();
		Material material = geoset.getMaterial();

		List<Material> materialList = geoset.getParentModel().getMaterials();
		for (int i = 0; i < materialList.size(); i++) {
//			System.out.println(materialList.get(i).getName());
//			materialMap.put("\u2116 " + i + " " + materialList.get(i).getName(), materialList.get(i));
			materialMap.put("# " + i + " " + materialList.get(i).getName(), materialList.get(i));
//			materialMap.put(materialList.get(i).getName(), materialList.get(i));
//			materialMap.put("Material " + i, materialList.get(i));
			if (materialNumber == -1 && material.equals(materialList.get(i))) {
				materialNumber = i;
			}
		}

		return materialMap.keySet().toArray(String[]::new);
	}

	private void checkIndex(Geoset geoset) {
		Material gMaterial = geoset.getMaterial();
		if (materialNumber >= materialChooser.getItemCount()) {
			materialNumber = -1;
		} else if (materialChooser.getItemCount() > 0) {
//			System.out.println("more than 0 materials");
			Material material = materialMap.get(materialChooser.getSelectedItem().toString());
			if (gMaterial != material) {
//				System.out.println("not same material");
				materialNumber = -1;
			}
		}
		if (materialNumber == -1) {
			List<Material> materials = new ArrayList<>(materialMap.values());

			int tempNum = -1;
			for (int i = 0; i < materials.size(); i++) {
				if (tempNum == -1 && gMaterial.equals(materials.get(i))) {
//					System.out.println(gMaterial.getName() + " equals " + i + " " + materials.get(i).getName());
					tempNum = i;
				}
				if (geoset.getMaterial() == materials.get(i)) {
//					System.out.println(gMaterial.getName() + " == " + i + " " + materials.get(i).getName());
//					geoset.setMaterial(materials.get(i));
					materialNumber = i;

					break;
				}
			}
			if (materialNumber == -1) {
				materialNumber = Math.max(tempNum, 0);
				geoset.setMaterial(materials.get(materialNumber));
			}
		}
	}


	private void chooseMaterial(Geoset geoset) {
		if (listenersEnabled) {
			Material material = materialMap.get(materialChooser.getSelectedItem().toString());
			materialNumber = materialChooser.getSelectedIndex();
			geoset.setMaterial(material);
		}
	}

	private void cloneMaterial(Geoset geoset, ModelViewManager modelViewManager) {

		if (listenersEnabled) {
			Material material = new Material(geoset.getMaterial());
			AddMaterialAction addMaterialAction = new AddMaterialAction(material, modelViewManager, modelStructureChangeListener);
			undoActionListener.pushAction(addMaterialAction);
			addMaterialAction.redo();
//			geoset.setMaterial(material);
			updateMaterialChooserBox(geoset);

//			materialChooser.setSelectedIndex(materialMap.size() - 1);
		}
	}

	public void setMaterialChooser(Geoset geoset, final ModelViewManager modelViewManager,
	                               final UndoActionListener undoActionListener,
	                               final ModelStructureChangeListener modelStructureChangeListener) {
//		System.out.println("setMaterialChooser");
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		listenersEnabled = false;
		int materialCount = geoset.getParentModel().getMaterials().size();

		if (currentlyDisplayedMaterialCount != materialCount) {
			updateMaterialChooserBox(geoset);

			remove(cloneMaterial);
			cloneMaterial = new JButton("Clone This Material");
			cloneMaterial.addActionListener(e -> cloneMaterial(geoset, modelViewManager));
			add(cloneMaterial);

			revalidate();
			repaint();
			currentlyDisplayedMaterialCount = materialCount;
		}

		materialChooser.setSelectedItem(geoset.getMaterial().getName());

		listenersEnabled = true;
	}
}