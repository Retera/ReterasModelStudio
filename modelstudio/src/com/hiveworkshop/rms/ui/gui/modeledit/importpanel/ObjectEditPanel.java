package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.List;

public class ObjectEditPanel extends JPanel {

	public CardLayout objectCardLayout = new CardLayout();
	public JPanel objectPanelCards = new JPanel(objectCardLayout);
	public MultiObjectPanel multiObjectPane;
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
		final ObjPanelListCellRenderer objectPanelRenderer = new ObjPanelListCellRenderer();

		objectPanelCards.add(new JPanel(), "blank");

		singleObjectPanel = new ObjectPanel(mht, bonePanelRenderer);
		objectPanelCards.add(singleObjectPanel, "single");

		multiObjectPane = new MultiObjectPanel(mht, mht.getFutureBoneListExtended(true));
		objectPanelCards.add(multiObjectPane, "multiple");


		objectPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		mht.donModObjectJList.setCellRenderer(objectPanelRenderer);
		mht.donModObjectJList.addListSelectionListener(e -> objectTabsValueChanged(mht, e));
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
						if (current.getImportStatus() == BoneShell.ImportType.MOTIONFROM) {
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

	private void objectTabsValueChanged(ModelHolderThing mht, ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
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
				multiObjectPane.updateMultiObjectPanel();
				objectCardLayout.show(objectPanelCards, "multiple");
			}
		}
	}
}
