package com.hiveworkshop.rms.ui.application.actions.uv;

import java.util.List;

import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel.UnwrapDirection;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vertex2;

public class UVRemapAction implements UndoAction {

	private final List<Vertex2> tVertices;
	private final List<Vertex2> newValueHolders;
	private final List<Vertex2> oldValueHolders;
	private final UnwrapDirection direction;

	public UVRemapAction(final List<Vertex2> tVertices, final List<Vertex2> newValueHolders,
			final List<Vertex2> oldValueHolders, final UVPanel.UnwrapDirection direction) {
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
