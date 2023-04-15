package com.hiveworkshop.rms.util;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * A Buttongroup with button creation functionality and a panel creator
 *
 * To get a grid of i columns use removeButtonConst("wrap").addPanelConst("wrap " + i)
 * To get a row use removeButtonConst("wrap")
 */
public class EnumButtonGroup<E extends Enum<E>> extends JPanel {
	private final EnumButtonGroupGroup<E> bg;
	private String title = "";
	private String panelConstraints = "ins 0,";
	private String buttonConstraints = "wrap,";
	private MigLayout layout;


	public EnumButtonGroup(Class<E> elementType, int type, Consumer<E> enumConsumer) {
		this(elementType, "", type, enumConsumer);
	}

	/**
	 * If a title is provided getButtonPanel will be wrapped in a TitledBorder
	 *
	 * @param title title of this button group
	 */
	public EnumButtonGroup(Class<E> elementType, String title, int type, Consumer<E> enumConsumer) {
//		elementType.cast()
		bg = new EnumButtonGroupGroup<>(elementType, type, enumConsumer);
		this.title = title;
		if (title.length() != 0) {
			setBorder(BorderFactory.createTitledBorder(title));
			panelConstraints = panelConstraints.replaceFirst("ins 0,", "");

		}
		layout = new MigLayout(panelConstraints);
		setLayout(layout);

		int size = bg.getNumElements();
		for (int i = 0; i < size; i++) {
			AbstractButton button = bg.getButton(i);
			if (button != null) {
				add(button, buttonConstraints);
			} else {
				size++;
			}
		}
	}

	public int getSelectedIndex() {
		return bg.getSelectedIndex();
	}

	public EnumButtonGroup<E> setSelectedIndex(int index) {
		bg.setSelectedIndex(index);
		return this;
	}

	public EnumButtonGroup<E> setSelectedName(String name) {
		bg.setSelectedName(name);
		return this;
	}

	public EnumButtonGroup<E> setSelected(E... eValues) {
		bg.setSelected(eValues);
		return this;
	}

	public EnumButtonGroup<E> setSelected(Collection<E> eValues) {
		bg.setSelected(eValues);
		return this;
	}

	public EnumButtonGroup<E> addButton(AbstractButton button) {
		bg.addButton(button);
		addUIButton(button);
		return this;
	}

	public EnumButtonGroup<E> removeButton(AbstractButton button) {
		bg.remove(button);
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
		return addUIButton(bg.addJRadioButton(text, null, stateChangeListener));
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
		return this;
	}

	public EnumButtonGroup<E> setPanelConst(String constraints) {
		panelConstraints = constraints;
		layout.setLayoutConstraints(panelConstraints);
		return this;
	}

	public EnumButtonGroup<E> addPanelConst(String... constraints) {
		StringBuilder sb = new StringBuilder();
		for (String s : constraints) {
			sb.append(s).append(",");
		}
		panelConstraints += sb;
		layout.setLayoutConstraints(panelConstraints);
		return this;
	}

	public EnumButtonGroup<E> removePanelConst(String... constraints) {
		for (String s : constraints) {
			panelConstraints = panelConstraints.replaceFirst(s + ",", "");
		}
		updateButtonConstraints();
		return this;
	}

	public EnumButtonGroup<E> setButtonConst(String constraints) {
		buttonConstraints = constraints;
		updateButtonConstraints();
		return this;
	}

	public EnumButtonGroup<E> addButtonConst(String... constraints) {
		StringBuilder sb = new StringBuilder();
		for (String s : constraints) {
			sb.append(s).append(",");
		}
		buttonConstraints += sb;
		updateButtonConstraints();
		return this;
	}

	public EnumButtonGroup<E> removeButtonConst(String... constraints) {
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
