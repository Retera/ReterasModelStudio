package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.preferences.KeyBindingPrefs;
import net.infonode.docking.FloatingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;

public class FloatingWindowFactory {
    public static void openNewWindow(View view, RootWindow rootWindow) {
        if ((view.getTopLevelAncestor() == null) || !view.getTopLevelAncestor().isVisible()) {
            FloatingWindow createFloatingWindow
                    = rootWindow.createFloatingWindow(rootWindow.getLocation(), new Dimension(640, 480), view);
            createFloatingWindow.getTopLevelAncestor().setVisible(true);
        }
    }
    public static void openNewWindowWithKB(View view, RootWindow rootWindow) {
        if ((view.getTopLevelAncestor() == null) || !view.getTopLevelAncestor().isVisible()) {
            FloatingWindow createFloatingWindow
                    = rootWindow.createFloatingWindow(rootWindow.getLocation(), new Dimension(640, 480), view);
            createFloatingWindow.getTopLevelAncestor().setVisible(true);

	        KeyBindingPrefs keyBindingPrefs = ProgramGlobals.getKeyBindingPrefs();
//            view.getRootPane().setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keyBindingPrefs.getInputMap());
            createFloatingWindow.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keyBindingPrefs.getInputMap());
            createFloatingWindow.setActionMap(keyBindingPrefs.getActionMap());
        }
    }

//    public static void openNewWindow(String title, Component component, RootWindow rootWindow) {
//        openNewWindow(title, component, null, rootWindow);
//    }
//
//    public static void openNewWindow(String title, Component component, ImageIcon icon, RootWindow rootWindow) {
//        View view = new View(title, icon, component);
//        if ((view.getTopLevelAncestor() == null) || !view.getTopLevelAncestor().isVisible()) {
//            FloatingWindow createFloatingWindow
//                    = rootWindow.createFloatingWindow(rootWindow.getLocation(), new Dimension(640, 480), view);
//            createFloatingWindow.getTopLevelAncestor().setVisible(true);
//        }
//    }


}
