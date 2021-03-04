package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

class VisibilityPanel extends JPanel {
	static final String NOTVISIBLE = "Not visible";
	static final String VISIBLE = "Always visible";
	JLabel oldAnimsLabel;
	JComboBox<Object> oldSourcesBox;
	JLabel newAnimsLabel;
	JComboBox<Object> newSourcesBox;
	JCheckBox favorOld;
	VisibilityShell sourceShell;
	ModelHolderThing mht;

	JLabel title;

	protected VisibilityPanel() {
		// for use in multi pane
	}

	public VisibilityPanel(ModelHolderThing mht, final VisibilityShell sourceShell, final DefaultComboBoxModel<Object> oldSources,
	                       final DefaultComboBoxModel<Object> newSources, final VisShellBoxCellRenderer renderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0"));
		this.sourceShell = sourceShell;
		title = new JLabel(sourceShell.model.getName() + ": " + sourceShell.source.getName());
		title.setFont(new Font("Arial", Font.BOLD, 26));
		title.setMaximumSize(new Dimension(500, 500));
		add(title, "align center, wrap");

		oldAnimsLabel = new JLabel("Existing animation visibility from: ");
		add(oldAnimsLabel, "left, wrap");

		oldSourcesBox = getSourceComboBox(sourceShell, oldSources, renderer);
		add(oldSourcesBox, "grow, wrap");

		newAnimsLabel = new JLabel("Imported animation visibility from: ");

		newSourcesBox = getSourceComboBox(sourceShell, newSources, renderer);
		add(newSourcesBox, "grow, wrap");

		favorOld = new JCheckBox("Favor component's original visibility when combining");
		favorOld.setSelected(true);
		add(favorOld, "left, wrap");
	}

	public JComboBox<Object> getSourceComboBox(VisibilityShell sourceShell, DefaultComboBoxModel<Object> sources, VisShellBoxCellRenderer renderer) {
		JComboBox<Object> jComboBox = new JComboBox<>(sources);
		jComboBox.setEditable(false);
		jComboBox.setMaximumSize(new Dimension(1000, 25));
		jComboBox.setRenderer(renderer);
		boolean didContain = false;
		for (int i = 0; (i < sources.getSize()) && !didContain; i++) {
			if (sourceShell == sources.getElementAt(i)) {
				didContain = true;
			}
		}
		if (didContain) {
			jComboBox.setSelectedItem(sourceShell);
		} else {
			jComboBox.setSelectedItem(VISIBLE);
		}
		return jComboBox;
	}

	public void selectSimilarOptions() {
		final ListModel<Object> oldSources = oldSourcesBox.getModel();
		selectSimilar(oldSources, oldSourcesBox);
		final ListModel<Object> newSources = newSourcesBox.getModel();
		selectSimilar(newSources, newSourcesBox);
	}

	public void selectSimilar(ListModel<Object> sources, JComboBox<Object> sourcesBox) {
		for (int i = 0; i < sources.getSize(); i++) {
			if (!(sources.getElementAt(i) instanceof String)) {
				if (sourceShell.source.getName().equals(((VisibilityShell) sources.getElementAt(i)).source.getName())) {
					System.out.println(sourceShell.source.getName());
					sourcesBox.setSelectedItem(sources.getElementAt(i));
				}
			}
		}
	}
}
