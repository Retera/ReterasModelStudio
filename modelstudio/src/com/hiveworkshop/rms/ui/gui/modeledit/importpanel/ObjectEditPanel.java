package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.ObjectShellListCellRenderer;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class ObjectEditPanel extends JPanel {

	private final CardLayout cardLayout = new CardLayout();
	private final JPanel panelCards = new JPanel(cardLayout);
	private final MultiObjectPanel multiObjectPane;
	private final ModelHolderThing mht;
	private final JList<IdObjectShell<?>> allObjectJList;
	private final ObjectPanel singleObjectPanel;
	private final BoneShellListCellRenderer bonePanelRenderer;

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

		panel.add(Button.create("Import All", e -> importTypeConsumer.accept(true)), "");
		panel.add(Button.create("Leave All", e -> importTypeConsumer.accept(false)), "");

		return panel;
	}

	private JScrollPane getObjectListPane(ModelHolderThing mht) {
		ObjectShellListCellRenderer objectPanelRenderer = new ObjectShellListCellRenderer(mht.receivingModel, mht.donatingModel);
		allObjectJList.setCellRenderer(objectPanelRenderer);
		allObjectJList.addListSelectionListener(e -> objectTabsValueChanged(mht, e));
		allObjectJList.setSelectedValue(null, false);
		return new JScrollPane(allObjectJList);
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
