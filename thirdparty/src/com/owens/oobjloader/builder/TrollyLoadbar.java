package com.owens.oobjloader.builder;

import javax.swing.*;
import java.awt.*;

/**
 * This is not thread safe and not very well programmed... It's 5 am.
 *
 * @author Eric
 *
 */
public class TrollyLoadbar {
	private final JFrame frame;
	private final JPanel panel;
	private final JLabel text;
	private final JProgressBar bar;
	public TrollyLoadbar() {
		frame = new JFrame("Loading...");
		frame.setUndecorated(true);
		panel = new JPanel();
		text = new JLabel("Loading...");
		bar = new JProgressBar(0, 100);
		bar.setPreferredSize(new Dimension(270,20));
		panel.add(text);
		panel.add(bar);
		panel.setPreferredSize(new Dimension(300,50));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setContentPane(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
	}

	public void show() {
		frame.setVisible(true);
		frame.toFront();
		frame.repaint();
	}

	public void hide() {
		frame.setVisible(false);
	}

	public boolean isVisible() {
		return frame.isVisible();
	}

	public void setPercent(final float f) {
		int amt = (int)(f*100);
		if( amt > 100 ) {
			System.out.println(f);
			amt = 50;
		}
		bar.setValue(amt);
	}

	public void repaint() {
		frame.repaint();
	}

	public void setText(final String s) {
		text.setText(s);
	}
}
