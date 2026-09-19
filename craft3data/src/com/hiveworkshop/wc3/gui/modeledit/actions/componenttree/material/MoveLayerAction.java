package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Material;

/** Moves a layer up or down in its material's draw order. */
public class MoveLayerAction implements UndoAction {
	private final Material material;
	private final Layer layer;
	private final int fromIndex;
	private final int toIndex;
	private final ModelStructureChangeListener listener;

	public MoveLayerAction(final Material material, final Layer layer, final int toIndex,
			final ModelStructureChangeListener listener) {
		this.material = material;
		this.layer = layer;
		this.fromIndex = RemoveLayerAction.indexOf(material, layer);
		this.toIndex = toIndex;
		this.listener = listener;
	}

	private void move(final int from, final int to) {
		material.getLayers().remove(from);
		material.getLayers().add(to, layer);
		listener.componentChanged(material);
	}

	@Override
	public void undo() {
		move(toIndex, fromIndex);
	}

	@Override
	public void redo() {
		move(fromIndex, toIndex);
	}

	@Override
	public String actionName() {
		return "move material layer";
	}
}
