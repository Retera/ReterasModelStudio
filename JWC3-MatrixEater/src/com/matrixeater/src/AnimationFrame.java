package com.matrixeater.src;

import javax.swing.JFrame;

import com.hiveworkshop.wc3.gui.GlobalIcons;
import com.hiveworkshop.wc3.gui.modeledit.MDLDisplay;

public class AnimationFrame extends JFrame {
	public AnimationFrame(MDLDisplay mdlDisp) {
		super("Animation Editor: " + mdlDisp.getMDL().getName());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImage(GlobalIcons.animIcon.getImage());
		setContentPane(new AnimationPanel(mdlDisp, this));
		pack();
		setLocationRelativeTo(mdlDisp.getModelPanel());
	}
}
