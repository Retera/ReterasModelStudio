package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class AnimationController extends JPanel {
	private ModelView mdlDisp;
	private DefaultComboBoxModel<Animation> animations;
	private JComboBox<Animation> animationBox;
	private final boolean allowUnanimated;

	public AnimationController(final ModelView mdlDisp, final boolean allowUnanimated, final AnimationControllerListener listener) {
		this(mdlDisp, allowUnanimated, listener, null);
	}

	public AnimationController(final ModelView mdlDisp, final boolean allowUnanimated,
	                           final AnimationControllerListener listener, final Animation defaultAnimation) {
		this.mdlDisp = mdlDisp;
		this.allowUnanimated = allowUnanimated;
		setLayout(new MigLayout("fillx"));

		createAnimationChooser(mdlDisp, allowUnanimated, listener);
		add(animationBox, "wrap, w 90%:90%:90%, gapbottom 16");

		final JButton playAnimationButton = new JButton("Play Animation");
		final ActionListener playAnimationActionListener = e -> listener.playAnimation();
		playAnimationButton.addActionListener(playAnimationActionListener);
		add(playAnimationButton, "wrap, gapbottom 16");

		final ButtonGroup buttonGroup = new ButtonGroup();

		final JRadioButton defaultLoopButton = new JRadioButton("Default Loop");
		defaultLoopButton.addActionListener(e -> setLoopType(listener, "default"));
		buttonGroup.add(defaultLoopButton);
		defaultLoopButton.setSelected(true);
		add(defaultLoopButton, "wrap");

		final JRadioButton alwaysLoopButton = new JRadioButton("Always Loop");
		alwaysLoopButton.addActionListener(e -> setLoopType(listener, "always"));
		buttonGroup.add(alwaysLoopButton);
		add(alwaysLoopButton, "wrap");

		final JRadioButton neverLoopButton = new JRadioButton("Never Loop");
		neverLoopButton.addActionListener(e -> setLoopType(listener, "never"));
		buttonGroup.add(neverLoopButton);
		add(neverLoopButton, "wrap, gapbottom 16");

		final JSlider speedSlider = new JSlider(0, 100, 50);
		final JLabel speedSliderLabel = new JLabel("Speed: 100%");
		add(speedSliderLabel, "wrap");
		speedSlider.addChangeListener(e -> changeAnimationSpeed(listener, speedSlider, speedSliderLabel));
		add(speedSlider, "wrap, w 90%:90%:90%, gapbottom 16");

		add(new JLabel("Level of Detail"), "wrap");
		final JSpinner levelOfDetailSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
		levelOfDetailSpinner.addChangeListener(e -> listener.setLevelOfDetail(((Number) levelOfDetailSpinner.getValue()).intValue()));
		levelOfDetailSpinner.setMaximumSize(new Dimension(99999, 25));
		add(levelOfDetailSpinner, "wrap, w 90%:90%:90%");

		animationBox.setSelectedItem(defaultAnimation);
		listener.setLoop(AnimationControllerListener.LoopType.DEFAULT_LOOP);
	}

	public void createAnimationChooser(ModelView mdlDisp, boolean allowUnanimated, AnimationControllerListener listener) {
		animations = new DefaultComboBoxModel<>();
		if (allowUnanimated || (mdlDisp.getModel().getAnims().size() == 0)) {
			animations.addElement(null);
		}
		for (final Animation animation : mdlDisp.getModel().getAnims()) {
			animations.addElement(animation);
		}
		animationBox = new JComboBox<>(animations);
		animationBox.setRenderer(getComboBoxRenderer());
		animationBox.addActionListener(e -> playSelectedAnimation(listener));

		animationBox.setMaximumSize(new Dimension(99999999, 35));
		animationBox.setFocusable(true);
		animationBox.addMouseWheelListener(this::changeAnimation);
	}

	private BasicComboBoxRenderer getComboBoxRenderer() {
		return new BasicComboBoxRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList list, final Object value, final int index,
			                                              final boolean isSelected, final boolean cellHasFocus) {
				Object display = value == null ? "(Unanimated)" : value;
				if (value != null) {
					display = "(" + mdlDisp.getModel().getAnims().indexOf(value) + ") " + display;
				}
				return super.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
			}
		};
	}

	public void playSelectedAnimation(AnimationControllerListener listener) {
		listener.setAnimation((Animation) animationBox.getSelectedItem());
		listener.playAnimation();
	}

	public void changeAnimation(java.awt.event.MouseWheelEvent e) {
		final int wheelRotation = e.getWheelRotation();
		int previousSelectedIndex = animationBox.getSelectedIndex();
		if (previousSelectedIndex < 0) {
			previousSelectedIndex = 0;
		}
		int newIndex = previousSelectedIndex + wheelRotation;
		if (newIndex > (animations.getSize() - 1)) {
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

	public void changeAnimationSpeed(AnimationControllerListener listener, JSlider speedSlider, JLabel speedSliderLabel) {
		speedSliderLabel.setText("Speed: " + (speedSlider.getValue() * 2) + "%");
		listener.setSpeed(speedSlider.getValue() / 50f);
	}

	public void setLoopType(AnimationControllerListener listener, String loopType) {
		switch (loopType) {
			case "always" -> listener.setLoop(AnimationControllerListener.LoopType.ALWAYS_LOOP);
			case "never" -> listener.setLoop(AnimationControllerListener.LoopType.NEVER_LOOP);
			default -> listener.setLoop(AnimationControllerListener.LoopType.DEFAULT_LOOP);
		}
	}

	public void reload() {
		final Animation selectedItem = (Animation) animationBox.getSelectedItem();
		animations.removeAllElements();
		List<Animation> anims = mdlDisp.getModel().getAnims();
		if (allowUnanimated || (anims.size() == 0)) {
			animations.addElement(null);
		}
		animations.addAll(anims);


		System.out.println("AC allow unanimated: " + allowUnanimated);

		if (anims.contains(selectedItem) && ((selectedItem != null) || allowUnanimated)) {
			animationBox.setSelectedItem(selectedItem);
		} else if (!allowUnanimated && (anims.size() > 0)) {
			animationBox.setSelectedItem(anims.get(0));
		}
	}

	public Animation getCurrentAnimation() {
		return (Animation) animationBox.getSelectedItem();
	}

	public void setModel(final ModelView modelView) {
		mdlDisp = modelView;
		reload();
	}
}
