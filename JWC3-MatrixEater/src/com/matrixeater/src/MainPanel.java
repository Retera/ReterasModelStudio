package com.matrixeater.src;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordDisplayListener;
import com.hiveworkshop.wc3.gui.modeledit.ImportPanel;
import com.hiveworkshop.wc3.gui.modeledit.MDLDisplay;
import com.hiveworkshop.wc3.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.wc3.gui.modeledit.ModeButton;
import com.hiveworkshop.wc3.gui.modeledit.ModelPanel;
import com.hiveworkshop.wc3.gui.modeledit.PerspDisplayPanel;
import com.hiveworkshop.wc3.gui.modeledit.UVPanel;
import com.hiveworkshop.wc3.gui.modeledit.UndoHandler;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarActionButtonType;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.gui.modeledit.viewport.IconUtils;
import com.hiveworkshop.wc3.jworldedit.models.UnitEditorModelSelector;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.AnimFlag.Entry;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.MDXHandler;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.UVLayer;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.VisibilitySource;
import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.units.Element;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.ModelOptionPane;
import com.hiveworkshop.wc3.units.ModelOptionPane.ModelElement;
import com.hiveworkshop.wc3.units.UnitOptionPane;
import com.hiveworkshop.wc3.user.DirectorySelector;
import com.hiveworkshop.wc3.user.SaveProfile;
import com.hiveworkshop.wc3.util.Callback;
import com.matrixeater.imp.ImportPanelSimple;
import com.matrixeater.src.viewer.AnimationViewer;
import com.owens.oobjloader.builder.Build;
import com.owens.oobjloader.parser.Parse;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

