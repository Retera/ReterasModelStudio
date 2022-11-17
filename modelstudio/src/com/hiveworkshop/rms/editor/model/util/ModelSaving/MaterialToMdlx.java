package com.hiveworkshop.rms.editor.model.util.ModelSaving;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer;
import com.hiveworkshop.rms.parsers.mdlx.MdlxMaterial;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;

public class MaterialToMdlx {

	public static MdlxMaterial toMdlx(Material material, EditableModel model) {
//		System.out.println("\nMdlxMaterial");
		final MdlxMaterial mdlxMaterial = new MdlxMaterial();

		if (model.getFormatVersion() < 900) {
			for (final Layer layer : material.getLayers()) {
				MdlxLayer mdlxLayer = toMdlx(layer, model);
				mdlxLayer.hdTextureIds.add(mdlxLayer.textureId);
				mdlxMaterial.layers.add(mdlxLayer);
			}
		} else if (model.getFormatVersion() < 1100) {
			for (final Layer layer : material.getLayers()) {
				for (int i = 0; i < layer.getTextures().size(); i++){
					MdlxLayer mdlxLayer = toMdlx(layer, i, model);
					mdlxLayer.hdTextureIds.add(mdlxLayer.textureId);
					mdlxMaterial.layers.add(mdlxLayer);
				}

//				for(Bitmap bitmap : layer.getTextures()){
//					mdlxMaterial.layers.add(toMdlx(layer, model));
//				}
			}
		} else {
			String shaderString = material.getShaderString();
			if(shaderString != null
					&& !shaderString.isEmpty()
					&& !shaderString.equals(Material.SHADER_SD_FIXEDFUNCTION)
					&& !shaderString.equals(Material.SHADER_SD_LEGACY)
//					&& 1<material.getLayers().size()
			){

				for (int i = 0; i <material.getLayers().size(); i++) {
					Layer layer = material.getLayer(i);
					MdlxLayer mdlxLayer = getMdlxLayer(layer, 0, model);

					if(shaderString.equalsIgnoreCase(Material.SHADER_HD_DEFAULT_UNIT)){
						mdlxLayer.hdFlag = 1;
					} else if(shaderString.equalsIgnoreCase(Material.SHADER_HD_CRYSTAL)){
						mdlxLayer.hdFlag = 2;
					} else if(shaderString.equalsIgnoreCase(Material.SHADER_SD_FIXEDFUNCTION)){
						mdlxLayer.hdFlag = 3;
					} else if(shaderString.equalsIgnoreCase(Material.SHADER_SD_LEGACY)){
						mdlxLayer.hdFlag = 4;
					} else if(shaderString.matches("\\w+")){
						mdlxLayer.hdFlag = Integer.parseInt(shaderString);
					} else {
						mdlxLayer.hdFlag = 1;
					}
					for(int j = 0; j<layer.getTextures().size(); j++){
						AnimFlag<?> animFlag = layer.getFlipbookTexture(j);
//						System.out.println("saving texture " + j + ", animflag:" + animFlag);
						if(animFlag != null && 0 < animFlag.size()){
//							System.out.println(animFlag.getName());
							mdlxLayer.hdTextureIds.add(0);
							mdlxLayer.hdTextureSlots.add(0);
							mdlxLayer.textureIdTimelineMap.put(j, animFlag.toMdlx(layer, model));
//							System.out.println("writing timeline for layer: " + i);
//							System.out.println("saving texture " + j + " for layer " + i + ", animflag:" + animFlag.getName());
						} else {
//							System.out.println("writing layer "+ i + ": " + model.getTextureId(layer.getTexture(0)) + ", " + i);
//							System.out.println("saving texture " + j + " for layer " + i + ", animflag:" + animFlag);
							mdlxLayer.hdTextureIds.add(model.getTextureId(layer.getTexture(j)));
							mdlxLayer.hdTextureSlots.add(j);
						}
					}

					for (AnimFlag<?> timeline : layer.getAnimFlags()) {
						if (!timeline.getAnimMap().isEmpty()
								&& !timeline.getName().equals(MdlUtils.TOKEN_TEXTURE_ID)) {
//							System.out.println("writing animflag: " + timeline.getName());
							mdlxLayer.timelines.add(timeline.toMdlx(layer, model));
						}
					}
					mdlxMaterial.layers.add(mdlxLayer);
				}
//				for (int i = 0; i <material.getLayers().size(); i++) {
//					Layer layer = material.getLayer(i);
//					AnimFlag<?> animFlag = layer.find(MdlUtils.TOKEN_TEXTURE_ID);
//					if(animFlag != null && 0 < animFlag.size()){
//						mdlxLayer.hdTextureIds.add(0);
//						mdlxLayer.hdTextureSlots.add(0);
//						mdlxLayer.textureIdTimelineMap.put(i, animFlag.toMdlx(layer, model));
////						System.out.println("writing timeline for layer: " + i);
//					} else {
////						System.out.println("writing layer + "+ i + ": " + model.getTextureId(layer.getTexture(0)) + ", " + i);
//						mdlxLayer.hdTextureIds.add(model.getTextureId(layer.getTexture(0)));
//						mdlxLayer.hdTextureSlots.add(i);
//					}
//				}
//				for (AnimFlag<?> timeline : firstLayer.getAnimFlags()) {
//					if (!timeline.getAnimMap().isEmpty()
//							&& !timeline.getName().equals(MdlUtils.TOKEN_TEXTURE_ID)) {
////						System.out.println("writing animflag: " + timeline.getName());
//						mdlxLayer.timelines.add(timeline.toMdlx(firstLayer, model));
//					}
//				}
//				mdlxMaterial.layers.add(mdlxLayer);


			} else {
				for (final Layer layer : material.getLayers()) {
					MdlxLayer mdlxLayer = toMdlx(layer, model);
					mdlxLayer.hdTextureIds.add(model.getTextureId(layer.getTexture(0)));
					mdlxLayer.hdTextureSlots.add(0);
					mdlxMaterial.layers.add(mdlxLayer);
				}
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

		mdlxMaterial.shader = material.getShaderString();

		return mdlxMaterial;
	}

	public static MdlxLayer toMdlx(Layer layer, EditableModel model) {
		MdlxLayer mdlxLayer = getMdlxLayer(layer,0,  model);
		layer.timelinesToMdlx(mdlxLayer, model);

		return mdlxLayer;
	}
	public static MdlxLayer toMdlx(Layer layer, int slot, EditableModel model) {
		MdlxLayer mdlxLayer = getMdlxLayer(layer, slot, model);
		layer.timelinesToMdlx(mdlxLayer, model);

		return mdlxLayer;
	}

	private static MdlxLayer getMdlxLayer(Layer layer, int slot, EditableModel model) {
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
		return mdlxLayer;
	}
}
