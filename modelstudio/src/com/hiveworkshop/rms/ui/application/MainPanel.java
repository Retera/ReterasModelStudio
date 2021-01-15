package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.ClonedNodeNamePickerImplementation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.RedoActionImplementation;
import com.hiveworkshop.rms.ui.application.edit.UndoActionImplementation;
import com.hiveworkshop.rms.ui.application.edit.animation.ControllableTimeBoundProvider;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeSliderPanel;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorChangeActivityListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ActiveViewportWatcher;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanelCloseListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.CreatorModelingPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarActionButtonType;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
//    final View hackerView;
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
    //    final JButton setKeyframe;
//    final JButton setTimeBounds;
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
    public AbstractAction undoAction = new UndoActionImplementation("Undo", this);
    public AbstractAction redoAction = new RedoActionImplementation("Redo", this);
    ClonedNodeNamePicker namePicker = new ClonedNodeNamePickerImplementation(this);
    AbstractAction cloneAction = new AbstractAction("CloneSelection") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            cloneActionRes();
        }
    };
    //    protected ModelEditorActionType actionType;
    public ModelEditorActionType actionType;

    AbstractAction deleteAction = new AbstractAction("Delete") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            deleteActionRes();
        }
    };

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

        TimeSliderView.createMouseCoordDisp(mouseCoordDisplay);

        modelStructureChangeListener = ModelStructureChangeListenerImplementation.getModelStructureChangeListener(this);
        animatedRenderEnvironment = new TimeEnvironmentImpl();
        blpPanel = new ZoomableImagePreviewPanel(null);

        TimeSliderView.createTimeSliderPanel(this);

        animatedRenderEnvironment.addChangeListener((start, end) -> animatedRenderEnvChangeResult(MainPanel.this, start, end));

//        setKeyframe = TimeSliderViewThing.createSetKeyframeButton(this);

//        setTimeBounds = TimeSliderViewThing.createSetTimeBoundsButton(this);

        animationModeButton = new ModeButton("Animate");
        animationModeButton.setVisible(false);// TODO remove this if unused

        ClosePopup.createContextMenuPopup(this);

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

//        timeSliderView = TimeSliderViewThing.createTimeSliderView(mouseCoordDisplay, setKeyframe, setTimeBounds, timeSliderPanel);
        timeSliderView = TimeSliderView.createTimeSliderView(timeSliderPanel);

//        hackerView = ScriptView.createHackerView(this);

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
        coordDisplayListener = (dim1, dim2, value1, value2) -> TimeSliderView.setMouseCoordDisplay(mouseCoordDisplay, dim1, dim2, value1, value2);
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

    private static DockingWindowListener getDockingWindowListener2(Runnable fixit) {
        return new DockingWindowAdapter() {

            @Override
            public void windowUndocking(final DockingWindow removedWindow) {
                if (OLDMODE) {
                    setTitleBarVisibility(removedWindow, true, ": (windowUndocking removedWindow as view) title bar visible now");
                } else {
                    SwingUtilities.invokeLater(fixit);
                }
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
        };
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

    private static DockingWindowListener getDockingWindowListener(final MainPanel mainPanel) {
        return new DockingWindowAdapter() {
            @Override
            public void windowUndocked(final DockingWindow dockingWindow) {
                SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> {
                    if (dockingWindow instanceof View) {
                        final Component component = ((View) dockingWindow).getComponent();
                        if (component instanceof JComponent) {
                            MainPanelLinkActions.linkActions(mainPanel, ((JComponent) component).getRootPane());
                        }
                    }
                }));
            }
        };
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

    private void cloneActionRes() {
        final ModelPanel mpanel = currentModelPanel();
        if (mpanel != null) {
            try {
                mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor()
                        .cloneSelectedComponents(namePicker));
            } catch (final Exception exc) {
                ExceptionPopup.display(exc);
            }
            refreshUndo();
            repaintSelfAndChildren(MainPanel.this);
            mpanel.repaintSelfAndRelatedChildren();
        }
    }

    private static void setTitleBarVisibility(DockingWindow removedWindow, boolean setVisible, String s) {
        if (removedWindow instanceof View) {
            final View view = (View) removedWindow;
            view.getViewProperties().getViewTitleBarProperties().setVisible(setVisible);
            System.out.println(view.getTitle() + s);
        }
    }

    private void deleteActionRes() {
        final ModelPanel mpanel = currentModelPanel();
        if (mpanel != null) {
            if (animationModeState) {
                timeSliderPanel.deleteSelectedKeyframes();
            } else {
                mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().deleteSelectedComponents());
            }
            repaintSelfAndChildren(MainPanel.this);
            mpanel.repaintSelfAndRelatedChildren();
        }
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

    private void rigActionRes() {
        final ModelPanel mpanel = currentModelPanel();
        if (mpanel != null) {
            EditableModel model = mpanel.getModel();
            ModelEditorManager editorManager = mpanel.getModelEditorManager();
            boolean valid = false;
            for (final Vec3 v : editorManager.getSelectionView().getSelectedVertices()) {
                final int index = model.getPivots().indexOf(v);

                if (index != -1 && index < model.getIdObjects().size()) {
                    final IdObject node = model.getIdObject(index);
                    if ((node instanceof Bone) && !(node instanceof Helper)) {
                        valid = true;
                    }
                }
            }
            if (valid) {
                mpanel.getUndoManager().pushAction(editorManager.getModelEditor().rig());
            } else {
                System.err.println("NOT RIGGING, NOT VALID");
            }
        }
        repaint();
    }

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
        MainPanelLinkActions.linkActions(this, root);

        MenuBarActions.updateUIFromProgramPreferences(fetchPortraitsToo, modelPanels, prefs, showNormals, showVertexModifyControls, solid, textureModels, wireframe);
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

    public static void repaintSelfAndChildren(MainPanel mainPanel) {
        mainPanel.repaint();
        mainPanel.geoControl.repaint();
        mainPanel.geoControlModelData.repaint();
    }

    final ExportTextureDialog.TextureExporterImpl textureExporter = new ExportTextureDialog.TextureExporterImpl(this);

    @Override
    public void save(final EditableModel model) {
//        System.out.println("Save");
        FileDialog fileDialog = new FileDialog(this);
        fileDialog.onClickSave();
//        fileDialog.onClickSaveAs();
//        File modelFile = model.getFile();
//        if (modelFile != null) {
//            System.out.println(Arrays.toString(modelFile.getName().split(".*(?=\\..+)")));
//            try {
//                MdxUtils.saveMdx(model, modelFile); // TODO should save in the format of the file!
//            } catch (final IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            System.out.println("lul");
//            MenuBarActions.onClickSaveAs(this, model);
//        }
    }
}
