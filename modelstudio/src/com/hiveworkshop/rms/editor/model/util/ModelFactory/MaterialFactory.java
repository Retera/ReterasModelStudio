package com.hiveworkshop.rms.editor.model.util.ModelFactory;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.animflag.IntAnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer;
import com.hiveworkshop.rms.parsers.mdlx.MdlxMaterial;
import com.hiveworkshop.rms.parsers.mdlx.MdlxTexture;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.Vec3;

public class MaterialFactory {
	public static Material makeMaterial(final MdlxMaterial mdlxMaterial, final EditableModel model) {
		Material material = new Material();

		for (final MdlxLayer mdlxLayer : mdlxMaterial.layers) {
			final Layer layer = makeLayer(mdlxLayer, model);

			updateRefs(layer, model);

			material.getLayers().add(layer);
		}

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

		if (ModelUtils.isShaderStringSupported(model.getFormatVersion()) && ((mdlxMaterial.flags & 0x2) != 0)) {
			material.setTwoSided(true);
		}

		material.setShaderString(mdlxMaterial.shader);
		return material;
	}

	public static Layer makeLayer(MdlxLayer mdlxLayer, EditableModel model) {
		Layer layer = new Layer(mdlxLayer.filterMode.toString(), mdlxLayer.textureId);

		int shadingFlags = mdlxLayer.flags;
		if ((shadingFlags & 0x1) != 0) {
			layer.setUnshaded(true);
		}
		if ((shadingFlags & 0x2) != 0) {
			layer.setSphereEnvMap(true);
		}
		if ((shadingFlags & 0x10) != 0) {
			layer.setTwoSided(true);
		}
		if ((shadingFlags & 0x20) != 0) {
			layer.setUnfogged(true);
		}
		if ((shadingFlags & 0x40) != 0) {
			layer.setNoDepthTest(true);
		}
		if ((shadingFlags & 0x80) != 0) {
			layer.setNoDepthSet(true);
		}
		if ((shadingFlags & 0x100) != 0) {
			layer.setUnlit(true);
		}

		layer.setTVertexAnimId(mdlxLayer.textureAnimationId);
		layer.setCoordId((int) mdlxLayer.coordId);
		layer.setStaticAlpha(mdlxLayer.alpha);

		// > 800
		layer.setEmissive(mdlxLayer.emissiveGain);
		// > 900
//		layer.setFresnelColor(new Vec3(ModelUtils.flipRGBtoBGR(mdlxLayer.fresnelColor)));
		layer.setFresnelColor(new Vec3(mdlxLayer.fresnelColor));
		layer.setFresnelOpacity(mdlxLayer.fresnelOpacity);
		layer.setFresnelTeamColor(mdlxLayer.fresnelTeamColor);

		layer.loadTimelines(mdlxLayer, model);
		return layer;
	}

	public static Material createMaterial(MdlxMaterial mdlxMaterial, EditableModel model) {
		Material material = new Material();
		for (final MdlxLayer mdlxLayer : mdlxMaterial.layers) {
			final Layer layer = createLayer(mdlxLayer, model);

			updateRefs(layer, model);

			material.addLayer(layer);
		}

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

		if (ModelUtils.isShaderStringSupported(model.getFormatVersion()) && ((mdlxMaterial.flags & 0x2) != 0)) {
			material.setTwoSided(true);
		}

		material.setShaderString(mdlxMaterial.shader);

		return material;
	}

	static Layer createLayer(MdlxLayer mdlxLayer, EditableModel model) {
		Layer layer = new Layer(mdlxLayer.filterMode.toString(), mdlxLayer.textureId);
		int shadingFlags = mdlxLayer.flags;

		layer.setUnshaded((shadingFlags & 0x1) != 0);
		layer.setSphereEnvMap((shadingFlags & 0x2) != 0);
		layer.setTwoSided((shadingFlags & 0x10) != 0);
		layer.setUnfogged((shadingFlags & 0x20) != 0);
		layer.setNoDepthTest((shadingFlags & 0x40) != 0);
		layer.setNoDepthSet((shadingFlags & 0x80) != 0);
		layer.setUnlit((shadingFlags & 0x100) != 0);

		layer.setTVertexAnimId(mdlxLayer.textureAnimationId);
		layer.setCoordId((int) mdlxLayer.coordId);
		layer.setStaticAlpha(mdlxLayer.alpha);

		// > 800
		layer.setEmissive(mdlxLayer.emissiveGain);
		// > 900
//		layer.setFresnelColor(new Vec3(ModelUtils.flipRGBtoBGR(mdlxLayer.fresnelColor)));
		layer.setFresnelColor(new Vec3(mdlxLayer.fresnelColor));
		layer.setFresnelOpacity(mdlxLayer.fresnelOpacity);
		layer.setFresnelTeamColor(mdlxLayer.fresnelTeamColor);

		layer.loadTimelines(mdlxLayer, model);
		return layer;
	}

	public static Bitmap createBitmap(MdlxTexture texture) {
		Bitmap bitmap = new Bitmap();
		bitmap.setPath(texture.path);
		bitmap.setReplaceableId(texture.replaceableId);
		bitmap.setWrapMode(texture.wrapMode);
		return bitmap;
	}

	public static void updateRefs(Layer layer, EditableModel model) {
		if ((layer.getTextureId() >= 0) && (layer.getTextureId() < model.getTextures().size())) {
			layer.setTexture(model.getTexture(layer.getTextureId()));
		}
		if ((layer.getTVertexAnimId() >= 0) && (layer.getTVertexAnimId() < model.getTexAnims().size())) {
			layer.setTextureAnim(model.getTexAnims().get(layer.getTVertexAnimId()));
		}
		IntAnimFlag txFlag = (IntAnimFlag) layer.find(MdlUtils.TOKEN_TEXTURE_ID);
		if (txFlag != null) {
			layer.buildTextureList(model);
		}
	}
}
