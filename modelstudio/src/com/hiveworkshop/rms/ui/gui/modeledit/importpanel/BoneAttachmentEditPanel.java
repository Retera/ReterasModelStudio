package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Matrix;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.GeosetShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.IdObjectShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.MatrixShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.util.ScreenInfo;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoneAttachmentEditPanel extends JPanel {

	JCheckBox displayParents;
	BoneShellListCellRenderer renderer;
	ModelHolderThing mht;
	public JTabbedPane geosetAnimTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);

	public BoneAttachmentEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[][grow]"));
		this.mht = mht;

		renderer = new BoneShellListCellRenderer(mht.receivingModel, mht.donatingModel);

		add(getTopPanel(), "align center, wrap");

		for (GeosetShell geosetShell : mht.allGeoShells) {
			final BoneAttachmentPanel geoPanel = new BoneAttachmentPanel(mht, renderer);
			geoPanel.setGeoset(geosetShell);
			ImageIcon imageIcon = ImportPanel.greenIcon;
			if (geosetShell.isFromDonating()) {
				imageIcon = ImportPanel.orangeIcon;
			}
			geosetAnimTabs.addTab(geosetShell.getModelName() + " " + (geosetShell.getIndex() + 1), imageIcon, geoPanel, "Click to modify animation data for Geoset " + geosetShell.getIndex() + " from " + geosetShell.getModelName() + ".");
		}

		add(geosetAnimTabs, "growx, growy");

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				super.componentShown(e);
				for (GeosetShell geosetShell : mht.allGeoShells) {
					for (int i = 0; i < geosetAnimTabs.getTabCount(); i++) {
						final BoneAttachmentPanel geoPanel = (BoneAttachmentPanel) geosetAnimTabs.getComponentAt(i);
						if (geoPanel.getSelectedGeoset() == geosetShell) {
							geosetAnimTabs.setEnabledAt(i, geosetShell.isDoImport());
						}
					}
				}
			}
		});
	}

	private JPanel getTopPanel() {

		JPanel topPanel = new JPanel(new MigLayout("gap 0", "[][]", "[align center][align center]"));
		topPanel.setOpaque(true);

		topPanel.add(getMatchBonesPanel(mht.receivingModel.getName(), mht.donatingModel.getName(), false), "");
		topPanel.add(getMatchBonesPanel(mht.donatingModel.getName(), mht.receivingModel.getName(), true), "wrap");

		displayParents = new JCheckBox("Display parent names");
		displayParents.addActionListener(e -> showParents(renderer, displayParents));
		topPanel.add(displayParents, "spanx, align center, wrap");

		return topPanel;
	}

	private JPanel getMatchBonesPanel(String modelName, String otherModelName, boolean donModel){
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0", "[][][]", "[align center]"));
		panel.setOpaque(true);
		panel.setBorder(BorderFactory.createTitledBorder(modelName));

		JButton allMatrOriginal = new JButton("Reset all Matrices");
		allMatrOriginal.setToolTipText("Resets all matrices to original bones, regardless of their import status.");
		allMatrOriginal.addActionListener(e -> matrOriginal(donModel));
		panel.add(allMatrOriginal);

		JButton prioOther = new JButton("Auto match, prioritizing other");

		int width = (int) ScreenInfo.getSuitableSize(1500, 800, .4).getWidth();
		String toolTip1 = "Matches matrices' <code>[original bones]</code> to <code>[bones to be imported]</code> by name (ignoring CaSe).";
		String toolTip2 = "<br><i>(if no match is found, an other try is made ignoring \"<code>bone</code>\", \"<code>helper</code>\", \"<code>_</code>\" and \"<code> </code>\".)</i>";
		String toolTipOther = "<br>Prioritizing bones from \"" + otherModelName + "\"";
		prioOther.setToolTipText("<html><p max-width=\"" + width + "\">" + toolTip1 + toolTipOther + toolTip2 + "</p></html>");
		ToolTipManager.sharedInstance().setDismissDelay(1000*60);
		prioOther.addActionListener(e -> allMatrSameNameStrictFirst(donModel, !donModel));
		panel.add(prioOther);

		JButton prioSelf = new JButton("Auto match, prioritizing self");
		String toolTip2Alt = "<br><i>(if no match is found, an other try is made ignoring \"<code>bone</code>\", \"<code>helper</code>\", \"<code>_</code>\" and \"<code> </code>\".)</i>";
		String toolTipSelf = "<br>Prioritizing bones from \"" + modelName + "\"";
		prioSelf.setToolTipText("<html><p max-width=\"" + width + "\">" + toolTip1 + toolTipSelf + toolTip2Alt + "</p></html>");
		prioSelf.addActionListener(e -> allMatrSameNameStrictFirst(donModel, donModel));
		panel.add(prioSelf);

		return panel;
	}

	private void showParents(BoneShellListCellRenderer renderer, JCheckBox checkBox) {
		renderer.setShowParent(checkBox.isSelected());
		repaint();
	}

	public void allMatrOriginal() {
		for (GeosetShell geosetShell : mht.allGeoShells) {
			if (geosetShell.isDoImport()) {
				for (MatrixShell ms : geosetShell.getMatrixShells()) {
					ms.resetMatrix();
				}
			}
		}
	}
	public void matrOriginal(boolean donModel) {
		List<GeosetShell> geoShells = donModel ? mht.donModGeoShells : mht.recModGeoShells;
		for (GeosetShell geosetShell : geoShells) {
			if (geosetShell.isDoImport()) {
				for (MatrixShell ms : geosetShell.getMatrixShells()) {
					ms.resetMatrix();
				}
			}
		}
	}


	public void allMatrSameNameStrictFirst(boolean donModel, boolean prioDonBones) {
		List<IdObjectShell<?>> prioBoneList = prioDonBones ? mht.donModBoneShells : mht.recModBoneShells;
		List<IdObjectShell<?>> secBoneList = prioDonBones ? mht.recModBoneShells : mht.donModBoneShells;
		List<GeosetShell> geoShells = donModel ? mht.donModGeoShells : mht.recModGeoShells;

		Map<String, IdObjectShell<?>> nameMap = new HashMap<>();
		for (IdObjectShell<?> boneShell : secBoneList) {
			if (boneShell.getShouldImport()) {
				String name = boneShell.getName();
				nameMap.put(name, boneShell);
				nameMap.put(getFuzzyName(name), boneShell);
			}
		}
		// this will overwrite names that exist in both bone lists
		for (IdObjectShell<?> boneShell : prioBoneList) {
			if (boneShell.getShouldImport()) {
				String name = boneShell.getName();
				nameMap.put(name, boneShell);
				nameMap.put(getFuzzyName(name), boneShell);
			}
		}

		int totFailedMatches = 0;
		int totFailedInGeos = 0;
		int totFailedInMatrices = 0;

		for (GeosetShell geosetShell : geoShells) {
			if (geosetShell.isDoImport()) {
				int failedInMatrices = 0;
				for (MatrixShell matrixShell : geosetShell.getMatrixShells()) {
					matrixShell.clearNewBones();
					final Matrix matrix = matrixShell.getMatrix();
					// For look to find similarly named stuff and add it
					int failedMatches = 0;
					for (final Bone bone : matrix.getBones()) {
						final String mName = bone.getName();
						if (nameMap.get(mName) != null) {
							matrixShell.addNewBone(nameMap.get(mName));
						} else {
							String fuzzyName = getFuzzyName(mName);
							if (nameMap.get(fuzzyName) != null) {
								matrixShell.addNewBone(nameMap.get(fuzzyName));
							} else {
								failedMatches++;
							}
						}
					}
					totFailedMatches += failedMatches;
					failedInMatrices += failedMatches == 0 ? 0 : 1;
				}
				totFailedInMatrices += failedInMatrices;
				totFailedInGeos += failedInMatrices == 0 ? 0 : 1;
			}
		}
	}


	String placeholder = "UGG";
	private String getFuzzyName(String boneShellName) {
		return boneShellName.toLowerCase()
				.replaceAll("bone", placeholder)
				.replaceAll("helper", placeholder)
				.replaceAll("_", "")
				.replaceAll(" ", "")
				.replaceAll(placeholder, "") + "FUZZY";
	}
}
