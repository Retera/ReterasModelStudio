package com.hiveworkshop.rms.ui.application.scripts;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;

public class ChangeAnimationLengthFrame extends JFrame {
	public ChangeAnimationLengthFrame(final ModelPanel mdlDisp, final Runnable onFinish) {
		super("Animation Editor: " + mdlDisp.getModel().getName());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImage(RMSIcons.animIcon.getImage());
		setContentPane(new JScrollPane(
				new ChangeAnimationLengthPanel(mdlDisp.getModelViewManager(), this, mdlDisp.getUndoManager(), onFinish)));
		pack();
		setLocationRelativeTo(mdlDisp.getParent());
	}
}
