package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.DoodadTabTreeBrowserBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorTree;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.UnitFields;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPane;
import com.hiveworkshop.rms.ui.browsers.mpq.MPQBrowser;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarActionButtonType;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.War3ID;
import jassimp.AiPostProcessSteps;
import jassimp.AiScene;
import jassimp.Jassimp;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.View;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MPQBrowserView {
    static final ImageIcon MDLIcon = RMSIcons.MDLIcon;

    static View createMPQBrowser(MainPanel mainPanel, final ImageIcon imageIcon) {
        final MPQBrowser mpqBrowser = new MPQBrowser(GameDataFileSystem.getDefault(),
                filepath -> getFilepath(mainPanel, filepath),
                path -> getPath(mainPanel, path));
        final View view = new View("Data Browser", imageIcon, mpqBrowser);
        view.getWindowProperties().setCloseEnabled(true);
        return view;
    }

    private static void getFilepath(MainPanel mainPanel, String filepath) {
        if (filepath.toLowerCase().endsWith(".mdx")) {
            loadFile(mainPanel, GameDataFileSystem.getDefault().getFile(filepath), true);
        } else if (filepath.toLowerCase().endsWith(".blp")) {
            loadBLPPathAsModel(mainPanel, filepath);
        } else if (filepath.toLowerCase().endsWith(".png")) {
            loadBLPPathAsModel(mainPanel, filepath);
        } else if (filepath.toLowerCase().endsWith(".dds")) {
            loadBLPPathAsModel(mainPanel, filepath, null, 1000);
        }
    }

    private static void getPath(MainPanel mainPanel, String path) {
        final int modIndex = Math.max(path.lastIndexOf(".w3mod/"), path.lastIndexOf(".w3mod\\"));
        String finalPath;
        if (modIndex == -1) {
            finalPath = path;
        } else {
            finalPath = path.substring(modIndex + ".w3mod/".length());
        }
        final ModelPanel modelPanel = mainPanel.currentModelPanel();
        if (modelPanel != null) {
            if (modelPanel.getModel().getFormatVersion() > 800) {
                finalPath = finalPath.replace("\\", "/"); // Reforged prefers forward slash
            }
            modelPanel.getModel().add(new Bitmap(finalPath));
            mainPanel.modelStructureChangeListener.texturesChanged();
        }
    }

    public static void refreshAnimationModeState(MainPanel mainPanel) {
        if (mainPanel.animationModeState) {
            if ((mainPanel.currentModelPanel() != null) && (mainPanel.currentModelPanel().getModel() != null)) {
                if (mainPanel.currentModelPanel().getModel().getAnimsSize() > 0) {
                    final Animation anim = mainPanel.currentModelPanel().getModel().getAnim(0);
                    mainPanel.animatedRenderEnvironment.setBounds(anim.getStart(), anim.getEnd());
                }
                refreshAndUpdateModelPanel(mainPanel);
                mainPanel.timeSliderPanel.setNodeSelectionManager(mainPanel.currentModelPanel().getModelEditorManager().getNodeAnimationSelectionManager());
            }
            if ((mainPanel.actionTypeGroup.getActiveButtonType() == mainPanel.actionTypeGroup.getToolbarButtonTypes()[3])
                    || (mainPanel.actionTypeGroup.getActiveButtonType() == mainPanel.actionTypeGroup.getToolbarButtonTypes()[4])) {
                mainPanel.actionTypeGroup.setToolbarButtonType(mainPanel.actionTypeGroup.getToolbarButtonTypes()[0]);
            }
        }

        mainPanel.animatedRenderEnvironment.setStaticViewMode(!mainPanel.animationModeState);

        if (!mainPanel.animationModeState) {
            if ((mainPanel.currentModelPanel() != null) && (mainPanel.currentModelPanel().getModel() != null)) {
                refreshAndUpdateModelPanel(mainPanel);
            }
        }
        final List<ToolbarButtonGroup<ToolbarActionButtonType>.ToolbarButtonAction> buttons = mainPanel.actionTypeGroup
                .getButtons();
        final int numberOfButtons = buttons.size();
        for (int i = 3; i < numberOfButtons; i++) {
            buttons.get(i).getButton().setVisible(!mainPanel.animationModeState);
        }
        mainPanel.snapButton.setVisible(!mainPanel.animationModeState);
        mainPanel.timeSliderPanel.setDrawing(mainPanel.animationModeState);
        mainPanel.setKeyframe.setVisible(mainPanel.animationModeState);
        mainPanel.setTimeBounds.setVisible(mainPanel.animationModeState);
        mainPanel.timeSliderPanel.setKeyframeModeActive(mainPanel.animationModeState);

        if (mainPanel.animationModeState) {
            mainPanel.animationModeButton.setColors(mainPanel.prefs.getActiveColor1(), mainPanel.prefs.getActiveColor2());
        } else {
            mainPanel.animationModeButton.resetColors();
        }
        mainPanel.timeSliderPanel.repaint();
        mainPanel.creatorPanel.setAnimationModeState(mainPanel.animationModeState);
    }

    private static void refreshAndUpdateModelPanel(MainPanel mainPanel) {
        mainPanel
                .currentModelPanel()
                .getEditorRenderModel()
                .refreshFromEditor(
                        mainPanel.animatedRenderEnvironment,
                        ModelStructureChangeListenerImplementation.IDENTITY,
                        ModelStructureChangeListenerImplementation.IDENTITY,
                        ModelStructureChangeListenerImplementation.IDENTITY,
                        mainPanel.currentModelPanel().getPerspArea().getViewport());
        mainPanel
                .currentModelPanel()
                .getEditorRenderModel()
                .updateNodes(true, false); // update to 0 position
    }

    private static ModelPanel newTempModelPanel(MainPanel mainPanel, ImageIcon icon, EditableModel model) {
        ModelPanel temp;
        temp = new ModelPanel(mainPanel, model, mainPanel.prefs, mainPanel,
                mainPanel.selectionItemTypeGroup,
                mainPanel.selectionModeGroup,
                mainPanel.modelStructureChangeListener,
                mainPanel.coordDisplayListener,
                mainPanel.viewportTransferHandler,
                mainPanel.activeViewportWatcher, icon, false,
                mainPanel.textureExporter);
        return temp;
    }

    public static void loadBLPPathAsModel(MainPanel mainPanel, final String filepath) {
        loadBLPPathAsModel(mainPanel, filepath, null);
    }

    public static void loadBLPPathAsModel(MainPanel mainPanel, final String filepath, final File workingDirectory) {
        loadBLPPathAsModel(mainPanel, filepath, workingDirectory, 800);
    }

    public static void loadBLPPathAsModel(MainPanel mainPanel, final String filepath, final File workingDirectory, final int version) {
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

        final GeosetVertex upperLeft = createVertex(newGeoset, displayWidth / 2, displayHeight + groundOffset, 1, 0);

        final GeosetVertex upperRight = createVertex(newGeoset, -displayWidth / 2, displayHeight + groundOffset, 0, 0);

        final GeosetVertex lowerLeft = createVertex(newGeoset, displayWidth / 2, groundOffset, 1, 1);

        final GeosetVertex lowerRight = createVertex(newGeoset, -displayWidth / 2, groundOffset, 0, 1);

        newGeoset.add(new Triangle(upperLeft, upperRight, lowerLeft));
        newGeoset.add(new Triangle(upperRight, lowerRight, lowerLeft));
        blankTextureModel.add(newGeoset);
        blankTextureModel.add(new Animation("Stand", 0, 1000));
        blankTextureModel.doSavePreps();

        loadModel(mainPanel, workingDirectory == null, true,
                new ModelPanel(mainPanel, blankTextureModel, mainPanel.prefs, mainPanel,
                        mainPanel.selectionItemTypeGroup, mainPanel.selectionModeGroup, mainPanel.modelStructureChangeListener,
                        mainPanel.coordDisplayListener, mainPanel.viewportTransferHandler, mainPanel.activeViewportWatcher,
                        RMSIcons.orangeIcon, true, mainPanel.textureExporter));
    }

    private static GeosetVertex createVertex(Geoset newGeoset, int yValue, int zValue, int tx, int ty) {
        final GeosetVertex vertex = new GeosetVertex(0, yValue, zValue, new Vec3(0, 0, 1));
        newGeoset.add(vertex);
        final Vec2 tVert = new Vec2(tx, ty);
        vertex.addTVertex(tVert);
        vertex.setGeoset(newGeoset);
        return vertex;
    }

    public static void loadModel(MainPanel mainPanel, final boolean temporary, final boolean selectNewTab, final ModelPanel modelPanel) {
	    if (temporary) {
		    modelPanel.getModelViewManager().getModel().setTemp(true);
	    }
	    final JMenuItem menuItem = new JMenuItem(modelPanel.getModel().getName());
	    menuItem.setIcon(modelPanel.getIcon());
	    mainPanel.windowMenu.add(menuItem);
	    menuItem.addActionListener(e -> setCurrentModel(mainPanel, modelPanel));
	    modelPanel.setJMenuItem(menuItem);
	    modelPanel.getModelViewManager().addStateListener(new RepaintingModelStateListener(mainPanel));
	    modelPanel.changeActivity(mainPanel.currentActivity);

	    if (mainPanel.geoControl == null) {
		    mainPanel.geoControl = new JScrollPane(modelPanel.getModelViewManagingTree());
		    mainPanel.viewportControllerWindowView.setComponent(mainPanel.geoControl);
		    mainPanel.viewportControllerWindowView.repaint();
		    mainPanel.geoControlModelData = new JScrollPane(modelPanel.getModelComponentBrowserTree());
		    mainPanel.modelDataView.setComponent(mainPanel.geoControlModelData);
		    mainPanel.modelComponentView.setComponent(modelPanel.getComponentsPanel());
		    mainPanel.modelDataView.repaint();
	    }
        if (selectNewTab) {
	        modelPanel.getMenuItem().doClick();
        }
	    mainPanel.modelPanels.add(modelPanel);

        if (temporary) {
	        modelPanel.getModelViewManager().getModel().setFileRef(null);
        }

        mainPanel.toolsMenu.setEnabled(true);

        if (selectNewTab && (mainPanel.prefs.getQuickBrowse() != null) && mainPanel.prefs.getQuickBrowse()) {
            for (int i = (mainPanel.modelPanels.size() - 2); i >= 0; i--) {
                final ModelPanel openModelPanel = mainPanel.modelPanels.get(i);
                if (openModelPanel.getUndoManager().isRedoListEmpty()
                        && openModelPanel.getUndoManager().isUndoListEmpty()) {
                    if (openModelPanel.close(mainPanel)) {
                        mainPanel.modelPanels.remove(openModelPanel);
                        mainPanel.windowMenu.remove(openModelPanel.getMenuItem());
                    }
                }
            }
        }
    }

    public static void setCurrentModel(MainPanel mainPanel, final ModelPanel modelContextManager) {
        mainPanel.currentModelPanel = modelContextManager;
        if (mainPanel.currentModelPanel == null) {
            final JPanel jPanel = new JPanel();
            jPanel.add(new JLabel("..."));
            mainPanel.viewportControllerWindowView.setComponent(jPanel);
            mainPanel.geoControl = null;

            mainPanel.frontView.setComponent(new JPanel());
            mainPanel.bottomView.setComponent(new JPanel());
            mainPanel.leftView.setComponent(new JPanel());
            mainPanel.perspectiveView.setComponent(new JPanel());
            mainPanel.previewView.setComponent(new JPanel());
            mainPanel.animationControllerView.setComponent(new JPanel());
            refreshAnimationModeState(mainPanel);

            mainPanel.timeSliderPanel.setUndoManager(null, mainPanel.animatedRenderEnvironment);
            mainPanel.timeSliderPanel.setModelView(null);
            mainPanel.creatorPanel.setModelEditorManager(null);
            mainPanel.creatorPanel.setCurrentModel(null);
            mainPanel.creatorPanel.setUndoManager(null);

            mainPanel.modelComponentView.setComponent(new JPanel());
            mainPanel.geoControlModelData = null;
        } else {
            mainPanel.geoControl.setViewportView(mainPanel.currentModelPanel.getModelViewManagingTree());
            mainPanel.geoControl.repaint();

            mainPanel.frontView.setComponent(modelContextManager.getFrontArea());
            mainPanel.bottomView.setComponent(modelContextManager.getBotArea());
            mainPanel.leftView.setComponent(modelContextManager.getSideArea());
            mainPanel.perspectiveView.setComponent(modelContextManager.getPerspArea());
            mainPanel.previewView.setComponent(modelContextManager.getAnimationViewer());
            mainPanel.animationControllerView.setComponent(modelContextManager.getAnimationController());
            refreshAnimationModeState(mainPanel);

            mainPanel.timeSliderPanel.setUndoManager(mainPanel.currentModelPanel.getUndoManager(), mainPanel.animatedRenderEnvironment);
            mainPanel.timeSliderPanel.setModelView(mainPanel.currentModelPanel.getModelViewManager());
            mainPanel.creatorPanel.setModelEditorManager(mainPanel.currentModelPanel.getModelEditorManager());
            mainPanel.creatorPanel.setCurrentModel(mainPanel.currentModelPanel.getModelViewManager());
            mainPanel.creatorPanel.setUndoManager(mainPanel.currentModelPanel.getUndoManager());

            mainPanel.modelComponentView.setComponent(mainPanel.currentModelPanel.getComponentsPanel());
            mainPanel.geoControlModelData.setViewportView(mainPanel.currentModelPanel.getModelComponentBrowserTree());
            mainPanel.geoControlModelData.repaint();
            mainPanel.currentModelPanel.getModelComponentBrowserTree().reloadFromModelView();
        }
        mainPanel.activeViewportWatcher.viewportChanged(null);
        mainPanel.timeSliderPanel.revalidateKeyframeDisplay();
    }

    static void openMPQViewer(MainPanel mainPanel) {
        final View view = createMPQBrowser(mainPanel, new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)));
        mainPanel.rootWindow.setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(), view));
    }

    public static void loadFile(MainPanel mainPanel, final File f) {
        loadFile(mainPanel, f, false, true, MDLIcon);
    }

    public static void loadFile(MainPanel mainPanel, final File f, final boolean temporary) {
        loadFile(mainPanel, f, temporary, true, MDLIcon);
    }

    public static void loadFile(MainPanel mainPanel, final File f, final boolean temporary, final boolean selectNewTab, final ImageIcon icon) {
        final String pathLow = f.getPath().toLowerCase();

        if (pathLow.endsWith("blp")) {
            loadBLPPathAsModel(mainPanel, f.getName(), f.getParentFile());
            return;
        }
        if (pathLow.endsWith("png")) {
            loadBLPPathAsModel(mainPanel, f.getName(), f.getParentFile());
            return;
        }
        ModelPanel temp = null;
        if (pathLow.endsWith("mdx") || pathLow.endsWith("mdl")) {
            try {

                final EditableModel model = MdxUtils.loadEditable(f);
                model.setFileRef(f);

                temp = newTempModelPanel(mainPanel, icon, model);

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

                temp = newTempModelPanel(mainPanel, icon, model);
            } catch (final Exception e) {
                ExceptionPopup.display(e);
                e.printStackTrace();
            }
        }
        loadModel(mainPanel, temporary, selectNewTab, temp);
    }

    static void revert(MainPanel mainPanel) {
        final ModelPanel modelPanel = mainPanel.currentModelPanel();
        final int oldIndex = mainPanel.modelPanels.indexOf(modelPanel);
        if (modelPanel != null) {
            if (modelPanel.close(mainPanel)) {
                mainPanel.modelPanels.remove(modelPanel);
                mainPanel.windowMenu.remove(modelPanel.getMenuItem());
                if (mainPanel.modelPanels.size() > 0) {
                    final int newIndex = Math.min(mainPanel.modelPanels.size() - 1, oldIndex);
                    setCurrentModel(mainPanel, mainPanel.modelPanels.get(newIndex));
                } else {
                    // TODO remove from notifiers to fix leaks
                    setCurrentModel(mainPanel, null);
                }
                final File fileToRevert = modelPanel.getModel().getFile();
                loadFile(mainPanel, fileToRevert);
            }
        }
    }

    public static void loadStreamMdx(MainPanel mainPanel, final InputStream f, final boolean temporary, final boolean selectNewTab,
                                     final ImageIcon icon) {
        ModelPanel temp;
        try {
            final EditableModel model = MdxUtils.loadEditable(f);
            model.setFileRef(null);
            temp = newTempModelPanel(mainPanel, icon, model);
        } catch (final IOException e) {
            e.printStackTrace();
            ExceptionPopup.display(e);
            throw new RuntimeException("Reading mdx failed");
        }

        loadModel(mainPanel, temporary, selectNewTab, temp);
    }

    static void loadMdxStream(MutableObjectData.MutableGameObject obj, String prePath, MainPanel mainPanel, boolean b) {
        final String path = ImportFileActions.convertPathToMDX(prePath);
        final String portrait = ModelUtils.getPortrait(path);
        final ImageIcon icon = new ImageIcon(IconUtils
                .getIcon(obj, MutableObjectData.WorldEditorDataType.DOODADS)
                .getScaledInstance(16, 16, Image.SCALE_DEFAULT));

        loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(path), true, b, icon);

        if (mainPanel.prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
            loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false, icon);
        }
    }

    static void OpenDoodadViewer(MainPanel mainPanel) {
        final UnitEditorTree unitEditorTree = new UnitEditorTree(MenuBarActions.getDoodadData(), new DoodadTabTreeBrowserBuilder(),
                MainLayoutCreator.getUnitEditorSettings(), MutableObjectData.WorldEditorDataType.DOODADS);
        unitEditorTree.selectFirstUnit();

        unitEditorTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                try {
                    dodadViewerMouseClick(e, unitEditorTree, mainPanel);
                } catch (final Exception exc) {
                    exc.printStackTrace();
                    ExceptionPopup.display(exc);
                }
            }
        });
        mainPanel.rootWindow.setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(),
                new View("Doodad Browser",
                        new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
                        new JScrollPane(unitEditorTree))));
    }

    private static void dodadViewerMouseClick(MouseEvent e, UnitEditorTree unitEditorTree, MainPanel mainPanel) {
        if (e.getClickCount() >= 2) {
            final TreePath currentUnitTreePath = unitEditorTree.getSelectionPath();
            if (currentUnitTreePath != null) {

                final DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath .getLastPathComponent();
                if (o.getUserObject() instanceof MutableObjectData.MutableGameObject) {

                    final MutableObjectData.MutableGameObject obj = (MutableObjectData.MutableGameObject) o.getUserObject();
                    final int numberOfVariations = obj.getFieldAsInteger(War3ID.fromString("dvar"), 0);
                    if (numberOfVariations > 1) {
                        for (int i = 0; i < numberOfVariations; i++) {
                            String prePath = obj.getFieldAsString(War3ID.fromString("dfil"), 0) + i + ".mdl";
                            loadMdxStream(obj, prePath, mainPanel, i == 0);
                        }
                    } else {
                        String prePath = obj.getFieldAsString(War3ID.fromString("dfil"), 0);
                        loadMdxStream(obj, prePath, mainPanel, true);
                    }
                    mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
                            "Allows the user to control which parts of the model are displayed for editing.");
                    mainPanel.toolsMenu.setEnabled(true);
                }
            }
        }
    }

    static void fetchObject(MainPanel mainPanel) {
        final MutableObjectData.MutableGameObject objectFetched = ImportFileActions.fetchObject(mainPanel);
        if (objectFetched != null) {

            final String filepath = ImportFileActions.convertPathToMDX(objectFetched.getFieldAsString(UnitFields.MODEL_FILE, 0));
            final ImageIcon icon = new ImageIcon(BLPHandler.get().getGameTex(objectFetched.getFieldAsString(UnitFields.INTERFACE_ICON, 0)).getScaledInstance(16, 16, Image.SCALE_FAST));

            loadFromStream(mainPanel, filepath, icon);
        }
    }

    static void fetchModel(MainPanel mainPanel) {
        final ModelOptionPane.ModelElement model = ImportFileActions.fetchModel(mainPanel);
        if (model != null) {

            final String filepath = ImportFileActions.convertPathToMDX(model.getFilepath());
            final ImageIcon icon = model.hasCachedIconPath() ? new ImageIcon(BLPHandler.get().getGameTex(model.getCachedIconPath()).getScaledInstance(16, 16, Image.SCALE_FAST)) : MDLIcon;

            loadFromStream(mainPanel, filepath, icon);
        }
    }

    static void fetchUnit(MainPanel mainPanel) {
        final GameObject unitFetched = ImportFileActions.fetchUnit(mainPanel);
        if (unitFetched != null) {

            final String filepath = ImportFileActions.convertPathToMDX(unitFetched.getField("file"));
            final ImageIcon icon = unitFetched.getScaledIcon(0.25f);

            loadFromStream(mainPanel, filepath, icon);
        }
    }

    private static void loadFromStream(MainPanel mainPanel, String filepath, ImageIcon icon) {
        if (filepath != null) {

            loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(filepath), true, true, icon);

            final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait" + filepath.substring(filepath.lastIndexOf('.'));

            if (mainPanel.prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
                loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false, icon);
            }
            mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
                    "Allows the user to control which parts of the model are displayed for editing.");
            mainPanel.toolsMenu.setEnabled(true);
        }
    }
}
