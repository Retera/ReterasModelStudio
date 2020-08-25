package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Vertex;
import com.hiveworkshop.rms.editor.model.Vertex;

public final class FlipNormalsAction implements UndoAction {
	private final List<Vertex> selection;

	public FlipNormalsAction(final Collection<? extends Vertex> selection) {
		this.selection = new ArrayList<>(selection);
	}

	@Override
	public void undo() {
		doFlip();
	}

	@Override
	public void redo() {
		doFlip();
	}

	private void doFlip() {
		for (int i = 0; i < selection.size(); i++) {
			final Vertex vert = selection.get(i);
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				final Vertex normal = gv.getNormal();
				if (normal != null) {
					// why is this nullable?
					normal.inverse();
				}
			}
		}
	}

	@Override
	public String actionName() {
		return "flip normals";
	}

}
