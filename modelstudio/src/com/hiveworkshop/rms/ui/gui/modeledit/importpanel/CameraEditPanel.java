package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.renderers.CameraShellListCellRenderer;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class CameraEditPanel extends JPanel {

	private CardLayout cardLayout = new CardLayout();
	private JPanel panelCards = new JPanel(cardLayout);
	private MultiCameraPanel multiCameraPanel;
	private ModelHolderThing mht;
	private JList<CameraShell> donModCameraJList;
	private JList<CameraShell> allCameraJList;
	private CameraPanel singleCameraPanel;

	public CameraEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0", "[grow][grow]", "[][grow]"));
		this.mht = mht;
		donModCameraJList = new JList<>(mht.donModCameraShells);
		allCameraJList = new JList<>(mht.allCameraShells);

//		add(getButton("Import All", e -> mht.setImportAllDonCams(true)), "cell 0 0, right");
//		add(getButton("Leave All", e -> mht.setImportAllDonCams(false)), "cell 1 0, left");
		add(getSetImpTypePanel(mht.receivingModel.getName(), mht::setImportAllRecCams), "cell 0 0, right");
		add(getSetImpTypePanel(mht.donatingModel.getName(), mht::setImportAllDonCams), "cell 1 0, left");


		singleCameraPanel = new CameraPanel(mht);
		multiCameraPanel = new MultiCameraPanel(mht);

		panelCards.add(new JPanel(), "blank");
		panelCards.add(singleCameraPanel, "single");
		panelCards.add(multiCameraPanel, "multiple");
		panelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));
		JScrollPane cardScrollPane = new JScrollPane(panelCards);

		JScrollPane cameraListPane = getCameraListPane(mht);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cameraListPane, cardScrollPane);
		add(splitPane, "cell 0 1, growx, growy, spanx 2");
	}

	private JScrollPane getCameraListPane(ModelHolderThing mht) {
		CameraShellListCellRenderer cameraPanelRenderer = new CameraShellListCellRenderer();
		allCameraJList.setCellRenderer(cameraPanelRenderer);
		allCameraJList.addListSelectionListener(e -> objectTabsValueChanged(mht, e));
		return new JScrollPane(allCameraJList);
	}

	private JPanel getSetImpTypePanel(String modelName, Consumer<Boolean> importTypeConsumer) {
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0", "[][][]", "[align center]"));
		panel.setOpaque(true);
		panel.setBorder(BorderFactory.createTitledBorder(modelName));

		panel.add(Button.create("Import All", e -> importTypeConsumer.accept(true)), "");
		panel.add(Button.create("Leave All", e -> importTypeConsumer.accept(false)), "");

		return panel;
	}

	private void objectTabsValueChanged(ModelHolderThing mht, ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			List<CameraShell> selectedValuesList = donModCameraJList.getSelectedValuesList();
			if (selectedValuesList.size() < 1) {
				cardLayout.show(panelCards, "blank");
			} else if (selectedValuesList.size() == 1) {
				cardLayout.show(panelCards, "single");
				singleCameraPanel.setSelectedObject(donModCameraJList.getSelectedValue());
			} else {
				multiCameraPanel.updateMultiCameraPanel(selectedValuesList);
				cardLayout.show(panelCards, "multiple");
			}
		}
	}
}
