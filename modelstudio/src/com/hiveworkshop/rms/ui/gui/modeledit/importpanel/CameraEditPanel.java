package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.CameraShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.CameraShellListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;

public class CameraEditPanel extends JPanel {

	private final CardLayout cardLayout = new CardLayout();
	private final JPanel panelCards = new JPanel(cardLayout);
	private final CameraMultiPanel cameraMultiPanel;
	private final ModelHolderThing mht;
	private final TwiList<CameraShell> allCameraJList;
	private final CameraPanel singleCameraPanel;

	public CameraEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0", "[grow][grow]", "[][grow]"));
		this.mht = mht;
		allCameraJList = new TwiList<>(mht.allCameraShells);

		add(getSetImpTypePanel(mht.receivingModel.getName(), false), "cell 0 0, right");
		add(getSetImpTypePanel(mht.donatingModel.getName(), true), "cell 1 0, left");


		singleCameraPanel = new CameraPanel(mht);
		cameraMultiPanel = new CameraMultiPanel(mht);

		panelCards.add(new JPanel(), "blank");
		panelCards.add(singleCameraPanel, "single");
		panelCards.add(cameraMultiPanel, "multiple");
		panelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));
		JScrollPane cardScrollPane = new JScrollPane(panelCards);

		JScrollPane cameraListPane = getCameraListPane(mht);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cameraListPane, cardScrollPane);
		add(splitPane, "cell 0 1, growx, growy, spanx 2");
	}

	private JScrollPane getCameraListPane(ModelHolderThing mht) {
		CameraShellListCellRenderer cameraPanelRenderer = new CameraShellListCellRenderer();
		allCameraJList.setCellRenderer(cameraPanelRenderer);
		allCameraJList.addMultiSelectionListener(this::objectTabsValueChanged);
		return new JScrollPane(allCameraJList);
	}

	private JPanel getSetImpTypePanel(String modelName, boolean donMod) {
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0", "[][][]", "[align center]"));
		panel.setOpaque(true);
		panel.setBorder(BorderFactory.createTitledBorder(modelName));

		panel.add(Button.create("Import All", e -> setImportCams(true, donMod)), "");
		panel.add(Button.create("Leave All", e -> setImportCams(false, donMod)), "");

		return panel;
	}

	private void objectTabsValueChanged(Collection<CameraShell> selectedValuesList) {
		if (selectedValuesList.size() < 1) {
			showCard("blank");
		} else if (selectedValuesList.size() == 1) {
			singleCameraPanel.setSelectedObject(((List<CameraShell>) selectedValuesList).get(0));
			showCard("single");
		} else {
			cameraMultiPanel.updateMultiCameraPanel((List<CameraShell>) selectedValuesList);
			showCard("multiple");
		}
	}
	private void showCard(String name){
		cardLayout.show(panelCards, name);
	}


	public void setImportCams(boolean imp, boolean donMod) {
		List<CameraShell> cameraShells = donMod ? mht.donModCameraShells : mht.recModCameraShells;
		cameraShells.forEach(shell -> shell.setShouldImport(imp));
	}
}
