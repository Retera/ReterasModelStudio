package com.hiveworkshop.rms.ui.application.actions.model.material;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class ChangeLayerStaticTextureAction implements UndoAction {
	private final Bitmap bitmap;
	private final Bitmap oldBitmap;
	private final int textureId;
	private final int oldTextureId;
	private final Layer layer;
	private final ModelStructureChangeListener structureChangeListener;

	public ChangeLayerStaticTextureAction(final Bitmap bitmap, final Layer layer,
	                                      final ModelStructureChangeListener modelStructureChangeListener) {
		this(bitmap, -1, layer, modelStructureChangeListener);
	}

	public ChangeLayerStaticTextureAction(final Bitmap bitmap, final int textureId, final Layer layer,
	                                      final ModelStructureChangeListener modelStructureChangeListener) {
		this.bitmap = bitmap;
		this.layer = layer;
		this.textureId = textureId;
		oldBitmap = layer.getTextureBitmap();
		oldTextureId = layer.getTextureId();
		this.structureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void undo() {
		layer.setTexture(oldBitmap);
		layer.setTextureId(oldTextureId);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public void redo() {
		layer.setTexture(bitmap);
		layer.setTextureId(textureId);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public String actionName() {
		return "Change Texture";
	}
}
