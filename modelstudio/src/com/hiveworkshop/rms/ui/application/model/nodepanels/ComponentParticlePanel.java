package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.ParticleEmitter;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;

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

		pathField = new ComponentEditorTextField(24, this::texturePathField);
		topPanel.add(pathField, "");
		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(e -> export());
		topPanel.add(exportButton, "wrap");

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
		longitudePanel.reloadNewValue((float) idObject.getLongitude(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_LONGITUDE), idObject, MdlUtils.TOKEN_LONGITUDE, this::setLongitude);
		latitudePanel.reloadNewValue((float) idObject.getLatitude(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_LATITUDE), idObject, MdlUtils.TOKEN_LATITUDE, this::setLatitude);
		speedPanel.reloadNewValue((float) idObject.getInitVelocity(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_SPEED), idObject, MdlUtils.TOKEN_SPEED, this::setInitVelocity);
		gravityPanel.reloadNewValue((float) idObject.getGravity(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_GRAVITY), idObject, MdlUtils.TOKEN_GRAVITY, this::setGravity);
		emissionPanel.reloadNewValue((float) idObject.getEmissionRate(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_EMISSION_RATE), idObject, MdlUtils.TOKEN_EMISSION_RATE, this::setEmissionRate);
		visibilityPanel.reloadNewValue(1f, (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_EMISSION_RATE), idObject, MdlUtils.TOKEN_EMISSION_RATE, null);
	}

	private void texturePathField(String newPath) {
		idObject.setPath(newPath);
	}

	private void setLongitude(float value){
		if(idObject.getLongitude() != value) {
//			undoManager.pushAction(new xx().redo);
			idObject.setLongitude(value);
		}
	}
	private void setLatitude(float value){
		if(idObject.getLatitude() != value) {
//			undoManager.pushAction(new xx().redo);
			idObject.setLatitude(value);
		}
	}
	private void setInitVelocity(float value){
		if(idObject.getInitVelocity() != value) {
//			undoManager.pushAction(new xx().redo);
			idObject.setInitVelocity(value);
		}
	}
	private void setGravity(float value){
		if(idObject.getGravity() != value) {
//			undoManager.pushAction(new xx().redo);
			idObject.setGravity(value);
		}
	}
	private void setEmissionRate(float value){
		if(idObject.getEmissionRate() != value) {
//			undoManager.pushAction(new xx().redo);
			idObject.setEmissionRate(value);
		}
	}

	private void export(){
		String particlePath = ImportFileActions.convertPathToMDX(idObject.getPath());
		if(!particlePath.isEmpty()){
			FileDialog fileDialog = new FileDialog();
			fileDialog.exportInternalFile(particlePath);
		}
	}
}
