package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;

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
		final ArrayList<Triangle> selTris = new ArrayList<>();
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
			tri.flip(true);
		}
	}

	@Override
	public String actionName() {
		return "flip faces";
	}

}
