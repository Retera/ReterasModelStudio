package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.*;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class CreateAnimationPopup extends JPanel {
	private final UndoManager undoManager;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
	private final NewAnimSettings newAnimSettings;
	private final Animation anim;
	private static boolean scaleKfs = true;

	public CreateAnimationPopup(ModelHandler modelHandler, Animation anim) {
		super(new MigLayout());
		this.anim = anim;
		this.model = modelHandler.getModel();
		this.undoManager = modelHandler.getUndoManager();
		if (anim == null) {
			newAnimSettings = new NewAnimSettings("", 0, 1000);
		} else {
			newAnimSettings = new NewAnimSettings(anim);
		}
		fillPanel();
	}

	private void fillPanel() {
		add(new JLabel("Name: "));
		add(newAnimSettings.getNameField(), "span x, wrap");

		add(new JLabel("Length: "));
		add(newAnimSettings.getLengthSpinner(), "");
		add(newAnimSettings.getScaleKfsBox(), "wrap");

		add(newAnimSettings.getNonLoopingChooser(), "spanx");
		add(new JLabel(MdlUtils.TOKEN_RARITY));
		add(newAnimSettings.getRarityChooser(), "wrap");
		add(new JLabel(MdlUtils.TOKEN_MOVESPEED));
		add(newAnimSettings.getMoveSpeedChooser(), "wrap");
	}

	private void addNewAnim() {
		undoManager.pushAction(getAddAnimAction().redo());
	}

	public AddSequenceAction getAddAnimAction() {
		Animation sequence = getNewAnimation();
		return new AddSequenceAction(model, sequence, changeListener);
	}

	public Animation getNewAnimation() {
		Animation sequence = newAnimSettings.getTempAnim().deepCopy();
		sequence.setExtents(model.getExtents().deepCopy());
		return sequence;
	}

	private void editAnim() {
		List<UndoAction> actions = getEditAnimActions();
		String actionName = actions.size() == 1 ? actions.get(0).actionName() : "Edit \"" + anim.getName() + "\"";
		undoManager.pushAction(new CompoundAction(actionName, actions, changeListener::animationParamsChanged).redo());
	}

	public List<UndoAction> getEditAnimActions() {
		Animation tempAnim = newAnimSettings.getTempAnim();
		List<UndoAction> actions = new ArrayList<>();

		if (!Objects.equals(tempAnim.getName(), anim.getName())) {
			actions.add(new SetAnimationNameAction(tempAnim.getName(), anim, changeListener));
		}
		if (tempAnim.getLength() != anim.getLength()) {
			if (scaleKfs) {
				HashMap<Sequence, Integer> lenghtMap = new HashMap<>();
				lenghtMap.put(anim, tempAnim.getLength());
				actions.add(new ScaleSequencesLengthsAction(model, lenghtMap, changeListener));
			} else {
				actions.add(new SetSequenceLengthAction(anim, tempAnim.getLength(), changeListener));
			}
		}
		if (tempAnim.getRarity() != anim.getRarity()) {
			actions.add(new SetAnimationRarityAction(tempAnim.getRarity(), anim, changeListener));
		}
		if (tempAnim.getMoveSpeed() != anim.getMoveSpeed()) {
			actions.add(new SetAnimationMoveSpeedAction(tempAnim.getMoveSpeed(), anim, changeListener));
		}
		if (tempAnim.isNonLooping() != anim.isNonLooping()) {
			actions.add(new SetAnimationNonLoopingAction(tempAnim.isNonLooping(), anim, changeListener));
		}
		return actions;
	}

	public void showPopup(Component parent) {
		String title = anim == null ? "Create Animation" : "Edit " + anim.getName() + " (" + anim.getLength() + ")";
		int result = JOptionPane.showConfirmDialog(parent, this,
				title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			if (anim == null) {
				addNewAnim();
			} else {
				editAnim();
			}
		}
	}

	public static void showPopup(ModelHandler modelHandler, Animation selectedAnim, Component parent) {
		CreateAnimationPopup createAnimationPopup = new CreateAnimationPopup(modelHandler, selectedAnim);
		String title = selectedAnim == null ? "Create Animation" : "Edit " + selectedAnim.getName() + " (" + selectedAnim.getLength() + ")";
		int result = JOptionPane.showConfirmDialog(parent, createAnimationPopup,
				title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			if (selectedAnim == null) {
				createAnimationPopup.addNewAnim();
			} else {
				createAnimationPopup.editAnim();
			}
		}
	}

	private static class NewAnimSettings {
		private final Animation tempAnim;
		private TwiTextField nameField;
		private IntEditorJSpinner lengthSpinner;
		private JCheckBox scaleKfsBox;
		private JCheckBox nonLoopingChooser;
		private FloatEditorJSpinner rarityChooser;
		private FloatEditorJSpinner moveSpeedChooser;

		NewAnimSettings(String name, int start, int length) {
			tempAnim = new Animation(name, start, start + length);
			makeComponents();
		}

		NewAnimSettings(Animation animation) {
			tempAnim = animation.deepCopy();
			makeComponents();
		}

		public Animation getTempAnim() {
			return tempAnim;
		}

		private void makeComponents() {
			nameField = new TwiTextField(tempAnim.getName(), 24, tempAnim::setName);
			rarityChooser = new FloatEditorJSpinner(tempAnim.getRarity(), 0, 1, tempAnim::setRarity);
			moveSpeedChooser = new FloatEditorJSpinner(tempAnim.getMoveSpeed(), 0, tempAnim::setMoveSpeed);
			lengthSpinner = new IntEditorJSpinner(tempAnim.getLength(), 0, i -> setAndUpdate(i, tempAnim::setLength));
			nonLoopingChooser = CheckBox.create(MdlUtils.TOKEN_NONLOOPING, tempAnim.isNonLooping(), tempAnim::setNonLooping);

			scaleKfsBox = CheckBox.create("Scale Keyframes", scaleKfs, b -> scaleKfs = b);
			CheckBox.setTooltip(scaleKfsBox, "Adjust the speed of the animation to fit the new length");
		}

		private void setAndUpdate(int i, Consumer<Integer> consumer) {
			consumer.accept(i);
			updateSpinners();
		}

		public TwiTextField getNameField() {
			return nameField;
		}

		public FloatEditorJSpinner getRarityChooser() {
			return rarityChooser;
		}

		public FloatEditorJSpinner getMoveSpeedChooser() {
			return moveSpeedChooser;
		}

		public JCheckBox getNonLoopingChooser() {
			return nonLoopingChooser;
		}

		public JCheckBox getScaleKfsBox() {
			return scaleKfsBox;
		}

		public IntEditorJSpinner getLengthSpinner() {
			return lengthSpinner;
		}

		private void updateSpinners() {
			lengthSpinner.reloadNewValue(tempAnim.getLength());
		}
	}
}
