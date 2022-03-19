package com.hiveworkshop.rms.editor.model.util.ModelSaving;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer;
import com.hiveworkshop.rms.parsers.mdlx.MdlxMaterial;

public class MaterialToMdlx {

	public static MdlxMaterial toMdlx(Material material, EditableModel model) {
		final MdlxMaterial mdlxMaterial = new MdlxMaterial();

		for (final Layer layer : material.getLayers()) {
			mdlxMaterial.layers.add(toMdlx(layer, model));
		}

		mdlxMaterial.priorityPlane = material.getPriorityPlane();

		if (material.getConstantColor()) {
			mdlxMaterial.flags |= 0x1;
		}

		if (material.getSortPrimsFarZ()) {
			mdlxMaterial.flags |= 0x10;
		}

		if (material.getFullResolution()) {
			mdlxMaterial.flags |= 0x20;
		}

		if (material.getTwoSided()) {
			mdlxMaterial.flags |= 0x2;
		}

		mdlxMaterial.shader = material.getShaderString();

		return mdlxMaterial;
	}

	public static MdlxLayer toMdlx(Layer layer, EditableModel model) {
		MdlxLayer mdlxLayer = new MdlxLayer();

		mdlxLayer.filterMode = layer.getFilterMode();

		if (layer.getUnshaded()) {
			mdlxLayer.flags |= 0x1;
		}

		if (layer.getSphereEnvMap()) {
			mdlxLayer.flags |= 0x2;
		}

		if (layer.getTwoSided()) {
			mdlxLayer.flags |= 0x10;
		}

		if (layer.getUnfogged()) {
			mdlxLayer.flags |= 0x20;
		}

		if (layer.getNoDepthTest()) {
			mdlxLayer.flags |= 0x40;
		}

		if (layer.getNoDepthSet()) {
			mdlxLayer.flags |= 0x80;
		}

		if (layer.getUnlit()) {
			mdlxLayer.flags |= 0x100;
		}

		mdlxLayer.textureId = layer.getTextureId();
		mdlxLayer.textureAnimationId = layer.getTVertexAnimId();
		mdlxLayer.coordId = layer.getCoordId();
		mdlxLayer.alpha = (float) layer.getStaticAlpha();

		// > 800
		mdlxLayer.emissiveGain = (float) layer.getEmissive();
		// > 900
//		mdlxLayer.fresnelColor = ModelUtils.flipRGBtoBGR(layer.getFresnelColor().toFloatArray());
		mdlxLayer.fresnelColor = layer.getFresnelColor().toFloatArray();
		mdlxLayer.fresnelOpacity = (float) layer.getFresnelOpacity();
		mdlxLayer.fresnelTeamColor = (float) layer.getFresnelTeamColor();

		layer.timelinesToMdlx(mdlxLayer, model);

		return mdlxLayer;
	}
}
