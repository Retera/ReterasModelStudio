package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import com.hiveworkshop.rms.ui.util.TwiListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.List;

class ObjectPanel extends JPanel {
	ModelHolderThing mht;
	JLabel title;

	JCheckBox doImport;
	JLabel parentLabel;
	JLabel oldParentLabel;
	List<IdObjectShell<?>> parents;
	TwiList<IdObjectShell<?>> parentsList;
	JScrollPane parentsPane;
	BoneShellListCellRenderer bonePanelRenderer;

	IdObjectShell<?> selectedObject;

	protected ObjectPanel() {

	}

	public ObjectPanel(ModelHolderThing mht, BoneShellListCellRenderer bonePanelRenderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0, ins 0", "[grow]", "[][][][][grow]"));

		title = new JLabel("Object Title");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		this.bonePanelRenderer = bonePanelRenderer;

		doImport = new JCheckBox("Import this object");
		doImport.addActionListener(e -> setImportStatus(doImport.isSelected()));
		add(doImport, "left, wrap");


		oldParentLabel = new JLabel("(Old Parent: {no parent})");
		add(oldParentLabel, "left, wrap");


		parentLabel = new JLabel("Parent:");
		add(parentLabel, "left, wrap");

		add(getParentListPane(bonePanelRenderer), "growx, growy 200");
	}

	private JScrollPane getParentListPane(BoneShellListCellRenderer bonePanelRenderer) {
		parentsList = new TwiList<>();
		parentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		parentsList.setCellRenderer(bonePanelRenderer);
		parentsList.addListSelectionListener(this::setParent);

		parentsPane = new JScrollPane(parentsList);
		return parentsPane;
	}

	public void setSelectedObject(IdObjectShell<?> objectShell) {
		this.selectedObject = objectShell;
		setTitles();
		parents = mht.getFutureBoneHelperList();
		bonePanelRenderer.setSelectedObjectShell(objectShell);
		setParentListModel();
		scrollToRevealParent(objectShell);
		doImport.setSelected(objectShell.getShouldImport());
		repaint();
	}


	private void scrollToRevealParent(IdObjectShell<?> objectShell) {
		if (objectShell.getNewParentShell() != null) {
			int i = parents.indexOf(objectShell.getNewParentShell());
			if (i != -1) {
				Rectangle cellBounds = parentsList.getCellBounds(i, i);
				if (cellBounds != null) {
					parentsList.scrollRectToVisible(cellBounds);
				}
			}
		}
	}


	private void setParentListModel() {
		parentsList.setListModel(new TwiListModel<>(parents));
	}

	private void setParent(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && parentsList.getSelectedValue() != null) {
			if (parentsList.getSelectedValue() == selectedObject.getNewParentShell()) {
				selectedObject.setNewParentShell(null);
			} else {
				selectedObject.setNewParentShell(parentsList.getSelectedValue());
			}
		}
	}


	private void setTitles() {
		title.setText(selectedObject.toString());
//		title.setText(object.getClass().getSimpleName() + " \"" + object.getName() + "\"");

		if (selectedObject.getOldParentShell() != null) {
			oldParentLabel.setText("(Old Parent: " + selectedObject.getOldParentShell().getName() + ")");
		} else {
			oldParentLabel.setText("(Old Parent: {no parent})");
		}
	}

	private void setImportStatus(boolean doImport) {
		selectedObject.setShouldImport(doImport);
	}

}
