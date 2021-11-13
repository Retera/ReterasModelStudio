package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.SplitVertexAction;
import com.hiveworkshop.rms.editor.actions.uv.MirrorTVerticesAction;
import com.hiveworkshop.rms.editor.actions.uv.UVRemapAction;
import com.hiveworkshop.rms.editor.actions.uv.UVSnapAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.MultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.TVertexEditorManager;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexUtils;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.listener.ModelEditorChangeNotifier;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.TVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorWidgetType;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.TVertexSelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.ModeButton;
import com.hiveworkshop.rms.util.Vec2;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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

	private ModelHandler modelHandler;
	private final JTextField[] mouseCoordDisplay = new JTextField[2];
	private ViewportActivityManager viewportActivityManager;
	private TVertexEditorManager modelEditorManager;
	private UndoManager undoManager;
	private JCheckBoxMenuItem wrapImage;
	private ArrayList<ModeButton> buttons = new ArrayList<>();
	private JPanel zoomPanel;
	private JPanel navPanel;

	DefaultComboBoxModel<Bitmap> comboBoxModel;
	JComboBox<Bitmap> textureComboBox;


	private final UVViewport uvViewport;
	private final UVLinkActions uvLinkActions;

	public UVPanel() {
		this.uvLinkActions = new UVLinkActions(this);
		JToolBar toolbar = createJToolBar();

		setBorder(BorderFactory.createLineBorder(Color.black));// BorderFactory.createCompoundBorder(
		// BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title),BorderFactory.createBevelBorder(1)),BorderFactory.createEmptyBorder(1,1,1,1)));
		setOpaque(true);
		uvViewport = new UVViewport(this, this);
		add(uvViewport);

		setLayout(new MigLayout("fill", "[grow][]", "[][grow][]"));
		add(toolbar, "wrap, spanx");
		add(uvViewport, "grow");
		add(getStuffPanel(), "growy, wrap");
		add(getBotomPanel());
		setMinimumSize(new Dimension(200, 200));

		uvLinkActions.selectionItemTypeGroup.addToolbarButtonListener(newType -> {
			modelEditorManager.setSelectionItemType(newType);
			repaint();
		});

		uvLinkActions.actionTypeGroup.addToolbarButtonListener(newType -> {
			if (newType != null && modelEditorManager != null) {
				changeActivity(newType);
			}
		});
		uvLinkActions.actionTypeGroup.setActiveButton(ModelEditorWidgetType.TRANSLATION);

		getMenuHolderPanel();
	}

	public JPanel getMenuHolderPanel() {
		JPanel menuHolderPanel = new JPanel(new BorderLayout());
		menuHolderPanel.add(this, BorderLayout.CENTER);
		menuHolderPanel.add(createMenuBar(), BorderLayout.BEFORE_FIRST_LINE);
		return menuHolderPanel;
	}

	private JPanel getBotomPanel() {
		zoomPanel = new JPanel(new MigLayout("gap 0", "[]16[]"));
		zoomPanel.add(getButton(20, 20, "Plus.png", e -> zoom(1.15)));
		zoomPanel.add(getButton(20, 20, "Minus.png", e -> zoom(-1.15)));

		navPanel = new JPanel(new MigLayout("gap 0"));
		navPanel.add(getButton(32, 16, "ArrowUp.png", e -> moveUpDown(20)), "cell 1 0");
		navPanel.add(getButton(32, 16, "ArrowDown.png", e -> moveUpDown(-20)), "cell 0 1");
		navPanel.add(getButton(16, 32, "ArrowRight.png", e -> moveLeftRight(-20)), "cell 2 1");
		navPanel.add(getButton(32, 16, "ArrowDown.png", e -> moveUpDown(-20)), "cell 1 2");


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
		unwrapButton.addActionListener(e -> unwrapFromView((UnwrapDirection) unwrapDirectionBox.getSelectedItem()));

		buttons.add(loadImage);
		buttons.add(unwrapButton);

		unwrapDirectionBox.setMaximumSize(new Dimension(100, 35));
		unwrapDirectionBox.setMinimumSize(new Dimension(90, 15));


		comboBoxModel = new DefaultComboBoxModel<>();
		textureComboBox = getTextureComboBox(comboBoxModel);

		// ToDo the texture combo box should maybe be limited in size and/or moved to a better spot to allow to view longer strings
		JPanel stuffPanel = new JPanel(new MigLayout("wrap 1, gap 0"));
		stuffPanel.add(loadImage);
		stuffPanel.add(textureComboBox);
		stuffPanel.add(divider[0]);
		stuffPanel.add(uvLinkActions.selectionModeGroup.getModeButton(SelectionMode.SELECT));
		stuffPanel.add(uvLinkActions.selectionModeGroup.getModeButton(SelectionMode.ADD));
		stuffPanel.add(uvLinkActions.selectionModeGroup.getModeButton(SelectionMode.DESELECT));
		stuffPanel.add(divider[1]);
		stuffPanel.add(uvLinkActions.actionTypeGroup.getModeButton(ModelEditorWidgetType.TRANSLATION));
		stuffPanel.add(uvLinkActions.actionTypeGroup.getModeButton(ModelEditorWidgetType.ROTATION));
		stuffPanel.add(uvLinkActions.actionTypeGroup.getModeButton(ModelEditorWidgetType.SCALING));
		stuffPanel.add(divider[2]);
		stuffPanel.add(unwrapDirectionBox);
		stuffPanel.add(unwrapButton);
		return stuffPanel;
	}

	private void mirror(byte i) {
		if (modelHandler != null) {
			Vec2 selectionCenter = modelEditorManager.getSelectionView().getUVCenter(0);

			Collection<Vec2> tVertices = TVertexUtils.getTVertices(modelHandler.getModelView().getSelectedVertices(), 0);
			UndoAction mirror = new MirrorTVerticesAction(tVertices, i, selectionCenter.x, selectionCenter.y).redo();
			modelHandler.getUndoManager().pushAction(mirror);
		}
		repaint();
	}

	private void unwrapFromView(UnwrapDirection unwrapDirection) {
		if (unwrapDirection != null) {
			switch (unwrapDirection) {
				case BOTTOM -> remap((byte) 1, (byte) 0, unwrapDirection);
				case FRONT -> remap((byte) 1, (byte) 2, unwrapDirection);
				case RIGHT -> remap((byte) 0, (byte) 2, unwrapDirection);
//                    case PERSPECTIVE -> System.out.println("ugg");
			}
		} else {
			JOptionPane.showMessageDialog(UVPanel.this, "Please select a direction", "Error",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	protected void remap(byte xDim, byte yDim, UnwrapDirection direction) {
		if (modelHandler != null) {
			modelHandler.getUndoManager().pushAction(new UVRemapAction(modelHandler.getModelView().getSelectedVertices(), 0, xDim, yDim, direction).redo());
		}
		repaint();
	}

	public boolean isWrapImage() {
		return wrapImage.isSelected();
	}

	private JButton getButton(int width, int height, String iconPath, ActionListener actionListener) {
		Dimension dim = new Dimension(width, height);
		JButton button = new JButton("");
		button.setMaximumSize(dim);
		button.setMinimumSize(dim);
		button.setPreferredSize(dim);
		button.setIcon(new ImageIcon(RMSIcons.loadDeprecatedImage(iconPath)));
		button.addActionListener(actionListener);
		return button;
	}

	public JToolBar createJToolBar() {
		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setFloatable(false);
		toolbar.addSeparator();

		toolbar.add(uvLinkActions.undoAction);
		toolbar.add(uvLinkActions.redoAction);

		toolbar.addSeparator();

		uvLinkActions.selectionModeGroup = new ToolbarButtonGroup2<>(toolbar, SelectionMode.values());
		uvLinkActions.selectionModeGroup.setActiveButton(SelectionMode.SELECT);
		toolbar.addSeparator();
		uvLinkActions.selectionItemTypeGroup = new ToolbarButtonGroup2<>(toolbar, TVertexSelectionItemTypes.values());
		uvLinkActions.selectionItemTypeGroup.setActiveButton(TVertexSelectionItemTypes.VERTEX);
		toolbar.addSeparator();
		uvLinkActions.actionTypeGroup = new ToolbarButtonGroup2<>(toolbar, ModelEditorWidgetType.values());
		uvLinkActions.actionTypeGroup.setActiveButton(ModelEditorWidgetType.TRANSLATION);

		JButton snapButton = toolbar.add(new AbstractAction("Snap", RMSIcons.loadToolBarImageIcon("snap.png")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(modelHandler != null){
					modelHandler.getUndoManager().pushAction(snapSelectedVertices());
				}
			}
		});


		toolbar.setMaximumSize(new Dimension(80000, 48));

		return toolbar;
	}


	public UndoAction snapSelectedVertices() {
		Collection<Vec2> selection = TVertexUtils.getTVertices(modelHandler.getModelView().getSelectedVertices(), 0);
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

		createAndAddMenuItem("Select All", "control A", uvLinkActions.selectAllAction, editMenu);
		createAndAddMenuItem("Invert Selection", "control I", uvLinkActions.invertSelectAction, editMenu);
		createAndAddMenuItem("Expand Selection", "control E", uvLinkActions.expandSelectionAction, editMenu);
		createAndAddMenuItem("Select from Viewer", "control V", uvLinkActions.selFromMainAction, editMenu);

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
		if (modelHandler != null) {
			new SplitVertexAction(ModelStructureChangeListener.changeListener, modelHandler.getModelView());
		}
		repaint();
	}

	public void setControlsVisible(boolean flag) {
		navPanel.setVisible(flag);
		zoomPanel.setVisible(flag);
	}

	public void initViewport() {
		uvViewport.setAspectRatio(1);
		uvViewport.revalidate();
	}

	public void init() {
		System.out.println("UVPanel: initiating");
		uvViewport.init();
		System.out.println("UVPanel: vp initiated, setting controls visibility");

//		JRootPane root = getRootPane();

//		uvLinkActions.linkActions(root, this);

		setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
		System.out.println("UVPanel: controls visibility set");
	}

	private JComboBox<Bitmap> getTextureComboBox(DefaultComboBoxModel<Bitmap> comboBoxModel) {
		JComboBox<Bitmap> jComboBox = new JComboBox<>(comboBoxModel);
		jComboBox.addActionListener(e -> chooseImage(comboBoxModel));
		return jComboBox;
	}

	private void chooseImage(DefaultComboBoxModel<Bitmap> comboBoxModel) {
		BufferedImage image = null;
		if (comboBoxModel.getSelectedItem() != null) {
			Bitmap bitmap = (Bitmap) comboBoxModel.getSelectedItem();
			if (bitmap != null) {
				image = BLPHandler.getImage(bitmap, modelHandler.getModel().getWrappedDataSource());
			}
		}
		setTextureAsBackground(image);
	}


	public void setMouseCoordDisplay(double x, double y) {
//		String.format(Locale.US,"", x)
		mouseCoordDisplay[0].setText(String.format(Locale.US, "%3.4f", x));
		mouseCoordDisplay[1].setText(String.format(Locale.US, "%3.4f", y));
//		mouseCoordDisplay[0].setText((float) x + "");
//		mouseCoordDisplay[1].setText((float) y + "");
	}

	public UVPanel setModel(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		undoManager = modelHandler.getUndoManager();
		viewportActivityManager = new ViewportActivityManager(null);

		ModelEditorChangeNotifier modelEditorChangeNotifier = new ModelEditorChangeNotifier();
		modelEditorChangeNotifier.subscribe(viewportActivityManager);

		modelEditorManager = new TVertexEditorManager(this.modelHandler, uvLinkActions.selectionModeGroup, modelEditorChangeNotifier, viewportActivityManager);
		uvViewport.setModel(this.modelHandler, viewportActivityManager);

		comboBoxModel.removeAllElements();
		comboBoxModel.addAll(modelHandler.getModel().getTextures());
		textureComboBox.setRenderer(new TextureListRenderer(modelHandler.getModel()));
		if (comboBoxModel.getSize() > 0) {
			textureComboBox.setSelectedIndex(0);
		}

		return this;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	private void moveUpDown(int i) {
		uvViewport.getCoordinateSystem().translateZoomed(0, i);
		uvViewport.repaint();
	}

	private void moveLeftRight(int i) {
		uvViewport.getCoordinateSystem().translateZoomed(i, 0);
		uvViewport.repaint();
	}

	private void zoom(double v) {
		if (v > 0) {
			uvViewport.getCoordinateSystem().zoomIn(v);
		} else {
			uvViewport.getCoordinateSystem().zoomOut(-v);

		}
		uvViewport.repaint();
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
		uvViewport.setAspectRatio((Integer) widthVal.getValue() / (double) (Integer) heightVal.getValue());
	}

	private void loadImage3() {
		FileDialog fileDialog = new FileDialog(this);
		Bitmap bitmap = fileDialog.importImage();
		if (bitmap != null) {
			setTextureAsBackground(BLPHandler.getImage(bitmap, modelHandler.getModel().getWrappedDataSource()));
		}
	}

	private void setTextureAsBackground(BufferedImage image) {
		uvViewport.clearBackgroundImage();
		if (image != null) {
			uvViewport.addBackgroundImage(image);
		}
		uvViewport.repaint();
	}

	public ImageIcon getImageIcon() {
		return new ImageIcon(uvViewport.getBufferedImage());
	}

	public BufferedImage getBufferedImage() {
		return uvViewport.getBufferedImage();
	}

	/**
	 * A method defining the currently selected UV layer.
	 */
	public int currentLayer() {
		int uvLayerIndex = 0;
		return uvLayerIndex;
	}

	@Override
	public void notifyUpdate(CoordinateSystem coordinateSystem, double coord1, double coord2) {
		setMouseCoordDisplay(coord1, coord2);
	}

	//	@Override
	public void changeActivity(ModelEditorWidgetType newType) {
		ViewportActivity activity = createActivity(modelEditorManager, modelHandler, newType);
		viewportActivityManager.setCurrentActivity(activity);
	}

	public ViewportActivity createActivity(TVertexEditorManager modelEditorManager, ModelHandler modelHandler, ModelEditorWidgetType editorActionType) {
		TVertexEditorManipulatorBuilder manipulatorBuilder = new TVertexEditorManipulatorBuilder(modelEditorManager, modelHandler, editorActionType);
		return new MultiManipulatorActivity(manipulatorBuilder, modelHandler, modelEditorManager);
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
