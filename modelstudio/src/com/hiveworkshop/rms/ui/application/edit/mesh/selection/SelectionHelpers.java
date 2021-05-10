package com.hiveworkshop.rms.ui.application.edit.mesh.selection;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.util.HashableVector;

import java.util.*;

public class SelectionHelpers {
	// ToDo make sure editable checks is correct and is checking at all appropriate times
	Set<Triangle> trisToSelectTris1 = new HashSet<>();
	private ModelView modelView;

	public SelectionHelpers(ModelView modelView) {
		this.modelView = modelView;
	}

	public void selectConnected() {
		Set<GeosetVertex> verticesToSelect = new HashSet<>();
		for (GeosetVertex geosetVertex : modelView.getSelectedVertices()) {
			for (Triangle triangle : geosetVertex.getTriangles()) {
				for (GeosetVertex triVert : triangle.getVerts()) {
					if (modelView.isEditable(geosetVertex)) {
						verticesToSelect.add(triVert);
					}
				}
			}
		}
	}

	private void selectConnectedTris(Triangle triangle) {
		trisToSelectTris1.add(triangle);
		for (GeosetVertex vertex : triangle.getVerts()) {
			if (modelView.isEditable(vertex)) {
				for (Triangle tri : vertex.getTriangles()) {
					if (!trisToSelectTris1.contains(tri)) {
						selectConnectedTris(tri);
					}
				}
			}
		}
	}

	private void selectConnectedTris2(Triangle triangle, Map<HashableVector, Set<GeosetVertex>> locationToGVs) {
		trisToSelectTris1.add(triangle);
		for (GeosetVertex vertex : triangle.getVerts()) {
			if (modelView.isEditable(vertex)) {
				for (GeosetVertex gvLoc : locationToGVs.get(new HashableVector(vertex))) {
					for (Triangle tri : gvLoc.getTriangles()) {
						if (!trisToSelectTris1.contains(tri)) {
							selectConnectedTris2(tri, locationToGVs);
						}
					}
				}
			}
		}
	}

	private Map<HashableVector, Set<GeosetVertex>> getLocationMap(Collection<GeosetVertex> affectedVertices) {
		Map<HashableVector, Set<GeosetVertex>> locationToGVs = new HashMap<>();
		for (GeosetVertex geosetVertex : affectedVertices) {
			HashableVector location = new HashableVector(geosetVertex);
			Set<GeosetVertex> gvAtLocation = locationToGVs.computeIfAbsent(location, gvList -> new HashSet<>());
			gvAtLocation.add(geosetVertex);
		}
		return locationToGVs;
	}
}
