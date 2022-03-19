package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class FlipFacesAction implements UndoAction {
	private final List<Vec3> selection;

	public FlipFacesAction(final Collection<? extends Vec3> selection) {
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
        for (final Vec3 vert : selection) {
            if (vert.getClass() == GeosetVertex.class) {
	            final GeosetVertex gv = (GeosetVertex) vert;

	            for (Triangle temptr : gv.getTriangles()) {
		            if (!selTris.contains(temptr)) {
			            selTris.add(temptr);
		            }
	            }
//                for (int i = 0; i < gv.getTriangles().size(); i++) {
//                    Triangle temptr = gv.getTriangles().get(i);
//                    if (!selTris.contains(temptr)) {
//                        selTris.add(temptr);
//                    }
//                }

	            if (gv.getNormal() != null) {
		            gv.getNormal().negate();
	            }
            }
        }

		for (int i = selTris.size() - 1; i >= 0; i--) {
			boolean goodTri = true;
			for (final Vec3 v : selTris.get(i).getAll()) {
				if (!selection.contains(v)) {
					goodTri = false;
					break;
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
