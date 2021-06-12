package com.hiveworkshop.rms.util;

import javax.swing.*;

public class FramePopup {
	public static JFrame show(JComponent jComponent, JComponent parent, String title) {
		final JFrame frame = new JFrame(title);
		frame.setContentPane(jComponent);
		frame.pack();
		frame.setLocationRelativeTo(parent);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		return frame;
	}

	public static JFrame get(JComponent jComponent, JComponent parent, String title) {
		final JFrame frame = new JFrame(title);
		frame.setContentPane(jComponent);
		frame.pack();
		frame.setLocationRelativeTo(parent);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		return frame;
	}
}
