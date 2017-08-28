package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class FaceSelectionManager extends AbstractSelectionManager<Triangle> {

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
}
