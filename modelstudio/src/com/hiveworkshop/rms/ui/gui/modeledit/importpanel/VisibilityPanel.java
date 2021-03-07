package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

class VisibilityPanel extends JPanel {
	static final String NOTVISIBLE = "Not visible";
	static final String VISIBLE = "Always visible";
	JComboBox<Object> oldSourcesBox;
	JComboBox<Object> newSourcesBox;
	JCheckBox favorOld;
	VisibilityShell sourceShell;
	ModelHolderThing mht;

	DefaultComboBoxModel<Object> oldSources;
	DefaultComboBoxModel<Object> newSources;

	JLabel title;

	protected VisibilityPanel() {
		// for use in multi pane
	}

	public VisibilityPanel(ModelHolderThing mht, VisShellBoxCellRenderer renderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0"));
		title = new JLabel("Select a source");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		title.setMaximumSize(new Dimension(500, 500));
		add(title, "align center, wrap");

		JLabel oldAnimsLabel = new JLabel("Existing animation visibility from: ");
		add(oldAnimsLabel, "left, wrap");

		oldSourcesBox = getSourceComboBox(renderer);
		add(oldSourcesBox, "grow, wrap");

		JLabel newAnimsLabel = new JLabel("Imported animation visibility from: ");
		add(newAnimsLabel, "left, wrap");

		newSourcesBox = getSourceComboBox(renderer);
		add(newSourcesBox, "grow, wrap");

		oldSources = new DefaultComboBoxModel<>(mht.recModVisSourcesOld.toArray());
		oldSourcesBox.setModel(oldSources);
		newSources = new DefaultComboBoxModel<>(mht.donModVisSourcesNew.toArray());
		newSourcesBox.setModel(newSources);

		favorOld = new JCheckBox("Favor component's original visibility when combining");
		favorOld.setSelected(true);
		add(favorOld, "left, wrap");
	}

	public void setSource(VisibilityShell sourceShell) {
		this.sourceShell = sourceShell;
		title.setText(sourceShell.model.getName() + ": " + sourceShell.source.getName());
		selectVisSource(sourceShell, oldSources, oldSourcesBox);
		selectVisSource(sourceShell, newSources, newSourcesBox);

	}

	public JComboBox<Object> getSourceComboBox(VisShellBoxCellRenderer renderer) {
		JComboBox<Object> jComboBox = new JComboBox<>();
		jComboBox.setEditable(false);
		jComboBox.setMaximumSize(new Dimension(1000, 25));
		jComboBox.setRenderer(renderer);
		return jComboBox;
	}

	private void selectVisSource(VisibilityShell sourceShell, DefaultComboBoxModel<Object> sources, JComboBox<Object> jComboBox) {
//		jComboBox.setModel(sources);
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
