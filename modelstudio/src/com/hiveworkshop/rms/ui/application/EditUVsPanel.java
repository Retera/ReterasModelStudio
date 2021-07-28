package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import net.infonode.docking.FloatingWindow;

import java.awt.*;

public class EditUVsPanel {
    public static void showEditUVs() {
        UVPanel panel = new UVPanel();
        panel.initViewport();

        FloatingWindow floatingWindow = getWindow(panel);

//        FloatingWindowFactory.openNewWindow("Edit UVs", panel, mainPanel.getRootWindow());
        panel.init();
        floatingWindow.getTopLevelAncestor().setVisible(true);
        panel.packFrame();
    }

    private static FloatingWindow getWindow(UVPanel panel) {
        MainPanel mainPanel = ProgramGlobals.getMainPanel();
        int wPos = mainPanel.getX() + mainPanel.getWidth() / 2;
        int hPos = mainPanel.getY() + mainPanel.getHeight() / 2;
        Point point = new Point(wPos, hPos);
        return mainPanel.rootWindow.createFloatingWindow(point, panel.getSize(), panel.getView());
    }


//    public static void showEditUVs() {
//        MainPanel mainPanel = ProgramGlobals.getMainPanel();
//        ModelPanel currentModelPanel = ProgramGlobals.getCurrentModelPanel();
//        UVPanel panel = currentModelPanel.getEditUVPanel();
//        if (panel == null) {
//            panel = new UVPanel();
//	        currentModelPanel.setEditUVPanel(panel);
//
//            panel.initViewport();
//
//            final FloatingWindow floatingWindow = getWindow(mainPanel, panel);
//
//            panel.init();
//            floatingWindow.getTopLevelAncestor().setVisible(true);
//            panel.packFrame();
//        } else if (!panel.frameVisible()) {
//
//            FloatingWindow floatingWindow = getWindow(mainPanel, panel);
//            floatingWindow.getTopLevelAncestor().setVisible(true);
//        }
//    }



//    private static FloatingWindow getWindow(MainPanel mainPanel, UVPanel panel) {
//        int wPos = mainPanel.getX() + mainPanel.getWidth() / 2;
//        int hPos = mainPanel.getY() + mainPanel.getHeight() / 2;
//        Point point = new Point(wPos, hPos);
//        return mainPanel.rootWindow.createFloatingWindow(point, panel.getSize(), panel.getView());
//    }
}
