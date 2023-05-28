package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.CameraShell;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class CameraMultiPanel extends CameraPanel {
	ModelHolderThing mht;
	List<CameraShell> selectedValuesList;

	public CameraMultiPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0", "[grow]", "[][][][][grow]"));
		this.mht = mht;
		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		doImport = new TriCheckBox("Import");
		doImport.setSelected(true);
		doImport.addActionListener(e -> doImportPressed(doImport.isSelected()));
		add(doImport, "left, wrap");
	}

	public void updateMultiCameraPanel(List<CameraShell> selectedValuesList){
		this.selectedValuesList = selectedValuesList;

		boolean firstShouldImport = selectedValuesList.get(0).getShouldImport();

		if (selectedValuesList.stream().anyMatch(objectShell -> objectShell.getShouldImport() != firstShouldImport)){
			doImport.setIndeterminate(false);
		} else {
			doImport.setSelected(firstShouldImport);
		}
	}

	private void doImportPressed(boolean doImport) {
		for (CameraShell cam : selectedValuesList) {
			cam.setShouldImport(doImport);
		}
	}
}
