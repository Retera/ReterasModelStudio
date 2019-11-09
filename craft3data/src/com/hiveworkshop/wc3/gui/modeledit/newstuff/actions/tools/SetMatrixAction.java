package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.etheller.collections.HashMap;
import com.etheller.collections.Map;
import com.etheller.collections.MapView;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.GeosetVertex;

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
		for (final MapView.Entry<GeosetVertex, Bone[]> entry : vertexToOldSkinBoneReferences) {
			vertexToNewSkinBoneReferences.put(entry.getKey(), entry.getKey().getSkinBones().clone());
		}
		for (final MapView.Entry<GeosetVertex, short[]> entry : vertexToOldSkinBoneWeightReferences) {
			vertexToNewSkinBoneWeightReferences.put(entry.getKey(), entry.getKey().getSkinBoneWeights().clone());
		}
	}

	@Override
	public void undo() {
		for (final MapView.Entry<GeosetVertex, List<Bone>> entry : vertexToOldBoneReferences) {
			entry.getKey().setBones(new ArrayList<>(entry.getValue()));
		}
		for (final MapView.Entry<GeosetVertex, Bone[]> entry : vertexToOldSkinBoneReferences) {
			entry.getKey().setSkinBones(entry.getValue());
		}
		for (final MapView.Entry<GeosetVertex, short[]> entry : vertexToOldSkinBoneWeightReferences) {
			entry.getKey().setSkinBoneWeights(entry.getValue());
		}
	}

	@Override
	public void redo() {
		for (final MapView.Entry<GeosetVertex, List<Bone>> entry : vertexToOldBoneReferences) {
			entry.getKey().setBones(new ArrayList<>(newBoneReferences));
		}
		for (final MapView.Entry<GeosetVertex, Bone[]> entry : vertexToNewSkinBoneReferences) {
			entry.getKey().setSkinBones(entry.getValue());
		}
		for (final MapView.Entry<GeosetVertex, short[]> entry : vertexToNewSkinBoneWeightReferences) {
			entry.getKey().setSkinBoneWeights(entry.getValue());
		}
	}

	@Override
	public String actionName() {
		return "re-assign matrix";
	}

}
