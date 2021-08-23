package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class GlobalSeqOverviewPanel extends JPanel {

	public GlobalSeqOverviewPanel(ModelHandler modelHandler) {
		super(new MigLayout("fill, ins 0", "[grow]", "[grow]"));
		JPanel panel = new JPanel(new MigLayout("wrap 2", "[]10[Right]", ""));

		panel.add(new JLabel("Number"));
		panel.add(new JLabel("Duration"));

		for (GlobalSeq globalSeq : modelHandler.getModel().getGlobalSeqs()) {

			panel.add(new JLabel("GlobalSequence " + modelHandler.getModel().getGlobalSeqId(globalSeq)));
			panel.add(new JLabel("" + globalSeq.getLength()));
		}

		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, "growx, growy");
	}
}
