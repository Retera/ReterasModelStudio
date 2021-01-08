package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

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

	JLabel title;

	protected VisibilityPanel() {
		// for use in multi pane
	}

	public VisibilityPanel(final VisibilityShell sourceShell, final DefaultComboBoxModel<Object> oldSources,
	                       final DefaultComboBoxModel<Object> newSources, final VisShellBoxCellRenderer renderer) {
		this.sourceShell = sourceShell;
		title = new JLabel(sourceShell.model.getName() + ": " + sourceShell.source.getName());
		title.setFont(new Font("Arial", Font.BOLD, 26));

		oldAnimsLabel = new JLabel("Existing animation visibility from: ");
		oldSourcesBox = new JComboBox<>(oldSources);
		oldSourcesBox.setEditable(false);
		oldSourcesBox.setMaximumSize(new Dimension(1000, 25));
		oldSourcesBox.setRenderer(renderer);
		boolean didContain = false;
		for (int i = 0; (i < oldSources.getSize()) && !didContain; i++) {
			if (sourceShell == oldSources.getElementAt(i)) {
				didContain = true;
			}
		}
		if (didContain) {
			oldSourcesBox.setSelectedItem(sourceShell);
		} else {
			oldSourcesBox.setSelectedItem(VISIBLE);
		}

		newAnimsLabel = new JLabel("Imported animation visibility from: ");
		newSourcesBox = new JComboBox<>(newSources);
		newSourcesBox.setEditable(false);
		newSourcesBox.setMaximumSize(new Dimension(1000, 25));
		newSourcesBox.setRenderer(renderer);
		didContain = false;
		for (int i = 0; (i < newSources.getSize()) && !didContain; i++) {
			if (sourceShell == newSources.getElementAt(i)) {
				didContain = true;
			}
		}
		if (didContain) {
			newSourcesBox.setSelectedItem(sourceShell);
		} else {
			newSourcesBox.setSelectedItem(VISIBLE);
		}

		favorOld = new JCheckBox("Favor component's original visibility when combining");
		favorOld.setSelected(true);

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(title)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(oldAnimsLabel)
						.addComponent(oldSourcesBox)
						.addComponent(newAnimsLabel)
						.addComponent(newSourcesBox)
						.addComponent(favorOld)));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(title).addGap(16)
				.addComponent(oldAnimsLabel)
				.addComponent(oldSourcesBox)
				.addComponent(newAnimsLabel)
				.addComponent(newSourcesBox)
				.addComponent(favorOld));
		setLayout(layout);
	}

	public void selectSimilarOptions() {
		final VisibilityShell temp = null;
		final ListModel oldSources = oldSourcesBox.getModel();
		for (int i = 0; i < oldSources.getSize(); i++) {
			if (!(oldSources.getElementAt(i) instanceof String)) {
				if (sourceShell.source.getName()
						.equals(((VisibilityShell) oldSources.getElementAt(i)).source.getName())) {
					System.out.println(sourceShell.source.getName());
					oldSourcesBox.setSelectedItem(oldSources.getElementAt(i));
				}
			}
		}
		final ListModel newSources = newSourcesBox.getModel();
		for (int i = 0; i < newSources.getSize(); i++) {
			if (!(newSources.getElementAt(i) instanceof String)) {
				if (sourceShell.source.getName()
						.equals(((VisibilityShell) newSources.getElementAt(i)).source.getName())) {
					System.out.println(sourceShell.source.getName());
					newSourcesBox.setSelectedItem(newSources.getElementAt(i));
				}
			}
		}
	}
}
