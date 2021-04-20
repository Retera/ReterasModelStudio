package com.hiveworkshop.rms.ui.util;

import javax.swing.*;
import java.awt.*;

public final class InfoPopup {
    public static void show(Component parent, String text) {
        final JTextArea tpane = new JTextArea(text);
        tpane.setLineWrap(true);
        tpane.setWrapStyleWord(true);
        tpane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tpane.setEditable(false);
        tpane.setSize(400, 400);

        final JScrollPane jspane = new JScrollPane(tpane);
        jspane.setPreferredSize(new Dimension(440, 230));

        JOptionPane.showMessageDialog(parent, jspane);
    }

    private InfoPopup(){}
}
