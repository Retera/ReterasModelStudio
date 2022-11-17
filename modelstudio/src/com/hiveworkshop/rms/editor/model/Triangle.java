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
		verts[0] = a.addTriangle(this);
		verts[1] = b.addTriangle(this);
		verts[2] = c.addTriangle(this);
		geoset = geoRef;
	}

	public Triangle(GeosetVertex a, GeosetVertex b, GeosetVertex c) {
		verts[0] = a.addTriangle(this);
		verts[1] = b.addTriangle(this);
		verts[2] = c.addTriangle(this);
		geoset = null;
	}

	public void forceVertsUpdate() {
		verts[0].addTriangle(this);
		verts[1].addTriangle(this);
		verts[2].addTriangle(this);
	}

	public boolean containsRef(GeosetVertex v) {
		return verts[0] == v || verts[1] == v || verts[2] == v;
	}

	public boolean containsLoc(GeosetVertex v) {
		return verts[0].equalLocs(v) || verts[1].equalLocs(v) || verts[2].equalLocs(v);
	}

	public GeosetVertex get(int index) {
		return verts[index];
	}

	public int getId(int index) {
		return geoset.getVertexId(verts[index]);
	}

	public void set(int index, GeosetVertex v) {
		verts[index] = v;
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

	public int indexOf(GeosetVertex v) {
		for (int i = 0; i < verts.length; i++) {
			if (verts[i] == v) {
				return i;
			}
		}
		return -1;
	}
	public int indexOfLoc(GeosetVertex v) {
		int out = -1;
		for (int i = 0; i < verts.length && out == -1; i++) {
			if (verts[i].equalLocs(v)) {
				out = i;
			}
		}
		return out;
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
			if (!(verts[0] == t.verts[i]
					|| verts[1] == t.verts[i]
					|| verts[2] == t.verts[i])) {
				return false;
			}
		}
		return true;
	}

	public int indexOfRef(GeosetVertex v) {
		for (int i = 0; i < verts.length; i++) {
			if (verts[i] == v) {
				return i;
			}
		}
		return -1;
	}

	public boolean equalRefsNoIds(Triangle t) {
		for (int i = 0; i < 3; i++) {
			if (t.verts[i] != verts[i]) {
				return false;
			}
		}
		return true;
	}

	public boolean containsSameVerts(Triangle t) {
		for (int i = 0; i < 3; i++) {
			if(!(t.verts[i] == verts[0]
					|| t.verts[i] == verts[1]
					|| t.verts[i] == verts[2])){
				return false;
			}
		}

		return true;
	}
	public boolean containsSameVerts(GeosetVertex[] vertices) {
		for (int i = 0; i < 3; i++) {
			if(!(vertices[i] == verts[0]
					|| vertices[i] == verts[1]
					|| vertices[i] == verts[2])){
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

	public int[] getIntCoords(byte dim) {
		int[] output = new int[3];
		for (int i = 0; i < 3; i++) {
			output[i] = (int) (verts[i].getCoord(dim));
		}
		return output;
	}

	public double[] getCoords(byte dim) {
		double[] output = new double[3];
		for (int i = 0; i < 3; i++) {
			output[i] = (verts[i].getCoord(dim));
		}
		return output;
	}

	public Vec2[] getProjectedVerts(byte axis1, byte axis2) {
		Vec2[] output = new Vec2[3];
		for (int i = 0; i < 3; i++) {
//			output[i] = new Vec2(verts[i].getCoord(axis1), verts[i].getCoord(axis2));
			output[i] = verts[i].getProjected(axis1, axis2);
		}
		return output;
	}

	public Vec2[] getProjectedNorms(byte axis1, byte axis2) {
		Vec2[] output = new Vec2[3];
		for (int i = 0; i < 3; i++) {
//			output[i] = new Vec2(verts[i].getCoord(axis1), verts[i].getCoord(axis2));
			output[i] = verts[i].getNormal().getProjected(axis1, axis2);
		}
		return output;
	}

	public double[] getTVertCoords(byte dim, int layerId) {
		double[] output = new double[3];
		for (int i = 0; i < 3; i++) {
			output[i] = (verts[i].getTVertex(layerId).getCoord(dim));
		}
		return output;
	}

	public Vec2[] getTVerts(int layerId) {
		Vec2[] output = new Vec2[3];
		for (int i = 0; i < 3; i++) {
			output[i] = verts[i].getTVertex(layerId);
		}
		return output;
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

	public Geoset getGeoset() {
		return geoset;
	}

	public void setGeoset(Geoset geoset) {
		this.geoset = geoset;
	}

	public GeosetVertex[] getAll() {
		return verts;
	}

	public GeosetVertex[] getVerts() {
		return verts;
	}

	public void setVerts(GeosetVertex[] verts) {
		this.verts[0] = verts[0];
		this.verts[1] = verts[1];
		this.verts[2] = verts[2];
	}

	public Vec3 getNormal() {
		Vec3 edge1 = Vec3.getDiff(verts[1], verts[0]);
		Vec3 edge2 = Vec3.getDiff(verts[2], verts[1]);
		return Vec3.getCross(edge1, edge2);
	}
}
