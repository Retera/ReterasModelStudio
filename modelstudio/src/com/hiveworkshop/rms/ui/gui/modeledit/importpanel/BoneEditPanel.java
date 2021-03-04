package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.util.IterableListModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BoneEditPanel {
	static JPanel makeBonePanel(ModelHolderThing mht) {
		final BonePanelListCellRenderer bonePanelRenderer = new BonePanelListCellRenderer(mht.recModelManager, mht.donModelManager);
		mht.existingBones = new IterableListModel<>();
		final List<Bone> recModBones = mht.receivingModel.getBones();
		final List<Helper> recModHelpers = mht.receivingModel.getHelpers();
		for (Bone currentMDLBone : recModBones) {
			mht.existingBones.addElement(new BoneShell(currentMDLBone));
		}
		for (Helper currentMDLHelper : recModHelpers) {
			mht.existingBones.addElement(new BoneShell(currentMDLHelper));
		}

		final List<Bone> donModBones = mht.donatingModel.getBones();

		final List<Helper> donModHelpers = mht.donatingModel.getHelpers();

		mht.clearExistingBones = new JCheckBox("Clear pre-existing bones and helpers");
		// Initialized up here for use with BonePanels

		for (int i = 0; i < donModBones.size(); i++) {
			final Bone b = donModBones.get(i);
			final BonePanel bonePanel = new BonePanel(mht, b, mht.existingBones, mht.boneShellRenderer);

			mht.bonePanelCards.add(bonePanel, i + "");// (bonePanel.title.getText()));
			mht.bonePanels.addElement(bonePanel);
			mht.boneToPanel.put(b, bonePanel);
		}
		for (int i = 0; i < donModHelpers.size(); i++) {
			final Bone b = donModHelpers.get(i);
			final BonePanel bonePanel = new BonePanel(mht, b, mht.existingBones, mht.boneShellRenderer);

			mht.bonePanelCards.add(bonePanel, donModBones.size() + i + "");// (bonePanel.title.getText()));
			mht.bonePanels.addElement(bonePanel);
			mht.boneToPanel.put(b, bonePanel);
		}
		for (int i = 0; i < mht.bonePanels.size(); i++) {
			mht.bonePanels.get(i).initList();
		}
		mht.multiBonePane = new MultiBonePanel(mht, mht.existingBones, mht.boneShellRenderer);
		mht.bonePanelCards.add(mht.blankPane, "blank");
		mht.bonePanelCards.add(mht.multiBonePane, "multiple");
		mht.boneTabs.setCellRenderer(bonePanelRenderer);// bonePanelRenderer);
		mht.boneTabs.addListSelectionListener(e -> boneTabsValueChanged(mht));
		mht.boneTabs.setSelectedIndex(0);
		mht.bonePanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JButton importAllBones = new JButton("Import All");
		importAllBones.addActionListener(e -> mht.importAllBones(0));
		mht.bonesPanel.add(importAllBones);

		JButton uncheckAllBones = new JButton("Leave All");
		uncheckAllBones.addActionListener(e -> mht.importAllBones(2));
		mht.bonesPanel.add(uncheckAllBones);

		JButton motionFromBones = new JButton("Motion From All");
		motionFromBones.addActionListener(e -> mht.importAllBones(1));
		mht.bonesPanel.add(motionFromBones);

		JButton uncheckUnusedBones = new JButton("Uncheck Unused");
		uncheckUnusedBones.addActionListener(e -> uncheckUnusedBones(mht));
		mht.bonesPanel.add(uncheckUnusedBones);

		JScrollPane boneTabsPane = new JScrollPane(mht.boneTabs);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, boneTabsPane, mht.bonePanelCards);

		final GroupLayout boneLayout = new GroupLayout(mht.bonesPanel);
		boneLayout.setHorizontalGroup(boneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(boneLayout.createSequentialGroup()
						.addComponent(importAllBones).addGap(8)
						.addComponent(motionFromBones).addGap(8)
						.addComponent(uncheckUnusedBones).addGap(8)
						.addComponent(uncheckAllBones))
				.addComponent(mht.clearExistingBones)
				.addComponent(splitPane)
		);
		boneLayout.setVerticalGroup(boneLayout.createSequentialGroup()
				.addGroup(boneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(importAllBones)
						.addComponent(motionFromBones)
						.addComponent(uncheckUnusedBones)
						.addComponent(uncheckAllBones))
				.addComponent(mht.clearExistingBones).addGap(8)
				.addComponent(splitPane)
		);
		mht.bonesPanel.setLayout(boneLayout);
		return mht.bonesPanel;
	}

	private static void boneTabsValueChanged(ModelHolderThing mht) {
		// boolean listEnabledNow = false;
		if (mht.boneTabs.getSelectedValuesList().toArray().length < 1) {
			// listEnabledNow = listEnabled;
			mht.boneCardLayout.show(mht.bonePanelCards, "blank");
		} else if (mht.boneTabs.getSelectedValuesList().toArray().length == 1) {
			// listEnabledNow = true;
			mht.boneCardLayout.show(mht.bonePanelCards, (mht.boneTabs.getSelectedIndex()) + "");
			mht.boneTabs.getSelectedValue().updateSelectionPicks();
		} else if (mht.boneTabs.getSelectedValuesList().toArray().length > 1) {
			mht.boneCardLayout.show(mht.bonePanelCards, "multiple");
			// listEnabledNow = false;
			final Object[] selected = mht.boneTabs.getSelectedValuesList().toArray();
			boolean dif = false;
			int tempIndex = -99;
			for (int i = 0; (i < selected.length) && !dif; i++) {
				final BonePanel temp = (BonePanel) selected[i];
				if (tempIndex == -99) {
					tempIndex = temp.importTypeBox.getSelectedIndex();
				}
				if (tempIndex != temp.importTypeBox.getSelectedIndex()) {
					dif = true;
				}
			}
			if (dif) {
				mht.multiBonePane.setMultiTypes();
			} else {
				mht.multiBonePane.setSelectedIndex(tempIndex);
			}
		}
	}

	static void uncheckUnusedActualBones(ModelHolderThing mht, List<BonePanel> usedBonePanels) {
		for (BonePanel bonePanel : mht.bonePanels) {
			if (bonePanel.getSelectedIndex() != 1) {
				if (usedBonePanels.contains(bonePanel)) {
					BonePanel current = bonePanel;
					boolean good = true;
					int k = 0;
					while (good) {
						if ((current == null) || (current.getSelectedIndex() == 1)) {
							break;
						}
						final BoneShell shell = current.futureBonesList.getSelectedValue();
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
							JOptionPane.showMessageDialog(null,
									"Unexpected error has occurred: Bone parent loop, circular logic");
							break;
						}
					}
				}
			}
		}
		for (BonePanel bonePanel : mht.bonePanels) {
			if (bonePanel.getSelectedIndex() != 1) {
				if (usedBonePanels.contains(bonePanel)) {
					bonePanel.setSelectedIndex(0);
				} else {
					bonePanel.setSelectedIndex(2);
				}
			}
		}
	}

	public static void uncheckUnusedBones(ModelHolderThing mht) {
		// Unselect all bones by iterating + setting to index 2 ("Do not
		// import" index)
		// Bones could be referenced by:
		// - A matrix
		// - Another bone
		// - An IdObject
		final List<BonePanel> usedBonePanels = new ArrayList<>();
		ObjectEditPanel.uncheckUnusedObjects(mht, usedBonePanels);
		BoneAttatchmentEditPanel.uncheckUnusedBoneAttatchments(mht, usedBonePanels);
		uncheckUnusedActualBones(mht, usedBonePanels);
	}
}
