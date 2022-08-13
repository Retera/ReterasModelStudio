package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.ShaderTextureTypeHD;

public class SetLayerBitmapAction implements UndoAction {

	private final Layer layer;
	private final ShaderTextureTypeHD shaderTextureTypeHD;
	private final Bitmap prevBitmap;
	private final Bitmap newBitmap;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public SetLayerBitmapAction(final Layer layer, final ShaderTextureTypeHD shaderTextureTypeHD,
			final Bitmap prevBitmap, final Bitmap newBitmap,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.layer = layer;
		this.shaderTextureTypeHD = shaderTextureTypeHD;
		this.prevBitmap = prevBitmap;
		this.newBitmap = newBitmap;
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void undo() {
		if (prevBitmap == null) {
			layer.getShaderTextures().remove(shaderTextureTypeHD);
		}
		else {
			layer.getShaderTextures().put(shaderTextureTypeHD, prevBitmap);
		}
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public void redo() {
		if (newBitmap == null) {
			layer.getShaderTextures().remove(shaderTextureTypeHD);
		}
		else {
			layer.getShaderTextures().put(shaderTextureTypeHD, newBitmap);
		}
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public String actionName() {
		return "set Layer Bitmap";
	}

}
