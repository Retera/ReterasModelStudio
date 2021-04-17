package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Undoable snap action.
 *
 * Eric Theller 6/11/2012
 */
public class RecalculateNormalsAction2 implements UndoAction {
	List<Vec3> oldNormals;
	List<GeosetVertex> affectedVertices;
	Vec3 snapPoint;
	double maxAngle;
	boolean useTries;

	public RecalculateNormalsAction2(final List<GeosetVertex> affectedVertices, final List<Vec3> oldNormals, final Vec3 snapPoint) {
		this.affectedVertices = new ArrayList<>(affectedVertices);
		this.oldNormals = oldNormals;
		this.snapPoint = new Vec3(snapPoint);
		maxAngle = 360.0;
		useTries = false;
	}

	public RecalculateNormalsAction2(final List<GeosetVertex> affectedVertices, final List<Vec3> oldNormals, final Vec3 snapPoint, final double maxAngle, boolean useTries) {
		this.affectedVertices = new ArrayList<>(affectedVertices);
		this.oldNormals = oldNormals;
		this.snapPoint = new Vec3(snapPoint);
		this.maxAngle = maxAngle;
		this.useTries = useTries;
	}

	@Override
	public void undo() {
		for (int i = 0; i < affectedVertices.size(); i++) {
			affectedVertices.get(i).getNormal().set(oldNormals.get(i));
		}
	}

	@Override
	public void redo() {
		final Map<Tuplet, List<GeosetVertex>> tupletToMatches = new HashMap<>();
		for (final GeosetVertex geosetVertex : affectedVertices) {
			final Tuplet tuplet = new Tuplet(geosetVertex.x, geosetVertex.y, geosetVertex.z);
			List<GeosetVertex> matches = tupletToMatches.computeIfAbsent(tuplet, k -> new ArrayList<>());
			matches.add(geosetVertex);
		}
		for (final GeosetVertex geosetVertex : affectedVertices) {
			final Tuplet tuplet = new Tuplet(geosetVertex.x, geosetVertex.y, geosetVertex.z);
			final List<GeosetVertex> matches = tupletToMatches.get(tuplet);
			if (useTries) {
				geosetVertex.getNormal().set(geosetVertex.createNormalFromFaces(matches, maxAngle));
			} else {
				geosetVertex.getNormal().set(geosetVertex.createNormal(matches, maxAngle));
			}
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
