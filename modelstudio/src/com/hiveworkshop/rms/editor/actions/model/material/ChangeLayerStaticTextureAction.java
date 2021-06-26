package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class ChangeLayerStaticTextureAction implements UndoAction {
	private final Bitmap bitmap;
	private final Bitmap oldBitmap;
	private final int textureId;
	private final int oldTextureId;
	private final Layer layer;
	private final ModelStructureChangeListener changeListener;

	public ChangeLayerStaticTextureAction(Bitmap bitmap, Layer layer, ModelStructureChangeListener changeListener) {
		this(bitmap, -1, layer, changeListener);
	}

	public ChangeLayerStaticTextureAction(Bitmap bitmap, int textureId, Layer layer, ModelStructureChangeListener changeListener) {
		this.bitmap = bitmap;
		this.layer = layer;
		this.textureId = textureId;
		oldBitmap = layer.getTextureBitmap();
		oldTextureId = layer.getTextureId();
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		layer.setTexture(oldBitmap);
		layer.setTextureId(oldTextureId);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		layer.setTexture(bitmap);
		layer.setTextureId(textureId);
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
