package com.hiveworkshop.rms.ui.application;

import javax.swing.*;
import java.awt.event.ActionListener;

public class MenuCreationUtils {
    static JMenu createMenu(String menuName, int keyEvent) {
        JMenu menu = new JMenu(menuName);
        menu.setMnemonic(keyEvent);
        return menu;
    }

    static JMenu createMenu(String menuName, int keyEvent, String description) {
        JMenu menu = createMenu(menuName, keyEvent);
        menu.getAccessibleContext().setAccessibleDescription(description);
        return menu;
    }

    static void createAndAddMenuItem(String itemText, JMenu menu, ActionListener actionListener) {
        createAndAddMenuItem(itemText, menu, -1, actionListener);
    }

    static void createAndAddMenuItem(String itemText, JMenu menu, int keyEvent, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(itemText);
        menuItem.setMnemonic(keyEvent);
        menuItem.addActionListener(actionListener);
        menu.add(menuItem);
    }

    static void createAndAddMenuItem(String itemText, JMenu menu, KeyStroke keyStroke, String actionCommand, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(itemText);
        menuItem.addActionListener(actionListener);
        menuItem.setActionCommand(actionCommand);
        menuItem.setAccelerator(keyStroke);
        menu.add(menuItem);
    }

    private static void createAndAddMenuItem(String itemText, JMenu menu, KeyStroke keyStroke, ActionListener actionListener) {
        createAndAddMenuItem(itemText, menu, -1, keyStroke, actionListener);
    }

    static void createAndAddMenuItem(String itemText, JMenu menu, int keyEvent, KeyStroke keyStroke, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(itemText);
        menuItem.setMnemonic(keyEvent);
        menuItem.setAccelerator(keyStroke);
        menuItem.addActionListener(actionListener);
        menu.add(menuItem);
    }
}
