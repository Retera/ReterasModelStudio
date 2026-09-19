package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Material;

public class AddLayerAction implements UndoAction {
	private final Material material;
	private final Layer layer;
	private final int index;
	private final ModelStructureChangeListener listener;

	public AddLayerAction(final Material material, final Layer layer, final int index,
			final ModelStructureChangeListener listener) {
		this.material = material;
		this.layer = layer;
		this.index = index;
		this.listener = listener;
	}

	@Override
	public void undo() {
		material.getLayers().remove(layer);
		listener.componentChanged(material);
	}

	@Override
	public void redo() {
		material.getLayers().add(Math.min(index, material.getLayers().size()), layer);
		listener.componentChanged(material);
	}

	@Override
	public String actionName() {
		return "add material layer";
	}
}
