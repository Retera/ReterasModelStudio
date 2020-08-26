package com.hiveworkshop.rms.ui.application.actions.mesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vector3;

/**
 * Undoable snap action.
 *
 * Eric Theller 6/11/2012
 */
public class RecalculateNormalsAction2 implements UndoAction {
	List<Vector3> oldSelLocs;
	List<GeosetVertex> selection;
	Vector3 snapPoint;

	public RecalculateNormalsAction2(final List<GeosetVertex> selection, final List<Vector3> oldSelLocs,
			final Vector3 snapPoint) {
		this.selection = new ArrayList<>(selection);
		this.oldSelLocs = oldSelLocs;
		this.snapPoint = new Vector3(snapPoint);
	}

	@Override
	public void undo() {
		for (int i = 0; i < selection.size(); i++) {
			selection.get(i).getNormal().set(oldSelLocs.get(i));
		}
	}

	@Override
	public void redo() {
		final Map<Tuplet, List<GeosetVertex>> tupletToMatches = new HashMap<>();
		for (int i = 0; i < selection.size(); i++) {
			final GeosetVertex geosetVertex = selection.get(i);
			final Tuplet tuplet = new Tuplet(geosetVertex.x, geosetVertex.y, geosetVertex.z);
			List<GeosetVertex> matches = tupletToMatches.get(tuplet);
			if (matches == null) {
				matches = new ArrayList<>();
				tupletToMatches.put(tuplet, matches);
			}
			matches.add(geosetVertex);
		}
		for (int i = 0; i < selection.size(); i++) {
			final GeosetVertex geosetVertex = selection.get(i);
			final Tuplet tuplet = new Tuplet(geosetVertex.x, geosetVertex.y, geosetVertex.z);
			final List<GeosetVertex> matches = tupletToMatches.get(tuplet);
			geosetVertex.getNormal().set(selection.get(i).createNormal(matches));
		}
	}

	@Override
	public String actionName() {
		return "recalculate normals";
	}

	private static final class Tuplet {
		private final double x, y, z;

		public Tuplet(final double x, final double y, final double z) {
			super();
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(x);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(y);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(z);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Tuplet other = (Tuplet) obj;
			if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
				return false;
			}
			if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
				return false;
			}
			return Double.doubleToLongBits(z) == Double.doubleToLongBits(other.z);
		}

	}
}
