package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.editability;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;

import java.util.List;

public class IdObjectEditabilityToggleHandler implements EditabilityToggleHandler {
	private final List<IdObject> nodes;
	private final ModelView modelViewManager;

	public IdObjectEditabilityToggleHandler(final List<IdObject> nodes, final ModelView modelViewManager) {
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
