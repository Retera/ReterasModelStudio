package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.util.List;

public class AnimationController extends JPanel {
	private ModelHandler modelHandler;
	private TimeEnvironmentImpl renderEnv;
	private final TwiComboBox<Animation> animationBox;
	private final JSlider speedSlider;
	private final SmartButtonGroup loopTypePanel;
	private boolean allowUnanimated = true;

	public AnimationController(PreviewPanel previewPanel) {
		super(new MigLayout("fillx"));

		animationBox = getAnimationChooser();
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
	}

	public AnimationController setModel(ModelHandler modelHandler, boolean allowUnanimated, Animation defaultAnimation) {
		this.modelHandler = modelHandler;
		if (modelHandler != null) {
			renderEnv = modelHandler.getPreviewTimeEnv();
			renderEnv.setAnimationSpeed(speedSlider.getValue() / 50f);
			loopTypePanel.setSelectedIndex(loopTypePanel.getSelectedIndex());
		}
		updateAnimationList(modelHandler, allowUnanimated);
		System.out.println("defaultAnimation: " + defaultAnimation);
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

	private TwiComboBox<Animation> getAnimationChooser() {
		TwiComboBox<Animation> animationBox = new TwiComboBox<>(new Animation("Stand and work for me", 0, 1));
		animationBox.setStringFunctionRender(this::getDisplayString);
		animationBox.addActionListener(e -> playSelectedAnimation());

		animationBox.setMaximumSize(new Dimension(99999999, 35));
		animationBox.setFocusable(true);
		animationBox.addMouseWheelListener(this::changeAnimation);
		return animationBox;
	}

	private String getDisplayString(Object value) {
		if (value instanceof Animation && modelHandler != null) {
			return "(" + modelHandler.getModel().getAnims().indexOf(value) + ") " + value;
		} else if (modelHandler != null){
			return "(Unanimated)";
		}
		return "";
	}

	public void playSelectedAnimation() {
		if (renderEnv != null) {
			renderEnv.setSequence((Animation) animationBox.getSelectedItem());
			playAnimation();
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
		int newIndex = (animationBox.getItemCount() + previousSelectedIndex + wheelRotation) % animationBox.getItemCount();

		if (newIndex != previousSelectedIndex) {
			animationBox.setSelectedIndex(newIndex);
		}
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

	private void updateAnimationList(ModelHandler modelHandler, boolean allowUnanimated) {
		animationBox.removeAllItems();
		if (modelHandler != null) {
			List<Animation> anims = modelHandler.getModel().getAnims();
			if (allowUnanimated || (anims.size() == 0)) {
				animationBox.addItem(null);
			}
			animationBox.addAll(anims);
		}
	}

	public AnimationController reload() {
		Animation selectedItem = (Animation) animationBox.getSelectedItem();

		updateAnimationList(modelHandler, allowUnanimated);
		if (modelHandler != null && (selectedItem != null || allowUnanimated)) {
			animationBox.selectOrFirst(selectedItem);
		}
		return this;
	}
}
