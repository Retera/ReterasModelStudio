package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
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

		BoneShellListCellRenderer bonePanelRenderer = new BoneShellListCellRenderer(mht.recModelManager, mht.donModelManager);

		mht.donModBoneShellJList.setCellRenderer(bonePanelRenderer);
		mht.donModBoneShellJList.addListSelectionListener(e -> showBoneCard(mht, e));
		mht.donModBoneShellJList.setSelectedValue(null, false);
		JScrollPane boneTabsPane = new JScrollPane(mht.donModBoneShellJList);


		bonePanelCards.add(blankPane, "blank");

		singleBonePanel = new BonePanel(mht, mht.boneShellRenderer);
		bonePanelCards.add(singleBonePanel, "single");

		multiBonePane = new MultiBonePanel(mht, mht.boneShellRenderer);
		bonePanelCards.add(multiBonePane, "multiple");

		bonePanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, boneTabsPane, bonePanelCards);
		add(splitPane, "growx, growy");
	}

	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("gap 0, debug", "[][][][]", "[][align center]"));
		topPanel.setOpaque(true);

		JButton importAllBones = createButton(e -> mht.setImportStatusForAllBones(BoneShell.ImportType.IMPORT), "Import All");
		topPanel.add(importAllBones);

		JButton motionFromBones = createButton(e -> mht.setImportStatusForAllBones(BoneShell.ImportType.MOTIONFROM), "Motion From All");
		topPanel.add(motionFromBones);

		JButton uncheckUnusedBones = createButton(e -> uncheckUnusedBones(mht), "Uncheck Unused");
		topPanel.add(uncheckUnusedBones);

		JButton uncheckAllBones = createButton(e -> mht.setImportStatusForAllBones(BoneShell.ImportType.DONTIMPORT), "Leave All");
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

	private void showBoneCard(ModelHolderThing mht, ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			List<BoneShell> selectedValuesList = mht.donModBoneShellJList.getSelectedValuesList();
			if (selectedValuesList.size() < 1) {
				mht.boneShellRenderer.setSelectedBoneShell(null);
				boneCardLayout.show(bonePanelCards, "blank");
			} else if (selectedValuesList.size() == 1) {
				mht.boneShellRenderer.setSelectedBoneShell(mht.donModBoneShellJList.getSelectedValue());
				singleBonePanel.setSelectedBone(mht.donModBoneShellJList.getSelectedValue());
				boneCardLayout.show(bonePanelCards, "single");
			} else {
				mht.boneShellRenderer.setSelectedBoneShell(null);
				multiBonePane.updateMultiBonePanel();
				boneCardLayout.show(bonePanelCards, "multiple");
			}
		}
	}

	void uncheckUnusedActualBones(ModelHolderThing mht, List<BoneShell> usedBonePanels) {
		for (BoneShell bonePanel : mht.donModBoneShells) {
			if (bonePanel.getImportStatus() != BoneShell.ImportType.MOTIONFROM) {
				if (usedBonePanels.contains(bonePanel)) {
					BoneShell current = bonePanel;
					boolean good = true;
					int k = 0;
					while (good) {
						if (current.getImportStatus() == BoneShell.ImportType.MOTIONFROM) {
							break;
						}
						final BoneShell shell = current.getNewParentBs();
						// If shell is null, then the bone has "No Parent"
						// If current's selected index is not 2,
						if (shell == null)// current.getSelectedIndex() != 2
						{
							good = false;
						} else {
							current = shell;
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
		for (BoneShell boneShell : mht.donModBoneShells) {
			if (boneShell.getImportStatus() != BoneShell.ImportType.MOTIONFROM) {
				if (usedBonePanels.contains(boneShell)) {
					boneShell.setImportStatus(BoneShell.ImportType.IMPORT);
				} else {
					boneShell.setImportStatus(BoneShell.ImportType.DONTIMPORT);
				}
			}
		}
	}

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
}
