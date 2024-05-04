package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.animation.*;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.actions.util.ConsumerAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.editor.model.FakeAnimation;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.tools.ChangeSingleAnimLengthPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.SequenceComboBoxRenderer;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ComponentAnimationPanel extends ComponentPanel<Animation> {
	private final ComponentEditorTextField nameField;
	private final JLabel realAnimChooserLabel;
	private final List<Animation> realAnims = new ArrayList<>();
	private final TwiComboBox<Animation> realAnimChooser;
	private final JButton duplicateButton;
	private final IntEditorJSpinner animTimeStart;
	private final IntEditorJSpinner animLength;
	private final FloatEditorJSpinner rarityChooser;
	private final FloatEditorJSpinner moveSpeedChooser;
	private final JCheckBox nonLoopingChooser;
	private final ExtLogEditor extLogEditor;
	private Animation animation;

	public ComponentAnimationPanel(ModelHandler modelHandler, ComponentsPanel componentsPanel) {
		super(modelHandler, componentsPanel, new MigLayout("hidemode 3"));

		add(new JLabel("Name: "), "");
		nameField = new ComponentEditorTextField(24, this::nameField);
		add(nameField, "");

		duplicateButton = Button.create("Duplicate", e -> duplicateAnim());
		add(duplicateButton, "spanx, wrap");

		realAnimChooserLabel = new JLabel("Real Anim:");
		add(realAnimChooserLabel);
		realAnimChooser = new TwiComboBox<>(realAnims, new Animation("PrototypePrototypePrototype", 0, 1));
		realAnimChooser.addOnSelectItemListener(this::setFakeAnimSourceAnim);
		realAnimChooser.setRenderer(new SequenceComboBoxRenderer(modelHandler));
		add(realAnimChooser, "growx, wrap");

		add(new JLabel("Start: "), "");
		animTimeStart = new IntEditorJSpinner(300, this::setAnimTimeStart);
		add(animTimeStart, "");

		add(new JLabel("Length: "), "");
		animLength = new IntEditorJSpinner(1300, this::setAnimLength);
		add(animLength, "wrap");

		nonLoopingChooser = new JCheckBox("NonLooping");
		nonLoopingChooser.addActionListener(e -> nonLoopingChooser(nonLoopingChooser.isSelected()));
		add(nonLoopingChooser, "");

		add(Button.create("Scale Length", e -> ChangeSingleAnimLengthPanel.showPopup(modelHandler, animation, this)), "skip 2, right, wrap");

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

	private void setFakeAnimSourceAnim(Animation source) {
		if (animation instanceof FakeAnimation) {
			Animation oldSource = ((FakeAnimation) animation).getRealAnim();
			if (oldSource != source) {
				Consumer<Animation> setRealAnim = ((FakeAnimation) animation)::setRealAnim;
				undoManager.pushAction(new ConsumerAction<>(setRealAnim, source, oldSource, "Source Animation").redo());
			}
		}
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

	private void duplicateAnim() {
		if (animation instanceof FakeAnimation) {
			RemoveSequenceAction removeAction = new RemoveSequenceAction(model, animation, null);
			DuplicateAnimationAction duplicateAction = new DuplicateAnimationAction(model, ((FakeAnimation) animation).getRealAnim(), animation.getName(), null);
			CompoundAction action = new CompoundAction("Make \"" + animation.getName() + "\" Real", changeListener::animationParamsChanged, removeAction, duplicateAction);
			undoManager.pushAction(action.redo());
		} else {
			undoManager.pushAction(new DuplicateAnimationAction(model, animation, animation.getName() + " copy", changeListener).redo());
		}
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
		realAnims.clear();
		if (animation instanceof FakeAnimation) {
			duplicateButton.setText("Make Real");
			realAnimChooserLabel.setVisible(true);
			model.getAnims().stream().filter(a -> !(a instanceof FakeAnimation)).forEach(realAnims::add);
			realAnimChooser.selectOrFirst(((FakeAnimation) animation).getRealAnim());
			realAnimChooser.setVisible(true);
		} else {
			duplicateButton.setText("Duplicate");
			realAnimChooserLabel.setVisible(false);
			realAnimChooser.setVisible(false);
		}
		return this;
	}
}
