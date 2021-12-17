package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.ObjectShellListCellRenderer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;

public class ObjectEditPanel extends JPanel {

	private CardLayout cardLayout = new CardLayout();
	private JPanel panelCards = new JPanel(cardLayout);
	private MultiObjectPanel multiObjectPane;
	private ModelHolderThing mht;
	private JList<IdObjectShell<?>> allObjectJList;

	private ObjectPanel singleObjectPanel;
	private BoneShellListCellRenderer bonePanelRenderer;

	public ObjectEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0", "[grow][grow]", "[][grow]"));
		this.mht = mht;
		allObjectJList = new JList<>(mht.allObjectShells);

		add(getSetImpTypePanel(mht.receivingModel.getName(), mht::setImportAllRecObjs), "cell 0 0, right");
		add(getSetImpTypePanel(mht.donatingModel.getName(), mht::setImportAllDonObjs), "cell 1 0, left");

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

	private JPanel getSetImpTypePanel(String modelName, Consumer<Boolean> importTypeConsumer) {
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0", "[][][]", "[align center]"));
		panel.setOpaque(true);
		panel.setBorder(BorderFactory.createTitledBorder(modelName));

		panel.add(getButton("Import All", e -> importTypeConsumer.accept(true)), "");
		panel.add(getButton("Leave All", e -> importTypeConsumer.accept(false)), "");

		return panel;
	}

	private JScrollPane getObjectListPane(ModelHolderThing mht) {
		ObjectShellListCellRenderer objectPanelRenderer = new ObjectShellListCellRenderer(mht.receivingModel, mht.donatingModel);
		allObjectJList.setCellRenderer(objectPanelRenderer);
		allObjectJList.addListSelectionListener(e -> objectTabsValueChanged(mht, e));
		allObjectJList.setSelectedValue(null, false);
		return new JScrollPane(allObjectJList);
	}

	private JButton getButton(String text, ActionListener actionListener) {
		JButton button = new JButton(text);
		button.addActionListener(actionListener);
		return button;
	}

	private void objectTabsValueChanged(ModelHolderThing mht, ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			List<IdObjectShell<?>> selectedValuesList = allObjectJList.getSelectedValuesList();
			if (selectedValuesList.size() < 1) {
				bonePanelRenderer.setSelectedObjectShell(null);
				cardLayout.show(panelCards, "blank");
			} else if (selectedValuesList.size() == 1) {
//				mht.getFutureBoneHelperList();
				singleObjectPanel.setSelectedObject(allObjectJList.getSelectedValue());
				cardLayout.show(panelCards, "single");
			} else {
				multiObjectPane.setSelectedObjects(selectedValuesList);
				cardLayout.show(panelCards, "multiple");
			}
		}
	}
}
