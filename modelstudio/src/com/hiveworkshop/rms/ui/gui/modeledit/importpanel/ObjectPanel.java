package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

class ObjectPanel extends JPanel {
	JLabel title;

	IdObject object;
	Camera camera;
	JCheckBox doImport;
	JLabel parentLabel;
	JLabel oldParentLabel;
	IterableListModel<BoneShell> parents;
	JList<BoneShell> parentsList;
	JScrollPane parentsPane;
	ModelHolderThing mht;

	protected ObjectPanel() {

	}

	public ObjectPanel(ModelHolderThing mht, final IdObject whichObject, final IterableListModel<BoneShell> possibleParents) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0", "[grow]", "[][][][][grow]"));
		object = whichObject;

		title = new JLabel(object.getClass().getSimpleName() + " \"" + object.getName() + "\"");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		doImport = new JCheckBox("Import this object");
		doImport.setSelected(true);
		add(doImport, "left, wrap");

		if (object.getParent() != null) {
			oldParentLabel = new JLabel("(Old Parent: " + object.getParent().getName() + ")");
		} else {
			oldParentLabel = new JLabel("(Old Parent: {no parent})");
		}
		add(oldParentLabel, "left, wrap");

		parentLabel = new JLabel("Parent:");
		add(parentLabel, "left, wrap");

		parents = possibleParents;
		parentsList = new JList<>(parents);
		parentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (BoneShell bs : parents) {
			if (bs.bone == object.getParent()) {
				parentsList.setSelectedValue(bs, true);
			}
		}

		parentsPane = new JScrollPane(parentsList);
		add(parentsPane, "growx, growy 200");
	}

	public ObjectPanel(final Camera c) {
		setLayout(new MigLayout("gap 0", "[grow]", "[][][][][grow]"));
		camera = c;

		title = new JLabel(c.getClass().getSimpleName() + " \"" + c.getName() + "\"");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		doImport = new JCheckBox("Import this object");
		doImport.setSelected(true);
		add(doImport, "left, wrap");

		oldParentLabel = new JLabel("(Cameras don't have parents)");
		add(oldParentLabel, "left, wrap");

//		parentLabel = new JLabel("Parent:");
//		add(parentLabel, "left, wrap");
	}
}
