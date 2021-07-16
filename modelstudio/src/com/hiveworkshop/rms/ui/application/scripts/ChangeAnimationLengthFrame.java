package com.hiveworkshop.rms.ui.application.scripts;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;

public class ChangeAnimationLengthFrame extends JFrame {
	public ChangeAnimationLengthFrame(ModelPanel modelPanel) {
		super("Animation Editor: " + modelPanel.getModel().getName());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImage(RMSIcons.animIcon.getImage());
		setContentPane(new JScrollPane(new ChangeAnimationLengthPanel(modelPanel.getModelHandler(), this)));
		pack();
		setLocationRelativeTo(modelPanel.getParent());
	}
}
