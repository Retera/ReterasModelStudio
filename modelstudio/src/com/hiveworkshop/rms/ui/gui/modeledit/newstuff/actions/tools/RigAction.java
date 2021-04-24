package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class RigAction implements UndoAction {
	private final List<Vec3> selectedVertices;
	private final List<Bone> selectedBones;
	private final Map<Vec3, List<Bone>> vertexToPriorBoneAttachment;
	private final Map<GeosetVertex, Bone[]> vertexToOldSkinBoneReferences;
	private final Map<GeosetVertex, short[]> vertexToOldSkinBoneWeightReferences;

	public RigAction(Collection<? extends Vec3> selectedVertices,
	                 Collection<? extends Bone> selectedBones) {
		this.selectedVertices = new ArrayList<>(selectedVertices);
		this.selectedBones = new ArrayList<>(selectedBones);
		this.vertexToPriorBoneAttachment = new HashMap<>();
		this.vertexToOldSkinBoneReferences = new HashMap<>();
		this.vertexToOldSkinBoneWeightReferences = new HashMap<>();
		loadUndoData();
	}

	public RigAction(RigAction... rigActions) {
		this.selectedVertices = new ArrayList<>();
		this.selectedBones = new ArrayList<>();
		this.vertexToPriorBoneAttachment = new HashMap<>();
		this.vertexToOldSkinBoneReferences = new HashMap<>();
		this.vertexToOldSkinBoneWeightReferences = new HashMap<>();
		for (RigAction other : rigActions) {
			selectedVertices.addAll(other.selectedVertices);
			selectedBones.addAll(other.selectedBones);
		}
		loadUndoData();
	}

	private void loadUndoData() {
		for (Vec3 vertex : selectedVertices) {
			if (vertex instanceof GeosetVertex) {
				GeosetVertex geosetVertex = (GeosetVertex) vertex;
				if (geosetVertex.getSkinBoneBones() != null) {
					vertexToOldSkinBoneReferences.put(geosetVertex, geosetVertex.getSkinBoneBones().clone());
					vertexToOldSkinBoneWeightReferences.put(geosetVertex, geosetVertex.getSkinBoneWeights().clone());
				} else {
					List<Bone> boneAttachments = geosetVertex.getBoneAttachments();
					vertexToPriorBoneAttachment.put(vertex, new ArrayList<>(boneAttachments));
				}
			}
		}
	}

	@Override
	public void undo() {
		for (Vec3 vertex : selectedVertices) {
			if (vertex instanceof GeosetVertex) {
				List<Bone> list = vertexToPriorBoneAttachment.get(vertex);
				if (list != null) {
					((GeosetVertex) vertex).rigBones(new ArrayList<>(list));
				} else {
					Bone[] bones = vertexToOldSkinBoneReferences.get(vertex);
					short[] boneWeights = vertexToOldSkinBoneWeightReferences.get(vertex);
					((GeosetVertex) vertex).setSkinBones(bones, boneWeights);
				}
			}
		}
	}

	@Override
	public void redo() {
		for (Vec3 vertex : selectedVertices) {
			if (vertex instanceof GeosetVertex) {
				((GeosetVertex) vertex).rigBones(new ArrayList<>(selectedBones));
			}
		}
	}

	@Override
	public String actionName() {
		return "rig";
	}

}
