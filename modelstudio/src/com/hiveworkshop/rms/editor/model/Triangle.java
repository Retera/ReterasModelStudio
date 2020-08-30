package com.hiveworkshop.rms.editor.model;

import java.util.List;

import com.hiveworkshop.rms.util.Vec3;

public class Triangle {
	GeosetVertex[] verts = new GeosetVertex[3];
	int[] vertIds = new int[3];
	Geoset geoset;

	public Triangle(final GeosetVertex a, final GeosetVertex b, final GeosetVertex c, final Geoset geoRef) {
		verts[0] = a;
		verts[1] = b;
		verts[2] = c;
		geoset = geoRef;
	}

	public Triangle(final int a, final int b, final int c, final Geoset geoRef) {
		vertIds[0] = a;
		vertIds[1] = b;
		vertIds[2] = c;
		verts[0] = geoRef.getVertex(a);
		verts[1] = geoRef.getVertex(b);
		verts[2] = geoRef.getVertex(c);
		geoset = geoRef;
	}

	public Triangle(final GeosetVertex a, final GeosetVertex b, final GeosetVertex c) {
		verts[0] = a;
		verts[1] = b;
		verts[2] = c;
		geoset = null;
	}

	public Triangle(final int a, final int b, final int c) {
		vertIds[0] = a;
		vertIds[1] = b;
		vertIds[2] = c;
		// m_verts[0] = geoRef.getVertex(a);
		// m_verts[1] = geoRef.getVertex(b);
		// m_verts[2] = geoRef.getVertex(c);
		geoset = null;
	}

	public void setGeoRef(final Geoset geoRef) {
		geoset = geoRef;
	}

	public void updateVertexRefs() {
		verts[0] = geoset.getVertex(vertIds[0]);
		verts[1] = geoset.getVertex(vertIds[1]);
		verts[2] = geoset.getVertex(vertIds[2]);
	}

	public void updateVertexIds() {
		// Potentially this procedure could lag a bunch in the way I wrote it,
		// but it will
		// change vertex ids to match a changed geoset, assuming the geoset
		// still contains the
		// vertex
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

	public void updateVertexIds(final Geoset geoRef) {
		geoset = geoRef;
		updateVertexIds();
	}

	public void updateVertexRefs(final List<GeosetVertex> list) {
		verts[0] = list.get(vertIds[0]);
		verts[1] = list.get(vertIds[1]);
		verts[2] = list.get(vertIds[2]);
	}

	public boolean containsRef(final GeosetVertex v) {
		return verts[0] == v || verts[1] == v || verts[2] == v;
	}

	public boolean contains(final GeosetVertex v) {
		return verts[0].equalLocs(v) || verts[1].equalLocs(v) || verts[2].equalLocs(v);
	}

	public GeosetVertex get(final int index) {
		return verts[index];
	}

	public int getId(final int index) {
		return vertIds[index];
	}

	public void set(final int index, final GeosetVertex v) {
		verts[index] = v;
		vertIds[index] = geoset.getVertexId(v);
	}

	public int indexOf(final GeosetVertex v) {
		int out = -1;
		for (int i = 0; i < verts.length && out == -1; i++) {
			if (verts[i].equalLocs(v)) {
				out = i;
			}
		}
		return out;
	}

	public boolean equalLocs(final Triangle t) {
		boolean equal = true;
		for (int i = 0; i < 3 && equal; i++) {
			if (!t.verts[i].equalLocs(verts[i]) || t.vertIds[i] != vertIds[i]) {
				equal = false;
			}
		}
		return equal;
	}

	public boolean sameVerts(final Triangle t) {
		boolean equal = true;
		for (int i = 0; i < 3 && equal; i++) {
			if (!contains(t.verts[i])) {
				equal = false;
			}
		}
		return equal;
	}

	public int indexOfRef(final GeosetVertex v) {
		int out = -1;
		for (int i = 0; i < verts.length && out == -1; i++) {
			if (verts[i] == v) {
				out = i;
			}
		}
		return out;
	}

	public boolean equalRefsNoIds(final Triangle t) {
		boolean equal = true;
		for (int i = 0; i < 3 && equal; i++) {
            if (t.verts[i] != verts[i]) {
                equal = false;
                break;
            }
		}
		return equal;
	}

	public boolean equalRefs(final Triangle t) {
		boolean equal = true;
		for (int i = 0; i < 3 && equal; i++) {
            if (t.verts[i] != verts[i] || t.vertIds[i] != vertIds[i]) {
                equal = false;
                break;
            }
		}
		return equal;
	}

	public GeosetVertex[] getAll() {
		return verts;
	}

	public int[] getIntCoords(final byte dim) {
		final int[] output = new int[3];
		for (int i = 0; i < 3; i++) {
			output[i] = (int) (verts[i].getCoord(dim));
		}
		return output;
	}

	public double[] getCoords(final byte dim) {
		final double[] output = new double[3];
		for (int i = 0; i < 3; i++) {
			output[i] = (verts[i].getCoord(dim));
		}
		return output;
	}

	public double[] getTVertCoords(final byte dim, final int layerId) {
		final double[] output = new double[3];
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
	public void flip(final boolean flipNormals) {
		GeosetVertex tempVert;
		int tempVertId;
		tempVert = verts[2];
		tempVertId = vertIds[2];
		verts[2] = verts[1];
		vertIds[2] = vertIds[1];
		verts[1] = tempVert;
		vertIds[1] = tempVertId;
		if (flipNormals) {
			for (final GeosetVertex geosetVertex : verts) {
				final Vec3 normal = geosetVertex.getNormal();
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

	public void setGeoset(final Geoset geoset) {
		this.geoset = geoset;
	}

	public GeosetVertex[] getVerts() {
		return verts;
	}

	public void setVerts(final GeosetVertex[] verts) {
		this.verts = verts;
	}

	public int[] getVertIds() {
		return vertIds;
	}

	public void setVertIds(final int[] vertIds) {
		this.vertIds = vertIds;
	}

	public Vec3 getNormal() {
		final Vec3 edge1 = new Vec3();
		final Vec3 edge2 = new Vec3();
		final Vec3 normal = new Vec3();
		
		verts[1].sub(verts[0], edge1);
		verts[2].sub(verts[1], edge2);
		edge1.cross(edge2, normal);

		return normal;
	}
}
