package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.ui.gui.modeledit.MatrixShell;

import javax.swing.*;
import java.util.List;

public class BoneAttatchmentEditPanel {
	private static ParentToggleRenderer makeMatricesPanel(ModelHolderThing mht, ModelViewManager recModelManager, ModelViewManager donModelManager) {
//		addTab("Skin", orangeIcon, new JPanel(), "Edit SKIN chunk");

		final ParentToggleRenderer ptr = new ParentToggleRenderer(mht.displayParents, recModelManager, donModelManager);

		mht.displayParents.addChangeListener(mht.getDaChangeListener());

		mht.allMatrOriginal.addActionListener(e -> allMatrOriginal(mht.geosetAnimTabs));
		mht.allMatrSameName.addActionListener(e -> allMatrSameName(mht.geosetAnimTabs));
		return ptr;
	}

	static JPanel makeGeosetAnimPanel(ModelHolderThing mht) {
		final ParentToggleRenderer ptr = makeMatricesPanel(mht, mht.recModelManager, mht.donModelManager);
		for (int i = 0; i < mht.receivingModel.getGeosets().size(); i++) {
			final BoneAttachmentPanel geoPanel = new BoneAttachmentPanel(mht, mht.receivingModel, mht.receivingModel.getGeoset(i), ptr);

			mht.geosetAnimTabs.addTab(mht.receivingModel.getName() + " " + (i + 1), ImportPanel.greenIcon, geoPanel, "Click to modify animation data for Geoset " + i + " from " + mht.receivingModel.getName() + ".");
		}
		for (int i = 0; i < mht.donatingModel.getGeosets().size(); i++) {
			final BoneAttachmentPanel geoPanel = new BoneAttachmentPanel(mht, mht.donatingModel, mht.donatingModel.getGeoset(i), ptr);

			mht.geosetAnimTabs.addTab(mht.donatingModel.getName() + " " + (i + 1), ImportPanel.orangeIcon, geoPanel, "Click to modify animation data for Geoset " + i + " from " + mht.donatingModel.getName() + ".");
		}
		mht.geosetAnimTabs.addChangeListener(mht.getDaChangeListener());

		mht.geosetAnimPanel.add(mht.geosetAnimTabs);
		final GroupLayout gaLayout = new GroupLayout(mht.geosetAnimPanel);
		gaLayout.setVerticalGroup(gaLayout.createSequentialGroup()
				.addComponent(mht.displayParents)
				.addComponent(mht.allMatrOriginal)
				.addComponent(mht.allMatrSameName)
				.addComponent(mht.geosetAnimTabs));
		gaLayout.setHorizontalGroup(gaLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(mht.displayParents)
				.addComponent(mht.allMatrOriginal)
				.addComponent(mht.allMatrSameName)
				.addComponent(mht.geosetAnimTabs));
		mht.geosetAnimPanel.setLayout(gaLayout);

		return mht.geosetAnimPanel;
	}

	static void uncheckUnusedBoneAttatchments(ModelHolderThing mht, List<BonePanel> usedBonePanels) {
		for (int i = 0; i < mht.geosetAnimTabs.getTabCount(); i++) {
			if (mht.geosetAnimTabs.isEnabledAt(i)) {
				final BoneAttachmentPanel bap = (BoneAttachmentPanel) mht.geosetAnimTabs.getComponentAt(i);
				for (int mk = 0; mk < bap.oldBoneRefs.size(); mk++) {
					final MatrixShell ms = bap.oldBoneRefs.get(mk);
					for (final BoneShell bs : ms.newBones) {
						BoneShell shell = bs;
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

	public static void allMatrOriginal(JTabbedPane geosetAnimTabs) {
		for (int i = 0; i < geosetAnimTabs.getTabCount(); i++) {
			if (geosetAnimTabs.isEnabledAt(i)) {
				final BoneAttachmentPanel bap = (BoneAttachmentPanel) geosetAnimTabs.getComponentAt(i);
				bap.resetMatrices();
			}
		}
	}

	public static void allMatrSameName(JTabbedPane geosetAnimTabs) {
		for (int i = 0; i < geosetAnimTabs.getTabCount(); i++) {
			if (geosetAnimTabs.isEnabledAt(i)) {
				final BoneAttachmentPanel bap = (BoneAttachmentPanel) geosetAnimTabs.getComponentAt(i);
				bap.setMatricesToSimilarNames();
			}
		}
	}
}
