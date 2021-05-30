package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.visitor.GeosetVisitor;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.CursorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.BasicCoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.AnimatedViewportModelRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.ResettableAnimatedIdObjectParentLinkRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;
import net.infonode.docking.View;
import net.infonode.docking.title.DockingWindowTitleProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Viewport extends JPanel implements CoordinateSystem, ViewportView, ModelEditorChangeListener {
	byte m_d1;
	byte m_d2;
	double m_a = 0;
	double m_b = 0;
	double m_zoom = 1;
	Point lastClick;
	Point selectStart;
	Point actStart;
	Timer clickTimer = new Timer(16, e -> clickTimer());
	Timer paintTimer;
	boolean mouseInBounds = false;
	JPopupMenu contextMenu;

	private final ViewportModelRenderer viewportModelRenderer;
	private final AnimatedViewportModelRenderer animatedViewportModelRenderer;
	private final ResettableAnimatedIdObjectParentLinkRenderer linkRenderer;
	private final ViewportActivity activityListener;
	private final CursorManager cursorManager;
	private final UndoActionListener undoListener;
	private final ProgramPreferences programPreferences;
	private final CoordDisplayListener coordDisplayListener;
	private final ModelView modelView;
	private final UndoHandler undoHandler;
	private ModelEditorManager modelEditorManager;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private Point lastMouseMotion = new Point(0, 0);
	private final RenderModel renderModel;
	private final ModelVisitorImplementation linkRenderingVisitorAdapter;
	private final Vec3 facingVector;
	private final ViewportListener viewportListener;
	private View view;

	long totTempRenderTime;
	long renderCount;

	CoordinateSystem coordinateSystem;
	Viewport viewport;
	JPanel thisPanel;

	public Viewport(byte d1, byte d2, ModelView modelView, ProgramPreferences programPreferences, ViewportActivity activityListener, ModelStructureChangeListener modelStructureChangeListener, UndoActionListener undoListener, CoordDisplayListener coordDisplayListener, UndoHandler undoHandler, ModelEditorManager modelEditorManager, ViewportTransferHandler viewportTransferHandler, RenderModel renderModel, ViewportListener viewportListener) {
		// Dimension 1 and Dimension 2, these specify which dimensions to display.
		// the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z dimensions
		m_d1 = d1;
		m_d2 = d2;
		this.modelView = modelView;
		this.programPreferences = programPreferences;
		this.activityListener = activityListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.modelEditorManager = modelEditorManager;
		this.undoListener = undoListener;
		this.coordDisplayListener = coordDisplayListener;
		this.undoHandler = undoHandler;
		this.renderModel = renderModel;
		this.viewportListener = viewportListener;
		cursorManager = Viewport.this::setCursor;
		setupCopyPaste(viewportTransferHandler);
		// Viewport border
		setBorder(BorderFactory.createBevelBorder(1));
		setupViewportBackground(programPreferences);
		programPreferences.addChangeListener(() -> setupViewportBackground(programPreferences));
		setMinimumSize(new Dimension(200, 200));
		add(Box.createHorizontalStrut(200));
		add(Box.createVerticalStrut(200));
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		addMouseListener(getMouseAdapter());
		addMouseWheelListener(getMouseAdapter());
		addMouseMotionListener(getMouseAdapter());

		coordinateSystem = this;
		viewport = this;
		thisPanel = this;

		contextMenu = new ViewportPopupMenu(this, this.undoListener, this.modelEditorManager, this.modelView);
		add(contextMenu);

		viewportModelRenderer = new ViewportModelRenderer(programPreferences.getVertexSize());
		animatedViewportModelRenderer = new AnimatedViewportModelRenderer(programPreferences.getVertexSize());
		linkRenderer = new ResettableAnimatedIdObjectParentLinkRenderer(programPreferences.getVertexSize());
		linkRenderingVisitorAdapter = new ModelVisitorImplementation();

		facingVector = new Vec3(0, 0, 0);
		final byte unusedXYZ = CoordinateSystem.Util.getUnusedXYZ(this);
		facingVector.setCoord(unusedXYZ, unusedXYZ == 0 ? 1 : -1);
		paintTimer = new Timer(16, e -> {
			repaint();
			if (!isShowing()) {
				paintTimer.stop();
			}
		});
		paintTimer.start();
	}

	public void setView(View view) {
		this.view = view;
	}

	public void setupViewportBackground(ProgramPreferences programPreferences) {
		// if (programPreferences.isInvertedDisplay()) {
		// setBackground(Color.DARK_GRAY.darker());
		// } else {setBackground(new Color(255, 255, 255));}
		setBackground(programPreferences.getBackgroundColor());
	}

	private void setupCopyPaste(ViewportTransferHandler viewportTransferHandler) {
		setTransferHandler(viewportTransferHandler);
		ActionMap map = getActionMap();
		map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
		map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
		map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
		setFocusable(true);
	}

	public void setPosition(double a, double b) {
		m_a = a;
		m_b = b;
	}

	public void translate(double a, double b) {
		m_a += a;
		m_b += b;
	}

	public void zoom(double amount) {
		m_zoom *= 1 + amount;
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
		BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		paintComponent(image.getGraphics(), 5);
		return image;
	}

	@Override
	public void paintComponent(Graphics g) {
		paintComponent(g, 1);
	}

	private static void addMenuItem(String itemText, ActionListener actionListener, JMenu menu) {
		JMenuItem menuItem = new JMenuItem(itemText);
		menuItem.addActionListener(actionListener);
		menu.add(menuItem);
	}

	public void paintComponent(Graphics g, int vertexSize) {
		super.paintComponent(g);
		long renderStart = System.nanoTime();
		if (programPreferences.show2dGrid()) {
			drawGrid(g);
		}
		Graphics2D graphics2d = (Graphics2D) g;

		// dispMDL.drawGeosets(g, this, 1);
		// dispMDL.drawPivots(g, this, 1);
		// dispMDL.drawCameras(g, this, 1);
		if (modelEditorManager.getModelEditor().editorWantsAnimation()) {
			Stroke stroke = graphics2d.getStroke();
			graphics2d.setStroke(new BasicStroke(3));
			renderModel.updateNodes(false);
			linkRenderer.reset(this, graphics2d, NodeIconPalette.HIGHLIGHT, renderModel);
			modelView.visit(linkRenderingVisitorAdapter);
			graphics2d.setStroke(stroke);
			animatedViewportModelRenderer.reset(graphics2d, programPreferences, m_d1, m_d2, this, this, modelView, renderModel);
			modelView.visit(animatedViewportModelRenderer);
			activityListener.render(graphics2d, this, renderModel);
		} else {
			viewportModelRenderer.reset(graphics2d, programPreferences, m_d1, m_d2, this, this, modelView);
			modelView.visit(viewportModelRenderer);
			activityListener.renderStatic(graphics2d, this);
		}

		getColor(g, m_d1);
		g.drawLine((int) Math.round(viewX(0)), (int) Math.round(viewY(0)), (int) Math.round(viewX(5)), (int) Math.round(viewY(0)));

		getColor(g, m_d2);
		g.drawLine((int) Math.round(viewX(0)), (int) Math.round(viewY(0)), (int) Math.round(viewX(0)), (int) Math.round(viewY(5)));


		adjustAndRunPaintTimer(renderStart);
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

	public void adjustAndRunPaintTimer(long renderStart) {
		long renderEnd = System.nanoTime();
		long currFrameRenderTime = renderEnd - renderStart;

//		minRenderTime = Math.min(currFrameRenderTime, minRenderTime);
//		maxRenderTime = Math.max(currFrameRenderTime, maxRenderTime);
//		totTempRenderTime += currFrameRenderTime;
//		renderCount += 1;
//		if (renderCount >= 100) {
////			long millis = ((totTempRenderTime / renderCount) / 1000000L) + 1;
//			long millis = ((totTempRenderTime/1000000L) / renderCount);
//			System.out.println("millis: " + millis);
//			if (millis > paintTimer.getDelay()) {
//				int millis2 = (int) (millis * 5);
//				System.out.println("min, delay=" + millis2);
//				paintTimer.setDelay(millis2);
//			} else if (millis < paintTimer.getDelay()) {
//				int max2 = Math.max(16, (int) (millis * 5));
//				System.out.println("max, delay=" + max2);
//				paintTimer.setDelay(max2);
//			}
//			System.out.println("min render time: " + (minRenderTime/1000000L) + "ms, max render time: " + (maxRenderTime/1000000L) + "ms");
//			minRenderTime = Long.MAX_VALUE;
//			maxRenderTime = 0;
//		}

		totTempRenderTime += currFrameRenderTime;
		renderCount += 1;
		if (renderCount >= 100) {
			long millis = ((totTempRenderTime / 1000000L) / renderCount) + 1;
			paintTimer.setDelay(Math.max(16, (int) (millis * 5)));
//			System.out.println("delay: " + paintTimer.getDelay());

			totTempRenderTime = 0;
			renderCount = 0;
		}
		boolean showing = isShowing();
		boolean running = paintTimer.isRunning();
		if (showing && !running) {
			paintTimer.start();
		} else if (!showing && running) {
			paintTimer.stop();
		}
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

	private void getColor(Graphics g, byte dir) {
		switch (dir) {
			case 0 -> g.setColor(new Color(0, 255, 0));
			case 1 -> g.setColor(new Color(255, 0, 0));
			case 2 -> g.setColor(new Color(0, 0, 255));
		}
	}

	@Override
	public double viewX(double x) {
		return ((x + m_a) * m_zoom) + (getWidth() / 2.0);
	}

	@Override
	public double viewY(double y) {
		return ((-y + m_b) * m_zoom) + (getHeight() / 2.0);
	}

	@Override
	public double geomX(double x) {
		return ((x - (getWidth() / 2.0)) / m_zoom) - m_a;
	}

	@Override
	public double geomY(double y) {
		return -(((y - (getHeight() / 2.0)) / m_zoom) - m_b);
	}

	private boolean clickTimer() {
		int xoff = 0;
		int yoff = 0;
		Component temp = this;
		while (temp != null) {
			xoff += temp.getX();
			yoff += temp.getY();
			// if( temp.getClass() == ModelPanel.class ){
			//// temp = MainFrame.panel;
			// temp = null; // TODO
			// }else{
			temp = temp.getParent();
			// }
		}
		PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		if ((pointerInfo == null) || (pointerInfo.getLocation() == null)) {
			return true;
		}
		double mx = pointerInfo.getLocation().x - xoff;
		double my = pointerInfo.getLocation().y - yoff;
		// JOptionPane.showMessageDialog(null,mx+","+my+" as mouse,
		// "+lastClick.x+","+lastClick.y+" as last.");
		// System.out.println(xoff+" and "+mx);
		if (lastClick != null) {

			m_a += ((int) mx - lastClick.x) / m_zoom;
			m_b += ((int) my - lastClick.y) / m_zoom;
			lastClick.x = (int) mx;
			lastClick.y = (int) my;
		}
		coordDisplayListener.notifyUpdate(m_d1, m_d2, ((mx - (getWidth() / 2.0)) / m_zoom) - m_a,
				-(((my - (getHeight() / 2.0)) / m_zoom) - m_b));
		// MainFrame.panel.setMouseCoordDisplay(m_d1,m_d2,((mx-getWidth()/2)/m_zoom)-m_a,-(((my-getHeight()/2)/m_zoom)-m_b));
		// TODO update mouse coord display could be used still

		// if (actStart != null) {
		// Point actEnd = new Point((int) mx, (int) my);
		// Point2D.Double convertedStart = new
		// Point2D.Double(geomX(actStart.x), geomY(actStart.y));
		// Point2D.Double convertedEnd = new
		// Point2D.Double(geomX(actEnd.x), geomY(actEnd.y));
		// dispMDL.updateAction(convertedStart, convertedEnd, m_d1, m_d2);
		// actStart = actEnd;
		// }
//				repaint();
		return false;
	}

	private MouseAdapter getMouseAdapter() {
		return new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				if (!activityListener.isEditing()) {
					activityListener.viewportChanged(cursorManager);
					viewportListener.viewportChanged(viewport);
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
					// selectStart = new Point(e.getX(), e.getY());
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					activityListener.viewportChanged(cursorManager);
					viewportListener.viewportChanged(viewport);
					requestFocus();
					activityListener.mousePressed(e, coordinateSystem);
					// actStart = new Point(e.getX(), e.getY());
					// final Point2D.Double convertedStart = new
					// Point2D.Double(geomX(actStart.x), geomY(actStart.y));
					// dispMDL.startAction(convertedStart, m_d1, m_d2,
					// dispMDL.getProgramPreferences().currentActionType());
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if ((e.getButton() == MouseEvent.BUTTON2) && (lastClick != null)) {
					m_a += (e.getX() - lastClick.x) / m_zoom;
					m_b += (e.getY() - lastClick.y) / m_zoom;
					lastClick = null;
				} else if (e.getButton() == MouseEvent.BUTTON1/* && selectStart != null */) {
					activityListener.mouseReleased(e, coordinateSystem);
					// final Point selectEnd = new Point(e.getX(), e.getY());
					// final Rectangle2D.Double area = pointsToGeomRect(selectStart,selectEnd);
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
				}
				undoHandler.refreshUndo();
				if (mouseInBounds && !getBounds().contains(e.getPoint()) && !activityListener.isEditing()) {
					mouseExited(e);
				}
			}

			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					contextMenu.show(thisPanel, e.getX(), e.getY());
				}
			}

			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				int wr = e.getWheelRotation();

				int dir = wr < 0 ? -1 : 1;

				double mx = e.getX();
				double my = e.getY();

				for (int i = 0; i < Math.abs(wr); i++) {
					double zoomAmount = (1 / m_zoom - 1 / (m_zoom * 1.15)) * dir;

					double w = mx - (getWidth() / 2.0);
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
			}

			@Override
			public void mouseMoved(final MouseEvent e) {
				if (!mouseInBounds && getBounds().contains(e.getPoint()) && !activityListener.isEditing()) {
					mouseEntered(e);
				}
				activityListener.mouseMoved(e, coordinateSystem);
				lastMouseMotion = e.getPoint();
			}
		};
	}

	public Rectangle2D.Double pointsToGeomRect(Point a, Point b) {
		Point2D.Double topLeft = new Point2D.Double(Math.min(geomX(a.x), geomX(b.x)), Math.min(geomY(a.y), geomY(b.y)));
		Point2D.Double lowRight = new Point2D.Double(Math.max(geomX(a.x), geomX(b.x)), Math.max(geomY(a.y), geomY(b.y)));
		return new Rectangle2D.Double(topLeft.x, topLeft.y, lowRight.x - topLeft.x, lowRight.y - topLeft.y);
	}

	public Rectangle2D.Double pointsToRect(Point a, Point b) {
		Point2D.Double topLeft = new Point2D.Double(Math.min(a.x, b.x), Math.min(a.y, b.y));
		Point2D.Double lowRight = new Point2D.Double(Math.max(a.x, b.x), Math.max(a.y, b.y));
		return new Rectangle2D.Double(topLeft.x, topLeft.y, lowRight.x - topLeft.x, lowRight.y - topLeft.y);
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
	public CoordinateSystem copy() {
		return new BasicCoordinateSystem(m_d1, m_d2, m_a, m_b, m_zoom, getWidth(), getHeight());
	}

	@Override
	public void modelEditorChanged(ModelEditor newModelEditor) {
//		modelEditorManager = newModelEditor;
		// TODO call from display panel and above
	}

	public void setViewportAxises(String name, byte dim1, byte dim2) {
		view.getViewProperties().setTitle(name);
		m_d1 = dim1;
		m_d2 = dim2;
	}

	private class ChangeViewportAxisAction implements ActionListener {
		private final String name;
		private final byte dim1;
		private final byte dim2;

		public ChangeViewportAxisAction(String name, byte dim1, byte dim2) {
			this.name = name;
			this.dim1 = dim1;
			this.dim2 = dim2;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			changeViewportAxis1();
		}

		private void changeViewportAxis1() {
			DockingWindowTitleProvider titleProvider = view.getWindowProperties().getTitleProvider();
			view.getViewProperties().setTitle(name);
			view.getWindowProperties().setTitleProvider(titleProvider);
			m_d1 = dim1;
			m_d2 = dim2;
		}
	}

	public ModelEditorManager getModelEditorManager() {
		return modelEditorManager;
	}

	public static class DropLocation extends TransferHandler.DropLocation {
		protected DropLocation(Point dropPoint) {
			super(dropPoint);
		}
	}

	public ModelView getModelView() {
		return modelView;
	}

	public Point getLastMouseMotion() {
		return lastMouseMotion;
	}

	public ModelStructureChangeListener getModelStructureChangeListener() {
		return modelStructureChangeListener;
	}

	private final class ModelVisitorImplementation implements ModelVisitor {
		@Override
		public void ribbonEmitter(RibbonEmitter particleEmitter) {
			linkRenderer.ribbonEmitter(particleEmitter);
		}

		@Override
		public void particleEmitter2(ParticleEmitter2 particleEmitter) {
			linkRenderer.particleEmitter2(particleEmitter);
		}

		@Override
		public void particleEmitter(ParticleEmitter particleEmitter) {
			linkRenderer.particleEmitter(particleEmitter);
		}

		@Override
		public void popcornFxEmitter(ParticleEmitterPopcorn popcornFxEmitter) {
			linkRenderer.popcornFxEmitter(popcornFxEmitter);
		}

		@Override
		public void light(Light light) {
			linkRenderer.light(light);
		}

		@Override
		public void helper(Helper object) {
			linkRenderer.helper(object);
		}

		@Override
		public void eventObject(EventObject eventObject) {
			linkRenderer.eventObject(eventObject);
		}

		@Override
		public void collisionShape(CollisionShape collisionShape) {
			linkRenderer.collisionShape(collisionShape);
		}

		@Override
		public void camera(Camera camera) {
		}

		@Override
		public void bone(Bone object) {
			linkRenderer.bone(object);
		}

		@Override
		public void attachment(Attachment attachment) {
			linkRenderer.attachment(attachment);
		}

		@Override
		public GeosetVisitor beginGeoset(int geosetId, Material material, GeosetAnim geosetAnim) {
			return GeosetVisitor.NO_ACTION;
		}
	}

	public Vec3 getFacingVector() {
		return facingVector;
	}

}