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
	    UVPanel uvPanel = new UVPanel().setModel(ProgramGlobals.getCurrentModelPanel().getModelHandler());
	    uvPanel.initViewport();

	    ImageIcon UVIcon = new ImageIcon(IconUtils.worldEditStyleIcon(RMSIcons.loadTabImage("UVMap.png")));
	    String s = "Texture Coordinate Editor: " + ProgramGlobals.getCurrentModelPanel().getModel().getName();
	    View view = new View(s, UVIcon, uvPanel.getMenuHolderPanel());
	    FloatingWindow floatingWindow = getWindow(uvPanel, view);

	    uvPanel.init();
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
}
