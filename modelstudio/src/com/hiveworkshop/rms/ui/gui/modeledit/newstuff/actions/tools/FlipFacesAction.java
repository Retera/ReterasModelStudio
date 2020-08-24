package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.Vertex;

public final class FlipFacesAction implements UndoAction {
	private final List<Vertex> selection;

	public FlipFacesAction(final Collection<? extends Vertex> selection) {
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
		final List<Triangle> selTris = new ArrayList<>();
		for (int i = 0; i < selection.size(); i++) {
			final Vertex vert = selection.get(i);
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;

				for (int ti = 0; ti < gv.getTriangles().size(); ti++) {
					final Triangle temptr = gv.getTriangles().get(ti);
					if (!selTris.contains(temptr)) {
						selTris.add(temptr);
					}
				}
				if (gv.getNormal() != null) {
					gv.getNormal().inverse();
				}
			}
		}

		for (int i = selTris.size() - 1; i >= 0; i--) {
			boolean goodTri = true;
			for (final Vertex v : selTris.get(i).getAll()) {
				if (!selection.contains(v)) {
					goodTri = false;
				}
			}
			if (!goodTri) {
				selTris.remove(i);
			}
		}

		for (final Triangle tri : selTris) {
			tri.flip(false);
		}
	}

	@Override
	public String actionName() {
		return "flip faces";
	}

}
