package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.util.ModelUtils.Mesh;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewStateListener;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.parsers.w3o.WTSFile;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
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
import com.hiveworkshop.rms.ui.application.tools.EditTexturesPopupPanel;
import com.hiveworkshop.rms.ui.application.viewer.AnimationViewer;
import com.hiveworkshop.rms.ui.application.viewer.perspective.PerspDisplayPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.models.BetterUnitEditorModelSelector;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.*;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.UnitFields;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPane;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPane.ModelElement;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
import com.hiveworkshop.rms.ui.browsers.mpq.MPQBrowser;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPane;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.*;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.CreatorModelingPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model.*;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarActionButtonType;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TextureExporter;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TransferActionListener;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.ui.util.ModeButton;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.*;
import de.wc3data.stream.BlizzardDataInputStream;
import jassimp.AiPostProcessSteps;
import jassimp.AiScene;
import jassimp.Jassimp;
import net.infonode.docking.*;
import net.infonode.docking.util.StringViewMap;
import net.infonode.tabbedpanel.TabAreaVisiblePolicy;
import net.infonode.tabbedpanel.titledtab.TitledTabBorderSizePolicy;
import net.infonode.tabbedpanel.titledtab.TitledTabSizePolicy;
import net.miginfocom.swing.MigLayout;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Queue;
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
    List<RecentItem> recentItems = new ArrayList<>();
    UndoMenuItem undo;
    RedoMenuItem redo;

    JMenu viewMode;
    JRadioButtonMenuItem wireframe, solid;
    ButtonGroup viewModes;

    JFileChooser fc, exportTextureDialog;
    File currentFile;
    ImportPanel importPanel;
    static final ImageIcon MDLIcon = RMSIcons.MDLIcon;
    static final ImageIcon POWERED_BY_HIVE = RMSIcons.loadHiveBrowserImageIcon("powered_by_hive.png");
    public static final ImageIcon AnimIcon = RMSIcons.AnimIcon;
    protected static final boolean OLDMODE = false;
    boolean loading;
    List<ModelPanel> modelPanels;
    ModelPanel currentModelPanel;
    View frontView, leftView, bottomView, perspectiveView;
    private final View timeSliderView;
    private final View hackerView;
    private final View previewView;
    private final View creatorView;
    private final View animationControllerView;
    JScrollPane geoControl;
    JScrollPane geoControlModelData;
    JTextField[] mouseCoordDisplay = new JTextField[3];
    boolean cheatShift = false;
    boolean cheatAlt = false;
    SaveProfile profile = SaveProfile.get();
    ProgramPreferences prefs = profile.getPreferences();

    JToolBar toolbar;

    TimeSliderPanel timeSliderPanel;
    private final JButton setKeyframe;
    private final JButton setTimeBounds;
    private final ModeButton animationModeButton;
    private boolean animationModeState = false;
    private final ZoomableImagePreviewPanel blpPanel;

    private final ActiveViewportWatcher activeViewportWatcher = new ActiveViewportWatcher();

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
            repaintSelfAndChildren();
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
            repaintSelfAndChildren();
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
            repaintSelfAndChildren();
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
            repaintSelfAndChildren();
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
            repaintSelfAndChildren();
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
    AbstractAction expandSelectionAction = new AbstractAction("Expand Selection") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().expandSelection());
            }
            repaint();
        }
    };
    AbstractAction snapNormalsAction = new AbstractAction("Snap Normals") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().snapNormals());
            }
            repaint();
        }
    };
    AbstractAction snapVerticesAction = new AbstractAction("Snap Vertices") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                mpanel.getUndoManager()
                        .pushAction(mpanel.getModelEditorManager().getModelEditor().snapSelectedVertices());
            }
            repaint();
        }
    };
    AbstractAction recalcNormalsAction = new AbstractAction("RecalculateNormals") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().recalcNormals());
            }
            repaint();
        }
    };
    AbstractAction recalcExtentsAction = new AbstractAction("RecalculateExtents") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                final JPanel messagePanel = new JPanel(new MigLayout());
                messagePanel.add(new JLabel("This will calculate the extents of all model components. Proceed?"),
                        "wrap");
                messagePanel.add(new JLabel("(It may destroy existing extents)"), "wrap");
                final JRadioButton considerAllBtn = new JRadioButton("Consider all geosets for calculation");
                final JRadioButton considerCurrentBtn = new JRadioButton(
                        "Consider current editable geosets for calculation");
                final ButtonGroup buttonGroup = new ButtonGroup();
                buttonGroup.add(considerAllBtn);
                buttonGroup.add(considerCurrentBtn);
                considerAllBtn.setSelected(true);
                messagePanel.add(considerAllBtn, "wrap");
                messagePanel.add(considerCurrentBtn, "wrap");
                final int userChoice = JOptionPane.showConfirmDialog(MainPanel.this, messagePanel, "Message",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (userChoice == JOptionPane.YES_OPTION) {
                    mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor()
                            .recalcExtents(considerCurrentBtn.isSelected()));
                }
            }
            repaint();
        }
    };
    AbstractAction flipAllUVsUAction = new AbstractAction("Flip All UVs U") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            for (final Geoset geo : currentMDL().getGeosets()) {
                for (final GeosetVertex vertex : geo.getVertices()) {
                    for (final Vec2 tvert : vertex.getTverts()) {
                        tvert.x = 1.0f - tvert.x;
                    }
                }
            }
            repaint();
        }
    };
    AbstractAction flipAllUVsVAction = new AbstractAction("Flip All UVs V") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            // TODO this should be an action
            for (final Geoset geo : currentMDL().getGeosets()) {
                for (final GeosetVertex vertex : geo.getVertices()) {
                    for (final Vec2 tvert : vertex.getTverts()) {
                        tvert.y = 1.0f - tvert.y;
                    }
                }
            }
            repaint();
        }
    };
    AbstractAction inverseAllUVsAction = new AbstractAction("Swap UVs U for V") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            // TODO this should be an action
            for (final Geoset geo : currentMDL().getGeosets()) {
                for (final GeosetVertex vertex : geo.getVertices()) {
                    for (final Vec2 tvert : vertex.getTverts()) {
                        final float temp = tvert.x;
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
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                final Vec3 selectionCenter = mpanel.getModelEditorManager().getModelEditor().getSelectionCenter();
                mpanel.getUndoManager()
                        .pushAction(mpanel.getModelEditorManager().getModelEditor().mirror((byte) 0,
                                mirrorFlip.isSelected(), selectionCenter.x, selectionCenter.y,
                                selectionCenter.z));
            }
            repaint();
        }
    };
    AbstractAction mirrorYAction = new AbstractAction("Mirror Y") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                final Vec3 selectionCenter = mpanel.getModelEditorManager().getModelEditor().getSelectionCenter();

                mpanel.getUndoManager()
                        .pushAction(mpanel.getModelEditorManager().getModelEditor().mirror((byte) 1,
                                mirrorFlip.isSelected(), selectionCenter.x, selectionCenter.y,
                                selectionCenter.z));
            }
            repaint();
        }
    };
    AbstractAction mirrorZAction = new AbstractAction("Mirror Z") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                final Vec3 selectionCenter = mpanel.getModelEditorManager().getModelEditor().getSelectionCenter();

                mpanel.getUndoManager()
                        .pushAction(mpanel.getModelEditorManager().getModelEditor().mirror((byte) 2,
                                mirrorFlip.isSelected(), selectionCenter.x, selectionCenter.y,
                                selectionCenter.z));
            }
            repaint();
        }
    };
    AbstractAction insideOutAction = new AbstractAction("Inside Out") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().flipSelectedFaces());
            }
            repaint();
        }
    };
    AbstractAction insideOutNormalsAction = new AbstractAction("Inside Out Normals") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                mpanel.getUndoManager()
                        .pushAction(mpanel.getModelEditorManager().getModelEditor().flipSelectedNormals());
            }
            repaint();
        }
    };
    AbstractAction viewMatricesAction = new AbstractAction("View Matrices") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ModelPanel mpanel = currentModelPanel();
            if (mpanel != null) {
                mpanel.viewMatrices();
            }
            repaint();
        }
    };
    AbstractAction openAnimationViewerAction = new OpenViewAction("Animation Preview", new OpenViewGetter() {
        @Override
        public View getView() {
            return previewView;
        }
    });
    AbstractAction openAnimationControllerAction = new OpenViewAction("Animation Controller", new OpenViewGetter() {
        @Override
        public View getView() {
            return animationControllerView;
        }
    });
    AbstractAction openModelingTabAction = new OpenViewAction("Modeling", new OpenViewGetter() {
        @Override
        public View getView() {
            return creatorView;
        }
    });
    AbstractAction openPerspectiveAction = new OpenViewAction("Perspective", new OpenViewGetter() {
        @Override
        public View getView() {
            return perspectiveView;
        }
    });
    AbstractAction openOutlinerAction = new OpenViewAction("Outliner", new OpenViewGetter() {
        @Override
        public View getView() {
            return viewportControllerWindowView;
        }
    });
    AbstractAction openSideAction = new OpenViewAction("Side", new OpenViewGetter() {
        @Override
        public View getView() {
            return leftView;
        }
    });
    AbstractAction openTimeSliderAction = new OpenViewAction("Footer", new OpenViewGetter() {
        @Override
        public View getView() {
            return timeSliderView;
        }
    });
    AbstractAction openFrontAction = new OpenViewAction("Front", new OpenViewGetter() {
        @Override
        public View getView() {
            return frontView;
        }
    });
    AbstractAction openBottomAction = new OpenViewAction("Bottom", new OpenViewGetter() {
        @Override
        public View getView() {
            return bottomView;
        }
    });
    AbstractAction openToolsAction = new OpenViewAction("Tools", new OpenViewGetter() {
        @Override
        public View getView() {
            return toolView;
        }
    });
    AbstractAction openModelDataContentsViewAction = new OpenViewAction("Model", new OpenViewGetter() {
        @Override
        public View getView() {
            return modelDataView;
        }
    });
    AbstractAction hackerViewAction = new OpenViewAction("Matrix Eater Script", new OpenViewGetter() {
        @Override
        public View getView() {
            return hackerView;
        }
    });
    AbstractAction openPreferencesAction = new AbstractAction("Open Preferences") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final ProgramPreferences programPreferences = new ProgramPreferences();
            programPreferences.loadFrom(prefs);
            final List<DataSourceDescriptor> priorDataSources = SaveProfile.get().getDataSources();
            final ProgramPreferencesPanel programPreferencesPanel = new ProgramPreferencesPanel(programPreferences,
                    priorDataSources);
            // final JFrame frame = new JFrame("Preferences");
            // frame.setIconImage(MainFrame.frame.getIconImage());
            // frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            // frame.setContentPane(programPreferencesPanel);
            // frame.pack();
            // frame.setLocationRelativeTo(MainPanel.this);
            // frame.setVisible(true);

            final int ret = JOptionPane.showConfirmDialog(MainPanel.this, programPreferencesPanel, "Preferences",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ret == JOptionPane.OK_OPTION) {
                prefs.loadFrom(programPreferences);
                final List<DataSourceDescriptor> dataSources = programPreferencesPanel.getDataSources();
                final boolean changedDataSources = (dataSources != null) && !dataSources.equals(priorDataSources);
                if (changedDataSources) {
                    SaveProfile.get().setDataSources(dataSources);
                }
                SaveProfile.save();
                if (changedDataSources) {
                    dataSourcesChanged();
                }
                updateUIFromProgramPreferences();
            }
        }
    };
    AbstractAction openMPQViewerAction = new AbstractAction("Open MPQ Browser") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final View view = createMPQBrowser();
            rootWindow
                    .setWindow(new SplitWindow(true, 0.75f, rootWindow.getWindow(), view));
        }
    };
    AbstractAction openUnitViewerAction = new AbstractAction("Open Unit Browser") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final UnitEditorTree unitEditorTree = createUnitEditorTree();
            rootWindow.setWindow(new SplitWindow(true, 0.75f, rootWindow.getWindow(),
                    new View("Unit Browser",
                            new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
                            new JScrollPane(unitEditorTree))));
        }
    };
    AbstractAction openDoodadViewerAction = new AbstractAction("Open Doodad Browser") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final UnitEditorTree unitEditorTree = new UnitEditorTree(getDoodadData(), new DoodadTabTreeBrowserBuilder(),
                    getUnitEditorSettings(), WorldEditorDataType.DOODADS);
            unitEditorTree.selectFirstUnit();
            // final FloatingWindow floatingWindow =
            // rootWindow.createFloatingWindow(rootWindow.getLocation(),
            // mpqBrowser.getPreferredSize(),
            // new View("MPQ Browser",
            // new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16,
            // Image.SCALE_FAST)),
            // mpqBrowser));
            // floatingWindow.getTopLevelAncestor().setVisible(true);
            unitEditorTree.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    try {
                        if (e.getClickCount() >= 2) {
                            final TreePath currentUnitTreePath = unitEditorTree.getSelectionPath();
                            if (currentUnitTreePath != null) {
                                final DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath
                                        .getLastPathComponent();
                                if (o.getUserObject() instanceof MutableGameObject) {
                                    final MutableGameObject obj = (MutableGameObject) o.getUserObject();
                                    final int numberOfVariations = obj.getFieldAsInteger(War3ID.fromString("dvar"), 0);
                                    if (numberOfVariations > 1) {
                                        for (int i = 0; i < numberOfVariations; i++) {
                                            final String path = convertPathToMDX(
                                                    obj.getFieldAsString(War3ID.fromString("dfil"), 0) + i + ".mdl");
                                            final String portrait = ModelUtils.getPortrait(path);
                                            final ImageIcon icon = new ImageIcon(IconUtils
                                                    .getIcon(obj, WorldEditorDataType.DOODADS)
                                                    .getScaledInstance(16, 16, Image.SCALE_DEFAULT));

                                            System.out.println(path);
                                            loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(path), true, i == 0,
                                                    icon);
                                            if (prefs.isLoadPortraits()
                                                    && GameDataFileSystem.getDefault().has(portrait)) {
                                                loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(portrait), true,
                                                        false, icon);
                                            }
                                        }
                                    } else {
                                        final String path = convertPathToMDX(
                                                obj.getFieldAsString(War3ID.fromString("dfil"), 0));
                                        final String portrait = ModelUtils.getPortrait(path);
                                        final ImageIcon icon = new ImageIcon(IconUtils
                                                .getIcon(obj, WorldEditorDataType.DOODADS)
                                                .getScaledInstance(16, 16, Image.SCALE_DEFAULT));
                                        System.out.println(path);
                                        loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(path), true, true, icon);
                                        if (prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
                                            loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false,
                                                    icon);
                                        }
                                    }
                                    toolsMenu.getAccessibleContext().setAccessibleDescription(
                                            "Allows the user to control which parts of the model are displayed for editing.");
                                    toolsMenu.setEnabled(true);
                                }
                            }
                        }
                    } catch (final Exception exc) {
                        exc.printStackTrace();
                        ExceptionPopup.display(exc);
                    }
                }
            });
            rootWindow.setWindow(new SplitWindow(true, 0.75f, rootWindow.getWindow(),
                    new View("Doodad Browser",
                            new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
                            new JScrollPane(unitEditorTree))));
        }
    };
    AbstractAction openHiveViewerAction = new AbstractAction("Open Hive Browser") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(BorderLayout.BEFORE_FIRST_LINE, new JLabel(POWERED_BY_HIVE));
            // final JPanel resourceFilters = new JPanel();
            // resourceFilters.setBorder(BorderFactory.createTitledBorder("Resource
            // Filters"));
            // panel.add(BorderLayout.BEFORE_LINE_BEGINS, resourceFilters);
            // resourceFilters.add(new JLabel("Resource Type"));
            // resourceFilters.add(new JComboBox<>(new String[] { "Any" }));
            final JList<String> view = new JList<>(
                    new String[]{"Bongo Bongo (Phantom Shadow Beast)", "Other Model", "Other Model"});
            view.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(final javax.swing.JList<?> list, final Object value,
                                                              final int index, final boolean isSelected, final boolean cellHasFocus) {
                    final Component listCellRendererComponent = super.getListCellRendererComponent(list, value, index,
                            isSelected, cellHasFocus);
                    final ImageIcon icon = new ImageIcon(MainPanel.class.getResource("ImageBin/deleteme.png"));
                    setIcon(new ImageIcon(icon.getImage().getScaledInstance(48, 32, Image.SCALE_DEFAULT)));
                    return listCellRendererComponent;
                }
            });
            panel.add(BorderLayout.BEFORE_LINE_BEGINS, new JScrollPane(view));

            final JPanel tags = new JPanel();
            tags.setBorder(BorderFactory.createTitledBorder("Tags"));
            tags.setLayout(new GridLayout(30, 1));
            tags.add(new JCheckBox("Results must include all selected tags"));
            tags.add(new JSeparator());
            tags.add(new JLabel("Types (Models)"));
            tags.add(new JSeparator());
            tags.add(new JCheckBox("Building"));
            tags.add(new JCheckBox("Doodad"));
            tags.add(new JCheckBox("Item"));
            tags.add(new JCheckBox("User Interface"));
            panel.add(BorderLayout.CENTER, tags);
            // final FloatingWindow floatingWindow =
            // rootWindow.createFloatingWindow(rootWindow.getLocation(),
            // mpqBrowser.getPreferredSize(),
            // new View("MPQ Browser",
            // new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16,
            // Image.SCALE_FAST)),
            // mpqBrowser));
            // floatingWindow.getTopLevelAncestor().setVisible(true);
            rootWindow.setWindow(new SplitWindow(true, 0.75f, rootWindow.getWindow(),
                    new View("Hive Browser",
                            new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
                            panel)));
        }
    };
    private ToolbarButtonGroup<SelectionItemTypes> selectionItemTypeGroup;
    private ToolbarButtonGroup<SelectionMode> selectionModeGroup;
    private ToolbarButtonGroup<ToolbarActionButtonType> actionTypeGroup;
    private final ModelStructureChangeListener modelStructureChangeListener;
    private JMenuItem combineAnims;
    private JMenuItem exportAnimatedToStaticMesh;
    private JMenuItem exportAnimatedFramePNG;
    private final ViewportTransferHandler viewportTransferHandler;
    private final StringViewMap viewMap;
    private final RootWindow rootWindow;
    private final View viewportControllerWindowView;
    private final View toolView;
    private final View modelDataView;
    private final View modelComponentView;
    private ControllableTimeBoundProvider timeBoundProvider;
    private ActivityDescriptor currentActivity;

    public MainPanel() {
        super();

        add(createJToolBar());
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
        modelStructureChangeListener = new ModelStructureChangeListenerImplementation(() -> currentModelPanel().getModel());
        animatedRenderEnvironment = new TimeEnvironmentImpl();
        blpPanel = new ZoomableImagePreviewPanel(null);
        timeSliderPanel = new TimeSliderPanel(animatedRenderEnvironment, modelStructureChangeListener,
                prefs);
        timeSliderPanel.setDrawing(false);
        timeSliderPanel.addListener(currentTime -> {
            animatedRenderEnvironment
                    .setCurrentTime(currentTime - animatedRenderEnvironment.getStart());
            if (currentModelPanel() != null) {
                currentModelPanel().getEditorRenderModel().updateNodes(true, false);
                currentModelPanel().repaintSelfAndRelatedChildren();
            }
        });
//		timeSliderPanel.addListener(creatorPanel);
        animatedRenderEnvironment.addChangeListener(new TimeBoundChangeListener() {
            @Override
            public void timeBoundsChanged(final int start, final int end) {
                final Integer globalSeq = animatedRenderEnvironment.getGlobalSeq();
                if (globalSeq != null) {
                    creatorPanel.setChosenGlobalSeq(globalSeq);
                } else {
                    final ModelPanel modelPanel = currentModelPanel();
                    if (modelPanel != null) {
                        boolean foundAnim = false;
                        for (final Animation animation : modelPanel.getModel().getAnims()) {
                            if ((animation.getStart() == start) && (animation.getEnd() == end)) {
                                creatorPanel.setChosenAnimation(animation);
                                foundAnim = true;
                                break;
                            }
                        }
                        if (!foundAnim) {
                            creatorPanel.setChosenAnimation(null);
                        }
                    }

                }
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
            repaintSelfAndChildren();
            mpanel.repaintSelfAndRelatedChildren();
        });
        setTimeBounds = new JButton(RMSIcons.setTimeBoundsIcon);
        setTimeBounds.setMargin(new Insets(0, 0, 0, 0));
        setTimeBounds.setToolTipText("Choose Time Bounds");
        setTimeBounds.addActionListener(e -> {
            final TimeBoundChooserPanel timeBoundChooserPanel = new TimeBoundChooserPanel(
                    currentModelPanel() == null ? null : currentModelPanel().getModelViewManager(),
                    modelStructureChangeListener);
            final int confirmDialogResult = JOptionPane.showConfirmDialog(MainPanel.this, timeBoundChooserPanel,
                    "Set Time Bounds", JOptionPane.OK_CANCEL_OPTION);
            if (confirmDialogResult == JOptionPane.OK_OPTION) {
                timeBoundChooserPanel.applyTo(animatedRenderEnvironment);
                if (currentModelPanel() != null) {
                    currentModelPanel().getEditorRenderModel().refreshFromEditor(
                            animatedRenderEnvironment, IDENTITY, IDENTITY, IDENTITY,
                            currentModelPanel().getPerspArea().getViewport());
                    currentModelPanel().getEditorRenderModel().updateNodes(true, false);
                }
            }
        });

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
        rootWindow.addListener(new DockingWindowListener() {
            @Override
            public void windowUndocking(final DockingWindow arg0) throws OperationAbortedException {
            }

            @Override
            public void windowUndocked(final DockingWindow dockingWindow) {
                SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> {
                    if (dockingWindow instanceof View) {
                        final Component component = ((View) dockingWindow).getComponent();
                        if (component instanceof JComponent) {
                            linkActions(((JComponent) component).getRootPane());
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
        });
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
            traverseAndReset(rootWindow);
            traverseAndFix(rootWindow);
        };
        rootWindow.addListener(new DockingWindowListener() {

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
        });
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
                final ModelPanel modelPanel = currentModelPanel();
                if (modelPanel != null) {
                    engine.put("modelPanel", modelPanel);
                    engine.put("model", modelPanel.getModel());
                    engine.put("world", MainPanel.this);
                    try {
                        engine.eval(text);
                    } catch (final ScriptException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(MainPanel.this, e1.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(MainPanel.this, "Must open a file!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        hackerPanel.add(run, BorderLayout.NORTH);
        hackerView = new View("Matrix Eater Script", null, hackerPanel);
        creatorPanel = new CreatorModelingPanel(newType -> {
            actionTypeGroup.maybeSetButtonType(newType);
            changeActivity(newType);
        }, prefs, actionTypeGroup, activeViewportWatcher, animatedRenderEnvironment);
        creatorView = new View("Modeling", null, creatorPanel);
        animationControllerView = new View("Animation Controller", null, new JPanel());
        final TabWindow startupTabWindow = createMainLayout();
        rootWindow.setWindow(startupTabWindow);
        rootWindow.getRootWindowProperties().getFloatingWindowProperties().setUseFrame(true);
        startupTabWindow.setSelectedTab(0);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(toolbar)
                .addComponent(rootWindow));
        layout.setVerticalGroup(
                layout.createSequentialGroup().addComponent(toolbar).addComponent(rootWindow));
        setLayout(layout);
        // Create a file chooser
        fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Supported Files (*.mdx;*.mdl;*.blp;*.dds;*.tga;*.png;*.obj,*.fbx)", "mdx", "mdl", "blp", "dds", "tga", "png", "obj", "fbx"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Files (*.mdx;*.mdl;*.blp;*.dds;*.tga)", "mdx", "mdl", "blp", "dds", "tga"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Binary Model (*.mdx)", "mdx"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Text Model (*.mdl)", "mdl"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III BLP Image (*.blp)", "blp"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("DDS Image (*.dds)", "dds"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("TGA Image (*.tga)", "tga"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("PNG Image (*.png)", "png"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Wavefront OBJ Model (*.obj)", "obj"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Autodesk FBX Model (*.fbx)", "fbx"));
        exportTextureDialog = new JFileChooser();
        exportTextureDialog.setDialogTitle("Export Texture");
        final String[] imageTypes = ImageIO.getWriterFileSuffixes();
        for (final String suffix : imageTypes) {
            exportTextureDialog
                    .addChoosableFileFilter(new FileNameExtensionFilter(suffix.toUpperCase() + " Image File", suffix));
        }

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
            SwingUtilities.invokeLater(this::refreshAnimationModeState);

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
        coordDisplayListener = this::setMouseCoordDisplay;
    }

    private TabWindow createMainLayout() {
        final TabWindow leftHandTabWindow = new TabWindow(
                new DockingWindow[]{viewportControllerWindowView, toolView});
        leftHandTabWindow.setSelectedTab(0);
//		leftHandTabWindow.getWindowProperties().setCloseEnabled(false);
        final SplitWindow editingTab = new SplitWindow(false, 0.875f,
                new SplitWindow(true, 0.2f, leftHandTabWindow,
                        new SplitWindow(true, 0.8f,
                                new SplitWindow(false, new SplitWindow(true, frontView, bottomView),
                                        new SplitWindow(true, leftView, perspectiveView)),
                                creatorView)),
                timeSliderView);
        editingTab.getWindowProperties().setCloseEnabled(false);
        editingTab.getWindowProperties().setTitleProvider(arg0 -> "Edit");
        final ImageIcon imageIcon;
        imageIcon = new ImageIcon(MainFrame.MAIN_PROGRAM_ICON.getScaledInstance(16, 16, Image.SCALE_FAST));

        final View mpqBrowserView = createMPQBrowser(imageIcon);

        final UnitEditorTree unitEditorTree = createUnitEditorTree();
        final TabWindow tabWindow = new TabWindow(new DockingWindow[]{mpqBrowserView,
                new View("Unit Browser", imageIcon, new JScrollPane(unitEditorTree))});
        tabWindow.setSelectedTab(0);
        final SplitWindow viewingTab = new SplitWindow(true, 0.8f,
                new SplitWindow(true, 0.8f, previewView, animationControllerView), tabWindow);
        viewingTab.getWindowProperties().setTitleProvider(arg0 -> "View");
        viewingTab.getWindowProperties().setCloseEnabled(false);

        final SplitWindow modelTab = new SplitWindow(true, 0.2f, modelDataView, modelComponentView);
        modelTab.getWindowProperties().setTitleProvider(arg0 -> "Model");

        final TabWindow startupTabWindow = new TabWindow(new DockingWindow[]{viewingTab, editingTab, modelTab});
        traverseAndFix(startupTabWindow);
        return startupTabWindow;
    }

    private View createMPQBrowser(final ImageIcon imageIcon) {
        final MPQBrowser mpqBrowser = new MPQBrowser(GameDataFileSystem.getDefault(), filepath -> {
            if (filepath.toLowerCase().endsWith(".mdx")) {
                loadFile(GameDataFileSystem.getDefault().getFile(filepath), true);
            } else if (filepath.toLowerCase().endsWith(".blp")) {
                loadBLPPathAsModel(filepath);
            } else if (filepath.toLowerCase().endsWith(".png")) {
                loadBLPPathAsModel(filepath);
            } else if (filepath.toLowerCase().endsWith(".dds")) {
                loadBLPPathAsModel(filepath, null, 1000);
            }
        }, path -> {
            final int modIndex = Math.max(path.lastIndexOf(".w3mod/"), path.lastIndexOf(".w3mod\\"));
            String finalPath;
            if (modIndex == -1) {
                finalPath = path;
            } else {
                finalPath = path.substring(modIndex + ".w3mod/".length());
            }
            final ModelPanel modelPanel = currentModelPanel();
            if (modelPanel != null) {
                if (modelPanel.getModel().getFormatVersion() > 800) {
                    finalPath = finalPath.replace("\\", "/"); // Reforged prefers forward slash
                }
                modelPanel.getModel().add(new Bitmap(finalPath));
                modelStructureChangeListener.texturesChanged();
            }
        });
        final View view = new View("Data Browser", imageIcon, mpqBrowser);
        view.getWindowProperties().setCloseEnabled(true);
        return view;
    }

    private View createMPQBrowser() {
        return createMPQBrowser(
                new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)));
    }

    private void traverseAndFix(final DockingWindow window) {
        final boolean tabWindow = window instanceof TabWindow;
        final int childWindowCount = window.getChildWindowCount();
        for (int i = 0; i < childWindowCount; i++) {
            final DockingWindow childWindow = window.getChildWindow(i);
            traverseAndFix(childWindow);
            if (tabWindow && (childWindowCount != 1) && (childWindow instanceof View)) {
                final View view = (View) childWindow;
                view.getViewProperties().getViewTitleBarProperties().setVisible(false);
            }
        }
    }

    private void traverseAndReset(final DockingWindow window) {
        final int childWindowCount = window.getChildWindowCount();
        for (int i = 0; i < childWindowCount; i++) {
            final DockingWindow childWindow = window.getChildWindow(i);
            traverseAndReset(childWindow);
            if (childWindow instanceof View) {
                final View view = (View) childWindow;
                view.getViewProperties().getViewTitleBarProperties().setVisible(true);
            }
        }
    }

    private void traverseAndReloadData(final DockingWindow window) {
        final int childWindowCount = window.getChildWindowCount();
        for (int i = 0; i < childWindowCount; i++) {
            final DockingWindow childWindow = window.getChildWindow(i);
            traverseAndReloadData(childWindow);
            if (childWindow instanceof View) {
                final View view = (View) childWindow;
                final Component component = view.getComponent();
                if (component instanceof JScrollPane) {
                    final JScrollPane pane = (JScrollPane) component;
                    final Component viewportView = pane.getViewport().getView();
                    if (viewportView instanceof UnitEditorTree) {
                        final UnitEditorTree unitEditorTree = (UnitEditorTree) viewportView;
                        final WorldEditorDataType dataType = unitEditorTree.getDataType();
                        if (dataType == WorldEditorDataType.UNITS) {
                            System.out.println("saw unit tree");
                            unitEditorTree.setUnitDataAndReloadVerySlowly(getUnitData());
                        } else if (dataType == WorldEditorDataType.DOODADS) {
                            System.out.println("saw doodad tree");
                            unitEditorTree.setUnitDataAndReloadVerySlowly(getDoodadData());
                        }
                    }
                } else if (component instanceof MPQBrowser) {
                    System.out.println("saw mpq tree");
                    final MPQBrowser comp = (MPQBrowser) component;
                    comp.refreshTree();
                }
            }
        }
    }

    private UnitEditorTree createUnitEditorTree() {
        final UnitEditorTree unitEditorTree = new UnitEditorTreeBrowser(getUnitData(), new UnitTabTreeBrowserBuilder(),
                getUnitEditorSettings(), WorldEditorDataType.UNITS, (mdxFilePath, b, c, icon) -> loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(mdxFilePath), b, c, icon), prefs);
        return unitEditorTree;
    }

    @Override
    public void changeActivity(final ActivityDescriptor newType) {
        currentActivity = newType;
        for (final ModelPanel modelPanel : modelPanels) {
            modelPanel.changeActivity(newType);
        }
        creatorPanel.changeActivity(newType);
    }

    private static final Quat IDENTITY = new Quat();
    private final TimeEnvironmentImpl animatedRenderEnvironment;
    private JButton snapButton;
    private final CoordDisplayListener coordDisplayListener;
    protected ModelEditorActionType actionType;
    private JMenu teamColorMenu;
    private final CreatorModelingPanel creatorPanel;
    private ToolbarActionButtonType selectAndMoveDescriptor;
    private ToolbarActionButtonType selectAndRotateDescriptor;
    private ToolbarActionButtonType selectAndScaleDescriptor;
    private ToolbarActionButtonType selectAndExtrudeDescriptor;
    private ToolbarActionButtonType selectAndExtendDescriptor;

    public void refreshAnimationModeState() {
        if (animationModeState) {
            if ((currentModelPanel() != null) && (currentModelPanel().getModel() != null)) {
                if (currentModelPanel().getModel().getAnimsSize() > 0) {
                    final Animation anim = currentModelPanel().getModel().getAnim(0);
                    animatedRenderEnvironment.setBounds(anim.getStart(), anim.getEnd());
                }
                currentModelPanel().getEditorRenderModel().refreshFromEditor(animatedRenderEnvironment, IDENTITY,
                        IDENTITY, IDENTITY, currentModelPanel().getPerspArea().getViewport());
                currentModelPanel().getEditorRenderModel().updateNodes(true, false);
                timeSliderPanel.setNodeSelectionManager(
                        currentModelPanel().getModelEditorManager().getNodeAnimationSelectionManager());
            }
            if ((actionTypeGroup.getActiveButtonType() == actionTypeGroup.getToolbarButtonTypes()[3])
                    || (actionTypeGroup
                    .getActiveButtonType() == actionTypeGroup.getToolbarButtonTypes()[4])) {
                actionTypeGroup.setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[0]);
            }
        }
        animatedRenderEnvironment.setStaticViewMode(!animationModeState);
        if (!animationModeState) {
            if ((currentModelPanel() != null) && (currentModelPanel().getModel() != null)) {
                currentModelPanel().getEditorRenderModel().refreshFromEditor(animatedRenderEnvironment, IDENTITY,
                        IDENTITY, IDENTITY, currentModelPanel().getPerspArea().getViewport());
                currentModelPanel().getEditorRenderModel().updateNodes(true, false); // update to 0 position
            }
        }
        final List<ToolbarButtonGroup<ToolbarActionButtonType>.ToolbarButtonAction> buttons = actionTypeGroup
                .getButtons();
        final int numberOfButtons = buttons.size();
        for (int i = 3; i < numberOfButtons; i++) {
            buttons.get(i).getButton().setVisible(!animationModeState);
        }
        snapButton.setVisible(!animationModeState);
        timeSliderPanel.setDrawing(animationModeState);
        setKeyframe.setVisible(animationModeState);
        setTimeBounds.setVisible(animationModeState);
        timeSliderPanel.setKeyframeModeActive(animationModeState);
        if (animationModeState) {
            animationModeButton.setColors(prefs.getActiveColor1(), prefs.getActiveColor2());
        } else {
            animationModeButton.resetColors();
        }
        timeSliderPanel.repaint();
        creatorPanel.setAnimationModeState(animationModeState);
    }

    private void reloadGeosetManagers(final ModelPanel display) {
        geoControl.repaint();
        display.getModelViewManagingTree().reloadFromModelView();
        geoControl.setViewportView(display.getModelViewManagingTree());
        reloadComponentBrowser(display);
        display.getPerspArea().reloadTextures();// .mpanel.perspArea.reloadTextures();//addGeosets(newGeosets);
        display.getAnimationViewer().reload();
        display.getAnimationController().reload();
        creatorPanel.reloadAnimationList();

        display.getEditorRenderModel().refreshFromEditor(animatedRenderEnvironment, IDENTITY, IDENTITY, IDENTITY,
                display.getPerspArea().getViewport());
    }

    private void reloadComponentBrowser(final ModelPanel display) {
        geoControlModelData.repaint();
        display.getModelComponentBrowserTree().reloadFromModelView();
        geoControlModelData.setViewportView(display.getModelComponentBrowserTree());
    }

    public void reloadGUI() {
        refreshUndo();
        refreshController();
        refreshAnimationModeState();
        reloadGeosetManagers(currentModelPanel());

    }

    /**
     * Right now this is a plug to the statics to load unit data. However, it's a
     * non-static method so that we can have it load from an opened map in the
     * future -- the MutableObjectData class can parse map unit data!
     *
     * @return
     */
    public MutableObjectData getUnitData() {
        final War3ObjectDataChangeset editorData = new War3ObjectDataChangeset('u');
        try {
            final CompoundDataSource gameDataFileSystem = GameDataFileSystem.getDefault();
            if (gameDataFileSystem.has("war3map.w3u")) {
                editorData.load(new BlizzardDataInputStream(gameDataFileSystem.getResourceAsStream("war3map.w3u")),
                        gameDataFileSystem.has("war3map.wts") ? new WTSFile(gameDataFileSystem.getResourceAsStream("war3map.wts"))
                                : null,
                        true);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return new MutableObjectData(WorldEditorDataType.UNITS, StandardObjectData.getStandardUnits(),
                StandardObjectData.getStandardUnitMeta(), editorData);
    }

    public MutableObjectData getDoodadData() {
        final War3ObjectDataChangeset editorData = new War3ObjectDataChangeset('d');
        try {
            final CompoundDataSource gameDataFileSystem = GameDataFileSystem.getDefault();
            if (gameDataFileSystem.has("war3map.w3d")) {
                editorData.load(new BlizzardDataInputStream(gameDataFileSystem.getResourceAsStream("war3map.w3d")),
                        gameDataFileSystem.has("war3map.wts") ? new WTSFile(gameDataFileSystem.getResourceAsStream("war3map.wts"))
                                : null,
                        true);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return new MutableObjectData(WorldEditorDataType.DOODADS, StandardObjectData.getStandardDoodads(),
                StandardObjectData.getStandardDoodadMeta(), editorData);
    }

    public UnitEditorSettings getUnitEditorSettings() {
        return new UnitEditorSettings();
    }

    public JToolBar createJToolBar() {
        toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        toolbar.add(new AbstractAction("New", RMSIcons.loadToolBarImageIcon("new.png")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    newModel();
                } catch (final Exception exc) {
                    exc.printStackTrace();
                    ExceptionPopup.display(exc);
                }
            }
        });
        toolbar.add(new AbstractAction("Open", RMSIcons.loadToolBarImageIcon("open.png")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    onClickOpen();
                } catch (final Exception exc) {
                    exc.printStackTrace();
                    ExceptionPopup.display(exc);
                }
            }
        });
        toolbar.add(new AbstractAction("Save", RMSIcons.loadToolBarImageIcon("save.png")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    onClickSave();
                } catch (final Exception exc) {
                    exc.printStackTrace();
                    ExceptionPopup.display(exc);
                }
            }
        });
        toolbar.addSeparator();
        toolbar.add(new AbstractAction("Undo", RMSIcons.loadToolBarImageIcon("undo.png")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    currentModelPanel().getUndoManager().undo();
                } catch (final NoSuchElementException exc) {
                    JOptionPane.showMessageDialog(MainPanel.this, "Nothing to undo!");
                } catch (final Exception exc) {
                    ExceptionPopup.display(exc);
                    // exc.printStackTrace();
                }
                repaint();
            }
        });
        toolbar.add(new AbstractAction("Redo", RMSIcons.loadToolBarImageIcon("redo.png")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    currentModelPanel().getUndoManager().redo();
                } catch (final NoSuchElementException exc) {
                    JOptionPane.showMessageDialog(MainPanel.this, "Nothing to redo!");
                } catch (final Exception exc) {
                    ExceptionPopup.display(exc);
                    // exc.printStackTrace();
                }
                repaint();
            }
        });
        toolbar.addSeparator();
        selectionModeGroup = new ToolbarButtonGroup<>(toolbar, SelectionMode.values());
        toolbar.addSeparator();
        selectionItemTypeGroup = new ToolbarButtonGroup<>(toolbar, SelectionItemTypes.values());
        toolbar.addSeparator();
        selectAndMoveDescriptor = new ToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("move2.png"), "Select and Move") {
            @Override
            public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                              final ModelView modelView, final UndoActionListener undoActionListener) {
                actionType = ModelEditorActionType.TRANSLATION;
                return new ModelEditorMultiManipulatorActivity(
                        new MoverWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
                                modelEditorManager.getViewportSelectionHandler(), prefs, modelView),
                        undoActionListener, modelEditorManager.getSelectionView());
            }
        };
        selectAndRotateDescriptor = new ToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("rotate.png"), "Select and Rotate") {
            @Override
            public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                              final ModelView modelView, final UndoActionListener undoActionListener) {
                actionType = ModelEditorActionType.ROTATION;
                return new ModelEditorMultiManipulatorActivity(
                        new RotatorWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
                                modelEditorManager.getViewportSelectionHandler(), prefs, modelView),
                        undoActionListener, modelEditorManager.getSelectionView());
            }
        };
        selectAndScaleDescriptor = new ToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("scale.png"), "Select and Scale") {
            @Override
            public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                              final ModelView modelView, final UndoActionListener undoActionListener) {
                actionType = ModelEditorActionType.SCALING;
                return new ModelEditorMultiManipulatorActivity(
                        new ScaleWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
                                modelEditorManager.getViewportSelectionHandler(), prefs, modelView),
                        undoActionListener, modelEditorManager.getSelectionView());
            }
        };
        selectAndExtrudeDescriptor = new ToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("extrude.png"), "Select and Extrude") {
            @Override
            public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                              final ModelView modelView, final UndoActionListener undoActionListener) {
                actionType = ModelEditorActionType.TRANSLATION;
                return new ModelEditorMultiManipulatorActivity(
                        new ExtrudeWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
                                modelEditorManager.getViewportSelectionHandler(), prefs, modelView),
                        undoActionListener, modelEditorManager.getSelectionView());
            }
        };
        selectAndExtendDescriptor = new ToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("extend.png"), "Select and Extend") {
            @Override
            public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                              final ModelView modelView, final UndoActionListener undoActionListener) {
                actionType = ModelEditorActionType.TRANSLATION;
                return new ModelEditorMultiManipulatorActivity(
                        new ExtendWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
                                modelEditorManager.getViewportSelectionHandler(), prefs, modelView),
                        undoActionListener, modelEditorManager.getSelectionView());
            }
        };
        actionTypeGroup = new ToolbarButtonGroup<>(toolbar,
                new ToolbarActionButtonType[]{selectAndMoveDescriptor, selectAndRotateDescriptor,
                        selectAndScaleDescriptor, selectAndExtrudeDescriptor,
                        selectAndExtendDescriptor,});
        currentActivity = actionTypeGroup.getActiveButtonType();
        toolbar.addSeparator();
        snapButton = toolbar
                .add(new AbstractAction("Snap", RMSIcons.loadToolBarImageIcon("snap.png")) {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        try {
                            final ModelPanel currentModelPanel = currentModelPanel();
                            if (currentModelPanel != null) {
                                currentModelPanel.getUndoManager().pushAction(currentModelPanel.getModelEditorManager()
                                        .getModelEditor().snapSelectedVertices());
                            }
                        } catch (final NoSuchElementException exc) {
                            JOptionPane.showMessageDialog(MainPanel.this, "Nothing to undo!");
                        } catch (final Exception exc) {
                            ExceptionPopup.display(exc);
                        }
                    }
                });

        return toolbar;
    }

    public void init() {
        final JRootPane root = getRootPane();
        // JPanel root = this;
        linkActions(root);

        updateUIFromProgramPreferences();
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

    private void linkActions(final JComponent root) {
        root.getActionMap().put("Undo", undoAction);
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Z"),
                "Undo");

        root.getActionMap().put("Redo", redoAction);
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Y"),
                "Redo");

        root.getActionMap().put("Delete", deleteAction);
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DELETE"), "Delete");

        root.getActionMap().put("CloneSelection", cloneAction);

        root.getActionMap().put("MaximizeSpacebar", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Component focusedComponent = getFocusedComponent();
                if (focusedComponentNeedsTyping(focusedComponent)) {
                    return;
                }
                final View focusedView = rootWindow.getFocusedView();
                if (focusedView != null) {
                    if (focusedView.isMaximized()) {
                        rootWindow.setMaximizedWindow(null);
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
                if (animationModeState) {
                    timeSliderPanel.jumpRight();
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
                if (animationModeState) {
                    timeSliderPanel.jumpLeft();
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
                if (animationModeState) {
                    timeSliderPanel.jumpFrames(1);
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
                if (animationModeState) {
                    timeSliderPanel.jumpFrames(10);
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
                if (animationModeState) {
                    timeSliderPanel.jumpFrames(-1);
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
                if (animationModeState) {
                    timeSliderPanel.jumpFrames(-10);
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
                timeSliderPanel.play();
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
                actionTypeGroup
                        .setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[0]);
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
                actionTypeGroup
                        .setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[1]);
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
                actionTypeGroup
                        .setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[2]);
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
                if (!animationModeState) {
                    actionTypeGroup
                            .setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[3]);
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
                if (!animationModeState) {
                    actionTypeGroup
                            .setToolbarButtonType(actionTypeGroup.getToolbarButtonTypes()[4]);
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
                selectionItemTypeGroup
                        .setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[0]);
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
                selectionItemTypeGroup
                        .setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[1]);
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
                selectionItemTypeGroup
                        .setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[2]);
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
                selectionItemTypeGroup
                        .setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[3]);
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
                selectionItemTypeGroup
                        .setToolbarButtonType(selectionItemTypeGroup.getToolbarButtonTypes()[4]);
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
                prefs.setViewMode(prefs.getViewMode() == 1 ? 0 : 1);
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
                if (!animationModeState) {
                    try {
                        final ModelPanel modelPanel = currentModelPanel();
                        if (modelPanel != null) {
                            final Viewport viewport = activeViewportWatcher.getViewport();
                            final Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1)
                                    : viewport.getFacingVector();
                            final UndoAction createFaceFromSelection = modelPanel.getModelEditorManager()
                                    .getModelEditor().createFaceFromSelection(facingVector);
                            modelPanel.getUndoManager().pushAction(createFaceFromSelection);
                        }
                    } catch (final FaceCreationException exc) {
                        JOptionPane.showMessageDialog(MainPanel.this, exc.getMessage(), "Error",
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
                    final DockingWindow window = rootWindow.getWindow();
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
                if ((selectionModeGroup.getActiveButtonType() == SelectionMode.ADD)
                        && cheatShift) {
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
                if ((selectionModeGroup.getActiveButtonType() == SelectionMode.DESELECT)
                        && cheatAlt) {
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

        root.getActionMap().put("RigAction", rigAction);
        root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control W"),
                "RigAction");
    }

    private void updateUIFromProgramPreferences() {
        // prefs.setShowVertexModifierControls(showVertexModifyControls.isSelected());
        showVertexModifyControls.setSelected(prefs.isShowVertexModifierControls());
        // prefs.setTextureModels(textureModels.isSelected());
        textureModels.setSelected(prefs.isTextureModels());
        // prefs.setShowNormals(showNormals.isSelected());
        showNormals.setSelected(prefs.isShowNormals());
        // prefs.setLoadPortraits(true);
        fetchPortraitsToo.setSelected(prefs.isLoadPortraits());
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
        for (final ModelPanel mpanel : modelPanels) {
            mpanel.getEditorRenderModel()
                    .setSpawnParticles((prefs.getRenderParticles() == null) || prefs.getRenderParticles());
            mpanel.getEditorRenderModel().setAllowInanimateParticles(
                    (prefs.getRenderStaticPoseParticles() == null) || prefs.getRenderStaticPoseParticles());
            mpanel.getAnimationViewer()
                    .setSpawnParticles((prefs.getRenderParticles() == null) || prefs.getRenderParticles());
        }
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

        recentMenu = new JMenu("Open Recent");
        recentMenu.setMnemonic(KeyEvent.VK_R);
        recentMenu.getAccessibleContext().setAccessibleDescription("Allows you to access recently opened files.");

        editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        // editMenu.addMouseListener(this);
        editMenu.getAccessibleContext()
                .setAccessibleDescription("Allows the user to use various tools to edit the currently selected model.");
        menuBar.add(editMenu);

        toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        toolsMenu.getAccessibleContext().setAccessibleDescription(
                "Allows the user to use various model editing tools. (You must open a model before you may use this menu.)");
        toolsMenu.setEnabled(false);
        menuBar.add(toolsMenu);

        viewMenu = new JMenu("View");
        // viewMenu.setMnemonic(KeyEvent.VK_V);
        viewMenu.getAccessibleContext().setAccessibleDescription("Allows the user to control view settings.");
        menuBar.add(viewMenu);

        teamColorMenu = new JMenu("Team Color");
        teamColorMenu.getAccessibleContext()
                .setAccessibleDescription("Allows the user to control team color settings.");
        menuBar.add(teamColorMenu);

        directoryChangeNotifier.subscribe(() -> {
            GameDataFileSystem.refresh(SaveProfile.get().getDataSources());
            // cache priority order...
            UnitOptionPanel.dropRaceCache();
            DataTable.dropCache();
            ModelOptionPanel.dropCache();
            WEString.dropCache();
            BLPHandler.get().dropCache();
            teamColorMenu.removeAll();
            createTeamColorMenuItems();
            traverseAndReloadData(rootWindow);
        });
        createTeamColorMenuItems();

        windowMenu = new JMenu("Window");
        windowMenu.setMnemonic(KeyEvent.VK_W);
        windowMenu.getAccessibleContext()
                .setAccessibleDescription("Allows the user to open various windows containing the program features.");
        menuBar.add(windowMenu);

        final JMenuItem resetViewButton = new JMenuItem("Reset Layout");
        resetViewButton.addActionListener(e -> {
            traverseAndReset(rootWindow);
            final TabWindow startupTabWindow = createMainLayout();
            rootWindow.setWindow(startupTabWindow);
            traverseAndFix(rootWindow);
        });
        windowMenu.add(resetViewButton);

        final JMenu viewsMenu = new JMenu("Views");
        viewsMenu.setMnemonic(KeyEvent.VK_V);
        windowMenu.add(viewsMenu);

        final JMenuItem testItem = new JMenuItem("test");
        testItem.addActionListener(new OpenViewAction("Animation Preview", () -> {
            final JPanel testPanel = new JPanel();

            for (int i = 0; i < 3; i++) {
//					final ControlledAnimationViewer animationViewer = new ControlledAnimationViewer(
//							currentModelPanel().getModelViewManager(), prefs);
//					animationViewer.setMinimumSize(new Dimension(400, 400));
//					final AnimationController animationController = new AnimationController(
//							currentModelPanel().getModelViewManager(), true, animationViewer);

                final AnimationViewer animationViewer2 = new AnimationViewer(
                        currentModelPanel().getModelViewManager(), prefs, false);
                animationViewer2.setMinimumSize(new Dimension(400, 400));
                testPanel.add(animationViewer2);
//					testPanel.add(animationController);
            }
            testPanel.setLayout(new GridLayout(1, 4));
            return new View("Test", null, testPanel);
        }));

//		viewsMenu.add(testItem);

        animationViewer = new JMenuItem("Animation Preview");
        animationViewer.setMnemonic(KeyEvent.VK_A);
        animationViewer.addActionListener(openAnimationViewerAction);
        viewsMenu.add(animationViewer);

        animationController = new JMenuItem("Animation Controller");
        animationController.setMnemonic(KeyEvent.VK_C);
        animationController.addActionListener(openAnimationControllerAction);
        viewsMenu.add(animationController);

        modelingTab = new JMenuItem("Modeling");
        modelingTab.setMnemonic(KeyEvent.VK_M);
        modelingTab.addActionListener(openModelingTabAction);
        viewsMenu.add(modelingTab);

        final JMenuItem outlinerItem = new JMenuItem("Outliner");
        outlinerItem.setMnemonic(KeyEvent.VK_O);
        outlinerItem.addActionListener(openOutlinerAction);
        viewsMenu.add(outlinerItem);

        final JMenuItem perspectiveItem = new JMenuItem("Perspective");
        perspectiveItem.setMnemonic(KeyEvent.VK_P);
        perspectiveItem.addActionListener(openPerspectiveAction);
        viewsMenu.add(perspectiveItem);

        final JMenuItem frontItem = new JMenuItem("Front");
        frontItem.setMnemonic(KeyEvent.VK_F);
        frontItem.addActionListener(openFrontAction);
        viewsMenu.add(frontItem);

        final JMenuItem sideItem = new JMenuItem("Side");
        sideItem.setMnemonic(KeyEvent.VK_S);
        sideItem.addActionListener(openSideAction);
        viewsMenu.add(sideItem);

        final JMenuItem bottomItem = new JMenuItem("Bottom");
        bottomItem.setMnemonic(KeyEvent.VK_B);
        bottomItem.addActionListener(openBottomAction);
        viewsMenu.add(bottomItem);

        final JMenuItem toolsItem = new JMenuItem("Tools");
        toolsItem.setMnemonic(KeyEvent.VK_T);
        toolsItem.addActionListener(openToolsAction);
        viewsMenu.add(toolsItem);

        final JMenuItem contentsItem = new JMenuItem("Contents");
        contentsItem.setMnemonic(KeyEvent.VK_C);
        contentsItem.addActionListener(openModelDataContentsViewAction);
        viewsMenu.add(contentsItem);

        final JMenuItem timeItem = new JMenuItem("Footer");
        timeItem.addActionListener(openTimeSliderAction);
        viewsMenu.add(timeItem);

        final JMenuItem hackerViewItem = new JMenuItem("Matrix Eater Script");
        hackerViewItem.setMnemonic(KeyEvent.VK_H);
        hackerViewItem.setAccelerator(KeyStroke.getKeyStroke("control P"));
        hackerViewItem.addActionListener(hackerViewAction);
        viewsMenu.add(hackerViewItem);

        final JMenu browsersMenu = new JMenu("Browsers");
        browsersMenu.setMnemonic(KeyEvent.VK_B);
        windowMenu.add(browsersMenu);

        mpqViewer = new JMenuItem("Data Browser");
        mpqViewer.setMnemonic(KeyEvent.VK_A);
        mpqViewer.addActionListener(openMPQViewerAction);
        browsersMenu.add(mpqViewer);

        unitViewer = new JMenuItem("Unit Browser");
        unitViewer.setMnemonic(KeyEvent.VK_U);
        unitViewer.addActionListener(openUnitViewerAction);
        browsersMenu.add(unitViewer);

        final JMenuItem doodadViewer = new JMenuItem("Doodad Browser");
        doodadViewer.setMnemonic(KeyEvent.VK_D);
        doodadViewer.addActionListener(openDoodadViewerAction);
        browsersMenu.add(doodadViewer);

        hiveViewer = new JMenuItem("Hive Browser");
        hiveViewer.setMnemonic(KeyEvent.VK_H);
        hiveViewer.addActionListener(openHiveViewerAction);
//		browsersMenu.add(hiveViewer);

        windowMenu.addSeparator();

        addMenu = new JMenu("Add");
        addMenu.setMnemonic(KeyEvent.VK_A);
        addMenu.getAccessibleContext()
                .setAccessibleDescription("Allows the user to add new components to the model.");
        menuBar.add(addMenu);

        addParticle = new JMenu("Particle");
        addParticle.setMnemonic(KeyEvent.VK_P);
        addMenu.add(addParticle);

        final File stockFolder = new File("stock/particles");
        final File[] stockFiles = stockFolder.listFiles((dir, name) -> name.endsWith(".mdx"));
        if (stockFiles != null) {
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
                                final ParticleEmitter2 particle;
                                try {
                                    particle = MdxUtils.loadEditable(file).sortedIdObjects(ParticleEmitter2.class).get(0);
                                } catch (final IOException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                    return;
                                }

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
                                parent.addActionListener(e14 -> {
                                    final IdObject choice = parent.getItemAt(parent.getSelectedIndex());
                                    xSpinner.setValue(choice.getPivotPoint().x);
                                    ySpinner.setValue(choice.getPivotPoint().y);
                                    zSpinner.setValue(choice.getPivotPoint().z);
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
                                chooseAnimations.addActionListener(e13 -> JOptionPane.showMessageDialog(particlePanel, animPanel));
                                final JButton[] colorButtons = new JButton[3];
                                final Color[] colors = new Color[colorButtons.length];
                                for (int i = 0; i < colorButtons.length; i++) {
                                    final Vec3 colorValues = particle.getSegmentColor(i);
                                    final Color color = new Color((int) (colorValues.z * 255), (int) (colorValues.y * 255),
                                            (int) (colorValues.x * 255));

                                    final JButton button = new JButton("Color " + (i + 1),
                                            new ImageIcon(IconUtils.createBlank(color, 32, 32)));
                                    colors[i] = color;
                                    final int index = i;
                                    button.addActionListener(e12 -> {
                                        final Color colorChoice = JColorChooser.showDialog(MainPanel.this,
                                                "Chooser Color", colors[index]);
                                        if (colorChoice != null) {
                                            colors[index] = colorChoice;
                                            button.setIcon(new ImageIcon(IconUtils.createBlank(colors[index], 32, 32)));
                                        }
                                    });
                                    colorButtons[i] = button;
                                }

                                final GroupLayout layout = new GroupLayout(particlePanel);

                                layout.setHorizontalGroup(
                                        layout.createSequentialGroup().addComponent(imageLabel).addGap(8)
                                                .addGroup(layout.createParallelGroup(Alignment.CENTER)
                                                        .addComponent(titleLabel)
                                                        .addGroup(layout.createSequentialGroup().addComponent(nameLabel)
                                                                .addGap(4).addComponent(nameField))
                                                        .addGroup(layout.createSequentialGroup().addComponent(parentLabel)
                                                                .addGap(4).addComponent(parent))
                                                        .addComponent(chooseAnimations)
                                                        .addGroup(layout.createSequentialGroup().addComponent(xLabel)
                                                                .addComponent(xSpinner).addGap(4).addComponent(yLabel)
                                                                .addComponent(ySpinner).addGap(4).addComponent(zLabel)
                                                                .addComponent(zSpinner))
                                                        .addGroup(
                                                                layout.createSequentialGroup().addComponent(colorButtons[0])
                                                                        .addGap(4).addComponent(colorButtons[1]).addGap(4)
                                                                        .addComponent(colorButtons[2]))));
                                layout.setVerticalGroup(
                                        layout.createParallelGroup(Alignment.CENTER).addComponent(imageLabel)
                                                .addGroup(
                                                        layout.createSequentialGroup().addComponent(titleLabel)
                                                                .addGroup(layout.createParallelGroup(Alignment.CENTER)
                                                                        .addComponent(nameLabel).addComponent(nameField))
                                                                .addGap(4)
                                                                .addGroup(layout.createParallelGroup(Alignment.CENTER)
                                                                        .addComponent(parentLabel).addComponent(parent))
                                                                .addGap(4).addComponent(chooseAnimations).addGap(4)
                                                                .addGroup(layout.createParallelGroup(Alignment.CENTER)
                                                                        .addComponent(xLabel).addComponent(xSpinner)
                                                                        .addComponent(yLabel).addComponent(ySpinner)
                                                                        .addComponent(zLabel).addComponent(zSpinner))
                                                                .addGap(4)
                                                                .addGroup(layout.createParallelGroup(Alignment.CENTER)
                                                                        .addComponent(colorButtons[0])
                                                                        .addComponent(colorButtons[1])
                                                                        .addComponent(colorButtons[2]))));
                                particlePanel.setLayout(layout);
                                final int x = JOptionPane.showConfirmDialog(MainPanel.this, particlePanel,
                                        "Add " + basicName, JOptionPane.OK_CANCEL_OPTION);
                                if (x == JOptionPane.OK_OPTION) {
                                    // do stuff
                                    particle.setPivotPoint(new Vec3(((Number) xSpinner.getValue()).doubleValue(),
                                            ((Number) ySpinner.getValue()).doubleValue(),
                                            ((Number) zSpinner.getValue()).doubleValue()));
                                    for (int i = 0; i < colors.length; i++) {
                                        particle.setSegmentColor(i, new Vec3(colors[i].getBlue() / 255.00,
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
                                            visFlag.addEntry(anim.getStart(), 0);
                                        }
                                        animIndex++;
                                    }
                                    particle.setVisibilityFlag(visFlag);
                                    particle.setName(nameField.getText());
                                    currentMDL().add(particle);
                                    modelStructureChangeListener
                                            .nodesAdded(Collections.singletonList(particle));
                                }
                            }
                        });
                        addParticle.add(particleItem);
                    } catch (final IOException e1) {
                        e1.printStackTrace();
                    }
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

        scriptsMenu = new JMenu("Scripts");
        scriptsMenu.setMnemonic(KeyEvent.VK_A);
        scriptsMenu.getAccessibleContext()
                .setAccessibleDescription("Allows the user to execute model edit scripts.");
        menuBar.add(scriptsMenu);

        importButtonS = new JMenuItem("Oinkerwinkle-Style AnimTransfer");
        importButtonS.setAccelerator(KeyStroke.getKeyStroke("control shift S"));
        importButtonS.setMnemonic(KeyEvent.VK_P);
        importButtonS.addActionListener(this);
        // importButtonS.setEnabled(false);
        scriptsMenu.add(importButtonS);

        mergeGeoset = new JMenuItem("Oinkerwinkle-Style Merge Geoset");
        mergeGeoset.setAccelerator(KeyStroke.getKeyStroke("control M"));
        mergeGeoset.setMnemonic(KeyEvent.VK_M);
        mergeGeoset.addActionListener(this);
        scriptsMenu.add(mergeGeoset);

        nullmodelButton = new JMenuItem("Edit/delete model components");
        nullmodelButton.setAccelerator(KeyStroke.getKeyStroke("control E"));
        nullmodelButton.setMnemonic(KeyEvent.VK_E);
        nullmodelButton.addActionListener(this);
        scriptsMenu.add(nullmodelButton);

        exportAnimatedToStaticMesh = new JMenuItem("Export Animated to Static Mesh");
        exportAnimatedToStaticMesh.setMnemonic(KeyEvent.VK_E);
        exportAnimatedToStaticMesh.addActionListener(e -> {
            if (!animationModeState) {
                JOptionPane.showMessageDialog(MainPanel.this, "You must be in the Animation Editor to use that!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            final Vec4 vertexHeap = new Vec4();
            final Vec4 appliedVertexHeap = new Vec4();
            final Vec4 vertexSumHeap = new Vec4();
            final Vec4 normalHeap = new Vec4();
            final Vec4 appliedNormalHeap = new Vec4();
            final Vec4 normalSumHeap = new Vec4();
            final ModelPanel modelContext = currentModelPanel();
            final RenderModel editorRenderModel = modelContext.getEditorRenderModel();
            final EditableModel model = modelContext.getModel();
            final ModelViewManager modelViewManager = modelContext.getModelViewManager();
            final EditableModel snapshotModel = EditableModel.deepClone(model, model.getHeaderName() + "At"
                    + editorRenderModel.getAnimatedRenderEnvironment().getAnimationTime());
            for (int geosetIndex = 0; geosetIndex < snapshotModel.getGeosets().size(); geosetIndex++) {
                final Geoset geoset = model.getGeoset(geosetIndex);
                final Geoset snapshotGeoset = snapshotModel.getGeoset(geosetIndex);
                for (int vertexIndex = 0; vertexIndex < geoset.getVertices().size(); vertexIndex++) {
                    final GeosetVertex vertex = geoset.getVertex(vertexIndex);
                    final GeosetVertex snapshotVertex = snapshotGeoset.getVertex(vertexIndex);
                    final List<Bone> bones = vertex.getBones();
                    vertexHeap.x = (float) vertex.x;
                    vertexHeap.y = (float) vertex.y;
                    vertexHeap.z = (float) vertex.z;
                    vertexHeap.w = 1;
                    if (bones.size() > 0) {
                        vertexSumHeap.set(0, 0, 0, 0);
                        for (final Bone bone : bones) {
                            editorRenderModel.getRenderNode(bone).getWorldMatrix().transform(vertexHeap, appliedVertexHeap);
                            vertexSumHeap.add(appliedVertexHeap);
                        }
                        final int boneCount = bones.size();
                        vertexSumHeap.x /= boneCount;
                        vertexSumHeap.y /= boneCount;
                        vertexSumHeap.z /= boneCount;
                        vertexSumHeap.w /= boneCount;
                    } else {
                        vertexSumHeap.set(vertexHeap);
                    }
                    snapshotVertex.x = vertexSumHeap.x;
                    snapshotVertex.y = vertexSumHeap.y;
                    snapshotVertex.z = vertexSumHeap.z;

                    normalHeap.x = (float) vertex.getNormal().x;
                    normalHeap.y = (float) vertex.getNormal().y;
                    normalHeap.z = (float) vertex.getNormal().z;
                    normalHeap.w = 0;
                    if (bones.size() > 0) {
                        normalSumHeap.set(0, 0, 0, 0);
                        for (final Bone bone : bones) {
                            editorRenderModel.getRenderNode(bone).getWorldMatrix().transform(normalHeap, appliedNormalHeap);
                            normalSumHeap.add(appliedNormalHeap);
                        }

                        if (normalSumHeap.length() > 0) {
                            normalSumHeap.normalize();
                        } else {
                            normalSumHeap.set(0, 1, 0, 0);
                        }
                    } else {
                        normalSumHeap.set(normalHeap);
                    }
                    snapshotVertex.getNormal().x = normalSumHeap.x;
                    snapshotVertex.getNormal().y = normalSumHeap.y;
                    snapshotVertex.getNormal().z = normalSumHeap.z;
                }
            }
            snapshotModel.getIdObjects().clear();
            final Bone boneRoot = new Bone("Bone_Root");
            boneRoot.setPivotPoint(new Vec3(0, 0, 0));
            snapshotModel.add(boneRoot);
            for (final Geoset geoset : snapshotModel.getGeosets()) {
                for (final GeosetVertex vertex : geoset.getVertices()) {
                    vertex.getBones().clear();
                    vertex.getBones().add(boneRoot);
                }
            }
            final Iterator<Geoset> geosetIterator = snapshotModel.getGeosets().iterator();
            while (geosetIterator.hasNext()) {
                final Geoset geoset = geosetIterator.next();
                final GeosetAnim geosetAnim = geoset.getGeosetAnim();
                if (geosetAnim != null) {
                    final Object visibilityValue = geosetAnim.getVisibilityFlag()
                            .interpolateAt(editorRenderModel.getAnimatedRenderEnvironment());
                    if (visibilityValue instanceof Float) {
                        final Float visibility = (Float) visibilityValue;
                        final double visvalue = visibility;
                        if (visvalue < 0.01) {
                            geosetIterator.remove();
                            snapshotModel.remove(geosetAnim);
                        }
                    }

                }
            }
            snapshotModel.getAnims().clear();
            snapshotModel.add(new Animation("Stand", 333, 1333));
            final List<AnimFlag> allAnimFlags = snapshotModel.getAllAnimFlags();
            for (final AnimFlag flag : allAnimFlags) {
                if (!flag.hasGlobalSeq()) {
                    if (flag.size() > 0) {
                        final Object value = flag.interpolateAt(animatedRenderEnvironment);
                        flag.setInterpType(InterpolationType.DONT_INTERP);
                        flag.getValues().clear();
                        flag.getTimes().clear();
                        flag.getInTans().clear();
                        flag.getOutTans().clear();
                        flag.addEntry(333, value);
                    }
                }
            }
            fc.setDialogTitle("Export Static Snapshot");
            final int result = fc.showSaveDialog(MainPanel.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fc.getSelectedFile();
                if (selectedFile != null) {
                    if (!selectedFile.getPath().toLowerCase().endsWith(".mdx")) {
                        selectedFile = new File(selectedFile.getPath() + ".mdx");
                    }
                    try {
                        MdxUtils.saveMdx(snapshotModel, selectedFile);
                    } catch (final IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }

        });
        scriptsMenu.add(exportAnimatedToStaticMesh);

        exportAnimatedFramePNG = new JMenuItem("Export Animated Frame PNG");
        exportAnimatedFramePNG.setMnemonic(KeyEvent.VK_F);
        exportAnimatedFramePNG.addActionListener(e -> {
            final BufferedImage fBufferedImage = currentModelPanel().getAnimationViewer().getBufferedImage();

            if (exportTextureDialog.getCurrentDirectory() == null) {
                final EditableModel current = currentMDL();
                if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                    fc.setCurrentDirectory(current.getFile().getParentFile());
                } else if (profile.getPath() != null) {
                    fc.setCurrentDirectory(new File(profile.getPath()));
                }
            }
            if (exportTextureDialog.getCurrentDirectory() == null) {
                exportTextureDialog.setSelectedFile(
                        new File(exportTextureDialog.getCurrentDirectory() + File.separator));
            }

            final int x = exportTextureDialog.showSaveDialog(MainPanel.this);
            if (x == JFileChooser.APPROVE_OPTION) {
                final File file = exportTextureDialog.getSelectedFile();
                if (file != null) {
                    try {
                        if (file.getName().lastIndexOf('.') >= 0) {
                            BufferedImage bufferedImage = fBufferedImage;
                            String fileExtension = file.getName().substring(file.getName().lastIndexOf('.') + 1)
                                    .toUpperCase();
                            if (fileExtension.equals("BMP") || fileExtension.equals("JPG")
                                    || fileExtension.equals("JPEG")) {
                                JOptionPane.showMessageDialog(MainPanel.this,
                                        "Warning: Alpha channel was converted to black. Some data will be lost\nif you convert this texture back to Warcraft BLP.");
                                bufferedImage = BLPHandler.removeAlphaChannel(bufferedImage);
                            }
                            if (fileExtension.equals("BLP")) {
                                fileExtension = "blp";
                            }
                            final boolean write = ImageIO.write(bufferedImage, fileExtension, file);
                            if (!write) {
                                JOptionPane.showMessageDialog(MainPanel.this, "File type unknown or unavailable");
                            }
                        } else {
                            JOptionPane.showMessageDialog(MainPanel.this, "No file type was specified");
                        }
                    } catch (final IOException e1) {
                        ExceptionPopup.display(e1);
                        e1.printStackTrace();
                    } catch (final Exception e2) {
                        ExceptionPopup.display(e2);
                        e2.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(MainPanel.this, "No output file was specified");
                }
            }
        });
        scriptsMenu.add(exportAnimatedFramePNG);

        combineAnims = new JMenuItem("Create Back2Back Animation");
        combineAnims.setMnemonic(KeyEvent.VK_P);
        combineAnims.addActionListener(e -> {
            final List<Animation> anims = currentMDL().getAnims();
            final Animation[] array = anims.toArray(new Animation[0]);
            final Object choice = JOptionPane.showInputDialog(MainPanel.this, "Pick the first animation",
                    "Choose 1st Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
            final Animation animation = (Animation) choice;

            final Object choice2 = JOptionPane.showInputDialog(MainPanel.this, "Pick the second animation",
                    "Choose 2nd Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
            final Animation animation2 = (Animation) choice2;

            final String nameChoice = JOptionPane.showInputDialog(MainPanel.this,
                    "What should the combined animation be called?");
            if (nameChoice != null) {
                final int anim1Length = animation.getEnd() - animation.getStart();
                final int anim2Length = animation2.getEnd() - animation2.getStart();
                final int totalLength = anim1Length + anim2Length;

                final EditableModel model = currentMDL();
                final int animTrackEnd = model.animTrackEnd();
                final int start = animTrackEnd + 1000;
                animation.copyToInterval(start, start + anim1Length, model.getAllAnimFlags(),
                        model.sortedIdObjects(EventObject.class));
                animation2.copyToInterval(start + anim1Length, start + totalLength, model.getAllAnimFlags(),
                        model.sortedIdObjects(EventObject.class));

                final Animation newAnimation = new Animation(nameChoice, start, start + totalLength);
                model.add(newAnimation);
                newAnimation.setNonLooping(true);
                newAnimation.setExtents(new ExtLog(animation.getExtents()));
                JOptionPane.showMessageDialog(MainPanel.this,
                        "DONE! Made a combined animation called " + newAnimation.getName(), "Success",
                        JOptionPane.PLAIN_MESSAGE);
            }
        });
        scriptsMenu.add(combineAnims);

        scaleAnimations = new JMenuItem("Change Animation Lengths by Scaling");
        scaleAnimations.setMnemonic(KeyEvent.VK_A);
        scaleAnimations.addActionListener(this);
        scriptsMenu.add(scaleAnimations);

        final JMenuItem version800Toggle = new JMenuItem("Assign FormatVersion 800");
        version800Toggle.setMnemonic(KeyEvent.VK_A);
        version800Toggle.addActionListener(e -> currentMDL().setFormatVersion(800));
        scriptsMenu.add(version800Toggle);

        final JMenuItem version1000Toggle = new JMenuItem("Assign FormatVersion 1000");
        version1000Toggle.setMnemonic(KeyEvent.VK_A);
        version1000Toggle.addActionListener(e -> currentMDL().setFormatVersion(1000));
        scriptsMenu.add(version1000Toggle);

        final JMenuItem makeItHDItem = new JMenuItem("SD -> HD (highly experimental, requires 900 or 1000)");
        makeItHDItem.setMnemonic(KeyEvent.VK_A);
        makeItHDItem.addActionListener(e -> EditableModel.makeItHD(currentMDL()));
        scriptsMenu.add(makeItHDItem);

        final JMenuItem version800EditingToggle = new JMenuItem("HD -> SD (highly experimental, becomes 800)");
        version800EditingToggle.setMnemonic(KeyEvent.VK_A);
        version800EditingToggle.addActionListener(e -> EditableModel.convertToV800(1, currentMDL()));
        scriptsMenu.add(version800EditingToggle);

        final JMenuItem recalculateTangents = new JMenuItem("Recalculate Tangents (requires 900 or 1000)");
        recalculateTangents.setMnemonic(KeyEvent.VK_A);
        recalculateTangents.addActionListener(e -> EditableModel.recalculateTangents(currentMDL(), MainPanel.this));
        scriptsMenu.add(recalculateTangents);

        final JMenuItem jokebutton = new JMenuItem("Load Retera Land");
        jokebutton.setMnemonic(KeyEvent.VK_A);
        jokebutton.addActionListener(e -> {
            final StringBuilder sb = new StringBuilder();
            for (final File file : new File(
                    "C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\CustomMapData\\LuaFpsMap\\Maps\\MultiplayerFun004")
                    .listFiles()) {
                if (!file.getName().toLowerCase().endsWith("_init.txt")) {
                    sb.setLength(0);
                    try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.contains("BlzSetAbilityActivatedIcon")) {
                                final int startIndex = line.indexOf('"') + 1;
                                final int endIndex = line.lastIndexOf('"');
                                final String dataString = line.substring(startIndex, endIndex);
                                sb.append(dataString);
                            }
                        }
                    } catch (final FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (final IOException e1) {
                        e1.printStackTrace();
                    }
                    final String dataString = sb.toString();
                    for (int i = 0; (i + 23) < dataString.length(); i += 24) {
                        final Geoset geo = new Geoset();
                        currentMDL().addGeoset(geo);
                        geo.setParentModel(currentMDL());
                        geo.setMaterial(new Material(new Layer("Blend", new Bitmap("textures\\white.blp"))));
                        final String data = dataString.substring(i, i + 24);
                        final int x = Integer.parseInt(data.substring(0, 3));
                        final int y = Integer.parseInt(data.substring(3, 6));
                        final int z = Integer.parseInt(data.substring(6, 9));
                        final int sX = Integer.parseInt(data.substring(9, 10));
                        final int sY = Integer.parseInt(data.substring(10, 11));
                        final int sZ = Integer.parseInt(data.substring(11, 12));
                        final int red = Integer.parseInt(data.substring(12, 15));
                        final int green = Integer.parseInt(data.substring(15, 18));
                        final int blue = Integer.parseInt(data.substring(18, 21));
                        final int alpha = Integer.parseInt(data.substring(21, 24));
                        final GeosetAnim forceGetGeosetAnim = geo.forceGetGeosetAnim();
                        forceGetGeosetAnim.setStaticColor(new Vec3(blue / 255.0, green / 255.0, red / 255.0));
                        forceGetGeosetAnim.setStaticAlpha(alpha / 255.0);
                        System.out.println(x + "," + y + "," + z);

                        final Mesh mesh = ModelUtils.createBox(new Vec3(x * 10, y * 10, z * 10),
                                new Vec3((x * 10) + (sX * 10), (y * 10) + (sY * 10), (z * 10) + (sZ * 10)), 1, 1,
                                1, geo);
                        geo.getVertices().addAll(mesh.getVertices());
                        geo.getTriangles().addAll(mesh.getTriangles());
                    }
                }

            }
            modelStructureChangeListener.geosetsAdded(new ArrayList<>(currentMDL().getGeosets()));
        });
//		scriptsMenu.add(jokebutton);

        final JMenuItem fixReteraLand = new JMenuItem("Fix Retera Land");
        fixReteraLand.setMnemonic(KeyEvent.VK_A);
        fixReteraLand.addActionListener(e -> {
            final EditableModel currentMDL = currentMDL();
            for (final Geoset geo : currentMDL.getGeosets()) {
                final Animation anim = new Animation(new ExtLog(currentMDL.getExtents()));
                geo.add(anim);
            }
        });
//		scriptsMenu.add(fixReteraLand);

        aboutMenu = new JMenu("Help");
        aboutMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(aboutMenu);

        recentMenu.add(new JSeparator());

        clearRecent = new JMenuItem("Clear");
        clearRecent.setMnemonic(KeyEvent.VK_C);
        clearRecent.addActionListener(this);
        recentMenu.add(clearRecent);

        updateRecent();

        changelogButton = new JMenuItem("Changelog");
        changelogButton.setMnemonic(KeyEvent.VK_A);
        changelogButton.addActionListener(this);
        aboutMenu.add(changelogButton);

        creditsButton = new JMenuItem("About");
        creditsButton.setMnemonic(KeyEvent.VK_A);
        creditsButton.addActionListener(this);
        aboutMenu.add(creditsButton);

        jokeButton = new JMenuItem("HTML Magic");
        jokeButton.setMnemonic(KeyEvent.VK_H);
        jokeButton.addActionListener(e -> {
            final JEditorPane jEditorPane;
            try {
                jEditorPane = new JEditorPane(new URL("http://79.179.129.227:8080/clients/editor/"));
                final JFrame testFrame = new JFrame("Test");
                testFrame.setContentPane(jEditorPane);
                testFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                testFrame.pack();
                testFrame.setLocationRelativeTo(nullmodelButton);
                testFrame.setVisible(true);
            } catch (final MalformedURLException e1) {
                e1.printStackTrace();
            } catch (final IOException e1) {
                e1.printStackTrace();
            }
        });
        aboutMenu.add(jokeButton);

        showMatrices = new JMenuItem("View Selected \"Matrices\"");
        // showMatrices.setMnemonic(KeyEvent.VK_V);
        showMatrices.addActionListener(viewMatricesAction);
        toolsMenu.add(showMatrices);

        insideOut = new JMenuItem("Flip all selected faces");
        insideOut.setMnemonic(KeyEvent.VK_I);
        insideOut.addActionListener(insideOutAction);
        insideOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));
        toolsMenu.add(insideOut);

        insideOutNormals = new JMenuItem("Flip all selected normals");
        insideOutNormals.addActionListener(insideOutNormalsAction);
        toolsMenu.add(insideOutNormals);

        toolsMenu.add(new JSeparator());

        editUVs = new JMenuItem("Edit UV Mapping");
        editUVs.setMnemonic(KeyEvent.VK_U);
        editUVs.addActionListener(this);
        toolsMenu.add(editUVs);

        editTextures = new JMenuItem("Edit Textures");
        editTextures.setMnemonic(KeyEvent.VK_T);
        editTextures.addActionListener(e -> {
            final EditTexturesPopupPanel textureManager = new EditTexturesPopupPanel(currentModelPanel().getModelViewManager(),
                    modelStructureChangeListener, textureExporter);
            final JFrame frame = new JFrame("Edit Textures");
            textureManager.setSize(new Dimension(800, 650));
            frame.setContentPane(textureManager);
            frame.setSize(textureManager.getSize());
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
        });
        toolsMenu.add(editTextures);

        rigButton = new JMenuItem("Rig Selection");
        rigButton.setMnemonic(KeyEvent.VK_R);
        rigButton.setAccelerator(KeyStroke.getKeyStroke("control W"));
        rigButton.addActionListener(rigAction);
        toolsMenu.add(rigButton);

        tweaksSubmenu = new JMenu("Tweaks");
        tweaksSubmenu.setMnemonic(KeyEvent.VK_T);
        tweaksSubmenu.getAccessibleContext()
                .setAccessibleDescription("Allows the user to tweak conversion mistakes.");
        toolsMenu.add(tweaksSubmenu);

        flipAllUVsU = new JMenuItem("Flip All UVs U");
        flipAllUVsU.setMnemonic(KeyEvent.VK_U);
        flipAllUVsU.addActionListener(flipAllUVsUAction);
        tweaksSubmenu.add(flipAllUVsU);

        flipAllUVsV = new JMenuItem("Flip All UVs V");
        // flipAllUVsV.setMnemonic(KeyEvent.VK_V);
        flipAllUVsV.addActionListener(flipAllUVsVAction);
        tweaksSubmenu.add(flipAllUVsV);

        inverseAllUVs = new JMenuItem("Swap All UVs U for V");
        inverseAllUVs.setMnemonic(KeyEvent.VK_S);
        inverseAllUVs.addActionListener(inverseAllUVsAction);
        tweaksSubmenu.add(inverseAllUVs);

        mirrorSubmenu = new JMenu("Mirror");
        mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
        mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");
        toolsMenu.add(mirrorSubmenu);

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
//		viewMenu.add(newDirectory);

        viewMenu.add(new JSeparator());

        showVertexModifyControls = new JCheckBoxMenuItem("Show Viewport Buttons", true);
        // showVertexModifyControls.setMnemonic(KeyEvent.VK_V);
        showVertexModifyControls.addActionListener(this);
        viewMenu.add(showVertexModifyControls);

        viewMenu.add(new JSeparator());

        showNormals = new JCheckBoxMenuItem("Show Normals", true);
        showNormals.setMnemonic(KeyEvent.VK_N);
        showNormals.setSelected(false);
        showNormals.addActionListener(this);
        viewMenu.add(showNormals);

        viewMode = new JMenu("3D View Mode");
        viewMenu.add(viewMode);

        viewModes = new ButtonGroup();

        final ActionListener repainter = e -> {
            if (wireframe.isSelected()) {
                prefs.setViewMode(0);
            } else if (solid.isSelected()) {
                prefs.setViewMode(1);
            } else {
                prefs.setViewMode(-1);
            }
            repaint();
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

        fileMenu.add(recentMenu);

        fetch = new JMenu("Open Internal");
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
        fetchPortraitsToo.addActionListener(e -> prefs.setLoadPortraits(fetchPortraitsToo.isSelected()));
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
        importUnit.setAccelerator(KeyStroke.getKeyStroke("control shift U"));
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

        exportTextures = new JMenuItem("Export Texture");
        exportTextures.setMnemonic(KeyEvent.VK_E);
        exportTextures.addActionListener(this);
        fileMenu.add(exportTextures);

        fileMenu.add(new JSeparator());

        revert = new JMenuItem("Revert");
        revert.addActionListener(e -> {
            final ModelPanel modelPanel = currentModelPanel();
            final int oldIndex = modelPanels.indexOf(modelPanel);
            if (modelPanel != null) {
                if (modelPanel.close(MainPanel.this)) {
                    modelPanels.remove(modelPanel);
                    windowMenu.remove(modelPanel.getMenuItem());
                    if (modelPanels.size() > 0) {
                        final int newIndex = Math.min(modelPanels.size() - 1, oldIndex);
                        setCurrentModel(modelPanels.get(newIndex));
                    } else {
                        // TODO remove from notifiers to fix leaks
                        setCurrentModel(null);
                    }
                    final File fileToRevert = modelPanel.getModel().getFile();
                    loadFile(fileToRevert);
                }
            }
        });
        fileMenu.add(revert);

        close = new JMenuItem("Close");
        close.setAccelerator(KeyStroke.getKeyStroke("control E"));
        close.setMnemonic(KeyEvent.VK_E);
        close.addActionListener(this);
        fileMenu.add(close);

        fileMenu.add(new JSeparator());

        exit = new JMenuItem("Exit");
        exit.setMnemonic(KeyEvent.VK_E);
        exit.addActionListener(e -> {
            if (closeAll()) {
                MainFrame.frame.dispose();
            }
        });
        fileMenu.add(exit);

        undo = new UndoMenuItem("Undo");
        undo.addActionListener(undoAction);
        undo.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        // undo.addMouseListener(this);
        editMenu.add(undo);
        undo.setEnabled(undo.funcEnabled());

        redo = new RedoMenuItem("Redo");
        redo.addActionListener(redoAction);
        redo.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        // redo.addMouseListener(this);
        editMenu.add(redo);
        redo.setEnabled(redo.funcEnabled());

        editMenu.add(new JSeparator());

        final JMenu optimizeMenu = new JMenu("Optimize");
        optimizeMenu.setMnemonic(KeyEvent.VK_O);
        editMenu.add(optimizeMenu);

        linearizeAnimations = new JMenuItem("Linearize Animations");
        linearizeAnimations.setMnemonic(KeyEvent.VK_L);
        linearizeAnimations.addActionListener(this);
        optimizeMenu.add(linearizeAnimations);

        simplifyKeyframes = new JMenuItem("Simplify Keyframes (Experimental)");
        simplifyKeyframes.setMnemonic(KeyEvent.VK_K);
        simplifyKeyframes.addActionListener(this);
        optimizeMenu.add(simplifyKeyframes);

        final JMenuItem minimizeGeoset = new JMenuItem("Minimize Geosets");
        minimizeGeoset.setMnemonic(KeyEvent.VK_K);
        minimizeGeoset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int confirm = JOptionPane.showConfirmDialog(MainPanel.this,
                        "This is experimental and I did not code the Undo option for it yet. Continue?\nMy advice is to click cancel and save once first.",
                        "Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.OK_OPTION) {
                    return;
                }

                currentMDL().doSavePreps();

                final Map<Geoset, Geoset> sourceToDestination = new HashMap<>();
                final List<Geoset> retainedGeosets = new ArrayList<>();
                for (final Geoset geoset : currentMDL().getGeosets()) {
                    boolean alreadyRetained = false;
                    for (final Geoset retainedGeoset : retainedGeosets) {
                        if (retainedGeoset.getMaterial().equals(geoset.getMaterial())
                                && (retainedGeoset.getSelectionGroup() == geoset.getSelectionGroup())
                                && (retainedGeoset.getUnselectable() == geoset.getUnselectable())
                                && mergableGeosetAnims(retainedGeoset.getGeosetAnim(), geoset.getGeosetAnim())) {
                            alreadyRetained = true;
                            for (final GeosetVertex gv : geoset.getVertices()) {
                                retainedGeoset.add(gv);
                            }
                            for (final Triangle t : geoset.getTriangles()) {
                                retainedGeoset.add(t);
                            }
                            break;
                        }
                    }
                    if (!alreadyRetained) {
                        retainedGeosets.add(geoset);
                    }
                }
                final EditableModel currentMDL = currentMDL();
                final List<Geoset> geosets = currentMDL.getGeosets();
                final List<Geoset> geosetsRemoved = new ArrayList<>();
                final Iterator<Geoset> iterator = geosets.iterator();
                while (iterator.hasNext()) {
                    final Geoset geoset = iterator.next();
                    if (!retainedGeosets.contains(geoset)) {
                        iterator.remove();
                        final GeosetAnim geosetAnim = geoset.getGeosetAnim();
                        if (geosetAnim != null) {
                            currentMDL.remove(geosetAnim);
                        }
                        geosetsRemoved.add(geoset);
                    }
                }
                modelStructureChangeListener.geosetsRemoved(geosetsRemoved);
            }

            private boolean mergableGeosetAnims(final GeosetAnim first, final GeosetAnim second) {
                if ((first == null) && (second == null)) {
                    return true;
                }
                if ((first == null) || (second == null)) {
                    return false;
                }
                final AnimFlag firstVisibilityFlag = first.getVisibilityFlag();
                final AnimFlag secondVisibilityFlag = second.getVisibilityFlag();
                if ((firstVisibilityFlag == null) != (secondVisibilityFlag == null)) {
                    return false;
                }
                if ((firstVisibilityFlag != null) && !firstVisibilityFlag.equals(secondVisibilityFlag)) {
                    return false;
                }
                if (first.isDropShadow() != second.isDropShadow()) {
                    return false;
                }
                if (Math.abs(first.getStaticAlpha() - second.getStaticAlpha()) > 0.001) {
                    return false;
                }
                if ((first.getStaticColor() == null) != (second.getStaticColor() == null)) {
                    return false;
                }
                if ((first.getStaticColor() != null) && !first.getStaticColor().equalLocs(second.getStaticColor())) {
                    return false;
                }
                final AnimFlag firstAnimatedColor = first.find("Color");
                final AnimFlag secondAnimatedColor = second.find("Color");
                if ((firstAnimatedColor == null) != (secondAnimatedColor == null)) {
                    return false;
                }
                return (firstAnimatedColor == null) || firstAnimatedColor.equals(secondAnimatedColor);
            }
        });
        optimizeMenu.add(minimizeGeoset);

        sortBones = new JMenuItem("Sort Nodes");
        sortBones.setMnemonic(KeyEvent.VK_S);
        sortBones.addActionListener(e -> {
            final EditableModel model = currentMDL();
            final List<IdObject> roots = new ArrayList<>();
            final List<IdObject> modelList = model.getIdObjects();
            for (final IdObject object : modelList) {
                if (object.getParent() == null) {
                    roots.add(object);
                }
            }
            final Queue<IdObject> bfsQueue = new LinkedList<>(roots);
            final List<IdObject> result = new ArrayList<>();
            while (!bfsQueue.isEmpty()) {
                final IdObject nextItem = bfsQueue.poll();
                bfsQueue.addAll(nextItem.getChildrenNodes());
                result.add(nextItem);
            }
            for (final IdObject node : result) {
                model.remove(node);
            }
            modelStructureChangeListener.nodesRemoved(result);
            for (final IdObject node : result) {
                model.add(node);
            }
            modelStructureChangeListener.nodesAdded(result);
        });
        optimizeMenu.add(sortBones);

        final JMenuItem flushUnusedTexture = new JMenuItem("Flush Unused Texture");
        flushUnusedTexture.setEnabled(false);
        flushUnusedTexture.setMnemonic(KeyEvent.VK_F);
        optimizeMenu.add(flushUnusedTexture);

        final JMenuItem recalcNormals = new JMenuItem("Recalculate Normals");
        recalcNormals.setAccelerator(KeyStroke.getKeyStroke("control N"));
        recalcNormals.addActionListener(recalcNormalsAction);
        editMenu.add(recalcNormals);

        final JMenuItem recalcExtents = new JMenuItem("Recalculate Extents");
        recalcExtents.setAccelerator(KeyStroke.getKeyStroke("control shift E"));
        recalcExtents.addActionListener(recalcExtentsAction);
        editMenu.add(recalcExtents);

        editMenu.add(new JSeparator());

        final TransferActionListener transferActionListener = new TransferActionListener();
        final ActionListener copyActionListener = e -> {
            if (!animationModeState) {
                transferActionListener.actionPerformed(e);
            } else {
                if (e.getActionCommand().equals(TransferHandler.getCutAction().getValue(Action.NAME))) {
                    timeSliderPanel.cut();
                } else if (e.getActionCommand().equals(TransferHandler.getCopyAction().getValue(Action.NAME))) {
                    timeSliderPanel.copy();
                } else if (e.getActionCommand().equals(TransferHandler.getPasteAction().getValue(Action.NAME))) {
                    timeSliderPanel.paste();
                }
            }
        };
        cut = new JMenuItem("Cut");
        cut.addActionListener(copyActionListener);
        cut.setActionCommand((String) TransferHandler.getCutAction().getValue(Action.NAME));
        cut.setAccelerator(KeyStroke.getKeyStroke("control X"));
        editMenu.add(cut);

        copy = new JMenuItem("Copy");
        copy.addActionListener(copyActionListener);
        copy.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));
        copy.setAccelerator(KeyStroke.getKeyStroke("control C"));
        editMenu.add(copy);

        paste = new JMenuItem("Paste");
        paste.addActionListener(copyActionListener);
        paste.setActionCommand((String) TransferHandler.getPasteAction().getValue(Action.NAME));
        paste.setAccelerator(KeyStroke.getKeyStroke("control V"));
        editMenu.add(paste);

        duplicateSelection = new JMenuItem("Duplicate");
        // divideVertices.setMnemonic(KeyEvent.VK_V);
        duplicateSelection.setAccelerator(KeyStroke.getKeyStroke("control D"));
        duplicateSelection.addActionListener(cloneAction);
        editMenu.add(duplicateSelection);

        editMenu.add(new JSeparator());

        snapVertices = new JMenuItem("Snap Vertices");
        snapVertices.setAccelerator(KeyStroke.getKeyStroke("control shift W"));
        snapVertices.addActionListener(snapVerticesAction);
        editMenu.add(snapVertices);

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

        editMenu.addSeparator();

        final JMenuItem deleteButton = new JMenuItem("Delete");
        deleteButton.setMnemonic(KeyEvent.VK_D);
        deleteButton.addActionListener(deleteAction);
        editMenu.add(deleteButton);

        editMenu.addSeparator();

        preferencesWindow = new JMenuItem("Preferences Window");
        preferencesWindow.setMnemonic(KeyEvent.VK_P);
        preferencesWindow.addActionListener(openPreferencesAction);
        editMenu.add(preferencesWindow);

        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            menuBar.getMenu(i).getPopupMenu().setLightWeightPopupEnabled(false);
        }
        return menuBar;
    }

    private void createTeamColorMenuItems() {
        for (int i = 0; i < 25; i++) {
            final String colorNumber = String.format("%2s", i).replace(' ', '0');
            try {
                final String colorName = WEString.getString("WESTRING_UNITCOLOR_" + colorNumber);
                final JMenuItem menuItem = new JMenuItem(colorName, new ImageIcon(BLPHandler.get()
                        .getGameTex("ReplaceableTextures\\TeamColor\\TeamColor" + colorNumber + ".blp")));
                teamColorMenu.add(menuItem);
                final int teamColorValueNumber = i;
                menuItem.addActionListener(e -> {
                    Material.teamColor = teamColorValueNumber;
                    final ModelPanel modelPanel = currentModelPanel();
                    if (modelPanel != null) {
                        modelPanel.getAnimationViewer().reloadAllTextures();
                        modelPanel.getPerspArea().reloadAllTextures();

                        reloadComponentBrowser(modelPanel);
                    }
                    profile.getPreferences().setTeamColor(teamColorValueNumber);
                });
            } catch (final Exception ex) {
                // load failed
                break;
            }
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        // Open, off of the file menu:
        refreshUndo();
        try {
            if (e.getSource() == newModel) {
                newModel();
            } else if (e.getSource() == open) {
                onClickOpen();
            } else if (e.getSource() == close) {
                final ModelPanel modelPanel = currentModelPanel();
                final int oldIndex = modelPanels.indexOf(modelPanel);
                if (modelPanel != null) {
                    if (modelPanel.close(this)) {
                        modelPanels.remove(modelPanel);
                        windowMenu.remove(modelPanel.getMenuItem());
                        if (modelPanels.size() > 0) {
                            final int newIndex = Math.min(modelPanels.size() - 1, oldIndex);
                            setCurrentModel(modelPanels.get(newIndex));
                        } else {
                            // TODO remove from notifiers to fix leaks
                            setCurrentModel(null);
                        }
                    }
                }
            } else if (e.getSource() == fetchUnit) {
                final GameObject unitFetched = fetchUnit();
                if (unitFetched != null) {
                    final String filepath = convertPathToMDX(unitFetched.getField("file"));
                    if (filepath != null) {
                        loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(filepath), true, true,
                                unitFetched.getScaledIcon(0.25f));
                        final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
                                + filepath.substring(filepath.lastIndexOf('.'));
                        if (prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
                            loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false,
                                    unitFetched.getScaledIcon(0.25f));
                        }
                        toolsMenu.getAccessibleContext().setAccessibleDescription(
                                "Allows the user to control which parts of the model are displayed for editing.");
                        toolsMenu.setEnabled(true);
                    }
                }
            } else if (e.getSource() == fetchModel) {
                final ModelElement model = fetchModel();
                if (model != null) {
                    final String filepath = convertPathToMDX(model.getFilepath());
                    if (filepath != null) {

                        final ImageIcon icon = model.hasCachedIconPath() ? new ImageIcon(BLPHandler.get()
                                .getGameTex(model.getCachedIconPath()).getScaledInstance(16, 16, Image.SCALE_FAST))
                                : MDLIcon;
                        loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(filepath), true, true, icon);
                        final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
                                + filepath.substring(filepath.lastIndexOf('.'));
                        if (prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
                            loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false, icon);
                        }
                        toolsMenu.getAccessibleContext().setAccessibleDescription(
                                "Allows the user to control which parts of the model are displayed for editing.");
                        toolsMenu.setEnabled(true);
                    }
                }
            } else if (e.getSource() == fetchObject) {
                final MutableGameObject objectFetched = fetchObject();
                if (objectFetched != null) {
                    final String filepath = convertPathToMDX(objectFetched.getFieldAsString(UnitFields.MODEL_FILE, 0));
                    if (filepath != null) {
                        loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(filepath), true, true,
                                new ImageIcon(BLPHandler.get()
                                        .getGameTex(objectFetched.getFieldAsString(UnitFields.INTERFACE_ICON, 0))
                                        .getScaledInstance(16, 16, Image.SCALE_FAST)));
                        final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
                                + filepath.substring(filepath.lastIndexOf('.'));
                        if (prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
                            loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false,
                                    new ImageIcon(BLPHandler.get()
                                            .getGameTex(objectFetched.getFieldAsString(UnitFields.INTERFACE_ICON, 0))
                                            .getScaledInstance(16, 16, Image.SCALE_FAST)));
                        }
                        toolsMenu.getAccessibleContext().setAccessibleDescription(
                                "Allows the user to control which parts of the model are displayed for editing.");
                        toolsMenu.setEnabled(true);
                    }
                }
            } else if (e.getSource() == importButton) {
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
                    importFile(currentFile);
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
                refreshController();
            } else if (e.getSource() == importUnit) {
                final GameObject fetchUnitResult = fetchUnit();
                if (fetchUnitResult == null) {
                    return;
                }
                final String filepath = convertPathToMDX(fetchUnitResult.getField("file"));
                final EditableModel current = currentMDL();
                if (filepath != null) {
                    final File animationSource = GameDataFileSystem.getDefault().getFile(filepath);
                    importFile(animationSource);
                }
                refreshController();
            } else if (e.getSource() == importGameModel) {
                final ModelElement fetchModelResult = fetchModel();
                if (fetchModelResult == null) {
                    return;
                }
                final String filepath = convertPathToMDX(fetchModelResult.getFilepath());
                final EditableModel current = currentMDL();
                if (filepath != null) {
                    final File animationSource = GameDataFileSystem.getDefault().getFile(filepath);
                    importFile(animationSource);
                }
                refreshController();
            } else if (e.getSource() == importGameObject) {
                final MutableGameObject fetchObjectResult = fetchObject();
                if (fetchObjectResult == null) {
                    return;
                }
                final String filepath = convertPathToMDX(fetchObjectResult.getFieldAsString(UnitFields.MODEL_FILE, 0));
                final EditableModel current = currentMDL();
                if (filepath != null) {
                    final File animationSource = GameDataFileSystem.getDefault().getFile(filepath);
                    importFile(animationSource);
                }
                refreshController();
            } else if (e.getSource() == importFromWorkspace) {
                final List<EditableModel> optionNames = new ArrayList<>();
                for (final ModelPanel modelPanel : modelPanels) {
                    final EditableModel model = modelPanel.getModel();
                    optionNames.add(model);
                }
                final EditableModel choice = (EditableModel) JOptionPane.showInputDialog(this,
                        "Choose a workspace item to import data from:", "Import from Workspace",
                        JOptionPane.OK_CANCEL_OPTION, null, optionNames.toArray(), optionNames.get(0));
                if (choice != null) {
                    importFile(EditableModel.deepClone(choice, choice.getHeaderName()));
                }
                refreshController();
            } else if (e.getSource() == importButtonS) {
                final JFrame frame = new JFrame("Animation Transferer");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setContentPane(new AnimationTransfer(frame));
                frame.setIconImage(MainPanel.AnimIcon.getImage());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } else if (e.getSource() == mergeGeoset) {
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
                final int dialogResult = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to clear the Recent history?", "Confirm Clear",
                        JOptionPane.YES_NO_OPTION);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    SaveProfile.get().clearRecent();
                    updateRecent();
                }
            } else if (e.getSource() == nullmodelButton) {
                nullmodelFile();
                refreshController();
            } else if ((e.getSource() == save) && (currentMDL() != null) && (currentMDL().getFile() != null)) {
                onClickSave();
            } else if (e.getSource() == saveAs) {
                if (!onClickSaveAs()) {
                    return;
                }
                // } else if (e.getSource() == contextClose) {
                // if (((ModelPanel) tabbedPane.getComponentAt(contextClickedTab)).close()) {//
                // this);
                // tabbedPane.remove(contextClickedTab);
                // }
            } else if (e.getSource() == contextCloseAll) {
                closeAll();
            } else if (e.getSource() == contextCloseOthers) {
                closeOthers(currentModelPanel);
            } else if (e.getSource() == showVertexModifyControls) {
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
            } else if (e.getSource() == textureModels) {
                prefs.setTextureModels(textureModels.isSelected());
            } else if (e.getSource() == showNormals) {
                prefs.setShowNormals(showNormals.isSelected());
            } else if (e.getSource() == editUVs) {
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
            } else if (e.getSource() == exportTextures) {
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
            } else if (e.getSource() == duplicateSelection) {
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
            } else if (e.getSource() == simplifyKeyframes) {
                final int x = JOptionPane.showConfirmDialog(this,
                        "This is an irreversible process that will lose some of your model data,\nin exchange for making it a smaller storage size.\n\nContinue and simplify keyframes?",
                        "Warning: Simplify Keyframes", JOptionPane.OK_CANCEL_OPTION);
                if (x == JOptionPane.OK_OPTION) {
                    simplifyKeyframes();
                }
            } else if (e.getSource() == riseFallBirth) {
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
            } else if (e.getSource() == animFromFile) {
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
                    addSingleAnimation(current, animationSourceModel);
                }

                fc.setSelectedFile(null);

                refreshController();
            } else if (e.getSource() == animFromUnit) {
                fc.setDialogTitle("Animation Source");
                final GameObject fetchResult = fetchUnit();
                if (fetchResult == null) {
                    return;
                }
                final String filepath = convertPathToMDX(fetchResult.getField("file"));
                final EditableModel current = currentMDL();
                if (filepath != null) {
                    final EditableModel animationSource = MdxUtils.loadEditable(GameDataFileSystem.getDefault().getFile(filepath));
                    addSingleAnimation(current, animationSource);
                }
            } else if (e.getSource() == animFromModel) {
                fc.setDialogTitle("Animation Source");
                final ModelElement fetchResult = fetchModel();
                if (fetchResult == null) {
                    return;
                }
                final String filepath = convertPathToMDX(fetchResult.getFilepath());
                final EditableModel current = currentMDL();
                if (filepath != null) {
                    final EditableModel animationSource = MdxUtils.loadEditable(GameDataFileSystem.getDefault().getFile(filepath));
                    addSingleAnimation(current, animationSource);
                }
            } else if (e.getSource() == animFromObject) {
                fc.setDialogTitle("Animation Source");
                final MutableGameObject fetchResult = fetchObject();
                if (fetchResult == null) {
                    return;
                }
                final String filepath = convertPathToMDX(fetchResult.getFieldAsString(UnitFields.MODEL_FILE, 0));
                final EditableModel current = currentMDL();
                if (filepath != null) {
                    final EditableModel animationSource = MdxUtils.loadEditable(GameDataFileSystem.getDefault().getFile(filepath));
                    addSingleAnimation(current, animationSource);
                }
            } else if (e.getSource() == creditsButton) {
                final DefaultStyledDocument panel = new DefaultStyledDocument();
                final JTextPane epane = new JTextPane();
                epane.setForeground(Color.BLACK);
                epane.setBackground(Color.WHITE);
                final RTFEditorKit rtfk = new RTFEditorKit();
                try {
                    rtfk.read(GameDataFileSystem.getDefault().getResourceAsStream("docs/credits.rtf"), panel, 0);
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
            } else if (e.getSource() == changelogButton) {
                final DefaultStyledDocument panel = new DefaultStyledDocument();
                final JTextPane epane = new JTextPane();
                epane.setForeground(Color.BLACK);
                epane.setBackground(Color.WHITE);
                final RTFEditorKit rtfk = new RTFEditorKit();
                try {
                    rtfk.read(GameDataFileSystem.getDefault().getResourceAsStream("docs/changelist.rtf"), panel, 0);
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
                final JFrame frame = new JFrame("Changelog");
                frame.setContentPane(new JScrollPane(epane));
                frame.setSize(650, 500);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
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
        } catch (

                final Exception exc) {
            ExceptionPopup.display(exc);
        }
    }

    private void dataSourcesChanged() {
        for (final ModelPanel modelPanel : modelPanels) {
            final PerspDisplayPanel pdp = modelPanel.getPerspArea();
            pdp.reloadAllTextures();
            modelPanel.getAnimationViewer().reloadAllTextures();
        }
        directoryChangeNotifier.dataSourcesChanged();
    }

    private void simplifyKeyframes() {
        final EditableModel currentMDL = currentMDL();
        currentMDL.simplifyKeyframes();
    }

    private boolean onClickSaveAs() {
        final EditableModel current = currentMDL();
        return onClickSaveAs(current);
    }

    private boolean onClickSaveAs(final EditableModel current) {
        try {
            fc.setDialogTitle("Save as");
            if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                fc.setCurrentDirectory(current.getFile().getParentFile());
                fc.setSelectedFile(current.getFile());
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
                        if (!name.substring(name.lastIndexOf('.')).equals(ext)) {
                            temp = new File(
                                    temp.getAbsolutePath().substring(0, temp.getAbsolutePath().lastIndexOf('.')) + ext);
                        }
                    } else {
                        temp = new File(temp.getAbsolutePath() + ext);
                    }
                    currentFile = temp;
                    if (temp.exists()) {
                        final Object[] options = {"Overwrite", "Cancel"};
                        final int n = JOptionPane.showOptionDialog(MainFrame.frame, "Selected file already exists.",
                                "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
                                options[1]);
                        if (n == 1) {
                            fc.setSelectedFile(null);
                            return false;
                        }
                    }
                    profile.setPath(currentFile.getParent());

                    final MdlxModel mdlx = currentMDL().toMdlx();
                    final FileOutputStream stream = new FileOutputStream(currentFile);

                    if (ext.equals(".mdl")) {
                        MdxUtils.saveMdl(mdlx, stream);
                    } else {
                        MdxUtils.saveMdx(mdlx, stream);
                    }
                    currentMDL().setFileRef(currentFile);
                    // currentMDLDisp().resetBeenSaved();
                    // TODO reset been saved
                    currentModelPanel().getMenuItem().setName(currentFile.getName().split("\\.")[0]);
                    currentModelPanel().getMenuItem().setToolTipText(currentFile.getPath());
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
                MdxUtils.saveMdx(currentMDL(), currentMDL().getFile());
                profile.setPath(currentMDL().getFile().getParent());
                // currentMDLDisp().resetBeenSaved();
                // TODO reset been saved
            }
        } catch (final Exception exc) {
            ExceptionPopup.display(exc);
        }
        refreshController();
    }

    private void onClickOpen() {
        fc.setDialogTitle("Open");
        final EditableModel current = currentMDL();
        if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
            fc.setCurrentDirectory(current.getFile().getParentFile());
        } else if (profile.getPath() != null) {
            fc.setCurrentDirectory(new File(profile.getPath()));
        }

        final int returnValue = fc.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            openFile(fc.getSelectedFile());
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
        final JPanel newModelPanel = new JPanel();
        newModelPanel.setLayout(new MigLayout());
        newModelPanel.add(new JLabel("Model Name: "), "cell 0 0");
        final JTextField newModelNameField = new JTextField("MrNew", 25);
        newModelPanel.add(newModelNameField, "cell 1 0");
        final JRadioButton createEmptyButton = new JRadioButton("Create Empty", true);
        newModelPanel.add(createEmptyButton, "cell 0 1");
        final JRadioButton createPlaneButton = new JRadioButton("Create Plane");
        newModelPanel.add(createPlaneButton, "cell 0 2");
        final JRadioButton createBoxButton = new JRadioButton("Create Box");
        newModelPanel.add(createBoxButton, "cell 0 3");
        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(createBoxButton);
        buttonGroup.add(createPlaneButton);
        buttonGroup.add(createEmptyButton);

        final int userDialogResult = JOptionPane.showConfirmDialog(this, newModelPanel, "New Model",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (userDialogResult == JOptionPane.OK_OPTION) {
            final EditableModel mdl = new EditableModel(newModelNameField.getText());
            if (createBoxButton.isSelected()) {
                final SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
                final JSpinner spinner = new JSpinner(sModel);
                final int userChoice = JOptionPane.showConfirmDialog(this, spinner, "Box: Choose Segments",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (userChoice != JOptionPane.OK_OPTION) {
                    return;
                }
                ModelUtils.createBox(mdl, new Vec3(64, 64, 128), new Vec3(-64, -64, 0),
                        ((Number) spinner.getValue()).intValue());
            } else if (createPlaneButton.isSelected()) {
                final SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
                final JSpinner spinner = new JSpinner(sModel);
                final int userChoice = JOptionPane.showConfirmDialog(this, spinner, "Plane: Choose Segments",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (userChoice != JOptionPane.OK_OPTION) {
                    return;
                }
                ModelUtils.createGroundPlane(mdl, new Vec3(64, 64, 0), new Vec3(-64, -64, 0),
                        ((Number) spinner.getValue()).intValue());
            }
            final ModelPanel temp = new ModelPanel(this, mdl, prefs, MainPanel.this, selectionItemTypeGroup,
                    selectionModeGroup, modelStructureChangeListener, coordDisplayListener,
                    viewportTransferHandler, activeViewportWatcher, RMSIcons.MDLIcon, false,
                    textureExporter);
            loadModel(true, true, temp);
        }

    }

    private GameObject fetchUnit() {
        final GameObject choice = UnitOptionPane.show(this);
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
        if (model == null) {
            return null;
        }
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

    private MutableGameObject fetchObject() {
        final BetterUnitEditorModelSelector selector = new BetterUnitEditorModelSelector(getUnitData(),
                getUnitEditorSettings());
        final int x = JOptionPane.showConfirmDialog(this, selector, "Object Editor - Select Unit",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        final MutableGameObject choice = selector.getSelection();
        if ((choice == null) || (x != JOptionPane.OK_OPTION)) {
            return null;
        }

        String filepath = choice.getFieldAsString(UnitFields.MODEL_FILE, 0);

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

    private void addSingleAnimation(final EditableModel current, final EditableModel animationSourceModel) {
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
        modelStructureChangeListener.animationsAdded(animationsAdded);
    }

    private interface OpenViewGetter {
        View getView();
    }

    private final class OpenViewAction extends AbstractAction {
        private final OpenViewGetter openViewGetter;

        private OpenViewAction(final String name, final OpenViewGetter openViewGetter) {
            super(name);
            this.openViewGetter = openViewGetter;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final View view = openViewGetter.getView();
            if ((view.getTopLevelAncestor() == null) || !view.getTopLevelAncestor().isVisible()) {
                final FloatingWindow createFloatingWindow = rootWindow
                        .createFloatingWindow(rootWindow.getLocation(), new Dimension(640, 480), view);
                createFloatingWindow.getTopLevelAncestor().setVisible(true);
            }
        }
    }

    private interface ModelReference {
        EditableModel getModel();
    }

    private final class ModelStructureChangeListenerImplementation implements ModelStructureChangeListener {
        private final ModelReference modelReference;

        public ModelStructureChangeListenerImplementation(final ModelReference modelReference) {
            this.modelReference = modelReference;
        }

        public ModelStructureChangeListenerImplementation(final EditableModel model) {
            modelReference = () -> model;
        }

        @Override
        public void nodesRemoved(final List<IdObject> nodes) {
            // Tell program to set visibility after import
            final ModelPanel display = displayFor(modelReference.getModel());
            if (display != null) {
                // display.setBeenSaved(false); // we edited the model
                // TODO notify been saved system, wherever that moves to
                for (final IdObject geoset : nodes) {
                    display.getModelViewManager().makeIdObjectNotVisible(geoset);
                }
                reloadGeosetManagers(display);
            }
        }

        @Override
        public void nodesAdded(final List<IdObject> nodes) {
            // Tell program to set visibility after import
            final ModelPanel display = displayFor(modelReference.getModel());
            if (display != null) {
                // display.setBeenSaved(false); // we edited the model
                // TODO notify been saved system, wherever that moves to
                for (final IdObject geoset : nodes) {
                    display.getModelViewManager().makeIdObjectVisible(geoset);
                }
                reloadGeosetManagers(display);
                display.getEditorRenderModel().refreshFromEditor(animatedRenderEnvironment, IDENTITY,
                        IDENTITY, IDENTITY, display.getPerspArea().getViewport());
                display.getAnimationViewer().reload();
            }
        }

        @Override
        public void geosetsRemoved(final List<Geoset> geosets) {
            // Tell program to set visibility after import
            final ModelPanel display = displayFor(modelReference.getModel());
            if (display != null) {
                // display.setBeenSaved(false); // we edited the model
                // TODO notify been saved system, wherever that moves to
                for (final Geoset geoset : geosets) {
                    display.getModelViewManager().makeGeosetNotEditable(geoset);
                    display.getModelViewManager().makeGeosetNotVisible(geoset);
                }
                reloadGeosetManagers(display);
            }
        }

        @Override
        public void geosetsAdded(final List<Geoset> geosets) {
            // Tell program to set visibility after import
            final ModelPanel display = displayFor(modelReference.getModel());
            if (display != null) {
                // display.setBeenSaved(false); // we edited the model
                // TODO notify been saved system, wherever that moves to
                for (final Geoset geoset : geosets) {
                    display.getModelViewManager().makeGeosetEditable(geoset);
                    // display.getModelViewManager().makeGeosetVisible(geoset);
                }
                reloadGeosetManagers(display);
            }
        }

        @Override
        public void camerasAdded(final List<Camera> cameras) {
            // Tell program to set visibility after import
            final ModelPanel display = displayFor(modelReference.getModel());
            if (display != null) {
                // display.setBeenSaved(false); // we edited the model
                // TODO notify been saved system, wherever that moves to
                for (final Camera camera : cameras) {
                    display.getModelViewManager().makeCameraVisible(camera);
                    // display.getModelViewManager().makeGeosetVisible(geoset);
                }
                reloadGeosetManagers(display);
            }
        }

        @Override
        public void camerasRemoved(final List<Camera> cameras) {
            // Tell program to set visibility after import
            final ModelPanel display = displayFor(modelReference.getModel());
            if (display != null) {
                // display.setBeenSaved(false); // we edited the model
                // TODO notify been saved system, wherever that moves to
                for (final Camera camera : cameras) {
                    display.getModelViewManager().makeCameraNotVisible(camera);
                    // display.getModelViewManager().makeGeosetVisible(geoset);
                }
                reloadGeosetManagers(display);
            }
        }

        @Override
        public void timelineAdded(final TimelineContainer node, final AnimFlag timeline) {

        }

        @Override
        public void keyframeAdded(final TimelineContainer node, final AnimFlag timeline, final int trackTime) {
            timeSliderPanel.revalidateKeyframeDisplay();
        }

        @Override
        public void timelineRemoved(final TimelineContainer node, final AnimFlag timeline) {

        }

        @Override
        public void keyframeRemoved(final TimelineContainer node, final AnimFlag timeline, final int trackTime) {
            timeSliderPanel.revalidateKeyframeDisplay();
        }

        @Override
        public void animationsAdded(final List<Animation> animation) {
            currentModelPanel().getAnimationViewer().reload();
            currentModelPanel().getAnimationController().reload();
            creatorPanel.reloadAnimationList();
            final ModelPanel display = displayFor(modelReference.getModel());
            if (display != null) {
                reloadComponentBrowser(display);
            }
        }

        @Override
        public void animationsRemoved(final List<Animation> animation) {
            currentModelPanel().getAnimationViewer().reload();
            currentModelPanel().getAnimationController().reload();
            creatorPanel.reloadAnimationList();
            final ModelPanel display = displayFor(modelReference.getModel());
            if (display != null) {
                reloadComponentBrowser(display);
            }
        }

        @Override
        public void texturesChanged() {
            final ModelPanel modelPanel = currentModelPanel();
            if (modelPanel != null) {
                modelPanel.getAnimationViewer().reloadAllTextures();
                modelPanel.getPerspArea().reloadAllTextures();
            }
            final ModelPanel display = displayFor(modelReference.getModel());
            if (display != null) {
                reloadComponentBrowser(display);
            }
        }

        @Override
        public void headerChanged() {
            final ModelPanel display = displayFor(modelReference.getModel());
            if (display != null) {
                reloadComponentBrowser(display);
            }
        }

        @Override
        public void animationParamsChanged(final Animation animation) {
            currentModelPanel().getAnimationViewer().reload();
            currentModelPanel().getAnimationController().reload();
            creatorPanel.reloadAnimationList();
            final ModelPanel display = displayFor(modelReference.getModel());
            if (display != null) {
                reloadComponentBrowser(display);
            }
        }

        @Override
        public void globalSequenceLengthChanged(final int index, final Integer newLength) {
            currentModelPanel().getAnimationViewer().reload();
            currentModelPanel().getAnimationController().reload();
            creatorPanel.reloadAnimationList();
            final ModelPanel display = displayFor(modelReference.getModel());
            if (display != null) {
                reloadComponentBrowser(display);
            }
        }
    }

    private final class RepaintingModelStateListener implements ModelViewStateListener {
        private final JComponent component;

        public RepaintingModelStateListener(final JComponent component) {
            this.component = component;
        }

        @Override
        public void idObjectVisible(final IdObject bone) {
            component.repaint();
        }

        @Override
        public void idObjectNotVisible(final IdObject bone) {
            component.repaint();
        }

        @Override
        public void highlightGeoset(final Geoset geoset) {
            component.repaint();
        }

        @Override
        public void geosetVisible(final Geoset geoset) {
            component.repaint();
        }

        @Override
        public void geosetNotVisible(final Geoset geoset) {
            component.repaint();
        }

        @Override
        public void geosetNotEditable(final Geoset geoset) {
            component.repaint();
        }

        @Override
        public void geosetEditable(final Geoset geoset) {
            component.repaint();
        }

        @Override
        public void cameraVisible(final Camera camera) {
            component.repaint();
        }

        @Override
        public void cameraNotVisible(final Camera camera) {
            component.repaint();
        }

        @Override
        public void unhighlightGeoset(final Geoset geoset) {
            component.repaint();
        }

        @Override
        public void highlightNode(final IdObject node) {
            component.repaint();
        }

        @Override
        public void unhighlightNode(final IdObject node) {
            component.repaint();
        }
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
            if ((recentItems.size() <= i) || (recentItems.get(i).filepath != fp)) {
                // String[] bits = recent.get(i).split("/");

                final RecentItem item = new RecentItem(new File(fp).getName());
                item.filepath = fp;
                recentItems.add(item);
                item.addActionListener(e -> {

                    currentFile = new File(item.filepath);
                    profile.setPath(currentFile.getParent());
                    // frontArea.clearGeosets();
                    // sideArea.clearGeosets();
                    // botArea.clearGeosets();
                    toolsMenu.getAccessibleContext().setAccessibleDescription(
                            "Allows the user to control which parts of the model are displayed for editing.");
                    toolsMenu.setEnabled(true);
                    SaveProfile.get().addRecent(currentFile.getPath());
                    updateRecent();
                    loadFile(currentFile);
                });
                recentMenu.add(item, recentMenu.getItemCount() - 2);
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

    public ModelEditorManager currentMDLDisp() {
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
     *
     * @param model
     * @return
     */
    public ModelPanel displayFor(final EditableModel model) {
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

    public void loadFile(final File f, final boolean temporary, final boolean selectNewTab, final ImageIcon icon) {
        final String pathLow = f.getPath().toLowerCase();

        if (pathLow.endsWith("blp")) {
            loadBLPPathAsModel(f.getName(), f.getParentFile());
            return;
        }
        if (pathLow.endsWith("png")) {
            loadBLPPathAsModel(f.getName(), f.getParentFile());
            return;
        }
        ModelPanel temp = null;
        if (pathLow.endsWith("mdx") || pathLow.endsWith("mdl")) {
            try {
                final EditableModel model = MdxUtils.loadEditable(f);
                model.setFileRef(f);
                temp = new ModelPanel(this, model, prefs, MainPanel.this, selectionItemTypeGroup,
                        selectionModeGroup, modelStructureChangeListener, coordDisplayListener,
                        viewportTransferHandler, activeViewportWatcher, icon, false, textureExporter);
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
                ExceptionPopup.display(e);
                throw new RuntimeException("Reading mdx failed");
            } catch (final IOException e) {
                e.printStackTrace();
                ExceptionPopup.display(e);
                throw new RuntimeException("Reading mdx failed");
            }
        } else if (pathLow.endsWith("obj") || pathLow.endsWith("fbx")) {
            try {
                AiScene scene = Jassimp.importFile(f.getPath(), new HashSet<>(Arrays.asList(AiPostProcessSteps.TRIANGULATE)));

                final EditableModel model = new EditableModel(scene);
                model.setFileRef(f);

                temp = new ModelPanel(this, model, prefs, MainPanel.this,
                        selectionItemTypeGroup, selectionModeGroup, modelStructureChangeListener,
                        coordDisplayListener, viewportTransferHandler, activeViewportWatcher, icon,
                        false, textureExporter);
            } catch (final Exception e) {
                ExceptionPopup.display(e);
                e.printStackTrace();
            }
            // final Build builder = new Build();
            // final MDLOBJBuilderInterface builder = new
            // MDLOBJBuilderInterface();
            // final Build builder = new Build();
            // try {
            //     final Parse obj = new Parse(builder, f.getPath());
            //     temp = new ModelPanel(this, builder.createMDL(), prefs, MainPanel.this,
            //             selectionItemTypeGroup, selectionModeGroup, modelStructureChangeListener,
            //             coordDisplayListener, viewportTransferHandler, activeViewportWatcher, icon,
            //             false, textureExporter);
            // } catch (final FileNotFoundException e) {
            //     ExceptionPopup.display(e);
            //     e.printStackTrace();
            // } catch (final IOException e) {
            //     ExceptionPopup.display(e);
            //     e.printStackTrace();
            // }
        }
        loadModel(temporary, selectNewTab, temp);
    }

    public void loadStreamMdx(final InputStream f, final boolean temporary, final boolean selectNewTab,
                              final ImageIcon icon) {
        ModelPanel temp = null;
        try {
            final EditableModel model = MdxUtils.loadEditable(f);
            model.setFileRef(null);
            temp = new ModelPanel(this, model, prefs, MainPanel.this, selectionItemTypeGroup,
                    selectionModeGroup, modelStructureChangeListener, coordDisplayListener,
                    viewportTransferHandler, activeViewportWatcher, icon, false, textureExporter);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            ExceptionPopup.display(e);
            throw new RuntimeException("Reading mdx failed");
        } catch (final IOException e) {
            e.printStackTrace();
            ExceptionPopup.display(e);
            throw new RuntimeException("Reading mdx failed");
        }
        loadModel(temporary, selectNewTab, temp);
    }

    public void loadBLPPathAsModel(final String filepath) {
        loadBLPPathAsModel(filepath, null);
    }

    public void loadBLPPathAsModel(final String filepath, final File workingDirectory) {
        loadBLPPathAsModel(filepath, workingDirectory, 800);
    }

    public void loadBLPPathAsModel(final String filepath, final File workingDirectory, final int version) {
        final EditableModel blankTextureModel = new EditableModel(filepath.substring(filepath.lastIndexOf('\\') + 1));
        blankTextureModel.setFormatVersion(version);
        if (workingDirectory != null) {
            blankTextureModel.setFileRef(new File(workingDirectory.getPath() + "/" + filepath + ".mdl"));
        }
        final Geoset newGeoset = new Geoset();
        final Layer layer = new Layer("Blend", new Bitmap(filepath));
        layer.setUnshaded(true);
        final Material material = new Material(layer);
        newGeoset.setMaterial(material);
        final BufferedImage bufferedImage = material.getBufferedImage(blankTextureModel.getWrappedDataSource());
        final int textureWidth = bufferedImage.getWidth();
        final int textureHeight = bufferedImage.getHeight();
        final float aspectRatio = textureWidth / (float) textureHeight;

        final int displayWidth = (int) (aspectRatio > 1 ? 128 : 128 * aspectRatio);
        final int displayHeight = (int) (aspectRatio < 1 ? 128 : 128 / aspectRatio);

        final int groundOffset = aspectRatio > 1 ? (128 - displayHeight) / 2 : 0;
        final GeosetVertex upperLeft = new GeosetVertex(0, displayWidth / 2, displayHeight + groundOffset,
                new Vec3(0, 0, 1));
        final Vec2 upperLeftTVert = new Vec2(1, 0);
        upperLeft.addTVertex(upperLeftTVert);
        newGeoset.add(upperLeft);
        upperLeft.setGeoset(newGeoset);

        final GeosetVertex upperRight = new GeosetVertex(0, -displayWidth / 2, displayHeight + groundOffset,
                new Vec3(0, 0, 1));
        newGeoset.add(upperRight);
        final Vec2 upperRightTVert = new Vec2(0, 0);
        upperRight.addTVertex(upperRightTVert);
        upperRight.setGeoset(newGeoset);

        final GeosetVertex lowerLeft = new GeosetVertex(0, displayWidth / 2, groundOffset, new Vec3(0, 0, 1));
        newGeoset.add(lowerLeft);
        final Vec2 lowerLeftTVert = new Vec2(1, 1);
        lowerLeft.addTVertex(lowerLeftTVert);
        lowerLeft.setGeoset(newGeoset);

        final GeosetVertex lowerRight = new GeosetVertex(0, -displayWidth / 2, groundOffset, new Vec3(0, 0, 1));
        newGeoset.add(lowerRight);
        final Vec2 lowerRightTVert = new Vec2(0, 1);
        lowerRight.addTVertex(lowerRightTVert);
        lowerRight.setGeoset(newGeoset);

        newGeoset.add(new Triangle(upperLeft, upperRight, lowerLeft));
        newGeoset.add(new Triangle(upperRight, lowerRight, lowerLeft));
        blankTextureModel.add(newGeoset);
        blankTextureModel.add(new Animation("Stand", 0, 1000));
        blankTextureModel.doSavePreps();

        loadModel(workingDirectory == null, true,
                new ModelPanel(MainPanel.this, blankTextureModel, prefs, MainPanel.this,
                        selectionItemTypeGroup, selectionModeGroup, modelStructureChangeListener,
                        coordDisplayListener, viewportTransferHandler, activeViewportWatcher,
                        RMSIcons.orangeIcon, true, textureExporter));
    }

    public void loadModel(final boolean temporary, final boolean selectNewTab, final ModelPanel temp) {
        if (temporary) {
            temp.getModelViewManager().getModel().setTemp(true);
        }
        final ModelPanel modelPanel = temp;
        // temp.getRootWindow().addMouseListener(new MouseAdapter() {
        // @Override
        // public void mouseEntered(final MouseEvent e) {
        // currentModelPanel = ModelPanel;
        // geoControl.setViewportView(currentModelPanel.getModelViewManagingTree());
        // geoControl.repaint();
        // }
        // });
        final JMenuItem menuItem = new JMenuItem(temp.getModel().getName());
        menuItem.setIcon(temp.getIcon());
        windowMenu.add(menuItem);
        menuItem.addActionListener(e -> setCurrentModel(modelPanel));
        temp.setJMenuItem(menuItem);
        temp.getModelViewManager().addStateListener(new RepaintingModelStateListener(MainPanel.this));
        temp.changeActivity(currentActivity);

        if (geoControl == null) {
            geoControl = new JScrollPane(temp.getModelViewManagingTree());
            viewportControllerWindowView.setComponent(geoControl);
            viewportControllerWindowView.repaint();
            geoControlModelData = new JScrollPane(temp.getModelComponentBrowserTree());
            modelDataView.setComponent(geoControlModelData);
            modelComponentView.setComponent(temp.getComponentsPanel());
            modelDataView.repaint();
        }
        addTabForView(temp, selectNewTab);
        modelPanels.add(temp);

        // tabbedPane.addTab(f.getName().split("\\.")[0], icon, temp, f.getPath());
        // if (selectNewTab) {
        // tabbedPane.setSelectedComponent(temp);
        // }
        if (temporary) {
            temp.getModelViewManager().getModel().setFileRef(null);
        }
        // }
        // }).start();
        toolsMenu.setEnabled(true);

        if (selectNewTab && (prefs.getQuickBrowse() != null) && prefs.getQuickBrowse()) {
            for (int i = (modelPanels.size() - 2); i >= 0; i--) {
                final ModelPanel openModelPanel = modelPanels.get(i);
                if (openModelPanel.getUndoManager().isRedoListEmpty()
                        && openModelPanel.getUndoManager().isUndoListEmpty()) {
                    if (openModelPanel.close(this)) {
                        modelPanels.remove(openModelPanel);
                        windowMenu.remove(openModelPanel.getMenuItem());
                    }
                }
            }
        }
    }

    public void addTabForView(final ModelPanel view, final boolean selectNewTab) {
        // modelTabStringViewMap.addView(view);
        // final DockingWindow previousWindow = modelTabWindow.getWindow();
        // final TabWindow tabWindow = previousWindow instanceof TabWindow ? (TabWindow)
        // previousWindow : new
        // TabWindow();
        // DockingWindow selectedWindow = null;
        // if (previousWindow == tabWindow) {
        // selectedWindow = tabWindow.getSelectedWindow();
        // }
        // if (previousWindow != null && tabWindow != previousWindow) {
        // tabWindow.addTab(previousWindow);
        // }
        // tabWindow.addTab(view);
        // if (selectedWindow != null) {
        // tabWindow.setSelectedTab(tabWindow.getChildWindowIndex(selectNewTab ? view :
        // selectedWindow));
        // }
        // modelTabWindow.setWindow(tabWindow);
        if (selectNewTab) {
            view.getMenuItem().doClick();
        }
    }

    public void setCurrentModel(final ModelPanel modelContextManager) {
        currentModelPanel = modelContextManager;
        if (currentModelPanel == null) {
            final JPanel jPanel = new JPanel();
            jPanel.add(new JLabel("..."));
            viewportControllerWindowView.setComponent(jPanel);
            geoControl = null;
            frontView.setComponent(new JPanel());
            bottomView.setComponent(new JPanel());
            leftView.setComponent(new JPanel());
            perspectiveView.setComponent(new JPanel());
            previewView.setComponent(new JPanel());
            animationControllerView.setComponent(new JPanel());
            refreshAnimationModeState();
            timeSliderPanel.setUndoManager(null, animatedRenderEnvironment);
            timeSliderPanel.setModelView(null);
            creatorPanel.setModelEditorManager(null);
            creatorPanel.setCurrentModel(null);
            creatorPanel.setUndoManager(null);
            modelComponentView.setComponent(new JPanel());
            geoControlModelData = null;
        } else {
            geoControl.setViewportView(currentModelPanel.getModelViewManagingTree());
            geoControl.repaint();

            frontView.setComponent(modelContextManager.getFrontArea());
            bottomView.setComponent(modelContextManager.getBotArea());
            leftView.setComponent(modelContextManager.getSideArea());
            perspectiveView.setComponent(modelContextManager.getPerspArea());
            previewView.setComponent(modelContextManager.getAnimationViewer());
            animationControllerView.setComponent(modelContextManager.getAnimationController());
            refreshAnimationModeState();
            timeSliderPanel.setUndoManager(currentModelPanel.getUndoManager(),
                    animatedRenderEnvironment);
            timeSliderPanel.setModelView(currentModelPanel.getModelViewManager());
            creatorPanel.setModelEditorManager(currentModelPanel.getModelEditorManager());
            creatorPanel.setCurrentModel(currentModelPanel.getModelViewManager());
            creatorPanel.setUndoManager(currentModelPanel.getUndoManager());

            geoControlModelData.setViewportView(currentModelPanel.getModelComponentBrowserTree());

            modelComponentView.setComponent(currentModelPanel.getComponentsPanel());
            geoControlModelData.repaint();
            currentModelPanel.getModelComponentBrowserTree().reloadFromModelView();
        }
        activeViewportWatcher.viewportChanged(null);
        timeSliderPanel.revalidateKeyframeDisplay();
    }

    public void loadFile(final File f, final boolean temporary) {
        loadFile(f, temporary, true, MDLIcon);
    }

    public void loadFile(final File f) {
        loadFile(f, false);
    }

    public void openFile(final File f) {
        currentFile = f;
        profile.setPath(currentFile.getParent());
        // frontArea.clearGeosets();
        // sideArea.clearGeosets();
        // botArea.clearGeosets();
        toolsMenu.getAccessibleContext().setAccessibleDescription(
                "Allows the user to control which parts of the model are displayed for editing.");
        toolsMenu.setEnabled(true);
        SaveProfile.get().addRecent(currentFile.getPath());
        updateRecent();
        loadFile(currentFile);
    }

    public void importFile(final File f) throws IOException {
        final EditableModel currentModel = currentMDL();
        if (currentModel != null) {
            importFile(MdxUtils.loadEditable(f));
        }
    }

    public void importFile(final EditableModel model) {
        final EditableModel currentModel = currentMDL();
        if (currentModel != null) {
            importPanel = new ImportPanel(currentModel, model);
            importPanel.setCallback(new ModelStructureChangeListenerImplementation(new ModelReference() {
                private final EditableModel model = currentMDL();

                @Override
                public EditableModel getModel() {
                    return model;
                }
            }));

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

    public void nullmodelFile() {
        final EditableModel currentMDL = currentMDL();
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
            importPanel = new ImportPanel(newModel, EditableModel.deepClone(currentMDL, "CurrentModel"));

            final Thread watcher = new Thread(() -> {
                while (importPanel.getParentFrame().isVisible()
                        && (!importPanel.importStarted()
                        || importPanel.importEnded())) {
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
                        try {
                            MdxUtils.saveMdx(newModel, newModel.getFile());
                        } catch (final IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        loadFile(newModel.getFile());
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

    public void refreshController() {
        if (geoControl != null) {
            geoControl.repaint();
        }
        if (geoControlModelData != null) {
            geoControlModelData.repaint();
        }
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

    public void setMouseCoordDisplay(final byte dim1, final byte dim2, final double value1, final double value2) {
        for (final JTextField jTextField : mouseCoordDisplay) {
            jTextField.setText("");
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
                return "Undo " + currentModelPanel().getUndoManager().getUndoText();// +"
                // Ctrl+Z";
            } else {
                return "Can't undo";// +" Ctrl+Z";
            }
        }

        public boolean funcEnabled() {
            try {
                return !currentModelPanel().getUndoManager().isUndoListEmpty();
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
                return "Redo " + currentModelPanel().getUndoManager().getRedoText();// +"
                // Ctrl+Y";
            } else {
                return "Can't redo";// +" Ctrl+Y";
            }
        }

        public boolean funcEnabled() {
            try {
                return !currentModelPanel().getUndoManager().isRedoListEmpty();
            } catch (final NullPointerException e) {
                return false;
            }
        }
    }

    public boolean closeAll() {
        boolean success = true;
        final Iterator<ModelPanel> iterator = modelPanels.iterator();
        boolean closedCurrentPanel = false;
        ModelPanel lastUnclosedModelPanel = null;
        while (iterator.hasNext()) {
            final ModelPanel panel = iterator.next();
            if (success = panel.close(this)) {
                windowMenu.remove(panel.getMenuItem());
                iterator.remove();
                if (panel == currentModelPanel) {
                    closedCurrentPanel = true;
                }
            } else {
                lastUnclosedModelPanel = panel;
                break;
            }
        }
        if (closedCurrentPanel) {
            setCurrentModel(lastUnclosedModelPanel);
        }
        return success;
    }

    public boolean closeOthers(final ModelPanel panelToKeepOpen) {
        boolean success = true;
        final Iterator<ModelPanel> iterator = modelPanels.iterator();
        boolean closedCurrentPanel = false;
        ModelPanel lastUnclosedModelPanel = null;
        while (iterator.hasNext()) {
            final ModelPanel panel = iterator.next();
            if (panel == panelToKeepOpen) {
                lastUnclosedModelPanel = panel;
                continue;
            }
            if (success = panel.close(this)) {
                windowMenu.remove(panel.getMenuItem());
                iterator.remove();
                if (panel == currentModelPanel) {
                    closedCurrentPanel = true;
                }
            } else {
                lastUnclosedModelPanel = panel;
                break;
            }
        }
        if (closedCurrentPanel) {
            setCurrentModel(lastUnclosedModelPanel);
        }
        return success;
    }

    public void repaintSelfAndChildren() {
        repaint();
        geoControl.repaint();
        geoControlModelData.repaint();
    }

    private final TextureExporterImpl textureExporter = new TextureExporterImpl();

    public final class TextureExporterImpl implements TextureExporter {
        public JFileChooser getFileChooser() {
            return exportTextureDialog;
        }

        @Override
        public void showOpenDialog(final String suggestedName, final TextureExporterClickListener fileHandler,
                                   final Component parent) {
            if (exportTextureDialog.getCurrentDirectory() == null) {
                final EditableModel current = currentMDL();
                if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                    fc.setCurrentDirectory(current.getFile().getParentFile());
                } else if (profile.getPath() != null) {
                    fc.setCurrentDirectory(new File(profile.getPath()));
                }
            }
            if (exportTextureDialog.getCurrentDirectory() == null) {
                exportTextureDialog.setSelectedFile(new File(
                        exportTextureDialog.getCurrentDirectory() + File.separator + suggestedName));
            }
            final int showOpenDialog = exportTextureDialog.showOpenDialog(parent);
            if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
                final File file = exportTextureDialog.getSelectedFile();
                if (file != null) {
                    fileHandler.onClickOK(file, exportTextureDialog.getFileFilter());
                } else {
                    JOptionPane.showMessageDialog(parent, "No import file was specified");
                }
            }
        }

        @Override
        public void exportTexture(final String suggestedName, final TextureExporterClickListener fileHandler,
                                  final Component parent) {

            if (exportTextureDialog.getCurrentDirectory() == null) {
                final EditableModel current = currentMDL();
                if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                    fc.setCurrentDirectory(current.getFile().getParentFile());
                } else if (profile.getPath() != null) {
                    fc.setCurrentDirectory(new File(profile.getPath()));
                }
            }
            if (exportTextureDialog.getCurrentDirectory() == null) {
                exportTextureDialog.setSelectedFile(new File(
                        exportTextureDialog.getCurrentDirectory() + File.separator + suggestedName));
            }

            final int x = exportTextureDialog.showSaveDialog(parent);
            if (x == JFileChooser.APPROVE_OPTION) {
                final File file = exportTextureDialog.getSelectedFile();
                if (file != null) {
                    try {
                        if (file.getName().lastIndexOf('.') >= 0) {
                            fileHandler.onClickOK(file, exportTextureDialog.getFileFilter());
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
            onClickSaveAs(model);
        }
    }

    private Component getFocusedComponent() {
        final KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        final Component focusedComponent = kfm.getFocusOwner();
        return focusedComponent;
    }

    private boolean focusedComponentNeedsTyping(final Component focusedComponent) {
        return (focusedComponent instanceof JTextArea) || (focusedComponent instanceof JTextField);
    }
}
