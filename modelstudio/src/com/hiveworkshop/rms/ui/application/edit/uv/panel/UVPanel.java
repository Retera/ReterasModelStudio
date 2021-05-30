package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.editor.actions.uv.MirrorTVerticesAction;
import com.hiveworkshop.rms.editor.actions.uv.UVRemapAction;
import com.hiveworkshop.rms.editor.actions.uv.UVSnapAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.MultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.uv.TVertexEditorManager;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexUtils;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.listener.ModelEditorChangeNotifier;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.TVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType2;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.TVertexSelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
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
public class UVPanel extends JPanel implements CoordDisplayListener {

	static final ImageIcon UVIcon;

	static {
		UVIcon = new ImageIcon(IconUtils.worldEditStyleIcon(RMSIcons.loadTabImage("UVMap.png")));
	}

	final MainPanel mainPanel;
	private final ModelPanel modelPanel;
	private final JTextField[] mouseCoordDisplay = new JTextField[2];
	private final ViewportActivityManager viewportActivityManager;
	private final TVertexEditorManager modelEditorManager;
	UndoManager undoListener;
	JCheckBoxMenuItem wrapImage;
	ArrayList<ModeButton> buttons = new ArrayList<>();
	boolean cheatShift = false;
	boolean cheatAlt = false;
	View view;
	JPanel zoomPanel;
	JPanel navPanel;

	private UVViewport vp;
	private ToolbarButtonGroup2<TVertexSelectionItemTypes> selectionItemTypeGroup;
	private ToolbarButtonGroup2<SelectionMode> selectionModeGroup;
	private ToolbarButtonGroup2<ModelEditorActionType2> actionTypeGroup;
	private AbstractAction undoAction;
	private AbstractAction redoAction;

	AbstractAction selectAllAction = new AbstractAction("Select All") {
		@Override
		public void actionPerformed(ActionEvent e) {
			UndoAction undoAction = selectAll(modelPanel.getModelView());
			addUndoAction(undoAction);
		}
	};
	AbstractAction invertSelectAction = new AbstractAction("Invert Selection") {
		@Override
		public void actionPerformed(ActionEvent e) {
			UndoAction undoAction = invertSelection(modelPanel.getModelView());
			addUndoAction(undoAction);
//            addUndoAction(modelEditorManager.getModelEditor().invertSelection());
		}
	};
	AbstractAction expandSelectionAction = new AbstractAction("Expand Selection") {
		@Override
		public void actionPerformed(ActionEvent e) {
			UndoAction undoAction = expandSelection(modelPanel.getModelView());
			addUndoAction(undoAction);
		}
	};
	AbstractAction selFromMainAction = new AbstractAction("Sel From Main") {
		@Override
		public void actionPerformed(ActionEvent e) {
			ModelPanel mpanel = currentModelPanel();
			final UndoAction action = selectFromViewer(mpanel.getModelView());
			mpanel.getUndoManager().pushAction(action);
			repaint();
		}
	};

	public UndoAction selectFromViewer(ModelView modelView) {
		return new SetSelectionAction(modelView.getSelectedVertices(), modelView, "").redo();
	}

	public UVPanel(MainPanel mainPanel, ModelStructureChangeListener modelStructureChangeListener) {
		this.mainPanel = mainPanel;
		JToolBar toolbar = createJToolBar();
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
//        ModelStructureChangeListener modelStructureChangeListener = mainPanel.modelStructureChangeListener;

		undoListener = ProgramGlobals.getCurrentModelPanel().getUndoManager();
		viewportActivityManager = new ViewportActivityManager(null);
		ModelEditorChangeNotifier modelEditorChangeNotifier = new ModelEditorChangeNotifier();
		modelEditorChangeNotifier.subscribe(viewportActivityManager);

		modelEditorManager = new TVertexEditorManager(modelPanel.getModelHandler(), selectionModeGroup, modelEditorChangeNotifier, viewportActivityManager, modelStructureChangeListener);

		setBorder(BorderFactory.createLineBorder(Color.black));// BorderFactory.createCompoundBorder(
		// BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title),BorderFactory.createBevelBorder(1)),BorderFactory.createEmptyBorder(1,1,1,1)));
		setOpaque(true);
		setViewport(modelPanel);
		this.modelPanel = modelPanel;

		JPanel stuffPanel = getStuffPanel();

		JPanel bottomPanel = getBotomPanel();


		setLayout(new MigLayout("fill", "[grow][]", "[][grow][]"));
		add(toolbar, "wrap, spanx");
		add(vp, "grow");
		add(stuffPanel, "growy, wrap");
		add(bottomPanel);

		selectionItemTypeGroup.addToolbarButtonListener(newType -> {
			modelEditorManager.setSelectionItemType(newType);
			repaint();
		});

		actionTypeGroup.addToolbarButtonListener(newType -> {
			if (newType != null) {
				changeActivity(newType);
			}
		});
		actionTypeGroup.setActiveButton(ModelEditorActionType2.TRANSLATION);

		JPanel menuHolderPanel = new JPanel(new BorderLayout());
		menuHolderPanel.add(this, BorderLayout.CENTER);
		menuHolderPanel.add(createMenuBar(), BorderLayout.BEFORE_FIRST_LINE);
		view = new View("Texture Coordinate Editor: " + currentModelPanel().getModel().getName(), UVIcon, menuHolderPanel);
	}

