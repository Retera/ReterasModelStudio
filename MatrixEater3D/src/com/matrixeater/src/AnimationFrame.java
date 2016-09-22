package com.matrixeater.src;

import javax.swing.JFrame;

public class AnimationFrame extends JFrame {
	public AnimationFrame(MDLDisplay mdlDisp) {
		super("Animation Editor: " + mdlDisp.getMDL().getName());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImage(ImportPanel.animIcon.getImage());
		setContentPane(new AnimationPanel(mdlDisp, this));
		pack();
		setLocationRelativeTo(mdlDisp.mpanel);
	}
}
