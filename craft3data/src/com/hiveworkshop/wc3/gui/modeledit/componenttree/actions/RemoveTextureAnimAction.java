package com.hiveworkshop.wc3.gui.modeledit.componenttree.actions;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.TextureAnim;

/**
 * Removes a texture animation and clears it from every layer that used it.
 */
public final class RemoveTextureAnimAction implements UndoAction {
	private final EditableModel model;
	private final TextureAnim textureAnim;
	private final ModelStructureChangeListener listener;
	private final List<Layer> clearedLayers = new ArrayList<>();
	private int index = -1;

	public RemoveTextureAnimAction(final EditableModel model, final TextureAnim textureAnim,
			final ModelStructureChangeListener listener) {
		this.model = model;
		this.textureAnim = textureAnim;
		this.listener = listener;
	}

	public static int countReferences(final EditableModel model, final TextureAnim textureAnim) {
		int count = 0;
		for (final Material material : model.getMaterials()) {
			for (final Layer layer : material.getLayers()) {
				if (layer.getTextureAnim() == textureAnim) {
					count++;
				}
			}
		}
		return count;
	}

	@Override
	public void redo() {
		clearedLayers.clear();
		index = ComponentListUtil.removeIdentity(model.getTexAnims(), textureAnim);
		for (final Material material : model.getMaterials()) {
			for (final Layer layer : material.getLayers()) {
				if (layer.getTextureAnim() == textureAnim) {
					layer.setTextureAnim(null);
					clearedLayers.add(layer);
				}
			}
		}
		listener.texturesChanged();
	}

	@Override
	public void undo() {
		for (final Layer layer : clearedLayers) {
			layer.setTextureAnim(textureAnim);
		}
		clearedLayers.clear();
		ComponentListUtil.insertAt(model.getTexAnims(), index, textureAnim);
		listener.texturesChanged();
	}

	@Override
	public String actionName() {
		return "delete texture anim";
	}
}
