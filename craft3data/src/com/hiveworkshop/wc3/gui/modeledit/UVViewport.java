package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

public class UVViewport extends JPanel implements MouseListener, ActionListener, MouseWheelListener {
	ArrayList<Image> backgrounds = new ArrayList<>();
	MDLDisplay dispMDL;
	double m_a = 0;
	double m_b = 0;
	double m_zoom = 1;
	double aspectRatio = 1;
	Point lastClick;
	Point selectStart;
	Point actStart;
	Timer clickTimer = new Timer(16, this);
	boolean mouseInBounds = false;
	JPopupMenu contextMenu;
	JMenuItem placeholderButton;
	UVPanel parent;
	Component boxX, boxY;

	public UVViewport(final MDLDisplay dispMDL, final UVPanel parent) {
		// Dimension 1 and Dimension 2, these specify which dimensions to
		// display.
		// the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z
		// dimensions
		//
		// Viewport border
		setBorder(BorderFactory.createBevelBorder(1));
		setBackground(new Color(255, 255, 255));
		setMinimumSize(new Dimension(400, 400));
		add(boxX = Box.createHorizontalStrut(400));
		add(boxY = Box.createVerticalStrut(400));
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.dispMDL = dispMDL;
		addMouseListener(this);
		addMouseWheelListener(this);

		contextMenu = new JPopupMenu();
		placeholderButton = new JMenuItem("Placeholder Button");
		placeholderButton.addActionListener(this);
		contextMenu.add(placeholderButton);

		this.parent = parent;
	}

	public void init() {
		m_zoom = getWidth();
		m_a = geomX(0);
		m_b = geomY(0);
	}

	public void setPosition(final double a, final double b) {
		m_a = a;
		m_b = b;
	}

	public void translate(final double a, final double b) {
		m_a += a / aspectRatio;
		m_b += b;
	}

	public void zoom(final double amount) {
		m_zoom *= (1 + amount);
	}

	public double getZoomAmount() {
		return m_zoom;
	}

	public Point2D.Double getDisplayOffset() {
		return new Point2D.Double(m_a, m_b);
	}

	public BufferedImage getBufferedImage() {
		final BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		paintComponent(image.getGraphics(), 5);
		return image;
	}

	@Override
	public void paintComponent(final Graphics g) {
		paintComponent(g, 1);
	}

	public void paintComponent(final Graphics g, final int vertexSize) {
		super.paintComponent(g);
		for (int i = 0; i < backgrounds.size(); i++) {
			if (parent.wrapImage.isSelected()) {
				for (int y = -15; y < 15; y++) {
					for (int x = -15; x < 15; x++) {
						g.drawImage(backgrounds.get(i), (int) convertX(x), (int) convertY(y),
								(int) (convertX(x + 1) - convertX(x)), (int) (convertY(y + 1) - convertY(y)), null);
					}
				}
			} else {
				g.drawImage(backgrounds.get(i), (int) convertX(0), (int) convertY(0), (int) (convertX(1) - convertX(0)),
						(int) (convertY(1) - convertY(0)), null);
			}
		}
		dispMDL.drawGeosets(g, this, vertexSize);

		// Visual effects from user controls
		int xoff = 0;
		int yoff = 0;
		Component temp = this;
		while (temp != null) {
			xoff += temp.getX();
			yoff += temp.getY();
			if (temp.getClass() == ModelPanel.class) {
				// temp = MainFrame.panel;
				temp = null; // TODO fix
			} else {
				temp = temp.getParent();
			}
		}

		try {
			final double mx = (MouseInfo.getPointerInfo().getLocation().x - xoff);// MainFrame.frame.getX()-8);
			final double my = (MouseInfo.getPointerInfo().getLocation().y - yoff);// MainFrame.frame.getY()-30);

			// SelectionBox:
			if (selectStart != null) {
				final Point sEnd = new Point((int) mx, (int) my);
				final Rectangle2D.Double r = pointsToRect(selectStart, sEnd);
				g.setColor(MDLDisplay.selectColor);
				((Graphics2D) g).draw(r);
			}
		} catch (final Exception exc) {
			// JOptionPane.showMessageDialog(null,"Error retrieving mouse
			// coordinates. (Probably not a major issue. Due to sleep mode?)");
			throw new RuntimeException(exc);
		}
	}

