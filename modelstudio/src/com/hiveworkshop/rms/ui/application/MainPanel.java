package com.hiveworkshop.rms.ui.application;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class MainPanel extends JPanel {

	public MainPanel(JToolBar toolBar, RootWindowUgg rootWindowUgg) {
	    super(new MigLayout("fill, ins 0, gap 0, novisualpadding, wrap 1", "[fill, grow]", "[][fill, grow]"));
	    add(toolBar);

	    ClosePopup.createContextMenuPopup();

		add(rootWindowUgg);
    }

    public void repaintSelfAndChildren() {
        repaint();
    }

}
