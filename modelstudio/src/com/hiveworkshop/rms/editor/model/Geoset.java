package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
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
		if (tempName == null) {
			if (parentModel != null) {
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

	public void resetTempName() {
		tempName = null;
	}

	public boolean isOpaque() {
		return  staticAlpha == 1 && (getVisibilityFlag() == null || getVisibilityFlag().size() == 0) && material.isOpaque();
	}

	@Override
	public void setName(String text) {
		setLevelOfDetailName(text);
	}

	public Geoset addVertex(final GeosetVertex v) {
		add(v);
		return this;
	}

	public Geoset add(final GeosetVertex v) {
		if (!vertices.contains(v) && v.isValid()) {
			vertices.add(v);
		}
		return this;
	}

	public GeosetVertex getVertex(final int vertId) {
		return vertices.get(vertId);
	}

	public int getVertexId(final GeosetVertex v) {
		return vertices.indexOf(v);
	}

	public Geoset remove(final GeosetVertex v) {
		vertices.remove(v);
		return this;
	}

	public Geoset remove(Collection<GeosetVertex> v) {
		vertices.removeAll(v);
		return this;
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
		return vertices.isEmpty() ? 0 : vertices.get(0).getTverts().size();
	}

	public Geoset addTriangle(final Triangle p) {
		// Left for compat
		add(p);
		return this;
	}

	public Geoset addTriangles(Collection<Triangle> t) {
		triangles.addAll(t);
		return this;
	}

	public Geoset add(final Triangle p) {
		if (!triangles.contains(p)) {
			triangles.add(p);
		}
		return this;
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

	public Geoset removeTriangle(final Triangle t) {
		triangles.remove(t);
		return this;
	}

	public Geoset removeTriangles(Collection<Triangle> t) {
		triangles.removeAll(t);
		return this;
	}

	public Geoset setMaterial(final Material m) {
		material = m;
		return this;
	}

	public Material getMaterial() {
		return material;
	}

	public Geoset setExtents(final ExtLog extents) {
		this.extents.set(extents);
		return this;
	}

	public ExtLog getExtents() {
		return extents;
	}

	public Geoset add(Animation a, ExtLog e) {
		if (e != null) {
			animExts.put(a, e);
		} else {
			animExts.remove(a);
		}
		return this;
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

	public Geoset addVerticies(Collection<GeosetVertex> vertex) {
		this.vertices.addAll(vertex);
		return this;
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
			Matrix matrix = vertex.getMatrix();
			if (!matrix.isEmpty()) {
				matrixSet.add(matrix);
			}
		}
		return matrixSet;
	}

	public Set<Bone> collectBones() {
		Set<Bone> boneSet = new HashSet<>();
		for (GeosetVertex vertex : vertices) {
			boneSet.addAll(vertex.getAllBones());
		}
		return boneSet;
	}

	public void reMakeMatrixList() {
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

	public Geoset setSelectionGroup(final int selectionGroup) {
		this.selectionGroup = selectionGroup;
		return this;
	}

	public boolean getUnselectable() {
		return unselectable;
	}

	public Geoset setUnselectable(final boolean unselectable) {
		this.unselectable = unselectable;
		return this;
	}

	public Geoset setLevelOfDetail(final int levelOfDetail) {
		this.levelOfDetail = levelOfDetail;
		return this;
	}

	public Geoset setLevelOfDetailName(final String levelOfDetailName) {
		this.levelOfDetailName = levelOfDetailName;
		return this;
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

	public Geoset setParentModel(final EditableModel parentModel) {
		this.parentModel = parentModel;
		return this;
	}

	public Geoset remove(final Triangle tri) {
		triangles.remove(tri);
		return this;
	}

	public Geoset cureVertTries() {
		Set<Triangle> triangleSet = new HashSet<>(triangles);
		for (GeosetVertex vertex : vertices) {
			vertex.getTriangles().removeIf(t -> !triangleSet.contains(t));
		}
		return this;
	}

	public boolean isHD() {
//		return 900 <= getParentModel().getFormatVersion() && !getVertices().isEmpty() && (getVertex(0).getTangent() != null);
		return 900 <= getParentModel().getFormatVersion() && !getVertices().isEmpty() && (getVertex(0).getTangent() != null || getVertex(0).getSkinBones() != null);
	}
	public boolean hasSkin() {
		return !getVertices().isEmpty() && getVertex(0).getSkinBones() != null;
	}
	public boolean hasTangents() {
		return !getVertices().isEmpty() && getVertex(0).getTangent() != null;
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
	public Set<Bone> getBones() {
		Set<Bone> bones = new HashSet<>();
		vertices.forEach(v -> bones.addAll(v.getAllBones()));
		return bones;
	}

	public Geoset deepCopy() {
		Geoset geoset = emptyCopy();

		Map<GeosetVertex, GeosetVertex> oldToNew = new HashMap<>();
		for (GeosetVertex geosetVertex : vertices) {
			GeosetVertex newVertex = oldToNew.computeIfAbsent(geosetVertex, k -> geosetVertex.deepCopy());
			newVertex.clearTriangles();
			newVertex.setGeoset(geoset);
			geoset.add(newVertex);
		}
		for (Triangle triangle : triangles) {
			GeosetVertex v0 = oldToNew.get(triangle.get(0));
			GeosetVertex v1 = oldToNew.get(triangle.get(1));
			GeosetVertex v2 = oldToNew.get(triangle.get(2));
			geoset.add(new Triangle(v0, v1, v2, geoset).addToVerts());
		}
		return geoset;
	}

	public Geoset emptyCopy() {
		Geoset geoset = new Geoset();
		geoset.setExtents(extents.deepCopy());

		geoset.setLevelOfDetailName(levelOfDetailName);
		for (Animation anim : animExts.keySet()) {
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

	public boolean hasAnim() {
		return !animFlags.isEmpty() || staticAlpha != 1f || !staticColor.equalLocs(Vec3.ONE);
	}

	public float getRenderVisibility(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getRenderVisibility(animatedRenderEnvironment, (float) staticAlpha);
	}

	@Override
	public String visFlagName() {
		return MdlUtils.TOKEN_ALPHA;
	}

	public double getStaticAlpha() {
		return staticAlpha;
	}

	public Geoset setStaticAlpha(double staticAlpha) {
		this.staticAlpha = staticAlpha;
		return this;
	}

	public Vec3 getStaticColor() {
		return staticColor;
	}

	public Geoset setStaticColor(Vec3 staticColor) {
		this.staticColor.set(staticColor);
		return this;
	}

	public boolean isDropShadow() {
		return dropShadow;
	}

	public Geoset setDropShadow(boolean dropShadow) {
		this.dropShadow = dropShadow;
		return this;
	}

	public Vec3 getRenderColor(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, MdlUtils.TOKEN_COLOR, staticColor);
	}
}
