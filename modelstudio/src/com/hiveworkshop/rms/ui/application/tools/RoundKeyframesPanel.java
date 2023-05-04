package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.animation.RoundKeyframesAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.render3d.EmitterIdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.ActionFunction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.uiFactories.Button;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class RoundKeyframesPanel extends JPanel {
	private static final LastState transLastState = new LastState(true, 0, null, null);
	private static final LastState scaleLastState = new LastState(true, 0, null, null);
	private static final LastState rotChLastState = new LastState(true, 0, null, null);
	private static final LastState otherLastState = new LastState(false, 0, null, null);

	private static boolean onlySBool = true;
	private static boolean materBool = false;
	private static boolean textuBool = false;
	private static boolean geoseBool = false;
	private static boolean camerBool = false;
	private static boolean nodesBool = true;
	private static boolean partiBool = true;


	public RoundKeyframesPanel() {
		super(new MigLayout("fill", "[grow]", ""));

		JPanel typePanel = new JPanel(new MigLayout());
		typePanel.setBorder(BorderFactory.createTitledBorder(""));


		typePanel.add(CheckBox.create("Nodes", nodesBool, b -> nodesBool = b), "");
		typePanel.add(CheckBox.create("Materials", materBool, b -> materBool = b), "");
		typePanel.add(CheckBox.create("Cameras", camerBool, b -> camerBool = b), "wrap");
		typePanel.add(CheckBox.create("TextureAnims", textuBool, b -> textuBool = b), "wrap");
		typePanel.add(CheckBox.create("GeosetAnims", geoseBool, b -> geoseBool = b), "");
		typePanel.add(CheckBox.create("Particle", partiBool, b -> partiBool = b), "wrap");
		add(typePanel, "growx, spanx, wrap");

		add(CheckBox.create("Only selected nodes", onlySBool, b -> onlySBool = b), "wrap");

		add(new RoundSpinners("Translation", transLastState, .01), "growx, wrap");
		add(new RoundSpinners("Scaling", scaleLastState, .01), "growx, wrap");
		add(new RoundSpinners("Rotation", rotChLastState, .01), "growx, wrap");
		add(new RoundSpinners("Other", otherLastState, .01), "growx, wrap");

		add(Button.create("Round values", e -> round()), "wrap");
	}

	private void round() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		EditableModel model = modelPanel.getModel();
		ModelView modelView = modelPanel.getModelView();
		List<AnimFlag<?>> animFlags = new ArrayList<>();

		if(nodesBool){
			Collection<IdObject> idObjects;
			if(onlySBool){
				idObjects = modelView.getSelectedIdObjects();
			} else {
				idObjects = model.getIdObjects();
			}
			for (IdObject idObject : idObjects) {
				if(!(idObject instanceof EmitterIdObject) || (partiBool)){
					animFlags.addAll(idObject.getAnimFlags());
				}
			}
		}

		if(camerBool){
			Collection<Camera> cameras1;
			if(onlySBool){
				cameras1 = modelView.getSelectedCameras();
			} else {
				cameras1 = model.getCameras();
			}
			for (Camera x : cameras1) {
				animFlags.addAll(x.getSourceNode().getAnimFlags());
				animFlags.addAll(x.getTargetNode().getAnimFlags());
			}
		}
		if(materBool){
			for (Material m : model.getMaterials()) {
				for (Layer lay : m.getLayers()) {
					animFlags.addAll(lay.getAnimFlags());
				}
			}
		}
		if(textuBool){
			for (TextureAnim texa : model.getTexAnims()) {
				animFlags.addAll(texa.getAnimFlags());
			}
		}
		if(geoseBool){
			for (Geoset geoset : model.getGeosets()) {
				animFlags.addAll(geoset.getAnimFlags());
			}
		}

		if(!animFlags.isEmpty()) {

			Float trans     = getFixedFloat(transLastState.clampValue);
			Float scale     = getFixedFloat(scaleLastState.clampValue);
			Float rot       = getFixedFloat(rotChLastState.clampValue);
			Float valueDiff = getFixedFloat(otherLastState.clampValue);

			Integer transI = transLastState.magnitude;
			Integer scaleI = scaleLastState.magnitude;
			Integer rotI = rotChLastState.magnitude;
			Integer valueDiffI = otherLastState.magnitude;
			RoundKeyframesAction action = new RoundKeyframesAction(animFlags, trans, transI, scale, scaleI, rot, rotI, valueDiff, valueDiffI, ModelStructureChangeListener.changeListener);

			modelPanel.getUndoManager().pushAction(action.redo());
		}
	}

	private Float getFixedFloat(Float value){
		if(value != null && value == 0f){
			return Float.MIN_VALUE;
		}
		return value;
	}

	private static class RoundSpinners extends JPanel {
		private final SmartButtonGroup buttonGroup = new SmartButtonGroup();
		private final Consumer<Boolean> onCheckedConsumer;

		MagnitudePanel magnitudePanel;
		ClampPanel clampPanel;
		RoundSpinners(String checkboxLabel, LastState lastState, double stepSize) {
			super(new MigLayout("ins 0, fill", "[][grow][]", ""));
			setBorder(BorderFactory.createTitledBorder(checkboxLabel));
			setName("RoundPanel" + checkboxLabel);

			onCheckedConsumer = b -> lastState.isActive = b;

			magnitudePanel = new MagnitudePanel("", buttonGroup, i -> lastState.magnitude = i);
			clampPanel = new ClampPanel("", stepSize, buttonGroup, f -> lastState.clampValue = f);

			magnitudePanel.radioButton().addActionListener(e -> lastState.mode = buttonGroup.getSelectedIndex());
			clampPanel.radioButton().addActionListener(e -> lastState.mode = buttonGroup.getSelectedIndex());


//			add(CheckBox.create(null, null, lastState.isActive, this::setEnabled), "id box, pos (box.x - box.w/3) (box.y + box.h/2)");
//			String name = "RoundPanel" + checkboxLabel;
//			add(CheckBox.create(null, null, lastState.isActive, this::setEnabled), "id box, pos (" + name + ".x - box.w/3) (" + name + ".y + " + name + ".h/2)");
//			add(CheckBox.create(null, null, lastState.isActive, this::setEnabled), "id box, pos (" + name + ".x) (" + name + ".y + " + name + ".h/2)");
			add(CheckBox.create(null, null, lastState.isActive, this::setEnabled), "");
//			add(magnitudePanel, "gapleft (box.w*1.5)");

			add(magnitudePanel, "sg mag, growx, left");
			add(clampPanel, "sg clamp");
			buttonGroup.setSelectedIndex(lastState.mode);
			setEnabled(lastState.isActive);


		}

		public void setEnabled(boolean enabled) {
			if(onCheckedConsumer != null){
				onCheckedConsumer.accept(enabled);
			}
			magnitudePanel.setEnabled(enabled);
			clampPanel.setEnabled(enabled);
			repaint();
		}
	}


	private static class MagnitudePanel extends JPanel {
		private boolean isEnabled = true;
		private final JRadioButton radioButton;
		private final IntEditorJSpinner spinner;
		private final JLabel label;
		private final JLabel numberLabel;
		private Consumer<Integer> valueConsumer;

		MagnitudePanel(String checkboxLabel, SmartButtonGroup sbg, Consumer<Integer> valueConsumer) {
			super(new MigLayout("ins 0 0 0 2, ", "", ""));
			setBorder(BorderFactory.createTitledBorder(checkboxLabel));
			radioButton = sbg.addJRadioButton(null, null, this::setState);
//			radioButton = new JRadioButton();
//			radioButton.addItemListener(e -> setState(e.getStateChange() == ItemEvent.SELECTED));
			label = new JLabel("Rounding magnitude");
			numberLabel = new JLabel("00000000000");
			spinner = new IntEditorJSpinner(5, -10, 10, null);
			setIntConsumer(valueConsumer, true);

			add(radioButton, "sg sg1");
			add(label, "sg sg2");
			add(spinner, "sg sg3");
			add(numberLabel, "sg sg4, right");
			setState(false);
		}

		public JRadioButton radioButton() {
			return radioButton;
		}

		public void setEnabled(boolean enabled) {
			radioButton.setEnabled(enabled);
			spinner.setEnabled(enabled && isEnabled);
			label.setEnabled(enabled && isEnabled);
			numberLabel.setEnabled(enabled && isEnabled);
			if(valueConsumer != null){
				valueConsumer.accept((enabled && isEnabled) ? spinner.getIntValue() : null);
			}
			repaint();
		}

		public MagnitudePanel setIntConsumer(Consumer<Integer> scaleConsumer, boolean consumeOnSet){
			this.valueConsumer = scaleConsumer;
			if(scaleConsumer != null){
				spinner.addIntEditingStoppedListener(i -> {scaleConsumer.accept(isEnabled ? i : null); numberLabel.setText("" + (float)(Math.pow(10, -i)));});
				if(consumeOnSet){
					scaleConsumer.accept(isEnabled ? spinner.getIntValue() : null);
					numberLabel.setText("" + (float)(Math.pow(10, -spinner.getIntValue())));
				}
			} else {
				spinner.addIntEditingStoppedListener(null);
			}
			return this;
		}

		private void setState(boolean selected) {
			isEnabled = selected;
			spinner.setEnabled(selected);
			label.setEnabled(selected);
			numberLabel.setEnabled(selected);
			if(valueConsumer != null){
				valueConsumer.accept(isEnabled ? spinner.getIntValue() : null);
			}
			repaint();
		}
	}
	private static class ClampPanel extends JPanel {
		private boolean isEnabled = true;
		private final JRadioButton radioButton;
		private final FloatEditorJSpinner spinner;
		private final JLabel label;
		private Consumer<Float> valueConsumer;
		ClampPanel(String checkboxLabel, double stepSize, SmartButtonGroup sbg, Consumer<Float> valueConsumer) {
			super(new MigLayout("ins 0, ", "", ""));
			setBorder(BorderFactory.createTitledBorder(checkboxLabel));
			radioButton = sbg.addJRadioButton(null, null, this::setState);
//			radioButton = new JRadioButton();
//			radioButton.addItemListener(e -> setState(e.getStateChange() == ItemEvent.SELECTED));

			label = new JLabel("Set to 0 if smaller than \u00b1");
			spinner = new FloatEditorJSpinner(0f, 0f, 10000.0f, (float) stepSize, null);
			setFloatConsumer(valueConsumer, true);

			add(radioButton, "sg sg5");
			add(label, "sg sg6");
			add(spinner, "sg sg7");
			setState(false);
		}


		public JRadioButton radioButton() {
			return radioButton;
		}

		public void setEnabled(boolean enabled) {
			radioButton.setEnabled(enabled);
			spinner.setEnabled(enabled && isEnabled);
			label.setEnabled(enabled && isEnabled);
			if(valueConsumer != null){
				valueConsumer.accept((enabled && isEnabled) ? spinner.getFloatValue() : null);
			}
			repaint();
		}

		public ClampPanel setFloatConsumer(Consumer<Float> valueConsumer, boolean consumeOnSet){
			this.valueConsumer = valueConsumer;
			if(valueConsumer != null){
				spinner.setFloatEditingStoppedListener(f -> valueConsumer.accept(isEnabled ? f : null));
				if(consumeOnSet){
					valueConsumer.accept(isEnabled ? spinner.getFloatValue() : null);
				}
			} else {
				spinner.setFloatEditingStoppedListener(null);
			}
			return this;
		}

		private void setState(boolean selected) {
			isEnabled = selected;
			spinner.setEnabled(selected);
			label.setEnabled(selected);
			if(valueConsumer != null){
				valueConsumer.accept(isEnabled ? spinner.getFloatValue() : null);
			}
			repaint();
		}
	}

	private static class LastState {
		private boolean isActive;
		private int mode;
		private Float clampValue;
		private Integer magnitude;

		LastState(boolean isActive, int mode, Float clampValue, Integer magnitude){
			this.isActive = isActive;
			this.mode = mode;
			this.clampValue = clampValue;
			this.magnitude = magnitude;
		}
	}

	public static void showPopup() {
		JComponent parent = ProgramGlobals.getMainPanel();
		FramePopup.show(new RoundKeyframesPanel(), parent, "Round keyframes");
	}


	private static class RoundKeyframes extends ActionFunction {

		RoundKeyframes(){
			super(TextKey.ROUND_KEYFRAMES, () -> showPopup());
			setMenuItemMnemonic(KeyEvent.VK_K);
		}
	}


	public static JMenuItem getMenuItem(){
		return new RoundKeyframes().getMenuItem();
	}
}
