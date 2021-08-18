package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.HashableVector;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VertexClusterDefinitions {
	private final Map<Vec3, Integer> vertexToClusterId = new HashMap<>();
	private final int maxClusterIdKnown;

	public VertexClusterDefinitions(EditableModel model) {
		Map<HashableVector, List<GeosetVertex>> positionToVertices = new HashMap<>();
		for (Geoset geoset : model.getGeosets()) {
			for (GeosetVertex vertex : geoset.getVertices()) {
				HashableVector hashKey = new HashableVector(vertex);
				List<GeosetVertex> verticesAtPoint = positionToVertices.computeIfAbsent(hashKey, k -> new ArrayList<>());
				verticesAtPoint.add(vertex);
			}
		}
		int clusterId = 0;
		for (Geoset geoset : model.getGeosets()) {
			for (GeosetVertex vertex : geoset.getVertices()) {
				if (vertexToClusterId.get(vertex) == null) {
					// build component
					assignConnected(vertex, clusterId, positionToVertices);
					clusterId++;
				}
			}
		}
		maxClusterIdKnown = clusterId;
	}

	public int getMaxClusterIdKnown() {
		return maxClusterIdKnown;
	}

	/**
	 * Returns the cluster ID of the vertex. Returns -1 for vertices added dynamically, so they should all be in a group
	 * together and not cause error.
	 */
	public int getClusterId(Vec3 vertex) {
		Integer clusterId = vertexToClusterId.get(vertex);
		if (clusterId == null) {
			return -1;
		}
		return clusterId;
	}

	private void assignConnected(GeosetVertex vertex, int clusterId, Map<HashableVector, List<GeosetVertex>> positionToVertices) {
		vertexToClusterId.put(vertex, clusterId);
		for (Triangle triangle : vertex.getTriangles()) {
			for (GeosetVertex neighborPosition : triangle.getVerts()) {
				List<GeosetVertex> neighbors = positionToVertices.get(new HashableVector(neighborPosition));
				for (GeosetVertex neighbor : neighbors) {
					if (vertexToClusterId.get(neighbor) == null) {
						assignConnected(neighbor, clusterId, positionToVertices);
					}
				}
			}
		}
	}
}
