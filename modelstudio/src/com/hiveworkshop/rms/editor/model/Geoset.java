package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class Geoset implements Named, VisibilitySource {
	ExtLog extents;
	List<GeosetVertex> vertices = new ArrayList<>();
	List<Triangle> triangles = new ArrayList<>();
	List<Matrix> matrix = new ArrayList<>();
	List<Animation> anims = new ArrayList<>();
	Material material;
	int selectionGroup = 0;
	EditableModel parentModel;
	GeosetAnim geosetAnim = null;
	int levelOfDetail = -1;
	String levelOfDetailName = "";
	List<short[]> skin;
	List<float[]> tangents;
	boolean unselectable = false;

	public Geoset() {
	}

	@Override
//	public String getName() {
//		if (levelOfDetailName.equals("")) {
//			if (skin != null) {
//				String name = hdGetMostCommonUniqueBoneName();
//				if (name.equals("")) {
//					Map<Bone, List<GeosetVertex>> boneMap = getBoneMap();
//					matrix.get(0).getName();
//				}
//				return "Geoset " + (parentModel.getGeosetId(this)) + ": " + name;
//			} else if (!matrix.isEmpty()) {
//				Map<Bone, List<GeosetVertex>> boneMap = getBoneMap();
//
//				String name = sdGetMostCommonUniqueBoneName();
//				if (name.equals("")) {
//					matrix.get(0).getName();
//				}
////				return "Geoset " + (parentModel.getGeosetId(this)) + ": " + matrix.get(0).getName();
//				return "Geoset " + (parentModel.getGeosetId(this)) + ": " + name;
//			}
//		} else {
//			return "Geoset " + (parentModel.getGeosetId(this)) + ": " + levelOfDetailName;
//		}
//		return "Geoset " + (parentModel.getGeosetId(this));// parentModel.getName() // + "
//	}
	public String getName() {
		if (levelOfDetailName.equals("")) {
			if (skin != null && !skin.isEmpty()) {
				String name = hdGetMostCommonUniqueBoneName();
				if (name.equals("")) {
					Map<Bone, List<GeosetVertex>> boneMap = getBoneMap();
//					matrix.get(0).getName();
				}
//				return "Geoset " + (parentModel.getGeosetId(this)) + ": " + name;
				return "# " + (parentModel.getGeosetId(this)) + ": " + name;
			} else {
				Map<Bone, List<GeosetVertex>> boneMap = getBoneMap();
				if (!boneMap.isEmpty()) {
					Set<Bone> bones = boneMap.keySet();
					String name = sdGetMostCommonUniqueBoneName(bones);
//					if (name.equals("")) {
//						matrix.get(0).getName();
//					}
//				return "Geoset " + (parentModel.getGeosetId(this)) + ": " + matrix.get(0).getName();
//					return "Geoset " + (parentModel.getGeosetId(this)) + ": " + name;
					return "# " + (parentModel.getGeosetId(this)) + ": " + name;
				}
			}
		} else {
//			return "Geoset " + (parentModel.getGeosetId(this)) + ": " + levelOfDetailName;
			return "# " + (parentModel.getGeosetId(this)) + ": " + levelOfDetailName;
		}
//		return "Geoset " + (parentModel.getGeosetId(this));// parentModel.getName() // + "
		return "# " + (parentModel.getGeosetId(this));// parentModel.getName() // + "
	}

	@Override
	public void setName(String text) {
		if (getParentModel() != null && getParentModel().getFormatVersion() > 900) {
			setLevelOfDetailName(text);
		}
	}

	public String sdGetMostCommonUniqueBoneName(Set<Bone> bones) {
		List<Bone> nonSharedParentBones = new ArrayList<>();
		List<Bone> mBones = new ArrayList<>(bones);
		if (!bones.isEmpty()) {
			if (mBones.size() == 1) {
				return mBones.get(0).getName().replaceAll("(?i)bone_*", "");
			}
			for (Bone bone : mBones) {
				Bone lp = lastParentIn(bone, mBones);

				if ((lp.getGeoset() == this || lp instanceof Helper) && !nonSharedParentBones.contains(lp)) {
					nonSharedParentBones.add(lp);
				}
			}
			List<Bone> curatedBones = new ArrayList<>();
			for (Bone bone : nonSharedParentBones) {
				if (!bone.getName().toLowerCase().startsWith("mesh")) {
					curatedBones.add(bone);
				}
			}
			if (curatedBones.size() < 3) {
				for (int i = 0; i < nonSharedParentBones.size() && curatedBones.size() < 3; i++) {
					if (!curatedBones.contains(nonSharedParentBones.get(i))) {
						curatedBones.add(nonSharedParentBones.get(i));
					}
				}
			}
//			for (Bone bone : nonSharedParentBones) {
//				if(!nonSharedParentBones.contains(bone.getParent())){
//					curatedBones.add(bone);
//				}
//			}
			List<String> nameParts = new ArrayList<>();
//			for (Bone bone : nonSharedParentBones) {
//				nameParts.add(bone.getName().replaceAll("(?i)bone_*", ""));
//			}
			for (Bone bone : curatedBones) {
				nameParts.add(bone.getName().replaceAll("(?i)bone_*", ""));
			}
			return String.join(", ", nameParts);
		}
		return "";
	}

	public String sdGetMostCommonUniqueBoneName() {
		List<Bone> nonSharedParentBones = new ArrayList<>();
		List<Bone> mBones = new ArrayList<>();
		if (!matrix.isEmpty()) {
			for (Matrix m : matrix) {
				mBones.addAll(m.getBones());
			}

			if (mBones.size() == 1) {
				return mBones.get(0).getName().replaceAll("(?i)bone_*", "");
			}
			for (Bone bone : mBones) {
				Bone lp = lastParentIn(bone, mBones);

				if ((lp.getGeoset() == this || lp instanceof Helper) && !nonSharedParentBones.contains(lp)) {
					nonSharedParentBones.add(lp);
				}
			}
			List<Bone> curatedBones = new ArrayList<>();
			for (Bone bone : nonSharedParentBones) {
				if (!bone.getName().toLowerCase().startsWith("mesh")) {
					curatedBones.add(bone);
				}
			}
			if (curatedBones.size() < 3) {
				for (int i = 0; i < nonSharedParentBones.size() && curatedBones.size() < 3; i++) {
					if (!curatedBones.contains(nonSharedParentBones.get(i))) {
						curatedBones.add(nonSharedParentBones.get(i));
					}
				}
			}
//			for (Bone bone : nonSharedParentBones) {
//				if(!nonSharedParentBones.contains(bone.getParent())){
//					curatedBones.add(bone);
//				}
//			}
			List<String> nameParts = new ArrayList<>();
//			for (Bone bone : nonSharedParentBones) {
//				nameParts.add(bone.getName().replaceAll("(?i)bone_*", ""));
//			}
			for (Bone bone : curatedBones) {
				nameParts.add(bone.getName().replaceAll("(?i)bone_*", ""));
			}
			return String.join(", ", nameParts);
		}
		return "";
	}


	public Bone lastParentIn(Bone bone, List<Bone> list) {
		Bone parentBone = bone;
		int infStopper = 0;
		while (list.contains(parentBone) && parentBone != null && infStopper < 1000) {
			if (bone.getParent() instanceof Bone) {
				parentBone = (Bone) bone.getParent();
				if (parentBone.isMultiGeo() || (parentBone.getGeoset() != this && parentBone.getGeoset() != null)) {
					return bone;
				} else if (list.contains(parentBone)) {
					bone = parentBone;
				}
			} else {
				return bone;
			}
			infStopper++;
		}
		return bone;
	}


	public String hdGetMostCommonUniqueBoneName() {
		List<Bone> nonSharedParentBones = new ArrayList<>();
		List<Bone> mBones = new ArrayList<>();
		Set<Short> parentIds = new HashSet<>();
		Set<Short> prioParentIds = new HashSet<>();
		if (skin != null) {
			for (short[] vert : skin) {
				for (int i = 0; i < 4; i++) {
					if (vert[i + 4] > 60) {
						parentIds.add(vert[i]);
						if (vert[i + 4] > 250) {
							prioParentIds.add(vert[i]);
						}
					}
				}
			}
			if (prioParentIds.isEmpty()) {
				for (short s : parentIds) {
					mBones.add(parentModel.getBone(s));
				}
			} else {
				for (short s : prioParentIds) {
					mBones.add(parentModel.getBone(s));
				}
			}

			if (mBones.size() == 1) {
				return mBones.get(0).getName().replaceAll("[Bb][Oo][Nn][Ee]_*", "");
			}
			for (Bone bone : mBones) {
				Bone lp = lastParentIn(bone, mBones);
				if (lp.getGeoset() == this && !nonSharedParentBones.contains(lp)) {
					nonSharedParentBones.add(lp);
				}
			}
			List<String> nameParts = new ArrayList<>();
			for (Bone bone : nonSharedParentBones) {
				nameParts.add(bone.getName().replaceAll("[Bb][Oo][Nn][Ee]_*", ""));
			}
			return String.join(", ", nameParts);
		}
		return "";
	}

	public void addVertex(final GeosetVertex v) {
		add(v);
	}

	public void add(final GeosetVertex v) {
		if (!vertices.contains(v)) {
			vertices.add(v);
		}
	}

	public GeosetVertex getVertex(final int vertId) {
		return vertices.get(vertId);
	}

	public int getVertexId(final GeosetVertex v) {
		return vertices.indexOf(v);
	}

	public void remove(final GeosetVertex v) {
		vertices.remove(v);
	}

	public void remove(Collection<GeosetVertex> v) {
		vertices.removeAll(v);
	}

	public boolean contains(final Triangle t) {
		return triangles.contains(t);
	}

	public boolean contains(final GeosetVertex v) {
		return vertices.contains(v);
	}

	public int numVerteces() {
		return vertices.size();
	}

	public int numUVLayers() {
		return vertices.get(0).getTverts().size();
	}

	public void setTriangles(final List<Triangle> list) {
		triangles = list;
	}

	public void addTriangle(final Triangle p) {
		// Left for compat
		add(p);
	}

	public void addTriangles(Collection<Triangle> t) {
		triangles.addAll(t);
	}

	public void add(final Triangle p) {
		if (!triangles.contains(p)) {
			triangles.add(p);
		}
//		else {
////			System.out.println("2x triangles");
//		}
	}

	public Triangle getTriangle(final int triId) {
		return triangles.get(triId);
	}

	/**
	 * Returns all vertices that directly inherit motion from the specified Bone, or
	 * an empty list if no vertices reference the object.
	 */
	public List<GeosetVertex> getChildrenOf(final Bone parent) {
		final List<GeosetVertex> children = new ArrayList<>();
		for (final GeosetVertex gv : vertices) {
			if (gv.getBones().contains(parent) || Arrays.stream(gv.getSkinBoneBones()).anyMatch(bone -> bone == parent)) {
				children.add(gv);
			}
		}
		return children;
	}

	public int numTriangles() {
		return triangles.size();
	}

	public void removeTriangle(final Triangle t) {
		triangles.remove(t);
	}

	public void removeTriangles(Collection<Triangle> t) {
		triangles.removeAll(t);
	}

	public void addMatrix(final Matrix v) {
		matrix.add(v);
	}

	public Matrix getMatrix(final int vertId) {
		if ((vertId < 0) && (vertId >= -128)) {
			return getMatrix(256 + vertId);
		}
		if (vertId >= matrix.size()) {
			return null;
		}
		return matrix.get(vertId);
	}

	public int numMatrices() {
		return matrix.size();
	}

	public void setMaterial(final Material m) {
		material = m;
	}

	public Material getMaterial() {
		return material;
	}

	public void setExtLog(final ExtLog e) {
		extents = e;
	}

	public ExtLog getExtLog() {
		return extents;
	}

	public void add(final Animation a) {
		anims.add(a);
	}

	public Animation getAnim(final int id) {
		return anims.get(id);
	}

	public int numAnims() {
		return anims.size();
	}

	public void applyMatricesToVertices(EditableModel mdlr) {
		for (GeosetVertex gv : getVertices()) {
			gv.clearBoneAttachments();
			Matrix mx = getMatrix(gv.getVertexGroup());
			if (((gv.getVertexGroup() == -1) || (mx == null))) {
				if (!ModelUtils.isTangentAndSkinSupported(mdlr.getFormatVersion())) {
					throw new IllegalStateException("You have empty vertex groupings but FormatVersion is 800. Did you load HD mesh into an SD model?");
				}
			} else {
				mx.updateIds(mdlr);
				for (Bone bone : mx.getBones()) {
					gv.addBoneAttachment(bone);
				}
			}
		}
	}

	public void applyVerticesToMatrices(EditableModel mdlr) {
		matrix.clear();
		for (GeosetVertex vertex : vertices) {
			Matrix newTemp = vertex.getMatrix();
//			Matrix newTemp = new Matrix(vertex.getBones());
//			for (Matrix m : matrix) {
//				if (newTemp.equals(m)) {
//					newTemp = m;
//					break;
//				}
//			}
			if (!matrix.contains(newTemp)) {
				matrix.add(newTemp);
				newTemp.updateIds(mdlr);
			}
			vertex.setVertexGroup(matrix.indexOf(newTemp));
//			vertex.setMatrix(newTemp);
		}
	}

	public boolean isEmpty() {
		return vertices.size() <= 0;
	}

	public GeosetAnim forceGetGeosetAnim() {
		if (geosetAnim == null) {
			geosetAnim = new GeosetAnim(this);
			parentModel.add(geosetAnim);
		}
		return geosetAnim;
	}

	@Override
	public AnimFlag<Float> getVisibilityFlag() {
		if (geosetAnim != null) {
			return geosetAnim.getVisibilityFlag();
		}
		return null;
	}

	@Override
	public void setVisibilityFlag(AnimFlag<Float> a) {
		if (a != null) {
			forceGetGeosetAnim().setVisibilityFlag(a);
		}
	}

	@Override
	public String visFlagName() {
		return "Alpha";
	}

	public ExtLog getExtents() {
		return extents;
	}

	public void setExtents(final ExtLog extents) {
		this.extents = extents;
	}

	public List<GeosetVertex> getVertices() {
		return vertices;
	}

	public void addVerticies(Collection<GeosetVertex> vertex) {
		this.vertices.addAll(vertex);
	}

	public void setVertex(final List<GeosetVertex> vertex) {
		this.vertices = vertex;
	}

	public List<Triangle> getTriangles() {
		return triangles;
	}

	public void setTriangle(final List<Triangle> triangle) {
		triangles = triangle;
	}

	public List<Matrix> getMatrix() {
		return matrix;
	}

	public void setMatrix(final List<Matrix> matrix) {
		this.matrix = matrix;
	}

	public List<Animation> getAnims() {
		return anims;
	}

	public void setAnims(final List<Animation> anims) {
		this.anims = anims;
	}

	public int getSelectionGroup() {
		return selectionGroup;
	}

	public void setSelectionGroup(final int selectionGroup) {
		this.selectionGroup = selectionGroup;
	}

	public boolean getUnselectable() {
		return unselectable;
	}

	public void setUnselectable(final boolean unselectable) {
		this.unselectable = unselectable;
	}

	public void setLevelOfDetail(final int levelOfDetail) {
		this.levelOfDetail = levelOfDetail;
	}

	public void setLevelOfDetailName(final String levelOfDetailName) {
		this.levelOfDetailName = levelOfDetailName;
	}

	public int getLevelOfDetail() {
		return levelOfDetail;
	}

	public String getLevelOfDetailName() {
		return levelOfDetailName;
	}

	public EditableModel getParentModel() {
		return parentModel;
	}

	public void setParentModel(final EditableModel parentModel) {
		this.parentModel = parentModel;
	}

	public GeosetAnim getGeosetAnim() {
		return geosetAnim;
	}

	public void setGeosetAnim(final GeosetAnim geosetAnim) {
		this.geosetAnim = geosetAnim;
	}

	public void remove(final Triangle tri) {
		triangles.remove(tri);
	}

	public void removeExtended(final Triangle tri) {
		triangles.remove(tri);
		for (GeosetVertex vertex : tri.getVerts()) {
			vertex.removeTriangle(tri);
		}
	}

	public void addExtended(final Triangle tri) {
		triangles.add(tri);
		for (GeosetVertex vertex : tri.getVerts()) {
			vertex.addTriangle(tri);
		}
	}

	public boolean isHD() {
		return getParentModel().getFormatVersion() >= 900 && !getVertices().isEmpty() && getVertex(0).getTangent() != null;
	}

	public ExtLog calculateExtent() {
		double maximumDistanceFromCenter = 0;
		Vec3 max = new Vec3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
		Vec3 min = new Vec3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

		for (final GeosetVertex geosetVertex : vertices) {
			max.maximize(geosetVertex);
			min.minimize(geosetVertex);

			final double distanceFromCenter = geosetVertex.length();
			if (distanceFromCenter > maximumDistanceFromCenter) {
				maximumDistanceFromCenter = distanceFromCenter;
			}
		}
		return new ExtLog(min, max, maximumDistanceFromCenter);
	}

	public void makeHd() {
		final List<GeosetVertex> vertices = getVertices();
		for (final GeosetVertex gv : vertices) {
			final Vec3 normal = gv.getNormal();
			gv.initV900();
			if (normal != null) {
				gv.setTangent(normal, 1);
			}
			gv.magicSkinBones();
		}
	}

	public void makeSd() {
		for (final GeosetVertex vertex : getVertices()) {
			vertex.un900Heuristic();
		}
	}

	public Map<Bone, List<GeosetVertex>> getBoneMap() {
		Map<Bone, List<GeosetVertex>> boneMap = new HashMap<>();
		for (GeosetVertex geosetVertex : getVertices()) {
			Bone[] sb = geosetVertex.getSkinBoneBones();
			short[] bw = geosetVertex.getSkinBoneWeights();
			if (sb != null && bw != null) {
				for (int i = 0; i < sb.length; i++) {
					if (!boneMap.containsKey(sb[i])) {
						boneMap.put(sb[i], new ArrayList<>());
					}
					if (bw[i] > 0) {
//						System.out.println("added geoVert");
						boneMap.get(sb[i]).add(geosetVertex);
					}
				}
			} else {
				for (Bone bone : geosetVertex.getBones()) {
					if (!boneMap.containsKey(bone)) {
						boneMap.put(bone, new ArrayList<>());
					}
					boneMap.get(bone).add(geosetVertex);
				}
			}
		}
		return boneMap;
	}

	public void setSkin(List<short[]> skin) {
		this.skin = skin;
	}

	public List<short[]> getSkin() {
		return skin;
	}

	public void setTangents(List<float[]> tangents) {
		this.tangents = tangents;
	}

	public List<float[]> getTangents() {
		return tangents;
	}
}
