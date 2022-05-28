package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;

import java.util.List;

public final class Mesh {
	private final List<GeosetVertex> vertices;
	private final List<Triangle> triangles;

	public Mesh(List<GeosetVertex> vertices, List<Triangle> triangles) {
		this.vertices = vertices;
		this.triangles = triangles;
	}

	public Mesh add(GeosetVertex vertex) {
		vertices.add(vertex);
		return this;
	}

	public Mesh add(Triangle triangle) {
		triangles.add(triangle);
		return this;
	}

	public Mesh addVertices(List<GeosetVertex> vertices) {
		this.vertices.addAll(vertices);
		return this;
	}

	public Mesh addTriangles(List<Triangle> triangles) {
		this.triangles.addAll(triangles);
		return this;
	}

	public List<GeosetVertex> getVertices() {
		return vertices;
	}

	public List<Triangle> getTriangles() {
		return triangles;
	}

}
