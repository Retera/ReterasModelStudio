package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.model.*;
import com.hiveworkshop.rms.editor.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.editor.actions.nodes.NameChangeAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec3SpinnerArray;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class ComponentCameraPanel extends ComponentPanel<Camera> {
	private final TwiTextField nameField;
	private Camera camera;

	private final FloatEditorJSpinner fieldOfViewSpinner;
	private final FloatEditorJSpinner farClipSpinner;
	private final FloatEditorJSpinner nearClipSpinner;

	private final Vec3SpinnerArray positionSpinner;
	private final Vec3SpinnerArray targetSpinner;

	public ComponentCameraPanel(ModelHandler modelHandler) {
		super(modelHandler);

		setLayout(new MigLayout("fill, gap 0", "[]5[]5[grow]", "[][][][][][][grow]"));
		nameField = new TwiTextField(24, this::changeName1);
		nameField.setFont(new Font("Arial", Font.BOLD, 18));
		add(nameField, "spanx 2");
//		add(getDeleteButton(e -> removeCamera()), "skip 1, wrap");
		add(getDeleteButton(e -> removeCamera()), "wrap");


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
	}

	@Override
	public ComponentPanel<Camera> setSelectedItem(Camera itemToSelect) {
		camera = itemToSelect;
		nameField.setText(camera.getName());

		positionSpinner.setValues(camera.getPosition());
		targetSpinner.setValues(camera.getTargetPosition());

		fieldOfViewSpinner.reloadNewValue(camera.getFieldOfView());
		farClipSpinner.reloadNewValue(camera.getFarClip());
		nearClipSpinner.reloadNewValue(camera.getNearClip());

		revalidate();
		repaint();

		return this;
	}

	private void changeName1(String newName) {
		if (!newName.equals("")) {
			undoManager.pushAction(new NameChangeAction(camera, newName, changeListener).redo());
		}
	}

	private void removeCamera() {
		undoManager.pushAction(new DeleteNodesAction(camera, changeListener, model).redo());
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
		if (fov != camera.getFieldOfView()) {
			undoManager.pushAction(new SetCameraFoVAction(camera, fov, changeListener).redo());
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
