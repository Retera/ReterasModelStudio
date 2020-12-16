package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.edit.ClonedNodeNamePickerImplementation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.RedoActionImplementation;
import com.hiveworkshop.rms.ui.application.edit.UndoActionImplementation;
import com.hiveworkshop.rms.ui.application.edit.animation.ControllableTimeBoundProvider;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeBoundChooserPanel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeSliderPanel;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorChangeActivityListener;
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
import com.hiveworkshop.rms.util.Vec3;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainPanel extends JPanel
        implements ActionListener, UndoHandler, ModelEditorChangeActivityListener, ModelPanelCloseListener {
    JMenuBar menuBar;
    JMenu recentMenu, toolsMenu, windowMenu;
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
            cloneActionRes();
        }
    };

    private void cloneActionRes() {
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

    AbstractAction deleteAction = new AbstractAction("Delete") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            deleteActionRes();
        }
    };

    private void deleteActionRes() {
        final ModelPanel mpanel = currentModelPanel();
        if (mpanel != null) {
            if (animationModeState) {
                timeSliderPanel.deleteSelectedKeyframes();
            } else {
                mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().deleteSelectedComponents());
            }
        }
        repaintSelfAndChildren(MainPanel.this);
        mpanel.repaintSelfAndRelatedChildren();
    }

    AbstractAction selectAllAction = new AbstractAction("Select All") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            selectAllActionRes();
        }
    };

    private void selectAllActionRes() {
        final ModelPanel mpanel = currentModelPanel();
        if (mpanel != null) {
            mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().selectAll());
        }
        repaint();
    }

    AbstractAction invertSelectAction = new AbstractAction("Invert Selection") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            invertSelectActionRes();
        }
    };

    private void invertSelectActionRes() {
        final ModelPanel mpanel = currentModelPanel();
        if (mpanel != null) {
            mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().invertSelection());
        }
        repaint();
    }

    AbstractAction rigAction = new AbstractAction("Rig") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            rigActionRes();
        }
    };

    private void rigActionRes() {
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

    AbstractAction expandSelectionAction = getExpandSelectionAction();

    private AbstractAction getExpandSelectionAction() {
        return new AbstractAction("Expand Selection") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                getExpandSelectionActionRes();
            }
        };
    }

    private void getExpandSelectionActionRes() {
        final ModelPanel mpanel = currentModelPanel();
        if (mpanel != null) {
            mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().expandSelection());
        }
        repaint();
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

        animatedRenderEnvironment.addChangeListener((start, end) -> animatedRenderEnvChangeResult(MainPanel.this, start, end));

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

        selectionItemTypeGroup.addToolbarButtonListener(this::selectionItemTypeGroupActionRes);

        actionTypeGroup.addToolbarButtonListener(this::actionTypeGroupActionRes);
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
            ModelEditorManager.MOVE_LINKED = dialogResult == settings[0];
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
        setKeyframe.addActionListener(e -> createKeyframe(mainPanel));
        return setKeyframe;
    }

    private static void createKeyframe(MainPanel mainPanel) {
        final ModelPanel mpanel = mainPanel.currentModelPanel();
        if (mpanel != null) {
            mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().createKeyframe(mainPanel.actionType));
        }
        repaintSelfAndChildren(mainPanel);
        mpanel.repaintSelfAndRelatedChildren();
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
        setTimeBounds.addActionListener(e -> timeBoundsChooserPanel(mainPanel));
        return setTimeBounds;
    }

    private static void timeBoundsChooserPanel(MainPanel mainPanel) {
        final TimeBoundChooserPanel timeBoundChooserPanel = new TimeBoundChooserPanel(
                mainPanel.currentModelPanel() == null ? null : mainPanel.currentModelPanel().getModelViewManager(),
                mainPanel.modelStructureChangeListener);
        final int confirmDialogResult = JOptionPane.showConfirmDialog(mainPanel, timeBoundChooserPanel,
                "Set Time Bounds", JOptionPane.OK_CANCEL_OPTION);
        if (confirmDialogResult == JOptionPane.OK_OPTION) {
            timeBoundChooserPanel.applyTo(mainPanel.animatedRenderEnvironment);
            if (mainPanel.currentModelPanel() != null) {
                mainPanel.currentModelPanel().getEditorRenderModel().refreshFromEditor(
                        mainPanel.animatedRenderEnvironment,
                        ModelStructureChangeListenerImplementation.IDENTITY,
                        ModelStructureChangeListenerImplementation.IDENTITY,
                        ModelStructureChangeListenerImplementation.IDENTITY,
                        mainPanel.currentModelPanel().getPerspArea().getViewport());
                mainPanel.currentModelPanel().getEditorRenderModel().updateNodes(true, false);
            }
        }
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

        ImageIcon icon = new ImageIcon(BLPHandler.get()
                .getGameTex("ReplaceableTextures\\CommandButtons\\BTNReplay-Play.blp")
                .getScaledInstance(24, 24, Image.SCALE_FAST));
        final JButton run = new JButton("Run", icon);
        run.addActionListener(showScriptViewAction(mainPanel, matrixEaterScriptTextArea));
        hackerPanel.add(run, BorderLayout.NORTH);
        hackerView = new View("Matrix Eater Script", null, hackerPanel);
        return hackerView;
    }

    private static ActionListener showScriptViewAction(MainPanel mainPanel, RSyntaxTextArea matrixEaterScriptTextArea) {
        return new ActionListener() {
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
                        JOptionPane.showMessageDialog(mainPanel, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(mainPanel, "Must open a file!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
    }

    private static DockingWindowListener getDockingWindowListener2(Runnable fixit) {
        return new DockingWindowListener() {

            @Override
            public void windowUndocking(final DockingWindow removedWindow) {
                if (OLDMODE) {
                    setTitleBarVisibility(removedWindow, true, ": (windowUndocking removedWindow as view) title bar visible now");
                } else {
                    SwingUtilities.invokeLater(fixit);
                }
            }

            @Override
            public void windowUndocked(final DockingWindow arg0) {
            }

            @Override
            public void windowShown(final DockingWindow arg0) {
            }

            @Override
            public void windowRestoring(final DockingWindow arg0) {
            }

            @Override
            public void windowRestored(final DockingWindow arg0) {
            }

            @Override
            public void windowRemoved(final DockingWindow removedFromWindow, final DockingWindow removedWindow) {
                if (OLDMODE) {
                    if (removedFromWindow instanceof TabWindow) {
                        setTitleBarVisibility(removedWindow, true, ": (removedWindow as view) title bar visible now");
                        final TabWindow tabWindow = (TabWindow) removedFromWindow;
                        if (tabWindow.getChildWindowCount() == 1) {
                            final DockingWindow childWindow = tabWindow.getChildWindow(0);
                            setTitleBarVisibility(childWindow, true, ": (singleChildView, windowRemoved()) title bar visible now");
                        } else if (tabWindow.getChildWindowCount() == 0) {
                            System.out.println(tabWindow.getTitle() + ": force close because 0 child windows in windowRemoved()");
//						tabWindow.close();
                        }
                    }
                } else {
                    SwingUtilities.invokeLater(fixit);
                }
            }

            @Override
            public void windowMinimizing(final DockingWindow arg0) {
            }

            @Override
            public void windowMinimized(final DockingWindow arg0) {
            }

            @Override
            public void windowMaximizing(final DockingWindow arg0) {
            }

            @Override
            public void windowMaximized(final DockingWindow arg0) {
            }

            @Override
            public void windowHidden(final DockingWindow arg0) {
            }

            @Override
            public void windowDocking(final DockingWindow arg0) {
            }

            @Override
            public void windowDocked(final DockingWindow arg0) {
            }

            @Override
            public void windowClosing(final DockingWindow closingWindow) {
                if (OLDMODE) {
                    if (closingWindow.getWindowParent() instanceof TabWindow) {
                        setTitleBarVisibility(closingWindow, true, ": (closingWindow as view) title bar visible now");
                        final TabWindow tabWindow = (TabWindow) closingWindow.getWindowParent();
                        if (tabWindow.getChildWindowCount() == 1) {
                            final DockingWindow childWindow = tabWindow.getChildWindow(0);
                            setTitleBarVisibility(childWindow, true, ": (singleChildView, windowClosing()) title bar visible now");
                        } else if (tabWindow.getChildWindowCount() == 0) {
                            System.out.println(tabWindow.getTitle() + ": force close because 0 child windows in windowClosing()");
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
                                setTitleBarVisibility(childWindow, false, ": (singleChildView as view, windowAdded()) title bar NOT visible now");
                            }
                        }
                        setTitleBarVisibility(addedWindow, false, ": (addedWindow as view) title bar NOT visible now");
                    }
                } else {
                    SwingUtilities.invokeLater(fixit);
                }
            }

            @Override
            public void viewFocusChanged(final View arg0, final View arg1) {
            }
        };
    }

    private static void setTitleBarVisibility(DockingWindow removedWindow, boolean setVisible, String s) {
        if (removedWindow instanceof View) {
            final View view = (View) removedWindow;
            view.getViewProperties().getViewTitleBarProperties().setVisible(setVisible);
            System.out.println(view.getTitle() + s);
        }
    }

    private static DockingWindowListener getDockingWindowListener(final MainPanel mainPanel) {
        return new DockingWindowListener() {
            @Override
            public void windowUndocking(final DockingWindow arg0) {
            }

            @Override
            public void windowUndocked(final DockingWindow dockingWindow) {
                SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> {
                    if (dockingWindow instanceof View) {
                        final Component component = ((View) dockingWindow).getComponent();
                        if (component instanceof JComponent) {
                            linkActions(mainPanel, ((JComponent) component).getRootPane());
                        }
                    }
                }));
            }

            @Override
            public void windowShown(final DockingWindow arg0) {
            }

            @Override
            public void windowRestoring(final DockingWindow arg0) {
            }

            @Override
            public void windowRestored(final DockingWindow arg0) {
            }

            @Override
            public void windowRemoved(final DockingWindow arg0, final DockingWindow arg1) {
            }

            @Override
            public void windowMinimizing(final DockingWindow arg0) {
            }

            @Override
            public void windowMinimized(final DockingWindow arg0) {
            }

            @Override
            public void windowMaximizing(final DockingWindow arg0) {
            }

            @Override
            public void windowMaximized(final DockingWindow arg0) {
            }

            @Override
            public void windowHidden(final DockingWindow arg0) {
            }

            @Override
            public void windowDocking(final DockingWindow arg0) {
            }

            @Override
            public void windowDocked(final DockingWindow arg0) {
            }

            @Override
            public void windowClosing(final DockingWindow arg0) {
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
        MenuBarActions.refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
        MPQBrowserView.refreshAnimationModeState(mainPanel);
        ModelStructureChangeListenerImplementation.reloadGeosetManagers(mainPanel, mainPanel.currentModelPanel());

    }

    public void init() {
        final JRootPane root = getRootPane();
        linkActions(this, root);

        MenuBarActions.updateUIFromProgramPreferences(fetchPortraitsToo, modelPanels, prefs, showNormals, showVertexModifyControls, solid, textureModels, wireframe);
    }

    private static void linkActions(final MainPanel mainPanel, final JComponent root) {
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Z"),"Undo");
        root.getActionMap().put("Undo", mainPanel.undoAction);

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Y"), "Redo");
        root.getActionMap().put("Redo", mainPanel.redoAction);

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DELETE"), "Delete");
        root.getActionMap().put("Delete", mainPanel.deleteAction);

        root.getActionMap().put("CloneSelection", mainPanel.cloneAction);

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("SPACE"), "MaximizeSpacebar");
        root.getActionMap().put("MaximizeSpacebar", maximizeSpacebarAction(mainPanel));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("RIGHT"), "PressRight");
        root.getActionMap().put("PressRight", pressRightAction(mainPanel));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("LEFT"), "PressLeft");
        root.getActionMap().put("PressLeft", pressLeftAction(mainPanel));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("UP"), "PressUp");
        root.getActionMap().put("PressUp", jumpFramesAction(mainPanel, 1));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift UP"), "PressShiftUp");
        root.getActionMap().put("PressShiftUp", jumpFramesAction(mainPanel, 10));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DOWN"), "PressDown");
        root.getActionMap().put("PressDown", jumpFramesAction(mainPanel, -1));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift DOWN"), "PressShiftDown");
        root.getActionMap().put("PressShiftDown", jumpFramesAction(mainPanel, -10));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control SPACE"), "PlayKeyboardKey");
        root.getActionMap().put("PlayKeyboardKey", playKeyboardKeyAction(mainPanel));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("W"), "QKeyboardKey");
        root.getActionMap().put("QKeyboardKey", actionShortcutAction(mainPanel, 0));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("E"), "WKeyboardKey");
        root.getActionMap().put("WKeyboardKey", actionShortcutAction(mainPanel, 1));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("R"), "EKeyboardKey");
        root.getActionMap().put("EKeyboardKey", actionShortcutAction(mainPanel, 2));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("T"), "RKeyboardKey");
        root.getActionMap().put("RKeyboardKey", notAnimationActionShortcutAction(mainPanel, 3));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("Y"), "TKeyboardKey");
        root.getActionMap().put("TKeyboardKey", notAnimationActionShortcutAction(mainPanel, 4));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("A"), "AKeyboardKey");
        root.getActionMap().put("AKeyboardKey", itemShortcutAction(mainPanel, 0));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("S"), "SKeyboardKey");
        root.getActionMap().put("SKeyboardKey", itemShortcutAction(mainPanel, 1));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("D"), "DKeyboardKey");
        root.getActionMap().put("DKeyboardKey", itemShortcutAction(mainPanel, 2));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("F"), "FKeyboardKey");
        root.getActionMap().put("FKeyboardKey", itemShortcutAction(mainPanel, 3));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("G"), "GKeyboardKey");
        root.getActionMap().put("GKeyboardKey", itemShortcutAction(mainPanel, 4));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("Z"), "ZKeyboardKey");
        root.getActionMap().put("ZKeyboardKey", zKeyboardKeyAction(mainPanel));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control F"), "CreateFaceShortcut");
        root.getActionMap().put("CreateFaceShortcut", createFaceShortcutAction(mainPanel));

        for (int i = 1; i <= 9; i++) {
            root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("alt pressed " + i), i + "KeyboardKey");
            final int index = i;
            root.getActionMap().put(i + "KeyboardKey", getKeyPressedAction(mainPanel, index));
        }

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift pressed SHIFT"), "shiftSelect");
        root.getActionMap().put("shiftSelect", shiftSelectAction(mainPanel));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("alt pressed ALT"), "altSelect");
        root.getActionMap().put("altSelect", altSelectAction(mainPanel));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released SHIFT"), "unShiftSelect");
        root.getActionMap().put("unShiftSelect", unShiftSelectAction(mainPanel));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released ALT"), "unAltSelect");
        root.getActionMap().put("unAltSelect", unAltSelectAction(mainPanel));

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control A"), "Select All");
        root.getActionMap().put("Select All", mainPanel.selectAllAction);

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control I"), "Invert Selection");
        root.getActionMap().put("Invert Selection", mainPanel.invertSelectAction);

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control E"), "Expand Selection");
        root.getActionMap().put("Expand Selection", mainPanel.expandSelectionAction);

        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control W"), "RigAction");
        root.getActionMap().put("RigAction", mainPanel.rigAction);
    }

    private static AbstractAction unAltSelectAction(MainPanel mainPanel) {
        return new AbstractAction("unAltSelect") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if ((mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.DESELECT) && mainPanel.cheatAlt) {
                    mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
                    mainPanel.cheatAlt = false;
                }
            }
        };
    }


    private static AbstractAction unShiftSelectAction(MainPanel mainPanel) {
        return new AbstractAction("unShiftSelect") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (isTextField()) return;
                if ((mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.ADD)
                        && mainPanel.cheatShift) {
                    mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
                    mainPanel.cheatShift = false;
                }
                ;
            }
        };
    }

    private static AbstractAction altSelectAction(MainPanel mainPanel) {
        return new AbstractAction("altSelect") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
                    mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.DESELECT);
                    mainPanel.cheatAlt = true;
                }
            }
        };
    }

    private static AbstractAction shiftSelectAction(MainPanel mainPanel) {
        return new AbstractAction("shiftSelect") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (isTextField()) return;
                if (mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
                    mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.ADD);
                    mainPanel.cheatShift = true;
                }
            }
        };
    }

    private static AbstractAction maximizeSpacebarAction(MainPanel mainPanel) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                maximizeFocusedWindow(mainPanel);
            }
        };
    }

    private static AbstractAction getKeyPressedAction(MainPanel mainPanel, int index) {
        return new AbstractAction() {
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
        };
    }

    private static AbstractAction createFaceShortcutAction(MainPanel mainPanel) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (isTextField()) return;
                if (!mainPanel.animationModeState) {
                    try {
                        final ModelPanel modelPanel = mainPanel.currentModelPanel();
                        if (modelPanel != null) {
                            final Viewport viewport = mainPanel.activeViewportWatcher.getViewport();
                            final Vec3 facingVector = viewport == null
                                    ? new Vec3(0, 0, 1) : viewport.getFacingVector();
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
        };
    }

    private static AbstractAction zKeyboardKeyAction(MainPanel mainPanel) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (isTextField()) return;
                mainPanel.prefs.setViewMode(mainPanel.prefs.getViewMode() == 1 ? 0 : 1);
            }
        };
    }

    private static AbstractAction itemShortcutAction(MainPanel mainPanel, int i) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (isTextField()) return;
                mainPanel.selectionItemTypeGroup.setToolbarButtonType(mainPanel.selectionItemTypeGroup.getToolbarButtonTypes()[i]);
            }
        };
    }

    private static AbstractAction notAnimationActionShortcutAction(MainPanel mainPanel, int i) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (isTextField()) return;
                if (!mainPanel.animationModeState) {
                    mainPanel.actionTypeGroup.setToolbarButtonType(mainPanel.actionTypeGroup.getToolbarButtonTypes()[i]);
                }
            }
        };
    }

    private static AbstractAction actionShortcutAction(MainPanel mainPanel, int i) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (isTextField()) return;
                mainPanel.actionTypeGroup.setToolbarButtonType(mainPanel.actionTypeGroup.getToolbarButtonTypes()[i]);
            }
        };
    }

    private static AbstractAction playKeyboardKeyAction(MainPanel mainPanel) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (isTextField()) return;
                mainPanel.timeSliderPanel.play();
            }
        };
    }

    private static AbstractAction jumpFramesAction(MainPanel mainPanel, int i) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (isTextField()) return;
                if (mainPanel.animationModeState) {
                    mainPanel.timeSliderPanel.jumpFrames(i);
                }
            }
        };
    }

    private static AbstractAction pressLeftAction(MainPanel mainPanel) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (isTextField()) return;
                if (mainPanel.animationModeState) {
                    mainPanel.timeSliderPanel.jumpLeft();
                }
            }
        };
    }

    private static AbstractAction pressRightAction(MainPanel mainPanel) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (isTextField()) return;
                if (mainPanel.animationModeState) {
                    mainPanel.timeSliderPanel.jumpRight();
                }
            }
        };
    }

    private static boolean isTextField() {
        return focusedComponentNeedsTyping(getFocusedComponent());
    }

    private static void maximizeFocusedWindow(MainPanel mainPanel) {
        if (isTextField()) return;
        final View focusedView = mainPanel.rootWindow.getFocusedView();
        if (focusedView != null) {
            if (focusedView.isMaximized()) {
                mainPanel.rootWindow.setMaximizedWindow(null);
            } else {
                focusedView.maximize();
            }
        }
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

    public static void setMouseCoordDisplay(JTextField[] mouseCoordDisplay, byte dim1, byte dim2, double value1, double value2) {
        for (final JTextField jTextField : mouseCoordDisplay) {
            jTextField.setText("");
        }
        if(dim1 < 0) {
            dim1 = (byte)(-dim1-1);
            value1 = -value1;
        }
        if(dim2 < 0) {
            dim2 = (byte)(-dim2-1);
            value2 = -value2;
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

    private static boolean focusedComponentNeedsTyping(final Component focusedComponent) {
        return (focusedComponent instanceof JTextArea) || (focusedComponent instanceof JTextField) || (focusedComponent instanceof JTextPane);
    }

    private static Component getFocusedComponent() {
        final KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        return kfm.getFocusOwner();
    }

    @Override
    public void save(final EditableModel model) {
        if (model.getFile() != null) {
            try {
                MdxUtils.saveMdx(model, model.getFile()); // TODO should save in the format of the file!
            } catch (final IOException e) {
                e.printStackTrace();
            }
        } else {
            MenuBarActions.onClickSaveAs(this, model);
        }
    }
}
