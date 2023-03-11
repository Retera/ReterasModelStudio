package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.animation.SimplifyKeyframesAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.render3d.EmitterIdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.ActionFunction;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.CheckSpinner;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.uiFactories.Button;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SimplifyKeyframesPanel extends JPanel {
	private static final LastState transLastState = new LastState(true, 0.1f);
	private static final LastState scaleLastState = new LastState(true, 0.1f);
	private static final LastState rotChLastState = new LastState(true, 0.01f);
	private static final LastState otherLastState = new LastState(false, 0.01f);
	private static boolean spareBool = false;
	private static boolean onlySBool = true;
	private static boolean materBool = false;
	private static boolean textuBool = false;
	private static boolean geoseBool = false;
	private static boolean camerBool = false;
	private static boolean nodesBool = true;
	private static boolean partiBool = true;

	public SimplifyKeyframesPanel() {
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

		add(getCheckedSpinner("Translation", transLastState, .1), "wrap");
		add(getCheckedSpinner("Scaling", scaleLastState, .1), "wrap");
		add(getCheckedSpinner("Rotation", rotChLastState, .01), "wrap");
		add(getCheckedSpinner("Other", otherLastState, .01), "wrap");

//		add(CheckBox.create("Keep keyframes at direction change", spareBool, b -> spareBool = b), "wrap");

		add(Button.create("Simplify", e -> simplify()), "wrap");
	}

	private CheckSpinner getCheckedSpinner(String name, LastState lastState, double stepSize){
		String allowed = "Allowed diff to remove keyframe";
		return  new CheckSpinner(name, lastState.isActive, allowed, stepSize)
				.setOnCheckedConsumer(b -> lastState.isActive = b, true)
				.setFloatConsumer(f -> lastState.allowedDiff = f, true);
	}

	private void simplify() {
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

		if(!animFlags.isEmpty()){
			Float trans     = getFixedFloat(transLastState.allowedDiff);
			Float scale     = getFixedFloat(scaleLastState.allowedDiff);
			Float rot       = getFixedFloat(rotChLastState.allowedDiff);
			Float valueDiff = getFixedFloat(otherLastState.allowedDiff);
			boolean allowRemovePeaks = !spareBool;

			SimplifyKeyframesAction action = new SimplifyKeyframesAction(animFlags, model.getAllSequences(), trans, scale, rot, valueDiff, allowRemovePeaks);
			modelPanel.getUndoManager().pushAction(action.redo());

			JOptionPane.showMessageDialog(this, "Removed " + action.getNumberOfEntriesToRemove() + " keyframes", "Removed Keyframes", JOptionPane.PLAIN_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, "Found no animation data to simplify", "No Keyframes Removed", JOptionPane.WARNING_MESSAGE);
		}
	}

	private Float getFixedFloat(Float value){
		if(value != null && value == 0f){
			return Float.MIN_VALUE;
		}
		return value;
	}

	public static void showPopup() {
		JComponent parent = ProgramGlobals.getMainPanel();
		FramePopup.show(new SimplifyKeyframesPanel(), parent, "Simplyfy keyframes");
	}

	public static void showPopup(JComponent parent) {
		FramePopup.show(new SimplifyKeyframesPanel(), parent, "Simplyfy keyframes");
	}

	private static class SimplifyKeyframes extends ActionFunction {

		SimplifyKeyframes(){
			super(TextKey.SIMPLIFY_KEYFRAMES, () -> showPopup());
			setMenuItemMnemonic(KeyEvent.VK_K);
		}
	}

	public static JMenuItem getMenuItem(){
		return new SimplifyKeyframes().getMenuItem();
	}

	private static class LastState {
		private boolean isActive;
		private Float allowedDiff;

		LastState(boolean isActive, Float allowedDiff){
			this.isActive = isActive;
			this.allowedDiff = allowedDiff;
		}
	}
}
