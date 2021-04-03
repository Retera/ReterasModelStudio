package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.List;

public class GeosetEditPanel extends JPanel {

	public CardLayout geoCardLayout = new CardLayout();
	public JPanel geoPanelCards = new JPanel(geoCardLayout);
	public JPanel blankPane = new JPanel();
	//	public MultiGeosetPanel multiGeosetPane;
	GeosetPanel singleGeosetPanel;
	ModelHolderThing mht;

	public GeosetEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[]8[grow]"));
		this.mht = mht;

		add(getTopPanel(), "spanx, align center, wrap");

		GeosetListCellRenderer2D geosetListCellRenderer = new GeosetListCellRenderer2D(mht.recModelManager, mht.donModelManager);
		mht.geosetShellJList.setCellRenderer(geosetListCellRenderer);
		mht.geosetShellJList.addListSelectionListener(e -> showGeosetCard(mht, e));
//		mht.geosetShellJList.setSelectedIndex(0);
		mht.geosetShellJList.setSelectedValue(null, false);
		JScrollPane geosetTabsPane = new JScrollPane(mht.geosetShellJList);
		geosetTabsPane.setMinimumSize(new Dimension(150, 200));

		geoPanelCards.add(blankPane, "blank");

		singleGeosetPanel = new GeosetPanel(mht, mht.allMaterials);
		geoPanelCards.add(singleGeosetPanel, "single");

//		multiGeosetPane = new MultiGeosetPanel(mht, mht.boneShellRenderer);
//		geoPanelCards.add(multiGeosetPane, "multiple");

		geoPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, geosetTabsPane, geoPanelCards);
		add(splitPane, "growx, growy");
	}

	private void showGeosetCard(ModelHolderThing mht, ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			List<GeosetShell> selectedValuesList = mht.geosetShellJList.getSelectedValuesList();
			if (selectedValuesList.size() < 1) {
//				mht.geoShellRenderer.setSelectedBoneShell(null);
				geoCardLayout.show(geoPanelCards, "blank");
			} else if (selectedValuesList.size() == 1) {
//				mht.geoShellRenderer.setSelectedBoneShell(mht.geosetShellJList.getSelectedValue());
				singleGeosetPanel.setGeoset(mht.geosetShellJList.getSelectedValue());
				geoCardLayout.show(geoPanelCards, "single");
			} else {
//				mht.geoShellRenderer.setSelectedBoneShell(null);
//				multiGeosetPane.updateMultiBonePanel();
				geoCardLayout.show(geoPanelCards, "multiple");
			}
		}
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
