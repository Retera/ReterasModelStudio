package com.hiveworkshop.rms.ui.application;

import net.infonode.docking.FloatingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

class OpenViewAction extends AbstractAction {
    private final OpenViewGetter openViewGetter;
    private final RootWindow rootWindow;

    OpenViewAction(RootWindow rootWindow, final String name, final OpenViewGetter openViewGetter) {
        super(name);
        this.openViewGetter = openViewGetter;
        this.rootWindow = rootWindow;
    }

    static OpenViewAction getOpenViewAction(RootWindow rootWindow, String s, View view) {
        return new OpenViewAction(rootWindow, s, () -> view);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final View view = openViewGetter.getView();
        if ((view.getTopLevelAncestor() == null) || !view.getTopLevelAncestor().isVisible()) {
            final FloatingWindow createFloatingWindow
                    = rootWindow.createFloatingWindow(rootWindow.getLocation(), new Dimension(640, 480), view);
            createFloatingWindow.getTopLevelAncestor().setVisible(true);
        }
    }

    interface OpenViewGetter {
        View getView();
    }
}
