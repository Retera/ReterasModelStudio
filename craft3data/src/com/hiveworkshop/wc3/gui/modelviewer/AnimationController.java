package com.hiveworkshop.wc3.gui.modelviewer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import com.hiveworkshop.wc3.gui.modelviewer.AnimationControllerListener.LoopType;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class AnimationController extends JPanel {
	private ModelView mdlDisp;
	private final DefaultComboBoxModel<Animation> animations;
	private final JComboBox<Animation> animationBox;
	private final boolean allowUnanimated;

	public AnimationController(final ModelView mdlDisp, final boolean allowUnanimated,
			final AnimationControllerListener listener) {
		this.mdlDisp = mdlDisp;
		this.allowUnanimated = allowUnanimated;
		final GroupLayout groupLayout = new GroupLayout(this);

		animations = new DefaultComboBoxModel<>();
		if (allowUnanimated || mdlDisp.getModel().getAnims().size() == 0) {
			animations.addElement(null);
		}
		for (final Animation animation : mdlDisp.getModel().getAnims()) {
			animations.addElement(animation);
		}
		animationBox = new JComboBox<>(animations);
		animationBox.setRenderer(new BasicComboBoxRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList list, final Object value, final int index,
					final boolean isSelected, final boolean cellHasFocus) {
				Object display = value == null ? "(Unanimated)" : value;
				if (value != null) {
					display = "(" + mdlDisp.getModel().getAnims().indexOf(value) + ") " + display;
				}
				return super.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
			}
		});
		animationBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				listener.setAnimation((Animation) animationBox.getSelectedItem());
				listener.playAnimation();
			}
		});
		animationBox.setMaximumSize(new Dimension(99999999, 35));
		animationBox.setFocusable(true);
		animationBox.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				final int wheelRotation = e.getWheelRotation();
				int previousSelectedIndex = animationBox.getSelectedIndex();
				if (previousSelectedIndex < 0) {
					previousSelectedIndex = 0;
				}
				int newIndex = previousSelectedIndex + wheelRotation;
				if (newIndex > animations.getSize() - 1) {
					newIndex = animations.getSize() - 1;
				} else if (newIndex < 0) {
					newIndex = 0;
				}
				if (newIndex != previousSelectedIndex) {
					animationBox.setSelectedIndex(newIndex);
					// animationBox.setSelectedIndex(
					// ((newIndex % animations.getSize()) + animations.getSize()) %
					// animations.getSize());
				}
			}
		});
		final JSlider speedSlider = new JSlider(0, 100, 50);
		final JLabel speedSliderLabel = new JLabel("Speed: 100%");
		speedSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				speedSliderLabel.setText("Speed: " + speedSlider.getValue() * 2 + "%");
				listener.setSpeed(speedSlider.getValue() / 50f);
			}
		});

		final JButton playAnimationButton = new JButton("Play Animation");
		final ActionListener playAnimationActionListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				listener.playAnimation();
			}
		};
		playAnimationButton.addActionListener(playAnimationActionListener);

		final JRadioButton defaultLoopButton = new JRadioButton("Default Loop");
		final JRadioButton alwaysLoopButton = new JRadioButton("Always Loop");
		final JRadioButton neverLoopButton = new JRadioButton("Never Loop");

		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(defaultLoopButton);
		buttonGroup.add(alwaysLoopButton);
		buttonGroup.add(neverLoopButton);
		final ActionListener setLoopTypeActionListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				AnimationControllerListener.LoopType loopType;
				if (defaultLoopButton.isSelected()) {
					loopType = LoopType.DEFAULT_LOOP;
				} else if (alwaysLoopButton.isSelected()) {
					loopType = LoopType.ALWAYS_LOOP;
				} else if (neverLoopButton.isSelected()) {
					loopType = LoopType.NEVER_LOOP;
				} else {
					throw new IllegalStateException();
				}
				listener.setLoop(loopType);
				listener.playAnimation();
			}
		};
		defaultLoopButton.addActionListener(setLoopTypeActionListener);
		alwaysLoopButton.addActionListener(setLoopTypeActionListener);
		neverLoopButton.addActionListener(setLoopTypeActionListener);

		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup().addComponent(animationBox)
				.addGroup(groupLayout.createSequentialGroup().addGap(8)
						.addGroup(groupLayout.createParallelGroup().addComponent(playAnimationButton)
								.addComponent(defaultLoopButton).addComponent(alwaysLoopButton)
								.addComponent(neverLoopButton).addComponent(speedSliderLabel).addComponent(speedSlider))
						.addGap(8)

				));
		groupLayout.setVerticalGroup(groupLayout.createSequentialGroup().addComponent(animationBox).addGap(32)
				.addComponent(playAnimationButton).addGap(16).addComponent(defaultLoopButton)
				.addComponent(alwaysLoopButton).addComponent(neverLoopButton).addGap(16).addComponent(speedSliderLabel)
				.addComponent(speedSlider)

		);
		setLayout(groupLayout);

		defaultLoopButton.doClick();
	}

	public void reload() {
		final Animation selectedItem = (Animation) animationBox.getSelectedItem();
		animations.removeAllElements();
		boolean sawLast = selectedItem == null;
		if (allowUnanimated || mdlDisp.getModel().getAnims().size() == 0) {
			animations.addElement(null);
		}
		for (final Animation animation : mdlDisp.getModel().getAnims()) {
			animations.addElement(animation);
			if (animation == selectedItem) {
				sawLast = true;
			}
		}
		if (sawLast && (selectedItem != null || allowUnanimated)) {
			animationBox.setSelectedItem(selectedItem);
		} else if (!allowUnanimated && mdlDisp.getModel().getAnims().size() > 0) {
			animationBox.setSelectedItem(mdlDisp.getModel().getAnim(0));
		}
	}

	public void setModel(final ModelView modelView) {
		this.mdlDisp = modelView;
		reload();
	}
}
