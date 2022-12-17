package com.hiveworkshop.rms.editor.model.util.ModelSaving;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer;
import com.hiveworkshop.rms.parsers.mdlx.MdlxMaterial;

public class MaterialToMdlx {
	public static MdlxMaterial toMdlx(Material material, EditableModel model) {
		final MdlxMaterial mdlxMaterial = new MdlxMaterial();

		String shaderString = material.getShaderString();
		mdlxMaterial.shader = material.getShaderString();

		if (model.getFormatVersion() < 1100
				|| shaderString == null
				|| shaderString.isEmpty()
				|| shaderString.equals(Material.SHADER_SD_FIXEDFUNCTION)
				|| shaderString.equals(Material.SHADER_SD_LEGACY)) {
			for (final Layer layer : material.getLayers()) {
				for (int slot = 0; slot < layer.getTextureSlots().size(); slot++){
					MdlxLayer mdlxLayer = getMdlxLayer(layer, slot, model);
					mdlxLayer.hdFlag = getHdFlag(shaderString);
					AnimFlag<?> animFlag = layer.getFlipbookTexture(slot);
					if(animFlag != null && 0 < animFlag.size()){
						mdlxLayer.hdTextureIds.add(0);
						mdlxLayer.hdTextureSlots.add(0);
						mdlxLayer.timelines.add(animFlag.toMdlx(layer, model));
					} else {
						mdlxLayer.hdTextureIds.add(model.getTextureId(layer.getTexture(slot)));
						mdlxLayer.hdTextureSlots.add(slot);
					}
					mdlxMaterial.layers.add(mdlxLayer);
				}
			}
		} else {
			for (Layer layer : material.getLayers()) {
				int slot1 = 0;
				MdlxLayer mdlxLayer = getMdlxLayer(layer, slot1, model);
				mdlxLayer.hdFlag = getHdFlag(shaderString);

				for (int slot = 0; slot < layer.getTextureSlots().size(); slot++) {
					AnimFlag<?> animFlag = layer.getFlipbookTexture(slot);
					if (animFlag != null && 0 < animFlag.size()) {
						mdlxLayer.hdTextureIds.add(0);
						mdlxLayer.hdTextureSlots.add(0);
						mdlxLayer.textureIdTimelineMap.put(slot, animFlag.toMdlx(layer, model));
					} else {
						mdlxLayer.hdTextureIds.add(model.getTextureId(layer.getTexture(slot)));
						mdlxLayer.hdTextureSlots.add(slot);
					}
				}
				mdlxMaterial.layers.add(mdlxLayer);
			}

		}

		mdlxMaterial.priorityPlane = material.getPriorityPlane();

		if (material.getConstantColor()) {
			mdlxMaterial.flags |= 0x1;
		}

		if (material.getSortPrimsFarZ()) {
			mdlxMaterial.flags |= 0x10;
		}
		if (material.getSortPrimsNearZ()) {
			mdlxMaterial.flags |= 0x8;
		}

		if (material.getFullResolution()) {
			mdlxMaterial.flags |= 0x20;
		}

		if (material.getTwoSided()) {
			mdlxMaterial.flags |= 0x2;
		}

		return mdlxMaterial;
	}

	private static int getHdFlag(String shaderString) {
		if (shaderString == null || shaderString.isEmpty()) {
			return 0;
		} else if(shaderString.equalsIgnoreCase(Material.SHADER_HD_DEFAULT_UNIT)){
			return 1;
		} else if(shaderString.equalsIgnoreCase(Material.SHADER_HD_CRYSTAL)){
			return 2;
		} else if(shaderString.equalsIgnoreCase(Material.SHADER_SD_FIXEDFUNCTION)){
			return 3;
		} else if(shaderString.equalsIgnoreCase(Material.SHADER_SD_LEGACY)){
			return 4;
		} else if(shaderString.matches("\\w+")){
			return Integer.parseInt(shaderString);
		} else {
			return 1;
		}
	}

	public static MdlxLayer toMdlx(Layer layer, int slot, EditableModel model) {
		MdlxLayer mdlxLayer = getMdlxLayer(layer, slot, model);
		if(slot == 0) {
			mdlxLayer.timelines.addAll(layer.timelinesToMdlx(model));
		}
		AnimFlag<?> animFlag = layer.getFlipbookTexture(slot);
		if(animFlag != null && 0 < animFlag.size()){
			mdlxLayer.timelines.add(animFlag.toMdlx(layer, model));
		}

		return mdlxLayer;
	}

	private static MdlxLayer getMdlxLayer(Layer layer, int slot, EditableModel model) {
		MdlxLayer mdlxLayer = new MdlxLayer();

		mdlxLayer.filterMode = layer.getFilterMode();
		for (Layer.flag flag : layer.getFlags()){
			mdlxLayer.flags |= flag.getFlagBit();
		}

		mdlxLayer.textureId = model.getTextureId(layer.getTexture(slot));
		mdlxLayer.textureAnimationId = model.getTextureAnimId(layer.getTextureAnim());
		mdlxLayer.coordId = layer.getCoordId();
		mdlxLayer.alpha = (float) layer.getStaticAlpha();

		// > 800
		mdlxLayer.emissiveGain = (float) layer.getEmissive();
		// > 900
//		mdlxLayer.fresnelColor = ModelUtils.flipRGBtoBGR(layer.getFresnelColor().toFloatArray());
		mdlxLayer.fresnelColor = layer.getFresnelColor().toFloatArray();
		mdlxLayer.fresnelOpacity = (float) layer.getFresnelOpacity();
		mdlxLayer.fresnelTeamColor = (float) layer.getFresnelTeamColor();

		if(slot == 0) {
			mdlxLayer.timelines.addAll(layer.timelinesToMdlx(model));
		}
		return mdlxLayer;
	}
}
