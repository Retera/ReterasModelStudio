package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ObjectEditPanel extends JPanel {

	public CardLayout objectCardLayout = new CardLayout();
	public JPanel objectPanelCards = new JPanel(objectCardLayout);
	public MultiObjectPanel multiObjectPane;
	public JPanel blankPane = new JPanel();
	ModelHolderThing mht;

	ObjectPanel singleObjectPanel;
	BoneShellListCellRenderer bonePanelRenderer;

	public ObjectEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0", "[grow][grow]", "[][grow]"));
		this.mht = mht;

		JButton importAllObjs = new JButton("Import All");
		importAllObjs.addActionListener(e -> mht.importAllObjs(true));
		add(importAllObjs, "cell 0 0, right");

		JButton uncheckAllObjs = new JButton("Leave All");
		uncheckAllObjs.addActionListener(e -> mht.importAllObjs(false));
		add(uncheckAllObjs, "cell 1 0, left");


		mht.getFutureBoneListExtended(false);

		bonePanelRenderer = new BoneShellListCellRenderer(mht.recModelManager, mht.donModelManager);

		// Build the objectTabs list of ObjectPanels
		final ObjPanelListCellRenderer objectPanelRenderer = new ObjPanelListCellRenderer();
		int panelid = 0;
		for (IdObject obj : mht.donatingModel.getIdObjects()) {
			if ((obj.getClass() != Bone.class) && (obj.getClass() != Helper.class)) {
				mht.donModObjectShells.addElement(new ObjectShell(obj));
			}
		}
		for (Camera obj : mht.donatingModel.getCameras()) {
			mht.donModObjectShells.addElement(new ObjectShell(obj));
		}

		for (ObjectShell os : mht.donModObjectShells) {
			os.setParentBs(mht.donModBoneShellBiMap);
		}

		singleObjectPanel = new ObjectPanel(mht, bonePanelRenderer);
		objectPanelCards.add(singleObjectPanel, "single");

		objectPanelCards.add(blankPane, "blank");

		multiObjectPane = new MultiObjectPanel(mht, mht.getFutureBoneListExtended(true));
		objectPanelCards.add(multiObjectPane, "multiple");

		mht.donModObjectJList.setCellRenderer(objectPanelRenderer);
		mht.donModObjectJList.addListSelectionListener(e -> objectTabsValueChanged(mht));
		mht.donModObjectJList.setSelectedIndex(0);
		objectPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));


		JScrollPane objectTabsPane = new JScrollPane(mht.donModObjectJList);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, objectTabsPane, objectPanelCards);
		add(splitPane, "cell 0 1, growx, growy, spanx 2");
	}

	static void uncheckUnusedObjects(ModelHolderThing mht, List<BoneShell> usedBonePanels) {
		for (ObjectShell objectPanel : mht.donModObjectShells) {
			if (objectPanel.getShouldImport()) {
				BoneShell shell = objectPanel.getNewParentBs();
				if ((shell != null) && (shell.getBone() != null)) {
					BoneShell current = shell;
					if (!usedBonePanels.contains(current)) {
						usedBonePanels.add(current);
					}

					boolean good = true;
					int k = 0;
					while (good) {
						if (current.getImportStatus() == 1) {
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

	private void objectTabsValueChanged(ModelHolderThing mht) {
		List<ObjectShell> selectedValuesList = mht.donModObjectJList.getSelectedValuesList();
		if (selectedValuesList.size() < 1) {
			bonePanelRenderer.setSelectedObjectShell(null);
			objectCardLayout.show(objectPanelCards, "blank");
		} else if (selectedValuesList.size() == 1) {
			mht.getFutureBoneListExtended(false);
			bonePanelRenderer.setSelectedObjectShell(mht.donModObjectJList.getSelectedValue());
			objectCardLayout.show(objectPanelCards, "single");
			singleObjectPanel.setSelectedObject(mht.donModObjectJList.getSelectedValue());
		} else {
			bonePanelRenderer.setSelectedObjectShell(null);
			objectCardLayout.show(objectPanelCards, "multiple");

			boolean dif = false;
			boolean selectedt = selectedValuesList.get(0).getShouldImport();

			for (ObjectShell op : selectedValuesList) {
				if (selectedt != op.getShouldImport()) {
					dif = true;
					break;
				}
			}
			if (!dif) {
				multiObjectPane.doImport.setSelected(selectedt);
			}
		}
	}
}
