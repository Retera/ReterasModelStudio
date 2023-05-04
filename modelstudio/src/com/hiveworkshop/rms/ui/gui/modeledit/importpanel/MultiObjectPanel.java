package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class MultiObjectPanel extends ObjectPanel {
	private List<IdObjectShell<?>> selectedValuesList;

	public MultiObjectPanel(ModelHolderThing mht, BoneShellListCellRenderer bonePanelRenderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0", "[grow]", "[][][][][grow]"));

		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		this.bonePanelRenderer = bonePanelRenderer;

		doImport = new JCheckBox("Import these objects (click to apply to all)");
		doImport.addActionListener(e -> doImportPressed(doImport.isSelected()));
		add(doImport, "left, wrap");

		oldParentLabel = new JLabel("(Old parent can only be displayed for a single object)");
		add(oldParentLabel, "left, wrap");

		parentLabel = new JLabel("Parent:");
		add(parentLabel, "left, wrap");

		add(getParentListPane(bonePanelRenderer), "growx, growy 200");
	}

	private JScrollPane getParentListPane(BoneShellListCellRenderer bonePanelRenderer) {
		parentsList = new TwiList<>();
		parentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		parentsList.setCellRenderer(bonePanelRenderer);
		parentsPane = new JScrollPane(parentsList);
		parentsPane.setEnabled(false);
		return parentsPane;
	}

	public void setSelectedObjects(List<IdObjectShell<?>> selectedValuesList) {
		this.selectedValuesList = selectedValuesList;

		boolean firstShouldImport = selectedValuesList.get(0).getShouldImport();
		parents = mht.getFutureBoneHelperList();
		bonePanelRenderer.setSelectedObjectShell(null);

		if (selectedValuesList.stream().anyMatch(objectShell -> objectShell.getShouldImport() != firstShouldImport)) {
			doImport.setSelected(false);
			doImport.setBackground(Color.ORANGE);
		} else {
			doImport.setSelected(firstShouldImport);
			doImport.setBackground(this.getBackground());
		}
		repaint();
	}

	private void doImportPressed(boolean doImport) {
		for (IdObjectShell<?> op : selectedValuesList) {
			op.setShouldImport(doImport);
		}
	}
}
