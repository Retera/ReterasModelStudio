package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.ui.application.edit.mesh.activity.CursorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public abstract class ViewportView extends JPanel {

	protected CoordinateSystem coordinateSystem;

	protected boolean mouseInBounds = false;
	protected Viewport viewport;
	protected JPanel popupParent;
	protected JPopupMenu contextMenu;

	protected Component boxX, boxY;

	protected Point lastMouseMotion = new Point(0, 0);
	protected CursorManager cursorManager;

	protected ModelHandler modelHandler;
	protected ProgramPreferences programPreferences;
	protected ViewportActivity viewportActivity;
	protected ViewportListener viewportListener;
	protected CoordDisplayListener coordDisplayListener;

	protected Point lastClick;
	protected Point selectStart;
	protected Point actStart;
	protected Timer clickTimer = new Timer(16, e -> clickTimer());

	public ViewportView(ModelHandler modelHandler, byte d1, byte d2,
	                    Dimension minDim,
	                    ProgramPreferences programPreferences,
	                    ViewportActivity viewportActivity,
	                    ViewportListener viewportListener,
	                    CoordDisplayListener coordDisplayListener) {
		this.modelHandler = modelHandler;
		this.programPreferences = programPreferences;
		this.viewportActivity = viewportActivity;
		this.viewportListener = viewportListener;
		this.coordDisplayListener = coordDisplayListener;

		coordinateSystem = new CoordinateSystem(d1, d2, this);
		popupParent = this;

		setBorder(BorderFactory.createBevelBorder(1));
		setBackground(programPreferences.getBackgroundColor());
		programPreferences.addChangeListener(() -> setBackground(programPreferences.getBackgroundColor()));

		// Viewport border
		setMinimumSize(minDim);
		add(boxX = Box.createHorizontalStrut(minDim.width));
		add(boxY = Box.createVerticalStrut(minDim.height));
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		MouseAdapter mouseAdapter = getMouseAdapter();
		addMouseListener(mouseAdapter);
		addMouseWheelListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);


		cursorManager = this::setCursor;
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


	private boolean clickTimer() {
		int xoff = 0;
		int yoff = 0;
		Component temp = this;
		while (temp != null) {
			xoff += temp.getX();
			yoff += temp.getY();
			temp = temp.getParent();
		}
		PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		if ((pointerInfo == null) || (pointerInfo.getLocation() == null)) {
			return true;
		}
		double mx = pointerInfo.getLocation().x - xoff;
		double my = pointerInfo.getLocation().y - yoff;

		if (lastClick != null) {

			int deltaX = (int) mx - lastClick.x;
			int deltaY = (int) my - lastClick.y;
			coordinateSystem.translateZoomed(deltaX, deltaY);

			lastClick.x = (int) mx;
			lastClick.y = (int) my;
		}

		coordDisplayListener.notifyUpdate(coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomX(mx), coordinateSystem.geomY(my));

		repaint();
		return false;
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


	protected MouseAdapter getMouseAdapter() {
		return new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				if (!viewportActivity.isEditing()) {
					viewportActivity.viewportChanged(cursorManager);
					requestFocus();
					mouseInBounds = true;
					setBorder(BorderFactory.createBevelBorder(1, Color.YELLOW, Color.YELLOW.darker()));
					clickTimer.setRepeats(true);
					clickTimer.start();
				}
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				if (!viewportActivity.isEditing()) {
					if ((selectStart == null) && (actStart == null) && (lastClick == null)) {
						clickTimer.stop();
					}
					mouseInBounds = false;
					setBorder(BorderFactory.createBevelBorder(1));
				}
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON2) {
					lastClick = new Point(e.getX(), e.getY());
				} else if (e.getButton() == MouseEvent.BUTTON1) {
					viewportActivity.viewportChanged(cursorManager);
					viewportListener.viewportChanged(viewport);
					requestFocus();
					viewportActivity.mousePressed(e, coordinateSystem);
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					viewportActivity.viewportChanged(cursorManager);
					viewportListener.viewportChanged(viewport);
					requestFocus();
					viewportActivity.mousePressed(e, coordinateSystem);
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
//				if (!mouseInBounds && (selectStart == null) && (actStart == null) && (lastClick == null)) {
//					clickTimer.stop();
//					repaint();
//				}
				modelHandler.getUndoHandler().refreshUndo();
				// TODO fix, refresh undo
				if ((e.getButton() == MouseEvent.BUTTON2) && (lastClick != null)) {
					double translateX = (e.getX() - lastClick.x);
					double translateY = (e.getY() - lastClick.y);
					coordinateSystem.translateZoomed(translateX, translateY);
					lastClick = null;
				} else if (e.getButton() == MouseEvent.BUTTON1) {
					viewportActivity.mouseReleased(e, coordinateSystem);
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					viewportActivity.mouseReleased(e, coordinateSystem);
				}
				if (!mouseInBounds && (selectStart == null) && (actStart == null) && (lastClick == null)) {
					clickTimer.stop();
					repaint();
				}
				if (mouseInBounds && !getBounds().contains(e.getPoint()) && !viewportActivity.isEditing()) {
					mouseExited(e);
				}
			}

			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					contextMenu.show(popupParent, e.getX(), e.getY());
				}
			}

			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				int wr = e.getWheelRotation();

				int dir = wr < 0 ? -1 : 1;

				double mouseX = e.getX();
				double mouseY = e.getY();

				for (int i = 0; i < wr * dir; i++) {
					double zoomAdjust = .15 * dir / 1.15;

					double w = mouseX - (getWidth() / 2.0) ;
					double h = mouseY - (getHeight() / 2.0);

					coordinateSystem.translateZoomed(w * zoomAdjust, h * zoomAdjust);

					if (dir == -1) {
						coordinateSystem.zoomIn(1.15);
					} else {
						coordinateSystem.zoomOut(1.15);
					}
				}
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				viewportActivity.mouseDragged(e, coordinateSystem);
				lastMouseMotion = e.getPoint();
//				repaint();
			}

			@Override
			public void mouseMoved(final MouseEvent e) {
				if (!mouseInBounds && getBounds().contains(e.getPoint()) && !viewportActivity.isEditing()) {
					mouseEntered(e);
				}
				viewportActivity.mouseMoved(e, coordinateSystem);
				lastMouseMotion = e.getPoint();
				repaint();
			}
		};
	}
}
