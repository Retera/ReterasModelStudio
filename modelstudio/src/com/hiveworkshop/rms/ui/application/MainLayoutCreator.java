package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.parsers.w3o.WTSFile;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeSliderPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorTree;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorTreeBrowser;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitTabTreeBrowserBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.CreatorModelingPanel;
import de.wc3data.stream.BlizzardDataInputStream;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.function.Consumer;

public class MainLayoutCreator {
    public View viewportControllerWindowView;
    public View toolView;
    public View frontView;
    public View leftView;
    public View bottomView;
    public View perspectiveView;
    public View modelDataView;
    public View modelComponentView;
    public View previewView;
    public View animationControllerView;

    public View timeSliderView;
    public View creatorView;

    TimeSliderPanel timeSliderPanel;
    CreatorModelingPanel creatorPanel;

    public MainLayoutCreator(MainPanel mainPanel) {
        timeSliderPanel = createTimeSliderPanel(mainPanel);
        timeSliderView = TimeSliderView.createTimeSliderView(timeSliderPanel);

        creatorPanel = new CreatorModelingPanel(mainPanel.viewportListener);
        creatorView = new View("Modeling", null, creatorPanel);

        JPanel contentsDummy = new JPanel();
        contentsDummy.add(new JLabel("..."));

        modelDataView = new View("Contents", null, contentsDummy);
        modelComponentView = new View("Component", null, new JPanel());


        previewView = new View("Preview", null, new JPanel());
        animationControllerView = new View("Animation Controller", null, new JPanel());


        JPanel jPanel = new JPanel();
        jPanel.add(new JLabel("..."));
        viewportControllerWindowView = new View("Outliner", null, jPanel);// GlobalIcons.geoIcon
//		viewportControllerWindowView.getWindowProperties().setCloseEnabled(false);
//		viewportControllerWindowView.getWindowProperties().setMaximizeEnabled(true);
//		viewportControllerWindowView.getWindowProperties().setMinimizeEnabled(true);
//		viewportControllerWindowView.getWindowProperties().setRestoreEnabled(true);
        toolView = new View("Tools", null, new JPanel());

        leftView = new View("Side", null, new JPanel());
        frontView = new View("Front", null, new JPanel());
        bottomView = new View("Bottom", null, new JPanel());
        perspectiveView = new View("Perspective", null, new JPanel());
    }
//    static TabWindow createMainLayout() {
//
//        JPanel jPanel = new JPanel();
//        jPanel.add(new JLabel("..."));
//        MainPanel mainPanel = ProgramGlobals.getMainPanel();
//        mainPanel.setViewportControllerWindowView(new View("Outliner", null, jPanel));// GlobalIcons.geoIcon
////		viewportControllerWindowView.getWindowProperties().setCloseEnabled(false);
////		viewportControllerWindowView.getWindowProperties().setMaximizeEnabled(true);
////		viewportControllerWindowView.getWindowProperties().setMinimizeEnabled(true);
////		viewportControllerWindowView.getWindowProperties().setRestoreEnabled(true);
//        mainPanel.setToolView(new View("Tools", null, new JPanel()));
//
//        mainPanel.setLeftView(new View("Side", null, new JPanel()));
//        mainPanel.setFrontView(new View("Front", null, new JPanel()));
//        mainPanel.setBottomView(new View("Bottom", null, new JPanel()));
//        mainPanel.setPerspectiveView(new View("Perspective", null, new JPanel()));
//
//
//        return getStartupTabWindow(mainPanel);
//    }

    public TabWindow getStartupTabWindow() {
        SplitWindow editingTab = getEditTab();

        SplitWindow viewingTab = getViewTab();

        SplitWindow modelTab = new SplitWindow(true, 0.2f, getModelDataView(), getModelComponentView());
        modelTab.getWindowProperties().setTitleProvider(arg0 -> "Model");

        TabWindow startupTabWindow = new TabWindow(new DockingWindow[] {viewingTab, editingTab, modelTab});
//        TabWindow startupTabWindow = new TabWindow(new DockingWindow[] {editingTab, viewingTab, modelTab});
        WindowHandler.traverseAndFix(startupTabWindow);
        return startupTabWindow;
    }

    private SplitWindow getViewTab() {
        ImageIcon imageIcon;
        imageIcon = new ImageIcon(MainFrame.MAIN_PROGRAM_ICON.getScaledInstance(16, 16, Image.SCALE_FAST));

        View mpqBrowserView = MPQBrowserView.createMPQBrowser(imageIcon);

        UnitEditorTree unitEditorTree = createUnitEditorTree();
        View view = new View("Unit Browser", imageIcon, new JScrollPane(unitEditorTree));
        DockingWindow[] dockingWindow = new DockingWindow[] {view, mpqBrowserView};

        TabWindow tabWindow = new TabWindow(dockingWindow);

        tabWindow.setSelectedTab(0);

        SplitWindow animPersp = new SplitWindow(true, 0.8f, getPreviewView(), getAnimationControllerView());
        SplitWindow viewingTab = new SplitWindow(true, 0.8f, animPersp, tabWindow);

        viewingTab.getWindowProperties().setTitleProvider(arg0 -> "View");
        viewingTab.getWindowProperties().setCloseEnabled(false);
        return viewingTab;
    }

