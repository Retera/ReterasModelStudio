package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.GameObject;
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
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.application.scripts.AnimationTransfer;
import com.hiveworkshop.rms.ui.application.scripts.ChangeAnimationLengthFrame;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterUnitEditorModelSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.UnitFields;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPane;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPane.ModelElement;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPane;
import com.hiveworkshop.rms.ui.gui.modeledit.*;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.CreatorModelingPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarActionButtonType;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TextureExporter;
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

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
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
    JMenuItem newModel, open, fetchUnit, fetchModel, fetchObject, save, close, exit, revert, mergeGeoset, saveAs,
            importButton, importUnit, importGameModel, importGameObject, importFromWorkspace, importButtonS,
            newDirectory, creditsButton, jokeButton, changelogButton, clearRecent, nullmodelButton, selectAll, invertSelect,
            expandSelection, snapNormals, snapVertices, flipAllUVsU, flipAllUVsV, inverseAllUVs, mirrorX, mirrorY,
            mirrorZ, insideOut, insideOutNormals, showMatrices, editUVs, exportTextures, editTextures, scaleAnimations,
            animationViewer, animationController, modelingTab, mpqViewer, hiveViewer, unitViewer, preferencesWindow,
            linearizeAnimations, sortBones, simplifyKeyframes, rigButton, duplicateSelection, riseFallBirth,
            animFromFile, animFromUnit, animFromModel, animFromObject, teamColor, teamGlow;
    JMenuItem cut, copy, paste;
    List<MenuBar.RecentItem> recentItems = new ArrayList<>();
    MenuBar.UndoMenuItem undo;
    MenuBar.RedoMenuItem redo;

    JMenu viewMode;
    JRadioButtonMenuItem wireframe, solid;
    ButtonGroup viewModes;

    JFileChooser fc, exportTextureDialog;
    File currentFile;
    ImportPanel importPanel;
    public static final ImageIcon AnimIcon = RMSIcons.AnimIcon;
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

    JMenuItem contextClose, contextCloseAll, contextCloseOthers;
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
    AbstractAction cutAction = new AbstractAction("Cut") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                try {
                    mpanel.getModelEditorManager();// cut
                    // something
                    // to
                    // clipboard
                } catch (final Exception exc) {
                    ExceptionPopup.display(exc);
                }
            }
            refreshUndo();
            repaintSelfAndChildren(MainPanel.this);
            mpanel.repaintSelfAndRelatedChildren();
        }
    };
    AbstractAction copyAction = new AbstractAction("Copy") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                try {
                    mpanel.getModelEditorManager();// copy
                    // something
                    // to
                    // clipboard
                } catch (final Exception exc) {
                    ExceptionPopup.display(exc);
                }
            }
            refreshUndo();
            repaintSelfAndChildren(MainPanel.this);
            mpanel.repaintSelfAndRelatedChildren();
        }
    };
    AbstractAction pasteAction = new AbstractAction("Paste") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                try {
                    mpanel.getModelEditorManager();// paste
                    // something
                    // from
                    // clipboard
                } catch (final Exception exc) {
                    ExceptionPopup.display(exc);
                }
            }
            refreshUndo();
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
    JMenuItem combineAnims;
    JMenuItem exportAnimatedToStaticMesh;
    JMenuItem exportAnimatedFramePNG;
    final ViewportTransferHandler viewportTransferHandler;
    final StringViewMap viewMap;
    final RootWindow rootWindow;
    final View viewportControllerWindowView;
    final View toolView;
    final View modelDataView;
    final View modelComponentView;
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
        for (int i = 0; i < mouseCoordDisplay.length; i++) {
            mouseCoordDisplay[i] = new JTextField("");
            mouseCoordDisplay[i].setMaximumSize(new Dimension(80, 18));
            mouseCoordDisplay[i].setMinimumSize(new Dimension(50, 15));
            mouseCoordDisplay[i].setEditable(false);
        }
        modelStructureChangeListener = new ModelStructureChangeListenerImplementation(this, () -> currentModelPanel().getModel());
        animatedRenderEnvironment = new TimeEnvironmentImpl();
        blpPanel = new ZoomableImagePreviewPanel(null);

        createTimeSliderPanel(this);

        animatedRenderEnvironment.addChangeListener(new TimeBoundChangeListener() {
            @Override
            public void timeBoundsChanged(final int start, final int end) {
                animatedRenderEnvChangeResult(MainPanel.this, start, end);
            }
        });

        setKeyframe = new JButton(RMSIcons.setKeyframeIcon);
        setKeyframe.setMargin(new Insets(0, 0, 0, 0));
        setKeyframe.setToolTipText("Create Keyframe");
        setKeyframe.addActionListener(e -> {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                mpanel.getUndoManager().pushAction(
                        mpanel.getModelEditorManager().getModelEditor().createKeyframe(actionType));
            }
            repaintSelfAndChildren(this);
            mpanel.repaintSelfAndRelatedChildren();
        });

        setTimeBounds = createSetTimeBoundsButton(this);

        animationModeButton = new ModeButton("Animate");
        animationModeButton.setVisible(false);// TODO remove this if unused

        contextMenu = new JPopupMenu();
        contextClose = new JMenuItem("Close");
        contextClose.addActionListener(this);
        contextMenu.add(contextClose);

        contextCloseOthers = new JMenuItem("Close Others");
        contextCloseOthers.addActionListener(this);
        contextMenu.add(contextCloseOthers);

        contextCloseAll = new JMenuItem("Close All");
        contextCloseAll.addActionListener(this);
        contextMenu.add(contextCloseAll);

        modelPanels = new ArrayList<>();
        final JPanel toolsPanel = new JPanel();
        toolsPanel.setMaximumSize(new Dimension(30, 999999));
        final GroupLayout layout = new GroupLayout(this);
        toolbar.setMaximumSize(new Dimension(80000, 48));

        viewMap = new StringViewMap();

        rootWindow = new RootWindow(viewMap);
        rootWindow.addListener(getDockingWindowListener(this));

        final JPanel jPanel = new JPanel();
        jPanel.add(new JLabel("..."));

        viewportControllerWindowView = new View("Outliner", null, jPanel);// GlobalIcons.geoIcon
//		viewportControllerWindowView.getWindowProperties().setCloseEnabled(false);
//		viewportControllerWindowView.getWindowProperties().setMaximizeEnabled(true);
//		viewportControllerWindowView.getWindowProperties().setMinimizeEnabled(true);
//		viewportControllerWindowView.getWindowProperties().setRestoreEnabled(true);
        toolView = new View("Tools", null, new JPanel());
        final JPanel contentsDummy = new JPanel();
        contentsDummy.add(new JLabel("..."));
        modelDataView = new View("Contents", null, contentsDummy);
        modelComponentView = new View("Component", null, new JPanel());

