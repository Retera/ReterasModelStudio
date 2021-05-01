package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

public class CheckableModelElement extends CheckableDisplayElement<Void> {
	public CheckableModelElement(ModelHandler modelHandler) {
		super(modelHandler.getModelView(), null);
	}

	@Override
	protected void setChecked(Void item, ModelView modelViewManager, boolean checked) {

	}

	@Override
	protected String getName(Void item, ModelView modelViewManager) {
		return modelViewManager.getModel().getHeaderName();
	}
}