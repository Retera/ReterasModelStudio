package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class BoneEditPanel extends JPanel {

	public CardLayout boneCardLayout = new CardLayout();
	public JPanel bonePanelCards = new JPanel(boneCardLayout);
	public JPanel blankPane = new JPanel();
	public MultiBonePanel multiBonePane;
	ModelHolderThing mht;

	public BoneEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[][grow]"));
		this.mht = mht;

		add(getTopPanel(), "align center, wrap");


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

		for (int i = 0; i < donModBones.size(); i++) {
			final Bone b = donModBones.get(i);
			final BonePanel bonePanel = new BonePanel(mht, b, mht.existingBones, mht.boneShellRenderer);

			bonePanelCards.add(bonePanel, i + "");// (bonePanel.title.getText()));
			mht.bonePanels.addElement(bonePanel);
			mht.boneToPanel.put(b, bonePanel);
		}
		for (int i = 0; i < donModHelpers.size(); i++) {
			final Bone b = donModHelpers.get(i);
			final BonePanel bonePanel = new BonePanel(mht, b, mht.existingBones, mht.boneShellRenderer);

			bonePanelCards.add(bonePanel, donModBones.size() + i + "");// (bonePanel.title.getText()));
			mht.bonePanels.addElement(bonePanel);
			mht.boneToPanel.put(b, bonePanel);
		}
		for (int i = 0; i < mht.bonePanels.size(); i++) {
			mht.bonePanels.get(i).initList();
		}
		multiBonePane = new MultiBonePanel(mht, mht.existingBones, mht.boneShellRenderer);
		bonePanelCards.add(blankPane, "blank");
		bonePanelCards.add(multiBonePane, "multiple");
		mht.boneTabs.setCellRenderer(bonePanelRenderer);// bonePanelRenderer);
		mht.boneTabs.addListSelectionListener(e -> boneTabsValueChanged(mht));
		mht.boneTabs.setSelectedIndex(0);
		bonePanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));


		JScrollPane boneTabsPane = new JScrollPane(mht.boneTabs);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, boneTabsPane, bonePanelCards);

		add(splitPane, "growx, growy");

	}

	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("gap 0, debug", "[][][][]", "[][align center]"));
		topPanel.setBackground(Color.magenta);
		topPanel.setOpaque(true);

		JButton importAllBones = createButton(e -> mht.setImportStatusForAllBones(0), "Import All");
		topPanel.add(importAllBones);

		JButton motionFromBones = createButton(e -> mht.setImportStatusForAllBones(1), "Motion From All");
		topPanel.add(motionFromBones);

		JButton uncheckUnusedBones = createButton(e -> uncheckUnusedBones(mht), "Uncheck Unused");
		topPanel.add(uncheckUnusedBones);

		JButton uncheckAllBones = createButton(e -> mht.setImportStatusForAllBones(2), "Leave All");
		topPanel.add(uncheckAllBones, "wrap");

		mht.clearExistingBones = new JCheckBox("Clear pre-existing bones and helpers");
		topPanel.add(mht.clearExistingBones, "spanx 4, align center");
		return topPanel;
	}

	public JButton createButton(ActionListener actionListener, String text) {
		JButton jButton = new JButton(text);
		jButton.addActionListener(actionListener);
		return jButton;
	}

	private void boneTabsValueChanged(ModelHolderThing mht) {
		// boolean listEnabledNow = false;
		if (mht.boneTabs.getSelectedValuesList().toArray().length < 1) {
			// listEnabledNow = listEnabled;
			boneCardLayout.show(bonePanelCards, "blank");
		} else if (mht.boneTabs.getSelectedValuesList().toArray().length == 1) {
			// listEnabledNow = true;
			boneCardLayout.show(bonePanelCards, (mht.boneTabs.getSelectedIndex()) + "");
			mht.boneTabs.getSelectedValue().updateSelectionPicks();
		} else if (mht.boneTabs.getSelectedValuesList().toArray().length > 1) {
			boneCardLayout.show(bonePanelCards, "multiple");
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
				multiBonePane.setMultiTypes();
			} else {
				multiBonePane.setSelectedIndex(tempIndex);
			}
		}
	}

	void uncheckUnusedActualBones(ModelHolderThing mht, List<BonePanel> usedBonePanels) {
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

	public void uncheckUnusedBones(ModelHolderThing mht) {
		// Unselect all bones by iterating + setting to index 2 ("Do not
		// import" index)
		// Bones could be referenced by:
		// - A matrix
		// - Another bone
		// - An IdObject
		final List<BonePanel> usedBonePanels = new ArrayList<>();
		ObjectEditPanel.uncheckUnusedObjects(mht, usedBonePanels);
		BoneAttachmentEditPanel.uncheckUnusedBoneAttatchments(mht, usedBonePanels);
		uncheckUnusedActualBones(mht, usedBonePanels);
	}
}
