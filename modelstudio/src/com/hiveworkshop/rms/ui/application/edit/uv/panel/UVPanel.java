package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.uv.activity.TVertexEditorActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.uv.activity.TVertexEditorMultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.uv.activity.TVertexEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.uv.activity.TVertexEditorViewportActivityManager;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.TargaReader;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv.MoverWidgetTVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv.RotatorWidgetTVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv.ScaleWidgetTVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv.TVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.DoNothingTVertexActivity;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexEditorChangeActivityListener;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexEditorChangeNotifier;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexToolbarActionButtonType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.viewport.TVertexEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.TVertexSelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.ui.util.ModeButton;
import com.hiveworkshop.rms.util.Vec2;
import net.infonode.docking.View;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * Write a description of class DisplayPanel here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class UVPanel extends JPanel
		implements ActionListener, CoordDisplayListener, TVertexEditorChangeActivityListener {

	static final ImageIcon UVIcon;

	static {
		UVIcon = new ImageIcon(IconUtils.worldEditStyleIcon(RMSIcons.loadTabImage("UVMap.png")));
	}

	final MainPanel mainPanel;
	private final JComboBox<UnwrapDirection> unwrapDirectionBox;
	private final ModelPanel dispMDL;
	private final JButton up, down, left, right, plusZoom, minusZoom;
	private final JTextField[] mouseCoordDisplay = new JTextField[2];
	private final TVertexEditorViewportActivityManager viewportActivityManager;
	private final TVertexEditorChangeNotifier modelEditorChangeNotifier;
	private final TVertexEditorManager modelEditorManager;
	private final int uvLayerIndex = 0;
	private final ProgramPreferences prefs;
	private final List<ModeButton> modeButtons = new ArrayList<>();
	private final List<ModeButton> selectionModeButtons = new ArrayList<>();
	private final Map<TVertexEditorActivityDescriptor, ModeButton> typeToButton = new HashMap<>();
	private final Map<SelectionMode, ModeButton> modeToButton = new HashMap<>();
	ModeButton loadImage, selectButton, addButton, deselectButton, moveButton, rotateButton, scaleButton, unwrapButton;
	JButton snapButton;
	JMenuItem selectAll, invertSelect, expandSelection, selFromMain, mirrorX, mirrorY, setAspectRatio;
	JMenu editMenu, mirrorSubmenu, dispMenu;
	JMenuBar menuBar;
	JCheckBoxMenuItem wrapImage;
	ArrayList<ModeButton> buttons = new ArrayList<>();
	int selectionType = 0;
	boolean cheatShift = false;
	boolean cheatAlt = false;
	ModelEditorActionType actionType;
	View view;

	AbstractAction selectAllAction = new AbstractAction("Select All") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			UndoAction undoAction = modelEditorManager.getModelEditor().selectAll();

			addUndoAction(undoAction);
		}
	};
	AbstractAction invertSelectAction = new AbstractAction("Invert Selection") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			UndoAction undoAction = modelEditorManager.getModelEditor().invertSelection();
			addUndoAction(undoAction);
//            addUndoAction(modelEditorManager.getModelEditor().invertSelection());
		}
	};
	AbstractAction expandSelectionAction = new AbstractAction("Expand Selection") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			UndoAction undoAction = modelEditorManager.getModelEditor().expandSelection();
			addUndoAction(undoAction);
		}
	};
	AbstractAction selFromMainAction = new AbstractAction("Sel From Main") {
		@Override
		public void actionPerformed(final ActionEvent e) {
//            UndoAction undoAction = modelEditorManager.getModelEditor().selectFromViewer(mpanel.getModelEditorManager().getSelectionView());
			final ModelPanel mpanel = currentModelPanel();
			mpanel.getUndoManager().pushAction(modelEditorManager.getModelEditor().selectFromViewer(mpanel.getModelEditorManager().getSelectionView()));
			repaint();
		}
	};
	private UVViewport vp;
	private ToolbarButtonGroup<TVertexSelectionItemTypes> selectionItemTypeGroup;
	private ToolbarButtonGroup<SelectionMode> selectionModeGroup;
	private ToolbarButtonGroup<TVertexToolbarActionButtonType> actionTypeGroup;
	private JToolBar toolbar;
	private TVertexToolbarActionButtonType selectAndMoveDescriptor;
	private TVertexToolbarActionButtonType selectAndRotateDescriptor;
	private TVertexToolbarActionButtonType selectAndScaleDescriptor;
	private TVertexEditorActivityDescriptor currentActivity;
	private AbstractAction undoAction;
	private AbstractAction redoAction;

	//    public UVPanel(final ModelPanel modelPanel, final ProgramPreferences prefs,
