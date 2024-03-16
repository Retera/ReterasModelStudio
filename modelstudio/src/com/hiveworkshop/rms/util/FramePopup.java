package com.hiveworkshop.rms.util;

import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;

public class FramePopup {
	public static JFrame show(JComponent jComponent, JComponent parent, String title) {
		final JFrame frame = new JFrame(title);
		frame.setContentPane(jComponent);
		frame.pack();
		frame.setLocationRelativeTo(parent);
		frame.setIconImage(RMSIcons.MAIN_PROGRAM_ICON);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		return frame;
	}

	public static JFrame get(JComponent jComponent, JComponent parent, String title) {
		final JFrame frame = new JFrame(title);
		frame.setContentPane(jComponent);
		frame.setIconImage(RMSIcons.MAIN_PROGRAM_ICON);
		frame.pack();
		frame.setLocationRelativeTo(parent);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		return frame;
	}
	public static JFrame get(JComponent jComponent, JComponent parent, String title, boolean undecorated) {
		final JFrame frame = new JFrame(title);
		frame.setUndecorated(undecorated);
		frame.setContentPane(jComponent);
		frame.setIconImage(RMSIcons.MAIN_PROGRAM_ICON);
		frame.pack();
		frame.setLocationRelativeTo(parent);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		return frame;
	}
}
