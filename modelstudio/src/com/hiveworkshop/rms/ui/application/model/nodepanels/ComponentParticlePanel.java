package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.ParticleEmitter;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

public class ComponentParticlePanel extends ComponentIdObjectPanel<ParticleEmitter> {
	private final ComponentEditorTextField pathField;

	private final FloatValuePanel longitudePanel;
	private final FloatValuePanel latitudePanel;
	private final FloatValuePanel speedPanel;
	private final FloatValuePanel gravityPanel;
	private final FloatValuePanel emissionPanel;
	private final FloatValuePanel visibilityPanel;

	public ComponentParticlePanel(ModelHandler modelHandler) {
		super(modelHandler);

		pathField = new ComponentEditorTextField(24);
		pathField.addEditingStoppedListener(this::texturePathField);
		topPanel.add(pathField, "wrap");

		longitudePanel = new FloatValuePanel(modelHandler, "Longitude");
		latitudePanel = new FloatValuePanel(modelHandler, "Latitude");
		speedPanel = new FloatValuePanel(modelHandler, "Speed");
		gravityPanel = new FloatValuePanel(modelHandler, "Gravity");
		emissionPanel = new FloatValuePanel(modelHandler, "EmissionRate");
		visibilityPanel = new FloatValuePanel(modelHandler, "Visibility");
		topPanel.add(longitudePanel, "spanx, growx, wrap");
		topPanel.add(latitudePanel, "spanx, growx, wrap");
		topPanel.add(speedPanel, "spanx, growx, wrap");
		topPanel.add(gravityPanel, "spanx, growx, wrap");
		topPanel.add(emissionPanel, "spanx, growx, wrap");
		topPanel.add(visibilityPanel, "spanx, growx, wrap");
	}

	@Override
	public void updatePanels() {
		pathField.reloadNewValue(idObject.getPath());
		longitudePanel.reloadNewValue((float) idObject.getLongitude(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_LONGITUDE), idObject, MdlUtils.TOKEN_LONGITUDE, idObject::setLongitude);
		latitudePanel.reloadNewValue((float) idObject.getLatitude(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_LATITUDE), idObject, MdlUtils.TOKEN_LATITUDE, idObject::setLatitude);
		speedPanel.reloadNewValue((float) idObject.getInitVelocity(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_SPEED), idObject, MdlUtils.TOKEN_SPEED, idObject::setInitVelocity);
		gravityPanel.reloadNewValue((float) idObject.getGravity(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_GRAVITY), idObject, MdlUtils.TOKEN_GRAVITY, idObject::setGravity);
		emissionPanel.reloadNewValue((float) idObject.getEmissionRate(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_EMISSION_RATE), idObject, MdlUtils.TOKEN_EMISSION_RATE, idObject::setEmissionRate);
		visibilityPanel.reloadNewValue(1f, (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_EMISSION_RATE), idObject, MdlUtils.TOKEN_EMISSION_RATE, null);
	}

	private void texturePathField() {
		idObject.setPath(pathField.getText());
	}
}