//                   final ModelStructureChangeListener modelStructureChangeListener) {
	public UVPanel(final MainPanel mainPanel, ModelStructureChangeListener modelStructureChangeListener, ProgramPreferences prefs) {

		this.mainPanel = mainPanel;
		add(createJToolBar());
		ModelPanel modelPanel = mainPanel.currentModelPanel();
//        ModelStructureChangeListener modelStructureChangeListener = mainPanel.modelStructureChangeListener;

		viewportActivityManager = new TVertexEditorViewportActivityManager(new DoNothingTVertexActivity());
		modelEditorChangeNotifier = new TVertexEditorChangeNotifier();
		modelEditorChangeNotifier.subscribe(viewportActivityManager);
		modelEditorManager = new TVertexEditorManager(modelPanel.getModelViewManager(), prefs, selectionModeGroup,
				modelEditorChangeNotifier, viewportActivityManager, modelPanel.getEditorRenderModel(),
				modelStructureChangeListener);

		this.prefs = prefs;
		setBorder(BorderFactory.createLineBorder(Color.black));// BorderFactory.createCompoundBorder(
		// BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title),BorderFactory.createBevelBorder(1)),BorderFactory.createEmptyBorder(1,1,1,1)
		// ));
		setOpaque(true);
		setViewport(modelPanel);
		this.dispMDL = modelPanel;

		// Copied from MainPanel
		selectButton = new ModeButton("Select");
		selectButton.addActionListener(new ButtonModeChangeListener(0));
		modeToButton.put(selectionModeGroup.getToolbarButtonTypes()[0], selectButton);
		selectionModeButtons.add(selectButton);

		addButton = new ModeButton("Add");
		addButton.addActionListener(new ButtonModeChangeListener(1));
		modeToButton.put(selectionModeGroup.getToolbarButtonTypes()[1], addButton);
		selectionModeButtons.add(addButton);

		deselectButton = new ModeButton("Deselect");
		deselectButton.addActionListener(new ButtonModeChangeListener(2));
		modeToButton.put(selectionModeGroup.getToolbarButtonTypes()[2], deselectButton);
		selectionModeButtons.add(deselectButton);

		final JLabel[] divider = new JLabel[4];
		for (int i = 0; i < divider.length; i++) {
			divider[i] = new JLabel("----------");
		}
		for (int i = 0; i < mouseCoordDisplay.length; i++) {
			mouseCoordDisplay[i] = new JTextField("");
			mouseCoordDisplay[i].setMaximumSize(new Dimension(80, 18));
			mouseCoordDisplay[i].setMinimumSize(new Dimension(50, 15));
			mouseCoordDisplay[i].setEditable(false);
		}
		loadImage = new ModeButton("Load Image");
		moveButton = new ModeButton("Move");
		moveButton.addActionListener(new ButtonActionChangeListener(0));

		rotateButton = new ModeButton("Rotate");
		rotateButton.addActionListener(new ButtonActionChangeListener(1));

		scaleButton = new ModeButton("Scale");
		scaleButton.addActionListener(new ButtonActionChangeListener(2));

		unwrapDirectionBox = new JComboBox<>(UnwrapDirection.values());

		unwrapButton = new ModeButton("Remap UVs");
		unwrapButton.addActionListener(e -> unwrapFromView());

		typeToButton.put(actionTypeGroup.getToolbarButtonTypes()[0], moveButton);
		typeToButton.put(actionTypeGroup.getToolbarButtonTypes()[1], rotateButton);
		typeToButton.put(actionTypeGroup.getToolbarButtonTypes()[2], scaleButton);

		modeButtons.add(moveButton);
		modeButtons.add(rotateButton);
		modeButtons.add(scaleButton);

		buttons.add(selectButton);
		buttons.add(addButton);
		buttons.add(deselectButton);
		buttons.add(moveButton);
		buttons.add(rotateButton);
		buttons.add(scaleButton);
		buttons.add(loadImage);
		buttons.add(unwrapButton);

		unwrapDirectionBox.setMaximumSize(new Dimension(100, 35));
		unwrapDirectionBox.setMinimumSize(new Dimension(90, 15));
		unwrapDirectionBox.addActionListener(this);

		for (final ModeButton button : buttons) {
			button.setMaximumSize(new Dimension(100, 35));
			button.setMinimumSize(new Dimension(90, 15));
			button.addActionListener(this);
		}

		plusZoom = addButton(20, 20, "Plus.png", e -> zoom(.15));

		minusZoom = addButton(20, 20, "Minus.png", e -> zoom(-.15));

		up = addButton(32, 16, "ArrowUp.png", e -> moveUpDown(20));

		down = addButton(32, 16, "ArrowDown.png", e -> moveUpDown(-20));

		left = addButton(16, 32, "ArrowLeft.png", e -> moveLeftRight(20));

		right = addButton(16, 32, "ArrowRight.png", e -> moveLeftRight(-20));

		toolbar.setMaximumSize(new Dimension(80000, 48));

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(toolbar)
						.addComponent(vp)
						.addGroup(layout.createSequentialGroup()
								.addComponent(mouseCoordDisplay[0])
								.addComponent(mouseCoordDisplay[1]).addGap(120)
								.addGroup(layout.createSequentialGroup()
										.addComponent(left)
										.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
												.addComponent(up)
												.addComponent(down))
										.addComponent(right)).addGap(16)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(plusZoom)
										.addComponent(minusZoom))))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(loadImage)
						.addComponent(divider[0])
						.addComponent(selectButton)
						.addComponent(addButton)
						.addComponent(deselectButton)
						.addComponent(divider[1])
						.addComponent(moveButton)
						.addComponent(rotateButton)
						.addComponent(scaleButton)
						.addComponent(divider[2])
						.addComponent(unwrapDirectionBox)
						.addComponent(unwrapButton)));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(toolbar)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addComponent(vp)
								.addGroup(layout.createParallelGroup()
										.addComponent(mouseCoordDisplay[0])
										.addComponent(mouseCoordDisplay[1])
										.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
												.addGroup(layout.createSequentialGroup()
														.addComponent(up)
														.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
																.addComponent(left)
																.addComponent(right))
														.addComponent(down))
												.addGroup(layout.createSequentialGroup()
														.addComponent(plusZoom)
														.addGap(16)
														.addComponent(minusZoom)))))
						.addGroup(layout.createSequentialGroup()
								.addComponent(loadImage).addGap(8)
								.addComponent(divider[0]).addGap(8)
								.addComponent(selectButton).addGap(8)
								.addComponent(addButton).addGap(8)
								.addComponent(deselectButton).addGap(8)
								.addComponent(divider[1]).addGap(8)
								.addComponent(moveButton).addGap(8)
								.addComponent(rotateButton).addGap(8)
								.addComponent(scaleButton).addGap(8)
								.addComponent(divider[2]).addGap(8)
								.addComponent(unwrapDirectionBox).addGap(8)
								.addComponent(unwrapButton).addGap(8).addGap(8))));

		setLayout(layout);
		selectionModeGroup.addToolbarButtonListener(newType -> {
			resetSelectionModeButtons();
			final ModeButton selectionModeButton = modeToButton.get(newType);
			if (selectionModeButton != null) {
				selectionModeButton.setColors(prefs.getActiveColor1(), prefs.getActiveColor2());
			}
		});

		selectionItemTypeGroup.addToolbarButtonListener(newType -> {
//				animationModeState = newType == SelectionItemTypes.ANIMATE;
//				// we need to refresh the state of stuff AFTER the ModelPanels, this
//				// is a pretty signficant design flaw, so we're just going to
//				// post to the EDT to get behind them (they're called
//				// on the same notifier as this method)
//				SwingUtilities.invokeLater(new Runnable() {
//					@Override
//					public void run() {
//						refreshAnimationModeState();
//					}
//				});
//
//				if (newType == SelectionItemTypes.TPOSE) {
//
//					final Object[] settings = { "Move Linked", "Move Single" };
//					final Object dialogResult = JOptionPane.showInputDialog(null, "Choose settings:", "T-Pose Settings",
//							JOptionPane.PLAIN_MESSAGE, null, settings, settings[0]);
//					final boolean moveLinked = dialogResult == settings[0];
//					ModelEditorManager.MOVE_LINKED = moveLinked;
//				}
			modelEditorManager.setSelectionItemType(newType);
			repaint();
		});

		actionTypeGroup.addToolbarButtonListener(newType -> {
			if (newType != null) {
				changeActivity(newType);
			}
		});
		actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[0]);

		final JPanel menuHolderPanel = new JPanel(new BorderLayout());
		menuHolderPanel.add(this, BorderLayout.CENTER);
		menuHolderPanel.add(createMenuBar(), BorderLayout.BEFORE_FIRST_LINE);
		view = new View("Texture Coordinate Editor: " + currentModelPanel().getModel().getName(), UVIcon, menuHolderPanel);
	}

	private void addUndoAction(UndoAction undoAction) {
		final ModelPanel mpanel = currentModelPanel();
		mpanel.getUndoManager().pushAction(undoAction);
		repaint();
	}

	private void mirror(byte i) {
		final ModelPanel mpanel = currentModelPanel();
		if (mpanel != null) {
			final Vec2 selectionCenter = modelEditorManager.getModelEditor().getSelectionCenter();
			mpanel.getUndoManager().pushAction(modelEditorManager.getModelEditor().mirror(i, selectionCenter.x, selectionCenter.y));
		}
		repaint();
	}

	private void unwrapFromView() {
		final UnwrapDirection selectedItem = (UnwrapDirection) unwrapDirectionBox.getSelectedItem();

		if (selectedItem != null) {
			switch (selectedItem) {
				case BOTTOM -> remap((byte) 1, (byte) 0, selectedItem);
				case FRONT -> remap((byte) 1, (byte) 2, selectedItem);
				case RIGHT -> remap((byte) 0, (byte) 2, selectedItem);
//                    case PERSPECTIVE -> System.out.println("ugg");
			}
		} else {
			JOptionPane.showMessageDialog(UVPanel.this, "Please select a direction", "Error",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	private JButton addButton(int width, int height, String iconPath, ActionListener actionListener) {
		Dimension dim = new Dimension(width, height);
		JButton button = new JButton("");
		button.setMaximumSize(dim);
		button.setMinimumSize(dim);
		button.setPreferredSize(dim);
		button.setIcon(new ImageIcon(RMSIcons.loadDeprecatedImage(iconPath)));
		button.addActionListener(actionListener);
		add(button);
		return button;
	}

	protected void remap(final byte xDim, final byte yDim, final UnwrapDirection direction) {
		final ModelPanel mpanel = currentModelPanel();
		if (mpanel != null) {
			mpanel.getUndoManager().pushAction(modelEditorManager.getModelEditor().remap(xDim, yDim, direction));
		}
		repaint();
	}

	public JToolBar createJToolBar() {
		toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setFloatable(false);
		toolbar.addSeparator();
		undoAction = mainPanel.undoAction;
//        undoAction = getUndoAction();
		toolbar.add(undoAction);
		redoAction = mainPanel.redoAction;
//        redoAction = getRedoAction();
		toolbar.add(redoAction);

		toolbar.addSeparator();

		selectionModeGroup = new ToolbarButtonGroup<>(toolbar, SelectionMode.values());
		toolbar.addSeparator();
		selectionItemTypeGroup = new ToolbarButtonGroup<>(toolbar, TVertexSelectionItemTypes.values());
		toolbar.addSeparator();

		selectAndMoveDescriptor = getButtonStuff("move2.png", "Select and Move", ModelEditorActionType.TRANSLATION);

		selectAndRotateDescriptor = getButtonStuff("rotate.png", "Select and Rotate", ModelEditorActionType.ROTATION);

		selectAndScaleDescriptor = getButtonStuff("scale.png", "Select and Scale", ModelEditorActionType.SCALING);

		actionTypeGroup = new ToolbarButtonGroup<>(toolbar, new TVertexToolbarActionButtonType[] {selectAndMoveDescriptor, selectAndRotateDescriptor, selectAndScaleDescriptor});
		currentActivity = actionTypeGroup.getActiveButtonType();
		toolbar.addSeparator();

		snapButton = toolbar.add(new AbstractAction("Snap", RMSIcons.loadToolBarImageIcon("snap.png")) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final ModelPanel currentModelPanel = currentModelPanel();
				currentModelPanel.getUndoManager().pushAction(modelEditorManager.getModelEditor().snapSelectedVertices());

			}
		});

		return toolbar;
	}

	private TVertexToolbarActionButtonType getButtonStuff(String path, String name, ModelEditorActionType editorActionType) {
		return new TVertexToolbarActionButtonType(
				RMSIcons.loadToolBarImageIcon(path), name) {
			@Override
			public TVertexEditorViewportActivity createActivity(final TVertexEditorManager modelEditorManager,
			                                                    final ModelView modelView, final UndoActionListener undoActionListener) {
				actionType = editorActionType;

				return new TVertexEditorMultiManipulatorActivity(
						getManipulatorWidget(modelEditorManager, modelView, editorActionType),
						undoActionListener,
						modelEditorManager.getSelectionView());
			}
		};
	}

	private TVertexEditorManipulatorBuilder getManipulatorWidget(TVertexEditorManager modelEditorManager, ModelView modelView, ModelEditorActionType editorActionType) {
		return switch (editorActionType) {
			case SCALING -> new ScaleWidgetTVertexEditorManipulatorBuilder(modelEditorManager.getModelEditor(),
					modelEditorManager.getViewportSelectionHandler(), prefs, modelView);
			case ROTATION -> new RotatorWidgetTVertexEditorManipulatorBuilder(modelEditorManager.getModelEditor(),
					modelEditorManager.getViewportSelectionHandler(), prefs, modelView);
			case TRANSLATION -> new MoverWidgetTVertexEditorManipulatorBuilder(modelEditorManager.getModelEditor(),
					modelEditorManager.getViewportSelectionHandler(), prefs, modelView);

		};
	}

//    private AbstractAction getRedoAction() {
//        return new AbstractAction("Redo", RMSIcons.loadToolBarImageIcon("redo.png")) {
//            @Override
//            public void actionPerformed(final ActionEvent e) {
//                currentModelPanel().getUndoManager().redo();
////                try {
////                    currentModelPanel().getUndoManager().redo();
////                } catch (final NoSuchElementException exc) {
////                    JOptionPane.showMessageDialog(UVPanel.this, "Nothing to redo!");
////                } catch (final Exception exc) {
////                    ExceptionPopup.display(exc);
////                    // exc.printStackTrace();
////                }
//                repaint();
//            }
//        };
//    }
//
//    private AbstractAction getUndoAction() {
//        return new AbstractAction("Undo", RMSIcons.loadToolBarImageIcon("undo.png")) {
//            @Override
//            public void actionPerformed(final ActionEvent e) {
//                currentModelPanel().getUndoManager().undo();
////                try {
////                    currentModelPanel().getUndoManager().undo();
////                } catch (final NoSuchElementException exc) {
////                    JOptionPane.showMessageDialog(UVPanel.this, "Nothing to undo!");
////                } catch (final Exception exc) {
////                    ExceptionPopup.display(exc);
////                    // exc.printStackTrace();
////                }
//                repaint();
//            }
//        };
//    }

	public JMenuBar createMenuBar() {
		// Create my menu bar
		menuBar = new JMenuBar();

		editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		editMenu.getAccessibleContext().setAccessibleDescription("Allows the user to use various tools to edit the currently selected model's TVertices.");
		menuBar.add(editMenu);

		dispMenu = new JMenu("View");
		dispMenu.setMnemonic(KeyEvent.VK_V);
		dispMenu.getAccessibleContext().setAccessibleDescription("Control display settings for this Texture Coordinate Editor window.");
		menuBar.add(dispMenu);

		createAndAddMenuItem("Select All", "control A", selectAllAction);

//        createAndAddMenuItem("Invert Selection", "control I", invertSelectAction);
		createAndAddMenuItem("Invert Selection", "control I", e -> addUndoAction(modelEditorManager.getModelEditor().invertSelection()));

		createAndAddMenuItem("Expand Selection", "control E", expandSelectionAction);

		createAndAddMenuItem("Select from Viewer", "control V", selFromMainAction);

		createAndAddMenuItem("Split Vertex", "control V", e -> splitVertex());

		wrapImage = new JCheckBoxMenuItem("Wrap Image", false);
		wrapImage.setToolTipText("Repeat the texture many times in a grid-like display. This feature does not edit the model in any way; only this viewing window.");
		// wrapImage.addActionListener(this);
		dispMenu.add(wrapImage);

		setAspectRatio = new JMenuItem("Set Aspect Ratio");
		setAspectRatio.setMnemonic(KeyEvent.VK_S);
		setAspectRatio.setAccelerator(KeyStroke.getKeyStroke("control R"));
		setAspectRatio.setToolTipText("Sets the amount by which the texture display is stretched, for editing textures with non-uniform width and height.");
		setAspectRatio.addActionListener(e -> setAspectRatio());
		dispMenu.add(setAspectRatio);

		editMenu.add(new JSeparator());

		mirrorSubmenu = new JMenu("Mirror");
		mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
		mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");
		editMenu.add(mirrorSubmenu);

		createAndAddMenuItem("Mirror X", KeyEvent.VK_X, e -> mirror((byte) 0));

		createAndAddMenuItem("Mirror Y", KeyEvent.VK_Y, e -> mirror((byte) 1));

		return menuBar;
	}

	private void createAndAddMenuItem(String itemText, int keyEvent, ActionListener actionListener) {
		JMenuItem menuItem = new JMenuItem(itemText);
		menuItem.setMnemonic(keyEvent);
		menuItem.addActionListener(actionListener);
		mirrorSubmenu.add(menuItem);
	}

	private JMenuItem createAndAddMenuItem(String itemText, String keyStroke, ActionListener actionListener) {
		JMenuItem menuItem = new JMenuItem(itemText);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(keyStroke));
		menuItem.addActionListener(actionListener);
		editMenu.add(menuItem);
		return menuItem;
	}

	private void splitVertex() {
		final ModelPanel mpanel = currentModelPanel();
		if (mpanel != null) {
			// mpanel.getUndoManager().pushAction(modelEditorManager.getModelEditor()
			// .selectFromViewer(mpanel.getModelEditorManager().getSelectionView()));

			final Collection<? extends Vec2> selectedTVertices = modelEditorManager.getSelectionView()
					.getSelectedTVertices(currentLayer());
			for (final Geoset g : mpanel.getModel().getGeosets()) {
				for (final GeosetVertex gv : new ArrayList<>(g.getVertices())) {
					final Vec2 tVertex = gv.getTVertex(currentLayer());
					if (selectedTVertices.contains(tVertex)) {
						final List<Triangle> triangles = gv.getTriangles();
						final Iterator<Triangle> iterator = triangles.iterator();
						if (iterator.hasNext()) {
							iterator.next(); // keep using gv in 1 triangle, but not more
						}
						while (iterator.hasNext()) {
							final Triangle tri = iterator.next();
							final int vertexIndex = tri.indexOfRef(gv);
							final GeosetVertex newVertex = new GeosetVertex(gv);
							tri.set(vertexIndex, newVertex);
							newVertex.getTriangles().add(tri);
							newVertex.getGeoset().add(newVertex);
							iterator.remove();
						}
					}
				}
			}
		}
		repaint();
	}

	public void setControlsVisible(final boolean flag) {
		up.setVisible(flag);
		down.setVisible(flag);
		left.setVisible(flag);
		right.setVisible(flag);
		plusZoom.setVisible(flag);
		minusZoom.setVisible(flag);
	}

	public void initViewport() {
		vp.setAspectRatio(1);
		vp.revalidate();
	}

	public void init() {
		vp.init();
		buttons.get(0).setColors(prefs.getActiveColor1(), prefs.getActiveColor2());
		buttons.get(3).setColors(prefs.getActiveRColor1(), prefs.getActiveRColor2());

		final JRootPane root = getRootPane();

		root.getActionMap().put("Undo", undoAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Z"), "Undo");

		root.getActionMap().put("Redo", redoAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Y"), "Redo");

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("W"), "QKeyboardKey");
		root.getActionMap().put("QKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[0]);
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("E"), "WKeyboardKey");
		root.getActionMap().put("WKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[1]);
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("R"), "EKeyboardKey");
		root.getActionMap().put("EKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[2]);
			}
		});

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("A"), "AKeyboardKey");
		root.getActionMap().put("AKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				selectionItemTypeGroup.setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[0]);
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("S"), "SKeyboardKey");
		root.getActionMap().put("SKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				selectionItemTypeGroup.setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[1]);
			}
		});
