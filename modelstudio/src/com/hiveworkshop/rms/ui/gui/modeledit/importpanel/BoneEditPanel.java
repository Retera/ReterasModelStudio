package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.util.BiMap;
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
	BonePanel singleBonePanel;
	ModelHolderThing mht;

	public BoneEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[][grow]"));
		this.mht = mht;

		add(getTopPanel(), "align center, wrap");


		final BonePanelListCellRenderer bonePanelRenderer = new BonePanelListCellRenderer(mht.recModelManager, mht.donModelManager);
		mht.recModOrgBones = new IterableListModel<>();

		final List<Bone> recModBones = mht.receivingModel.getBones();
		final List<Helper> recModHelpers = mht.receivingModel.getHelpers();

		mht.recModBoneShellBiMap = new BiMap<>();

		for (Bone bone : recModBones) {
			mht.recModOrgBones.addElement(new BoneShell(bone));
			mht.recModBoneShellBiMap.put(bone, new BoneShell(bone));
		}
		for (Helper helper : recModHelpers) {
			mht.recModOrgBones.addElement(new BoneShell(helper));
			mht.recModBoneShellBiMap.put(helper, new BoneShell(helper));
		}
		for (BoneShell bs : mht.recModBoneShellBiMap.values()) {
			bs.setParentBs(mht.recModBoneShellBiMap);
		}

		final List<Bone> donModBones = mht.donatingModel.getBones();
		final List<Helper> donModHelpers = mht.donatingModel.getHelpers();

		mht.donModBoneShellBiMap = new BiMap<>();
		for (Bone bone : donModBones) {
			mht.donModBoneShellBiMap.put(bone, new BoneShell(bone));
		}
		for (Helper helper : donModHelpers) {
			BoneShell bs = new BoneShell(helper);
			mht.donModBoneShellBiMap.put(helper, bs);
		}
		mht.donModBoneShells.addAll(mht.donModBoneShellBiMap.values());
		for (BoneShell bs : mht.donModBoneShellBiMap.values()) {
			bs.setParentBs(mht.donModBoneShellBiMap);
		}

		singleBonePanel = new BonePanel(mht, mht.boneShellRenderer);

		bonePanelCards.add(singleBonePanel, "single");

//		for (int i = 0; i < donModBones.size(); i++) {
//			final BoneShell b = mht.donModBoneShellBiMap.get(donModBones.get(i));
//			final BonePanel bonePanel = new BonePanel(mht, b, mht.boneShellRenderer);
//
//			bonePanelCards.add(bonePanel, i + "");
//			mht.donModBonePanels.addElement(bonePanel);
//			mht.boneToPanel.put(b.getBone(), bonePanel);
//		}
//		for (int i = 0; i < donModHelpers.size(); i++) {
//			final BoneShell b = mht.donModBoneShellBiMap.get(donModHelpers.get(i));
//			final BonePanel bonePanel = new BonePanel(mht, b, mht.boneShellRenderer);
//
//			bonePanelCards.add(bonePanel, donModBones.size() + i + "");
//			mht.donModBonePanels.addElement(bonePanel);
//			mht.boneToPanel.put(b.getBone(), bonePanel);
//		}

