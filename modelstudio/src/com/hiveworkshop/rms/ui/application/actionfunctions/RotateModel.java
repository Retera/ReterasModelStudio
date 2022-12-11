package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.RotateNodeAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.actions.editor.CompoundRotateAction;
import com.hiveworkshop.rms.editor.actions.editor.StaticMeshRotateAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.SmartNumberSlider;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.*;

public class RotateModel extends ActionFunction {
	private final static String SLIDER_CONSTRAINTS = "wrap, growx, spanx";

	public RotateModel(){
		super(TextKey.ROTATE_MODEL, RotateModel::showPopup);
	}

	private static void showPopup(ModelHandler modelHandler) {
		if(modelHandler != null){
			CompoundRotateAction action = startRotateModel(new Vec3(0, 0, 0), new Vec3(Vec3.Z_AXIS), modelHandler.getModel());
			float[] scaleDiff = new float[]{0, 0};
			JPanel sliderPanel = new JPanel(new MigLayout("ins 0"));
			SmartNumberSlider smartNumberSlider = new SmartNumberSlider("Rotate", 0, 0, 360, (i) -> updateRotateModel(action, i, scaleDiff));
			smartNumberSlider.setMaxUpperLimit(360);
			smartNumberSlider.setMinLowerLimit(0);
			sliderPanel.add(smartNumberSlider, SLIDER_CONSTRAINTS);
			int opt = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), sliderPanel, "Scale model", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			applyRotateModel(modelHandler, action, opt == JOptionPane.OK_OPTION);
		}
	}

	private static CompoundRotateAction startRotateModel(Vec3 center, Vec3 axis, EditableModel model){
		List<AbstractTransformAction> rotateActions = new ArrayList<>();
		Set<GeosetVertex> vertices = new HashSet<>();
		for(Geoset geoset : model.getGeosets()){
			vertices.addAll(geoset.getVertices());
		}

		Set<CameraNode> cameraNodes = new HashSet<>();
		for (Camera camera : model.getCameras()){
			cameraNodes.add(camera.getSourceNode());
			cameraNodes.add(camera.getTargetNode());
		}
		rotateActions.add(new StaticMeshRotateAction(vertices, Collections.emptySet(), cameraNodes, center, axis, 0));

		for(IdObject idObject : model.getIdObjects()){
			rotateActions.add(new RotateNodeAction(idObject, axis, 0, center, null).doSetup());
		}

		return new CompoundRotateAction("Rotate Model", rotateActions);
	}
	private static void updateRotateModel(CompoundRotateAction action, float rotation, float[] lastRotation){
		float newRotation = 0f + (rotation - lastRotation[0]);
		lastRotation[1]+=newRotation;
		System.out.println("rotation: " + rotation + ", lastRotation: " + lastRotation[0] + ", newRotation: " + newRotation + ", (" + lastRotation[1] + ")");
		lastRotation[0]=rotation;
		action.updateRotation((float) Math.toRadians(newRotation));

	}
	private static void applyRotateModel(ModelHandler modelHandler, UndoAction action, boolean doApply){
		if(doApply){
			modelHandler.getUndoManager().pushAction(action);
		} else {
			action.undo();
		}
	}
}
