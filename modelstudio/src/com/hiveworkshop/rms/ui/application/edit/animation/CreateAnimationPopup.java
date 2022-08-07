package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.*;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CreateAnimationPopup extends JPanel {
	private final UndoManager undoManager;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;

	private Component parent;

	NewAnimSettings newAnimSettings;


	Animation anim;

	public CreateAnimationPopup(ModelHandler modelHandler, Animation anim){
		super(new MigLayout());
		this.anim = anim;
		this.model = modelHandler.getModel();
		this.undoManager = modelHandler.getUndoManager();
		if(anim == null){
			newAnimSettings = new NewAnimSettings("", 0, 1000);
		} else {
			newAnimSettings = new NewAnimSettings(anim);
		}
		fillPanel();
	}

	private void fillPanel() {
		add(new JLabel("Name: "));
		add(newAnimSettings.getNameField(), "span x, wrap");

		add(newAnimSettings.getLengthButton());
		add(newAnimSettings.getLengthSpinner(), "wrap");
		add(newAnimSettings.getTimeRangeButton(), "wrap");

		JPanel timeRangePanel = new JPanel(new MigLayout("fill, ins 0"));
		timeRangePanel.add(new JLabel("Start: "));
		timeRangePanel.add(newAnimSettings.getStartSpinner());
		timeRangePanel.add(new JLabel("End: "));
		timeRangePanel.add(newAnimSettings.getEndSpinner(), "wrap");
		add(timeRangePanel, "spanx, wrap");

		JPanel extraProperties = getExtraPropsPanel();

		add(extraProperties, "spanx, wrap");
	}

	private void addNewAnim() {
		Animation newAnimation = new Animation(newAnimSettings.getName(),
				newAnimSettings.getStart(),
				newAnimSettings.getEnd());
		float rarityValue = newAnimSettings.getRarity();
		float moveValue = newAnimSettings.getMoveSpeed();
		if (rarityValue != 0) {
			newAnimation.setRarity(rarityValue);
		}
		if (moveValue != 0) {
			newAnimation.setMoveSpeed(moveValue);
		}
		if (newAnimSettings.isNonLooping()) {
			newAnimation.setNonLooping(true);
		}
		undoManager.pushAction(new AddSequenceAction(model, newAnimation, changeListener).redo());
	}

	private void changeAnim() {
		boolean scaleAnim = false;
		List<UndoAction> actions = new ArrayList<>();

		if (!Objects.equals(newAnimSettings.getName(), anim.getName())) {
			actions.add(new SetAnimationNameAction(newAnimSettings.getName(), anim, changeListener));
		}
		if (newAnimSettings.getStart() != anim.getStart()) {
			actions.add(new SetAnimationStartAction(anim, newAnimSettings.getStart(), changeListener));
		}
		if (newAnimSettings.getLength() != anim.getLength()) {
			if(scaleAnim){
				HashMap<Sequence, Integer> lenghtMap = new HashMap<>();
				lenghtMap.put(anim, newAnimSettings.getLength());
				actions.add(new ScaleSequencesLengthsAction(model, lenghtMap, changeListener));
			} else {
				actions.add(new SetSequenceLengthAction(anim, newAnimSettings.getLength(), changeListener));
			}
		}
		if (newAnimSettings.getRarity() != anim.getRarity()) {
			actions.add(new SetAnimationRarityAction(newAnimSettings.getRarity(), anim, changeListener));
		}
		if (newAnimSettings.getMoveSpeed() != anim.getMoveSpeed()) {
			actions.add(new SetAnimationMoveSpeedAction(newAnimSettings.getMoveSpeed(), anim, changeListener));
		}
		if (newAnimSettings.isNonLooping() != anim.isNonLooping()) {
			actions.add(new SetAnimationNonLoopingAction(newAnimSettings.isNonLooping(), anim, changeListener));
		}

		undoManager.pushAction(new CompoundAction("", actions, changeListener::animationParamsChanged).redo());
	}

	private JPanel getExtraPropsPanel() {
		JPanel extraProperties = new JPanel();

		extraProperties.setBorder(BorderFactory.createTitledBorder("Misc"));
		extraProperties.setLayout(new MigLayout());

		extraProperties.add(newAnimSettings.getNonLoopingChooser(), "spanx");
		extraProperties.add(new JLabel("Rarity"));
		extraProperties.add(newAnimSettings.getRarityChooser(), "wrap");
		extraProperties.add(new JLabel("MoveSpeed"));
		extraProperties.add(newAnimSettings.getMoveSpeedChooser(), "wrap");
		return extraProperties;
	}

	public void showPopup(Component parent) {
		int result = JOptionPane.showConfirmDialog(parent, this,
				"Create Animation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			if(anim == null){
				addNewAnim();
			} else {
				changeAnim();
			}
		}
	}

	public static void showPopup(ModelHandler modelHandler, Animation selectedAnim, Component parent) {
		CreateAnimationPopup createAnimationPopup = new CreateAnimationPopup(modelHandler, selectedAnim);
		int result = JOptionPane.showConfirmDialog(parent, createAnimationPopup,
				"Create Animation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			if(selectedAnim == null){
				createAnimationPopup.addNewAnim();
			} else {
				createAnimationPopup.changeAnim();
			}
		}
	}


	private static class NewAnimSettings{
		String name;
		int length;
		int start;
		int end;

		float rarity = 0;
		float moveSpeed = 0;

		boolean nonLooping = false;
		IntEditorJSpinner lengthSpinner;
		IntEditorJSpinner startSpinner;
		IntEditorJSpinner endSpinner;

		SmartButtonGroup newAnimBtnGrp;
		JRadioButton lengthButton;
		JRadioButton timeRangeButton;
		TwiTextField nameField;

		FloatEditorJSpinner rarityChooser;
		FloatEditorJSpinner moveSpeedChooser;

		JCheckBox nonLoopingChooser;

		NewAnimSettings(String name, int start, int length){
			this.name = name;
			this.start = start;
			this.length = length;
			this.end = start + length;
			makeComponents();
		}

		NewAnimSettings(Animation animation){
			this.name = animation.getName();
			this.length = animation.getLength();
			this.start = animation.getStart();
			this.end = start + length;
			this.moveSpeed = animation.getMoveSpeed();
			this.nonLooping = animation.isNonLooping();
			this.rarity = animation.getRarity();
			makeComponents();
		}

		private void makeComponents() {
			nameField = new TwiTextField(name, 24, this::setName);
			rarityChooser = new FloatEditorJSpinner(rarity, 0, 1, this::setRarity);
			moveSpeedChooser = new FloatEditorJSpinner(moveSpeed, 0, this::setMoveSpeed);
			startSpinner = new IntEditorJSpinner(start, 0, this::setStart);
			lengthSpinner = new IntEditorJSpinner(length, 0, this::setLength);
			endSpinner = new IntEditorJSpinner(end, 0, this::setEnd);
			nonLoopingChooser = new JCheckBox("NonLooping", nonLooping);
			nonLoopingChooser.addActionListener(e -> setNonLooping(nonLoopingChooser.isSelected()));


			newAnimBtnGrp = new SmartButtonGroup();
			lengthButton = newAnimBtnGrp.addJRadioButton("Length", e -> setMode(!lengthButton.isSelected()));
			timeRangeButton = newAnimBtnGrp.addJRadioButton("Time Range", e -> setMode(timeRangeButton.isSelected()));
			newAnimBtnGrp.setSelectedIndex(0);
			setMode(false);
		}

		public TwiTextField getNameField() {
			return nameField;
		}

		public NewAnimSettings setName(String name) {
			this.name = name;
			return this;
		}

		public String getName() {
			return name;
		}

		public FloatEditorJSpinner getRarityChooser() {
			return rarityChooser;
		}

		public NewAnimSettings setRarity(float rarity) {
			this.rarity = rarity;
			return this;
		}

		public float getRarity() {
			return rarity;
		}

		public FloatEditorJSpinner getMoveSpeedChooser() {
			return moveSpeedChooser;
		}

		public NewAnimSettings setMoveSpeed(float moveSpeed) {
			this.moveSpeed = moveSpeed;
			return this;
		}

		public float getMoveSpeed() {
			return moveSpeed;
		}

		public JCheckBox getNonLoopingChooser() {
			return nonLoopingChooser;
		}

		public NewAnimSettings setNonLooping(boolean nonLooping) {
			this.nonLooping = nonLooping;
			return this;
		}

		public boolean isNonLooping() {
			return nonLooping;
		}

		public IntEditorJSpinner getStartSpinner() {
			return startSpinner;
		}

		public NewAnimSettings setStart(int start) {
			this.start = start;
			this.end = start + length;
			updateSpinners();
			return this;
		}

		public int getStart() {
			return start;
		}

		public IntEditorJSpinner getLengthSpinner() {
			return lengthSpinner;
		}

		public NewAnimSettings setLength(int length) {
			this.length = length;
			this.end = start + length;
			updateSpinners();
			return this;
		}

		public int getLength() {
			return length;
		}

		public IntEditorJSpinner getEndSpinner() {
			return endSpinner;
		}

		public NewAnimSettings setEnd(int end) {
			this.end = end;
			this.length = end - start;
			updateSpinners();
			return this;
		}

		public int getEnd() {
			return end;
		}

		public JRadioButton getLengthButton() {
			return lengthButton;
		}

		public JRadioButton getTimeRangeButton() {
			return timeRangeButton;
		}

		private void updateSpinners(){
			startSpinner.reloadNewValue(start);
			endSpinner.reloadNewValue(end);
			lengthSpinner.reloadNewValue(length);
		}

		private void setMode(boolean isTimeRange) {
			System.out.println("timeRangeMode: " + isTimeRange);
			lengthSpinner.setEnabled(!isTimeRange);
			startSpinner.setEnabled(isTimeRange);
			endSpinner.setEnabled(isTimeRange);
		}
	}
}
