package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Matrix;

import java.util.*;

public final class RemoveBoneFromGeoAction implements UndoAction {
	List<GeosetVertex> selectedVertices = new ArrayList<>();
	private final Map<GeosetVertex, Integer> vertexToOldBoneReferences = new HashMap<>();
	private final Bone boneToRemove;
	private final Map<GeosetVertex, Bone[]> vertexToOldSkinBoneReferences = new HashMap<>();
	private final Map<GeosetVertex, short[]> vertexToOldSkinBoneWeightReferences = new HashMap<>();
	private Matrix mx;

	public RemoveBoneFromGeoAction(Collection<GeosetVertex> vertices, Bone boneToRemove) {
		selectedVertices.addAll(vertices);
		this.boneToRemove = boneToRemove;

		for (GeosetVertex gv : selectedVertices) {
			if (gv.getSkinBones() != null) {
				vertexToOldSkinBoneReferences.put(gv, gv.getSkinBoneBones());
				vertexToOldSkinBoneWeightReferences.put(gv, gv.getSkinBoneWeights());
			} else {
				vertexToOldBoneReferences.put(gv, gv.getBones().indexOf(boneToRemove));
			}
		}
	}

	@Override
	public UndoAction undo() {
		for (GeosetVertex gv : selectedVertices) {
			if (gv.getSkinBones() != null) {
				gv.setSkinBones(vertexToOldSkinBoneReferences.get(gv), vertexToOldSkinBoneWeightReferences.get(gv));
			} else {
				gv.addBoneAttachment(vertexToOldBoneReferences.get(gv), boneToRemove);
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
			gv.removeBone(boneToRemove);
		} else {
//            Arrays.fill(skinBones, null);
//            Arrays.fill(skinBoneWeights, (short) 0);


			int numBones = 0;
			for (Bone bone : gv.getSkinBoneBones()) {
				numBones += bone == null ? 0 : 1;
			}
			numBones -= 1;


			for (int i = 0; i < 4; i++) {
				if (gv.getSkinBoneBones()[i] == boneToRemove) {
					for (; i < numBones; i++) {
						gv.setSkinBone(gv.getSkinBoneBones()[i + 1], gv.getSkinBoneWeights()[i + 1], i);
					}
					for (; i < 4; i++) {
						gv.setSkinBone(null, (short) 0, i);
					}
				}
			}
			short weight = 255;
			for (int i = 1; i < 4; i++) {
				if (gv.getSkinBoneWeights() != null) {
					weight -= gv.getSkinBoneWeights()[i];
				}
			}

//			int offset = 255 - (weight * numBones);
			gv.setSkinBone(gv.getSkinBoneBones()[0], weight, 0);
		}
	}

	@Override
	public String actionName() {
		return "re-assign matrix";
	}

}
