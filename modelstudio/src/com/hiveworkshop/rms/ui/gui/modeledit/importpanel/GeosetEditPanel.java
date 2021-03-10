package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class GeosetEditPanel extends JPanel {

	ModelHolderThing mht;

	public GeosetEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[]8[grow]"));
		this.mht = mht;

		add(getTopPanel(), "spanx, align center, wrap");

		// A list of all materials available for use during this import, in the form of a IterableListModel

		final MaterialListCellRenderer materialsRenderer = new MaterialListCellRenderer(mht.receivingModel);
		// All material lists will know which materials come from the
		// out-of-model source (imported model)

		// Build the geosetTabs list of GeosetPanels

		JTabbedPane geosetTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);

		for (GeosetShell geosetShell : mht.allGeoShells) {
			final GeosetPanel geoPanel = new GeosetPanel(mht, mht.allMaterials, materialsRenderer);
			geoPanel.setGeoset(geosetShell);

			ImageIcon imageIcon = ImportPanel.greenIcon;
			if(geosetShell.isFromDonating()){
				imageIcon = ImportPanel.orangeIcon;
			}
			geosetTabs.addTab(geosetShell.getModelName() + " " + (geosetShell.getIndex() + 1), imageIcon, geoPanel, "Click to modify material data for this geoset.");
		}

		add(geosetTabs, "growx, growy");
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
