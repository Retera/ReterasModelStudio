package com.hiveworkshop.wc3.gui.animedit;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

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

	public TimeBoundChooserPanel(final ModelView modelView) {
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
		animationTimeButton.addChangeListener(buttonStatesChangeListener);
		customTimeButton = new JRadioButton("Custom Time");
		customTimeButton.addChangeListener(buttonStatesChangeListener);
		globalSequencesButton = new JRadioButton("Global Sequence");
		globalSequencesButton.addChangeListener(buttonStatesChangeListener);
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

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(16)
				.addGroup(layout.createParallelGroup().addComponent(animationTimeButton)
						.addComponent(animationScrollPane).addComponent(customTimeButton)
						.addGroup(layout.createSequentialGroup().addComponent(startLabel).addComponent(timeStart)
								.addComponent(endLabel).addComponent(timeEnd))
						.addComponent(globalSequencesButton).addComponent(globalSeqScrollPane))
				.addGap(16));
		layout.setVerticalGroup(layout.createSequentialGroup().addGap(16).addComponent(animationTimeButton)
				.addComponent(animationScrollPane).addComponent(customTimeButton)
				.addGroup(layout.createParallelGroup().addComponent(startLabel).addComponent(timeStart)
						.addComponent(endLabel).addComponent(timeEnd))
				.addComponent(globalSequencesButton).addComponent(globalSeqScrollPane).addGap(16));
		setLayout(layout);

		animationTimeButton.doClick();
	}

	public void applyTo(final TimeEnvironmentImpl timeEnvironmentImpl) {
		if (animationTimeButton.isSelected()) {
			final Animation selectedAnimation = animationBox.getSelectedValue();
			if (selectedAnimation != null) {
				timeEnvironmentImpl.setBounds(selectedAnimation.getStart(), selectedAnimation.getEnd());
			}
		} else if (customTimeButton.isSelected()) {
			timeEnvironmentImpl.setBounds(((Number) timeStart.getValue()).intValue(),
					((Number) timeEnd.getValue()).intValue());
		} else if (globalSequencesButton.isSelected()) {
			timeEnvironmentImpl.setGlobalSeq(globalSeqBox.getSelectedValue());
		}
	}
}
