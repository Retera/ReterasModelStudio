package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class GlobalSeqOverviewPanel extends OverviewPanel {
	private final JPanel infoPanel;

	public GlobalSeqOverviewPanel(ModelHandler modelHandler) {
		super(modelHandler, new MigLayout("fill, ins 0", "[grow]", "[grow]"));
		infoPanel = new JPanel(new MigLayout("wrap 2", "[]10[Right]", ""));

		fillInfoPanel();

		JScrollPane scrollPane = new JScrollPane(infoPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, "growx, growy");
	}

	private void fillInfoPanel() {
		infoPanel.add(new JLabel("Number"));
		infoPanel.add(new JLabel("Duration"));

		for (GlobalSeq globalSeq : modelHandler.getModel().getGlobalSeqs()) {

			infoPanel.add(new JLabel("GlobalSequence " + modelHandler.getModel().getGlobalSeqId(globalSeq)));
			infoPanel.add(new JLabel("" + globalSeq.getLength()));
		}
	}

	@Override
	public void update() {
		infoPanel.removeAll();
		fillInfoPanel();
		revalidate();
		repaint();
	}
}
