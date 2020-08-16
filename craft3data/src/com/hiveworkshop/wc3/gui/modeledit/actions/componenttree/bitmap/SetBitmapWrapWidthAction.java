package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.bitmap;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Bitmap;

public class SetBitmapWrapWidthAction implements UndoAction {
	private final Bitmap bitmap;
	private final boolean prevState;
	private final boolean newState;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public SetBitmapWrapWidthAction(final Bitmap bitmap, final boolean prevState, final boolean newState,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.bitmap = bitmap;
		this.prevState = prevState;
		this.newState = newState;
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void undo() {
		bitmap.setWrapWidth(prevState);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public void redo() {
		bitmap.setWrapWidth(newState);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public String actionName() {
		return "change texture ReplaceableId";
	}
}
