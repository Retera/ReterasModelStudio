package com.hiveworkshop.rms.ui.application.actions.uv;

import java.util.List;

import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel.UnwrapDirection;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vector2;

public class UVRemapAction implements UndoAction {

	private final List<Vector2> tVertices;
	private final List<Vector2> newValueHolders;
	private final List<Vector2> oldValueHolders;
	private final UnwrapDirection direction;

	public UVRemapAction(final List<Vector2> tVertices, final List<Vector2> newValueHolders,
			final List<Vector2> oldValueHolders, final UVPanel.UnwrapDirection direction) {
		this.tVertices = tVertices;
		this.newValueHolders = newValueHolders;
		this.oldValueHolders = oldValueHolders;
		this.direction = direction;
	}

	@Override
	public void undo() {
		for (int i = 0; i < tVertices.size(); i++) {
			tVertices.get(i).set(oldValueHolders.get(i));
		}
	}

	@Override
	public void redo() {
		for (int i = 0; i < tVertices.size(); i++) {
			tVertices.get(i).set(newValueHolders.get(i));
		}
	}

	@Override
	public String actionName() {
		return "remap TVertices " + direction;
	}

}
