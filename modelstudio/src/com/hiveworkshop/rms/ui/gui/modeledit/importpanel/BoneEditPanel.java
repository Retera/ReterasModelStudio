package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
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

		singleBonePanel = new BonePanel(mht, mht.boneShellRenderer);
		multiBonePane = new MultiBonePanel(mht, mht.boneShellRenderer);

		bonePanelCards.add(blankPane, "blank");
		bonePanelCards.add(singleBonePanel, "single");
		bonePanelCards.add(multiBonePane, "multiple");
		bonePanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getBoneListPane(mht), bonePanelCards);
		add(splitPane, "growx, growy");
	}

	private JScrollPane getBoneListPane(ModelHolderThing mht) {
		BoneShellListCellRenderer bonePanelRenderer = new BoneShellListCellRenderer(mht.receivingModel, mht.donatingModel);

		mht.allBoneShellJList.setCellRenderer(bonePanelRenderer);
		mht.allBoneShellJList.addListSelectionListener(e -> showBoneCard(mht, e));
		mht.allBoneShellJList.setSelectedValue(null, false);
		return new JScrollPane(mht.allBoneShellJList);
//		mht.donModBoneShellJList.setCellRenderer(bonePanelRenderer);
//		mht.donModBoneShellJList.addListSelectionListener(e -> showBoneCard(mht, e));
//		mht.donModBoneShellJList.setSelectedValue(null, false);
//		return new JScrollPane(mht.donModBoneShellJList);
	}

	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("gap 0, debug", "[][][][]", "[][align center]"));
		topPanel.setOpaque(true);

		topPanel.add(createButton("Import All", e -> mht.setImportStatusForAllBones(BoneShell.ImportType.IMPORT)));
		topPanel.add(createButton("Motion From All", e -> mht.setImportStatusForAllBones(BoneShell.ImportType.MOTIONFROM)));
		topPanel.add(createButton("Uncheck Unused", e -> uncheckUnusedBones(mht)));
		topPanel.add(createButton("Leave All", e -> mht.setImportStatusForAllBones(BoneShell.ImportType.DONTIMPORT)), "wrap");

		mht.clearExistingBones = new JCheckBox("Clear pre-existing bones and helpers");
		topPanel.add(mht.clearExistingBones, "spanx 4, align center");
		return topPanel;
	}

	public JButton createButton(String text, ActionListener actionListener) {
		JButton jButton = new JButton(text);
		jButton.addActionListener(actionListener);
		return jButton;
	}

	private void showBoneCard(ModelHolderThing mht, ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			List<BoneShell> selectedValuesList = mht.allBoneShellJList.getSelectedValuesList();
			if (selectedValuesList.size() < 1) {
				mht.boneShellRenderer.setSelectedBoneShell(null);
				boneCardLayout.show(bonePanelCards, "blank");
			} else if (selectedValuesList.size() == 1) {
				singleBonePanel.setSelectedBone(mht.allBoneShellJList.getSelectedValue());
				boneCardLayout.show(bonePanelCards, "single");
			} else {
				multiBonePane.setSelectedBones(selectedValuesList);
				boneCardLayout.show(bonePanelCards, "multiple");
			}
		}
	}

	public void uncheckUnusedBones(ModelHolderThing mht) {
		// Unselect all bones by iterating + setting to index 2 ("Do not import" index)
		// Bones could be referenced by:
		// - A matrix
		// - Another bone
		// - An IdObject
		List<BoneShell> usedBoneShells = new ArrayList<>();

		collectUsedObjectParents(mht, usedBoneShells);
		collectUsedBoneAttatchments(mht, usedBoneShells);
		collectUsedBones(mht, usedBoneShells);
		uncheckUnusedActualBones(mht, usedBoneShells);
	}

	void uncheckUnusedActualBones(ModelHolderThing mht, List<BoneShell> usedBoneShells) {
		for (BoneShell boneShell : mht.donModBoneShells) {
			if (boneShell.getImportStatus() != BoneShell.ImportType.MOTIONFROM) {
				if (usedBoneShells.contains(boneShell)) {
					boneShell.setImportStatus(BoneShell.ImportType.IMPORT);
				} else {
					boneShell.setImportStatus(BoneShell.ImportType.DONTIMPORT);
				}
			}
		}
	}

	private void collectUsedBones(ModelHolderThing mht, List<BoneShell> usedBoneShells) {
		for (BoneShell boneShell : mht.donModBoneShells) {
			if (boneShell.getImportStatus() != BoneShell.ImportType.MOTIONFROM) {
				if (usedBoneShells.contains(boneShell)) {
					checkIfUsed(usedBoneShells, boneShell);
				}
			}
		}
	}

	void collectUsedObjectParents(ModelHolderThing mht, List<BoneShell> usedBoneShells) {
		for (ObjectShell objectShell : mht.donModObjectShells) {
			if (objectShell.getShouldImport()) {
				BoneShell shell = objectShell.getNewParentBs();
				if ((shell != null) && (shell.getBone() != null)) {
					if (!usedBoneShells.contains(shell)) {
						usedBoneShells.add(shell);
					}

					checkIfUsed(usedBoneShells, shell);
				}
			}
		}
	}

	void collectUsedBoneAttatchments(ModelHolderThing mht, List<BoneShell> usedBonePanels) {
		for (GeosetShell geosetShell : mht.allGeoShells) {
			if (geosetShell.isDoImport()) {
				for (MatrixShell ms : geosetShell.getMatrixShells()) {
					for (BoneShell boneShell : ms.getNewBones()) {
						if (!usedBonePanels.contains(boneShell)) {
							usedBonePanels.add(boneShell);
						}
						checkIfUsed(usedBonePanels, boneShell);
					}
				}
			}
		}
	}

	private void checkIfUsed(List<BoneShell> usedBoneShells, BoneShell boneShell) {
		int k = 0;

		BoneShell current = boneShell;
		for (; k < 1000; k++) {
			if (current == null
					|| current.getImportStatus() == BoneShell.ImportType.MOTIONFROM
					|| current.getNewParentBs() == null
					|| usedBoneShells.contains(current.getNewParentBs())) {
				break;
			}
			current = current.getNewParentBs();
			usedBoneShells.add(current);
		}

		if (k >= 1000) {
			JOptionPane.showMessageDialog(null, "Unexpected error has occurred: Bone parent loop, circular logic");

		}
	}
}
