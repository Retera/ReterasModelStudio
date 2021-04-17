package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Vec3;

public final class FlipNormalsAction implements UndoAction {
	private final List<Vec3> selection;

	public FlipNormalsAction(final Collection<? extends Vec3> selection) {
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
        for (final Vec3 vert : selection) {
            if (vert.getClass() == GeosetVertex.class) {
                final GeosetVertex gv = (GeosetVertex) vert;
                final Vec3 normal = gv.getNormal();
                if (normal != null) {
                    // why is this nullable?
                    normal.negate();
                }
            }
        }
	}

	@Override
	public String actionName() {
		return "flip normals";
	}

}
