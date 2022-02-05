package com.matrixeater.imp;

import com.hiveworkshop.wc3.gui.icons.RMSIcons;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ImportPanelSimple extends JPanel implements ActionListener {
	JButton animationTransfer = new JButton(RMSIcons.AnimIcon);// "Animation
																					// Transferer");
	JFrame frame;

	public ImportPanelSimple() {
		add(animationTransfer);
		animationTransfer.addActionListener(this);

		setPreferredSize(new Dimension(800, 600));
		frame = new JFrame("Simple Import Handler");
		frame.setContentPane(this);

		frame.pack();
		frame.setLocationRelativeTo(null);

		frame.setVisible(true);

		// animationTransfer.doClick();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == animationTransfer) {
			frame.setContentPane(new AnimationTransfer(frame));
			frame.setTitle("Animation Transferer");
			frame.setIconImage(RMSIcons.AnimIcon.getImage());
			frame.revalidate();
			frame.pack();
			frame.setLocationRelativeTo(null);
		}
	}
}
