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
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MaterialFactory {
	public static Material createMaterial(MdlxMaterial mdlxMaterial, EditableModel model) {

		List<Layer> layers = new ArrayList<>();
		if(model.getFormatVersion() < 900
				|| 1000 < model.getFormatVersion()
				|| !mdlxMaterial.shader.equals(Material.SHADER_HD_DEFAULT_UNIT)
				&& !mdlxMaterial.shader.equals(Material.SHADER_HD_CRYSTAL)){
			for (final MdlxLayer mdlxLayer : mdlxMaterial.layers) {
				layers.add(createLayer(Collections.singletonList(mdlxLayer), model));
			}
		} else {
			layers.add(createLayer(mdlxMaterial.layers, model));
		}
		Material material = new Material(layers);

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
		Layer layer = new Layer();
		for (int slot = 0; slot < mdlxLayers.size(); slot++) {
			MdlxLayer mdlxLayer = mdlxLayers.get(slot);
			for (int i = 0; i < mdlxLayer.hdTextureIds.size(); i++){
				layer.setTexture(slot+i, model.getTexture(mdlxLayer.hdTextureIds.get(i)));
				MdlxTimeline<?> timeline = mdlxLayer.textureIdTimelineMap.get(i);
				if(timeline != null){
					AnimFlag<?> animFlag = AnimFlag.createFromTimeline(timeline, model);
					if(animFlag instanceof BitmapAnimFlag){
						layer.setFlipbookTexture(slot+i, (BitmapAnimFlag) animFlag);
					}
				}
			}
		}
		MdlxLayer mdlxLayer = mdlxLayers.get(0);
		layer.setFilterMode(mdlxLayer.filterMode);

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
		layer.setFresnelColor(new Vec3(mdlxLayer.fresnelColor));
		layer.setFresnelOpacity(mdlxLayer.fresnelOpacity);
		layer.setFresnelTeamColor(mdlxLayer.fresnelTeamColor);

		for (MdlxTimeline<?> timeline : mdlxLayer.timelines) {
			System.out.println("adding other timeline: " + timeline.name);
			AnimFlag<?> fromTimeline = AnimFlag.createFromTimeline(timeline, model);
			layer.add(fromTimeline);
		}
		return layer;
	}

	public static Bitmap createBitmap(MdlxTexture texture) {
		Bitmap bitmap = new Bitmap();
		bitmap.setPath(texture.path);
		bitmap.setReplaceableId(texture.replaceableId);

		bitmap.setWrapWidth((texture.wrapFlag & 0x1) != 0);
		bitmap.setWrapHeight((texture.wrapFlag & 0x2) != 0);
		return bitmap;
	}
}
