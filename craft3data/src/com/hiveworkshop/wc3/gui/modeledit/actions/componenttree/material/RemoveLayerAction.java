package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Material;

public class RemoveLayerAction implements UndoAction {
	private final Material material;
	private final Layer layer;
	private final int index;
	private final ModelStructureChangeListener listener;

	public RemoveLayerAction(final Material material, final Layer layer, final ModelStructureChangeListener listener) {
		this.material = material;
		this.layer = layer;
		this.index = indexOf(material, layer);
		this.listener = listener;
	}

	static int indexOf(final Material material, final Layer layer) {
		for (int i = 0; i < material.getLayers().size(); i++) {
			if (material.getLayers().get(i) == layer) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void undo() {
		material.getLayers().add(Math.min(index, material.getLayers().size()), layer);
		listener.componentChanged(material);
	}

	@Override
	public void redo() {
		material.getLayers().remove(layer);
		listener.componentChanged(material);
	}

	@Override
	public String actionName() {
		return "remove material layer";
	}
}
