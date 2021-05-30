package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.ArrayList;
import java.util.List;

/**
 * GeosetVertex is a extended version of the Vertex class, for use strictly
 * inside of Geosets. The idea is that a Vertex object is used all over this
 * program for any sort of point in 3d space (PivotPoint, Min/max extents, data
 * in translations and scaling) and is strictly three connected double values,
 * while a GeosetVertex is an object that has many additional useful parts for a
 * Geoset
 * <p>
 * Eric Theller 3/9/2012
 */
public class GeosetVertex extends Vec3 {
	public int vertexGroup = -1;
	Matrix matrixRef;
	List<Vec2> tverts = new ArrayList<>();
	List<Bone> bones = new ArrayList<>();
	List<Triangle> triangles = new ArrayList<>();
	Geoset geoset;
	private Vec3 normal = new Vec3();
	private byte[] skinBoneIndexes;
	private Vec4 tangent;
	private SkinBone[] skinBones;

	public GeosetVertex(double x, double y, double z) {
		super(x, y, z);
	}

	public GeosetVertex(double x, double y, double z, Vec3 n) {
		super(x, y, z);
		normal = n;
	}

	public GeosetVertex(GeosetVertex old) {
		super(old.x, old.y, old.z);
		normal = new Vec3(old.normal);
		bones = new ArrayList<>(old.bones);
		tverts = new ArrayList<>();
		for (Vec2 tv : old.tverts) {
			tverts.add(new Vec2(tv));
		}
		// odd, but when writing
		geoset = old.geoset;
		// TODO copy triangles???????
		if (old.skinBoneIndexes != null) {
			skinBoneIndexes = old.skinBoneIndexes.clone();
		}
//        if (old.skinBones != null) {
//            skinBones = old.skinBones.clone();
//        }
//        if (old.skinBoneWeights != null) {
//            skinBoneWeights = old.skinBoneWeights.clone();
//        }
		if (old.skinBones != null) {
//            sskinBones = old.sskinBones.clone();
			setSkinBones(old.getSkinBoneBones(), old.getSkinBoneWeights());
		}
		if (old.tangent != null) {
			tangent = new Vec4(old.tangent);
		}
	}

	public void initV900() {
		skinBoneIndexes = new byte[4];
//        skinBones = new Bone[4];
//        skinBoneWeights = new short[4];
		skinBones = new SkinBone[4];
		tangent = new Vec4(0, 0, 0, 0);
	}

	public void magicSkinBones() {
		int bonesNum = Math.min(4, bones.size());
		short weight = 0;
		if (bonesNum > 0) {
			weight = (short) (255 / bonesNum);
		}

		for (int i = 0; i < 4; i++) {
			if (i < bonesNum) {
				setSkinBone(bones.get(i), weight, i);
			} else {
				setSkinBone((short) 0, i);
			}
		}
		if (!bones.isEmpty()) {
			setSkinBone(bones.get(0), (short) (weight + (255 % bonesNum)), 0);
		}
	}

	public void un900Heuristic() {
		if (tangent != null) {
			tangent = null;
		}
		if (skinBones != null) {
			bones.clear();
			boolean fallback = false;
			for (SkinBone skinBone : skinBones) {
				if (skinBone != null && skinBone.getBone() != null) {
					fallback = true;
					if (skinBone.getWeight() > 110) {
						bones.add(skinBone.getBone());
					}
				}
			}
			if (bones.isEmpty() && fallback) {
				for (SkinBone skinBone : skinBones) {
					if (skinBone != null && skinBone.getBone() != null) {
						bones.add(skinBone.getBone());
					}
				}
			}
//            skinBones = null;
//            skinBoneWeights = null;
			skinBoneIndexes = null;
		}
	}

	public void addTVertex(Vec2 v) {
		tverts.add(v);
	}

	public Vec2 getTVertex(int i) {
		try {
			return tverts.get(i);
		} catch (final IndexOutOfBoundsException e) {
			return null;
		}
	}

	public int getVertexGroup() {
		return vertexGroup;
	}

	public void setVertexGroup(int k) {
		vertexGroup = k;
	}

	public void clearBoneAttachments() {
		bones.clear();
	}

	public void clearTVerts() {
		tverts.clear();
	}

	public void addBoneAttachment(Bone b) {
		bones.add(b);
	}

	public void addBoneAttachments(List<Bone> b) {
		bones.addAll(b);
	}

	public List<Bone> getBoneAttachments() {
		return bones;
	}

	public void setMatrix(Matrix ref) {
		matrixRef = ref;
	}

	public Vec3 getNormal() {
		return normal;
	}

	public void setNormal(Vec3 n) {
		normal = n;
	}

	public List<Vec2> getTverts() {
		return tverts;
	}

	public void setTverts(List<Vec2> tverts) {
		this.tverts = tverts;
	}

	public List<Bone> getBones() {
		return bones;
	}

	public void setBones(List<Bone> bones) {
		this.bones = bones;
	}

	public List<Triangle> getTriangles() {
		return triangles;
	}

	public void setTriangles(List<Triangle> triangles) {
		this.triangles = triangles;
	}

