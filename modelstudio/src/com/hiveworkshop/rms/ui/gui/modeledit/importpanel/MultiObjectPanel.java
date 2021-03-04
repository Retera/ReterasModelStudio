package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

class MultiObjectPanel extends ObjectPanel {
	boolean oldVal = true;
	ModelHolderThing mht;

	public MultiObjectPanel(ModelHolderThing mht, final IterableListModel<BoneShell> possibleParents) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0", "[grow]", "[][][][][grow]"));
		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		doImport = new JCheckBox("Import these objects (click to apply to all)");
		doImport.setSelected(true);
		doImport.addChangeListener(e -> doImportPressed());
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

	private void doImportPressed() {
		if (doImport.isSelected() != oldVal) {
			setObjGroupSelected(doImport.isSelected());
			oldVal = doImport.isSelected();
		}
	}

	public void setObjGroupSelected(final boolean flag) {
		for (ObjectPanel temp : mht.objectTabs.getSelectedValuesList()) {
			temp.doImport.setSelected(flag);
		}
	}
}
