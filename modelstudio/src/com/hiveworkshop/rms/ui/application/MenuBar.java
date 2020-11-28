package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.application.scripts.AnimationTransfer;
import com.hiveworkshop.rms.ui.application.tools.EditTexturesPopupPanel;
import com.hiveworkshop.rms.ui.application.viewer.AnimationViewer;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.*;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
import com.hiveworkshop.rms.ui.browsers.mpq.MPQBrowser;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TransferActionListener;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import net.infonode.docking.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createAndAddMenuItem;
import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenu;

public class MenuBar {
    public static final ImageIcon AnimIcon = RMSIcons.AnimIcon;

    public static JMenuBar createMenuBar(MainPanel mainPanel) {
        // Create my menu bar
        mainPanel.menuBar = new JMenuBar();

        // Build the file menu
        JMenu fileMenu = createMenu("File", KeyEvent.VK_F, "Allows the user to open, save, close, and manipulate files.");
        mainPanel.menuBar.add(fileMenu);


        mainPanel.recentMenu = createMenu("Open Recent", KeyEvent.VK_R, "Allows you to access recently opened files.");

        JMenu editMenu = createMenu("Edit", KeyEvent.VK_E, "Allows the user to use various tools to edit the currently selected model.");
        mainPanel.menuBar.add(editMenu);

        mainPanel.toolsMenu = createMenu("Tools", KeyEvent.VK_T, "Allows the user to use various model editing tools. (You must open a model before you may use this menu.)");
        mainPanel.toolsMenu.setEnabled(false);
        mainPanel.menuBar.add(mainPanel.toolsMenu);

        JMenu viewMenu = createMenu("View", -1, "Allows the user to control view settings.");
        mainPanel.menuBar.add(viewMenu);

        mainPanel.teamColorMenu = createMenu("Team Color", -1, "Allows the user to control team color settings.");
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

        JMenu windowMenu = createMenu("Window", KeyEvent.VK_W, "Allows the user to open various windows containing the program features.");
        mainPanel.windowMenu = windowMenu;
        mainPanel.menuBar.add(windowMenu);

        fillWindowsMenu(mainPanel, windowMenu);

        JMenu addMenu = createMenu("Add", KeyEvent.VK_A, "Allows the user to add new components to the model.");
        mainPanel.menuBar.add(addMenu);

        fillAddMenu(mainPanel, addMenu);

        JMenu scriptsMenu = createMenu("Scripts", KeyEvent.VK_A, "Allows the user to execute model edit scripts.");
        mainPanel.menuBar.add(scriptsMenu);

        fillScriptsMenu(mainPanel, scriptsMenu);

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

        mainPanel.aboutMenu = createMenu("Help", KeyEvent.VK_H, "");
        mainPanel.menuBar.add(mainPanel.aboutMenu);

        mainPanel.recentMenu.add(new JSeparator());

        createAndAddMenuItem("Clear", mainPanel.recentMenu, KeyEvent.VK_C, e -> MenuBarActions.clearRecentActionRes(mainPanel));

        updateRecent(mainPanel);

        fillAboutMenu(mainPanel);

        fillToolsMenu(mainPanel);

        fillViewMenu(mainPanel, viewMenu);

        fillFileMenu(mainPanel, fileMenu);


        fillEditMenu(mainPanel, editMenu);

        for (int i = 0; i < mainPanel.menuBar.getMenuCount(); i++) {
            mainPanel.menuBar.getMenu(i).getPopupMenu().setLightWeightPopupEnabled(false);
        }
        return mainPanel.menuBar;
    }

