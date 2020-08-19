package com.hiveworkshop.wc3.gui.modeledit.actions;

import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UVPanel;
import com.hiveworkshop.wc3.gui.modeledit.UVPanel.UnwrapDirection;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.TVertex;

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
