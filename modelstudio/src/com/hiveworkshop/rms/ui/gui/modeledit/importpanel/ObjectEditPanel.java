package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ObjectEditPanel {
	static JPanel makeObjecsPanel(ModelHolderThing mht) {
		JPanel objectsPanel = new JPanel();
		JSplitPane splitPane;
		mht.getFutureBoneListExtended(false);

		// Build the objectTabs list of ObjectPanels
		final ObjPanelListCellRenderer objectPanelRenderer = new ObjPanelListCellRenderer();
		int panelid = 0;
		for (int i = 0; i < mht.donatingModel.getIdObjects().size(); i++) {
			final IdObject obj = mht.donatingModel.getIdObjects().get(i);
			if ((obj.getClass() != Bone.class) && (obj.getClass() != Helper.class)) {

				final ObjectPanel objPanel = new ObjectPanel(mht, obj, mht.getFutureBoneListExtended(true));

				mht.objectPanelCards.add(objPanel, panelid + "");
				mht.objectPanels.addElement(objPanel);
				panelid++;
			}
		}
		for (int i = 0; i < mht.donatingModel.getCameras().size(); i++) {
			final Camera obj = mht.donatingModel.getCameras().get(i);

			final ObjectPanel objPanel = new ObjectPanel(obj);

			mht.objectPanelCards.add(objPanel, panelid + "");// (objPanel.title.getText()));
			mht.objectPanels.addElement(objPanel);
			panelid++;
		}
		mht.multiObjectPane = new MultiObjectPanel(mht, mht.getFutureBoneListExtended(true));
		mht.objectPanelCards.add(mht.blankPane, "blank");
		mht.objectPanelCards.add(mht.multiObjectPane, "multiple");
		mht.objectTabs.setCellRenderer(objectPanelRenderer);
		mht.objectTabs.addListSelectionListener(e -> objectTabsValueChanged(mht));
		mht.objectTabs.setSelectedIndex(0);
		mht.objectPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JButton importAllObjs = new JButton("Import All");
		importAllObjs.addActionListener(e -> mht.importAllObjs(true));
		mht.bonesPanel.add(importAllObjs);

		JButton uncheckAllObjs = new JButton("Leave All");
		uncheckAllObjs.addActionListener(e -> mht.importAllObjs(false));
		mht.bonesPanel.add(uncheckAllObjs);

		JScrollPane objectTabsPane = new JScrollPane(mht.objectTabs);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, objectTabsPane, mht.objectPanelCards);

		final GroupLayout objectLayout = new GroupLayout(objectsPanel);
		objectLayout.setHorizontalGroup(objectLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(objectLayout.createSequentialGroup()
						.addComponent(importAllObjs).addGap(8)
						.addComponent(uncheckAllObjs))
				.addComponent(splitPane));
		objectLayout.setVerticalGroup(objectLayout.createSequentialGroup()
				.addGroup(objectLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(importAllObjs)
						.addComponent(uncheckAllObjs)).addGap(8)
				.addComponent(splitPane));
		objectsPanel.setLayout(objectLayout);
		return objectsPanel;
	}

	private static void objectTabsValueChanged(ModelHolderThing mht) {
		if (mht.objectTabs.getSelectedValuesList().toArray().length < 1) {
			mht.objectCardLayout.show(mht.objectPanelCards, "blank");
		} else if (mht.objectTabs.getSelectedValuesList().toArray().length == 1) {
			mht.getFutureBoneListExtended(false);
			mht.objectCardLayout.show(mht.objectPanelCards, (mht.objectTabs.getSelectedIndex()) + "");// .title.getText()
		} else if (mht.objectTabs.getSelectedValuesList().toArray().length > 1) {
			mht.objectCardLayout.show(mht.objectPanelCards, "multiple");
			final Object[] selected = mht.objectTabs.getSelectedValuesList().toArray();
			boolean dif = false;
			boolean set = false;
			boolean selectedt = false;
			for (int i = 0; (i < selected.length) && !dif; i++) {
				final ObjectPanel temp = (ObjectPanel) selected[i];
				if (!set) {
					set = true;
					selectedt = temp.doImport.isSelected();
				} else if (selectedt != temp.doImport.isSelected()) {
					dif = true;
				}
			}
			if (!dif) {
				mht.multiObjectPane.doImport.setSelected(selectedt);
			}
		}
	}

	static void uncheckUnusedObjects(ModelHolderThing mht, List<BonePanel> usedBonePanels) {
		for (ObjectPanel objectPanel : mht.objectPanels) {
			if (objectPanel.doImport.isSelected() && (objectPanel.parentsList != null)) {
				BoneShell shell = objectPanel.parentsList.getSelectedValue();
				if ((shell != null) && (shell.bone != null)) {
					BonePanel current = mht.getPanelOf(shell.bone);
					if (!usedBonePanels.contains(current)) {
						usedBonePanels.add(current);
					}

					boolean good = true;
					int k = 0;
					while (good) {
						if ((current == null) || (current.getSelectedIndex() == 1)) {
							break;
						}
						shell = current.futureBonesList.getSelectedValue();
						// If shell is null, then the bone has "No Parent"
						// If current's selected index is not 2,
						if (shell == null)// current.getSelectedIndex() != 2
						{
							good = false;
						} else {
							current = mht.getPanelOf(shell.bone);
							if (usedBonePanels.contains(current)) {
								good = false;
							} else {
								usedBonePanels.add(current);
							}
						}
						k++;
						if (k > 1000) {
							JOptionPane.showMessageDialog(null, "Unexpected error has occurred: IdObject to Bone parent loop, circular logic");
							break;
						}
					}
				}
			}
		}
	}
}