    private static void fillWindowsMenu(MainPanel mainPanel, JMenu windowMenu) {
        final JMenuItem resetViewButton = new JMenuItem("Reset Layout");
        resetViewButton.addActionListener(e -> resetViewButtonActionRes(mainPanel));
        windowMenu.add(resetViewButton);

        final JMenu viewsMenu = createMenu("Views", KeyEvent.VK_V);
        windowMenu.add(viewsMenu);

        final JMenuItem testItem = new JMenuItem("test");
        testItem.addActionListener(new OpenViewAction(mainPanel.rootWindow, "Animation Preview", () -> {return testItemActionRes(mainPanel);}));

//		viewsMenu.add(testItem);

        createAndAddMenuItem("Animation Preview", viewsMenu, KeyEvent.VK_A, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Animation Preview", mainPanel.previewView));
//        createAndAddMenuItem("Animation Preview", viewsMenu, KeyEvent.VK_A, new OpenViewAction(mainPanel.rootWindow, "Animation Preview", () -> mainPanel.previewView));

        createAndAddMenuItem("Animation Controller", viewsMenu, KeyEvent.VK_C, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Animation Controller", mainPanel.animationControllerView));

        createAndAddMenuItem("Modeling", viewsMenu, KeyEvent.VK_M, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Modeling", mainPanel.creatorView));

        createAndAddMenuItem("Outliner", viewsMenu, KeyEvent.VK_O, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Outliner", mainPanel.viewportControllerWindowView));

        createAndAddMenuItem("Perspective", viewsMenu, KeyEvent.VK_P, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Perspective", mainPanel.perspectiveView));

        createAndAddMenuItem("Front", viewsMenu, KeyEvent.VK_F, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Front", mainPanel.frontView));

        createAndAddMenuItem("Side", viewsMenu, KeyEvent.VK_S, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Side", mainPanel.leftView));

        createAndAddMenuItem("Bottom", viewsMenu, KeyEvent.VK_B, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Bottom", mainPanel.bottomView));

        createAndAddMenuItem("Tools", viewsMenu, KeyEvent.VK_T, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Tools", mainPanel.toolView));

        createAndAddMenuItem("Contents", viewsMenu, KeyEvent.VK_C, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Model", mainPanel.modelDataView));

        createAndAddMenuItem("Footer", viewsMenu, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Footer", mainPanel.timeSliderView));

        createAndAddMenuItem("Matrix Eater Script", viewsMenu, KeyEvent.VK_H, KeyStroke.getKeyStroke("control P"), OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Matrix Eater Script", mainPanel.hackerView));

        final JMenu browsersMenu = createMenu("Browsers", KeyEvent.VK_B);
        windowMenu.add(browsersMenu);

        createAndAddMenuItem("Data Browser", browsersMenu, KeyEvent.VK_A, MPQBrowserView.getOpenMPQViewerAction(mainPanel));

        createAndAddMenuItem("Unit Browser", browsersMenu, KeyEvent.VK_U, MenuBarActions.getOpenUnitViewerAction(mainPanel));

//        createAndAddMenuItem("Doodad Browser", browsersMenu, KeyEvent.VK_D, getOpenDoodadViewerAction(mainPanel));
        createAndAddMenuItem("Doodad Browser", browsersMenu, KeyEvent.VK_D, e -> MenuBarActions.OpenDoodadViewerActionRes(mainPanel));

        JMenuItem hiveViewer = new JMenuItem("Hive Browser");
        hiveViewer.setMnemonic(KeyEvent.VK_H);
        hiveViewer.addActionListener(MenuBarActions.getOpenHiveViewerAction(mainPanel));
//		browsersMenu.add(hiveViewer);

        windowMenu.addSeparator();
    }

    private static View testItemActionRes(MainPanel mainPanel) {
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
    }

    private static void resetViewButtonActionRes(MainPanel mainPanel) {
        traverseAndReset(mainPanel.rootWindow);
        final TabWindow startupTabWindow = MainLayoutCreator.createMainLayout(mainPanel);
        mainPanel.rootWindow.setWindow(startupTabWindow);
        MainLayoutCreator.traverseAndFix(mainPanel.rootWindow);
    }

    private static void fillAddMenu(final MainPanel mainPanel, JMenu addMenu) {
        mainPanel.addParticle = new JMenu("Particle");
        mainPanel.addParticle.setMnemonic(KeyEvent.VK_P);
        addMenu.add(mainPanel.addParticle);

        AddParticlePanel.fetchIncludedParticles(mainPanel);

        mainPanel.animationMenu = new JMenu("Animation");
        mainPanel.animationMenu.setMnemonic(KeyEvent.VK_A);
        addMenu.add(mainPanel.animationMenu);

        createAndAddMenuItem("Rising/Falling Birth/Death", mainPanel.animationMenu, KeyEvent.VK_R, e -> MenuBarActions.riseFallBirthActionRes(mainPanel));

        mainPanel.singleAnimationMenu = new JMenu("Single");
        mainPanel.singleAnimationMenu.setMnemonic(KeyEvent.VK_S);
        mainPanel.animationMenu.add(mainPanel.singleAnimationMenu);

        JMenuItem animFromFile = new JMenuItem("From File");
        animFromFile.setMnemonic(KeyEvent.VK_F);
        animFromFile.addActionListener(e -> {
            MenuBarActions.animFromFileActionRes(mainPanel);
        });
        mainPanel.singleAnimationMenu.add(animFromFile);

        JMenuItem animFromUnit = new JMenuItem("From Unit");
        animFromUnit.setMnemonic(KeyEvent.VK_U);
        animFromUnit.addActionListener(e -> MenuBarActions.animFromUnitActionRes(mainPanel));
        mainPanel.singleAnimationMenu.add(animFromUnit);

        JMenuItem animFromModel = new JMenuItem("From Model");
        animFromModel.setMnemonic(KeyEvent.VK_M);
        animFromModel.addActionListener(e -> MenuBarActions.animFromModelActionRes(mainPanel));
        mainPanel.singleAnimationMenu.add(animFromModel);

        JMenuItem animFromObject = new JMenuItem("From Object");
        animFromObject.setMnemonic(KeyEvent.VK_O);
        animFromObject.addActionListener(e -> MenuBarActions.animFromObjectActionRes(mainPanel));
        mainPanel.singleAnimationMenu.add(animFromObject);
    }

    private static void fillAboutMenu(MainPanel mainPanel) {
        createAndAddMenuItem("Changelog", mainPanel.aboutMenu, KeyEvent.VK_A, e -> MenuBarActions.creditsButtonActionRes("docs/changelist.rtf", "Changelog"));

        createAndAddMenuItem("About", mainPanel.aboutMenu, KeyEvent.VK_A, e -> MenuBarActions.creditsButtonActionRes("docs/credits.rtf", "About"));

        JMenuItem jokeButton = new JMenuItem("HTML Magic");
        jokeButton.setMnemonic(KeyEvent.VK_H);
        jokeButton.addActionListener(e -> jokeButtonActionRes(mainPanel));
        mainPanel.aboutMenu.add(jokeButton);
    }

    private static void jokeButtonActionRes(MainPanel mainPanel) {
        final JEditorPane jEditorPane;
        try {
            jEditorPane = new JEditorPane(new URL("http://79.179.129.227:8080/clients/editor/"));
            final JFrame testFrame = new JFrame("Test");
            testFrame.setContentPane(jEditorPane);
            testFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            testFrame.pack();
            testFrame.setLocationRelativeTo(mainPanel.nullmodelButton);
            testFrame.setVisible(true);
        } catch (final IOException e1) {
            e1.printStackTrace();
        }
    }

    private static void fillToolsMenu(MainPanel mainPanel) {
        JMenuItem showMatrices = new JMenuItem("View Selected \"Matrices\"");
        // showMatrices.setMnemonic(KeyEvent.VK_V);
        showMatrices.addActionListener(MenuBarActions.getViewMatricesAction(mainPanel));
        mainPanel.toolsMenu.add(showMatrices);

        JMenuItem insideOut = new JMenuItem("Flip all selected faces");
        insideOut.setMnemonic(KeyEvent.VK_I);
        insideOut.addActionListener(MenuBarActions.getInsideOutAction(mainPanel));
        insideOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));
        mainPanel.toolsMenu.add(insideOut);

        JMenuItem insideOutNormals = new JMenuItem("Flip all selected normals");
        insideOutNormals.addActionListener(MenuBarActions.getInsideOutNormalsAction(mainPanel));
        mainPanel.toolsMenu.add(insideOutNormals);

        mainPanel.toolsMenu.add(new JSeparator());

        createAndAddMenuItem("Edit UV Mapping", mainPanel.toolsMenu, KeyEvent.VK_U, e -> MenuBarActions.editUVsActionRes(mainPanel));

        JMenuItem editTextures = new JMenuItem("Edit Textures");
        editTextures.setMnemonic(KeyEvent.VK_T);
        editTextures.addActionListener(e -> editTexturesActionRes(mainPanel));
        mainPanel.toolsMenu.add(editTextures);

        createAndAddMenuItem("Rig Selection", mainPanel.toolsMenu, KeyEvent.VK_R, KeyStroke.getKeyStroke("control W"), mainPanel.rigAction);

        mainPanel.tweaksSubmenu = new JMenu("Tweaks");
        mainPanel.tweaksSubmenu.setMnemonic(KeyEvent.VK_T);
        mainPanel.tweaksSubmenu.getAccessibleContext()
                .setAccessibleDescription("Allows the user to tweak conversion mistakes.");
        mainPanel.toolsMenu.add(mainPanel.tweaksSubmenu);

        createAndAddMenuItem("Flip All UVs U", mainPanel.tweaksSubmenu, KeyEvent.VK_U, MenuBarActions.getFlipAllUVsUAction(mainPanel));

        JMenuItem flipAllUVsV = new JMenuItem("Flip All UVs V");
        // flipAllUVsV.setMnemonic(KeyEvent.VK_V);
        flipAllUVsV.addActionListener(MenuBarActions.getFlipAllUVsVAction(mainPanel));
        mainPanel.tweaksSubmenu.add(flipAllUVsV);

        createAndAddMenuItem("Swap All UVs U for V", mainPanel.tweaksSubmenu, KeyEvent.VK_S, MenuBarActions.getInverseAllUVsAction(mainPanel));

        mainPanel.mirrorSubmenu = new JMenu("Mirror");
        mainPanel.mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
        mainPanel.mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");
        mainPanel.toolsMenu.add(mainPanel.mirrorSubmenu);

        createAndAddMenuItem("Mirror X", mainPanel.mirrorSubmenu, KeyEvent.VK_X, MenuBarActions.getMirrorAxisAction(mainPanel, "Mirror X", (byte) 0));

        createAndAddMenuItem("Mirror Y", mainPanel.mirrorSubmenu, KeyEvent.VK_Y, MenuBarActions.getMirrorAxisAction(mainPanel, "Mirror Y", (byte) 1));

        createAndAddMenuItem("Mirror Z", mainPanel.mirrorSubmenu, KeyEvent.VK_Z, MenuBarActions.getMirrorAxisAction(mainPanel, "Mirror Z", (byte) 2));

        mainPanel.mirrorSubmenu.add(new JSeparator());

        mainPanel.mirrorFlip = new JCheckBoxMenuItem("Automatically flip after mirror (preserves surface)", true);
        mainPanel.mirrorFlip.setMnemonic(KeyEvent.VK_A);
        mainPanel.mirrorSubmenu.add(mainPanel.mirrorFlip);
    }

    private static void editTexturesActionRes(MainPanel mainPanel) {
        final EditTexturesPopupPanel textureManager = new EditTexturesPopupPanel(mainPanel.currentModelPanel().getModelViewManager(),
                mainPanel.modelStructureChangeListener, mainPanel.textureExporter);
        final JFrame frame = new JFrame("Edit Textures");
        textureManager.setSize(new Dimension(800, 650));
        frame.setContentPane(textureManager);
        frame.setSize(textureManager.getSize());
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void fillViewMenu(MainPanel mainPanel, JMenu viewMenu) {
        mainPanel.textureModels = new JCheckBoxMenuItem("Texture Models", true);
        mainPanel.textureModels.setMnemonic(KeyEvent.VK_T);
        mainPanel.textureModels.setSelected(true);
        mainPanel.textureModels.addActionListener(e -> mainPanel.prefs.setTextureModels(mainPanel.textureModels.isSelected()));
        viewMenu.add(mainPanel.textureModels);

        JMenuItem newDirectory = new JMenuItem("Change Game Directory");
        newDirectory.setAccelerator(KeyStroke.getKeyStroke("control shift D"));
        newDirectory.setToolTipText("Changes the directory from which to load texture files for the 3D display.");
        newDirectory.setMnemonic(KeyEvent.VK_D);
        newDirectory.addActionListener(mainPanel);
//		viewMenu.add(newDirectory);

        viewMenu.add(new JSeparator());

        mainPanel.showVertexModifyControls = new JCheckBoxMenuItem("Show Viewport Buttons", true);
        // showVertexModifyControls.setMnemonic(KeyEvent.VK_V);
        mainPanel.showVertexModifyControls.addActionListener(e -> showVertexModifyControlsActionRes(mainPanel.modelPanels, mainPanel.prefs, mainPanel.showVertexModifyControls));
        viewMenu.add(mainPanel.showVertexModifyControls);

        viewMenu.add(new JSeparator());

        mainPanel.showNormals = new JCheckBoxMenuItem("Show Normals", true);
        mainPanel.showNormals.setMnemonic(KeyEvent.VK_N);
        mainPanel.showNormals.setSelected(false);
        mainPanel.showNormals.addActionListener(e -> mainPanel.prefs.setShowNormals(mainPanel.showNormals.isSelected()));
        viewMenu.add(mainPanel.showNormals);

        mainPanel.viewMode = new JMenu("3D View Mode");
        viewMenu.add(mainPanel.viewMode);

        mainPanel.viewModes = new ButtonGroup();

        final ActionListener repainter = e -> repainterActionRes(mainPanel);

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

    private static void repainterActionRes(MainPanel mainPanel) {
        if (mainPanel.wireframe.isSelected()) {
            mainPanel.prefs.setViewMode(0);
        } else if (mainPanel.solid.isSelected()) {
            mainPanel.prefs.setViewMode(1);
        } else {
            mainPanel.prefs.setViewMode(-1);
        }
        mainPanel.repaint();
    }

    private static void fillFileMenu(MainPanel mainPanel, JMenu fileMenu) {
        createAndAddMenuItem("New", fileMenu, KeyEvent.VK_N, KeyStroke.getKeyStroke("control N"), e -> ToolBar.newModel(mainPanel));

        createAndAddMenuItem("Open", fileMenu, KeyEvent.VK_O, KeyStroke.getKeyStroke("control O"), e -> ToolBar.onClickOpen(mainPanel));

        fileMenu.add(mainPanel.recentMenu);

        mainPanel.fetch = new JMenu("Open Internal");
        mainPanel.fetch.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(mainPanel.fetch);

        createAndAddMenuItem("Unit", mainPanel.fetch, KeyEvent.VK_U, KeyStroke.getKeyStroke("control U"), e -> MenuBarActions.fetchUnitActionRes(mainPanel));

        createAndAddMenuItem("Model", mainPanel.fetch, KeyEvent.VK_M, KeyStroke.getKeyStroke("control M"), e -> MenuBarActions.fetchModelActionRes(mainPanel));

        createAndAddMenuItem("Object Editor", mainPanel.fetch, KeyEvent.VK_O, KeyStroke.getKeyStroke("control O"), e -> MenuBarActions.fetchObjectActionRes(mainPanel));

        mainPanel.fetch.add(new JSeparator());

        JCheckBoxMenuItem fetchPortraitsToo = new JCheckBoxMenuItem("Fetch portraits, too!", true);
        fetchPortraitsToo.setMnemonic(KeyEvent.VK_P);
        fetchPortraitsToo.addActionListener(e -> mainPanel.prefs.setLoadPortraits(fetchPortraitsToo.isSelected()));
        mainPanel.fetchPortraitsToo = fetchPortraitsToo;
        mainPanel.fetch.add(fetchPortraitsToo);
        fetchPortraitsToo.setSelected(true);

        fileMenu.add(new JSeparator());

        JMenu importMenu = createMenu("Import", KeyEvent.VK_I);
        fileMenu.add(importMenu);

        createAndAddMenuItem("From File", importMenu, KeyEvent.VK_I, KeyStroke.getKeyStroke("control shift I"), e -> ImportFileActions.importButtonActionRes(mainPanel));

        createAndAddMenuItem("From Unit", importMenu, KeyEvent.VK_U, KeyStroke.getKeyStroke("control shift U"), e -> ImportFileActions.importUnitActionRes(mainPanel));

        createAndAddMenuItem("From WC3 Model", importMenu, KeyEvent.VK_M, e -> ImportFileActions.importGameModelActionRes(mainPanel));

        createAndAddMenuItem("From Object Editor", importMenu, KeyEvent.VK_O, e -> ImportFileActions.importGameObjectActionRes(mainPanel));

        createAndAddMenuItem("From Workspace", importMenu, KeyEvent.VK_O, e -> ImportFileActions.importFromWorkspaceActionRes(mainPanel));

        createAndAddMenuItem("Save", fileMenu, KeyEvent.VK_S, KeyStroke.getKeyStroke("control S"), e -> SaveActionRes(mainPanel));

        createAndAddMenuItem("Save as", fileMenu, KeyEvent.VK_A, KeyStroke.getKeyStroke("control Q"), e -> MenuBarActions.onClickSaveAs(mainPanel));

        fileMenu.add(new JSeparator());

        createAndAddMenuItem("Export Texture", fileMenu, KeyEvent.VK_E, e -> MenuBarActions.exportTexturesActionRes(mainPanel));

        fileMenu.add(new JSeparator());

        createAndAddMenuItem("Revert", fileMenu, -1, e -> MPQBrowserView.revertActionRes(mainPanel));

        createAndAddMenuItem("Close", fileMenu, KeyEvent.VK_E, KeyStroke.getKeyStroke("control E"), e -> MenuBarActions.closePanelActionRes(mainPanel));

        fileMenu.add(new JSeparator());

        createAndAddMenuItem("Exit", fileMenu, KeyEvent.VK_E, e -> ExitActionRes(mainPanel));
    }

    private static void SaveActionRes(MainPanel mainPanel) {
        if ((mainPanel.currentMDL() != null) && (mainPanel.currentMDL().getFile() != null)) {
            ToolBar.onClickSave(mainPanel);}
    }

    private static void ExitActionRes(MainPanel mainPanel) {
        if (closeAll(mainPanel)) {
            MainFrame.frame.dispose();
        }
    }

    private static void fillEditMenu(final MainPanel mainPanel, JMenu editMenu) {
        mainPanel.undo = new UndoMenuItem(mainPanel, "Undo");
        mainPanel.undo.addActionListener(mainPanel.undoAction);
        mainPanel.undo.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        mainPanel.undo.setEnabled(mainPanel.undo.funcEnabled());
        // undo.addMouseListener(this);
        editMenu.add(mainPanel.undo);

        mainPanel.redo = new RedoMenuItem(mainPanel, "Redo");
        mainPanel.redo.addActionListener(mainPanel.redoAction);
        mainPanel.redo.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        mainPanel.redo.setEnabled(mainPanel.redo.funcEnabled());
        // redo.addMouseListener(this);
        editMenu.add(mainPanel.redo);


        editMenu.add(new JSeparator());
        final JMenu optimizeMenu = createMenu("Optimize", KeyEvent.VK_O);
        editMenu.add(optimizeMenu);
        createAndAddMenuItem("Linearize Animations", optimizeMenu, KeyEvent.VK_L, e -> MenuBarActions.linearizeAnimationsActionRes(mainPanel));

        createAndAddMenuItem("Simplify Keyframes (Experimental)", optimizeMenu, KeyEvent.VK_K, e -> MenuBarActions.simplifyKeyframesActionRes(mainPanel));

        createAndAddMenuItem("Minimize Geosets", optimizeMenu, KeyEvent.VK_K, e -> minimizeGeosetActionRes(mainPanel));

        createAndAddMenuItem("Sort Nodes", optimizeMenu, KeyEvent.VK_S, e -> sortBones(mainPanel));

        final JMenuItem flushUnusedTexture = new JMenuItem("Flush Unused Texture");
        flushUnusedTexture.setEnabled(false);
        flushUnusedTexture.setMnemonic(KeyEvent.VK_F);
        optimizeMenu.add(flushUnusedTexture);

        createAndAddMenuItem("Recalculate Normals", editMenu, -1, KeyStroke.getKeyStroke("control N"), MenuBarActions.getRecalculateNormalsAction(mainPanel));

        createAndAddMenuItem("Recalculate Extents", editMenu, -1, KeyStroke.getKeyStroke("control shift E"), MenuBarActions.getRecalculateExtentsAction(mainPanel));

        editMenu.add(new JSeparator());
        final TransferActionListener transferActionListener = new TransferActionListener();
        final ActionListener copyActionListener = e -> copyActionListenerRes(mainPanel, transferActionListener, e);


        createAndAddMenuItem("Cut", editMenu, KeyStroke.getKeyStroke("control X"), (String) TransferHandler.getCutAction().getValue(Action.NAME), copyActionListener);

        createAndAddMenuItem("Copy", editMenu, KeyStroke.getKeyStroke("control C"), (String) TransferHandler.getCopyAction().getValue(Action.NAME), copyActionListener);

        createAndAddMenuItem("Paste", editMenu, KeyStroke.getKeyStroke("control V"), (String) TransferHandler.getPasteAction().getValue(Action.NAME), copyActionListener);

        createAndAddMenuItem("Duplicate", editMenu, -1, KeyStroke.getKeyStroke("control D"), mainPanel.cloneAction);

        editMenu.add(new JSeparator());

        createAndAddMenuItem("Snap Vertices", editMenu, -1, KeyStroke.getKeyStroke("control shift W"), e -> MenuBarActions.getSnapVerticiesAction(mainPanel));

        createAndAddMenuItem("Snap Normals", editMenu, -1, KeyStroke.getKeyStroke("control L"), MenuBarActions.getSnapNormalsAction(mainPanel));

        editMenu.add(new JSeparator());

        createAndAddMenuItem("Select All", editMenu, -1, KeyStroke.getKeyStroke("control A"), mainPanel.selectAllAction);

        createAndAddMenuItem("Invert Selection", editMenu, -1, KeyStroke.getKeyStroke("control I"), mainPanel.invertSelectAction);

        createAndAddMenuItem("Expand Selection", editMenu, -1, KeyStroke.getKeyStroke("control E"), mainPanel.expandSelectionAction);

        editMenu.addSeparator();

        createAndAddMenuItem("Delete", editMenu, KeyEvent.VK_D, mainPanel.deleteAction);

        editMenu.addSeparator();
        createAndAddMenuItem("Preferences Window", editMenu, KeyEvent.VK_P, MenuBarActions.getOpenPreferencesAction(mainPanel));
    }

    private static void copyActionListenerRes(MainPanel mainPanel, TransferActionListener transferActionListener, ActionEvent e) {
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
    }

    private static void sortBones(MainPanel mainPanel) {
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
    }

    private static void fillScriptsMenu(MainPanel mainPanel, JMenu scriptsMenu) {
        createAndAddMenuItem("Oinkerwinkle-Style AnimTransfer", scriptsMenu, KeyEvent.VK_P, KeyStroke.getKeyStroke("control shift S"), e -> importButtonSActionRes());

        JMenuItem mergeGeoset = new JMenuItem("Oinkerwinkle-Style Merge Geoset");
        mergeGeoset.setMnemonic(KeyEvent.VK_M);
        mergeGeoset.setAccelerator(KeyStroke.getKeyStroke("control M"));
        mergeGeoset.addActionListener(e -> {
            try {
                MenuBarActions.mergeGeosetActionRes(mainPanel);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        scriptsMenu.add(mergeGeoset);

        JMenuItem nullmodelButton = new JMenuItem("Edit/delete model components");
        nullmodelButton.setMnemonic(KeyEvent.VK_E);
        nullmodelButton.setAccelerator(KeyStroke.getKeyStroke("control E"));
        nullmodelButton.addActionListener(e -> MenuBarActions.nullmodelButtonActionRes(mainPanel));
        mainPanel.nullmodelButton = nullmodelButton;
        scriptsMenu.add(nullmodelButton);

        createAndAddMenuItem("Export Animated to Static Mesh", scriptsMenu, KeyEvent.VK_E, e -> {
            MenuBarActions.exportAnimatedToStaticMeshActionRes(mainPanel); });

        createAndAddMenuItem("Export Animated Frame PNG", scriptsMenu, KeyEvent.VK_F, e -> {
            MenuBarActions.exportAnimatedFramePNGActionRes(mainPanel);});

        createAndAddMenuItem("Create Back2Back Animation", scriptsMenu, KeyEvent.VK_P, e -> {
            MenuBarActions.combineAnimsActionRes(mainPanel);});

        createAndAddMenuItem("Change Animation Lengths by Scaling", scriptsMenu, KeyEvent.VK_A, e -> MenuBarActions.scaleAnimationsActionRes(mainPanel));

        createAndAddMenuItem("Assign FormatVersion 800", scriptsMenu, KeyEvent.VK_A, e -> mainPanel.currentMDL().setFormatVersion(800));

        createAndAddMenuItem("Assign FormatVersion 1000", scriptsMenu, KeyEvent.VK_A, e -> mainPanel.currentMDL().setFormatVersion(1000));

        createAndAddMenuItem("SD -> HD (highly experimental, requires 900 or 1000)", scriptsMenu, KeyEvent.VK_A, e -> EditableModel.makeItHD(mainPanel.currentMDL()));

        createAndAddMenuItem("HD -> SD (highly experimental, becomes 800)", scriptsMenu, KeyEvent.VK_A, e -> EditableModel.convertToV800(1, mainPanel.currentMDL()));

        createAndAddMenuItem("Recalculate Tangents (requires 900 or 1000)", scriptsMenu, KeyEvent.VK_A, e -> EditableModel.recalculateTangents(mainPanel.currentMDL(), mainPanel));

        final JMenuItem jokebutton = new JMenuItem("Load Retera Land");
        jokebutton.setMnemonic(KeyEvent.VK_A);
        jokebutton.addActionListener(e -> {
            MenuBarActions.jokeButtonActionResponse(mainPanel);
        });
//		scriptsMenu.add(jokebutton);
    }

    private static void minimizeGeosetActionRes(MainPanel mainPanel) {
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

    private static boolean mergableGeosetAnims(final GeosetAnim first, final GeosetAnim second) {
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

                        ModelStructureChangeListenerImplementation.reloadComponentBrowser(mainPanel.geoControlModelData, modelPanel);
                    }
                    mainPanel.profile.getPreferences().setTeamColor(teamColorValueNumber);
                });
            } catch (final Exception ex) {
                // load failed
                break;
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
                    MPQBrowserView.loadFile(mainPanel, mainPanel.currentFile);
                });
                mainPanel.recentMenu.add(item, mainPanel.recentMenu.getItemCount() - 2);
            }
        }
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
            MPQBrowserView.setCurrentModel(mainPanel, lastUnclosedModelPanel);
        }
        return success;
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
                            unitEditorTree.setUnitDataAndReloadVerySlowly(MainLayoutCreator.getUnitData());
                        } else if (dataType == MutableObjectData.WorldEditorDataType.DOODADS) {
                            System.out.println("saw doodad tree");
                            unitEditorTree.setUnitDataAndReloadVerySlowly(MenuBarActions.getDoodadData());
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

    static void showVertexModifyControlsActionRes(List<ModelPanel> modelPanels, ProgramPreferences prefs, JCheckBoxMenuItem showVertexModifyControls) {
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

    static void importButtonSActionRes() {
        final JFrame frame = new JFrame("Animation Transferer");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(new AnimationTransfer(frame));
        frame.setIconImage(AnimIcon.getImage());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
