package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class Geoset extends TimelineContainer implements Named {
	private EditableModel parentModel;
	private Material material;
	private int selectionGroup = 0;
	private int levelOfDetail = 0;
	private String levelOfDetailName = "";
	private boolean unselectable = false;
	private final ExtLog extents = new ExtLog();
	private final List<GeosetVertex> vertices = new ArrayList<>();
	private final List<Triangle> triangles = new ArrayList<>();
	private final Map<Animation, ExtLog> animExts = new HashMap<>();
	private double staticAlpha = 1;
	private final Vec3 staticColor = new Vec3(1, 1, 1);
	private boolean dropShadow = false;
	private String tempName;

	public Geoset() {
	}

	@Override
	public String getName() {
		if(tempName == null){
			if(parentModel != null){
				tempName = "# " + (parentModel.getGeosetId(this)) + ": ";
			} else {
				tempName = "# ? ";
			}
			if (levelOfDetailName.equals("")) {
				Map<Bone, List<GeosetVertex>> boneMap = getBoneMap();
				if (!boneMap.isEmpty()) {
					Set<Bone> bones = boneMap.keySet();
					bones.removeIf(Objects::isNull);
					tempName += ModelUtils.sdGetMostCommonUniqueBoneName(bones);
				} else {
					tempName += "No bones";
				}
			} else {
				tempName += levelOfDetailName;
			}
		}
		return tempName;
	}

	public void resetTempName(){
		tempName = null;
	}

	public String getName11() {
		if(parentModel != null){
			if (levelOfDetailName.equals("")) {
				Map<Bone, List<GeosetVertex>> boneMap = getBoneMap();
				if (!boneMap.isEmpty()) {
					Set<Bone> bones = boneMap.keySet();
					bones.removeIf(Objects::isNull);
					String name = ModelUtils.sdGetMostCommonUniqueBoneName(bones);
					return "# " + (parentModel.getGeosetId(this)) + ": " + name;
				}
				return "# " + (parentModel.getGeosetId(this));
			} else {
				return "# " + (parentModel.getGeosetId(this)) + ": " + levelOfDetailName;
			}
		}
		return "Geosets";
	}

	@Override
	public void setName(String text) {
		if (getParentModel() != null && getParentModel().getFormatVersion() > 900) {
			setLevelOfDetailName(text);
		}
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

	public void setMaterial(final Material m) {
		material = m;
	}

	public Material getMaterial() {
		return material;
	}

	public void setExtents(final ExtLog extents) {
		this.extents.set(extents);
	}

	public ExtLog getExtents() {
		return extents;
	}

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

	public boolean isEmpty() {
		return vertices.size() <= 0;
	}

	public List<GeosetVertex> getVertices() {
		return vertices;
	}

	public void addVerticies(Collection<GeosetVertex> vertex) {
		this.vertices.addAll(vertex);
	}

	public List<Triangle> getTriangles() {
		return triangles;
	}

	public void clearMatrices() {
//		matrices.clear();
	}
	public Set<Matrix> collectMatrices() {
		LinkedHashSet<Matrix> matrixSet = new LinkedHashSet<>();
		for (GeosetVertex vertex : vertices) {
			matrixSet.add(vertex.getMatrix());
		}
		return matrixSet;
	}

	public void reMakeMatrixList(){
//		matrices.clear();
//		for (GeosetVertex vertex : vertices) {
//			if (!matrices.contains(vertex.getMatrix())) {
//				matrices.add(vertex.getMatrix());
//			}
//		}
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

		return boneMap;
	}

	public Geoset deepCopy(){
		Geoset geoset = emptyCopy();

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
		return geoset;
	}

	public Geoset emptyCopy(){
		Geoset geoset = new Geoset();
		geoset.setExtents(extents.deepCopy());

		geoset.setLevelOfDetailName(levelOfDetailName);
		for(Animation anim : animExts.keySet()){
			geoset.add(anim, animExts.get(anim).deepCopy());
		}
		geoset.setMaterial(material);
		geoset.setSelectionGroup(selectionGroup);
		geoset.setParentModel(parentModel);
		geoset.setLevelOfDetail(levelOfDetail);
		geoset.setUnselectable(unselectable);
		geoset.staticAlpha = staticAlpha;
		geoset.staticColor.set(staticColor);
		geoset.dropShadow = dropShadow;


		for (AnimFlag<?> animFlag : getAnimFlags()) {
			geoset.add(animFlag.deepCopy());
		}

		return geoset;
	}

	public boolean hasAnim(){
		return !animFlags.isEmpty() || staticAlpha != 1f || !staticColor.equalLocs(Vec3.ONE);
	}

	public float getRenderVisibility(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getRenderVisibility(animatedRenderEnvironment, (float) staticAlpha);
	}

	@Override
	public String visFlagName() {
		return "Alpha";
	}

	public double getStaticAlpha() {
		return staticAlpha;
	}

	public void setStaticAlpha(double staticAlpha) {
		this.staticAlpha = staticAlpha;
	}

	public Vec3 getStaticColor() {
		return staticColor;
	}

	public void setStaticColor(Vec3 staticColor) {
		this.staticColor.set(staticColor);
	}

	public boolean isDropShadow() {
		return dropShadow;
	}

	public void setDropShadow(boolean dropShadow) {
		this.dropShadow = dropShadow;
	}

	public Vec3 getRenderColor(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, "Color", staticColor);
	}
}
