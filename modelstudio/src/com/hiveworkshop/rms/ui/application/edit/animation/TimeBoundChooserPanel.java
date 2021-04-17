package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class TimeBoundChooserPanel extends JPanel {
	private JSpinner timeStart, timeEnd;
	private IterableListModel<Animation> animations;
	private JList<Animation> animationBox;
	private IterableListModel<Integer> globalSeqs;
	private JList<Integer> globalSeqBox;
	private JTabbedPane tabs;

	public TimeBoundChooserPanel(ModelView modelView, ModelStructureChangeListener structureChangeListener) {
		makeAnimationBox(modelView);
		final JPanel animationPanel = getAnimationPanel(modelView, structureChangeListener);

		final JPanel globSeqPanel = getGlobSeqPanel(modelView);

		final JPanel customTimePanel = getCustomTimePanel(modelView);

		setLayout(new BorderLayout());
		tabs = new JTabbedPane();

		tabs.addTab("Animation", animationPanel);

		tabs.addTab("Custom Time", customTimePanel);

		tabs.addTab("Global Sequence", globSeqPanel);
		add(tabs);
	}

	private JPanel getAnimationPanel(ModelView modelView, ModelStructureChangeListener structureChangeListener) {
		final JPanel animationPanel = new JPanel(new MigLayout("fill", "[]", "[grow][]"));

		JScrollPane animationScrollPane = new JScrollPane(animationBox);
		animationScrollPane.setPreferredSize(new Dimension(500, 320));
		animationPanel.add(animationScrollPane, "spanx, growx, growy, wrap");

		JPanel buttonPanel = new JPanel(new MigLayout("ins 0"));
		final JButton createAnimation = new JButton("Create");
		createAnimation.addActionListener(e -> createAnimation(modelView, structureChangeListener));
		buttonPanel.add(createAnimation);

		final JButton duplicateAnimation = new JButton("Duplicate");
		duplicateAnimation.addActionListener(e -> duplicateAnimation(modelView, structureChangeListener));
		buttonPanel.add(duplicateAnimation);


		final JButton editAnimation = new JButton("Edit");
		editAnimation.addActionListener(e -> editAnimation(modelView, structureChangeListener));
		buttonPanel.add(editAnimation);

		final JButton deleteAnimation = new JButton("Delete");
		deleteAnimation.addActionListener(e -> deleteAnimation(modelView, structureChangeListener));
		buttonPanel.add(deleteAnimation);
		animationPanel.add(buttonPanel);
		return animationPanel;
	}

	private JPanel getGlobSeqPanel(ModelView modelView) {
		final JPanel globSeqPanel = new JPanel(new MigLayout("fill"));

		globalSeqs = new IterableListModel<>();
		if (modelView != null) {
			for (final Integer animation : modelView.getModel().getGlobalSeqs()) {
				globalSeqs.addElement(animation);
			}
		}

		globalSeqBox = new JList<>(globalSeqs);
		JScrollPane globalSeqScrollPane = new JScrollPane(globalSeqBox);
		globalSeqScrollPane.setPreferredSize(new Dimension(500, 320));
		globSeqPanel.add(globalSeqScrollPane, "spanx, growx, growy");

		final JButton createGlobalSeq = new JButton("Create");
		createGlobalSeq.addActionListener(e -> createGlobalSeq(modelView));

		final JButton deleteGlobalSeq = new JButton("Delete");
		deleteGlobalSeq.addActionListener(e -> deleteGlobalSeq(modelView));

		globSeqPanel.add(createGlobalSeq);
		globSeqPanel.add(deleteGlobalSeq);
		return globSeqPanel;
	}

	private JPanel getCustomTimePanel(ModelView modelView) {
		TimeBoundProvider timeBound = getTimeBound(modelView);
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

	private TimeBoundProvider getTimeBound(ModelView modelView) {
		RenderModel editorRenderModel = modelView.getEditorRenderModel();
		if (editorRenderModel != null) {
			AnimatedRenderEnvironment renderEnvironment = editorRenderModel.getAnimatedRenderEnvironment();
			if (renderEnvironment != null) {
				return renderEnvironment.getCurrentAnimation();
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
				timeEnvironmentImpl.setBounds(selectedAnimation);
			}
		} else if (tabs.getSelectedIndex() == 1) {
			timeEnvironmentImpl.setBounds(((Integer) timeStart.getValue()), ((Integer) timeEnd.getValue()));
		} else if (tabs.getSelectedIndex() == 2) {
			final Integer selectedValue = globalSeqBox.getSelectedValue();
			if (selectedValue != null) {
				timeEnvironmentImpl.setGlobalSeq(selectedValue);
			} else {
				JOptionPane.showMessageDialog(this, "You didn't select a global sequence!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void deleteAnimation(ModelView modelView, ModelStructureChangeListener structureChangeListener) {
		final int result = JOptionPane.showConfirmDialog(TimeBoundChooserPanel.this, "Also delete keyframes?",
				"Delete Animation(s)", JOptionPane.YES_NO_CANCEL_OPTION);
		final List<Animation> selectedValues = animationBox.getSelectedValuesList();
		if (result == JOptionPane.YES_OPTION) {
			// del keys
			for (Animation animation : selectedValues) {
				animation.clearData(modelView.getModel().getAllAnimFlags(), modelView.getModel().getEvents());
			}
//			selectedValue.clearData(modelView.getModel().getAllAnimFlags(), modelView.getModel().getEvents());
		}
		if (result != JOptionPane.CANCEL_OPTION) {
			// del anim
			for (Animation animation : selectedValues) {
				modelView.getModel().remove(animation);
				animations.removeElement(animation);
				structureChangeListener.animationsRemoved(Collections.singletonList(animation));
			}
		}
	}

	private void deleteGlobalSeq(ModelView modelView) {
		final int result = JOptionPane.showConfirmDialog(TimeBoundChooserPanel.this,
				"Also delete linked timelines and their keyframes?", "Delete Animation",
				JOptionPane.YES_NO_CANCEL_OPTION);
		final Integer selectedValue = globalSeqBox.getSelectedValue();
		if (result == JOptionPane.YES_OPTION) {
			// del keys
			modelView.getModel().removeAllTimelinesForGlobalSeq(selectedValue);
		}
		if (result != JOptionPane.CANCEL_OPTION) {
			// del anim
			modelView.getModel().getGlobalSeqs().remove(selectedValue);
			globalSeqs.removeElement(selectedValue);
		}
	}

	private void createGlobalSeq(ModelView modelView) {
		final SpinnerNumberModel sModel = new SpinnerNumberModel(1000, 1, Integer.MAX_VALUE, 1);
		final JSpinner spinner = new JSpinner(sModel);
		final int userChoice = JOptionPane.showConfirmDialog(TimeBoundChooserPanel.this, spinner,
				"Enter Length", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (userChoice != JOptionPane.OK_OPTION) {
			return;
		}
		if (globalSeqs.contains(spinner.getValue())) {
			JOptionPane.showMessageDialog(TimeBoundChooserPanel.this,
					"A Global Sequence with that length already exists." +
							"\nThis program does not support multiple Global Sequences of the same length." +
							"\nInstead, simply add animation data to the sequence of that length which already exists.",
					"Error", JOptionPane.ERROR_MESSAGE);
		} else {
			globalSeqs.addElement((Integer) spinner.getValue());
			modelView.getModel().add((Integer) spinner.getValue());
		}
	}

	private void createAnimation(ModelView modelView, ModelStructureChangeListener structureChangeListener) {
		final JPanel createAnimQuestionPanel = new JPanel(new MigLayout());
		final JSpinner newAnimLength = new JSpinner(new SpinnerNumberModel(1000, 0, Integer.MAX_VALUE, 1));
		EditableModel model = modelView.getModel();
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
			final Animation newAnimation = new Animation(nameField.getText(),
					(Integer) newAnimTimeStart.getValue(),
					(Integer) newAnimTimeEnd.getValue());
			final int rarityValue = (Integer) rarityChooser.getValue();
			final int moveValue = (Integer) moveSpeedChooser.getValue();
			model.add(newAnimation);
			structureChangeListener.animationsAdded(Collections.singletonList(newAnimation));
			animations.addElement(newAnimation);
			if (rarityValue != 0) {
				newAnimation.setRarity(rarityValue);
			}
			if (moveValue != 0) {
				newAnimation.setMoveSpeed(moveValue);
			}
			if (nonLoopingChooser.isSelected()) {
				newAnimation.setNonLooping(true);
			}
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

	private void editAnimation(ModelView modelView, ModelStructureChangeListener structureChangeListener) {
		final Animation selectedValue = animationBox.getSelectedValue();
		if (selectedValue != null) {
			createAnimation(modelView, structureChangeListener);
		}
	}

	private void duplicateAnimation(ModelView modelView, ModelStructureChangeListener structureChangeListener) {
		final Animation selectedAnimation = animationBox.getSelectedValue();
//		final List<Animation> selectedValues = animationBox.getSelectedValuesList();
		final String userChosenName = JOptionPane.showInputDialog(TimeBoundChooserPanel.this,
				"Choose new animation name:", selectedAnimation.getName() + " Second");
		if (userChosenName != null) {
//			for (Animation animation : selectedValues){
//			}
			final Animation copyAnimation = new Animation(selectedAnimation);
			EditableModel model = modelView.getModel();
			final Animation lastAnim = model.getAnim(model.getAnimsSize() - 1);
			copyAnimation.setInterval(lastAnim.getEnd() + 300, lastAnim.getEnd() + 300 + selectedAnimation.length());
			copyAnimation.setName(userChosenName);
			model.add(copyAnimation);
			selectedAnimation.copyToInterval(copyAnimation.getStart(), copyAnimation.getEnd(), model.getAllAnimFlags(), model.getEvents());

			animations.addElement(copyAnimation);
			structureChangeListener.animationsAdded(Collections.singletonList(copyAnimation));
		}
	}
}
