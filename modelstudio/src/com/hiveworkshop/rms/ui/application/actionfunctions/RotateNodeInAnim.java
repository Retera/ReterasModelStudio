package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.RotateNodeInAnimAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.actions.editor.CompoundRotateAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.SmartNumberSlider;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RotateNodeInAnim extends ActionFunction {
	private final static String SLIDER_CONSTRAINTS = "wrap, growx, spanx";

	public RotateNodeInAnim(){
		super(TextKey.ROTATE_NODE_IN_SEQ, RotateNodeInAnim::showPopup);
	}

	private static void showPopup(ModelHandler modelHandler) {
		if(modelHandler != null){
//			CompoundRotateAction action = startRotateNodes(new Vec3(0, 0, 0), new Vec3(Vec3.X_AXIS), modelHandler.getModelView().getSelectedIdObjects(), modelHandler.getEditTimeEnv().getCurrentSequence());
			List<RotateNodeInAnimAction> rotateActions = getRotateNodesActions(new Vec3(0, 0, 0), new Vec3(Vec3.X_AXIS), modelHandler.getModelView().getSelectedIdObjects(), modelHandler.getEditTimeEnv().getCurrentSequence());
			CompoundRotateAction action = new CompoundRotateAction("Rotate Model", rotateActions);;


			float[] scaleDiff = new float[]{0, 0};
//			float[] rotV = new float[]{0, 0, 0};
			Vec3 rotV = new Vec3();
			JPanel sliderPanel = new JPanel(new MigLayout("ins 0"));
			Quat rot = new Quat();

			SmartNumberSlider smartNumberSliderX = new SmartNumberSlider("Rotate X", 0, 0, 360, (i) -> {
				rotV.x = (float) Math.toRadians(i);
				updateRotateModel(rotateActions, rotV, rot, scaleDiff);
			});
			smartNumberSliderX.setMaxUpperLimit(360);
			smartNumberSliderX.setMinLowerLimit(0);
			sliderPanel.add(smartNumberSliderX, SLIDER_CONSTRAINTS);

			SmartNumberSlider smartNumberSliderY = new SmartNumberSlider("Rotate Y", 0, 0, 360, (i) -> {
				rotV.y = (float) Math.toRadians(i);
				updateRotateModel(rotateActions, rotV, rot, scaleDiff);
			});
			smartNumberSliderY.setMaxUpperLimit(360);
			smartNumberSliderY.setMinLowerLimit(0);
			sliderPanel.add(smartNumberSliderY, SLIDER_CONSTRAINTS);

			SmartNumberSlider smartNumberSliderZ = new SmartNumberSlider("Rotate Z", 0, 0, 360, (i) -> {
				rotV.z = (float) Math.toRadians(i);
				updateRotateModel(rotateActions, rotV, rot, scaleDiff);
			});
			smartNumberSliderZ.setMaxUpperLimit(360);
			smartNumberSliderZ.setMinLowerLimit(0);
			sliderPanel.add(smartNumberSliderZ, SLIDER_CONSTRAINTS);

			int opt = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), sliderPanel, "Rotate Node", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			applyRotateModel(modelHandler, action, opt == JOptionPane.OK_OPTION);
		}
	}

	private static CompoundRotateAction startRotateNodes(Vec3 center, Vec3 axis, Collection<IdObject> idObjects, Sequence sequence){
		List<AbstractTransformAction> rotateActions = new ArrayList<>();
//		Set<GeosetVertex> vertices = new HashSet<>();
//		for(Geoset geoset : model.getGeosets()){
//			vertices.addAll(geoset.getVertices());
//		}

//		Set<CameraNode> cameraNodes = new HashSet<>();
//		for (Camera camera : model.getCameras()){
//			cameraNodes.add(camera.getSourceNode());
//			cameraNodes.add(camera.getTargetNode());
//		}
//		rotateActions.add(new StaticMeshRotateAction(vertices, Collections.emptySet(), cameraNodes, center, axis, 0));

		for(IdObject idObject : idObjects){
			rotateActions.add(new RotateNodeInAnimAction(idObject, sequence, axis, 0, center, null).doSetup());
		}

		return new CompoundRotateAction("Rotate Model", rotateActions);
	}
	private static List<RotateNodeInAnimAction> getRotateNodesActions(Vec3 center, Vec3 axis, Collection<IdObject> idObjects, Sequence sequence){
		List<RotateNodeInAnimAction> rotateActions = new ArrayList<>();
//		Set<GeosetVertex> vertices = new HashSet<>();
//		for(Geoset geoset : model.getGeosets()){
//			vertices.addAll(geoset.getVertices());
//		}

//		Set<CameraNode> cameraNodes = new HashSet<>();
//		for (Camera camera : model.getCameras()){
//			cameraNodes.add(camera.getSourceNode());
//			cameraNodes.add(camera.getTargetNode());
//		}
//		rotateActions.add(new StaticMeshRotateAction(vertices, Collections.emptySet(), cameraNodes, center, axis, 0));

		for(IdObject idObject : idObjects){
			rotateActions.add(new RotateNodeInAnimAction(idObject, sequence, axis, 0, center, null).doSetup());
		}

		return rotateActions;
	}
	private static void updateRotateModel(CompoundRotateAction action, float rotation, float[] lastRotation){
		float newRotation = 0f + (rotation - lastRotation[0]);
		lastRotation[1]+=newRotation;
		System.out.println("rotation: " + rotation + ", lastRotation: " + lastRotation[0] + ", newRotation: " + newRotation + ", (" + lastRotation[1] + ")");
		lastRotation[0]=rotation;
		action.updateRotation((float) Math.toRadians(newRotation));

	}

	private static Quat rx = new Quat();
	private static Quat ry = new Quat();
	private static Quat rz = new Quat();
	private static void updateRotateModel(List<RotateNodeInAnimAction> actions, Vec3 rotV, Quat rot, float[] lastRotation){
//		float newRotation = 0f + (rotation - lastRotation[0]);
//		lastRotation[1]+=newRotation;
//		System.out.println("rotation: " + rotation + ", lastRotation: " + lastRotation[0] + ", newRotation: " + newRotation + ", (" + lastRotation[1] + ")");
//		lastRotation[0]=rotation;

		rx.setFromAxisAngle(Vec3.X_AXIS, rotV.x);
		ry.setFromAxisAngle(Vec3.Y_AXIS, rotV.y);
		rz.setFromAxisAngle(Vec3.Z_AXIS, rotV.z);

		rot.set(ry).mul(rx).mul(rz);
//		rot.set(rotV);

		for (final RotateNodeInAnimAction action : actions) {
			action.setRotation(rot);
		}

	}
	private static void applyRotateModel(ModelHandler modelHandler, UndoAction action, boolean doApply){
		if(doApply){
			modelHandler.getUndoManager().pushAction(action);
		} else {
			action.undo();
		}
	}
}
