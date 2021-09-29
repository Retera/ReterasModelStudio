package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class MultiCameraPanel extends CameraPanel {
	ModelHolderThing mht;
	List<CameraShell> selectedValuesList;

	public MultiCameraPanel(ModelHolderThing mht) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0", "[grow]", "[][][][][grow]"));
		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		doImport = new JCheckBox("Import these objects (click to apply to all)");
		doImport.setSelected(true);
		doImport.addActionListener(e -> doImportPressed(doImport.isSelected()));
		add(doImport, "left, wrap");
	}

	public void updateMultiCameraPanel(List<CameraShell> selectedValuesList){
		this.selectedValuesList = selectedValuesList;

		boolean firstShouldImport = selectedValuesList.get(0).getShouldImport();

		if (selectedValuesList.stream().anyMatch(objectShell -> objectShell.getShouldImport() != firstShouldImport)){
			doImport.setSelected(false);
			doImport.setBackground(Color.ORANGE);
		} else {
			doImport.setSelected(firstShouldImport);
			doImport.setBackground(this.getBackground());
		}
	}

	private void doImportPressed(boolean doImport) {
		for (CameraShell cam : selectedValuesList) {
			cam.setShouldImport(doImport);
		}
	}
}
