package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

public class CheckableNodeElement extends CheckableDisplayElement<IdObject> {
	public CheckableNodeElement(ModelHandler modelHandler, IdObject item) {
		super(modelHandler.getModelView(), item);
	}

	@Override
	protected void setChecked(IdObject item, ModelView modelViewManager, boolean checked) {
		if (checked) {
			modelViewManager.makeIdObjectVisible(item);
		} else {
			modelViewManager.makeIdObjectNotVisible(item);
		}
	}

	@Override
	protected String getName(IdObject item, ModelView modelViewManager) {
		return item.getClass().getSimpleName() + " \"" + item.getName() + "\"";
	}

	@Override
	public void mouseEntered() {
		modelViewManager.highlightNode(item);
	}

	@Override
	public void mouseExited() {
		modelViewManager.unhighlightNode(item);
	}
}