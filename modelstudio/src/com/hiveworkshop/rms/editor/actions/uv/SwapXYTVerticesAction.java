package com.hiveworkshop.rms.editor.actions.uv;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Vec2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SwapXYTVerticesAction implements UndoAction {
	private final List<Vec2> selection;
	private final ModelStructureChangeListener changeListener;

	public SwapXYTVerticesAction(Collection<Vec2> selection,
	                             ModelStructureChangeListener changeListener) {
		this.selection = new ArrayList<>(selection);
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		doSwap();

		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		doSwap();

		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	private void doSwap() {
		for (Vec2 tvert : selection) {
			final float temp = tvert.x;
			tvert.x = tvert.y;
			tvert.y = temp;
		}
	}

	@Override
	public String actionName() {
		return "Swap UVs X/Y";
	}

}
