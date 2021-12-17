package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.renderers.GeosetListCellRenderer2D;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;

public class GeosetEditPanel extends JPanel {

	private CardLayout geoCardLayout = new CardLayout();
	private JPanel geoPanelCards = new JPanel(geoCardLayout);
	private JPanel blankPane = new JPanel();
	private MultiGeosetPanel multiGeosetPanel;
	private GeosetPanel singleGeosetPanel;
	private ModelHolderThing mht;

	public JList<GeosetShell> geosetShellJList;

	public GeosetEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[]8[grow]"));
		this.mht = mht;
		geosetShellJList = new JList<>(mht.allGeoShells);

		add(getTopPanel(), "spanx, align center, wrap");

		singleGeosetPanel = new GeosetPanel(mht, mht.allMaterials);

		geoPanelCards.add(blankPane, "blank");
		geoPanelCards.add(singleGeosetPanel, "single");

		multiGeosetPanel = new MultiGeosetPanel(mht, mht.allMaterials);
		geoPanelCards.add(multiGeosetPanel, "multiple");

		geoPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getGeosetListPane(mht), geoPanelCards);
		add(splitPane, "growx, growy");
	}

	private JScrollPane getGeosetListPane(ModelHolderThing mht) {
		GeosetListCellRenderer2D geosetListCellRenderer = new GeosetListCellRenderer2D(mht.receivingModel, mht.donatingModel);
		geosetShellJList.setCellRenderer(geosetListCellRenderer);
		geosetShellJList.addListSelectionListener(e -> showGeosetCard(e));
		geosetShellJList.setSelectedValue(null, false);
		JScrollPane geosetTabsPane = new JScrollPane(geosetShellJList);
		geosetTabsPane.setMinimumSize(new Dimension(150, 200));
		return geosetTabsPane;
	}

	private void showGeosetCard(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			List<GeosetShell> selectedValuesList = geosetShellJList.getSelectedValuesList();
			if (selectedValuesList.size() < 1) {
				geoCardLayout.show(geoPanelCards, "blank");
			} else if (selectedValuesList.size() == 1) {
				singleGeosetPanel.setGeoset(geosetShellJList.getSelectedValue());
				geoCardLayout.show(geoPanelCards, "single");
			} else {
				multiGeosetPanel.setGeosets(selectedValuesList);
				geoCardLayout.show(geoPanelCards, "multiple");
			}
		}
	}

	private JPanel getTopPanel() {
//		JPanel topPanel = new JPanel(new MigLayout("gap 0", "[]8[]"));
		JPanel topPanel = new JPanel(new MigLayout("gap 0", "[][]", "[align center][align center]"));
		topPanel.setOpaque(true);

		topPanel.add(getSetImpTypePanel(mht.receivingModel.getName(), (b) -> mht.setImportAllRecGeos(b)), "");
		topPanel.add(getSetImpTypePanel(mht.donatingModel.getName(), (b) -> mht.setImportAllDonGeos(b)), "wrap");

		return topPanel;
	}

	private JPanel getSetImpTypePanel(String modelName, Consumer<Boolean> importTypeConsumer) {
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0", "[][][]", "[align center]"));
		panel.setOpaque(true);
		panel.setBorder(BorderFactory.createTitledBorder(modelName));

		panel.add(getButton("Import All", e -> importTypeConsumer.accept(true)), "");
		panel.add(getButton("Leave All", e -> importTypeConsumer.accept(false)), "");

		return panel;
	}

	public JButton getButton(String text, ActionListener actionListener) {
		JButton jButton = new JButton(text);
		jButton.addActionListener(actionListener);
		return jButton;
	}
}
