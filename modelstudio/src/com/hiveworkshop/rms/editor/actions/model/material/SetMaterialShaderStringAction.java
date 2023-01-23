package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.AddBitmapAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.BitmapAnimFlag;
import com.hiveworkshop.rms.editor.model.util.HD_Material_Layer;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetMaterialShaderStringAction implements UndoAction {
	private final Material material;
	private final String prevShader;
	private final String newShader;
	private final List<Layer> layers = new ArrayList<>();
	private final List<Layer> newLayers = new ArrayList<>();
	private final ModelStructureChangeListener changeListener;
	private final List<UndoAction> addTextureActions = new ArrayList<>();

	public SetMaterialShaderStringAction(EditableModel model, Material material, String newShader, ModelStructureChangeListener changeListener) {
		this.material = material;
		this.layers.addAll(material.getLayers());
		this.prevShader = material.getShaderString();
		this.newShader = newShader;
		this.changeListener = changeListener;
//		if ((prevShader.isBlank() || prevShader.contains("_SD_")) && newShader.equals("Shader_HD_DefaultUnit")) {
		if ((prevShader.isBlank() || prevShader.contains("_SD_")) && newShader.contains("_HD_")) {
			sdToHd(material);
		} else if ((newShader.isBlank() || newShader.contains("_SD_")) && prevShader.contains("_HD_")) {
			hdToSd(material);
//		} else if (prevShader.equals("Shader_SD_FixedFunction")){
//			Layer diffuseLayer = material.getLayers().get(0).deepCopy();
//			if (material.getTwoSided()) {
//				diffuseLayer.setTwoSided(true);
//			}
//			final AnimFlag<?> flag = diffuseLayer.find("Emissive");
//			if (flag != null) {
//				diffuseLayer.remove(flag);
//			}
//			newLayers.add(diffuseLayer);
//			if(diffuseLayer.getFilterMode() == FilterMode.TRANSPARENT || diffuseLayer.getFilterMode() == FilterMode.BLEND){
//				Layer teamColorLayer = new Layer(new Bitmap("", 1));
//				teamColorLayer.setFilterMode(FilterMode.TRANSPARENT);
//				newLayers.add(0, teamColorLayer);
//			}
		}

//		if(prevShader.equals("Shader_HD_DefaultUnit")){
//			for(Layer layer : material.getLayers()){
//				Layer newLayer = layer.deepCopy();
//				if (material.getTwoSided()) {
//					newLayer.setTwoSided(true);
//				}
//				final AnimFlag<?> flag = newLayer.find("Emissive");
//				if (flag != null) {
//					newLayer.remove(flag);
//				}
//				newLayers.add(newLayer);
////				if(newLayer.getFilterMode() == FilterMode.TRANSPARENT || newLayer.getFilterMode() == FilterMode.BLEND){
////					Layer teamColorLayer = new Layer(new Bitmap("", 1));
////					teamColorLayer.setFilterMode(FilterMode.TRANSPARENT);
////					newLayers.add(0, teamColorLayer);
////				}
//			}
////		} else if (prevShader.equals("Shader_SD_FixedFunction")){
////			Layer diffuseLayer = material.getLayers().get(0).deepCopy();
////			if (material.getTwoSided()) {
////				diffuseLayer.setTwoSided(true);
////			}
////			final AnimFlag<?> flag = diffuseLayer.find("Emissive");
////			if (flag != null) {
////				diffuseLayer.remove(flag);
////			}
////			newLayers.add(diffuseLayer);
////			if(diffuseLayer.getFilterMode() == FilterMode.TRANSPARENT || diffuseLayer.getFilterMode() == FilterMode.BLEND){
////				Layer teamColorLayer = new Layer(new Bitmap("", 1));
////				teamColorLayer.setFilterMode(FilterMode.TRANSPARENT);
////				newLayers.add(0, teamColorLayer);
////			}
//		} else if (newShader.equals("Shader_HD_DefaultUnit")){
//
//			for(Layer layer : material.getLayers()){
//				if(newLayers.size() < HD_Material_Layer.values().length){
//					Layer newLayer = layer.deepCopy();
//					newLayers.add(newLayer);
//				} else {
//					newLayers.add(new Layer(HD_Material_Layer.values()[newLayers.size()].getPlaceholderBitmap()));
//				}
//			}
//			for(int i = 0; i< HD_Material_Layer.values().length; i++){
//				Layer layer = material.getLayer(i);
//				if(layer != null){
//					newLayers.add(layer.deepCopy());
//				} else {
//					newLayers.add(new Layer(HD_Material_Layer.values()[newLayers.size()].getPlaceholderBitmap()));
//				}
//			}
////			if (!material.getLayers().isEmpty()) {
////				newLayers.add(material.getLayers().stream().filter(layer -> !layer.getTextureBitmap().getPath().equals("")).findFirst().orElse(material.getLayers().get(0)));
////			} else {
////				newLayers.add(new Layer(getBitmap("Textures\\White.dds")));
////			}
////			newLayers.add(HD_Material_Layer.VERTEX.ordinal(), new Layer(getBitmap("Textures\\normal.dds")));
////			newLayers.add(HD_Material_Layer.ORM.ordinal(), new Layer(getBitmap("Textures\\orm.dds")));
////			newLayers.add(HD_Material_Layer.EMISSIVE.ordinal(), new Layer(getBitmap("Textures\\Black32.dds")));
////			newLayers.add(HD_Material_Layer.TEAM_COLOR.ordinal(), new Layer(new Bitmap("", 1)));
////			newLayers.add(HD_Material_Layer.REFLECTIONS.ordinal(), new Layer(getBitmap("ReplaceableTextures\\EnvironmentMap.dds")));
//		}


//		if(newShader.equals("Shader_HD_DefaultUnit")){
//			if (!material.getLayers().isEmpty()) {
//				newLayers.add(material.getLayers().stream().filter(layer -> !layer.getTextureBitmap().getPath().equals("")).findFirst().orElse(material.getLayers().get(0)));
//			} else {
////				newLayers.add(new Layer(getBitmap("Textures\\White.dds")));
//				newLayers.add(new Layer(HD_Material_Layer.DIFFUSE.getPlaceholderBitmap()));
//			}
//			newLayers.add(HD_Material_Layer.VERTEX.ordinal(), new Layer(HD_Material_Layer.VERTEX.getPlaceholderBitmap()));
//			newLayers.add(HD_Material_Layer.ORM.ordinal(), new Layer(HD_Material_Layer.ORM.getPlaceholderBitmap()));
//			newLayers.add(HD_Material_Layer.EMISSIVE.ordinal(), new Layer(HD_Material_Layer.EMISSIVE.getPlaceholderBitmap()));
//			newLayers.add(HD_Material_Layer.TEAM_COLOR.ordinal(), new Layer(HD_Material_Layer.TEAM_COLOR.getPlaceholderBitmap()));
//			newLayers.add(HD_Material_Layer.REFLECTIONS.ordinal(), new Layer(HD_Material_Layer.REFLECTIONS.getPlaceholderBitmap()));
//		} else if (newShader.equals("Shader_SD_FixedFunction")){
//			Layer diffuseLayer = material.getLayers().get(0).deepCopy();
//			if (material.getTwoSided()) {
//				diffuseLayer.setTwoSided(true);
//			}
//			final AnimFlag<?> flag = diffuseLayer.find("Emissive");
//			if (flag != null) {
//				diffuseLayer.remove(flag);
//			}
//			newLayers.add(diffuseLayer);
//			if(diffuseLayer.getFilterMode() == FilterMode.TRANSPARENT || diffuseLayer.getFilterMode() == FilterMode.BLEND){
//				Layer teamColorLayer = new Layer(new Bitmap("", 1));
//				teamColorLayer.setFilterMode(FilterMode.TRANSPARENT);
//				newLayers.add(0, teamColorLayer);
//			}
//		} else {
//			Layer diffuseLayer = material.getLayers().get(0).deepCopy();
//			if (material.getTwoSided()) {
//				diffuseLayer.setTwoSided(true);
//			}
//			final AnimFlag<?> flag = diffuseLayer.find("Emissive");
//			if (flag != null) {
//				diffuseLayer.remove(flag);
//			}
//			newLayers.add(diffuseLayer);
//			if(diffuseLayer.getFilterMode() == FilterMode.TRANSPARENT || diffuseLayer.getFilterMode() == FilterMode.BLEND){
//				Layer teamColorLayer = new Layer(new Bitmap("", 1));
//				teamColorLayer.setFilterMode(FilterMode.TRANSPARENT);
//				newLayers.add(0, teamColorLayer);
//			}
//		}


		Set<Bitmap> bitmaps = new HashSet<>(model.getTextures());
		for (Layer layer : newLayers) {
			if (layer.getTextures() != null && !layer.getTextures().isEmpty()) {
				for (Bitmap bitmap1 : layer.getTextures()) {
					if (bitmap1 != null && !bitmaps.contains(bitmap1)) {
						addTextureActions.add(new AddBitmapAction(bitmap1, model, null));
					}
				}
			}
		}
	}

	private void hdToSd(Material material) {
		for (Layer layer : material.getLayers()) {
			for (Layer.Texture texture: layer.getTextureSlots()) {
				Layer newLayer = new Layer();
				newLayers.add(newLayer);
				newLayer.setFilterMode(layer.getFilterMode());
				newLayer.setTexture(0, texture.getTexture());
				newLayer.setCoordId(layer.getCoordId());
				newLayer.setTextureAnim(layer.getTextureAnim());
				newLayer.setEmissive(layer.getEmissive());

				newLayer.setFresnelColor(layer.getFresnelColor());
				newLayer.setFresnelOpacity(layer.getFresnelOpacity());
				newLayer.setFresnelTeamColor(layer.getFresnelTeamColor());
				newLayer.setStaticAlpha(layer.getStaticAlpha());

				newLayer.setFlags(layer.getFlags());

				AnimFlag<Float> visibilityFlag = layer.getVisibilityFlag();
				if (visibilityFlag != null) {
					newLayer.add(visibilityFlag.deepCopy());
				}

				if (texture.getFlipbookTexture() != null) {
					newLayer.setFlipbookTexture(0, texture.getFlipbookTexture());
				}
				if (material.getTwoSided()) {
					newLayer.setTwoSided(true);
				}
				newLayers.add(newLayer);
			}
		}
	}

	private void sdToHd(Material material) {
		Layer newLayer = new Layer();
		newLayers.add(newLayer);
		for (int i = 0; i < HD_Material_Layer.values().length; i++) {
			Layer layer = material.getLayer(i);
			if (layer != null) {
				newLayer.setFilterMode(layer.getFilterMode());
				newLayer.setTexture(i, layer.getTexture(0));
				newLayer.setCoordId(layer.getCoordId());
				newLayer.setTextureAnim(layer.getTextureAnim());
				newLayer.setEmissive(layer.getEmissive());

				newLayer.setFresnelColor(layer.getFresnelColor());
				newLayer.setFresnelOpacity(layer.getFresnelOpacity());
				newLayer.setFresnelTeamColor(layer.getFresnelTeamColor());
				newLayer.setStaticAlpha(layer.getStaticAlpha());

				newLayer.setFlags(layer.getFlags());

				AnimFlag<Float> visibilityFlag = layer.getVisibilityFlag();
				if (visibilityFlag != null) {
					newLayer.add(visibilityFlag.deepCopy());
				}

				if(layer.getTextureSlot(0).getFlipbookTexture() != null){
					newLayer.setFlipbookTexture(i, (BitmapAnimFlag) layer.getTextureSlot(0).getFlipbookTexture().deepCopy());
				}
			} else {
//				newLayer.setTexture(i, HD_Material_Layer.values()[newLayers.size()].getPlaceholderBitmap());
				newLayer.setTexture(i, HD_Material_Layer.values()[i].getPlaceholderBitmap());
			}
		}
	}

	@Override
	public UndoAction undo() {
		material.setShaderString(prevShader);
		if(!newLayers.isEmpty()){
			material.clearLayers();
			for(Layer layer : layers){
				material.addLayer(layer);
			}
		}
		for (UndoAction action : addTextureActions) {
			action.undo();
		}
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		material.setShaderString(newShader);
		if(!newLayers.isEmpty()){
			material.clearLayers();
			for(Layer layer : newLayers){
				material.addLayer(layer);
			}
		}
		for (UndoAction action : addTextureActions) {
			action.redo();
		}
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}



//	public static void makeMaterialSD(Material material) {
//		if (material.getShaderString() != null) {
//			material.setShaderString(null);
//			Layer layerZero = material.getLayers().get(0);
//			material.clearLayers();
//			material.addLayer(layerZero);
//			if (material.getTwoSided()) {
//				material.setTwoSided(false);
//				layerZero.setTwoSided(true);
//			}
//		}
//		for (final Layer layer : material.getLayers()) {
//			if (!Double.isNaN(layer.getEmissive())) {
//				layer.setEmissive(Double.NaN);
//			}
//			final AnimFlag<?> flag = layer.find("Emissive");
//			if (flag != null) {
//				layer.remove(flag);
//			}
//		}
//	}

	public static void makeMaterialHD(Material material) {
		material.setShaderString("Shader_HD_DefaultUnit");
		Layer diffuseLayer;
		if (!material.getLayers().isEmpty()) {
			diffuseLayer = material.getLayers().stream().filter(layer -> !layer.getTexture(0).getPath().equals("")).findFirst().orElse(material.getLayers().get(0));
		} else {
			diffuseLayer = new Layer(getBitmap("Textures\\White.dds"));
		}
		material.clearLayers();

		material.addLayer(HD_Material_Layer.DIFFUSE.ordinal(), diffuseLayer);
		material.addLayer(HD_Material_Layer.VERTEX.ordinal(), new Layer(getBitmap("Textures\\normal.dds")));
		material.addLayer(HD_Material_Layer.ORM.ordinal(), new Layer(getBitmap("Textures\\orm.dds")));
		material.addLayer(HD_Material_Layer.EMISSIVE.ordinal(), new Layer(getBitmap("Textures\\Black32.dds")));
		material.addLayer(HD_Material_Layer.TEAM_COLOR.ordinal(), new Layer(new Bitmap("", 1)));
		material.addLayer(HD_Material_Layer.REFLECTIONS.ordinal(), new Layer(getBitmap("ReplaceableTextures\\EnvironmentMap.dds")));

		for (final Layer l : material.getLayers()) {
			l.setEmissive(1.0);
		}
	}


	private static Bitmap getBitmap(String s) {
		Bitmap bitmap = new Bitmap(s);
		bitmap.setWrapHeight(true);
		bitmap.setWrapWidth(true);
		return bitmap;
	}

	@Override
	public String actionName() {
		return "set material Shader";
	}

}
