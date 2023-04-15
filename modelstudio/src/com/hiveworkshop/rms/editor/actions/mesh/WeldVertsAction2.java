package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.HashableVector;
import com.hiveworkshop.rms.util.Pair;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class WeldVertsAction2 implements UndoAction {
	private final Geoset geoset;
	private final ModelStructureChangeListener changeListener;
	private final Map<GeosetVertex, GeosetVertex> oldToNew = new HashMap<>();
	private final Map<Triangle, GeosetVertex[]> triOrgVertMap = new HashMap<>();
	private final Set<Triangle> trianglesToRemove = new HashSet<>();
	private final float weight;

	public WeldVertsAction2(Geoset geoset, Collection<GeosetVertex> selection, float weight, ModelStructureChangeListener changeListener) {
		this.geoset = geoset;
		this.changeListener = changeListener;
		this.weight = weight;

		Map<HashableVector, List<GeosetVertex>> locationToGVs = new HashMap<>();
		for (GeosetVertex vertex : selection) {
			locationToGVs.computeIfAbsent(new HashableVector(vertex), gvSet -> new ArrayList<>()).add(vertex);
		}

		for (HashableVector location : locationToGVs.keySet()){
			List<GeosetVertex> vertices = locationToGVs.get(location);
			GeosetVertex vertexToKeep = vertices.get(0);
			for(GeosetVertex vertex : vertices){
				if(vertex != vertexToKeep){
					oldToNew.put(vertex, vertexToKeep);
				}
			}
		}

		Set<Triangle> affectedTris = new HashSet<>();
		for (GeosetVertex vertexToRemove : oldToNew.keySet()) {
			affectedTris.addAll(vertexToRemove.getTriangles());
		}

		for (Triangle triangle : affectedTris){
			GeosetVertex temp0 = oldToNew.getOrDefault(triangle.get(0), triangle.get(0));
			GeosetVertex temp1 = oldToNew.getOrDefault(triangle.get(1), triangle.get(1));
			GeosetVertex temp2 = oldToNew.getOrDefault(triangle.get(2), triangle.get(2));

			if(temp0 == temp1 || temp0 == temp2 || temp1 == temp2){
				trianglesToRemove.add(triangle);
			} else {
				triOrgVertMap.put(triangle, new GeosetVertex[]{triangle.get(0), triangle.get(1), triangle.get(2)});
			}
		}

	}

	private void avrageStuff(){
		Map<GeosetVertex, Set<GeosetVertex>> keepToRemove =  new HashMap<>();
		for (GeosetVertex vertexToRemove : oldToNew.keySet()) {
			keepToRemove.computeIfAbsent(oldToNew.get(vertexToRemove), k -> new HashSet<>()).add(vertexToRemove);
		}
		for (GeosetVertex vertexToKeep : keepToRemove.keySet()) {
			Set<GeosetVertex> vertices = keepToRemove.get(vertexToKeep);
			Vec3 normal = vertexToKeep.getNormal();
			Set<Bone> bones = new HashSet<>(vertexToKeep.getBones());
			Map<Bone, int[]> skinBonesMap = new HashMap<>();
			if(vertexToKeep.getSkinBones() != null){
				for (SkinBone skinBone : vertexToKeep.getSkinBones()){
					if (skinBone != null && skinBone.getBone() != null && skinBone.getWeight() != 0){
						int[] pair = skinBonesMap.computeIfAbsent(skinBone.getBone(), k -> new int[]{0,0});
						pair[0] += skinBone.getWeight();
						pair[1] ++;
					}
				}
			}
			for(GeosetVertex vertex : vertices){
				normal.add(vertex.getNormal());
				bones.addAll(vertex.getBones());
				if(vertex.getSkinBones() != null){
					for (SkinBone skinBone : vertex.getSkinBones()){
						if (skinBone != null && skinBone.getBone() != null && skinBone.getWeight() != 0){
							int[] pair = skinBonesMap.computeIfAbsent(skinBone.getBone(), k -> new int[]{0,0});
							pair[0] += skinBone.getWeight();
							pair[1] ++;
						}
					}
				}
			}
			normal.normalize();
			vertexToKeep.clearBoneAttachments();
			vertexToKeep.addBoneAttachments(bones);
			if(!skinBonesMap.isEmpty()){
				List<Pair<Bone, Short>> skinBoneList = new ArrayList<>();

				for(Bone bone : skinBonesMap.keySet()){
					int[] ints = skinBonesMap.get(bone);
					ints[0] /= ints[1];
//					skinBoneList.add(new SkinBone((short) ints[0], bone));
					skinBoneList.add(new Pair<>(bone, (short) ints[0]));
				}
				skinBoneList.sort(Comparator.comparingInt(Pair::getSecond));
				int startIndex = skinBoneList.size() - 1;
				for (int i = 0; i < 4; i++){
					Pair<Bone, Short> boneShortPair = skinBoneList.get(startIndex - i);
					vertexToKeep.setSkinBone(boneShortPair.getFirst(), boneShortPair.getSecond(), i);
				}
			}
		}
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
	public WeldVertsAction2 redo() {
		geoset.remove(oldToNew.keySet());
		geoset.removeTriangles(trianglesToRemove);

		replaceTriVerts();

		sanitize();

		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public WeldVertsAction2 undo() {
		putBackTriVerts();
		geoset.addTriangles(trianglesToRemove);
		geoset.addVerticies(oldToNew.keySet());

		sanitize();

		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Weld Vertices";
	}
}
