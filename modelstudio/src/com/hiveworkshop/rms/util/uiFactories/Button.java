package com.hiveworkshop.rms.util.uiFactories;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class Button {
	public static JButton create(String text){
		return new JButton(text);
	}
	public static JButton create(String text, Icon icon){
		return new JButton(text, icon);
	}
	public static JButton create(Icon icon){
		return new JButton(icon);
	}
	public static JButton create(String text, ActionListener actionListener){
		JButton button = new JButton(text);
		button.addActionListener(actionListener);
		return button;
	}
	public static JButton create(Icon icon, ActionListener actionListener){
		JButton button = new JButton(icon);
		button.addActionListener(actionListener);
		return button;
	}
	public static JButton create(String text, Icon icon, ActionListener actionListener){
		JButton button = new JButton(text, icon);
		button.addActionListener(actionListener);
		return button;
	}
	public static JButton create(String text, Icon icon, ActionListener actionListener, boolean enabled){
		JButton button = new JButton(text, icon);
		button.addActionListener(actionListener);
		button.setEnabled(enabled);
		return button;
	}
	public static JButton createDisabled(String text, Icon icon, ActionListener actionListener){
		JButton button = new JButton(text, icon);
		button.addActionListener(actionListener);
		button.setEnabled(false);
		return button;
	}
	public static JButton create(String text, ActionListener actionListener, Color bg){
		JButton button = new JButton(text);
		button.addActionListener(actionListener);
		button.setBackground(bg);
		return button;
	}
	public static JButton create(String text, ActionListener actionListener, Color bg, Color fg){
		JButton button = new JButton(text);
		button.addActionListener(actionListener);
		button.setBackground(bg);
		button.setForeground(fg);
		return button;
	}

	public static JButton create(Consumer<JButton> buttonConsumer, String text){
		JButton button = new JButton(text);
		button.addActionListener(e -> buttonConsumer.accept(button));
		return button;
	}
	public static JButton create(Consumer<JButton> buttonConsumer, Icon icon){
		JButton button = new JButton(icon);
		button.addActionListener(e -> buttonConsumer.accept(button));
		return button;
	}
	public static JButton create(Consumer<JButton> buttonConsumer, String text, Icon icon){
		JButton button = new JButton(text, icon);
		button.addActionListener(e -> buttonConsumer.accept(button));
		return button;
	}

	public static JButton setTooltip(JButton button, String text){
		button.setToolTipText(text);
		return button;
	}
	public static JButton forceSize(JButton button, int width, int height){
		return forceSize(button, new Dimension(width, height));
	}
	public static JButton forceSize(JButton button, Dimension dimension){
		button.setMaximumSize(dimension);
		button.setMinimumSize(dimension);
		button.setPreferredSize(dimension);
		return button;
	}
}
