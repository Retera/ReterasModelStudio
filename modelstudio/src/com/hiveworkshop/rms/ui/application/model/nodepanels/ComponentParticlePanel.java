package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.actions.util.ConsumerAction;
import com.hiveworkshop.rms.editor.model.ParticleEmitter;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ExportInternal;
import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiTextEditor.EditorHelpers;

import javax.swing.*;

public class ComponentParticlePanel extends ComponentIdObjectPanel<ParticleEmitter> {
	private final ComponentEditorTextField pathField;

	private final EditorHelpers.FloatEditor longitudePanel;
	private final EditorHelpers.FloatEditor latitudePanel;
	private final EditorHelpers.FloatEditor speedPanel;
	private final EditorHelpers.FloatEditor gravityPanel;
	private final EditorHelpers.FloatEditor emissionPanel;
	private final EditorHelpers.FloatEditor lifeSpanPanel;
	private final EditorHelpers.FloatEditor visibilityPanel;

	public ComponentParticlePanel(ModelHandler modelHandler) {
		super(modelHandler);

		pathField = new ComponentEditorTextField(24, this::texturePathField);
		topPanel.add(pathField, "");
		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(e -> export());
		topPanel.add(exportButton, "wrap");

		longitudePanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_LONGITUDE, this::setLongitude);
		latitudePanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_LATITUDE, this::setLatitude);
		speedPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_SPEED, this::setInitVelocity);
		gravityPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_GRAVITY, this::setGravity);
		emissionPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_EMISSION_RATE, this::setEmissionRate);
		lifeSpanPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_LIFE_SPAN, this::setLifeSpan);
		visibilityPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_VISIBILITY, null);
		topPanel.add(longitudePanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(latitudePanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(speedPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(gravityPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(emissionPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(lifeSpanPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(visibilityPanel.getFlagPanel(), "spanx, growx, wrap");
	}

	@Override
	public void updatePanels() {
		pathField.reloadNewValue(idObject.getPath());
		longitudePanel.update(idObject, (float) idObject.getLongitude());
		latitudePanel.update(idObject, (float) idObject.getLatitude());
		speedPanel.update(idObject, (float) idObject.getInitVelocity());
		gravityPanel.update(idObject, (float) idObject.getGravity());
		emissionPanel.update(idObject, (float) idObject.getEmissionRate());
		lifeSpanPanel.update(idObject, (float) idObject.getLifeSpan());
		visibilityPanel.update(idObject, 1f);
	}

	private void texturePathField(String newPath) {
		idObject.setPath(newPath);
	}


	private void setLongitude(float value){
		if(value != idObject.getLongitude()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setLongitude, (double) value, idObject.getLongitude(), "Longitude").redo());
		}
	}
	private void setLatitude(float value){
		if(value != idObject.getLatitude()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setLatitude, (double) value, idObject.getLatitude(), "Latitude").redo());
		}
	}
	private void setInitVelocity(float value){
		if(value != idObject.getInitVelocity()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setInitVelocity, (double) value, idObject.getInitVelocity(), "InitVelocity").redo());
		}
	}
	private void setGravity(float value){
		if(value != idObject.getGravity()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setGravity, (double) value, idObject.getGravity(), "Gravity").redo());
		}
	}

	private void setEmissionRate(float value){
		if(value != idObject.getEmissionRate()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setEmissionRate, (double) value, idObject.getEmissionRate(), "EmissionRate").redo());
		}
	}
	private void setLifeSpan(float value){
		if(value != idObject.getLifeSpan()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setLifeSpan, (double) value, idObject.getLifeSpan(), "LifeSpan").redo());
		}
	}

	private void export(){
		String particlePath = ImportFileActions.convertPathToMDX(idObject.getPath());
		if(!particlePath.isEmpty()){
			ExportInternal.exportInternalFile(particlePath, "Particle", this);
		}
	}
}
