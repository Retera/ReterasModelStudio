package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.bitmap;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Bitmap;

public class SetBitmapPathAction implements UndoAction {
	private final Bitmap bitmap;
	private final String prevPath;
	private final String newPath;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public SetBitmapPathAction(final Bitmap bitmap, final String prevPath, final String newPath,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.bitmap = bitmap;
		this.prevPath = prevPath;
		this.newPath = newPath;
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void undo() {
		bitmap.setPath(prevPath);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public void redo() {
		bitmap.setPath(newPath);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public String actionName() {
		return "change texture Path";
	}
}
