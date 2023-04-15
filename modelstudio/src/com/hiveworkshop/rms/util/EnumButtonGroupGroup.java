package com.hiveworkshop.rms.util;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * A Buttongroup with button creation functionality and a panel creator
 *
 * To get a grid of i columns use removeButtonConst("wrap").addPanelConst("wrap " + i)
 * To get a row use removeButtonConst("wrap")
 */
public class EnumButtonGroupGroup<E extends Enum<E>> extends ButtonGroup {
	private final HashMap<ButtonModel, Integer> modelIndexMap = new HashMap<>();
	private final BiMap<Integer, AbstractButton> buttonIndexMap = new BiMap<>();
	private final BiMap<String, AbstractButton> buttonNameMap = new BiMap<>();
	public static int TYPE_RADIO = 0;
	public static int TYPE_CHECK = 1;
	public static int TYPE_BUTTON = 2;
	Class<E> elementType;
	Consumer<E> enumConsumer;
	E lastSelected;
	EnumSet<E> selectedValues;
	int type;

	public EnumButtonGroupGroup(Class<E> elementType, int type, Consumer<E> enumConsumer) {
		this.elementType = elementType;
		this.enumConsumer = enumConsumer;
		this.type = type;
		selectedValues = EnumSet.noneOf(elementType);
		E[] enumConstants = elementType.getEnumConstants();
		if(type == TYPE_CHECK){
			for (E eValue : enumConstants) {
				addJCheckBox(eValue.toString(), e -> onSelection(eValue));
			}
		} else if(type == TYPE_BUTTON){
			for (E eValue : enumConstants) {
				addJButton(eValue.toString(), e -> onSelection(eValue));
			}
		} else {
			for (E eValue : enumConstants) {
				addJRadioButton(eValue.toString(), e -> onSelection(eValue));
			}
		}
	}

	public int getNumElements(){
		return modelIndexMap.size();
	}

	private void onSelection(E eValue){
		consumeEnum(eValue);
		if (type == TYPE_RADIO) {
			selectedValues.remove(lastSelected);
		}
		lastSelected = eValue;
		if (!selectedValues.remove(eValue)){
			selectedValues.add(eValue);
		}
	}
	private void consumeEnum(E eValue){
		if(enumConsumer != null){
			enumConsumer.accept(eValue);
		}
	}

	public int getSelectedIndex() {
		Integer selectedIndex = modelIndexMap.get(getSelection());
		return selectedIndex == null ? -1 : selectedIndex;
	}

	public EnumButtonGroupGroup<E> setSelectedIndex(int index) {
		if(type == TYPE_CHECK){
			buttonIndexMap.get(index).setSelected(true);
		} else {
			setSelected(buttonIndexMap.get(index).getModel(), true);
		}
		return this;
	}

	public EnumButtonGroupGroup<E> setSelectedName(String name) {
		if(type == TYPE_CHECK){
			buttonNameMap.get(name).setSelected(true);
		} else {
			setSelected(buttonNameMap.get(name).getModel(), true);
		}
		return this;
	}

	public EnumButtonGroupGroup<E> setSelected(E... eValues) {
		for(E eValue : eValues){
			if(type == TYPE_CHECK){
				getButton(eValue.ordinal()).setSelected(true);
			} else if(type == TYPE_BUTTON){
				getButton(eValue.ordinal()).setSelected(true);
				setSelected(buttonNameMap.get(eValue.toString()).getModel(), true);
			} else {
				setSelected(buttonNameMap.get(eValue.toString()).getModel(), true);
			}
		}
		return this;
	}

	public EnumButtonGroupGroup<E> setSelected(Collection<E> eValues) {
		for(E eValue : eValues){
			if(type == TYPE_CHECK){
				getButton(eValue.ordinal()).setSelected(true);
			} else if(type == TYPE_BUTTON){
				getButton(eValue.ordinal()).setSelected(true);
				setSelected(buttonNameMap.get(eValue.toString()).getModel(), true);
			} else {
				setSelected(buttonNameMap.get(eValue.toString()).getModel(), true);
			}
		}
		return this;
	}

	public EnumButtonGroupGroup<E> addButton(AbstractButton button) {
		addNewButton(null, buttonIndexMap.size(), button);
		return this;
	}

	public EnumButtonGroupGroup<E> removeButton(AbstractButton button) {
		if(type != TYPE_CHECK){
			remove(button);
		}
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

	private EnumButtonGroupGroup<E> addJRadioButtonMenuItem(String text, ActionListener actionListener, int index) {
		JRadioButtonMenuItem button = new JRadioButtonMenuItem(text);
		addNewButton(actionListener, index, button);
		return this;
	}

	private void addNewButton(ActionListener actionListener, int index, AbstractButton button) {
		button.addActionListener(actionListener);
		if(type != TYPE_CHECK){
			add(button);
		}
		modelIndexMap.put(button.getModel(), index);
		buttonIndexMap.put(index, button);
		buttonNameMap.put(button.getText(), button);
	}

}
