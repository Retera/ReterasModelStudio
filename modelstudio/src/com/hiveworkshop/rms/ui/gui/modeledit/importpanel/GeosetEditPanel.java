package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.util.IterableListModel;

import javax.swing.*;

public class GeosetEditPanel {
	static JPanel makeGeosetPanel(ModelHolderThing mht) {
		JPanel geosetsPanel = new JPanel();

		final IterableListModel<Material> materials = new IterableListModel<>();
		for (Material material : mht.receivingModel.getMaterials()) {
			materials.addElement(material);
		}
		for (Material material : mht.donatingModel.getMaterials()) {
			materials.addElement(material);
		}
		// A list of all materials available for use during this import, in
		// the form of a IterableListModel

		final MaterialListCellRenderer materialsRenderer = new MaterialListCellRenderer(mht.receivingModel);
		// All material lists will know which materials come from the
		// out-of-model source (imported model)

		// Build the geosetTabs list of GeosetPanels
		for (int i = 0; i < mht.receivingModel.getGeosets().size(); i++) {
			final GeosetPanel geoPanel = new GeosetPanel(mht, false, mht.receivingModel, i, materials, materialsRenderer);

			mht.geosetTabs.addTab(mht.receivingModel.getName() + " " + (i + 1), ImportPanel.greenIcon, geoPanel, "Click to modify material data for this geoset.");
		}
		for (int i = 0; i < mht.donatingModel.getGeosets().size(); i++) {
			final GeosetPanel geoPanel = new GeosetPanel(mht, true, mht.donatingModel, i, materials, materialsRenderer);

			mht.geosetTabs.addTab(mht.donatingModel.getName() + " " + (i + 1), ImportPanel.orangeIcon, geoPanel, "Click to modify importing and material data for this geoset.");
		}

		JButton importAllGeos = new JButton("Import All");
		importAllGeos.addActionListener(e -> mht.importAllGeos(true));
		geosetsPanel.add(importAllGeos);

		JButton uncheckAllGeos = new JButton("Leave All");
		uncheckAllGeos.addActionListener(e -> mht.importAllGeos(false));
		geosetsPanel.add(uncheckAllGeos);

		final GroupLayout geosetLayout = new GroupLayout(geosetsPanel);
		geosetLayout.setHorizontalGroup(geosetLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(geosetLayout.createSequentialGroup()
						.addComponent(importAllGeos).addGap(8)
						.addComponent(uncheckAllGeos))
				.addComponent(mht.geosetTabs));
		geosetLayout.setVerticalGroup(geosetLayout.createSequentialGroup()
				.addGroup(geosetLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(importAllGeos)
						.addComponent(uncheckAllGeos)).addGap(8)
				.addComponent(mht.geosetTabs));
		geosetsPanel.setLayout(geosetLayout);

		return geosetsPanel;
	}
}
