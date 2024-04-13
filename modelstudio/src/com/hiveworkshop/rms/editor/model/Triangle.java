package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public class Triangle {
	private final GeosetVertex[] verts = new GeosetVertex[3];
	private Geoset geoset;

	public Triangle(Geoset geoRef) {
		verts[0] = null;
		verts[1] = null;
		verts[2] = null;
		geoset = geoRef;
	}

	public Triangle(GeosetVertex a, GeosetVertex b, GeosetVertex c, Geoset geoRef) {
		verts[0] = a;
		verts[1] = b;
		verts[2] = c;
		geoset = geoRef;
	}

	public Triangle(GeosetVertex a, GeosetVertex b, GeosetVertex c) {
		verts[0] = a;
		verts[1] = b;
		verts[2] = c;
		geoset = null;
	}

	public Triangle addToVerts() {
		verts[0].addTriangle(this);
		verts[1].addTriangle(this);
		verts[2].addTriangle(this);
		return this;
	}

	public Triangle removeFromVerts() {
		verts[0].removeTriangle(this);
		verts[1].removeTriangle(this);
		verts[2].removeTriangle(this);
		return this;
	}

	public boolean containsRef(GeosetVertex v) {
		return verts[0] == v || verts[1] == v || verts[2] == v;
	}

	public boolean containsLoc(GeosetVertex v) {
		return verts[0].equalLocs(v) || verts[1].equalLocs(v) || verts[2].equalLocs(v);
	}
	public int indexOf(GeosetVertex v) {
		for (int i = 0; i < verts.length; i++) {
			if (verts[i] == v) {
				return i;
			}
		}
		return -1;
	}
	public int indexOfLoc(GeosetVertex v) {
		for (int i = 0; i < verts.length; i++) {
			if (verts[i].equalLocs(v)) {
				return i;
			}
		}
		return -1;
	}

	public boolean equalLocs(Triangle t) {
		for (int i = 0; i < 3; i++) {
			if (!t.verts[i].equalLocs(verts[i])) {
				return false;
			}
		}
		return true;
	}

	public boolean sameLocVerts(Triangle t) {
		for (int i = 0; i < 3; i++) {
			if (!containsLoc(t.verts[i])) {
				return false;
			}
		}
		return true;
	}

	public boolean sameVerts(Triangle t) {
		for (int i = 0; i < 3; i++) {
			if (!containsRef(t.verts[i])) {
				return false;
			}
		}
		return true;
	}

	public boolean shareEdge(Triangle t) {
		int common = 0;
		for (int i = 0; i < 3; i++) {
			if (containsRef(t.verts[i])) {
				common++;
			}
		}
		return common <= 2;
	}

	public boolean containsSameVerts(GeosetVertex[] vertices) {
		for (int i = 0; i < 3; i++) {
			if (!containsRef(vertices[i])) {
				return false;
			}
		}
		return true;
	}

	public boolean containsVerts(GeosetVertex... vertices) {
		for (GeosetVertex vertex : vertices) {
			if (!containsRef(vertex)) {
				return false;
			}
		}
		return true;
	}

	public boolean equalRefs(Triangle t) {
		for (int i = 0; i < 3; i++) {
			if (t.verts[i] != verts[i]) {
				return false;
			}
		}
		return true;
	}

	public Vec2[] getTVerts(int layerId) {
		Vec2[] output = new Vec2[3];
		for (int i = 0; i < 3; i++) {
			output[i] = verts[i].getTVertex(layerId);
		}
		return output;
	}

	public Vec2 getTVert(int i,int layerId) {
		return verts[i].getTVertex(layerId);
	}

	@Override
	public String toString() {
		return verts[0] + ", " + verts[1] + ", " + verts[2];
	}

	/**
	 * Flips the triangle's orientation, and optionally the normal vectors for all the triangle's components.
	 */
	public void flip(boolean flipNormals) {
		GeosetVertex tempVert;
		tempVert = verts[2];
		verts[2] = verts[1];
		verts[1] = tempVert;

		if (flipNormals) {
			for (GeosetVertex geosetVertex : verts) {
				Vec3 normal = geosetVertex.getNormal();
				if (normal != null) {
					// Flip normals, preserve lighting!
					normal.negate();
				}
			}
		}
	}

	public GeosetVertex get(int index) {
		return verts[index];
	}

	public Triangle set(int index, GeosetVertex v) {
		verts[index] = v;
		return this;
	}

	public Triangle replace(GeosetVertex oldV, GeosetVertex newV) {
		for (int i = 0; i < verts.length; i++) {
			if (verts[i] == oldV) {
				verts[i] = newV;
				break;
			}
		}
		return this;
	}

	public int getId(int index) {
		return geoset.getVertexId(verts[index]);
	}

	public Geoset getGeoset() {
		return geoset;
	}

	public Triangle setGeoset(Geoset geoset) {
		this.geoset = geoset;
		return this;
	}

	public GeosetVertex[] getVerts() {
		return verts;
	}

	public Triangle setVerts(GeosetVertex[] verts) {
		this.verts[0] = verts[0];
		this.verts[1] = verts[1];
		this.verts[2] = verts[2];
		return this;
	}

	public Vec3 getNormal() {
		return Vec3.getPlaneNorm(verts[0], verts[1], verts[2]);
	}
}
