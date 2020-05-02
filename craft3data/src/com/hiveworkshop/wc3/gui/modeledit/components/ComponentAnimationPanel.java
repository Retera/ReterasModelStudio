package com.hiveworkshop.wc3.gui.modeledit.components;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.mdl.Animation;

import net.miginfocom.swing.MigLayout;

public class ComponentAnimationPanel extends JPanel {
	private final JTextField nameField;
	private final JSpinner newAnimTimeStart;
	private final JSpinner newAnimTimeEnd;
	private final JSpinner rarityChooser;
	private final JSpinner moveSpeedChooser;
	private final JCheckBox nonLoopingChooser;

	public ComponentAnimationPanel() {
		nameField = new JTextField(24);
		newAnimTimeStart = new JSpinner(new SpinnerNumberModel(300, 0, Integer.MAX_VALUE, 1));
		newAnimTimeEnd = new JSpinner(new SpinnerNumberModel(1300, 0, Integer.MAX_VALUE, 1));

		setLayout(new MigLayout());
		add(new JLabel("Name: "), "cell 0 0");
		add(nameField, "cell 1 0");
		add(new JLabel("Start: "), "cell 0 1");
		add(newAnimTimeStart, "cell 1 1");
		add(new JLabel("End: "), "cell 2 1");
		add(newAnimTimeEnd, "cell 3 1");

		rarityChooser = new JSpinner(new SpinnerNumberModel(0d, 0d, Long.MAX_VALUE, 1d));
		moveSpeedChooser = new JSpinner(new SpinnerNumberModel(0d, 0d, Long.MAX_VALUE, 1d));
		nonLoopingChooser = new JCheckBox("NonLooping");
		add(nonLoopingChooser, "cell 0 2");
		add(new JLabel("Rarity"), "cell 0 3");
		add(rarityChooser, "cell 1 3");
		add(new JLabel("MoveSpeed"), "cell 0 4");
		add(moveSpeedChooser, "cell 1 4");
	}

	public Animation getAnimation() {
		final Animation newAnimation = new Animation(nameField.getText(),
				((Number) newAnimTimeStart.getValue()).intValue(), ((Number) newAnimTimeEnd.getValue()).intValue());
		final int rarityValue = ((Number) rarityChooser.getValue()).intValue();
		final int moveValue = ((Number) moveSpeedChooser.getValue()).intValue();
		if (rarityValue != 0) {
			newAnimation.addTag("Rarity " + rarityValue);
		}
		if (moveValue != 0) {
			newAnimation.addTag("MoveSpeed " + moveValue);
		}
		if (nonLoopingChooser.isSelected()) {
			newAnimation.addTag("NonLooping");
		}
		return newAnimation;
	}

	/**
	 * @param animation
	 */
	public void setAnimation(final Animation animation) {
		nameField.setText(animation.getName());
		newAnimTimeStart.setValue(animation.getStart());
		newAnimTimeEnd.setValue(animation.getEnd());
		rarityChooser.setValue(0);
		moveSpeedChooser.setValue(0);
		nonLoopingChooser.setSelected(false);
		for (final String tag : animation.getTags()) {
			final String lowerCaseTag = tag.trim().toLowerCase();
			if (lowerCaseTag.startsWith("nonlooping")) {
				nonLoopingChooser.setSelected(true);
			} else if (lowerCaseTag.startsWith("rarity ")) {
				final String rarityString = lowerCaseTag.substring("rarity ".length());
				final double rarityValue = Double.parseDouble(rarityString);
				rarityChooser.setValue(rarityValue);
			} else if (lowerCaseTag.startsWith("movespeed ")) {
				final String movespeedValueString = lowerCaseTag.substring("movespeed ".length());
				final double movespeedValue = Double.parseDouble(movespeedValueString);
				moveSpeedChooser.setValue(movespeedValue);
			}
		}
	}
}
