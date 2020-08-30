package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.Vec3;

public final class FlipFacesAction implements UndoAction {
	private final List<Vec3> selection;

	public FlipFacesAction(final Collection<? extends Vec3> selection) {
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
        for (final Vec3 vert : selection) {
            if (vert.getClass() == GeosetVertex.class) {
                final GeosetVertex gv = (GeosetVertex) vert;

                for (int ti = 0; ti < gv.getTriangles().size(); ti++) {
                    final Triangle temptr = gv.getTriangles().get(ti);
                    if (!selTris.contains(temptr)) {
                        selTris.add(temptr);
                    }
                }
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
