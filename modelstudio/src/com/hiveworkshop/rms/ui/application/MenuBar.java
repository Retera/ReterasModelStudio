package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewStateListener;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.parsers.w3o.WTSFile;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.application.tools.EditTexturesPopupPanel;
import com.hiveworkshop.rms.ui.application.viewer.AnimationViewer;
import com.hiveworkshop.rms.ui.application.viewer.perspective.PerspDisplayPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.*;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
import com.hiveworkshop.rms.ui.browsers.mpq.MPQBrowser;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ProgramPreferencesPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarActionButtonType;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TransferActionListener;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.*;
import de.wc3data.stream.BlizzardDataInputStream;
import jassimp.AiPostProcessSteps;
import jassimp.AiScene;
import jassimp.Jassimp;
import net.infonode.docking.*;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class MenuBar {
    static final ImageIcon MDLIcon = RMSIcons.MDLIcon;
    static final Quat IDENTITY = new Quat();
    static final ImageIcon POWERED_BY_HIVE = RMSIcons.loadHiveBrowserImageIcon("powered_by_hive.png");

    static TabWindow createMainLayout(MainPanel mainPanel) {
        final TabWindow leftHandTabWindow = new TabWindow(
                new DockingWindow[]{mainPanel.viewportControllerWindowView, mainPanel.toolView});
        leftHandTabWindow.setSelectedTab(0);
//		leftHandTabWindow.getWindowProperties().setCloseEnabled(false);
        final SplitWindow editingTab = new SplitWindow(false, 0.875f,
                new SplitWindow(true, 0.2f, leftHandTabWindow,
                        new SplitWindow(true, 0.8f,
                                new SplitWindow(false, new SplitWindow(true, mainPanel.frontView, mainPanel.bottomView),
                                        new SplitWindow(true, mainPanel.leftView, mainPanel.perspectiveView)),
                                mainPanel.creatorView)),
                mainPanel.timeSliderView);
        editingTab.getWindowProperties().setCloseEnabled(false);
        editingTab.getWindowProperties().setTitleProvider(arg0 -> "Edit");
        final ImageIcon imageIcon;
        imageIcon = new ImageIcon(MainFrame.MAIN_PROGRAM_ICON.getScaledInstance(16, 16, Image.SCALE_FAST));

        final View mpqBrowserView = createMPQBrowser(mainPanel, imageIcon);

        final UnitEditorTree unitEditorTree = createUnitEditorTree(mainPanel);
        final TabWindow tabWindow = new TabWindow(new DockingWindow[]{
                new View("Unit Browser", imageIcon, new JScrollPane(unitEditorTree)), mpqBrowserView});
        tabWindow.setSelectedTab(0);
        final SplitWindow viewingTab = new SplitWindow(true, 0.8f,
                new SplitWindow(true, 0.8f, mainPanel.previewView, mainPanel.animationControllerView), tabWindow);
        viewingTab.getWindowProperties().setTitleProvider(arg0 -> "View");
        viewingTab.getWindowProperties().setCloseEnabled(false);

        final SplitWindow modelTab = new SplitWindow(true, 0.2f, mainPanel.modelDataView, mainPanel.modelComponentView);
        modelTab.getWindowProperties().setTitleProvider(arg0 -> "Model");

        final TabWindow startupTabWindow = new TabWindow(new DockingWindow[]{viewingTab, editingTab, modelTab});
        traverseAndFix(startupTabWindow);
        return startupTabWindow;
    }

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

    static void traverseAndFix(final DockingWindow window) {
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

    static void traverseAndReset(final DockingWindow window) {
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

    static UnitEditorTree createUnitEditorTree(MainPanel mainPanel) {
        final UnitEditorTree unitEditorTree = new UnitEditorTreeBrowser(getUnitData(), new UnitTabTreeBrowserBuilder(),
                getUnitEditorSettings(), MutableObjectData.WorldEditorDataType.UNITS, (mdxFilePath, b, c, icon) -> loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(mdxFilePath), b, c, icon), mainPanel.prefs);
        return unitEditorTree;
    }

    public static void refreshAnimationModeState(MainPanel mainPanel) {
        if (mainPanel.animationModeState) {
            if ((mainPanel.currentModelPanel() != null) && (mainPanel.currentModelPanel().getModel() != null)) {
                if (mainPanel.currentModelPanel().getModel().getAnimsSize() > 0) {
                    final Animation anim = mainPanel.currentModelPanel().getModel().getAnim(0);
                    mainPanel.animatedRenderEnvironment.setBounds(anim.getStart(), anim.getEnd());
                }
                mainPanel.currentModelPanel().getEditorRenderModel().refreshFromEditor(mainPanel.animatedRenderEnvironment, IDENTITY,
                        IDENTITY, IDENTITY, mainPanel.currentModelPanel().getPerspArea().getViewport());
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
                mainPanel.currentModelPanel().getEditorRenderModel().refreshFromEditor(mainPanel.animatedRenderEnvironment, IDENTITY,
                        IDENTITY, IDENTITY, mainPanel.currentModelPanel().getPerspArea().getViewport());
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

    static void reloadComponentBrowser(JScrollPane geoControlModelData, final ModelPanel display) {
        geoControlModelData.repaint();
        display.getModelComponentBrowserTree().reloadFromModelView();
        geoControlModelData.setViewportView(display.getModelComponentBrowserTree());
    }

    /**
     * Right now this is a plug to the statics to load unit data. However, it's a
     * non-static method so that we can have it load from an opened map in the
     * future -- the MutableObjectData class can parse map unit data!
     */
    public static MutableObjectData getUnitData() {
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
        return new MutableObjectData(MutableObjectData.WorldEditorDataType.UNITS, StandardObjectData.getStandardUnits(),
                StandardObjectData.getStandardUnitMeta(), editorData);
    }

    public static UnitEditorSettings getUnitEditorSettings() {
        return new UnitEditorSettings();
    }

    public static JMenuBar createMenuBar(MainPanel mainPanel) {
        // Create my menu bar
        mainPanel.menuBar = new JMenuBar();

        // Build the file menu
        mainPanel.fileMenu = new JMenu("File");
        mainPanel.fileMenu.setMnemonic(KeyEvent.VK_F);
        mainPanel.fileMenu.getAccessibleContext()
                .setAccessibleDescription("Allows the user to open, save, close, and manipulate files.");
        mainPanel.menuBar.add(mainPanel.fileMenu);

        mainPanel.recentMenu = new JMenu("Open Recent");
        mainPanel.recentMenu.setMnemonic(KeyEvent.VK_R);
        mainPanel.recentMenu.getAccessibleContext().setAccessibleDescription("Allows you to access recently opened files.");

        mainPanel.editMenu = new JMenu("Edit");
        mainPanel.editMenu.setMnemonic(KeyEvent.VK_E);
        // editMenu.addMouseListener(this);
        mainPanel.editMenu.getAccessibleContext()
                .setAccessibleDescription("Allows the user to use various tools to edit the currently selected model.");
        mainPanel.menuBar.add(mainPanel.editMenu);

        mainPanel.toolsMenu = new JMenu("Tools");
        mainPanel.toolsMenu.setMnemonic(KeyEvent.VK_T);
        mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
                "Allows the user to use various model editing tools. (You must open a model before you may use this menu.)");
        mainPanel.toolsMenu.setEnabled(false);
        mainPanel.menuBar.add(mainPanel.toolsMenu);

        mainPanel.viewMenu = new JMenu("View");
        // viewMenu.setMnemonic(KeyEvent.VK_V);
        mainPanel.viewMenu.getAccessibleContext().setAccessibleDescription("Allows the user to control view settings.");
        mainPanel.menuBar.add(mainPanel.viewMenu);

        mainPanel.teamColorMenu = new JMenu("Team Color");
        mainPanel.teamColorMenu.getAccessibleContext()
                .setAccessibleDescription("Allows the user to control team color settings.");
        mainPanel.menuBar.add(mainPanel.teamColorMenu);

        mainPanel.directoryChangeNotifier.subscribe(() -> {
            GameDataFileSystem.refresh(SaveProfile.get().getDataSources());
            // cache priority order...
            UnitOptionPanel.dropRaceCache();
            DataTable.dropCache();
            ModelOptionPanel.dropCache();
            WEString.dropCache();
            BLPHandler.get().dropCache();
            mainPanel.teamColorMenu.removeAll();
            createTeamColorMenuItems(mainPanel);
            traverseAndReloadData(mainPanel.rootWindow);
        });
        createTeamColorMenuItems(mainPanel);

        mainPanel.windowMenu = new JMenu("Window");
        mainPanel.windowMenu.setMnemonic(KeyEvent.VK_W);
        mainPanel.windowMenu.getAccessibleContext()
                .setAccessibleDescription("Allows the user to open various windows containing the program features.");
        mainPanel.menuBar.add(mainPanel.windowMenu);

        fillWindowsMenu(mainPanel);

        mainPanel.addMenu = new JMenu("Add");
        mainPanel.addMenu.setMnemonic(KeyEvent.VK_A);
        mainPanel.addMenu.getAccessibleContext()
                .setAccessibleDescription("Allows the user to add new components to the model.");
        mainPanel.menuBar.add(mainPanel.addMenu);

        fillAddMenu(mainPanel);

        mainPanel.scriptsMenu = new JMenu("Scripts");
        mainPanel.scriptsMenu.setMnemonic(KeyEvent.VK_A);
        mainPanel.scriptsMenu.getAccessibleContext()
                .setAccessibleDescription("Allows the user to execute model edit scripts.");
        mainPanel.menuBar.add(mainPanel.scriptsMenu);

        fillScriptsMenu(mainPanel);

        final JMenuItem fixReteraLand = new JMenuItem("Fix Retera Land");
        fixReteraLand.setMnemonic(KeyEvent.VK_A);
        fixReteraLand.addActionListener(e -> {
            final EditableModel currentMDL = mainPanel.currentMDL();
            for (final Geoset geo : currentMDL.getGeosets()) {
                final Animation anim = new Animation(new ExtLog(currentMDL.getExtents()));
                geo.add(anim);
            }
        });
//		scriptsMenu.add(fixReteraLand);

        mainPanel.aboutMenu = new JMenu("Help");
        mainPanel.aboutMenu.setMnemonic(KeyEvent.VK_H);
        mainPanel.menuBar.add(mainPanel.aboutMenu);

        mainPanel.recentMenu.add(new JSeparator());

        mainPanel.clearRecent = new JMenuItem("Clear");
        mainPanel.clearRecent.setMnemonic(KeyEvent.VK_C);
        mainPanel.clearRecent.addActionListener(mainPanel);
        mainPanel.recentMenu.add(mainPanel.clearRecent);

        updateRecent(mainPanel);

        fillAboutMenu(mainPanel);

        fillToolsMenu(mainPanel);

        fillViewMenu(mainPanel);

        fillFileMenu(mainPanel);


        fillEdidMenu(mainPanel);

        for (int i = 0; i < mainPanel.menuBar.getMenuCount(); i++) {
            mainPanel.menuBar.getMenu(i).getPopupMenu().setLightWeightPopupEnabled(false);
        }
        return mainPanel.menuBar;
    }

    private static void fillWindowsMenu(MainPanel mainPanel) {
        final JMenuItem resetViewButton = new JMenuItem("Reset Layout");
        resetViewButton.addActionListener(e -> {
            traverseAndReset(mainPanel.rootWindow);
            final TabWindow startupTabWindow = createMainLayout(mainPanel);
            mainPanel.rootWindow.setWindow(startupTabWindow);
            traverseAndFix(mainPanel.rootWindow);
        });
        mainPanel.windowMenu.add(resetViewButton);

        final JMenu viewsMenu = new JMenu("Views");
        viewsMenu.setMnemonic(KeyEvent.VK_V);
        mainPanel.windowMenu.add(viewsMenu);

        final JMenuItem testItem = new JMenuItem("test");
        testItem.addActionListener(new OpenViewAction(mainPanel.rootWindow, "Animation Preview", () -> {
            final JPanel testPanel = new JPanel();

            for (int i = 0; i < 3; i++) {
//					final ControlledAnimationViewer animationViewer = new ControlledAnimationViewer(
//							currentModelPanel().getModelViewManager(), prefs);
//					animationViewer.setMinimumSize(new Dimension(400, 400));
//					final AnimationController animationController = new AnimationController(
//							currentModelPanel().getModelViewManager(), true, animationViewer);

                final AnimationViewer animationViewer2 = new AnimationViewer(
                        mainPanel.currentModelPanel().getModelViewManager(), mainPanel.prefs, false);
                animationViewer2.setMinimumSize(new Dimension(400, 400));
                testPanel.add(animationViewer2);
//					testPanel.add(animationController);
            }
            testPanel.setLayout(new GridLayout(1, 4));
            return new View("Test", null, testPanel);
        }));

//		viewsMenu.add(testItem);

        mainPanel.animationViewer = new JMenuItem("Animation Preview");
        mainPanel.animationViewer.setMnemonic(KeyEvent.VK_A);
        mainPanel.animationViewer.addActionListener(getAnimationPreviewAction(mainPanel.rootWindow, "Animation Preview", mainPanel.previewView));
        viewsMenu.add(mainPanel.animationViewer);

        mainPanel.animationController = new JMenuItem("Animation Controller");
        mainPanel.animationController.setMnemonic(KeyEvent.VK_C);
        mainPanel.animationController.addActionListener(getAnimationPreviewAction(mainPanel.rootWindow, "Animation Controller", mainPanel.animationControllerView));
        viewsMenu.add(mainPanel.animationController);

        mainPanel.modelingTab = new JMenuItem("Modeling");
        mainPanel.modelingTab.setMnemonic(KeyEvent.VK_M);
        mainPanel.modelingTab.addActionListener(getAnimationPreviewAction(mainPanel.rootWindow, "Modeling", mainPanel.creatorView));
        viewsMenu.add(mainPanel.modelingTab);

        final JMenuItem outlinerItem = new JMenuItem("Outliner");
        outlinerItem.setMnemonic(KeyEvent.VK_O);
        outlinerItem.addActionListener(getAnimationPreviewAction(mainPanel.rootWindow, "Outliner", mainPanel.viewportControllerWindowView));
        viewsMenu.add(outlinerItem);

        final JMenuItem perspectiveItem = new JMenuItem("Perspective");
        perspectiveItem.setMnemonic(KeyEvent.VK_P);
        perspectiveItem.addActionListener(getAnimationPreviewAction(mainPanel.rootWindow, "Perspective", mainPanel.perspectiveView));
        viewsMenu.add(perspectiveItem);

        final JMenuItem frontItem = new JMenuItem("Front");
        frontItem.setMnemonic(KeyEvent.VK_F);
        frontItem.addActionListener(getAnimationPreviewAction(mainPanel.rootWindow, "Front", mainPanel.frontView));
        viewsMenu.add(frontItem);

        final JMenuItem sideItem = new JMenuItem("Side");
        sideItem.setMnemonic(KeyEvent.VK_S);
        sideItem.addActionListener(getAnimationPreviewAction(mainPanel.rootWindow, "Side", mainPanel.leftView));
        viewsMenu.add(sideItem);

        final JMenuItem bottomItem = new JMenuItem("Bottom");
        bottomItem.setMnemonic(KeyEvent.VK_B);
        bottomItem.addActionListener(getAnimationPreviewAction(mainPanel.rootWindow, "Bottom", mainPanel.bottomView));
        viewsMenu.add(bottomItem);

        final JMenuItem toolsItem = new JMenuItem("Tools");
        toolsItem.setMnemonic(KeyEvent.VK_T);
        toolsItem.addActionListener(getAnimationPreviewAction(mainPanel.rootWindow, "Tools", mainPanel.toolView));
        viewsMenu.add(toolsItem);

        final JMenuItem contentsItem = new JMenuItem("Contents");
        contentsItem.setMnemonic(KeyEvent.VK_C);
        contentsItem.addActionListener(getAnimationPreviewAction(mainPanel.rootWindow, "Model", mainPanel.modelDataView));
        viewsMenu.add(contentsItem);

        final JMenuItem timeItem = new JMenuItem("Footer");
        timeItem.addActionListener(getAnimationPreviewAction(mainPanel.rootWindow, "Footer", mainPanel.timeSliderView));
        viewsMenu.add(timeItem);

        final JMenuItem hackerViewItem = new JMenuItem("Matrix Eater Script");
        hackerViewItem.setMnemonic(KeyEvent.VK_H);
        hackerViewItem.setAccelerator(KeyStroke.getKeyStroke("control P"));
        hackerViewItem.addActionListener(getAnimationPreviewAction(mainPanel.rootWindow, "Matrix Eater Script", mainPanel.hackerView));
        viewsMenu.add(hackerViewItem);

        final JMenu browsersMenu = new JMenu("Browsers");
        browsersMenu.setMnemonic(KeyEvent.VK_B);
        mainPanel.windowMenu.add(browsersMenu);

        mainPanel.mpqViewer = new JMenuItem("Data Browser");
        mainPanel.mpqViewer.setMnemonic(KeyEvent.VK_A);
        mainPanel.mpqViewer.addActionListener(getOpenMPQViewerAction(mainPanel));
        browsersMenu.add(mainPanel.mpqViewer);

        mainPanel.unitViewer = new JMenuItem("Unit Browser");
        mainPanel.unitViewer.setMnemonic(KeyEvent.VK_U);
        mainPanel.unitViewer.addActionListener(getOpenUnitViewerAction(mainPanel));
        browsersMenu.add(mainPanel.unitViewer);

        final JMenuItem doodadViewer = new JMenuItem("Doodad Browser");
        doodadViewer.setMnemonic(KeyEvent.VK_D);
        doodadViewer.addActionListener(getOpenDoodadViewerAction(mainPanel));
        browsersMenu.add(doodadViewer);

        mainPanel.hiveViewer = new JMenuItem("Hive Browser");
        mainPanel.hiveViewer.setMnemonic(KeyEvent.VK_H);
        mainPanel.hiveViewer.addActionListener(getOpenHiveViewerAction(mainPanel));
//		browsersMenu.add(hiveViewer);

        mainPanel.windowMenu.addSeparator();
    }

    private static void fillAddMenu(final MainPanel mainPanel) {
        mainPanel.addParticle = new JMenu("Particle");
        mainPanel.addParticle.setMnemonic(KeyEvent.VK_P);
        mainPanel.addMenu.add(mainPanel.addParticle);

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
                                final List<IdObject> idObjects = new ArrayList<>(mainPanel.currentMDL().getIdObjects());
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
                                final List<Animation> anims = mainPanel.currentMDL().getAnims();
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
                                        final Color colorChoice = JColorChooser.showDialog(mainPanel,
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
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
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
                                        layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(imageLabel)
                                                .addGroup(
                                                        layout.createSequentialGroup().addComponent(titleLabel)
                                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                                        .addComponent(nameLabel).addComponent(nameField))
                                                                .addGap(4)
                                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                                        .addComponent(parentLabel).addComponent(parent))
                                                                .addGap(4).addComponent(chooseAnimations).addGap(4)
                                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                                        .addComponent(xLabel).addComponent(xSpinner)
                                                                        .addComponent(yLabel).addComponent(ySpinner)
                                                                        .addComponent(zLabel).addComponent(zSpinner))
                                                                .addGap(4)
                                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                                        .addComponent(colorButtons[0])
                                                                        .addComponent(colorButtons[1])
                                                                        .addComponent(colorButtons[2]))));
                                particlePanel.setLayout(layout);
                                final int x = JOptionPane.showConfirmDialog(mainPanel, particlePanel,
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
                                    mainPanel.currentMDL().add(particle);
                                    mainPanel.modelStructureChangeListener
                                            .nodesAdded(Collections.singletonList(particle));
                                }
                            }
                        });
                        mainPanel.addParticle.add(particleItem);
                    } catch (final IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        mainPanel.animationMenu = new JMenu("Animation");
        mainPanel.animationMenu.setMnemonic(KeyEvent.VK_A);
        mainPanel.addMenu.add(mainPanel.animationMenu);

        mainPanel.riseFallBirth = new JMenuItem("Rising/Falling Birth/Death");
        mainPanel.riseFallBirth.setMnemonic(KeyEvent.VK_R);
        mainPanel.riseFallBirth.addActionListener(mainPanel);
        mainPanel.animationMenu.add(mainPanel.riseFallBirth);

        mainPanel.singleAnimationMenu = new JMenu("Single");
        mainPanel.singleAnimationMenu.setMnemonic(KeyEvent.VK_S);
        mainPanel.animationMenu.add(mainPanel.singleAnimationMenu);

        mainPanel.animFromFile = new JMenuItem("From File");
        mainPanel.animFromFile.setMnemonic(KeyEvent.VK_F);
        mainPanel.animFromFile.addActionListener(mainPanel);
        mainPanel.singleAnimationMenu.add(mainPanel.animFromFile);

        mainPanel.animFromUnit = new JMenuItem("From Unit");
        mainPanel.animFromUnit.setMnemonic(KeyEvent.VK_U);
        mainPanel.animFromUnit.addActionListener(mainPanel);
        mainPanel.singleAnimationMenu.add(mainPanel.animFromUnit);

        mainPanel.animFromModel = new JMenuItem("From Model");
        mainPanel.animFromModel.setMnemonic(KeyEvent.VK_M);
        mainPanel.animFromModel.addActionListener(mainPanel);
        mainPanel.singleAnimationMenu.add(mainPanel.animFromModel);

        mainPanel.animFromObject = new JMenuItem("From Object");
        mainPanel.animFromObject.setMnemonic(KeyEvent.VK_O);
        mainPanel.animFromObject.addActionListener(mainPanel);
        mainPanel.singleAnimationMenu.add(mainPanel.animFromObject);
    }

    private static void fillScriptsMenu(MainPanel mainPanel) {
        mainPanel.importButtonS = new JMenuItem("Oinkerwinkle-Style AnimTransfer");
        mainPanel.importButtonS.setAccelerator(KeyStroke.getKeyStroke("control shift S"));
        mainPanel.importButtonS.setMnemonic(KeyEvent.VK_P);
        mainPanel.importButtonS.addActionListener(mainPanel);
        // importButtonS.setEnabled(false);
        mainPanel.scriptsMenu.add(mainPanel.importButtonS);

        mainPanel.mergeGeoset = new JMenuItem("Oinkerwinkle-Style Merge Geoset");
        mainPanel.mergeGeoset.setAccelerator(KeyStroke.getKeyStroke("control M"));
        mainPanel.mergeGeoset.setMnemonic(KeyEvent.VK_M);
        mainPanel.mergeGeoset.addActionListener(mainPanel);
        mainPanel.scriptsMenu.add(mainPanel.mergeGeoset);

        mainPanel.nullmodelButton = new JMenuItem("Edit/delete model components");
        mainPanel.nullmodelButton.setAccelerator(KeyStroke.getKeyStroke("control E"));
        mainPanel.nullmodelButton.setMnemonic(KeyEvent.VK_E);
        mainPanel.nullmodelButton.addActionListener(mainPanel);
        mainPanel.scriptsMenu.add(mainPanel.nullmodelButton);

        mainPanel.exportAnimatedToStaticMesh = new JMenuItem("Export Animated to Static Mesh");
        mainPanel.exportAnimatedToStaticMesh.setMnemonic(KeyEvent.VK_E);
        mainPanel.exportAnimatedToStaticMesh.addActionListener(e -> {
            if (!mainPanel.animationModeState) {
                JOptionPane.showMessageDialog(mainPanel, "You must be in the Animation Editor to use that!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            final Vec4 vertexHeap = new Vec4();
            final Vec4 appliedVertexHeap = new Vec4();
            final Vec4 vertexSumHeap = new Vec4();
            final Vec4 normalHeap = new Vec4();
            final Vec4 appliedNormalHeap = new Vec4();
            final Vec4 normalSumHeap = new Vec4();
            final ModelPanel modelContext = mainPanel.currentModelPanel();
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
                        final Object value = flag.interpolateAt(mainPanel.animatedRenderEnvironment);
                        flag.setInterpType(InterpolationType.DONT_INTERP);
                        flag.getValues().clear();
                        flag.getTimes().clear();
                        flag.getInTans().clear();
                        flag.getOutTans().clear();
                        flag.addEntry(333, value);
                    }
                }
            }
            mainPanel.fc.setDialogTitle("Export Static Snapshot");
            final int result = mainPanel.fc.showSaveDialog(mainPanel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = mainPanel.fc.getSelectedFile();
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
        mainPanel.scriptsMenu.add(mainPanel.exportAnimatedToStaticMesh);

        mainPanel.exportAnimatedFramePNG = new JMenuItem("Export Animated Frame PNG");
        mainPanel.exportAnimatedFramePNG.setMnemonic(KeyEvent.VK_F);
        mainPanel.exportAnimatedFramePNG.addActionListener(e -> {
            final BufferedImage fBufferedImage = mainPanel.currentModelPanel().getAnimationViewer().getBufferedImage();

            if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
                final EditableModel current = mainPanel.currentMDL();
                if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                    mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
                } else if (mainPanel.profile.getPath() != null) {
                    mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
                }
            }
            if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
                mainPanel.exportTextureDialog.setSelectedFile(
                        new File(mainPanel.exportTextureDialog.getCurrentDirectory() + File.separator));
            }

            final int x = mainPanel.exportTextureDialog.showSaveDialog(mainPanel);
            if (x == JFileChooser.APPROVE_OPTION) {
                final File file = mainPanel.exportTextureDialog.getSelectedFile();
                if (file != null) {
                    try {
                        if (file.getName().lastIndexOf('.') >= 0) {
                            BufferedImage bufferedImage = fBufferedImage;
                            String fileExtension = file.getName().substring(file.getName().lastIndexOf('.') + 1)
                                    .toUpperCase();
                            if (fileExtension.equals("BMP") || fileExtension.equals("JPG")
                                    || fileExtension.equals("JPEG")) {
                                JOptionPane.showMessageDialog(mainPanel,
                                        "Warning: Alpha channel was converted to black. Some data will be lost\nif you convert this texture back to Warcraft BLP.");
                                bufferedImage = BLPHandler.removeAlphaChannel(bufferedImage);
                            }
                            if (fileExtension.equals("BLP")) {
                                fileExtension = "blp";
                            }
                            final boolean write = ImageIO.write(bufferedImage, fileExtension, file);
                            if (!write) {
                                JOptionPane.showMessageDialog(mainPanel, "File type unknown or unavailable");
                            }
                        } else {
                            JOptionPane.showMessageDialog(mainPanel, "No file type was specified");
                        }
                    } catch (final IOException e1) {
                        ExceptionPopup.display(e1);
                        e1.printStackTrace();
                    } catch (final Exception e2) {
                        ExceptionPopup.display(e2);
                        e2.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(mainPanel, "No output file was specified");
                }
            }
        });
        mainPanel.scriptsMenu.add(mainPanel.exportAnimatedFramePNG);

        mainPanel.combineAnims = new JMenuItem("Create Back2Back Animation");
        mainPanel.combineAnims.setMnemonic(KeyEvent.VK_P);
        mainPanel.combineAnims.addActionListener(e -> {
            final List<Animation> anims = mainPanel.currentMDL().getAnims();
            final Animation[] array = anims.toArray(new Animation[0]);
            final Object choice = JOptionPane.showInputDialog(mainPanel, "Pick the first animation",
                    "Choose 1st Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
            final Animation animation = (Animation) choice;

            final Object choice2 = JOptionPane.showInputDialog(mainPanel, "Pick the second animation",
                    "Choose 2nd Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
            final Animation animation2 = (Animation) choice2;

            final String nameChoice = JOptionPane.showInputDialog(mainPanel,
                    "What should the combined animation be called?");
            if (nameChoice != null) {
                final int anim1Length = animation.getEnd() - animation.getStart();
                final int anim2Length = animation2.getEnd() - animation2.getStart();
                final int totalLength = anim1Length + anim2Length;

                final EditableModel model = mainPanel.currentMDL();
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
                JOptionPane.showMessageDialog(mainPanel,
                        "DONE! Made a combined animation called " + newAnimation.getName(), "Success",
                        JOptionPane.PLAIN_MESSAGE);
            }
        });
        mainPanel.scriptsMenu.add(mainPanel.combineAnims);

        mainPanel.scaleAnimations = new JMenuItem("Change Animation Lengths by Scaling");
        mainPanel.scaleAnimations.setMnemonic(KeyEvent.VK_A);
        mainPanel.scaleAnimations.addActionListener(mainPanel);
        mainPanel.scriptsMenu.add(mainPanel.scaleAnimations);

        final JMenuItem version800Toggle = new JMenuItem("Assign FormatVersion 800");
        version800Toggle.setMnemonic(KeyEvent.VK_A);
        version800Toggle.addActionListener(e -> mainPanel.currentMDL().setFormatVersion(800));
        mainPanel.scriptsMenu.add(version800Toggle);

        final JMenuItem version1000Toggle = new JMenuItem("Assign FormatVersion 1000");
        version1000Toggle.setMnemonic(KeyEvent.VK_A);
        version1000Toggle.addActionListener(e -> mainPanel.currentMDL().setFormatVersion(1000));
        mainPanel.scriptsMenu.add(version1000Toggle);

        final JMenuItem makeItHDItem = new JMenuItem("SD -> HD (highly experimental, requires 900 or 1000)");
        makeItHDItem.setMnemonic(KeyEvent.VK_A);
        makeItHDItem.addActionListener(e -> EditableModel.makeItHD(mainPanel.currentMDL()));
        mainPanel.scriptsMenu.add(makeItHDItem);

        final JMenuItem version800EditingToggle = new JMenuItem("HD -> SD (highly experimental, becomes 800)");
        version800EditingToggle.setMnemonic(KeyEvent.VK_A);
        version800EditingToggle.addActionListener(e -> EditableModel.convertToV800(1, mainPanel.currentMDL()));
        mainPanel.scriptsMenu.add(version800EditingToggle);

        final JMenuItem recalculateTangents = new JMenuItem("Recalculate Tangents (requires 900 or 1000)");
        recalculateTangents.setMnemonic(KeyEvent.VK_A);
        recalculateTangents.addActionListener(e -> EditableModel.recalculateTangents(mainPanel.currentMDL(), mainPanel));
        mainPanel.scriptsMenu.add(recalculateTangents);

        final JMenuItem jokebutton = new JMenuItem("Load Retera Land");
        jokebutton.setMnemonic(KeyEvent.VK_A);
        jokebutton.addActionListener(e -> {
            jokeButtonActionResponse(mainPanel);
        });
//		scriptsMenu.add(jokebutton);
    }

    private static void jokeButtonActionResponse(MainPanel mainPanel) {
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
                } catch (final IOException e1) {
                    e1.printStackTrace();
                }
                final String dataString = sb.toString();
                for (int i = 0; (i + 23) < dataString.length(); i += 24) {
                    final Geoset geo = new Geoset();
                    mainPanel.currentMDL().addGeoset(geo);
                    geo.setParentModel(mainPanel.currentMDL());
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

                    final ModelUtils.Mesh mesh = ModelUtils.createBox(new Vec3(x * 10, y * 10, z * 10),
                            new Vec3((x * 10) + (sX * 10), (y * 10) + (sY * 10), (z * 10) + (sZ * 10)), 1, 1,
                            1, geo);
                    geo.getVertices().addAll(mesh.getVertices());
                    geo.getTriangles().addAll(mesh.getTriangles());
                }
            }

        }
        mainPanel.modelStructureChangeListener.geosetsAdded(new ArrayList<>(mainPanel.currentMDL().getGeosets()));
    }

    private static void fillAboutMenu(MainPanel mainPanel) {
        mainPanel.changelogButton = new JMenuItem("Changelog");
        mainPanel.changelogButton.setMnemonic(KeyEvent.VK_A);
        mainPanel.changelogButton.addActionListener(mainPanel);
        mainPanel.aboutMenu.add(mainPanel.changelogButton);

        mainPanel.creditsButton = new JMenuItem("About");
        mainPanel.creditsButton.setMnemonic(KeyEvent.VK_A);
        mainPanel.creditsButton.addActionListener(mainPanel);
        mainPanel.aboutMenu.add(mainPanel.creditsButton);

        mainPanel.jokeButton = new JMenuItem("HTML Magic");
        mainPanel.jokeButton.setMnemonic(KeyEvent.VK_H);
        mainPanel.jokeButton.addActionListener(e -> {
            final JEditorPane jEditorPane;
            try {
                jEditorPane = new JEditorPane(new URL("http://79.179.129.227:8080/clients/editor/"));
                final JFrame testFrame = new JFrame("Test");
                testFrame.setContentPane(jEditorPane);
                testFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                testFrame.pack();
                testFrame.setLocationRelativeTo(mainPanel.nullmodelButton);
                testFrame.setVisible(true);
            } catch (final MalformedURLException e1) {
                e1.printStackTrace();
            } catch (final IOException e1) {
                e1.printStackTrace();
            }
        });
        mainPanel.aboutMenu.add(mainPanel.jokeButton);
    }

    private static void fillToolsMenu(MainPanel mainPanel) {
        mainPanel.showMatrices = new JMenuItem("View Selected \"Matrices\"");
        // showMatrices.setMnemonic(KeyEvent.VK_V);
        mainPanel.showMatrices.addActionListener(getViewMatricesAction(mainPanel));
        mainPanel.toolsMenu.add(mainPanel.showMatrices);

        mainPanel.insideOut = new JMenuItem("Flip all selected faces");
        mainPanel.insideOut.setMnemonic(KeyEvent.VK_I);
        mainPanel.insideOut.addActionListener(getInsideOutAction(mainPanel));
        mainPanel.insideOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));
        mainPanel.toolsMenu.add(mainPanel.insideOut);

        mainPanel.insideOutNormals = new JMenuItem("Flip all selected normals");
        mainPanel.insideOutNormals.addActionListener(getInsideOutNormalsAction(mainPanel));
        mainPanel.toolsMenu.add(mainPanel.insideOutNormals);

        mainPanel.toolsMenu.add(new JSeparator());

        mainPanel.editUVs = new JMenuItem("Edit UV Mapping");
        mainPanel.editUVs.setMnemonic(KeyEvent.VK_U);
        mainPanel.editUVs.addActionListener(mainPanel);
        mainPanel.toolsMenu.add(mainPanel.editUVs);

        mainPanel.editTextures = new JMenuItem("Edit Textures");
        mainPanel.editTextures.setMnemonic(KeyEvent.VK_T);
        mainPanel.editTextures.addActionListener(e -> {
            final EditTexturesPopupPanel textureManager = new EditTexturesPopupPanel(mainPanel.currentModelPanel().getModelViewManager(),
                    mainPanel.modelStructureChangeListener, mainPanel.textureExporter);
            final JFrame frame = new JFrame("Edit Textures");
            textureManager.setSize(new Dimension(800, 650));
            frame.setContentPane(textureManager);
            frame.setSize(textureManager.getSize());
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
        });
        mainPanel.toolsMenu.add(mainPanel.editTextures);

        mainPanel.rigButton = new JMenuItem("Rig Selection");
        mainPanel.rigButton.setMnemonic(KeyEvent.VK_R);
        mainPanel.rigButton.setAccelerator(KeyStroke.getKeyStroke("control W"));
        mainPanel.rigButton.addActionListener(mainPanel.rigAction);
        mainPanel.toolsMenu.add(mainPanel.rigButton);

        mainPanel.tweaksSubmenu = new JMenu("Tweaks");
        mainPanel.tweaksSubmenu.setMnemonic(KeyEvent.VK_T);
        mainPanel.tweaksSubmenu.getAccessibleContext()
                .setAccessibleDescription("Allows the user to tweak conversion mistakes.");
        mainPanel.toolsMenu.add(mainPanel.tweaksSubmenu);

        mainPanel.flipAllUVsU = new JMenuItem("Flip All UVs U");
        mainPanel.flipAllUVsU.setMnemonic(KeyEvent.VK_U);
        mainPanel.flipAllUVsU.addActionListener(getFlipAllUVsUAction(mainPanel));
        mainPanel.tweaksSubmenu.add(mainPanel.flipAllUVsU);

        mainPanel.flipAllUVsV = new JMenuItem("Flip All UVs V");
        // flipAllUVsV.setMnemonic(KeyEvent.VK_V);
        mainPanel.flipAllUVsV.addActionListener(getFlipAllUVsVAction(mainPanel));
        mainPanel.tweaksSubmenu.add(mainPanel.flipAllUVsV);

        mainPanel.inverseAllUVs = new JMenuItem("Swap All UVs U for V");
        mainPanel.inverseAllUVs.setMnemonic(KeyEvent.VK_S);
        mainPanel.inverseAllUVs.addActionListener(getInverseAllUVsAction(mainPanel));
        mainPanel.tweaksSubmenu.add(mainPanel.inverseAllUVs);

        mainPanel.mirrorSubmenu = new JMenu("Mirror");
        mainPanel.mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
        mainPanel.mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");
        mainPanel.toolsMenu.add(mainPanel.mirrorSubmenu);

        mainPanel.mirrorX = new JMenuItem("Mirror X");
        mainPanel.mirrorX.setMnemonic(KeyEvent.VK_X);
        mainPanel.mirrorX.addActionListener(getMirrorAxisAction(mainPanel, "Mirror X", (byte) 0));
        mainPanel.mirrorSubmenu.add(mainPanel.mirrorX);

        mainPanel.mirrorY = new JMenuItem("Mirror Y");
        mainPanel.mirrorY.setMnemonic(KeyEvent.VK_Y);
        mainPanel.mirrorY.addActionListener(getMirrorAxisAction(mainPanel, "Mirror Y", (byte) 1));
        mainPanel.mirrorSubmenu.add(mainPanel.mirrorY);

        mainPanel.mirrorZ = new JMenuItem("Mirror Z");
        mainPanel.mirrorZ.setMnemonic(KeyEvent.VK_Z);
        mainPanel.mirrorZ.addActionListener(getMirrorAxisAction(mainPanel, "Mirror Z", (byte) 2));
        mainPanel.mirrorSubmenu.add(mainPanel.mirrorZ);

        mainPanel.mirrorSubmenu.add(new JSeparator());

        mainPanel.mirrorFlip = new JCheckBoxMenuItem("Automatically flip after mirror (preserves surface)", true);
        mainPanel.mirrorFlip.setMnemonic(KeyEvent.VK_A);
        mainPanel.mirrorSubmenu.add(mainPanel.mirrorFlip);
    }

    private static void fillViewMenu(MainPanel mainPanel) {
        mainPanel.textureModels = new JCheckBoxMenuItem("Texture Models", true);
        mainPanel.textureModels.setMnemonic(KeyEvent.VK_T);
        mainPanel.textureModels.setSelected(true);
        mainPanel.textureModels.addActionListener(mainPanel);
        mainPanel.viewMenu.add(mainPanel.textureModels);

        mainPanel.newDirectory = new JMenuItem("Change Game Directory");
        mainPanel.newDirectory.setAccelerator(KeyStroke.getKeyStroke("control shift D"));
        mainPanel.newDirectory.setToolTipText("Changes the directory from which to load texture files for the 3D display.");
        mainPanel.newDirectory.setMnemonic(KeyEvent.VK_D);
        mainPanel.newDirectory.addActionListener(mainPanel);
//		viewMenu.add(newDirectory);

        mainPanel.viewMenu.add(new JSeparator());

        mainPanel.showVertexModifyControls = new JCheckBoxMenuItem("Show Viewport Buttons", true);
        // showVertexModifyControls.setMnemonic(KeyEvent.VK_V);
        mainPanel.showVertexModifyControls.addActionListener(mainPanel);
        mainPanel.viewMenu.add(mainPanel.showVertexModifyControls);

        mainPanel.viewMenu.add(new JSeparator());

        mainPanel.showNormals = new JCheckBoxMenuItem("Show Normals", true);
        mainPanel.showNormals.setMnemonic(KeyEvent.VK_N);
        mainPanel.showNormals.setSelected(false);
        mainPanel.showNormals.addActionListener(mainPanel);
        mainPanel.viewMenu.add(mainPanel.showNormals);

        mainPanel.viewMode = new JMenu("3D View Mode");
        mainPanel.viewMenu.add(mainPanel.viewMode);

        mainPanel.viewModes = new ButtonGroup();

        final ActionListener repainter = e -> {
            if (mainPanel.wireframe.isSelected()) {
                mainPanel.prefs.setViewMode(0);
            } else if (mainPanel.solid.isSelected()) {
                mainPanel.prefs.setViewMode(1);
            } else {
                mainPanel.prefs.setViewMode(-1);
            }
            mainPanel.repaint();
        };

        mainPanel.wireframe = new JRadioButtonMenuItem("Wireframe");
        mainPanel.wireframe.addActionListener(repainter);
        mainPanel.viewMode.add(mainPanel.wireframe);
        mainPanel.viewModes.add(mainPanel.wireframe);

        mainPanel.solid = new JRadioButtonMenuItem("Solid");
        mainPanel.solid.addActionListener(repainter);
        mainPanel.viewMode.add(mainPanel.solid);
        mainPanel.viewModes.add(mainPanel.solid);

        mainPanel.viewModes.setSelected(mainPanel.solid.getModel(), true);
    }

    private static void fillFileMenu(MainPanel mainPanel) {
        mainPanel.newModel = new JMenuItem("New");
        mainPanel.newModel.setAccelerator(KeyStroke.getKeyStroke("control N"));
        mainPanel.newModel.setMnemonic(KeyEvent.VK_N);
        mainPanel.newModel.addActionListener(mainPanel);
        mainPanel.fileMenu.add(mainPanel.newModel);

        mainPanel.open = new JMenuItem("Open");
        mainPanel.open.setAccelerator(KeyStroke.getKeyStroke("control O"));
        mainPanel.open.setMnemonic(KeyEvent.VK_O);
        mainPanel.open.addActionListener(mainPanel);
        mainPanel.fileMenu.add(mainPanel.open);

        mainPanel.fileMenu.add(mainPanel.recentMenu);

        mainPanel.fetch = new JMenu("Open Internal");
        mainPanel.fetch.setMnemonic(KeyEvent.VK_F);
        mainPanel.fileMenu.add(mainPanel.fetch);

        mainPanel.fetchUnit = new JMenuItem("Unit");
        mainPanel.fetchUnit.setAccelerator(KeyStroke.getKeyStroke("control U"));
        mainPanel.fetchUnit.setMnemonic(KeyEvent.VK_U);
        mainPanel.fetchUnit.addActionListener(mainPanel);
        mainPanel.fetch.add(mainPanel.fetchUnit);

        mainPanel.fetchModel = new JMenuItem("Model");
        mainPanel.fetchModel.setAccelerator(KeyStroke.getKeyStroke("control M"));
        mainPanel.fetchModel.setMnemonic(KeyEvent.VK_M);
        mainPanel.fetchModel.addActionListener(mainPanel);
        mainPanel.fetch.add(mainPanel.fetchModel);

        mainPanel.fetchObject = new JMenuItem("Object Editor");
        mainPanel.fetchObject.setAccelerator(KeyStroke.getKeyStroke("control O"));
        mainPanel.fetchObject.setMnemonic(KeyEvent.VK_O);
        mainPanel.fetchObject.addActionListener(mainPanel);
        mainPanel.fetch.add(mainPanel.fetchObject);

        mainPanel.fetch.add(new JSeparator());

        mainPanel.fetchPortraitsToo = new JCheckBoxMenuItem("Fetch portraits, too!", true);
        mainPanel.fetchPortraitsToo.setMnemonic(KeyEvent.VK_P);
        mainPanel.fetchPortraitsToo.setSelected(true);
        mainPanel.fetchPortraitsToo.addActionListener(e -> mainPanel.prefs.setLoadPortraits(mainPanel.fetchPortraitsToo.isSelected()));
        mainPanel.fetch.add(mainPanel.fetchPortraitsToo);

        mainPanel.fileMenu.add(new JSeparator());

        mainPanel.importMenu = new JMenu("Import");
        mainPanel.importMenu.setMnemonic(KeyEvent.VK_I);
        mainPanel.fileMenu.add(mainPanel.importMenu);

        mainPanel.importButton = new JMenuItem("From File");
        mainPanel.importButton.setAccelerator(KeyStroke.getKeyStroke("control shift I"));
        mainPanel.importButton.setMnemonic(KeyEvent.VK_I);
        mainPanel.importButton.addActionListener(mainPanel);
        mainPanel.importMenu.add(mainPanel.importButton);

        mainPanel.importUnit = new JMenuItem("From Unit");
        mainPanel.importUnit.setMnemonic(KeyEvent.VK_U);
        mainPanel.importUnit.setAccelerator(KeyStroke.getKeyStroke("control shift U"));
        mainPanel.importUnit.addActionListener(mainPanel);
        mainPanel.importMenu.add(mainPanel.importUnit);

        mainPanel.importGameModel = new JMenuItem("From WC3 Model");
        mainPanel.importGameModel.setMnemonic(KeyEvent.VK_M);
        mainPanel.importGameModel.addActionListener(mainPanel);
        mainPanel.importMenu.add(mainPanel.importGameModel);

        mainPanel.importGameObject = new JMenuItem("From Object Editor");
        mainPanel.importGameObject.setMnemonic(KeyEvent.VK_O);
        mainPanel.importGameObject.addActionListener(mainPanel);
        mainPanel.importMenu.add(mainPanel.importGameObject);

        mainPanel.importFromWorkspace = new JMenuItem("From Workspace");
        mainPanel.importFromWorkspace.setMnemonic(KeyEvent.VK_O);
        mainPanel.importFromWorkspace.addActionListener(mainPanel);
        mainPanel.importMenu.add(mainPanel.importFromWorkspace);

        mainPanel.save = new JMenuItem("Save");
        mainPanel.save.setMnemonic(KeyEvent.VK_S);
        mainPanel.save.setAccelerator(KeyStroke.getKeyStroke("control S"));
        mainPanel.save.addActionListener(mainPanel);
        mainPanel.fileMenu.add(mainPanel.save);

        mainPanel.saveAs = new JMenuItem("Save as");
        mainPanel.saveAs.setMnemonic(KeyEvent.VK_A);
        mainPanel.saveAs.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        mainPanel.saveAs.addActionListener(mainPanel);
        mainPanel.fileMenu.add(mainPanel.saveAs);

        mainPanel.fileMenu.add(new JSeparator());

        mainPanel.exportTextures = new JMenuItem("Export Texture");
        mainPanel.exportTextures.setMnemonic(KeyEvent.VK_E);
        mainPanel.exportTextures.addActionListener(mainPanel);
        mainPanel.fileMenu.add(mainPanel.exportTextures);

        mainPanel.fileMenu.add(new JSeparator());

        mainPanel.revert = new JMenuItem("Revert");
        mainPanel.revert.addActionListener(e -> {
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
        });
        mainPanel.fileMenu.add(mainPanel.revert);

        mainPanel.close = new JMenuItem("Close");
        mainPanel.close.setAccelerator(KeyStroke.getKeyStroke("control E"));
        mainPanel.close.setMnemonic(KeyEvent.VK_E);
        mainPanel.close.addActionListener(mainPanel);
        mainPanel.fileMenu.add(mainPanel.close);

        mainPanel.fileMenu.add(new JSeparator());

        mainPanel.exit = new JMenuItem("Exit");
        mainPanel.exit.setMnemonic(KeyEvent.VK_E);
        mainPanel.exit.addActionListener(e -> {
            if (closeAll(mainPanel)) {
                MainFrame.frame.dispose();
            }
        });
        mainPanel.fileMenu.add(mainPanel.exit);
    }

    private static void fillEdidMenu(final MainPanel mainPanel) {
        mainPanel.undo = new UndoMenuItem(mainPanel, "Undo");
        mainPanel.undo.addActionListener(mainPanel.undoAction);
        mainPanel.undo.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        // undo.addMouseListener(this);
        mainPanel.editMenu.add(mainPanel.undo);
        mainPanel.undo.setEnabled(mainPanel.undo.funcEnabled());

        mainPanel.redo = new RedoMenuItem(mainPanel, "Redo");
        mainPanel.redo.addActionListener(mainPanel.redoAction);
        mainPanel.redo.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        // redo.addMouseListener(this);
        mainPanel.editMenu.add(mainPanel.redo);
        mainPanel.redo.setEnabled(mainPanel.redo.funcEnabled());

        mainPanel.editMenu.add(new JSeparator());

        final JMenu optimizeMenu = new JMenu("Optimize");
        optimizeMenu.setMnemonic(KeyEvent.VK_O);
        mainPanel.editMenu.add(optimizeMenu);

        mainPanel.linearizeAnimations = new JMenuItem("Linearize Animations");
        mainPanel.linearizeAnimations.setMnemonic(KeyEvent.VK_L);
        mainPanel.linearizeAnimations.addActionListener(mainPanel);
        optimizeMenu.add(mainPanel.linearizeAnimations);

        mainPanel.simplifyKeyframes = new JMenuItem("Simplify Keyframes (Experimental)");
        mainPanel.simplifyKeyframes.setMnemonic(KeyEvent.VK_K);
        mainPanel.simplifyKeyframes.addActionListener(mainPanel);
        optimizeMenu.add(mainPanel.simplifyKeyframes);

        final JMenuItem minimizeGeoset = new JMenuItem("Minimize Geosets");
        minimizeGeoset.setMnemonic(KeyEvent.VK_K);
        minimizeGeoset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int confirm = JOptionPane.showConfirmDialog(mainPanel,
                        "This is experimental and I did not code the Undo option for it yet. Continue?\nMy advice is to click cancel and save once first.",
                        "Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.OK_OPTION) {
                    return;
                }

                mainPanel.currentMDL().doSavePreps();

                final Map<Geoset, Geoset> sourceToDestination = new HashMap<>();
                final List<Geoset> retainedGeosets = new ArrayList<>();
                for (final Geoset geoset : mainPanel.currentMDL().getGeosets()) {
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
                final EditableModel currentMDL = mainPanel.currentMDL();
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
                mainPanel.modelStructureChangeListener.geosetsRemoved(geosetsRemoved);
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

        mainPanel.sortBones = new JMenuItem("Sort Nodes");
        mainPanel.sortBones.setMnemonic(KeyEvent.VK_S);
        mainPanel.sortBones.addActionListener(e -> {
            final EditableModel model = mainPanel.currentMDL();
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
            mainPanel.modelStructureChangeListener.nodesRemoved(result);
            for (final IdObject node : result) {
                model.add(node);
            }
            mainPanel.modelStructureChangeListener.nodesAdded(result);
        });
        optimizeMenu.add(mainPanel.sortBones);

        final JMenuItem flushUnusedTexture = new JMenuItem("Flush Unused Texture");
        flushUnusedTexture.setEnabled(false);
        flushUnusedTexture.setMnemonic(KeyEvent.VK_F);
        optimizeMenu.add(flushUnusedTexture);

        final JMenuItem recalcNormals = new JMenuItem("Recalculate Normals");
        recalcNormals.setAccelerator(KeyStroke.getKeyStroke("control N"));
        recalcNormals.addActionListener(getRecalculateNormalsAction(mainPanel));
        mainPanel.editMenu.add(recalcNormals);

        final JMenuItem recalcExtents = new JMenuItem("Recalculate Extents");
        recalcExtents.setAccelerator(KeyStroke.getKeyStroke("control shift E"));
        recalcExtents.addActionListener(getRecalculateExtentsAction(mainPanel));
        mainPanel.editMenu.add(recalcExtents);

        mainPanel.editMenu.add(new JSeparator());

        final TransferActionListener transferActionListener = new TransferActionListener();
        final ActionListener copyActionListener = e -> {
            if (!mainPanel.animationModeState) {
                transferActionListener.actionPerformed(e);
            } else {
                if (e.getActionCommand().equals(TransferHandler.getCutAction().getValue(Action.NAME))) {
                    mainPanel.timeSliderPanel.cut();
                } else if (e.getActionCommand().equals(TransferHandler.getCopyAction().getValue(Action.NAME))) {
                    mainPanel.timeSliderPanel.copy();
                } else if (e.getActionCommand().equals(TransferHandler.getPasteAction().getValue(Action.NAME))) {
                    mainPanel.timeSliderPanel.paste();
                }
            }
        };
        mainPanel.cut = new JMenuItem("Cut");
        mainPanel.cut.addActionListener(copyActionListener);
        mainPanel.cut.setActionCommand((String) TransferHandler.getCutAction().getValue(Action.NAME));
        mainPanel.cut.setAccelerator(KeyStroke.getKeyStroke("control X"));
        mainPanel.editMenu.add(mainPanel.cut);

        mainPanel.copy = new JMenuItem("Copy");
        mainPanel.copy.addActionListener(copyActionListener);
        mainPanel.copy.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));
        mainPanel.copy.setAccelerator(KeyStroke.getKeyStroke("control C"));
        mainPanel.editMenu.add(mainPanel.copy);

        mainPanel.paste = new JMenuItem("Paste");
        mainPanel.paste.addActionListener(copyActionListener);
        mainPanel.paste.setActionCommand((String) TransferHandler.getPasteAction().getValue(Action.NAME));
        mainPanel.paste.setAccelerator(KeyStroke.getKeyStroke("control V"));
        mainPanel.editMenu.add(mainPanel.paste);

        mainPanel.duplicateSelection = new JMenuItem("Duplicate");
        // divideVertices.setMnemonic(KeyEvent.VK_V);
        mainPanel.duplicateSelection.setAccelerator(KeyStroke.getKeyStroke("control D"));
        mainPanel.duplicateSelection.addActionListener(mainPanel.cloneAction);
        mainPanel.editMenu.add(mainPanel.duplicateSelection);

        mainPanel.editMenu.add(new JSeparator());

        mainPanel.snapVertices = new JMenuItem("Snap Vertices");
        mainPanel.snapVertices.setAccelerator(KeyStroke.getKeyStroke("control shift W"));
        mainPanel.snapVertices.addActionListener(e -> getAbstractAction(mainPanel));
        mainPanel.editMenu.add(mainPanel.snapVertices);

        mainPanel.snapNormals = new JMenuItem("Snap Normals");
        mainPanel.snapNormals.setAccelerator(KeyStroke.getKeyStroke("control L"));
        mainPanel.snapNormals.addActionListener(getSnapNormalsAction(mainPanel));
        mainPanel.editMenu.add(mainPanel.snapNormals);

        mainPanel.editMenu.add(new JSeparator());

        mainPanel.selectAll = new JMenuItem("Select All");
        mainPanel.selectAll.setAccelerator(KeyStroke.getKeyStroke("control A"));
        mainPanel.selectAll.addActionListener(mainPanel.selectAllAction);
        mainPanel.editMenu.add(mainPanel.selectAll);

        mainPanel.invertSelect = new JMenuItem("Invert Selection");
        mainPanel.invertSelect.setAccelerator(KeyStroke.getKeyStroke("control I"));
        mainPanel.invertSelect.addActionListener(mainPanel.invertSelectAction);
        mainPanel.editMenu.add(mainPanel.invertSelect);

        mainPanel.expandSelection = new JMenuItem("Expand Selection");
        mainPanel.expandSelection.setAccelerator(KeyStroke.getKeyStroke("control E"));
        mainPanel.expandSelection.addActionListener(mainPanel.expandSelectionAction);
        mainPanel.editMenu.add(mainPanel.expandSelection);

        mainPanel.editMenu.addSeparator();

        final JMenuItem deleteButton = new JMenuItem("Delete");
        deleteButton.setMnemonic(KeyEvent.VK_D);
        deleteButton.addActionListener(mainPanel.deleteAction);
        mainPanel.editMenu.add(deleteButton);

        mainPanel.editMenu.addSeparator();

        mainPanel.preferencesWindow = new JMenuItem("Preferences Window");
        mainPanel.preferencesWindow.setMnemonic(KeyEvent.VK_P);
        mainPanel.preferencesWindow.addActionListener(getOpenPreferencesAction(mainPanel));
        mainPanel.editMenu.add(mainPanel.preferencesWindow);
    }

    private static void createTeamColorMenuItems(MainPanel mainPanel) {
        for (int i = 0; i < 25; i++) {
            final String colorNumber = String.format("%2s", i).replace(' ', '0');
            try {
                final String colorName = WEString.getString("WESTRING_UNITCOLOR_" + colorNumber);
                final JMenuItem menuItem = new JMenuItem(colorName, new ImageIcon(BLPHandler.get()
                        .getGameTex("ReplaceableTextures\\TeamColor\\TeamColor" + colorNumber + ".blp")));
                mainPanel.teamColorMenu.add(menuItem);
                final int teamColorValueNumber = i;
                menuItem.addActionListener(e -> {
                    Material.teamColor = teamColorValueNumber;
                    final ModelPanel modelPanel = mainPanel.currentModelPanel();
                    if (modelPanel != null) {
                        modelPanel.getAnimationViewer().reloadAllTextures();
                        modelPanel.getPerspArea().reloadAllTextures();

                        reloadComponentBrowser(mainPanel.geoControlModelData, modelPanel);
                    }
                    mainPanel.profile.getPreferences().setTeamColor(teamColorValueNumber);
                });
            } catch (final Exception ex) {
                // load failed
                break;
            }
        }
    }

    public static void updateRecent(MainPanel mainPanel) {
        final List<String> recent = SaveProfile.get().getRecent();
        for (final RecentItem recentItem : mainPanel.recentItems) {
            mainPanel.recentMenu.remove(recentItem);
        }
        mainPanel.recentItems.clear();
        for (int i = 0; i < recent.size(); i++) {
            final String fp = recent.get(recent.size() - i - 1);
            if ((mainPanel.recentItems.size() <= i) || (mainPanel.recentItems.get(i).filepath != fp)) {
                // String[] bits = recent.get(i).split("/");

                final RecentItem item = new RecentItem(new File(fp).getName());
                item.filepath = fp;
                mainPanel.recentItems.add(item);
                item.addActionListener(e -> {

                    mainPanel.currentFile = new File(item.filepath);
                    mainPanel.profile.setPath(mainPanel.currentFile.getParent());
                    // frontArea.clearGeosets();
                    // sideArea.clearGeosets();
                    // botArea.clearGeosets();
                    mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
                            "Allows the user to control which parts of the model are displayed for editing.");
                    mainPanel.toolsMenu.setEnabled(true);
                    SaveProfile.get().addRecent(mainPanel.currentFile.getPath());
                    updateRecent(mainPanel);
                    loadFile(mainPanel, mainPanel.currentFile);
                });
                mainPanel.recentMenu.add(item, mainPanel.recentMenu.getItemCount() - 2);
            }
        }
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

    public static void loadFile(MainPanel mainPanel, final File f) {
        loadFile(mainPanel, f, false);
    }

    public static boolean closeAll(MainPanel mainPanel) {
        boolean success = true;
        final Iterator<ModelPanel> iterator = mainPanel.modelPanels.iterator();
        boolean closedCurrentPanel = false;
        ModelPanel lastUnclosedModelPanel = null;
        while (iterator.hasNext()) {
            final ModelPanel panel = iterator.next();
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
            setCurrentModel(mainPanel, lastUnclosedModelPanel);
        }
        return success;
    }

    static AbstractAction getAbstractAction(final MainPanel mainPanel) {
        return new AbstractAction("Snap Vertices") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
                if (mpanel != null) {
                    mpanel.getUndoManager()
                            .pushAction(mpanel.getModelEditorManager().getModelEditor().snapSelectedVertices());
                }
                mainPanel.repaint();
            }
        };
    }

    static OpenViewAction getAnimationPreviewAction(RootWindow rootWindow, String s, View previewView) {
        return new OpenViewAction(rootWindow, s, new OpenViewGetter() {
            @Override
            public View getView() {
                return previewView;
            }
        });
    }

    static AbstractAction getSnapNormalsAction(final MainPanel mainPanel) {
        return new AbstractAction("Snap Normals") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
                if (mpanel != null) {
                    mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().snapNormals());
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getRecalculateNormalsAction(final MainPanel mainPanel) {
        return new AbstractAction("RecalculateNormals") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
                if (mpanel != null) {
                    mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().recalcNormals());
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getRecalculateExtentsAction(final MainPanel mainPanel) {
        return new AbstractAction("RecalculateExtents") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
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
                    final int userChoice = JOptionPane.showConfirmDialog(mainPanel, messagePanel, "Message",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (userChoice == JOptionPane.YES_OPTION) {
                        mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor()
                                .recalcExtents(considerCurrentBtn.isSelected()));
                    }
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getMirrorAxisAction(final MainPanel mainPanel, String s, byte i) {
        return new AbstractAction(s) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
                if (mpanel != null) {
                    final Vec3 selectionCenter = mpanel.getModelEditorManager().getModelEditor().getSelectionCenter();
                    mpanel.getUndoManager()
                            .pushAction(mpanel.getModelEditorManager().getModelEditor().mirror(i,
                                    mainPanel.mirrorFlip.isSelected(), selectionCenter.x, selectionCenter.y,
                                    selectionCenter.z));
                }
                mainPanel.repaint();
            }
        };
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

    private static View createMPQBrowser(MainPanel mainPanel) {
        return createMPQBrowser(
                mainPanel, new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)));
    }

    static AbstractAction getOpenUnitViewerAction(MainPanel mainPanel) {
        return new AbstractAction("Open Unit Browser") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final UnitEditorTree unitEditorTree = createUnitEditorTree(mainPanel);
                mainPanel.rootWindow.setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(),
                        new View("Unit Browser",
                                new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
                                new JScrollPane(unitEditorTree))));
            }
        };
    }

    static AbstractAction getOpenHiveViewerAction(MainPanel mainPanel) {
        return new AbstractAction("Open Hive Browser") {
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
                    public Component getListCellRendererComponent(final JList<?> list, final Object value,
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
                mainPanel.rootWindow.setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(),
                        new View("Hive Browser",
                                new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
                                panel)));
            }
        };
    }

    static AbstractAction getOpenDoodadViewerAction(MainPanel mainPanel) {
        return new AbstractAction("Open Doodad Browser") {

            @Override
            public void actionPerformed(final ActionEvent e) {

                final UnitEditorTree unitEditorTree = new UnitEditorTree(getDoodadData(), new DoodadTabTreeBrowserBuilder(),
                        getUnitEditorSettings(), MutableObjectData.WorldEditorDataType.DOODADS);
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
                                    if (o.getUserObject() instanceof MutableObjectData.MutableGameObject) {
                                        final MutableObjectData.MutableGameObject obj = (MutableObjectData.MutableGameObject) o.getUserObject();
                                        final int numberOfVariations = obj.getFieldAsInteger(War3ID.fromString("dvar"), 0);
                                        if (numberOfVariations > 1) {
                                            for (int i = 0; i < numberOfVariations; i++) {
                                                final String path = convertPathToMDX(
                                                        obj.getFieldAsString(War3ID.fromString("dfil"), 0) + i + ".mdl");
                                                final String portrait = ModelUtils.getPortrait(path);
                                                final ImageIcon icon = new ImageIcon(IconUtils
                                                        .getIcon(obj, MutableObjectData.WorldEditorDataType.DOODADS)
                                                        .getScaledInstance(16, 16, Image.SCALE_DEFAULT));

                                                System.out.println(path);
                                                loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(path), true, i == 0,
                                                        icon);
                                                if (mainPanel.prefs.isLoadPortraits()
                                                        && GameDataFileSystem.getDefault().has(portrait)) {
                                                    loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(portrait), true,
                                                            false, icon);
                                                }
                                            }
                                        } else {
                                            final String path = convertPathToMDX(
                                                    obj.getFieldAsString(War3ID.fromString("dfil"), 0));
                                            final String portrait = ModelUtils.getPortrait(path);
                                            final ImageIcon icon = new ImageIcon(IconUtils
                                                    .getIcon(obj, MutableObjectData.WorldEditorDataType.DOODADS)
                                                    .getScaledInstance(16, 16, Image.SCALE_DEFAULT));
                                            System.out.println(path);
                                            loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(path), true, true, icon);
                                            if (mainPanel.prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
                                                loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false,
                                                        icon);
                                            }
                                        }
                                        mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
                                                "Allows the user to control which parts of the model are displayed for editing.");
                                        mainPanel.toolsMenu.setEnabled(true);
                                    }
                                }
                            }
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
        };
    }

    public static MutableObjectData getDoodadData() {
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
        return new MutableObjectData(MutableObjectData.WorldEditorDataType.DOODADS, StandardObjectData.getStandardDoodads(),
                StandardObjectData.getStandardDoodadMeta(), editorData);
    }

    static String convertPathToMDX(String filepath) {
        if (filepath.endsWith(".mdl")) {
            filepath = filepath.replace(".mdl", ".mdx");
        } else if (!filepath.endsWith(".mdx")) {
            filepath = filepath.concat(".mdx");
        }
        return filepath;
    }

    static AbstractAction getOpenPreferencesAction(MainPanel mainPanel) {
        return new AbstractAction("Open Preferences") {

            @Override
            public void actionPerformed(final ActionEvent e) {

                final ProgramPreferences programPreferences = new ProgramPreferences();
                programPreferences.loadFrom(mainPanel.prefs);
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

                final int ret = JOptionPane.showConfirmDialog(mainPanel, programPreferencesPanel, "Preferences",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (ret == JOptionPane.OK_OPTION) {
                    mainPanel.prefs.loadFrom(programPreferences);
                    final List<DataSourceDescriptor> dataSources = programPreferencesPanel.getDataSources();
                    final boolean changedDataSources = (dataSources != null) && !dataSources.equals(priorDataSources);
                    if (changedDataSources) {
                        SaveProfile.get().setDataSources(dataSources);
                    }
                    SaveProfile.save();
                    if (changedDataSources) {
                        dataSourcesChanged(mainPanel.directoryChangeNotifier, mainPanel.modelPanels);
                    }
                    updateUIFromProgramPreferences(mainPanel.fetchPortraitsToo, mainPanel.modelPanels, mainPanel.prefs, mainPanel.showNormals, mainPanel.showVertexModifyControls, mainPanel.solid, mainPanel.textureModels, mainPanel.wireframe);
                }
            }
        };
    }

    static void updateUIFromProgramPreferences(JCheckBoxMenuItem fetchPortraitsToo, List<ModelPanel> modelPanels, ProgramPreferences prefs, JCheckBoxMenuItem showNormals, JCheckBoxMenuItem showVertexModifyControls, JRadioButtonMenuItem solid, JCheckBoxMenuItem textureModels, JRadioButtonMenuItem wireframe) {
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

    private static void dataSourcesChanged(WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier directoryChangeNotifier, List<ModelPanel> modelPanels) {
        for (final ModelPanel modelPanel : modelPanels) {
            final PerspDisplayPanel pdp = modelPanel.getPerspArea();
            pdp.reloadAllTextures();
            modelPanel.getAnimationViewer().reloadAllTextures();
        }
        directoryChangeNotifier.dataSourcesChanged();
    }

    static AbstractAction getViewMatricesAction(final MainPanel mainPanel) {
        return new AbstractAction("View Matrices") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
                if (mpanel != null) {
                    mpanel.viewMatrices();
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getInsideOutNormalsAction(final MainPanel mainPanel) {
        return new AbstractAction("Inside Out Normals") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
                if (mpanel != null) {
                    mpanel.getUndoManager()
                            .pushAction(mpanel.getModelEditorManager().getModelEditor().flipSelectedNormals());
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getInsideOutAction(final MainPanel mainPanel) {
        return new AbstractAction("Inside Out") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ModelPanel mpanel = mainPanel.currentModelPanel();
                if (mpanel != null) {
                    mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().flipSelectedFaces());
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getInverseAllUVsAction(MainPanel mainPanel) {
        return new AbstractAction("Swap UVs U for V") {

            @Override
            public void actionPerformed(final ActionEvent e) {
                for (final Geoset geo : mainPanel.currentMDL().getGeosets()) {
                    for (final GeosetVertex vertex : geo.getVertices()) {
                        for (final Vec2 tvert : vertex.getTverts()) {
                            final float temp = tvert.x;
                            tvert.x = tvert.y;
                            tvert.y = temp;
                        }
                    }
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getFlipAllUVsVAction(MainPanel mainPanel) {
        return new AbstractAction("Flip All UVs V") {

            @Override
            public void actionPerformed(final ActionEvent e) {
                for (final Geoset geo : mainPanel.currentMDL().getGeosets()) {
                    for (final GeosetVertex vertex : geo.getVertices()) {
                        for (final Vec2 tvert : vertex.getTverts()) {
                            tvert.y = 1.0f - tvert.y;
                        }
                    }
                }
                mainPanel.repaint();
            }
        };
    }

    static AbstractAction getFlipAllUVsUAction(MainPanel mainPanel) {
        return new AbstractAction("Flip All UVs U") {

            @Override
            public void actionPerformed(final ActionEvent e) {
                for (final Geoset geo : mainPanel.currentMDL().getGeosets()) {
                    for (final GeosetVertex vertex : geo.getVertices()) {
                        for (final Vec2 tvert : vertex.getTverts()) {
                            tvert.x = 1.0f - tvert.x;
                        }
                    }
                }
                mainPanel.repaint();
            }
        };
    }

    static void traverseAndReloadData(final DockingWindow window) {
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
                        final MutableObjectData.WorldEditorDataType dataType = unitEditorTree.getDataType();
                        if (dataType == MutableObjectData.WorldEditorDataType.UNITS) {
                            System.out.println("saw unit tree");
                            unitEditorTree.setUnitDataAndReloadVerySlowly(getUnitData());
                        } else if (dataType == MutableObjectData.WorldEditorDataType.DOODADS) {
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

    interface OpenViewGetter {
        View getView();
    }

    static class OpenViewAction extends AbstractAction {
        private final OpenViewGetter openViewGetter;
        private RootWindow rootWindow;

        private OpenViewAction(RootWindow rootWindow, final String name, final OpenViewGetter openViewGetter) {
            super(name);
            this.openViewGetter = openViewGetter;
            this.rootWindow = rootWindow;
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

    private static class RepaintingModelStateListener implements ModelViewStateListener {
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

    static class RecentItem extends JMenuItem {
        public RecentItem(final String what) {
            super(what);
        }

        String filepath;
    }

    static class UndoMenuItem extends JMenuItem {
        private MainPanel mainPanel;

        public UndoMenuItem(MainPanel mainPanel, final String text) {
            super(text);
            this.mainPanel = mainPanel;
        }

        @Override
        public String getText() {
            if (funcEnabled()) {
                return "Undo " + mainPanel.currentModelPanel().getUndoManager().getUndoText();// +"
                // Ctrl+Z";
            } else {
                return "Can't undo";// +" Ctrl+Z";
            }
        }

        public boolean funcEnabled() {
            try {
                return !mainPanel.currentModelPanel().getUndoManager().isUndoListEmpty();
            } catch (final NullPointerException e) {
                return false;
            }
        }
    }

    static class RedoMenuItem extends JMenuItem {
        private MainPanel mainPanel;

        public RedoMenuItem(MainPanel mainPanel, final String text) {
            super(text);
            this.mainPanel = mainPanel;
        }

        @Override
        public String getText() {
            if (funcEnabled()) {
                return "Redo " + mainPanel.currentModelPanel().getUndoManager().getRedoText();// +"
                // Ctrl+Y";
            } else {
                return "Can't redo";// +" Ctrl+Y";
            }
        }

        public boolean funcEnabled() {
            try {
                return !mainPanel.currentModelPanel().getUndoManager().isRedoListEmpty();
            } catch (final NullPointerException e) {
                return false;
            }
        }
    }
}
