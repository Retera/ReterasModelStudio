package com.matrixeater.src;

import javax.swing.JFrame;

import com.hiveworkshop.wc3.gui.GlobalIcons;
import com.hiveworkshop.wc3.gui.modeledit.ModelPanel;

public class AnimationFrame extends JFrame {
	public AnimationFrame(final ModelPanel mdlDisp) {
		super("Animation Editor: " + mdlDisp.getModel().getName());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImage(GlobalIcons.animIcon.getImage());
		setContentPane(new AnimationPanel(mdlDisp.getModelViewManager(), this, mdlDisp.getUndoManager()));
		pack();
		setLocationRelativeTo(mdlDisp.getParent());
	}
}
