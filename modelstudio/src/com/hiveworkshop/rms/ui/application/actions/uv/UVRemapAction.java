package com.hiveworkshop.rms.ui.application.actions.uv;

import com.hiveworkshop.rms.editor.model.TVertex;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel.UnwrapDirection;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.List;

public class UVRemapAction implements UndoAction {

	private final List<TVertex> tVertices;
	private final List<TVertex> newValueHolders;
	private final List<TVertex> oldValueHolders;
	private final UnwrapDirection direction;

	public UVRemapAction(final List<TVertex> tVertices, final List<TVertex> newValueHolders,
			final List<TVertex> oldValueHolders, final UVPanel.UnwrapDirection direction) {
		this.tVertices = tVertices;
		this.newValueHolders = newValueHolders;
		this.oldValueHolders = oldValueHolders;
		this.direction = direction;
	}

	@Override
	public void undo() {
		for (int i = 0; i < tVertices.size(); i++) {
			tVertices.get(i).setTo(oldValueHolders.get(i));
		}
	}

	@Override
	public void redo() {
		for (int i = 0; i < tVertices.size(); i++) {
			tVertices.get(i).setTo(newValueHolders.get(i));
		}
	}

	@Override
	public String actionName() {
		return "remap TVertices " + direction;
	}

}
