package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.ArrayList;
import java.util.List;

public class ReplaceLayerTextureAction implements UndoAction {
	private final Bitmap newBitmap;
	private final boolean wasMainTexture;
	private final ArrayList<Integer> oldIndices = new ArrayList<>();
	private final Bitmap oldBitmap;
	private final Layer layer;
	private final ModelStructureChangeListener changeListener;

	public ReplaceLayerTextureAction(Bitmap oldBitmap, Bitmap newBitmap, Layer layer, ModelStructureChangeListener changeListener) {
		this(oldBitmap, newBitmap, -1, layer, changeListener);
	}

	public ReplaceLayerTextureAction(Bitmap oldBitmap, Bitmap newBitmap, int textureId, Layer layer, ModelStructureChangeListener changeListener) {
		this.newBitmap = newBitmap;
		this.layer = layer;
		this.oldBitmap = oldBitmap;
		this.changeListener = changeListener;

		wasMainTexture = layer.getTextureBitmap() != null && layer.getTextureBitmap().equals(oldBitmap);

		List<Bitmap> textures = layer.getTextures();
		for(int i = 0; i < textures.size(); i++){
			if(textures.get(i).equals(oldBitmap)){
				oldIndices.add(i);
			}
		}
	}

	@Override
	public UndoAction undo() {
		if(wasMainTexture){
			layer.setTexture(oldBitmap);
		}
		for (int i : oldIndices){
			layer.setTexture(i, oldBitmap);
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		if(wasMainTexture){
			layer.setTexture(newBitmap);
		}
		for (int i : oldIndices){
			layer.setTexture(i, newBitmap);
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Replace Texture";
	}
}
