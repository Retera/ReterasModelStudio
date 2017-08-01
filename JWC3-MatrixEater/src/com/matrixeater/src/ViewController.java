package com.matrixeater.src;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.hiveworkshop.wc3.gui.GlobalIcons;
import com.hiveworkshop.wc3.gui.modeledit.MDLDisplay;

/**
 * A view control, containing several control options
 *
 * Eric Theller 7/8/2012
 */
public class ViewController extends JTabbedPane {
	JFrame frame;
	GeosetController geoControl;
	ObjectController objControl;
	MDLDisplay dispModel;
	JScrollPane geoScroll;

	public ViewController(final MDLDisplay disp, final boolean spawnFrame) {
		super();
		dispModel = disp;
		if (spawnFrame) {
			frame = new JFrame("View Controller");
			frame.setContentPane(this);
			frame.setIconImage(GlobalIcons.redIcon.getImage());
		}
		geoControl = new GeosetController(disp, false);// !spawnFrame);
		objControl = new ObjectController(disp);
		geoScroll = new JScrollPane(geoControl);
		addTab("", GlobalIcons.geoIcon, geoScroll, "Controls visibility of geosets.");
		addTab("", GlobalIcons.boneIcon, objControl, "Controls visibility of bones.");
		if (spawnFrame) {
			frame.setVisible(true);
			frame.pack();
		}
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setMDLDisplay(final MDLDisplay disp) {
		geoControl.setMDLDisplay(disp);
		objControl.setMDLDisplay(disp);
		repaint();
	}
}
