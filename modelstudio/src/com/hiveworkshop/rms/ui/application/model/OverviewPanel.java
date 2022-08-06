package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.awt.*;

public abstract class OverviewPanel extends JPanel {
	protected final ModelHandler modelHandler;

	public OverviewPanel(ModelHandler modelHandler, LayoutManager layoutManager) {
		super(layoutManager);
		this.modelHandler = modelHandler;
//		JPanel panel = new JPanel(new MigLayout("wrap 2", "[]10[Right]", ""));
//
//		panel.add(new JLabel("Number"));
//		panel.add(new JLabel("Duration"));
//
//		for (GlobalSeq globalSeq : modelHandler.getModel().getGlobalSeqs()) {
//
//			panel.add(new JLabel("GlobalSequence " + modelHandler.getModel().getGlobalSeqId(globalSeq)));
//			panel.add(new JLabel("" + globalSeq.getLength()));
//		}
//
//		JScrollPane scrollPane = new JScrollPane(panel);
//		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
//		add(scrollPane, "growx, growy");
	}

	public abstract void update();
}
