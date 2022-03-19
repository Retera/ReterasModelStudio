package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Matrix;

import java.util.*;

public final class SetMatrixAction3 implements UndoAction {
	List<GeosetVertex> selectedVertices = new ArrayList<>();
	private final Map<GeosetVertex, List<Bone>> vertexToOldBoneReferences = new HashMap<>();
	private final List<Bone> newBoneReferences = new ArrayList<>();
	private final Map<GeosetVertex, Bone[]> vertexToOldSkinBoneReferences = new HashMap<>();
	private final Map<GeosetVertex, short[]> vertexToOldSkinBoneWeightReferences = new HashMap<>();
	private final Set<Bone> bonesNotAffect;
	private Matrix mx;

	public SetMatrixAction3(Collection<GeosetVertex> vertices, Collection<Bone> bones, Set<Bone> bonesNotAffect) {
		selectedVertices.addAll(vertices);
		this.bonesNotAffect = bonesNotAffect;

		this.newBoneReferences.addAll(bones);

		for (GeosetVertex gv : selectedVertices) {
			if (gv.getSkinBones() != null) {
				vertexToOldSkinBoneReferences.put(gv, gv.getSkinBoneBones());
				vertexToOldSkinBoneWeightReferences.put(gv, gv.getSkinBoneWeights());
			} else {
				vertexToOldBoneReferences.put(gv, new ArrayList<>(gv.getBones()));
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
//			gv.rigBones(newBoneReferences);
			rigBones(gv);
//			gv.rigBones(mx.getBones());
		}
//		for (GeosetVertex gv : selectedVertices) {
//			gv.rigBones(newBoneReferences);
////			gv.rigBones(mx.getBones());
//		}
		return this;
	}

	public void rigBones(GeosetVertex gv) {
		if (gv.getSkinBones() == null) {
			gv.clearBoneAttachments();
			for (Bone bone : newBoneReferences) {
				if (!bonesNotAffect.contains(bone) || bonesNotAffect.contains(bone) && vertexToOldBoneReferences.get(gv).contains(bone)) {
					gv.addBoneAttachment(bone);
				}
			}
		} else {
//            Arrays.fill(skinBones, null);
//            Arrays.fill(skinBoneWeights, (short) 0);


			int weight = 255 / newBoneReferences.size();
			int offset = 255 - (weight * newBoneReferences.size());
			for (int i = 1; i < 4; i++) {
				if (i < newBoneReferences.size()) {
					gv.setSkinBone(newBoneReferences.get(i), (short) weight, i);
				} else {
					gv.setSkinBone((short) 0, i);
				}
			}
			gv.setSkinBone(newBoneReferences.get(0), (short) (weight + offset), 0);
		}
	}

	@Override
	public String actionName() {
		return "re-assign matrix";
	}

}
