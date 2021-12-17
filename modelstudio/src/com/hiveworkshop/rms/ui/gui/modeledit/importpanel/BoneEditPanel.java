package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BoneEditPanel extends JPanel {

	private final CardLayout boneCardLayout = new CardLayout();
	private final JPanel bonePanelCards = new JPanel(boneCardLayout);
	private final MultiBonePanel multiBonePane;
	private final BonePanel singleBonePanel;

	private final JList<IdObjectShell<?>> allBoneShellJList;
	private final ModelHolderThing mht;

	public BoneEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[][grow]"));
		this.mht = mht;
		allBoneShellJList = new JList<>(mht.allBoneShells);

		add(getTopPanel(), "align center, wrap");

		singleBonePanel = new BonePanel(mht, mht.boneShellRenderer);
		multiBonePane = new MultiBonePanel(mht, mht.boneShellRenderer);

		JPanel blankPane = new JPanel();
		bonePanelCards.add(blankPane, "blank");
		bonePanelCards.add(singleBonePanel, "single");
		bonePanelCards.add(multiBonePane, "multiple");
		bonePanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getBoneListPane(mht), bonePanelCards);
		add(splitPane, "growx, growy");
	}

	private JScrollPane getBoneListPane(ModelHolderThing mht) {
		BoneShellListCellRenderer bonePanelRenderer = new BoneShellListCellRenderer(mht.receivingModel, mht.donatingModel);

		allBoneShellJList.setCellRenderer(bonePanelRenderer);
		allBoneShellJList.addListSelectionListener(e -> showBoneCard(mht, e));
		allBoneShellJList.setSelectedValue(null, false);
		return new JScrollPane(allBoneShellJList);
//		mht.donModBoneShellJList.setCellRenderer(bonePanelRenderer);
//		mht.donModBoneShellJList.addListSelectionListener(e -> showBoneCard(mht, e));
//		mht.donModBoneShellJList.setSelectedValue(null, false);
//		return new JScrollPane(mht.donModBoneShellJList);
	}

	private JPanel getTopPanel() {
//		JPanel topPanel = new JPanel(new MigLayout("gap 0, debug", "[][][][]", "[][align center]"));
		JPanel topPanel = new JPanel(new MigLayout("gap 0", "[][]", "[align center][align center]"));
		topPanel.setOpaque(true);

		topPanel.add(getSetImpTypePanel(mht.receivingModel.getName(), (i) -> mht.setImportStatusForAllRecBones(i)), "");
		topPanel.add(getSetImpTypePanel(mht.donatingModel.getName(), (i) -> mht.setImportStatusForAllDonBones(i)), "wrap");
		topPanel.add(getButton("Uncheck Unused", e -> uncheckUnusedBones(mht)), "spanx 4, align center");

		mht.clearExistingBones = new JCheckBox("Clear pre-existing bones and helpers");
//		topPanel.add(mht.clearExistingBones, "spanx 4, align center");
		return topPanel;
	}

	private JPanel getSetImpTypePanel(String modelName, Consumer<IdObjectShell.ImportType> importTypeConsumer) {
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0", "[][][]", "[align center]"));
		panel.setOpaque(true);
		panel.setBorder(BorderFactory.createTitledBorder(modelName));

		panel.add(getButton("Import All", e -> importTypeConsumer.accept(IdObjectShell.ImportType.IMPORT)), "");
		panel.add(getButton("Motion From All", e -> importTypeConsumer.accept(IdObjectShell.ImportType.MOTION_FROM)), "");
		panel.add(getButton("Leave All", e -> importTypeConsumer.accept(IdObjectShell.ImportType.DONT_IMPORT)), "");

		return panel;
	}

	public JButton getButton(String text, ActionListener actionListener) {
		JButton jButton = new JButton(text);
		jButton.addActionListener(actionListener);
		return jButton;
	}

	private void showBoneCard(ModelHolderThing mht, ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			List<IdObjectShell<?>> selectedValuesList = allBoneShellJList.getSelectedValuesList();
			if (selectedValuesList.size() < 1) {
				mht.boneShellRenderer.setSelectedBoneShell(null);
				boneCardLayout.show(bonePanelCards, "blank");
			} else if (selectedValuesList.size() == 1) {
				singleBonePanel.setSelectedBone(allBoneShellJList.getSelectedValue());
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
		List<IdObjectShell<?>> usedBoneShells = new ArrayList<>();

		collectUsedObjectParents(mht, usedBoneShells);
		collectUsedBoneAttatchments(mht, usedBoneShells);
		collectUsedBones(mht, usedBoneShells);
		uncheckUnusedActualBones(mht, usedBoneShells);
	}

	void uncheckUnusedActualBones(ModelHolderThing mht, List<IdObjectShell<?>> usedBoneShells) {
		for (IdObjectShell<?> boneShell : mht.donModBoneShells) {
			if (boneShell.getImportStatus() != IdObjectShell.ImportType.MOTION_FROM) {
				if (usedBoneShells.contains(boneShell)) {
					boneShell.setImportStatus(IdObjectShell.ImportType.IMPORT);
				} else {
					boneShell.setImportStatus(IdObjectShell.ImportType.DONT_IMPORT);
				}
			}
		}
	}

	private void collectUsedBones(ModelHolderThing mht, List<IdObjectShell<?>> usedBoneShells) {
		for (IdObjectShell<?> boneShell : mht.donModBoneShells) {
			if (boneShell.getImportStatus() != IdObjectShell.ImportType.MOTION_FROM) {
				if (usedBoneShells.contains(boneShell)) {
					checkIfUsed(usedBoneShells, boneShell);
				}
			}
		}
	}

	void collectUsedObjectParents(ModelHolderThing mht, List<IdObjectShell<?>> usedBoneShells) {
		for (IdObjectShell<?> objectShell : mht.donModObjectShells) {
			if (objectShell.getShouldImport()) {
				IdObjectShell<?> shell = objectShell.getNewParentShell();
				if ((shell != null) && (shell.getIdObject() != null)) {
					if (!usedBoneShells.contains(shell)) {
						usedBoneShells.add(shell);
					}

					checkIfUsed(usedBoneShells, shell);
				}
			}
		}
	}

	void collectUsedBoneAttatchments(ModelHolderThing mht, List<IdObjectShell<?>> usedBonePanels) {
		for (GeosetShell geosetShell : mht.allGeoShells) {
			if (geosetShell.isDoImport()) {
				for (MatrixShell ms : geosetShell.getMatrixShells()) {
					for (IdObjectShell<?> boneShell : ms.getNewBones()) {
						if (!usedBonePanels.contains(boneShell)) {
							usedBonePanels.add(boneShell);
						}
						checkIfUsed(usedBonePanels, boneShell);
					}
				}
			}
		}
	}

	private void checkIfUsed(List<IdObjectShell<?>> usedBoneShells, IdObjectShell<?> boneShell) {
		int k = 0;

		IdObjectShell<?> current = boneShell;
		for (; k < 1000; k++) {
			if (current == null
					|| current.getImportStatus() == IdObjectShell.ImportType.MOTION_FROM
					|| current.getNewParentShell() == null
					|| usedBoneShells.contains(current.getNewParentShell())) {
				break;
			}
			current = current.getNewParentShell();
			usedBoneShells.add(current);
		}

		if (k >= 1000) {
			JOptionPane.showMessageDialog(null, "Unexpected error has occurred: Bone parent loop, circular logic");

		}
	}
}
