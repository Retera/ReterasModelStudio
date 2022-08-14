package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.nodepanels.AnimationChooser;
import com.hiveworkshop.rms.ui.application.viewer.LoopType;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.SmartNumberSlider;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class AnimationController2 extends JPanel {
	private TimeEnvironmentImpl renderEnv;
	private final AnimationChooser animationChooser;
	private final SmartNumberSlider slider;
//	private final JSlider speedSlider;
	private final SmartButtonGroup loopTypePanel;
	private boolean allowUnanimated = true;

	public AnimationController2(Consumer<Integer> lodListener) {
		super(new MigLayout("fillx"));

		animationChooser = new AnimationChooser(true, false, true).setAllowUnanimated(true);
		add(animationChooser, "wrap, w 90%:90%:90%, gapbottom 16");

		JButton playAnimationButton = new JButton("Play Animation");
		playAnimationButton.addActionListener(e -> animationChooser.playAnimation());
		add(playAnimationButton, "wrap, gapbottom 16");

		loopTypePanel = getLoopTypePanel();
		add(loopTypePanel.getButtonPanel(), "wrap");

		slider = getSpeedSlider2();
//		speedSlider = getSpeedSlider();
//		add(speedSlider, "wrap, w 90%:90%:90%, gapbottom 16");
		add(slider, "wrap, w 90%:90%:90%, gapbottom 16");

		add(new JLabel("Level of Detail"), "wrap");
		add(getLodSpinner(lodListener), "wrap, w 90%:90%:90%");
	}

	public AnimationController2 setModel(RenderModel renderModel, Animation defaultAnimation, boolean allowUnanimated) {
		this.allowUnanimated = allowUnanimated;
		if (renderModel != null) {
			animationChooser.setModel(renderModel);
			renderEnv = renderModel.getTimeEnvironment();
			renderEnv.setAnimationSpeed(slider.getValue() / 100f);
			loopTypePanel.setSelectedIndex(loopTypePanel.getSelectedIndex());
		} else {
			animationChooser.setModel(null, null);
		}
		System.out.println("defaultAnimation: " + defaultAnimation);
		animationChooser.chooseSequence(defaultAnimation);
		return this;
	}

	private JSpinner getLodSpinner1(Consumer<Integer> lodListener) {
		JSpinner levelOfDetailSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
		levelOfDetailSpinner.addChangeListener(e -> lodListener.accept(((Number) levelOfDetailSpinner.getValue()).intValue()));
		levelOfDetailSpinner.setMaximumSize(new Dimension(99999, 25));
		return levelOfDetailSpinner;
	}
	private IntEditorJSpinner getLodSpinner(Consumer<Integer> lodListener) {
		return new IntEditorJSpinner(0, 0, 5, lodListener);
	}

	private JSlider getSpeedSlider() {
		JSlider speedSlider = new JSlider(0, 100, 50);
		JLabel speedSliderLabel = new JLabel("Speed: 100%");
		speedSlider.addChangeListener(e -> changeAnimationSpeed(speedSlider, speedSliderLabel));
		return speedSlider;
	}
	private JSlider getTimeSlider() {
		JSlider slider = new JSlider(0, 100, 0);

		slider.addChangeListener(e -> setAnimationTime(slider.getValue()));
		return slider;
	}
	private SmartNumberSlider getSpeedSlider2() {
		SmartNumberSlider slider = new SmartNumberSlider("Speed", 0, 200, 100, false, true);
		slider.setMinLowerLimit(0);
		slider.setMinUpperLimit(0);
		slider.setMaxLowerLimit(200);
		slider.setMaxUpperLimit(1000);
		slider.addValueConsumer(this::changeAnimationSpeed);
		return slider;
	}

	private SmartButtonGroup getLoopTypePanel() {
		SmartButtonGroup loopTypeGroup = new SmartButtonGroup();
		loopTypeGroup.addJRadioButton("Default Loop", e -> setLoopType(LoopType.DEFAULT_LOOP));
		loopTypeGroup.addJRadioButton("Always Loop", e -> setLoopType(LoopType.ALWAYS_LOOP));
		loopTypeGroup.addJRadioButton("Never Loop", e -> setLoopType(LoopType.NEVER_LOOP));
		loopTypeGroup.setSelectedIndex(0);
		return loopTypeGroup;
	}

	public void changeAnimationSpeed(JSlider speedSlider, JLabel speedSliderLabel) {
		speedSliderLabel.setText("Speed: " + (speedSlider.getValue() * 2) + "%");

		if (renderEnv != null) {
			renderEnv.setAnimationSpeed(speedSlider.getValue() / 50f);
		}
	}
	public void setAnimationTime(int time) {
		if (renderEnv != null && renderEnv.getEnvTrackTime() != time) {
			renderEnv.setAnimationTime(time);
		}
	}

	public void changeAnimationSpeed(int speed) {
		if (renderEnv != null) {
			renderEnv.setAnimationSpeed(speed / 100f);
		}
	}

	public void setLoopType(LoopType loopType) {
		if (renderEnv != null) {
			renderEnv.setLoopType(loopType);
		}
	}

	public AnimationController2 reload() {
		animationChooser.updateAnimationList();
		return this;
	}
}