//		toolView.getWindowProperties().setCloseEnabled(false);
        rootWindow.getWindowProperties().getTabProperties().getTitledTabProperties()
                .setSizePolicy(TitledTabSizePolicy.EQUAL_SIZE);
        rootWindow.getRootWindowProperties().getViewProperties().getViewTitleBarProperties().setVisible(true);
        rootWindow.getWindowProperties().getTabProperties().getTitledTabProperties()
                .setBorderSizePolicy(TitledTabBorderSizePolicy.EQUAL_SIZE);
        rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties()
                .getTabAreaProperties().setTabAreaVisiblePolicy(TabAreaVisiblePolicy.MORE_THAN_ONE_TAB);
        rootWindow.setBackground(Color.GREEN);
        rootWindow.setForeground(Color.GREEN);
        final Runnable fixit = () -> {
            MenuBar.traverseAndReset(rootWindow);
            MenuBar.traverseAndFix(rootWindow);
        };

        rootWindow.addListener(getDockingWindowListener2(fixit));

        leftView = new View("Side", null, new JPanel());
        frontView = new View("Front", null, new JPanel());
        bottomView = new View("Bottom", null, new JPanel());
        perspectiveView = new View("Perspective", null, new JPanel());
        previewView = new View("Preview", null, new JPanel());
        final JPanel timeSliderAndExtra = new JPanel();
        final GroupLayout tsaeLayout = new GroupLayout(timeSliderAndExtra);
        final Component horizontalGlue = Box.createHorizontalGlue();
        final Component verticalGlue = Box.createVerticalGlue();
        tsaeLayout.setHorizontalGroup(tsaeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(timeSliderPanel)
                .addGroup(tsaeLayout.createSequentialGroup().addComponent(mouseCoordDisplay[0])
                        .addComponent(mouseCoordDisplay[1]).addComponent(mouseCoordDisplay[2])
                        .addComponent(horizontalGlue).addComponent(setKeyframe).addComponent(setTimeBounds)));
        tsaeLayout.setVerticalGroup(tsaeLayout.createSequentialGroup().addComponent(timeSliderPanel)
                .addGroup(tsaeLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(mouseCoordDisplay[0]).addComponent(mouseCoordDisplay[1])
                        .addComponent(mouseCoordDisplay[2]).addComponent(horizontalGlue)
                        .addComponent(setKeyframe).addComponent(setTimeBounds)));
        timeSliderAndExtra.setLayout(tsaeLayout);

        timeSliderView = new View("Footer", null, timeSliderAndExtra);


        hackerView = createHackerView(this);


        creatorPanel = new CreatorModelingPanel(newType -> {
            actionTypeGroup.maybeSetButtonType(newType);
            changeActivity(newType);
        }, prefs, actionTypeGroup, activeViewportWatcher, animatedRenderEnvironment);
        creatorView = new View("Modeling", null, creatorPanel);


        animationControllerView = new View("Animation Controller", null, new JPanel());

        final TabWindow startupTabWindow = MenuBar.createMainLayout(this);
        rootWindow.setWindow(startupTabWindow);
        rootWindow.getRootWindowProperties().getFloatingWindowProperties().setUseFrame(true);
        startupTabWindow.setSelectedTab(0);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(toolbar)
                .addComponent(rootWindow));
        layout.setVerticalGroup(
                layout.createSequentialGroup().addComponent(toolbar).addComponent(rootWindow));
        setLayout(layout);


        // Create a file chooser
        createFileChooser(this);

        createExportTextureDialog(this);

        // getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
        // Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Redo" );

        // getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
        // Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Undo" );

        // setFocusable(true);
        // selectButton.requestFocus();
        selectionItemTypeGroup.addToolbarButtonListener(newType -> {
            animationModeState = newType == SelectionItemTypes.ANIMATE;
            // we need to refresh the state of stuff AFTER the ModelPanels, this
            // is a pretty signficant design flaw, so we're just going to
            // post to the EDT to get behind them (they're called
            // on the same notifier as this method)
            SwingUtilities.invokeLater(() -> MenuBar.refreshAnimationModeState(MainPanel.this));

            if (newType == SelectionItemTypes.TPOSE) {

                final Object[] settings = {"Move Linked", "Move Single"};
                final Object dialogResult = JOptionPane.showInputDialog(null, "Choose settings:", "T-Pose Settings",
                        JOptionPane.PLAIN_MESSAGE, null, settings, settings[0]);
                final boolean moveLinked = dialogResult == settings[0];
                ModelEditorManager.MOVE_LINKED = moveLinked;
            }
            repaint();
        });

        actionTypeGroup.addToolbarButtonListener(newType -> {
            if (newType != null) {
                changeActivity(newType);
            }
        });
        actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[0]);
        viewportTransferHandler = new ViewportTransferHandler();
        coordDisplayListener = (dim1, dim2, value1, value2) -> setMouseCoordDisplay(mouseCoordDisplay, dim1, dim2, value1, value2);
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
                            mainPanel.animatedRenderEnvironment, MenuBar.IDENTITY, MenuBar.IDENTITY, MenuBar.IDENTITY,
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

    private static void createExportTextureDialog(MainPanel mainPanel) {
        mainPanel.exportTextureDialog = new JFileChooser();
        mainPanel.exportTextureDialog.setDialogTitle("Export Texture");
        final String[] imageTypes = ImageIO.getWriterFileSuffixes();
        for (final String suffix : imageTypes) {
            mainPanel.exportTextureDialog
                    .addChoosableFileFilter(new FileNameExtensionFilter(suffix.toUpperCase() + " Image File", suffix));
        }
    }

    private static void createFileChooser(MainPanel mainPanel) {
        mainPanel.fc = new JFileChooser();
        mainPanel.fc.setAcceptAllFileFilterUsed(false);
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("Supported Files (*.mdx;*.mdl;*.blp;*.dds;*.tga;*.png;*.obj,*.fbx)", "mdx", "mdl", "blp", "dds", "tga", "png", "obj", "fbx"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Files (*.mdx;*.mdl;*.blp;*.dds;*.tga)", "mdx", "mdl", "blp", "dds", "tga"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Binary Model (*.mdx)", "mdx"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Text Model (*.mdl)", "mdl"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III BLP Image (*.blp)", "blp"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("DDS Image (*.dds)", "dds"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("TGA Image (*.tga)", "tga"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("PNG Image (*.png)", "png"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("Wavefront OBJ Model (*.obj)", "obj"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("Autodesk FBX Model (*.fbx)", "fbx"));
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

    private static void reloadGeosetManagers(MainPanel mainPanel, final ModelPanel display) {
        mainPanel.geoControl.repaint();
        display.getModelViewManagingTree().reloadFromModelView();
        mainPanel.geoControl.setViewportView(display.getModelViewManagingTree());
        MenuBar.reloadComponentBrowser(mainPanel.geoControlModelData, display);
        display.getPerspArea().reloadTextures();// .mpanel.perspArea.reloadTextures();//addGeosets(newGeosets);
        display.getAnimationViewer().reload();
        display.getAnimationController().reload();
        mainPanel.creatorPanel.reloadAnimationList();

        display.getEditorRenderModel().refreshFromEditor(mainPanel.animatedRenderEnvironment, MenuBar.IDENTITY, MenuBar.IDENTITY, MenuBar.IDENTITY,
                display.getPerspArea().getViewport());
    }

    public static void reloadGUI(MainPanel mainPanel) {
        mainPanel.refreshUndo();
        ToolBar.refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
        MenuBar.refreshAnimationModeState(mainPanel);
        reloadGeosetManagers(mainPanel, mainPanel.currentModelPanel());

    }

    public void init() {
        final JRootPane root = getRootPane();
        // JPanel root = this;
        linkActions(this, root);

        MenuBar.updateUIFromProgramPreferences(fetchPortraitsToo, modelPanels, prefs, showNormals, showVertexModifyControls, solid, textureModels, wireframe);
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
        root.getActionMap().put("Undo", mainPanel.undoAction);
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Z"),
                "Undo");

        root.getActionMap().put("Redo", mainPanel.redoAction);
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Y"),
                "Redo");

        root.getActionMap().put("Delete", mainPanel.deleteAction);
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DELETE"), "Delete");

        root.getActionMap().put("CloneSelection", mainPanel.cloneAction);

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
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("SPACE"),
                "MaximizeSpacebar");

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
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("RIGHT"),
                "PressRight");
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
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("LEFT"),
                "PressLeft");
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
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("UP"), "PressUp");
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
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift UP"),
                "PressShiftUp");
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
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DOWN"),
                "PressDown");
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
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift DOWN"),
                "PressShiftDown");

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
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("shift pressed SHIFT"), "shiftSelect");
        // root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        // .put(KeyStroke.getKeyStroke("control pressed CONTROL"), "shiftSelect");
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("alt pressed ALT"),
                "altSelect");

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
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released SHIFT"),
                "unShiftSelect");
        // root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released
        // CONTROL"),
        // "unShiftSelect");
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released ALT"),
                "unAltSelect");

        root.getActionMap().put("Select All", mainPanel.selectAllAction);
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control A"),
                "Select All");

        root.getActionMap().put("Invert Selection", mainPanel.invertSelectAction);
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control I"),
                "Invert Selection");

        root.getActionMap().put("Expand Selection", mainPanel.expandSelectionAction);
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control E"),
                "Expand Selection");

        root.getActionMap().put("RigAction", mainPanel.rigAction);
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control W"),
                "RigAction");
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        // Open, off of the file menu:
        refreshUndo();
        try {
            if (e.getSource() == newModel) {
                ToolBar.newModel(this);
            } else if (e.getSource() == open) {
                ToolBar.onClickOpen(this);
            } else if (e.getSource() == close) {
                closePanelActionRes();
            } else if (e.getSource() == fetchUnit) {
                fetchUnitActionRes();
            } else if (e.getSource() == fetchModel) {
                fetchModelActionRes();
            } else if (e.getSource() == fetchObject) {
                fetchObjectActionRes();
            } else if (e.getSource() == importButton) {
                importButtonActionRes();
            } else if (e.getSource() == importUnit) {
                importUnitActionRes();
            } else if (e.getSource() == importGameModel) {
                importGameModelActionRes();
            } else if (e.getSource() == importGameObject) {
                importGameObjectActionRes();
            } else if (e.getSource() == importFromWorkspace) {
                importFromWorkspaceActionRes();
            } else if (e.getSource() == importButtonS) {
                importButtonSActionRes();
            } else if (e.getSource() == mergeGeoset) {
                mergeGeosetActionRes();
            } else if (e.getSource() == clearRecent) {
                clearRecentActionRes();
            } else if (e.getSource() == nullmodelButton) {
                nullmodelButtonActionRes();
            } else if ((e.getSource() == save) && (currentMDL() != null) && (currentMDL().getFile() != null)) {
                ToolBar.onClickSave(this);
            } else if (e.getSource() == saveAs) {
                onClickSaveAs(this);
                // } else if (e.getSource() == contextClose) {
                // if (((ModelPanel) tabbedPane.getComponentAt(contextClickedTab)).close()) {//
                // this);
                // tabbedPane.remove(contextClickedTab);
                // }
            } else if (e.getSource() == contextCloseAll) {
                MenuBar.closeAll(this);
            } else if (e.getSource() == contextCloseOthers) {
                closeOthers(this, currentModelPanel);
            } else if (e.getSource() == showVertexModifyControls) {
                showVertexModifyControlsActionRes();
            } else if (e.getSource() == textureModels) {
                prefs.setTextureModels(textureModels.isSelected());
            } else if (e.getSource() == showNormals) {
                prefs.setShowNormals(showNormals.isSelected());
            } else if (e.getSource() == editUVs) {
                editUVsActionRes();
            } else if (e.getSource() == exportTextures) {
                exportTexturesActionRes();
            } else if (e.getSource() == scaleAnimations) {
                scaleAnimationsActionRes();
            } else if (e.getSource() == linearizeAnimations) {
                linearizeAnimationsActionRes();
            } else if (e.getSource() == duplicateSelection) {
                duplicateSelectionActionRes();
            } else if (e.getSource() == simplifyKeyframes) {
                simplifyKeyframesActionRes();
            } else if (e.getSource() == riseFallBirth) {
                riseFallBirthActionRes();
            } else if (e.getSource() == animFromFile) {
                animFromFileActionRes();
            } else if (e.getSource() == animFromUnit) {
                animFromUnitActionRes();
            } else if (e.getSource() == animFromModel) {
                animFromModelActionRes();
            } else if (e.getSource() == animFromObject) {
                animFromObjectActionRes();
            } else if (e.getSource() == creditsButton) {
                creditsButtonActionRes("docs/credits.rtf", "About");
            } else if (e.getSource() == changelogButton) {
                creditsButtonActionRes("docs/changelist.rtf", "Changelog");
                // JOptionPane.showMessageDialog(this,new JScrollPane(epane));
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
        } catch (final Exception exc) {
            ExceptionPopup.display(exc);
        }
    }

    private void creditsButtonActionRes(String s, String about) {
        final DefaultStyledDocument panel = new DefaultStyledDocument();
        final JTextPane epane = new JTextPane();
        epane.setForeground(Color.BLACK);
        epane.setBackground(Color.WHITE);
        final RTFEditorKit rtfk = new RTFEditorKit();
        try {
            rtfk.read(GameDataFileSystem.getDefault().getResourceAsStream(s), panel, 0);
        } catch (final BadLocationException | IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        epane.setDocument(panel);
        final JFrame frame = new JFrame(about);
        frame.setContentPane(new JScrollPane(epane));
        frame.setSize(650, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        // JOptionPane.showMessageDialog(this,new JScrollPane(epane));
    }

    private void animFromObjectActionRes() throws IOException {
        fc.setDialogTitle("Animation Source");
        final MutableGameObject fetchResult = fetchObject(this);
        if (fetchResult == null) {
            return;
        }
        final String filepath = MenuBar.convertPathToMDX(fetchResult.getFieldAsString(UnitFields.MODEL_FILE, 0));
        final EditableModel current = currentMDL();
        if (filepath != null) {
            final EditableModel animationSource = MdxUtils.loadEditable(GameDataFileSystem.getDefault().getFile(filepath));
            addSingleAnimation(this, current, animationSource);
        }
    }

    private void animFromModelActionRes() throws IOException {
        fc.setDialogTitle("Animation Source");
        final ModelElement fetchResult = fetchModel(this);
        if (fetchResult == null) {
            return;
        }
        final String filepath = MenuBar.convertPathToMDX(fetchResult.getFilepath());
        final EditableModel current = currentMDL();
        if (filepath != null) {
            final EditableModel animationSource = MdxUtils.loadEditable(GameDataFileSystem.getDefault().getFile(filepath));
            addSingleAnimation(this, current, animationSource);
        }
    }

    private void animFromUnitActionRes() throws IOException {
        fc.setDialogTitle("Animation Source");
        final GameObject fetchResult = fetchUnit(this);
        if (fetchResult == null) {
            return;
        }
        final String filepath = MenuBar.convertPathToMDX(fetchResult.getField("file"));
        final EditableModel current = currentMDL();
        if (filepath != null) {
            final EditableModel animationSource = MdxUtils.loadEditable(GameDataFileSystem.getDefault().getFile(filepath));
            addSingleAnimation(this, current, animationSource);
        }
    }

    private void animFromFileActionRes() throws IOException {
        fc.setDialogTitle("Animation Source");
        final EditableModel current = currentMDL();
        if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
            fc.setCurrentDirectory(current.getFile().getParentFile());
        } else if (profile.getPath() != null) {
            fc.setCurrentDirectory(new File(profile.getPath()));
        }
        final int returnValue = fc.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            currentFile = fc.getSelectedFile();
            profile.setPath(currentFile.getParent());
            final EditableModel animationSourceModel = MdxUtils.loadEditable(currentFile);
            addSingleAnimation(this, current, animationSourceModel);
        }

        fc.setSelectedFile(null);

        ToolBar.refreshController(geoControl, geoControlModelData);
    }

    private void riseFallBirthActionRes() {
        final ModelView disp = currentModelPanel().getModelViewManager();
        final EditableModel model = disp.getModel();
        final Animation lastAnim = model.getAnim(model.getAnimsSize() - 1);

        final Animation oldBirth = model.findAnimByName("birth");
        final Animation oldDeath = model.findAnimByName("death");

        Animation birth = new Animation("Birth", lastAnim.getEnd() + 300, lastAnim.getEnd() + 2300);
        Animation death = new Animation("Death", birth.getEnd() + 300, birth.getEnd() + 2300);
        final Animation stand = model.findAnimByName("stand");

        final int confirmed = JOptionPane.showConfirmDialog(this,
                "This will permanently alter model. Are you sure?", "Confirmation",
                JOptionPane.OK_CANCEL_OPTION);
        if (confirmed != JOptionPane.OK_OPTION) {
            return;
        }

        boolean wipeoutOldBirth = false;
        if (oldBirth != null) {
            final String[] choices = {"Ignore", "Delete", "Overwrite"};
            final Object x = JOptionPane.showInputDialog(this,
                    "Existing birth detected. What should be done with it?", "Question",
                    JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
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
            final String[] choices = {"Ignore", "Delete", "Overwrite"};
            final Object x = JOptionPane.showInputDialog(this,
                    "Existing death detected. What should be done with it?", "Question",
                    JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
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
                    final List<Integer> times = new ArrayList<>();
                    final List<Integer> values = new ArrayList<>();
                    trans = new AnimFlag("Translation", times, values);
                    trans.setInterpType(InterpolationType.LINEAR);
                    b.getAnimFlags().add(trans);
                }
                trans.addEntry(birth.getStart(), new Vec3(0, 0, -300));
                trans.addEntry(birth.getEnd(), new Vec3(0, 0, 0));
                trans.addEntry(death.getStart(), new Vec3(0, 0, 0));
                trans.addEntry(death.getEnd(), new Vec3(0, 0, -300));
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
            af.setEntry(death.getEnd(), 0);
        }

        if (!birth.isNonLooping()) {
            birth.setNonLooping(true);
        }
        if (!death.isNonLooping()) {
            death.setNonLooping(true);
        }

        if (!model.contains(birth)) {
            model.add(birth);
        }
        if (!model.contains(death)) {
            model.add(death);
        }

        JOptionPane.showMessageDialog(this, "Done!");
    }

    private void simplifyKeyframesActionRes() {
        final int x = JOptionPane.showConfirmDialog(this,
                "This is an irreversible process that will lose some of your model data,\nin exchange for making it a smaller storage size.\n\nContinue and simplify keyframes?",
                "Warning: Simplify Keyframes", JOptionPane.OK_CANCEL_OPTION);
        if (x == JOptionPane.OK_OPTION) {
            simplifyKeyframes(this);
        }
    }

    private void duplicateSelectionActionRes() {
        // final int x = JOptionPane.showConfirmDialog(this,
        // "This is an irreversible process that will split selected
        // vertices into many copies of themself, one for each face, so
        // you can wrap textures and normals in a different
        // way.\n\nContinue?",
        // "Warning"/* : Divide Vertices" */,
        // JOptionPane.OK_CANCEL_OPTION);
        // if (x == JOptionPane.OK_OPTION) {
        final ModelPanel currentModelPanel = currentModelPanel();
        if (currentModelPanel != null) {
            currentModelPanel.getUndoManager().pushAction(currentModelPanel.getModelEditorManager()
                    .getModelEditor().cloneSelectedComponents(namePicker));
        }
        // }
    }

    private void linearizeAnimationsActionRes() {
        final int x = JOptionPane.showConfirmDialog(this,
                "This is an irreversible process that will lose some of your model data,\nin exchange for making it a smaller storage size.\n\nContinue and simplify animations?",
                "Warning: Linearize Animations", JOptionPane.OK_CANCEL_OPTION);
        if (x == JOptionPane.OK_OPTION) {
            final List<AnimFlag> allAnimFlags = currentMDL().getAllAnimFlags();
            for (final AnimFlag flag : allAnimFlags) {
                flag.linearize();
            }
        }
    }

    private void scaleAnimationsActionRes() {
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
        final ChangeAnimationLengthFrame aFrame = new ChangeAnimationLengthFrame(currentModelPanel(), () -> timeSliderPanel.revalidateKeyframeDisplay());
        aFrame.setVisible(true);
    }

    private void exportTexturesActionRes() {
        final DefaultListModel<Material> materials = new DefaultListModel<>();
        for (int i = 0; i < currentMDL().getMaterials().size(); i++) {
            final Material mat = currentMDL().getMaterials().get(i);
            materials.addElement(mat);
        }
        for (final ParticleEmitter2 emitter2 : currentMDL().sortedIdObjects(ParticleEmitter2.class)) {
            final Material dummyMaterial = new Material(
                    new Layer("Blend", currentMDL().getTexture(emitter2.getTextureID())));
        }

        final JList<Material> materialsList = new JList<>(materials);
        materialsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        materialsList.setCellRenderer(new MaterialListRenderer(currentMDL()));
        JOptionPane.showMessageDialog(this, new JScrollPane(materialsList));

        if (exportTextureDialog.getCurrentDirectory() == null) {
            final EditableModel current = currentMDL();
            if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                fc.setCurrentDirectory(current.getFile().getParentFile());
            } else if (profile.getPath() != null) {
                fc.setCurrentDirectory(new File(profile.getPath()));
            }
        }
        if (exportTextureDialog.getCurrentDirectory() == null) {
            exportTextureDialog.setSelectedFile(new File(exportTextureDialog.getCurrentDirectory()
                    + File.separator + materialsList.getSelectedValue().getName()));
        }

        final int x = exportTextureDialog.showSaveDialog(this);
        if (x == JFileChooser.APPROVE_OPTION) {
            final File file = exportTextureDialog.getSelectedFile();
            if (file != null) {
                try {
                    if (file.getName().lastIndexOf('.') >= 0) {
                        BufferedImage bufferedImage = materialsList.getSelectedValue()
                                .getBufferedImage(currentMDL().getWrappedDataSource());
                        String fileExtension = file.getName().substring(file.getName().lastIndexOf('.') + 1)
                                .toUpperCase();
                        if (fileExtension.equals("BMP") || fileExtension.equals("JPG")
                                || fileExtension.equals("JPEG")) {
                            JOptionPane.showMessageDialog(this,
                                    "Warning: Alpha channel was converted to black. Some data will be lost\nif you convert this texture back to Warcraft BLP.");
                            bufferedImage = BLPHandler.removeAlphaChannel(bufferedImage);
                        }
                        if (fileExtension.equals("BLP")) {
                            fileExtension = "blp";
                        }
                        final boolean write = ImageIO.write(bufferedImage, fileExtension, file);
                        if (!write) {
                            JOptionPane.showMessageDialog(this, "File type unknown or unavailable");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "No file type was specified");
                    }
                } catch (final Exception e1) {
                    ExceptionPopup.display(e1);
                    e1.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "No output file was specified");
            }
        }
    }

    private void editUVsActionRes() {
        final ModelPanel disp = currentModelPanel();
        if (disp.getEditUVPanel() == null) {
            final UVPanel panel = new UVPanel(disp, prefs, modelStructureChangeListener);
            disp.setEditUVPanel(panel);

            panel.initViewport();
            final FloatingWindow floatingWindow = rootWindow.createFloatingWindow(
                    new Point(getX() + (getWidth() / 2), getY() + (getHeight() / 2)), panel.getSize(),
                    panel.getView());
            panel.init();
            floatingWindow.getTopLevelAncestor().setVisible(true);
            panel.packFrame();
        } else if (!disp.getEditUVPanel().frameVisible()) {
            final FloatingWindow floatingWindow = rootWindow.createFloatingWindow(
                    new Point(getX() + (getWidth() / 2), getY() + (getHeight() / 2)),
                    disp.getEditUVPanel().getSize(), disp.getEditUVPanel().getView());
            floatingWindow.getTopLevelAncestor().setVisible(true);
        }
    }

    private void showVertexModifyControlsActionRes() {
        final boolean selected = showVertexModifyControls.isSelected();
        prefs.setShowVertexModifierControls(selected);
        // SaveProfile.get().setShowViewportButtons(selected);
        for (final ModelPanel panel : modelPanels) {
            panel.getFrontArea().setControlsVisible(selected);
            panel.getBotArea().setControlsVisible(selected);
            panel.getSideArea().setControlsVisible(selected);
            final UVPanel uvPanel = panel.getEditUVPanel();
            if (uvPanel != null) {
                uvPanel.setControlsVisible(selected);
            }
        }
    }

    private void nullmodelButtonActionRes() {
        nullmodelFile(this);
        ToolBar.refreshController(geoControl, geoControlModelData);
    }

    private void clearRecentActionRes() {
        final int dialogResult = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear the Recent history?", "Confirm Clear",
                JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            SaveProfile.get().clearRecent();
            MenuBar.updateRecent(this);
        }
    }

    private void mergeGeosetActionRes() throws IOException {
        fc.setDialogTitle("Merge Single Geoset (Oinker-based)");
        final EditableModel current = currentMDL();
        if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
            fc.setCurrentDirectory(current.getFile().getParentFile());
        } else if (profile.getPath() != null) {
            fc.setCurrentDirectory(new File(profile.getPath()));
        }
        final int returnValue = fc.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            currentFile = fc.getSelectedFile();
            final EditableModel geoSource = MdxUtils.loadEditable(currentFile);
            profile.setPath(currentFile.getParent());
            boolean going = true;
            Geoset host = null;
            while (going) {
                final String s = JOptionPane.showInputDialog(this,
                        "Geoset into which to Import: (1 to " + current.getGeosetsSize() + ")");
                try {
                    final int x = Integer.parseInt(s);
                    if ((x >= 1) && (x <= current.getGeosetsSize())) {
                        host = current.getGeoset(x - 1);
                        going = false;
                    }
                } catch (final NumberFormatException ignored) {

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
                } catch (final NumberFormatException ignored) {

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
    }

    private void importButtonSActionRes() {
        final JFrame frame = new JFrame("Animation Transferer");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(new AnimationTransfer(frame));
        frame.setIconImage(MainPanel.AnimIcon.getImage());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void importFromWorkspaceActionRes() {
        final List<EditableModel> optionNames = new ArrayList<>();
        for (final ModelPanel modelPanel : modelPanels) {
            final EditableModel model = modelPanel.getModel();
            optionNames.add(model);
        }
        final EditableModel choice = (EditableModel) JOptionPane.showInputDialog(this,
                "Choose a workspace item to import data from:", "Import from Workspace",
                JOptionPane.OK_CANCEL_OPTION, null, optionNames.toArray(), optionNames.get(0));
        if (choice != null) {
            importFile(this, EditableModel.deepClone(choice, choice.getHeaderName()));
        }
        ToolBar.refreshController(geoControl, geoControlModelData);
    }

    private void importGameObjectActionRes() throws IOException {
        final MutableGameObject fetchObjectResult = fetchObject(this);
        if (fetchObjectResult == null) {
            return;
        }
        final String filepath = MenuBar.convertPathToMDX(fetchObjectResult.getFieldAsString(UnitFields.MODEL_FILE, 0));
        final EditableModel current = currentMDL();
        if (filepath != null) {
            final File animationSource = GameDataFileSystem.getDefault().getFile(filepath);
            importFile(this, animationSource);
        }
        ToolBar.refreshController(geoControl, geoControlModelData);
    }

    private void importGameModelActionRes() throws IOException {
        final ModelElement fetchModelResult = fetchModel(this);
        if (fetchModelResult == null) {
            return;
        }
        final String filepath = MenuBar.convertPathToMDX(fetchModelResult.getFilepath());
        final EditableModel current = currentMDL();
        if (filepath != null) {
            final File animationSource = GameDataFileSystem.getDefault().getFile(filepath);
            importFile(this, animationSource);
        }
        ToolBar.refreshController(geoControl, geoControlModelData);
    }

    private void importUnitActionRes() throws IOException {
        final GameObject fetchUnitResult = fetchUnit(this);
        if (fetchUnitResult == null) {
            return;
        }
        final String filepath = MenuBar.convertPathToMDX(fetchUnitResult.getField("file"));
        final EditableModel current = currentMDL();
        if (filepath != null) {
            final File animationSource = GameDataFileSystem.getDefault().getFile(filepath);
            importFile(this, animationSource);
        }
        ToolBar.refreshController(geoControl, geoControlModelData);
    }

    private void importButtonActionRes() throws IOException {
        fc.setDialogTitle("Import");
        final EditableModel current = currentMDL();
        if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
            fc.setCurrentDirectory(current.getFile().getParentFile());
        } else if (profile.getPath() != null) {
            fc.setCurrentDirectory(new File(profile.getPath()));
        }
        final int returnValue = fc.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            currentFile = fc.getSelectedFile();
            profile.setPath(currentFile.getParent());
            toolsMenu.getAccessibleContext().setAccessibleDescription(
                    "Allows the user to control which parts of the model are displayed for editing.");
            toolsMenu.setEnabled(true);
            importFile(this, currentFile);
        }

        fc.setSelectedFile(null);

        // //Special thanks to the JWSFileChooserDemo from oracle's Java
        // tutorials, from which many ideas were borrowed for the
        // following
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
        ToolBar.refreshController(geoControl, geoControlModelData);
    }

    private void fetchObjectActionRes() {
        final MutableGameObject objectFetched = fetchObject(this);
        if (objectFetched != null) {
            final String filepath = MenuBar.convertPathToMDX(objectFetched.getFieldAsString(UnitFields.MODEL_FILE, 0));
            if (filepath != null) {
                MenuBar.loadStreamMdx(this, GameDataFileSystem.getDefault().getResourceAsStream(filepath), true, true,
                        new ImageIcon(BLPHandler.get()
                                .getGameTex(objectFetched.getFieldAsString(UnitFields.INTERFACE_ICON, 0))
                                .getScaledInstance(16, 16, Image.SCALE_FAST)));
                final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
                        + filepath.substring(filepath.lastIndexOf('.'));
                if (prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
                    MenuBar.loadStreamMdx(this, GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false,
                            new ImageIcon(BLPHandler.get()
                                    .getGameTex(objectFetched.getFieldAsString(UnitFields.INTERFACE_ICON, 0))
                                    .getScaledInstance(16, 16, Image.SCALE_FAST)));
                }
                toolsMenu.getAccessibleContext().setAccessibleDescription(
                        "Allows the user to control which parts of the model are displayed for editing.");
                toolsMenu.setEnabled(true);
            }
        }
    }

    private void fetchModelActionRes() {
        final ModelElement model = fetchModel(this);
        if (model != null) {
            final String filepath = MenuBar.convertPathToMDX(model.getFilepath());
            if (filepath != null) {

                final ImageIcon icon = model.hasCachedIconPath() ? new ImageIcon(BLPHandler.get()
                        .getGameTex(model.getCachedIconPath()).getScaledInstance(16, 16, Image.SCALE_FAST))
                        : MenuBar.MDLIcon;
                MenuBar.loadStreamMdx(this, GameDataFileSystem.getDefault().getResourceAsStream(filepath), true, true, icon);
                final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
                        + filepath.substring(filepath.lastIndexOf('.'));
                if (prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
                    MenuBar.loadStreamMdx(this, GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false, icon);
                }
                toolsMenu.getAccessibleContext().setAccessibleDescription(
                        "Allows the user to control which parts of the model are displayed for editing.");
                toolsMenu.setEnabled(true);
            }
        }
    }

    private void fetchUnitActionRes() {
        final GameObject unitFetched = fetchUnit(this);
        if (unitFetched != null) {
            final String filepath = MenuBar.convertPathToMDX(unitFetched.getField("file"));
            if (filepath != null) {
                MenuBar.loadStreamMdx(this, GameDataFileSystem.getDefault().getResourceAsStream(filepath), true, true,
                        unitFetched.getScaledIcon(0.25f));
                final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
                        + filepath.substring(filepath.lastIndexOf('.'));
                if (prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
                    MenuBar.loadStreamMdx(this, GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false,
                            unitFetched.getScaledIcon(0.25f));
                }
                toolsMenu.getAccessibleContext().setAccessibleDescription(
                        "Allows the user to control which parts of the model are displayed for editing.");
                toolsMenu.setEnabled(true);
            }
        }
    }

    private void closePanelActionRes() {
        final ModelPanel modelPanel = currentModelPanel();
        final int oldIndex = modelPanels.indexOf(modelPanel);
        if (modelPanel != null) {
            if (modelPanel.close(this)) {
                modelPanels.remove(modelPanel);
                windowMenu.remove(modelPanel.getMenuItem());
                if (modelPanels.size() > 0) {
                    final int newIndex = Math.min(modelPanels.size() - 1, oldIndex);
                    MenuBar.setCurrentModel(this, modelPanels.get(newIndex));
                } else {
                    // TODO remove from notifiers to fix leaks
                    MenuBar.setCurrentModel(this, null);
                }
            }
        }
    }

    private static void simplifyKeyframes(MainPanel mainPanel) {
        final EditableModel currentMDL = mainPanel.currentMDL();
        currentMDL.simplifyKeyframes();
    }

    private static void onClickSaveAs(MainPanel mainPanel) {
        final EditableModel current = mainPanel.currentMDL();
        MainPanel.onClickSaveAs(mainPanel, current);
    }

    private static boolean onClickSaveAs(MainPanel mainPanel, final EditableModel current) {
        try {
            mainPanel.fc.setDialogTitle("Save as");
            if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
                mainPanel.fc.setSelectedFile(current.getFile());
            } else if (mainPanel.profile.getPath() != null) {
                mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
            }
            final int returnValue = mainPanel.fc.showSaveDialog(mainPanel);
            File temp = mainPanel.fc.getSelectedFile();
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                if (temp != null) {
                    final FileFilter ff = mainPanel.fc.getFileFilter();
                    final String ext = ff.accept(new File("junk.mdl")) ? ".mdl" : ".mdx";
                    if (ff.accept(new File("junk.obj"))) {
                        throw new UnsupportedOperationException("OBJ saving has not been coded yet.");
                    }
                    final String name = temp.getName();
                    if (name.lastIndexOf('.') != -1) {
                        if (!name.substring(name.lastIndexOf('.')).equals(ext)) {
                            temp = new File(
                                    temp.getAbsolutePath().substring(0, temp.getAbsolutePath().lastIndexOf('.')) + ext);
                        }
                    } else {
                        temp = new File(temp.getAbsolutePath() + ext);
                    }
                    mainPanel.currentFile = temp;
                    if (temp.exists()) {
                        final Object[] options = {"Overwrite", "Cancel"};
                        final int n = JOptionPane.showOptionDialog(MainFrame.frame, "Selected file already exists.",
                                "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
                                options[1]);
                        if (n == 1) {
                            mainPanel.fc.setSelectedFile(null);
                            return false;
                        }
                    }
                    mainPanel.profile.setPath(mainPanel.currentFile.getParent());

                    final MdlxModel mdlx = mainPanel.currentMDL().toMdlx();
                    final FileOutputStream stream = new FileOutputStream(mainPanel.currentFile);

                    if (ext.equals(".mdl")) {
                        MdxUtils.saveMdl(mdlx, stream);
                    } else {
                        MdxUtils.saveMdx(mdlx, stream);
                    }
                    mainPanel.currentMDL().setFileRef(mainPanel.currentFile);
                    // currentMDLDisp().resetBeenSaved();
                    // TODO reset been saved
                    mainPanel.currentModelPanel().getMenuItem().setName(mainPanel.currentFile.getName().split("\\.")[0]);
                    mainPanel.currentModelPanel().getMenuItem().setToolTipText(mainPanel.currentFile.getPath());
                } else {
                    JOptionPane.showMessageDialog(mainPanel,
                            "You tried to save, but you somehow didn't select a file.\nThat is bad.");
                }
            }
            mainPanel.fc.setSelectedFile(null);
            return true;
        } catch (final Exception exc) {
            ExceptionPopup.display(exc);
        }
        ToolBar.refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
        return false;
    }

    private static GameObject fetchUnit(MainPanel mainPanel) {
        final GameObject choice = UnitOptionPane.show(mainPanel);
        if (choice != null) {

        } else {
            return null;
        }

        String filepath = choice.getField("file");

        try {
            filepath = MenuBar.convertPathToMDX(filepath);
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

    private static ModelOptionPane.ModelElement fetchModel(MainPanel mainPanel) {
        final ModelOptionPane.ModelElement model = ModelOptionPane.showAndLogIcon(mainPanel);
        if (model == null) {
            return null;
        }
        String filepath = model.getFilepath();
        if (filepath != null) {

        } else {
            return null;
        }
        try {
            filepath = MenuBar.convertPathToMDX(filepath);
        } catch (final Exception exc) {
            exc.printStackTrace();
            // bad model!
            JOptionPane.showMessageDialog(MainFrame.frame, "The chosen model could not be used.", "Program Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return model;
    }

    private static MutableGameObject fetchObject(MainPanel mainPanel) {
        final BetterUnitEditorModelSelector selector = new BetterUnitEditorModelSelector(MenuBar.getUnitData(),
                MenuBar.getUnitEditorSettings());
        final int x = JOptionPane.showConfirmDialog(mainPanel, selector, "Object Editor - Select Unit",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        final MutableGameObject choice = selector.getSelection();
        if ((choice == null) || (x != JOptionPane.OK_OPTION)) {
            return null;
        }

        String filepath = choice.getFieldAsString(UnitFields.MODEL_FILE, 0);

        try {
            filepath = MenuBar.convertPathToMDX(filepath);
        } catch (final Exception exc) {
            exc.printStackTrace();
            // bad model!
            JOptionPane.showMessageDialog(MainFrame.frame, "The chosen model could not be used.", "Program Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return choice;
    }

    private static void addSingleAnimation(MainPanel mainPanel, final EditableModel current, final EditableModel animationSourceModel) {
        Animation choice = null;
        choice = (Animation) JOptionPane.showInputDialog(mainPanel, "Choose an animation!", "Add Animation",
                JOptionPane.QUESTION_MESSAGE, null, animationSourceModel.getAnims().toArray(),
                animationSourceModel.getAnims().get(0));
        if (choice == null) {
            JOptionPane.showMessageDialog(mainPanel, "Bad choice. No animation added.");
            return;
        }
        final Animation visibilitySource = (Animation) JOptionPane.showInputDialog(mainPanel,
                "Which animation from THIS model to copy visiblity from?", "Add Animation",
                JOptionPane.QUESTION_MESSAGE, null, current.getAnims().toArray(), current.getAnims().get(0));
        if (visibilitySource == null) {
            JOptionPane.showMessageDialog(mainPanel, "No visibility will be copied.");
        }
        final List<Animation> animationsAdded = current.addAnimationsFrom(animationSourceModel,
                Collections.singletonList(choice));
        for (final Animation anim : animationsAdded) {
            current.copyVisibility(visibilitySource, anim);
        }
        JOptionPane.showMessageDialog(mainPanel, "Added " + animationSourceModel.getName() + "'s " + choice.getName()
                + " with " + visibilitySource.getName() + "'s visibility  OK!");
        mainPanel.modelStructureChangeListener.animationsAdded(animationsAdded);
    }

    private interface ModelReference {
        EditableModel getModel();
    }

    private static class ModelStructureChangeListenerImplementation implements ModelStructureChangeListener {
        private final ModelReference modelReference;
        private MainPanel mainPanel;

        public ModelStructureChangeListenerImplementation(MainPanel mainPanel, final ModelReference modelReference) {
            this.modelReference = modelReference;
            this.mainPanel = mainPanel;
        }

        public ModelStructureChangeListenerImplementation(MainPanel mainPanel, final EditableModel model) {
            modelReference = () -> model;
            this.mainPanel = mainPanel;
        }

        @Override
        public void nodesRemoved(final List<IdObject> nodes) {
            // Tell program to set visibility after import
            final ModelPanel display = MainPanel.displayFor(mainPanel.modelPanels, modelReference.getModel());
            if (display != null) {
                // display.setBeenSaved(false); // we edited the model
                // TODO notify been saved system, wherever that moves to
                for (final IdObject geoset : nodes) {
                    display.getModelViewManager().makeIdObjectNotVisible(geoset);
                }
                MainPanel.reloadGeosetManagers(mainPanel, display);
            }
        }

        @Override
        public void nodesAdded(final List<IdObject> nodes) {
            // Tell program to set visibility after import
            final ModelPanel display = MainPanel.displayFor(mainPanel.modelPanels, modelReference.getModel());
            if (display != null) {
                // display.setBeenSaved(false); // we edited the model
                // TODO notify been saved system, wherever that moves to
                for (final IdObject geoset : nodes) {
                    display.getModelViewManager().makeIdObjectVisible(geoset);
                }
                MainPanel.reloadGeosetManagers(mainPanel, display);
                display.getEditorRenderModel().refreshFromEditor(mainPanel.animatedRenderEnvironment, MenuBar.IDENTITY,
                        MenuBar.IDENTITY, MenuBar.IDENTITY, display.getPerspArea().getViewport());
                display.getAnimationViewer().reload();
            }
        }

        @Override
        public void geosetsRemoved(final List<Geoset> geosets) {
            // Tell program to set visibility after import
            final ModelPanel display = MainPanel.displayFor(mainPanel.modelPanels, modelReference.getModel());
            if (display != null) {
                // display.setBeenSaved(false); // we edited the model
                // TODO notify been saved system, wherever that moves to
                for (final Geoset geoset : geosets) {
                    display.getModelViewManager().makeGeosetNotEditable(geoset);
                    display.getModelViewManager().makeGeosetNotVisible(geoset);
                }
                MainPanel.reloadGeosetManagers(mainPanel, display);
            }
        }

        @Override
        public void geosetsAdded(final List<Geoset> geosets) {
            // Tell program to set visibility after import
            final ModelPanel display = MainPanel.displayFor(mainPanel.modelPanels, modelReference.getModel());
            if (display != null) {
                // display.setBeenSaved(false); // we edited the model
                // TODO notify been saved system, wherever that moves to
                for (final Geoset geoset : geosets) {
                    display.getModelViewManager().makeGeosetEditable(geoset);
                    // display.getModelViewManager().makeGeosetVisible(geoset);
                }
                MainPanel.reloadGeosetManagers(mainPanel, display);
            }
        }

        @Override
        public void camerasAdded(final List<Camera> cameras) {
            // Tell program to set visibility after import
            final ModelPanel display = MainPanel.displayFor(mainPanel.modelPanels, modelReference.getModel());
            if (display != null) {
                // display.setBeenSaved(false); // we edited the model
                // TODO notify been saved system, wherever that moves to
                for (final Camera camera : cameras) {
                    display.getModelViewManager().makeCameraVisible(camera);
                    // display.getModelViewManager().makeGeosetVisible(geoset);
                }
                MainPanel.reloadGeosetManagers(mainPanel, display);
            }
        }

        @Override
        public void camerasRemoved(final List<Camera> cameras) {
            // Tell program to set visibility after import
            final ModelPanel display = MainPanel.displayFor(mainPanel.modelPanels, modelReference.getModel());
            if (display != null) {
                // display.setBeenSaved(false); // we edited the model
                // TODO notify been saved system, wherever that moves to
                for (final Camera camera : cameras) {
                    display.getModelViewManager().makeCameraNotVisible(camera);
                    // display.getModelViewManager().makeGeosetVisible(geoset);
                }
                MainPanel.reloadGeosetManagers(mainPanel, display);
            }
        }

        @Override
        public void timelineAdded(final TimelineContainer node, final AnimFlag timeline) {

        }

        @Override
        public void keyframeAdded(final TimelineContainer node, final AnimFlag timeline, final int trackTime) {
            mainPanel.timeSliderPanel.revalidateKeyframeDisplay();
        }

        @Override
        public void timelineRemoved(final TimelineContainer node, final AnimFlag timeline) {

        }

        @Override
        public void keyframeRemoved(final TimelineContainer node, final AnimFlag timeline, final int trackTime) {
            mainPanel.timeSliderPanel.revalidateKeyframeDisplay();
        }

        @Override
        public void animationsAdded(final List<Animation> animation) {
            mainPanel.currentModelPanel().getAnimationViewer().reload();
            mainPanel.currentModelPanel().getAnimationController().reload();
            mainPanel.creatorPanel.reloadAnimationList();
            final ModelPanel display = MainPanel.displayFor(mainPanel.modelPanels, modelReference.getModel());
            if (display != null) {
                MenuBar.reloadComponentBrowser(mainPanel.geoControlModelData, display);
            }
        }

        @Override
        public void animationsRemoved(final List<Animation> animation) {
            mainPanel.currentModelPanel().getAnimationViewer().reload();
            mainPanel.currentModelPanel().getAnimationController().reload();
            mainPanel.creatorPanel.reloadAnimationList();
            final ModelPanel display = MainPanel.displayFor(mainPanel.modelPanels, modelReference.getModel());
            if (display != null) {
                MenuBar.reloadComponentBrowser(mainPanel.geoControlModelData, display);
            }
        }

        @Override
        public void texturesChanged() {
            final ModelPanel modelPanel = mainPanel.currentModelPanel();
            if (modelPanel != null) {
                modelPanel.getAnimationViewer().reloadAllTextures();
                modelPanel.getPerspArea().reloadAllTextures();
            }
            final ModelPanel display = MainPanel.displayFor(mainPanel.modelPanels, modelReference.getModel());
            if (display != null) {
                MenuBar.reloadComponentBrowser(mainPanel.geoControlModelData, display);
            }
        }

        @Override
        public void headerChanged() {
            final ModelPanel display = MainPanel.displayFor(mainPanel.modelPanels, modelReference.getModel());
            if (display != null) {
                MenuBar.reloadComponentBrowser(mainPanel.geoControlModelData, display);
            }
        }

        @Override
        public void animationParamsChanged(final Animation animation) {
            mainPanel.currentModelPanel().getAnimationViewer().reload();
            mainPanel.currentModelPanel().getAnimationController().reload();
            mainPanel.creatorPanel.reloadAnimationList();
            final ModelPanel display = MainPanel.displayFor(mainPanel.modelPanels, modelReference.getModel());
            if (display != null) {
                MenuBar.reloadComponentBrowser(mainPanel.geoControlModelData, display);
            }
        }

        @Override
        public void globalSequenceLengthChanged(final int index, final Integer newLength) {
            mainPanel.currentModelPanel().getAnimationViewer().reload();
            mainPanel.currentModelPanel().getAnimationController().reload();
            mainPanel.creatorPanel.reloadAnimationList();
            final ModelPanel display = MainPanel.displayFor(mainPanel.modelPanels, modelReference.getModel());
            if (display != null) {
                MenuBar.reloadComponentBrowser(mainPanel.geoControlModelData, display);
            }
        }
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

    /**
     * Returns the MDLDisplay associated with a given MDL, or null if one cannot be
     * found.
     */
    public static ModelPanel displayFor(List<ModelPanel> modelPanels, final EditableModel model) {
        ModelPanel output = null;
        ModelView tempDisplay;
        for (final ModelPanel modelPanel : modelPanels) {
            tempDisplay = modelPanel.getModelViewManager();
            if (tempDisplay.getModel() == model) {
                output = modelPanel;
                break;
            }
        }
        return output;
    }

    public static void importFile(MainPanel mainPanel, final File f) throws IOException {
        final EditableModel currentModel = mainPanel.currentMDL();
        if (currentModel != null) {
            MainPanel.importFile(mainPanel, MdxUtils.loadEditable(f));
        }
    }

    public static void importFile(final MainPanel mainPanel, final EditableModel model) {
        final EditableModel currentModel = mainPanel.currentMDL();
        if (currentModel != null) {
            mainPanel.importPanel = new ImportPanel(currentModel, model);
            mainPanel.importPanel.setCallback(new ModelStructureChangeListenerImplementation(mainPanel, new ModelReference() {
                private final EditableModel model = mainPanel.currentMDL();

                @Override
                public EditableModel getModel() {
                    return model;
                }
            }));

        }
    }

    public static String incName(final String name) {
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
            for (char n = '0'; (n < '9') && continueLoop; n++) {
                // JOptionPane.showMessageDialog(null,"checking "+c+" against
                // "+n);
                if (c == n) {
                    char x = c;
                    x++;
                    output = output.substring(0, output.length() - depth) + x
                            + output.substring((output.length() - depth) + 1);
                    continueLoop = false;
                }
            }
            if (c == '9') {
                output = output.substring(0, output.length() - depth) + 0
                        + output.substring((output.length() - depth) + 1);
            } else if (continueLoop) {
                output = output.substring(0, (output.length() - depth) + 1) + 1
                        + output.substring((output.length() - depth) + 1);
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

    public static void nullmodelFile(MainPanel mainPanel) {
        final EditableModel currentMDL = mainPanel.currentMDL();
        if (currentMDL != null) {
            final EditableModel newModel = new EditableModel();
            newModel.copyHeaders(currentMDL);
            if (newModel.getFileRef() == null) {
                newModel.setFileRef(
                        new File(System.getProperty("java.io.tmpdir") + "MatrixEaterExtract/matrixeater_anonymousMDL",
                                "" + (int) (Math.random() * Integer.MAX_VALUE) + ".mdl"));
            }
            while (newModel.getFile().exists()) {
                newModel.setFileRef(
                        new File(currentMDL.getFile().getParent() + "/" + incName(newModel.getName()) + ".mdl"));
            }
            mainPanel.importPanel = new ImportPanel(newModel, EditableModel.deepClone(currentMDL, "CurrentModel"));

            final Thread watcher = new Thread(() -> {
                while (mainPanel.importPanel.getParentFrame().isVisible()
                        && (!mainPanel.importPanel.importStarted()
                        || mainPanel.importPanel.importEnded())) {
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

                if (mainPanel.importPanel.importStarted()) {
                    while (!mainPanel.importPanel.importEnded()) {
                        try {
                            Thread.sleep(1);
                        } catch (final Exception e) {
                            ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
                        }
                    }

                    if (mainPanel.importPanel.importSuccessful()) {
                        try {
                            MdxUtils.saveMdx(newModel, newModel.getFile());
                        } catch (final IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        MenuBar.loadFile(mainPanel, newModel.getFile());
                    }
                }
            });
            watcher.start();
        }
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

    public static boolean closeOthers(MainPanel mainPanel, final ModelPanel panelToKeepOpen) {
        boolean success = true;
        final Iterator<ModelPanel> iterator = mainPanel.modelPanels.iterator();
        boolean closedCurrentPanel = false;
        ModelPanel lastUnclosedModelPanel = null;
        while (iterator.hasNext()) {
            final ModelPanel panel = iterator.next();
            if (panel == panelToKeepOpen) {
                lastUnclosedModelPanel = panel;
                continue;
            }
            if (success = panel.close(mainPanel)) {
                mainPanel.windowMenu.remove(panel.getMenuItem());
                iterator.remove();
                if (panel == mainPanel.currentModelPanel) {
                    closedCurrentPanel = true;
                }
            } else {
                lastUnclosedModelPanel = panel;
                break;
            }
        }
        if (closedCurrentPanel) {
            MenuBar.setCurrentModel(mainPanel, lastUnclosedModelPanel);
        }
        return success;
    }

    public static void repaintSelfAndChildren(MainPanel mainPanel) {
        mainPanel.repaint();
        mainPanel.geoControl.repaint();
        mainPanel.geoControlModelData.repaint();
    }

    final TextureExporterImpl textureExporter = new TextureExporterImpl(this);

    public static class TextureExporterImpl implements TextureExporter {
        private MainPanel mainPanel;

        public TextureExporterImpl(MainPanel mainPanel) {
            this.mainPanel = mainPanel;
        }

        public JFileChooser getFileChooser() {
            return mainPanel.exportTextureDialog;
        }

        @Override
        public void showOpenDialog(final String suggestedName, final TextureExporterClickListener fileHandler,
                                   final Component parent) {
            if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
                final EditableModel current = mainPanel.currentMDL();
                if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                    mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
                } else if (mainPanel.profile.getPath() != null) {
                    mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
                }
            }
            if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
                mainPanel.exportTextureDialog.setSelectedFile(new File(
                        mainPanel.exportTextureDialog.getCurrentDirectory() + File.separator + suggestedName));
            }
            final int showOpenDialog = mainPanel.exportTextureDialog.showOpenDialog(parent);
            if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
                final File file = mainPanel.exportTextureDialog.getSelectedFile();
                if (file != null) {
                    fileHandler.onClickOK(file, mainPanel.exportTextureDialog.getFileFilter());
                } else {
                    JOptionPane.showMessageDialog(parent, "No import file was specified");
                }
            }
        }

        @Override
        public void exportTexture(final String suggestedName, final TextureExporterClickListener fileHandler,
                                  final Component parent) {

            if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
                final EditableModel current = mainPanel.currentMDL();
                if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                    mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
                } else if (mainPanel.profile.getPath() != null) {
                    mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
                }
            }
            if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
                mainPanel.exportTextureDialog.setSelectedFile(new File(
                        mainPanel.exportTextureDialog.getCurrentDirectory() + File.separator + suggestedName));
            }

            final int x = mainPanel.exportTextureDialog.showSaveDialog(parent);
            if (x == JFileChooser.APPROVE_OPTION) {
                final File file = mainPanel.exportTextureDialog.getSelectedFile();
                if (file != null) {
                    try {
                        if (file.getName().lastIndexOf('.') >= 0) {
                            fileHandler.onClickOK(file, mainPanel.exportTextureDialog.getFileFilter());
                        } else {
                            JOptionPane.showMessageDialog(parent, "No file type was specified");
                        }
                    } catch (final Exception e2) {
                        ExceptionPopup.display(e2);
                        e2.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(parent, "No output file was specified");
                }
            }
        }

    }

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
            onClickSaveAs(this, model);
        }
    }

    private static Component getFocusedComponent() {
        final KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        final Component focusedComponent = kfm.getFocusOwner();
        return focusedComponent;
    }

    private static boolean focusedComponentNeedsTyping(final Component focusedComponent) {
        return (focusedComponent instanceof JTextArea) || (focusedComponent instanceof JTextField);
    }
}
