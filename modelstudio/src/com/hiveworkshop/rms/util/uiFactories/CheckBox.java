package com.hiveworkshop.rms.util.uiFactories;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class CheckBox {
	public static JCheckBox create(String text){
		return new JCheckBox(text);
	}
	public static JCheckBox create(String text, boolean selected){
		return new JCheckBox(text, selected);
	}
	public static JCheckBox create(String text, Icon icon){
		return new JCheckBox(text, icon);
	}
	public static JCheckBox create(String text, Icon icon, boolean selected){
		return new JCheckBox(text, icon, selected);
	}
	public static JCheckBox create(Icon icon){
		return new JCheckBox(icon);
	}
	public static JCheckBox create(Icon icon, boolean selected){
		return new JCheckBox(icon, selected);
	}
	public static JCheckBox create(String text, Consumer<Boolean> consumer){
		JCheckBox checkBox = new JCheckBox(text);
		if (consumer != null) checkBox.addActionListener(e -> consumer.accept(checkBox.isSelected()));
		return checkBox;
	}
	public static JCheckBox create(String text, boolean selected, Consumer<Boolean> consumer){
		JCheckBox checkBox = new JCheckBox(text, selected);
		if (consumer != null) checkBox.addActionListener(e -> consumer.accept(checkBox.isSelected()));
		return checkBox;
	}
	public static JCheckBox create(Icon icon, Consumer<Boolean> consumer){
		JCheckBox checkBox = new JCheckBox(icon);
		if (consumer != null) checkBox.addActionListener(e -> consumer.accept(checkBox.isSelected()));
		return checkBox;
	}
	public static JCheckBox create(Icon icon, boolean selected, Consumer<Boolean> consumer){
		JCheckBox checkBox = new JCheckBox(icon, selected);
		if (consumer != null) checkBox.addActionListener(e -> consumer.accept(checkBox.isSelected()));
		return checkBox;
	}
	public static JCheckBox create(String text, Icon icon, Consumer<Boolean> consumer){
		JCheckBox checkBox = new JCheckBox(text, icon);
		if (consumer != null) checkBox.addActionListener(e -> consumer.accept(checkBox.isSelected()));
		return checkBox;
	}
	public static JCheckBox create(String text, Icon icon, boolean selected, Consumer<Boolean> consumer){
		JCheckBox checkBox = new JCheckBox(text, icon, selected);
		if (consumer != null) checkBox.addActionListener(e -> consumer.accept(checkBox.isSelected()));
		return checkBox;
	}
	public static JCheckBox create(String text, Icon icon, boolean selected, Consumer<Boolean> consumer, String tooltip){
		JCheckBox checkBox = new JCheckBox(text, icon, selected);
		checkBox.setToolTipText(tooltip);
		if (consumer != null) checkBox.addActionListener(e -> consumer.accept(checkBox.isSelected()));
		return checkBox;
	}
	public static JCheckBox create(String text, Icon icon, boolean selected, Consumer<Boolean> consumer, boolean enabled){
		JCheckBox checkBox = new JCheckBox(text, icon, selected);
		if (consumer != null) checkBox.addActionListener(e -> consumer.accept(checkBox.isSelected()));
		checkBox.setEnabled(enabled);
		return checkBox;
	}

	public static JCheckBox createAL(String text, ActionListener actionListener){
		JCheckBox checkBox = new JCheckBox(text);
		checkBox.addActionListener(actionListener);
		return checkBox;
	}
	public static JCheckBox createAL(String text, boolean selected, ActionListener actionListener){
		JCheckBox checkBox = new JCheckBox(text, selected);
		checkBox.addActionListener(actionListener);
		return checkBox;
	}
	public static JCheckBox createAL(Icon icon, ActionListener actionListener){
		JCheckBox checkBox = new JCheckBox(icon);
		checkBox.addActionListener(actionListener);
		return checkBox;
	}
	public static JCheckBox createAL(Icon icon, boolean selected, ActionListener actionListener){
		JCheckBox checkBox = new JCheckBox(icon, selected);
		checkBox.addActionListener(actionListener);
		return checkBox;
	}
	public static JCheckBox createAL(String text, Icon icon, ActionListener actionListener){
		JCheckBox checkBox = new JCheckBox(text, icon);
		checkBox.addActionListener(actionListener);
		return checkBox;
	}
	public static JCheckBox createAL(String text, Icon icon, boolean selected, ActionListener actionListener){
		JCheckBox checkBox = new JCheckBox(text, icon, selected);
		checkBox.addActionListener(actionListener);
		return checkBox;
	}
	public static JCheckBox setTooltip(JCheckBox checkBox, String text){
		checkBox.setToolTipText(text);
		return checkBox;
	}
}
