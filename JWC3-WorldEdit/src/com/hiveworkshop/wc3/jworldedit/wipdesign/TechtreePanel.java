package com.hiveworkshop.wc3.jworldedit.wipdesign;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class TechtreePanel extends JPanel {
	private static final Color BACKGROUND_COLOR = new Color(27,26,24);
	private static final Color GRIDLINE_COLOR = new Color(51, 50, 48);
	private static final int CELLSIZE = 64;
	private final int yCells;
	private final int xCells;

	public TechtreePanel(final int xCells, final int yCells) {
		this.xCells = xCells;
		this.yCells = yCells;
		setBackground(BACKGROUND_COLOR);
		setPreferredSize(new Dimension(xCells*CELLSIZE, yCells*CELLSIZE));
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		g.setColor(GRIDLINE_COLOR);
		for(int i = 0; i <= yCells; i++) {
			g.drawLine(0, i*CELLSIZE, xCells*CELLSIZE, i*CELLSIZE);
		}
		for(int j = 0; j <= xCells; j++) {
			g.drawLine(j*CELLSIZE, 0, j*CELLSIZE, yCells*CELLSIZE);
		}
	}
}
