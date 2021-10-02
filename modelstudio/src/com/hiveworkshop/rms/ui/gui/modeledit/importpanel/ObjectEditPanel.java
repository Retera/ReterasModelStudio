package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.ObjectShellListCellRenderer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class ObjectEditPanel extends JPanel {

	public CardLayout cardLayout = new CardLayout();
	public JPanel panelCards = new JPanel(cardLayout);
	public MultiObjectPanel multiObjectPane;
	ModelHolderThing mht;

	ObjectPanel singleObjectPanel;
	BoneShellListCellRenderer bonePanelRenderer;

	public ObjectEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0", "[grow][grow]", "[][grow]"));
		this.mht = mht;

		add(getButton("Import All", e -> mht.setImportAllDonObjs(true)), "cell 0 0, right");
		add(getButton("Leave All", e -> mht.setImportAllDonObjs(false)), "cell 1 0, left");

		mht.getFutureBoneHelperList();

		bonePanelRenderer = new BoneShellListCellRenderer(mht.receivingModel, mht.donatingModel);
		singleObjectPanel = new ObjectPanel(mht, bonePanelRenderer);
		multiObjectPane = new MultiObjectPanel(mht, bonePanelRenderer);

		panelCards.add(new JPanel(), "blank");
		panelCards.add(singleObjectPanel, "single");
		panelCards.add(multiObjectPane, "multiple");
		panelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getObjectListPane(mht), panelCards);
		add(splitPane, "cell 0 1, growx, growy, spanx 2");
	}

	private JScrollPane getObjectListPane(ModelHolderThing mht) {
		ObjectShellListCellRenderer objectPanelRenderer = new ObjectShellListCellRenderer(mht.receivingModel, mht.donatingModel);
		mht.allObjectJList.setCellRenderer(objectPanelRenderer);
		mht.allObjectJList.addListSelectionListener(e -> objectTabsValueChanged(mht, e));
		mht.allObjectJList.setSelectedValue(null, false);
		return new JScrollPane(mht.allObjectJList);
//		mht.donModObjectJList.setCellRenderer(objectPanelRenderer);
//		mht.donModObjectJList.addListSelectionListener(e -> objectTabsValueChanged(mht, e));
//		mht.donModObjectJList.setSelectedValue(null, false);
//		return new JScrollPane(mht.donModObjectJList);
	}

	private JButton getButton(String text, ActionListener actionListener) {
		JButton button = new JButton(text);
		button.addActionListener(actionListener);
		return button;
	}

	private void objectTabsValueChanged(ModelHolderThing mht, ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			List<ObjectShell> selectedValuesList = mht.allObjectJList.getSelectedValuesList();
			if (selectedValuesList.size() < 1) {
				bonePanelRenderer.setSelectedObjectShell(null);
				cardLayout.show(panelCards, "blank");
			} else if (selectedValuesList.size() == 1) {
//				mht.getFutureBoneHelperList();
				singleObjectPanel.setSelectedObject(mht.allObjectJList.getSelectedValue());
				cardLayout.show(panelCards, "single");
			} else {
				multiObjectPane.setSelectedObjects(selectedValuesList);
				cardLayout.show(panelCards, "multiple");
			}
		}
	}
}
