package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.InexactHashVector;
import com.hiveworkshop.rms.util.Vec2;

import java.util.*;

public class SimplifyGeometryAction2 implements UndoAction {
	private final Geoset geoset;
	private final ModelStructureChangeListener changeListener;
	private final Map<GeosetVertex, GeosetVertex> oldToNew = new HashMap<>();
	private final Map<Triangle, GeosetVertex[]> triOrgVertMap = new HashMap<>();
	private final boolean ign_norm;
	private final boolean ign_uv;
	private final boolean ign_tang;
	private final boolean ign_skin;
	private final float norm_prec;
	private final float uv_prec;
	private final float tang_prec;
	private Set<Triangle> trianglesToRemove = new HashSet<>();

	public SimplifyGeometryAction2(Geoset geoset, Collection<GeosetVertex> selection,
	                               int precision, boolean ign_skin,
	                               float norm_prec, float uv_prec, float tang_prec,
	                               ModelStructureChangeListener changeListener) {
		this.geoset = geoset;
		this.changeListener = changeListener;
		this.norm_prec = norm_prec;
		this.uv_prec = uv_prec;
		this.tang_prec = tang_prec;
		this.ign_norm = -1f == norm_prec;
		this.ign_uv = -1f == uv_prec;
		this.ign_tang = -1f == tang_prec;
		this.ign_skin = ign_skin;

		Map<InexactHashVector, List<GeosetVertex>> locationToGVs = new HashMap<>();
		for (GeosetVertex vertex : selection) {
			locationToGVs.computeIfAbsent(new InexactHashVector(vertex, precision), k -> new ArrayList<>()).add(vertex);
		}

		for (InexactHashVector location : locationToGVs.keySet()){
			Set<GeosetVertex> verticesToKeep = new HashSet<>();
			for(GeosetVertex vertex : locationToGVs.get(location)){

				GeosetVertex vertexToKeep = getVertexToKeep(verticesToKeep, vertex);

				if(vertexToKeep == null){
					verticesToKeep.add(vertex);
				} else {
					oldToNew.put(vertex, vertexToKeep);
				}
			}
		}

		for (GeosetVertex vertexToRemove : oldToNew.keySet()) {

			for (Triangle triangle : vertexToRemove.getTriangles()) {
				triOrgVertMap.put(triangle, new GeosetVertex[]{triangle.get(0), triangle.get(1), triangle.get(2)});
			}
		}
	}

	public SimplifyGeometryAction2(Geoset geoset, Collection<GeosetVertex> selection, ModelStructureChangeListener changeListener) {
		this(geoset, selection, 100,
				false,
				0.001f, 0.00001f, 0.001f,
				changeListener);
	}

	private GeosetVertex getVertexToKeep(Set<GeosetVertex> verticesToKeep, GeosetVertex vertex) {
		for(GeosetVertex vtk : verticesToKeep){
			if(isSameStuff(vtk, vertex)){
				return vtk;
			}
		}
		return null;
	}


	private Set<Triangle> getTrianglesToRemove() {
		Set<Triangle> trianglesToRemove = new HashSet<>();
		for (Triangle triangle : triOrgVertMap.keySet()) {
			if(!trianglesToRemove.contains(triangle)){
				for (Triangle t : triOrgVertMap.keySet()) {
					if(t != triangle && t.equalRefs(triangle)){
						trianglesToRemove.add(t);
					}
				}
			}
		}
		return trianglesToRemove;
	}

	private void replaceTriVerts() {
		for (Triangle triangle : triOrgVertMap.keySet()) {
			for (int i = 0; i < 3; i++){
				GeosetVertex vertex = oldToNew.get(triangle.get(i));
				if(vertex != null){
					triangle.set(i, vertex);
				}
			}
		}
	}

	private void putBackTriVerts() {
		for (Triangle triangle : triOrgVertMap.keySet()){
			GeosetVertex[] verts = triOrgVertMap.get(triangle);
			triangle.setVerts(verts);
		}
	}

	private void sanitize(){
		geoset.getVertices().forEach(GeosetVertex::clearTriangles);
		for(Triangle triangle : geoset.getTriangles()){
			for (GeosetVertex vertex : triangle.getVerts()) {
				vertex.addTriangle(triangle);
			}
		}
	}

	@Override
	public UndoAction redo() {
		geoset.remove(oldToNew.keySet());

		replaceTriVerts();
		if(trianglesToRemove == null){
			trianglesToRemove = getTrianglesToRemove();
		}
		geoset.removeTriangles(trianglesToRemove);

		sanitize();

		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		geoset.addVerticies(oldToNew.keySet());

		putBackTriVerts();
		geoset.addTriangles(trianglesToRemove);

		sanitize();

		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Simplify Geosets";
	}

	private boolean isSameStuff(GeosetVertex vertexToKeep, GeosetVertex vertex) {
		return isSameNormal(vertexToKeep, vertex)
				&& isSameTangent(vertexToKeep, vertex)
				&& isSameBones(vertexToKeep, vertex)
				&& isSameUvCoord(vertexToKeep, vertex);
	}

	private boolean isSameNormal(GeosetVertex vertexToKeep, GeosetVertex vertex) {
		return ign_norm
				|| vertexToKeep.getNormal() == null
				&& vertex.getNormal() == null
				|| vertexToKeep.getNormal() != null
				&& vertex.getNormal() != null
				&& vertexToKeep.getNormal().distance(vertex.getNormal()) < norm_prec;
	}
	private boolean isSameTangent(GeosetVertex vertexToKeep, GeosetVertex vertex) {
		return ign_tang
				|| vertexToKeep.getTangent() == null
				&& vertex.getTangent() == null
				|| vertexToKeep.getTangent() != null
				&& vertex.getTangent() != null
				&& vertexToKeep.getTangent().distance(vertex.getTangent()) < tang_prec;
	}

	private final HashSet<Bone> tempSet = new HashSet<>();
	private boolean isSameBones(GeosetVertex vertexToKeep, GeosetVertex vertex) {
		if(ign_skin){
			return true;
		}
		SkinBone[] vertexToKeepSkinBones = vertexToKeep.getSkinBones();
		SkinBone[] vertexSkinBones = vertex.getSkinBones();
		if (vertexToKeepSkinBones != null && vertexSkinBones != null) {
			return Arrays.equals(vertexToKeepSkinBones, vertexSkinBones);
		} else {
			tempSet.clear();
			List<Bone> vertexToKeepBones = vertexToKeep.getBones();
			List<Bone> vertexBones = vertex.getBones();
			if (vertexToKeepBones.size() > 0 && vertexToKeepBones.size() == vertexBones.size()) {
				tempSet.addAll(vertexToKeepBones);
				return tempSet.containsAll(vertexBones);
			}
		}
		return false;
	}

	private boolean isSameUvCoord(GeosetVertex vertexToKeep, GeosetVertex vertex) {
		if(ign_uv){
			return true;
		}
		List<Vec2> tverts1 = vertexToKeep.getTverts();
		List<Vec2> tverts2 = vertex.getTverts();
		int size1 = tverts1.size();
		int size2 = tverts2.size();
		if(size1 != size2){
			return false;
		}
		for (int tvI = 0; tvI < size1; tvI++) {
			if (tverts1.get(tvI).distance(tverts2.get(tvI)) > uv_prec) {
				return false;
			}
		}
		return true;
	}
}
