package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Layer.FilterMode;

public class SetLayerFilterModeAction implements UndoAction {
	private final Layer layer;
	private final FilterMode prevFilterMode;
	private final FilterMode newFilterMode;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public SetLayerFilterModeAction(final Layer layer, final FilterMode prevFilterMode, final FilterMode newFilterMode,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.layer = layer;
		this.prevFilterMode = prevFilterMode;
		this.newFilterMode = newFilterMode;
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void undo() {
		layer.setFilterMode(prevFilterMode);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public void redo() {
		layer.setFilterMode(newFilterMode);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public String actionName() {
		return "set Layer FilterMode";
	}

}
