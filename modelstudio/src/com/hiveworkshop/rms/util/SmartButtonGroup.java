package com.hiveworkshop.rms.util;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * A Buttongroup with button creation functionality and a panel creator
 *
 * To get a grid of i columns use removeButtonConst("wrap").addPanelConst("wrap " + i)
 * To get a row use removeButtonConst("wrap")
 */
public class SmartButtonGroup extends JPanel {
	SmartButtonGroupGroup bg = new SmartButtonGroupGroup();
	private String title = "";
	private String panelConstraints = "ins 0,";
	private String buttonConstraints = "wrap,";
	MigLayout layout = new MigLayout(panelConstraints);


	public SmartButtonGroup() {
		this("");
	}

	/**
	 * If a title is provided getButtonPanel will be wrapped in a TitledBorder
	 *
	 * @param title title of this button group
	 */
	public SmartButtonGroup(String title) {
		this.title = title;
		if (title.length() != 0) {
			setBorder(BorderFactory.createTitledBorder(title));
			panelConstraints = panelConstraints.replaceFirst("ins 0,", "");

		}
		layout = new MigLayout(panelConstraints);
		setLayout(layout);


//		int size = buttonIndexMap.size();
//		int maxSearch = buttonIndexMap.size() * 3;
//		for (int i = 0; i < size && i < maxSearch; i++) {
//			AbstractButton button = buttonIndexMap.get(i);
//			if (button != null) {
//				buttonPanel.add(button, buttonConstraints);
//			} else {
//				size++;
//			}
//		}
	}

	public ButtonModel getSelection() {
		return bg.getSelection();
	}
	public void setSelected(ButtonModel m, boolean b) {
		bg.setSelected(m, b);
	}

	public int getSelectedIndex() {
		return bg.getSelectedIndex();
	}

	public SmartButtonGroup setSelectedIndex(int index) {
		bg.setSelectedIndex(index);
		return this;
	}

	public SmartButtonGroup setSelectedName(String name) {
		bg.setSelectedName(name);
		return this;
	}

	public SmartButtonGroup addButton(AbstractButton button) {
		bg.addButton(button);
		addUIButton(button);
		return this;
	}

	public SmartButtonGroup removeButton(AbstractButton button) {
		bg.removeButton(button);
		return this;
	}

	public AbstractButton getButton(int i) {
		return bg.getButton(i);
	}

	public AbstractButton getButton(String name) {
		return bg.getButton(name);
	}

	public JButton addJButton(String text, ActionListener actionListener) {
		return addUIButton(bg.addJButton(text, actionListener));
	}

	public JCheckBox addJCheckBox(String text, ActionListener actionListener) {
		return addUIButton(bg.addJCheckBox(text, actionListener));
	}

	public JRadioButton addJRadioButton(String text, ActionListener actionListener) {
		return addUIButton(bg.addJRadioButton(text, actionListener));
	}
	public JRadioButton addJRadioButton(String text, ActionListener actionListener, Consumer<Boolean> stateChangeListener) {
		return addUIButton(bg.addJRadioButton(text, actionListener, stateChangeListener));
	}
	public JRadioButton addJRadioToggleButton(String text, Consumer<Boolean> stateChangeListener) {
		return addUIButton(bg.addJRadioToggleButton(text, stateChangeListener));
	}

	public JRadioButton addJRadioButtonMenuItem(String text, ActionListener actionListener) {
		return addUIButton(bg.addJRadioButtonMenuItem(text, actionListener));
	}

	private <Q extends AbstractButton> Q addUIButton(Q button) {
		if (button != null) {
			add(button, buttonConstraints);
		}
		return button;
	}

	public JPanel getButtonPanel() {
//		JPanel buttonPanel = new JPanel(new MigLayout(panelConstraints));
//		if (title.length() != 0) {
//			buttonPanel.setBorder(BorderFactory.createTitledBorder(title));
//			panelConstraints = panelConstraints.replaceFirst("ins 0,", "");
//			buttonPanel.setLayout(new MigLayout(panelConstraints));
//		}
////		int size = buttonIndexMap.size();
////		int maxSearch = buttonIndexMap.size() * 3;
////		for (int i = 0; i < size && i < maxSearch; i++) {
////			AbstractButton button = buttonIndexMap.get(i);
////			if (button != null) {
////				buttonPanel.add(button, buttonConstraints);
////			} else {
////				size++;
////			}
////		}
//		return buttonPanel;
		return this;
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
		layout.setLayoutConstraints(panelConstraints);
		return this;
	}

	public SmartButtonGroup removePanelConst(String... constraints) {
		for (String s : constraints) {
			panelConstraints = panelConstraints.replaceFirst(s + ",", "");
		}
		layout.setLayoutConstraints(panelConstraints);
		return this;
	}

	public SmartButtonGroup setButtonConst(String constraints) {
		buttonConstraints = constraints;
		updateButtonConstraints();
		return this;
	}

	public SmartButtonGroup addButtonConst(String... constraints) {
		StringBuilder sb = new StringBuilder();
		for (String s : constraints) {
			sb.append(s).append(",");
		}
		buttonConstraints += sb;
		updateButtonConstraints();
		return this;
	}

	public SmartButtonGroup removeButtonConst(String... constraints) {
		for (String s : constraints) {
			buttonConstraints = buttonConstraints.replaceFirst(s + ",", "");
		}
		updateButtonConstraints();
		return this;
	}

	private void updateButtonConstraints() {
		for(Component comp : getComponents()){
			layout.setComponentConstraints(comp, buttonConstraints);
		}
	}
}
