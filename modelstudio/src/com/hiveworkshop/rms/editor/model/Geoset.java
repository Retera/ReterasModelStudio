package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;

import java.util.*;

public class Geoset implements Named, VisibilitySource {
	private ExtLog extents;
	private List<GeosetVertex> vertices = new ArrayList<>();
	private List<Triangle> triangles = new ArrayList<>();
	private List<Matrix> matrices = new ArrayList<>();
	private final Map<Animation, ExtLog> animExts = new HashMap<>();
	private Material material;
	private int selectionGroup = 0;
	private EditableModel parentModel;
	private GeosetAnim geosetAnim = null;
	private int levelOfDetail = 0;
	private String levelOfDetailName = "";
	private List<float[]> tangents;
	private boolean unselectable = false;

	public Geoset() {
	}

	@Override
	public String getName() {
		if (levelOfDetailName.equals("")) {
			Map<Bone, List<GeosetVertex>> boneMap = getBoneMap();
			if (!boneMap.isEmpty()) {
				Set<Bone> bones = boneMap.keySet();
				bones.removeIf(Objects::isNull);
				String name = sdGetMostCommonUniqueBoneName(bones);
				return "# " + (parentModel.getGeosetId(this)) + ": " + name;
			}
		} else {
			return "# " + (parentModel.getGeosetId(this)) + ": " + levelOfDetailName;
		}
		if (parentModel != null) {
			return "# " + (parentModel.getGeosetId(this));
		}
		return "Geosets";
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

				if ((lp != null && lp.getGeoset() == this || lp instanceof Helper) && !nonSharedParentBones.contains(lp)) {
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
			List<String> nameParts = new ArrayList<>();
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
	public void addVertex(final GeosetVertex v) {
		add(v);
	}

	public void add(final GeosetVertex v) {
		if (!vertices.contains(v) && v.isValid()) {
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
		matrices.add(v);
	}

	public Matrix getMatrix(final int vertId) {
		if ((vertId < 0) && (vertId >= -128)) {
			return getMatrix(256 + vertId);
		}
		if (vertId >= matrices.size()) {
			return null;
		}
		return matrices.get(vertId);
	}

	public int getMatrixId(Matrix matrix){
		return matrices.indexOf(matrix);
	}

	public int numMatrices() {
		return matrices.size();
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

//	public void add(final Animation a) {
//		anims.add(a);
//	}
//	public Animation getAnim(final int id) {
//		return anims.get(id);
//	}

	public void add(Animation a, ExtLog e) {
		if (e != null){
			animExts.put(a, e);
		} else {
			animExts.remove(a);
		}
	}
	public ExtLog getAnimExtent(Animation a) {
		return animExts.get(a);
	}

	public Map<Animation, ExtLog> getAnimExts() {
		return animExts;
	}

//	public int numAnims() {
//		return anims.size();
//	}

	public void reMakeMatrixList(){
		matrices.clear();
		for (GeosetVertex vertex : vertices) {
			if (!matrices.contains(vertex.getMatrix())) {
				matrices.add(vertex.getMatrix());
			}
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

	public List<Matrix> getMatrices() {
		return matrices;
	}

	public void setMatrices(final List<Matrix> matrices) {
		this.matrices = matrices;
	}

//	public List<Animation> getAnims() {
//		return anims;
//	}

//	public void setAnims(final List<Animation> anims) {
//		this.anims = anims;
//	}

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

	public void cureVertTries() {
		Set<Triangle> triangleSet = new HashSet<>(triangles);
		for (GeosetVertex vertex : vertices) {
			vertex.getTriangles().removeIf(t -> !triangleSet.contains(t));
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

	public Map<Bone, List<GeosetVertex>> getBoneMap() {
		Map<Bone, List<GeosetVertex>> boneMap = new HashMap<>();
		for (GeosetVertex geosetVertex : getVertices()) {
			SkinBone[] ssb = geosetVertex.getSkinBones();
			if (ssb != null) {
				for (SkinBone skinBone : ssb) {
					if (skinBone != null && skinBone.getBone() != null && skinBone.getWeight() > 0) {
						boneMap.computeIfAbsent(skinBone.getBone(), k -> new ArrayList<>()).add(geosetVertex);
					}
				}
			} else {
				for (Bone bone : geosetVertex.getBones()) {
					boneMap.computeIfAbsent(bone, k -> new ArrayList<>()).add(geosetVertex);
				}
			}
		}

//		for (GeosetVertex geosetVertex : getVertices()) {
//			SkinBone[] ssb = geosetVertex.getSkinBones();
//			if (ssb != null) {
//				for (SkinBone skinBone : ssb) {
//					if (skinBone != null && skinBone.getBone() != null) {
//						if (!boneMap.containsKey(skinBone.getBone())) {
//							boneMap.put(skinBone.getBone(), new ArrayList<>());
//						}
//						if (skinBone.getWeight() > 0) {
////						System.out.println("added geoVert");
//							boneMap.get(skinBone.getBone()).add(geosetVertex);
//						}
//					}
//				}
//			} else {
//				for (Bone bone : geosetVertex.getBones()) {
//					if (!boneMap.containsKey(bone)) {
//						boneMap.put(bone, new ArrayList<>());
//					}
//					boneMap.get(bone).add(geosetVertex);
//				}
//			}
//		}

		return boneMap;
	}

	public void setTangents(List<float[]> tangents) {
		this.tangents = tangents;
	}

	public List<float[]> getTangents() {
		return tangents;
	}

	public Geoset deepCopy(){
		Geoset geoset = new Geoset();
		geoset.setExtents(extents.deepCopy());
		Map<GeosetVertex, GeosetVertex> oldToNew = new HashMap<>();
		for(GeosetVertex geosetVertex : vertices){
			GeosetVertex newVertex = oldToNew.computeIfAbsent(geosetVertex, k -> geosetVertex.deepCopy());
			newVertex.clearTriangles();
			newVertex.setGeoset(geoset);
			geoset.add(newVertex);
		}
		for(Triangle triangle : triangles){
			GeosetVertex v0 = oldToNew.get(triangle.get(0));
			GeosetVertex v1 = oldToNew.get(triangle.get(1));
			GeosetVertex v2 = oldToNew.get(triangle.get(2));
			geoset.add(new Triangle(v0, v1, v2, geoset));
		}
		geoset.setLevelOfDetailName(levelOfDetailName);
		for(Animation anim : animExts.keySet()){
			geoset.add(anim, animExts.get(anim).deepCopy());
		}
		geoset.setMaterial(material);
		geoset.setSelectionGroup(selectionGroup);
		geoset.setParentModel(parentModel);
		geoset.setLevelOfDetail(levelOfDetail);
		if (tangents != null) {
			geoset.setTangents(new ArrayList<>(tangents));
		}
		geoset.setUnselectable(unselectable);
		if (geosetAnim != null) {
			GeosetAnim geosetAnimC = geosetAnim.deepCopy();
			geoset.setGeosetAnim(geosetAnimC);
			geosetAnimC.setGeoset(geoset);
		}
		return geoset;
	}

}
