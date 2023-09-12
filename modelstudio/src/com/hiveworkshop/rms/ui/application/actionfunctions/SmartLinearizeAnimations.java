package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.SmartLinearizeAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.render3d.EmitterIdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class SmartLinearizeAnimations extends ActionFunction {

	public SmartLinearizeAnimations() {
		super(TextKey.LINEARIZE_ANIMATIONS, SmartLinearizeAnimations::linearizeAnimations);
	}

	public static void linearizeAnimations(ModelHandler modelHandler) {
		SmartLAPanel2 smartLAPanel = new SmartLAPanel2();
		int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(),
				smartLAPanel,
				"Linearize Animations", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (x == JOptionPane.OK_OPTION) {
			List<AnimFlag<?>> allAnimFlags = smartLAPanel.getAnimFlags(modelHandler);

			List<UndoAction> interpTypActions = new ArrayList<>();
			for (AnimFlag<?> flag : allAnimFlags) {
				if (flag.tans()) {
					if (flag.getName().equals(MdlUtils.TOKEN_TRANSLATION) && SmartLAPanel2.transLastState.isActive) {
						interpTypActions.add(new SmartLinearizeAction<>(flag, SmartLAPanel2.transLastState.getAllowedDiff(), null));
					} else if (flag.getName().equals(MdlUtils.TOKEN_ROTATION) && SmartLAPanel2.rotChLastState.isActive) {
						interpTypActions.add(new SmartLinearizeAction<>(flag, SmartLAPanel2.rotChLastState.getAllowedDiff(), null));
					} else if (flag.getName().equals(MdlUtils.TOKEN_SCALING) && SmartLAPanel2.scaleLastState.isActive) {
						interpTypActions.add(new SmartLinearizeAction<>(flag, SmartLAPanel2.scaleLastState.getAllowedDiff(), null));
					} else if (SmartLAPanel2.otherLastState.isActive) {
						interpTypActions.add(new SmartLinearizeAction<>(flag, SmartLAPanel2.otherLastState.getAllowedDiff(), null));
					}
				}
			}

			UndoAction action = new CompoundAction("Liniarize Animations", interpTypActions, ModelStructureChangeListener.changeListener::animationParamsChanged);
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}



//	public static void linearizeAnimations2(ModelHandler modelHandler) {
//		SmartLAPanel smartLAPanel = new SmartLAPanel();
//		int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(),
//				smartLAPanel,
//				"Warning: Linearize Animations", JOptionPane.OK_CANCEL_OPTION);
//		if (x == JOptionPane.OK_OPTION) {
//			List<UndoAction> interpTypActions = new ArrayList<>();
//			if (SmartLAPanel.onlySBool) {
//				for (IdObject idObject : modelHandler.getModelView().getSelectedIdObjects()) {
//					idObject.getAnimFlags().forEach(flag -> addLinearizeAction(interpTypActions, flag));
//				}
//			} else {
//				ModelUtils.doForAnimFlags(modelHandler.getModel(), flag -> addLinearizeAction(interpTypActions, flag));
//			}
//
//			UndoAction action = new CompoundAction("Liniarize Animations", interpTypActions, ModelStructureChangeListener.changeListener::animationParamsChanged);
//			modelHandler.getUndoManager().pushAction(action.redo());
//		}
//	}
//
//	private static void addLinearizeAction(List<UndoAction> interpTypActions, AnimFlag<?> flag) {
//		if (flag.getName().equals(MdlUtils.TOKEN_TRANSLATION) && SmartLAPanel.transLastState.isActive) {
//			interpTypActions.add(new SmartLinearizeAction<>(flag, SmartLAPanel.transLastState.allowedDiff, null));
//		} else if (flag.getName().equals(MdlUtils.TOKEN_ROTATION) && SmartLAPanel.rotChLastState.isActive) {
//			interpTypActions.add(new SmartLinearizeAction<>(flag, SmartLAPanel.rotChLastState.allowedDiff, null));
//		} else if (flag.getName().equals(MdlUtils.TOKEN_SCALING) && SmartLAPanel.scaleLastState.isActive) {
//			interpTypActions.add(new SmartLinearizeAction<>(flag, SmartLAPanel.scaleLastState.allowedDiff, null));
//		} else if (SmartLAPanel.otherLastState.isActive) {
//			interpTypActions.add(new SmartLinearizeAction<>(flag, SmartLAPanel.otherLastState.allowedDiff, null));
//		}
//	}




//	private static class SmartLAPanel extends JPanel {
//		private static final LastState transLastState = new LastState(true, 0.1f);
//		private static final LastState scaleLastState = new LastState(true, 0.1f);
//		private static final LastState rotChLastState = new LastState(true, 0.01f);
//		private static final LastState otherLastState = new LastState(false, 0.01f);
//		private static boolean onlySBool = true;
//
//		SmartLAPanel() {
//			super(new MigLayout("fill", "", ""));
//			add(CheckBox.create("Only selected nodes", onlySBool, b -> onlySBool = b), "wrap");
//			add(new JLabel("(leave at 0 to not add additional kfs)"),  "wrap");
//			add(getCheckedSpinner("Translation", transLastState, .1), "wrap");
//			add(getCheckedSpinner("Scaling", scaleLastState, .1), "wrap");
//			add(getCheckedSpinner("Rotation", rotChLastState, .01), "wrap");
//			add(getCheckedSpinner("Other", otherLastState, .01), "wrap");
//		}
//
//	    private CheckSpinner getCheckedSpinner(String name, LastState lastState, double stepSize) {
//	        String allowed = "Add keyframe if diff larger than:";
//	        return new CheckSpinner(name, lastState.isActive, allowed, stepSize)
//	                .setOnCheckedConsumer(b -> lastState.isActive = b, true)
//	                .setFloatConsumer(f -> lastState.allowedDiff = f, true);
//	    }
//
//		private static class LastState {
//			private boolean isActive;
//			private Float allowedDiff;
//
//			LastState(boolean isActive, Float allowedDiff) {
//				this.isActive = isActive;
//				this.allowedDiff = allowedDiff;
//			}
//		}
//	}

	private static class SmartLAPanel2 extends JPanel {
		private static final LastState transLastState = new LastState(true, 0, 0.1f);
		private static final LastState scaleLastState = new LastState(true, 0, 0.1f);
		private static final LastState rotChLastState = new LastState(true, 0, 0.01f);
		private static final LastState otherLastState = new LastState(false, 0, 0.01f);

		private static boolean materBool = false;
		private static boolean textuBool = false;
		private static boolean geoseBool = false;
		private static boolean camerBool = false;
		private static boolean nodesBool = true;
		private static boolean partiBool = true;
		private static boolean onlySBool = true;


		SmartLAPanel2() {
			super(new MigLayout("fill", "", ""));

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
//			add(new JLabel("(leave at 0 to not add additional kfs)"),  "wrap");


			add(new TransOptPanel("Translation", transLastState, .1), "wrap");
			add(new TransOptPanel("Scaling", scaleLastState, .1), "wrap");
			add(new TransOptPanel("Rotation", rotChLastState, .01), "wrap");
			add(new TransOptPanel("Other", otherLastState, .01), "wrap");

		}

		public List<AnimFlag<?>> getAnimFlags(ModelHandler modelHandler) {
			EditableModel model = modelHandler.getModel();
			ModelView modelView = modelHandler.getModelView();
			List<AnimFlag<?>> animFlags = new ArrayList<>();


			if (nodesBool) {
				Collection<IdObject> idObjects = onlySBool ? modelView.getSelectedIdObjects() : model.getIdObjects();
				for (IdObject idObject : idObjects) {
					if (!(idObject instanceof EmitterIdObject) || (partiBool)) {
						animFlags.addAll(idObject.getAnimFlags());
					}
				}
			}

			if (camerBool) {
				Collection<Camera> cameras = onlySBool ? modelView.getSelectedCameras() : model.getCameras();

				for (Camera x : cameras) {
					animFlags.addAll(x.getSourceNode().getAnimFlags());
					animFlags.addAll(x.getTargetNode().getAnimFlags());
				}
			}

			if (materBool) {
				for (Material m : model.getMaterials()) {
					for (Layer lay : m.getLayers()) {
						animFlags.addAll(lay.getAnimFlags());
					}
				}
			}
			if (textuBool) {
				for (TextureAnim texa : model.getTexAnims()) {
					animFlags.addAll(texa.getAnimFlags());
				}
			}
			if (geoseBool) {
				for (Geoset geoset : model.getGeosets()) {
					animFlags.addAll(geoset.getAnimFlags());
				}
			}

			return animFlags;
		}


		private static class TransOptPanel extends JPanel {
			LabelPanel labelPanel;
			SpinnerPanel spinnerPanel;
			LastState lastState;
			SmartButtonGroup buttonGroup;
			TransOptPanel(String name, LastState lastState, double stepSize) {
				super(new MigLayout("ins 0, ", "", ""));
				setBorder(BorderFactory.createTitledBorder(name));

				this.lastState = lastState;

				buttonGroup = new SmartButtonGroup();

				labelPanel = new LabelPanel(buttonGroup.addJRadioButton(null, null, e -> setMode(0)), "No additional kfs");
				spinnerPanel = new SpinnerPanel(buttonGroup.addJRadioButton(null, null, e -> setMode(1)), "Add keyframe if diff larger than:", lastState.getLastDiff(), stepSize, lastState::setAllowedDiff);

//				labelPanel = new LabelPanel(buttonGroup.addJRadioButton("No additional kfs", null, e -> setMode(0)), "");
//				spinnerPanel = new SpinnerPanel(buttonGroup.addJRadioButton("Add keyframe if diff larger than:", null, e -> setMode(1)), "", lastState.getLastDiff(), stepSize, lastState::setAllowedDiff);

				add(CheckBox.create(null, null, lastState.isActive, this::setTypeEnabled), "");
				add(labelPanel);
				add(spinnerPanel);
				buttonGroup.setSelectedIndex(lastState.mode);
				setTypeEnabled(lastState.isActive);
			}

			private void setTypeEnabled(boolean enabled) {
				buttonGroup.setEnabled(enabled);
				labelPanel.setEnabled(enabled);
				spinnerPanel.setEnabled(enabled);
				lastState.isActive = enabled;
			}

			private void setMode(int mode) {
				lastState.mode = mode;
				labelPanel.setChosen(mode == 0);
				spinnerPanel.setChosen(mode == 1);
			}
		}


		private static class LabelPanel extends JPanel {
			private boolean isChosen = true;
			JRadioButton radioButton;
			JLabel textComp;
			LabelPanel(JRadioButton radioButton, String text) {
				super(new MigLayout("ins 0 0 0 2, ", "", ""));
				setBorder(BorderFactory.createTitledBorder(""));
				this.radioButton = radioButton;
				textComp = new JLabel(text);
				add(radioButton);
				add(textComp);
			}

			@Override
			public void setEnabled(boolean enabled) {
				textComp.setEnabled(enabled && isChosen);
			}

			public LabelPanel setChosen(boolean chosen) {
				isChosen = chosen;
				textComp.setEnabled(isChosen);
				return this;
			}
		}

		private static class SpinnerPanel extends JPanel {
			private boolean isChosen = true;
			private final JRadioButton radioButton;
			private final JLabel textComp;
			private final FloatEditorJSpinner spinner;
			private final Consumer<Float> floatConsumer;
			SpinnerPanel(JRadioButton radioButton, String text, float value, double stepSize, Consumer<Float> floatConsumer) {
				super(new MigLayout("ins 0, ", "", ""));
				setBorder(BorderFactory.createTitledBorder(""));
				this.floatConsumer = floatConsumer;
				spinner = new FloatEditorJSpinner(value, 0f, 10000.0f, (float) stepSize, floatConsumer);
				this.radioButton = radioButton;
				add(radioButton);
				textComp = new JLabel(text);
				add(textComp);
				add(spinner);
			}

			@Override
			public void setEnabled(boolean enabled) {
//				radioButton.setEnabled(enabled);
				spinner.setEnabled(enabled && isChosen);
				textComp.setEnabled(enabled && isChosen);
				if (floatConsumer != null) {
					floatConsumer.accept((enabled && isChosen) ? spinner.getFloatValue() : null);
				}
			}

			public SpinnerPanel setChosen(boolean chosen) {
				isChosen = chosen;
				spinner.setEnabled(isChosen);
				textComp.setEnabled(isChosen);
				if (floatConsumer != null) {
					floatConsumer.accept((isChosen) ? spinner.getFloatValue() : null);
				}
				return this;
			}
		}

		private static class LastState {
			private boolean isActive;
			private int mode;
			private float lastDiff;
			private Float allowedDiff;

			LastState(boolean isActive, int mode, Float allowedDiff) {
				this.isActive = isActive;
				this.mode = mode;
				this.allowedDiff = allowedDiff;
				if (allowedDiff != null) {
					lastDiff = allowedDiff;
				}
			}

			public LastState setAllowedDiff(Float allowedDiff) {
				System.out.println("new allowedDiff: " + allowedDiff);
				this.allowedDiff = allowedDiff;
				if (allowedDiff != null) {
					lastDiff = allowedDiff;
				}
				return this;
			}

			public float getLastDiff() {
				return lastDiff;
			}

			public Float getAllowedDiff() {
				return allowedDiff;
			}

			@Override
			public String toString() {
				return "LastState{" +
						"isActive=" + isActive +
						", mode=" + mode +
						", allowedDiff=" + allowedDiff +
						'}';
			}
		}
	}
}
