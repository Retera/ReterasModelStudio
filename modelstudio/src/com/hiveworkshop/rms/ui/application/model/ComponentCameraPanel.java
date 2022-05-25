package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.animation.SimplifyKeyframesAction;
import com.hiveworkshop.rms.editor.actions.model.*;
import com.hiveworkshop.rms.editor.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.editor.actions.nodes.NameChangeAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.IntAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.editors.*;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec3SpinnerArray;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;

public class ComponentCameraPanel extends ComponentPanel<Camera> {
	private final TwiTextField nameField;
	private Camera camera;

	private final FloatEditorJSpinner fieldOfViewSpinner;
	private final FloatEditorJSpinner farClipSpinner;
	private final FloatEditorJSpinner nearClipSpinner;

	private final Vec3SpinnerArray positionSpinner;
	private final Vec3SpinnerArray targetSpinner;

	Vec3ValuePanel transPanel;
	QuatValuePanel rotPanelQuat;
	FloatValuePanel rotPanelFloat;
	IntegerValuePanel rotPanelInt;
	Vec3ValuePanel targetTransPanel;
	public ComponentCameraPanel(ModelHandler modelHandler) {
		super(modelHandler);

		setLayout(new MigLayout("fill, gap 0, hidemode 3", "[]5[]5[grow]", "[][][][][][][grow][grow][grow]"));
		nameField = new TwiTextField(24, this::changeName1);
		nameField.setFont(new Font("Arial", Font.BOLD, 18));
		add(nameField, "spanx 2");
//		add(getDeleteButton(e -> removeCamera()), "skip 1, wrap");
		add(getDeleteButton(e -> removeCamera()), "wrap");
		add(getButton("Simplyfy keyframes", e -> simplifyKFs()), "wrap");


		positionSpinner = new Vec3SpinnerArray().setVec3Consumer(this::setPosition);
//		add(new JLabel("Position: "), "split, spanx 2");
		add(new JLabel("Position: "), "");
		add(positionSpinner.spinnerPanel(), "wrap");

		targetSpinner = new Vec3SpinnerArray().setVec3Consumer(this::setTarget);
//		add(new JLabel("Target: "), "split, spanx 2");
		add(new JLabel("Target: "), "");
		add(targetSpinner.spinnerPanel(), "wrap");

		add(new JLabel("FoV"), "");
		fieldOfViewSpinner = new FloatEditorJSpinner(0f, 0f, this::setFov);
		add(fieldOfViewSpinner, "wrap");

		add(new JLabel("farClip"), "");
		farClipSpinner = new FloatEditorJSpinner(0f, 0f, this::setFarClip);
		add(farClipSpinner, "wrap");

		add(new JLabel("nearClip"), "");
		nearClipSpinner = new FloatEditorJSpinner(0f, 0f, this::setNearClip);
		add(nearClipSpinner, "wrap");

		transPanel = new Vec3ValuePanel(modelHandler, MdlUtils.TOKEN_TRANSLATION);
		rotPanelQuat = new QuatValuePanel(modelHandler, MdlUtils.TOKEN_ROTATION);
		rotPanelFloat = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_ROTATION);
		rotPanelInt = new IntegerValuePanel(modelHandler, MdlUtils.TOKEN_ROTATION);
		targetTransPanel = new Vec3ValuePanel(modelHandler, MdlUtils.TOKEN_TRANSLATION + " Target");

