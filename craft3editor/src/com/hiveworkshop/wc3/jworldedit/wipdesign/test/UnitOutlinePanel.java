package com.hiveworkshop.wc3.jworldedit.wipdesign.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.hiveworkshop.wc3.gui.modeledit.MDLSnapshot;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.units.GameObject;

public class UnitOutlinePanel extends JPanel {
	private static final Color GREEN = new Color(100, 255, 0);
	// private final Area outline;
	private BufferedImage bufferedImage;
	private MDLSnapshot snapshot;
	private float xAngle = 0;

	public UnitOutlinePanel(final GameObject unit) {
		// setBackground(Color.black);
		try {
			final ModelViewManager mdlDisplay = MDLSnapshot.createDefaultDisplay(unit);
			snapshot = new MDLSnapshot(mdlDisplay, 1024, 1024, null);
			snapshot.zoomToFit();
			bufferedImage = snapshot.getBufferedImage();
			for (final Geoset geo : mdlDisplay.getModel().getGeosets()) {
				if (geo.getMaterial().firstLayer().getTextureBitmap().getPath().equals("")) {
					mdlDisplay.makeGeosetNotEditable(geo);
					mdlDisplay.makeGeosetNotVisible(geo);
				}
			}
			// outline = snapshot.getOutline();
			setPreferredSize(new Dimension(1024, 1024));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

		final Timer timer = new Timer(200, new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					snapshot.setXangle(xAngle += 45);
					bufferedImage = snapshot.getBufferedImage();
				} catch (final Exception e1) {
					e1.printStackTrace();
				}
				repaint();
			}
		});
		timer.start();
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		g.setColor(GREEN);
		g.drawImage(bufferedImage, 0, 0, null);
		// ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);
		// ((Graphics2D)g).setStroke(new BasicStroke(3));
		// ((Graphics2D)g).draw(outline);
	}
}