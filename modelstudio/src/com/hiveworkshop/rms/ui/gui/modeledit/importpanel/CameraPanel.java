package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

class CameraPanel extends JPanel {
	JLabel title;

	JCheckBox doImport;
	ModelHolderThing mht;

	CameraShell selectedObject;

	protected CameraPanel() {

	}

	public CameraPanel(ModelHolderThing mht) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0, ins 0", "[grow]", "[][][][][grow]"));

		title = new JLabel("Object Title");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		doImport = new JCheckBox("Import this object");
		doImport.setSelected(true);
		doImport.addActionListener(e -> setImportStatus(doImport.isSelected()));
		add(doImport, "left, wrap");

	}

	public CameraPanel setSelectedObject(CameraShell selectedObject) {
		this.selectedObject = selectedObject;
		setTitles();
		setCheckboxStatus(selectedObject.getShouldImport());
		return this;
	}


	private void setTitles() {
		title.setText(selectedObject.toString());
	}

	private void setCheckboxStatus(boolean isChecked) {
		doImport.setSelected(isChecked);
	}

	private void setImportStatus(boolean doImport) {
		selectedObject.setShouldImport(doImport);
	}

}
