package com.hiveworkshop.wc3.gui.animedit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

import net.miginfocom.swing.MigLayout;

public class TimeBoundChooserPanel extends JPanel {
	private final JRadioButton animationTimeButton;
	private final JRadioButton customTimeButton;
	private final JRadioButton globalSequencesButton;
	private final JSpinner timeStart, timeEnd;
	private final DefaultListModel<Animation> animations;
	private final JList<Animation> animationBox;
	private final DefaultListModel<Integer> globalSeqs;
	private final JList<Integer> globalSeqBox;
	private final JLabel startLabel;
	private final JLabel endLabel;
	private final JScrollPane animationScrollPane;
	private final JScrollPane globalSeqScrollPane;
	private final ButtonGroup buttonGroup;
	private final JTabbedPane tabs;

	public TimeBoundChooserPanel(final ModelView modelView,
			final ModelStructureChangeListener structureChangeListener) {
		animations = new DefaultListModel<>();
		if (modelView != null) {
			for (final Animation animation : modelView.getModel().getAnims()) {
				animations.addElement(animation);
			}
		}
		animationBox = new JList<>(animations);
		animationBox.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				if (animationTimeButton.isSelected()) {
					final Animation selectedValue = animationBox.getSelectedValue();
					if (selectedValue != null) {
						timeStart.setValue(selectedValue.getStart());
						timeEnd.setValue(selectedValue.getEnd());
					}
				}
			}
		});

		globalSeqs = new DefaultListModel<>();
		if (modelView != null) {
			for (final Integer animation : modelView.getModel().getGlobalSeqs()) {
				globalSeqs.addElement(animation);
			}
		}
		globalSeqBox = new JList<>(globalSeqs);
		final ChangeListener buttonStatesChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				animationBox.setEnabled(animationTimeButton.isSelected());
				animationScrollPane.setEnabled(animationTimeButton.isSelected());
				globalSeqBox.setEnabled(globalSequencesButton.isSelected());
				globalSeqScrollPane.setEnabled(globalSequencesButton.isSelected());

				timeStart.setEnabled(customTimeButton.isSelected());
				timeEnd.setEnabled(customTimeButton.isSelected());
				startLabel.setEnabled(customTimeButton.isSelected());
				endLabel.setEnabled(customTimeButton.isSelected());
			}
		};

		animationTimeButton = new JRadioButton("Animation Sequence");
		// animationTimeButton.addChangeListener(buttonStatesChangeListener);
		customTimeButton = new JRadioButton("Custom Time");
		// customTimeButton.addChangeListener(buttonStatesChangeListener);
		globalSequencesButton = new JRadioButton("Global Sequence");
		// globalSequencesButton.addChangeListener(buttonStatesChangeListener);
		buttonGroup = new ButtonGroup();
		buttonGroup.add(animationTimeButton);
		buttonGroup.add(customTimeButton);
		buttonGroup.add(globalSequencesButton);
		timeStart = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		timeEnd = new JSpinner(new SpinnerNumberModel(1000, 0, Integer.MAX_VALUE, 1));
		startLabel = new JLabel("Start:");
		endLabel = new JLabel("End:");
		animationScrollPane = new JScrollPane(animationBox);
		globalSeqScrollPane = new JScrollPane(globalSeqBox);

		// final GroupLayout layout = new GroupLayout(this);
		// layout.setHorizontalGroup(layout.createSequentialGroup().addGap(16)
		// .addGroup(layout.createParallelGroup().addComponent(animationTimeButton)
		// .addComponent(animationScrollPane).addComponent(customTimeButton)
		// .addGroup(layout.createSequentialGroup().addComponent(startLabel).addComponent(timeStart)
		// .addComponent(endLabel).addComponent(timeEnd))
		// .addComponent(globalSequencesButton).addComponent(globalSeqScrollPane))
		// .addGap(16));
		// layout.setVerticalGroup(layout.createSequentialGroup().addGap(16).addComponent(animationTimeButton)
		// .addComponent(animationScrollPane).addComponent(customTimeButton)
		// .addGroup(layout.createParallelGroup().addComponent(startLabel).addComponent(timeStart)
		// .addComponent(endLabel).addComponent(timeEnd))
		// .addComponent(globalSequencesButton).addComponent(globalSeqScrollPane).addGap(16));
		// setLayout(layout);

		animationTimeButton.doClick();

		setLayout(new BorderLayout());
		tabs = new JTabbedPane();
		final JPanel animationPanel = new JPanel();
		animationPanel.setLayout(new MigLayout());
		animationScrollPane.setPreferredSize(new Dimension(640, 320));
		globalSeqScrollPane.setPreferredSize(new Dimension(640, 320));
		animationPanel.add(animationScrollPane, "cell 0 0 5 12");
		final JButton createAnimation = new JButton("Create");
		final JButton duplicateAnimation = new JButton("Duplicate");
		duplicateAnimation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Animation selectedAnimation = animationBox.getSelectedValue();
				final String userChosenName = JOptionPane.showInputDialog(TimeBoundChooserPanel.this,
						"Choose new animation name:", selectedAnimation.getName() + " Second");
				if (userChosenName != null) {
					final Animation copyAnimation = new Animation(selectedAnimation);
					final Animation lastAnim = modelView.getModel().getAnim(modelView.getModel().getAnimsSize() - 1);
					copyAnimation.setInterval(lastAnim.getEnd() + 300,
							lastAnim.getEnd() + 300 + selectedAnimation.length());
					copyAnimation.setName(userChosenName);
					modelView.getModel().add(copyAnimation);
					selectedAnimation.copyToInterval(copyAnimation.getStart(), copyAnimation.getEnd(),
							modelView.getModel().getAllAnimFlags(),
							modelView.getModel().sortedIdObjects(EventObject.class));
					animations.addElement(copyAnimation);
					structureChangeListener.animationsAdded(Collections.singletonList(copyAnimation));
				}
			}
		});
		final JButton deleteAnimation = new JButton("Delete");
		createAnimation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JPanel createAnimQuestionPanel = new JPanel();
				final JTextField nameField = new JTextField(24);
				final JRadioButton lengthButton = new JRadioButton("Length");
				final JSpinner newAnimLength = new JSpinner(new SpinnerNumberModel(1000, 0, Integer.MAX_VALUE, 1));
				final JRadioButton timeRangeButton = new JRadioButton("Time Range");
				final Animation lastAnimation = modelView.getModel().getAnimsSize() == 0 ? null
						: modelView.getModel().getAnim(modelView.getModel().getAnimsSize() - 1);
				final int lastAnimationEnd = lastAnimation == null ? 0 : lastAnimation.getEnd();
				final JSpinner newAnimTimeStart = new JSpinner(
						new SpinnerNumberModel(lastAnimationEnd + 300, 0, Integer.MAX_VALUE, 1));
				final JSpinner newAnimTimeEnd = new JSpinner(
						new SpinnerNumberModel(lastAnimationEnd + 1300, 0, Integer.MAX_VALUE, 1));

				newAnimLength.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(final ChangeEvent e) {
						newAnimTimeStart.setValue(lastAnimationEnd + 300);
						newAnimTimeEnd
								.setValue(lastAnimationEnd + 300 + ((Number) newAnimLength.getValue()).intValue());
					}
				});

				final ButtonGroup newAnimBtnGrp = new ButtonGroup();
				newAnimBtnGrp.add(lengthButton);
				newAnimBtnGrp.add(timeRangeButton);
				final ActionListener actions = new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						newAnimLength.setEnabled(lengthButton.isSelected());
						newAnimTimeStart.setEnabled(timeRangeButton.isSelected());
						newAnimTimeEnd.setEnabled(timeRangeButton.isSelected());
					}
				};
				lengthButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						newAnimTimeStart.setValue(lastAnimationEnd + 300);
						newAnimTimeEnd
								.setValue(lastAnimationEnd + 300 + ((Number) newAnimLength.getValue()).intValue());
						newAnimLength.setEnabled(lengthButton.isSelected());
						newAnimTimeStart.setEnabled(timeRangeButton.isSelected());
						newAnimTimeEnd.setEnabled(timeRangeButton.isSelected());
					}
				});
				timeRangeButton.addActionListener(actions);
				createAnimQuestionPanel.setLayout(new MigLayout());
				createAnimQuestionPanel.add(new JLabel("Name: "), "cell 0 0");
				createAnimQuestionPanel.add(nameField, "cell 1 0");
				createAnimQuestionPanel.add(lengthButton, "cell 0 1");
				createAnimQuestionPanel.add(new JLabel("Length: "), "cell 0 2");
				createAnimQuestionPanel.add(newAnimLength, "cell 1 2");
				createAnimQuestionPanel.add(timeRangeButton, "cell 0 3");
				createAnimQuestionPanel.add(new JLabel("Start: "), "cell 0 4");
				createAnimQuestionPanel.add(newAnimTimeStart, "cell 1 4");
				createAnimQuestionPanel.add(new JLabel("End: "), "cell 2 4");
				createAnimQuestionPanel.add(newAnimTimeEnd, "cell 3 4");
				final JPanel extraProperties = new JPanel();
				createAnimQuestionPanel.add(extraProperties, "cell 0 5");

				final JSpinner rarityChooser = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
				final JSpinner moveSpeedChooser = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
				extraProperties.setBorder(BorderFactory.createTitledBorder("Misc"));
				extraProperties.setLayout(new MigLayout());
				extraProperties.add(new JCheckBox("NonLooping"), "cell 0 0");
				extraProperties.add(new JLabel("Rarity"), "cell 0 1");
				extraProperties.add(rarityChooser, "cell 1 1");
				extraProperties.add(new JLabel("MoveSpeed"), "cell 0 2");
				extraProperties.add(moveSpeedChooser, "cell 1 2");

				lengthButton.doClick();
				final int result = JOptionPane.showConfirmDialog(TimeBoundChooserPanel.this, createAnimQuestionPanel,
						"Create Animation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (result == JOptionPane.OK_OPTION) {
					final Animation newAnimation = new Animation(nameField.getText(),
							((Number) newAnimTimeStart.getValue()).intValue(),
							((Number) newAnimTimeEnd.getValue()).intValue());
					final int rarityValue = ((Number) rarityChooser.getValue()).intValue();
					final int moveValue = ((Number) moveSpeedChooser.getValue()).intValue();
					modelView.getModel().add(newAnimation);
					structureChangeListener.animationsAdded(Collections.singletonList(newAnimation));
					animations.addElement(newAnimation);
					if (rarityValue != 0) {
						newAnimation.addTag("Rarity " + rarityValue);
					}
					if (moveValue != 0) {
						newAnimation.addTag("MoveSpeed " + moveValue);
					}
				}

			}
		});
		deleteAnimation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final int result = JOptionPane.showConfirmDialog(TimeBoundChooserPanel.this, "Also delete keyframes?",
						"Delete Animation", JOptionPane.YES_NO_CANCEL_OPTION);
				final Animation selectedValue = animationBox.getSelectedValue();
				if (result == JOptionPane.YES_OPTION) {
					// del keys
					selectedValue.clearData(modelView.getModel().getAllAnimFlags(),
							modelView.getModel().sortedIdObjects(EventObject.class));
				}
				if (result != JOptionPane.CANCEL_OPTION) {
					// del anim
					modelView.getModel().remove(selectedValue);
					animations.removeElement(selectedValue);
					structureChangeListener.animationsRemoved(Collections.singletonList(selectedValue));
				}
			}
		});
		animationPanel.add(createAnimation, "cell 0 12");
		animationPanel.add(duplicateAnimation, "cell 1 12");
		animationPanel.add(deleteAnimation, "cell 2 12");
		tabs.addTab("Animation", animationPanel);
		final JPanel customTimePanel = new JPanel(new MigLayout());
		customTimePanel.add(startLabel, "cell 0 0");
		customTimePanel.add(timeStart, "cell 1 0");
		customTimePanel.add(endLabel, "cell 2 0");
		customTimePanel.add(timeEnd, "cell 3 0");
		tabs.addTab("Custom Time", customTimePanel);
		final JPanel globSeq = new JPanel(new MigLayout());
		final JButton createGlobalSeq = new JButton("Create");
		createGlobalSeq.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final SpinnerNumberModel sModel = new SpinnerNumberModel(1000, 1, Integer.MAX_VALUE, 1);
				final JSpinner spinner = new JSpinner(sModel);
				final int userChoice = JOptionPane.showConfirmDialog(TimeBoundChooserPanel.this, spinner,
						"Enter Length", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (userChoice != JOptionPane.OK_OPTION) {
					return;
				}
				if (globalSeqs.contains(spinner.getValue())) {
					JOptionPane.showMessageDialog(TimeBoundChooserPanel.this,
							"A Global Sequence with that length already exists.\nThis program does not support multiple Global Sequences of the same length.\nInstead, simply add animation data to the sequence of that length which already exists.",
							"Error", JOptionPane.ERROR_MESSAGE);
				} else {
					globalSeqs.addElement((Integer) spinner.getValue());
					modelView.getModel().add((Integer) spinner.getValue());
				}
			}
		});
		final JButton deleteGlobalSeq = new JButton("Delete");
		deleteGlobalSeq.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
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
		});
		globSeq.add(globalSeqScrollPane, "cell 0 0 2 1");
		globSeq.add(createGlobalSeq, "cell 0 1");
		globSeq.add(deleteGlobalSeq, "cell 1 1");
		tabs.addTab("Global Sequence", globSeq);
		add(tabs);
		setPreferredSize(new Dimension(640, 480));
	}

	public void applyTo(final TimeEnvironmentImpl timeEnvironmentImpl) {
		if (tabs.getSelectedIndex() == 0) {
			final Animation selectedAnimation = animationBox.getSelectedValue();
			if (selectedAnimation != null) {
				timeEnvironmentImpl.setBounds(selectedAnimation.getStart(), selectedAnimation.getEnd());
			}
		} else if (tabs.getSelectedIndex() == 1) {
			timeEnvironmentImpl.setBounds(((Number) timeStart.getValue()).intValue(),
					((Number) timeEnd.getValue()).intValue());
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
}