	public GeosetVertex addTriangle(Triangle triangle) {
		triangles.add(triangle);
		return this;
	}

	public GeosetVertex removeTriangle(Triangle triangle) {
		triangles.remove(triangle);
		return this;
	}

	public GeosetVertex clearTriangles() {
		triangles.clear();
		return this;
	}

	public boolean isInTriangle(Triangle triangle) {
		return triangles.contains(triangle) && triangle.contains(this);
	}

	public boolean hasTriangle(Triangle triangle) {
		return triangles.contains(triangle);
	}

	public Geoset getGeoset() {
		return geoset;
	}

	public void setGeoset(Geoset geoset) {
		this.geoset = geoset;
	}

//    public Bone[] getSkinBones() {
//        return skinBones;
//    }

	/**
	 * @deprecated for use only with saving functionalities inside the system
	 */
	@Deprecated
	public byte[] getSkinBoneIndexes() {
		return skinBoneIndexes;
	}

	public Bone[] getSkinBoneBones() {
		if (this.skinBones == null) {
			return null;
		}
		Bone[] sb = new Bone[4];
		for (int i = 0; i < skinBones.length; i++) {
			sb[i] = skinBones[i].getBone();
		}
		return sb;
	}

//    public void setSkinBones(final Bone[] skinBones) {
//        this.skinBones = skinBones;
//    }

	public SkinBone[] getSkinBones() {
		return skinBones;
	}

//    public void setSkinBones(final Bone[] skinBones, final short[] skinBoneWeights) {
//        this.skinBones = skinBones;
//        this.skinBoneWeights = skinBoneWeights;
//    }

	public void setSkinBones(Bone[] skinBones) {
//        this.skinBones = skinBones;
		if (this.skinBones == null) {
			this.skinBones = new SkinBone[4];
		}
		for (int i = 0; i < 4; i++) {
			if (this.skinBones[i] == null) {
				this.skinBones[i] = new SkinBone(skinBones[i]);
			} else {
				this.skinBones[i].setBone(skinBones[i]);
			}
		}
	}

	public void setSkinBones(Bone[] skinBones, short[] skinBoneWeights) {
//        this.skinBones = skinBones;
//        this.skinBoneWeights = skinBoneWeights;

		if (this.skinBones == null) {
			this.skinBones = new SkinBone[4];
		}
		for (int i = 0; i < 4; i++) {
			if (this.skinBones[i] == null) {
				this.skinBones[i] = new SkinBone(skinBoneWeights[i], skinBones[i]);
			} else {
				this.skinBones[i].set(skinBoneWeights[i], skinBones[i]);
			}
		}
	}

	public void setSkinBone(Bone skinBone, short skinBoneWeight, int i) {
//        this.skinBones[i] = skinBone;
//        this.skinBoneWeights[i] = skinBoneWeight;
		if (this.skinBones[i] == null) {
			this.skinBones[i] = new SkinBone(skinBoneWeight, skinBone);
		} else {
			this.skinBones[i].set(skinBoneWeight, skinBone);
		}
	}

	public void setSkinBone(Bone skinBone, int i) {
//        this.skinBones[i] = skinBone;
		if (this.skinBones[i] == null) {
			this.skinBones[i] = new SkinBone(skinBone);
		} else {
			this.skinBones[i].setBone(skinBone);
		}
	}

//    public short[] getSkinBoneWeights() {
//        return skinBoneWeights;
//    }

	public void setSkinBone(short skinBoneWeight, int i) {
//        this.skinBoneWeights[i] = skinBoneWeight;
		if (this.skinBones[i] == null) {
			this.skinBones[i] = new SkinBone(skinBoneWeight);
		} else {
			this.skinBones[i].setWeight(skinBoneWeight);
		}
	}

//    public void setSkinBoneWeights(final short[] skinBoneWeights) {
//        this.skinBoneWeights = skinBoneWeights;
//    }

	public short[] getSkinBoneWeights() {
		if (this.skinBones == null) {
			return null;
		}
		short[] sw = new short[4];
		for (int i = 0; i < skinBones.length; i++) {
			sw[i] = skinBones[i].getWeight();
		}
		return sw;
	}

	public void setSkinBoneWeights(short[] skinBoneWeights) {
//        this.skinBoneWeights = skinBoneWeights;
		if (this.skinBones == null) {
			this.skinBones = new SkinBone[4];
		}
		for (int i = 0; i < 4; i++) {
			if (this.skinBones[i] == null) {
				this.skinBones[i] = new SkinBone(skinBoneWeights[i]);
			} else {
				this.skinBones[i].setWeight(skinBoneWeights[i]);
			}
		}
	}

	public Vec4 getTangent() {
		return tangent;
	}

	public void setTangent(float[] tangent) {
		this.tangent = new Vec4(tangent);
	}

	public void setTangent(Vec4 tangent) {
		this.tangent = tangent;
	}

	public Vec4 getTang() {
		return tangent;
	}

	public void setTangent(Vec3 tangent, float w) {
		this.tangent = new Vec4(tangent, w);
	}

