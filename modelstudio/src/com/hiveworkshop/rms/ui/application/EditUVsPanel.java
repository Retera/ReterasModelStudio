package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import net.infonode.docking.FloatingWindow;

import java.awt.*;

public class EditUVsPanel {
    static void showEditUVs(MainPanel mainPanel) {
        final ModelPanel currentModelPanel = mainPanel.currentModelPanel();
        UVPanel panel = currentModelPanel.getEditUVPanel();
        if (panel == null) {
            panel = new UVPanel(mainPanel, mainPanel.modelStructureChangeListener, mainPanel.prefs);
            currentModelPanel.setEditUVPanel(panel);

            panel.initViewport();

            final FloatingWindow floatingWindow = mainPanel.rootWindow.createFloatingWindow(
                    new Point(
                            mainPanel.getX() + (mainPanel.getWidth() / 2),
                            mainPanel.getY() + (mainPanel.getHeight() / 2)),
                    panel.getSize(),
                    panel.getView());

            panel.init();
            floatingWindow.getTopLevelAncestor().setVisible(true);
            panel.packFrame();
        } else if (!panel.frameVisible()) {
            final FloatingWindow floatingWindow = mainPanel.rootWindow.createFloatingWindow(
                    new Point(
                            mainPanel.getX() + (mainPanel.getWidth() / 2),
                            mainPanel.getY() + (mainPanel.getHeight() / 2)),
                    panel.getSize(),
                    panel.getView());
            floatingWindow.getTopLevelAncestor().setVisible(true);
        }
    }
}
