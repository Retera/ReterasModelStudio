package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.actions.util.ConsumerAction;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.RibbonEmitter;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.ValueParserUtil;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.TwiTextEditor.EditorHelpers;
import com.hiveworkshop.rms.util.TwiTextEditor.FlagPanel;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;

public class ComponentRibbonPanel extends ComponentIdObjectPanel<RibbonEmitter> {

	private final IntEditorJSpinner rowsSpinner;
	private final IntEditorJSpinner colsSpinner;
	private final EditorHelpers.FloatEditor heightAbovePanel;
	private final EditorHelpers.FloatEditor heightBelowPanel;
	private final EditorHelpers.FloatEditor alphaPanel;
	private final EditorHelpers.IntegerEditor textureSlotPanel;
	private final EditorHelpers.FloatEditor lifeSpanPanel;
	private final EditorHelpers.FloatEditor gravityPanel;
	private final EditorHelpers.IntegerEditor emissionRatePanel;
	private final EditorHelpers.ColorEditor colorPanel;
	private final FlagPanel<Float> visibilityPanel;
	private final TwiComboBox<Material> materialChooser;


	public ComponentRibbonPanel(ModelHandler modelHandler) {
		super(modelHandler);

		topPanel.add(new JLabel("Material:"), "");
		materialChooser = getMaterialChooser();
		topPanel.add(materialChooser, "wrap");

		rowsSpinner = new IntEditorJSpinner(0, 0, 500, this::setRows);
		colsSpinner = new IntEditorJSpinner(0, 0, 500, this::setColumns);

		alphaPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_ALPHA, this::setAlpha);
		textureSlotPanel = new EditorHelpers.IntegerEditor(modelHandler, MdlUtils.TOKEN_TEXTURE_SLOT, this::setTextureSlot);
		heightAbovePanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_HEIGHT_ABOVE, this::setHeightAbove);
		heightBelowPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_HEIGHT_BELOW, this::setHeightBelow);
		lifeSpanPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_LIFE_SPAN, this::setLifeSpan);
		gravityPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_GRAVITY, this::setGravity);

		emissionRatePanel = new EditorHelpers.IntegerEditor(modelHandler, MdlUtils.TOKEN_EMISSION_RATE, this::setEmissionRate);

		colorPanel = new EditorHelpers.ColorEditor(modelHandler, MdlUtils.TOKEN_COLOR, this::setStaticColor);

		visibilityPanel = new FlagPanel<>(MdlUtils.TOKEN_VISIBILITY, ValueParserUtil::parseValueFloat, 1f, modelHandler);


		topPanel.add(new JLabel("Rows:"), "");
		topPanel.add(rowsSpinner, "growx, wrap");
		topPanel.add(new JLabel("Cols:"), "");
		topPanel.add(colsSpinner, "growx, wrap");

		topPanel.add(alphaPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(textureSlotPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(lifeSpanPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(gravityPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(heightAbovePanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(heightBelowPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(visibilityPanel, "spanx, growx, wrap");
	}

	private TwiComboBox<Material> getMaterialChooser() {
		TwiComboBox<Material> materialComboBox = new TwiComboBox<>(modelHandler.getModel().getMaterials(), new Material());
		materialComboBox.setRenderer(new MaterialListRenderer(modelHandler.getModel()));
		materialComboBox.addOnSelectItemListener(this::setMaterial);
		return materialComboBox;
	}
	@Override
	public void updatePanels() {
		materialChooser.setSelectedItem(idObject.getMaterial());
		rowsSpinner.reloadNewValue(idObject.getRows());
		colsSpinner.reloadNewValue(idObject.getColumns());

		alphaPanel.update(idObject, (float) idObject.getAlpha());
		textureSlotPanel.update(idObject, idObject.getTextureSlot());
		heightAbovePanel.update(idObject, (float) idObject.getHeightAbove());
		heightBelowPanel.update(idObject, (float) idObject.getHeightBelow());
		lifeSpanPanel.update(idObject, (float) idObject.getLifeSpan());
		gravityPanel.update(idObject, (float) idObject.getGravity());
		emissionRatePanel.update(idObject, idObject.getEmissionRate());
		colorPanel.update(idObject, idObject.getStaticColor());
		visibilityPanel.update(idObject, idObject.getVisibilityFlag());

	}
	private void setHeightAbove(float value){
		if(value != idObject.getHeightAbove()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setHeightAbove, (double) value, idObject.getHeightAbove(), "HeightAbove").redo());
		}
	}

	private void setHeightBelow(float value){
		if(value != idObject.getHeightBelow()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setHeightBelow, (double) value, idObject.getHeightBelow(), "HeightBelow").redo());
		}
	}

	private void setAlpha(float value){
		if(idObject.getAlpha() != value){
			undoManager.pushAction(new ConsumerAction<>(idObject::setAlpha, (double) value, idObject.getAlpha(), "Alpha").redo());
		}
	}
	private void setRows(int value){
		if(idObject.getRows() != value){
			undoManager.pushAction(new ConsumerAction<>(idObject::setRows, value, idObject.getRows(), "Rows").redo());
		}
	}
	private void setColumns(int value){
		if(idObject.getColumns() != value){
			undoManager.pushAction(new ConsumerAction<>(idObject::setColumns, value, idObject.getColumns(), "Columns").redo());
		}
	}
	private void setLifeSpan(float value){
		if(idObject.getLifeSpan() != value){
			undoManager.pushAction(new ConsumerAction<>(idObject::setLifeSpan, (double) value, idObject.getLifeSpan(), "LifeSpan").redo());
		}
	}

	private void setTextureSlot(int value){
		if(value != idObject.getTextureSlot()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setTextureSlot, value, idObject.getTextureSlot(), "TextureSlot").redo());
		}
	}

	private void setMaterial(Material value){
		if(value != idObject.getMaterial()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setMaterial, value, idObject.getMaterial(), "Material").redo());
		}
	}

	private void setStaticColor(Vec3 value){
		if(value != idObject.getStaticColor()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setStaticColor, value, idObject.getStaticColor(), "StaticColor").redo());
		}
	}

	private void setGravity(float value){
		if(value != idObject.getGravity()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setGravity, (double) value, idObject.getGravity(), "Gravity").redo());
		}
	}

	private void setEmissionRate(int value){
		if(value != idObject.getEmissionRate()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setEmissionRate, value, idObject.getEmissionRate(), "EmissionRate").redo());
		}
	}
}