		add(transPanel, "spanx, growx, wrap");
		add(rotPanelQuat, "spanx, growx, wrap");
		add(rotPanelFloat, "spanx, growx, wrap");
		add(rotPanelInt, "spanx, growx, wrap");
		add(targetTransPanel, "spanx, growx, wrap");
	}

	@Override
	public ComponentPanel<Camera> setSelectedItem(Camera itemToSelect) {
		camera = itemToSelect;
		nameField.setText(camera.getName());

		positionSpinner.setValues(camera.getPosition());
		targetSpinner.setValues(camera.getTargetPosition());

		fieldOfViewSpinner.reloadNewValue(Math.toDegrees(camera.getFieldOfView()));
		farClipSpinner.reloadNewValue(camera.getFarClip());
		nearClipSpinner.reloadNewValue(camera.getNearClip());

		transPanel.reloadNewValue(new Vec3(0, 0, 0), (Vec3AnimFlag) camera.getSourceNode().find(MdlUtils.TOKEN_TRANSLATION), camera.getSourceNode(), MdlUtils.TOKEN_TRANSLATION, null);
		if(camera.getSourceNode().find(MdlUtils.TOKEN_ROTATION) instanceof QuatAnimFlag) {
			rotPanelQuat.reloadNewValue(new Quat(0, 0, 0, 1), (QuatAnimFlag) camera.getSourceNode().find(MdlUtils.TOKEN_ROTATION), camera.getSourceNode(), MdlUtils.TOKEN_ROTATION, null);
			rotPanelFloat.reloadNewValue(0f, null, camera.getSourceNode(), MdlUtils.TOKEN_ROTATION, null);
			rotPanelInt.reloadNewValue(0, null, camera.getSourceNode(), MdlUtils.TOKEN_ROTATION, null);
			rotPanelQuat.setVisible(true);
			rotPanelFloat.setVisible(false);
			rotPanelInt.setVisible(false);
		} else if (camera.getSourceNode().find(MdlUtils.TOKEN_ROTATION) instanceof FloatAnimFlag) {
			rotPanelFloat.reloadNewValue(0f, (FloatAnimFlag) camera.getSourceNode().find(MdlUtils.TOKEN_ROTATION), camera.getSourceNode(), MdlUtils.TOKEN_ROTATION, null);
			rotPanelQuat.reloadNewValue(new Quat(0, 0, 0, 1), null, camera.getSourceNode(), MdlUtils.TOKEN_ROTATION, null);
			rotPanelInt.reloadNewValue(0, null, camera.getSourceNode(), MdlUtils.TOKEN_ROTATION, null);
			rotPanelFloat.setVisible(true);
			rotPanelInt.setVisible(false);
			rotPanelQuat.setVisible(false);
		} else if (camera.getSourceNode().find(MdlUtils.TOKEN_ROTATION) instanceof IntAnimFlag) {
			rotPanelInt.reloadNewValue(0, (IntAnimFlag) camera.getSourceNode().find(MdlUtils.TOKEN_ROTATION), camera.getSourceNode(), MdlUtils.TOKEN_ROTATION, null);
			rotPanelFloat.reloadNewValue(0f, null, camera.getSourceNode(), MdlUtils.TOKEN_ROTATION, null);
			rotPanelQuat.reloadNewValue(new Quat(0, 0, 0, 1), null, camera.getSourceNode(), MdlUtils.TOKEN_ROTATION, null);
			rotPanelInt.setVisible(true);
			rotPanelFloat.setVisible(false);
			rotPanelQuat.setVisible(false);
		} else {
			rotPanelFloat.reloadNewValue(0f, (FloatAnimFlag) camera.getSourceNode().find(MdlUtils.TOKEN_ROTATION), camera.getSourceNode(), MdlUtils.TOKEN_ROTATION, null);
			rotPanelQuat.reloadNewValue(new Quat(0, 0, 0, 1), null, camera.getSourceNode(), MdlUtils.TOKEN_ROTATION, null);
			rotPanelInt.reloadNewValue(0, null, camera.getSourceNode(), MdlUtils.TOKEN_ROTATION, null);
			rotPanelFloat.setVisible(true);
			rotPanelInt.setVisible(false);
			rotPanelQuat.setVisible(false);
		}
		targetTransPanel.reloadNewValue(new Vec3(0, 0, 0), (Vec3AnimFlag) camera.getTargetNode().find(MdlUtils.TOKEN_TRANSLATION), camera.getTargetNode(), MdlUtils.TOKEN_TRANSLATION, null);

		revalidate();
		repaint();

		return this;
	}

	private void changeName1(String newName) {
		if (!newName.equals("") && !newName.equals(camera.getName())) {
			undoManager.pushAction(new NameChangeAction(camera, newName, changeListener).redo());
		}
	}

	private void removeCamera() {
		undoManager.pushAction(new DeleteNodesAction(camera, changeListener, model).redo());
	}
	private void simplifyKFs() {
		boolean allowRemovePeaks = false;
		undoManager.pushAction(new SimplifyKeyframesAction(Collections.singleton(camera), model.getAllSequences(), 0.1f, 0.1f, allowRemovePeaks).redo());
	}

	private void setPosition(Vec3 newPosition) {
		if (!newPosition.equalLocs(camera.getPosition())) {
			undoManager.pushAction(new SetCameraPosAction(camera, newPosition, changeListener).redo());
		}
	}

	private void setTarget(Vec3 newTarget) {
		if (!newTarget.equalLocs(camera.getTargetPosition())) {
			undoManager.pushAction(new SetCameraTargetAction(camera, newTarget, changeListener).redo());
		}
	}

	private void setFov(Float fov) {
		if (fov != Math.toDegrees(camera.getFieldOfView())) {
			undoManager.pushAction(new SetCameraFoVAction(camera, Math.toRadians(fov), changeListener).redo());
		}
	}

	private void setNearClip(Float clip) {
		if (clip != camera.getNearClip()) {
			undoManager.pushAction(new SetCameraNearClipAction(camera, clip, changeListener).redo());
		}
	}

	private void setFarClip(Float clip) {
		if (clip != camera.getFarClip()) {
			undoManager.pushAction(new SetCameraFarClipAction(camera, clip, changeListener).redo());
		}
	}

}
