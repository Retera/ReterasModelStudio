package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
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

		// Build the objectTabs list of ObjectPanels
		final ObjPanelListCellRenderer objectPanelRenderer = new ObjPanelListCellRenderer();
		int panelid = 0;
		for (int i = 0; i < mht.donatingModel.getIdObjects().size(); i++) {
			final IdObject obj = mht.donatingModel.getIdObjects().get(i);
			if ((obj.getClass() != Bone.class) && (obj.getClass() != Helper.class)) {

				final ObjectPanel objPanel = new ObjectPanel(mht, obj, mht.getFutureBoneListExtended(true));

				objectPanelCards.add(objPanel, panelid + "");
				mht.objectPanels.addElement(objPanel);
				panelid++;
			}
		}
		for (int i = 0; i < mht.donatingModel.getCameras().size(); i++) {
			final Camera obj = mht.donatingModel.getCameras().get(i);

			final ObjectPanel objPanel = new ObjectPanel(obj);

			objectPanelCards.add(objPanel, panelid + "");
			mht.objectPanels.addElement(objPanel);
			panelid++;
		}
		multiObjectPane = new MultiObjectPanel(mht, mht.getFutureBoneListExtended(true));
		objectPanelCards.add(blankPane, "blank");
		objectPanelCards.add(multiObjectPane, "multiple");
		mht.objectTabs.setCellRenderer(objectPanelRenderer);
		mht.objectTabs.addListSelectionListener(e -> objectTabsValueChanged(mht));
		mht.objectTabs.setSelectedIndex(0);
		objectPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));


		JScrollPane objectTabsPane = new JScrollPane(mht.objectTabs);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, objectTabsPane, objectPanelCards);
		add(splitPane, "cell 0 1, growx, growy, spanx 2");
	}

	private void objectTabsValueChanged(ModelHolderThing mht) {
		List<ObjectPanel> selectedValuesList = mht.objectTabs.getSelectedValuesList();
		if (selectedValuesList.size() < 1) {
			objectCardLayout.show(objectPanelCards, "blank");
		} else if (selectedValuesList.size() == 1) {
			mht.getFutureBoneListExtended(false);
			objectCardLayout.show(objectPanelCards, (mht.objectTabs.getSelectedIndex()) + "");
		} else {
			objectCardLayout.show(objectPanelCards, "multiple");

			boolean dif = false;
			boolean selectedt = selectedValuesList.get(0).doImport.isSelected();

			for (ObjectPanel op : selectedValuesList) {
				if (selectedt != op.doImport.isSelected()) {
					dif = true;
					break;
				}
			}
			if (!dif) {
				multiObjectPane.doImport.setSelected(selectedt);
			}
		}
	}

	static void uncheckUnusedObjects(ModelHolderThing mht, List<BonePanel> usedBonePanels) {
		for (ObjectPanel objectPanel : mht.objectPanels) {
			if (objectPanel.doImport.isSelected() && (objectPanel.parentsList != null)) {
				BoneShell shell = objectPanel.parentsList.getSelectedValue();
				if ((shell != null) && (shell.bone != null)) {
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
