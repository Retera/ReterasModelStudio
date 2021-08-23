package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.tools.ChangeAnimationLengthPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.ScreenInfo;

import javax.swing.*;

public class ScaleAnimationLength extends ActionFunction {
	public ScaleAnimationLength() {
		super(TextKey.SCALING_ANIM_LENGTHS, ScaleAnimationLength::showPanel);
	}

	public static void showPanel() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		JFrame frame = new JFrame("Animation Editor: " + modelPanel.getModel().getName());
		frame.setPreferredSize(ScreenInfo.getSmallWindow());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setIconImage(RMSIcons.animIcon.getImage());
		frame.setContentPane(new ChangeAnimationLengthPanel(modelPanel.getModelHandler(), frame));
		frame.pack();
		frame.setLocationRelativeTo(ProgramGlobals.getMainPanel());
		frame.setVisible(true);
	}
}
