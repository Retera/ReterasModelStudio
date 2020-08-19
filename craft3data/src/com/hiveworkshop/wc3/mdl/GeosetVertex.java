package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;
import java.util.List;

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
	List<Bone> bones = new ArrayList<>();
	List<Triangle> triangles = new ArrayList<>();
	private byte[] skinBoneIndexes;
	private Bone[] skinBones;
	private short[] skinBoneWeights;
	private float[] tangent;

	Geoset geoset;

	public GeosetVertex(final double x, final double y, final double z) {
		super(x, y, z);
	}

	public GeosetVertex(final double x, final double y, final double z, final Normal n) {
		super(x, y, z);
		normal = n;
	}

	public void initV900() {
		skinBoneIndexes = new byte[4];
		skinBones = new Bone[4];
		skinBoneWeights = new short[4];
		tangent = new float[4];
	}

	public void un900Heuristic() {
		if (tangent != null) {
			tangent = null;
		}
		if (skinBones != null) {
			bones.clear();
			int index = 0;
			boolean fallback = false;
			for (final Bone bone : skinBones) {
				if (bone != null) {
					fallback = true;
					if (skinBoneWeights[index] > 110) {
						bones.add(bone);
					}
				}
				index++;
			}
			if (bones.isEmpty() && fallback) {
				for (final Bone bone : skinBones) {
					if (bone != null) {
						bones.add(bone);
					}
				}
			}
			skinBones = null;
			skinBoneWeights = null;
			skinBoneIndexes = null;
		}
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
		if (old.skinBoneIndexes != null) {
			this.skinBoneIndexes = old.skinBoneIndexes.clone();
		}
		if (old.skinBones != null) {
			this.skinBones = old.skinBones.clone();
		}
		if (old.skinBoneWeights != null) {
			this.skinBoneWeights = old.skinBoneWeights.clone();
		}
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

	public void addBoneAttachments(final List<Bone> b) {
		bones.addAll(b);
	}

	public List<Bone> getBoneAttachments() {
		return bones;
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

	public List<TVertex> getTverts() {
		return tverts;
	}

	public void setTverts(final List<TVertex> tverts) {
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

	public void setTriangles(final List<Triangle> triangles) {
		this.triangles = triangles;
	}

	public Geoset getGeoset() {
		return geoset;
	}

	/**
	 * @return
	 * @deprecated for use only with saving functionalities inside the system
	 */
	@Deprecated
	public byte[] getSkinBoneIndexes() {
		return skinBoneIndexes;
	}

	public Bone[] getSkinBones() {
		return skinBones;
	}

	public void setSkinBones(final Bone[] skinBones) {
		this.skinBones = skinBones;
	}

	public short[] getSkinBoneWeights() {
		return skinBoneWeights;
	}

	public void setSkinBoneWeights(final short[] skinBoneWeights) {
		this.skinBoneWeights = skinBoneWeights;
	}

	public float[] getTangent() {
		return tangent;
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
				final double vectorMagnitude = perpendicular.vectorMagnitude();
				perpendicular.x /= vectorMagnitude;
				perpendicular.y /= vectorMagnitude;
				perpendicular.z /= vectorMagnitude;
				sum.x += perpendicular.x;
				sum.y += perpendicular.y;
				sum.z += perpendicular.z;
			}
		}
		final double vectorMagnitude = sum.vectorMagnitude();
		for (int i = 0; i < 3; i++) {
			sum.setCoord((byte) i, sum.getCoord((byte) i) / vectorMagnitude);
		}
		return sum;
	}
}
