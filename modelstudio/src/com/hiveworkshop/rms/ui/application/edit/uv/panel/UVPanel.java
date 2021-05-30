package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.uv.TVertexEditorManager;
import com.hiveworkshop.rms.ui.application.edit.uv.activity.TVertexEditorActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.uv.activity.TVertexEditorMultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.uv.activity.TVertexEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.uv.activity.TVertexEditorViewportActivityManager;
import com.hiveworkshop.rms.ui.application.edit.uv.types.DoNothingTVertexActivity;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditorChangeActivityListener;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditorChangeNotifier;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexToolbarActionButtonType;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv.MoverWidgetTVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv.RotatorWidgetTVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv.ScaleWidgetTVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv.TVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.TVertexSelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.ModeButton;
import com.hiveworkshop.rms.util.Vec2;
import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

/**
 * Write a description of class DisplayPanel here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class UVPanel extends JPanel implements CoordDisplayListener, TVertexEditorChangeActivityListener {

	static final ImageIcon UVIcon;

	static {
		UVIcon = new ImageIcon(IconUtils.worldEditStyleIcon(RMSIcons.loadTabImage("UVMap.png")));
	}

	final MainPanel mainPanel;
	private final ModelPanel dispMDL;
	private final JTextField[] mouseCoordDisplay = new JTextField[2];
	private final TVertexEditorViewportActivityManager viewportActivityManager;
	private final TVertexEditorManager modelEditorManager;
	private final ProgramPreferences prefs;
	private final List<ModeButton> modeButtons = new ArrayList<>();
	private final List<ModeButton> selectionModeButtons = new ArrayList<>();
	private final Map<TVertexEditorActivityDescriptor, ModeButton> typeToButton = new HashMap<>();
	private final Map<SelectionMode, ModeButton> modeToButton = new HashMap<>();
	JButton snapButton;
	JCheckBoxMenuItem wrapImage;
	ArrayList<ModeButton> buttons = new ArrayList<>();
	int selectionType = 0;
	boolean cheatShift = false;
	boolean cheatAlt = false;
	View view;
	JPanel zoomPanel;
	JPanel navPanel;

	private UVViewport vp;
	private ToolbarButtonGroup<TVertexSelectionItemTypes> selectionItemTypeGroup;
	private ToolbarButtonGroup<SelectionMode> selectionModeGroup;
	private ToolbarButtonGroup<TVertexToolbarActionButtonType> actionTypeGroup;
	private TVertexEditorActivityDescriptor currentActivity;
	private AbstractAction undoAction;
	private AbstractAction redoAction;

	AbstractAction selectAllAction = new AbstractAction("Select All") {
		@Override
		public void actionPerformed(ActionEvent e) {
			UndoAction undoAction = modelEditorManager.getModelEditor().selectAll();
			addUndoAction(undoAction);
		}
	};
	AbstractAction invertSelectAction = new AbstractAction("Invert Selection") {
		@Override
		public void actionPerformed(ActionEvent e) {
			UndoAction undoAction = modelEditorManager.getModelEditor().invertSelection();
			addUndoAction(undoAction);
//            addUndoAction(modelEditorManager.getModelEditor().invertSelection());
		}
	};
	AbstractAction expandSelectionAction = new AbstractAction("Expand Selection") {
		@Override
		public void actionPerformed(ActionEvent e) {
			UndoAction undoAction = modelEditorManager.getModelEditor().expandSelection();
			addUndoAction(undoAction);
		}
	};
	AbstractAction selFromMainAction = new AbstractAction("Sel From Main") {
		@Override
		public void actionPerformed(ActionEvent e) {
//            UndoAction undoAction = modelEditorManager.getModelEditor().selectFromViewer(mpanel.getModelEditorManager().getSelectionView());
			ModelPanel mpanel = currentModelPanel();
			mpanel.getUndoManager().pushAction(modelEditorManager.getModelEditor().selectFromViewer(mpanel.getModelEditorManager().getSelectionView()));
			repaint();
		}
	};

	//    public UVPanel(ModelPanel modelPanel, ProgramPreferences prefs,
//                   ModelStructureChangeListener modelStructureChangeListener) {
	public UVPanel(MainPanel mainPanel, ModelStructureChangeListener modelStructureChangeListener, ProgramPreferences prefs) {

		this.mainPanel = mainPanel;
		JToolBar toolbar = createJToolBar();
		ModelPanel modelPanel = mainPanel.currentModelPanel();
//        ModelStructureChangeListener modelStructureChangeListener = mainPanel.modelStructureChangeListener;

		viewportActivityManager = new TVertexEditorViewportActivityManager(new DoNothingTVertexActivity());
		TVertexEditorChangeNotifier modelEditorChangeNotifier = new TVertexEditorChangeNotifier();
		modelEditorChangeNotifier.subscribe(viewportActivityManager);
		modelEditorManager = new TVertexEditorManager(modelPanel.getModelViewManager(), prefs, selectionModeGroup, modelEditorChangeNotifier, viewportActivityManager, modelPanel.getEditorRenderModel(), modelStructureChangeListener);

		this.prefs = prefs;
		setBorder(BorderFactory.createLineBorder(Color.black));// BorderFactory.createCompoundBorder(
		// BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title),BorderFactory.createBevelBorder(1)),BorderFactory.createEmptyBorder(1,1,1,1)
		// ));
		setOpaque(true);
		setViewport(modelPanel);
		this.dispMDL = modelPanel;

		JPanel stuffPanel = getStuffPanel();

		JPanel bottomPanel = getBotomPanel();


		setLayout(new MigLayout("fill", "[grow][]", "[][grow][]"));
		add(toolbar, "wrap, spanx");
//		add(createJToolBar(), "wrap, spanx");
		add(vp, "grow");
		add(stuffPanel, "growy, wrap");
		add(bottomPanel);

		selectionModeGroup.addToolbarButtonListener(newType -> {
			resetSelectionModeButtons();
			ModeButton selectionModeButton = modeToButton.get(newType);
			if (selectionModeButton != null) {
				selectionModeButton.setColors(prefs.getActiveColor1(), prefs.getActiveColor2());
			}
		});

		selectionItemTypeGroup.addToolbarButtonListener(newType -> {
			modelEditorManager.setSelectionItemType(newType);
			repaint();
		});

		actionTypeGroup.addToolbarButtonListener(newType -> {
			if (newType != null) {
				changeActivity(newType);
			}
		});
		actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[0]);

		JPanel menuHolderPanel = new JPanel(new BorderLayout());
		menuHolderPanel.add(this, BorderLayout.CENTER);
		menuHolderPanel.add(createMenuBar(), BorderLayout.BEFORE_FIRST_LINE);
		view = new View("Texture Coordinate Editor: " + currentModelPanel().getModel().getName(), UVIcon, menuHolderPanel);
	}

	private JPanel getBotomPanel() {
		JButton plusZoom = addButton(20, 20, "Plus.png", e -> zoom(.15));
		JButton minusZoom = addButton(20, 20, "Minus.png", e -> zoom(-.15));
		zoomPanel = new JPanel(new MigLayout("gap 0", "[]16[]"));
		zoomPanel.add(plusZoom);
		zoomPanel.add(minusZoom);

		JButton up = addButton(32, 16, "ArrowUp.png", e -> moveUpDown(20));
		JButton down = addButton(32, 16, "ArrowDown.png", e -> moveUpDown(-20));
		JButton left = addButton(16, 32, "ArrowLeft.png", e -> moveLeftRight(20));
		JButton right = addButton(16, 32, "ArrowRight.png", e -> moveLeftRight(-20));

		navPanel = new JPanel(new MigLayout("gap 0"));
		navPanel.add(up, "cell 1 0");
		navPanel.add(left, "cell 0 1");
		navPanel.add(right, "cell 2 1");
		navPanel.add(down, "cell 1 2");


		for (int i = 0; i < mouseCoordDisplay.length; i++) {
			mouseCoordDisplay[i] = new JTextField("");
			mouseCoordDisplay[i].setMaximumSize(new Dimension(80, 18));
			mouseCoordDisplay[i].setMinimumSize(new Dimension(50, 15));
			mouseCoordDisplay[i].setEditable(false);
		}

		JPanel botomPanel = new JPanel(new MigLayout("gap 0, hidemode 2", "[][]120[]16[]"));
		botomPanel.add(mouseCoordDisplay[0], "aligny top");
		botomPanel.add(mouseCoordDisplay[1], "aligny top");
		botomPanel.add(navPanel);
		botomPanel.add(zoomPanel);
		return botomPanel;
	}

	private JPanel getStuffPanel() {
		// Copied from MainPanel
		ModeButton selectButton = new ModeButton("Select");
		selectButton.addActionListener(new ButtonModeChangeListener(0));
		modeToButton.put(selectionModeGroup.getToolbarButtonTypes()[0], selectButton);
		selectionModeButtons.add(selectButton);

		ModeButton addButton = new ModeButton("Add");
		addButton.addActionListener(new ButtonModeChangeListener(1));
		modeToButton.put(selectionModeGroup.getToolbarButtonTypes()[1], addButton);
		selectionModeButtons.add(addButton);

		ModeButton deselectButton = new ModeButton("Deselect");
		deselectButton.addActionListener(new ButtonModeChangeListener(2));
		modeToButton.put(selectionModeGroup.getToolbarButtonTypes()[2], deselectButton);
		selectionModeButtons.add(deselectButton);

		JLabel[] divider = new JLabel[4];
		for (int i = 0; i < divider.length; i++) {
			divider[i] = new JLabel("----------");
		}

		ModeButton loadImage = new ModeButton("Load Image");
		loadImage.addActionListener(e -> loadImage3());

		ModeButton moveButton = new ModeButton("Move");
		moveButton.addActionListener(new ButtonActionChangeListener(0));

		ModeButton rotateButton = new ModeButton("Rotate");
		rotateButton.addActionListener(new ButtonActionChangeListener(1));

		ModeButton scaleButton = new ModeButton("Scale");
		scaleButton.addActionListener(new ButtonActionChangeListener(2));

		JComboBox<UnwrapDirection> unwrapDirectionBox = new JComboBox<>(UnwrapDirection.values());

		ModeButton unwrapButton = new ModeButton("Remap UVs");
		unwrapButton.addActionListener(e -> unwrapFromView(unwrapDirectionBox));

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
//		unwrapDirectionBox.addActionListener(this);

		for (ModeButton button : buttons) {
			button.setMaximumSize(new Dimension(100, 35));
			button.setMinimumSize(new Dimension(90, 15));
//			button.addActionListener(this);
		}

		// ToDo the texture combo box should maybe be limited in size and/or moved to a better spot to allow to view longer strings
		JPanel stuffPanel = new JPanel(new MigLayout("wrap 1, gap 0"));
		stuffPanel.add(loadImage);
		stuffPanel.add(getTextureCombobox());
		stuffPanel.add(divider[0]);
		stuffPanel.add(selectButton);
		stuffPanel.add(addButton);
		stuffPanel.add(deselectButton);
		stuffPanel.add(divider[1]);
		stuffPanel.add(moveButton);
		stuffPanel.add(rotateButton);
		stuffPanel.add(scaleButton);
		stuffPanel.add(divider[2]);
		stuffPanel.add(unwrapDirectionBox);
		stuffPanel.add(unwrapButton);
		return stuffPanel;
	}

	private void addUndoAction(UndoAction undoAction) {
		ModelPanel mpanel = currentModelPanel();
		mpanel.getUndoManager().pushAction(undoAction);
		repaint();
	}

	private void mirror(byte i) {
		ModelPanel mpanel = currentModelPanel();
		if (mpanel != null) {
			Vec2 selectionCenter = modelEditorManager.getModelEditor().getSelectionCenter();
			mpanel.getUndoManager().pushAction(modelEditorManager.getModelEditor().mirror(i, selectionCenter.x, selectionCenter.y));
		}
		repaint();
	}

	private void unwrapFromView(JComboBox<UnwrapDirection> unwrapDirectionBox) {
		UnwrapDirection selectedItem = (UnwrapDirection) unwrapDirectionBox.getSelectedItem();

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

	protected void remap(byte xDim, byte yDim, UnwrapDirection direction) {
		ModelPanel mpanel = currentModelPanel();
		if (mpanel != null) {
			mpanel.getUndoManager().pushAction(modelEditorManager.getModelEditor().remap(xDim, yDim, direction));
		}
		repaint();
	}

	public JToolBar createJToolBar() {
		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
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

		TVertexToolbarActionButtonType selectAndMoveDescriptor = getButtonStuff("move2.png", "Select and Move", ModelEditorActionType.TRANSLATION);

		TVertexToolbarActionButtonType selectAndRotateDescriptor = getButtonStuff("rotate.png", "Select and Rotate", ModelEditorActionType.ROTATION);

		TVertexToolbarActionButtonType selectAndScaleDescriptor = getButtonStuff("scale.png", "Select and Scale", ModelEditorActionType.SCALING);

		actionTypeGroup = new ToolbarButtonGroup<>(toolbar, new TVertexToolbarActionButtonType[] {selectAndMoveDescriptor, selectAndRotateDescriptor, selectAndScaleDescriptor});
		currentActivity = actionTypeGroup.getActiveButtonType();
		toolbar.addSeparator();

		JButton snapButton = toolbar.add(new AbstractAction("Snap", RMSIcons.loadToolBarImageIcon("snap.png")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				ModelPanel currentModelPanel = currentModelPanel();
				currentModelPanel.getUndoManager().pushAction(modelEditorManager.getModelEditor().snapSelectedVertices());

			}
		});


		toolbar.setMaximumSize(new Dimension(80000, 48));

		return toolbar;
	}

	private TVertexToolbarActionButtonType getButtonStuff(String path, String name, ModelEditorActionType editorActionType) {
		return new TVertexToolbarActionButtonType(RMSIcons.loadToolBarImageIcon(path), name) {
			@Override
			public TVertexEditorViewportActivity createActivity(TVertexEditorManager modelEditorManager, ModelView modelView, UndoActionListener undoActionListener) {
				return new TVertexEditorMultiManipulatorActivity(
						getManipulatorWidget(modelEditorManager, modelView, editorActionType),
						undoActionListener,
						modelEditorManager.getSelectionView());
			}
		};
	}

	private TVertexEditorManipulatorBuilder getManipulatorWidget(TVertexEditorManager modelEditorManager, ModelView modelView, ModelEditorActionType editorActionType) {
		return switch (editorActionType) {
			case SCALING -> new ScaleWidgetTVertexEditorManipulatorBuilder(modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), prefs, modelView);
			case ROTATION -> new RotatorWidgetTVertexEditorManipulatorBuilder(modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), prefs, modelView);
			case TRANSLATION -> new MoverWidgetTVertexEditorManipulatorBuilder(modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), prefs, modelView);

		};
	}

	public JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		editMenu.getAccessibleContext().setAccessibleDescription("Allows the user to use various tools to edit the currently selected model's TVertices.");
		menuBar.add(editMenu);

		JMenu dispMenu = new JMenu("View");
		dispMenu.setMnemonic(KeyEvent.VK_V);
		dispMenu.getAccessibleContext().setAccessibleDescription("Control display settings for this Texture Coordinate Editor window.");
		menuBar.add(dispMenu);

		createAndAddMenuItem("Select All", "control A", selectAllAction, editMenu);

		createAndAddMenuItem("Invert Selection", "control I", e -> addUndoAction(modelEditorManager.getModelEditor().invertSelection()), editMenu);

		createAndAddMenuItem("Expand Selection", "control E", expandSelectionAction, editMenu);

		createAndAddMenuItem("Select from Viewer", "control V", selFromMainAction, editMenu);

		createAndAddMenuItem("Split Vertex", "control V", e -> splitVertex(), editMenu);

		wrapImage = new JCheckBoxMenuItem("Wrap Image", false);
		wrapImage.setToolTipText("Repeat the texture many times in a grid-like display. This feature does not edit the model in any way; only this viewing window.");
		// wrapImage.addActionListener(this);
		dispMenu.add(wrapImage);

		JMenuItem setAspectRatio = new JMenuItem("Set Aspect Ratio");
		setAspectRatio.setMnemonic(KeyEvent.VK_S);
		setAspectRatio.setAccelerator(KeyStroke.getKeyStroke("control R"));
		setAspectRatio.setToolTipText("Sets the amount by which the texture display is stretched, for editing textures with non-uniform width and height.");
		setAspectRatio.addActionListener(e -> setAspectRatio());
		dispMenu.add(setAspectRatio);

		editMenu.add(new JSeparator());

		JMenu mirrorSubmenu = new JMenu("Mirror");
		mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
		mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");
		editMenu.add(mirrorSubmenu);

		createAndAddMenuItem("Mirror X", KeyEvent.VK_X, e -> mirror((byte) 0), mirrorSubmenu);

		createAndAddMenuItem("Mirror Y", KeyEvent.VK_Y, e -> mirror((byte) 1), mirrorSubmenu);

		return menuBar;
	}

	private void createAndAddMenuItem(String itemText, int keyEvent, ActionListener actionListener, JMenu jMenu) {
		JMenuItem menuItem = new JMenuItem(itemText);
		menuItem.setMnemonic(keyEvent);
		menuItem.addActionListener(actionListener);
		jMenu.add(menuItem);
	}

	private JMenuItem createAndAddMenuItem(String itemText, String keyStroke, ActionListener actionListener, JMenu jMenu) {
		JMenuItem menuItem = new JMenuItem(itemText);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(keyStroke));
		menuItem.addActionListener(actionListener);
		jMenu.add(menuItem);
		return menuItem;
	}

	private void splitVertex() {
		ModelPanel mpanel = currentModelPanel();
		if (mpanel != null) {
			// mpanel.getUndoManager().pushAction(modelEditorManager.getModelEditor()
			// .selectFromViewer(mpanel.getModelEditorManager().getSelectionView()));

			Collection<? extends Vec2> selectedTVertices = modelEditorManager.getSelectionView()
					.getSelectedTVertices(currentLayer());
			for (Geoset g : mpanel.getModel().getGeosets()) {
				for (GeosetVertex gv : new ArrayList<>(g.getVertices())) {
					Vec2 tVertex = gv.getTVertex(currentLayer());
					if (selectedTVertices.contains(tVertex)) {
						List<Triangle> triangles = gv.getTriangles();
						Iterator<Triangle> iterator = triangles.iterator();
						if (iterator.hasNext()) {
							iterator.next(); // keep using gv in 1 triangle, but not more
						}
						while (iterator.hasNext()) {
							Triangle tri = iterator.next();
							int vertexIndex = tri.indexOfRef(gv);
							GeosetVertex newVertex = new GeosetVertex(gv);
							tri.set(vertexIndex, newVertex);
							newVertex.addTriangle(tri);
							newVertex.getGeoset().add(newVertex);
							iterator.remove();
						}
					}
				}
			}
		}
		repaint();
	}

	public void setControlsVisible(boolean flag) {
		navPanel.setVisible(flag);
		zoomPanel.setVisible(flag);
	}

	public void initViewport() {
		vp.setAspectRatio(1);
		vp.revalidate();
	}

	public void init() {
		vp.init();
		buttons.get(0).setColors(prefs.getActiveColor1(), prefs.getActiveColor2());
		buttons.get(3).setColors(prefs.getActiveRColor1(), prefs.getActiveRColor2());

		JRootPane root = getRootPane();

		root.getActionMap().put("Undo", undoAction);
		int isAncestor = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
		root.getInputMap(isAncestor).put(KeyStroke.getKeyStroke("control Z"), "Undo");

		root.getActionMap().put("Redo", redoAction);
		root.getInputMap(isAncestor).put(KeyStroke.getKeyStroke("control Y"), "Redo");

		root.getInputMap(isAncestor).put(KeyStroke.getKeyStroke("W"), "MoveKeyboardKey");
		root.getActionMap().put("MoveKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[0]);
			}
		});

		root.getInputMap(isAncestor).put(KeyStroke.getKeyStroke("E"), "RotateKeyboardKey");
		root.getActionMap().put("RotateKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[1]);
			}
		});

		root.getInputMap(isAncestor).put(KeyStroke.getKeyStroke("R"), "ScaleKeyboardKey");
		root.getActionMap().put("ScaleKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[2]);
			}
		});

//		SELECT("Select", RMSIcons.loadToolBarImageIcon("selectSingle.png")),
//				ADD("Add Selection", RMSIcons.loadToolBarImageIcon("selectAdd.png")),
//				DESELECT("Deselect", RMSIcons.loadToolBarImageIcon("selectRemove.png"));

		root.getInputMap(isAncestor).put(KeyStroke.getKeyStroke("A"), "SelectKeyboardKey");
		root.getActionMap().put("SelectKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionItemTypeGroup.setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[0]);
			}
		});
		root.getInputMap(isAncestor).put(KeyStroke.getKeyStroke("S"), "AddSelectKeyboardKey");
		root.getActionMap().put("AddSelectKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionItemTypeGroup.setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[1]);
			}
		});

		root.getActionMap().put("shiftSelect", new AbstractAction("shiftSelect") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
					selectionModeGroup.setToolbarButtonType(SelectionMode.ADD);
					cheatShift = true;
				}
			}
		});
		root.getActionMap().put("altSelect", new AbstractAction("altSelect") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
					selectionModeGroup.setToolbarButtonType(SelectionMode.DESELECT);
					cheatAlt = true;
				}
			}
		});

		root.getActionMap().put("unShiftSelect", new AbstractAction("unShiftSelect") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ((selectionModeGroup.getActiveButtonType() == SelectionMode.ADD) && cheatShift) {
					selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
					cheatShift = false;
				}
			}
		});
		root.getActionMap().put("unAltSelect", new AbstractAction("unAltSelect") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ((selectionModeGroup.getActiveButtonType() == SelectionMode.DESELECT) && cheatAlt) {
					selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
					cheatAlt = false;
				}
			}
		});

		root.getInputMap(isAncestor).put(KeyStroke.getKeyStroke("control A"), "Select All");
		root.getActionMap().put("Select All", selectAllAction);

		root.getInputMap(isAncestor).put(KeyStroke.getKeyStroke("control I"), "Invert Selection");
		root.getActionMap().put("Invert Selection", invertSelectAction);

		root.getInputMap(isAncestor).put(KeyStroke.getKeyStroke("control E"), "Expand Selection");
		root.getActionMap().put("Expand Selection", expandSelectionAction);

		setControlsVisible(prefs.showVMControls());

//		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("D"), "DKeyboardKey");
//		root.getActionMap().put("DKeyboardKey", new AbstractAction() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				selectionItemTypeGroup.setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[2]);
//			}
//		});
//		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("F"), "FKeyboardKey");
//		root.getActionMap().put("FKeyboardKey", new AbstractAction() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				selectionItemTypeGroup.setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[3]);
//			}
//		});
//		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("G"), "GKeyboardKey");
//		root.getActionMap().put("GKeyboardKey", new AbstractAction() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				selectionItemTypeGroup.setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[4]);
//			}
//		});

//		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control pressed CONTROL"), "shiftSelect");
//		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released CONTROL"), "unShiftSelect");
	}

	private JComboBox<String> getTextureCombobox() {
		System.out.println("getComboBox!");
		DefaultListModel<Bitmap> bitmapListModel;

		List<Bitmap> bitmaps = new ArrayList<>(dispMDL.getModel().getTextures());
		List<String> bitmapNames = new ArrayList<>();

		for (Bitmap bitmap : bitmaps) {
			bitmapNames.add(bitmap.getName());
		}
		bitmaps.add(0, null);
		bitmapNames.add(0, "no image");

//		JComboBox jComboBox = new JComboBox(bitmapListModel);
//		JComboBox<Bitmap> jComboBox = new JComboBox<>(dispMDL.getModel().getTextures().toArray(Bitmap[]::new));
		JComboBox<String> jComboBox = new JComboBox<>(bitmapNames.toArray(String[]::new));
		jComboBox.setSelectedIndex(0);
//		jComboBox.setRenderer(new ListCellRenderer<Bitmap>() {
//			@Override
//			public Component getListCellRendererComponent(JList<? extends Bitmap> list, Bitmap value, int index, boolean isSelected, boolean cellHasFocus) {
//				return new JLabel(value.getName());
//			}
//		});
//		jComboBox.addItem(new Bitmap("no image"));

		jComboBox.addActionListener(e -> {
			BufferedImage image = null;
			if (jComboBox.getSelectedItem() != null) {
				Bitmap bitmap = bitmaps.get(jComboBox.getSelectedIndex());
				if (bitmap != null) {
					image = BLPHandler.getImage(bitmap, dispMDL.getModel().getWrappedDataSource());
//					image = BLPHandler.getImage(bitmap, null);
				}
			}
			setTextureAsBackground(image);
		});

		return jComboBox;
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
		JFrame frame = (JFrame) view.getTopLevelAncestor();
		if (frame != null) {
			frame.pack();
//			frame.setLocationRelativeTo(null);
			frame.setLocationRelativeTo(mainPanel);
		}
	}

	public void setMouseCoordDisplay(double x, double y) {
		mouseCoordDisplay[0].setText((float) x + "");
		mouseCoordDisplay[1].setText((float) y + "");
	}

	public void setViewport(ModelPanel dispModel) {
		vp = new UVViewport(dispModel.getModelViewManager(), this, prefs, viewportActivityManager, this,
				modelEditorManager.getModelEditor());
		add(vp);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// g.drawString(title,3,3);
		// vp.repaint();
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
		JPanel panel = new JPanel();
		JSpinner widthVal = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
		JSpinner heightVal = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
		JLabel toLabel = new JLabel(" to ");
		panel.add(widthVal);
		panel.add(toLabel);
		panel.add(heightVal);
		JOptionPane.showMessageDialog(this, panel);
		vp.setAspectRatio((Integer) widthVal.getValue() / (double) (Integer) heightVal.getValue());
	}

	private void loadImage() {
		int x = JOptionPane.showConfirmDialog(this,
				"Do you want to use the texture auto-loader to find available textures?" +
						"\nIf you choose \"No\", then you will have to find a file on your hard drive instead.",
				"Load Image", JOptionPane.YES_NO_CANCEL_OPTION);
		if (x == JOptionPane.YES_OPTION) {
			DefaultListModel<Material> materials = new DefaultListModel<>();
			for (int i = 0; i < dispMDL.getModel().getMaterials().size(); i++) {
				Material mat = dispMDL.getModel().getMaterials().get(i);
				materials.addElement(mat);
			}

			JList<Material> materialsList = new JList<>(materials);
			materialsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			materialsList.setCellRenderer(new MaterialListRenderer(dispMDL.getModel()));
			JOptionPane.showMessageDialog(this, new JScrollPane(materialsList));

			vp.clearBackgroundImage();
			if (materialsList.getSelectedValue() != null) {
				vp.addBackgroundImage(materialsList.getSelectedValue().getBufferedImage(dispMDL.getModel().getWrappedDataSource()));
				boolean wrap = false;
				for (Layer layer : materialsList.getSelectedValue().getLayers()) {
					if ((layer.getTextureBitmap() != null) && (layer.getTextureBitmap().isWrapWidth()
							|| layer.getTextureBitmap().isWrapHeight())) {
						wrap = true;
					}
				}
				wrapImage.setSelected(wrap);
			}
		} else if (x == JOptionPane.NO_OPTION) {
			FileDialog fileDialog = new FileDialog(this);
			Bitmap bitmap = fileDialog.importImage();
			if (bitmap != null) {
				BufferedImage image = BLPHandler.get().loadTextureDirectly2(bitmap);
				if (image != null) {
					vp.clearBackgroundImage();
					vp.addBackgroundImage(image);
				}
			}
		}
	}

	private void loadImage2() {
		int x = JOptionPane.showConfirmDialog(this,
				"Do you want to use the texture auto-loader to find available textures?" +
						"\nIf you choose \"No\", then you will have to find a file on your hard drive instead.",
				"Load Image", JOptionPane.YES_NO_CANCEL_OPTION);
		if (x == JOptionPane.YES_OPTION) {
			DefaultListModel<Material> materials = new DefaultListModel<>();
			for (int i = 0; i < dispMDL.getModel().getMaterials().size(); i++) {
				Material mat = dispMDL.getModel().getMaterials().get(i);
				materials.addElement(mat);
			}

			JList<Material> materialsList = new JList<>(materials);
			materialsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			materialsList.setCellRenderer(new MaterialListRenderer(dispMDL.getModel()));
			JOptionPane.showMessageDialog(this, new JScrollPane(materialsList));
			if (materialsList.getSelectedValue() != null) {
				BufferedImage image = materialsList.getSelectedValue().getBufferedImage(dispMDL.getModel().getWrappedDataSource());
//				vp.addBackgroundImage(materialsList.getSelectedValue().getBufferedImage(dispMDL.getModel().getWrappedDataSource()));
				boolean wrap = false;
				for (Layer layer : materialsList.getSelectedValue().getLayers()) {
					if ((layer.getTextureBitmap() != null) && (layer.getTextureBitmap().isWrapWidth() || layer.getTextureBitmap().isWrapHeight())) {
						wrap = true;
					}
				}
				wrapImage.setSelected(wrap);
				setTextureAsBackground(image);
			}

			setTextureAsBackground(null);
		} else if (x == JOptionPane.NO_OPTION) {
			FileDialog fileDialog = new FileDialog(this);
			Bitmap bitmap = fileDialog.importImage();
			if (bitmap != null) {
//				BufferedImage image = BLPHandler.get().loadTextureDirectly2(bitmap);
				setTextureAsBackground(BLPHandler.get().loadTextureDirectly2(bitmap));
			}
		}
	}

	private void loadImage3() {
		FileDialog fileDialog = new FileDialog(this);
		Bitmap bitmap = fileDialog.importImage();
		if (bitmap != null) {
			setTextureAsBackground(BLPHandler.getImage(bitmap, dispMDL.getModel().getWrappedDataSource()));
		}
	}

	private void setTextureAsBackground(BufferedImage image) {
		vp.clearBackgroundImage();
		if (image != null) {
			vp.addBackgroundImage(image);
		}
		vp.repaint();
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
		int uvLayerIndex = 0;
		return uvLayerIndex;
	}

	private ModelPanel currentModelPanel() {
		return dispMDL;
	}

	@Override
	public void notifyUpdate(byte dimension1, byte dimension2, double coord1, double coord2) {
		setMouseCoordDisplay(coord1, coord2);
	}

	@Override
	public void changeActivity(TVertexEditorActivityDescriptor newType) {
		currentActivity = newType;
		viewportActivityManager.setCurrentActivity(newType.createActivity(modelEditorManager, dispMDL.getModelViewManager(), dispMDL.getUndoManager()));
		resetButtons();
		ModeButton modeButton = typeToButton.get(newType);
		if (modeButton != null) {
			modeButton.setColors(prefs.getActiveRColor1(), prefs.getActiveRColor2());
		}
		actionTypeGroup.maybeSetButtonType(newType);
	}

	public void resetButtons() {
		for (ModeButton button : modeButtons) {
			button.resetColors();
		}
	}

	public void resetSelectionModeButtons() {
		for (ModeButton button : selectionModeButtons) {
			button.resetColors();
		}
	}

	public enum UnwrapDirection {
		FRONT("Front"), RIGHT("Right"), BOTTOM("Bottom"), PERSPECTIVE("Perspective");

		private final String displayText;

		UnwrapDirection(String displayText) {
			this.displayText = displayText;
		}

		@Override
		public String toString() {
			return displayText;
		}
	}

	private class ButtonActionChangeListener implements ActionListener {
		private final int buttonIndex;

		private ButtonActionChangeListener(int buttonIndex) {
			this.buttonIndex = buttonIndex;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			changeActivity(actionTypeGroup.getToolbarButtonTypes()[buttonIndex]);
		}
	}

	private class ButtonModeChangeListener implements ActionListener {
		private final int buttonIndex;

		private ButtonModeChangeListener(int buttonIndex) {
			this.buttonIndex = buttonIndex;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			selectionModeGroup.setToolbarButtonType(selectionModeGroup.getToolbarButtonTypes()[buttonIndex]);
		}
	}
}
