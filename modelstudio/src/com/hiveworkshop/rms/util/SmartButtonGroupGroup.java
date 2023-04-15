package com.hiveworkshop.rms.util;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * A Buttongroup with button creation functionality
 */
public class SmartButtonGroupGroup extends ButtonGroup {
	private final HashMap<ButtonModel, Integer> modelIndexMap = new HashMap<>();
	private final BiMap<Integer, AbstractButton> buttonIndexMap = new BiMap<>();
	private final BiMap<String, AbstractButton> buttonNameMap = new BiMap<>();


	public SmartButtonGroupGroup() {
	}

	public int getSelectedIndex() {
		Integer selectedIndex = modelIndexMap.get(getSelection());
		return selectedIndex == null ? -1 : selectedIndex;
	}

	public SmartButtonGroupGroup setSelectedIndex(int index) {
		setSelected(buttonIndexMap.get(index).getModel(), true);
		return this;
	}

	public SmartButtonGroupGroup setSelectedName(String name) {
		setSelected(buttonNameMap.get(name).getModel(), true);
		return this;
	}

	public SmartButtonGroupGroup addButton(AbstractButton button) {
		addNewButton(null, buttonIndexMap.size(), button);
		return this;
	}

	public SmartButtonGroupGroup removeButton(AbstractButton button) {
		remove(button);
		modelIndexMap.remove(button.getModel());
		buttonIndexMap.removeByValue(button);
		buttonNameMap.removeByValue(button);
		return this;
	}

	public AbstractButton getButton(int i) {
		return buttonIndexMap.get(i);
	}

	public AbstractButton getButton(String name) {
		return buttonNameMap.get(name);
	}

	public JButton addJButton(String text, ActionListener actionListener) {
		return addJButton(text, actionListener, buttonIndexMap.size());
	}

	private JButton addJButton(String text, ActionListener actionListener, int index) {
		JButton button = new JButton(text);
		addNewButton(actionListener, index, button);
		return button;
	}

	public JCheckBox addJCheckBox(String text, ActionListener actionListener) {
		return addJCheckBox(text, actionListener, buttonIndexMap.size());
	}

	private JCheckBox addJCheckBox(String text, ActionListener actionListener, int index) {
		JCheckBox button = new JCheckBox(text);
		addNewButton(actionListener, index, button);
		return button;
	}

	public JRadioButton addJRadioButton(String text, ActionListener actionListener) {
		return addJRadioButton(text, actionListener, buttonIndexMap.size());
	}
	public JRadioButton addJRadioButton(String text, ActionListener actionListener, Consumer<Boolean> stateChangeListener) {
		JRadioButton jRadioButton = addJRadioButton(text, actionListener, buttonIndexMap.size());
		jRadioButton.addItemListener(e -> stateChangeListener.accept(e.getStateChange() == ItemEvent.SELECTED));
		return jRadioButton;
	}
	public JRadioButton addJRadioToggleButton(String text, Consumer<Boolean> stateChangeListener) {
		JRadioButton jRadioButton = addJRadioButton(text, null, buttonIndexMap.size());
		jRadioButton.addItemListener(e -> stateChangeListener.accept(e.getStateChange() == ItemEvent.SELECTED));
		return jRadioButton;
	}

	private JRadioButton addJRadioButton(String text, ActionListener actionListener, int index) {
		JRadioButton button = new JRadioButton(text);
		addNewButton(actionListener, index, button);
		return button;
	}

	public JRadioButton addJRadioButtonMenuItem(String text, ActionListener actionListener) {
		return addJRadioButton(text, actionListener, buttonIndexMap.size());
	}

	private SmartButtonGroupGroup addJRadioButtonMenuItem(String text, ActionListener actionListener, int index) {
		JRadioButtonMenuItem button = new JRadioButtonMenuItem(text);
		addNewButton(actionListener, index, button);
		return this;
	}

	private void addNewButton(ActionListener actionListener, int index, AbstractButton button) {
		button.addActionListener(actionListener);
		add(button);
		modelIndexMap.put(button.getModel(), index);
		buttonIndexMap.put(index, button);
		buttonNameMap.put(button.getText(), button);
	}
}
