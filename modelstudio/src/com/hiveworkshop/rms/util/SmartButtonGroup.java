package com.hiveworkshop.rms.util;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * A Buttongroup with button creation functionality and a panel creator
 *
 * To get a grid of i columns use removeButtonConst("wrap").addPanelConst("wrap " + i)
 * To get a row use removeButtonConst("wrap")
 */
public class SmartButtonGroup extends ButtonGroup {
	private final BiMap<Integer, AbstractButton> buttonIndexMap = new BiMap<>();
	private final BiMap<String, AbstractButton> buttonNameMap = new BiMap<>();
	private String title = "";
	private String panelConstraints = "ins 0,";
	private String buttonConstraints = "wrap,";


	public SmartButtonGroup() {
	}

	/**
	 * If a title is provided getButtonPanel will be wrapped in a TitledBorder
	 *
	 * @param title title of this button group
	 */
	public SmartButtonGroup(String title) {
		this.title = title;
	}

	public int getSelectedIndex() {
		Integer selectedIndex = buttonIndexMap.getByValue(getSelection());
		return selectedIndex == null ? -1 : selectedIndex;
	}

	public SmartButtonGroup setSelectedIndex(int index) {
		setSelected(buttonIndexMap.get(index).getModel(), true);
		return this;
	}

	public SmartButtonGroup setSelectedName(String name) {
		setSelected(buttonNameMap.get(name).getModel(), true);
		return this;
	}

	public SmartButtonGroup addButton(AbstractButton button) {
		add(button);
		buttonIndexMap.put(buttonIndexMap.size(), button);
		buttonNameMap.put(button.getName(), button);
		return this;
	}

	public SmartButtonGroup removeButton(AbstractButton button) {
		remove(button);
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

	public SmartButtonGroup addJButton(String text, ActionListener actionListener) {
		return addJButton(text, actionListener, buttonIndexMap.size());
	}

	private SmartButtonGroup addJButton(String text, ActionListener actionListener, int index) {
		JButton button = new JButton(text);
		addNewButton(actionListener, index, button);
		return this;
	}

	public SmartButtonGroup addJCheckBox(String text, ActionListener actionListener) {
		return addJCheckBox(text, actionListener, buttonIndexMap.size());
	}

	private SmartButtonGroup addJCheckBox(String text, ActionListener actionListener, int index) {
		JCheckBox button = new JCheckBox(text);
		addNewButton(actionListener, index, button);
		return this;
	}

	public SmartButtonGroup addJRadioButton(String text, ActionListener actionListener) {
		return addJRadioButton(text, actionListener, buttonIndexMap.size());
	}

	private SmartButtonGroup addJRadioButton(String text, ActionListener actionListener, int index) {
		JRadioButton button = new JRadioButton(text);
		addNewButton(actionListener, index, button);
		return this;
	}

	private void addNewButton(ActionListener actionListener, int index, AbstractButton button) {
		button.addActionListener(actionListener);
		add(button);
		buttonIndexMap.put(index, button);
		buttonNameMap.put(button.getText(), button);
	}

	public JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel();
		if (title.length() != 0) {
			buttonPanel.setBorder(BorderFactory.createTitledBorder(title));
			panelConstraints = panelConstraints.replaceFirst("ins 0,", "");
		}
		buttonPanel.setLayout(new MigLayout(buttonConstraints));
		int size = buttonIndexMap.size();
		int maxSearch = buttonIndexMap.size() * 3;
		for (int i = 0; i < size && i < maxSearch; i++) {
			AbstractButton button = buttonIndexMap.get(i);
			if (button != null) {
				buttonPanel.add(button, buttonConstraints);
			} else {
				size++;
			}
		}
		return buttonPanel;
	}

	public SmartButtonGroup setPanelConst(String constraints) {
		panelConstraints = constraints;
		return this;
	}

	public SmartButtonGroup addPanelConst(String... constraints) {
		StringBuilder sb = new StringBuilder();
		for (String s : constraints) {
			sb.append(s).append(",");
		}
		panelConstraints += sb;
		return this;
	}

	public SmartButtonGroup removePanelConst(String... constraints) {
		for (String s : constraints) {
			panelConstraints = panelConstraints.replaceFirst(s + ",", "");
		}
		return this;
	}

	public SmartButtonGroup setButtonConst(String constraints) {
		buttonConstraints = constraints;
		return this;
	}

	public SmartButtonGroup addButtonConst(String... constraints) {
		StringBuilder sb = new StringBuilder();
		for (String s : constraints) {
			sb.append(s).append(",");
		}
		buttonConstraints += sb;
		return this;
	}

	public SmartButtonGroup removeButtonConst(String... constraints) {
		for (String s : constraints) {
			buttonConstraints = buttonConstraints.replaceFirst(s + ",", "");
		}
		return this;
	}
}
