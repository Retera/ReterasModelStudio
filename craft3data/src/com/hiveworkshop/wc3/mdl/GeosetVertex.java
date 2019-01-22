package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * GeosetVertex is a extended version of the Vertex class, for use strictly inside of Geosets. The idea is that a Vertex
 * object is used all over this program for any sort of point in 3d space (PivotPoint, Min/max extents, data in
 * translations and scaling) and is strictly three connected double values, while a GeosetVertex is an object that has
 * many additional useful parts for a Geoset
 *
 * Eric Theller 3/9/2012
 */
public class GeosetVertex extends Vertex {
	Matrix matrixRef;
	private Normal normal;
	public int VertexGroup;
	List<TVertex> tverts = new ArrayList<>();
	List<Bone> bones = new ArrayList<>();
	List<Triangle> triangles = new ArrayList<>();

	Geoset geoset;

	public GeosetVertex(final double x, final double y, final double z) {
		super(x, y, z);
	}

	public GeosetVertex(final double x, final double y, final double z, final Normal n) {
		super(x, y, z);
		normal = n;
	}

	public GeosetVertex(final GeosetVertex old) {
		super(old.x, old.y, old.z);
		this.normal = new Normal(old.normal);
		this.bones = new ArrayList<>(old.bones);
		this.tverts = new ArrayList<>();
		for (final TVertex tv : old.tverts) {
			tverts.add(new TVertex(tv));
		}
		// odd, but when writing
		this.geoset = old.geoset;
		// TODO copy triangles???????
	}

	public void addTVertex(final TVertex v) {
		tverts.add(v);
	}

	public TVertex getTVertex(final int i) {
		try {
			return tverts.get(i);
		} catch (final ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public void setVertexGroup(final int k) {
		VertexGroup = k;
	}

	public int getVertexGroup() {
		return VertexGroup;
	}

	public void clearBoneAttachments() {
		bones.clear();
	}

	public void clearTVerts() {
		tverts.clear();
	}

	public void addBoneAttachment(final Bone b) {
		bones.add(b);
	}

	public void addBoneAttachments(final ArrayList<Bone> b) {
		bones.addAll(b);
	}

	public List<Bone> getBoneAttachments() {
		return bones;
	}

	public void updateMatrixRef(final ArrayList<Matrix> list) {
		try {
			matrixRef = list.get(VertexGroup);
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error in Matrices: VertexGroup does not reference a real matrix id!");
		}
	}

	public void setMatrix(final Matrix ref) {
		matrixRef = ref;
	}

	public void setNormal(final Normal n) {
		normal = n;
	}

	public Normal getNormal() {
		return normal;
	}

	public static GeosetVertex parseText(final String input) {
		final String[] entries = input.split(",");
		GeosetVertex temp = null;
		double x = 0;
		double y = 0;
		double z = 0;
		try {
			x = Double.parseDouble(entries[0].split("\\{")[1]);
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: Vertex coordinates could not be interpreted.");
		}
		try {
			y = Double.parseDouble(entries[1]);
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: Vertex coordinates could not be interpreted.");
		}
		try {
			z = Double.parseDouble(entries[2].split("}")[0]);
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: Vertex coordinates could not be interpreted.");
		}
		temp = new GeosetVertex(x, y, z);
		return temp;
	}

	public List<TVertex> getTverts() {
		return tverts;
	}

	public void setTverts(final ArrayList<TVertex> tverts) {
		this.tverts = tverts;
	}

	public List<Bone> getBones() {
		return bones;
	}

	public void setBones(final List<Bone> bones) {
		this.bones = bones;
	}

	public List<Triangle> getTriangles() {
		return triangles;
	}

	public void setTriangles(final ArrayList<Triangle> triangles) {
		this.triangles = triangles;
	}

	public Geoset getGeoset() {
		return geoset;
	}

	public void setGeoset(final Geoset geoset) {
		this.geoset = geoset;
	}

	@Deprecated()
	public Matrix getMatrixRef() {
		return matrixRef;
	}

	@Override
	public void rotate(final double centerX, final double centerY, final double centerZ, final double radians,
			final byte firstXYZ, final byte secondXYZ) {
		super.rotate(centerX, centerY, centerZ, radians, firstXYZ, secondXYZ);
		// TODO fix bad design, use interface or something instead of bizarre
		// override
		normal.rotate(0, 0, 0, radians, firstXYZ, secondXYZ);
	}

	public Vertex createNormal() {
		final Vertex sum = new Vertex(0, 0, 0);
		for (final Triangle triangle : triangles) {
			final Vertex perpendicular = triangle.verts[0].delta(triangle.verts[1])
					.crossProduct(triangle.verts[1].delta(triangle.verts[2]));
			sum.x += perpendicular.x;
			sum.y += perpendicular.y;
			sum.z += perpendicular.z;
		}
		final double vectorMagnitude = sum.vectorMagnitude();
		for (int i = 0; i < 3; i++) {
			sum.setCoord((byte) i, sum.getCoord((byte) i) / vectorMagnitude);
		}
		return sum;
	}
}
