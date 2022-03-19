package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.*;

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
	private final Matrix matrix = new Matrix();
//	private int vertexGroup = -1;
	private List<Vec2> tverts = new ArrayList<>();
	private Set<Triangle> triangles = new HashSet<>();
	private Geoset geoset;
	private Vec3 normal = new Vec3();
//	private byte[] skinBoneIndexes;
	private Vec4 tangent;
	private SkinBone[] skinBones;

	public GeosetVertex(double x, double y, double z) {
		super(x, y, z);
	}

	public GeosetVertex(double x, double y, double z, Vec3 n) {
		super(x, y, z);
		normal = n;
	}
	public GeosetVertex(Vec3 pos, Vec3 n) {
		super(pos);
		normal = n;
	}

	private GeosetVertex(GeosetVertex old) {
		super(old);
		normal.set(old.getNormal());
		matrix.addAll(old.matrix.getBones());
		for (Vec2 tv : old.tverts) {
			tverts.add(new Vec2(tv));
		}

		geoset = old.geoset;
		// TODO copy triangles???????
		triangles.addAll(old.getTriangles());
//		vertexGroup = old.vertexGroup;
		if (old.skinBones != null) {
			setSkinBones(old.getSkinBoneBones(), old.getSkinBoneWeights());
		}
		if (old.tangent != null) {
			tangent = new Vec4(old.tangent);
		}
	}

	public GeosetVertex deepCopy(){
		return new GeosetVertex(this);
	}

	public void initV900() {
		skinBones = new SkinBone[4];
		tangent = new Vec4(0, 1, 0, 1);
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

	public void clearTVerts() {
		tverts.clear();
	}

	public int getMatrixIndex(){
		return geoset.getMatrices().indexOf(matrix);
	}

	public void clearBoneAttachments() {
		matrix.clear();
	}

	public void addBoneAttachment(Bone b) {
		matrix.add(b);
	}

	public void addBoneAttachment(int i, Bone b) {
		matrix.add(i, b);
	}

	public void addBoneAttachments(Collection<Bone> b) {
		matrix.addAll(b);
	}

	public List<Bone> getBones() {
		return matrix.getBones();
	}

	public List<Bone> getAllBones() {
		if(skinBones != null){
			List<Bone> bones = new ArrayList<>();
			for (SkinBone skinBone : skinBones){
				if(skinBone != null && skinBone.getBone() != null){
					bones.add(skinBone.getBone());
				}
			}
			return bones;
		}
		return matrix.getBones();
	}


	public void setBone(int i, final Bone bone) {
		matrix.set(i, bone);
	}

	public void removeBone(Bone b) {
		matrix.remove(b);
	}

	public GeosetVertex removeBones(Collection<Bone> bones) {
		matrix.removeAll(bones);
		return this;
	}

	public GeosetVertex replaceBones(Map<IdObject, IdObject> newBoneMap) {
		if(skinBones != null){
			for (SkinBone skinBone : skinBones){
				if (skinBone != null && skinBone.getBone() != null){
					IdObject idObject = newBoneMap.get(skinBone.getBone());
					if(idObject instanceof Bone && !(idObject instanceof Helper)){
						skinBone.setBone((Bone) idObject);
					}
				}
			}
		}
		matrix.replaceBones(newBoneMap);
		return this;
	}
	public GeosetVertex replaceBones(Map<IdObject, IdObject> newBoneMap, boolean removeIfNull) {
		if(skinBones != null){
			for (SkinBone skinBone : skinBones){
				if (skinBone != null && skinBone.getBone() != null){
					IdObject idObject = newBoneMap.get(skinBone.getBone());
					if(idObject instanceof Bone && !(idObject instanceof Helper)){
						skinBone.setBone((Bone) idObject);
					}
				}
			}
		}
		matrix.replaceBones(newBoneMap, removeIfNull);
		return this;
	}

	public Matrix getMatrix() {
		return matrix;
	}

	public void setBones(List<Bone> bones) {
		matrix.clear();
		matrix.addAll(bones);
	}

	public Vec3 getNormal() {
		return normal;
	}

	public void setNormal(Vec3 n) {
		normal = n;
	}

	public void setNormalValue(Vec3 n) {
		if (n == null) {
			normal = null;
		} else if (normal == null) {
			normal = new Vec3(n);
		} else {
			normal.set(n);
		}
	}

	public List<Vec2> getTverts() {
		return tverts;
	}

	public void setTverts(List<Vec2> tverts) {
		this.tverts = tverts;
	}

	public Set<Triangle> getTriangles() {
		return triangles;
	}

	public void setTriangles(Set<Triangle> triangles) {
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

	public GeosetVertex removeTriangles(Collection<Triangle> triangles) {
		this.triangles.removeAll(triangles);
		return this;
	}

	public GeosetVertex clearTriangles() {
		triangles.clear();
		return this;
	}

	public boolean isInTriangle(Triangle triangle) {
		return triangles.contains(triangle) && triangle.containsLoc(this);
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

	public Bone[] getSkinBoneBones() {
		if (this.skinBones == null) {
			return null;
		}
		Bone[] sb = new Bone[4];
		for (int i = 0; i < skinBones.length; i++) {
			if(skinBones[i] != null){
				sb[i] = skinBones[i].getBone();
			} else {
				sb[i] = null;
			}
		}
		return sb;
	}

	public SkinBone[] getSkinBones() {
		return skinBones;
	}

	public void setSkinBones(Bone[] bones) {
//        this.bones = bones;
		if (this.skinBones == null) {
			this.skinBones = new SkinBone[4];
		}
		for (int i = 0; i < 4; i++) {
			if (this.skinBones[i] == null) {
				this.skinBones[i] = new SkinBone(bones[i]);
			} else {
				this.skinBones[i].setBone(bones[i]);
			}
		}
	}

	public void setSkinBones(Bone[] bones, short[] weights) {
//        this.bones = bones;
//        this.weights = weights;

		if (this.skinBones == null) {
			this.skinBones = new SkinBone[4];
		}
		for (int i = 0; i < 4; i++) {
			if (this.skinBones[i] == null) {
				this.skinBones[i] = new SkinBone(weights[i], bones[i]);
			} else {
				this.skinBones[i].set(weights[i], bones[i]);
			}
		}
	}

	public void setSkinBone(Bone bone, short weight, int i) {
//        this.skinBones[i] = bone;
//        this.skinBoneWeights[i] = weight;
		if (this.skinBones[i] == null) {
			this.skinBones[i] = new SkinBone(weight, bone);
		} else {
			this.skinBones[i].set(weight, bone);
		}
	}

	public void setSkinBone(Bone bone, int i) {
//        this.skinBones[i] = bone;
		if (this.skinBones[i] == null) {
			this.skinBones[i] = new SkinBone(bone);
		} else {
			this.skinBones[i].setBone(bone);
		}
	}
	public void setSkinBone(SkinBone skinBone, int i) {
        this.skinBones[i] = skinBone;
//		if (this.skinBones[i] == null) {
//			this.skinBones[i] = new SkinBone(skinBone);
//		} else {
//			this.skinBones[i].setBone(skinBone);
//		}
	}

//    public short[] getSkinBoneWeights() {
//        return skinBoneWeights;
//    }

	public void setSkinBone(short weight, int i) {
//        this.skinBoneWeights[i] = weight;
		if (this.skinBones[i] == null) {
			this.skinBones[i] = new SkinBone(weight);
		} else {
			this.skinBones[i].setWeight(weight);
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
			if(skinBones[i] != null){
				sw[i] = skinBones[i].getWeight();
			} else {
				sw[i] = 0;
			}
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

	public void normalizeBoneWeights(){
		float totWeight = 0;
		for (int i = 0; i < 4; i++) {
			if (this.skinBones[i] != null) {
				totWeight += this.skinBones[i].getWeight();
			}
		}

		float totWeight2 = 255;
		for (int i = 0; i < 4; i++) {
			if (this.skinBones[i] != null) {
				this.skinBones[i].setWeight((short) ((this.skinBones[i].getWeight()/totWeight)*255));
				totWeight2 -= this.skinBones[i].getWeight();
			}
		}

		if (totWeight2 != 0 && this.skinBones[0] != null) {
			this.skinBones[0].setWeight((short) (this.skinBones[0].getWeight() + totWeight2));
		}
	}

	public void initSkinBones(){
		if(skinBones == null){
			skinBones = new SkinBone[4];
		}
	}

	public SkinBone[] removeSkinBones(){
		SkinBone[] skinBones = this.skinBones;
		this.skinBones = null;
		return skinBones;
	}

	public Vec4 getTangent() {
		return tangent;
	}

	public void setTangent(float[] tangent) {
		this.tangent.set(tangent);
	}

	public void setTangent(Vec4 tangent) {
		this.tangent = tangent;
	}

	public void removeTangent(){
		this.tangent = null;
	}

	public Vec4 getTang() {
		return tangent;
	}

	public void setTangent(Vec3 tangent, float w) {
		if(this.tangent == null){
			this.tangent = new Vec4(tangent, w);
		} else {
			this.tangent.set(tangent, w);
		}
	}

	@Override
	public Vec3 rotate(Vec3 center, double radians,
	                   byte firstXYZ, byte secondXYZ) {
		super.rotate(center, radians, firstXYZ, secondXYZ);
		normal.rotate(Vec3.ZERO, radians, firstXYZ, secondXYZ);
		if (tangent != null) {
			tangent.rotateAsVec3(Vec3.ZERO, radians, firstXYZ, secondXYZ);
		}
		return this;
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

//	public static class SkinBone {
//		short weight;
//		Bone bone;
//
//		SkinBone() {
//		}
//
//		SkinBone(SkinBone skinBone) {
//			this.weight = skinBone.weight;
//			this.bone = skinBone.bone;
//		}
//
//		SkinBone(short weight, Bone bone) {
//			this.weight = weight;
//			this.bone = bone;
//		}
//
//		SkinBone(short weight) {
//			this.weight = weight;
//			this.bone = null;
//		}
//
//		SkinBone(Bone bone) {
//			this.weight = 0;
//			this.bone = bone;
//		}
//
//		public SkinBone set(short weight, Bone bone) {
//			this.bone = bone;
//			this.weight = weight;
//			return this;
//		}
//
//		public Bone getBone() {
//			return bone;
//		}
//
//		public SkinBone setBone(Bone bone) {
//			this.bone = bone;
//			return this;
//		}
//
//		public short getWeight() {
//			return weight;
//		}
//		public float getWeightFraction() {
//			return weight/255f;
//		}
//
//		public SkinBone setWeight(short weight) {
//			this.weight = weight;
//			return this;
//		}
//
//		int getBoneId(EditableModel model) {
//			return model.getObjectId(bone);
//		}
//
//		public SkinBone copy() {
//			return new SkinBone(weight, bone);
//		}
//
//		public boolean equals(SkinBone otherSkinBone) {
////            return weight == otherSkinBone.weight && bone.equals(otherSkinBone.bone);
//			return weight == otherSkinBone.weight && bone == otherSkinBone.bone;
//		}
//	}
}
