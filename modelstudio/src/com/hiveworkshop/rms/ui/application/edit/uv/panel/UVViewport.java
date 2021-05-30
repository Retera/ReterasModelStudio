package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.CursorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.BasicCoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.UVViewportModelRenderer;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditorChangeListener;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class UVViewport extends JPanel implements CoordinateSystem, ViewportView, TVertexEditorChangeListener {
	ArrayList<Image> backgrounds = new ArrayList<>();
	double m_a = 0;
	double m_b = 0;
	double m_zoom = 1;
	double aspectRatio = 1;
	Point lastClick;
	Point selectStart;
	Point actStart;
	Timer clickTimer = new Timer(16, e -> clickTimer());
	boolean mouseInBounds = false;
	JPopupMenu contextMenu;
	JMenuItem placeholderButton;
	UVPanel parent;
	Component boxX, boxY;
	private final ProgramPreferences programPreferences;
	private final UVViewportModelRenderer viewportModelRenderer;
	private final ModelView modelView;
	private final ViewportActivity activityListener;
	private final CoordDisplayListener coordDisplayListener;
	private final CursorManager cursorManager;
	private Point lastMouseMotion = new Point(0, 0);
	private TVertexEditor editor;

	CoordinateSystem coordinateSystem;

	public UVViewport(final ModelView modelView, final UVPanel parent, final ProgramPreferences programPreferences, final ViewportActivity viewportActivity, final CoordDisplayListener coordDisplayListener, final TVertexEditor editor) {
		this.modelView = modelView;
		this.programPreferences = programPreferences;
		activityListener = viewportActivity;
		this.coordDisplayListener = coordDisplayListener;
		this.editor = editor;
		// Dimension 1 and Dimension 2, these specify which dimensions to display.
		// the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z dimensions

		// Viewport border
		setBorder(BorderFactory.createBevelBorder(1));
		setBackground(programPreferences.getBackgroundColor());
		setMinimumSize(new Dimension(400, 400));
		add(boxX = Box.createHorizontalStrut(400));
		add(boxY = Box.createVerticalStrut(400));
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		coordinateSystem = this;
		MouseAdapter mouseAdapter = getMouseAdapter();
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
		addMouseWheelListener(mouseAdapter);

		contextMenu = new JPopupMenu();
		placeholderButton = new JMenuItem("Placeholder Button");
		contextMenu.add(placeholderButton);

		this.parent = parent;

		viewportModelRenderer = new UVViewportModelRenderer();
		cursorManager = UVViewport.this::setCursor;
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
		m_zoom *= 1 + amount;
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
		if (programPreferences.show2dGrid()) {
			final Point2D.Double cameraOrigin = new Point2D.Double(viewX(0), viewY(0));

			drawGrid(g, cameraOrigin);
		}
		PaintBackgroundImage(g);
		final Graphics2D graphics2d = (Graphics2D) g;
//		dispMDL.drawGeosets(g, this, vertexSize);
		viewportModelRenderer.reset(graphics2d, programPreferences, this, coordinateSystem, modelView);
		modelView.visitMesh(viewportModelRenderer);
		activityListener.renderStatic(graphics2d, coordinateSystem);
	}

	private void PaintBackgroundImage(Graphics g) {
		for (Image background : backgrounds) {
			if (parent.wrapImage.isSelected()) {
				final double geomMinX = geomX(0);
				final double geomMinY = geomY(0);
				final double geomMaxX = geomX(getWidth());
				final double geomMaxY = geomY(getHeight());
				final int minX = (int) Math.floor(geomMinX);
				final int minY = (int) Math.floor(geomMinY);
				final int maxX = (int) Math.ceil(geomMaxX);
				final int maxY = (int) Math.ceil(geomMaxY);
				for (int y = minY; y < maxY; y++) {
					for (int x = minX; x < maxX; x++) {
						g.drawImage(background, (int) viewX(x), (int) viewY(y), (int) (viewX(x + 1) - viewX(x)), (int) (viewY(y + 1) - viewY(y)), null);
					}
				}
			} else {
				g.drawImage(background, (int) viewX(0), (int) viewY(0), (int) (viewX(1) - viewX(0)), (int) (viewY(1) - viewY(0)), null);
			}
		}
	}

	private void drawGrid(Graphics g, Point2D.Double cameraOrigin) {
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

	private void drawXLines(Graphics g, Point2D.Double cameraOrigin, float increment) {
		for (float x = 0; ((cameraOrigin.x + x) < getWidth()) || ((cameraOrigin.x - x) >= 0); x += increment) {
			g.drawLine((int) (cameraOrigin.x + x), 0, (int) (cameraOrigin.x + x), getHeight());
			g.drawLine((int) (cameraOrigin.x - x), 0, (int) (cameraOrigin.x - x), getHeight());
		}
	}

	private void drawYLines(Graphics g, Point2D.Double cameraOrigin, float increment) {
		for (float y = 0; ((cameraOrigin.y + y) < getHeight()) || ((cameraOrigin.y - y) >= 0); y += increment) {
			g.drawLine(0, (int) (cameraOrigin.y + y), getWidth(), (int) (cameraOrigin.y + y));
			g.drawLine(0, (int) (cameraOrigin.y - y), getWidth(), (int) (cameraOrigin.y - y));
		}
	}

	@Override
	public double viewX(final double x) {
		return ((x + m_a) * m_zoom * aspectRatio) + (getWidth() / 2.0);
	}

	@Override
	public double viewY(final double y) {
		return ((y + m_b) * m_zoom) + (getHeight() / 2.0);
	}

	@Override
	public double geomX(final double x) {
		return ((x - (getWidth() / 2.0)) / aspectRatio / m_zoom) - m_a;
	}

	@Override
	public double geomY(final double y) {
		return ((y - (getHeight() / 2.0)) / m_zoom) - m_b;
	}

	private void clickTimer() {
		int xoff = 0;
		int yoff = 0;
		Component temp = this;
		while (temp != null) {
			xoff += temp.getX();
			yoff += temp.getY();
			temp = temp.getParent();
		}
		final double mx = MouseInfo.getPointerInfo().getLocation().x - xoff;// MainFrame.frame.getX()-8);
		final double my = MouseInfo.getPointerInfo().getLocation().y - yoff;// MainFrame.frame.getY()-30);
		// JOptionPane.showMessageDialog(null,mx+","+my+" as mouse,
		// "+lastClick.x+","+lastClick.y+" as last.");
		// System.out.println(xoff+" and "+mx);
		if (lastClick != null) {

			m_a += ((int) mx - lastClick.x) / aspectRatio / m_zoom;
			m_b += ((int) my - lastClick.y) / m_zoom;
			lastClick.x = (int) mx;
			lastClick.y = (int) my;
		}
		parent.setMouseCoordDisplay(((mx - (getWidth() / 2.0)) / aspectRatio / m_zoom) - m_a, ((my - (getHeight() / 2.0)) / m_zoom) - m_b);

		repaint();
	}

	private MouseAdapter getMouseAdapter() {
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
					requestFocus();
					activityListener.mousePressed(e, coordinateSystem);
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					activityListener.viewportChanged(cursorManager);
					requestFocus();
					activityListener.mousePressed(e, coordinateSystem);
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if (!mouseInBounds && (selectStart == null) && (actStart == null) && (lastClick == null)) {
					clickTimer.stop();
					repaint();
				}
				// MainFrame.panel.refreshUndo();
				// TODO fix, refresh undo
				if ((e.getButton() == MouseEvent.BUTTON2) && (lastClick != null)) {
					m_a += (e.getX() - lastClick.x) / m_zoom;
					m_b += (e.getY() - lastClick.y) / m_zoom;
					lastClick = null;
				} else if (e.getButton() == MouseEvent.BUTTON1/* && selectStart != null */) {
					activityListener.mouseReleased(e, coordinateSystem);
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
					activityListener.mouseReleased(e, coordinateSystem);
				}
				if (!mouseInBounds && (selectStart == null) && (actStart == null) && (lastClick == null)) {
					clickTimer.stop();
					repaint();
				}
				// MainFrame.panel.refreshUndo();
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
					contextMenu.show(parent, e.getX(), e.getY());
					// }
				}
			}

			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				int wr = e.getWheelRotation();
				final boolean neg = wr < 0;

				final double mx = e.getX();
				final double my = e.getY();

				if (neg) {
					wr = -wr;
				}
				for (int i = 0; i < wr; i++) {
					if (neg) {
						m_a -= ((mx - (getWidth() / 2.0)) / aspectRatio) * ((1 / m_zoom) - (1 / (m_zoom * 1.15)));
						m_b -= (my - (getHeight() / 2.0)) * ((1 / m_zoom) - (1 / (m_zoom * 1.15)));
						m_zoom *= 1.15;
					} else {
						m_zoom /= 1.15;
						m_a -= ((mx - (getWidth() / 2.0)) / aspectRatio) * ((1 / (m_zoom * 1.15)) - (1 / m_zoom));
						m_b -= (my - (getHeight() / 2.0)) * ((1 / (m_zoom * 1.15)) - (1 / m_zoom));
					}
				}
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				activityListener.mouseDragged(e, coordinateSystem);
				lastMouseMotion = e.getPoint();
				repaint();
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



	public Rectangle2D.Double pointsToGeomRect(final Point a, final Point b) {
		final Point2D.Double topLeft = new Point2D.Double(Math.min(geomX(a.x), geomX(b.x)), Math.min(geomY(a.y), geomY(b.y)));
		final Point2D.Double lowRight = new Point2D.Double(Math.max(geomX(a.x), geomX(b.x)), Math.max(geomY(a.y), geomY(b.y)));
		return new Rectangle2D.Double(topLeft.x, topLeft.y, lowRight.x - topLeft.x, lowRight.y - topLeft.y);
	}

	public Rectangle2D.Double pointsToRect(final Point a, final Point b) {
		final Point2D.Double topLeft = new Point2D.Double(Math.min(a.x, b.x), Math.min(a.y, b.y));
		final Point2D.Double lowRight = new Point2D.Double(Math.max(a.x, b.x), Math.max(a.y, b.y));
		return new Rectangle2D.Double(topLeft.x, topLeft.y, lowRight.x - topLeft.x, lowRight.y - topLeft.y);
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

	@Override
	public byte getPortFirstXYZ() {
		return 0;
	}

	@Override
	public byte getPortSecondXYZ() {
		return 1;
	}

	@Override
	public CoordinateSystem copy() {
		return new BasicCoordinateSystem((byte) 0, (byte) 1, m_a, m_b, m_zoom, getWidth(), getHeight());
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
	public void editorChanged(final TVertexEditor newModelEditor) {
		editor = newModelEditor;
	}
}