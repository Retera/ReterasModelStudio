package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class FlipFacesAction implements UndoAction {
	private final List<GeosetVertex> selection;

	public FlipFacesAction(final Collection<GeosetVertex> selection) {
		this.selection = new ArrayList<>(selection);
	}

	@Override
	public UndoAction undo() {
		doFlip();
		return this;
	}

	@Override
	public UndoAction redo() {
		doFlip();
		return this;
	}

	private void doFlip() {
		final List<Triangle> selTris = new ArrayList<>();
        for (final GeosetVertex vert : selection) {
	        for (Triangle triangle : vert.getTriangles()) {
		        if (!selTris.contains(triangle)) {
			        selTris.add(triangle);
		        }
	        }

	        if (vert.getNormal() != null) {
		        vert.getNormal().negate();
	        }
        }

		for (int i = selTris.size() - 1; i >= 0; i--) {
			Triangle triangle = selTris.get(i);
			for (final GeosetVertex v : triangle.getVerts()) {
				if (!selection.contains(v)) {
					selTris.remove(i);
					break;
				}
			}
		}

		for (final Triangle tri : selTris) {
			tri.flip(false);
		}
	}

	@Override
	public String actionName() {
		return "Flip Faces";
	}

}