//		for (BonePanel bonePanel : mht.bonePanels) {
//			bonePanel.initList();
//		}
		multiBonePane = new MultiBonePanel(mht, mht.boneShellRenderer);
		bonePanelCards.add(blankPane, "blank");
		bonePanelCards.add(multiBonePane, "multiple");

		mht.donModBoneJList.setCellRenderer(bonePanelRenderer);
		mht.donModBoneJList.addListSelectionListener(e -> showBoneCard(mht));
		mht.donModBoneJList.setSelectedIndex(0);

		bonePanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));


		JScrollPane boneTabsPane = new JScrollPane(mht.donModBoneJList);
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

	private void showBoneCard(ModelHolderThing mht) {
		List<BoneShell> selectedValuesList = mht.donModBoneJList.getSelectedValuesList();
		if (selectedValuesList.size() < 1) {
			boneCardLayout.show(bonePanelCards, "blank");
		} else if (selectedValuesList.size() == 1) {
			boneCardLayout.show(bonePanelCards, "single");
			singleBonePanel.setSelectedBone(mht.donModBoneJList.getSelectedValue());
			singleBonePanel.updateSelectionPicks();
		} else {
			boneCardLayout.show(bonePanelCards, "multiple");
			boolean dif = false;

			int tempIndex = selectedValuesList.get(0).getImportStatus();

			for (BoneShell bp : selectedValuesList) {
				if (tempIndex != bp.getImportStatus()) {
					dif = true;
					break;
				}
			}
			if (dif) {
				multiBonePane.setMultiTypes();
			} else {
				multiBonePane.setSelectedIndex(tempIndex);
			}
		}
	}

	void uncheckUnusedActualBones(ModelHolderThing mht, List<BoneShell> usedBonePanels) {
		for (BoneShell bonePanel : mht.donModBoneShells) {
			if (bonePanel.getImportStatus() != 1) {
				if (usedBonePanels.contains(bonePanel)) {
					BoneShell current = bonePanel;
					boolean good = true;
					int k = 0;
					while (good) {
						if ((current == null) || (current.getImportStatus() == 1)) {
							break;
						}
						final BoneShell shell = current.getParentBs();
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
		for (BoneShell bonePanel : mht.donModBoneShells) {
			if (bonePanel.getImportStatus() != 1) {
				if (usedBonePanels.contains(bonePanel)) {
					bonePanel.setImportStatus(0);
				} else {
					bonePanel.setImportStatus(2);
				}
			}
		}
	}
//	void uncheckUnusedActualBones(ModelHolderThing mht, List<BonePanel> usedBonePanels) {
//		for (BonePanel bonePanel : mht.donModBonePanels) {
//			if (bonePanel.getSelectedIndex() != 1) {
//				if (usedBonePanels.contains(bonePanel)) {
//					BonePanel current = bonePanel;
//					boolean good = true;
//					int k = 0;
//					while (good) {
//						if ((current == null) || (current.getSelectedIndex() == 1)) {
//							break;
//						}
//						final BoneShell shell = current.futureBonesList.getSelectedValue();
//						// If shell is null, then the bone has "No Parent"
//						// If current's selected index is not 2,
//						if (shell == null)// current.getSelectedIndex() != 2
//						{
//							good = false;
//						} else {
//							current = mht.getPanelOf(shell.bone);
//							if (usedBonePanels.contains(current)) {
//								good = false;
//							} else {
//								usedBonePanels.add(current);
//							}
//						}
//						k++;
//						if (k > 1000) {
//							JOptionPane.showMessageDialog(null,
//									"Unexpected error has occurred: Bone parent loop, circular logic");
//							break;
//						}
//					}
//				}
//			}
//		}
//		for (BonePanel bonePanel : mht.donModBonePanels) {
//			if (bonePanel.getSelectedIndex() != 1) {
//				if (usedBonePanels.contains(bonePanel)) {
//					bonePanel.setSelectedIndex(0);
//				} else {
//					bonePanel.setSelectedIndex(2);
//				}
//			}
//		}
//	}

	public void uncheckUnusedBones(ModelHolderThing mht) {
		// Unselect all bones by iterating + setting to index 2 ("Do not import" index)
		// Bones could be referenced by:
		// - A matrix
		// - Another bone
		// - An IdObject
		final List<BoneShell> usedBonePanels = new ArrayList<>();
		ObjectEditPanel.uncheckUnusedObjects(mht, usedBonePanels);
		BoneAttachmentEditPanel.uncheckUnusedBoneAttatchments(mht, usedBonePanels);
		uncheckUnusedActualBones(mht, usedBonePanels);
	}
//	public void uncheckUnusedBones(ModelHolderThing mht) {
//		// Unselect all bones by iterating + setting to index 2 ("Do not import" index)
//		// Bones could be referenced by:
//		// - A matrix
//		// - Another bone
//		// - An IdObject
//		final List<BonePanel> usedBonePanels = new ArrayList<>();
//		ObjectEditPanel.uncheckUnusedObjects(mht, usedBonePanels);
//		BoneAttachmentEditPanel.uncheckUnusedBoneAttatchments(mht, usedBonePanels);
//		uncheckUnusedActualBones(mht, usedBonePanels);
//	}
}
