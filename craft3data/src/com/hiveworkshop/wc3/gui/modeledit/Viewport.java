package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.TransferHandler;

import com.etheller.util.CollectionUtils;
import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.ProgramPreferencesChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.CursorManager;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.ViewportActivity;
import com.hiveworkshop.wc3.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.viewport.AnimatedViewportModelRenderer;
import com.hiveworkshop.wc3.gui.modeledit.viewport.NodeIconPalette;
import com.hiveworkshop.wc3.gui.modeledit.viewport.ResettableAnimatedIdObjectParentLinkRenderer;
import com.hiveworkshop.wc3.gui.modeledit.viewport.ViewportModelRenderer;
import com.hiveworkshop.wc3.gui.modeledit.viewport.ViewportView;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.mdl.v2.MaterialView;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.mdl.v2.visitor.GeosetVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.ModelVisitor;

public class Viewport extends JPanel implements MouseListener, ActionListener, MouseWheelListener, CoordinateSystem,
		ViewportView, MouseMotionListener, ModelEditorChangeListener {
	byte m_d1;
	byte m_d2;
	double m_a = 0;
	double m_b = 0;
	double m_zoom = 1;
	Point lastClick;
	Point selectStart;
	Point actStart;
	Timer clickTimer = new Timer(16, this);
	Timer paintTimer;
	boolean mouseInBounds = false;
	JPopupMenu contextMenu;
	JMenuItem reAssignMatrix;
	JMenuItem setParent;
	JMenuItem renameBone;
	JMenuItem appendBoneBone;
	JMenuItem cogBone;
	JMenuItem manualMove;
	JMenuItem manualRotate;
	JMenuItem manualSet;
	JMenuItem manualScale;
	JMenuItem addTeamColor;
	JMenuItem splitGeo;

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
	private final JMenuItem createFace;
	private final Vertex facingVector;
	private final ViewportListener viewportListener;

	public Viewport(final byte d1, final byte d2, final ModelView modelView,
			final ProgramPreferences programPreferences, final ViewportActivity activityListener,
			final ModelStructureChangeListener modelStructureChangeListener, final UndoActionListener undoListener,
			final CoordDisplayListener coordDisplayListener, final UndoHandler undoHandler,
			final ModelEditor modelEditor, final ViewportTransferHandler viewportTransferHandler,
			final RenderModel renderModel, final ViewportListener viewportListener) {
		// Dimension 1 and Dimension 2, these specify which dimensions to
		// display.
		// the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z
		// dimensions
		//
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
		this.cursorManager = new CursorManager() {
			@Override
			public void setCursor(final Cursor cursor) {
				Viewport.this.setCursor(cursor);
			}
		};
		setupCopyPaste(viewportTransferHandler);
		// Viewport border
		setBorder(BorderFactory.createBevelBorder(1));
		setupViewportBackground(programPreferences);
		programPreferences.addChangeListener(new ProgramPreferencesChangeListener() {
			@Override
			public void preferencesChanged() {
				setupViewportBackground(programPreferences);
			}
		});
		setMinimumSize(new Dimension(200, 200));
		add(Box.createHorizontalStrut(200));
		add(Box.createVerticalStrut(200));
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		addMouseListener(this);
		addMouseWheelListener(this);
		addMouseMotionListener(this);

		contextMenu = new JPopupMenu();
		createFace = new JMenuItem("Create Face");
		createFace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
		createFace.addActionListener(this);
		contextMenu.add(createFace);
		addTeamColor = new JMenuItem("Split Geoset and Add Team Color");
		addTeamColor.addActionListener(this);
		contextMenu.add(addTeamColor);
		splitGeo = new JMenuItem("Split Geoset");
		splitGeo.addActionListener(this);
		contextMenu.add(splitGeo);
		contextMenu.addSeparator();
		manualMove = new JMenuItem("Translation Type-in");
		manualMove.addActionListener(this);
		contextMenu.add(manualMove);
		manualRotate = new JMenuItem("Rotate Type-in");
		manualRotate.addActionListener(this);
		contextMenu.add(manualRotate);
		manualSet = new JMenuItem("Position Type-in");
		manualSet.addActionListener(this);
		contextMenu.add(manualSet);
		manualScale = new JMenuItem("Scale Type-in");
		manualScale.addActionListener(this);
		contextMenu.add(manualScale);
		contextMenu.addSeparator();
		reAssignMatrix = new JMenuItem("Re-assign Matrix");
		reAssignMatrix.addActionListener(this);
		contextMenu.add(reAssignMatrix);
		setParent = new JMenuItem("Set Parent");
		setParent.addActionListener(this);
		contextMenu.add(setParent);
		cogBone = new JMenuItem("Auto-Center Bone(s)");
		cogBone.addActionListener(this);
		contextMenu.add(cogBone);
		renameBone = new JMenuItem("Rename Bone");
		renameBone.addActionListener(this);
		contextMenu.add(renameBone);
		appendBoneBone = new JMenuItem("Append Bone Suffix");
		appendBoneBone.addActionListener(this);
		contextMenu.add(appendBoneBone);

		viewportModelRenderer = new ViewportModelRenderer(programPreferences.getVertexSize());
		animatedViewportModelRenderer = new AnimatedViewportModelRenderer(programPreferences.getVertexSize());
		linkRenderer = new ResettableAnimatedIdObjectParentLinkRenderer(programPreferences.getVertexSize());
		linkRenderingVisitorAdapter = new ModelVisitorImplementation();

		facingVector = new Vertex(0, 0, 0);
		final byte unusedXYZ = CoordinateSystem.Util.getUnusedXYZ(this);
		facingVector.setCoord(unusedXYZ, unusedXYZ == 0 ? 1 : -1);
		paintTimer = new Timer(16, new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				repaint();
				if (!isShowing()) {
					paintTimer.stop();
				}
			}
		});
		paintTimer.start();
	}

	public void setupViewportBackground(final ProgramPreferences programPreferences) {
		// if (programPreferences.isInvertedDisplay()) {
		// setBackground(Color.DARK_GRAY.darker());
		// } else {
		// setBackground(new Color(255, 255, 255));
		// }
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

	long min = Long.MAX_VALUE;
	long max;
	long avg;
	long runningSum;
	long count;

	public void paintComponent(final Graphics g, final int vertexSize) {
		super.paintComponent(g);
		final long renderStart = System.nanoTime();
		if (programPreferences.isInvertedDisplay()) {
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
			for (float x = 0; ((cameraOrigin.x + x) < getWidth()) || ((cameraOrigin.x - x) >= 0); x += lightIncrement) {
				g.drawLine((int) (cameraOrigin.x + x), 0, (int) (cameraOrigin.x + x), getHeight());
				g.drawLine((int) (cameraOrigin.x - x), 0, (int) (cameraOrigin.x - x), getHeight());
			}
			for (float y = 0; ((cameraOrigin.y + y) < getHeight())
					|| ((cameraOrigin.y - y) >= 0); y += lightIncrement) {
				g.drawLine(0, (int) (cameraOrigin.y + y), getWidth(), (int) (cameraOrigin.y + y));
				g.drawLine(0, (int) (cameraOrigin.y - y), getWidth(), (int) (cameraOrigin.y - y));
			}
			g.setColor(Color.GRAY);
			for (float x = 0; ((cameraOrigin.x + x) < getWidth()) || ((cameraOrigin.x - x) >= 0); x += increment) {
				g.drawLine((int) (cameraOrigin.x + x), 0, (int) (cameraOrigin.x + x), getHeight());
				g.drawLine((int) (cameraOrigin.x - x), 0, (int) (cameraOrigin.x - x), getHeight());
			}
			for (float y = 0; ((cameraOrigin.y + y) < getHeight()) || ((cameraOrigin.y - y) >= 0); y += increment) {
				g.drawLine(0, (int) (cameraOrigin.y + y), getWidth(), (int) (cameraOrigin.y + y));
				g.drawLine(0, (int) (cameraOrigin.y - y), getWidth(), (int) (cameraOrigin.y - y));
			}
			g.setColor(Color.ORANGE);
			for (float x = 0; ((cameraOrigin.x + x) < getWidth()) || ((cameraOrigin.x - x) >= 0); x += darkIncrement) {
				g.drawLine((int) (cameraOrigin.x + x), 0, (int) (cameraOrigin.x + x), getHeight());
				g.drawLine((int) (cameraOrigin.x - x), 0, (int) (cameraOrigin.x - x), getHeight());
			}
			for (float y = 0; ((cameraOrigin.y + y) < getHeight()) || ((cameraOrigin.y - y) >= 0); y += darkIncrement) {
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
		if (modelEditor.editorWantsAnimation()) {
			final Stroke stroke = graphics2d.getStroke();
			graphics2d.setStroke(new BasicStroke(3));
			renderModel.updateNodes(true, false);
			linkRenderer.reset(this, graphics2d, NodeIconPalette.HIGHLIGHT, renderModel);
			modelView.visit(linkRenderingVisitorAdapter);
			graphics2d.setStroke(stroke);
			animatedViewportModelRenderer.reset(graphics2d, programPreferences, m_d1, m_d2, this, this, modelView,
					renderModel);
			modelView.visit(animatedViewportModelRenderer);
			activityListener.render(graphics2d, this, renderModel);
		} else {
			viewportModelRenderer.reset(graphics2d, programPreferences, m_d1, m_d2, this, this, modelView);
			modelView.visit(viewportModelRenderer);
			activityListener.renderStatic(graphics2d, this);
		}

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
		// int xoff = 0;
		// int yoff = 0;
		// Component temp = this;
		// while (temp != null) {
		// xoff += temp.getX();
		// yoff += temp.getY();
		// // if( temp.getClass() == ModelPanel.class )
		// // {
		// //// temp = MainFrame.panel; TODO
		// // temp = null;
		// // }
		// // else
		// // {
		// temp = temp.getParent();
		// // }
		// }

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

		final long renderEnd = System.nanoTime();
		final long elapsed = renderEnd - renderStart;
		if (elapsed < min) {
			min = elapsed;
		}
		if (elapsed > max) {
			max = elapsed;
		}
		runningSum += elapsed;
		count += 1;
		if (count >= 100) {
			final long millis = ((runningSum / count) / 1000000L) + 1;
			if (millis > paintTimer.getDelay()) {
				final int millis2 = (int) (millis * 5);
				System.out.println("delay=" + millis2);
				paintTimer.setDelay(millis2);
			} else if (millis < paintTimer.getDelay()) {
				final int max2 = Math.max(16, (int) (millis * 5));
				System.out.println("delay=" + max2);
				paintTimer.setDelay(max2);
			}
			min = Long.MAX_VALUE;
			max = 0;
			runningSum = 0;
			count = 0;
		}
		final boolean showing = isShowing();
		final boolean running = paintTimer.isRunning();
		if (showing && !running) {
			paintTimer.start();
		} else if (!showing && running) {
			paintTimer.stop();
		}
	}

	@Override
	public double convertX(final double x) {
		return ((x + m_a) * m_zoom) + (getWidth() / 2);
	}

	@Override
	public double convertY(final double y) {
		return ((-y + m_b) * m_zoom) + (getHeight() / 2);
	}

	@Override
	public double geomX(final double x) {
		return ((x - (getWidth() / 2)) / m_zoom) - m_a;
	}

	@Override
	public double geomY(final double y) {
		return -(((y - (getHeight() / 2)) / m_zoom) - m_b);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		try {
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
				final PointerInfo pointerInfo = MouseInfo.getPointerInfo();
				if ((pointerInfo == null) || (pointerInfo.getLocation() == null)) {
					return;
				}
				final double mx = pointerInfo.getLocation().x - xoff;// MainFrame.frame.getX()-8);
				final double my = pointerInfo.getLocation().y - yoff;// MainFrame.frame.getY()-30);
				// JOptionPane.showMessageDialog(null,mx+","+my+" as mouse,
				// "+lastClick.x+","+lastClick.y+" as last.");
				// System.out.println(xoff+" and "+mx);
				if (lastClick != null) {

					m_a += ((int) mx - lastClick.x) / m_zoom;
					m_b += ((int) my - lastClick.y) / m_zoom;
					lastClick.x = (int) mx;
					lastClick.y = (int) my;
				}
				coordDisplayListener.notifyUpdate(m_d1, m_d2, ((mx - (getWidth() / 2)) / m_zoom) - m_a,
						-(((my - (getHeight() / 2)) / m_zoom) - m_b));
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
			} else if (e.getSource() == reAssignMatrix) {
				final MatrixPopup matrixPopup = new MatrixPopup(modelView.getModel());
				final String[] words = { "Accept", "Cancel" };
				final int i = JOptionPane.showOptionDialog(this, matrixPopup, "Rebuild Matrix",
						JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null, words, words[1]);
				if (i == 0) {
					// JOptionPane.showMessageDialog(null,"action approved");
					modelEditor.setMatrix(BoneShell.toBonesList(CollectionUtils.asList(matrixPopup.newRefs)));
				}
			} else if (e.getSource() == setParent) {
				class NodeShell {
					IdObject node;

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

				final ArrayList<IdObject> idObjects = modelView.getModel().getIdObjects();
				final NodeShell[] nodeOptions = new NodeShell[idObjects.size() + 1];
				nodeOptions[0] = new NodeShell(null);
				final NodeShell defaultChoice = nodeOptions[0];
				for (int i = 0; i < idObjects.size(); i++) {
					final IdObject node = idObjects.get(i);
					nodeOptions[i + 1] = new NodeShell(node);
				}
				final NodeShell result = (NodeShell) JOptionPane.showInputDialog(this, "Choose a parent node",
						"Set Parent Node", JOptionPane.PLAIN_MESSAGE, null, nodeOptions, defaultChoice);
				final MatrixPopup matrixPopup = new MatrixPopup(modelView.getModel());
				if (result != null) {
					// JOptionPane.showMessageDialog(null,"action approved");
					modelEditor.setParent(result.getNode());
				}
			} else if (e.getSource() == renameBone) {
				final String name = JOptionPane.showInputDialog(this, "Enter bone name:");
				if (name != null) {
					modelEditor.setSelectedBoneName(name);
				}
			} else if (e.getSource() == appendBoneBone) {
				final String name = JOptionPane.showInputDialog(this, "Enter bone suffix:");
				if (name != null) {
					modelEditor.addSelectedBoneSuffix(name);
				}
			} else if (e.getSource() == cogBone) {
				undoListener.pushAction(modelEditor.autoCenterSelectedBones());
			} else if (e.getSource() == addTeamColor) {
				undoListener.pushAction(modelEditor.addTeamColor());
			} else if (e.getSource() == splitGeo) {
				undoListener.pushAction(modelEditor.splitGeoset());
			} else if (e.getSource() == manualMove) {
				manualMove();
			} else if (e.getSource() == manualRotate) {
				manualRotate();
			} else if (e.getSource() == manualSet) {
				manualSet();
			} else if (e.getSource() == manualScale) {
				manualScale();
			} else if (e.getSource() == createFace) {
				try {
					undoListener.pushAction(modelEditor.createFaceFromSelection(facingVector));
				} catch (final FaceCreationException exc) {
					JOptionPane.showMessageDialog(this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		} catch (final Exception exc) {
			ExceptionPopup.display(exc);
		}
	}

	private void manualMove() {
		final JPanel inputPanel = new JPanel();
		final GridLayout layout = new GridLayout(6, 1);
		inputPanel.setLayout(layout);
		final JSpinner[] spinners = new JSpinner[3];
		inputPanel.add(new JLabel("Move X:"));
		inputPanel.add(spinners[0] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
		inputPanel.add(new JLabel("Move Y:"));
		inputPanel.add(spinners[1] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
		inputPanel.add(new JLabel("Move Z:"));
		inputPanel.add(spinners[2] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
		final int x = JOptionPane.showConfirmDialog(getRootPane(), inputPanel, "Manual Translation",
				JOptionPane.OK_CANCEL_OPTION);
		if (x != JOptionPane.OK_OPTION) {
			return;
		}
		final double deltaX = ((Number) spinners[0].getValue()).doubleValue();
		final double deltaY = ((Number) spinners[1].getValue()).doubleValue();
		final double deltaZ = ((Number) spinners[2].getValue()).doubleValue();
		final UndoAction translate = modelEditor.translate(deltaX, deltaY, deltaZ);
		undoListener.pushAction(translate);
	}

	private void manualRotate() {
		final JPanel inputPanel = new JPanel();
		final GridLayout layout = new GridLayout(6, 1);
		inputPanel.setLayout(layout);
		final JSpinner[] spinners = new JSpinner[3];
		inputPanel.add(new JLabel("Rotate X degrees (around axis facing front):"));
		inputPanel.add(spinners[0] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
		inputPanel.add(new JLabel("Rotate Y degrees (around axis facing left):"));
		inputPanel.add(spinners[1] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
		inputPanel.add(new JLabel("Rotate Z degrees (around axis facing up):"));
		inputPanel.add(spinners[2] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
		final int x = JOptionPane.showConfirmDialog(getRootPane(), inputPanel, "Manual Rotation",
				JOptionPane.OK_CANCEL_OPTION);
		if (x != JOptionPane.OK_OPTION) {
			return;
		}

		final double deltaXAngle = Math.toRadians(((Number) spinners[0].getValue()).doubleValue());
		final double deltaYAngle = Math.toRadians(((Number) spinners[1].getValue()).doubleValue());
		final double deltaZAngle = Math.toRadians(((Number) spinners[2].getValue()).doubleValue());
		final UndoAction rotate = modelEditor.rotate(modelEditor.getSelectionCenter(), deltaXAngle, deltaYAngle,
				deltaZAngle);
		undoListener.pushAction(rotate);

	}

	private void manualSet() {
		final JPanel inputPanel = new JPanel();
		final GridLayout layout = new GridLayout(6, 1);
		inputPanel.setLayout(layout);
		final JSpinner[] spinners = new JSpinner[3];
		inputPanel.add(new JLabel("New Position X:"));
		inputPanel.add(spinners[0] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
		inputPanel.add(new JLabel("New Position Y:"));
		inputPanel.add(spinners[1] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
		inputPanel.add(new JLabel("New Position Z:"));
		inputPanel.add(spinners[2] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
		final int x = JOptionPane.showConfirmDialog(getRootPane(), inputPanel, "Manual Position",
				JOptionPane.OK_CANCEL_OPTION);
		if (x != JOptionPane.OK_OPTION) {
			return;
		}
		final double positionX = ((Number) spinners[0].getValue()).doubleValue();
		final double positionY = ((Number) spinners[1].getValue()).doubleValue();
		final double positionZ = ((Number) spinners[2].getValue()).doubleValue();
		final UndoAction setPosition = modelEditor.setPosition(modelEditor.getSelectionCenter(), positionX, positionY,
				positionZ);
		undoListener.pushAction(setPosition);
	}

	private void manualScale() {
		final JPanel inputPanel = new JPanel();
		final GridLayout layout = new GridLayout(13, 1);
		inputPanel.setLayout(layout);
		final JSpinner[] spinners = new JSpinner[3];
		final JSpinner[] centerSpinners = new JSpinner[3];
		inputPanel.add(new JLabel("Scale X:"));
		inputPanel.add(spinners[0] = new JSpinner(new SpinnerNumberModel(1.0, -100000.00, 100000.0, 0.0001)));
		inputPanel.add(new JLabel("Scale Y:"));
		inputPanel.add(spinners[1] = new JSpinner(new SpinnerNumberModel(1.0, -100000.00, 100000.0, 0.0001)));
		inputPanel.add(new JLabel("Scale Z:"));
		inputPanel.add(spinners[2] = new JSpinner(new SpinnerNumberModel(1.0, -100000.00, 100000.0, 0.0001)));
		final JCheckBox customOrigin = new JCheckBox("Custom Scaling Origin");
		inputPanel.add(customOrigin);

		final Vertex selectionCenter = modelEditor.getSelectionCenter();
		inputPanel.add(new JLabel("Center X:"));
		inputPanel.add(centerSpinners[0] = new JSpinner(
				new SpinnerNumberModel(selectionCenter.x, -100000.00, 100000.0, 0.0001)));
		inputPanel.add(new JLabel("Center Y:"));
		inputPanel.add(centerSpinners[1] = new JSpinner(
				new SpinnerNumberModel(selectionCenter.y, -100000.00, 100000.0, 0.0001)));
		inputPanel.add(new JLabel("Center Z:"));
		inputPanel.add(centerSpinners[2] = new JSpinner(
				new SpinnerNumberModel(selectionCenter.z, -100000.00, 100000.0, 0.0001)));
		for (final JSpinner spinner : centerSpinners) {
			spinner.setEnabled(false);
		}
		customOrigin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				for (final JSpinner spinner : centerSpinners) {
					spinner.setEnabled(customOrigin.isSelected());
				}
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
				m_a -= (mx - (getWidth() / 2)) * ((1 / m_zoom) - (1 / (m_zoom * 1.15)));
				m_b -= (my - (getHeight() / 2)) * ((1 / m_zoom) - (1 / (m_zoom * 1.15)));
				m_zoom *= 1.15;
			} else {
				m_zoom /= 1.15;
				m_a -= (mx - (getWidth() / 2)) * ((1 / (m_zoom * 1.15)) - (1 / m_zoom));
				m_b -= (my - (getHeight() / 2)) * ((1 / (m_zoom * 1.15)) - (1 / m_zoom));
			}
		}
	}

	public Rectangle2D.Double pointsToGeomRect(final Point a, final Point b) {
		final Point2D.Double topLeft = new Point2D.Double(Math.min(geomX(a.x), geomX(b.x)),
				Math.min(geomY(a.y), geomY(b.y)));
		final Point2D.Double lowRight = new Point2D.Double(Math.max(geomX(a.x), geomX(b.x)),
				Math.max(geomY(a.y), geomY(b.y)));
		final Rectangle2D.Double temp = new Rectangle2D.Double(topLeft.x, topLeft.y, lowRight.x - topLeft.x,
				lowRight.y - topLeft.y);
		return temp;
	}

	public Rectangle2D.Double pointsToRect(final Point a, final Point b) {
		final Point2D.Double topLeft = new Point2D.Double(Math.min(a.x, b.x), Math.min(a.y, b.y));
		final Point2D.Double lowRight = new Point2D.Double(Math.max(a.x, b.x), Math.max(a.y, b.y));
		final Rectangle2D.Double temp = new Rectangle2D.Double(topLeft.x, topLeft.y, lowRight.x - topLeft.x,
				lowRight.y - topLeft.y);
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

	@Override
	public CoordinateSystem copy() {
		return new BasicCoordinateSystem(m_d1, m_d2, m_a, m_b, m_zoom, getWidth(), getHeight());
	}

	@Override
	public void modelEditorChanged(final ModelEditor newModelEditor) {
		this.modelEditor = newModelEditor;
		// TODO call from display panel and above
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
		public void camera(final Camera camera) {
		}

		@Override
		public void bone(final Bone object) {
			linkRenderer.bone(object);
		}

		@Override
		public void attachment(final Attachment attachment) {
			linkRenderer.attachment(attachment);
		}

		@Override
		public GeosetVisitor beginGeoset(final int geosetId, final MaterialView material, final GeosetAnim geosetAnim) {
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

	public Vertex getFacingVector() {
		return facingVector;
	}

}