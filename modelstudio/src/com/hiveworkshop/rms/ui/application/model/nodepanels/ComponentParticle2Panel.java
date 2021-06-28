package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

public class ComponentParticle2Panel extends ComponentIdObjectPanel<ParticleEmitter2> {

	private final FloatValuePanel widthPanel;
	private final FloatValuePanel lengthPanel;
	private final FloatValuePanel latitudePanel;
	private final FloatValuePanel variationPanel;
	private final FloatValuePanel speedPanel;
	private final FloatValuePanel gravityPanel;
	private final FloatValuePanel emissionPanel;
//	private final FloatValuePanel visibilityPanel;

	public ComponentParticle2Panel(ModelHandler modelHandler) {
		super(modelHandler);

		widthPanel = new FloatValuePanel(modelHandler, "Width", modelHandler.getUndoManager());
		lengthPanel = new FloatValuePanel(modelHandler, "Length", modelHandler.getUndoManager());
		latitudePanel = new FloatValuePanel(modelHandler, "Latitude", modelHandler.getUndoManager());
		variationPanel = new FloatValuePanel(modelHandler, "Variation", modelHandler.getUndoManager());
		speedPanel = new FloatValuePanel(modelHandler, "Speed", modelHandler.getUndoManager());
		gravityPanel = new FloatValuePanel(modelHandler, "Gravity", modelHandler.getUndoManager());
		emissionPanel = new FloatValuePanel(modelHandler, "EmissionRate", modelHandler.getUndoManager());
//		visibilityPanel = new FloatValuePanel(modelHandler, "Visibility", modelHandler.getUndoManager(), changeListener);
		topPanel.add(emissionPanel, "spanx, growx, wrap");
		topPanel.add(speedPanel, "spanx, growx, wrap");
		topPanel.add(gravityPanel, "spanx, growx, wrap");
		topPanel.add(variationPanel, "spanx, growx, wrap");
		topPanel.add(widthPanel, "spanx, growx, wrap");
		topPanel.add(lengthPanel, "spanx, growx, wrap");
		topPanel.add(latitudePanel, "spanx, growx, wrap");
//		add(visibilityPanel, "spanx, growx, wrap");
	}

	@Override
	public void updatePanels() {
		widthPanel.reloadNewValue((float) idObject.getWidth(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_WIDTH), idObject, MdlUtils.TOKEN_WIDTH, idObject::setWidth);
		lengthPanel.reloadNewValue((float) idObject.getLength(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_LENGTH), idObject, MdlUtils.TOKEN_LENGTH, idObject::setLength);
		latitudePanel.reloadNewValue((float) idObject.getLatitude(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_LATITUDE), idObject, MdlUtils.TOKEN_LATITUDE, idObject::setLatitude);
		variationPanel.reloadNewValue((float) idObject.getVariation(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_VARIATION), idObject, MdlUtils.TOKEN_VARIATION, idObject::setVariation);
		speedPanel.reloadNewValue((float) idObject.getSpeed(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_SPEED), idObject, MdlUtils.TOKEN_SPEED, idObject::setSpeed);
		gravityPanel.reloadNewValue((float) idObject.getGravity(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_GRAVITY), idObject, MdlUtils.TOKEN_GRAVITY, idObject::setGravity);
		emissionPanel.reloadNewValue((float) idObject.getEmissionRate(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_EMISSION_RATE), idObject, MdlUtils.TOKEN_EMISSION_RATE, idObject::setEmissionRate);
//		visibilityPanel.reloadNewValue( 1f, (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_EMISSION_RATE), idObject, MdlUtils.TOKEN_EMISSION_RATE, null);
	}
}
