package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Matrix;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public final class SetMatrixAction2 implements UndoAction {
	List<GeosetVertex> selectedVertices = new ArrayList<>();
	private Map<GeosetVertex, List<Bone>> vertexToOldBoneReferences;
	private List<Bone> newBoneReferences = new ArrayList<>();
	private Map<GeosetVertex, Bone[]> vertexToOldSkinBoneReferences = new HashMap<>();
	private Map<GeosetVertex, short[]> vertexToOldSkinBoneWeightReferences = new HashMap<>();
	private Matrix mx;

	public SetMatrixAction2(Collection<? extends Vec3> vertices, Collection<Bone> bones) {
		vertices.forEach(vert -> selectedVertices.add((GeosetVertex) vert));
//		mx = new Matrix();
//		mx.setBones(new ArrayList<>());
//		for (Bone bone : bones) {
//			mx.add(bone);
//		}
		this.newBoneReferences.addAll(bones);

		for (GeosetVertex gv : selectedVertices) {
			if (gv.getSkinBones() != null) {
				vertexToOldSkinBoneReferences.put(gv, gv.getSkinBoneBones());
				vertexToOldSkinBoneWeightReferences.put(gv, gv.getSkinBoneWeights());
			} else {
				vertexToOldBoneReferences.put(gv, new ArrayList<>(gv.getBoneAttachments()));
			}
		}
	}

	@Override
	public UndoAction undo() {
		for (GeosetVertex gv : selectedVertices) {
			if (gv.getSkinBones() != null) {
				gv.setSkinBones(vertexToOldSkinBoneReferences.get(gv), vertexToOldSkinBoneWeightReferences.get(gv));
			} else {
				gv.setBones(vertexToOldBoneReferences.get(gv));
			}
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (GeosetVertex gv : selectedVertices) {
			gv.rigBones(newBoneReferences);
//			gv.rigBones(mx.getBones());
		}
		return this;
	}

	@Override
	public String actionName() {
		return "re-assign matrix";
	}

}
