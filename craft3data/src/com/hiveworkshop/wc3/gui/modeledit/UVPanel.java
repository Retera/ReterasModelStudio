package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.icons.RMSIcons;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.TVertexEditorActivityDescriptor;
import com.hiveworkshop.wc3.gui.modeledit.activity.TVertexEditorMultiManipulatorActivity;
import com.hiveworkshop.wc3.gui.modeledit.activity.TVertexEditorViewportActivity;
import com.hiveworkshop.wc3.gui.modeledit.activity.TVertexEditorViewportActivityManager;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.uv.MoverWidgetTVertexEditorManipulatorBuilder;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.uv.RotatorWidgetTVertexEditorManipulatorBuilder;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.uv.ScaleWidgetTVertexEditorManipulatorBuilder;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.DoNothingTVertexActivity;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexEditorChangeActivityListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexEditorChangeNotifier;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexToolbarActionButtonType;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.viewport.TVertexEditorManager;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.wc3.gui.modeledit.selection.TVertexSelectionItemTypes;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonListener;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.user.SaveProfile;
import com.hiveworkshop.wc3.util.IconUtils;

import net.infonode.docking.View;

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
        UVIcon = new ImageIcon(IconUtils
                .worldEditStyleIcon(RMSIcons.loadTabImage("UVMap.png")));
    }

    ModeButton loadImage, selectButton, addButton, deselectButton, moveButton, rotateButton, scaleButton, unwrapButton;
    private final JComboBox<UnwrapDirection> unwrapDirectionBox;
    JButton snapButton;
    JMenuItem selectAll, invertSelect, expandSelection, selFromMain, mirrorX, mirrorY, setAspectRatio;
    JMenu editMenu, mirrorSubmenu, dispMenu;
    JMenuBar menuBar;
    JCheckBoxMenuItem wrapImage;

    ArrayList<ModeButton> buttons = new ArrayList<>();
    private final ModelPanel dispMDL;
    private UVViewport vp;
    private final JButton up, down, left, right, plusZoom, minusZoom;
    private final JTextField[] mouseCoordDisplay = new JTextField[2];
    private final TVertexEditorViewportActivityManager viewportActivityManager;
    private final TVertexEditorChangeNotifier modelEditorChangeNotifier;
    private final TVertexEditorManager modelEditorManager;

    int selectionType = 0;
    boolean cheatShift = false;
    boolean cheatAlt = false;
    ModelEditorActionType actionType;

    View view;
    AbstractAction selectAllAction = new AbstractAction("Select All") {
        @Override
        public void actionPerformed(final ActionEvent e) {

            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                mpanel.getUndoManager().pushAction(modelEditorManager.getModelEditor().selectAll());
            }
            repaint();
        }
    };
    AbstractAction invertSelectAction = new AbstractAction("Invert Selection") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                mpanel.getUndoManager().pushAction(modelEditorManager.getModelEditor().invertSelection());
            }
            repaint();
        }
    };
    AbstractAction expandSelectionAction = new AbstractAction("Expand Selection") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                mpanel.getUndoManager().pushAction(modelEditorManager.getModelEditor().expandSelection());
            }
            repaint();
        }
    };
    AbstractAction selFromMainAction = new AbstractAction("Sel From Main") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                mpanel.getUndoManager().pushAction(modelEditorManager.getModelEditor()
                        .selectFromViewer(mpanel.getModelEditorManager().getSelectionView()));
            }
            repaint();
        }
    };
    AbstractAction mirrorXAction = new AbstractAction("Mirror X") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                final TVertex selectionCenter = modelEditorManager.getModelEditor().getSelectionCenter();
                mpanel.getUndoManager().pushAction(
                        modelEditorManager.getModelEditor().mirror((byte) 0, selectionCenter.x, selectionCenter.y));
            }
            repaint();
        }
    };
    AbstractAction mirrorYAction = new AbstractAction("Mirror Y") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                final TVertex selectionCenter = modelEditorManager.getModelEditor().getSelectionCenter();
                mpanel.getUndoManager().pushAction(
                        modelEditorManager.getModelEditor().mirror((byte) 1, selectionCenter.x, selectionCenter.y));
            }
            repaint();
        }
    };
    private ToolbarButtonGroup<TVertexSelectionItemTypes> selectionItemTypeGroup;
    private ToolbarButtonGroup<SelectionMode> selectionModeGroup;
    private ToolbarButtonGroup<TVertexToolbarActionButtonType> actionTypeGroup;

    private JToolBar toolbar;

    private final int uvLayerIndex = 0;

    private TVertexToolbarActionButtonType selectAndMoveDescriptor;

    private final ProgramPreferences prefs;

    private TVertexToolbarActionButtonType selectAndRotateDescriptor;

    private TVertexToolbarActionButtonType selectAndScaleDescriptor;

    private TVertexEditorActivityDescriptor currentActivity;

    private AbstractAction undoAction;

    private AbstractAction redoAction;
    private final List<ModeButton> modeButtons = new ArrayList<>();
    private final List<ModeButton> selectionModeButtons = new ArrayList<>();
    private final Map<TVertexEditorActivityDescriptor, ModeButton> typeToButton = new HashMap<>();
    private final Map<SelectionMode, ModeButton> modeToButton = new HashMap<>();

    public UVPanel(final ModelPanel dispMDL, final ProgramPreferences prefs,
                   final ModelStructureChangeListener modelStructureChangeListener) {
        add(createJToolBar());

        viewportActivityManager = new TVertexEditorViewportActivityManager(new DoNothingTVertexActivity());
        modelEditorChangeNotifier = new TVertexEditorChangeNotifier();
        modelEditorChangeNotifier.subscribe(viewportActivityManager);
        modelEditorManager = new TVertexEditorManager(dispMDL.getModelViewManager(), prefs, selectionModeGroup,
                modelEditorChangeNotifier, viewportActivityManager, dispMDL.getEditorRenderModel(),
                modelStructureChangeListener);

        this.prefs = prefs;
        setBorder(BorderFactory.createLineBorder(Color.black));// BorderFactory.createCompoundBorder(
        // BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title),BorderFactory.createBevelBorder(1)),BorderFactory.createEmptyBorder(1,1,1,1)
        // ));
        setOpaque(true);
        setViewport(dispMDL);
        this.dispMDL = dispMDL;

        // Copied from MainPanel
        selectButton = new ModeButton("Select");
        modeToButton.put(selectionModeGroup.getToolbarButtonTypes()[0], selectButton);
        selectButton.addActionListener(new ButtonModeChangeListener(0));
        selectionModeButtons.add(selectButton);
        addButton = new ModeButton("Add");
        modeToButton.put(selectionModeGroup.getToolbarButtonTypes()[1], addButton);
        addButton.addActionListener(new ButtonModeChangeListener(1));
        selectionModeButtons.add(addButton);
        deselectButton = new ModeButton("Deselect");
        modeToButton.put(selectionModeGroup.getToolbarButtonTypes()[2], deselectButton);
        deselectButton.addActionListener(new ButtonModeChangeListener(2));
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
        unwrapButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final UnwrapDirection selectedItem = (UnwrapDirection) unwrapDirectionBox.getSelectedItem();
                if (selectedItem != null) {
                    switch (selectedItem) {
                        case BOTTOM:
                            remap((byte) 1, (byte) 0, selectedItem);
                            break;
                        case FRONT:
                            remap((byte) 1, (byte) 2, selectedItem);
                            break;
                        case RIGHT:
                            remap((byte) 0, (byte) 2, selectedItem);
                            break;
                        case PERSPECTIVE:
                            break;
                    }
                } else {
                    JOptionPane.showMessageDialog(UVPanel.this, "Please select a direction", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

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

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setMaximumSize(new Dimension(100, 35));
            buttons.get(i).setMinimumSize(new Dimension(90, 15));
            buttons.get(i).addActionListener(this);
        }

        plusZoom = new JButton("");
        Dimension dim = new Dimension(20, 20);
        plusZoom.setMaximumSize(dim);
        plusZoom.setMinimumSize(dim);
        plusZoom.setPreferredSize(dim);
        plusZoom.setIcon(new ImageIcon(RMSIcons.loadDeprecatedImage("Plus.png")));
        plusZoom.addActionListener(this);
        add(plusZoom);

        minusZoom = new JButton("");
        minusZoom.setMaximumSize(dim);
        minusZoom.setMinimumSize(dim);
        minusZoom.setPreferredSize(dim);
        minusZoom.setIcon(new ImageIcon(RMSIcons.loadDeprecatedImage("Minus.png")));
        minusZoom.addActionListener(this);
        add(minusZoom);

        up = new JButton("");
        dim = new Dimension(32, 16);
        up.setMaximumSize(dim);
        up.setMinimumSize(dim);
        up.setPreferredSize(dim);
        up.setIcon(new ImageIcon(RMSIcons.loadDeprecatedImage("ArrowUp.png")));
        up.addActionListener(this);
        add(up);

        down = new JButton("");
        down.setMaximumSize(dim);
        down.setMinimumSize(dim);
        down.setPreferredSize(dim);
        down.setIcon(new ImageIcon(RMSIcons.loadDeprecatedImage("ArrowDown.png")));
        down.addActionListener(this);
        add(down);

        dim = new Dimension(16, 32);
        left = new JButton("");
        left.setMaximumSize(dim);
        left.setMinimumSize(dim);
        left.setPreferredSize(dim);
        left.setIcon(new ImageIcon(RMSIcons.loadDeprecatedImage("ArrowLeft.png")));
        left.addActionListener(this);
        add(left);

        right = new JButton("");
        right.setMaximumSize(dim);
        right.setMinimumSize(dim);
        right.setPreferredSize(dim);
        right.setIcon(new ImageIcon(RMSIcons.loadDeprecatedImage("ArrowRight.png")));
        right.addActionListener(this);
        add(right);

        toolbar.setMaximumSize(new Dimension(80000, 48));
        final GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(toolbar)
                                .addComponent(vp)
                                .addGroup(layout.createSequentialGroup().addComponent(mouseCoordDisplay[0])
                                        .addComponent(mouseCoordDisplay[1]).addGap(120)
                                        .addGroup(layout.createSequentialGroup().addComponent(left)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                        .addComponent(up).addComponent(down))
                                                .addComponent(right))

                                        .addGap(16)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                .addComponent(plusZoom).addComponent(minusZoom))))

                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(loadImage)
                                .addComponent(divider[0]).addComponent(selectButton).addComponent(addButton)
                                .addComponent(deselectButton).addComponent(divider[1]).addComponent(moveButton)
                                .addComponent(rotateButton).addComponent(scaleButton).addComponent(divider[2])
                                .addComponent(unwrapDirectionBox).addComponent(unwrapButton)));
        layout.setVerticalGroup(layout.createSequentialGroup().addComponent(toolbar)
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup().addComponent(vp).addGroup(layout.createParallelGroup()
                                .addComponent(mouseCoordDisplay[0]).addComponent(mouseCoordDisplay[1])
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addGroup(layout.createSequentialGroup().addComponent(up)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                        .addComponent(left).addComponent(right))
                                                .addComponent(down))

                                        .addGroup(layout.createSequentialGroup().addComponent(plusZoom).addGap(16)
                                                .addComponent(minusZoom)))

                        ))

                        .addGroup(layout.createSequentialGroup().addComponent(loadImage).addGap(8)
                                .addComponent(divider[0]).addGap(8).addComponent(selectButton).addGap(8)
                                .addComponent(addButton).addGap(8).addComponent(deselectButton).addGap(8)
                                .addComponent(divider[1]).addGap(8).addComponent(moveButton).addGap(8)
                                .addComponent(rotateButton).addGap(8).addComponent(scaleButton).addGap(8)
                                .addComponent(divider[2]).addGap(8).addComponent(unwrapDirectionBox).addGap(8)
                                .addComponent(unwrapButton).addGap(8).addGap(8))));

        setLayout(layout);
        selectionModeGroup.addToolbarButtonListener(new ToolbarButtonListener<SelectionMode>() {
            @Override
            public void typeChanged(final SelectionMode newType) {
                resetSelectionModeButtons();
                final ModeButton selectionModeButton = modeToButton.get(newType);
                if (selectionModeButton != null) {
                    selectionModeButton.setColors(prefs.getActiveColor1(), prefs.getActiveColor2());
                }
            }
        });

        selectionItemTypeGroup.addToolbarButtonListener(new ToolbarButtonListener<TVertexSelectionItemTypes>() {
            @Override
            public void typeChanged(final TVertexSelectionItemTypes newType) {
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
            }
        });

        actionTypeGroup.addToolbarButtonListener(new ToolbarButtonListener<TVertexToolbarActionButtonType>() {
            @Override
            public void typeChanged(final TVertexToolbarActionButtonType newType) {
                if (newType != null) {
                    changeActivity(newType);
                }
            }
        });
        actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[0]);

        final JPanel menuHolderPanel = new JPanel(new BorderLayout());
        menuHolderPanel.add(this, BorderLayout.CENTER);
        menuHolderPanel.add(createMenuBar(), BorderLayout.BEFORE_FIRST_LINE);
        view = new View("Texture Coordinate Editor: " + currentModelPanel().getModel().getName(), UVIcon,
                menuHolderPanel);
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
        undoAction = new AbstractAction("Undo", RMSIcons.loadToolBarImageIcon("undo.png")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    currentModelPanel().getUndoManager().undo();
                } catch (final NoSuchElementException exc) {
                    JOptionPane.showMessageDialog(UVPanel.this, "Nothing to undo!");
                } catch (final Exception exc) {
                    ExceptionPopup.display(exc);
                    // exc.printStackTrace();
                }
                repaint();
            }
        };
        toolbar.add(undoAction);
        redoAction = new AbstractAction("Redo", RMSIcons.loadToolBarImageIcon("redo.png")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    currentModelPanel().getUndoManager().redo();
                } catch (final NoSuchElementException exc) {
                    JOptionPane.showMessageDialog(UVPanel.this, "Nothing to redo!");
                } catch (final Exception exc) {
                    ExceptionPopup.display(exc);
                    // exc.printStackTrace();
                }
                repaint();
            }
        };
        toolbar.add(redoAction);
        toolbar.addSeparator();
        selectionModeGroup = new ToolbarButtonGroup<>(toolbar, SelectionMode.values());
        toolbar.addSeparator();
        selectionItemTypeGroup = new ToolbarButtonGroup<>(toolbar, TVertexSelectionItemTypes.values());
        toolbar.addSeparator();
        selectAndMoveDescriptor = new TVertexToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("move2.png"), "Select and Move") {
            @Override
            public TVertexEditorViewportActivity createActivity(final TVertexEditorManager modelEditorManager,
                                                                final ModelView modelView, final UndoActionListener undoActionListener) {
                actionType = ModelEditorActionType.TRANSLATION;
                return new TVertexEditorMultiManipulatorActivity(
                        new MoverWidgetTVertexEditorManipulatorBuilder(modelEditorManager.getModelEditor(),
                                modelEditorManager.getViewportSelectionHandler(), prefs, modelView),
                        undoActionListener, modelEditorManager.getSelectionView());
            }
        };
        selectAndRotateDescriptor = new TVertexToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("rotate.png"), "Select and Rotate") {
            @Override
            public TVertexEditorViewportActivity createActivity(final TVertexEditorManager modelEditorManager,
                                                                final ModelView modelView, final UndoActionListener undoActionListener) {
                actionType = ModelEditorActionType.ROTATION;
                return new TVertexEditorMultiManipulatorActivity(
                        new RotatorWidgetTVertexEditorManipulatorBuilder(modelEditorManager.getModelEditor(),
                                modelEditorManager.getViewportSelectionHandler(), prefs, modelView),
                        undoActionListener, modelEditorManager.getSelectionView());
            }
        };
        selectAndScaleDescriptor = new TVertexToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("scale.png"), "Select and Scale") {
            @Override
            public TVertexEditorViewportActivity createActivity(final TVertexEditorManager modelEditorManager,
                                                                final ModelView modelView, final UndoActionListener undoActionListener) {
                actionType = ModelEditorActionType.SCALING;
                return new TVertexEditorMultiManipulatorActivity(
                        new ScaleWidgetTVertexEditorManipulatorBuilder(modelEditorManager.getModelEditor(),
                                modelEditorManager.getViewportSelectionHandler(), prefs, modelView),
                        undoActionListener, modelEditorManager.getSelectionView());
            }
        };
        actionTypeGroup = new ToolbarButtonGroup<>(toolbar, new TVertexToolbarActionButtonType[]{
                selectAndMoveDescriptor, selectAndRotateDescriptor, selectAndScaleDescriptor});
        currentActivity = actionTypeGroup.getActiveButtonType();
        toolbar.addSeparator();
        snapButton = toolbar.add(new AbstractAction("Snap", RMSIcons.loadToolBarImageIcon("snap.png")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    final ModelPanel currentModelPanel = currentModelPanel();
                    if (currentModelPanel != null) {
                        currentModelPanel.getUndoManager()
                                .pushAction(modelEditorManager.getModelEditor().snapSelectedVertices());
                    }
                } catch (final Exception exc) {
                    ExceptionPopup.display(exc);
                }
            }
        });

        return toolbar;
    }

    public JMenuBar createMenuBar() {
        // Create my menu bar
        menuBar = new JMenuBar();

        editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.getAccessibleContext().setAccessibleDescription(
                "Allows the user to use various tools to edit the currently selected model's TVertices.");
        menuBar.add(editMenu);

        dispMenu = new JMenu("View");
        dispMenu.setMnemonic(KeyEvent.VK_V);
        dispMenu.getAccessibleContext()
                .setAccessibleDescription("Control display settings for this Texture Coordinate Editor window.");
        menuBar.add(dispMenu);

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

        selFromMain = new JMenuItem("Select from Viewer");
        selFromMain.setAccelerator(KeyStroke.getKeyStroke("control V"));
        selFromMain.addActionListener(selFromMainAction);
        editMenu.add(selFromMain);

        final JMenuItem splitVertex = new JMenuItem("Split Vertex");
        splitVertex.setAccelerator(KeyStroke.getKeyStroke("control V"));
        splitVertex.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = currentModelPanel();
                if (mpanel != null) {
                    // mpanel.getUndoManager().pushAction(modelEditorManager.getModelEditor()
                    // .selectFromViewer(mpanel.getModelEditorManager().getSelectionView()));

                    final Collection<? extends TVertex> selectedTVertices = modelEditorManager.getSelectionView()
                            .getSelectedTVertices(currentLayer());
                    for (final Geoset g : mpanel.getModel().getGeosets()) {
                        for (final GeosetVertex gv : new ArrayList<>(g.getVertices())) {
                            final TVertex tVertex = gv.getTVertex(currentLayer());
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
                    /*
                     * Collection<Triangle> selectedFaces =
                     * modelEditorManager.getSelectionView().getSelectedFaces(); for(Triangle t:
                     * selectedFaces) { for(int i = 0; i < 3; i++) { GeosetVertex gv = t.get(i);
                     * if(gv.getTriangles().size()>1) { GeosetVertex copy = new GeosetVertex(gv);
                     * t.set(i, copy); copy.getTriangles().add(t); } } }
                     */

                }
                repaint();
            }
        });
        editMenu.add(splitVertex);

        wrapImage = new JCheckBoxMenuItem("Wrap Image", false);
        wrapImage.setToolTipText(
                "Repeat the texture many times in a grid-like display. This feature does not edit the model in any way; only this viewing window.");
        // wrapImage.addActionListener(this);
        dispMenu.add(wrapImage);

        setAspectRatio = new JMenuItem("Set Aspect Ratio");
        setAspectRatio.setMnemonic(KeyEvent.VK_S);
        setAspectRatio.setAccelerator(KeyStroke.getKeyStroke("control R"));
        setAspectRatio.setToolTipText(
                "Sets the amount by which the texture display is stretched, for editing textures with non-uniform width and height.");
        setAspectRatio.addActionListener(this);
        dispMenu.add(setAspectRatio);

        editMenu.add(new JSeparator());

        mirrorSubmenu = new JMenu("Mirror");
        mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
        mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");
        editMenu.add(mirrorSubmenu);

        mirrorX = new JMenuItem("Mirror X");
        mirrorX.setMnemonic(KeyEvent.VK_X);
        mirrorX.addActionListener(mirrorXAction);
        mirrorSubmenu.add(mirrorX);

        mirrorY = new JMenuItem("Mirror Y");
        mirrorY.setMnemonic(KeyEvent.VK_Y);
        mirrorY.addActionListener(mirrorYAction);
        mirrorSubmenu.add(mirrorY);

        return menuBar;
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
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Z"),
                "Undo");

        root.getActionMap().put("Redo", redoAction);
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Y"),
                "Redo");

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("W"),
                "QKeyboardKey");
        root.getActionMap().put("QKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[0]);
            }
        });
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("E"),
                "WKeyboardKey");
        root.getActionMap().put("WKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[1]);
            }
        });
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("R"),
                "EKeyboardKey");
        root.getActionMap().put("EKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[2]);
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("A"),
                "AKeyboardKey");
        root.getActionMap().put("AKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                selectionItemTypeGroup.setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[0]);
            }
        });
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("S"),
                "SKeyboardKey");
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
                // if (prefs.getSelectionType() == 0) {
                // for (int b = 0; b < 3; b++) {
                // buttons.get(b).resetColors();
                // }
                // addButton.setColors(prefs.getActiveColor1(),
                // prefs.getActiveColor2());
                // prefs.setSelectionType(1);
                // cheatShift = true;
                // }
                if (selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
                    selectionModeGroup.setToolbarButtonType(SelectionMode.ADD);
                    cheatShift = true;
                }
            }
        });
        root.getActionMap().put("altSelect", new AbstractAction("altSelect") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                // if (prefs.getSelectionType() == 0) {
                // for (int b = 0; b < 3; b++) {
                // buttons.get(b).resetColors();
                // }
                // deselectButton.setColors(prefs.getActiveColor1(),
                // prefs.getActiveColor2());
                // prefs.setSelectionType(2);
                // cheatAlt = true;
                // }
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
                // if (prefs.getSelectionType() == 1 && cheatShift) {
                // for (int b = 0; b < 3; b++) {
                // buttons.get(b).resetColors();
                // }
                // selectButton.setColors(prefs.getActiveColor1(),
                // prefs.getActiveColor2());
                // prefs.setSelectionType(0);
                // cheatShift = false;
                // }
                if ((selectionModeGroup.getActiveButtonType() == SelectionMode.ADD) && cheatShift) {
                    selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
                    cheatShift = false;
                }
            }
        });
        root.getActionMap().put("unAltSelect", new AbstractAction("unAltSelect") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                // if (prefs.getSelectionType() == 2 && cheatAlt) {
                // for (int b = 0; b < 3; b++) {
                // buttons.get(b).resetColors();
                // }
                // selectButton.setColors(prefs.getActiveColor1(),
                // prefs.getActiveColor2());
                // prefs.setSelectionType(0);
                // cheatAlt = false;
                // }
                if ((selectionModeGroup.getActiveButtonType() == SelectionMode.DESELECT) && cheatAlt) {
                    selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
                    cheatAlt = false;
                }
            }
        });
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released SHIFT"),
                "unShiftSelect");
        // root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released
        // CONTROL"),
        // "unShiftSelect");
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

    // public void addGeoset(Geoset g)
    // {
    // m_geosets.add(g);
    // }
    // public void setGeosetVisible(int index, boolean flag)
    // {
    // Geoset geo = (Geoset)m_geosets.get(index);
    // geo.setVisible(flag);
    // }
    // public void setGeosetHighlight(int index, boolean flag)
    // {
    // Geoset geo = (Geoset)m_geosets.get(index);
    // geo.setHighlight(flag);
    // }
    // public void clearGeosets()
    // {
    // m_geosets.clear();
    // }
    // public int getGeosetsSize()
    // {
    // return m_geosets.size();
    // }
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == up) {
            vp.translate(0, 20 * (1 / vp.getZoomAmount()));
            vp.repaint();
        } else if (e.getSource() == down) {
            vp.translate(0, -20 * (1 / vp.getZoomAmount()));
            vp.repaint();
        } else if (e.getSource() == left) {
            vp.translate(20 * (1 / vp.getZoomAmount()), 0);
            vp.repaint();
        } else if (e.getSource() == right) {
            vp.translate(-20 * (1 / vp.getZoomAmount()), 0);// *vp.getZoomAmount()
            vp.repaint();
        } else if (e.getSource() == plusZoom) {
            vp.zoom(.15);
            vp.repaint();
        } else if (e.getSource() == minusZoom) {
            vp.zoom(-.15);
            vp.repaint();
        } else if (e.getSource() == loadImage) {

            final int x = JOptionPane.showConfirmDialog(this,
                    "Do you want to use the texture auto-loader to find available textures?\nIf you choose \"No\", then you will have to find a file on your hard drive instead.",
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
                // if( dispMDL.visibleGeosets.size() > 0 )
                // {
                //// for(Layer lay:
                // dispMDL.visibleGeosets.get(0).getMaterial().layers)
                //// {
                //// Bitmap tex = lay.firstTexture();
                //// String path = tex.getPath();
                //// if( path.length() == 0 )
                //// {
                //// if( tex.getReplaceableId() == 1 )
                //// {
                //// path = "ReplaceableTextures\\TeamColor\\TeamColor00.blp";
                //// }
                //// else if( tex.getReplaceableId() == 2 )
                //// {
                //// path = "ReplaceableTextures\\TeamGlow\\TeamGlow00.blp";
                //// }
                //// }
                //// try {
                //// vp.addBackgroundImage(BLPHandler.get().getGameTex(path));
                //// }
                //// catch (Exception exc)
                //// {
                //// exc.printStackTrace();
                //// try {
                //// vp.addBackgroundImage(BLPHandler.get().getCustomTex(dispMDL.getMDL().getFile().getParent()+"\\"+path));
                //// }
                //// catch (Exception exc2)
                //// {
                //// exc2.printStackTrace();
                //// JOptionPane.showMessageDialog(this, "BLP texture-loader
                // failed.");
                //// }
                //// }
                //// }
                // }
            }
        } else if (e.getSource() == setAspectRatio) {
            final JPanel panel = new JPanel();
            final JSpinner widthVal = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
            final JSpinner heightVal = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
            final JLabel toLabel = new JLabel(" to ");
            panel.add(widthVal);
            panel.add(toLabel);
            panel.add(heightVal);
            JOptionPane.showMessageDialog(this, panel);
            vp.setAspectRatio(
                    ((Integer) widthVal.getValue()).intValue() / (double) ((Integer) heightVal.getValue()).intValue());
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
     *
     * @return
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

    public static enum UnwrapDirection {
        FRONT("Front"), RIGHT("Right"), BOTTOM("Bottom"), PERSPECTIVE("Perspective");

        private final String displayText;

        private UnwrapDirection(final String displayText) {
            this.displayText = displayText;
        }

        @Override
        public String toString() {
            return displayText;
        }
    }
}
