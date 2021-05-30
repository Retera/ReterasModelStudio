package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.*;

public final class SetMatrixAction implements UndoAction {
	private final Map<GeosetVertex, List<Bone>> vertexToOldBoneReferences;
	private final Collection<Bone> newBoneReferences;
	private final Map<GeosetVertex, Bone[]> vertexToOldSkinBoneReferences;
	private final Map<GeosetVertex, short[]> vertexToOldSkinBoneWeightReferences;
	private final Map<GeosetVertex, Bone[]> vertexToNewSkinBoneReferences;
	private final Map<GeosetVertex, short[]> vertexToNewSkinBoneWeightReferences;

	public SetMatrixAction(final Map<GeosetVertex, List<Bone>> vertexToOldBoneReferences,
			final Map<GeosetVertex, Bone[]> vertexToOldSkinBoneReferences,
			final Map<GeosetVertex, short[]> vertexToOldSkinBoneWeightReferences,
			final Collection<Bone> newBoneReferences) {
		this.vertexToOldBoneReferences = vertexToOldBoneReferences;
		this.vertexToOldSkinBoneReferences = vertexToOldSkinBoneReferences;
		this.vertexToOldSkinBoneWeightReferences = vertexToOldSkinBoneWeightReferences;
		this.newBoneReferences = newBoneReferences;
		vertexToNewSkinBoneReferences = new HashMap<>();
		vertexToNewSkinBoneWeightReferences = new HashMap<>();
		for (final Map.Entry<GeosetVertex, Bone[]> entry : vertexToOldSkinBoneReferences.entrySet()) {
			vertexToNewSkinBoneReferences.put(entry.getKey(), entry.getKey().getSkinBoneBones().clone());
		}
		for (final Map.Entry<GeosetVertex, short[]> entry : vertexToOldSkinBoneWeightReferences.entrySet()) {
			vertexToNewSkinBoneWeightReferences.put(entry.getKey(), entry.getKey().getSkinBoneWeights().clone());
		}
	}

	@Override
	public void undo() {
		for (final Map.Entry<GeosetVertex, List<Bone>> entry : vertexToOldBoneReferences.entrySet()) {
			entry.getKey().setBones(new ArrayList<>(entry.getValue()));
		}
		for (final Map.Entry<GeosetVertex, Bone[]> entry : vertexToOldSkinBoneReferences.entrySet()) {
			entry.getKey().setSkinBones(entry.getValue());
		}
		for (final Map.Entry<GeosetVertex, short[]> entry : vertexToOldSkinBoneWeightReferences.entrySet()) {
			entry.getKey().setSkinBoneWeights(entry.getValue());
		}
	}

	@Override
	public void redo() {
		for (final Map.Entry<GeosetVertex, List<Bone>> entry : vertexToOldBoneReferences.entrySet()) {
			entry.getKey().setBones(new ArrayList<>(newBoneReferences));
		}
		for (final Map.Entry<GeosetVertex, Bone[]> entry : vertexToNewSkinBoneReferences.entrySet()) {
			entry.getKey().setSkinBones(entry.getValue());
		}
		for (final Map.Entry<GeosetVertex, short[]> entry : vertexToNewSkinBoneWeightReferences.entrySet()) {
			entry.getKey().setSkinBoneWeights(entry.getValue());
		}
	}

	@Override
	public String actionName() {
		return "re-assign matrix";
	}

}
