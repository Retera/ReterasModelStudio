package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.parsers.w3o.WTSFile;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorTree;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorTreeBrowser;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitTabTreeBrowserBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import de.wc3data.stream.BlizzardDataInputStream;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MainLayoutCreator {
    static TabWindow createMainLayout(MainPanel mainPanel) {

        final JPanel jPanel = new JPanel();
        jPanel.add(new JLabel("..."));
        mainPanel.viewportControllerWindowView = new View("Outliner", null, jPanel);// GlobalIcons.geoIcon
//		viewportControllerWindowView.getWindowProperties().setCloseEnabled(false);
//		viewportControllerWindowView.getWindowProperties().setMaximizeEnabled(true);
//		viewportControllerWindowView.getWindowProperties().setMinimizeEnabled(true);
//		viewportControllerWindowView.getWindowProperties().setRestoreEnabled(true);
        mainPanel.toolView = new View("Tools", null, new JPanel());

        mainPanel.leftView = new View("Side", null, new JPanel());
        mainPanel.frontView = new View("Front", null, new JPanel());
        mainPanel.bottomView = new View("Bottom", null, new JPanel());
        mainPanel.perspectiveView = new View("Perspective", null, new JPanel());


        final SplitWindow editingTab = getEditTab(mainPanel);

        final SplitWindow viewingTab = getViewTab(mainPanel);

        final SplitWindow modelTab = new SplitWindow(true, 0.2f, mainPanel.modelDataView, mainPanel.modelComponentView);
        modelTab.getWindowProperties().setTitleProvider(arg0 -> "Model");

        final TabWindow startupTabWindow = new TabWindow(new DockingWindow[] {viewingTab, editingTab, modelTab});
//        final TabWindow startupTabWindow = new TabWindow(new DockingWindow[] {editingTab, viewingTab, modelTab});
        traverseAndFix(startupTabWindow);
        return startupTabWindow;
    }

    private static SplitWindow getViewTab(MainPanel mainPanel) {
        final ImageIcon imageIcon;
        imageIcon = new ImageIcon(MainFrame.MAIN_PROGRAM_ICON.getScaledInstance(16, 16, Image.SCALE_FAST));

        final View mpqBrowserView = MPQBrowserView.createMPQBrowser(mainPanel, imageIcon);

        final UnitEditorTree unitEditorTree = createUnitEditorTree(mainPanel);
        View view = new View("Unit Browser", imageIcon, new JScrollPane(unitEditorTree));
        DockingWindow[] dockingWindow = new DockingWindow[] {view, mpqBrowserView};

        final TabWindow tabWindow = new TabWindow(dockingWindow);

        tabWindow.setSelectedTab(0);

        SplitWindow animPersp = new SplitWindow(true, 0.8f, mainPanel.previewView, mainPanel.animationControllerView);
        final SplitWindow viewingTab = new SplitWindow(true, 0.8f, animPersp, tabWindow);

        viewingTab.getWindowProperties().setTitleProvider(arg0 -> "View");
        viewingTab.getWindowProperties().setCloseEnabled(false);
        return viewingTab;
    }

    private static SplitWindow getEditTab(MainPanel mainPanel) {
        final TabWindow leftHandTabWindow = new TabWindow(new DockingWindow[] {mainPanel.viewportControllerWindowView, mainPanel.toolView});
        leftHandTabWindow.setSelectedTab(0);

        SplitWindow frBt = new SplitWindow(true, mainPanel.frontView, mainPanel.bottomView);
        SplitWindow lfPs = new SplitWindow(true, mainPanel.leftView, mainPanel.perspectiveView);
        SplitWindow quadView = new SplitWindow(false, frBt, lfPs);

        SplitWindow splitWindow = new SplitWindow(true, 0.2f, leftHandTabWindow, new SplitWindow(true, 0.8f, quadView, mainPanel.creatorView));

        final SplitWindow editingTab = new SplitWindow(false, 0.875f, splitWindow, mainPanel.timeSliderView);

        editingTab.getWindowProperties().setCloseEnabled(false);
        editingTab.getWindowProperties().setTitleProvider(arg0 -> "Edit");
        return editingTab;
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

    static UnitEditorTree createUnitEditorTree(MainPanel mainPanel) {
        return new UnitEditorTreeBrowser(getUnitData(), new UnitTabTreeBrowserBuilder(),
                getUnitEditorSettings(), MutableObjectData.WorldEditorDataType.UNITS, (mdxFilePath, b, c, icon) -> MPQBrowserView.loadStreamMdx(mainPanel, GameDataFileSystem.getDefault().getResourceAsStream(mdxFilePath), b, c, icon), mainPanel.prefs);
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
                editorData.load(
                        new BlizzardDataInputStream(gameDataFileSystem.getResourceAsStream("war3map.w3u")),
                        gameDataFileSystem.has("war3map.wts")
                                ? new WTSFile(gameDataFileSystem.getResourceAsStream("war3map.wts")) : null,
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


////        TempBonePanel tempBonePanel = new TempBonePanel();
////        final DockingWindow boneTab = new SplitWindow(true, 0.5f, tempBonePanel.getBoneView(), mainPanel.previewView);
////        boneTab.getWindowProperties().setTitleProvider(arg0 -> "Bones");
//
//        editingTab.setDebugGraphicsOptions(JComponent.WHEN_FOCUSED);
//        System.out.println("editingTab insets: " + editingTab.getInsets());
//        System.out.println("leftView insets: " + mainPanel.leftView.getInsets());
////        final TabWindow startupTabWindow = new TabWindow(new DockingWindow[]{boneTab, viewingTab, editingTab, modelTab});
}
