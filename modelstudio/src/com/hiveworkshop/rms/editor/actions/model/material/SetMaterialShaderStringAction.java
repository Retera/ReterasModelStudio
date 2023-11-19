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
		if ((prevShader.isBlank() || prevShader.contains("_SD_")) && newShader.contains("_HD_")) {
			sdToHd(material);
		} else if ((newShader.isBlank() || newShader.contains("_SD_")) && prevShader.contains("_HD_")) {
			hdToSd(material);
		}

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

				if (layer.getTextureSlot(0).getFlipbookTexture() != null) {
					newLayer.setFlipbookTexture(i, (BitmapAnimFlag) layer.getTextureSlot(0).getFlipbookTexture().deepCopy());
				}
			} else {
				newLayer.setTexture(i, HD_Material_Layer.values()[i].getPlaceholderBitmap());
			}
		}
	}

	@Override
	public SetMaterialShaderStringAction undo() {
		material.setShaderString(prevShader);
		if (!newLayers.isEmpty()) {
			material.clearLayers();
			for (Layer layer : layers) {
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
	public SetMaterialShaderStringAction redo() {
		material.setShaderString(newShader);
		if (!newLayers.isEmpty()) {
			material.clearLayers();
			for (Layer layer : newLayers) {
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

	@Override
	public String actionName() {
		return "Set Material Shader";
	}

}