	@Deprecated()
	public Matrix getMatrixRef() {
		return matrixRef;
	}

	@Override
	public Vec3 rotate(double centerX, double centerY, double centerZ, double radians,
	                   byte firstXYZ, byte secondXYZ) {
		super.rotate(centerX, centerY, centerZ, radians, firstXYZ, secondXYZ);
		normal.rotate(0, 0, 0, radians, firstXYZ, secondXYZ);
		if (tangent != null) {
			tangent.set(tangent.getVec3().rotate(0, 0, 0, radians, firstXYZ, secondXYZ));
		}
		return this;
	}

	@Override
	public Vec3 rotate(Vec3 center, double radians,
	                   byte firstXYZ, byte secondXYZ) {
		super.rotate(center, radians, firstXYZ, secondXYZ);
		normal.rotate(0, 0, 0, radians, firstXYZ, secondXYZ);
		if (tangent != null) {
//            rotateTangent(0, 0, 0, radians, firstXYZ, secondXYZ, tangent);
			tangent.set(tangent.getVec3().rotate(0, 0, 0, radians, firstXYZ, secondXYZ));
		}
		return this;
	}

	public Vec3 createNormal() {
		Vec3 sum = new Vec3();

		for (Triangle triangle : triangles) {
			sum.add(triangle.getNormal());
		}

		sum.normalize();

		return sum;
	}

	public Vec3 createNormal(List<GeosetVertex> matches) {
		Vec3 sum = new Vec3();

		for (GeosetVertex match : matches) {
			for (Triangle triangle : match.triangles) {
				sum.add(triangle.getNormal());
			}
		}

		return sum.normalize();
	}

	public Vec3 createNormal(List<GeosetVertex> matches, double maxAngle) {
		Vec3 sum = new Vec3();
		Vec3 normal = createNormal();
		List<Vec3> uniqueNormals = new ArrayList<>();
		for (GeosetVertex match : matches) {
			Vec3 matchNormal = match.createNormal();
			uniqueNormals.add(matchNormal);
		}
		uniqueNormals.stream().filter(n -> normal.degAngleTo(n) < maxAngle).forEach(sum::add);

		return sum.normalize();
	}


	public Vec3 createNormalFromFaces(List<GeosetVertex> matches, double maxAngle) {
		Vec3 sum = new Vec3();
		Vec3 normal = createNormal();
		for (GeosetVertex match : matches) {
			for (Triangle triangle : match.triangles) {
				Vec3 matchNormal = triangle.getNormal().normalize();
				double angle = normal.degAngleTo(matchNormal);
				if (angle < maxAngle) {
					sum.add(matchNormal);
				}
			}
		}

		return sum.normalize();
	}

	public void rigBones(List<Bone> matrixBones) {
		if (skinBones == null) {
			clearBoneAttachments();
			addBoneAttachments(matrixBones);
		} else {
//            Arrays.fill(skinBones, null);
//            Arrays.fill(skinBoneWeights, (short) 0);


			int weight = 255 / matrixBones.size();
			int offset = 255 - (weight * matrixBones.size());
			for (int i = 1; i < 4; i++) {
				if (i < matrixBones.size()) {
					setSkinBone(matrixBones.get(i), (short) weight, i);
				} else {
					setSkinBone((short) 0, i);
				}
			}
			setSkinBone(matrixBones.get(0), (short) (weight + offset), 0);
		}
	}

	public short[] getSkinBoneEntry() {
		short[] skinEntry = {0, 0, 0, 0, 0, 0, 0, 0};

		for (int i = 0; i < skinBones.length && i < 4; i++) {
			skinEntry[i] = skinBones[i].weight;
			skinEntry[i + 4] = (short) skinBones[i].getBoneId(geoset.getParentModel());
		}
		return skinEntry;
	}

	public static class SkinBone {
		short weight;
		Bone bone;

		SkinBone() {
		}

		SkinBone(SkinBone skinBone) {
			this.weight = skinBone.weight;
			this.bone = skinBone.bone;
		}

		SkinBone(short weight, Bone bone) {
			this.weight = weight;
			this.bone = bone;
		}

		SkinBone(short weight) {
			this.weight = weight;
			this.bone = null;
		}

		SkinBone(Bone bone) {
			this.weight = 0;
			this.bone = bone;
		}

		public SkinBone set(short weight, Bone bone) {
			this.bone = bone;
			this.weight = weight;
			return this;
		}

		public Bone getBone() {
			return bone;
		}

		public SkinBone setBone(Bone bone) {
			this.bone = bone;
			return this;
		}

		public short getWeight() {
			return weight;
		}

		public SkinBone setWeight(short weight) {
			this.weight = weight;
			return this;
		}

		int getBoneId(EditableModel model) {
			return model.getObjectId(bone);
		}

		public SkinBone copy() {
			return new SkinBone(weight, bone);
		}

		public boolean equals(SkinBone otherSkinBone) {
//            return weight == otherSkinBone.weight && bone.equals(otherSkinBone.bone);
			return weight == otherSkinBone.weight && bone == otherSkinBone.bone;
		}
	}
}
