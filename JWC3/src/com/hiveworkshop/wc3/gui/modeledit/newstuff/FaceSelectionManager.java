package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public final class FaceSelectionManager extends AbstractSelectionManager<Triangle> {
	private static final Color FACE_HIGHLIGHT_COLOR = new Color(1f, 0.45f, 0.45f, 0.3f);

	@Override
	public Vertex getCenter() {
		final Set<Vertex> selectedVertices = new HashSet<>();
		for (final Triangle triangle : selection) {
			for (final GeosetVertex geosetVertex : triangle.getVerts()) {
				selectedVertices.add(geosetVertex);
			}
		}
		return Vertex.centerOfGroup(selectedVertices);
	}

	@Override
	public Set<Vertex> getSelectedVertices() {
		final Set<Vertex> vertices = new HashSet<>();
		for (final Triangle triangle : getSelection()) {
			for (final Vertex vertex : triangle.getVerts()) {
				vertices.add(vertex);
			}
		}
		return vertices;
	}

	@Override
	public double getCircumscribedSphereRadius(final Vertex sphereCenter) {
		double radius = 0;
		for (final Triangle item : selection) {
			for (final GeosetVertex geosetVertex : item.getVerts()) {
				final double distance = sphereCenter.distance(geosetVertex);
				if (distance >= radius) {
					radius = distance;
				}
			}
		}
		return radius;
	}

	@Override
	public void renderSelection(final ModelElementRenderer renderer, final CoordinateSystem coordinateSystem,
			final ModelView modelView, final ProgramPreferences programPreferences) {
		for (final Triangle triangle : selection) {
			renderer.renderFace(Color.RED, FACE_HIGHLIGHT_COLOR, triangle.get(0), triangle.get(1), triangle.get(2));
		}
	}

	@Override
	public Collection<Triangle> getSelectedFaces() {
		return getSelection();
	}
}
