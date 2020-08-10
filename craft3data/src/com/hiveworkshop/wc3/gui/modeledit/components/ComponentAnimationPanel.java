package com.hiveworkshop.wc3.gui.modeledit.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.animation.SetAnimationIntervalEndAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.animation.SetAnimationIntervalStartAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.animation.SetAnimationMoveSpeedAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.animation.SetAnimationNameAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.animation.SetAnimationNonLoopingAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.animation.SetAnimationRarityAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ComponentEditorJSpinner;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ComponentEditorTextField;
import com.hiveworkshop.wc3.mdl.Animation;

import net.miginfocom.swing.MigLayout;

public class ComponentAnimationPanel extends JPanel {
	private final ComponentEditorTextField nameField;
	private final ComponentEditorJSpinner newAnimTimeStart;
	private final ComponentEditorJSpinner newAnimTimeEnd;
	private final ComponentEditorJSpinner rarityChooser;
	private final ComponentEditorJSpinner moveSpeedChooser;
	private final JCheckBox nonLoopingChooser;
	private Animation animation;
	private UndoActionListener undoListener;
	private ModelStructureChangeListener modelStructureChangeListener;

	public ComponentAnimationPanel() {
		nameField = new ComponentEditorTextField(24);
		nameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final SetAnimationNameAction setAnimationNameAction = new SetAnimationNameAction(animation.getName(),
						nameField.getText(), animation, modelStructureChangeListener);
				setAnimationNameAction.redo();
				undoListener.pushAction(setAnimationNameAction);
			}
		});
		newAnimTimeStart = new ComponentEditorJSpinner(new SpinnerNumberModel(300, 0, Integer.MAX_VALUE, 1));
		newAnimTimeStart.addActionListener(new Runnable() {
			@Override
			public void run() {
				final SetAnimationIntervalStartAction setAnimationIntervalStartAction = new SetAnimationIntervalStartAction(
						animation.getIntervalStart(), ((Number) newAnimTimeStart.getValue()).intValue(), animation,
						modelStructureChangeListener);
				setAnimationIntervalStartAction.redo();
				undoListener.pushAction(setAnimationIntervalStartAction);
			}
		});
		newAnimTimeEnd = new ComponentEditorJSpinner(new SpinnerNumberModel(1300, 0, Integer.MAX_VALUE, 1));
		newAnimTimeEnd.addActionListener(new Runnable() {
			@Override
			public void run() {
				final SetAnimationIntervalEndAction setAnimationIntervalEndAction = new SetAnimationIntervalEndAction(
						animation.getIntervalEnd(), ((Number) newAnimTimeEnd.getValue()).intValue(), animation,
						modelStructureChangeListener);
				setAnimationIntervalEndAction.redo();
				undoListener.pushAction(setAnimationIntervalEndAction);
			}
		});

		setLayout(new MigLayout());
		add(new JLabel("Name: "), "cell 0 0");
		add(nameField, "cell 1 0");
		add(new JLabel("Start: "), "cell 0 1");
		add(newAnimTimeStart, "cell 1 1");
		add(new JLabel("End: "), "cell 2 1");
		add(newAnimTimeEnd, "cell 3 1");

		rarityChooser = new ComponentEditorJSpinner(new SpinnerNumberModel(0d, 0d, Long.MAX_VALUE, 1d));
		rarityChooser.addActionListener(new Runnable() {
			@Override
			public void run() {
				final SetAnimationRarityAction setAnimationRarityAction = new SetAnimationRarityAction(
						animation.getRarity(), ((Number) rarityChooser.getValue()).floatValue(), animation,
						modelStructureChangeListener);
				setAnimationRarityAction.redo();
				undoListener.pushAction(setAnimationRarityAction);
			}
		});
		moveSpeedChooser = new ComponentEditorJSpinner(new SpinnerNumberModel(0d, 0d, Long.MAX_VALUE, 1d));
		moveSpeedChooser.addActionListener(new Runnable() {
			@Override
			public void run() {
				final SetAnimationMoveSpeedAction setAnimationMoveSpeedAction = new SetAnimationMoveSpeedAction(
						animation.getMoveSpeed(), ((Number) moveSpeedChooser.getValue()).floatValue(), animation,
						modelStructureChangeListener);
				setAnimationMoveSpeedAction.redo();
				undoListener.pushAction(setAnimationMoveSpeedAction);
			}
		});
		nonLoopingChooser = new JCheckBox("NonLooping");
		nonLoopingChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final SetAnimationNonLoopingAction setAnimationNonLoopingAction = new SetAnimationNonLoopingAction(
						animation.isNonLooping(), nonLoopingChooser.isSelected(), animation,
						modelStructureChangeListener);
				setAnimationNonLoopingAction.redo();
				undoListener.pushAction(setAnimationNonLoopingAction);
			}
		});

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
	 * @param modelStructureChangeListener
	 * @param undoListener
	 */
	public void setAnimation(final Animation animation, final UndoActionListener undoListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.animation = animation;
		this.undoListener = undoListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		nameField.reloadNewValue(animation.getName());
		newAnimTimeStart.reloadNewValue(animation.getStart());
		newAnimTimeEnd.reloadNewValue(animation.getEnd());
		rarityChooser.reloadNewValue(0);
		moveSpeedChooser.reloadNewValue(0);
		nonLoopingChooser.setSelected(false);
		for (final String tag : animation.getTags()) {
			final String lowerCaseTag = tag.trim().toLowerCase();
			if (lowerCaseTag.startsWith("nonlooping")) {
				nonLoopingChooser.setSelected(true);
			} else if (lowerCaseTag.startsWith("rarity ")) {
				final String rarityString = lowerCaseTag.substring("rarity ".length());
				final double rarityValue = Double.parseDouble(rarityString);
				rarityChooser.reloadNewValue(rarityValue);
			} else if (lowerCaseTag.startsWith("movespeed ")) {
				final String movespeedValueString = lowerCaseTag.substring("movespeed ".length());
				final double movespeedValue = Double.parseDouble(movespeedValueString);
				moveSpeedChooser.reloadNewValue(movespeedValue);
			}
		}
	}
}
