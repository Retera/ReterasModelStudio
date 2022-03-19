package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddSequenceAction;
import com.hiveworkshop.rms.editor.actions.animation.RemoveSequenceAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryMapAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class TimeBoundChooserPanel extends JPanel {
	private JSpinner timeStart, timeEnd;
	private IterableListModel<Animation> animations;
	private JList<Animation> animationBox;
	private IterableListModel<GlobalSeq> globalSeqList;
	private JList<GlobalSeq> globalSeqBox;
	private JTabbedPane tabs;

	public TimeBoundChooserPanel(ModelHandler modelHandler) {
		makeAnimationBox(modelHandler.getModelView());
		final JPanel animationPanel = getAnimationPanel(modelHandler);

		final JPanel globSeqPanel = getGlobSeqPanel(modelHandler);

		final JPanel customTimePanel = getCustomTimePanel(modelHandler);

		setLayout(new BorderLayout());
		tabs = new JTabbedPane();

		tabs.addTab("Animation", animationPanel);

		tabs.addTab("Custom Time", customTimePanel);

		tabs.addTab("Global Sequence", globSeqPanel);
		add(tabs);
	}

	private JPanel getAnimationPanel(ModelHandler modelHandler) {
		final JPanel animationPanel = new JPanel(new MigLayout("fill", "[]", "[grow][]"));

		JScrollPane animationScrollPane = new JScrollPane(animationBox);
		animationScrollPane.setPreferredSize(new Dimension(500, 320));
		animationPanel.add(animationScrollPane, "spanx, growx, growy, wrap");

		JPanel buttonPanel = new JPanel(new MigLayout("ins 0"));
		final JButton createAnimation = new JButton("Create");
		createAnimation.addActionListener(e -> createAnimation(modelHandler));
		buttonPanel.add(createAnimation);

		final JButton duplicateAnimation = new JButton("Duplicate");
		duplicateAnimation.addActionListener(e -> duplicateAnimation(modelHandler));
		buttonPanel.add(duplicateAnimation);


		final JButton editAnimation = new JButton("Edit");
		editAnimation.addActionListener(e -> editAnimation(modelHandler));
		buttonPanel.add(editAnimation);

		final JButton deleteAnimation = new JButton("Delete");
		deleteAnimation.addActionListener(e -> deleteAnimation(modelHandler));
		buttonPanel.add(deleteAnimation);
		animationPanel.add(buttonPanel);
		return animationPanel;
	}

	private JPanel getGlobSeqPanel(ModelHandler modelHandler) {
		final JPanel globSeqPanel = new JPanel(new MigLayout("fill"));

		globalSeqList = new IterableListModel<>();
		for (final GlobalSeq animation : modelHandler.getModel().getGlobalSeqs()) {
			globalSeqList.addElement(animation);
		}

		globalSeqBox = new JList<>(globalSeqList);
		JScrollPane globalSeqScrollPane = new JScrollPane(globalSeqBox);
		globalSeqScrollPane.setPreferredSize(new Dimension(500, 320));
		globSeqPanel.add(globalSeqScrollPane, "spanx, growx, growy");

		final JButton createGlobalSeq = new JButton("Create");
		createGlobalSeq.addActionListener(e -> createGlobalSeq(modelHandler));

		final JButton deleteGlobalSeq = new JButton("Delete");
		deleteGlobalSeq.addActionListener(e -> deleteGlobalSeq(modelHandler));

		globSeqPanel.add(createGlobalSeq);
		globSeqPanel.add(deleteGlobalSeq);
		return globSeqPanel;
	}

	private JPanel getCustomTimePanel(ModelHandler modelHandler) {
		Sequence timeBound = getTimeBound(modelHandler);
		int startTime = 0;
		int endTime = 1000;
		if (timeBound != null) {
			startTime = timeBound.getStart();
			endTime = timeBound.getEnd();
		}
		final JPanel customTimePanel = new JPanel(new MigLayout("", "[]"));
		customTimePanel.add(new JLabel("Start:"));
		timeStart = new JSpinner(new SpinnerNumberModel(startTime, 0, Integer.MAX_VALUE, 1));
		customTimePanel.add(timeStart, "growx, wrap");

		customTimePanel.add(new JLabel("End:"));
		timeEnd = new JSpinner(new SpinnerNumberModel(endTime, 0, Integer.MAX_VALUE, 1));
		customTimePanel.add(timeEnd, "growx, wrap");
		return customTimePanel;
	}

	private Sequence getTimeBound(ModelHandler modelHandler) {
		RenderModel editorRenderModel = modelHandler.getRenderModel();
		if (editorRenderModel != null) {
			TimeEnvironmentImpl renderEnvironment = editorRenderModel.getTimeEnvironment();
			if (renderEnvironment != null) {
				return renderEnvironment.getCurrentSequence();
			}
		}
		return null;
	}

	private void makeAnimationBox(ModelView modelView) {
		animations = new IterableListModel<>();
		if (modelView != null) {
			for (final Animation animation : modelView.getModel().getAnims()) {
				animations.addElement(animation);
			}
		}
		animationBox = new JList<>(animations);
//		animationBox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // until code is improved
		ListSelectionListener listener = getListSelectionListener();
		animationBox.addListSelectionListener(listener);
	}

	private ListSelectionListener getListSelectionListener() {
		return e -> {
			final Animation selectedValue = animationBox.getSelectedValue();
			if (selectedValue != null) {
				timeStart.setValue(selectedValue.getStart());
				timeEnd.setValue(selectedValue.getEnd());
			}
		};
	}

	public void applyTo(final TimeEnvironmentImpl timeEnvironmentImpl) {
		if (tabs.getSelectedIndex() == 0) {
			final Animation selectedAnimation = animationBox.getSelectedValue();
			if (selectedAnimation != null) {
				timeEnvironmentImpl.setSequence(selectedAnimation);
			}
		} else if (tabs.getSelectedIndex() == 1) {
			timeEnvironmentImpl.setBounds(((Integer) timeStart.getValue()), ((Integer) timeEnd.getValue()));
		} else if (tabs.getSelectedIndex() == 2) {
			final GlobalSeq selectedValue = globalSeqBox.getSelectedValue();
			if (selectedValue != null) {
				timeEnvironmentImpl.setSequence(selectedValue);
			} else {
				JOptionPane.showMessageDialog(this, "You didn't select a global sequence!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void deleteAnimation(ModelHandler modelHandler) {
		int option = JOptionPane.showConfirmDialog(TimeBoundChooserPanel.this, "Also delete keyframes?",
				"Delete Animation(s)", JOptionPane.YES_NO_CANCEL_OPTION);
		if (option != JOptionPane.CANCEL_OPTION) {
			List<Animation> selectedValues = animationBox.getSelectedValuesList();
			List<UndoAction> deleteActions = new ArrayList<>();
			for (Animation animation : selectedValues) {
				UndoAction deleteAnimationAction = new RemoveSequenceAction(modelHandler.getModel(), animation, null);
				deleteActions.add(deleteAnimationAction);
			}
			UndoAction undoAction = new CompoundAction("Delete " + deleteActions.size() + " Animation(s)", deleteActions, ModelStructureChangeListener.changeListener::animationParamsChanged);
			modelHandler.getUndoManager().pushAction(undoAction.redo());
		}
	}

	private void deleteGlobalSeq(ModelHandler modelHandler) {
		final int result = JOptionPane.showConfirmDialog(TimeBoundChooserPanel.this,
				"Also delete linked timelines and their keyframes?", "Delete Animation",
				JOptionPane.YES_NO_CANCEL_OPTION);
		final GlobalSeq selectedValue = globalSeqBox.getSelectedValue();
		if (result != JOptionPane.CANCEL_OPTION) {
			// del anim
			UndoAction action = new RemoveSequenceAction(modelHandler.getModel(), selectedValue, ModelStructureChangeListener.changeListener);
			modelHandler.getUndoManager().pushAction(action.redo());
			globalSeqList.removeElement(selectedValue);
		}
	}

	private void createGlobalSeq(ModelHandler modelHandler) {
		final SpinnerNumberModel sModel = new SpinnerNumberModel(1000, 1, Integer.MAX_VALUE, 1);
		final JSpinner spinner = new JSpinner(sModel);
		final int userChoice = JOptionPane.showConfirmDialog(TimeBoundChooserPanel.this, spinner,
				"Enter Length", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (userChoice != JOptionPane.OK_OPTION) {
			return;
		}
		if (globalSeqList.contains(spinner.getValue())) {
			JOptionPane.showMessageDialog(TimeBoundChooserPanel.this,
					"A Global Sequence with that length already exists." +
							"\nThis program does not support multiple Global Sequences of the same length." +
							"\nInstead, simply add animation data to the sequence of that length which already exists.",
					"Error", JOptionPane.ERROR_MESSAGE);
		} else {
			UndoAction action = new AddSequenceAction(modelHandler.getModel(), new GlobalSeq((Integer) spinner.getValue()), ModelStructureChangeListener.getModelStructureChangeListener());
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}

	private void createAnimation(ModelHandler modelHandler) {
		EditableModel model = modelHandler.getModel();
		final JPanel createAnimQuestionPanel = new JPanel(new MigLayout());
		final JSpinner newAnimLength = new JSpinner(new SpinnerNumberModel(1000, 0, Integer.MAX_VALUE, 1));
		final Animation lastAnimation = model.getAnimsSize() == 0 ? null : model.getAnim(model.getAnimsSize() - 1);
		final int lastAnimationEnd = lastAnimation == null ? 0 : lastAnimation.getEnd();

		final JSpinner newAnimTimeStart = new JSpinner(new SpinnerNumberModel(lastAnimationEnd + 300, 0, Integer.MAX_VALUE, 1));
		final JSpinner newAnimTimeEnd = new JSpinner(new SpinnerNumberModel(lastAnimationEnd + 1300, 0, Integer.MAX_VALUE, 1));

		newAnimLength.addChangeListener(e13 -> {
			newAnimTimeStart.setValue(lastAnimationEnd + 300);
			newAnimTimeEnd.setValue(lastAnimationEnd + 300 + ((Number) newAnimLength.getValue()).intValue());
		});

		final ButtonGroup newAnimBtnGrp = new ButtonGroup();
		final JRadioButton lengthButton = new JRadioButton("Length");
		final JRadioButton timeRangeButton = new JRadioButton("Time Range");
		newAnimBtnGrp.add(lengthButton);
		newAnimBtnGrp.add(timeRangeButton);

		lengthButton.addActionListener(e -> lengthButton(lengthButton, newAnimLength, timeRangeButton, lastAnimationEnd, newAnimTimeStart, newAnimTimeEnd));
		timeRangeButton.addActionListener(e -> timeRangeButton(lengthButton, newAnimLength, timeRangeButton, newAnimTimeStart, newAnimTimeEnd));

		createAnimQuestionPanel.add(new JLabel("Name: "));
		final JTextField nameField = new JTextField(24);
		createAnimQuestionPanel.add(nameField, "span x, wrap");

		createAnimQuestionPanel.add(lengthButton);
		createAnimQuestionPanel.add(newAnimLength, "wrap");
		createAnimQuestionPanel.add(timeRangeButton, "wrap");

		JPanel timeRangePanel = new JPanel(new MigLayout("fill, ins 0"));
		timeRangePanel.add(new JLabel("Start: "));
		timeRangePanel.add(newAnimTimeStart);
		timeRangePanel.add(new JLabel("End: "));
		timeRangePanel.add(newAnimTimeEnd, "wrap");
		createAnimQuestionPanel.add(timeRangePanel, "spanx, wrap");

		final JPanel extraProperties = new JPanel();

		final JSpinner rarityChooser = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		final JSpinner moveSpeedChooser = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		extraProperties.setBorder(BorderFactory.createTitledBorder("Misc"));
		extraProperties.setLayout(new MigLayout());
		final JCheckBox nonLoopingChooser = new JCheckBox("NonLooping");
		extraProperties.add(nonLoopingChooser, "spanx");
		extraProperties.add(new JLabel("Rarity"));
		extraProperties.add(rarityChooser, "wrap");
		extraProperties.add(new JLabel("MoveSpeed"));
		extraProperties.add(moveSpeedChooser, "wrap");

		createAnimQuestionPanel.add(extraProperties, "spanx, wrap");

		lengthButton.doClick();
		final int result = JOptionPane.showConfirmDialog(TimeBoundChooserPanel.this, createAnimQuestionPanel,
				"Create Animation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			Animation newAnimation = new Animation(nameField.getText(),
					(Integer) newAnimTimeStart.getValue(),
					(Integer) newAnimTimeEnd.getValue());
			int rarityValue = (Integer) rarityChooser.getValue();
			int moveValue = (Integer) moveSpeedChooser.getValue();
			if (rarityValue != 0) {
				newAnimation.setRarity(rarityValue);
			}
			if (moveValue != 0) {
				newAnimation.setMoveSpeed(moveValue);
			}
			if (nonLoopingChooser.isSelected()) {
				newAnimation.setNonLooping(true);
			}
			UndoAction action = new AddSequenceAction(modelHandler.getModel(), newAnimation, ModelStructureChangeListener.getModelStructureChangeListener());
			animations.addElement(newAnimation);
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}

	private void timeRangeButton(JRadioButton lengthButton, JSpinner newAnimLength, JRadioButton timeRangeButton, JSpinner newAnimTimeStart, JSpinner newAnimTimeEnd) {
		newAnimLength.setEnabled(lengthButton.isSelected());
		newAnimTimeStart.setEnabled(timeRangeButton.isSelected());
		newAnimTimeEnd.setEnabled(timeRangeButton.isSelected());
	}

	private void lengthButton(JRadioButton lengthButton, JSpinner newAnimLength, JRadioButton timeRangeButton, int lastAnimationEnd, JSpinner newAnimTimeStart, JSpinner newAnimTimeEnd) {
		newAnimTimeStart.setValue(lastAnimationEnd + 300);
		newAnimTimeEnd.setValue(lastAnimationEnd + 300 + (Integer) newAnimLength.getValue());
		timeRangeButton(lengthButton, newAnimLength, timeRangeButton, newAnimTimeStart, newAnimTimeEnd);
	}

	private void editAnimation(ModelHandler modelHandler) {
		final Animation selectedValue = animationBox.getSelectedValue();
		if (selectedValue != null) {
			createAnimation(modelHandler);
		}
	}

	private void duplicateAnimation(ModelHandler modelHandler) {
		EditableModel model = modelHandler.getModel();
		Animation selectedAnimation = animationBox.getSelectedValue();
//		List<Animation> selectedValues = animationBox.getSelectedValuesList();
		String userChosenName = JOptionPane.showInputDialog(TimeBoundChooserPanel.this,
				"Choose new animation name:", selectedAnimation.getName() + " Second");
		if (userChosenName != null) {
//			for (Animation animation : selectedValues){
//			}
			Animation copyAnimation = selectedAnimation.deepCopy();
			Animation lastAnim = model.getAnim(model.getAnimsSize() - 1);
			copyAnimation.setAnimStuff(lastAnim.getEnd() + 300, selectedAnimation.getLength());
			copyAnimation.setName(userChosenName);

			List<UndoAction> undoActions = new ArrayList<>();

			for (AnimFlag<?> animFlag : ModelUtils.getAllAnimFlags(model)) {
				AddFlagEntryMapAction<?> e = getAddFlagEntryMapAction(selectedAnimation, copyAnimation, animFlag);
				if (e != null) {
					undoActions.add(e);
				}
			}

			undoActions.add(new AddSequenceAction(model, copyAnimation, null));
			CompoundAction action = new CompoundAction("Added Animation " + copyAnimation.getName(), undoActions, ModelStructureChangeListener.changeListener::animationParamsChanged);
			animations.addElement(copyAnimation);
			modelHandler.getUndoManager().pushAction(action.redo());
//			copyAnimation.copyFromAnimation(selectedAnimation, model.getEvents());
			for (EventObject e : model.getEvents()) {
				if (!e.hasGlobalSeq()) {
					e.copyFrom(e.copy(), selectedAnimation, copyAnimation);
				}
			}

		}
	}

	private <Q> AddFlagEntryMapAction<Q> getAddFlagEntryMapAction(Animation selectedAnimation, Animation copyAnimation, AnimFlag<Q> animFlag) {
		if(animFlag.hasSequence(selectedAnimation)){
			TreeMap<Integer, Entry<Q>> sequenceEntryMapCopy = animFlag.getSequenceEntryMapCopy(selectedAnimation);
			return new AddFlagEntryMapAction<>(animFlag, copyAnimation, sequenceEntryMapCopy, null);
		}
		return null;
	}
}
