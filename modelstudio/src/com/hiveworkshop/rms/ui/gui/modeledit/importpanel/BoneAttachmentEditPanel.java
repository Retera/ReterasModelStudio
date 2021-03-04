package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.ui.gui.modeledit.MatrixShell;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;

public class BoneAttachmentEditPanel extends JPanel {

	public JCheckBox displayParents = new JCheckBox("Display parent names");
	public JButton allMatrOriginal = new JButton("Reset all Matrices");
	public JButton allMatrSameName = new JButton("Set all to available, original names");
	ModelHolderThing mht;

	public BoneAttachmentEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[][grow]"));
		this.mht = mht;

		add(getTopPanel(), "align center, wrap");

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


		add(mht.geosetAnimTabs, "growx, growy");
	}

	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("gap 0", "[align center]"));
		topPanel.add(displayParents, "wrap");
		topPanel.add(allMatrOriginal, "wrap");
		topPanel.add(allMatrSameName, "wrap");
		return topPanel;
	}

	private ParentToggleRenderer makeMatricesPanel(ModelHolderThing mht, ModelViewManager recModelManager, ModelViewManager donModelManager) {
//		addTab("Skin", orangeIcon, new JPanel(), "Edit SKIN chunk");

		final ParentToggleRenderer ptr = new ParentToggleRenderer(displayParents, recModelManager, donModelManager);

		displayParents.addChangeListener(mht.getDaChangeListener());
		allMatrOriginal.addActionListener(e -> allMatrOriginal(mht.geosetAnimTabs));
		allMatrSameName.addActionListener(e -> allMatrSameName(mht.geosetAnimTabs));
		return ptr;
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
