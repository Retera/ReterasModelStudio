package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.util.TwiAiIoSys;
import com.hiveworkshop.rms.editor.model.util.TwiAiSceneParser;
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
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.ui.util.ExtFilter;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.War3ID;
import jassimp.AiPostProcessSteps;
import jassimp.AiProgressHandler;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class MPQBrowserView {
    static final ImageIcon MDLIcon = RMSIcons.MDLIcon;

    static View createMPQBrowser(MainPanel mainPanel, final ImageIcon imageIcon) {
        final MPQBrowser mpqBrowser = new MPQBrowser(GameDataFileSystem.getDefault(),
                filepath -> loadFileByType(mainPanel, filepath),
                path -> fetchModelTexture(mainPanel, path));
        final View view = new View("Data Browser", imageIcon, mpqBrowser);
        view.getWindowProperties().setCloseEnabled(true);
        return view;
    }

    private static void loadFileByType(MainPanel mainPanel, String filepath) {
        loadFile(mainPanel, GameDataFileSystem.getDefault().getFile(filepath), true);
    }

    private static void fetchModelTexture(MainPanel mainPanel, String path) {
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
                    mainPanel.animatedRenderEnvironment.setBounds(anim);
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
//        mainPanel.setKeyframe.setVisible(mainPanel.animationModeState);
//        mainPanel.setTimeBounds.setVisible(mainPanel.animationModeState);
        mainPanel.timeSliderPanel.setKeyframeModeActive(mainPanel.animationModeState);

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
                .updateNodes(false); // update to 0 position
    }

    private static ModelPanel newTempModelPanel(MainPanel mainPanel, ImageIcon icon, EditableModel model) {
        ModelPanel temp;
        temp = new ModelPanel(mainPanel, model, mainPanel.prefs, mainPanel,
                mainPanel.selectionItemTypeGroup,
                mainPanel.selectionModeGroup,
                mainPanel.modelStructureChangeListener,
                mainPanel.coordDisplayListener,
                mainPanel.viewportTransferHandler,
                mainPanel.viewportListener, icon, false
        );
        return temp;
    }


    //    public static EditableModel getImagePlaneModel(String fileName, File workingDirectory, int version) {
    public static EditableModel getImagePlaneModel(File file, int version) {
        String fileName = file.getName();
        System.out.println("fileName: " + fileName);
//        File fileRef = new File(file.getPath().replaceAll("\\.[^.]+$", "") + ".mdl");
        File fileRef = new File(file.getPath());
        System.out.println("fileRef: " + fileRef + ", fileRefPath: " + fileRef.getPath());

        EditableModel blankTextureModel = new EditableModel(fileName);
        blankTextureModel.setFileRef(fileRef);
        blankTextureModel.setFormatVersion(version);
//        blankTextureModel.setTemp(true);

        Geoset newGeoset = new Geoset();
        if (version == 1000) {
            newGeoset.setLevelOfDetail(0);
        }
        Layer layer = new Layer("Blend", new Bitmap(fileName));
        layer.setUnshaded(true);
//        layer.setTwoSided(true);
        Material material = new Material(layer);
        newGeoset.setMaterial(material);
        BufferedImage bufferedImage = material.getBufferedImage(blankTextureModel.getWrappedDataSource());
        int textureWidth = bufferedImage.getWidth();
        int textureHeight = bufferedImage.getHeight();
        float aspectRatio = textureWidth / (float) textureHeight;

        int displayWidth = (int) (aspectRatio > 1 ? 128 : 128 * aspectRatio);
        int displayHeight = (int) (aspectRatio < 1 ? 128 : 128 / aspectRatio);

        int groundOffset = aspectRatio > 1 ? (128 - displayHeight) / 2 : 0;

        Vec2 min = new Vec2(-displayWidth / 2.0, groundOffset);
        Vec2 max = new Vec2(displayWidth / 2.0, displayHeight + groundOffset);

        ModelUtils.Mesh planeMesh = ModelUtils.createPlane((byte) 0, true, 0, max, min, 1);
        newGeoset.addVerticies(planeMesh.getVertices());
        newGeoset.setTriangles(planeMesh.getTriangles());

        blankTextureModel.add(newGeoset);
        ExtLog extLog = new ExtLog(128).setDefault();
        blankTextureModel.setExtents(extLog);
        blankTextureModel.add(new Animation("Stand", 0, 1000));
        blankTextureModel.doSavePreps();
        return blankTextureModel;
    }

    public static void loadModel(MainPanel mainPanel, final boolean temporary, final boolean selectNewTab, final ModelPanel modelPanel) {
        if (temporary) {
            modelPanel.getModelViewManager().getModel().setTemp(true);
        }
        final JMenuItem menuItem = new JMenuItem(modelPanel.getModel().getName());
        menuItem.setIcon(modelPanel.getIcon());
//	    mainPanel.windowMenu.add(menuItem);
        MenuBar.windowMenu.add(menuItem);
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

//        mainPanel.toolsMenu.setEnabled(true);
        MenuBar.toolsMenu.setEnabled(true);

        if (selectNewTab && mainPanel.prefs.getQuickBrowse()) {
            for (int i = (mainPanel.modelPanels.size() - 2); i >= 0; i--) {
                final ModelPanel openModelPanel = mainPanel.modelPanels.get(i);
                if (openModelPanel.getUndoManager().isRedoListEmpty() && openModelPanel.getUndoManager().isUndoListEmpty()) {
                    if (openModelPanel.close(mainPanel)) {
                        mainPanel.modelPanels.remove(openModelPanel);
//                        mainPanel.windowMenu.remove(openModelPanel.getMenuItem());
                        MenuBar.windowMenu.remove(openModelPanel.getMenuItem());
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
        mainPanel.viewportListener.viewportChanged(null);
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

    public static void loadFile(MainPanel mainPanel, final File f, boolean temporary, final boolean selectNewTab, final ImageIcon icon) {
        System.out.println("loadFile: " + f.getName());
        System.out.println("filePath: " + f.getPath());
        ExtFilter extFilter = new ExtFilter();
        if (f.exists()) {
            final String pathLow = f.getPath().toLowerCase();
            String ext = pathLow.replaceAll(".+\\.(?=.+)", "");
            ModelPanel tempModelPanel = null;
            if (extFilter.isSupTexture(ext)) {
                final EditableModel model;
                if (ext.equals("dds")) {
                    model = getImagePlaneModel(f, 1000);
                } else {
                    model = getImagePlaneModel(f, 800);
                }
                model.setTemp(true);
//            model.setFileRef(f);
                temporary = false;
                tempModelPanel = newTempModelPanel(mainPanel, icon, model);

            }

            if (Arrays.asList("mdx", "mdl").contains(ext)) {
                try {

                    final EditableModel model = MdxUtils.loadEditable(f);
                    model.setFileRef(f);

                    tempModelPanel = newTempModelPanel(mainPanel, icon, model);

                } catch (final IOException e) {
                    e.printStackTrace();
                    ExceptionPopup.display(e);
                    throw new RuntimeException("Reading mdx failed");
                }
            } else if (Arrays.asList("obj", "fbx").contains(ext)) {
                try {
                    System.out.println("importing file \"" + f.getName() + "\" this might take a while...");
                    long timeStart = System.currentTimeMillis();
                    AiProgressHandler aiProgressHandler = new AiProgressHandler() {
                        @Override
                        public boolean update(float v) {
//                            System.out.println("progress: " + (int)((v+1)*100) + "%  " + (System.currentTimeMillis()-timeStart) + " ms");
                            return true;
                        }
                    };
//                    AiClassLoaderIOSystem aiIOSystem = new AiClassLoaderIOSystem();
                    TwiAiIoSys twiAiIoSys = new TwiAiIoSys();
                    AiScene scene = Jassimp.importFile(f.getPath(), new HashSet<>(Collections.singletonList(AiPostProcessSteps.TRIANGULATE)), twiAiIoSys, aiProgressHandler);
                    TwiAiSceneParser twiAiSceneParser = new TwiAiSceneParser(scene);
//                    final EditableModel model = new EditableModel(scene);
                    System.out.println("took " + (System.currentTimeMillis() - timeStart) + " ms to load the model");
                    EditableModel model = twiAiSceneParser.getEditableModel();
                    model.setFileRef(f);
//
                    tempModelPanel = newTempModelPanel(mainPanel, icon, model);
                } catch (final Exception e) {
                    ExceptionPopup.display(e);
                    e.printStackTrace();
                }
            }
            if (tempModelPanel != null) {
                loadModel(mainPanel, temporary, selectNewTab, tempModelPanel);
            }
        } else if (SaveProfile.get().getRecent().contains(f.getPath())) {
            int option = JOptionPane.showConfirmDialog(mainPanel, "Could not find the file.\nRemove from recent?", "File not found", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                SaveProfile.get().removeFromRecent(f.getPath());
                MenuBar.updateRecent();
            }
        }
    }

    static void revert(MainPanel mainPanel) {
        final ModelPanel modelPanel = mainPanel.currentModelPanel();
        final int oldIndex = mainPanel.modelPanels.indexOf(modelPanel);
        if (modelPanel != null) {
            if (modelPanel.close(mainPanel)) {
                mainPanel.modelPanels.remove(modelPanel);
//                mainPanel.windowMenu.remove(modelPanel.getMenuItem());
                MenuBar.windowMenu.remove(modelPanel.getMenuItem());
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
        View doodadBrowserView = new View("Doodad Browser", new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)), new JScrollPane(unitEditorTree));
        mainPanel.rootWindow.setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(), doodadBrowserView));
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
//                    mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
                    MenuBar.toolsMenu.getAccessibleContext().setAccessibleDescription(
                            "Allows the user to control which parts of the model are displayed for editing.");
//                    mainPanel.toolsMenu.setEnabled(true);
                    MenuBar.toolsMenu.setEnabled(true);
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
            final ImageIcon icon = unitFetched.getScaledIcon(16);

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
//            mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
            MenuBar.toolsMenu.getAccessibleContext().setAccessibleDescription(
                    "Allows the user to control which parts of the model are displayed for editing.");
//            mainPanel.toolsMenu.setEnabled(true);
            MenuBar.toolsMenu.setEnabled(true);
        }
    }
}
