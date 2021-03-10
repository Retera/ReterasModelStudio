package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.visitor.GeosetVisitor;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.CursorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.BasicCoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.AnimatedViewportModelRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.ResettableAnimatedIdObjectParentLinkRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.MatrixPopup;
import com.hiveworkshop.rms.ui.gui.modeledit.SkinPopup;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.InfoPopup;
import com.hiveworkshop.rms.util.Vec3;
import net.infonode.docking.View;
import net.infonode.docking.title.DockingWindowTitleProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class Viewport extends JPanel
		implements MouseListener, MouseWheelListener, CoordinateSystem, ViewportView, MouseMotionListener, ModelEditorChangeListener {
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
	private ModelEditor modelEditor;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private Point lastMouseMotion = new Point(0, 0);
	private final RenderModel renderModel;
	private final ModelVisitorImplementation linkRenderingVisitorAdapter;
	private final Vec3 facingVector;
	private final ViewportListener viewportListener;
	private View view;

	long totTempRenderTime;
	long renderCount;

	public Viewport(final byte d1, final byte d2, final ModelView modelView, final ProgramPreferences programPreferences, final ViewportActivity activityListener, final ModelStructureChangeListener modelStructureChangeListener, final UndoActionListener undoListener, final CoordDisplayListener coordDisplayListener, final UndoHandler undoHandler, final ModelEditor modelEditor, final ViewportTransferHandler viewportTransferHandler, final RenderModel renderModel, final ViewportListener viewportListener) {
		// Dimension 1 and Dimension 2, these specify which dimensions to display.
		// the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z dimensions
		m_d1 = d1;
		m_d2 = d2;
		this.modelView = modelView;
		this.programPreferences = programPreferences;
		this.activityListener = activityListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.modelEditor = modelEditor;
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
		addMouseListener(this);
		addMouseWheelListener(this);
		addMouseMotionListener(this);

		createViewPortMenu(undoListener);

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

	public void setupViewportBackground(final ProgramPreferences programPreferences) {
		// if (programPreferences.isInvertedDisplay()) {
		// setBackground(Color.DARK_GRAY.darker());
		// } else {setBackground(new Color(255, 255, 255));}
		setBackground(programPreferences.getBackgroundColor());
	}

	private void setupCopyPaste(final ViewportTransferHandler viewportTransferHandler) {
		setTransferHandler(viewportTransferHandler);
		final ActionMap map = getActionMap();
		map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
		map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
		map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
		setFocusable(true);
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
		final BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		paintComponent(image.getGraphics(), 5);
		return image;
	}

	@Override
	public void paintComponent(final Graphics g) {
		paintComponent(g, 1);
	}

	private static void addMenuItem(String itemText, ActionListener actionListener, JMenu menu) {
		JMenuItem menuItem = new JMenuItem(itemText);
		menuItem.addActionListener(actionListener);
		menu.add(menuItem);
	}

	private void createViewPortMenu(UndoActionListener undoListener) {
		contextMenu = new JPopupMenu();

		JMenu viewMenu = new JMenu("View");
		contextMenu.add(viewMenu);

		addMenuItem("Front", new ChangeViewportAxisAction("Front", (byte) 1, (byte) 2), viewMenu);
		addMenuItem("Back", new ChangeViewportAxisAction("Back", (byte) -2, (byte) 2), viewMenu);
		addMenuItem("Top", new ChangeViewportAxisAction("Top", (byte) 1, (byte) -1), viewMenu);
		addMenuItem("Bottom", new ChangeViewportAxisAction("Bottom", (byte) 1, (byte) 0), viewMenu);
		addMenuItem("Left", new ChangeViewportAxisAction("Left", (byte) -1, (byte) 2), viewMenu);
		addMenuItem("Right", new ChangeViewportAxisAction("Right", (byte) 0, (byte) 2), viewMenu);

		JMenu meshMenu = new JMenu("Mesh");
		contextMenu.add(meshMenu);

		JMenuItem createFace = new JMenuItem("Create Face");
		createFace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
		createFace.addActionListener(e -> createFace());
		meshMenu.add(createFace);

		addMenuItem("Split Geoset and Add Team Color", e -> undoListener.pushAction(modelEditor.addTeamColor()), meshMenu);
		addMenuItem("Split Geoset", e -> undoListener.pushAction(modelEditor.splitGeoset()), meshMenu);

		JMenu editMenu = new JMenu("Edit");
		contextMenu.add(editMenu);

		addMenuItem("Translation Type-in", e -> manualMove(), editMenu);
		addMenuItem("Rotate Type-in", e -> manualRotate(), editMenu);
		addMenuItem("Position Type-in", e -> manualSet(), editMenu);
		addMenuItem("Scale Type-in", e -> manualScale(), editMenu);

		JMenu matrixMenu = new JMenu("Rig");
		contextMenu.add(matrixMenu);

		addMenuItem("Selected Mesh to Selected Nodes", e -> undoListener.pushAction(modelEditor.rig()), matrixMenu);
		addMenuItem("Re-assign Matrix", e -> reAssignMatrix(), matrixMenu);
		addMenuItem("View Matrix", e -> InfoPopup.show(this, modelEditor.getSelectedMatricesDescription()), matrixMenu);
		addMenuItem("Re-assign HD Skin", e -> reAssignSkinning(), matrixMenu);
		addMenuItem("View HD Skin", e -> InfoPopup.show(this, modelEditor.getSelectedHDSkinningDescription()), matrixMenu);

		JMenu nodeMenu = new JMenu("Node");
		contextMenu.add(nodeMenu);

		addMenuItem("Set Parent", e -> setParent(), nodeMenu);
		addMenuItem("Auto-Center Bone(s)", e -> undoListener.pushAction(modelEditor.autoCenterSelectedBones()), nodeMenu);
		addMenuItem("Rename Bone", e -> renameBone(), nodeMenu);
		addMenuItem("Append Bone Suffix", e -> appendBoneBone(), nodeMenu);

	}

	public void paintComponent(final Graphics g, final int vertexSize) {
		super.paintComponent(g);
		final long renderStart = System.nanoTime();
		if (programPreferences.show2dGrid()) {
			drawGrid(g);
		}
		final Graphics2D graphics2d = (Graphics2D) g;

		// dispMDL.drawGeosets(g, this, 1);
		// dispMDL.drawPivots(g, this, 1);
		// dispMDL.drawCameras(g, this, 1);
		if (modelEditor.editorWantsAnimation()) {
			final Stroke stroke = graphics2d.getStroke();
			graphics2d.setStroke(new BasicStroke(3));
			renderModel.updateNodes(true, false);
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
		g.drawLine((int) Math.round(convertX(0)), (int) Math.round(convertY(0)), (int) Math.round(convertX(5)), (int) Math.round(convertY(0)));

		getColor(g, m_d2);
		g.drawLine((int) Math.round(convertX(0)), (int) Math.round(convertY(0)), (int) Math.round(convertX(0)), (int) Math.round(convertY(5)));


		adjustAndRunPaintTimer(renderStart);
	}

	public void drawGrid(Graphics g) {
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
		final long renderEnd = System.nanoTime();
		final long currFrameRenderTime = renderEnd - renderStart;

//		minRenderTime = Math.min(currFrameRenderTime, minRenderTime);
//		maxRenderTime = Math.max(currFrameRenderTime, maxRenderTime);
//		totTempRenderTime += currFrameRenderTime;
//		renderCount += 1;
//		if (renderCount >= 100) {
////			final long millis = ((totTempRenderTime / renderCount) / 1000000L) + 1;
//			final long millis = ((totTempRenderTime/1000000L) / renderCount);
//			System.out.println("millis: " + millis);
//			if (millis > paintTimer.getDelay()) {
//				final int millis2 = (int) (millis * 5);
//				System.out.println("min, delay=" + millis2);
//				paintTimer.setDelay(millis2);
//			} else if (millis < paintTimer.getDelay()) {
//				final int max2 = Math.max(16, (int) (millis * 5));
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
			final long millis = ((totTempRenderTime / 1000000L) / renderCount) + 1;
			paintTimer.setDelay(Math.max(16, (int) (millis * 5)));
//			System.out.println("delay: " + paintTimer.getDelay());

			totTempRenderTime = 0;
			renderCount = 0;
		}
		final boolean showing = isShowing();
		final boolean running = paintTimer.isRunning();
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

	private void getColor(Graphics g, byte m_d2) {
		switch (m_d2) {
			case 0 -> g.setColor(new Color(0, 255, 0));
			case 1 -> g.setColor(new Color(255, 0, 0));
			case 2 -> g.setColor(new Color(0, 0, 255));
		}
	}

	@Override
	public double convertX(final double x) {
		return ((x + m_a) * m_zoom) + (getWidth() / 2.0);
	}

	@Override
	public double convertY(final double y) {
		return ((-y + m_b) * m_zoom) + (getHeight() / 2.0);
	}

	@Override
	public double geomX(final double x) {
		return ((x - (getWidth() / 2.0)) / m_zoom) - m_a;
	}

	@Override
	public double geomY(final double y) {
		return -(((y - (getHeight() / 2.0)) / m_zoom) - m_b);
	}

	private void createFace() {
		try {
			undoListener.pushAction(modelEditor.createFaceFromSelection(facingVector));
		} catch (final FaceCreationException exc) {
			JOptionPane.showMessageDialog(this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
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
		final PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		if ((pointerInfo == null) || (pointerInfo.getLocation() == null)) {
			return true;
		}
		final double mx = pointerInfo.getLocation().x - xoff;
		final double my = pointerInfo.getLocation().y - yoff;
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
		// final Point actEnd = new Point((int) mx, (int) my);
		// final Point2D.Double convertedStart = new
		// Point2D.Double(geomX(actStart.x), geomY(actStart.y));
		// final Point2D.Double convertedEnd = new
		// Point2D.Double(geomX(actEnd.x), geomY(actEnd.y));
		// dispMDL.updateAction(convertedStart, convertedEnd, m_d1, m_d2);
		// actStart = actEnd;
		// }
//				repaint();
		return false;
	}

	private void reAssignMatrix() {
		final MatrixPopup matrixPopup = new MatrixPopup(modelView.getModel());
		final String[] words = { "Accept", "Cancel" };
		final int i = JOptionPane.showOptionDialog(this, matrixPopup, "Rebuild Matrix", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, words, words[1]);
		if (i == 0) {
			UndoAction reassignMatrixAction = modelEditor.setMatrix(matrixPopup.getNewBoneList());
			undoListener.pushAction(reassignMatrixAction);
		}
	}

	private void reAssignSkinning() {
		SkinPopup skinPopup = new SkinPopup(modelView);
		final String[] words = { "Accept", "Cancel" };
		final int i = JOptionPane.showOptionDialog(this, skinPopup, "Rebuild Skin", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, words, words[1]);
		if (i == 0) {
			undoListener.pushAction(modelEditor.setHDSkinning(skinPopup.getBones(), skinPopup.getSkinWeights()));
		}
	}

	private void appendBoneBone() {
		final String name = JOptionPane.showInputDialog(this, "Enter bone suffix:");
		if (name != null) {
			modelEditor.addSelectedBoneSuffix(name);
		}
	}

	private void renameBone() {
		final String name = JOptionPane.showInputDialog(this, "Enter bone name:");
		if (name != null) {
			modelEditor.setSelectedBoneName(name);
		}
	}

	private void setParent() {
		class NodeShell {
			final IdObject node;

			public NodeShell(final IdObject node) {
				this.node = node;
			}

			public IdObject getNode() {
				return node;
			}

			@Override
			public String toString() {
				if (node == null) {
					return "(No parent)";
				}
				return node.getName();
			}
		}

		final List<IdObject> idObjects = modelView.getModel().getIdObjects();
		final NodeShell[] nodeOptions = new NodeShell[idObjects.size() + 1];
		nodeOptions[0] = new NodeShell(null);
		final NodeShell defaultChoice = nodeOptions[0];
		for (int i = 0; i < idObjects.size(); i++) {
			final IdObject node = idObjects.get(i);
			nodeOptions[i + 1] = new NodeShell(node);
		}
		final NodeShell result = (NodeShell) JOptionPane.showInputDialog(this, "Choose a parent node", "Set Parent Node", JOptionPane.PLAIN_MESSAGE, null, nodeOptions, defaultChoice);
		final MatrixPopup matrixPopup = new MatrixPopup(modelView.getModel());
		if (result != null) {
			// JOptionPane.showMessageDialog(null,"action approved");
			modelEditor.setParent(result.getNode());
		}
	}

	private void manualMove() {
		final JPanel inputPanel = new JPanel();
		final GridLayout layout = new GridLayout(6, 1);
		inputPanel.setLayout(layout);
		final JSpinner[] spinners = getLabeledSpinnerArray(inputPanel, "Move X:", 0.0, "Move Y:", 0.0, "Move Z:", 0.0);
		final int x = JOptionPane.showConfirmDialog(getRootPane(), inputPanel, "Manual Translation", JOptionPane.OK_CANCEL_OPTION);
		if (x != JOptionPane.OK_OPTION) {
			return;
		}
		final double deltaX = ((Number) spinners[0].getValue()).doubleValue();
		final double deltaY = ((Number) spinners[1].getValue()).doubleValue();
		final double deltaZ = ((Number) spinners[2].getValue()).doubleValue();
		final UndoAction translate = modelEditor.translate(deltaX, deltaY, deltaZ);
		undoListener.pushAction(translate);
	}

	private JSpinner[] getLabeledSpinnerArray(JPanel panel, String labelX, double xValue, String labelY, double yValue, String labelZ, double zValue) {
		final JSpinner[] spinners = new JSpinner[3];
		panel.add(new JLabel(labelX));
		panel.add(spinners[0] = getStandardSpinner(xValue));
		panel.add(new JLabel(labelY));
		panel.add(spinners[1] = getStandardSpinner(yValue));
		panel.add(new JLabel(labelZ));
		panel.add(spinners[2] = getStandardSpinner(zValue));
		return spinners;
	}

	private void manualRotate() {
		final JPanel inputPanel = new JPanel();
		final GridLayout layout = new GridLayout(6, 1);
		inputPanel.setLayout(layout);
		final JSpinner[] spinners = getLabeledSpinnerArray(inputPanel, "Rotate X degrees (around axis facing front):", 0.0, "Rotate Y degrees (around axis facing left):", 0.0, "Rotate Z degrees (around axis facing up):", 0.0);
		final int x = JOptionPane.showConfirmDialog(getRootPane(), inputPanel, "Manual Rotation", JOptionPane.OK_CANCEL_OPTION);
		if (x != JOptionPane.OK_OPTION) {
			return;
		}

		final double deltaXAngle = Math.toRadians(((Number) spinners[0].getValue()).doubleValue());
		final double deltaYAngle = Math.toRadians(((Number) spinners[1].getValue()).doubleValue());
		final double deltaZAngle = Math.toRadians(((Number) spinners[2].getValue()).doubleValue());
		final UndoAction rotate = modelEditor.rotate(modelEditor.getSelectionCenter(), deltaXAngle, deltaYAngle, deltaZAngle);
		undoListener.pushAction(rotate);

	}

	private void manualSet() {
		final JPanel inputPanel = new JPanel();
		final GridLayout layout = new GridLayout(6, 1);
		inputPanel.setLayout(layout);
		final JSpinner[] spinners = getLabeledSpinnerArray(inputPanel, "New Position X:", 0.0, "New Position Y:", 0.0, "New Position Z:", 0.0);
		final int x = JOptionPane.showConfirmDialog(getRootPane(), inputPanel, "Manual Position", JOptionPane.OK_CANCEL_OPTION);
		if (x != JOptionPane.OK_OPTION) {
			return;
		}
		final double positionX = ((Number) spinners[0].getValue()).doubleValue();
		final double positionY = ((Number) spinners[1].getValue()).doubleValue();
		final double positionZ = ((Number) spinners[2].getValue()).doubleValue();
		final UndoAction setPosition = modelEditor.setPosition(modelEditor.getSelectionCenter(), positionX, positionY, positionZ);
		undoListener.pushAction(setPosition);
	}

	private void manualScale() {
		final JPanel inputPanel = new JPanel();
		final GridLayout layout = new GridLayout(13, 1);
		inputPanel.setLayout(layout);
		final JSpinner[] spinners = getLabeledSpinnerArray(inputPanel, "Scale X:", 1.0, "Scale Y:", 1.0, "Scale Z:", 1.0);
		final JCheckBox customOrigin = new JCheckBox("Custom Scaling Origin");
		inputPanel.add(customOrigin);

		Vec3 selectionCenter = modelEditor.getSelectionCenter();
		if (Double.isNaN(selectionCenter.x)) {
			selectionCenter = new Vec3(0, 0, 0);
		}
		final JSpinner[] centerSpinners = getLabeledSpinnerArray(inputPanel, "Center X:", selectionCenter.x, "Center Y:", selectionCenter.y, "Center Z:", selectionCenter.z);
		for (final JSpinner spinner : centerSpinners) {
			spinner.setEnabled(false);
		}
		customOrigin.addActionListener(e -> {
			for (final JSpinner spinner : centerSpinners) {
				spinner.setEnabled(customOrigin.isSelected());
			}
		});

		final int x = JOptionPane.showConfirmDialog(getRootPane(), inputPanel, "Manual Scaling",
				JOptionPane.OK_CANCEL_OPTION);
		if (x != JOptionPane.OK_OPTION) {
			return;
		}
		final double scaleX = ((Number) spinners[0].getValue()).doubleValue();
		final double scaleY = ((Number) spinners[1].getValue()).doubleValue();
		final double scaleZ = ((Number) spinners[2].getValue()).doubleValue();
		double centerX = selectionCenter.x;
		double centerY = selectionCenter.y;
		double centerZ = selectionCenter.z;
		if (customOrigin.isSelected()) {
			centerX = ((Number) centerSpinners[0].getValue()).doubleValue();
			centerY = ((Number) centerSpinners[1].getValue()).doubleValue();
			centerZ = ((Number) centerSpinners[2].getValue()).doubleValue();
		} else {
			centerX = selectionCenter.x;
			centerY = selectionCenter.y;
			centerZ = selectionCenter.z;
		}
		final GenericScaleAction scalingAction = modelEditor.beginScaling(centerX, centerY, centerZ);
		scalingAction.updateScale(scaleX, scaleY, scaleZ);
		undoListener.pushAction(scalingAction);
	}

	private JSpinner getStandardSpinner(double startValue) {
		return new JSpinner(new SpinnerNumberModel(startValue, -100000.00, 100000.0, 0.0001));
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		if (!activityListener.isEditing()) {
			activityListener.viewportChanged(cursorManager);
			viewportListener.viewportChanged(this);
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
			viewportListener.viewportChanged(this);
			requestFocus();
			activityListener.mousePressed(e, this);
			// selectStart = new Point(e.getX(), e.getY());
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			activityListener.viewportChanged(cursorManager);
			viewportListener.viewportChanged(this);
			requestFocus();
			activityListener.mousePressed(e, this);
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
			activityListener.mouseReleased(e, this);
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
			activityListener.mouseReleased(e, this);
		}
		if (!mouseInBounds && (selectStart == null) && (actStart == null) && (lastClick == null)) {
			clickTimer.stop();
		}
//		repaint();
		// MainFrame.panel.refreshUndo();
		undoHandler.refreshUndo();
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

		final double mx = e.getX();
		final double my = e.getY();

		if (neg) {
			wr = -wr;
		}
		for (int i = 0; i < wr; i++) {
			if (neg) {
				m_a -= (mx - (getWidth() / 2.0)) * ((1 / m_zoom) - (1 / (m_zoom * 1.15)));
				m_b -= (my - (getHeight() / 2.0)) * ((1 / m_zoom) - (1 / (m_zoom * 1.15)));
				m_zoom *= 1.15;
			} else {
				m_zoom /= 1.15;
				m_a -= (mx - (getWidth() / 2.0)) * ((1 / (m_zoom * 1.15)) - (1 / m_zoom));
				m_b -= (my - (getHeight() / 2.0)) * ((1 / (m_zoom * 1.15)) - (1 / m_zoom));
			}
		}
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		activityListener.mouseDragged(e, this);
		lastMouseMotion = e.getPoint();
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		if (!mouseInBounds && getBounds().contains(e.getPoint()) && !activityListener.isEditing()) {
			mouseEntered(e);
		}
		activityListener.mouseMoved(e, this);
		lastMouseMotion = e.getPoint();
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
	public void modelEditorChanged(final ModelEditor newModelEditor) {
		modelEditor = newModelEditor;
		// TODO call from display panel and above
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
			DockingWindowTitleProvider titleProvider = view.getWindowProperties().getTitleProvider();
			view.getViewProperties().setTitle(name);
			view.getWindowProperties().setTitleProvider(titleProvider);
			m_d1 = dim1;
			m_d2 = dim2;
		}
	}

	private final class ModelVisitorImplementation implements ModelVisitor {
		@Override
		public void ribbonEmitter(final RibbonEmitter particleEmitter) {
			linkRenderer.ribbonEmitter(particleEmitter);
		}

		@Override
		public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
			linkRenderer.particleEmitter2(particleEmitter);
		}

		@Override
		public void particleEmitter(final ParticleEmitter particleEmitter) {
			linkRenderer.particleEmitter(particleEmitter);
		}

		@Override
		public void popcornFxEmitter(final ParticleEmitterPopcorn popcornFxEmitter) {
			linkRenderer.popcornFxEmitter(popcornFxEmitter);
		}

		@Override
		public void light(final Light light) {
			linkRenderer.light(light);
		}

		@Override
		public void helper(final Helper object) {
			linkRenderer.helper(object);
		}

		@Override
		public void eventObject(final EventObject eventObject) {
			linkRenderer.eventObject(eventObject);
		}

		@Override
		public void collisionShape(final CollisionShape collisionShape) {
			linkRenderer.collisionShape(collisionShape);
		}

		@Override
		public void camera(final Camera camera) { }

		@Override
		public void bone(final Bone object) {
			linkRenderer.bone(object);
		}

		@Override
		public void attachment(final Attachment attachment) {
			linkRenderer.attachment(attachment);
		}

		@Override
		public GeosetVisitor beginGeoset(final int geosetId, final Material material, final GeosetAnim geosetAnim) {
			return GeosetVisitor.NO_ACTION;
		}
	}

	public static class DropLocation extends TransferHandler.DropLocation {
		protected DropLocation(final Point dropPoint) {
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

	public ModelEditor getModelEditor() {
		return modelEditor;
	}

	public Vec3 getFacingVector() {
		return facingVector;
	}

}