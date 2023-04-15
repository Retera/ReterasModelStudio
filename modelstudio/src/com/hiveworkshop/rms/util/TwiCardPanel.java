package com.hiveworkshop.rms.util;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * A card panel that keeps track of its own CardLayout
 */
public class TwiCardPanel extends JPanel {
	private final CardLayout cardLayout = new CardLayout();
	private final TwiComboBox<String> comboBox;

	public TwiCardPanel() {
		this("PrototypePrototype");
	}

	public TwiCardPanel(String comboBoxPrototype) {
		setLayout(cardLayout);
		comboBox = new TwiComboBox<>(comboBoxPrototype);
		comboBox.addOnSelectItemListener(this::showCard);
	}

	public void show(String key) {
		if (!Objects.equals(key, comboBox.getSelected())) {
			comboBox.setSelectedItem(key);
		}
		showCard(key);
	}
	private void showCard(String key) {
		cardLayout.show(this, key);
	}

	public CardLayout getCardLayout() {
		return cardLayout;
	}

	@Override
	public void add(Component comp, Object constraints) {
		super.add(comp, constraints);
		comboBox.add(constraints.toString());
	}

	@Override
	public void add(Component comp, Object constraints, int index) {
		super.add(comp, constraints, index);
		comboBox.add(constraints.toString(), index);
	}

	@Override
	public void remove(int index) {
		super.remove(index);
		comboBox.remove(index);
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
		comboBox.removeAll();
	}

	public JComboBox<String> getComboBox() {
		return comboBox;
	}
}
