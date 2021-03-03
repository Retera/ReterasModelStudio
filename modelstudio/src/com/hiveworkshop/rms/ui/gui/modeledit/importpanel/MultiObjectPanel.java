package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

class MultiObjectPanel extends ObjectPanel implements ChangeListener {
	boolean oldVal = true;
	ImportPanel impPanel;

	public MultiObjectPanel(final IterableListModel<BoneShell> possibleParents) {
		setLayout(new MigLayout("gap 0", "[grow]", "[][][][][grow]"));
		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		doImport = new JCheckBox("Import these objects (click to apply to all)");
		doImport.setSelected(true);
		doImport.addChangeListener(this);
		add(doImport, "left, wrap");

		oldParentLabel = new JLabel("(Old parent can only be displayed for a single object)");
		add(oldParentLabel, "left, wrap");

		parentLabel = new JLabel("Parent:");
		add(parentLabel, "left, wrap");

		parents = possibleParents;
		parentsList = new JList<>(parents);
		parentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		parentsPane = new JScrollPane(parentsList);
		parentsPane.setEnabled(false);
		parentsList.setEnabled(false);
		add(parentsPane, "growx, growy 200");
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