	private JPanel getBotomPanel() {
		JButton plusZoom = addButton(20, 20, "Plus.png", e -> zoom(1.15));
		JButton minusZoom = addButton(20, 20, "Minus.png", e -> zoom(-1.15));
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

		JLabel[] divider = new JLabel[4];
		for (int i = 0; i < divider.length; i++) {
			divider[i] = new JLabel("----------");
		}

		ModeButton loadImage = new ModeButton("Load Image");
		loadImage.addActionListener(e -> loadImage3());

		JComboBox<UnwrapDirection> unwrapDirectionBox = new JComboBox<>(UnwrapDirection.values());

		ModeButton unwrapButton = new ModeButton("Remap UVs");
		unwrapButton.addActionListener(e -> unwrapFromView(unwrapDirectionBox));

		buttons.add(loadImage);
		buttons.add(unwrapButton);

		unwrapDirectionBox.setMaximumSize(new Dimension(100, 35));
		unwrapDirectionBox.setMinimumSize(new Dimension(90, 15));
//		unwrapDirectionBox.addActionListener(this);

//		for (ModeButton button : buttons) {
//			button.setMaximumSize(new Dimension(100, 35));
//			button.setMinimumSize(new Dimension(90, 15));
////			button.addActionListener(this);
//		}

		// ToDo the texture combo box should maybe be limited in size and/or moved to a better spot to allow to view longer strings
		JPanel stuffPanel = new JPanel(new MigLayout("wrap 1, gap 0"));
		stuffPanel.add(loadImage);
		stuffPanel.add(getTextureCombobox());
		stuffPanel.add(divider[0]);
		stuffPanel.add(selectionModeGroup.getModeButton(SelectionMode.SELECT));
		stuffPanel.add(selectionModeGroup.getModeButton(SelectionMode.ADD));
		stuffPanel.add(selectionModeGroup.getModeButton(SelectionMode.DESELECT));
		stuffPanel.add(divider[1]);
		stuffPanel.add(actionTypeGroup.getModeButton(ModelEditorActionType2.TRANSLATION));
		stuffPanel.add(actionTypeGroup.getModeButton(ModelEditorActionType2.ROTATION));
		stuffPanel.add(actionTypeGroup.getModeButton(ModelEditorActionType2.SCALING));
		stuffPanel.add(divider[2]);
		stuffPanel.add(unwrapDirectionBox);
		stuffPanel.add(unwrapButton);
		return stuffPanel;
	}


	public UndoAction expandSelection(ModelView modelView) {
		Set<GeosetVertex> expandedSelection = new HashSet<>(modelView.getSelectedVertices());
		Set<GeosetVertex> oldSelection = new HashSet<>(modelView.getSelectedVertices());
		for (GeosetVertex v : oldSelection) {
			expandSelection(v, expandedSelection);
		}

		return new SetSelectionAction(expandedSelection, modelView, "expand selection").redo();
	}

	private void expandSelection(GeosetVertex currentVertex, Set<GeosetVertex> selection) {
		selection.add(currentVertex);
		for (Triangle tri : currentVertex.getTriangles()) {
			for (GeosetVertex other : tri.getVerts()) {
				if (!selection.contains(other)) {
					expandSelection(other, selection);
				}
			}
		}
	}

