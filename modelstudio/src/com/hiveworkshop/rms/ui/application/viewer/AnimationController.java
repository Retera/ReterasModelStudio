package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.model.nodepanels.AnimationChooser;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class AnimationController extends JPanel {
	private TimeEnvironmentImpl renderEnv;
	private final AnimationChooser animationChooser;
	private final JSlider speedSlider;
	private final SmartButtonGroup loopTypePanel;
	private boolean allowUnanimated = true;

	public AnimationController(Consumer<Integer> lodListener) {
		super(new MigLayout("fillx"));

		animationChooser = new AnimationChooser(true, false, true).setAllowUnanimated(true);
		add(animationChooser, "wrap, w 90%:90%:90%, gapbottom 16");

		JButton playAnimationButton = new JButton("Play Animation");
		playAnimationButton.addActionListener(e -> animationChooser.playAnimation());
		add(playAnimationButton, "wrap, gapbottom 16");

		loopTypePanel = getLoopTypePanel();
		add(loopTypePanel.getButtonPanel(), "wrap");

		speedSlider = getSpeedSlider();
		add(speedSlider, "wrap, w 90%:90%:90%, gapbottom 16");

		add(new JLabel("Level of Detail"), "wrap");
		add(getLodSpinner(lodListener), "wrap, w 90%:90%:90%");
	}

	public AnimationController setModel(ModelHandler modelHandler, boolean allowUnanimated, Animation defaultAnimation) {
		this.allowUnanimated = allowUnanimated;
		if (modelHandler != null) {
			animationChooser.setModel(modelHandler.getModel(), modelHandler.getPreviewRenderModel());
			renderEnv = modelHandler.getPreviewRenderModel().getTimeEnvironment();
			renderEnv.setAnimationSpeed(speedSlider.getValue() / 50f);
			loopTypePanel.setSelectedIndex(loopTypePanel.getSelectedIndex());
		} else {
			animationChooser.setModel(null, null);
		}
		System.out.println("defaultAnimation: " + defaultAnimation);
		animationChooser.chooseSequence(defaultAnimation);
		return this;
	}
	public AnimationController setModel(RenderModel renderModel, Animation defaultAnimation, boolean allowUnanimated) {
		this.allowUnanimated = allowUnanimated;
		if (renderModel != null) {
			animationChooser.setModel(renderModel);
			renderEnv = renderModel.getTimeEnvironment();
			renderEnv.setAnimationSpeed(speedSlider.getValue() / 50f);
			loopTypePanel.setSelectedIndex(loopTypePanel.getSelectedIndex());
		} else {
			animationChooser.setModel(null, null);
		}
		System.out.println("defaultAnimation: " + defaultAnimation);
		animationChooser.chooseSequence(defaultAnimation);
		return this;
	}

	private JSpinner getLodSpinner(Consumer<Integer> lodListener) {
		JSpinner levelOfDetailSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
		levelOfDetailSpinner.addChangeListener(e -> lodListener.accept(((Number) levelOfDetailSpinner.getValue()).intValue()));
		levelOfDetailSpinner.setMaximumSize(new Dimension(99999, 25));
		return levelOfDetailSpinner;
	}

	private JSlider getSpeedSlider() {
		JSlider speedSlider = new JSlider(0, 100, 50);
		JLabel speedSliderLabel = new JLabel("Speed: 100%");
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

	public void changeAnimationSpeed(JSlider speedSlider, JLabel speedSliderLabel) {
		speedSliderLabel.setText("Speed: " + (speedSlider.getValue() * 2) + "%");

		if (renderEnv != null) {
			renderEnv.setAnimationSpeed(speedSlider.getValue() / 50f);
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
