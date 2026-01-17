package com.matrixeater.src;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.hiveworkshop.wc3.gui.icons.RMSIcons;
import com.hiveworkshop.wc3.gui.modeledit.ModelViewManagingTree;
import com.matrixeater.localization.LocalizationManager;

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
			frame = new JFrame(LocalizationManager.getInstance().get("matrixeater.frame.viewcontroller"));
			frame.setContentPane(this);
			frame.setIconImage(RMSIcons.redIcon.getImage());
		}
		modelViewManagingTree = disp;
		geoScroll = new JScrollPane(modelViewManagingTree);
		addTab(LocalizationManager.getInstance().get("matrixeater.tab.geoscroll"), RMSIcons.geoIcon, geoScroll, "");
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