	public UndoAction invertSelection(ModelView modelView) {
		Set<GeosetVertex> invertedSelection = new HashSet<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			invertedSelection.addAll(geoset.getVertices());
		}
		invertedSelection.removeAll(modelView.getSelectedVertices());

		return new SetSelectionAction(invertedSelection, modelView, "invert selection").redo();
	}

	public UndoAction selectAll(ModelView modelView) {
		Set<GeosetVertex> allSelection = new HashSet<>();
		for (Geoset geo : modelView.getEditableGeosets()) {
			allSelection.addAll(geo.getVertices());
		}

		return new SetSelectionAction(allSelection, modelView, "select all").redo();
	}

	private void addUndoAction(UndoAction undoAction) {
		ModelPanel mpanel = currentModelPanel();
		mpanel.getUndoManager().pushAction(undoAction);
		repaint();
	}

	private void mirror(byte i) {
		ModelPanel mpanel = currentModelPanel();
		if (mpanel != null) {
//			Vec2 selectionCenter = modelEditorManager.getModelEditor().getSelectionCenter();
			Vec2 selectionCenter = modelEditorManager.getSelectionView().getUVCenter(0);

			UndoAction mirror = new MirrorTVerticesAction(TVertexUtils.getTVertices(modelPanel.getModelView().getSelectedVertices(), 0), i, selectionCenter.x, selectionCenter.y).redo();
			mpanel.getUndoManager().pushAction(mirror);
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

	protected void remap(byte xDim, byte yDim, UnwrapDirection direction) {
		ModelPanel mpanel = currentModelPanel();
		if (mpanel != null) {
			mpanel.getUndoManager().pushAction(new UVRemapAction(modelPanel.getModelView().getSelectedVertices(), 0, xDim, yDim, direction).redo());
		}
		repaint();
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

	public JToolBar createJToolBar() {
		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setFloatable(false);
		toolbar.addSeparator();
		undoAction = mainPanel.getUndoHandler().getUndoAction();
		toolbar.add(undoAction);

		redoAction = mainPanel.getUndoHandler().getRedoAction();
		toolbar.add(redoAction);

		toolbar.addSeparator();

		selectionModeGroup = new ToolbarButtonGroup2<>(toolbar, SelectionMode.values());
//		selectionModeGroup = new ToolbarButtonGroup<>(toolbar, SelectionMode.values());
		toolbar.addSeparator();
		selectionItemTypeGroup = new ToolbarButtonGroup2<>(toolbar,TVertexSelectionItemTypes.values() );
//		selectionItemTypeGroup = new ToolbarButtonGroup<>(toolbar, TVertexSelectionItemTypes.values());
		toolbar.addSeparator();
		actionTypeGroup = new ToolbarButtonGroup2<>(toolbar, ModelEditorActionType2.values());
//
//		TVertexToolbarActionButtonType selectAndMoveDescriptor = new TVertexToolbarActionButtonType("move2.png", "Select and Move", ModelEditorActionType.TRANSLATION);
//		TVertexToolbarActionButtonType selectAndRotateDescriptor = new TVertexToolbarActionButtonType("rotate.png", "Select and Rotate", ModelEditorActionType.ROTATION);
//		TVertexToolbarActionButtonType selectAndScaleDescriptor = new TVertexToolbarActionButtonType("scale.png", "Select and Scale", ModelEditorActionType.SCALING);
//
//		actionTypeGroup = new ToolbarButtonGroup<>(toolbar, new TVertexToolbarActionButtonType[] {selectAndMoveDescriptor, selectAndRotateDescriptor, selectAndScaleDescriptor});
//
//		currentActivity = actionTypeGroup.getActiveButtonType();
//		toolbar.addSeparator();

		JButton snapButton = toolbar.add(new AbstractAction("Snap", RMSIcons.loadToolBarImageIcon("snap.png")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				ModelPanel currentModelPanel = currentModelPanel();
				currentModelPanel.getUndoManager().pushAction(snapSelectedVertices());

			}
		});


		toolbar.setMaximumSize(new Dimension(80000, 48));

		return toolbar;
	}


	public UndoAction snapSelectedVertices() {
		Collection<? extends Vec2> selection = TVertexUtils.getTVertices(modelPanel.getModelView().getSelectedVertices(), 0);
		List<Vec2> oldLocations = new ArrayList<>();
		Vec2 cog = Vec2.centerOfGroup(selection);
		for (Vec2 vertex : selection) {
			oldLocations.add(new Vec2(vertex));
		}
		return new UVSnapAction(selection, oldLocations, cog).redo();
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

		createAndAddMenuItem("Invert Selection", "control I", e -> addUndoAction(invertSelection(modelPanel.getModelView())), editMenu);

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

			Collection<? extends Vec2> selectedTVertices = modelEditorManager.getSelectionView().getSelectedTVertices(currentLayer());
			for (Geoset g : mpanel.getModel().getGeosets()) {
				for (GeosetVertex gv : new ArrayList<>(g.getVertices())) {
					Vec2 tVertex = gv.getTVertex(currentLayer());
					if (selectedTVertices.contains(tVertex)) {
						Set<Triangle> triangles = gv.getTriangles();
						GeosetVertex newVertex = new GeosetVertex(gv);
						List<Triangle> trianglesToRemove = new ArrayList<>();
						for (Triangle triangle : triangles) {
							int vInd = triangle.indexOfRef(gv);
							if (vInd != -1) {
								triangle.set(vInd, newVertex);
								newVertex.addTriangle(triangle);
							}
							trianglesToRemove.add(triangle);
						}
						if (!newVertex.getTriangles().isEmpty()) {
							g.add(newVertex);
						}
						gv.getTriangles().removeAll(trianglesToRemove);
//						Iterator<Triangle> triangleIterator = triangles.iterator();
////						if (triangleIterator.hasNext()) {
////							triangleIterator.next(); // keep using gv in 1 triangle, but not more
////						}
//						while (triangleIterator.hasNext()) {
//							Triangle tri = triangleIterator.next();
//							int vertexIndex = tri.indexOfRef(gv);
//							if(vertexIndex != -1){
//								break;
//							}
//						}
//						while (triangleIterator.hasNext()) {
//							Triangle tri = triangleIterator.next();
//							int vertexIndex = tri.indexOfRef(gv);
//							if(vertexIndex != -1){
//								GeosetVertex newVertex = new GeosetVertex(gv);
//								tri.set(vertexIndex, newVertex);
//								newVertex.addTriangle(tri);
//								newVertex.getGeoset().add(newVertex);
//							}
//							triangleIterator.remove();
//						}
					}
				}
			}
//			mpanel.model
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
//		buttons.get(0).setColors(ProgramGlobals.getPrefs().getActiveColor1(), ProgramGlobals.getPrefs().getActiveColor2());
//		buttons.get(3).setColors(ProgramGlobals.getPrefs().getActiveRColor1(), ProgramGlobals.getPrefs().getActiveRColor2());

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
//				actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[0]);
				actionTypeGroup.setActiveButton(ModelEditorActionType2.TRANSLATION);
			}
		});

		root.getInputMap(isAncestor).put(KeyStroke.getKeyStroke("E"), "RotateKeyboardKey");
		root.getActionMap().put("RotateKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[1]);
				actionTypeGroup.setActiveButton(ModelEditorActionType2.ROTATION);
			}
		});

		root.getInputMap(isAncestor).put(KeyStroke.getKeyStroke("R"), "ScaleKeyboardKey");
		root.getActionMap().put("ScaleKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[2]);
				actionTypeGroup.setActiveButton(ModelEditorActionType2.SCALING);
			}
		});

