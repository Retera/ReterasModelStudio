package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;

public class CheckableModelElement extends CheckableDisplayElement<Void> {
	public CheckableModelElement(ModelViewManager modelViewManager) {
		super(modelViewManager, null);
	}

	@Override
	protected void setChecked(Void item, ModelViewManager modelViewManager, boolean checked) {

	}

	@Override
	protected String getName(Void item, ModelViewManager modelViewManager) {
		return modelViewManager.getModel().getHeaderName();
	}
}