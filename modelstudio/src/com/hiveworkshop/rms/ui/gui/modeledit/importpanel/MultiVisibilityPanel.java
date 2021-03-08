package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class MultiVisibilityPanel extends VisibilityPanel {
	boolean oldVal = true;
	ModelHolderThing mht;

	public MultiVisibilityPanel(ModelHolderThing mht,
	                            IterableListModel<VisibilityShell> recModVisSourcesOld,
	                            IterableListModel<VisibilityShell> donModVisSourcesNew,
	                            final VisShellBoxCellRenderer renderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0"));
		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));

		JLabel oldAnimsLabel = new JLabel("Existing animation visibility from: ");
		List<VisibilityShell> recModShells = new ArrayList<>();
		recModVisSourcesOld.forEach(recModShells::add);
		recModSources = new DefaultComboBoxModel<>(recModShells.toArray(new VisibilityShell[0]));
		receivingModelSourcesBox = new JComboBox<>(recModSources);
		receivingModelSourcesBox.setEditable(false);
		receivingModelSourcesBox.setMaximumSize(new Dimension(1000, 25));
		receivingModelSourcesBox.setRenderer(renderer);
		receivingModelSourcesBox.addItemListener(e -> setVisGroupItemOld((VisibilityShell) receivingModelSourcesBox.getSelectedItem()));

		JLabel newAnimsLabel = new JLabel("Imported animation visibility from: ");

		List<VisibilityShell> donModShells = new ArrayList<>();
		donModVisSourcesNew.forEach(donModShells::add);
		donModSources = new DefaultComboBoxModel<>(donModShells.toArray(new VisibilityShell[0]));
		donatingModelSourcesBox = new JComboBox<>(donModSources);
		donatingModelSourcesBox.setEditable(false);
		donatingModelSourcesBox.setMaximumSize(new Dimension(1000, 25));
		donatingModelSourcesBox.setRenderer(renderer);
		donatingModelSourcesBox.addItemListener(e -> setVisGroupItemNew((VisibilityShell) donatingModelSourcesBox.getSelectedItem()));

		favorOld = new JCheckBox("Favor component's original visibility when combining");
		favorOld.setSelected(true);
		favorOld.addChangeListener(e -> favorOldPressed());

		add(title, "cell 0 0, spanx, align center, wrap");
		add(oldAnimsLabel, "cell 0 1");
		add(receivingModelSourcesBox, "cell 1 1");
		add(newAnimsLabel, "cell 0 2");
		add(donatingModelSourcesBox, "cell 1 2");
		add(favorOld, "cell 0 3");
	}

	private void favorOldPressed() {
		if (favorOld.isSelected() != oldVal) {
			setVisGroupSelected(favorOld.isSelected());
			oldVal = favorOld.isSelected();
		}
	}

	public void setVisGroupSelected(final boolean flag) {
		for (VisibilityShell temp : mht.visTabs.getSelectedValuesList()) {
			temp.setFavorOld(flag);
		}
	}

	public void setVisGroupItemOld(VisibilityShell o) {
		for (VisibilityShell temp : mht.visTabs.getSelectedValuesList()) {
			temp.setOldVisSource(o);
		}
	}

	public void setVisGroupItemNew(VisibilityShell o) {
		for (VisibilityShell temp : mht.visTabs.getSelectedValuesList()) {
			temp.setNewVisSource(o);
		}
	}

	public void setMultipleOld() {
		receivingModelSourcesBox.setEditable(true);
		receivingModelSourcesBox.setSelectedItem("Multiple selected");
		receivingModelSourcesBox.setEditable(false);
	}

	public void setMultipleNew() {
		donatingModelSourcesBox.setEditable(true);
		donatingModelSourcesBox.setSelectedItem("Multiple selected");
		donatingModelSourcesBox.setEditable(false);
	}
}
