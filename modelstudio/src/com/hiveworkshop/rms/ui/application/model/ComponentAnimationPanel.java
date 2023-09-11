package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.animation.*;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ComponentAnimationPanel extends ComponentPanel<Animation> {
	private final ComponentEditorTextField nameField;
	private final IntEditorJSpinner animTimeStart;
	private final IntEditorJSpinner animLength;
	private final FloatEditorJSpinner rarityChooser;
	private final FloatEditorJSpinner moveSpeedChooser;
	private final JCheckBox nonLoopingChooser;
	private final ExtLogEditor extLogEditor;
	private Animation animation;

	public ComponentAnimationPanel(ModelHandler modelHandler, ComponentsPanel componentsPanel) {
		super(modelHandler, componentsPanel);
		setLayout(new MigLayout());

		add(new JLabel("Name: "), "");
		nameField = new ComponentEditorTextField(24, this::nameField);
		add(nameField, "wrap");

		add(new JLabel("Start: "), "");
		animTimeStart = new IntEditorJSpinner(300, this::setAnimTimeStart);
		add(animTimeStart, "");

		add(new JLabel("Length: "), "");
		animLength = new IntEditorJSpinner(1300, this::setAnimLength);
		add(animLength, "wrap");


		nonLoopingChooser = new JCheckBox("NonLooping");
		nonLoopingChooser.addActionListener(e -> nonLoopingChooser(nonLoopingChooser.isSelected()));
		add(nonLoopingChooser, "wrap");

		add(new JLabel("Rarity"), "");
		rarityChooser = new FloatEditorJSpinner(0f, 0f, this::rarityChooser);
		add(rarityChooser, "wrap");

		add(new JLabel("MoveSpeed"), "");
		moveSpeedChooser = new FloatEditorJSpinner(0f, 0f, this::moveSpeedChooser);
		add(moveSpeedChooser, "wrap");

		extLogEditor = new ExtLogEditor();
		extLogEditor.setBorder(BorderFactory.createTitledBorder("Extents"));
		extLogEditor.addExtLogConsumer(this::setExtLog);

		add(extLogEditor, "spanx, wrap");
		add(getDeleteButton(e -> deleteAnim()), "gapy 20px, wrap");

	}

	private void nonLoopingChooser(boolean b) {
		if (animation.isNonLooping() != b) {
			undoManager.pushAction(new SetAnimationNonLoopingAction(b, animation, changeListener).redo());
		}
	}

	private void moveSpeedChooser(float value) {
		if (animation.getMoveSpeed() != value) {
			undoManager.pushAction(new SetAnimationMoveSpeedAction(value, animation, changeListener).redo());
		}
	}

	private void rarityChooser(float value) {
		if (animation.getRarity() != value) {
			undoManager.pushAction(new SetAnimationRarityAction(value, animation, changeListener).redo());
		}
	}

	private void setAnimLength(int value) {
		if (animation.getLength() != value) {
			undoManager.pushAction(new SetSequenceLengthAction(animation, value, changeListener).redo());
		}
	}

	private void nameField(String text) {
		if (!animation.getName().equals(text)) {
			undoManager.pushAction(new SetAnimationNameAction(text, animation, changeListener).redo());
		}
	}

	private void setAnimTimeStart(int value) {
		if (animation.getStart() != value) {
			undoManager.pushAction(new SetAnimationStartAction(animation, value, changeListener).redo());
		}
	}

	private void deleteAnim() {
		undoManager.pushAction(new RemoveSequenceAction(model, animation, changeListener).redo());
	}

	private void setExtLog(ExtLog extLog) {
		if (!animation.getExtents().equals(extLog)) {
			undoManager.pushAction(new SetAnimExtentAction(animation, extLog, changeListener).redo());
		}
	}

	@Override
	public ComponentPanel<Animation> setSelectedItem(Animation animation) {
		this.animation = animation;
		selectedItem = animation;
		nameField.reloadNewValue(animation.getName());
		animTimeStart.reloadNewValue(animation.getStart());
		animLength.reloadNewValue(animation.getLength());
		nonLoopingChooser.setSelected(animation.isNonLooping());
		rarityChooser.reloadNewValue(animation.getRarity());
		moveSpeedChooser.reloadNewValue(animation.getMoveSpeed());
		extLogEditor.setExtLog(animation.getExtents());
		return this;
	}
}
