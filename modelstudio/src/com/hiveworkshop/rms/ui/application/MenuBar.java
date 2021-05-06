//package com.hiveworkshop.rms.ui.application;
//
//import com.hiveworkshop.rms.editor.model.Material;
//import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
//import com.hiveworkshop.rms.parsers.blp.BLPHandler;
//import com.hiveworkshop.rms.parsers.slk.DataTable;
//import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
//import com.hiveworkshop.rms.ui.application.scripts.AnimationTransfer;
//import com.hiveworkshop.rms.ui.application.tools.EditTexturesPopupPanel;
//import com.hiveworkshop.rms.ui.application.tools.KeyframeCopyPanel;
//import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
//import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorTree;
//import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
//import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
//import com.hiveworkshop.rms.ui.browsers.mpq.MPQBrowser;
//import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
//import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
//import com.hiveworkshop.rms.ui.gui.modeledit.util.TransferActionListener;
//import com.hiveworkshop.rms.ui.icons.RMSIcons;
//import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
//import com.hiveworkshop.rms.ui.preferences.SaveProfile;
//import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener;
//import com.hiveworkshop.rms.util.SmartButtonGroup;
//import net.infonode.docking.DockingWindow;
//import net.infonode.docking.View;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionListener;
//import java.awt.event.KeyEvent;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createAndAddMenuItem;
//import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenu;
//
//public class MenuBar {
//    static JMenuBar menuBar;
//    static MainPanel mainPanel;
//
//    public static final ImageIcon AnimIcon = RMSIcons.AnimIcon;
//
//    static JMenu recentMenu;
//    static JMenu toolsMenu;
//    static JMenu windowMenu;
//    static JMenu teamColorMenu;
//    static List<RecentItem> recentItems = new ArrayList<>();
//    static WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier directoryChangeNotifier = new WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier();
//
//    public static JMenuBar createMenuBar(MainPanel mainPanel) {
//        MenuBar.mainPanel = mainPanel;
//        menuBar = new JMenuBar();
//
//        recentMenu = createMenu("Open Recent", KeyEvent.VK_R, "Allows you to access recently opened files.");
//        recentMenu.add(new JSeparator());
//        createAndAddMenuItem("Clear", recentMenu, KeyEvent.VK_C, e -> MenuBarActions.clearRecent(mainPanel));
//
//        JMenu fileMenu = createMenu("File", KeyEvent.VK_F, "Allows the user to open, save, close, and manipulate files.");
//        fillFileMenu(mainPanel, fileMenu);
//
//        JMenu editMenu = createMenu("Edit", KeyEvent.VK_E, "Allows the user to use various tools to edit the currently selected model.");
//        fillEditMenu(mainPanel, editMenu);
//
//        toolsMenu = createMenu("Tools", KeyEvent.VK_T, "Allows the user to use various model editing tools. (You must open a model before you may use this menu.)");
//        toolsMenu.setEnabled(false);
//        fillToolsMenu(mainPanel);
//
//        JMenu viewMenu = createMenu("View", -1, "Allows the user to control view settings.");
//        fillViewMenu(mainPanel, viewMenu);
//
//        MenuBar.teamColorMenu = createMenu("Team Color", -1, "Allows the user to control team color settings.");
//        createTeamColorMenuItems(mainPanel);
//
//        MenuBar.windowMenu = createMenu("Window", KeyEvent.VK_W, "Allows the user to open various windows containing the program features.");
//        fillWindowsMenu(mainPanel, MenuBar.windowMenu);
//
//        JMenu addMenu = createMenu("Add", KeyEvent.VK_A, "Allows the user to add new components to the model.");
//        fillAddMenu(mainPanel, addMenu);
//
//        JMenu scriptsMenu = createMenu("Scripts", KeyEvent.VK_A, "Allows the user to execute model edit scripts.");
//        fillScriptsMenu(mainPanel, scriptsMenu);
//
//        JMenu aboutMenu = createMenu("Help", KeyEvent.VK_H, "");
//        fillAboutMenu(mainPanel, aboutMenu);
//
////        final JMenuItem fixReteraLand = new JMenuItem("Fix Retera Land");
////
////        fixReteraLand.setMnemonic(KeyEvent.VK_A);
////        fixReteraLand.addActionListener(e -> {
////            final EditableModel currentMDL = mainPanel.currentMDL();
////            for (final Geoset geo : currentMDL.getGeosets()) {
////                final Animation anim = new Animation(new ExtLog(currentMDL.getExtents()));
////                geo.add(anim);
////            }
////        });
//
//        directoryChangeNotifier.subscribe(() -> {
//            GameDataFileSystem.refresh(SaveProfile.get().getDataSources());
//            // cache priority order...
//            UnitOptionPanel.dropRaceCache();
//            DataTable.dropCache();
//            ModelOptionPanel.dropCache();
//            WEString.dropCache();
//            BLPHandler.get().dropCache();
//            MenuBar.teamColorMenu.removeAll();
//            createTeamColorMenuItems(mainPanel);
//            traverseAndReloadData(mainPanel.rootWindow);
//        });
//
//        menuBar.add(fileMenu);
//        menuBar.add(editMenu);
//        menuBar.add(toolsMenu);
//
//        menuBar.add(viewMenu);
//        menuBar.add(MenuBar.teamColorMenu);
//        menuBar.add(MenuBar.windowMenu);
//        menuBar.add(addMenu);
//        menuBar.add(scriptsMenu);
//        menuBar.add(aboutMenu);
//
//        for (int i = 0; i < menuBar.getMenuCount(); i++) {
//            menuBar.getMenu(i).getPopupMenu().setLightWeightPopupEnabled(false);
//        }
//
//        updateRecent();
//        return menuBar;
//    }
//
//    private static void fillFileMenu(MainPanel mainPanel, JMenu fileMenu) {
//        FileDialog fileDialog = new FileDialog(mainPanel);
//        createAndAddMenuItem("New", fileMenu, KeyEvent.VK_N, KeyStroke.getKeyStroke("control N"), e -> MenuBarActions.newModel(mainPanel));
//
//        createAndAddMenuItem("Open", fileMenu, KeyEvent.VK_O, KeyStroke.getKeyStroke("control O"), e -> fileDialog.onClickOpen());
//
//        fileMenu.add(recentMenu);
//
//        JMenu fetch = new JMenu("Open Internal");
//        fetch.setMnemonic(KeyEvent.VK_F);
//        fileMenu.add(fetch);
//
//        createAndAddMenuItem("Unit", fetch, KeyEvent.VK_U, KeyStroke.getKeyStroke("control U"), e -> InternalFileLoader.fetchUnit(mainPanel));
//
//        createAndAddMenuItem("Model", fetch, KeyEvent.VK_M, KeyStroke.getKeyStroke("control M"), e -> InternalFileLoader.fetchModel(mainPanel));
//
//        createAndAddMenuItem("Object Editor", fetch, KeyEvent.VK_O, KeyStroke.getKeyStroke("control O"), e -> InternalFileLoader.fetchObject(mainPanel));
//
//        fetch.add(new JSeparator());
//
//        JCheckBoxMenuItem fetchPortraitsToo = new JCheckBoxMenuItem("Fetch portraits, too!", true);
//        fetchPortraitsToo.setMnemonic(KeyEvent.VK_P);
//        fetchPortraitsToo.addActionListener(e -> mainPanel.prefs.setLoadPortraits(fetchPortraitsToo.isSelected()));
//        fetch.add(fetchPortraitsToo);
//        fetchPortraitsToo.setSelected(mainPanel.prefs.isLoadPortraits());
//
//        fileMenu.add(new JSeparator());
//
//        JMenu importMenu = createMenu("Import", KeyEvent.VK_I);
//        fileMenu.add(importMenu);
//
//        createAndAddMenuItem("From File", importMenu, KeyEvent.VK_I, KeyStroke.getKeyStroke("control shift I"), e -> ImportFileActions.importButtonActionRes(mainPanel));
//
//        createAndAddMenuItem("From Unit", importMenu, KeyEvent.VK_U, KeyStroke.getKeyStroke("control shift U"), e -> ImportFileActions.importUnitActionRes(mainPanel));
//
//        createAndAddMenuItem("From WC3 Model", importMenu, KeyEvent.VK_M, e -> ImportFileActions.importGameModelActionRes(mainPanel));
//
//        createAndAddMenuItem("From Object Editor", importMenu, KeyEvent.VK_O, e -> ImportFileActions.importGameObjectActionRes(mainPanel));
//
//        createAndAddMenuItem("From Workspace", importMenu, KeyEvent.VK_O, e -> ImportFileActions.importFromWorkspaceActionRes(mainPanel));
//
////        createAndAddMenuItem("Save", fileMenu, KeyEvent.VK_S, KeyStroke.getKeyStroke("control S"), e -> save(mainPanel));
//        createAndAddMenuItem("Save", fileMenu, KeyEvent.VK_S, KeyStroke.getKeyStroke("control S"), e -> fileDialog.onClickSave());
//
////        createAndAddMenuItem("Save as", fileMenu, KeyEvent.VK_A, KeyStroke.getKeyStroke("control Q"), e -> MenuBarActions.onClickSaveAs(mainPanel));
//        createAndAddMenuItem("Save as", fileMenu, KeyEvent.VK_A, KeyStroke.getKeyStroke("control Q"), e -> fileDialog.onClickSaveAs());
//
//        fileMenu.add(new JSeparator());
//
//        createAndAddMenuItem("Export Material as Texture", fileMenu, KeyEvent.VK_E, e -> ExportTextureDialog.exportMaterialAsTextures(mainPanel));
//        createAndAddMenuItem("Export Texture", fileMenu, KeyEvent.VK_E, e -> ExportTextureDialog.exportTextures(mainPanel));
//
//        fileMenu.add(new JSeparator());
//
//        createAndAddMenuItem("Revert", fileMenu, -1, e -> ModelLoader.revert(mainPanel));
//
//        createAndAddMenuItem("Close", fileMenu, KeyEvent.VK_E, KeyStroke.getKeyStroke("control E"), e -> MenuBarActions.closeModelPanel(mainPanel));
//
//        fileMenu.add(new JSeparator());
//
//        createAndAddMenuItem("Exit", fileMenu, KeyEvent.VK_E, e -> closeProgram(mainPanel));
//    }
//
//    private static void fillEditMenu(final MainPanel mainPanel, JMenu editMenu) {
//        editMenu.add(mainPanel.getUndoHandler().getUndo());
//
//        editMenu.add(mainPanel.getUndoHandler().getRedo());
//
//
//        editMenu.add(new JSeparator());
//        final JMenu optimizeMenu = createMenu("Optimize", KeyEvent.VK_O);
//        editMenu.add(optimizeMenu);
//        createAndAddMenuItem("Linearize Animations", optimizeMenu, KeyEvent.VK_L, e -> ModelEditActions.linearizeAnimations(mainPanel));
//
//        createAndAddMenuItem("Simplify Keyframes (Experimental)", optimizeMenu, KeyEvent.VK_K, e -> ModelEditActions.simplifyKeyframes(mainPanel));
//
//        createAndAddMenuItem("Minimize Geosets", optimizeMenu, KeyEvent.VK_K, e -> MenuBarActions.minimizeGeoset(mainPanel));
//
//        createAndAddMenuItem("Sort Nodes", optimizeMenu, KeyEvent.VK_S, e -> MenuBarActions.sortBones(mainPanel));
//
//        final JMenuItem flushUnusedTexture = new JMenuItem("Flush Unused Texture");
//        flushUnusedTexture.setEnabled(false);
//        flushUnusedTexture.setMnemonic(KeyEvent.VK_F);
//        optimizeMenu.add(flushUnusedTexture);
//
//        createAndAddMenuItem("Remove Materials Duplicates", optimizeMenu, KeyEvent.VK_S, e -> MenuBarActions.removeMaterialDuplicates(mainPanel));
//
//        createAndAddMenuItem("Recalculate Normals", editMenu, -1, KeyStroke.getKeyStroke("control N"), e -> ModelEditActions.recalculateNormals(mainPanel));
//
//        createAndAddMenuItem("Recalculate Extents", editMenu, -1, KeyStroke.getKeyStroke("control shift E"), e -> ModelEditActions.recalculateExtents(mainPanel));
//
//        editMenu.add(new JSeparator());
//        final TransferActionListener transferActionListener = new TransferActionListener();
//        final ActionListener copyActionListener = e -> MenuBarActions.copyCutPast(mainPanel, transferActionListener, e);
//
//
//        createAndAddMenuItem("Cut", editMenu, KeyStroke.getKeyStroke("control X"), (String) TransferHandler.getCutAction().getValue(Action.NAME), copyActionListener);
//
//        createAndAddMenuItem("Copy", editMenu, KeyStroke.getKeyStroke("control C"), (String) TransferHandler.getCopyAction().getValue(Action.NAME), copyActionListener);
//
//        createAndAddMenuItem("Paste", editMenu, KeyStroke.getKeyStroke("control V"), (String) TransferHandler.getPasteAction().getValue(Action.NAME), copyActionListener);
//
//        createAndAddMenuItem("Duplicate", editMenu, -1, KeyStroke.getKeyStroke("control D"), mainPanel.cloneAction);
//
//        editMenu.add(new JSeparator());
//
////        createAndAddMenuItem("Snap Vertices", editMenu, -1, KeyStroke.getKeyStroke("control shift W"), e -> MenuBarActions.getSnapVerticiesAction(mainPanel));
//        createAndAddMenuItem("Snap Vertices", editMenu, -1, KeyStroke.getKeyStroke("control shift W"), e -> ModelEditActions.snapVertices(mainPanel));
//
//        createAndAddMenuItem("Snap Normals", editMenu, -1, KeyStroke.getKeyStroke("control L"), e -> ModelEditActions.snapNormals(mainPanel));
//
//        editMenu.add(new JSeparator());
//
//        createAndAddMenuItem("Select All", editMenu, -1, KeyStroke.getKeyStroke("control A"), mainPanel.selectAllAction);
//
//        createAndAddMenuItem("Invert Selection", editMenu, -1, KeyStroke.getKeyStroke("control I"), mainPanel.invertSelectAction);
//
//        createAndAddMenuItem("Expand Selection", editMenu, -1, KeyStroke.getKeyStroke("control E"), mainPanel.expandSelectionAction);
//
//        editMenu.addSeparator();
//
//        createAndAddMenuItem("Delete", editMenu, KeyEvent.VK_D, mainPanel.deleteAction);
//
//        editMenu.addSeparator();
//        createAndAddMenuItem("Preferences Window", editMenu, KeyEvent.VK_P, e -> MenuBarActions.openPreferences(mainPanel));
//    }
//
//    private static void fillToolsMenu(MainPanel mainPanel) {
//        JMenuItem showMatrices = new JMenuItem("View Selected \"Matrices\"");
//        // showMatrices.setMnemonic(KeyEvent.VK_V);
//        showMatrices.addActionListener(e -> ModelEditActions.viewMatrices(mainPanel));
//        toolsMenu.add(showMatrices);
//
//        JMenuItem insideOut = new JMenuItem("Flip all selected faces");
//        insideOut.setMnemonic(KeyEvent.VK_I);
//        insideOut.addActionListener(e -> ModelEditActions.insideOut(mainPanel));
//        insideOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));
//        toolsMenu.add(insideOut);
//
//        JMenuItem insideOutNormals = new JMenuItem("Flip all selected normals");
//        insideOutNormals.addActionListener(e -> ModelEditActions.insideOutNormals(mainPanel));
//        toolsMenu.add(insideOutNormals);
//
//        toolsMenu.add(new JSeparator());
//
//        createAndAddMenuItem("Edit UV Mapping", toolsMenu, KeyEvent.VK_U, e -> EditUVsPanel.showEditUVs(mainPanel));
//
//        JMenuItem editTextures = new JMenuItem("Edit Textures");
//        editTextures.setMnemonic(KeyEvent.VK_T);
//        editTextures.addActionListener(e -> EditTexturesPopupPanel.show(mainPanel));
//        toolsMenu.add(editTextures);
//
//        createAndAddMenuItem("Rig Selection", toolsMenu, KeyEvent.VK_R, KeyStroke.getKeyStroke("control W"), mainPanel.rigAction);
//
//        JMenu tweaksSubmenu = new JMenu("Tweaks");
//        tweaksSubmenu.setMnemonic(KeyEvent.VK_T);
//        tweaksSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to tweak conversion mistakes.");
//        toolsMenu.add(tweaksSubmenu);
//        createAndAddMenuItem("Flip All UVs U", tweaksSubmenu, KeyEvent.VK_U, e -> ModelEditActions.flipAllUVsU(mainPanel));
//
//        JMenuItem flipAllUVsV = new JMenuItem("Flip All UVs V");
//        // flipAllUVsV.setMnemonic(KeyEvent.VK_V);
//        flipAllUVsV.addActionListener(e -> ModelEditActions.flipAllUVsV(mainPanel));
//        tweaksSubmenu.add(flipAllUVsV);
//
//        createAndAddMenuItem("Swap All UVs U for V", tweaksSubmenu, KeyEvent.VK_S, e -> ModelEditActions.inverseAllUVs(mainPanel));
//
//        JMenu mirrorSubmenu = new JMenu("Mirror");
//        mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
//        mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");
//        toolsMenu.add(mirrorSubmenu);
//
//        JCheckBoxMenuItem mirrorFlip = new JCheckBoxMenuItem("Automatically flip after mirror (preserves surface)", true);
//        mirrorFlip.setMnemonic(KeyEvent.VK_A);
//
//        createAndAddMenuItem("Mirror X", mirrorSubmenu, KeyEvent.VK_X, e -> ModelEditActions.mirrorAxis(mainPanel, (byte) 0, mirrorFlip.isSelected()));
//
//        createAndAddMenuItem("Mirror Y", mirrorSubmenu, KeyEvent.VK_Y, e -> ModelEditActions.mirrorAxis(mainPanel, (byte) 1, mirrorFlip.isSelected()));
//
//        createAndAddMenuItem("Mirror Z", mirrorSubmenu, KeyEvent.VK_Z, e -> ModelEditActions.mirrorAxis(mainPanel, (byte) 2, mirrorFlip.isSelected()));
//
//        mirrorSubmenu.add(new JSeparator());
//
//        mirrorSubmenu.add(mirrorFlip);
//    }
//
//    private static void fillViewMenu(MainPanel mainPanel, JMenu viewMenu) {
//        JCheckBoxMenuItem textureModels = new JCheckBoxMenuItem("Texture Models", true);
//        textureModels.setMnemonic(KeyEvent.VK_T);
//        textureModels.setSelected(mainPanel.prefs.textureModels());
//        textureModels.addActionListener(e -> mainPanel.prefs.setTextureModels(textureModels.isSelected()));
//        viewMenu.add(textureModels);
//
//        JCheckBoxMenuItem showNormals = new JCheckBoxMenuItem("Show Normals", true);
//        showNormals.setMnemonic(KeyEvent.VK_N);
//        showNormals.setSelected(mainPanel.prefs.showNormals());
//        showNormals.addActionListener(e -> mainPanel.prefs.setShowNormals(showNormals.isSelected()));
//        viewMenu.add(showNormals);
//
//        JCheckBoxMenuItem renderParticles = new JCheckBoxMenuItem("Render Particles", true);
//        renderParticles.setMnemonic(KeyEvent.VK_P);
//        renderParticles.setSelected(mainPanel.prefs.getRenderParticles());
//        renderParticles.addActionListener(e -> mainPanel.prefs.setRenderParticles(renderParticles.isSelected()));
//        viewMenu.add(renderParticles);
//
//        JCheckBoxMenuItem showPerspectiveGrid = new JCheckBoxMenuItem("Show Perspective Grid", true);
//        showPerspectiveGrid.setMnemonic(KeyEvent.VK_G);
//        showPerspectiveGrid.setSelected(mainPanel.prefs.showPerspectiveGrid());
//        showPerspectiveGrid.addActionListener(e -> mainPanel.prefs.setShowPerspectiveGrid(showPerspectiveGrid.isSelected()));
//        viewMenu.add(showPerspectiveGrid);
//
//        JMenuItem newDirectory = new JMenuItem("Change Game Directory");
//        newDirectory.setAccelerator(KeyStroke.getKeyStroke("control shift D"));
//        newDirectory.setToolTipText("Changes the directory from which to load texture files for the 3D display.");
//        newDirectory.setMnemonic(KeyEvent.VK_D);
//        newDirectory.addActionListener(e -> mainPanel.getUndoHandler().refreshUndo());
////		viewMenu.add(newDirectory);
//
//        viewMenu.add(new JSeparator());
//
//        JCheckBoxMenuItem showVertexModifyControls = new JCheckBoxMenuItem("Show Viewport Buttons", true);
//        // showVertexModifyControls.setMnemonic(KeyEvent.VK_V);
//        showVertexModifyControls.addActionListener(e -> showVertexModifyControls(mainPanel.modelPanels, mainPanel.prefs, showVertexModifyControls));
//        viewMenu.add(showVertexModifyControls);
//
//        viewMenu.add(new JSeparator());
//
//        JMenu viewModeMenu = new JMenu("3D View Mode");
//        viewMenu.add(viewModeMenu);
//
//        SmartButtonGroup viewModes2 = new SmartButtonGroup();
//
//        viewModes2.addJRadioButtonMenuItem("Wireframe", e -> ProgramGlobals.prefs.setViewMode(0));
//        viewModes2.addJRadioButtonMenuItem("Solid", e -> ProgramGlobals.prefs.setViewMode(1));
//        viewModes2.setSelectedIndex(ProgramGlobals.prefs.getViewMode());
//
//        viewModeMenu.add(viewModes2.getButton(0));
//        viewModeMenu.add(viewModes2.getButton(1));
//
////        ButtonGroup viewModes = new ButtonGroup();
////
////        JRadioButtonMenuItem wireframe = new JRadioButtonMenuItem("Wireframe");
////        wireframe.addActionListener(e -> repaint(mainPanel, 0));
////        wireframe.setSelected(mainPanel.prefs.getViewMode() == 0);
////        viewModeMenu.add(wireframe);
////        viewModes.add(wireframe);
////
////        JRadioButtonMenuItem solid = new JRadioButtonMenuItem("Solid");
////        solid.addActionListener(e -> repaint(mainPanel, 1));
////        solid.setSelected(mainPanel.prefs.getViewMode() == 1);
////        viewModeMenu.add(solid);
////        viewModes.add(solid);
//
//    }
//
//    private static void fillWindowsMenu(MainPanel mainPanel, JMenu windowMenu) {
//        JMenuItem resetViewButton = new JMenuItem("Reset Layout");
//        resetViewButton.addActionListener(e -> WindowHandler.resetView(mainPanel));
//        windowMenu.add(resetViewButton);
//
//        JMenu viewsMenu = createMenu("Views", KeyEvent.VK_V);
//        windowMenu.add(viewsMenu);
//
////        JMenuItem testItem = new JMenuItem("test");
////        testItem.addActionListener(new OpenViewAction(mainPanel.rootWindow, "Animation Preview", () -> MenuBarActions.testItemResponse(mainPanel)));
//
//
//        createAndAddMenuItem("Animation Preview", viewsMenu, KeyEvent.VK_A, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Animation Preview", mainPanel.previewView));
//
//        createAndAddMenuItem("Animation Controller", viewsMenu, KeyEvent.VK_C, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Animation Controller", mainPanel.animationControllerView));
//
//        createAndAddMenuItem("Modeling", viewsMenu, KeyEvent.VK_M, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Modeling", mainPanel.creatorView));
//
//        createAndAddMenuItem("Outliner", viewsMenu, KeyEvent.VK_O, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Outliner", mainPanel.viewportControllerWindowView));
//
//        createAndAddMenuItem("Perspective", viewsMenu, KeyEvent.VK_P, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Perspective", mainPanel.perspectiveView));
//
//        createAndAddMenuItem("Front", viewsMenu, KeyEvent.VK_F, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Front", mainPanel.frontView));
//
//        createAndAddMenuItem("Side", viewsMenu, KeyEvent.VK_S, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Side", mainPanel.leftView));
//
//        createAndAddMenuItem("Bottom", viewsMenu, KeyEvent.VK_B, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Bottom", mainPanel.bottomView));
//
//        createAndAddMenuItem("Tools", viewsMenu, KeyEvent.VK_T, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Tools", mainPanel.toolView));
//
//        createAndAddMenuItem("Contents", viewsMenu, KeyEvent.VK_C, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Model", mainPanel.modelDataView));
//
//        createAndAddMenuItem("Footer", viewsMenu, OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Footer", mainPanel.timeSliderView));
//
////        createAndAddMenuItem("Matrix Eater Script", viewsMenu, KeyEvent.VK_H, KeyStroke.getKeyStroke("control P"), OpenViewAction.getOpenViewAction(mainPanel.rootWindow, "Matrix Eater Script", ScriptView.createHackerView(mainPanel)));
//        createAndAddMenuItem("Matrix Eater Script", viewsMenu, KeyEvent.VK_H, KeyStroke.getKeyStroke("control P"), e -> ScriptView.openScriptView(mainPanel));
//
//        JMenu browsersMenu = createMenu("Browsers", KeyEvent.VK_B);
//        windowMenu.add(browsersMenu);
//
//        createAndAddMenuItem("Data Browser", browsersMenu, KeyEvent.VK_A, e -> MPQBrowserView.openMPQViewer(mainPanel));
//
//        createAndAddMenuItem("Unit Browser", browsersMenu, KeyEvent.VK_U, e -> MenuBarActions.openUnitViewer(mainPanel));
//
//        createAndAddMenuItem("Doodad Browser", browsersMenu, KeyEvent.VK_D, e -> InternalFileLoader.OpenDoodadViewer(mainPanel));
//
//        JMenuItem hiveViewer = new JMenuItem("Hive Browser");
//        hiveViewer.setMnemonic(KeyEvent.VK_H);
//        hiveViewer.addActionListener(e -> MenuBarActions.openHiveViewer(mainPanel));
//
//        windowMenu.addSeparator();
//    }
//
//    private static void fillAddMenu(final MainPanel mainPanel, JMenu addMenu) {
//        JMenu addParticle = new JMenu("Particle");
//        addParticle.setMnemonic(KeyEvent.VK_P);
//        addMenu.add(addParticle);
//
//        AddParticlePanel.addParticleButtons(mainPanel, addParticle);
//        createAndAddMenuItem("Empty Popcorn", addParticle, KeyEvent.VK_O, e -> AddParticlePanel.addEmptyPopcorn(mainPanel));
//
//        JMenu animationMenu = new JMenu("Animation");
//        animationMenu.setMnemonic(KeyEvent.VK_A);
//        addMenu.add(animationMenu);
//
//        createAndAddMenuItem("Empty", animationMenu, KeyEvent.VK_F, e -> AddSingleAnimationActions.addEmptyAnimation(mainPanel));
//
//        createAndAddMenuItem("Rising/Falling Birth/Death", animationMenu, KeyEvent.VK_R, e -> AddBirthDeathSequences.riseFallBirthActionRes(mainPanel));
//
//        JMenu singleAnimationMenu = new JMenu("Single");
//        singleAnimationMenu.setMnemonic(KeyEvent.VK_S);
//        animationMenu.add(singleAnimationMenu);
//
//        createAndAddMenuItem("From File", singleAnimationMenu, KeyEvent.VK_F, e -> AddSingleAnimationActions.addAnimationFromFile(mainPanel));
//
//        createAndAddMenuItem("From Unit", singleAnimationMenu, KeyEvent.VK_U, e -> AddSingleAnimationActions.addAnimationFromUnit(mainPanel));
//
//        createAndAddMenuItem("From Model", singleAnimationMenu, KeyEvent.VK_M, e -> AddSingleAnimationActions.addAnimFromModel(mainPanel));
//
//        createAndAddMenuItem("From Object", singleAnimationMenu, KeyEvent.VK_O, e -> AddSingleAnimationActions.addAnimationFromObject(mainPanel));
//
//        createAndAddMenuItem("Material", addMenu, KeyEvent.VK_M, e -> MenuBarActions.addNewMaterial(mainPanel));
////        JMenu addMaterial = new JMenu("Material");
////        addMaterial.setMnemonic(KeyEvent.VK_M);
////        addMenu.add(addMaterial);
//    }
//
//    private static void fillScriptsMenu(MainPanel mainPanel, JMenu scriptsMenu) {
//        createAndAddMenuItem("Oinkerwinkle-Style AnimTransfer", scriptsMenu, KeyEvent.VK_P, KeyStroke.getKeyStroke("control shift S"),  e -> new AnimationTransfer().showWindow());
//
//        FileDialog fileDialog = new FileDialog(mainPanel);
//
//        JMenuItem mergeGeoset = new JMenuItem("Oinkerwinkle-Style Merge Geoset");
//        mergeGeoset.setMnemonic(KeyEvent.VK_M);
//        mergeGeoset.setAccelerator(KeyStroke.getKeyStroke("control M"));
//        mergeGeoset.addActionListener(e -> {
//            try {
//                ScriptActions.mergeGeosetActionRes(mainPanel);
//            } catch (IOException ioException) {
//                ioException.printStackTrace();
//            }
//        });
//        scriptsMenu.add(mergeGeoset);
//
//        JMenuItem nullmodelButton = new JMenuItem("Edit/delete model components");
//        nullmodelButton.setMnemonic(KeyEvent.VK_E);
//        nullmodelButton.setAccelerator(KeyStroke.getKeyStroke("control E"));
//        nullmodelButton.addActionListener(e -> ScriptActions.nullmodelButtonActionRes(mainPanel));
//        scriptsMenu.add(nullmodelButton);
//
//        createAndAddMenuItem("Export Animated to Static Mesh", scriptsMenu, KeyEvent.VK_E, e -> ScriptActions.exportAnimatedToStaticMesh(mainPanel));
//
////        createAndAddMenuItem("Export Animated Frame PNG", scriptsMenu, KeyEvent.VK_F, e -> ScriptActions.exportAnimatedFramePNG(mainPanel));
//        createAndAddMenuItem("Export Animated Frame PNG", scriptsMenu, KeyEvent.VK_F, e -> fileDialog.exportAnimatedFramePNG());
//
//        createAndAddMenuItem("Copy Keyframes Between Animations", scriptsMenu, KeyEvent.VK_K, e -> KeyframeCopyPanel.show(mainPanel));
//
//        createAndAddMenuItem("Create Back2Back Animation", scriptsMenu, KeyEvent.VK_P, e -> ScriptActions.combineAnimations(mainPanel));
//
//        createAndAddMenuItem("Change Animation Lengths by Scaling", scriptsMenu, KeyEvent.VK_A, e -> ScriptActions.scaleAnimations(mainPanel));
//
//        createAndAddMenuItem("Assign FormatVersion 800", scriptsMenu, KeyEvent.VK_A, e -> mainPanel.currentMDL().setFormatVersion(800));
//
//        createAndAddMenuItem("Assign FormatVersion 1000", scriptsMenu, KeyEvent.VK_A, e -> mainPanel.currentMDL().setFormatVersion(1000));
//
//        createAndAddMenuItem("SD -> HD (highly experimental, requires 900 or 1000)", scriptsMenu, KeyEvent.VK_A, e -> ScriptActions.makeItHD(mainPanel.currentMDL()));
//
//        createAndAddMenuItem("HD -> SD (highly experimental, becomes 800)", scriptsMenu, KeyEvent.VK_A, e -> ScriptActions.convertToV800(1, mainPanel.currentMDL()));
//
//        createAndAddMenuItem("Remove LoDs (highly experimental)", scriptsMenu, KeyEvent.VK_A, e -> ScriptActions.removeLoDs(mainPanel));
//
//        createAndAddMenuItem("Recalculate Tangents (requires 900 or 1000)", scriptsMenu, KeyEvent.VK_A, e -> MenuBarActions.recalculateTangents(mainPanel.currentMDL(), mainPanel));
//
//        final JMenuItem jokebutton = new JMenuItem("Load Retera Land");
//        jokebutton.setMnemonic(KeyEvent.VK_A);
//        jokebutton.addActionListener(e -> ScriptActions.jokeButtonClickResponse(mainPanel));
////		scriptsMenu.add(jokebutton);
//    }
//
//    private static void fillAboutMenu(MainPanel mainPanel, JMenu aboutMenu) {
//        createAndAddMenuItem("Changelog", aboutMenu, KeyEvent.VK_A, e -> MenuBarActions.createAndShowRtfPanel("docs/changelist.rtf", "Changelog"));
//
//        createAndAddMenuItem("About", aboutMenu, KeyEvent.VK_A, e -> MenuBarActions.createAndShowRtfPanel("docs/credits.rtf", "About"));
//    }
//
//    private static void repaint(MainPanel mainPanel, int radioButton) {
//        if (radioButton == 0) {
//            mainPanel.prefs.setViewMode(0);
//        } else if (radioButton == 1) {
//            mainPanel.prefs.setViewMode(1);
//        } else {
//            mainPanel.prefs.setViewMode(-1);
//        }
//        mainPanel.repaint();
//    }
//
//    private static void closeProgram(MainPanel mainPanel) {
//        if (closeAll(mainPanel)) {
//            MainFrame.frame.dispose();
//        }
//    }
//
//    private static void createTeamColorMenuItems(MainPanel mainPanel) {
//        for (int i = 0; i < 25; i++) {
//            final String colorNumber = String.format("%2s", i).replace(' ', '0');
//            try {
//                final String colorName = WEString.getString("WESTRING_UNITCOLOR_" + colorNumber);
//                final JMenuItem menuItem = new JMenuItem(colorName, new ImageIcon(BLPHandler.get()
//                        .getGameTex("ReplaceableTextures\\TeamColor\\TeamColor" + colorNumber + ".blp")));
//                MenuBar.teamColorMenu.add(menuItem);
//                final int teamColorValueNumber = i;
//                menuItem.addActionListener(e -> {
//                    Material.teamColor = teamColorValueNumber;
//                    final ModelPanel modelPanel = mainPanel.currentModelPanel();
//                    if (modelPanel != null) {
//                        modelPanel.getAnimationViewer().reloadAllTextures();
//                        modelPanel.getPerspArea().reloadAllTextures();
//
//                        modelPanel.reloadComponentBrowser();
//                    }
//                    mainPanel.profile.getPreferences().setTeamColor(teamColorValueNumber);
//                });
//            } catch (final Exception ex) {
//                // load failed
//                break;
//            }
//        }
//    }
//
//    public static void updateRecent() {
//        final List<String> recent = SaveProfile.get().getRecent();
//        for (final RecentItem recentItem : recentItems) {
//            recentMenu.remove(recentItem);
//        }
//        recentItems.clear();
//        for (int i = 0; i < recent.size(); i++) {
//            final String fp = recent.get(recent.size() - i - 1);
//            if ((recentItems.size() <= i) || (!recentItems.get(i).filepath.equals(fp))) {
//                // String[] bits = recent.get(i).split("/");
//
//                final RecentItem item = new RecentItem(new File(fp).getName());
//                item.filepath = fp;
//                recentItems.add(item);
//                item.addActionListener(e -> {
//
//                    FileDialog.setCurrentFile(new File(item.filepath));
//                    FileDialog.setCurrentPath(FileDialog.getCurrentFile().getParentFile());
//                    // frontArea.clearGeosets();
//                    // sideArea.clearGeosets();
//                    // botArea.clearGeosets();
////                    toolsMenu.getAccessibleContext().setAccessibleDescription(
////                            "Allows the user to control which parts of the model are displayed for editing.");
//                    toolsMenu.setEnabled(true);
//                    SaveProfile.get().addRecent(FileDialog.getCurrentFile().getPath());
//                    updateRecent();
//                    ModelLoader.loadFile(mainPanel, FileDialog.getCurrentFile());
//                });
//                recentMenu.add(item, recentMenu.getItemCount() - 2);
//            }
//        }
//    }
//
//    public static boolean closeAll(MainPanel mainPanel) {
//        boolean success = true;
//        final Iterator<ModelPanel> iterator = mainPanel.modelPanels.iterator();
//        boolean closedCurrentPanel = false;
//        ModelPanel lastUnclosedModelPanel = null;
//        while (iterator.hasNext()) {
//            final ModelPanel panel = iterator.next();
//            if (success = panel.close()) {
//                windowMenu.remove(panel.getMenuItem());
//                iterator.remove();
//                if (panel == mainPanel.currentModelPanel) {
//                    closedCurrentPanel = true;
//                }
//            } else {
//                lastUnclosedModelPanel = panel;
//                break;
//            }
//        }
//        if (closedCurrentPanel) {
//            ModelLoader.setCurrentModel(mainPanel, lastUnclosedModelPanel);
//        }
//        return success;
//    }
//
//    static void traverseAndReloadData(final DockingWindow window) {
//        final int childWindowCount = window.getChildWindowCount();
//        for (int i = 0; i < childWindowCount; i++) {
//            final DockingWindow childWindow = window.getChildWindow(i);
//            traverseAndReloadData(childWindow);
//            if (childWindow instanceof View) {
//                final View view = (View) childWindow;
//                final Component component = view.getComponent();
//                if (component instanceof JScrollPane) {
//                    final JScrollPane pane = (JScrollPane) component;
//                    final Component viewportView = pane.getViewport().getView();
//                    if (viewportView instanceof UnitEditorTree) {
//                        final UnitEditorTree unitEditorTree = (UnitEditorTree) viewportView;
//                        final MutableObjectData.WorldEditorDataType dataType = unitEditorTree.getDataType();
//                        if (dataType == MutableObjectData.WorldEditorDataType.UNITS) {
//                            System.out.println("saw unit tree");
//                            unitEditorTree.setUnitDataAndReloadVerySlowly(MainLayoutCreator.getUnitData());
//                        } else if (dataType == MutableObjectData.WorldEditorDataType.DOODADS) {
//                            System.out.println("saw doodad tree");
//                            unitEditorTree.setUnitDataAndReloadVerySlowly(MenuBarActions.getDoodadData());
//                        }
//                    }
//                } else if (component instanceof MPQBrowser) {
//                    System.out.println("saw mpq tree");
//                    final MPQBrowser comp = (MPQBrowser) component;
//                    comp.refreshTree();
//                }
//            }
//        }
//    }
//
//    static void showVertexModifyControls(List<ModelPanel> modelPanels, ProgramPreferences prefs, JCheckBoxMenuItem showVertexModifyControls) {
//        final boolean selected = showVertexModifyControls.isSelected();
//        prefs.setShowVertexModifierControls(selected);
//        for (final ModelPanel panel : modelPanels) {
//            panel.getFrontArea().setControlsVisible(selected);
//            panel.getBotArea().setControlsVisible(selected);
//            panel.getSideArea().setControlsVisible(selected);
//            final UVPanel uvPanel = panel.getEditUVPanel();
//            if (uvPanel != null) {
//                uvPanel.setControlsVisible(selected);
//            }
//        }
//    }
//
//    static class RecentItem extends JMenuItem {
//        public RecentItem(final String what) {
//            super(what);
//        }
//
//        String filepath;
//    }
//
//}
