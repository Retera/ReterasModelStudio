package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.CursorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.BasicCoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public abstract class ViewportView extends JPanel implements CoordinateSystem {
	protected double aspectRatio = 1;
	protected byte m_d1;
	protected byte m_d2;
	protected double m_a = 0;
	protected double m_b = 0;
	protected double m_zoom = 1;

	protected boolean mouseInBounds = false;
	protected Viewport viewport;
	protected JPanel popupParent;
	protected JPopupMenu contextMenu;

	protected int yFlip = 1;

	protected Point lastMouseMotion = new Point(0, 0);
	protected CursorManager cursorManager;

	protected ModelView modelView;
	protected UndoHandler undoHandler;
	protected UndoActionListener undoListener;
	protected ProgramPreferences programPreferences;
	protected ViewportActivity activityListener;
	protected ViewportListener viewportListener;
	protected CoordDisplayListener coordDisplayListener;

	protected CoordinateSystem coordinateSystem;


	protected Point lastClick;
	protected Point selectStart;
	protected Point actStart;
	protected Timer clickTimer = new Timer(16, e -> clickTimer());


	public double getCameraX() {
		return m_a;
	}

	public double getCameraY() {
		return m_b;
	}

	public double getZoom() {
		return m_zoom;
	}


	public void setPosition(double a, double b) {
		m_a = a;
		m_b = b;
	}

	public void translate(double a, double b) {
		m_a += a / aspectRatio;
		m_b += b;
	}

	public void zoom(final double amount) {
		m_zoom *= 1 + amount;
	}

	public double getZoomAmount() {
		return m_zoom;
	}

	public void drawGrid(Graphics g) {
		Point2D.Double cameraOrigin = new Point2D.Double(viewX(0), viewY(0));

		float increment = 20 * (float) getZoomAmount();
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

			m_a += ((int) mx - lastClick.x) / aspectRatio / m_zoom;
			m_b += ((int) my - lastClick.y) / m_zoom;
			lastClick.x = (int) mx;
			lastClick.y = (int) my;
		}
		coordDisplayListener.notifyUpdate(m_d1, m_d2, ((mx - (getWidth() / 2.0)) / aspectRatio / m_zoom) - m_a, ((my - (getHeight() / 2.0)) / m_zoom) - m_b);
//		parent.setMouseCoordDisplay(((mx - (getWidth() / 2.0)) / aspectRatio / m_zoom) - m_a, ((my - (getHeight() / 2.0)) / m_zoom) - m_b);

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

	@Override
	public double viewX(double x) {
		return ((x + m_a) * m_zoom * aspectRatio) + (getWidth() / 2.0);
	}

	@Override
	public double viewY(double y) {
		return ((y * yFlip + m_b) * m_zoom) + (getHeight() / 2.0);
	}

	@Override
	public double geomX(double x) {
		return ((x - (getWidth() / 2.0)) / aspectRatio / m_zoom) - m_a;
	}

	@Override
	public double geomY(double y) {
		return yFlip * ((y - (getHeight() / 2.0)) / m_zoom) - m_b;
	}


	@Override
	public CoordinateSystem copy() {
		return new BasicCoordinateSystem(m_d1, m_d2, m_a, m_b, m_zoom, getWidth(), getHeight());
	}

	@Override
	public byte getPortFirstXYZ() {
		return m_d1;
	}

	@Override
	public byte getPortSecondXYZ() {
		return m_d2;
	}


	protected MouseAdapter getMouseAdapter() {
		return new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				if (!activityListener.isEditing()) {
					activityListener.viewportChanged(cursorManager);
					requestFocus();
					mouseInBounds = true;
					setBorder(BorderFactory.createBevelBorder(1, Color.YELLOW, Color.YELLOW.darker()));
					clickTimer.setRepeats(true);
					clickTimer.start();
				}
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				if (!activityListener.isEditing()) {
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
					activityListener.viewportChanged(cursorManager);
					viewportListener.viewportChanged(viewport);
					requestFocus();
					activityListener.mousePressed(e, coordinateSystem);
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					activityListener.viewportChanged(cursorManager);
					viewportListener.viewportChanged(viewport);
					requestFocus();
					activityListener.mousePressed(e, coordinateSystem);
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
//				if (!mouseInBounds && (selectStart == null) && (actStart == null) && (lastClick == null)) {
//					clickTimer.stop();
//					repaint();
//				}
				undoHandler.refreshUndo();
				// TODO fix, refresh undo
				if ((e.getButton() == MouseEvent.BUTTON2) && (lastClick != null)) {
					m_a += (e.getX() - lastClick.x) / m_zoom;
					m_b += (e.getY() - lastClick.y) / m_zoom;
					lastClick = null;
				} else if (e.getButton() == MouseEvent.BUTTON1) {
					activityListener.mouseReleased(e, coordinateSystem);
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					activityListener.mouseReleased(e, coordinateSystem);
				}
				if (!mouseInBounds && (selectStart == null) && (actStart == null) && (lastClick == null)) {
					clickTimer.stop();
					repaint();
				}
				if (mouseInBounds && !getBounds().contains(e.getPoint()) && !activityListener.isEditing()) {
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

				double mx = e.getX();
				double my = e.getY();

				for (int i = 0; i < wr * dir; i++) {
					double zoomAmount = ((1 / m_zoom) - (1 / (m_zoom * 1.15))) * dir;

					double w = (mx - (getWidth() / 2.0)) / aspectRatio;
					double h = my - (getHeight() / 2.0);


					m_a += w * zoomAmount;
					m_b += h * zoomAmount;

					if (dir == -1) {
						m_zoom *= 1.15;
					} else {
						m_zoom /= 1.15;
					}
				}
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				activityListener.mouseDragged(e, coordinateSystem);
				lastMouseMotion = e.getPoint();
//				repaint();
			}

			@Override
			public void mouseMoved(final MouseEvent e) {
				if (!mouseInBounds && getBounds().contains(e.getPoint()) && !activityListener.isEditing()) {
					mouseEntered(e);
				}
				activityListener.mouseMoved(e, coordinateSystem);
				lastMouseMotion = e.getPoint();
				repaint();
			}
		};
	}
}
