package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;

public class ComponentRibbonEmitterCard extends ComponentNodeCard<RibbonEmitter> {
	@Override
	protected void addTypeRows() {
		beginSection("Ribbon Emitter");
		addComboBox("Material", this::materialOptions, this::describeMaterial, RibbonEmitter::getMaterial,
				RibbonEmitter::setMaterial);
		addDoubleSpinner("Life Span", 0.1, RibbonEmitter::getLifeSpan, RibbonEmitter::setLifeSpan);
		addDoubleSpinner("Gravity", 0.1, RibbonEmitter::getGravity, RibbonEmitter::setGravity);
		addIntSpinner("Emission Rate", 0, Integer.MAX_VALUE, RibbonEmitter::getEmissionRate,
				RibbonEmitter::setEmissionRate);
		addIntSpinner("Rows", 1, Integer.MAX_VALUE, RibbonEmitter::getRows, RibbonEmitter::setRows);
		addIntSpinner("Columns", 1, Integer.MAX_VALUE, RibbonEmitter::getColumns, RibbonEmitter::setColumns);
		endSection();
		addFloatValue("Height Above", RibbonEmitter::getHeightAbove, RibbonEmitter::setHeightAbove, e -> e,
				RibbonEmitter::getAnimFlags, "HeightAbove");
		addFloatValue("Height Below", RibbonEmitter::getHeightBelow, RibbonEmitter::setHeightBelow, e -> e,
				RibbonEmitter::getAnimFlags, "HeightBelow");
		addFloatValue("Alpha", RibbonEmitter::getAlpha, RibbonEmitter::setAlpha, e -> e, RibbonEmitter::getAnimFlags,
				"Alpha");
		addColorValue("Color", RibbonEmitter::getStaticColor, RibbonEmitter::setStaticColor, e -> e,
				RibbonEmitter::getAnimFlags, "Color");
		addFloatValue("Texture Slot", RibbonEmitter::getTextureSlot, RibbonEmitter::setTextureSlot, e -> e,
				RibbonEmitter::getAnimFlags, "TextureSlot");
	}

	private List<Material> materialOptions() {
		return new ArrayList<>(model().getMaterials());
	}

	private String describeMaterial(final Material material) {
		return "Material " + model().getMaterials().indexOf(material) + ": " + material.getName();
	}
}
