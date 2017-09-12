package com.matrixeater.src;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.hiveworkshop.wc3.gui.GlobalIcons;
import com.hiveworkshop.wc3.gui.modeledit.ModelViewManagingTree;

/**
 * A view control, containing several control options
 *
 * Eric Theller 7/8/2012
 */
public class ViewController extends JTabbedPane {
	JFrame frame;
	ModelViewManagingTree modelViewManagingTree;
	JScrollPane geoScroll;

	public ViewController(final ModelViewManagingTree disp, final boolean spawnFrame) {
		super();
		if (spawnFrame) {
			frame = new JFrame("View Controller");
			frame.setContentPane(this);
			frame.setIconImage(GlobalIcons.redIcon.getImage());
		}
		modelViewManagingTree = disp;
		geoScroll = new JScrollPane(modelViewManagingTree);
		addTab("", GlobalIcons.geoIcon, geoScroll, "Controls visibility");
		if (spawnFrame) {
			frame.setVisible(true);
			frame.pack();
		}
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setMDLDisplay(final ModelViewManagingTree disp) {
		geoScroll.setViewportView(modelViewManagingTree = disp);
		repaint();
	}
}
