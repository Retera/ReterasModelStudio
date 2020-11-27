package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.edit.ClonedNodeNamePickerImplementation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.RedoActionImplementation;
import com.hiveworkshop.rms.ui.application.edit.UndoActionImplementation;
import com.hiveworkshop.rms.ui.application.edit.animation.*;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.*;
import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ActiveViewportWatcher;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.gui.modeledit.*;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.CreatorModelingPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarActionButtonType;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.ui.util.ModeButton;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.*;
import net.infonode.docking.*;
import net.infonode.docking.util.StringViewMap;
import net.infonode.tabbedpanel.TabAreaVisiblePolicy;
import net.infonode.tabbedpanel.titledtab.TitledTabBorderSizePolicy;
import net.infonode.tabbedpanel.titledtab.TitledTabSizePolicy;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.*;

public class MainPanel extends JPanel
        implements ActionListener, UndoHandler, ModelEditorChangeActivityListener, ModelPanelCloseListener {
    JMenuBar menuBar;
    JMenu fileMenu, recentMenu, editMenu, toolsMenu, mirrorSubmenu, tweaksSubmenu, viewMenu, importMenu, addMenu,
            scriptsMenu, windowMenu, addParticle, animationMenu, singleAnimationMenu, aboutMenu, fetch;
    JCheckBoxMenuItem mirrorFlip, fetchPortraitsToo, showNormals, textureModels, showVertexModifyControls;
    List<JMenuItem> geoItems = new ArrayList<>();
    JMenuItem nullmodelButton;
    List<MenuBar.RecentItem> recentItems = new ArrayList<>();
    MenuBar.UndoMenuItem undo;
    MenuBar.RedoMenuItem redo;

    JMenu viewMode;
    JRadioButtonMenuItem wireframe, solid;
    ButtonGroup viewModes;

    JFileChooser fc, exportTextureDialog;
    File currentFile;
    ImportPanel importPanel;
    protected static final boolean OLDMODE = false;
    boolean loading;
    List<ModelPanel> modelPanels;
    ModelPanel currentModelPanel;
    View frontView, leftView, bottomView, perspectiveView;
    final View timeSliderView;
    final View hackerView;
    final View previewView;
    final View creatorView;
    final View animationControllerView;
    JScrollPane geoControl;
    JScrollPane geoControlModelData;
    JTextField[] mouseCoordDisplay = new JTextField[3];
    boolean cheatShift = false;
    boolean cheatAlt = false;
    SaveProfile profile = SaveProfile.get();
    ProgramPreferences prefs = profile.getPreferences();

    JToolBar toolbar;

    TimeSliderPanel timeSliderPanel;
    final JButton setKeyframe;
    final JButton setTimeBounds;
    final ModeButton animationModeButton;
    boolean animationModeState = false;
    final ZoomableImagePreviewPanel blpPanel;

    final ActiveViewportWatcher activeViewportWatcher = new ActiveViewportWatcher();

    WarcraftDataSourceChangeNotifier directoryChangeNotifier = new WarcraftDataSourceChangeNotifier();

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

    int contextClickedTab = 0;
    JPopupMenu contextMenu;
    AbstractAction undoAction = new UndoActionImplementation("Undo", this);
    AbstractAction redoAction = new RedoActionImplementation("Redo", this);
    ClonedNodeNamePicker namePicker = new ClonedNodeNamePickerImplementation(this);
    AbstractAction cloneAction = new AbstractAction("CloneSelection") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                try {
                    mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor()
                            .cloneSelectedComponents(namePicker));
                } catch (final Exception exc) {
                    ExceptionPopup.display(exc);
                }
            }
            refreshUndo();
            repaintSelfAndChildren(MainPanel.this);
            mpanel.repaintSelfAndRelatedChildren();
        }
    };
    AbstractAction deleteAction = new AbstractAction("Delete") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                if (animationModeState) {
                    timeSliderPanel.deleteSelectedKeyframes();
                } else {
                    mpanel.getUndoManager()
                            .pushAction(mpanel.getModelEditorManager().getModelEditor().deleteSelectedComponents());
                }
            }
            repaintSelfAndChildren(MainPanel.this);
            mpanel.repaintSelfAndRelatedChildren();
        }
    };
    AbstractAction selectAllAction = new AbstractAction("Select All") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().selectAll());
            }
            repaint();
        }
    };
    AbstractAction invertSelectAction = new AbstractAction("Invert Selection") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().invertSelection());
            }
            repaint();
        }
    };
    AbstractAction rigAction = new AbstractAction("Rig") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                boolean valid = false;
                for (final Vec3 v : mpanel.getModelEditorManager().getSelectionView().getSelectedVertices()) {
                    final int index = mpanel.getModel().getPivots().indexOf(v);
                    if (index != -1) {
                        if (index < mpanel.getModel().getIdObjects().size()) {
                            final IdObject node = mpanel.getModel().getIdObject(index);
                            if ((node instanceof Bone) && !(node instanceof Helper)) {
                                valid = true;
                            }
                        }
                    }
                }
                if (valid) {
                    mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().rig());
                } else {
                    System.err.println("NOT RIGGING, NOT VALID");
                }
            }
            repaint();
        }
    };
    AbstractAction expandSelectionAction = getExpandSelectionAction();

    private AbstractAction getExpandSelectionAction() {
        return new AbstractAction("Expand Selection") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = currentModelPanel();
                if (mpanel != null) {
                    mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().expandSelection());
                }
                repaint();
            }
        };
    }


    ToolbarButtonGroup<SelectionItemTypes> selectionItemTypeGroup;
    ToolbarButtonGroup<SelectionMode> selectionModeGroup;
    ToolbarButtonGroup<ToolbarActionButtonType> actionTypeGroup;
    final ModelStructureChangeListener modelStructureChangeListener;

    final ViewportTransferHandler viewportTransferHandler;
    final StringViewMap viewMap;
    final RootWindow rootWindow;
    View viewportControllerWindowView;
    View toolView;
    View modelDataView;
    View modelComponentView;
    private ControllableTimeBoundProvider timeBoundProvider;
    ActivityDescriptor currentActivity;

    public MainPanel() {
        super();

        add(ToolBar.createJToolBar(this));
        // testArea = new PerspDisplayPanel("Graphic Test",2,0);
        // //botArea.setViewport(0,1);
        // add(testArea);

        final JLabel[] divider = new JLabel[3];
        for (int i = 0; i < divider.length; i++) {
            divider[i] = new JLabel("----------");
        }

        createMouseCoordDisp(mouseCoordDisplay);

        modelStructureChangeListener = ModelStructureChangeListenerImplementation.getModelStructureChangeListener(this);
        animatedRenderEnvironment = new TimeEnvironmentImpl();
        blpPanel = new ZoomableImagePreviewPanel(null);

        createTimeSliderPanel(this);

        animatedRenderEnvironment.addChangeListener(new TimeBoundChangeListener() {
            @Override
            public void timeBoundsChanged(final int start, final int end) {
                animatedRenderEnvChangeResult(MainPanel.this, start, end);
            }
        });

        setKeyframe = createSetKeyframeButton(this);

        setTimeBounds = createSetTimeBoundsButton(this);

        animationModeButton = new ModeButton("Animate");
        animationModeButton.setVisible(false);// TODO remove this if unused

        createContextMenuPopup();

        modelPanels = new ArrayList<>();
        final JPanel toolsPanel = new JPanel();
        toolsPanel.setMaximumSize(new Dimension(30, 999999));
        final GroupLayout layout = new GroupLayout(this);
        toolbar.setMaximumSize(new Dimension(80000, 48));

        viewMap = new StringViewMap();

        rootWindow = new RootWindow(viewMap);
        rootWindow.addListener(getDockingWindowListener(this));


        final JPanel contentsDummy = new JPanel();
        contentsDummy.add(new JLabel("..."));
        modelDataView = new View("Contents", null, contentsDummy);
        modelComponentView = new View("Component", null, new JPanel());

//		toolView.getWindowProperties().setCloseEnabled(false);
        rootWindow.getWindowProperties().getTabProperties().getTitledTabProperties().setSizePolicy(TitledTabSizePolicy.EQUAL_SIZE);
        rootWindow.getRootWindowProperties().getViewProperties().getViewTitleBarProperties().setVisible(true);
        rootWindow.getWindowProperties().getTabProperties().getTitledTabProperties().setBorderSizePolicy(TitledTabBorderSizePolicy.EQUAL_SIZE);
        rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties().setTabAreaVisiblePolicy(TabAreaVisiblePolicy.MORE_THAN_ONE_TAB);
        rootWindow.setBackground(Color.GREEN);
        rootWindow.setForeground(Color.GREEN);

        final Runnable fixit = () -> {
            MenuBar.traverseAndReset(rootWindow);
            MainLayoutCreator.traverseAndFix(rootWindow);
        };

        rootWindow.addListener(getDockingWindowListener2(fixit));

        previewView = new View("Preview", null, new JPanel());


        timeSliderView = createTimeSliderView(mouseCoordDisplay, setKeyframe, setTimeBounds, timeSliderPanel);


        hackerView = createHackerView(this);


        creatorPanel = new CreatorModelingPanel(newType -> {
            actionTypeGroup.maybeSetButtonType(newType);
            changeActivity(newType);
        }, prefs, actionTypeGroup, activeViewportWatcher, animatedRenderEnvironment);
        creatorView = new View("Modeling", null, creatorPanel);


        animationControllerView = new View("Animation Controller", null, new JPanel());

        final TabWindow startupTabWindow = MainLayoutCreator.createMainLayout(this);
        rootWindow.setWindow(startupTabWindow);
        rootWindow.getRootWindowProperties().getFloatingWindowProperties().setUseFrame(true);
        startupTabWindow.setSelectedTab(0);

        layout.setHorizontalGroup(layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(toolbar)
                .addComponent(rootWindow));
        layout.setVerticalGroup(layout
                .createSequentialGroup()
                .addComponent(toolbar)
                .addComponent(rootWindow));
        setLayout(layout);


        // Create a file chooser
        ExportTextureDialog.createFileChooser(this);

        ExportTextureDialog.createExportTextureDialog(this);

        // getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
        // Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Redo" );

        // getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
        // Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Undo" );

        // setFocusable(true);
        // selectButton.requestFocus();
        selectionItemTypeGroup.addToolbarButtonListener(newType -> selectionItemTypeGroupActionRes(newType));

        actionTypeGroup.addToolbarButtonListener(newType -> actionTypeGroupActionRes(newType));
        actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[0]);
        viewportTransferHandler = new ViewportTransferHandler();
        coordDisplayListener = (dim1, dim2, value1, value2) -> setMouseCoordDisplay(mouseCoordDisplay, dim1, dim2, value1, value2);
    }

    private void actionTypeGroupActionRes(ToolbarActionButtonType newType) {
        if (newType != null) {
            changeActivity(newType);
        }
    }

    private void selectionItemTypeGroupActionRes(SelectionItemTypes newType) {
        animationModeState = newType == SelectionItemTypes.ANIMATE;
        // we need to refresh the state of stuff AFTER the ModelPanels, this
        // is a pretty signficant design flaw, so we're just going to
        // post to the EDT to get behind them (they're called
        // on the same notifier as this method)
        SwingUtilities.invokeLater(() -> MPQBrowserView.refreshAnimationModeState(MainPanel.this));

        if (newType == SelectionItemTypes.TPOSE) {

            final Object[] settings = {"Move Linked", "Move Single"};
            final Object dialogResult = JOptionPane.showInputDialog(null, "Choose settings:", "T-Pose Settings",
                    JOptionPane.PLAIN_MESSAGE, null, settings, settings[0]);
            final boolean moveLinked = dialogResult == settings[0];
            ModelEditorManager.MOVE_LINKED = moveLinked;
        }
        repaint();
    }

    private void createContextMenuPopup() {
        contextMenu = new JPopupMenu();
        JMenuItem contextClose = new JMenuItem("Close");
        contextClose.addActionListener(this);
        contextMenu.add(contextClose);

        JMenuItem contextCloseOthers = new JMenuItem("Close Others");
        contextCloseOthers.addActionListener(e -> MenuBarActions.closeOthers(this, currentModelPanel));
        contextMenu.add(contextCloseOthers);

        JMenuItem contextCloseAll = new JMenuItem("Close All");
        contextCloseAll.addActionListener(e -> MenuBar.closeAll(this));
        contextMenu.add(contextCloseAll);
    }

    private static View createTimeSliderView(JTextField[] mouseCoordDisplay, JButton setKeyframe, JButton setTimeBounds, TimeSliderPanel timeSliderPanel) {
        final View timeSliderView;
        final JPanel timeSliderAndExtra = new JPanel();
        final GroupLayout tsaeLayout = new GroupLayout(timeSliderAndExtra);
        final Component horizontalGlue = Box.createHorizontalGlue();
        final Component verticalGlue = Box.createVerticalGlue();
        tsaeLayout.setHorizontalGroup(tsaeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(timeSliderPanel)
                .addGroup(tsaeLayout.createSequentialGroup()
                        .addComponent(mouseCoordDisplay[0])
                        .addComponent(mouseCoordDisplay[1])
                        .addComponent(mouseCoordDisplay[2])
                        .addComponent(horizontalGlue)
                        .addComponent(setKeyframe)
                        .addComponent(setTimeBounds)));
        tsaeLayout.setVerticalGroup(tsaeLayout.createSequentialGroup()
                .addComponent(timeSliderPanel)
                .addGroup(tsaeLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(mouseCoordDisplay[0])
                        .addComponent(mouseCoordDisplay[1])
                        .addComponent(mouseCoordDisplay[2])
                        .addComponent(horizontalGlue)
                        .addComponent(setKeyframe)
                        .addComponent(setTimeBounds)));
        timeSliderAndExtra.setLayout(tsaeLayout);

        timeSliderView = new View("Footer", null, timeSliderAndExtra);
        return timeSliderView;
    }

    private static JButton createSetKeyframeButton(MainPanel mainPanel) {
        final JButton setKeyframe;
        setKeyframe = new JButton(RMSIcons.setKeyframeIcon);
        setKeyframe.setMargin(new Insets(0, 0, 0, 0));
        setKeyframe.setToolTipText("Create Keyframe");
        setKeyframe.addActionListener(e -> {
            final ModelPanel mpanel = mainPanel.currentModelPanel();
            if (mpanel != null) {
                mpanel.getUndoManager().pushAction(
                        mpanel.getModelEditorManager().getModelEditor().createKeyframe(mainPanel.actionType));
            }
            repaintSelfAndChildren(mainPanel);
            mpanel.repaintSelfAndRelatedChildren();
        });
        return setKeyframe;
    }

    private static void createMouseCoordDisp(JTextField[] mouseCoordDisplay) {
        for (int i = 0; i < mouseCoordDisplay.length; i++) {
            mouseCoordDisplay[i] = new JTextField("");
            mouseCoordDisplay[i].setMaximumSize(new Dimension(80, 18));
            mouseCoordDisplay[i].setMinimumSize(new Dimension(50, 15));
            mouseCoordDisplay[i].setEditable(false);
        }
    }

    private static JButton createSetTimeBoundsButton(MainPanel mainPanel) {
        final JButton setTimeBounds;
        setTimeBounds = new JButton(RMSIcons.setTimeBoundsIcon);
        setTimeBounds.setMargin(new Insets(0, 0, 0, 0));
        setTimeBounds.setToolTipText("Choose Time Bounds");
        setTimeBounds.addActionListener(e -> {
            final TimeBoundChooserPanel timeBoundChooserPanel = new TimeBoundChooserPanel(
                    mainPanel.currentModelPanel() == null ? null : mainPanel.currentModelPanel().getModelViewManager(),
                    mainPanel.modelStructureChangeListener);
            final int confirmDialogResult = JOptionPane.showConfirmDialog(mainPanel, timeBoundChooserPanel,
                    "Set Time Bounds", JOptionPane.OK_CANCEL_OPTION);
            if (confirmDialogResult == JOptionPane.OK_OPTION) {
                timeBoundChooserPanel.applyTo(mainPanel.animatedRenderEnvironment);
                if (mainPanel.currentModelPanel() != null) {
                    mainPanel.currentModelPanel().getEditorRenderModel().refreshFromEditor(
                            mainPanel.animatedRenderEnvironment, ModelStructureChangeListenerImplementation.IDENTITY, ModelStructureChangeListenerImplementation.IDENTITY, ModelStructureChangeListenerImplementation.IDENTITY,
                            mainPanel.currentModelPanel().getPerspArea().getViewport());
                    mainPanel.currentModelPanel().getEditorRenderModel().updateNodes(true, false);
                }
            }
        });
        return setTimeBounds;
    }

    private static void animatedRenderEnvChangeResult(MainPanel mainPanel, int start, int end) {
        final Integer globalSeq = mainPanel.animatedRenderEnvironment.getGlobalSeq();
        if (globalSeq != null) {
            mainPanel.creatorPanel.setChosenGlobalSeq(globalSeq);
        } else {
            final ModelPanel modelPanel = mainPanel.currentModelPanel();
            if (modelPanel != null) {
                boolean foundAnim = false;
                for (final Animation animation : modelPanel.getModel().getAnims()) {
                    if ((animation.getStart() == start) && (animation.getEnd() == end)) {
                        mainPanel.creatorPanel.setChosenAnimation(animation);
                        foundAnim = true;
                        break;
                    }
                }
                if (!foundAnim) {
                    mainPanel.creatorPanel.setChosenAnimation(null);
                }
            }

        }
    }

    private static void createTimeSliderPanel(MainPanel mainPanel) {
        mainPanel.timeSliderPanel = new TimeSliderPanel(mainPanel.animatedRenderEnvironment, mainPanel.modelStructureChangeListener,
                mainPanel.prefs);
        mainPanel.timeSliderPanel.setDrawing(false);
        mainPanel.timeSliderPanel.addListener(currentTime -> {
            mainPanel.animatedRenderEnvironment
                    .setCurrentTime(currentTime - mainPanel.animatedRenderEnvironment.getStart());
            if (mainPanel.currentModelPanel() != null) {
                mainPanel.currentModelPanel().getEditorRenderModel().updateNodes(true, false);
                mainPanel.currentModelPanel().repaintSelfAndRelatedChildren();
            }
        });
//		timeSliderPanel.addListener(creatorPanel);
    }

    private static View createHackerView(final MainPanel mainPanel) {
        final View hackerView;
        final JPanel hackerPanel = new JPanel(new BorderLayout());
        final RSyntaxTextArea matrixEaterScriptTextArea = new RSyntaxTextArea(20, 60);
        matrixEaterScriptTextArea.setCodeFoldingEnabled(true);
        matrixEaterScriptTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        hackerPanel.add(new RTextScrollPane(matrixEaterScriptTextArea), BorderLayout.CENTER);
        final JButton run = new JButton("Run",
                new ImageIcon(BLPHandler.get().getGameTex("ReplaceableTextures\\CommandButtons\\BTNReplay-Play.blp")
                        .getScaledInstance(24, 24, Image.SCALE_FAST)));
        run.addActionListener(new ActionListener() {
            final ScriptEngineManager factory = new ScriptEngineManager();

            @Override
            public void actionPerformed(final ActionEvent e) {
                final String text = matrixEaterScriptTextArea.getText();
                final ScriptEngine engine = factory.getEngineByName("JavaScript");
                final ModelPanel modelPanel = mainPanel.currentModelPanel();
                if (modelPanel != null) {
                    engine.put("modelPanel", modelPanel);
                    engine.put("model", modelPanel.getModel());
                    engine.put("world", mainPanel);
                    try {
                        engine.eval(text);
                    } catch (final ScriptException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(mainPanel, e1.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(mainPanel, "Must open a file!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        hackerPanel.add(run, BorderLayout.NORTH);
        hackerView = new View("Matrix Eater Script", null, hackerPanel);
        return hackerView;
    }

    private static DockingWindowListener getDockingWindowListener2(Runnable fixit) {
        return new DockingWindowListener() {

            @Override
            public void windowUndocking(final DockingWindow removedWindow) throws OperationAbortedException {
                if (OLDMODE) {
                    if (removedWindow instanceof View) {
                        final View view = (View) removedWindow;
                        view.getViewProperties().getViewTitleBarProperties().setVisible(true);
                        System.out.println(
                                view.getTitle() + ": (windowUndocking removedWindow as view) title bar visible now");
                    }
                } else {
                    SwingUtilities.invokeLater(fixit);
                }
            }

            @Override
            public void windowUndocked(final DockingWindow arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowShown(final DockingWindow arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowRestoring(final DockingWindow arg0) throws OperationAbortedException {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowRestored(final DockingWindow arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowRemoved(final DockingWindow removedFromWindow, final DockingWindow removedWindow) {
                if (OLDMODE) {
                    if (removedFromWindow instanceof TabWindow) {
                        if (removedWindow instanceof View) {
                            final View view = (View) removedWindow;
                            view.getViewProperties().getViewTitleBarProperties().setVisible(true);
                            System.out.println(view.getTitle() + ": (removedWindow as view) title bar visible now");
                        }
                        final TabWindow tabWindow = (TabWindow) removedFromWindow;
                        if (tabWindow.getChildWindowCount() == 1) {
                            final DockingWindow childWindow = tabWindow.getChildWindow(0);
                            if (childWindow instanceof View) {
                                final View singleChildView = (View) childWindow;
                                System.out.println(singleChildView.getTitle()
                                        + ": (singleChildView, windowRemoved()) title bar visible now");
                                singleChildView.getViewProperties().getViewTitleBarProperties().setVisible(true);
                            }
                        } else if (tabWindow.getChildWindowCount() == 0) {
                            System.out.println(
                                    tabWindow.getTitle() + ": force close because 0 child windows in windowRemoved()");
//						tabWindow.close();
                        }
                    }
                } else {
                    SwingUtilities.invokeLater(fixit);
                }
            }

            @Override
            public void windowMinimizing(final DockingWindow arg0) throws OperationAbortedException {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowMinimized(final DockingWindow arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowMaximizing(final DockingWindow arg0) throws OperationAbortedException {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowMaximized(final DockingWindow arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowHidden(final DockingWindow arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowDocking(final DockingWindow arg0) throws OperationAbortedException {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowDocked(final DockingWindow arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowClosing(final DockingWindow closingWindow) throws OperationAbortedException {
                if (OLDMODE) {
                    if (closingWindow.getWindowParent() instanceof TabWindow) {
                        if (closingWindow instanceof View) {
                            final View view = (View) closingWindow;
                            view.getViewProperties().getViewTitleBarProperties().setVisible(true);
                            System.out.println(view.getTitle() + ": (closingWindow as view) title bar visible now");
                        }
                        final TabWindow tabWindow = (TabWindow) closingWindow.getWindowParent();
                        if (tabWindow.getChildWindowCount() == 1) {
                            final DockingWindow childWindow = tabWindow.getChildWindow(0);
                            if (childWindow instanceof View) {
                                final View singleChildView = (View) childWindow;
                                singleChildView.getViewProperties().getViewTitleBarProperties().setVisible(true);
                                System.out.println(singleChildView.getTitle()
                                        + ": (singleChildView, windowClosing()) title bar visible now");
                            }
                        } else if (tabWindow.getChildWindowCount() == 0) {
                            System.out.println(
                                    tabWindow.getTitle() + ": force close because 0 child windows in windowClosing()");
                            tabWindow.close();
                        }
                    }
                } else {
                    SwingUtilities.invokeLater(fixit);
                }
            }

            @Override
            public void windowClosed(final DockingWindow closedWindow) {
            }

            @Override
            public void windowAdded(final DockingWindow addedToWindow, final DockingWindow addedWindow) {
                if (OLDMODE) {
                    if (addedToWindow instanceof TabWindow) {
                        final TabWindow tabWindow = (TabWindow) addedToWindow;
                        if (tabWindow.getChildWindowCount() == 2) {
                            for (int i = 0; i < 2; i++) {
                                final DockingWindow childWindow = tabWindow.getChildWindow(i);
                                if (childWindow instanceof View) {
                                    final View singleChildView = (View) childWindow;
                                    singleChildView.getViewProperties().getViewTitleBarProperties().setVisible(false);
                                    System.out.println(singleChildView.getTitle()
                                            + ": (singleChildView as view, windowAdded()) title bar NOT visible now");
                                }
                            }
                        }
                        if (addedWindow instanceof View) {
                            final View view = (View) addedWindow;
                            view.getViewProperties().getViewTitleBarProperties().setVisible(false);
                            System.out.println(view.getTitle() + ": (addedWindow as view) title bar NOT visible now");
                        }
                    }
                } else {
                    SwingUtilities.invokeLater(fixit);
                }
            }

            @Override
            public void viewFocusChanged(final View arg0, final View arg1) {
                // TODO Auto-generated method stub

            }
        };
    }

    private static DockingWindowListener getDockingWindowListener(final MainPanel mainPanel) {
        return new DockingWindowListener() {
            @Override
            public void windowUndocking(final DockingWindow arg0) throws OperationAbortedException {
            }

            @Override
            public void windowUndocked(final DockingWindow dockingWindow) {
                SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> {
                    if (dockingWindow instanceof View) {
                        final Component component = ((View) dockingWindow).getComponent();
                        if (component instanceof JComponent) {
                            linkActions(mainPanel, ((JComponent) component).getRootPane());
//										linkActions(((JComponent) component));
                        }
                    }
//								final Container topLevelAncestor = dockingWindow.getTopLevelAncestor();
//								if (topLevelAncestor instanceof JComponent) {
//									linkActions(((JComponent) topLevelAncestor).getRootPane());
//									linkActions(((JComponent) topLevelAncestor));
//								}
//								topLevelAncestor.setVisible(false);
                }));
            }

            @Override
            public void windowShown(final DockingWindow arg0) {
            }

            @Override
            public void windowRestoring(final DockingWindow arg0) throws OperationAbortedException {
            }

            @Override
            public void windowRestored(final DockingWindow arg0) {
            }

            @Override
            public void windowRemoved(final DockingWindow arg0, final DockingWindow arg1) {
            }

            @Override
            public void windowMinimizing(final DockingWindow arg0) throws OperationAbortedException {
            }

            @Override
            public void windowMinimized(final DockingWindow arg0) {
            }

            @Override
            public void windowMaximizing(final DockingWindow arg0) throws OperationAbortedException {
            }

            @Override
            public void windowMaximized(final DockingWindow arg0) {
            }

            @Override
            public void windowHidden(final DockingWindow arg0) {
            }

            @Override
            public void windowDocking(final DockingWindow arg0) throws OperationAbortedException {
            }

            @Override
            public void windowDocked(final DockingWindow arg0) {
            }

            @Override
            public void windowClosing(final DockingWindow arg0) throws OperationAbortedException {
            }

            @Override
            public void windowClosed(final DockingWindow arg0) {
            }

            @Override
            public void windowAdded(final DockingWindow arg0, final DockingWindow arg1) {
            }

            @Override
            public void viewFocusChanged(final View arg0, final View arg1) {
            }
        };
    }

    @Override
    public void changeActivity(final ActivityDescriptor newType) {
        currentActivity = newType;
        for (final ModelPanel modelPanel : modelPanels) {
            modelPanel.changeActivity(newType);
        }
        creatorPanel.changeActivity(newType);
    }

    final TimeEnvironmentImpl animatedRenderEnvironment;
    JButton snapButton;
    final CoordDisplayListener coordDisplayListener;
    protected ModelEditorActionType actionType;
    JMenu teamColorMenu;
    final CreatorModelingPanel creatorPanel;
    ToolbarActionButtonType selectAndMoveDescriptor;
    ToolbarActionButtonType selectAndRotateDescriptor;
    ToolbarActionButtonType selectAndScaleDescriptor;
    ToolbarActionButtonType selectAndExtrudeDescriptor;
    ToolbarActionButtonType selectAndExtendDescriptor;

    public static void reloadGUI(MainPanel mainPanel) {
        mainPanel.refreshUndo();
        ToolBar.refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
        MPQBrowserView.refreshAnimationModeState(mainPanel);
        ModelStructureChangeListenerImplementation.reloadGeosetManagers(mainPanel, mainPanel.currentModelPanel());

    }

    public void init() {
        final JRootPane root = getRootPane();
        // JPanel root = this;
        linkActions(this, root);

        MenuBarActions.updateUIFromProgramPreferences(fetchPortraitsToo, modelPanels, prefs, showNormals, showVertexModifyControls, solid, textureModels, wireframe);
        // if( wireframe.isSelected() ){
        // prefs.setViewMode(0);
        // }
        // else if( solid.isSelected() ){
        // prefs.setViewMode(1);
        // }
        // else {
        // prefs.setViewMode(-1);
        // }

        // defaultModelStartupHack();
    }

    private static void linkActions(final MainPanel mainPanel, final JComponent root) {
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Z"),
                "Undo");
        root.getActionMap().put("Undo", mainPanel.undoAction);

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Y"),
                "Redo");
        root.getActionMap().put("Redo", mainPanel.redoAction);

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DELETE"), "Delete");
        root.getActionMap().put("Delete", mainPanel.deleteAction);

        root.getActionMap().put("CloneSelection", mainPanel.cloneAction);

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("SPACE"),
                "MaximizeSpacebar");
        root.getActionMap().put("MaximizeSpacebar", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                final View focusedView = mainPanel.rootWindow.getFocusedView();
                if (focusedView != null) {
                    if (focusedView.isMaximized()) {
                        mainPanel.rootWindow.setMaximizedWindow(null);
                    } else {
                        focusedView.maximize();
                    }
                }
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("RIGHT"),
                "PressRight");
        root.getActionMap().put("PressRight", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                if (mainPanel.animationModeState) {
                    mainPanel.timeSliderPanel.jumpRight();
                }
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("LEFT"),
                "PressLeft");
        root.getActionMap().put("PressLeft", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                if (mainPanel.animationModeState) {
                    mainPanel.timeSliderPanel.jumpLeft();
                }
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("UP"), "PressUp");
        root.getActionMap().put("PressUp", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                if (mainPanel.animationModeState) {
                    mainPanel.timeSliderPanel.jumpFrames(1);
                }
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift UP"),
                "PressShiftUp");
        root.getActionMap().put("PressShiftUp", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                if (mainPanel.animationModeState) {
                    mainPanel.timeSliderPanel.jumpFrames(10);
                }
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DOWN"),
                "PressDown");
        root.getActionMap().put("PressDown", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                if (mainPanel.animationModeState) {
                    mainPanel.timeSliderPanel.jumpFrames(-1);
                }
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift DOWN"),
                "PressShiftDown");
        root.getActionMap().put("PressShiftDown", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                if (mainPanel.animationModeState) {
                    mainPanel.timeSliderPanel.jumpFrames(-10);
                }
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control SPACE"),
                "PlayKeyboardKey");
        root.getActionMap().put("PlayKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                mainPanel.timeSliderPanel.play();
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("W"),
                "QKeyboardKey");
        root.getActionMap().put("QKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                mainPanel.actionTypeGroup
                        .setToolbarButtonType(mainPanel.actionTypeGroup.getToolbarButtonTypes()[0]);
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("E"),
                "WKeyboardKey");
        root.getActionMap().put("WKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                mainPanel.actionTypeGroup
                        .setToolbarButtonType(mainPanel.actionTypeGroup.getToolbarButtonTypes()[1]);
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("R"),
                "EKeyboardKey");
        root.getActionMap().put("EKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                mainPanel.actionTypeGroup
                        .setToolbarButtonType(mainPanel.actionTypeGroup.getToolbarButtonTypes()[2]);
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("T"),
                "RKeyboardKey");
        root.getActionMap().put("RKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                if (!mainPanel.animationModeState) {
                    mainPanel.actionTypeGroup
                            .setToolbarButtonType(mainPanel.actionTypeGroup.getToolbarButtonTypes()[3]);
                }
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("Y"),
                "TKeyboardKey");
        root.getActionMap().put("TKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                if (!mainPanel.animationModeState) {
                    mainPanel.actionTypeGroup
                            .setToolbarButtonType(mainPanel.actionTypeGroup.getToolbarButtonTypes()[4]);
                }
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("A"),
                "AKeyboardKey");
        root.getActionMap().put("AKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                mainPanel.selectionItemTypeGroup
                        .setToolbarButtonType(mainPanel.selectionItemTypeGroup.getToolbarButtonTypes()[0]);
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("S"),
                "SKeyboardKey");
        root.getActionMap().put("SKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                mainPanel.selectionItemTypeGroup
                        .setToolbarButtonType(mainPanel.selectionItemTypeGroup.getToolbarButtonTypes()[1]);
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("D"),
                "DKeyboardKey");
        root.getActionMap().put("DKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                mainPanel.selectionItemTypeGroup
                        .setToolbarButtonType(mainPanel.selectionItemTypeGroup.getToolbarButtonTypes()[2]);
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("F"),
                "FKeyboardKey");
        root.getActionMap().put("FKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                mainPanel.selectionItemTypeGroup
                        .setToolbarButtonType(mainPanel.selectionItemTypeGroup.getToolbarButtonTypes()[3]);
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("G"),
                "GKeyboardKey");
        root.getActionMap().put("GKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                mainPanel.selectionItemTypeGroup
                        .setToolbarButtonType(mainPanel.selectionItemTypeGroup.getToolbarButtonTypes()[4]);
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("Z"),
                "ZKeyboardKey");
        root.getActionMap().put("ZKeyboardKey", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                mainPanel.prefs.setViewMode(mainPanel.prefs.getViewMode() == 1 ? 0 : 1);
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control F"),
                "CreateFaceShortcut");
        root.getActionMap().put("CreateFaceShortcut", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                if (!mainPanel.animationModeState) {
                    try {
                        final ModelPanel modelPanel = mainPanel.currentModelPanel();
                        if (modelPanel != null) {
                            final Viewport viewport = mainPanel.activeViewportWatcher.getViewport();
                            final Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1)
                                    : viewport.getFacingVector();
                            final UndoAction createFaceFromSelection = modelPanel.getModelEditorManager()
                                    .getModelEditor().createFaceFromSelection(facingVector);
                            modelPanel.getUndoManager().pushAction(createFaceFromSelection);
                        }
                    } catch (final FaceCreationException exc) {
                        JOptionPane.showMessageDialog(mainPanel, exc.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (final Exception exc) {
                        ExceptionPopup.display(exc);
                    }
                }
            }
        });

        for (int i = 1; i <= 9; i++) {
            root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                    .put(KeyStroke.getKeyStroke("alt pressed " + i), i + "KeyboardKey");
            final int index = i;
            root.getActionMap().put(i + "KeyboardKey", new AbstractAction() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    final DockingWindow window = mainPanel.rootWindow.getWindow();
                    if (window instanceof TabWindow) {
                        final TabWindow tabWindow = (TabWindow) window;
                        final int tabCount = tabWindow.getChildWindowCount();
                        if ((index - 1) < tabCount) {
                            tabWindow.setSelectedTab(index - 1);
                        }
                    }
                }
            });
        }
        // root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control
        // V"), null);
        // root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control
        // V"),
        // "CloneSelection");

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("shift pressed SHIFT"), "shiftSelect");
        root.getActionMap().put("shiftSelect", new AbstractAction("shiftSelect") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                // if (prefs.getSelectionType() == 0) {
                // for (int b = 0; b < 3; b++) {
                // buttons.get(b).resetColors();
                // }
                // addButton.setColors(prefs.getActiveColor1(),
                // prefs.getActiveColor2());
                // prefs.setSelectionType(1);
                // cheatShift = true;
                // }
                if (mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
                    mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.ADD);
                    mainPanel.cheatShift = true;
                }
            }
        });
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("alt pressed ALT"),
                "altSelect");
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
                if (mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
                    mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.DESELECT);
                    mainPanel.cheatAlt = true;
                }
            }
        });
        // root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        // .put(KeyStroke.getKeyStroke("control pressed CONTROL"), "shiftSelect");

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released SHIFT"),
                "unShiftSelect");
        root.getActionMap().put("unShiftSelect", new AbstractAction("unShiftSelect") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                // if (prefs.getSelectionType() == 1 && cheatShift) {
                // for (int b = 0; b < 3; b++) {
                // buttons.get(b).resetColors();
                // }
                // selectButton.setColors(prefs.getActiveColor1(),
                // prefs.getActiveColor2());
                // prefs.setSelectionType(0);
                // cheatShift = false;
                // }
                if ((mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.ADD)
                        && mainPanel.cheatShift) {
                    mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
                    mainPanel.cheatShift = false;
                }
            }
        });

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released ALT"),
                "unAltSelect");
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
                if ((mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.DESELECT)
                        && mainPanel.cheatAlt) {
                    mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
                    mainPanel.cheatAlt = false;
                }
            }
        });
        // root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released
        // CONTROL"),
        // "unShiftSelect");

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control A"),
                "Select All");
        root.getActionMap().put("Select All", mainPanel.selectAllAction);

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control I"),
                "Invert Selection");
        root.getActionMap().put("Invert Selection", mainPanel.invertSelectAction);

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control E"),
                "Expand Selection");
        root.getActionMap().put("Expand Selection", mainPanel.expandSelectionAction);

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control W"),
                "RigAction");
        root.getActionMap().put("RigAction", mainPanel.rigAction);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        refreshUndo();
    }


    public EditableModel currentMDL() {
        if (currentModelPanel != null) {
            return currentModelPanel.getModel();
        } else {
            return null;
        }
    }

    public static ModelEditorManager currentMDLDisp(ModelPanel currentModelPanel) {
        if (currentModelPanel != null) {
            return currentModelPanel.getModelEditorManager();
        } else {
            return null;
        }
    }

    public ModelPanel currentModelPanel() {
        return currentModelPanel;
    }

    @Override
    public void refreshUndo() {
        undo.setEnabled(undo.funcEnabled());
        redo.setEnabled(redo.funcEnabled());
    }

    // @Override
    // public void mouseEntered(final MouseEvent e) {
    // refreshUndo();
    // }

    // @Override
    // public void mouseExited(final MouseEvent e) {
    // refreshUndo();
    // }

    // @Override
    // public void mousePressed(final MouseEvent e) {
    // refreshUndo();
    // }

    // @Override
    // public void mouseReleased(final MouseEvent e) {
    // refreshUndo();
    //
    // }

    // @Override
    // public void mouseClicked(final MouseEvent e) {
    // if (e.getSource() == tabbedPane && e.getButton() == MouseEvent.BUTTON3) {
    // for (int i = 0; i < tabbedPane.getTabCount(); i++) {
    // if (tabbedPane.getBoundsAt(i).contains(e.getX(), e.getY())) {
    // contextClickedTab = i;
    // contextMenu.show(tabbedPane, e.getX(), e.getY());
    // }
    // }
    // }
    // }

    // @Override
    // public void stateChanged(final ChangeEvent e) {
    // if (((ModelPanel) tabbedPane.getSelectedComponent()) != null) {
    // geoControl.setMDLDisplay(((ModelPanel)
    // tabbedPane.getSelectedComponent()).getModelViewManagingTree());
    // } else {
    // geoControl.setMDLDisplay(null);
    // }
    // }

    public static void setMouseCoordDisplay(JTextField[] mouseCoordDisplay, final byte dim1, final byte dim2, final double value1, final double value2) {
        for (final JTextField jTextField : mouseCoordDisplay) {
            jTextField.setText("");
        }
        mouseCoordDisplay[dim1].setText((float) value1 + "");
        mouseCoordDisplay[dim2].setText((float) value2 + "");
    }

    public static void repaintSelfAndChildren(MainPanel mainPanel) {
        mainPanel.repaint();
        mainPanel.geoControl.repaint();
        mainPanel.geoControlModelData.repaint();
    }

    final ExportTextureDialog.TextureExporterImpl textureExporter = new ExportTextureDialog.TextureExporterImpl(this);

    @Override
    public void save(final EditableModel model) {
        if (model.getFile() != null) {
            try {
                MdxUtils.saveMdx(model, model.getFile());
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            MenuBarActions.onClickSaveAs(this, model);
        }
    }

    private static Component getFocusedComponent() {
        final KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        return kfm.getFocusOwner();
    }

    private static boolean focusedComponentNeedsTyping(final Component focusedComponent) {
        return (focusedComponent instanceof JTextArea) || (focusedComponent instanceof JTextField);
    }
}
