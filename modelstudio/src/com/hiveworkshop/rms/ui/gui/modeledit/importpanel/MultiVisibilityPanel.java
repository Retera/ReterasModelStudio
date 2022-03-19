package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.renderers.VisShellBoxCellRenderer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class MultiVisibilityPanel extends VisibilityPanel {
	ModelHolderThing mht;
	List<VisibilityShell> selectedValuesList;

	public MultiVisibilityPanel(ModelHolderThing mht,
	                            List<VisibilityShell> recModVisSourcesOld,
	                            List<VisibilityShell> donModVisSourcesNew,
	                            final VisShellBoxCellRenderer renderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0"));
		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));

		JLabel oldAnimsLabel = new JLabel("Existing animation visibility from: ");
		receivingModelSourcesBox = getSourceComboBox(renderer, recModVisSourcesOld);
		receivingModelSourcesBox.addItemListener(e -> setVisGroupItemOld((VisibilityShell) receivingModelSourcesBox.getSelectedItem()));

		JLabel newAnimsLabel = new JLabel("Imported animation visibility from: ");
		donatingModelSourcesBox = getSourceComboBox(renderer, donModVisSourcesNew);
		donatingModelSourcesBox.addItemListener(e -> setVisGroupItemNew((VisibilityShell) donatingModelSourcesBox.getSelectedItem()));

		favorOld = new JCheckBox("Favor component's original visibility when combining");
		favorOld.addActionListener(e -> favorOldPressed());

		add(title, "align center, wrap");
		add(oldAnimsLabel, "left, wrap");
		add(receivingModelSourcesBox, "grow, wrap");
		add(newAnimsLabel, "left, wrap");
		add(donatingModelSourcesBox, "grow, wrap");
		add(favorOld, "left, wrap");
	}

	public void updateMultiVisPanel(List<VisibilityShell> selectedValuesList){
		this.selectedValuesList = selectedValuesList;

		boolean firstIsDoFavorOld = selectedValuesList.get(0).isFavorOld();

		if (selectedValuesList.stream().anyMatch(vs -> vs.isFavorOld() != firstIsDoFavorOld)) {
			this.favorOld.setSelected(false);
			this.favorOld.setBackground(Color.ORANGE);
		} else {
			this.favorOld.setSelected(firstIsDoFavorOld);
			this.favorOld.setBackground(this.getBackground());
		}

		VisibilityShell firstOldVisSource = selectedValuesList.get(0).getDonModAnimsVisSource();
		if (selectedValuesList.stream()
				.anyMatch(vs -> vs.getDonModAnimsVisSource() != firstOldVisSource
						&& !((firstOldVisSource == null || firstOldVisSource == mht.alwaysVisible)
						&& (vs.getDonModAnimsVisSource() == null || vs.getDonModAnimsVisSource() == mht.alwaysVisible)))) {
			setMultipleOld();
		} else if (firstOldVisSource == null) {
			receivingModelSourcesBox.setSelectedItem(mht.alwaysVisible);
		} else {
			receivingModelSourcesBox.setSelectedItem(firstOldVisSource);
		}

		VisibilityShell firstNewVisSource = selectedValuesList.get(0).getRecModAnimsVisSource();
		if (selectedValuesList.stream()
				.anyMatch(vs -> vs.getRecModAnimsVisSource() != firstNewVisSource
						&& !((firstOldVisSource == null || firstOldVisSource == mht.alwaysVisible)
						&& (vs.getRecModAnimsVisSource() == null || vs.getRecModAnimsVisSource() == mht.alwaysVisible)))) {
			setMultipleNew();
		} else if (firstNewVisSource == null) {
			donatingModelSourcesBox.setSelectedItem(mht.alwaysVisible);
		} else {
			donatingModelSourcesBox.setSelectedItem(firstNewVisSource);
		}

	}

	private JComboBox<VisibilityShell> getSourceComboBox(VisShellBoxCellRenderer renderer, List<VisibilityShell> visSources) {
		DefaultComboBoxModel<VisibilityShell> sourceModel = new DefaultComboBoxModel<>();
		sourceModel.addAll(visSources);

		sourceModel.insertElementAt(mht.multipleVisible, 0);
		JComboBox<VisibilityShell> jComboBox = new JComboBox<>(sourceModel);
		jComboBox.setMaximumSize(new Dimension(500, 25));
		jComboBox.setRenderer(renderer);
		return jComboBox;
	}

	private void favorOldPressed() {
		for(VisibilityShell vs : selectedValuesList){
			vs.setFavorOld(favorOld.isSelected());
		}
	}

	public void setVisGroupItemOld(VisibilityShell o) {
		if (receivingModelSourcesBox.getSelectedItem() != mht.multipleVisible){
			((DefaultComboBoxModel<VisibilityShell>)receivingModelSourcesBox.getModel()).removeElement(mht.multipleVisible);
			for (VisibilityShell temp : selectedValuesList) {
				temp.setDonModAnimsVisSource(o);
			}
		}
	}

	public void setVisGroupItemNew(VisibilityShell o) {
		if (donatingModelSourcesBox.getSelectedItem() != mht.multipleVisible){
			((DefaultComboBoxModel<VisibilityShell>)donatingModelSourcesBox.getModel()).removeElement(mht.multipleVisible);
			for (VisibilityShell temp : selectedValuesList) {
				temp.setRecModAnimsVisSource(o);
			}
		}
	}

	public void setMultipleOld() {
		// setting the combo boxes to editable enables select a temporary item not in the list model
		receivingModelSourcesBox.setEditable(true);
		receivingModelSourcesBox.setSelectedItem(mht.multipleVisible);
		receivingModelSourcesBox.setEditable(false);
	}

	public void setMultipleNew() {
		donatingModelSourcesBox.setEditable(true);
		donatingModelSourcesBox.setSelectedItem(mht.multipleVisible);
		donatingModelSourcesBox.setEditable(false);
	}
}
