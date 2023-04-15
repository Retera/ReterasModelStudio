package com.hiveworkshop.rms.util.uiFactories;

import javax.swing.*;
import java.awt.event.*;

public class Label {
	public static JLabel create(String text){
		return new JLabel(text);
	}
	public static JLabel create(String text, Icon icon){
		return new JLabel(text, icon, SwingConstants.LEFT);
	}
	public static JLabel create(Icon icon){
		return new JLabel(icon);
	}
	public static JLabel create(String text, String tooltip){
		JLabel jLabel = new JLabel(text);
		jLabel.setToolTipText(tooltip);
		return jLabel;
	}
	public static JLabel create(String text, Icon icon, String tooltip){
		JLabel jLabel = new JLabel(text, icon, SwingConstants.LEFT);
		jLabel.setToolTipText(tooltip);
		return jLabel;
	}
	public static JLabel create(Icon icon, String tooltip){
		JLabel jLabel = new JLabel(icon);
		jLabel.setToolTipText(tooltip);
		return jLabel;
	}

	public static JLabel create(String text, Runnable runnable){
		JLabel jLabel = new JLabel(text);
		if (runnable != null) {
			jLabel.setFocusable(true);
			jLabel.addMouseListener(getMouseAdapter(runnable, jLabel));
			jLabel.addKeyListener(getKeyAdapter(runnable));
		}
		return jLabel;
	}
	public static JLabel create(String text, Icon icon, Runnable runnable){
		JLabel jLabel = new JLabel(text, icon, SwingConstants.LEFT);
		if (runnable != null) {
			jLabel.setFocusable(true);
			jLabel.addMouseListener(getMouseAdapter(runnable, jLabel));
			jLabel.addKeyListener(getKeyAdapter(runnable));
		}
		return jLabel;
	}
	public static JLabel create(Icon icon, Runnable runnable){
		JLabel jLabel = new JLabel(icon);
		if (runnable != null) {
			jLabel.setFocusable(true);
			jLabel.addMouseListener(getMouseAdapter(runnable, jLabel));
			jLabel.addKeyListener(getKeyAdapter(runnable));
		}
		return jLabel;
	}
	public static JLabel create(String text, String tooltip, Runnable runnable){
		JLabel jLabel = new JLabel(text);
		jLabel.setToolTipText(tooltip);
		if (runnable != null) {
			jLabel.setFocusable(true);
			jLabel.addMouseListener(getMouseAdapter(runnable, jLabel));
			jLabel.addKeyListener(getKeyAdapter(runnable));
		}
		return jLabel;
	}

	public static JLabel create(String text, Icon icon, String tooltip, Runnable runnable){
		JLabel jLabel = new JLabel(text, icon, SwingConstants.LEFT);
		jLabel.setToolTipText(tooltip);
		if (runnable != null) {
			jLabel.setFocusable(true);
			jLabel.addMouseListener(getMouseAdapter(runnable, jLabel));
			jLabel.addKeyListener(getKeyAdapter(runnable));
		}
		return jLabel;
	}
	public static JLabel create(Icon icon, String tooltip, Runnable runnable){
		JLabel jLabel = new JLabel(icon);
		jLabel.setToolTipText(tooltip);
		if (runnable != null) {
			jLabel.setFocusable(true);
			jLabel.addMouseListener(getMouseAdapter(runnable, jLabel));
			jLabel.addKeyListener(getKeyAdapter(runnable));
		}
		return jLabel;
	}

	private static KeyAdapter getKeyAdapter(Runnable runnable) {
		return new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				if (KeyStroke.getKeyStrokeForEvent(e) == KeyStroke.getKeyStroke("released ENTER")
						|| KeyStroke.getKeyStrokeForEvent(e) == KeyStroke.getKeyStroke("released SPACE")) {
					runnable.run();
				}
			}
		};
	}

	private static MouseAdapter getMouseAdapter(Runnable runnable, JLabel label) {
		return new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				System.out.println("label.isFocusable(): " + label.isFocusable());
				if(label.isFocusable()) label.requestFocusInWindow();
				runnable.run();
			}
		};
	}

	private static class ActivateListener extends MouseAdapter implements KeyListener {
		Runnable runnable;
		ActivateListener(Runnable runnable){
			this.runnable = runnable;
		}
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			runnable.run();
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (KeyStroke.getKeyStrokeForEvent(e) == KeyStroke.getKeyStroke("released ENTER")
					|| KeyStroke.getKeyStrokeForEvent(e) == KeyStroke.getKeyStroke("released SPACE")) {
				runnable.run();
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {}
	}
}
