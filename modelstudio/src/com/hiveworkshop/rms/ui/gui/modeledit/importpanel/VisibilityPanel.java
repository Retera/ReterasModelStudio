package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;

class VisibilityPanel extends JPanel {
	protected JComboBox<VisibilityShell> receivingModelSourcesBox;
	protected JComboBox<VisibilityShell> donatingModelSourcesBox;
	protected JCheckBox favorOld;
	protected VisibilityShell selectedVisShell;
	protected ModelHolderThing mht;

	protected DefaultComboBoxModel<VisibilityShell> recModSources;
	protected DefaultComboBoxModel<VisibilityShell> donModSources;

	protected JLabel title;

	protected VisibilityPanel() {
		// for use in multi pane
	}

	public VisibilityPanel(ModelHolderThing mht, VisShellBoxCellRenderer renderer, List<VisibilityShell> recModVisSourcesOld, List<VisibilityShell> donModVisSourcesNew) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0"));
		title = new JLabel("Select a source");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		title.setMaximumSize(new Dimension(500, 500));
		add(title, "align center, wrap");

		JLabel oldAnimsLabel = new JLabel("Existing animation visibility from: ");
		add(oldAnimsLabel, "left, wrap");

		receivingModelSourcesBox = getSourceComboBox(renderer, recModVisSourcesOld);
		receivingModelSourcesBox.addItemListener(this::setOldSource);
		add(receivingModelSourcesBox, "grow, wrap");

		JLabel newAnimsLabel = new JLabel("Imported animation visibility from: ");
		add(newAnimsLabel, "left, wrap");

		donatingModelSourcesBox = getSourceComboBox(renderer, donModVisSourcesNew);
		donatingModelSourcesBox.addItemListener(this::setNewSource);
		add(donatingModelSourcesBox, "grow, wrap");

		favorOld = new JCheckBox("Favor component's original visibility when combining");
		favorOld.addActionListener(e -> setFavorOld());
		add(favorOld, "left, wrap");
	}

	public void setSource(VisibilityShell sourceShell) {
		this.selectedVisShell = sourceShell;
//		title.setText(sourceShell.getModel().getName() + ": " + sourceShell.getSource().getName());
		title.setText(sourceShell.toString());
		favorOld.setSelected(selectedVisShell.isFavorOld());

		if (sourceShell.getOldVisSource() != null && mht.recModVisSourcesOld.contains(sourceShell.getOldVisSource())) {
			receivingModelSourcesBox.setSelectedItem(sourceShell.getOldVisSource());
		} else if (mht.recModVisSourcesOld.contains(sourceShell)) {
			receivingModelSourcesBox.setSelectedItem(sourceShell);
		} else {
			receivingModelSourcesBox.setSelectedItem(mht.alwaysVisible);
		}
		if (sourceShell.getNewVisSource() != null && mht.donModVisSourcesNew.contains(sourceShell.getNewVisSource())) {
			donatingModelSourcesBox.setSelectedItem(sourceShell.getNewVisSource());
		} else if (mht.donModVisSourcesNew.contains(sourceShell)) {
			donatingModelSourcesBox.setSelectedItem(sourceShell);
		} else {
			donatingModelSourcesBox.setSelectedItem(mht.alwaysVisible);
		}
	}

	private void setFavorOld(){
		selectedVisShell.setFavorOld(favorOld.isSelected());
	}

	private JComboBox<VisibilityShell> getSourceComboBox(VisShellBoxCellRenderer renderer, List<VisibilityShell> visSources) {
		DefaultComboBoxModel<VisibilityShell> sourceModel = new DefaultComboBoxModel<>();
		sourceModel.addAll(visSources);

		JComboBox<VisibilityShell> jComboBox = new JComboBox<>(sourceModel);
		jComboBox.setMaximumSize(new Dimension(500, 25));
		jComboBox.setRenderer(renderer);
		return jComboBox;
	}

	private void setNewSource(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			selectedVisShell.setNewVisSource((VisibilityShell) donatingModelSourcesBox.getSelectedItem());
		}
	}

	private void setOldSource(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			selectedVisShell.setOldVisSource((VisibilityShell) receivingModelSourcesBox.getSelectedItem());
		}
	}
}
