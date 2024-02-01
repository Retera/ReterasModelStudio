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
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.SmartNumberSlider;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.*;

public class RotateModel extends ActionFunction {
	private final static String SLIDER_CONSTRAINTS = "wrap, growx, spanx";

	public RotateModel() {
		super(TextKey.ROTATE_MODEL, RotateModel::showPopup);
	}

	private static void showPopup(ModelHandler modelHandler) {
		if (modelHandler != null) {
			List<AbstractTransformAction> rotateActions = new ArrayList<>();
			startRotateModel(new Vec3(0, 0, 0), new Vec3(Vec3.Z_AXIS), modelHandler.getModel(), rotateActions);

			float[] scaleDiff = new float[]{0, 0};
			JPanel sliderPanel = new JPanel(new MigLayout("ins 0"));


			SmartNumberSlider smartNumberSlider = new SmartNumberSlider("Rotate", 0, -360, 360, (i) -> updateRotateModel(rotateActions, i, scaleDiff));
			smartNumberSlider.setMaxUpperLimit(360);
			smartNumberSlider.setMinLowerLimit(0);

			SmartButtonGroup axis = new SmartButtonGroup("Axis");
			axis.removeButtonConst("wrap");
			axis.addJRadioButton("X", e -> {
				startRotateModel(new Vec3(0, 0, 0), new Vec3(Vec3.X_AXIS), modelHandler.getModel(), rotateActions);
				updateRotateModel(rotateActions, smartNumberSlider.getValue(), scaleDiff);
			});
			axis.addJRadioButton("Y", e -> {
				startRotateModel(new Vec3(0, 0, 0), new Vec3(Vec3.Y_AXIS), modelHandler.getModel(), rotateActions);
				updateRotateModel(rotateActions, smartNumberSlider.getValue(), scaleDiff);

			});
			axis.addJRadioButton("Z", e -> {
				startRotateModel(new Vec3(0, 0, 0), new Vec3(Vec3.Z_AXIS), modelHandler.getModel(), rotateActions);
				updateRotateModel(rotateActions, smartNumberSlider.getValue(), scaleDiff);

			});
			axis.setSelectedIndex(2);
			sliderPanel.add(axis, "wrap");

			sliderPanel.add(smartNumberSlider, SLIDER_CONSTRAINTS);
			int opt = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), sliderPanel, "Rotate model", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			int deg = smartNumberSlider.getValue();
			applyRotateModel(modelHandler, new CompoundRotateAction("Rotate Model " + deg + "°", rotateActions), opt == JOptionPane.OK_OPTION);
		}
	}
	private static void startRotateModel(Vec3 center, Vec3 axis, EditableModel model, List<AbstractTransformAction> rotateActions) {
		for (AbstractTransformAction action : rotateActions) {
			action.undo();
		}
		rotateActions.clear();

		Set<GeosetVertex> vertices = new HashSet<>();
		for (Geoset geoset : model.getGeosets()) {
			vertices.addAll(geoset.getVertices());
		}

		Set<CameraNode> cameraNodes = new HashSet<>();
		for (Camera camera : model.getCameras()) {
			cameraNodes.add(camera.getSourceNode());
			cameraNodes.add(camera.getTargetNode());
		}
		rotateActions.add(new StaticMeshRotateAction(vertices, Collections.emptySet(), cameraNodes, center, axis, 0));

		for (IdObject idObject : model.getIdObjects()) {
			rotateActions.add(new RotateNodeAction(idObject, axis, 0, center, null).doSetup());
		}
	}

	private static void updateRotateModel(List<AbstractTransformAction> rotateActions, float rotation, float[] lastRotation) {
		float newRotation = 0f + (rotation - lastRotation[0]);
		lastRotation[1]+=newRotation;
		lastRotation[0]=rotation;
		for (final AbstractTransformAction action : rotateActions) {
			action.setRotation(Math.toRadians((rotation+360)%360));
		}
	}

	private static void applyRotateModel(ModelHandler modelHandler, UndoAction action, boolean doApply) {
		if (doApply) {
			modelHandler.getUndoManager().pushAction(action);
		} else {
			action.undo();
		}
	}
}
