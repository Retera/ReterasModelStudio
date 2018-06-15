package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public final class GeosetVertexSelectionManager extends AbstractSelectionManager<GeosetVertex> {

	@Override
	public Set<Triangle> getSelectedFaces() {
		final Set<Triangle> faces = new HashSet<>();
		final Set<GeosetVertex> selectedVertices = new HashSet<>();
		final Set<Triangle> partiallySelectedFaces = new HashSet<>();
		for (final GeosetVertex vertex : getSelection()) {
			partiallySelectedFaces.addAll(vertex.getTriangles());
			selectedVertices.add(vertex);
		}
		for (final Triangle face : partiallySelectedFaces) {
			boolean whollySelected = true;
			for (final GeosetVertex gv : face.getVerts()) {
				if (!selectedVertices.contains(gv)) {
					whollySelected = false;
				}
			}
			if (whollySelected) {
				faces.add(face);
			}
		}
		return faces;
	}

	@Override
	public Vertex getCenter() {
		return Vertex.centerOfGroup(selection);
	}

	@Override
	public double getCircumscribedSphereRadius(final Vertex sphereCenter) {
		double radius = 0;
		for (final Vertex item : selection) {
			final double distance = sphereCenter.distance(item);
			if (distance >= radius) {
				radius = distance;
			}
		}
		return radius;
	}

	@Override
	public void renderSelection(final ModelElementRenderer renderer, final CoordinateSystem coordinateSystem,
			final ModelView model, final ProgramPreferences programPreferences) {
		for (final Geoset geo : model.getEditableGeosets()) {
			final ArrayList<GeosetVertex> vertices = geo.getVertices();
			for (final GeosetVertex geosetVertex : vertices) {
				if (model.getHighlightedGeoset() == geo) {
					renderer.renderVertex(programPreferences.getHighlighVertexColor(), geosetVertex);
				} else if (selection.contains(geosetVertex)) {
					renderer.renderVertex(programPreferences.getSelectColor(), geosetVertex);
				} else {
					renderer.renderVertex(programPreferences.getVertexColor(), geosetVertex);
				}
			}
		}
	}

	@Override
	public Collection<? extends Vertex> getSelectedVertices() {
		return getSelection();
	}
}
