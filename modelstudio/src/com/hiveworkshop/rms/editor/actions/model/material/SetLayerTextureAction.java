package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.ArrayList;
import java.util.List;

public class SetLayerTextureAction implements UndoAction {
	private final Bitmap newBitmap;
	private final ArrayList<Integer> oldIndices = new ArrayList<>();
	private final Bitmap oldBitmap;
	private final Layer layer;
	private final ModelStructureChangeListener changeListener;

	public SetLayerTextureAction(Bitmap newBitmap, int slot, Layer layer, ModelStructureChangeListener changeListener) {
		this.newBitmap = newBitmap;
		this.layer = layer;
		this.oldBitmap = layer.getTexture(slot);
		this.changeListener = changeListener;
		oldIndices.add(slot);
	}

	public SetLayerTextureAction(Bitmap oldBitmap, Bitmap newBitmap, Layer layer, ModelStructureChangeListener changeListener) {
		this.newBitmap = newBitmap;
		this.layer = layer;
		this.oldBitmap = oldBitmap;
		this.changeListener = changeListener;

		List<Bitmap> textures = layer.getTextures();
		for(int i = 0; i < textures.size(); i++){
			if(textures.get(i).equals(oldBitmap)){
				oldIndices.add(i);
			}
		}
	}

	@Override
	public UndoAction undo() {
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
		return "Change Texture";
	}
}
