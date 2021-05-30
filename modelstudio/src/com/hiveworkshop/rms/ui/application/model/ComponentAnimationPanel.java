package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.model.animation.*;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class ComponentAnimationPanel extends ComponentPanel<Animation> {
	private final ComponentEditorTextField nameField;
	private final ComponentEditorJSpinner newAnimTimeStart;
	private final ComponentEditorJSpinner newAnimTimeEnd;
	private final ComponentEditorJSpinner rarityChooser;
	private final ComponentEditorJSpinner moveSpeedChooser;
	private final JCheckBox nonLoopingChooser;
	private final JButton deleteButton;
	private Animation animation;
	private final ModelHandler modelHandler;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public ComponentAnimationPanel(ModelHandler modelHandler,
	                               ModelStructureChangeListener modelStructureChangeListener) {
		this.modelHandler = modelHandler;
		this.modelStructureChangeListener = modelStructureChangeListener;
		nameField = new ComponentEditorTextField(24);
		nameField.addEditingStoppedListener(this::nameField);
//		nameField.addActionListener(e -> nameField());

		newAnimTimeStart = new ComponentEditorJSpinner(new SpinnerNumberModel(300, 0, Integer.MAX_VALUE, 1));
		newAnimTimeStart.addEditingStoppedListener(this::newAnimTimeStart);
//		newAnimTimeStart.addActionListener(this::newAnimTimeStart);

		newAnimTimeEnd = new ComponentEditorJSpinner(new SpinnerNumberModel(1300, 0, Integer.MAX_VALUE, 1));
		newAnimTimeEnd.addEditingStoppedListener(this::newAnimTimeEnd);
//		newAnimTimeEnd.addActionListener(this::newAnimTimeEnd);

		setLayout(new MigLayout());
		add(new JLabel("Name: "), "cell 0 0");
		add(nameField, "cell 1 0");
		add(new JLabel("Start: "), "cell 0 1");
		add(newAnimTimeStart, "cell 1 1");
		add(new JLabel("End: "), "cell 2 1");
		add(newAnimTimeEnd, "cell 3 1");

		rarityChooser = new ComponentEditorJSpinner(new SpinnerNumberModel(0d, 0d, Long.MAX_VALUE, 1d));
		rarityChooser.addEditingStoppedListener(this::rarityChooser);
//		rarityChooser.addActionListener(this::rarityChooser);

		moveSpeedChooser = new ComponentEditorJSpinner(new SpinnerNumberModel(0d, 0d, Long.MAX_VALUE, 1d));
		moveSpeedChooser.addEditingStoppedListener(this::moveSpeedChooser);
//		moveSpeedChooser.addActionListener(this::moveSpeedChooser);

		nonLoopingChooser = new JCheckBox("NonLooping");
		nonLoopingChooser.addActionListener(e -> nonLoopingChooser());

		add(nonLoopingChooser, "cell 0 2");
		add(new JLabel("Rarity"), "cell 0 3");
		add(rarityChooser, "cell 1 3");
		add(new JLabel("MoveSpeed"), "cell 0 4");
		add(moveSpeedChooser, "cell 1 4");

		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(e -> deleteAnim());
		deleteButton.setBackground(Color.RED);
		deleteButton.setForeground(Color.WHITE);
		add(deleteButton, "cell 0 5, gapy 20px");

	}

	private void nonLoopingChooser() {
		SetAnimationNonLoopingAction setAnimationNonLoopingAction = new SetAnimationNonLoopingAction(
				animation.isNonLooping(), nonLoopingChooser.isSelected(), animation, modelStructureChangeListener);
		setAnimationNonLoopingAction.redo();
		modelHandler.getUndoManager().pushAction(setAnimationNonLoopingAction);
	}

	private void moveSpeedChooser() {
		SetAnimationMoveSpeedAction setAnimationMoveSpeedAction = new SetAnimationMoveSpeedAction(
				animation.getMoveSpeed(), moveSpeedChooser.getFloatValue(), animation, modelStructureChangeListener);
		setAnimationMoveSpeedAction.redo();
		modelHandler.getUndoManager().pushAction(setAnimationMoveSpeedAction);
	}

	private void rarityChooser() {
		SetAnimationRarityAction setAnimationRarityAction = new SetAnimationRarityAction(
				animation.getRarity(), rarityChooser.getFloatValue(), animation, modelStructureChangeListener);
		setAnimationRarityAction.redo();
		modelHandler.getUndoManager().pushAction(setAnimationRarityAction);
	}

	private void newAnimTimeEnd() {
		SetAnimationIntervalEndAction setAnimationIntervalEndAction = new SetAnimationIntervalEndAction(
				animation.getEnd(), newAnimTimeEnd.getIntValue(), animation, modelStructureChangeListener);
		setAnimationIntervalEndAction.redo();
		modelHandler.getUndoManager().pushAction(setAnimationIntervalEndAction);
	}

	private void nameField() {
		SetAnimationNameAction setAnimationNameAction = new SetAnimationNameAction(
				animation.getName(), nameField.getText(), animation, modelStructureChangeListener);
		setAnimationNameAction.redo();
		modelHandler.getUndoManager().pushAction(setAnimationNameAction);
	}

	private void newAnimTimeStart() {
		SetAnimationIntervalStartAction setAnimationIntervalStartAction = new SetAnimationIntervalStartAction(
				animation.getStart(), newAnimTimeStart.getIntValue(), animation, modelStructureChangeListener);
		setAnimationIntervalStartAction.redo();
		modelHandler.getUndoManager().pushAction(setAnimationIntervalStartAction);
	}

	private void deleteAnim() {
		DeleteAnimationAction deleteAnimationAction = new DeleteAnimationAction(modelHandler.getModel(), animation, modelStructureChangeListener);
		modelHandler.getUndoManager().pushAction(deleteAnimationAction);
		deleteAnimationAction.redo();
	}

	public Animation getAnimation() {
		Animation newAnimation = new Animation(nameField.getText(), newAnimTimeStart.getIntValue(), ((Number) newAnimTimeEnd.getValue()).intValue());
		int rarityValue = ((Number) rarityChooser.getValue()).intValue();
		int moveValue = ((Number) moveSpeedChooser.getValue()).intValue();
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

	@Override
	public void setSelectedItem(Animation animation) {
		this.animation = animation;
		nameField.reloadNewValue(animation.getName());
		newAnimTimeStart.reloadNewValue(animation.getStart());
		newAnimTimeEnd.reloadNewValue(animation.getEnd());
		rarityChooser.reloadNewValue(0);
		moveSpeedChooser.reloadNewValue(0);
		nonLoopingChooser.setSelected(animation.isNonLooping());
		rarityChooser.reloadNewValue(animation.getRarity());
		moveSpeedChooser.reloadNewValue(animation.getMoveSpeed());
	}

	@Override
	public void save(EditableModel model, UndoManager undoManager, ModelStructureChangeListener changeListener) {

	}
}
