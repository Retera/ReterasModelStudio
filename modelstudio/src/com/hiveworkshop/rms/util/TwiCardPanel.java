package com.hiveworkshop.rms.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

/**
 * A card panel that keeps track of its own CardLayout
 */
public class TwiCardPanel extends JPanel {
	CardLayout cardLayout = new CardLayout();
	JComboBox<String> comboBox;
	TwiComboBoxModel<String> comboBoxModel;

	public TwiCardPanel() {
		setLayout(cardLayout);
		comboBoxModel = new TwiComboBoxModel<>();
		comboBox = new JComboBox<>(comboBoxModel);
		comboBox.addItemListener(this::chooseCard);
	}

	private void chooseCard(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			show(e.getItem().toString());
		}

	}

	public void show(String key) {
		cardLayout.show(this, key);
	}

	public CardLayout getCardLayout() {
		return cardLayout;
	}

	@Override
	public void add(Component comp, Object constraints) {
		super.add(comp, constraints);
		comboBoxModel.addElement(constraints.toString());
	}

	@Override
	public void add(Component comp, Object constraints, int index) {
		super.add(comp, constraints, index);
		comboBoxModel.insertElementAt(constraints.toString(), index);
	}

	@Override
	public void remove(int index) {
		super.remove(index);
		comboBoxModel.removeElementAt(index);
	}

	@Override
	public void remove(Component comp) {
		for (int i = 0; i < this.getComponentCount(); i++) {
			if (getComponent(i).equals(comp)) {
				remove(i);
			}
		}
	}

	@Override
	public void removeAll() {
		super.removeAll();
		comboBoxModel.removeAllElements();
	}

	public JComboBox<String> getCombobox() {
		return comboBox;
	}
}
