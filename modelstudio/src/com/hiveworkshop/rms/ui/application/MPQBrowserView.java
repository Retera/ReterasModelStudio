package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.mpq.MPQBrowser;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarActionButtonType;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import jassimp.AiPostProcessSteps;
import jassimp.AiScene;
import jassimp.Jassimp;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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
        final MPQBrowser mpqBrowser = new MPQBrowser(GameDataFileSystem.getDefault(), filepath -> {
            if (filepath.toLowerCase().endsWith(".mdx")) {
                loadFile(mainPanel, GameDataFileSystem.getDefault().getFile(filepath), true);
            } else if (filepath.toLowerCase().endsWith(".blp")) {
                loadBLPPathAsModel(mainPanel, filepath);
            } else if (filepath.toLowerCase().endsWith(".png")) {
                loadBLPPathAsModel(mainPanel, filepath);
            } else if (filepath.toLowerCase().endsWith(".dds")) {
                loadBLPPathAsModel(mainPanel, filepath, null, 1000);
            }
        }, path -> {
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
        });
        final View view = new View("Data Browser", imageIcon, mpqBrowser);
        view.getWindowProperties().setCloseEnabled(true);
        return view;
    }

    public static void refreshAnimationModeState(MainPanel mainPanel) {
        if (mainPanel.animationModeState) {
            if ((mainPanel.currentModelPanel() != null) && (mainPanel.currentModelPanel().getModel() != null)) {
                if (mainPanel.currentModelPanel().getModel().getAnimsSize() > 0) {
                    final Animation anim = mainPanel.currentModelPanel().getModel().getAnim(0);
                    mainPanel.animatedRenderEnvironment.setBounds(anim.getStart(), anim.getEnd());
                }
                mainPanel.currentModelPanel().getEditorRenderModel().refreshFromEditor(mainPanel.animatedRenderEnvironment, ModelStructureChangeListenerImplementation.IDENTITY,
                        ModelStructureChangeListenerImplementation.IDENTITY, ModelStructureChangeListenerImplementation.IDENTITY, mainPanel.currentModelPanel().getPerspArea().getViewport());
                mainPanel.currentModelPanel().getEditorRenderModel().updateNodes(true, false);
                mainPanel.timeSliderPanel.setNodeSelectionManager(
                        mainPanel.currentModelPanel().getModelEditorManager().getNodeAnimationSelectionManager());
            }
            if ((mainPanel.actionTypeGroup.getActiveButtonType() == mainPanel.actionTypeGroup.getToolbarButtonTypes()[3])
                    || (mainPanel.actionTypeGroup
                    .getActiveButtonType() == mainPanel.actionTypeGroup.getToolbarButtonTypes()[4])) {
                mainPanel.actionTypeGroup.setToolbarButtonType(mainPanel.actionTypeGroup.getToolbarButtonTypes()[0]);
            }
        }
        mainPanel.animatedRenderEnvironment.setStaticViewMode(!mainPanel.animationModeState);
        if (!mainPanel.animationModeState) {
            if ((mainPanel.currentModelPanel() != null) && (mainPanel.currentModelPanel().getModel() != null)) {
                mainPanel.currentModelPanel().getEditorRenderModel().refreshFromEditor(mainPanel.animatedRenderEnvironment, ModelStructureChangeListenerImplementation.IDENTITY,
                        ModelStructureChangeListenerImplementation.IDENTITY, ModelStructureChangeListenerImplementation.IDENTITY, mainPanel.currentModelPanel().getPerspArea().getViewport());
                mainPanel.currentModelPanel().getEditorRenderModel().updateNodes(true, false); // update to 0 position
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
                temp = new ModelPanel(mainPanel, model, mainPanel.prefs, mainPanel, mainPanel.selectionItemTypeGroup,
                        mainPanel.selectionModeGroup, mainPanel.modelStructureChangeListener, mainPanel.coordDisplayListener,
                        mainPanel.viewportTransferHandler, mainPanel.activeViewportWatcher, icon, false, mainPanel.textureExporter);
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

                temp = new ModelPanel(mainPanel, model, mainPanel.prefs, mainPanel,
                        mainPanel.selectionItemTypeGroup, mainPanel.selectionModeGroup, mainPanel.modelStructureChangeListener,
                        mainPanel.coordDisplayListener, mainPanel.viewportTransferHandler, mainPanel.activeViewportWatcher, icon,
                        false, mainPanel.textureExporter);
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
        loadModel(mainPanel, temporary, selectNewTab, temp);
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

        loadModel(mainPanel, workingDirectory == null, true,
                new ModelPanel(mainPanel, blankTextureModel, mainPanel.prefs, mainPanel,
                        mainPanel.selectionItemTypeGroup, mainPanel.selectionModeGroup, mainPanel.modelStructureChangeListener,
                        mainPanel.coordDisplayListener, mainPanel.viewportTransferHandler, mainPanel.activeViewportWatcher,
                        RMSIcons.orangeIcon, true, mainPanel.textureExporter));
    }

    public static void loadModel(MainPanel mainPanel, final boolean temporary, final boolean selectNewTab, final ModelPanel temp) {
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
        mainPanel.windowMenu.add(menuItem);
        menuItem.addActionListener(e -> setCurrentModel(mainPanel, modelPanel));
        temp.setJMenuItem(menuItem);
        temp.getModelViewManager().addStateListener(new RepaintingModelStateListener(mainPanel));
        temp.changeActivity(mainPanel.currentActivity);

        if (mainPanel.geoControl == null) {
            mainPanel.geoControl = new JScrollPane(temp.getModelViewManagingTree());
            mainPanel.viewportControllerWindowView.setComponent(mainPanel.geoControl);
            mainPanel.viewportControllerWindowView.repaint();
            mainPanel.geoControlModelData = new JScrollPane(temp.getModelComponentBrowserTree());
            mainPanel.modelDataView.setComponent(mainPanel.geoControlModelData);
            mainPanel.modelComponentView.setComponent(temp.getComponentsPanel());
            mainPanel.modelDataView.repaint();
        }
        addTabForView(temp, selectNewTab);
        mainPanel.modelPanels.add(temp);

        // tabbedPane.addTab(f.getName().split("\\.")[0], icon, temp, f.getPath());
        // if (selectNewTab) {
        // tabbedPane.setSelectedComponent(temp);
        // }
        if (temporary) {
            temp.getModelViewManager().getModel().setFileRef(null);
        }
        // }
        // }).start();
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

    public static void addTabForView(final ModelPanel view, final boolean selectNewTab) {
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
            mainPanel.timeSliderPanel.setUndoManager(mainPanel.currentModelPanel.getUndoManager(),
                    mainPanel.animatedRenderEnvironment);
            mainPanel.timeSliderPanel.setModelView(mainPanel.currentModelPanel.getModelViewManager());
            mainPanel.creatorPanel.setModelEditorManager(mainPanel.currentModelPanel.getModelEditorManager());
            mainPanel.creatorPanel.setCurrentModel(mainPanel.currentModelPanel.getModelViewManager());
            mainPanel.creatorPanel.setUndoManager(mainPanel.currentModelPanel.getUndoManager());

            mainPanel.geoControlModelData.setViewportView(mainPanel.currentModelPanel.getModelComponentBrowserTree());

            mainPanel.modelComponentView.setComponent(mainPanel.currentModelPanel.getComponentsPanel());
            mainPanel.geoControlModelData.repaint();
            mainPanel.currentModelPanel.getModelComponentBrowserTree().reloadFromModelView();
        }
        mainPanel.activeViewportWatcher.viewportChanged(null);
        mainPanel.timeSliderPanel.revalidateKeyframeDisplay();
    }

    public static void loadFile(MainPanel mainPanel, final File f, final boolean temporary) {
        loadFile(mainPanel, f, temporary, true, MDLIcon);
    }

    static View createMPQBrowser(MainPanel mainPanel) {
        return createMPQBrowser(
                mainPanel, new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)));
    }

    static AbstractAction getOpenMPQViewerAction(MainPanel mainPanel) {
        return new AbstractAction("Open MPQ Browser") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final View view = createMPQBrowser(mainPanel);
                mainPanel.rootWindow
                        .setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(), view));
            }
        };
    }

    public static void loadFile(MainPanel mainPanel, final File f) {
        loadFile(mainPanel, f, false);
    }

    static void revertActionRes(MainPanel mainPanel) {
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
        ModelPanel temp = null;
        try {
            final EditableModel model = MdxUtils.loadEditable(f);
            model.setFileRef(null);
            temp = new ModelPanel(mainPanel, model, mainPanel.prefs, mainPanel, mainPanel.selectionItemTypeGroup,
                    mainPanel.selectionModeGroup, mainPanel.modelStructureChangeListener, mainPanel.coordDisplayListener,
                    mainPanel.viewportTransferHandler, mainPanel.activeViewportWatcher, icon, false, mainPanel.textureExporter);
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
        System.out.println(path);
        loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(path), true, b, icon);
        if (mainPanel.prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
            loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false, icon);
        }
    }
}
