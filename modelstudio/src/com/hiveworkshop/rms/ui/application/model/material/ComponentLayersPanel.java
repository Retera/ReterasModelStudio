package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.*;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.ShaderBox;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public abstract class ComponentLayersPanel extends ComponentPanel<Material> {
	protected static final Color HIGHLIGHT_BUTTON_BACKGROUND_COLOR = new Color(100, 118, 135);
	protected Material material;
	protected ShaderBox<String> shaderOptionComboBox;
	protected IntEditorJSpinner priorityPlaneSpinner;
	protected JCheckBox twoSided;

	public ComponentLayersPanel(ModelHandler modelHandler) {
		super(modelHandler);
	}

	protected abstract JPanel getTopPanel();

	protected abstract JPanel getLayersHolderPanel();

	protected void setTwoSided() {
//		System.out.println("setTwoSided");
		material.setTwoSided(twoSided.isSelected());
	}

	protected ShaderBox<String> getShaderComboBox() {
		final String[] shaderOptions = {"", "Shader_SD_FixedFunction", "Shader_HD_DefaultUnit"};
		return new ShaderBox<>(shaderOptions, this::setShaderString);
	}

	protected void setShaderString(String newShader) {
		if(!material.getShaderString().equals(newShader)){
			undoManager.pushAction(new SetMaterialShaderStringAction(model, material, newShader, changeListener).redo());
		}
	}

	protected void changePriorityPlane(int newValue) {
		if (material.getPriorityPlane() != newValue) {
			undoManager.pushAction(new SetMaterialPriorityPlaneAction(material, newValue, changeListener).redo());
		}
	}

	protected void deleteMaterial() {
		if (!model.getMaterials().isEmpty()) {
			undoManager.pushAction(new RemoveMaterialAction(material, model, changeListener).redo());
		}
	}

	protected void filterModeDropdownListener(ItemEvent e, Layer layer) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			FilterMode selectedItem = (FilterMode) e.getItem();
			undoManager.pushAction(new SetLayerFilterModeAction(layer, selectedItem, changeListener).redo());
		}
	}

	protected void setCoordId(Layer layer, int value) {
		layer.setCoordId(value);
	}

	protected void duplicateMaterial(){
		Material newMaterial = this.material.deepCopy();
		undoManager.pushAction(new AddMaterialAction(newMaterial, model, ModelStructureChangeListener.changeListener).redo());
	}
}
