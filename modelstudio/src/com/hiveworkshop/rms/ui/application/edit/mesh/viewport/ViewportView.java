package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

public abstract class ViewportView extends JPanel {

	private final double ZOOM_FACTOR = 1.15;
	protected CoordinateSystem coordinateSystem;

	protected JPopupMenu contextMenu;

	protected Component boxX, boxY;

	protected Point lastMouseMotion = new Point(0, 0);

	protected ModelHandler modelHandler;
	protected ViewportActivityManager viewportActivity;

	protected MouseListenerThing2 mouseListenerThing2;

	public ViewportView(byte d1, byte d2,
	                    Dimension minDim,
	                    BiConsumer<Double, Double> coordDisplayListener2) {

		coordinateSystem = new CoordinateSystem(d1, d2, this);

		setBorder(BorderFactory.createBevelBorder(1));
		setBackground(ProgramGlobals.getPrefs().getBackgroundColor());

		// Viewport border
//		setMinimumSize(minDim);
//		add(boxX = Box.createHorizontalStrut(minDim.width));
//		add(boxY = Box.createVerticalStrut(minDim.height));
//		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		mouseListenerThing2 = new MouseListenerThing2(this, coordDisplayListener2, coordinateSystem);
		addMouseListener(mouseListenerThing2);
		addMouseWheelListener(mouseListenerThing2);
		addMouseMotionListener(mouseListenerThing2);
	}

	public void setModel(ModelHandler modelHandler, ViewportActivityManager viewportActivity) {
		this.modelHandler = modelHandler;
		this.viewportActivity = viewportActivity;
		if (modelHandler != null) {
			mouseListenerThing2.setUndoHandler(modelHandler.getUndoHandler());
		} else {
			mouseListenerThing2.setUndoHandler(null);
		}
		mouseListenerThing2.setViewportActivity(viewportActivity);
	}

	public CoordinateSystem getCoordinateSystem() {
		return coordinateSystem;
	}

	public void drawGrid(Graphics g) {
		Point2D.Double cameraOrigin = new Point2D.Double(coordinateSystem.viewX(0), coordinateSystem.viewY(0));

		float increment = 20 * (float) coordinateSystem.getZoom();
		while (increment < 100) {
			increment *= 10;
		}
		float lightIncrement = increment;
		while (lightIncrement > 100) {
			lightIncrement /= 10;
		}
		float darkIncrement = increment * 10;
		g.setColor(Color.DARK_GRAY);
		drawXLines(g, cameraOrigin, lightIncrement);
		drawYLines(g, cameraOrigin, lightIncrement);

		g.setColor(Color.GRAY);
		drawXLines(g, cameraOrigin, increment);
		drawYLines(g, cameraOrigin, increment);

		g.setColor(Color.ORANGE);
		drawXLines(g, cameraOrigin, darkIncrement);
		drawYLines(g, cameraOrigin, darkIncrement);

		g.setColor(Color.BLACK);
		g.drawLine(0, (int) cameraOrigin.y, getWidth(), (int) cameraOrigin.y);
		g.drawLine((int) cameraOrigin.x, 0, (int) cameraOrigin.x, getHeight());
	}

	private void drawXLines(Graphics g, Point2D.Double cameraOrigin, float distance) {
		for (float x = 0; ((cameraOrigin.x + x) < getWidth()) || ((cameraOrigin.x - x) >= 0); x += distance) {
			g.drawLine((int) (cameraOrigin.x + x), 0, (int) (cameraOrigin.x + x), getHeight());
			g.drawLine((int) (cameraOrigin.x - x), 0, (int) (cameraOrigin.x - x), getHeight());
		}
	}

	private void drawYLines(Graphics g, Point2D.Double cameraOrigin, float distance) {
		for (float y = 0; ((cameraOrigin.y + y) < getHeight()) || ((cameraOrigin.y - y) >= 0); y += distance) {
			g.drawLine(0, (int) (cameraOrigin.y + y), getWidth(), (int) (cameraOrigin.y + y));
			g.drawLine(0, (int) (cameraOrigin.y - y), getWidth(), (int) (cameraOrigin.y - y));
		}
	}

	public int[] getOffsets() {
		int[] off = new int[] {0, 0};
		Component temp = this;
		while (temp != null) {
			off[0] += temp.getX();
			off[1] += temp.getY();
			temp = temp.getParent();
		}
		return off;
	}

	public BufferedImage getBufferedImage() {
		BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		super.paintComponent(image.getGraphics());
		paintComponent(image.getGraphics(), 5);
		return image;
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		paintComponent(g, 1);
	}

	public abstract void paintComponent(final Graphics g, final int vertexSize);
}
