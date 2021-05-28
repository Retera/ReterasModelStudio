package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.*;

public final class SetMatrixAction implements UndoAction {
	private Map<GeosetVertex, List<Bone>> vertexToOldBoneReferences;
	private Collection<Bone> newBoneReferences;
	private Map<GeosetVertex, Bone[]> vertexToOldSkinBoneReferences;
	private Map<GeosetVertex, short[]> vertexToOldSkinBoneWeightReferences;
	private Map<GeosetVertex, Bone[]> vertexToNewSkinBoneReferences;
	private Map<GeosetVertex, short[]> vertexToNewSkinBoneWeightReferences;

	public SetMatrixAction(Map<GeosetVertex, List<Bone>> vertexToOldBoneReferences,
	                       Map<GeosetVertex, Bone[]> vertexToOldSkinBoneReferences,
	                       Map<GeosetVertex, short[]> vertexToOldSkinBoneWeightReferences,
	                       Collection<Bone> newBoneReferences) {
		this.vertexToOldBoneReferences = vertexToOldBoneReferences;
		this.vertexToOldSkinBoneReferences = vertexToOldSkinBoneReferences;
		this.vertexToOldSkinBoneWeightReferences = vertexToOldSkinBoneWeightReferences;
		this.newBoneReferences = newBoneReferences;
		vertexToNewSkinBoneReferences = new HashMap<>();
		vertexToNewSkinBoneWeightReferences = new HashMap<>();
		for (Map.Entry<GeosetVertex, Bone[]> entry : vertexToOldSkinBoneReferences.entrySet()) {
			vertexToNewSkinBoneReferences.put(entry.getKey(), entry.getKey().getSkinBoneBones().clone());
		}
		for (Map.Entry<GeosetVertex, short[]> entry : vertexToOldSkinBoneWeightReferences.entrySet()) {
			vertexToNewSkinBoneWeightReferences.put(entry.getKey(), entry.getKey().getSkinBoneWeights().clone());
		}
	}

	@Override
	public UndoAction undo() {
		for (Map.Entry<GeosetVertex, List<Bone>> entry : vertexToOldBoneReferences.entrySet()) {
			entry.getKey().setBones(new ArrayList<>(entry.getValue()));
		}
		for (Map.Entry<GeosetVertex, Bone[]> entry : vertexToOldSkinBoneReferences.entrySet()) {
			entry.getKey().setSkinBones(entry.getValue());
		}
		for (Map.Entry<GeosetVertex, short[]> entry : vertexToOldSkinBoneWeightReferences.entrySet()) {
			entry.getKey().setSkinBoneWeights(entry.getValue());
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Map.Entry<GeosetVertex, List<Bone>> entry : vertexToOldBoneReferences.entrySet()) {
			entry.getKey().setBones(new ArrayList<>(newBoneReferences));
		}
		for (Map.Entry<GeosetVertex, Bone[]> entry : vertexToNewSkinBoneReferences.entrySet()) {
			entry.getKey().setSkinBones(entry.getValue());
		}
		for (Map.Entry<GeosetVertex, short[]> entry : vertexToNewSkinBoneWeightReferences.entrySet()) {
			entry.getKey().setSkinBoneWeights(entry.getValue());
		}
		return this;
	}

	@Override
	public String actionName() {
		return "re-assign matrix";
	}

}
