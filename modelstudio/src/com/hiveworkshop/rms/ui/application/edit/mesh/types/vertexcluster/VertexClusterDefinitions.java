package com.hiveworkshop.rms.ui.application.edit.mesh.types.vertexcluster;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VertexClusterDefinitions {
	private final Map<Vec3, Integer> vertexToClusterId = new HashMap<>();
	private final int maxClusterIdKnown;

	public VertexClusterDefinitions(final EditableModel model) {
		final Map<HashableVector, List<GeosetVertex>> positionToVertices = new HashMap<>();
		for (final Geoset geoset : model.getGeosets()) {
			for (final GeosetVertex vertex : geoset.getVertices()) {
				final HashableVector hashKey = new HashableVector(vertex);
                List<GeosetVertex> verticesAtPoint = positionToVertices.computeIfAbsent(hashKey, k -> new ArrayList<>());
                verticesAtPoint.add(vertex);
			}
		}
		int clusterId = 0;
		for (final Geoset geoset : model.getGeosets()) {
			for (final GeosetVertex vertex : geoset.getVertices()) {
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
	public int getClusterId(final Vec3 vertex) {
		final Integer clusterId = vertexToClusterId.get(vertex);
		if (clusterId == null) {
			return -1;
		}
		return clusterId;
	}

	private void assignConnected(final GeosetVertex vertex, final int clusterId,
			final Map<HashableVector, List<GeosetVertex>> positionToVertices) {
		vertexToClusterId.put(vertex, clusterId);
		for (final Triangle triangle : vertex.getTriangles()) {
			for (final GeosetVertex neighborPosition : triangle.getVerts()) {
				final List<GeosetVertex> neighbors = positionToVertices.get(new HashableVector(neighborPosition));
				for (final GeosetVertex neighbor : neighbors) {
					if (vertexToClusterId.get(neighbor) == null) {
						assignConnected(neighbor, clusterId, positionToVertices);
					}
				}
			}
		}
	}

	private static final class HashableVector {
		private final float x, y, z;

		public HashableVector(final float x, final float y, final float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public HashableVector(final Vec3 vertex) {
			x = vertex.x;
			y = vertex.y;
			z = vertex.z;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Float.floatToIntBits(x);
			result = prime * result + Float.floatToIntBits(y);
			result = prime * result + Float.floatToIntBits(z);
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
			final HashableVector other = (HashableVector) obj;
			if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) {
				return false;
			}
			if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) {
				return false;
			}
			return Float.floatToIntBits(z) == Float.floatToIntBits(other.z);
		}

	}
}
