package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class Mesh {
	private final List<GeosetVertex> vertices;
	private final List<Triangle> triangles;

	public Mesh() {
		this(new ArrayList<>(), new ArrayList<>());
	}
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

	public Mesh setGeoset(Geoset geoset){
		vertices.forEach(vertex -> vertex.setGeoset(geoset));
		triangles.forEach(triangle -> triangle.setGeoset(geoset));
		return this;
	}
	public Mesh rotate(Quat rot){
		return rotate(Vec3.ZERO, rot);
	}
	public Mesh rotate(Vec3 point, Quat rot){
		for (GeosetVertex vertex : vertices) {
			vertex.rotate(point, rot);
		}
		return this;
	}
	public Mesh translate(Vec3 dist){
		for (GeosetVertex vertex : vertices) {
			vertex.add(dist);
		}
		return this;
	}

	public List<GeosetVertex> getVertices() {
		return vertices;
	}

	public List<Triangle> getTriangles() {
		return triangles;
	}

}
