//package com.hiveworkshop.rms.ui.application;
//
//import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.DisplayViewUgg;
//import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.PerspectiveViewUgg;
//import com.hiveworkshop.rms.ui.application.viewer.PreviewView;
//import com.hiveworkshop.rms.ui.gui.modeledit.creator.ModelingCreatorToolsView;
//import com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree.ModelComponentsView;
//import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.ModelViewManagingView;
//import net.infonode.docking.View;
//
//import javax.swing.*;
//
//public class MainLayoutCreator {
//    private ModelViewManagingView modelEditingTreeView;
//    private View toolView;
//    private DisplayViewUgg frontView;
//    private DisplayViewUgg leftView;
//    private DisplayViewUgg bottomView;
//    private PerspectiveViewUgg perspectiveView;
//    private PreviewView previewView;
////    private View animationControllerView;
//
//    private ModelComponentsView modelTab;
//
//    private final TimeSliderView timeSliderView;
//    private ModelingCreatorToolsView creatorView;
//
////    private CreatorModelingPanel creatorPanel;
//
//    public MainLayoutCreator(MainPanel mainPanel) {
//        timeSliderView = new TimeSliderView();
//
////        creatorView = new ModelingCreatorToolsView(mainPanel.viewportListener);
//
//        previewView = new PreviewView();
//
//
//        JPanel jPanel = new JPanel();
//        jPanel.add(new JLabel("..."));
//        modelEditingTreeView = new ModelViewManagingView();// GlobalIcons.geoIcon
////		viewportControllerWindowView.getWindowProperties().setCloseEnabled(false);
////		viewportControllerWindowView.getWindowProperties().setMaximizeEnabled(true);
////		viewportControllerWindowView.getWindowProperties().setMinimizeEnabled(true);
////		viewportControllerWindowView.getWindowProperties().setRestoreEnabled(true);
//        toolView = new View("Tools", null, new JPanel());
//
//        leftView = new DisplayViewUgg("Side");
//        frontView = new DisplayViewUgg("Front");
//        bottomView = new DisplayViewUgg("Bottom");
////        perspectiveView = new View("Perspective", null, new JPanel());
//        perspectiveView = new PerspectiveViewUgg();
//    }
//
////    public TabWindow getStartupTabWindow() {
////        SplitWindow editingTab = getEditTab();
////
////        SplitWindow viewingTab = getViewTab();
////
////        modelTab = new ModelComponentsView();
////        modelTab.getWindowProperties().setTitleProvider(arg0 -> "Model");
////
////        TabWindow startupTabWindow = new TabWindow(new DockingWindow[] {viewingTab, editingTab, modelTab});
////        WindowHandler.traverseAndFix(startupTabWindow);
////        return startupTabWindow;
////    }
//
////    private SplitWindow getViewTab() {
//////        ImageIcon imageIcon = new ImageIcon(MainFrame.MAIN_PROGRAM_ICON.getScaledInstance(16, 16, Image.SCALE_FAST));
////
//////        View mpqBrowserView = MPQBrowserView.createMPQBrowser(imageIcon);
////        View mpqBrowserView = new MPQBrowserView();
////        UnitBrowserView unitBrowserView = new UnitBrowserView();
////
////        DockingWindow[] dockingWindow = new DockingWindow[] {unitBrowserView, mpqBrowserView};
////
////        TabWindow tabWindow = new TabWindow(dockingWindow);
////
////        tabWindow.setSelectedTab(0);
////
//////        SplitWindow animPersp = new SplitWindow(true, 0.8f, previewView, animationControllerView);
//////        SplitWindow viewingTab = new SplitWindow(true, 0.8f, animPersp, tabWindow);
////        SplitWindow viewingTab = new SplitWindow(true, 0.8f, previewView, tabWindow);
////
////        viewingTab.getWindowProperties().setTitleProvider(arg0 -> "View");
////        viewingTab.getWindowProperties().setCloseEnabled(false);
////        viewingTab.getWindowProperties().setDragEnabled(false);
////        viewingTab.getSplitWindowProperties().setDividerLocationDragEnabled(false);
////        return viewingTab;
////    }
////
////    private SplitWindow getEditTab() {
////        TabWindow leftHandTabWindow = new TabWindow(new DockingWindow[] {getModelEditingTreeView(), toolView});
////        leftHandTabWindow.setSelectedTab(0);
////
////        SplitWindow frBt = new SplitWindow(true, frontView, bottomView);
////        SplitWindow lfPs = new SplitWindow(true, leftView, perspectiveView);
////        SplitWindow quadView = new SplitWindow(false, frBt, lfPs);
////
////        SplitWindow splitWindow = new SplitWindow(true, 0.2f, leftHandTabWindow, new SplitWindow(true, 0.8f, quadView, creatorView));
////
////        SplitWindow editingTab = new SplitWindow(false, 0.875f, splitWindow, timeSliderView);
////
////        editingTab.getWindowProperties().setCloseEnabled(false);
////        editingTab.getWindowProperties().setTitleProvider(arg0 -> "Edit");
////        return editingTab;
////    }
//
//
////    public MainLayoutCreator setModelPanel(ModelPanel modelPanel) {
////        modelEditingTreeView.setModelPanel(modelPanel);
////        frontView.setModelPanel(modelPanel);
////        bottomView.setModelPanel(modelPanel);
////        leftView.setModelPanel(modelPanel);
////        perspectiveView.setModelPanel(modelPanel);
////        previewView.setModelPanel(modelPanel);
////        timeSliderView.setModelPanel(modelPanel);
////        creatorView.setModelPanel(modelPanel);
////        modelTab.setModelPanel(modelPanel);
////
////        if (modelPanel != null) {
////            modelPanel.reloadComponentBrowser();
////            modelPanel.reloadModelEditingTree();
////        }
////        return this;
////    }
//
////    public MainLayoutCreator showModelPanel(ModelPanel modelPanel) {
//////        modelEditingTreeView.setComponent(modelPanel.getModelEditingTreePane());
////        modelEditingTreeView.setComponent(modelPanel.getModelEditingTreePane());
////        modelEditingTreeView.repaint();
////        modelTab.setModelPanel(modelPanel);
//////        modelDataView.setComponent(modelPanel.getComponentBrowserTreePane());
//////        modelComponentView.setComponent(modelPanel.getComponentsPanel());
//////        modelDataView.repaint();
////        return this;
////    }
//
////    public MainLayoutCreator setAnimationMode(boolean animationModeState) {
////        timeSliderView.setAnimationMode(animationModeState);
////        creatorView.getCreatorModelingPanel().setAnimationModeState(animationModeState);
////        return this;
////    }
//
////    public ModelViewManagingView getModelEditingTreeView() {
////        return modelEditingTreeView;
////    }
//
////    public View getToolView() {
////        return toolView;
////    }
//
////    public View getPerspectiveView() {
////        return perspectiveView;
////    }
//
////    public View getModelDataView() {
////        return modelTab;
////    }
//
////    public View getPreviewView() {
////        return previewView;
////    }
//
////    public View getAnimationControllerView() {
////        return animationControllerView;
////    }
////
////    public TimeSliderView getTimeSliderView() {
////        return timeSliderView;
////    }
////
////    public ModelingCreatorToolsView getCreatorView() {
////        return creatorView;
////    }
//
////    public TimeSliderPanel getTimeSliderPanel() {
////        return getTimeSliderView().getTimeSliderPanel();
////    }
//
////    public CreatorModelingPanel getCreatorPanel() {
////        return creatorView.getCreatorModelingPanel();
////    }
//
//
//////        TempBonePanel tempBonePanel = new TempBonePanel();
//////        DockingWindow boneTab = new SplitWindow(true, 0.5f, tempBonePanel.getBoneView(), mainPanel.previewView);
//////        boneTab.getWindowProperties().setTitleProvider(arg0 -> "Bones");
////
////        editingTab.setDebugGraphicsOptions(JComponent.WHEN_FOCUSED);
////        System.out.println("editingTab insets: " + editingTab.getInsets());
////        System.out.println("leftView insets: " + mainPanel.leftView.getInsets());
//////        TabWindow startupTabWindow = new TabWindow(new DockingWindow[]{boneTab, viewingTab, editingTab, modelTab});
//}
