package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.scripts.ChangeAnimationLengthPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;

public class ScaleAnimationLength extends ActionFunction {
	public ScaleAnimationLength() {
		super(TextKey.SCALING_ANIM_LENGTHS, ScaleAnimationLength::showPanel);
	}

	public static void showPanel() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
//		super("Animation Editor: " + modelPanel.getModel().getName());
//		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//		setIconImage(RMSIcons.animIcon.getImage());
//		setContentPane(new JScrollPane(new ChangeAnimationLengthPanel(modelPanel.getModelHandler(), this)));
//		pack();
//		setLocationRelativeTo(modelPanel.getParent());
		JFrame frame = new JFrame("Animation Editor: " + modelPanel.getModel().getName());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setIconImage(RMSIcons.animIcon.getImage());
		frame.setContentPane(new JScrollPane(new ChangeAnimationLengthPanel(modelPanel.getModelHandler(), frame)));
		frame.pack();
		frame.setLocationRelativeTo(ProgramGlobals.getMainPanel());
	}
}
