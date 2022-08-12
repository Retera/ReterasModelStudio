package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.CameraManager;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class VPButtonPanel extends JPanel {
	private final CameraManager cameraManager;

	public VPButtonPanel(CameraManager cameraManager){
		super(new MigLayout("gap 0, ins 0, fill", "[][][]", "[][][][][]"));
		this.cameraManager = cameraManager;

		JPanel arrowPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[][][]", "[][][]"));
		JButton plusZoom  = getButton(20, 20, "Plus.png",  e -> zoom(1/1.15));
		JButton minusZoom = getButton(20, 20, "Minus.png", e -> zoom(1.15));

		getArrowPanel(arrowPanel);


		add(plusZoom, "align center, wrap");
		add(minusZoom, "gapy 16, align center, wrap");
		add(arrowPanel, "gapy 16");
	}

	private void getArrowPanel(JPanel arrowPanel) {
		JButton up      = getButton(32, 16, "ArrowUp.png",      e -> pan(0, .2));
		JButton left    = getButton(16, 32, "ArrowLeft.png",    e -> pan(-.2, 0));
		JButton right   = getButton(16, 32, "ArrowRight.png",   e -> pan(.2, 0));
		JButton down    = getButton(32, 16, "ArrowDown.png",    e -> pan(0, -.2));
		// row\col 0  1  2
		//   0    [ ][^][ ]
		//   1    [<][ ][>]
		//   2    [ ][v][ ]
		arrowPanel.add(up,    "cell 1 0");
		arrowPanel.add(left,  "cell 0 1");
		arrowPanel.add(right, "cell 2 1");
		arrowPanel.add(down,  "cell 1 2");
	}


	private JButton getButton(int width, int height, String iconPath, ActionListener actionListener) {
		Dimension dim = new Dimension(width, height);
		JButton button = new JButton(new ImageIcon(RMSIcons.loadDeprecatedImage(iconPath)));
		button.setMaximumSize(dim);
		button.setMinimumSize(dim);
		button.setPreferredSize(dim);
		button.addActionListener(actionListener);
		return button;
	}


	public void zoom(double v) {
		cameraManager.zoom(v);
	}


	public void pan(double x, double y) {
		cameraManager.translate(x,y);
	}
}