/**
 * Write a description of class MainPanel here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
/**
 * Write a description of class MainPanel here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class MainPanel extends JPanel implements ActionListener, MouseListener, ChangeListener, UndoHandler {
	ModeButton selectButton, addButton, deselectButton, moveButton, rotateButton, scaleButton, extrudeButton,
			extendButton, snapButton, deleteButton, cloneButton, xButton, yButton, zButton;
	ArrayList<ModeButton> buttons = new ArrayList<>();
	JMenuBar menuBar;
	JMenu fileMenu, recentMenu, editMenu, modelMenu, mirrorSubmenu, tweaksSubmenu, viewMenu, importMenu, addMenu,
			windowMenu, addParticle, animationMenu, singleAnimationMenu, aboutMenu, fetch;
	JCheckBoxMenuItem mirrorFlip, fetchPortraitsToo, showNormals, useNativeMDXParser, textureModels,
			showVertexModifyControls;
	ArrayList geoItems = new ArrayList();
	JMenuItem newModel, open, fetchUnit, fetchModel, fetchObject, save, showController, mergeGeoset, saveAs,
			importButton, importUnit, importGameModel, importGameObject, importFromWorkspace, importButtonS,
			newDirectory, creditsButton, clearRecent, nullmodelButton, selectAll, invertSelect, expandSelection,
			snapNormals, flipAllUVsU, flipAllUVsV, inverseAllUVs, mirrorX, mirrorY, mirrorZ, insideOut, showMatrices,
			editUVs, exportTextures, scaleAnimations, animationViewer, linearizeAnimations, simplifyKeyframes,
			riseFallBirth, animFromFile, animFromUnit, animFromModel, animFromObject, teamColor, teamGlow;
	List<RecentItem> recentItems = new ArrayList<>();
	UndoMenuItem undo;
	RedoMenuItem redo;

	JMenu viewMode;
	JRadioButtonMenuItem wireframe, solid;
	ButtonGroup viewModes;

	JFileChooser fc, exportTextureDialog;
	FileFilter filter;
	File filterFile;
	File currentFile;
	ImportPanel importPanel;
	static final ImageIcon MDLIcon = new ImageIcon(MainPanel.class.getResource("ImageBin/MDLIcon_16.png"));
	public static final ImageIcon AnimIcon = new ImageIcon(MainPanel.class.getResource("ImageBin/Anim.png"));
	boolean loading;
	JTabbedPane tabbedPane;
	ViewController geoControl;
	JTextField[] mouseCoordDisplay = new JTextField[3];
	boolean cheatShift = false;
	boolean cheatAlt = false;
	SaveProfile profile = SaveProfile.get();
	ProgramPreferences prefs = profile.getPreferences();// new
														// ProgramPreferences();

	JToolBar toolbar;

	public boolean showNormals() {
		return showNormals.isSelected();
	}

	public boolean showVMControls() {
		return showVertexModifyControls.isSelected();
	}

	public boolean textureModels() {
		return textureModels.isSelected();
	}

	public int viewMode() {
		if (wireframe.isSelected()) {
			return 0;
		} else if (solid.isSelected()) {
			return 1;
		}
		return -1;
	}

	public void setCloneOn(final boolean flag) {
		prefs.setCloneOn(flag);
		// cloneOn = flag;
		if (flag) {
			cloneButton.setColors(prefs.getActiveBColor1(), prefs.getActiveBColor2());
		} else {
			cloneButton.resetColors();
			System.out.println("reset???");
		}
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		if (!prefs.isCloneOn() && cloneButton.isColorModeActive()) {
			cloneButton.resetColors();
		}
	}

	public ModeButton getDLockButton(final int x) {
		switch (x) {
		case 0:
			return zButton;
		case 1:
			return xButton;
		case 2:
			return yButton;
		}
		return null;
	}

	public void setDimLock(final int x, final boolean flag) {
		prefs.setDimLock(x, flag);
		if (prefs.getDimLock(x)) {
			getDLockButton(x).setColors(prefs.getActiveBColor1(), prefs.getActiveBColor2());
		} else {
			getDLockButton(x).resetColors();
		}
	}

	JMenuItem contextClose, contextCloseAll;
	int contextClickedTab = 0;
	JPopupMenu contextMenu;
	AbstractAction undoAction = new AbstractAction("Undo") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = ((ModelPanel) tabbedPane.getSelectedComponent());
			if (mpanel != null) {
				mpanel.getMDLDisplay().undo();
			}
			refreshUndo();
			repaint();
			geoControl.repaint();
			mpanel.getMDLDisplay().notifyUpdate();
		}
	};
	AbstractAction redoAction = new AbstractAction("Redo") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = ((ModelPanel) tabbedPane.getSelectedComponent());
			if (mpanel != null) {
				mpanel.getMDLDisplay().redo();
			}
			refreshUndo();
			repaint();
			geoControl.repaint();
			mpanel.getMDLDisplay().notifyUpdate();
		}
	};
	AbstractAction deleteAction = new AbstractAction("Delete") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = ((ModelPanel) tabbedPane.getSelectedComponent());
			if (mpanel != null) {
				mpanel.getMDLDisplay().delete();
			}
			repaint();
			geoControl.repaint();
			mpanel.getMDLDisplay().notifyUpdate();
		}
	};
	AbstractAction selectAllAction = new AbstractAction("Select All") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = ((ModelPanel) tabbedPane.getSelectedComponent());
			if (mpanel != null) {
				mpanel.getMDLDisplay().selectAll();
			}
			repaint();
		}
	};
	AbstractAction invertSelectAction = new AbstractAction("Invert Selection") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = ((ModelPanel) tabbedPane.getSelectedComponent());
			if (mpanel != null) {
				mpanel.getMDLDisplay().invertSelection();
			}
			repaint();
		}
	};
	AbstractAction expandSelectionAction = new AbstractAction("Expand Selection") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = ((ModelPanel) tabbedPane.getSelectedComponent());
			if (mpanel != null) {
				mpanel.getMDLDisplay().expandSelection();
			}
			repaint();
		}
	};
	AbstractAction snapNormalsAction = new AbstractAction("Expand Selection") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = ((ModelPanel) tabbedPane.getSelectedComponent());
			if (mpanel != null) {
				mpanel.getMDLDisplay().snapNormals();
			}
			repaint();
		}
	};
	AbstractAction flipAllUVsUAction = new AbstractAction("Flip All UVs U") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			for (final Geoset geo : currentMDL().getGeosets()) {
				for (final UVLayer layer : geo.getUVLayers()) {
					for (int i = 0; i < layer.numTVerteces(); i++) {
						final TVertex tvert = layer.getTVertex(i);
						tvert.y = 1.0 - tvert.y;
					}
				}
			}
			repaint();
		}
	};
	AbstractAction flipAllUVsVAction = new AbstractAction("Flip All UVs V") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			for (final Geoset geo : currentMDL().getGeosets()) {
				for (final UVLayer layer : geo.getUVLayers()) {
					for (int i = 0; i < layer.numTVerteces(); i++) {
						final TVertex tvert = layer.getTVertex(i);
						tvert.y = 1.0 - tvert.y;
					}
				}
			}
			repaint();
		}
	};
	AbstractAction inverseAllUVsAction = new AbstractAction("Swap UVs U for V") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			for (final Geoset geo : currentMDL().getGeosets()) {
				for (final UVLayer layer : geo.getUVLayers()) {
					for (int i = 0; i < layer.numTVerteces(); i++) {
						final TVertex tvert = layer.getTVertex(i);
						final double temp = tvert.x;
						tvert.x = tvert.y;
						tvert.y = temp;
					}
				}
			}
			repaint();
		}
	};
	AbstractAction mirrorXAction = new AbstractAction("Mirror X") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = ((ModelPanel) tabbedPane.getSelectedComponent());
			if (mpanel != null) {
				mpanel.getMDLDisplay().mirror((byte) 1, mirrorFlip.isSelected());
			}
			repaint();
		}
	};
	AbstractAction mirrorYAction = new AbstractAction("Mirror Y") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = ((ModelPanel) tabbedPane.getSelectedComponent());
			if (mpanel != null) {
				mpanel.getMDLDisplay().mirror((byte) 2, mirrorFlip.isSelected());
			}
			repaint();
		}
	};
	AbstractAction mirrorZAction = new AbstractAction("Mirror Z") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = ((ModelPanel) tabbedPane.getSelectedComponent());
			if (mpanel != null) {
				mpanel.getMDLDisplay().mirror((byte) 0, mirrorFlip.isSelected());
			}
			repaint();
		}
	};
	AbstractAction insideOutAction = new AbstractAction("Inside Out") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = ((ModelPanel) tabbedPane.getSelectedComponent());
			if (mpanel != null) {
				mpanel.getMDLDisplay().insideOut();
			}
			repaint();
		}
	};
	AbstractAction viewMatricesAction = new AbstractAction("View Matrices") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final ModelPanel mpanel = ((ModelPanel) tabbedPane.getSelectedComponent());
			if (mpanel != null) {
				mpanel.getMDLDisplay().viewMatrices();
			}
			repaint();
		}
	};
	AbstractAction openAnimationViewerAction = new AbstractAction("Open Animation Viewer") {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final AnimationViewer animationViewer = new AnimationViewer(currentMDLDisp());
			final JFrame frame = new JFrame("Animation Viewer: " + currentMDL().getName());
			frame.setIconImage(MainFrame.frame.getIconImage());
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setContentPane(animationViewer);
			frame.pack();
			frame.setLocationRelativeTo(MainPanel.this);
			frame.setVisible(true);
		}
	};
	private ToolbarButtonGroup<SelectionItemTypes> selectionItemTypeGroup;
	private ToolbarButtonGroup<SelectionMode> selectionModeGroup;
	private ToolbarButtonGroup<ToolbarActionButtonType> actionTypeGroup;

	public MainPanel() {
		super();

		add(createJToolBar());
		// testArea = new PerspDisplayPanel("Graphic Test",2,0);
		// //botArea.setViewport(0,1);
		// add(testArea);

		selectButton = new ModeButton("Select");
		addButton = new ModeButton("Add");
		deselectButton = new ModeButton("Deselect");
		final JLabel[] divider = new JLabel[3];
		for (int i = 0; i < divider.length; i++) {
			divider[i] = new JLabel("----------");
		}
		for (int i = 0; i < mouseCoordDisplay.length; i++) {
			mouseCoordDisplay[i] = new JTextField("");
			mouseCoordDisplay[i].setMaximumSize(new Dimension(80, 18));
			mouseCoordDisplay[i].setMinimumSize(new Dimension(50, 15));
			mouseCoordDisplay[i].setEditable(false);
		}
		moveButton = new ModeButton("Move");
		rotateButton = new ModeButton("Rotate");
		scaleButton = new ModeButton("Scale");
		extrudeButton = new ModeButton("Extrude");
		extendButton = new ModeButton("Extend");
		extendButton.setToolTipText("A modified version of extrude that favors creating less faces.");
		snapButton = new ModeButton("Snap");
		deleteButton = new ModeButton("Delete");
		cloneButton = new ModeButton("Clone");
		xButton = new ModeButton("X");
		yButton = new ModeButton("Y");
		zButton = new ModeButton("Z");

		contextMenu = new JPopupMenu();
		contextClose = new JMenuItem("Close");
		contextClose.addActionListener(this);
		contextMenu.add(contextClose);

		contextCloseAll = new JMenuItem("Close All");
		contextCloseAll.addActionListener(this);
		contextMenu.add(contextCloseAll);

		buttons.add(selectButton);
		buttons.add(addButton);
		buttons.add(deselectButton);
		buttons.add(moveButton);
		buttons.add(rotateButton);
		buttons.add(scaleButton);
		buttons.add(extrudeButton);
		buttons.add(extendButton);
		buttons.add(snapButton);
		buttons.add(deleteButton);
		buttons.add(cloneButton);

		xButton.addActionListener(this);
		xButton.setMaximumSize(new Dimension(26, 26));
		xButton.setMinimumSize(new Dimension(20, 20));
		xButton.setMargin(new Insets(0, 0, 0, 0));
		yButton.addActionListener(this);
		yButton.setMaximumSize(new Dimension(26, 26));
		yButton.setMinimumSize(new Dimension(20, 20));
		yButton.setMargin(new Insets(0, 0, 0, 0));
		zButton.addActionListener(this);
		zButton.setMaximumSize(new Dimension(26, 26));
		zButton.setMinimumSize(new Dimension(20, 20));
		zButton.setMargin(new Insets(0, 0, 0, 0));

		for (int i = 0; i < buttons.size(); i++) {
			buttons.get(i).setMaximumSize(new Dimension(100, 35));
			buttons.get(i).setMinimumSize(new Dimension(90, 15));
			if (buttons.get(i) != deleteButton) {
				buttons.get(i).addActionListener(this);
			} else {
				buttons.get(i).addActionListener(deleteAction);
			}
		}

		tabbedPane = new DnDTabbedPane();
		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(toolbar)
						.addComponent(tabbedPane)
						.addGroup(layout.createSequentialGroup().addComponent(mouseCoordDisplay[0])
								.addComponent(mouseCoordDisplay[1]).addComponent(mouseCoordDisplay[2])))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(selectButton)
						.addComponent(addButton).addComponent(deselectButton).addComponent(divider[0])
						.addComponent(moveButton).addComponent(rotateButton).addComponent(scaleButton)
						.addComponent(extrudeButton).addComponent(extendButton).addComponent(divider[1])
						.addComponent(snapButton).addComponent(deleteButton).addComponent(cloneButton)
						.addComponent(divider[2]).addGroup(layout.createSequentialGroup().addComponent(xButton)
								.addComponent(yButton).addComponent(zButton))));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(toolbar)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup().addComponent(tabbedPane)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(mouseCoordDisplay[0]).addComponent(mouseCoordDisplay[1])
										.addComponent(mouseCoordDisplay[2])))
						.addGroup(layout.createSequentialGroup().addComponent(selectButton).addGap(8)
								.addComponent(addButton).addGap(8).addComponent(deselectButton).addGap(8)
								.addComponent(divider[0]).addGap(8).addComponent(moveButton).addGap(8)
								.addComponent(rotateButton).addGap(8).addComponent(scaleButton).addGap(8)
								.addComponent(extrudeButton).addGap(8).addComponent(extendButton).addGap(8)
								.addComponent(divider[1]).addGap(8).addComponent(snapButton).addGap(8)
								.addComponent(deleteButton).addGap(8).addComponent(cloneButton).addGap(8)
								.addComponent(divider[2]).addGap(8)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(xButton)
										.addComponent(yButton).addComponent(zButton)))));
		setLayout(layout);
		// Create a file chooser
		fc = new JFileChooser();
		filterFile = new File("", ".mdl");
		filter = new MDLFilter();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(filter);
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Binary Model", "mdx"));
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Wavefront OBJ", "obj"));
		exportTextureDialog = new JFileChooser();
		exportTextureDialog.setDialogTitle("Export Texture");
		final String[] imageTypes = ImageIO.getWriterFileSuffixes();
		for (final String suffix : imageTypes) {
			exportTextureDialog
					.addChoosableFileFilter(new FileNameExtensionFilter(suffix.toUpperCase() + " Image File", suffix));
		}
		tabbedPane.addChangeListener(this);

		// getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
		// Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Redo" );

		// getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
		// Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Undo" );

		tabbedPane.addMouseListener(this);
		// setFocusable(true);
		// selectButton.requestFocus();
	}

	public JToolBar createJToolBar() {
		toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setFloatable(false);
		toolbar.add(new AbstractAction("New", IconUtils.loadImageIcon("icons/actions/new.png")) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				newModel();
			}
		});
		toolbar.add(new AbstractAction("Open", IconUtils.loadImageIcon("icons/actions/open.png")) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				onClickOpen();
			}
		});
		toolbar.add(new AbstractAction("Save", IconUtils.loadImageIcon("icons/actions/save.png")) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				onClickSave();
			}
		});
		toolbar.addSeparator();
		toolbar.add(new AbstractAction("Undo", IconUtils.loadImageIcon("icons/actions/undo.png")) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				currentMDLDisp().undo();
			}
		});
		toolbar.add(new AbstractAction("Redo", IconUtils.loadImageIcon("icons/actions/redo.png")) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				currentMDLDisp().redo();
			}
		});
		toolbar.addSeparator();
		selectionModeGroup = new ToolbarButtonGroup<>(toolbar, SelectionMode.values());
		toolbar.addSeparator();
		selectionItemTypeGroup = new ToolbarButtonGroup<>(toolbar, SelectionItemTypes.values());
		toolbar.addSeparator();
		actionTypeGroup = new ToolbarButtonGroup<>(toolbar, new ToolbarActionButtonType[] {
				new ToolbarActionButtonType(IconUtils.loadImageIcon("icons/actions/move2.png"), "Select and Move"),
				new ToolbarActionButtonType(IconUtils.loadImageIcon("icons/actions/rotate.png"), "Select and Rotate"),
				new ToolbarActionButtonType(IconUtils.loadImageIcon("icons/actions/scale.png"), "Select and Scale"),
				new ToolbarActionButtonType(IconUtils.loadImageIcon("icons/actions/extrude.png"), "Select and Extrude"),
				new ToolbarActionButtonType(IconUtils.loadImageIcon("icons/actions/extend.png"),
						"Select and Extend"), });
		return toolbar;
	}

	public void init() {
		buttons.get(0).setColors(prefs.getActiveColor1(), prefs.getActiveColor2());
		buttons.get(3).setColors(prefs.getActiveRColor1(), prefs.getActiveRColor2());
		final JRootPane root = getRootPane();
		// JPanel root = this;
		root.getActionMap().put("Undo", undoAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Z"),
				"Undo");

		root.getActionMap().put("Redo", redoAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Y"),
				"Redo");

		root.getActionMap().put("Delete", deleteAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DELETE"), "Delete");

		root.getActionMap().put("shiftSelect", new AbstractAction("shiftSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (prefs.getSelectionType() == 0) {
					for (int b = 0; b < 3; b++) {
						buttons.get(b).resetColors();
					}
					addButton.setColors(prefs.getActiveColor1(), prefs.getActiveColor2());
					prefs.setSelectionType(1);
					cheatShift = true;
				}
			}
		});
		root.getActionMap().put("altSelect", new AbstractAction("altSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (prefs.getSelectionType() == 0) {
					for (int b = 0; b < 3; b++) {
						buttons.get(b).resetColors();
					}
					deselectButton.setColors(prefs.getActiveColor1(), prefs.getActiveColor2());
					prefs.setSelectionType(2);
					cheatAlt = true;
				}
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke("shift pressed SHIFT"), "shiftSelect");
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke("control pressed CONTROL"), "shiftSelect");
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("alt pressed ALT"),
				"altSelect");

		root.getActionMap().put("unShiftSelect", new AbstractAction("unShiftSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (prefs.getSelectionType() == 1 && cheatShift) {
					for (int b = 0; b < 3; b++) {
						buttons.get(b).resetColors();
					}
					selectButton.setColors(prefs.getActiveColor1(), prefs.getActiveColor2());
					prefs.setSelectionType(0);
					cheatShift = false;
				}
			}
		});
		root.getActionMap().put("unAltSelect", new AbstractAction("unAltSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (prefs.getSelectionType() == 2 && cheatAlt) {
					for (int b = 0; b < 3; b++) {
						buttons.get(b).resetColors();
					}
					selectButton.setColors(prefs.getActiveColor1(), prefs.getActiveColor2());
					prefs.setSelectionType(0);
					cheatAlt = false;
				}
			}
		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released SHIFT"),
				"unShiftSelect");
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released CONTROL"),
				"unShiftSelect");
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released ALT"),
				"unAltSelect");

		root.getActionMap().put("Select All", selectAllAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control A"),
				"Select All");

		root.getActionMap().put("Invert Selection", invertSelectAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control I"),
				"Invert Selection");

		root.getActionMap().put("Expand Selection", expandSelectionAction);
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control E"),
				"Expand Selection");

		// prefs.setShowVertexModifierControls(showVertexModifyControls.isSelected());
		showVertexModifyControls.setSelected(prefs.isShowVertexModifierControls());
		// prefs.setTextureModels(textureModels.isSelected());
		textureModels.setSelected(prefs.isTextureModels());
		// prefs.setShowNormals(showNormals.isSelected());
		showNormals.setSelected(prefs.isShowNormals());
		// prefs.setLoadPortraits(true);
		fetchPortraitsToo.setSelected(prefs.isLoadPortraits());
		useNativeMDXParser.setSelected(prefs.isUseNativeMDXParser());
		// prefs.setUseNativeMDXParser(useNativeMDXParser.isSelected());
		switch (prefs.getViewMode()) {
		case 0:
			wireframe.setSelected(true);
			break;
		case 1:
			solid.setSelected(true);
			break;
		default:
			break;
		}
		// if( wireframe.isSelected() ){
		// prefs.setViewMode(0);
		// }
		// else if( solid.isSelected() ){
		// prefs.setViewMode(1);
		// }
		// else {
		// prefs.setViewMode(-1);
		// }
	}

	public JMenuBar createMenuBar() {
		// Create my menu bar
		menuBar = new JMenuBar();

		// Build the file menu
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.getAccessibleContext()
				.setAccessibleDescription("Allows the user to open, save, close, and manipulate files.");
		menuBar.add(fileMenu);

		recentMenu = new JMenu("Recent");
		recentMenu.setMnemonic(KeyEvent.VK_R);
		recentMenu.getAccessibleContext().setAccessibleDescription("Allows you to access recently opened files.");
		menuBar.add(recentMenu);

		editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		editMenu.addMouseListener(this);
		editMenu.getAccessibleContext()
				.setAccessibleDescription("Allows the user to use various tools to edit the currently selected model.");
		menuBar.add(editMenu);

		modelMenu = new JMenu("Tools");
		modelMenu.setMnemonic(KeyEvent.VK_T);
		modelMenu.getAccessibleContext().setAccessibleDescription(
				"Allows the user to use various model editing tools. (You must open a model before you may use this menu.)");
		modelMenu.setEnabled(false);
		menuBar.add(modelMenu);

		viewMenu = new JMenu("View");
		viewMenu.setMnemonic(KeyEvent.VK_V);
		viewMenu.getAccessibleContext().setAccessibleDescription("Allows the user to control view settings.");
		menuBar.add(viewMenu);

		windowMenu = new JMenu("Window");
		windowMenu.setMnemonic(KeyEvent.VK_W);
		windowMenu.getAccessibleContext()
				.setAccessibleDescription("Allows the user to open various windows containing the program features.");
		menuBar.add(windowMenu);

		animationViewer = new JMenuItem("Animation Viewer");
		animationViewer.setMnemonic(KeyEvent.VK_A);
		animationViewer.addActionListener(openAnimationViewerAction);
		windowMenu.add(animationViewer);

		addMenu = new JMenu("Add");
		addMenu.setMnemonic(KeyEvent.VK_A);
		addMenu.getAccessibleContext().setAccessibleDescription("Allows the user to add new components to the model.");
		menuBar.add(addMenu);

		addParticle = new JMenu("Particle");
		addParticle.setMnemonic(KeyEvent.VK_P);
		addMenu.add(addParticle);

		final File stockFolder = new File("stock/particles");
		final File[] stockFiles = stockFolder.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(final File dir, final String name) {
				return name.endsWith(".mdx");
			}
		});
		for (final File file : stockFiles) {
			final String basicName = file.getName().split("\\.")[0];
			final File pngImage = new File(file.getParent() + File.separatorChar + basicName + ".png");
			if (pngImage.exists()) {
				try {
					final Image image = ImageIO.read(pngImage);
					final JMenuItem particleItem = new JMenuItem(basicName,
							new ImageIcon(image.getScaledInstance(28, 28, Image.SCALE_DEFAULT)));
					particleItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							final ParticleEmitter2 particle = MDL.read(file).sortedIdObjects(ParticleEmitter2.class)
									.get(0);

							final JPanel particlePanel = new JPanel();
							final List<IdObject> idObjects = new ArrayList<>(currentMDL().getIdObjects());
							final Bone nullBone = new Bone("No parent");
							idObjects.add(0, nullBone);
							final JComboBox<IdObject> parent = new JComboBox<>(idObjects.toArray(new IdObject[0]));
							parent.setRenderer(new BasicComboBoxRenderer() {
								@Override
								public Component getListCellRendererComponent(final JList list, final Object value,
										final int index, final boolean isSelected, final boolean cellHasFocus) {
									final IdObject idObject = (IdObject) value;
									if (idObject == nullBone) {
										return super.getListCellRendererComponent(list, "No parent", index, isSelected,
												cellHasFocus);
									}
									return super.getListCellRendererComponent(list,
											value.getClass().getSimpleName() + " \"" + idObject.getName() + "\"", index,
											isSelected, cellHasFocus);
								}
							});
							final JLabel parentLabel = new JLabel("Parent:");
							final JLabel imageLabel = new JLabel(
									new ImageIcon(image.getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
							final JLabel titleLabel = new JLabel("Add " + basicName);
							titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

							final JLabel nameLabel = new JLabel("Particle Name:");
							final JTextField nameField = new JTextField("MyBlizParticle");

							final JLabel xLabel = new JLabel("Z:");
							final JSpinner xSpinner = new JSpinner(
									new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001));

							final JLabel yLabel = new JLabel("X:");
							final JSpinner ySpinner = new JSpinner(
									new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001));

							final JLabel zLabel = new JLabel("Y:");
							final JSpinner zSpinner = new JSpinner(
									new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001));
							parent.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(final ActionEvent e) {
									final IdObject choice = parent.getItemAt(parent.getSelectedIndex());
									xSpinner.setValue(choice.getPivotPoint().x);
									ySpinner.setValue(choice.getPivotPoint().y);
									zSpinner.setValue(choice.getPivotPoint().z);
								}
							});

							final JPanel animPanel = new JPanel();
							final List<Animation> anims = currentMDL().getAnims();
							animPanel.setLayout(new GridLayout(anims.size() + 1, 1));
							final JCheckBox[] checkBoxes = new JCheckBox[anims.size()];
							int animIndex = 0;
							for (final Animation anim : anims) {
								animPanel.add(checkBoxes[animIndex] = new JCheckBox(anim.getName()));
								checkBoxes[animIndex].setSelected(true);
								animIndex++;
							}
							final JButton chooseAnimations = new JButton("Choose when to show!");
							chooseAnimations.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(final ActionEvent e) {
									JOptionPane.showMessageDialog(particlePanel, animPanel);
								}
							});
							final JButton[] colorButtons = new JButton[3];
							final Color[] colors = new Color[colorButtons.length];
							for (int i = 0; i < colorButtons.length; i++) {
								final Vertex colorValues = particle.getSegmentColor(i);
								final Color color = new Color((int) (colorValues.z * 255), (int) (colorValues.y * 255),
										(int) (colorValues.x * 255));

								final JButton button = new JButton("Color " + (i + 1),
										new ImageIcon(ImageUtils.createBlank(color, 32, 32)));
								colors[i] = color;
								final int index = i;
								button.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(final ActionEvent e) {
										final Color colorChoice = JColorChooser.showDialog(MainPanel.this,
												"Chooser Color", colors[index]);
										if (colorChoice != null) {
											colors[index] = colorChoice;
											button.setIcon(
													new ImageIcon(ImageUtils.createBlank(colors[index], 32, 32)));
										}
									}
								});
								colorButtons[i] = button;
							}

							final GroupLayout layout = new GroupLayout(particlePanel);

							layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(imageLabel).addGap(8)
									.addGroup(layout.createParallelGroup(Alignment.CENTER).addComponent(titleLabel)
											.addGroup(layout.createSequentialGroup().addComponent(nameLabel).addGap(4)
													.addComponent(nameField))
											.addGroup(layout.createSequentialGroup().addComponent(parentLabel).addGap(4)
													.addComponent(parent))
											.addComponent(chooseAnimations)
											.addGroup(layout.createSequentialGroup().addComponent(xLabel)
													.addComponent(xSpinner).addGap(4).addComponent(yLabel)
													.addComponent(ySpinner).addGap(4).addComponent(zLabel)
													.addComponent(zSpinner))
											.addGroup(layout.createSequentialGroup().addComponent(colorButtons[0])
													.addGap(4).addComponent(colorButtons[1]).addGap(4)
													.addComponent(colorButtons[2]))));
							layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER)
									.addComponent(imageLabel)
									.addGroup(layout.createSequentialGroup().addComponent(titleLabel)
											.addGroup(layout.createParallelGroup(Alignment.CENTER)
													.addComponent(nameLabel).addComponent(nameField))
											.addGap(4)
											.addGroup(layout.createParallelGroup(Alignment.CENTER)
													.addComponent(parentLabel).addComponent(parent))
											.addGap(4).addComponent(chooseAnimations).addGap(4)
											.addGroup(layout.createParallelGroup(Alignment.CENTER).addComponent(xLabel)
													.addComponent(xSpinner).addComponent(yLabel).addComponent(ySpinner)
													.addComponent(zLabel).addComponent(zSpinner))
											.addGap(4)
											.addGroup(layout.createParallelGroup(Alignment.CENTER)
													.addComponent(colorButtons[0]).addComponent(colorButtons[1])
													.addComponent(colorButtons[2]))));
							particlePanel.setLayout(layout);
							final int x = JOptionPane.showConfirmDialog(MainPanel.this, particlePanel,
									"Add " + basicName, JOptionPane.OK_CANCEL_OPTION);
							if (x == JOptionPane.OK_OPTION) {
								// do stuff
								particle.setPivotPoint(new Vertex(((Number) xSpinner.getValue()).doubleValue(),
										((Number) ySpinner.getValue()).doubleValue(),
										((Number) zSpinner.getValue()).doubleValue()));
								for (int i = 0; i < colors.length; i++) {
									particle.setSegmentColor(i, new Vertex(colors[i].getBlue() / 255.00,
											colors[i].getGreen() / 255.00, colors[i].getRed() / 255.00));
								}
								final IdObject parentChoice = parent.getItemAt(parent.getSelectedIndex());
								if (parentChoice == nullBone) {
									particle.setParent(null);
								} else {
									particle.setParent(parentChoice);
								}
								AnimFlag oldFlag = particle.getVisibilityFlag();
								if (oldFlag == null) {
									oldFlag = new AnimFlag("Visibility");
								}
								final AnimFlag visFlag = AnimFlag.buildEmptyFrom(oldFlag);
								animIndex = 0;
								for (final Animation anim : anims) {
									if (!checkBoxes[animIndex].isSelected()) {
										visFlag.addEntry(anim.getStart(), new Integer(0));
									}
									animIndex++;
								}
								particle.setVisibilityFlag(visFlag);
								particle.setName(nameField.getText());
								currentMDL().add(particle);
							}
						}
					});
					addParticle.add(particleItem);
				} catch (final IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		animationMenu = new JMenu("Animation");
		animationMenu.setMnemonic(KeyEvent.VK_A);
		addMenu.add(animationMenu);

		riseFallBirth = new JMenuItem("Rising/Falling Birth/Death");
		riseFallBirth.setMnemonic(KeyEvent.VK_R);
		riseFallBirth.addActionListener(this);
		animationMenu.add(riseFallBirth);

		singleAnimationMenu = new JMenu("Single");
		singleAnimationMenu.setMnemonic(KeyEvent.VK_S);
		animationMenu.add(singleAnimationMenu);

		animFromFile = new JMenuItem("From File");
		animFromFile.setMnemonic(KeyEvent.VK_F);
		animFromFile.addActionListener(this);
		singleAnimationMenu.add(animFromFile);

		animFromUnit = new JMenuItem("From Unit");
		animFromUnit.setMnemonic(KeyEvent.VK_U);
		animFromUnit.addActionListener(this);
		singleAnimationMenu.add(animFromUnit);

		animFromModel = new JMenuItem("From Model");
		animFromModel.setMnemonic(KeyEvent.VK_M);
		animFromModel.addActionListener(this);
		singleAnimationMenu.add(animFromModel);

		animFromObject = new JMenuItem("From Object");
		animFromObject.setMnemonic(KeyEvent.VK_O);
		animFromObject.addActionListener(this);
		singleAnimationMenu.add(animFromObject);

		aboutMenu = new JMenu("Help");
		aboutMenu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(aboutMenu);

		clearRecent = new JMenuItem("Clear");
		clearRecent.setMnemonic(KeyEvent.VK_C);
		clearRecent.addActionListener(this);
		recentMenu.add(clearRecent);

		recentMenu.add(new JSeparator());

		updateRecent();

		creditsButton = new JMenuItem("About");
		creditsButton.setMnemonic(KeyEvent.VK_A);
		creditsButton.addActionListener(this);
		aboutMenu.add(creditsButton);

		showMatrices = new JMenuItem("View Selected \"Matrices\"");
		showMatrices.setMnemonic(KeyEvent.VK_V);
		showMatrices.addActionListener(viewMatricesAction);
		modelMenu.add(showMatrices);

		insideOut = new JMenuItem("Flip all selected faces");
		insideOut.setMnemonic(KeyEvent.VK_I);
		insideOut.addActionListener(insideOutAction);
		modelMenu.add(insideOut);

		modelMenu.add(new JSeparator());

		editUVs = new JMenuItem("Edit UV Mapping");
		editUVs.setMnemonic(KeyEvent.VK_U);
		editUVs.addActionListener(this);
		modelMenu.add(editUVs);

		exportTextures = new JMenuItem("Export Texture");
		exportTextures.setMnemonic(KeyEvent.VK_E);
		exportTextures.addActionListener(this);
		modelMenu.add(exportTextures);

		scaleAnimations = new JMenuItem("Edit Animations");
		scaleAnimations.setMnemonic(KeyEvent.VK_A);
		scaleAnimations.addActionListener(this);
		modelMenu.add(scaleAnimations);

		linearizeAnimations = new JMenuItem("Linearize Animations");
		linearizeAnimations.setMnemonic(KeyEvent.VK_L);
		linearizeAnimations.addActionListener(this);
		modelMenu.add(linearizeAnimations);

		simplifyKeyframes = new JMenuItem("Simplify Keyframes (Experimental)");
		simplifyKeyframes.setMnemonic(KeyEvent.VK_K);
		simplifyKeyframes.addActionListener(this);
		modelMenu.add(simplifyKeyframes);

		tweaksSubmenu = new JMenu("Tweaks");
		tweaksSubmenu.setMnemonic(KeyEvent.VK_T);
		tweaksSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to tweak conversion mistakes.");
		modelMenu.add(tweaksSubmenu);

		flipAllUVsU = new JMenuItem("Flip All UVs U");
		flipAllUVsU.setMnemonic(KeyEvent.VK_U);
		flipAllUVsU.addActionListener(flipAllUVsUAction);
		tweaksSubmenu.add(flipAllUVsU);

		flipAllUVsV = new JMenuItem("Flip All UVs V");
		flipAllUVsV.setMnemonic(KeyEvent.VK_V);
		flipAllUVsV.addActionListener(flipAllUVsVAction);
		tweaksSubmenu.add(flipAllUVsV);

		inverseAllUVs = new JMenuItem("Swap All UVs U for V");
		inverseAllUVs.setMnemonic(KeyEvent.VK_S);
		inverseAllUVs.addActionListener(inverseAllUVsAction);
		tweaksSubmenu.add(inverseAllUVs);

		mirrorSubmenu = new JMenu("Mirror");
		mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
		mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");
		modelMenu.add(mirrorSubmenu);

		mirrorX = new JMenuItem("Mirror X");
		mirrorX.setMnemonic(KeyEvent.VK_X);
		mirrorX.addActionListener(mirrorXAction);
		mirrorSubmenu.add(mirrorX);

		mirrorY = new JMenuItem("Mirror Y");
		mirrorY.setMnemonic(KeyEvent.VK_Y);
		mirrorY.addActionListener(mirrorYAction);
		mirrorSubmenu.add(mirrorY);

		mirrorZ = new JMenuItem("Mirror Z");
		mirrorZ.setMnemonic(KeyEvent.VK_Z);
		mirrorZ.addActionListener(mirrorZAction);
		mirrorSubmenu.add(mirrorZ);

		mirrorSubmenu.add(new JSeparator());

		mirrorFlip = new JCheckBoxMenuItem("Automatically flip after mirror (preserves surface)", true);
		mirrorFlip.setMnemonic(KeyEvent.VK_A);
		mirrorSubmenu.add(mirrorFlip);

		textureModels = new JCheckBoxMenuItem("Texture Models", true);
		textureModels.setMnemonic(KeyEvent.VK_T);
		textureModels.setSelected(true);
		textureModels.addActionListener(this);
		viewMenu.add(textureModels);

		newDirectory = new JMenuItem("Change Game Directory");
		newDirectory.setAccelerator(KeyStroke.getKeyStroke("control shift D"));
		newDirectory.setToolTipText("Changes the directory from which to load texture files for the 3D display.");
		newDirectory.setMnemonic(KeyEvent.VK_D);
		newDirectory.addActionListener(this);
		viewMenu.add(newDirectory);

		viewMenu.add(new JSeparator());

		showVertexModifyControls = new JCheckBoxMenuItem("Show Viewport Buttons", true);
		showVertexModifyControls.setMnemonic(KeyEvent.VK_V);
		showVertexModifyControls.addActionListener(this);
		viewMenu.add(showVertexModifyControls);

		useNativeMDXParser = new JCheckBoxMenuItem("Use Native MDX Parser", true);
		useNativeMDXParser.setMnemonic(KeyEvent.VK_M);
		useNativeMDXParser.addActionListener(this);
		viewMenu.add(useNativeMDXParser);

		viewMenu.add(new JSeparator());

		showNormals = new JCheckBoxMenuItem("Show Normals", true);
		showNormals.setMnemonic(KeyEvent.VK_N);
		showNormals.setSelected(false);
		showNormals.addActionListener(this);
		viewMenu.add(showNormals);

		viewMode = new JMenu("3D View Mode");
		viewMenu.add(viewMode);

		viewModes = new ButtonGroup();

		final ActionListener repainter = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (wireframe.isSelected()) {
					prefs.setViewMode(0);
				} else if (solid.isSelected()) {
					prefs.setViewMode(1);
				} else {
					prefs.setViewMode(-1);
				}
				repaint();
			}
		};

		wireframe = new JRadioButtonMenuItem("Wireframe");
		wireframe.addActionListener(repainter);
		viewMode.add(wireframe);
		viewModes.add(wireframe);

		solid = new JRadioButtonMenuItem("Solid");
		solid.addActionListener(repainter);
		viewMode.add(solid);
		viewModes.add(solid);

		viewModes.setSelected(solid.getModel(), true);

		newModel = new JMenuItem("New");
		newModel.setAccelerator(KeyStroke.getKeyStroke("control N"));
		newModel.setMnemonic(KeyEvent.VK_N);
		newModel.addActionListener(this);
		fileMenu.add(newModel);

		open = new JMenuItem("Open");
		open.setAccelerator(KeyStroke.getKeyStroke("control O"));
		open.setMnemonic(KeyEvent.VK_O);
		open.addActionListener(this);
		fileMenu.add(open);

		fetch = new JMenu("Fetch");
		fetch.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(fetch);

		fetchUnit = new JMenuItem("Unit");
		fetchUnit.setAccelerator(KeyStroke.getKeyStroke("control U"));
		fetchUnit.setMnemonic(KeyEvent.VK_U);
		fetchUnit.addActionListener(this);
		fetch.add(fetchUnit);

		fetchModel = new JMenuItem("Model");
		fetchModel.setAccelerator(KeyStroke.getKeyStroke("control M"));
		fetchModel.setMnemonic(KeyEvent.VK_M);
		fetchModel.addActionListener(this);
		fetch.add(fetchModel);

		fetchObject = new JMenuItem("Object Editor");
		fetchObject.setAccelerator(KeyStroke.getKeyStroke("control O"));
		fetchObject.setMnemonic(KeyEvent.VK_O);
		fetchObject.addActionListener(this);
		fetch.add(fetchObject);

		fetch.add(new JSeparator());

		fetchPortraitsToo = new JCheckBoxMenuItem("Fetch portraits, too!", true);
		fetchPortraitsToo.setMnemonic(KeyEvent.VK_P);
		fetchPortraitsToo.setSelected(true);
		fetchPortraitsToo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				prefs.setLoadPortraits(fetchPortraitsToo.isSelected());
			}

		});
		fetch.add(fetchPortraitsToo);

		fileMenu.add(new JSeparator());

		importMenu = new JMenu("Import");
		importMenu.setMnemonic(KeyEvent.VK_I);
		fileMenu.add(importMenu);

		importButton = new JMenuItem("From File");
		importButton.setAccelerator(KeyStroke.getKeyStroke("control shift I"));
		importButton.setMnemonic(KeyEvent.VK_I);
		importButton.addActionListener(this);
		importMenu.add(importButton);

		importUnit = new JMenuItem("From Unit");
		importUnit.setMnemonic(KeyEvent.VK_U);
		importUnit.addActionListener(this);
		importMenu.add(importUnit);

		importGameModel = new JMenuItem("From WC3 Model");
		importGameModel.setMnemonic(KeyEvent.VK_M);
		importGameModel.addActionListener(this);
		importMenu.add(importGameModel);

		importGameObject = new JMenuItem("From Object Editor");
		importGameObject.setMnemonic(KeyEvent.VK_O);
		importGameObject.addActionListener(this);
		importMenu.add(importGameObject);

		importFromWorkspace = new JMenuItem("From Workspace");
		importFromWorkspace.setMnemonic(KeyEvent.VK_O);
		importFromWorkspace.addActionListener(this);
		importMenu.add(importFromWorkspace);

		importButtonS = new JMenuItem("Simple Import");
		importButtonS.setAccelerator(KeyStroke.getKeyStroke("control shift S"));
		importButtonS.setMnemonic(KeyEvent.VK_P);
		importButtonS.addActionListener(this);
		// importButtonS.setEnabled(false);
		fileMenu.add(importButtonS);

		mergeGeoset = new JMenuItem("Merge Geoset");
		mergeGeoset.setAccelerator(KeyStroke.getKeyStroke("control M"));
		mergeGeoset.setMnemonic(KeyEvent.VK_M);
		mergeGeoset.addActionListener(this);
		fileMenu.add(mergeGeoset);

		nullmodelButton = new JMenuItem("Edit/delete model components");
		nullmodelButton.setAccelerator(KeyStroke.getKeyStroke("control E"));
		nullmodelButton.setMnemonic(KeyEvent.VK_E);
		nullmodelButton.addActionListener(this);
		fileMenu.add(nullmodelButton);

		fileMenu.add(new JSeparator());

		save = new JMenuItem("Save");
		save.setMnemonic(KeyEvent.VK_S);
		save.setAccelerator(KeyStroke.getKeyStroke("control S"));
		save.addActionListener(this);
		fileMenu.add(save);

		saveAs = new JMenuItem("Save as");
		saveAs.setMnemonic(KeyEvent.VK_A);
		saveAs.setAccelerator(KeyStroke.getKeyStroke("control Q"));
		saveAs.addActionListener(this);
		fileMenu.add(saveAs);

		fileMenu.add(new JSeparator());

		showController = new JMenuItem("Show Controller");
		showController.setMnemonic(KeyEvent.VK_H);
		showController.setAccelerator(KeyStroke.getKeyStroke("control H"));
		showController.addActionListener(this);
		fileMenu.add(showController);

		undo = new UndoMenuItem("Undo");
		undo.addActionListener(undoAction);
		undo.setAccelerator(KeyStroke.getKeyStroke("control Z"));
		undo.addMouseListener(this);
		editMenu.add(undo);
		undo.setEnabled(undo.funcEnabled());

		redo = new RedoMenuItem("Redo");
		redo.addActionListener(redoAction);
		redo.setAccelerator(KeyStroke.getKeyStroke("control Y"));
		redo.addMouseListener(this);
		editMenu.add(redo);
		redo.setEnabled(redo.funcEnabled());

		editMenu.add(new JSeparator());

		snapNormals = new JMenuItem("Snap Normals");
		snapNormals.setAccelerator(KeyStroke.getKeyStroke("control L"));
		snapNormals.addActionListener(snapNormalsAction);
		editMenu.add(snapNormals);

		editMenu.add(new JSeparator());

		selectAll = new JMenuItem("Select All");
		selectAll.setAccelerator(KeyStroke.getKeyStroke("control A"));
		selectAll.addActionListener(selectAllAction);
		editMenu.add(selectAll);

		invertSelect = new JMenuItem("Invert Selection");
		invertSelect.setAccelerator(KeyStroke.getKeyStroke("control I"));
		invertSelect.addActionListener(invertSelectAction);
		editMenu.add(invertSelect);

		expandSelection = new JMenuItem("Expand Selection");
		expandSelection.setAccelerator(KeyStroke.getKeyStroke("control E"));
		expandSelection.addActionListener(expandSelectionAction);
		editMenu.add(expandSelection);

		return menuBar;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		// Open, off of the file menu:
		refreshUndo();
		if (e.getSource() == newModel) {
			newModel();
		} else if (e.getSource() == open) {
			onClickOpen();
		} else if (e.getSource() == fetchUnit) {
			final Element unitFetched = fetchUnit();
			final String filepath = convertPathToMDX(unitFetched.getField("file"));
			if (filepath != null) {
				loadFile(MpqCodebase.get().getFile(filepath), true, true, unitFetched.getScaledIcon(0.25f));
				final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
						+ filepath.substring(filepath.lastIndexOf('.'), filepath.length());
				if (prefs.isLoadPortraits() && MpqCodebase.get().has(portrait)) {
					loadFile(MpqCodebase.get().getFile(portrait), true, false, unitFetched.getScaledIcon(0.25f));
				}
				modelMenu.getAccessibleContext().setAccessibleDescription(
						"Allows the user to control which parts of the model are displayed for editing.");
				modelMenu.setEnabled(true);
			}
		} else if (e.getSource() == fetchModel) {
			final ModelElement model = fetchModel();
			final String filepath = convertPathToMDX(model.getFilepath());
			if (filepath != null) {

				final ImageIcon icon = model.hasCachedIconPath() ? new ImageIcon(BLPHandler.get()
						.getGameTex(model.getCachedIconPath()).getScaledInstance(16, 16, Image.SCALE_FAST)) : MDLIcon;
				loadFile(MpqCodebase.get().getFile(filepath), true, true, icon);
				final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
						+ filepath.substring(filepath.lastIndexOf('.'), filepath.length());
				if (prefs.isLoadPortraits() && MpqCodebase.get().has(portrait)) {
					loadFile(MpqCodebase.get().getFile(portrait), true, false, icon);
				}
				modelMenu.getAccessibleContext().setAccessibleDescription(
						"Allows the user to control which parts of the model are displayed for editing.");
				modelMenu.setEnabled(true);
			}
		} else if (e.getSource() == fetchObject) {
			final GameObject objectFetched = fetchObject();
			final String filepath = convertPathToMDX(objectFetched.getField("file"));
			if (filepath != null) {
				loadFile(MpqCodebase.get().getFile(filepath), true, true, new ImageIcon(BLPHandler.get()
						.getGameTex(objectFetched.getField("Art")).getScaledInstance(16, 16, Image.SCALE_FAST)));
				final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
						+ filepath.substring(filepath.lastIndexOf('.'), filepath.length());
				if (prefs.isLoadPortraits() && MpqCodebase.get().has(portrait)) {
					loadFile(MpqCodebase.get().getFile(portrait), true, false, new ImageIcon(BLPHandler.get()
							.getGameTex(objectFetched.getField("Art")).getScaledInstance(16, 16, Image.SCALE_FAST)));
				}
				modelMenu.getAccessibleContext().setAccessibleDescription(
						"Allows the user to control which parts of the model are displayed for editing.");
				modelMenu.setEnabled(true);
			}
		} else if (e.getSource() == importButton) {
			fc.setDialogTitle("Import");
			final MDL current = currentMDL();
			if (current != null && !current.isTemp() && current.getFile() != null) {
				fc.setCurrentDirectory(current.getFile().getParentFile());
			} else if (profile.getPath() != null) {
				fc.setCurrentDirectory(new File(profile.getPath()));
			}
			final int returnValue = fc.showOpenDialog(this);

			if (returnValue == JFileChooser.APPROVE_OPTION) {
				currentFile = fc.getSelectedFile();
				profile.setPath(currentFile.getParent());
				modelMenu.getAccessibleContext().setAccessibleDescription(
						"Allows the user to control which parts of the model are displayed for editing.");
				modelMenu.setEnabled(true);
				importFile(currentFile);
			}

			fc.setSelectedFile(null);

			// //Special thanks to the JWSFileChooserDemo from oracle's Java
			// tutorials, from which many ideas were borrowed for the following
			// FileOpenService fos = null;
			// FileContents fileContents = null;
			//
			// try
			// {
			// fos =
			// (FileOpenService)ServiceManager.lookup("javax.jnlp.FileOpenService");
			// }
			// catch (UnavailableServiceException exc )
			// {
			//
			// }
			//
			// if( fos != null )
			// {
			// try
			// {
			// fileContents = fos.openFileDialog(null, null);
			// }
			// catch (Exception exc )
			// {
			// JOptionPane.showMessageDialog(this,"Opening command failed:
			// "+exc.getLocalizedMessage());
			// }
			// }
			//
			// if( fileContents != null)
			// {
			// try
			// {
			// fileContents.getName();
			// }
			// catch (IOException exc)
			// {
			// JOptionPane.showMessageDialog(this,"Problem opening file:
			// "+exc.getLocalizedMessage());
			// }
			// }
			refreshController();
		} else if (e.getSource() == importUnit) {
			final String filepath = convertPathToMDX(fetchUnit().getField("file"));
			final MDL current = currentMDL();
			if (filepath != null) {
				final File animationSource = MpqCodebase.get().getFile(filepath);
				importFile(animationSource);
			}
			refreshController();
		} else if (e.getSource() == importGameModel) {
			final String filepath = convertPathToMDX(fetchModel().getFilepath());
			final MDL current = currentMDL();
			if (filepath != null) {
				final File animationSource = MpqCodebase.get().getFile(filepath);
				importFile(animationSource);
			}
			refreshController();
		} else if (e.getSource() == importGameObject) {
			final String filepath = convertPathToMDX(fetchObject().getField("file"));
			final MDL current = currentMDL();
			if (filepath != null) {
				final File animationSource = MpqCodebase.get().getFile(filepath);
				importFile(animationSource);
			}
			refreshController();
		} else if (e.getSource() == importFromWorkspace) {
			final List<MDL> optionNames = new ArrayList<>();
			for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
				final ModelPanel modelPanel = (ModelPanel) tabbedPane.getComponentAt(i);
				final MDL model = modelPanel.getMDLDisplay().getMDL();
				optionNames.add(model);
			}
			final MDL choice = (MDL) JOptionPane.showInputDialog(this, "Choose a workspace item to import data from:",
					"Import from Workspace", JOptionPane.OK_CANCEL_OPTION, null, optionNames.toArray(),
					optionNames.get(0));
			if (choice != null) {
				importFile(MDL.deepClone(choice, choice.getHeaderName()));
			}
			refreshController();
		} else if (e.getSource() == importButtonS) {
			new ImportPanelSimple();
			refreshController();
		} else if (e.getSource() == mergeGeoset) {
			fc.setDialogTitle("Merge Geoset");
			final MDL current = currentMDL();
			if (current != null && !current.isTemp() && current.getFile() != null) {
				fc.setCurrentDirectory(current.getFile().getParentFile());
			} else if (profile.getPath() != null) {
				fc.setCurrentDirectory(new File(profile.getPath()));
			}
			final int returnValue = fc.showOpenDialog(this);

			if (returnValue == JFileChooser.APPROVE_OPTION) {
				currentFile = fc.getSelectedFile();
				final MDL geoSource = MDL.read(currentFile);
				profile.setPath(currentFile.getParent());
				boolean going = true;
				Geoset host = null;
				while (going) {
					final String s = JOptionPane.showInputDialog(this,
							"Geoset into which to Import: (1 to " + current.getGeosetsSize() + ")");
					try {
						final int x = Integer.parseInt(s);
						if (x >= 1 && x <= current.getGeosetsSize()) {
							host = current.getGeoset(x - 1);
							going = false;
						}
					} catch (final NumberFormatException exc) {

					}
				}
				Geoset newGeoset = null;
				going = true;
				while (going) {
					final String s = JOptionPane.showInputDialog(this,
							"Geoset to Import: (1 to " + geoSource.getGeosetsSize() + ")");
					try {
						final int x = Integer.parseInt(s);
						if (x <= geoSource.getGeosetsSize()) {
							newGeoset = geoSource.getGeoset(x - 1);
							going = false;
						}
					} catch (final NumberFormatException exc) {

					}
				}
				newGeoset.updateToObjects(current);
				System.out.println("putting " + newGeoset.numUVLayers() + " into a nice " + host.numUVLayers());
				for (int i = 0; i < newGeoset.numVerteces(); i++) {
					final GeosetVertex ver = newGeoset.getVertex(i);
					host.add(ver);
					ver.setGeoset(host);// geoset = host;
					// for( int z = 0; z < host.n.numUVLayers(); z++ )
					// {
					// host.getUVLayer(z).addTVertex(newGeoset.getVertex(i).getTVertex(z));
					// }
				}
				for (int i = 0; i < newGeoset.numTriangles(); i++) {
					final Triangle tri = newGeoset.getTriangle(i);
					host.add(tri);
					tri.setGeoRef(host);
				}
			}

			fc.setSelectedFile(null);
		} else if (e.getSource() == clearRecent) {
			SaveProfile.get().clearRecent();
			updateRecent();
		} else if (e.getSource() == nullmodelButton) {
			nullmodelFile();
			refreshController();
		} else if (e.getSource() == showController) {
			if (geoControl == null) {
				geoControl = new ViewController(currentMDLDisp());
			}
			if (!geoControl.getFrame().isVisible()) {
				geoControl.getFrame().setVisible(true);
				// geoControl.getFrame().setExtendedState(geoControl.getFrame().getExtendedState()
				// | JFrame.MAXIMIZED_BOTH);
				geoControl.getFrame().toFront();
			}
		} else if (e.getSource() == save && currentMDL() != null && currentMDL().getFile() != null) {
			onClickSave();
		} else if (e.getSource() == saveAs) {
			if (!onClickSaveAs()) {
				return;
			}
		} else if (e.getSource() == contextClose) {
			if (((ModelPanel) tabbedPane.getComponentAt(contextClickedTab)).close()) {// this);
				tabbedPane.remove(contextClickedTab);
			}
		} else if (e.getSource() == contextCloseAll) {
			this.closeAll();
		} else if (e.getSource() == snapButton) {
			final MDLDisplay mdlDisp = currentMDLDisp();
			if (mdlDisp != null) {
				mdlDisp.snap();
			}
		} else if (e.getSource() == cloneButton) {
			setCloneOn(!prefs.isCloneOn());
			// ModelPanel mpanel =
			// ((ModelPanel)tabbedPane.getSelectedComponent());
			// if( mpanel != null )
			// mpanel.getMDLDisplay().clone(mpanel.getMDLDisplay().selection,true);
		} else if (e.getSource() == xButton) {
			final int x = 1;
			setDimLock(x, !prefs.getDimLock(x));
		} else if (e.getSource() == yButton) {
			final int x = 2;
			setDimLock(x, !prefs.getDimLock(x));
		} else if (e.getSource() == zButton) {
			final int x = 0;
			setDimLock(x, !prefs.getDimLock(x));
		} else if (e.getSource() == newDirectory) {
			final DirectorySelector selector = new DirectorySelector(SaveProfile.get().getGameDirectory(), "");
			JOptionPane.showMessageDialog(null, selector, "Locating Warcraft III Directory",
					JOptionPane.QUESTION_MESSAGE);
			String wcDirectory = selector.getDir();
			if (!(wcDirectory.endsWith("/") || wcDirectory.endsWith("\\"))) {
				wcDirectory = wcDirectory + "\\";
			}
			SaveProfile.get().setGameDirectory(wcDirectory);

			PerspDisplayPanel pdp;
			for (int i = 0; i < tabbedPane.getComponentCount(); i++) {
				pdp = ((ModelPanel) tabbedPane.getComponentAt(i)).getPerspArea();
				pdp.reloadAllTextures();
			}
			MpqCodebase.get().refresh();
		} else if (e.getSource() == showVertexModifyControls) {
			final boolean selected = showVertexModifyControls.isSelected();
			prefs.setShowVertexModifierControls(selected);
			// SaveProfile.get().setShowViewportButtons(selected);
			for (int i = 0; i < tabbedPane.getComponentCount(); i++) {
				final ModelPanel panel = ((ModelPanel) tabbedPane.getComponentAt(i));
				panel.getFrontArea().setControlsVisible(selected);
				panel.getBotArea().setControlsVisible(selected);
				panel.getSideArea().setControlsVisible(selected);
				final MDLDisplay disp = ((ModelPanel) tabbedPane.getComponentAt(i)).getMDLDisplay();
				if (disp.getUVPanel() != null) {
					disp.getUVPanel().setControlsVisible(selected);
				}
			}
		} else if (e.getSource() == textureModels) {
			prefs.setTextureModels(textureModels.isSelected());
		} else if (e.getSource() == showNormals) {
			prefs.setShowNormals(showNormals.isSelected());
		} else if (e.getSource() == useNativeMDXParser) {
			final boolean selected = useNativeMDXParser.isSelected();
			prefs.setUseNativeMDXParser(selected);
			// SaveProfile.get().setNativeMDXParserEnabled(selected);
		} else if (e.getSource() == editUVs) {
			final MDLDisplay disp = currentMDLDisp();
			if (disp.getUVPanel() == null) {
				final UVPanel panel = new UVPanel(disp);
				disp.setUVPanel(panel);
				panel.showFrame();
			} else if (!disp.getUVPanel().frameVisible()) {
				disp.getUVPanel().showFrame();
			}
		} else if (e.getSource() == exportTextures) {
			final DefaultListModel<Material> materials = new DefaultListModel<>();
			for (int i = 0; i < currentMDL().getMaterials().size(); i++) {
				final Material mat = currentMDL().getMaterials().get(i);
				materials.addElement(mat);
			}

			final JList<Material> materialsList = new JList<>(materials);
			materialsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			materialsList.setCellRenderer(new MaterialListRenderer(currentMDL()));
			JOptionPane.showMessageDialog(this, new JScrollPane(materialsList));

			if (exportTextureDialog.getCurrentDirectory() == null) {
				final MDL current = currentMDL();
				if (current != null && !current.isTemp() && current.getFile() != null) {
					fc.setCurrentDirectory(current.getFile().getParentFile());
				} else if (profile.getPath() != null) {
					fc.setCurrentDirectory(new File(profile.getPath()));
				}
			}
			if (exportTextureDialog.getCurrentDirectory() == null) {
				exportTextureDialog.setSelectedFile(new File(exportTextureDialog.getCurrentDirectory() + File.separator
						+ materialsList.getSelectedValue().getName()));
			}

			final int x = exportTextureDialog.showSaveDialog(this);
			if (x == JFileChooser.APPROVE_OPTION) {
				final File file = exportTextureDialog.getSelectedFile();
				if (file != null) {
					try {
						if (file.getName().lastIndexOf('.') >= 0) {
							BufferedImage bufferedImage = materialsList.getSelectedValue()
									.getBufferedImage(currentMDL().getWorkingDirectory());
							final String fileExtension = file.getName().substring(file.getName().lastIndexOf('.') + 1)
									.toUpperCase();
							if (fileExtension.equals("BMP") || fileExtension.equals("JPG")
									|| fileExtension.equals("JPEG")) {
								JOptionPane.showMessageDialog(this,
										"Warning: Alpha channel was converted to black. Some data will be lost\nif you convert this texture back to Warcraft BLP.");
								bufferedImage = removeAlphaChannel(bufferedImage);
							}
							final boolean write = ImageIO.write(bufferedImage, fileExtension, file);
							if (!write) {
								JOptionPane.showMessageDialog(this, "File type unknown or unavailable");
							}
						} else {
							JOptionPane.showMessageDialog(this, "No file type was specified");
						}
					} catch (final IOException e1) {
						ExceptionPopup.display(e1);
						e1.printStackTrace();
					} catch (final Exception e2) {
						ExceptionPopup.display(e2);
						e2.printStackTrace();
					}
				} else {
					JOptionPane.showMessageDialog(this, "No output file was specified");
				}
			}
		} else if (e.getSource() == scaleAnimations) {
			final MDLDisplay disp = currentMDLDisp();
			// if( disp.animpanel == null )
			// {
			// AnimationPanel panel = new UVPanel(disp);
			// disp.setUVPanel(panel);
			// panel.showFrame();
			// }
			// else if(!disp.uvpanel.frameVisible() )
			// {
			// disp.uvpanel.showFrame();
			// }
			final AnimationFrame aFrame = new AnimationFrame(disp);
			aFrame.setVisible(true);
		} else if (e.getSource() == linearizeAnimations) {
			final int x = JOptionPane.showConfirmDialog(this,
					"This is an irreversible process that will lose some of your model data,\nin exchange for making it a smaller storage size.\n\nContinue and simplify animations?",
					"Warning: Linearize Animations", JOptionPane.OK_CANCEL_OPTION);
			if (x == JOptionPane.OK_OPTION) {
				final List<AnimFlag> allAnimFlags = currentMDL().getAllAnimFlags();
				for (final AnimFlag flag : allAnimFlags) {
					flag.linearize();
				}
			}
		} else if (e.getSource() == simplifyKeyframes) {
			final int x = JOptionPane.showConfirmDialog(this,
					"This is an irreversible process that will lose some of your model data,\nin exchange for making it a smaller storage size.\n\nContinue and simplify keyframes?",
					"Warning: Simplify Keyframes", JOptionPane.OK_CANCEL_OPTION);
			if (x == JOptionPane.OK_OPTION) {
				final List<AnimFlag> allAnimFlags = currentMDL().getAllAnimFlags();
				final List<Animation> anims = currentMDL().getAnims();

				for (final AnimFlag flag : allAnimFlags) {
					final List<Integer> indicesForDeletion = new ArrayList<>();
					Entry lastEntry = null;
					for (int i = 0; i < flag.length(); i++) {
						final Entry entry = flag.getEntry(i);
						if (lastEntry != null && lastEntry.time == entry.time) {
							indicesForDeletion.add(new Integer(i));
						}
						lastEntry = entry;
					}
					for (int i = indicesForDeletion.size() - 1; i >= 0; i--) {
						flag.deleteAt(indicesForDeletion.get(i));
					}
				}
				for (final Animation anim : anims) {
					for (final AnimFlag flag : allAnimFlags) {
						if (!flag.hasGlobalSeq()) {
							Object olderKeyframe = null;
							Object oldKeyframe = null;
							final List<Integer> indicesForDeletion = new ArrayList<>();
							for (int i = 0; i < flag.length(); i++) {
								final Entry entry = flag.getEntry(i);
								//
								// //Types of AnimFlags:
								// // 0 Alpha
								// public static final int ALPHA = 0;
								// // 1 Scaling
								// public static final int SCALING = 1;
								// // 2 Rotation
								// public static final int ROTATION = 2;
								// // 3 Translation
								// public static final int TRANSLATION = 3;
								// // 4 Color
								// public static final int COLOR = 4;
								// // 5 TextureID
								// public static final int TEXTUREID = 5;
								if (entry.time >= anim.getStart() && entry.time <= anim.getEnd()) {
									if (entry.value instanceof Double) {
										final Double d = (Double) entry.value;
										final Double older = (Double) olderKeyframe;
										final Double old = (Double) oldKeyframe;
										if (older != null && old != null && MathUtils.isBetween(older, old, d)) {
											indicesForDeletion.add(new Integer(i - 1));
										}
									} else if (entry.value instanceof Vertex) {
										final Vertex current = (Vertex) entry.value;
										final Vertex older = (Vertex) olderKeyframe;
										final Vertex old = (Vertex) oldKeyframe;
										if (older != null && old != null
												&& MathUtils.isBetween(older.x, old.x, current.x)
												&& MathUtils.isBetween(older.y, old.y, current.y)
												&& MathUtils.isBetween(older.z, old.z, current.z)) {
											indicesForDeletion.add(new Integer(i - 1));
										}
									} else if (entry.value instanceof QuaternionRotation) {
										final QuaternionRotation current = (QuaternionRotation) entry.value;
										final QuaternionRotation older = (QuaternionRotation) olderKeyframe;
										final QuaternionRotation old = (QuaternionRotation) oldKeyframe;
										final Vertex euler = current.toEuler();
										if (older != null && old != null) {
											final Vertex olderEuler = older.toEuler();
											final Vertex oldEuler = old.toEuler();
											if (MathUtils.isBetween(olderEuler.x, oldEuler.x, euler.x)
													&& MathUtils.isBetween(olderEuler.y, oldEuler.y, euler.y)
													&& MathUtils.isBetween(olderEuler.z, oldEuler.z, euler.z)) {
												// if
												// (MathUtils.isBetween(older.a,
												// old.a, current.a)
												// &&
												// MathUtils.isBetween(older.b,
												// old.b, current.b)
												// &&
												// MathUtils.isBetween(older.c,
												// old.c, current.c)
												// &&
												// MathUtils.isBetween(older.d,
												// old.d, current.d)) {
												indicesForDeletion.add(new Integer(i - 1));
											}
										}
									}
									olderKeyframe = oldKeyframe;
									oldKeyframe = entry.value;
								}
							}
							for (int i = indicesForDeletion.size() - 1; i >= 0; i--) {
								flag.deleteAt(indicesForDeletion.get(i));
							}
						}
					}
				}
				for (final Integer globalSeq : currentMDL().getGlobalSeqs()) {
					for (final AnimFlag flag : allAnimFlags) {
						if (flag.hasGlobalSeq() && flag.getGlobalSeq().equals(globalSeq)) {
							Object olderKeyframe = null;
							Object oldKeyframe = null;
							final List<Integer> indicesForDeletion = new ArrayList<>();
							for (int i = 0; i < flag.length(); i++) {
								final Entry entry = flag.getEntry(i);
								//
								// //Types of AnimFlags:
								// // 0 Alpha
								// public static final int ALPHA = 0;
								// // 1 Scaling
								// public static final int SCALING = 1;
								// // 2 Rotation
								// public static final int ROTATION = 2;
								// // 3 Translation
								// public static final int TRANSLATION = 3;
								// // 4 Color
								// public static final int COLOR = 4;
								// // 5 TextureID
								// public static final int TEXTUREID = 5;
								if (entry.value instanceof Double) {
									final Double d = (Double) entry.value;
									final Double older = (Double) olderKeyframe;
									final Double old = (Double) oldKeyframe;
									if (older != null && old != null && MathUtils.isBetween(older, old, d)) {
										indicesForDeletion.add(new Integer(i - 1));
									}
								} else if (entry.value instanceof Vertex) {
									final Vertex current = (Vertex) entry.value;
									final Vertex older = (Vertex) olderKeyframe;
									final Vertex old = (Vertex) oldKeyframe;
									if (older != null && old != null && MathUtils.isBetween(older.x, old.x, current.x)
											&& MathUtils.isBetween(older.y, old.y, current.y)
											&& MathUtils.isBetween(older.z, old.z, current.z)) {
										indicesForDeletion.add(new Integer(i - 1));
									}
								} else if (entry.value instanceof QuaternionRotation) {
									final QuaternionRotation current = (QuaternionRotation) entry.value;
									final QuaternionRotation older = (QuaternionRotation) olderKeyframe;
									final QuaternionRotation old = (QuaternionRotation) oldKeyframe;
									final Vertex euler = current.toEuler();
									if (older != null && old != null) {
										final Vertex olderEuler = older.toEuler();
										final Vertex oldEuler = old.toEuler();
										if (MathUtils.isBetween(olderEuler.x, oldEuler.x, euler.x)
												&& MathUtils.isBetween(olderEuler.y, oldEuler.y, euler.y)
												&& MathUtils.isBetween(olderEuler.z, oldEuler.z, euler.z)) {
											// if (MathUtils.isBetween(older.a,
											// old.a, current.a)
											// && MathUtils.isBetween(older.b,
											// old.b, current.b)
											// && MathUtils.isBetween(older.c,
											// old.c, current.c)
											// && MathUtils.isBetween(older.d,
											// old.d, current.d)) {
											indicesForDeletion.add(new Integer(i - 1));
										}
									}
								}
								olderKeyframe = oldKeyframe;
								oldKeyframe = entry.value;
							}
							for (int i = indicesForDeletion.size() - 1; i >= 0; i--) {
								flag.deleteAt(indicesForDeletion.get(i));
							}
						}
					}
				}
			}
		} else if (e.getSource() == riseFallBirth) {
			final MDLDisplay disp = currentMDLDisp();
			final MDL model = disp.getMDL();
			final Animation lastAnim = model.getAnim(model.getAnimsSize() - 1);

			final Animation oldBirth = model.findAnimByName("birth");
			final Animation oldDeath = model.findAnimByName("death");

			Animation birth = new Animation("Birth", lastAnim.getEnd() + 300, lastAnim.getEnd() + 2300);
			Animation death = new Animation("Death", birth.getEnd() + 300, birth.getEnd() + 2300);
			final Animation stand = model.findAnimByName("stand");

			final int confirmed = JOptionPane.showConfirmDialog(this,
					"This will permanently alter model. Are you sure?", "Confirmation", JOptionPane.OK_CANCEL_OPTION);
			if (confirmed != JOptionPane.OK_OPTION) {
				return;
			}

			boolean wipeoutOldBirth = false;
			if (oldBirth != null) {
				final String[] choices = { "Ignore", "Delete", "Overwrite" };
				final Object x = JOptionPane.showInputDialog(this,
						"Existing birth detected. What should be done with it?", "Question", JOptionPane.PLAIN_MESSAGE,
						null, choices, choices[0]);
				if (x == choices[1]) {
					wipeoutOldBirth = true;
				} else if (x == choices[2]) {
					birth = oldBirth;
				} else {
					return;
				}
			}
			boolean wipeoutOldDeath = false;
			if (oldDeath != null) {
				final String[] choices = { "Ignore", "Delete", "Overwrite" };
				final Object x = JOptionPane.showInputDialog(this,
						"Existing death detected. What should be done with it?", "Question", JOptionPane.PLAIN_MESSAGE,
						null, choices, choices[0]);
				if (x == choices[1]) {
					wipeoutOldDeath = true;
				} else if (x == choices[2]) {
					death = oldDeath;
				} else {
					return;
				}
			}
			if (wipeoutOldBirth) {
				model.remove(oldBirth);
			}
			if (wipeoutOldDeath) {
				model.remove(oldDeath);
			}

			final List<IdObject> roots = new ArrayList<>();
			for (final IdObject obj : model.getIdObjects()) {
				if (obj.getParent() == null) {
					roots.add(obj);
				}
			}
			for (final AnimFlag af : model.getAllAnimFlags()) {
				af.deleteAnim(birth);
				af.deleteAnim(death);
			}
			for (final IdObject obj : roots) {
				if (obj instanceof Bone) {
					final Bone b = (Bone) obj;
					AnimFlag trans = null;
					boolean globalSeq = false;
					for (final AnimFlag af : b.getAnimFlags()) {
						if (af.getTypeId() == AnimFlag.TRANSLATION) {
							if (af.hasGlobalSeq()) {
								globalSeq = true;
							} else {
								trans = af;
							}
						}
					}
					if (globalSeq) {
						continue;
					}
					if (trans == null) {
						final ArrayList<Integer> times = new ArrayList<>();
						final ArrayList<Integer> values = new ArrayList<>();
						trans = new AnimFlag("Translation", times, values);
						trans.addTag("Linear");
						b.getAnimFlags().add(trans);
					}
					trans.addEntry(birth.getStart(), new Vertex(0, 0, -300));
					trans.addEntry(birth.getEnd(), new Vertex(0, 0, 0));
					trans.addEntry(death.getStart(), new Vertex(0, 0, 0));
					trans.addEntry(death.getEnd(), new Vertex(0, 0, -300));
				}
			}

			// visibility
			for (final VisibilitySource source : model.getAllVisibilitySources()) {
				final AnimFlag dummy = new AnimFlag("dummy");
				final AnimFlag af = source.getVisibilityFlag();
				dummy.copyFrom(af);
				af.deleteAnim(birth);
				af.deleteAnim(death);
				af.copyFrom(dummy, stand.getStart(), stand.getEnd(), birth.getStart(), birth.getEnd());
				af.copyFrom(dummy, stand.getStart(), stand.getEnd(), death.getStart(), death.getEnd());
				af.setEntry(death.getEnd(), new Integer(0));
			}

			if (!birth.getTags().contains("NonLooping")) {
				birth.addTag("NonLooping");
			}
			if (!death.getTags().contains("NonLooping")) {
				death.addTag("NonLooping");
			}

			if (!model.contains(birth)) {
				model.add(birth);
			}
			if (!model.contains(death)) {
				model.add(death);
			}

			JOptionPane.showMessageDialog(this, "Done!");
		} else if (e.getSource() == animFromFile) {
			fc.setDialogTitle("Animation Source");
			final MDL current = currentMDL();
			if (current != null && !current.isTemp() && current.getFile() != null) {
				fc.setCurrentDirectory(current.getFile().getParentFile());
			} else if (profile.getPath() != null) {
				fc.setCurrentDirectory(new File(profile.getPath()));
			}
			final int returnValue = fc.showOpenDialog(this);

			if (returnValue == JFileChooser.APPROVE_OPTION) {
				currentFile = fc.getSelectedFile();
				profile.setPath(currentFile.getParent());
				final MDL animationSourceModel = MDL.read(currentFile);
				addSingleAnimation(current, animationSourceModel);
			}

			fc.setSelectedFile(null);

			refreshController();
		} else if (e.getSource() == animFromUnit) {
			fc.setDialogTitle("Animation Source");
			final String filepath = convertPathToMDX(fetchUnit().getField("file"));
			final MDL current = currentMDL();
			if (filepath != null) {
				final MDL animationSource = MDL.read(MpqCodebase.get().getFile(filepath));
				addSingleAnimation(current, animationSource);
			}
		} else if (e.getSource() == animFromModel) {
			fc.setDialogTitle("Animation Source");
			final String filepath = convertPathToMDX(fetchModel().getFilepath());
			final MDL current = currentMDL();
			if (filepath != null) {
				final MDL animationSource = MDL.read(MpqCodebase.get().getFile(filepath));
				addSingleAnimation(current, animationSource);
			}
		} else if (e.getSource() == animFromObject) {
			fc.setDialogTitle("Animation Source");
			final String filepath = convertPathToMDX(fetchObject().getField("file"));
			final MDL current = currentMDL();
			if (filepath != null) {
				final MDL animationSource = MDL.read(MpqCodebase.get().getFile(filepath));
				addSingleAnimation(current, animationSource);
			}
		} else if (e.getSource() == creditsButton) {
			final DefaultStyledDocument panel = new DefaultStyledDocument();
			final JTextPane epane = new JTextPane();
			final RTFEditorKit rtfk = new RTFEditorKit();
			try {
				rtfk.read(MainPanel.class.getResourceAsStream("credits.rtf"), panel, 0);
			} catch (final MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (final IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (final BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			epane.setDocument(panel);
			final JFrame frame = new JFrame("About");
			frame.setContentPane(new JScrollPane(epane));
			frame.setSize(650, 500);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			// JOptionPane.showMessageDialog(this,new JScrollPane(epane));
		} else {
			boolean done = false;
			for (int i = 3; i < 8 && !done; i++) {
				if (e.getSource() == buttons.get(i)) {
					done = true;
					for (int b = 3; b < 8; b++) {
						buttons.get(b).resetColors();
					}
					buttons.get(i).setColors(prefs.getActiveRColor1(), prefs.getActiveRColor2());
					prefs.setActionType(i);
				}
			}
			for (int i = 0; i < 3 && !done; i++) {
				if (e.getSource() == buttons.get(i)) {
					done = true;
					for (int b = 0; b < 3; b++) {
						buttons.get(b).resetColors();
					}
					buttons.get(i).setColors(prefs.getActiveColor1(), prefs.getActiveColor2());
					prefs.setSelectionType(i);
				}
			}
		}
		// for( int i = 0; i < geoItems.size(); i++ )
		// {
		// JCheckBoxMenuItem geoItem = (JCheckBoxMenuItem)geoItems.get(i);
		// if( e.getSource() == geoItem )
		// {
		// frontArea.setGeosetVisible(i,geoItem.isSelected());
		// frontArea.setGeosetHighlight(i,false);
		// }
		// repaint();
		// }
	}

	private boolean onClickSaveAs() {
		try {
			fc.setDialogTitle("Save as");
			final MDL current = currentMDL();
			if (current != null && !current.isTemp() && current.getFile() != null) {
				fc.setCurrentDirectory(current.getFile().getParentFile());
			} else if (profile.getPath() != null) {
				fc.setCurrentDirectory(new File(profile.getPath()));
			}
			final int returnValue = fc.showSaveDialog(this);
			File temp = fc.getSelectedFile();
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				if (temp != null) {
					final FileFilter ff = fc.getFileFilter();
					final String ext = ff.accept(new File("junk.mdl")) ? ".mdl" : ".mdx";
					if (ff.accept(new File("junk.obj"))) {
						throw new UnsupportedOperationException("OBJ saving has not been coded yet.");
					}
					final String name = temp.getName();
					if (name.lastIndexOf('.') != -1) {
						if (!name.substring(name.lastIndexOf('.'), name.length()).equals(ext)) {
							temp = (new File(
									temp.getAbsolutePath().substring(0, temp.getAbsolutePath().lastIndexOf('.'))
											+ ext));
						}
					} else {
						temp = (new File(temp.getAbsolutePath() + ext));
					}
					currentFile = temp;
					if (temp.exists()) {
						final Object[] options = { "Overwrite", "Cancel" };
						final int n = JOptionPane.showOptionDialog(MainFrame.frame, "Selected file already exists.",
								"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
								options[1]);
						if (n == 1) {
							fc.setSelectedFile(null);
							return false;
						}
					}
					profile.setPath(currentFile.getParent());
					if (ext.equals(".mdl")) {
						currentMDL().printTo(currentFile);
					} else if (prefs.isUseNativeMDXParser()) {
						final MdxModel model = new MdxModel(currentMDL());
						try (BlizzardDataOutputStream writer = new BlizzardDataOutputStream(currentFile)) {
							model.save(writer);
						} catch (final FileNotFoundException e1) {
							e1.printStackTrace();
						} catch (final IOException e1) {
							e1.printStackTrace();
						}
					} else {
						final File currentFileMDL = new File(
								currentFile.getPath().substring(0, currentFile.getPath().length() - 1) + "l");
						currentMDL().printTo(currentFileMDL);
						MDXHandler.compile(currentFileMDL);
					}
					currentMDL().setFile(currentFile);
					currentMDLDisp().resetBeenSaved();
					tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), currentFile.getName().split("\\.")[0]);
					tabbedPane.setToolTipTextAt(tabbedPane.getSelectedIndex(), currentFile.getPath());
				} else {
					JOptionPane.showMessageDialog(this,
							"You tried to save, but you somehow didn't select a file.\nThat is bad.");
				}
			}
			fc.setSelectedFile(null);
			return true;
		} catch (final Exception exc) {
			ExceptionPopup.display(exc);
		}
		refreshController();
		return false;
	}

	private void onClickSave() {
		try {
			if (currentMDL() != null) {
				currentMDL().saveFile();
				profile.setPath(currentMDL().getFile().getParent());
				currentMDLDisp().resetBeenSaved();
			}
		} catch (final Exception exc) {
			ExceptionPopup.display(exc);
		}
		refreshController();
	}

	private void onClickOpen() {
		fc.setDialogTitle("Open");
		final MDL current = currentMDL();
		if (current != null && !current.isTemp() && current.getFile() != null) {
			fc.setCurrentDirectory(current.getFile().getParentFile());
		} else if (profile.getPath() != null) {
			fc.setCurrentDirectory(new File(profile.getPath()));
		}

		final int returnValue = fc.showOpenDialog(this);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			currentFile = fc.getSelectedFile();
			profile.setPath(currentFile.getParent());
			// frontArea.clearGeosets();
			// sideArea.clearGeosets();
			// botArea.clearGeosets();
			modelMenu.getAccessibleContext().setAccessibleDescription(
					"Allows the user to control which parts of the model are displayed for editing.");
			modelMenu.setEnabled(true);
			SaveProfile.get().addRecent(currentFile.getPath());
			updateRecent();
			loadFile(currentFile);
		}

		fc.setSelectedFile(null);

		// //Special thanks to the JWSFileChooserDemo from oracle's Java
		// tutorials, from which many ideas were borrowed for the following
		// FileOpenService fos = null;
		// FileContents fileContents = null;
		//
		// try
		// {
		// fos =
		// (FileOpenService)ServiceManager.lookup("javax.jnlp.FileOpenService");
		// }
		// catch (UnavailableServiceException exc )
		// {
		//
		// }
		//
		// if( fos != null )
		// {
		// try
		// {
		// fileContents = fos.openFileDialog(null, null);
		// }
		// catch (Exception exc )
		// {
		// JOptionPane.showMessageDialog(this,"Opening command failed:
		// "+exc.getLocalizedMessage());
		// }
		// }
		//
		// if( fileContents != null)
		// {
		// try
		// {
		// fileContents.getName();
		// }
		// catch (IOException exc)
		// {
		// JOptionPane.showMessageDialog(this,"Problem opening file:
		// "+exc.getLocalizedMessage());
		// }
		// }
	}

	private void newModel() {
		// TODO Auto-generated method stub

	}

	private Element fetchUnit() {
		final Element choice = UnitOptionPane.show(this);
		if (choice != null) {

		} else {
			return null;
		}

		String filepath = choice.getField("file");

		try {
			filepath = convertPathToMDX(filepath);
			// modelDisp = new MDLDisplay(toLoad, null);
		} catch (final Exception exc) {
			exc.printStackTrace();
			// bad model!
			JOptionPane.showMessageDialog(MainFrame.frame, "The chosen model could not be used.", "Program Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return choice;
	}

	private String convertPathToMDX(String filepath) {
		if (filepath.endsWith(".mdl")) {
			filepath = filepath.replace(".mdl", ".mdx");
		} else if (!filepath.endsWith(".mdx")) {
			filepath = filepath.concat(".mdx");
		}
		return filepath;
	}

	private ModelOptionPane.ModelElement fetchModel() {
		final ModelOptionPane.ModelElement model = ModelOptionPane.showAndLogIcon(this);
		String filepath = model.getFilepath();
		if (filepath != null) {

		} else {
			return null;
		}
		try {
			filepath = convertPathToMDX(filepath);
		} catch (final Exception exc) {
			exc.printStackTrace();
			// bad model!
			JOptionPane.showMessageDialog(MainFrame.frame, "The chosen model could not be used.", "Program Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return model;
	}

	private GameObject fetchObject() {
		final UnitEditorModelSelector selector = new UnitEditorModelSelector();
		final int x = JOptionPane.showConfirmDialog(this, selector, "Object Editor - Select Unit",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		final GameObject choice = selector.getSelection();
		if (choice == null || x != JOptionPane.OK_OPTION) {
			return null;
		}

		String filepath = choice.getField("file");

		try {
			filepath = convertPathToMDX(filepath);
		} catch (final Exception exc) {
			exc.printStackTrace();
			// bad model!
			JOptionPane.showMessageDialog(MainFrame.frame, "The chosen model could not be used.", "Program Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return choice;
	}

	private void addSingleAnimation(final MDL current, final MDL animationSourceModel) {
		Animation choice = null;
		choice = (Animation) JOptionPane.showInputDialog(this, "Choose an animation!", "Add Animation",
				JOptionPane.QUESTION_MESSAGE, null, animationSourceModel.getAnims().toArray(),
				animationSourceModel.getAnims().get(0));
		if (choice == null) {
			JOptionPane.showMessageDialog(this, "Bad choice. No animation added.");
			return;
		}
		final Animation visibilitySource = (Animation) JOptionPane.showInputDialog(this,
				"Which animation from THIS model to copy visiblity from?", "Add Animation",
				JOptionPane.QUESTION_MESSAGE, null, current.getAnims().toArray(), current.getAnims().get(0));
		if (visibilitySource == null) {
			JOptionPane.showMessageDialog(this, "No visibility will be copied.");
		}
		final List<Animation> animationsAdded = current.addAnimationsFrom(animationSourceModel,
				Collections.singletonList(choice));
		for (final Animation anim : animationsAdded) {
			current.copyVisibility(visibilitySource, anim);
		}
		JOptionPane.showMessageDialog(this, "Added " + animationSourceModel.getName() + "'s " + choice.getName()
				+ " with " + visibilitySource.getName() + "'s visibility  OK!");
	}

	class RecentItem extends JMenuItem {
		public RecentItem(final String what) {
			super(what);
		}

		String filepath;
	}

	public void updateRecent() {
		final List<String> recent = SaveProfile.get().getRecent();
		for (final RecentItem recentItem : recentItems) {
			recentMenu.remove(recentItem);
		}
		recentItems.clear();
		for (int i = 0; i < recent.size(); i++) {
			final String fp = recent.get(recent.size() - i - 1);
			if (recentItems.size() <= i || recentItems.get(i).filepath != fp) {
				// String[] bits = recent.get(i).split("/");

				final RecentItem item = new RecentItem(new File(fp).getName());
				item.filepath = fp;
				recentItems.add(item);
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {

						currentFile = new File(item.filepath);
						profile.setPath(currentFile.getParent());
						// frontArea.clearGeosets();
						// sideArea.clearGeosets();
						// botArea.clearGeosets();
						modelMenu.getAccessibleContext().setAccessibleDescription(
								"Allows the user to control which parts of the model are displayed for editing.");
						modelMenu.setEnabled(true);
						SaveProfile.get().addRecent(currentFile.getPath());
						updateRecent();
						loadFile(currentFile);
					}
				});
				recentMenu.add(item);
			}
		}
	}

	public MDL currentMDL() {
		if (tabbedPane.getSelectedComponent() != null) {
			return ((ModelPanel) tabbedPane.getSelectedComponent()).getMDLDisplay().getMDL();
		} else {
			return null;
		}
	}

	public MDLDisplay currentMDLDisp() {
		if (tabbedPane.getSelectedComponent() != null) {
			return ((ModelPanel) tabbedPane.getSelectedComponent()).getMDLDisplay();
		} else {
			return null;
		}
	}

	/**
	 * Returns the MDLDisplay associated with a given MDL, or null if one cannot
	 * be found.
	 *
	 * @param model
	 * @return
	 */
	public MDLDisplay displayFor(final MDL model) {
		MDLDisplay output = null;
		MDLDisplay tempDisplay;
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			tempDisplay = ((ModelPanel) tabbedPane.getComponentAt(i)).getMDLDisplay();
			if (tempDisplay.getMDL() == model) {
				output = tempDisplay;
				break;
			}
		}
		return output;
	}

	public void loadFile(final File f, final boolean temporary, final boolean selectNewTab, final ImageIcon icon) {
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		ModelPanel temp = null;
		if (f.getPath().toLowerCase().endsWith("mdx")) {
			if (prefs.isUseNativeMDXParser()) {
				try (BlizzardDataInputStream in = new BlizzardDataInputStream(new FileInputStream(f))) {
					final MDL model = new MDL(MdxUtils.loadModel(in));
					model.setFile(f);
					temp = new ModelPanel(model, prefs, MainPanel.this, selectionItemTypeGroup, selectionModeGroup);
				} catch (final FileNotFoundException e) {
					e.printStackTrace();
					ExceptionPopup.display(e);
					throw new RuntimeException("Reading mdx failed");
				} catch (final IOException e) {
					e.printStackTrace();
					ExceptionPopup.display(e);
					throw new RuntimeException("Reading mdx failed");
				}
			} else {
				temp = new ModelPanel(MDXHandler.convert(f), prefs, MainPanel.this, selectionItemTypeGroup,
						selectionModeGroup);
			}
		} else if (f.getPath().toLowerCase().endsWith("obj")) {
			// final Build builder = new Build();
			// final MDLOBJBuilderInterface builder = new
			// MDLOBJBuilderInterface();
			final Build builder = new Build();
			try {
				final Parse obj = new Parse(builder, f.getPath());
				temp = new ModelPanel(builder.createMDL(), prefs, MainPanel.this, selectionItemTypeGroup,
						selectionModeGroup);
			} catch (final FileNotFoundException e) {
				ExceptionPopup.display(e);
				e.printStackTrace();
			} catch (final IOException e) {
				ExceptionPopup.display(e);
				e.printStackTrace();
			}
		} else {
			temp = new ModelPanel(f, prefs, MainPanel.this, selectionItemTypeGroup, selectionModeGroup);
		}
		temp.getMDLDisplay().addCoordDisplayListener(new CoordDisplayListener() {
			@Override
			public void notifyUpdate(final byte dimension1, final byte dimension2, final double coord1,
					final double coord2) {
				MainPanel.this.setMouseCoordDisplay(dimension1, dimension2, coord1, coord2);
			}
		});
		if (temporary) {
			temp.getMDLDisplay().getMDL().setTemp(true);
		}

		temp.setFocusable(false);
		if (geoControl == null) {
			geoControl = new ViewController(temp.getMDLDisplay());
		}
		if (!geoControl.getFrame().isVisible()) {
			geoControl.getFrame().setVisible(true);
		}
		tabbedPane.addTab(f.getName().split("\\.")[0], icon, temp, f.getPath());
		if (selectNewTab) {
			tabbedPane.setSelectedComponent(temp);
		}
		if (temporary) {
			temp.getMDLDisplay().getMDL().setFile(null);
		}
		// }
		// }).start();
	}

	public void loadFile(final File f, final boolean temporary) {
		loadFile(f, temporary, true, MDLIcon);
	}

	public void loadFile(final File f) {
		loadFile(f, false);
	}

	public void importFile(final File f) {
		final MDL currentModel = currentMDL();
		if (currentModel != null) {
			importFile(MDL.read(f));
		}
	}

	public void importFile(final MDL model) {
		final MDL currentModel = currentMDL();
		if (currentModel != null) {
			importPanel = new ImportPanel(currentModel, model);
			importPanel.setCallback(new Callback<List<Geoset>>() {
				@Override
				public void run(final List<Geoset> geosetsAdded) {

					// Tell program to set visibility after import
					final MDLDisplay display = displayFor(currentModel);
					if (display != null) {
						display.setBeenSaved(false); // we edited the model
						for (final Geoset geoset : geosetsAdded) {
							display.makeGeosetEditable(geoset, true);
							display.makeGeosetVisible(geoset, true);
						}
						geoControl.repaint();
						geoControl.setMDLDisplay(display);
						display.reloadTextures();// .mpanel.perspArea.reloadTextures();//addGeosets(newGeosets);
					}
				}
			});
		}
	}

	public String incName(final String name) {
		String output = name;

		int depth = 1;
		boolean continueLoop = true;
		while (continueLoop) {
			char c = '0';
			try {
				c = output.charAt(output.length() - depth);
			} catch (final IndexOutOfBoundsException e) {
				// c remains '0'
				continueLoop = false;
			}
			for (char n = '0'; n < '9' && continueLoop; n++) {
				// JOptionPane.showMessageDialog(null,"checking "+c+" against
				// "+n);
				if (c == n) {
					char x = c;
					x++;
					output = output.substring(0, output.length() - depth) + x
							+ output.substring(output.length() - depth + 1);
					continueLoop = false;
				}
			}
			if (c == '9') {
				output = output.substring(0, output.length() - depth) + (0)
						+ output.substring(output.length() - depth + 1);
			} else if (continueLoop) {
				output = output.substring(0, output.length() - depth + 1) + 1
						+ output.substring(output.length() - depth + 1);
				continueLoop = false;
			}
			depth++;
		}
		if (output == null) {
			output = "name error";
		} else if (output.equals(name)) {
			output = output + "_edit";
		}

		return output;
	}

	public void nullmodelFile() {
		final MDL currentMDL = currentMDL();
		if (currentMDL != null) {
			final MDL newModel = new MDL();
			newModel.copyHeaders(currentMDL);
			if (newModel.getFileRef() == null) {
				newModel.setFileRef(
						new File(System.getProperty("java.io.tempdir") + "MatrixEaterExtract/matrixeater_anonymousMDL",
								"" + (int) (Math.random() * Integer.MAX_VALUE) + ".mdl"));
			}
			while (newModel.getFile().exists()) {
				newModel.setFile(
						new File(currentMDL.getFile().getParent() + "/" + incName(newModel.getName()) + ".mdl"));
			}
			importPanel = new ImportPanel(newModel, MDL.deepClone(currentMDL, "CurrentModel"));

			final Thread watcher = new Thread(new Runnable() {
				@Override
				public void run() {
					while (importPanel.getParentFrame().isVisible()
							&& (!importPanel.importStarted() || importPanel.importEnded())) {
						try {
							Thread.sleep(1);
						} catch (final Exception e) {
							ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
						}
					}
					// if( !importPanel.getParentFrame().isVisible() &&
					// !importPanel.importEnded() )
					// JOptionPane.showMessageDialog(null,"bad voodoo
					// "+importPanel.importSuccessful());
					// else
					// JOptionPane.showMessageDialog(null,"good voodoo
					// "+importPanel.importSuccessful());
					// if( importPanel.importSuccessful() )
					// {
					// newModel.saveFile();
					// loadFile(newModel.getFile());
					// }

					if (importPanel.importStarted()) {
						while (!importPanel.importEnded()) {
							try {
								Thread.sleep(1);
							} catch (final Exception e) {
								ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
							}
						}

						if (importPanel.importSuccessful()) {
							newModel.saveFile();
							loadFile(newModel.getFile());
						}
					}
				}
			});
			watcher.start();
		}
	}

	public void parseTriangles(final String input, final Geoset g) {
		// Loading triangles to a geoset requires verteces to be loaded first
		final String[] s = input.split(",");
		s[0] = s[0].substring(4, s[0].length());
		final int s_size = countContainsString(input, ",");
		s[s_size - 1] = s[s_size - 1].substring(0, s[s_size - 1].length() - 2);
		for (int t = 0; t < s_size - 1; t += 3)// s[t+3].equals("")||
		{
			for (int i = 0; i < 3; i++) {
				s[t + i] = s[t + i].substring(1);
			}
			try {
				g.addTriangle(new Triangle(Integer.parseInt(s[t]), Integer.parseInt(s[t + 1]),
						Integer.parseInt(s[t + 2]), g));
			} catch (final NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Error: Unable to interpret information in Triangles: " + s[t]
						+ ", " + s[t + 1] + ", or " + s[t + 2]);
			}
		}
		// try
		// {
		// g.addTriangle(new Triangle(g.getVertex( Integer.parseInt(s[t])
		// ),g.getVertex( Integer.parseInt(s[t+1])),g.getVertex(
		// Integer.parseInt(s[t+2])),g) );
		// }
		// catch (NumberFormatException e)
		// {
		// JOptionPane.showMessageDialog(this,"Error: Unable to interpret
		// information in Triangles.");
		// }
	}

	public boolean doesContainString(final String a, final String b)// see if a
																	// contains
																	// b
	{
		final int l = a.length();
		for (int i = 0; i < l; i++) {
			if (a.startsWith(b, i)) {
				return true;
			}
		}
		return false;
	}

	public int countContainsString(final String a, final String b)// see if a
																	// contains
																	// b
	{
		final int l = a.length();
		int x = 0;
		for (int i = 0; i < l; i++) {
			if (a.startsWith(b, i)) {
				x++;
			}
		}
		return x;
	}

	@Override
	public void refreshUndo() {
		undo.setEnabled(undo.funcEnabled());
		redo.setEnabled(redo.funcEnabled());
	}

	public void refreshController() {
		if (geoControl != null) {
			geoControl.geoControl.repaint();
		}
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		refreshUndo();
		// for( int i = 0; i < geoItems.size(); i++ )
		// {
		// JCheckBoxMenuItem geoItem = (JCheckBoxMenuItem)geoItems.get(i);
		// if( e.getSource() == geoItem )
		// {
		// frontArea.setGeosetHighlight(i,true);
		// }
		// }
		// repaint();
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		refreshUndo();
		// for( int i = 0; i < geoItems.size(); i++ )
		// {
		// JCheckBoxMenuItem geoItem = (JCheckBoxMenuItem)geoItems.get(i);
		// if( e.getSource() == geoItem )
		// {
		// frontArea.setGeosetHighlight(i,false);
		// }
		// }
		// repaint();
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		refreshUndo();
		final Component compFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		System.out.println(compFocusOwner);

	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		refreshUndo();

	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (e.getSource() == tabbedPane && e.getButton() == MouseEvent.BUTTON3) {
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				if (tabbedPane.getBoundsAt(i).contains(e.getX(), e.getY())) {
					contextClickedTab = i;
					contextMenu.show(tabbedPane, e.getX(), e.getY());
				}
			}
		}
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		if (((ModelPanel) tabbedPane.getSelectedComponent()) != null) {
			geoControl.setMDLDisplay(((ModelPanel) tabbedPane.getSelectedComponent()).getMDLDisplay());
		} else {
			geoControl.setMDLDisplay(null);
		}
	}

	public void setMouseCoordDisplay(final byte dim1, final byte dim2, final double value1, final double value2) {
		for (int i = 0; i < mouseCoordDisplay.length; i++) {
			mouseCoordDisplay[i].setText("");
		}
		mouseCoordDisplay[dim1].setText((float) value1 + "");
		mouseCoordDisplay[dim2].setText((float) value2 + "");
	}

	private class UndoMenuItem extends JMenuItem {
		public UndoMenuItem(final String text) {
			super(text);
		}

		@Override
		public String getText() {
			if (funcEnabled()) {
				return "Undo " + ((ModelPanel) tabbedPane.getSelectedComponent()).getMDLDisplay().undoText();// +"
																												// Ctrl+Z";
			} else {
				return "Can't undo";// +" Ctrl+Z";
			}
		}

		public boolean funcEnabled() {
			try {
				return ((ModelPanel) tabbedPane.getSelectedComponent()).getMDLDisplay().canUndo();
			} catch (final NullPointerException e) {
				return false;
			}
		}
	}

	private class RedoMenuItem extends JMenuItem {
		public RedoMenuItem(final String text) {
			super(text);
		}

		@Override
		public String getText() {
			if (funcEnabled()) {
				return "Redo " + ((ModelPanel) tabbedPane.getSelectedComponent()).getMDLDisplay().redoText();// +"
																												// Ctrl+Y";
			} else {
				return "Can't redo";// +" Ctrl+Y";
			}
		}

		public boolean funcEnabled() {
			try {
				return ((ModelPanel) tabbedPane.getSelectedComponent()).getMDLDisplay().canRedo();
			} catch (final NullPointerException e) {
				return false;
			}
		}
	}

	public boolean closeAll() {
		boolean success = true;
		for (int i = tabbedPane.getTabCount() - 1; i >= 0 && success; i--) {
			final ModelPanel modelPanel = (ModelPanel) tabbedPane.getComponentAt(i);
			if (success = modelPanel.close()) {
				tabbedPane.remove(i);
			} // ;//this);
		}
		return success;
	}

	public static BufferedImage removeAlphaChannel(final BufferedImage source) {
		final BufferedImage combined = new BufferedImage(source.getWidth(), source.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		final Graphics g = combined.getGraphics();
		g.drawImage(source, 0, 0, source.getWidth(), source.getHeight(), null);

		return combined;
	}
}
