package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.etheller.collections.Map;
import com.etheller.collections.MapView;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.GeosetVertex;

public final class SetMatrixAction implements UndoAction {
	private final Map<GeosetVertex, List<Bone>> vertexToOldBoneReferences;
	private final Collection<Bone> newBoneReferences;

	public SetMatrixAction(final Map<GeosetVertex, List<Bone>> vertexToOldBoneReferences,
			final Collection<Bone> newBoneReferences) {
		this.vertexToOldBoneReferences = vertexToOldBoneReferences;
		this.newBoneReferences = newBoneReferences;
	}

	@Override
	public void undo() {
		for (final MapView.Entry<GeosetVertex, List<Bone>> entry : vertexToOldBoneReferences) {
			entry.getKey().setBones(new ArrayList<>(entry.getValue()));
		}
	}

	@Override
	public void redo() {
		for (final MapView.Entry<GeosetVertex, List<Bone>> entry : vertexToOldBoneReferences) {
			entry.getKey().setBones(new ArrayList<>(newBoneReferences));
		}
	}

	@Override
	public String actionName() {
		return "re-assign matrix";
	}

}
