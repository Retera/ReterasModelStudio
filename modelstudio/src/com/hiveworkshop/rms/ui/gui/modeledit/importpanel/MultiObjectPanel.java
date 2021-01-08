package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

class MultiObjectPanel extends ObjectPanel implements ChangeListener {
	boolean oldVal = true;
	ImportPanel impPanel;

	public MultiObjectPanel(final DefaultListModel<BoneShell> possibleParents) {
		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));

		doImport = new JCheckBox("Import these objects (click to apply to all)");
		doImport.setSelected(true);
		doImport.addChangeListener(this);
		parentLabel = new JLabel("Parent:");
		oldParentLabel = new JLabel("(Old parent can only be displayed for a single object)");

		parents = possibleParents;
		parentsList = new JList<>(parents);
		parentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		parentsPane = new JScrollPane(parentsList);
		parentsPane.setEnabled(false);
		parentsList.setEnabled(false);

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(title)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(doImport)
						.addComponent(oldParentLabel)
						.addGroup(layout.createSequentialGroup()
								.addComponent(parentLabel)
								.addComponent(parentsPane))));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(title).addGap(16)
				.addComponent(doImport)
				.addComponent(oldParentLabel)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(parentLabel)
						.addComponent(parentsPane)));
		setLayout(layout);
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		if (doImport.isSelected() != oldVal) {
			getImportPanel().setObjGroupSelected(doImport.isSelected());
			oldVal = doImport.isSelected();
		}
	}

	public ImportPanel getImportPanel() {
		if (impPanel == null) {
			Container temp = getParent();
			while ((temp != null) && (temp.getClass() != ImportPanel.class)) {
				temp = temp.getParent();
			}
			impPanel = (ImportPanel) temp;
		}
		return impPanel;
	}
}
