package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.actions.util.ConsumerAction;
import com.hiveworkshop.rms.editor.model.Light;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLight;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.TwiTextEditor.EditorHelpers;
import com.hiveworkshop.rms.util.Vec3;

public class ComponentLightPanel extends ComponentIdObjectPanel<Light> {
	private final EditorHelpers.FloatEditor visibilityPanel;
	private final EditorHelpers.FloatEditor attenuationStartPanel;
	private final EditorHelpers.FloatEditor attenuationEndPanel;
	private final EditorHelpers.FloatEditor intensityPanel;
	private final EditorHelpers.ColorEditor colorPanel;
	private final EditorHelpers.FloatEditor ambIntensityPanel;
	private final EditorHelpers.ColorEditor ambColorPanel;
	private final TwiComboBox<MdlxLight.Type> typeTwiComboBox;

	public ComponentLightPanel(ModelHandler modelHandler, ComponentsPanel componentsPanel) {
		super(modelHandler, componentsPanel);
		visibilityPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_VISIBILITY, null);
		attenuationStartPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_ATTENUATION_START, this::setAttenuationStart);
		attenuationEndPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_ATTENUATION_END, this::setAttenuationEnd);
		intensityPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_INTENSITY, this::setIntensity);
		colorPanel = new EditorHelpers.ColorEditor(modelHandler, this::setColor);
		ambIntensityPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_AMB_INTENSITY, this::setAmbIntensity);
		ambColorPanel = new EditorHelpers.ColorEditor(modelHandler, MdlUtils.TOKEN_AMB_COLOR, this::setAmbColor);

		typeTwiComboBox = new TwiComboBox<>(MdlxLight.Type.values()).addOnSelectItemListener(this::setType);
		topPanel.add(typeTwiComboBox, "spanx, growx, wrap");

		topPanel.add(visibilityPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(attenuationStartPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(attenuationEndPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(intensityPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(colorPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(ambIntensityPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(ambColorPanel.getFlagPanel(), "spanx, growx, wrap");
	}

	@Override
	public void updatePanels() {
		typeTwiComboBox.setSelectedItem(idObject.getType());
		visibilityPanel.update(idObject, 1f);
		attenuationStartPanel.update(idObject, idObject.getAttenuationStart());
		attenuationEndPanel.update(idObject, idObject.getAttenuationEnd());
		intensityPanel.update(idObject, (float) idObject.getIntensity());
		colorPanel.update(idObject, idObject.getStaticColor());
		ambIntensityPanel.update(idObject, (float) idObject.getAmbIntensity());
		ambColorPanel.update(idObject, idObject.getStaticAmbColor());
	}

	private void setType(MdlxLight.Type type) {
		if (idObject.getType() != type) {
			undoManager.pushAction(new ConsumerAction<>(idObject::setType, type, idObject.getType(), "Type").redo());
		}
	}

	private void setAttenuationStart(float value) {
		if (idObject.getAttenuationStart() != value) {
			undoManager.pushAction(new ConsumerAction<>(idObject::setAttenuationStart, value, idObject.getAttenuationStart(), MdlUtils.TOKEN_ATTENUATION_START).redo());
		}
	}

	private void setAttenuationEnd(float value) {
		if (idObject.getAttenuationEnd() != value) {
			undoManager.pushAction(new ConsumerAction<>(idObject::setAttenuationEnd, value, idObject.getAttenuationEnd(), MdlUtils.TOKEN_ATTENUATION_END).redo());
		}
	}

	private void setIntensity(float value) {
		if (idObject.getIntensity() != value) {
			undoManager.pushAction(new ConsumerAction<>(idObject::setIntensity, (double) value, idObject.getIntensity(), MdlUtils.TOKEN_INTENSITY).redo());
		}
	}

	private void setColor(Vec3 value) {
		if (!idObject.getStaticColor().equalLocs(value)) {
			undoManager.pushAction(new ConsumerAction<>(idObject::setStaticColor, value, idObject.getStaticColor(), MdlUtils.TOKEN_STATIC_COLOR).redo());
		}
	}

	private void setAmbIntensity(float value) {
		if (idObject.getAmbIntensity() != value) {
			undoManager.pushAction(new ConsumerAction<>(idObject::setAmbIntensity, (double) value, idObject.getAmbIntensity(), MdlUtils.TOKEN_AMB_INTENSITY).redo());
		}
	}

	private void setAmbColor(Vec3 value) {
		if (!idObject.getStaticAmbColor().equalLocs(value)) {
			undoManager.pushAction(new ConsumerAction<>(idObject::setStaticAmbColor, value, idObject.getStaticAmbColor(), MdlUtils.TOKEN_STATIC_AMB_COLOR).redo());
		}
	}
}
