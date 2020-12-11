package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.actions.model.material.AddMaterialAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentGeosetMaterialPanel extends JPanel {
	Map<String, Material> materialMap = new HashMap<>();
	int materialNumber = 0;
	private JComboBox<String> materialChooser;
	private UndoActionListener undoActionListener;
	private ModelStructureChangeListener modelStructureChangeListener;
	private boolean listenersEnabled = true;
	private int currentlyDisplayedMaterialCount = 0;
	private JButton cloneMaterial;


	public ComponentGeosetMaterialPanel() {
		final JPanel leftHandSettingsPanel = new JPanel();

		leftHandSettingsPanel.setLayout(new MigLayout());
		materialChooser = new JComboBox<>();
		cloneMaterial = new JButton();
		add(materialChooser);
	}

	private void updateMaterialChooserBox(Geoset geoset) {
		remove(materialChooser);
		materialChooser = new JComboBox<>(getMaterials(geoset));
		materialChooser.addActionListener(e -> chooseMaterial(geoset));
		materialChooser.setSelectedIndex(materialNumber);
		add(materialChooser);
	}

	private String[] getMaterials(Geoset geoset) {
		materialMap = new HashMap<>();
		Material material = geoset.getMaterial();

		List<Material> materialList = geoset.getParentModel().getMaterials();

		for (int i = 0; i < materialList.size(); i++) {
			materialMap.put("Material " + i, materialList.get(i));
			if (material.equals(materialList.get(i))) {
				materialNumber = i;
			}
		}

		return materialMap.keySet().toArray(String[]::new);
	}


	private void chooseMaterial(Geoset geoset) {
		if (listenersEnabled) {
			Material material = materialMap.get(materialChooser.getSelectedItem().toString());
			geoset.setMaterial(material);
		}
	}

	private void copyMaterial(Geoset geoset, ModelViewManager modelViewManager) {

		if (listenersEnabled) {
			new AddMaterialAction(geoset.getMaterial(), modelViewManager, modelStructureChangeListener).redo();
			updateMaterialChooserBox(geoset);
			materialChooser.setSelectedIndex(materialMap.size() - 1);
		}
	}

	public void setMaterialChooser(Geoset geoset, final ModelViewManager modelViewManager,
	                               final UndoActionListener undoActionListener,
	                               final ModelStructureChangeListener modelStructureChangeListener) {
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		listenersEnabled = false;
		int materialCount = geoset.getParentModel().getMaterials().size();

		if (currentlyDisplayedMaterialCount != materialCount) {
			updateMaterialChooserBox(geoset);

			remove(cloneMaterial);
			cloneMaterial = new JButton("Clone This Material");
			cloneMaterial.addActionListener(e -> copyMaterial(geoset, modelViewManager));
			add(cloneMaterial);

			revalidate();
			repaint();
			currentlyDisplayedMaterialCount = materialCount;
		}

		materialChooser.setSelectedItem(geoset.getMaterial().getName());

		listenersEnabled = true;
	}
}