package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.nodepanels.AnimationChooser;
import com.hiveworkshop.rms.util.EnumButtonGroup;
import com.hiveworkshop.rms.util.EnumButtonGroupGroup;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.SmartNumberSlider;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class AnimationController extends JPanel {
	private TimeEnvironmentImpl renderEnv;
	private final AnimationChooser animationChooser;
	private final SmartNumberSlider speedSlider;
	private final SmartButtonGroup loopTypePanel;
	private final EnumButtonGroup<LoopType> loopTypePanel2;
	private boolean allowUnanimated = true;

	public AnimationController(Consumer<Integer> lodListener) {
		super(new MigLayout("fillx"));

		animationChooser = new AnimationChooser(true, false, true).setAllowUnanimated(true);
		add(animationChooser, "wrap, w 90%:90%:90%, gapbottom 16");

		JButton playAnimationButton = new JButton("Play Animation");
		playAnimationButton.addActionListener(e -> animationChooser.playAnimation());
		add(playAnimationButton, "wrap, gapbottom 16");

		loopTypePanel = getLoopTypePanel();
		loopTypePanel2 = new EnumButtonGroup<>(LoopType.class, EnumButtonGroupGroup.TYPE_RADIO, this::setLoopType).setSelected(LoopType.DEFAULT_LOOP);
		add(loopTypePanel2, "wrap");

		speedSlider = getSpeedSlider();
		add(speedSlider, "wrap, w 90%:90%:90%, gapbottom 16");

		add(new JLabel("Level of Detail"), "wrap");
		add(getLodSpinner(lodListener), "wrap, w 90%:90%:90%");
	}

	public AnimationController setModel(RenderModel renderModel, Animation defaultAnimation, boolean allowUnanimated) {
		this.allowUnanimated = allowUnanimated;
		if (renderModel != null) {
			animationChooser.setModel(renderModel);
			renderEnv = renderModel.getTimeEnvironment();
			renderEnv.setAnimationSpeed(speedSlider.getValue() / 100f);
			loopTypePanel.setSelectedIndex(loopTypePanel.getSelectedIndex());
			loopTypePanel2.setSelectedIndex(loopTypePanel2.getSelectedIndex());
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

	private SmartNumberSlider getSpeedSlider() {
		SmartNumberSlider slider = new SmartNumberSlider("", 0, 200, 100, false, true);
		slider.setMinLowerLimit(0);
		slider.setMinUpperLimit(0);
		slider.setMaxLowerLimit(200);
		slider.setMaxUpperLimit(1000);
//		JLabel speedSliderLabel = new JLabel("Speed: 100%");
		slider.addValueConsumer((s) -> changeAnimationSpeed(s));
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

	public void changeAnimationSpeed(Integer speed) {
//		speedSliderLabel.setText("Speed: " + (speed * 2) + "%");

		if (renderEnv != null) {
			renderEnv.setAnimationSpeed(speed / 100f);
		}
	}

	public void setLoopType(LoopType loopType) {
		if (renderEnv != null) {
			renderEnv.setLoopType(loopType);
		}
	}

	public AnimationController reload() {
		animationChooser.updateAnimationList();
		return this;
	}
}
