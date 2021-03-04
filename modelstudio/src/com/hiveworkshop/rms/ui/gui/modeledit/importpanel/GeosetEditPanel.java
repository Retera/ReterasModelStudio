package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class GeosetEditPanel extends JPanel {

	ModelHolderThing mht;

	public GeosetEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[]8[grow]"));
		this.mht = mht;

		add(getTopPanel(), "spanx, align center, wrap");


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

		add(mht.geosetTabs, "growx, growy");
	}

	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("gap 0", "[]8[]"));

		JButton importAllGeos = new JButton("Import All");
		importAllGeos.addActionListener(e -> mht.importAllGeos(true));
		topPanel.add(importAllGeos);

		JButton uncheckAllGeos = new JButton("Leave All");
		uncheckAllGeos.addActionListener(e -> mht.importAllGeos(false));
		topPanel.add(uncheckAllGeos);
		return topPanel;
	}
}
