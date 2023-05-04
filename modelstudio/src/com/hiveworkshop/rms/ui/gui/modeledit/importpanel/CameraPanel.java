package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

class CameraPanel extends JPanel {
	protected JLabel title;
	protected TriCheckBox doImport;
	protected ModelHolderThing mht;
	private CameraShell selectedObject;

	protected CameraPanel() {
	}

	public CameraPanel(ModelHolderThing mht) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0, ins 0", "[grow]", "[][][][][grow]"));

		title = new JLabel("Object Title");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		doImport = new TriCheckBox("Import");
		doImport.setSelected(true);
		doImport.addActionListener(e -> setImportStatus(doImport.isSelected()));
		add(doImport, "left, wrap");

	}

	public CameraPanel setSelectedObject(CameraShell selectedObject) {
		this.selectedObject = selectedObject;
		title.setText(selectedObject.toString());
		doImport.setSelected(selectedObject.getShouldImport());
		return this;
	}

	private void setImportStatus(boolean doImport) {
		selectedObject.setShouldImport(doImport);
	}

}
