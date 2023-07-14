package com.hiveworkshop.rms.ui.application.edit.animation.altTimeline;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class TempGraphBGPanel extends JPanel {
	public TempGraphBGPanel() {
		setLayout(new MigLayout("fill"));
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		final int width = getWidth();

		final FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
		g.setColor(Color.BLACK);
	}
}