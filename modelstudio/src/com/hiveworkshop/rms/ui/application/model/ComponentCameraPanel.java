package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.animation.SimplifyKeyframesAction;
import com.hiveworkshop.rms.editor.actions.model.*;
import com.hiveworkshop.rms.editor.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.editor.actions.nodes.NameChangeAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.animflag.*;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.application.model.editors.ValueParserUtil;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.TwiTextEditor.FlagPanel;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec3SpinnerArray;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComponentCameraPanel extends ComponentPanel<Camera> {
	private final TwiTextField nameField;
	private Camera camera;
	private JLabel bindPoseLabel;

	private final FloatEditorJSpinner fieldOfViewSpinner;
	private final FloatEditorJSpinner farClipSpinner;
	private final FloatEditorJSpinner nearClipSpinner;

	private final Vec3SpinnerArray positionSpinner;
	private final Vec3SpinnerArray targetSpinner;


	protected FlagPanel<Vec3> transPanel;
	protected FlagPanel<Quat> rotPanelQuat;
	protected FlagPanel<Float> rotPanelFloat;
	protected FlagPanel<Integer> rotPanelInt;

	protected FlagPanel<Vec3> targetTransPanel;

	public ComponentCameraPanel(ModelHandler modelHandler, ComponentsPanel componentsPanel) {
		super(modelHandler, componentsPanel);

		setLayout(new MigLayout("fill, gap 0, hidemode 3", "[]5[]5[grow]", "[][][][][][][grow][grow][grow]"));
		nameField = new TwiTextField(24, this::changeName1);
		nameField.setFont(new Font("Arial", Font.BOLD, 18));
		add(nameField, "spanx 2");
//		add(getDeleteButton(e -> removeCamera()), "skip 1, wrap");
		add(getDeleteButton(e -> removeCamera()), "wrap");
		add(getButton("Simplyfy keyframes", e -> simplifyKFs()), "wrap");

		bindPoseLabel = new JLabel("BP:{1111111111111111111111111111111111111111111111111111}");
//		add(bindPoseLabel, "spanx");


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


		transPanel = new FlagPanel<>(MdlUtils.TOKEN_TRANSLATION, this::parseVec3, new Vec3(0,0,0), modelHandler);
		rotPanelQuat = new FlagPanel<>(MdlUtils.TOKEN_ROTATION, this::parseQuat, new Quat(0,0,0, 1), modelHandler);
		rotPanelFloat = new FlagPanel<>(MdlUtils.TOKEN_ROTATION, this::parseFloat, 0.0f, modelHandler);
		rotPanelInt = new FlagPanel<>(MdlUtils.TOKEN_ROTATION, this::parseInt, 0, modelHandler);

		targetTransPanel = new FlagPanel<>(MdlUtils.TOKEN_TRANSLATION + " Target", this::parseVec3, new Vec3(0,0,0), modelHandler);

		add(transPanel, "spanx, growx, wrap");
		add(rotPanelQuat, "spanx, growx, wrap");
		add(rotPanelFloat, "spanx, growx, wrap");
		add(rotPanelInt, "spanx, growx, wrap");
		add(targetTransPanel, "spanx, growx, wrap");
	}

	private Vec3 parseVec3(String s) {
		return Vec3.parseVec3(ValueParserUtil.getString(3,s));
	}
	private Quat parseQuat(String s) {
		return Quat.parseQuat(ValueParserUtil.getString(4,s));
	}
	private Float parseFloat(String s) {
		s = s.replaceAll("[^-\\.e\\d]", "");

		if (s.matches("(-?\\d+\\.+)")) {
//			System.out.println("5 \"(-?\\d+\\.+)\" - " + s);
			s = s.replace(".", "");
		}
		if (s.matches("(-?\\d+\\.+\\d+)")) {
//			System.out.println("5 \"(-?\\d+\\.+\\d+)\" - " + s);
			s = s.replaceAll("(\\.+)", ".");
		}
		if (s.matches(".*\\d.*") && s.matches("(-?\\d*\\.?\\d+(e\\d+)?)")) {
			return Float.parseFloat(s);
		}
		return 0.0f;
	}


	private Integer parseInt(String s) {
		return Integer.parseInt(s.replaceAll("[\\D]", ""));
	}

	@Override
	public ComponentPanel<Camera> setSelectedItem(Camera itemToSelect) {
		camera = itemToSelect;
		nameField.setText(camera.getName());

		if (itemToSelect.getBindPoseM4() != null) {
			bindPoseLabel.setText("BP: " + Arrays.toString(itemToSelect.getBindPoseM4().getBindPose()));
		} else {
			bindPoseLabel.setText("BP: null");
		}

		positionSpinner.setValues(camera.getPosition());
		targetSpinner.setValues(camera.getTargetPosition());

		fieldOfViewSpinner.reloadNewValue(Math.toDegrees(camera.getFieldOfView()));
		farClipSpinner.reloadNewValue(camera.getFarClip());
		nearClipSpinner.reloadNewValue(camera.getNearClip());

		transPanel.update(camera.getSourceNode(), (Vec3AnimFlag) camera.getSourceNode().find(MdlUtils.TOKEN_TRANSLATION), new Vec3(0, 0, 0));
		targetTransPanel.update(camera.getTargetNode(), (Vec3AnimFlag) camera.getTargetNode().find(MdlUtils.TOKEN_TRANSLATION), new Vec3(0, 0, 0));

		updateCameraRotationPanels();

		revalidate();
		repaint();

		return this;
	}

	private void updateCameraRotationPanels() {
		AnimFlag<?> rotationFlag = camera.getSourceNode().find(MdlUtils.TOKEN_ROTATION);
		if (rotationFlag instanceof QuatAnimFlag) {
			rotPanelQuat.update(camera.getSourceNode(), (QuatAnimFlag) rotationFlag, new Quat(0, 0, 0, 1));
		} else {
			rotPanelQuat.update(camera.getSourceNode(), null, new Quat(0, 0, 0, 1));
		}

		if (rotationFlag == null || rotationFlag instanceof FloatAnimFlag) {
			rotPanelFloat.update(camera.getSourceNode(), (FloatAnimFlag) rotationFlag, 0.0f);
		} else {
			rotPanelFloat.update(camera.getSourceNode(), null, 0.0f);
		}

		if (rotationFlag instanceof IntAnimFlag) {
			rotPanelInt.update(camera.getSourceNode(), (IntAnimFlag) rotationFlag, 0);
		} else {
			rotPanelInt.update(camera.getSourceNode(), null, 0);
		}

		rotPanelQuat.setVisible(rotationFlag instanceof QuatAnimFlag);
		rotPanelFloat.setVisible(rotationFlag == null || rotationFlag instanceof FloatAnimFlag);
		rotPanelInt.setVisible(rotationFlag instanceof IntAnimFlag);
	}
	private void updateCameraRotationPanels1() {
		AnimFlag<?> rotationFlag = camera.getSourceNode().find(MdlUtils.TOKEN_ROTATION);
		if (rotationFlag instanceof QuatAnimFlag) {
			rotPanelQuat.update(camera.getSourceNode(), (QuatAnimFlag) rotationFlag, new Quat(0, 0, 0, 1));
			rotPanelFloat.update(camera.getSourceNode(), null, 0.0f);
			rotPanelInt.update(camera.getSourceNode(), null, 0);
		} else if (rotationFlag instanceof FloatAnimFlag) {
			rotPanelQuat.update(camera.getSourceNode(), null, new Quat(0, 0, 0, 1));
			rotPanelFloat.update(camera.getSourceNode(), (FloatAnimFlag) rotationFlag, 0.0f);
			rotPanelInt.update(camera.getSourceNode(), null, 0);
		} else if (rotationFlag instanceof IntAnimFlag) {
			rotPanelQuat.update(camera.getSourceNode(), null, new Quat(0, 0, 0, 1));
			rotPanelInt.update(camera.getSourceNode(), (IntAnimFlag) rotationFlag, 0);
			rotPanelFloat.update(camera.getSourceNode(), null, 0.0f);
		} else {
			rotPanelQuat.update(camera.getSourceNode(), null, new Quat(0, 0, 0, 1));
			rotPanelFloat.update(camera.getSourceNode(), null, 0.0f);
			rotPanelInt.update(camera.getSourceNode(), null, 0);
		}
		rotPanelQuat.setVisible(rotationFlag instanceof QuatAnimFlag);
		rotPanelFloat.setVisible(rotationFlag instanceof FloatAnimFlag);
		rotPanelInt.setVisible(rotationFlag instanceof IntAnimFlag);
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
		List<AnimFlag<?>> animFlags = new ArrayList<>();
		animFlags.addAll(camera.getTargetNode().getAnimFlags());
		animFlags.addAll(camera.getSourceNode().getAnimFlags());
		if (!animFlags.isEmpty()) {
			undoManager.pushAction(new SimplifyKeyframesAction(animFlags, model.getAllSequences(), 0.1f, -1f, 0.1f, -1f, true).redo());
		}
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
