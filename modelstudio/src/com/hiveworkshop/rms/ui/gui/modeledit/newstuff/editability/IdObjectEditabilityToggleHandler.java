package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.editability;

import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;

public class IdObjectEditabilityToggleHandler implements EditabilityToggleHandler {
	private final List<IdObject> nodes;
	private final ModelViewManager modelViewManager;

	public IdObjectEditabilityToggleHandler(final List<IdObject> nodes, final ModelViewManager modelViewManager) {
		this.nodes = nodes;
		this.modelViewManager = modelViewManager;
	}

	@Override
	public void makeEditable() {
		for (final IdObject node : nodes) {
			modelViewManager.makeIdObjectVisible(node);
		}
	}

	@Override
	public void makeNotEditable() {
		for (final IdObject node : nodes) {
			modelViewManager.makeIdObjectNotVisible(node);
		}
	}

}
