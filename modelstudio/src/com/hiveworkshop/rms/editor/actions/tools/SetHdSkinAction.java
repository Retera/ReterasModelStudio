package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.*;

public final class SetHdSkinAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final List<GeosetVertex> selectedVertices = new ArrayList<>();
	private final Map<GeosetVertex, Bone[]> vertexToOldSkinBoneReferences = new HashMap<>();
	private final Map<GeosetVertex, short[]> vertexToOldSkinBoneWeightReferences = new HashMap<>();
	private final Bone[] bones;
	private final short[] skinWeights;


	public SetHdSkinAction(Collection<GeosetVertex> vertices, Bone[] bones, short[] skinWeights) {
		this(vertices, bones, skinWeights, null);
	}

	public SetHdSkinAction(Collection<GeosetVertex> vertices, Bone[] bones, short[] skinWeights, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		selectedVertices.addAll(vertices);
		this.bones = bones;
		this.skinWeights = skinWeights;

		for (GeosetVertex gv : selectedVertices) {
			if (gv.getSkinBones() != null) {
				vertexToOldSkinBoneReferences.put(gv, gv.getSkinBoneBones());
				vertexToOldSkinBoneWeightReferences.put(gv, gv.getSkinBoneWeights());
			} else {
				throw new IllegalStateException("Attempted to manipulate HD Skinning while SD mesh is selected!");
			}
		}
	}

	@Override
	public UndoAction undo() {
		for (GeosetVertex vertex : selectedVertices) {
			vertex.setSkinBones(vertexToOldSkinBoneReferences.get(vertex), vertexToOldSkinBoneWeightReferences.get(vertex));
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (GeosetVertex vertex : selectedVertices) {
			if (vertex.getSkinBones() != null) {
				vertex.setSkinBones(bones, skinWeights);
			}
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Edit Skinning";
	}

}
