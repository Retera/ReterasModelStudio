package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.util.ScreenInfo;
import net.infonode.docking.FloatingWindow;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;

public class EditUVsPanel {
    public static void showEditUVs() {
        UVPanel panel = new UVPanel().setModel(ProgramGlobals.getCurrentModelPanel().getModelHandler());
        panel.initViewport();

        ImageIcon UVIcon = new ImageIcon(IconUtils.worldEditStyleIcon(RMSIcons.loadTabImage("UVMap.png")));
        String s = "Texture Coordinate Editor: " + ProgramGlobals.getCurrentModelPanel().getModel().getName();
        View view = new View(s, UVIcon, panel.getMenuHolderPanel());
        FloatingWindow floatingWindow = getWindow(panel, view);

//        FloatingWindowFactory.openNewWindow("Edit UVs", panel, mainPanel.getRootWindow());
        panel.init();
        floatingWindow.getTopLevelAncestor().setVisible(true);
        packFrame(view);
    }

    private static FloatingWindow getWindow(UVPanel panel, View view) {
        MainPanel mainPanel = ProgramGlobals.getMainPanel();
        int wPos = mainPanel.getX() + mainPanel.getWidth() / 2;
        int hPos = mainPanel.getY() + mainPanel.getHeight() / 2;
        Point point = new Point(wPos, hPos);
//        View view = panel.getView();
        Dimension size = ScreenInfo.getSmallWindow();
        return ProgramGlobals.getRootWindowUgg().createFloatingWindow(point, size, view);
    }

    public static void packFrame(View view) {
        JFrame frame = (JFrame) view.getTopLevelAncestor();
        if (frame != null) {
            frame.pack();
            frame.setLocationRelativeTo(ProgramGlobals.getMainPanel());
        }
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
