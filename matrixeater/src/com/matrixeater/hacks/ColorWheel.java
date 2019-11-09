package com.matrixeater.hacks;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class ColorWheel extends JPanel {
	private double angle;

	public ColorWheel() {
		add(Box.createRigidArea(new Dimension(800, 600)));

		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(final MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				angle = (e.getX() / (double) getWidth()) * 360;
				repaint();
			}
		});
		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(final MouseEvent e) {

			}

			@Override
			public void mousePressed(final MouseEvent e) {
				angle = (e.getX() / (double) getWidth()) * 360;
				repaint();
			}

			@Override
			public void mouseExited(final MouseEvent e) {

			}

			@Override
			public void mouseEntered(final MouseEvent e) {

			}

			@Override
			public void mouseClicked(final MouseEvent e) {

			}
		});
	}

	private Color colorByAngle(final double angle) {
		final int red = Math.min(255, Math.max(0, (int) (Math.abs(((180 - angle) * 510) / 120.)) - 255));
		final int green = Math.min(255, Math.max(0, (int) (510 - Math.abs(((angle - 120) * 510) / 120.))));
		final int blue = Math.min(255, Math.max(0, (int) (510 - Math.abs(((angle - 240) * 510) / 120.))));
		return new Color(red, green, blue);
	}

	private Color colorByHSV(final double hue, final double saturation, final double value) {
		int red = Math.min(255, Math.max(0, (int) (Math.abs(((180 - hue) * 510) / 120.)) - 255));
		int green = Math.min(255, Math.max(0, (int) (510 - Math.abs(((hue - 120) * 510) / 120.))));
		int blue = Math.min(255, Math.max(0, (int) (510 - Math.abs(((hue - 240) * 510) / 120.))));
		final double saturationRatio = 1.0 - (saturation / 100.);
		red = (int) (((red + ((255 - red) * saturationRatio)) * value) / 100.);
		green = (int) (((green + ((255 - green) * saturationRatio)) * value) / 100.);
		blue = (int) (((blue + ((255 - blue) * saturationRatio)) * value) / 100.);
		return new Color(red, green, blue);
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);

		final int cx = getWidth() / 2;
		final int cy = getHeight() / 2;
		final double factor = 10;
		for (int i = 0; i < (360 * factor); i++) {
			g.setColor(colorByAngle(i / factor));
			g.drawLine(cx, cy, cx + (int) (Math.cos(Math.toRadians(i / factor)) * 200),
					cy + (int) (Math.sin(Math.toRadians(i / factor)) * 200));
			g.drawLine((int) ((i * getWidth()) / (360 * factor)), getHeight() - 100,
					(int) ((i * getWidth()) / (360 * factor)), getHeight());
		}
		g.setColor(Color.BLACK);
		g.drawLine((int) ((angle * getWidth()) / (360)), getHeight() - 100, (int) ((angle * getWidth()) / (360)),
				getHeight());

		final double factorTwo = 3;
		final int n = (int) (100 * factorTwo);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				g.setColor(colorByHSV(angle, i / factorTwo, 100 - (j / factorTwo)));
				g.fillRect(i, j, 1, 1);
			}
		}
	}

	public static void main(final String[] args) {
		final JFrame frame = new JFrame("Test");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setContentPane(new ColorWheel());
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

}
