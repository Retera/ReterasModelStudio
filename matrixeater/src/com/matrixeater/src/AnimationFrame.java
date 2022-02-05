package com.matrixeater.src;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import com.hiveworkshop.wc3.gui.icons.RMSIcons;
import com.hiveworkshop.wc3.gui.modeledit.ModelPanel;

public class AnimationFrame extends JFrame {
	public AnimationFrame(final ModelPanel mdlDisp, final Runnable onFinish) {
		super("Animation Editor: " + mdlDisp.getModel().getName());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImage(RMSIcons.animIcon.getImage());
		setContentPane(new JScrollPane(
				new AnimationPanel(mdlDisp.getModelViewManager(), this, mdlDisp.getUndoManager(), onFinish)));
		pack();
		setLocationRelativeTo(mdlDisp.getParent());
	}
}
