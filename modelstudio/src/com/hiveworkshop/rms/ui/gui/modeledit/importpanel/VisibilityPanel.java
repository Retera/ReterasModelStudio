package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class VisibilityPanel extends JPanel {
	static final String NOTVISIBLE = "Not visible";
	static final String VISIBLE = "Always visible";
	JComboBox<VisibilityShell> oldSourcesBox;
	JComboBox<VisibilityShell> newSourcesBox;
	JCheckBox favorOld;
	VisibilityShell sourceShell;
	ModelHolderThing mht;

	DefaultComboBoxModel<VisibilityShell> oldSources;
	DefaultComboBoxModel<VisibilityShell> newSources;

	JLabel title;

	protected VisibilityPanel() {
		// for use in multi pane
	}

	public VisibilityPanel(ModelHolderThing mht, VisShellBoxCellRenderer renderer, IterableListModel<VisibilityShell> recModVisSourcesOld, IterableListModel<VisibilityShell> donModVisSourcesNew) {
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


		List<VisibilityShell> recModShells = new ArrayList<>();
		recModVisSourcesOld.forEach(recModShells::add);
		oldSources = new DefaultComboBoxModel<>(recModShells.toArray(new VisibilityShell[0]));
		oldSourcesBox.setModel(oldSources);
		List<VisibilityShell> donModShells = new ArrayList<>();
		donModVisSourcesNew.forEach(donModShells::add);
		newSources = new DefaultComboBoxModel<>(donModShells.toArray(new VisibilityShell[0]));
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

	public JComboBox<VisibilityShell> getSourceComboBox(VisShellBoxCellRenderer renderer) {
		JComboBox<VisibilityShell> jComboBox = new JComboBox<>();
		jComboBox.setEditable(false);
		jComboBox.setMaximumSize(new Dimension(1000, 25));
		jComboBox.setRenderer(renderer);
		return jComboBox;
	}

	private void selectVisSource(VisibilityShell sourceShell, DefaultComboBoxModel<VisibilityShell> sources, JComboBox<VisibilityShell> jComboBox) {
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
		final ListModel<VisibilityShell> oldSources = oldSourcesBox.getModel();
		selectSimilar(oldSources, oldSourcesBox);
		final ListModel<VisibilityShell> newSources = newSourcesBox.getModel();
		selectSimilar(newSources, newSourcesBox);
	}

	public void selectSimilar(ListModel<VisibilityShell> sources, JComboBox<VisibilityShell> sourcesBox) {
		for (int i = 0; i < sources.getSize(); i++) {
			if (sourceShell.source.getName().equals(sources.getElementAt(i).source.getName())) {
				System.out.println(sourceShell.source.getName());
				sourcesBox.setSelectedItem(sources.getElementAt(i));
			}
		}
	}
}