//		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("D"),
//				"DKeyboardKey");
//		root.getActionMap().put("DKeyboardKey", new AbstractAction() {
//			@Override
//			public void actionPerformed(final ActionEvent e) {
//				selectionItemTypeGroup.setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[2]);
//			}
//		});
//		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("F"),
//				"FKeyboardKey");
//		root.getActionMap().put("FKeyboardKey", new AbstractAction() {
//			@Override
//			public void actionPerformed(final ActionEvent e) {
//				selectionItemTypeGroup.setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[3]);
//			}
//		});
//		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("G"),
//				"GKeyboardKey");
//		root.getActionMap().put("GKeyboardKey", new AbstractAction() {
//			@Override
//			public void actionPerformed(final ActionEvent e) {
//				selectionItemTypeGroup.setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[4]);
//			}
//		});

		root.getActionMap().put("shiftSelect", new AbstractAction("shiftSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
					selectionModeGroup.setToolbarButtonType(SelectionMode.ADD);
					cheatShift = true;
				}
			}
		});
		root.getActionMap().put("altSelect", new AbstractAction("altSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
					selectionModeGroup.setToolbarButtonType(SelectionMode.DESELECT);
					cheatAlt = true;
				}
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke("shift pressed SHIFT"), "shiftSelect");
		// root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
		// .put(KeyStroke.getKeyStroke("control pressed CONTROL"), "shiftSelect");
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("alt pressed ALT"),
				"altSelect");

		root.getActionMap().put("unShiftSelect", new AbstractAction("unShiftSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if ((selectionModeGroup.getActiveButtonType() == SelectionMode.ADD) && cheatShift) {
					selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
					cheatShift = false;
				}
			}
		});
		root.getActionMap().put("unAltSelect", new AbstractAction("unAltSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if ((selectionModeGroup.getActiveButtonType() == SelectionMode.DESELECT) && cheatAlt) {
					selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
					cheatAlt = false;
				}
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released SHIFT"), "unShiftSelect");
		// root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released
		// CONTROL"),
		// "unShiftSelect");
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released ALT"), "unAltSelect");

		root.getActionMap().put("Select All", selectAllAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control A"), "Select All");

//        root.getActionMap().put("Invert Selection", invertSelectAction);
		root.getActionMap().put("Invert Selection", invertSelectAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control I"), "Invert Selection");

		root.getActionMap().put("Expand Selection", expandSelectionAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control E"), "Expand Selection");

		setControlsVisible(prefs.showVMControls());
	}

	protected boolean animationModeState() {
		return false;
	}

	public boolean frameVisible() {
		return view.isVisible() && view.isShowing() && (view.getWindowParent() != null);
	}

	public View getView() {
		return view;
	}

	public void packFrame() {
		final JFrame frame = (JFrame) view.getTopLevelAncestor();
		if (frame != null) {
			frame.pack();
			frame.setLocationRelativeTo(null);
		}
	}

	public void setMouseCoordDisplay(final double x, final double y) {
		mouseCoordDisplay[0].setText((float) x + "");
		mouseCoordDisplay[1].setText((float) y + "");
	}

	public void setViewport(final ModelPanel dispModel) {
		vp = new UVViewport(dispModel.getModelViewManager(), this, prefs, viewportActivityManager, this,
				modelEditorManager.getModelEditor());
		add(vp);
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		// g.drawString(title,3,3);
		// vp.repaint();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == loadImage) {
			loadImage();
		} else if (e.getSource() == setAspectRatio) {
			setAspectRatio();
		}
	}

	private void moveUpDown(int i) {
		vp.translate(0, i * (1 / vp.getZoomAmount()));
		vp.repaint();
	}

	private void moveLeftRight(int i) {
		vp.translate(i * (1 / vp.getZoomAmount()), 0);// *vp.getZoomAmount()
		vp.repaint();
	}

	private void zoom(double v) {
		vp.zoom(v);
		vp.repaint();
	}

	private void setAspectRatio() {
		final JPanel panel = new JPanel();
		final JSpinner widthVal = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
		final JSpinner heightVal = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
		final JLabel toLabel = new JLabel(" to ");
		panel.add(widthVal);
		panel.add(toLabel);
		panel.add(heightVal);
		JOptionPane.showMessageDialog(this, panel);
		vp.setAspectRatio((Integer) widthVal.getValue() / (double) (Integer) heightVal.getValue());
	}

	private void loadImage() {
		final int x = JOptionPane.showConfirmDialog(this,
				"Do you want to use the texture auto-loader to find available textures?" +
						"\nIf you choose \"No\", then you will have to find a file on your hard drive instead.",
				"Load Image", JOptionPane.YES_NO_CANCEL_OPTION);
		if (x == JOptionPane.YES_OPTION) {
			final DefaultListModel<Material> materials = new DefaultListModel<>();
			for (int i = 0; i < dispMDL.getModel().getMaterials().size(); i++) {
				final Material mat = dispMDL.getModel().getMaterials().get(i);
				materials.addElement(mat);
			}

			final JList<Material> materialsList = new JList<>(materials);
			materialsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			materialsList.setCellRenderer(new MaterialListRenderer(dispMDL.getModel()));
			JOptionPane.showMessageDialog(this, new JScrollPane(materialsList));

			vp.clearBackgroundImage();
			if (materialsList.getSelectedValue() != null) {
				vp.addBackgroundImage(materialsList.getSelectedValue()
						.getBufferedImage(dispMDL.getModel().getWrappedDataSource()));
				boolean wrap = false;
				for (final Layer layer : materialsList.getSelectedValue().getLayers()) {
					if ((layer.getTextureBitmap() != null) && (layer.getTextureBitmap().isWrapWidth()
							|| layer.getTextureBitmap().isWrapHeight())) {
						wrap = true;
					}
				}
				wrapImage.setSelected(wrap);
			}
		} else if (x == JOptionPane.NO_OPTION) {
			final JFileChooser jfc = new JFileChooser();
			final EditableModel current = dispMDL.getModel();
			final SaveProfile profile = SaveProfile.get();
			if ((current != null) && (current.getWorkingDirectory() != null)) {
				jfc.setCurrentDirectory(current.getWorkingDirectory());
			} else if (profile.getPath() != null) {
				jfc.setCurrentDirectory(new File(profile.getPath()));
			}
			jfc.setSelectedFile(null);
			final int returnValue = jfc.showOpenDialog(this);

			if (returnValue == JFileChooser.APPROVE_OPTION) {
				final File temp = jfc.getSelectedFile();
				final String fileName = temp.getName();
				String extension = "";
				final int i = fileName.lastIndexOf('.');
				if (i > 0) {
					extension = fileName.substring(i + 1);
				}
				if (extension.toLowerCase().equals("blp")) {
					vp.clearBackgroundImage();
					vp.addBackgroundImage(BLPHandler.get().getCustomTex(temp.getPath()));
				} else if (extension.toLowerCase().equals("tga")) {
					try {
						vp.clearBackgroundImage();
						vp.addBackgroundImage(TargaReader.getImage(temp.getPath()));
					} catch (final Exception e1) {
						e1.printStackTrace();
						ExceptionPopup.display("Unable to load (special case TGA) image file:", e1);
					}
				} else {
					try {
						vp.clearBackgroundImage();
						vp.addBackgroundImage(ImageIO.read(temp));
					} catch (final IOException e1) {
						e1.printStackTrace();
						ExceptionPopup.display("Unable to load image file:", e1);
					}
				}
			}
		}
	}

	public ImageIcon getImageIcon() {
		return new ImageIcon(vp.getBufferedImage());
	}

	public BufferedImage getBufferedImage() {
		return vp.getBufferedImage();
	}

	/**
	 * A method defining the currently selected UV layer.
	 */
	public int currentLayer() {
		return uvLayerIndex;
	}

	private ModelPanel currentModelPanel() {
		return dispMDL;
	}

	@Override
	public void notifyUpdate(final byte dimension1, final byte dimension2, final double coord1, final double coord2) {
		setMouseCoordDisplay(coord1, coord2);
	}

	@Override
	public void changeActivity(final TVertexEditorActivityDescriptor newType) {
		currentActivity = newType;
		viewportActivityManager.setCurrentActivity(
				newType.createActivity(modelEditorManager, dispMDL.getModelViewManager(), dispMDL.getUndoManager()));
		resetButtons();
		final ModeButton modeButton = typeToButton.get(newType);
		if (modeButton != null) {
			modeButton.setColors(prefs.getActiveRColor1(), prefs.getActiveRColor2());
		}
		actionTypeGroup.maybeSetButtonType(newType);
	}

	public void resetButtons() {
		for (final ModeButton button : modeButtons) {
			button.resetColors();
		}
	}

	public void resetSelectionModeButtons() {
		for (final ModeButton button : selectionModeButtons) {
			button.resetColors();
		}
	}

	public enum UnwrapDirection {
		FRONT("Front"), RIGHT("Right"), BOTTOM("Bottom"), PERSPECTIVE("Perspective");

		private final String displayText;

		UnwrapDirection(final String displayText) {
			this.displayText = displayText;
		}

		@Override
		public String toString() {
			return displayText;
		}
	}

	private final class ButtonActionChangeListener implements ActionListener {
		private final int buttonIndex;

		private ButtonActionChangeListener(final int buttonIndex) {
			this.buttonIndex = buttonIndex;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			changeActivity(actionTypeGroup.getToolbarButtonTypes()[buttonIndex]);
		}
	}

	private final class ButtonModeChangeListener implements ActionListener {
		private final int buttonIndex;

		private ButtonModeChangeListener(final int buttonIndex) {
			this.buttonIndex = buttonIndex;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			selectionModeGroup.setToolbarButtonType(selectionModeGroup.getToolbarButtonTypes()[buttonIndex]);
		}
	}
}
