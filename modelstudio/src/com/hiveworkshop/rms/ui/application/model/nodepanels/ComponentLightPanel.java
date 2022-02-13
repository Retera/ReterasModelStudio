package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.Light;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLight;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.Vec3ValuePanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.Vec3;

public class ComponentLightPanel extends ComponentIdObjectPanel<Light> {
	private final FloatValuePanel visibilityPanel;
	private final FloatValuePanel attenuationStartPanel;
	private final FloatValuePanel attenuationEndPanel;
	private final FloatValuePanel intensityPanel;
	private final Vec3ValuePanel colorPanel;
	private final FloatValuePanel ambIntensityPanel;
	private final Vec3ValuePanel ambColorPanel;
	private final TwiComboBox<MdlxLight.Type> typeTwiComboBox;

	public ComponentLightPanel(ModelHandler modelHandler) {
		super(modelHandler);
		visibilityPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_VISIBILITY);
		attenuationStartPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_ATTENUATION_START);
		attenuationEndPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_ATTENUATION_END);
		intensityPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_INTENSITY);
		colorPanel = new Vec3ValuePanel(modelHandler, MdlUtils.TOKEN_COLOR);
		ambIntensityPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_AMB_INTENSITY);
		ambColorPanel = new Vec3ValuePanel(modelHandler, MdlUtils.TOKEN_AMB_COLOR);

		typeTwiComboBox = new TwiComboBox<>(MdlxLight.Type.values()).addOnSelectItemListener(this::setType);
		topPanel.add(typeTwiComboBox, "spanx, growx, wrap");

		topPanel.add(visibilityPanel, "spanx, growx, wrap");
		topPanel.add(attenuationStartPanel, "spanx, growx, wrap");
		topPanel.add(attenuationEndPanel, "spanx, growx, wrap");
		topPanel.add(intensityPanel, "spanx, growx, wrap");
		topPanel.add(colorPanel, "spanx, growx, wrap");
		topPanel.add(ambIntensityPanel, "spanx, growx, wrap");
		topPanel.add(ambColorPanel, "spanx, growx, wrap");
	}

	@Override
	public void updatePanels() {
		typeTwiComboBox.setSelectedItem(idObject.getType());
		visibilityPanel.reloadNewValue(1f, (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_VISIBILITY), idObject, MdlUtils.TOKEN_VISIBILITY, null);
		attenuationStartPanel.reloadNewValue(1f, (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_ATTENUATION_START), idObject, MdlUtils.TOKEN_ATTENUATION_START, this::setAttenuationStart);
		attenuationEndPanel.reloadNewValue(1f, (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_ATTENUATION_END), idObject, MdlUtils.TOKEN_ATTENUATION_END, this::setAttenuationEnd);
		intensityPanel.reloadNewValue(1f, (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_INTENSITY), idObject, MdlUtils.TOKEN_INTENSITY, this::setIntensity);
		colorPanel.reloadNewValue(new Vec3(255, 255, 255), (Vec3AnimFlag) idObject.find(MdlUtils.TOKEN_COLOR), idObject, MdlUtils.TOKEN_COLOR, this::setColor);
		ambIntensityPanel.reloadNewValue(1f, (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_AMB_INTENSITY), idObject, MdlUtils.TOKEN_AMB_INTENSITY, this::setAmbIntensity);
		ambColorPanel.reloadNewValue(new Vec3(255, 255, 255), (Vec3AnimFlag) idObject.find(MdlUtils.TOKEN_AMB_COLOR), idObject, MdlUtils.TOKEN_AMB_COLOR, this::setAmbColor);
	}

	private void setType(MdlxLight.Type type){
		if(idObject.getType() != type){
//			undoManager.pushAction(new xx().redo);
			idObject.setType(type);
		}
	}

	private void setAttenuationStart(float value){
		if(idObject.getAttenuationStart() != value) {
//			undoManager.pushAction(new xx().redo);
			idObject.setAttenuationStart(value);
		}
	}

	private void setAttenuationEnd(float value){
		if(idObject.getAttenuationEnd() != value) {
//			undoManager.pushAction(new xx().redo);
			idObject.setAttenuationEnd(value);
		}
	}

	private void setIntensity(float value){
		if(idObject.getIntensity() != value) {
//			undoManager.pushAction(new xx().redo);
			idObject.setIntensity(value);
		}
	}

	private void setColor(Vec3 value){
		if(idObject.getStaticColor() != value) {
//			undoManager.pushAction(new xx().redo);
			idObject.setStaticColor(value);
		}
	}

	private void setAmbIntensity(float value){
		if(idObject.getAmbIntensity() != value) {
//			undoManager.pushAction(new xx().redo);
			idObject.setAmbIntensity(value);
		}
	}

	private void setAmbColor(Vec3 value){
		if(idObject.getStaticAmbColor() != value) {
//			undoManager.pushAction(new xx().redo);
			idObject.setStaticAmbColor(value);
		}
	}
}
