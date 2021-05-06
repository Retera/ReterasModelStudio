package com.hiveworkshop.rms.ui.application;

import javax.swing.*;
import java.awt.event.ActionListener;

public class MenuCreationUtils {
    public static JMenu createMenu(String menuName, int keyEvent) {
        JMenu menu = new JMenu(menuName);
        menu.setMnemonic(keyEvent);
        return menu;
    }

    public static JMenu createMenu(String menuName, int keyEvent, String description) {
        JMenu menu = createMenu(menuName, keyEvent);
        menu.getAccessibleContext().setAccessibleDescription(description);
        return menu;
    }

    public static void createAndAddMenuItem(String itemText, JMenu menu, ActionListener actionListener) {
        createAndAddMenuItem(itemText, menu, -1, actionListener);
    }

    public static void createAndAddMenuItem(String itemText, JMenu menu, int keyEvent, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(itemText);
        menuItem.setMnemonic(keyEvent);
        menuItem.addActionListener(actionListener);
        menu.add(menuItem);
    }

    public static void createAndAddMenuItem(String itemText, JMenu menu, KeyStroke keyStroke, String actionCommand, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(itemText);
        menuItem.addActionListener(actionListener);
        menuItem.setActionCommand(actionCommand);
        menuItem.setAccelerator(keyStroke);
        menu.add(menuItem);
    }

    public static void createAndAddMenuItem(String itemText, JMenu menu, int keyEvent, KeyStroke keyStroke, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(itemText);
        menuItem.setMnemonic(keyEvent);
        menuItem.setAccelerator(keyStroke);
        menuItem.addActionListener(actionListener);
        menu.add(menuItem);
    }


    public static JMenuItem createMenuItem(String itemText, ActionListener actionListener) {
        return createMenuItem(itemText, -1, actionListener);
    }

    public static JMenuItem createMenuItem(String itemText, int keyEvent, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(itemText);
        menuItem.setMnemonic(keyEvent);
        menuItem.addActionListener(actionListener);
        return menuItem;
    }

    public static JMenuItem createMenuItem(String itemText, KeyStroke keyStroke, String actionCommand, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(itemText);
        menuItem.addActionListener(actionListener);
        menuItem.setActionCommand(actionCommand);
        menuItem.setAccelerator(keyStroke);
        return menuItem;
    }

    public static JMenuItem createMenuItem(String itemText, int keyEvent, KeyStroke keyStroke, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(itemText);
        menuItem.setMnemonic(keyEvent);
        menuItem.setAccelerator(keyStroke);
        menuItem.addActionListener(actionListener);
        return menuItem;
    }

    private static void createAndAddMenuItem(String itemText, JMenu menu, KeyStroke keyStroke, ActionListener actionListener) {
        createAndAddMenuItem(itemText, menu, -1, keyStroke, actionListener);
    }
}
