package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class GlobalSeqOverviewPanel extends JPanel {

	public GlobalSeqOverviewPanel(ModelHandler modelHandler) {
		super(new MigLayout("fill, ins 0", "[grow]", "[grow]"));
		JPanel panel = new JPanel(new MigLayout("wrap 2", "[]10[Right]", ""));

		panel.add(new JLabel("Number"));
		panel.add(new JLabel("Duration"));

		for (int globalSeqId = 0; globalSeqId < modelHandler.getModel().getGlobalSeqs().size(); globalSeqId++) {

			panel.add(new JLabel("GlobalSequence " + globalSeqId));
			panel.add(new JLabel("" + modelHandler.getModel().getGlobalSeq(globalSeqId)));
		}

		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, "growx, growy");
	}
}
