package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.*;

/**
 * Something to undo when you deleted something important.
 *
 * Eric Theller 6/11/2012
 */
public class ReplaceBonesAction implements UndoAction {
	private final Set<GeosetVertex> affectedVerts;
	private final ModelStructureChangeListener changeListener;
	private final Map<IdObject, IdObject> boneReplacents;
	Map<Integer, Bone[]> skinboneBoneMap = new HashMap<>();
	Map<Integer, short[]> skinboneWeightMap = new HashMap<>();
	Map<Integer, Set<GeosetVertex>> vertexMap = new HashMap<>();
	Map<Matrix, Set<GeosetVertex>> matrixVertexMap = new HashMap<>();


	public ReplaceBonesAction(Collection<GeosetVertex> selection, Map<IdObject, IdObject> boneReplacents, ModelStructureChangeListener changeListener) {
		this.affectedVerts = new HashSet<>(selection);
		this.changeListener = changeListener;
		this.boneReplacents = boneReplacents;

		for (GeosetVertex vertex : selection){
			SkinBone[] skinBones = vertex.getSkinBones();
			if (skinBones != null){
				Bone[] skinBoneBones = vertex.getSkinBoneBones();
				short[] skinBoneWeights = vertex.getSkinBoneWeights();
				int hashCode = Arrays.hashCode(skinBones);
				skinboneBoneMap.putIfAbsent(hashCode, skinBoneBones);
				skinboneWeightMap.putIfAbsent(hashCode, skinBoneWeights);
			} else {
				Set<GeosetVertex> vertexSet = matrixVertexMap.get(vertex.getMatrix());
				if(vertexSet == null){
					vertexSet = new HashSet<>();
					matrixVertexMap.put(new Matrix(vertex.getBones()), vertexSet);
				}
				vertexSet.add(vertex);
			}
		}

	}

	@Override
	public UndoAction redo() {
		for (GeosetVertex gv : affectedVerts) {
			gv.replaceBones(boneReplacents, true);
		}
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		for (Integer hashCode : skinboneBoneMap.keySet()){
			Bone[] bones = skinboneBoneMap.get(hashCode);
			short[] weights = skinboneWeightMap.get(hashCode);
			for(GeosetVertex vertex : vertexMap.get(hashCode)){
				vertex.setSkinBones(bones, weights);
			}
		}
		for (Matrix matrix : matrixVertexMap.keySet()){
			for(GeosetVertex vertex : matrixVertexMap.get(matrix)){
				vertex.setBones(matrix.getBones());
			}
		}
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Replace Vertices Bone Bindings";
	}
}
