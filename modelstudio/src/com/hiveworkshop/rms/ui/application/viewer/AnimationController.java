package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.util.List;

public class AnimationController extends JPanel {
	private ModelHandler modelHandler;
	private TimeEnvironmentImpl renderEnv;
	private final DefaultComboBoxModel<Animation> animations = new DefaultComboBoxModel<>();
	private final JComboBox<Animation> animationBox;
	private final JSlider speedSlider;
	private final SmartButtonGroup loopTypePanel;
	private boolean allowUnanimated = true;

	public AnimationController(PreviewPanel previewPanel) {
		super(new MigLayout("fillx"));

		animationBox = getAnimationChooser(animations);
		add(animationBox, "wrap, w 90%:90%:90%, gapbottom 16");

		JButton playAnimationButton = new JButton("Play Animation");
		playAnimationButton.addActionListener(e -> playAnimation());
		add(playAnimationButton, "wrap, gapbottom 16");


		loopTypePanel = getLoopTypePanel();
		add(loopTypePanel.getButtonPanel(), "wrap");

		speedSlider = getSpeedSlider();
		add(speedSlider, "wrap, w 90%:90%:90%, gapbottom 16");

		add(new JLabel("Level of Detail"), "wrap");
		add(getLodSpinner(previewPanel), "wrap, w 90%:90%:90%");

//		setLoopType(LoopType.DEFAULT_LOOP);
	}

	public AnimationController setModel(ModelHandler modelHandler, boolean allowUnanimated, Animation defaultAnimation) {
		System.out.println("AnimationController#setModel");
		this.modelHandler = modelHandler;
		if (modelHandler != null) {
			renderEnv = modelHandler.getPreviewTimeEnv();
			renderEnv.setAnimationSpeed(speedSlider.getValue() / 50f);
			loopTypePanel.setSelectedIndex(loopTypePanel.getSelectedIndex());
		}
		updateAnimationList(modelHandler, allowUnanimated, animations);
		animationBox.setSelectedItem(defaultAnimation);
		return this;
	}

	private JSpinner getLodSpinner(PreviewPanel listener) {
		JSpinner levelOfDetailSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
		levelOfDetailSpinner.addChangeListener(e -> listener.setLevelOfDetail(((Number) levelOfDetailSpinner.getValue()).intValue()));
		levelOfDetailSpinner.setMaximumSize(new Dimension(99999, 25));
		return levelOfDetailSpinner;
	}

	private JSlider getSpeedSlider() {
		JSlider speedSlider = new JSlider(0, 100, 50);
		JLabel speedSliderLabel = new JLabel("Speed: 100%");
		add(speedSliderLabel, "wrap");
		speedSlider.addChangeListener(e -> changeAnimationSpeed(speedSlider, speedSliderLabel));
		return speedSlider;
	}

	private SmartButtonGroup getLoopTypePanel() {
		SmartButtonGroup loopTypeGroup = new SmartButtonGroup();
		loopTypeGroup.addJRadioButton("Default Loop", e -> setLoopType(LoopType.DEFAULT_LOOP));
		loopTypeGroup.addJRadioButton("Always Loop", e -> setLoopType(LoopType.ALWAYS_LOOP));
		loopTypeGroup.addJRadioButton("Never Loop", e -> setLoopType(LoopType.NEVER_LOOP));
		loopTypeGroup.setSelectedIndex(0);
		return loopTypeGroup;
	}

	public JComboBox<Animation> getAnimationChooser(DefaultComboBoxModel<Animation> animations) {
		JComboBox<Animation> animationBox = new JComboBox<>(animations);
		// this prototype is to work around a weird bug where some components take forever to load
		animationBox.setPrototypeDisplayValue(new Animation("Stand and work for me", 0, 1));
		animationBox.setRenderer(getComboBoxRenderer());
		animationBox.addActionListener(e -> playSelectedAnimation());

		animationBox.setMaximumSize(new Dimension(99999999, 35));
		animationBox.setFocusable(true);
		animationBox.addMouseWheelListener(this::changeAnimation);
		return animationBox;
	}

	private void updateAnimationList(ModelHandler modelHandler, boolean allowUnanimated, DefaultComboBoxModel<Animation> animations) {
		System.out.println("AnimationController#updateAnimationList");
		animations.removeAllElements();
		if (modelHandler != null) {
			if (allowUnanimated || (modelHandler.getModel().getAnims().size() == 0)) {
				animations.addElement(null);
			}
			for (Animation animation : modelHandler.getModel().getAnims()) {
				animations.addElement(animation);
			}
		}
	}

	private BasicComboBoxRenderer getComboBoxRenderer() {
		return new BasicComboBoxRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index,
			                                              boolean isSelected, boolean cellHasFocus) {
				Object display = value == null ? "(Unanimated)" : value;
				if (value != null && modelHandler != null) {
					display = "(" + modelHandler.getModel().getAnims().indexOf(value) + ") " + display;
				}
				return super.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
			}
		};
	}

	public void playSelectedAnimation() {
		if (renderEnv != null) {
			renderEnv.setSequence((Animation) animationBox.getSelectedItem());
			renderEnv.setRelativeAnimationTime(0);
			renderEnv.setLive(true);
		}
	}

	public void playAnimation() {
		if (renderEnv != null) {
			renderEnv.setRelativeAnimationTime(0);
			renderEnv.setLive(true);
		}
	}

	public void changeAnimation(MouseWheelEvent e) {
		int wheelRotation = e.getWheelRotation();
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

	public void changeAnimationSpeed(JSlider speedSlider, JLabel speedSliderLabel) {
		speedSliderLabel.setText("Speed: " + (speedSlider.getValue() * 2) + "%");
//		previewPanel.setSpeed(speedSlider.getValue() / 50f);

		if (renderEnv != null) {
			renderEnv.setAnimationSpeed(speedSlider.getValue() / 50f);
		}
	}

	public void setLoopType(LoopType loopType) {
		if (renderEnv != null) {
			switch (loopType) {
				case ALWAYS_LOOP -> renderEnv.setLoopType(LoopType.ALWAYS_LOOP);
				case NEVER_LOOP -> renderEnv.setLoopType(LoopType.NEVER_LOOP);
				case DEFAULT_LOOP -> renderEnv.setLoopType(LoopType.DEFAULT_LOOP);
			}
		}
	}

	public AnimationController reload() {
		Animation selectedItem = (Animation) animationBox.getSelectedItem();
		animations.removeAllElements();
		if (modelHandler != null) {
			List<Animation> anims = modelHandler.getModel().getAnims();
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
		return this;
	}
}
