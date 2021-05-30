package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public final class SetHdSkinAction implements UndoAction {
	List<GeosetVertex> selectedVertices = new ArrayList<>();
	private Map<GeosetVertex, Bone[]> vertexToOldSkinBoneReferences = new HashMap<>();
	private Map<GeosetVertex, short[]> vertexToOldSkinBoneWeightReferences = new HashMap<>();
	private Bone[] bones;
	private short[] skinWeights;


	public SetHdSkinAction(Collection<? extends Vec3> vertices, Bone[] bones, short[] skinWeights) {
		vertices.forEach(vert -> selectedVertices.add((GeosetVertex) vert));
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
		return this;
	}

	@Override
	public UndoAction redo() {
		for (GeosetVertex vertex : selectedVertices) {
			if (vertex.getSkinBones() != null) {
				vertex.setSkinBones(bones, skinWeights);
			}
		}
		return this;
	}

	@Override
	public String actionName() {
		return "re-assign matrix";
	}

}
