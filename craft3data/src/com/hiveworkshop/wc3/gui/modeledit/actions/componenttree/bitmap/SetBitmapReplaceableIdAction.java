package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.bitmap;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Bitmap;

public class SetBitmapReplaceableIdAction implements UndoAction {
	private final Bitmap bitmap;
	private final int prevId;
	private final int newId;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public SetBitmapReplaceableIdAction(final Bitmap bitmap, final int prevId, final int newId,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.bitmap = bitmap;
		this.prevId = prevId;
		this.newId = newId;
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void undo() {
		bitmap.setReplaceableId(prevId);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public void redo() {
		bitmap.setReplaceableId(newId);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public String actionName() {
		return "change texture ReplaceableId";
	}
}
