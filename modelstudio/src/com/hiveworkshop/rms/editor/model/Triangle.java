package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.List;

public class Triangle {
	GeosetVertex[] verts = new GeosetVertex[3];
	int[] vertIds = new int[3];
	Geoset geoset;

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

	public Triangle(int a, int b, int c, Geoset geoRef) {
		vertIds[0] = a;
		vertIds[1] = b;
		vertIds[2] = c;
		verts[0] = geoRef.getVertex(a).addTriangle(this);
		verts[1] = geoRef.getVertex(b).addTriangle(this);
		verts[2] = geoRef.getVertex(c).addTriangle(this);
		geoset = geoRef;
	}

	public Triangle(GeosetVertex a, GeosetVertex b, GeosetVertex c) {
		verts[0] = a.addTriangle(this);
		verts[1] = b.addTriangle(this);
		verts[2] = c.addTriangle(this);
		geoset = null;
	}
//	public Triangle(GeosetVertex a, GeosetVertex b, GeosetVertex c) {
//		verts[0] = a;
//		verts[1] = b;
//		verts[2] = c;
//		geoset = null;
//	}

	public Triangle(int a, int b, int c) {
		vertIds[0] = a;
		vertIds[1] = b;
		vertIds[2] = c;
		// m_verts[0] = geoRef.getVertex(a);
		// m_verts[1] = geoRef.getVertex(b);
		// m_verts[2] = geoRef.getVertex(c);
		geoset = null;
	}

	public void setGeoRef(Geoset geoRef) {
		geoset = geoRef;
	}

	public void updateVertexRefs() {
		verts[0] = geoset.getVertex(vertIds[0]);
		verts[1] = geoset.getVertex(vertIds[1]);
		verts[2] = geoset.getVertex(vertIds[2]);
	}

	public void updateVertexIds() {
		// Potentially this procedure could lag a bunch in the way I wrote it,
		// but it will change vertex ids to match a changed geoset,
		// assuming the geoset still contains the vertex
		vertIds[0] = geoset.getVertexId(verts[0]);
		vertIds[1] = geoset.getVertexId(verts[1]);
		vertIds[2] = geoset.getVertexId(verts[2]);
	}

	public void forceVertsUpdate() {
		if (!verts[0].triangles.contains(this)) {
			verts[0].triangles.add(this);
		}
		if (!verts[1].triangles.contains(this)) {
			verts[1].triangles.add(this);
		}
		if (!verts[2].triangles.contains(this)) {
			verts[2].triangles.add(this);
		}
	}

	public void updateVertexIds(Geoset geoRef) {
		geoset = geoRef;
		updateVertexIds();
	}

	public void updateVertexRefs(List<GeosetVertex> list) {
		verts[0] = list.get(vertIds[0]);
		verts[1] = list.get(vertIds[1]);
		verts[2] = list.get(vertIds[2]);
	}

	public boolean containsRef(GeosetVertex v) {
		return verts[0] == v || verts[1] == v || verts[2] == v;
	}

	public boolean contains(GeosetVertex v) {
		return verts[0].equalLocs(v) || verts[1].equalLocs(v) || verts[2].equalLocs(v);
	}

	public GeosetVertex get(int index) {
		return verts[index];
	}

	public int getId(int index) {
		return vertIds[index];
	}

	public void set(int index, GeosetVertex v) {
		verts[index] = v;
		vertIds[index] = geoset.getVertexId(v);
	}

	public int indexOf(GeosetVertex v) {
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
			if (!t.verts[i].equalLocs(verts[i]) || t.vertIds[i] != vertIds[i]) {
				return false;
			}
		}
		return true;
	}

	public boolean sameVerts(Triangle t) {
		for (int i = 0; i < 3; i++) {
			if (!contains(t.verts[i])) {
				return false;
			}
		}
		return true;
	}

	public int indexOfRef(GeosetVertex v) {
		int out = -1;
		for (int i = 0; i < verts.length && out == -1; i++) {
			if (verts[i] == v) {
				out = i;
			}
		}
		return out;
	}

	public boolean equalRefsNoIds(Triangle t) {
		for (int i = 0; i < 3; i++) {
			if (t.verts[i] != verts[i]) {
				return false;
			}
		}
		return true;
	}

	public boolean equalRefs(Triangle t) {
		for (int i = 0; i < 3; i++) {
			if (t.verts[i] != verts[i] || t.vertIds[i] != vertIds[i]) {
				return false;
			}
		}
		return true;
	}

	public GeosetVertex[] getAll() {
		return verts;
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

	@Override
	public String toString() {
		return vertIds[0] + ", " + vertIds[1] + ", " + vertIds[2];
	}

	/**
	 * Flips the triangle's orientation, and optionally the normal vectors for all the triangle's components.
	 */
	public void flip(boolean flipNormals) {
		GeosetVertex tempVert;
		int tempVertId;
		tempVert = verts[2];
		tempVertId = vertIds[2];
		verts[2] = verts[1];
		vertIds[2] = vertIds[1];
		verts[1] = tempVert;
		vertIds[1] = tempVertId;
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

	public GeosetVertex[] getVerts() {
		return verts;
	}

	public void setVerts(GeosetVertex[] verts) {
		this.verts = verts;
	}

	public int[] getVertIds() {
		return vertIds;
	}

	public void setVertIds(int[] vertIds) {
		this.vertIds = vertIds;
	}

	public Vec3 getNormal() {
		Vec3 edge1 = Vec3.getDiff(verts[1], verts[0]);
		Vec3 edge2 = Vec3.getDiff(verts[2], verts[1]);
		return Vec3.getCross(edge1, edge2);
	}
}
