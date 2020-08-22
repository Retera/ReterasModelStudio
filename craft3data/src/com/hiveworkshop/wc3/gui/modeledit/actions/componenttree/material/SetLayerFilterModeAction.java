package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material;

import com.etheller.warsmash.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Layer;

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
		this.layer.setFilterMode(this.prevFilterMode);
		this.modelStructureChangeListener.texturesChanged();
	}

	@Override
	public void redo() {
		this.layer.setFilterMode(this.newFilterMode);
		this.modelStructureChangeListener.texturesChanged();
	}

	@Override
	public String actionName() {
		return "set Layer FilterMode";
	}

}
