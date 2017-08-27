package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionTypeApplicator;
import com.hiveworkshop.wc3.gui.modeledit.useractions.CursorManager;
import com.hiveworkshop.wc3.gui.modeledit.useractions.UndoManager;
import com.hiveworkshop.wc3.gui.modeledit.useractions.ViewportActivity;
import com.hiveworkshop.wc3.gui.modeledit.viewport.ViewportModelRenderer;
import com.hiveworkshop.wc3.gui.modeledit.viewport.ViewportView;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.util.Callback;

public class Viewport extends JPanel implements MouseListener, ActionListener, MouseWheelListener, CoordinateSystem,
		ViewportView, MouseMotionListener {
	MDLDisplay dispMDL;
	byte m_d1;
	byte m_d2;
	double m_a = 0;
	double m_b = 0;
	double m_zoom = 1;
	Point lastClick;
	Point selectStart;
	Point actStart;
	Timer clickTimer = new Timer(16, this);
	boolean mouseInBounds = false;
	JPopupMenu contextMenu;
	JMenuItem reAssignMatrix;
	JMenuItem renameBone;
	JMenuItem cogBone;
	JMenuItem manualMove;
	JMenuItem manualRotate;
	JMenuItem manualSet;
	JMenuItem addTeamColor;

	private final ViewportModelRenderer viewportModelRenderer;
	private final ViewportActivity activityListener;
	private final SelectionManager selectionManager;
	private final SelectionTypeApplicator selectionTypeApplicator;
	private final CursorManager cursorManager;
	private final UndoManager undoManager;
	private final Callback<List<Geoset>> geosetAdditionListener;

	public Viewport(final byte d1, final byte d2, final MDLDisplay dispMDL, final ViewportActivity activityListener,
			final SelectionManager selectionManager, final SelectionTypeApplicator selectionListener,
			final Callback<List<Geoset>> geosetAdditionListener) {
		// Dimension 1 and Dimension 2, these specify which dimensions to
		// display.
		// the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z
		// dimensions
		//
		m_d1 = d1;
		m_d2 = d2;
		this.activityListener = activityListener;
		this.selectionManager = selectionManager;
		this.selectionTypeApplicator = selectionListener;
		this.undoManager = dispMDL;
		this.geosetAdditionListener = geosetAdditionListener;
		this.cursorManager = new CursorManager() {
			@Override
			public void setCursor(final Cursor cursor) {
				Viewport.this.setCursor(cursor);
			}
		};
		// Viewport border
		setBorder(BorderFactory.createBevelBorder(1));
		if (dispMDL.getProgramPreferences().isInvertedDisplay()) {
			setBackground(Color.DARK_GRAY.darker());
		} else {
			setBackground(new Color(255, 255, 255));
		}
		setMinimumSize(new Dimension(200, 200));
		add(Box.createHorizontalStrut(200));
		add(Box.createVerticalStrut(200));
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.dispMDL = dispMDL;
		addMouseListener(this);
		addMouseWheelListener(this);
		addMouseMotionListener(this);

		contextMenu = new JPopupMenu();
		reAssignMatrix = new JMenuItem("Re-assign Matrix");
		reAssignMatrix.addActionListener(this);
		contextMenu.add(reAssignMatrix);
		cogBone = new JMenuItem("Auto-Center Bone(s)");
		cogBone.addActionListener(this);
		contextMenu.add(cogBone);
		manualMove = new JMenuItem("Translation Type-in");
		manualMove.addActionListener(this);
		contextMenu.add(manualMove);
		manualRotate = new JMenuItem("Rotate Type-in");
		manualRotate.addActionListener(this);
		contextMenu.add(manualRotate);
		manualSet = new JMenuItem("Position Type-in");
		manualSet.addActionListener(this);
		contextMenu.add(manualSet);
		renameBone = new JMenuItem("Rename Bone");
		renameBone.addActionListener(this);
		contextMenu.add(renameBone);
		addTeamColor = new JMenuItem("Add teamcolor underlayer");
		addTeamColor.addActionListener(this);
		contextMenu.add(addTeamColor);

		viewportModelRenderer = new ViewportModelRenderer(3);

	}

	public void setPosition(final double a, final double b) {
		m_a = a;
		m_b = b;
	}

	public void translate(final double a, final double b) {
		m_a += a;
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

	@Override
	public byte getPortFirstXYZ() {
		return m_d1;
	}

	@Override
	public byte getPortSecondXYZ() {
		return m_d2;
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
		if (dispMDL.getProgramPreferences().isInvertedDisplay()) {
			final Point2D.Double cameraOrigin = new Point2D.Double(convertX(0), convertY(0));

			float increment = 20 * (float) getZoomAmount();
			while (increment < 100) {
				increment *= 10;
			}
			float lightIncrement = increment;
			while (lightIncrement > 100) {
				lightIncrement /= 10;
			}
			final float darkIncrement = increment * 10;
			g.setColor(Color.DARK_GRAY);
			for (float x = 0; cameraOrigin.x + x < getWidth() || cameraOrigin.x - x >= 0; x += lightIncrement) {
				g.drawLine((int) (cameraOrigin.x + x), 0, (int) (cameraOrigin.x + x), getHeight());
				g.drawLine((int) (cameraOrigin.x - x), 0, (int) (cameraOrigin.x - x), getHeight());
			}
			for (float y = 0; cameraOrigin.y + y < getHeight() || cameraOrigin.y - y >= 0; y += lightIncrement) {
				g.drawLine(0, (int) (cameraOrigin.y + y), getWidth(), (int) (cameraOrigin.y + y));
				g.drawLine(0, (int) (cameraOrigin.y - y), getWidth(), (int) (cameraOrigin.y - y));
			}
			g.setColor(Color.GRAY);
			for (float x = 0; cameraOrigin.x + x < getWidth() || cameraOrigin.x - x >= 0; x += increment) {
				g.drawLine((int) (cameraOrigin.x + x), 0, (int) (cameraOrigin.x + x), getHeight());
				g.drawLine((int) (cameraOrigin.x - x), 0, (int) (cameraOrigin.x - x), getHeight());
			}
			for (float y = 0; cameraOrigin.y + y < getHeight() || cameraOrigin.y - y >= 0; y += increment) {
				g.drawLine(0, (int) (cameraOrigin.y + y), getWidth(), (int) (cameraOrigin.y + y));
				g.drawLine(0, (int) (cameraOrigin.y - y), getWidth(), (int) (cameraOrigin.y - y));
			}
			g.setColor(Color.ORANGE);
			for (float x = 0; cameraOrigin.x + x < getWidth() || cameraOrigin.x - x >= 0; x += darkIncrement) {
				g.drawLine((int) (cameraOrigin.x + x), 0, (int) (cameraOrigin.x + x), getHeight());
				g.drawLine((int) (cameraOrigin.x - x), 0, (int) (cameraOrigin.x - x), getHeight());
			}
			for (float y = 0; cameraOrigin.y + y < getHeight() || cameraOrigin.y - y >= 0; y += darkIncrement) {
				g.drawLine(0, (int) (cameraOrigin.y + y), getWidth(), (int) (cameraOrigin.y + y));
				g.drawLine(0, (int) (cameraOrigin.y - y), getWidth(), (int) (cameraOrigin.y - y));
			}
			g.setColor(Color.BLACK);
			g.drawLine(0, (int) cameraOrigin.y, getWidth(), (int) cameraOrigin.y);
			g.drawLine((int) cameraOrigin.x, 0, (int) cameraOrigin.x, getHeight());
		}
		final Graphics2D graphics2d = (Graphics2D) g;

		// dispMDL.drawGeosets(g, this, 1);
		// dispMDL.drawPivots(g, this, 1);
		// dispMDL.drawCameras(g, this, 1);
		viewportModelRenderer.reset(graphics2d, dispMDL.getProgramPreferences(), m_d1, m_d2, this, this);
		dispMDL.getMDL().render(viewportModelRenderer);
		selectionManager.render(graphics2d, this);
		activityListener.render(graphics2d);

		switch (m_d1) {
		case 0:
			g.setColor(new Color(0, 255, 0));
			break;
		case 1:
			g.setColor(new Color(255, 0, 0));
			break;
		case 2:
			g.setColor(new Color(0, 0, 255));
			break;
		}
		// g.setColor( new Color( 255, 0, 0 ) );
		g.drawLine((int) Math.round(convertX(0)), (int) Math.round(convertY(0)), (int) Math.round(convertX(5)),
				(int) Math.round(convertY(0)));

		switch (m_d2) {
		case 0:
			g.setColor(new Color(0, 255, 0));
			break;
		case 1:
			g.setColor(new Color(255, 0, 0));
			break;
		case 2:
			g.setColor(new Color(0, 0, 255));
			break;
		}
		// g.setColor( new Color( 255, 0, 0 ) );
		g.drawLine((int) Math.round(convertX(0)), (int) Math.round(convertY(0)), (int) Math.round(convertX(0)),
				(int) Math.round(convertY(5)));

		// Visual effects from user controls
		int xoff = 0;
		int yoff = 0;
		Component temp = this;
		while (temp != null) {
			xoff += temp.getX();
			yoff += temp.getY();
			// if( temp.getClass() == ModelPanel.class )
			// {
			//// temp = MainFrame.panel; TODO
			// temp = null;
			// }
			// else
			// {
			temp = temp.getParent();
			// }
		}

		// try {
		// final double mx = (MouseInfo.getPointerInfo().getLocation().x -
		// xoff);// MainFrame.frame.getX()-8);
		// final double my = (MouseInfo.getPointerInfo().getLocation().y -
		// yoff);// MainFrame.frame.getY()-30);
		//
		// // SelectionBox:
		// if (selectStart != null) {
		// final Point sEnd = new Point((int) mx, (int) my);
		// final Rectangle2D.Double r = pointsToRect(selectStart, sEnd);
		// g.setColor(MDLDisplay.selectColor);
		// graphics2d.draw(r);
		// }
		// } catch (final Exception exc) {
		// exc.printStackTrace();
		// // JOptionPane.showMessageDialog(null,"Error retrieving mouse
		// // coordinates. (Probably not a major issue. Due to sleep mode?)");
		// }
	}

	@Override
	public double convertX(final double x) {
		return (x + m_a) * m_zoom + getWidth() / 2;
	}

	@Override
	public double convertY(final double y) {
		return ((-y + m_b) * m_zoom) + getHeight() / 2;
	}

	@Override
	public double geomX(final double x) {
		return (x - getWidth() / 2) / m_zoom - m_a;
	}

	@Override
	public double geomY(final double y) {
		return -((y - getHeight() / 2) / m_zoom - m_b);
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
				// if( temp.getClass() == ModelPanel.class )
				// {
				//// temp = MainFrame.panel;
				// temp = null; // TODO
				// }
				// else
				// {
				temp = temp.getParent();
				// }
			}
			final double mx = (MouseInfo.getPointerInfo().getLocation().x - xoff);// MainFrame.frame.getX()-8);
			final double my = (MouseInfo.getPointerInfo().getLocation().y - yoff);// MainFrame.frame.getY()-30);
			// JOptionPane.showMessageDialog(null,mx+","+my+" as mouse,
			// "+lastClick.x+","+lastClick.y+" as last.");
			// System.out.println(xoff+" and "+mx);
			if (lastClick != null) {

				m_a += ((int) mx - lastClick.x) / m_zoom;
				m_b += ((int) my - lastClick.y) / m_zoom;
				lastClick.x = (int) mx;
				lastClick.y = (int) my;
			}
			dispMDL.notifyCoordDisplay(m_d1, m_d2, ((mx - getWidth() / 2) / m_zoom) - m_a,
					-(((my - getHeight() / 2) / m_zoom) - m_b));
			// MainFrame.panel.setMouseCoordDisplay(m_d1,m_d2,((mx-getWidth()/2)/m_zoom)-m_a,-(((my-getHeight()/2)/m_zoom)-m_b));
			// TODO update mouse coord display could be used still

			// if (actStart != null) {
			// final Point actEnd = new Point((int) mx, (int) my);
			// final Point2D.Double convertedStart = new
			// Point2D.Double(geomX(actStart.x), geomY(actStart.y));
			// final Point2D.Double convertedEnd = new
			// Point2D.Double(geomX(actEnd.x), geomY(actEnd.y));
			// dispMDL.updateAction(convertedStart, convertedEnd, m_d1, m_d2);
			// actStart = actEnd;
			// }
			repaint();
		} else if (e.getSource() == reAssignMatrix) {
			final MatrixPopup matrixPopup = new MatrixPopup(dispMDL.getMDL());
			final String[] words = { "Accept", "Cancel" };
			final int i = JOptionPane.showOptionDialog(this, matrixPopup, "Rebuild Matrix", JOptionPane.PLAIN_MESSAGE,
					JOptionPane.YES_NO_OPTION, null, words, words[1]);
			if (i == 0) {
				// JOptionPane.showMessageDialog(null,"action approved");
				dispMDL.setMatrix(matrixPopup.newRefs);
			}
		} else if (e.getSource() == renameBone) {
			final String name = JOptionPane.showInputDialog(this, "Enter bone name:");
			if (name != null) {
				dispMDL.setBoneName(name);
			}
		} else if (e.getSource() == cogBone) {
			dispMDL.cogBones();
		} else if (e.getSource() == addTeamColor) {
			dispMDL.addTeamColor(geosetAdditionListener);
		} else if (e.getSource() == manualMove) {
			dispMDL.manualMove();
		} else if (e.getSource() == manualRotate) {
			dispMDL.manualRotate();
		} else if (e.getSource() == manualSet) {
			dispMDL.manualSet();
		}
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		if (!activityListener.isEditing()) {
			clickTimer.setRepeats(true);
			clickTimer.start();
			mouseInBounds = true;
			setBorder(BorderFactory.createBevelBorder(1, Color.YELLOW, Color.YELLOW.darker()));
			activityListener.reset(selectionManager, selectionTypeApplicator, cursorManager, this, undoManager,
					dispMDL.getModelChangeNotifier());
		}
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		if (!activityListener.isEditing()) {
			if (selectStart == null && actStart == null && lastClick == null) {
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
			activityListener.reset(selectionManager, selectionTypeApplicator, cursorManager, this, undoManager,
					dispMDL.getModelChangeNotifier());
			activityListener.mousePressed(e);
			// selectStart = new Point(e.getX(), e.getY());
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			activityListener.reset(selectionManager, selectionTypeApplicator, cursorManager, this, undoManager,
					dispMDL.getModelChangeNotifier());
			activityListener.mousePressed(e);
			// actStart = new Point(e.getX(), e.getY());
			// final Point2D.Double convertedStart = new
			// Point2D.Double(geomX(actStart.x), geomY(actStart.y));
			// dispMDL.startAction(convertedStart, m_d1, m_d2,
			// dispMDL.getProgramPreferences().currentActionType());
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2) {
			m_a += (e.getX() - lastClick.x) / m_zoom;
			m_b += (e.getY() - lastClick.y) / m_zoom;
			lastClick = null;
		} else if (e
				.getButton() == MouseEvent.BUTTON1/* && selectStart != null */) {
			activityListener.mouseReleased(e);
			// final Point selectEnd = new Point(e.getX(), e.getY());
			// final Rectangle2D.Double area = pointsToGeomRect(selectStart,
			// selectEnd);
			// // System.out.println(area);
			// dispMDL.selectVerteces(area, m_d1, m_d2,
			// dispMDL.getProgramPreferences().currentSelectionType());
			// selectStart = null;
		} else if (e.getButton() == MouseEvent.BUTTON3/* && actStart != null */) {
			// final Point actEnd = new Point(e.getX(), e.getY());
			// final Point2D.Double convertedStart = new
			// Point2D.Double(geomX(actStart.x), geomY(actStart.y));
			// final Point2D.Double convertedEnd = new
			// Point2D.Double(geomX(actEnd.x), geomY(actEnd.y));
			// dispMDL.finishAction(convertedStart, convertedEnd, m_d1, m_d2);
			// actStart = null;
			activityListener.mouseReleased(e);
		}
		if (!mouseInBounds && selectStart == null && actStart == null && lastClick == null) {
			clickTimer.stop();
		}
		repaint();
		// MainFrame.panel.refreshUndo();
		dispMDL.refreshUndo();
		if (mouseInBounds && !getBounds().contains(e.getPoint()) && !activityListener.isEditing()) {
			mouseExited(e);
		}
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
			// if( temp.getClass() == ModelPanel.class )
			// {
			//// temp = MainFrame.panel;
			// temp = null; // TODO fix
			// }
			// else
			// {
			temp = temp.getParent();
			// }
		}
		final double mx = (MouseInfo.getPointerInfo().getLocation().x - xoff);
		final double my = (MouseInfo.getPointerInfo().getLocation().y - yoff);

		if (neg) {
			wr = -wr;
		}
		for (int i = 0; i < wr; i++) {
			if (neg) {
				m_a -= (mx - getWidth() / 2) * (1 / m_zoom - 1 / (m_zoom * 1.15));
				m_b -= (my - getHeight() / 2) * (1 / m_zoom - 1 / (m_zoom * 1.15));
				m_zoom *= 1.15;
			} else {
				m_zoom /= 1.15;
				m_a -= (mx - getWidth() / 2) * (1 / (m_zoom * 1.15) - 1 / m_zoom);
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

	@Override
	public double getCameraX() {
		return m_a;
	}

	@Override
	public double getCameraY() {
		return m_b;
	}

	@Override
	public double getZoom() {
		return m_zoom;
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		activityListener.mouseDragged(e);
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		if (!mouseInBounds && getBounds().contains(e.getPoint()) && !activityListener.isEditing()) {
			mouseEntered(e);
		}
		activityListener.mouseMoved(e);
	}
}