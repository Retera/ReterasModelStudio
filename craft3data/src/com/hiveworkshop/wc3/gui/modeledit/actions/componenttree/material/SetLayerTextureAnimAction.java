package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.TextureAnim;

public class SetLayerTextureAnimAction implements UndoAction {
	private final Layer layer;
	private final TextureAnim prevTexAnim;
	private final TextureAnim newTexAnim;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public SetLayerTextureAnimAction(final Layer layer, final TextureAnim prevTexAnim, final TextureAnim newTexAnim,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.layer = layer;
		this.prevTexAnim = prevTexAnim;
		this.newTexAnim = newTexAnim;
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void undo() {
		layer.setTextureAnim(prevTexAnim);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public void redo() {
		layer.setTextureAnim(newTexAnim);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public String actionName() {
		return "set Layer TVertexAnim";
	}

}
