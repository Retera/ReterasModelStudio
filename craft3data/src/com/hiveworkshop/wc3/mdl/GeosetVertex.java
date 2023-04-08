package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * GeosetVertex is a extended version of the Vertex class, for use strictly
 * inside of Geosets. The idea is that a Vertex object is used all over this
 * program for any sort of point in 3d space (PivotPoint, Min/max extents, data
 * in translations and scaling) and is strictly three connected double values,
 * while a GeosetVertex is an object that has many additional useful parts for a
 * Geoset
 *
 * Eric Theller 3/9/2012
 */
public class GeosetVertex extends Vertex {
	Matrix matrixRef;
	private Normal normal;
	public int VertexGroup = -1;
	List<TVertex> tverts = new ArrayList<>();
	private List<GeosetVertexBoneLink> links = new ArrayList<>();
	List<Triangle> triangles = new ArrayList<>();
	private byte[] skinBoneIndexes;
	private float[] tangent;

	Geoset geoset;

	public GeosetVertex(final double x, final double y, final double z) {
		super(x, y, z);
	}

	public GeosetVertex(final double x, final double y, final double z, final Normal n) {
		super(x, y, z);
		normal = n;
	}

	public void initV900Tangent() {
		tangent = new float[4];
	}

	public void initV900Skin() {
		skinBoneIndexes = new byte[4];
		while (links.size() > 4) {
			links.remove(links.size() - 1);
		}
		equalizeWeights();
	}

	public void equalizeWeights() {
		if (links.isEmpty()) {
			return;
		}
		final short weight = (short) (255 / links.size());
		final short offsetWeight = (short) (255 - (weight * links.size()));
		if (!links.isEmpty()) {
			links.get(0).weight = (short) (weight + offsetWeight);
			for (int i = 1; i < links.size(); i++) {
				links.get(i).weight = (weight);
			}
		}
	}

	public void un900Heuristic() {
		if (tangent != null) {
			tangent = null;
		}
	}

	public GeosetVertex(final GeosetVertex old) {
		super(old.x, old.y, old.z);
		this.normal = new Normal(old.normal);
		this.links = new ArrayList<>();
		for (final GeosetVertexBoneLink link : old.links) {
			this.links.add(new GeosetVertexBoneLink(link.weight, link.bone));
		}
		this.tverts = new ArrayList<>();
		for (final TVertex tv : old.tverts) {
			tverts.add(new TVertex(tv));
		}
		// odd, but when writing
		this.geoset = old.geoset;
		// TODO copy triangles???????
		if (old.tangent != null) {
			this.tangent = old.tangent.clone();
		}
	}

	public void addTVertex(final TVertex v) {
		tverts.add(v);
	}

