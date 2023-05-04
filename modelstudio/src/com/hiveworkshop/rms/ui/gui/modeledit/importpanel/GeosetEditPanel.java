package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.renderers.GeosetListCellRenderer2D;
import com.hiveworkshop.rms.ui.util.TwiList;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;

public class GeosetEditPanel extends JPanel {

	private final CardLayout geoCardLayout = new CardLayout();
	private final JPanel geoPanelCards = new JPanel(geoCardLayout);
	private final GeosetMultiPanel geosetMultiPanel;
	private final GeosetPanel singleGeosetPanel;
	private final ModelHolderThing mht;
	private final TwiList<GeosetShell> geosetShellJList;

	public GeosetEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[]8[grow]"));
		this.mht = mht;
		geosetShellJList = new TwiList<>(mht.allGeoShells);

		add(getTopPanel(), "spanx, align center, wrap");

		singleGeosetPanel = new GeosetPanel(mht, geosetShellJList, mht.allMaterials);
		geosetMultiPanel = new GeosetMultiPanel(mht, geosetShellJList, mht.allMaterials);

		geoPanelCards.add(new JPanel(), "blank");
		geoPanelCards.add(singleGeosetPanel, "single");
		geoPanelCards.add(geosetMultiPanel, "multiple");

		geoPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getGeosetListPane(mht), geoPanelCards);
		add(splitPane, "growx, growy");
	}

	private JScrollPane getGeosetListPane(ModelHolderThing mht) {
		GeosetListCellRenderer2D geosetListCellRenderer = new GeosetListCellRenderer2D(mht.receivingModel, mht.donatingModel);
		geosetShellJList.setCellRenderer(geosetListCellRenderer);
		geosetShellJList.setSelectedValue(null, false);
		geosetShellJList.addMultiSelectionListener(this::showGeosetCard);
		JScrollPane geosetTabsPane = new JScrollPane(geosetShellJList);
		geosetTabsPane.setMinimumSize(new Dimension(150, 200));
		return geosetTabsPane;
	}

	private void showGeosetCard(Collection<GeosetShell> selectedValuesList) {
		if (selectedValuesList.size() < 1) {
			showCard("blank");
		} else if (selectedValuesList.size() == 1) {
			singleGeosetPanel.setGeoset(geosetShellJList.getSelectedValue());
			showCard("single");
		} else {
			geosetMultiPanel.setGeosets((List<GeosetShell>)selectedValuesList);
			showCard("multiple");
		}
	}
	private void showCard(String name){
		geoCardLayout.show(geoPanelCards, name);
	}

	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("gap 0", "[][]", "[align center][align center]"));
		topPanel.setOpaque(true);

		topPanel.add(getSetImpTypePanel(mht.receivingModel.getName(), false), "");
		topPanel.add(getSetImpTypePanel(mht.donatingModel.getName(), true), "wrap");

		return topPanel;
	}

	private JPanel getSetImpTypePanel(String modelName, boolean donMod) {
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0", "[][][]", "[align center]"));
		panel.setOpaque(true);
		panel.setBorder(BorderFactory.createTitledBorder(modelName));

		panel.add(Button.create("Import All", e -> setImportGeos(true, donMod)), "");
		panel.add(Button.create("Leave All", e -> setImportGeos(false, donMod)), "");

		return panel;
	}

	public void setImportGeos(boolean imp, boolean donMod) {
		List<GeosetShell> geoShells = donMod ? mht.donModGeoShells : mht.recModGeoShells;
		for (GeosetShell geoShell : geoShells) {
			geoShell.setDoImport(imp);
		}
	}
}