	public double convertX(final double x) {
		return ((x + m_a) * m_zoom) * aspectRatio + getWidth() / 2;
	}

	public double convertY(final double y) {
		return ((y + m_b) * m_zoom) + getHeight() / 2;
	}

	public double geomX(final double x) {
		return (x - getWidth() / 2) / aspectRatio / m_zoom - m_a;
	}

	public double geomY(final double y) {
		return ((y - getHeight() / 2) / m_zoom - m_b);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == clickTimer) {
			int xoff = 0;
			int yoff = 0;
			Component temp = this;
			while (temp != null) {
				xoff += temp.getX();
				yoff += temp.getY();
				if (temp.getClass() == ModelPanel.class) {
					// temp = MainFrame.panel;
					temp = null; // TODO fix
				} else {
					temp = temp.getParent();
				}
			}
			final double mx = (MouseInfo.getPointerInfo().getLocation().x - xoff);// MainFrame.frame.getX()-8);
			final double my = (MouseInfo.getPointerInfo().getLocation().y - yoff);// MainFrame.frame.getY()-30);
			// JOptionPane.showMessageDialog(null,mx+","+my+" as mouse,
			// "+lastClick.x+","+lastClick.y+" as last.");
			// System.out.println(xoff+" and "+mx);
			if (lastClick != null) {

				m_a += ((int) mx - lastClick.x) / aspectRatio / m_zoom;
				m_b += ((int) my - lastClick.y) / m_zoom;
				lastClick.x = (int) (mx);
				lastClick.y = (int) my;
			}
			parent.setMouseCoordDisplay(((mx - getWidth() / 2) / aspectRatio / m_zoom) - m_a,
					(((my - getHeight() / 2) / m_zoom) - m_b));

			if (actStart != null) {
				final Point actEnd = new Point((int) mx, (int) my);
				final Point2D.Double convertedStart = new Point2D.Double(geomX(actStart.x), geomY(actStart.y));
				final Point2D.Double convertedEnd = new Point2D.Double(geomX(actEnd.x), geomY(actEnd.y));
				dispMDL.updateUVAction(convertedStart, convertedEnd);
				actStart = actEnd;
			}
			repaint();
		} else if (e.getSource() == placeholderButton) {
			JOptionPane.showMessageDialog(null, "Placeholder code.");
		}
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		clickTimer.setRepeats(true);
		clickTimer.start();
		mouseInBounds = true;
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		if (selectStart == null && actStart == null && lastClick == null) {
			clickTimer.stop();
		}
		mouseInBounds = false;
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2) {
			lastClick = new Point(e.getX(), e.getY());
		} else if (e.getButton() == MouseEvent.BUTTON1) {
			selectStart = new Point(e.getX(), e.getY());
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			actStart = new Point(e.getX(), e.getY());
			final Point2D.Double convertedStart = new Point2D.Double(geomX(actStart.x), geomY(actStart.y));
			dispMDL.startUVAction(convertedStart, parent.currentActionType());
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2) {
			m_a += (e.getX() - lastClick.x) / m_zoom;
			m_b += (e.getY() - lastClick.y) / m_zoom;
			lastClick = null;
		} else if (e.getButton() == MouseEvent.BUTTON1 && selectStart != null) {
			final Point selectEnd = new Point(e.getX(), e.getY());
			final Rectangle2D.Double area = pointsToGeomRect(selectStart, selectEnd);
			// System.out.println(area);
			dispMDL.selectTVerteces(area, parent.currentSelectionType());
			selectStart = null;
		} else if (e.getButton() == MouseEvent.BUTTON3 && actStart != null) {
			final Point actEnd = new Point(e.getX(), e.getY());
			final Point2D.Double convertedStart = new Point2D.Double(geomX(actStart.x), geomY(actStart.y));
			final Point2D.Double convertedEnd = new Point2D.Double(geomX(actEnd.x), geomY(actEnd.y));
			dispMDL.finishUVAction(convertedStart, convertedEnd);
			actStart = null;
		}
		if (!mouseInBounds && selectStart == null && actStart == null && lastClick == null) {
			clickTimer.stop();
			repaint();
		}
		// MainFrame.panel.refreshUndo();
		// TODO fix, refresh undo
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {

			// if( actEnd.equals(actStart) )
			// {
			// actStart = null;
			contextMenu.show(this, e.getX(), e.getY());
			// }
		}
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		int wr = e.getWheelRotation();
		final boolean neg = wr < 0;

		// get mouse coords
		int xoff = 0;
		int yoff = 0;
		Component temp = this;
		while (temp != null) {
			xoff += temp.getX();
			yoff += temp.getY();
			if (temp.getClass() == ModelPanel.class) {
				// temp = MainFrame.panel;
				temp = null; // TODO fix
			} else {
				temp = temp.getParent();
			}
		}
		final double mx = (MouseInfo.getPointerInfo().getLocation().x - xoff);
		final double my = (MouseInfo.getPointerInfo().getLocation().y - yoff);

		if (neg) {
			wr = -wr;
		}
		for (int i = 0; i < wr; i++) {
			if (neg) {
				m_a -= ((mx - getWidth() / 2) / aspectRatio) * (1 / m_zoom - 1 / (m_zoom * 1.15));
				m_b -= (my - getHeight() / 2) * (1 / m_zoom - 1 / (m_zoom * 1.15));
				m_zoom *= 1.15;
			} else {
				m_zoom /= 1.15;
				m_a -= ((mx - getWidth() / 2) / aspectRatio) * (1 / (m_zoom * 1.15) - 1 / m_zoom);
				m_b -= (my - getHeight() / 2) * (1 / (m_zoom * 1.15) - 1 / m_zoom);
			}
		}
	}

	public Rectangle2D.Double pointsToGeomRect(final Point a, final Point b) {
		final Point2D.Double topLeft = new Point2D.Double(Math.min(geomX(a.x), geomX(b.x)),
				Math.min(geomY(a.y), geomY(b.y)));
		final Point2D.Double lowRight = new Point2D.Double(Math.max(geomX(a.x), geomX(b.x)),
				Math.max(geomY(a.y), geomY(b.y)));
		final Rectangle2D.Double temp = new Rectangle2D.Double(topLeft.x, topLeft.y, (lowRight.x - (topLeft.x)),
				((lowRight.y) - (topLeft.y)));
		return temp;
	}

	public Rectangle2D.Double pointsToRect(final Point a, final Point b) {
		final Point2D.Double topLeft = new Point2D.Double(Math.min((a.x), (b.x)), Math.min((a.y), (b.y)));
		final Point2D.Double lowRight = new Point2D.Double(Math.max((a.x), (b.x)), Math.max((a.y), (b.y)));
		final Rectangle2D.Double temp = new Rectangle2D.Double(topLeft.x, topLeft.y, (lowRight.x - (topLeft.x)),
				((lowRight.y) - (topLeft.y)));
		return temp;
	}

	public void setAspectRatio(final double ratio) {
		aspectRatio = ratio;
		setMinimumSize(new Dimension((int) (400 * ratio), 400));
		remove(boxX);
		add(boxX = Box.createHorizontalStrut((int) (400 * ratio)));
		parent.packFrame();
	}

	public void addBackgroundImage(final Image i) {
		backgrounds.add(i);
		setAspectRatio(i.getWidth(null) / (double) i.getHeight(null));
	}

	public void clearBackgroundImage() {
		backgrounds.clear();
	}
}