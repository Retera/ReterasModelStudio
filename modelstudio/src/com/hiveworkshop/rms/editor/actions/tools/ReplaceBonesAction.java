package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.MathUtils;

import java.util.*;

/**
 * Something to undo when you deleted something important.
 *
 * Eric Theller 6/11/2012
 */
public class ReplaceBonesAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Map<IdObject, IdObject> boneReplacents;
	private final Map<Integer, Bone[]> skinboneBoneMap = new HashMap<>();
	private final Map<Integer, short[]> skinboneWeightMap = new HashMap<>();
	private final Map<Integer, Bone[]> newSkinboneBoneMap = new HashMap<>();
	private final Map<Integer, short[]> newSkinboneWeightMap = new HashMap<>();
	private final Map<Integer, Set<GeosetVertex>> vertexMap = new HashMap<>();
	private final Map<Matrix, Set<GeosetVertex>> matrixVertexMap = new HashMap<>();


	public ReplaceBonesAction(Collection<GeosetVertex> selection, Map<IdObject, IdObject> boneReplacents, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.boneReplacents = boneReplacents;

		for (GeosetVertex vertex : selection) {
			SkinBone[] skinBones = vertex.getSkinBones();
			if (skinBones != null) {
				collectSkinStuff(vertex, skinBones);
			} else {
				Set<GeosetVertex> vertexSet = matrixVertexMap.get(vertex.getMatrix());
				if (vertexSet == null) {
					vertexSet = new HashSet<>();
					matrixVertexMap.put(new Matrix(vertex.getBones()), vertexSet);
				}
				vertexSet.add(vertex);
			}
		}
	}

	private void collectSkinStuff(GeosetVertex vertex, SkinBone[] skinBones) {
		int hashCode = Arrays.hashCode(skinBones);
		if (!skinboneBoneMap.containsKey(hashCode)) {
			Bone[] skinBoneBones = vertex.getSkinBoneBones();
			short[] skinBoneWeights = vertex.getSkinBoneWeights();
			skinboneBoneMap.put(hashCode, skinBoneBones);
			skinboneWeightMap.put(hashCode, skinBoneWeights);


			Bone[] newSkinBoneBones = new Bone[skinBoneBones.length];
			short[] newSkinBoneWeights = new short[skinBoneWeights.length];
			int totWeight = 0;
			for (int i = 0; i < skinBoneBones.length; i++) {
				newSkinBoneBones[i] = (Bone) boneReplacents.get(skinBoneBones[i]);
				newSkinBoneWeights[i] = newSkinBoneBones[i] == null ? 0 : skinBoneWeights[i];
				totWeight += newSkinBoneWeights[i];
			}
			if (totWeight != 255) {
				int extraWeight = totWeight - 255;
				int notNullBones = (int) Arrays.stream(skinBoneBones).filter(Objects::nonNull).count();

				// Spread the weight disparity
				for (int i = 0; i < notNullBones; i++) {
					if (newSkinBoneBones[i] != null) {
						short tempW = (short) MathUtils.clamp(newSkinBoneWeights[i] - extraWeight/(notNullBones-i), 0, 255);
						extraWeight -= tempW - newSkinBoneWeights[i];
					}
				}

				if (extraWeight != 0) {
					for (int i = 0; i < notNullBones; i++) {
						short tempW = (short) MathUtils.clamp(newSkinBoneWeights[i] - extraWeight, 0, 255);
						extraWeight -= tempW - newSkinBoneWeights[i];
					}
				}
			}

			newSkinboneBoneMap.put(hashCode, newSkinBoneBones);
			newSkinboneWeightMap.put(hashCode, newSkinBoneWeights);
		}

		vertexMap.computeIfAbsent(hashCode, k -> new HashSet<>()).add(vertex);
	}

	@Override
	public UndoAction redo() {
		for (Integer hashCode : newSkinboneBoneMap.keySet()) {
			Bone[] bones = newSkinboneBoneMap.get(hashCode);
			short[] weights = newSkinboneWeightMap.get(hashCode);
			for (GeosetVertex vertex : vertexMap.get(hashCode)) {
				vertex.setSkinBones(bones, weights);
			}
		}

		for (Set<GeosetVertex> verts : matrixVertexMap.values()) {
			for (GeosetVertex vertex : verts) {
				vertex.replaceBones(boneReplacents, true);
			}
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		for (Integer hashCode : skinboneBoneMap.keySet()) {
			Bone[] bones = skinboneBoneMap.get(hashCode);
			short[] weights = skinboneWeightMap.get(hashCode);
			for (GeosetVertex vertex : vertexMap.get(hashCode)) {
				vertex.setSkinBones(bones, weights);
			}
		}
		for (Matrix matrix : matrixVertexMap.keySet()) {
			for (GeosetVertex vertex : matrixVertexMap.get(matrix)) {
				vertex.setBones(matrix.getBones());
			}
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Replace Vertices Bone Bindings";
	}
}
