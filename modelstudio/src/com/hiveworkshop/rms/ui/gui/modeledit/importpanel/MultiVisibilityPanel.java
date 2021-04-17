package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class MultiVisibilityPanel extends VisibilityPanel {
	ModelHolderThing mht;

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

	public void updateMultiVisPanel(){
		List<VisibilityShell> selectedValuesList = mht.visTabs.getSelectedValuesList();

		boolean firstIsDoFavorOld = selectedValuesList.get(0).isFavorOld();

		if(selectedValuesList.stream().anyMatch(vs -> vs.isFavorOld() != firstIsDoFavorOld)){
			this.favorOld.setSelected(false);
			this.favorOld.setBackground(Color.ORANGE);
		}  else {
			this.favorOld.setSelected(firstIsDoFavorOld);
			this.favorOld.setBackground(this.getBackground());
		}

		VisibilityShell firstOldVisSource = selectedValuesList.get(0).getOldVisSource();
		if(selectedValuesList.stream()
				.anyMatch(vs -> vs.getOldVisSource() != firstOldVisSource
						&& !((firstOldVisSource == null || firstOldVisSource == mht.alwaysVisible)
							&& (vs.getOldVisSource() == null || vs.getOldVisSource() == mht.alwaysVisible)))){
			setMultipleOld();
		} else if (firstOldVisSource == null) {
			receivingModelSourcesBox.setSelectedItem(mht.alwaysVisible);
		} else {
			receivingModelSourcesBox.setSelectedItem(firstOldVisSource);
		}

		VisibilityShell firstNewVisSource = selectedValuesList.get(0).getNewVisSource();
		if(selectedValuesList.stream()
				.anyMatch(vs -> vs.getNewVisSource() != firstNewVisSource
						&& !((firstOldVisSource == null || firstOldVisSource == mht.alwaysVisible)
							&& (vs.getNewVisSource() == null || vs.getNewVisSource() == mht.alwaysVisible)))){
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
		List<VisibilityShell> selectedValuesList = mht.visTabs.getSelectedValuesList();
		for(VisibilityShell vs : selectedValuesList){
			vs.setFavorOld(favorOld.isSelected());
		}
	}

	public void setVisGroupItemOld(VisibilityShell o) {
		if (receivingModelSourcesBox.getSelectedItem() != mht.multipleVisible){
			((DefaultComboBoxModel<VisibilityShell>)receivingModelSourcesBox.getModel()).removeElement(mht.multipleVisible);
			for (VisibilityShell temp : mht.visTabs.getSelectedValuesList()) {
				temp.setOldVisSource(o);
			}
		}
	}

	public void setVisGroupItemNew(VisibilityShell o) {
		if (donatingModelSourcesBox.getSelectedItem() != mht.multipleVisible){
			((DefaultComboBoxModel<VisibilityShell>)donatingModelSourcesBox.getModel()).removeElement(mht.multipleVisible);
			for (VisibilityShell temp : mht.visTabs.getSelectedValuesList()) {
				temp.setNewVisSource(o);
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
