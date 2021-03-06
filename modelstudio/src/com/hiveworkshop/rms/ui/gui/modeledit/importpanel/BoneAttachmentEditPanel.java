package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.gui.modeledit.MatrixShell;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;

public class BoneAttachmentEditPanel extends JPanel {

	JCheckBox displayParents;
	ModelHolderThing mht;

	public BoneAttachmentEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[][grow]"));
		this.mht = mht;

		add(getTopPanel(), "align center, wrap");

		final ParentToggleRenderer ptr = makeMatricesPanel(mht.recModelManager, mht.donModelManager);
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

	static void uncheckUnusedBoneAttatchments(ModelHolderThing mht, List<BoneShell> usedBonePanels) {
		for (int i = 0; i < mht.geosetAnimTabs.getTabCount(); i++) {
			if (mht.geosetAnimTabs.isEnabledAt(i)) {
				final BoneAttachmentPanel bap = (BoneAttachmentPanel) mht.geosetAnimTabs.getComponentAt(i);
				for (MatrixShell ms : bap.oldBoneRefs) {
					for (final BoneShell bs : ms.newBones) {
						BoneShell shell = bs;
						BoneShell current = shell;
						if (!usedBonePanels.contains(current)) {
							usedBonePanels.add(current);
						}

						boolean good = true;
						int k = 0;
						while (good) {
							if ((current == null) || (current.getImportStatus() == 1)) {
								break;
							}
							shell = current.getNewParentBs();
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
								JOptionPane.showMessageDialog(null, "Unexpected error has occurred: IdObject to Bone parent loop, circular logic");
								break;
							}
						}
					}
				}
			}
		}
	}

	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("gap 0", "[align center]"));

		displayParents = new JCheckBox("Display parent names");
		displayParents.addChangeListener(mht.getDaChangeListener());
		topPanel.add(displayParents, "wrap");

		JButton allMatrOriginal = new JButton("Reset all Matrices");
		allMatrOriginal.addActionListener(e -> allMatrOriginal());
		topPanel.add(allMatrOriginal, "wrap");

		JButton allMatrSameName = new JButton("Set all to available, original names");
		allMatrSameName.addActionListener(e -> allMatrSameName());
		topPanel.add(allMatrSameName, "wrap");

		return topPanel;
	}

	private ParentToggleRenderer makeMatricesPanel(ModelViewManager recModelManager, ModelViewManager donModelManager) {
		return new ParentToggleRenderer(displayParents, recModelManager, donModelManager);
	}

	public void allMatrOriginal() {
		for (int i = 0; i < mht.geosetAnimTabs.getTabCount(); i++) {
			if (mht.geosetAnimTabs.isEnabledAt(i)) {
				final BoneAttachmentPanel bap = (BoneAttachmentPanel) mht.geosetAnimTabs.getComponentAt(i);
				bap.resetMatrices();
			}
		}
	}

	public void allMatrSameName() {
		for (int i = 0; i < mht.geosetAnimTabs.getTabCount(); i++) {
			if (mht.geosetAnimTabs.isEnabledAt(i)) {
				final BoneAttachmentPanel bap = (BoneAttachmentPanel) mht.geosetAnimTabs.getComponentAt(i);
				bap.setMatricesToSimilarNames();
			}
		}
	}
}
