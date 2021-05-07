package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import net.infonode.docking.FloatingWindow;

import java.awt.*;

public class EditUVsPanel {
    public static void showEditUVs(MainPanel mainPanel) {
        final ModelPanel currentModelPanel = ProgramGlobals.getCurrentModelPanel();
        UVPanel panel = currentModelPanel.getEditUVPanel();
        if (panel == null) {
            panel = new UVPanel(mainPanel, mainPanel.modelStructureChangeListener);
            currentModelPanel.setEditUVPanel(panel);

            panel.initViewport();

            final FloatingWindow floatingWindow = getWindow(mainPanel, panel);

            panel.init();
            floatingWindow.getTopLevelAncestor().setVisible(true);
            panel.packFrame();
        } else if (!panel.frameVisible()) {

            final FloatingWindow floatingWindow = getWindow(mainPanel, panel);
            floatingWindow.getTopLevelAncestor().setVisible(true);
        }
    }

    private static FloatingWindow getWindow(MainPanel mainPanel, UVPanel panel) {
        int wPos = mainPanel.getX() + mainPanel.getWidth() / 2;
        int hPos = mainPanel.getY() + mainPanel.getHeight() / 2;
        Point point = new Point(wPos, hPos);
        return mainPanel.rootWindow.createFloatingWindow(point, panel.getSize(), panel.getView());
    }
}
