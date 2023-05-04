package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.ObjectShellListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;

public class ObjectEditPanel extends JPanel {

	private final CardLayout cardLayout = new CardLayout();
	private final JPanel panelCards = new JPanel(cardLayout);
	private final MultiObjectPanel multiObjectPane;
	private final ModelHolderThing mht;
	private final TwiList<IdObjectShell<?>> allObjectJList;
	private final ObjectPanel singleObjectPanel;
	private final BoneShellListCellRenderer bonePanelRenderer;

	public ObjectEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0", "[grow][grow]", "[][grow]"));
		this.mht = mht;
		allObjectJList = new TwiList<>(mht.allObjectShells);

		add(getSetImpTypePanel(mht.receivingModel.getName(), false), "cell 0 0, right");
		add(getSetImpTypePanel(mht.donatingModel.getName(), true), "cell 1 0, left");

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

	private JPanel getSetImpTypePanel(String modelName, boolean donMod) {
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0", "[][][]", "[align center]"));
		panel.setOpaque(true);
		panel.setBorder(BorderFactory.createTitledBorder(modelName));

		panel.add(Button.create("Import All", e -> setImportObjs(true, donMod)), "");
		panel.add(Button.create("Leave All", e -> setImportObjs(false, donMod)), "");

		return panel;
	}

	public void setImportObjs(boolean doImport, boolean donMod) {
		List<IdObjectShell<?>> objectShells = donMod ? mht.donModObjectShells : mht.recModObjectShells;
		objectShells.forEach(shell -> shell.setShouldImport(doImport));
	}

	private JScrollPane getObjectListPane(ModelHolderThing mht) {
		ObjectShellListCellRenderer objectPanelRenderer = new ObjectShellListCellRenderer(mht.receivingModel, mht.donatingModel);
		allObjectJList.setCellRenderer(objectPanelRenderer);
		allObjectJList.addMultiSelectionListener(this::objectTabsValueChanged);
		allObjectJList.setSelectedValue(null, false);
		return new JScrollPane(allObjectJList);
	}
	private void objectTabsValueChanged(Collection<IdObjectShell<?>> selectedValuesList) {
		if (selectedValuesList.size() < 1) {
			bonePanelRenderer.setSelectedObjectShell(null);
			showCard("blank");
		} else if (selectedValuesList.size() == 1) {
			singleObjectPanel.setSelectedObject(((List<IdObjectShell<?>>) selectedValuesList).get(0));
			showCard("single");
		} else {
			multiObjectPane.setSelectedObjects((List<IdObjectShell<?>>) selectedValuesList);
			showCard("multiple");
		}
	}

	private void showCard(String name){
		cardLayout.show(panelCards, name);
	}
}
