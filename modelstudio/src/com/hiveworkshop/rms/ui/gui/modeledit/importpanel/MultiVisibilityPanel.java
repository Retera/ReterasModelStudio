package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

class MultiVisibilityPanel extends VisibilityPanel {
	boolean oldVal = true;
	ModelHolderThing mht;

	public MultiVisibilityPanel(ModelHolderThing mht, final DefaultComboBoxModel<Object> oldSources, final DefaultComboBoxModel<Object> newSources,
	                            final VisShellBoxCellRenderer renderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0"));
		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));

		JLabel oldAnimsLabel = new JLabel("Existing animation visibility from: ");

		oldSourcesBox = new JComboBox<>(oldSources);
		oldSourcesBox.setEditable(false);
		oldSourcesBox.setMaximumSize(new Dimension(1000, 25));
		oldSourcesBox.setRenderer(renderer);
		oldSourcesBox.addItemListener(e -> setVisGroupItemOld(oldSourcesBox.getSelectedItem()));

		JLabel newAnimsLabel = new JLabel("Imported animation visibility from: ");

		newSourcesBox = new JComboBox<>(newSources);
		newSourcesBox.setEditable(false);
		newSourcesBox.setMaximumSize(new Dimension(1000, 25));
		newSourcesBox.setRenderer(renderer);
		newSourcesBox.addItemListener(e -> setVisGroupItemNew(newSourcesBox.getSelectedItem()));

		favorOld = new JCheckBox("Favor component's original visibility when combining");
		favorOld.setSelected(true);
		favorOld.addChangeListener(e -> favorOldPressed());

		add(title, "cell 0 0, spanx, align center, wrap");
		add(oldAnimsLabel, "cell 0 1");
		add(oldSourcesBox, "cell 1 1");
		add(newAnimsLabel, "cell 0 2");
		add(newSourcesBox, "cell 1 2");
		add(favorOld, "cell 0 3");
	}

	private void favorOldPressed() {
		if (favorOld.isSelected() != oldVal) {
			setVisGroupSelected(favorOld.isSelected());
			oldVal = favorOld.isSelected();
		}
	}

	public void setVisGroupSelected(final boolean flag) {
		for (VisibilityPanel temp : mht.visTabs.getSelectedValuesList()) {
			temp.favorOld.setSelected(flag);
		}
	}

	public void setVisGroupItemOld(final Object o) {
		for (VisibilityPanel temp : mht.visTabs.getSelectedValuesList()) {
			temp.oldSourcesBox.setSelectedItem(o);
		}
	}

	public void setVisGroupItemNew(final Object o) {
		for (VisibilityPanel temp : mht.visTabs.getSelectedValuesList()) {
			temp.newSourcesBox.setSelectedItem(o);
		}
	}

	public void setMultipleOld() {
		oldSourcesBox.setEditable(true);
		oldSourcesBox.setSelectedItem("Multiple selected");
		oldSourcesBox.setEditable(false);
	}

	public void setMultipleNew() {
		newSourcesBox.setEditable(true);
		newSourcesBox.setSelectedItem("Multiple selected");
		newSourcesBox.setEditable(false);
	}
}
