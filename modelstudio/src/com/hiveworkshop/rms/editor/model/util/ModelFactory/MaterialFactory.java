package com.hiveworkshop.rms.editor.model.util.ModelFactory;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.BitmapAnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer;
import com.hiveworkshop.rms.parsers.mdlx.MdlxMaterial;
import com.hiveworkshop.rms.parsers.mdlx.MdlxTexture;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MaterialFactory {
	public static Material createMaterial(MdlxMaterial mdlxMaterial, EditableModel model) {
		Material material = new Material();
		List<Layer> layers = new ArrayList<>();
		if(model.getFormatVersion() < 900
				|| 1000 < model.getFormatVersion()
				|| !mdlxMaterial.shader.equals(Material.SHADER_HD_DEFAULT_UNIT)
				&& !mdlxMaterial.shader.equals(Material.SHADER_HD_CRYSTAL)){
			for (final MdlxLayer mdlxLayer : mdlxMaterial.layers) {
				layers.add(createLayer(mdlxLayer, model));
			}
		} else {
			layers.add(createLayer(mdlxMaterial.layers, model));
		}
		material.setLayers(layers);

		material.setPriorityPlane(mdlxMaterial.priorityPlane);

		if ((mdlxMaterial.flags & 0x1) != 0) {
			material.setConstantColor(true);
		}

		if ((mdlxMaterial.flags & 0x10) != 0) {
			material.setSortPrimsFarZ(true);
		}

		if ((mdlxMaterial.flags & 0x20) != 0) {
			material.setFullResolution(true);
		}

		if (((mdlxMaterial.flags & 0x2) != 0)) {
			material.setTwoSided(true);
		}
//		if (ModelUtils.isShaderStringSupported(model.getFormatVersion()) && ((mdlxMaterial.flags & 0x2) != 0)) {
//			material.setTwoSided(true);
//		}

		material.setShaderString(mdlxMaterial.shader);
		if(1000 < model.getFormatVersion() && mdlxMaterial.layers.get(0).hdFlag != 0){
			if(mdlxMaterial.layers.get(0).hdFlag == 2){
				material.setShaderString(Material.SHADER_HD_CRYSTAL);
			} else {
				material.setShaderString(Material.SHADER_HD_DEFAULT_UNIT);
			}
		}

		return material;
	}

	static Layer createLayer(List<MdlxLayer> mdlxLayers, EditableModel model) {
		List<Bitmap> textures = new ArrayList<>();
		for (MdlxLayer mdlxLayer : mdlxLayers) {
			textures.add(model.getTexture(mdlxLayer.hdTextureIds.get(0)));

		}
		MdlxLayer mdlxLayer = mdlxLayers.get(0);
		Layer layer = new Layer(mdlxLayer.filterMode, textures);

		int shadingFlags = mdlxLayer.flags;

		layer.setUnshaded((shadingFlags & 0x1) != 0);
		layer.setSphereEnvMap((shadingFlags & 0x2) != 0);
		layer.setTwoSided((shadingFlags & 0x10) != 0);
		layer.setUnfogged((shadingFlags & 0x20) != 0);
		layer.setNoDepthTest((shadingFlags & 0x40) != 0);
		layer.setNoDepthSet((shadingFlags & 0x80) != 0);
		layer.setUnlit((shadingFlags & 0x100) != 0);

		layer.setTextureAnim(model.getTexAnim(mdlxLayer.textureAnimationId));
		layer.setCoordId((int) mdlxLayer.coordId);
		layer.setStaticAlpha(mdlxLayer.alpha);

		// > 800
		layer.setEmissive(mdlxLayer.emissiveGain);
		// > 900
//		    layer.setFresnelColor(new Vec3(ModelUtils.flipRGBtoBGR(mdlxLayer.fresnelColor)));
		layer.setFresnelColor(new Vec3(mdlxLayer.fresnelColor));
		layer.setFresnelOpacity(mdlxLayer.fresnelOpacity);
		layer.setFresnelTeamColor(mdlxLayer.fresnelTeamColor);

		layer.loadTimelines(mdlxLayer, model);

		for (int i = 0; i < mdlxLayer.hdTextureIds.size(); i++){
			if(mdlxLayer.textureIdTimelineMap.get(i) != null){
				AnimFlag<?> animFlag = AnimFlag.createFromTimeline(mdlxLayer.textureIdTimelineMap.get(i), model);
				if(animFlag instanceof BitmapAnimFlag){
					layer.setFlipbookTexture(i, (BitmapAnimFlag) animFlag);
				}
				layer.add(animFlag);
			}
		}
//		for (int i = 0; i < mdlxLayer.hdTextureIds.size(); i++){
//			Layer layer = new Layer(mdlxLayer.filterMode, model.getTexture(mdlxLayer.hdTextureIds.get(i)));
//			int shadingFlags = mdlxLayer.flags;
//
//			layer.setUnshaded((shadingFlags & 0x1) != 0);
//			layer.setSphereEnvMap((shadingFlags & 0x2) != 0);
//			layer.setTwoSided((shadingFlags & 0x10) != 0);
//			layer.setUnfogged((shadingFlags & 0x20) != 0);
//			layer.setNoDepthTest((shadingFlags & 0x40) != 0);
//			layer.setNoDepthSet((shadingFlags & 0x80) != 0);
//			layer.setUnlit((shadingFlags & 0x100) != 0);
//
//			layer.setTextureAnim(model.getTexAnim(mdlxLayer.textureAnimationId));
//			layer.setCoordId((int) mdlxLayer.coordId);
//			layer.setStaticAlpha(mdlxLayer.alpha);
//
//			// > 800
//			layer.setEmissive(mdlxLayer.emissiveGain);
//			// > 900
////		    layer.setFresnelColor(new Vec3(ModelUtils.flipRGBtoBGR(mdlxLayer.fresnelColor)));
//			layer.setFresnelColor(new Vec3(mdlxLayer.fresnelColor));
//			layer.setFresnelOpacity(mdlxLayer.fresnelOpacity);
//			layer.setFresnelTeamColor(mdlxLayer.fresnelTeamColor);
//
//			if(i == 0){
//				// loads all animated stuff into the first layer (in case of v1100)
//				layer.loadTimelines(mdlxLayer, model);
//			}
//			if(mdlxLayer.textureIdTimelineMap.get(i) != null){
//				layer.add(AnimFlag.createFromTimeline(mdlxLayer.textureIdTimelineMap.get(i), model));
//			}
//			layers.add(layer);
//		}
		return layer;
	}
	static Layer createLayer(MdlxLayer mdlxLayer, EditableModel model) {
		List<Bitmap> textures = new ArrayList<>();
		for (int i = 0; i < mdlxLayer.hdTextureIds.size(); i++){
			textures.add(model.getTexture(mdlxLayer.hdTextureIds.get(i)));
		}
		Layer layer = new Layer(mdlxLayer.filterMode, textures);
		int shadingFlags = mdlxLayer.flags;

		layer.setUnshaded((shadingFlags & 0x1) != 0);
		layer.setSphereEnvMap((shadingFlags & 0x2) != 0);
		layer.setTwoSided((shadingFlags & 0x10) != 0);
		layer.setUnfogged((shadingFlags & 0x20) != 0);
		layer.setNoDepthTest((shadingFlags & 0x40) != 0);
		layer.setNoDepthSet((shadingFlags & 0x80) != 0);
		layer.setUnlit((shadingFlags & 0x100) != 0);

		layer.setTextureAnim(model.getTexAnim(mdlxLayer.textureAnimationId));
		layer.setCoordId((int) mdlxLayer.coordId);
		layer.setStaticAlpha(mdlxLayer.alpha);

		// > 800
		layer.setEmissive(mdlxLayer.emissiveGain);
		// > 900
//		    layer.setFresnelColor(new Vec3(ModelUtils.flipRGBtoBGR(mdlxLayer.fresnelColor)));
		layer.setFresnelColor(new Vec3(mdlxLayer.fresnelColor));
		layer.setFresnelOpacity(mdlxLayer.fresnelOpacity);
		layer.setFresnelTeamColor(mdlxLayer.fresnelTeamColor);

		layer.loadTimelines(mdlxLayer, model);

		for (int i = 0; i < mdlxLayer.hdTextureIds.size(); i++){
			if(mdlxLayer.textureIdTimelineMap.get(i) != null){
				AnimFlag<?> animFlag = AnimFlag.createFromTimeline(mdlxLayer.textureIdTimelineMap.get(i), model);
				if(animFlag instanceof BitmapAnimFlag){
					layer.setFlipbookTexture(i, (BitmapAnimFlag) animFlag);
				}
				layer.add(animFlag);
			}
		}
//		for (int i = 0; i < mdlxLayer.hdTextureIds.size(); i++){
//			Layer layer = new Layer(mdlxLayer.filterMode, model.getTexture(mdlxLayer.hdTextureIds.get(i)));
//			int shadingFlags = mdlxLayer.flags;
//
//			layer.setUnshaded((shadingFlags & 0x1) != 0);
//			layer.setSphereEnvMap((shadingFlags & 0x2) != 0);
//			layer.setTwoSided((shadingFlags & 0x10) != 0);
//			layer.setUnfogged((shadingFlags & 0x20) != 0);
//			layer.setNoDepthTest((shadingFlags & 0x40) != 0);
//			layer.setNoDepthSet((shadingFlags & 0x80) != 0);
//			layer.setUnlit((shadingFlags & 0x100) != 0);
//
//			layer.setTextureAnim(model.getTexAnim(mdlxLayer.textureAnimationId));
//			layer.setCoordId((int) mdlxLayer.coordId);
//			layer.setStaticAlpha(mdlxLayer.alpha);
//
//			// > 800
//			layer.setEmissive(mdlxLayer.emissiveGain);
//			// > 900
////		    layer.setFresnelColor(new Vec3(ModelUtils.flipRGBtoBGR(mdlxLayer.fresnelColor)));
//			layer.setFresnelColor(new Vec3(mdlxLayer.fresnelColor));
//			layer.setFresnelOpacity(mdlxLayer.fresnelOpacity);
//			layer.setFresnelTeamColor(mdlxLayer.fresnelTeamColor);
//
//			if(i == 0){
//				// loads all animated stuff into the first layer (in case of v1100)
//				layer.loadTimelines(mdlxLayer, model);
//			}
//			if(mdlxLayer.textureIdTimelineMap.get(i) != null){
//				layer.add(AnimFlag.createFromTimeline(mdlxLayer.textureIdTimelineMap.get(i), model));
//			}
//			layers.add(layer);
//		}
//		List<Layer> layers = new ArrayList<>();
//		layers.add(layer);
		return layer;
	}

	public static Bitmap createBitmap(MdlxTexture texture) {
		Bitmap bitmap = new Bitmap();
		bitmap.setPath(texture.path);
		bitmap.setReplaceableId(texture.replaceableId);
		bitmap.setWrapMode(texture.wrapMode);
		return bitmap;
	}
}