    private SplitWindow getEditTab() {
        TabWindow leftHandTabWindow = new TabWindow(new DockingWindow[] {getViewportControllerWindowView(), getToolView()});
        leftHandTabWindow.setSelectedTab(0);

        SplitWindow frBt = new SplitWindow(true, getFrontView(), getBottomView());
        SplitWindow lfPs = new SplitWindow(true, getLeftView(), getPerspectiveView());
        SplitWindow quadView = new SplitWindow(false, frBt, lfPs);

        SplitWindow splitWindow = new SplitWindow(true, 0.2f, leftHandTabWindow, new SplitWindow(true, 0.8f, quadView, getCreatorView()));

        SplitWindow editingTab = new SplitWindow(false, 0.875f, splitWindow, getTimeSliderView());

        editingTab.getWindowProperties().setCloseEnabled(false);
        editingTab.getWindowProperties().setTitleProvider(arg0 -> "Edit");
        return editingTab;
    }

    static UnitEditorTree createUnitEditorTree() {
        return new UnitEditorTreeBrowser(getUnitData(),
                new UnitTabTreeBrowserBuilder(),
                getUnitEditorSettings(),
                MutableObjectData.WorldEditorDataType.UNITS,
                (mdxFilePath, b, c, icon) -> InternalFileLoader.loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(mdxFilePath), b, c, icon));
    }

    /**
     * Right now this is a plug to the statics to load unit data.
     * However, it's a non-static method so that we can have it load from an opened map
     * in the future -- the MutableObjectData class can parse map unit data!
     */
    public static MutableObjectData getUnitData() {
        War3ObjectDataChangeset editorData = new War3ObjectDataChangeset('u');
        try {
            CompoundDataSource gameDataFileSystem = GameDataFileSystem.getDefault();
            if (gameDataFileSystem.has("war3map.w3u")) {
                editorData.load(
                        new BlizzardDataInputStream(gameDataFileSystem.getResourceAsStream("war3map.w3u")),
                        gameDataFileSystem.has("war3map.wts")
                                ? new WTSFile(gameDataFileSystem.getResourceAsStream("war3map.wts")) : null,
                        true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new MutableObjectData(MutableObjectData.WorldEditorDataType.UNITS, StandardObjectData.getStandardUnits(),
                StandardObjectData.getStandardUnitMeta(), editorData);
    }

    public static UnitEditorSettings getUnitEditorSettings() {
        return new UnitEditorSettings();
    }

    private TimeSliderPanel createTimeSliderPanel(MainPanel mainPanel) {
        TimeSliderPanel timeSliderPanel = new TimeSliderPanel(mainPanel, ProgramGlobals.getPrefs());
        timeSliderPanel.setDrawing(false);
        Consumer<Integer> timeSliderTimeListener = currentTime -> {
//			mainPanel.animatedRenderEnvironment.setCurrentTime(currentTime);
//			mainPanel.animatedRenderEnvironment.setCurrentTime(currentTime - mainPanel.animatedRenderEnvironment.getStart());
            if (ProgramGlobals.getCurrentModelPanel() != null) {
                ProgramGlobals.getCurrentModelPanel().getEditorRenderModel().updateNodes(false);
                ProgramGlobals.getCurrentModelPanel().repaintSelfAndRelatedChildren();
            }
        };
        timeSliderPanel.addListener(timeSliderTimeListener);
        //		timeSliderPanel.addListener(creatorPanel);
        return timeSliderPanel;
    }

    public MainLayoutCreator setModelPanel(ModelPanel modelPanel) {
        if (modelPanel == null) {
            JPanel jPanel = new JPanel();
            jPanel.add(new JLabel("..."));
            viewportControllerWindowView.setComponent(jPanel);
            frontView.setComponent(new JPanel());
            bottomView.setComponent(new JPanel());
            leftView.setComponent(new JPanel());
            perspectiveView.setComponent(new JPanel());
            previewView.setComponent(new JPanel());
            animationControllerView.setComponent(new JPanel());
            timeSliderPanel.setModelHandler(null);
            creatorPanel.setModelEditorManager(null);
            creatorPanel.setCurrentModel(null);
            modelDataView.setComponent(new JPanel());
            modelComponentView.setComponent(new JPanel());
        } else {
            viewportControllerWindowView.setComponent(modelPanel.getModelEditingTreePane());
            frontView.setComponent(modelPanel.getFrontArea());
            bottomView.setComponent(modelPanel.getBotArea());
            leftView.setComponent(modelPanel.getSideArea());
            perspectiveView.setComponent(modelPanel.getPerspArea());
            previewView.setComponent(modelPanel.getAnimationViewer());
            animationControllerView.setComponent(modelPanel.getAnimationController());
            timeSliderPanel.setModelHandler(modelPanel.getModelHandler());
            creatorPanel.setModelEditorManager(modelPanel.getModelEditorManager());
            creatorPanel.setCurrentModel(modelPanel.getModelHandler());
            modelDataView.setComponent(modelPanel.getComponentBrowserTreePane());
            modelComponentView.setComponent(modelPanel.getComponentsPanel());

            modelPanel.reloadComponentBrowser();
            modelPanel.reloadModelEditingTree();
        }
        return this;
    }

    public MainLayoutCreator showModelPanel(ModelPanel modelPanel) {
        viewportControllerWindowView.setComponent(modelPanel.getModelEditingTreePane());
        viewportControllerWindowView.repaint();
        modelDataView.setComponent(modelPanel.getComponentBrowserTreePane());
        modelComponentView.setComponent(modelPanel.getComponentsPanel());
        modelDataView.repaint();
        return this;
    }

    public MainLayoutCreator setAnimationMode(boolean animationModeState) {
        timeSliderPanel.setDrawing(animationModeState);
        timeSliderPanel.setKeyframeModeActive(animationModeState);
        timeSliderPanel.repaint();
        creatorPanel.setAnimationModeState(animationModeState);
        return this;
    }

    public View getViewportControllerWindowView() {
        return viewportControllerWindowView;
    }

    public MainLayoutCreator setViewportControllerWindowView(View viewportControllerWindowView) {
        this.viewportControllerWindowView = viewportControllerWindowView;
        return this;
    }

    public View getToolView() {
        return toolView;
    }

    public MainLayoutCreator setToolView(View toolView) {
        this.toolView = toolView;
        return this;
    }

    public View getFrontView() {
        return frontView;
    }

    public MainLayoutCreator setFrontView(View frontView) {
        this.frontView = frontView;
        return this;
    }

    public View getLeftView() {
        return leftView;
    }

    public MainLayoutCreator setLeftView(View leftView) {
        this.leftView = leftView;
        return this;
    }

    public View getBottomView() {
        return bottomView;
    }

    public MainLayoutCreator setBottomView(View bottomView) {
        this.bottomView = bottomView;
        return this;
    }

    public View getPerspectiveView() {
        return perspectiveView;
    }

    public MainLayoutCreator setPerspectiveView(View perspectiveView) {
        this.perspectiveView = perspectiveView;
        return this;
    }

    public View getModelDataView() {
        return modelDataView;
    }

    public MainLayoutCreator setModelDataView(View modelDataView) {
        this.modelDataView = modelDataView;
        return this;
    }

    public View getModelComponentView() {
        return modelComponentView;
    }

    public MainLayoutCreator setModelComponentView(View modelComponentView) {
        this.modelComponentView = modelComponentView;
        return this;
    }

    public View getPreviewView() {
        return previewView;
    }

    public MainLayoutCreator setPreviewView(View previewView) {
        this.previewView = previewView;
        return this;
    }

    public View getAnimationControllerView() {
        return animationControllerView;
    }

    public MainLayoutCreator setAnimationControllerView(View animationControllerView) {
        this.animationControllerView = animationControllerView;
        return this;
    }

    public View getTimeSliderView() {
        return timeSliderView;
    }

    public MainLayoutCreator setTimeSliderView(View timeSliderView) {
        this.timeSliderView = timeSliderView;
        return this;
    }

    public View getCreatorView() {
        return creatorView;
    }

    public MainLayoutCreator setCreatorView(View creatorView) {
        this.creatorView = creatorView;
        return this;
    }

    public TimeSliderPanel getTimeSliderPanel() {
        return timeSliderPanel;
    }

    public CreatorModelingPanel getCreatorPanel() {
        return creatorPanel;
    }


////        TempBonePanel tempBonePanel = new TempBonePanel();
////        DockingWindow boneTab = new SplitWindow(true, 0.5f, tempBonePanel.getBoneView(), mainPanel.previewView);
////        boneTab.getWindowProperties().setTitleProvider(arg0 -> "Bones");
//
//        editingTab.setDebugGraphicsOptions(JComponent.WHEN_FOCUSED);
//        System.out.println("editingTab insets: " + editingTab.getInsets());
//        System.out.println("leftView insets: " + mainPanel.leftView.getInsets());
////        TabWindow startupTabWindow = new TabWindow(new DockingWindow[]{boneTab, viewingTab, editingTab, modelTab});
}
