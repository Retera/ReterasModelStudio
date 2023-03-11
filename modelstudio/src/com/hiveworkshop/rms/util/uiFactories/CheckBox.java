package com.hiveworkshop.rms.util.uiFactories;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class CheckBox {
	public static JCheckBox create(String text){
		return new JCheckBox(text);
	}
	public static JCheckBox create(String text, boolean enabled){
		return new JCheckBox(text, enabled);
	}
	public static JCheckBox create(String text, Icon icon){
		return new JCheckBox(text, icon);
	}
	public static JCheckBox create(String text, Icon icon, boolean enabled){
		return new JCheckBox(text, icon, enabled);
	}
	public static JCheckBox create(Icon icon){
		return new JCheckBox(icon);
	}
	public static JCheckBox create(Icon icon, boolean enabled){
		return new JCheckBox(icon, enabled);
	}
	public static JCheckBox create(String text, Consumer<Boolean> consumer){
		JCheckBox checkBox = new JCheckBox(text);
		if (consumer != null) checkBox.addActionListener(e -> consumer.accept(checkBox.isSelected()));
		return checkBox;
	}
	public static JCheckBox create(String text, boolean enabled, Consumer<Boolean> consumer){
		JCheckBox checkBox = new JCheckBox(text, enabled);
		if (consumer != null) checkBox.addActionListener(e -> consumer.accept(checkBox.isSelected()));
		return checkBox;
	}
	public static JCheckBox create(Icon icon, Consumer<Boolean> consumer){
		JCheckBox checkBox = new JCheckBox(icon);
		if (consumer != null) checkBox.addActionListener(e -> consumer.accept(checkBox.isSelected()));
		return checkBox;
	}
	public static JCheckBox create(Icon icon, boolean enabled, Consumer<Boolean> consumer){
		JCheckBox checkBox = new JCheckBox(icon, enabled);
		if (consumer != null) checkBox.addActionListener(e -> consumer.accept(checkBox.isSelected()));
		return checkBox;
	}
	public static JCheckBox create(String text, Icon icon, Consumer<Boolean> consumer){
		JCheckBox checkBox = new JCheckBox(text, icon);
		if (consumer != null) checkBox.addActionListener(e -> consumer.accept(checkBox.isSelected()));
		return checkBox;
	}
	public static JCheckBox create(String text, Icon icon, boolean enabled, Consumer<Boolean> consumer){
		JCheckBox checkBox = new JCheckBox(text, icon);
		if (consumer != null) checkBox.addActionListener(e -> consumer.accept(checkBox.isSelected()));
		checkBox.setEnabled(enabled);
		return checkBox;
	}

	public static JCheckBox createAL(String text, ActionListener actionListener){
		JCheckBox checkBox = new JCheckBox(text);
		checkBox.addActionListener(actionListener);
		return checkBox;
	}
	public static JCheckBox createAL(String text, boolean enabled, ActionListener actionListener){
		JCheckBox checkBox = new JCheckBox(text, enabled);
		checkBox.addActionListener(actionListener);
		return checkBox;
	}
	public static JCheckBox createAL(Icon icon, ActionListener actionListener){
		JCheckBox checkBox = new JCheckBox(icon);
		checkBox.addActionListener(actionListener);
		return checkBox;
	}
	public static JCheckBox createAL(Icon icon, boolean enabled, ActionListener actionListener){
		JCheckBox checkBox = new JCheckBox(icon, enabled);
		checkBox.addActionListener(actionListener);
		return checkBox;
	}
	public static JCheckBox createAL(String text, Icon icon, ActionListener actionListener){
		JCheckBox checkBox = new JCheckBox(text, icon);
		checkBox.addActionListener(actionListener);
		return checkBox;
	}
	public static JCheckBox createAL(String text, Icon icon, boolean enabled, ActionListener actionListener){
		JCheckBox checkBox = new JCheckBox(text, icon);
		checkBox.addActionListener(actionListener);
		checkBox.setEnabled(enabled);
		return checkBox;
	}
}
