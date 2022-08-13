package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Layer;

public class SetLayerCoordIdAction implements UndoAction {
	private final Layer layer;
	private final int prevCoordId;
	private final int newCoordId;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public SetLayerCoordIdAction(final Layer layer, final int prevCoordId, final int newCoordId,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.layer = layer;
		this.prevCoordId = prevCoordId;
		this.newCoordId = newCoordId;
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void undo() {
		layer.setCoordId(prevCoordId);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public void redo() {
		layer.setCoordId(newCoordId);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public String actionName() {
		return "set Layer CoordId";
	}

}