//		SELECT("Select", RMSIcons.loadToolBarImageIcon("selectSingle.png")),
//				ADD("Add Selection", RMSIcons.loadToolBarImageIcon("selectAdd.png")),
//				DESELECT("Deselect", RMSIcons.loadToolBarImageIcon("selectRemove.png"));

		root.getInputMap(isAncestor).put(KeyStroke.getKeyStroke("A"), "SelectKeyboardKey");
		root.getActionMap().put("SelectKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				selectionItemTypeGroup.setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[0]);
				selectionItemTypeGroup.setActiveButton(TVertexSelectionItemTypes.VERTEX);
			}
		});
		root.getInputMap(isAncestor).put(KeyStroke.getKeyStroke("S"), "AddSelectKeyboardKey");
		root.getActionMap().put("AddSelectKeyboardKey", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				selectionItemTypeGroup.setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[1]);
				selectionItemTypeGroup.setActiveButton(TVertexSelectionItemTypes.FACE);
			}
		});

		root.getActionMap().put("shiftSelect", new AbstractAction("shiftSelect") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
//					selectionModeGroup.setToolbarButtonType(SelectionMode.ADD);
					selectionModeGroup.setActiveButton(SelectionMode.ADD);
					cheatShift = true;
				}
			}
		});
		root.getActionMap().put("altSelect", new AbstractAction("altSelect") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
//					selectionModeGroup.setToolbarButtonType(SelectionMode.DESELECT);
					selectionModeGroup.setActiveButton(SelectionMode.DESELECT);
					cheatAlt = true;
				}
			}
		});

		root.getActionMap().put("unShiftSelect", new AbstractAction("unShiftSelect") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ((selectionModeGroup.getActiveButtonType() == SelectionMode.ADD) && cheatShift) {
//					selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
					selectionModeGroup.setActiveButton(SelectionMode.SELECT);
					cheatShift = false;
				}
			}
		});
		root.getActionMap().put("unAltSelect", new AbstractAction("unAltSelect") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ((selectionModeGroup.getActiveButtonType() == SelectionMode.DESELECT) && cheatAlt) {
//					selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
					selectionModeGroup.setActiveButton(SelectionMode.SELECT);
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

		setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
	}

	private JComboBox<String> getTextureCombobox() {
		System.out.println("getComboBox!");
		DefaultListModel<Bitmap> bitmapListModel;

		List<Bitmap> bitmaps = new ArrayList<>(modelPanel.getModel().getTextures());
		List<String> bitmapNames = new ArrayList<>();

		for (Bitmap bitmap : bitmaps) {
			bitmapNames.add(bitmap.getName());
		}
		bitmaps.add(0, null);
		bitmapNames.add(0, "no image");

		JComboBox<String> jComboBox = new JComboBox<>(bitmapNames.toArray(String[]::new));
		jComboBox.setSelectedIndex(0);

		jComboBox.addActionListener(e -> {
			BufferedImage image = null;
			if (jComboBox.getSelectedItem() != null) {
				Bitmap bitmap = bitmaps.get(jComboBox.getSelectedIndex());
				if (bitmap != null) {
					image = BLPHandler.getImage(bitmap, modelPanel.getModel().getWrappedDataSource());
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
//		String.format(Locale.US,"", x)
		mouseCoordDisplay[0].setText(String.format(Locale.US, "%3.4f", x));
		mouseCoordDisplay[1].setText(String.format(Locale.US, "%3.4f", y));
//		mouseCoordDisplay[0].setText((float) x + "");
//		mouseCoordDisplay[1].setText((float) y + "");
	}

	public void setViewport(ModelPanel dispModel) {
		vp = new UVViewport(dispModel.getModelHandler(), this, viewportActivityManager, this,
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
		vp.getCoordinateSystem().translateZoomed(0, i);
//		vp.translate(0, i * (1 / vp.getZoom()));
		vp.repaint();
	}

	private void moveLeftRight(int i) {
		vp.getCoordinateSystem().translateZoomed(i, 0);
//		vp.translate(i * (1 / vp.getZoom()), 0);// *vp.getZoomAmount()
		vp.repaint();
	}

	private void zoom(double v) {
		if (v>0){
			vp.getCoordinateSystem().zoomIn(v);
		}else {
			vp.getCoordinateSystem().zoomOut(-v);

		}
//		vp.zoom(v);
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

	private void loadImage3() {
		FileDialog fileDialog = new FileDialog(this);
		Bitmap bitmap = fileDialog.importImage();
		if (bitmap != null) {
			setTextureAsBackground(BLPHandler.getImage(bitmap, modelPanel.getModel().getWrappedDataSource()));
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
		return modelPanel;
	}

	@Override
	public void notifyUpdate(byte dimension1, byte dimension2, double coord1, double coord2) {
		setMouseCoordDisplay(coord1, coord2);
	}

	//	@Override
	public void changeActivity(ModelEditorActionType2 newType) {
		viewportActivityManager.setCurrentActivity(createActivity(modelEditorManager, modelPanel.getModelHandler(), newType));
	}

	public ViewportActivity createActivity(TVertexEditorManager modelEditorManager, ModelHandler modelHandler, ModelEditorActionType2 editorActionType) {
		TVertexEditorManipulatorBuilder manipulatorBuilder = new TVertexEditorManipulatorBuilder(modelEditorManager, modelHandler, editorActionType);
		return new MultiManipulatorActivity(
				manipulatorBuilder,
				modelHandler,
				modelEditorManager);
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

}