	public TVertex getTVertex(final int i) {
		try {
			return tverts.get(i);
		}
		catch (final ArrayIndexOutOfBoundsException e) {
			return null;
		}
		catch (final IndexOutOfBoundsException e) {
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
		links.clear();
	}

	public void clearTVerts() {
		tverts.clear();
	}

	public void addBoneAttachment(final short weight, final Bone b) {
		links.add(new GeosetVertexBoneLink(weight, b));
	}

	public List<GeosetVertexBoneLink> getLinks() {
		return links;
	}

	public void updateMatrixRef(final ArrayList<Matrix> list) {
		try {
			matrixRef = list.get(VertexGroup);
		}
		catch (final Exception e) {
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
		}
		catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: Vertex coordinates could not be interpreted.");
		}
		try {
			y = Double.parseDouble(entries[1]);
		}
		catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: Vertex coordinates could not be interpreted.");
		}
		try {
			z = Double.parseDouble(entries[2].split("}")[0]);
		}
		catch (final NumberFormatException e) {
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

	public List<Triangle> getTriangles() {
		return triangles;
	}

	public void setTriangles(final ArrayList<Triangle> triangles) {
		this.triangles = triangles;
	}

	public Geoset getGeoset() {
		return geoset;
	}

	public float[] getTangent() {
		return tangent;
	}

	public byte[] getSkinBoneIndexes() {
		return skinBoneIndexes;
	}

	public short getSkinBoneWeight(final int j) {
		if ((j >= 0) && (j < links.size())) {
			return links.get(j).weight;
		}
		return 0;
	}

	public void setTangent(final float[] tangent) {
		this.tangent = tangent;
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
		if (tangent != null) {
			rotateTangent(0, 0, 0, radians, firstXYZ, secondXYZ, tangent);
		}
	}

	public static void rotateTangent(final double centerX, final double centerY, final double centerZ,
			final double radians, final byte firstXYZ, final byte secondXYZ, final float[] vertex) {
		final double x1 = vertex[firstXYZ];
		final double y1 = vertex[secondXYZ];
		final double cx;// = coordinateSystem.geomX(centerX);
		switch (firstXYZ) {
		case 0:
			cx = centerX;
			break;
		case 1:
			cx = centerY;
			break;
		default:
		case 2:
			cx = centerZ;
			break;
		}
		final double dx = x1 - cx;
		final double cy;// = coordinateSystem.geomY(centerY);
		switch (secondXYZ) {
		case 0:
			cy = centerX;
			break;
		case 1:
			cy = centerY;
			break;
		default:
		case 2:
			cy = centerZ;
			break;
		}
		final double dy = y1 - cy;
		final double r = Math.sqrt((dx * dx) + (dy * dy));
		double verAng = Math.acos(dx / r);
		if (dy < 0) {
			verAng = -verAng;
		}
		// if( getDimEditable(dim1) )
		double nextDim = (Math.cos(verAng + radians) * r) + cx;
		if (!Double.isNaN(nextDim)) {
			vertex[firstXYZ] = (float) ((Math.cos(verAng + radians) * r) + cx);
		}
		// if( getDimEditable(dim2) )
		nextDim = (Math.sin(verAng + radians) * r) + cy;
		if (!Double.isNaN(nextDim)) {
			vertex[secondXYZ] = (float) ((Math.sin(verAng + radians) * r) + cy);
		}
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

	public Vertex createNormal(final List<GeosetVertex> matches) {
		final Vertex sum = new Vertex(0, 0, 0);
		for (final GeosetVertex match : matches) {
			for (final Triangle triangle : match.triangles) {
				final Vertex perpendicular = triangle.verts[0].delta(triangle.verts[1])
						.crossProduct(triangle.verts[1].delta(triangle.verts[2]));
				double vectorMagnitude = perpendicular.vectorMagnitude();
				if (vectorMagnitude == 0) {
					vectorMagnitude = 0.00001;
				}
				perpendicular.x /= vectorMagnitude;
				perpendicular.y /= vectorMagnitude;
				perpendicular.z /= vectorMagnitude;
				sum.x += perpendicular.x;
				sum.y += perpendicular.y;
				sum.z += perpendicular.z;
			}
		}
		double vectorMagnitude = sum.vectorMagnitude();
		if (vectorMagnitude == 0) {
			vectorMagnitude = 0.00001;
		}
		for (int i = 0; i < 3; i++) {
			sum.setCoord((byte) i, sum.getCoord((byte) i) / vectorMagnitude);
		}
		return sum;
	}

	public void setBoneAttachments(final List<Bone> bones2) {
		clearBoneAttachments();
		for (int i = 0; i < bones2.size(); i++) {
			addBoneAttachment((short) 0, bones2.get(i));
		}
		equalizeWeights();
	}

	public void setBoneAttachmentsRaw(final List<GeosetVertexBoneLink> bones2) {
		this.links = bones2;
	}

	public boolean isLinked(final IdObject bone) {
		for (final GeosetVertexBoneLink link : links) {
			if (link.bone == bone) {
				return true;
			}
		}
		return false;
	}

	public boolean isLinkingSameBones(final List<Bone> bones) {
		if (bones.size() != links.size()) {
			return false;
		}
		for (int i = 0; i < links.size(); i++) {
			if (bones.get(i) != links.get(i).bone) {
				return false;
			}
		}
		return true;
	}

}
