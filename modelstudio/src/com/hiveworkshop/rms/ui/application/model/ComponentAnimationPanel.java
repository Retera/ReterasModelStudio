package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.actions.model.animation.*;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

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
		nameField.addActionListener(e -> nameField());

		newAnimTimeStart = new ComponentEditorJSpinner(new SpinnerNumberModel(300, 0, Integer.MAX_VALUE, 1));
		newAnimTimeStart.addActionListener(this::newAnimTimeStart);

		newAnimTimeEnd = new ComponentEditorJSpinner(new SpinnerNumberModel(1300, 0, Integer.MAX_VALUE, 1));
		newAnimTimeEnd.addActionListener(this::newAnimTimeEnd);

		setLayout(new MigLayout());
		add(new JLabel("Name: "), "cell 0 0");
		add(nameField, "cell 1 0");
		add(new JLabel("Start: "), "cell 0 1");
		add(newAnimTimeStart, "cell 1 1");
		add(new JLabel("End: "), "cell 2 1");
		add(newAnimTimeEnd, "cell 3 1");

		rarityChooser = new ComponentEditorJSpinner(new SpinnerNumberModel(0d, 0d, Long.MAX_VALUE, 1d));
		rarityChooser.addActionListener(this::rarityChooser);

		moveSpeedChooser = new ComponentEditorJSpinner(new SpinnerNumberModel(0d, 0d, Long.MAX_VALUE, 1d));
		moveSpeedChooser.addActionListener(this::moveSpeedChooser);

		nonLoopingChooser = new JCheckBox("NonLooping");
		nonLoopingChooser.addActionListener(e -> nonLoopingChooser());

		add(nonLoopingChooser, "cell 0 2");
		add(new JLabel("Rarity"), "cell 0 3");
		add(rarityChooser, "cell 1 3");
		add(new JLabel("MoveSpeed"), "cell 0 4");
		add(moveSpeedChooser, "cell 1 4");
	}

	private void nonLoopingChooser() {
		final SetAnimationNonLoopingAction setAnimationNonLoopingAction = new SetAnimationNonLoopingAction(
				animation.isNonLooping(), nonLoopingChooser.isSelected(), animation,
				modelStructureChangeListener);
		setAnimationNonLoopingAction.redo();
		undoListener.pushAction(setAnimationNonLoopingAction);
	}

	private void moveSpeedChooser() {
		final SetAnimationMoveSpeedAction setAnimationMoveSpeedAction = new SetAnimationMoveSpeedAction(
				animation.getMoveSpeed(), ((Number) moveSpeedChooser.getValue()).floatValue(), animation,
				modelStructureChangeListener);
		setAnimationMoveSpeedAction.redo();
		undoListener.pushAction(setAnimationMoveSpeedAction);
	}

	private void rarityChooser() {
		final SetAnimationRarityAction setAnimationRarityAction = new SetAnimationRarityAction(
				animation.getRarity(), ((Number) rarityChooser.getValue()).floatValue(), animation,
				modelStructureChangeListener);
		setAnimationRarityAction.redo();
		undoListener.pushAction(setAnimationRarityAction);
	}

	private void newAnimTimeEnd() {
		final SetAnimationIntervalEndAction setAnimationIntervalEndAction = new SetAnimationIntervalEndAction(
				animation.getEnd(), ((Number) newAnimTimeEnd.getValue()).intValue(), animation,
				modelStructureChangeListener);
		setAnimationIntervalEndAction.redo();
		undoListener.pushAction(setAnimationIntervalEndAction);
	}

	private void nameField() {
		final SetAnimationNameAction setAnimationNameAction = new SetAnimationNameAction(animation.getName(),
				nameField.getText(), animation, modelStructureChangeListener);
		setAnimationNameAction.redo();
		undoListener.pushAction(setAnimationNameAction);
	}

	private void newAnimTimeStart() {
		final SetAnimationIntervalStartAction setAnimationIntervalStartAction = new SetAnimationIntervalStartAction(
				animation.getStart(), ((Number) newAnimTimeStart.getValue()).intValue(), animation,
				modelStructureChangeListener);
		setAnimationIntervalStartAction.redo();
		undoListener.pushAction(setAnimationIntervalStartAction);
	}

	public Animation getAnimation() {
		final Animation newAnimation = new Animation(nameField.getText(),
				((Number) newAnimTimeStart.getValue()).intValue(), ((Number) newAnimTimeEnd.getValue()).intValue());
		final int rarityValue = ((Number) rarityChooser.getValue()).intValue();
		final int moveValue = ((Number) moveSpeedChooser.getValue()).intValue();
		if (rarityValue != 0) {
			newAnimation.setRarity(rarityValue);
		}
		if (moveValue != 0) {
			newAnimation.setMoveSpeed(moveValue);
		}
		if (nonLoopingChooser.isSelected()) {
			newAnimation.setNonLooping(true);
		}
		return newAnimation;
	}

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
		nonLoopingChooser.setSelected(animation.isNonLooping());
		rarityChooser.reloadNewValue(animation.getRarity());
		moveSpeedChooser.reloadNewValue(animation.getMoveSpeed());
	}
}
