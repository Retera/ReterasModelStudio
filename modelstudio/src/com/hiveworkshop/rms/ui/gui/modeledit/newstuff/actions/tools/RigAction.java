package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Vector3;

public class RigAction implements UndoAction {
	private final List<Vector3> selectedVertices;
	private final List<Bone> selectedBones;
	private final Map<Vector3, List<Bone>> vertexToPriorBoneAttachment;

	public RigAction(final Collection<? extends Vector3> selectedVertices,
			final Collection<? extends Bone> selectedBones) {
		this.selectedVertices = new ArrayList<>(selectedVertices);
		this.selectedBones = new ArrayList<>(selectedBones);
		this.vertexToPriorBoneAttachment = new HashMap<>();
		loadUndoData();
	}

	public RigAction(final RigAction... rigActions) {
		this.selectedVertices = new ArrayList<>();
		this.selectedBones = new ArrayList<>();
		this.vertexToPriorBoneAttachment = new HashMap<>();
		for (final RigAction other : rigActions) {
			selectedVertices.addAll(other.selectedVertices);
			selectedBones.addAll(other.selectedBones);
		}
		loadUndoData();
	}

	private void loadUndoData() {
		for (final Vector3 vertex : selectedVertices) {
			if (vertex instanceof GeosetVertex) {
				final List<Bone> boneAttachments = ((GeosetVertex) vertex).getBoneAttachments();
				vertexToPriorBoneAttachment.put(vertex, new ArrayList<>(boneAttachments));
			}
		}
	}

	@Override
	public void undo() {
		for (final Vector3 vertex : selectedVertices) {
			final List<Bone> list = vertexToPriorBoneAttachment.get(vertex);
			if (list != null) {
				if (vertex instanceof GeosetVertex) {
					((GeosetVertex) vertex).setBones(new ArrayList<>(list));
				}
			}
		}
	}

	@Override
	public void redo() {
		for (final Vector3 vertex : selectedVertices) {
			if (vertex instanceof GeosetVertex) {
				((GeosetVertex) vertex).setBones(new ArrayList<>(selectedBones));
			}
		}
	}

	@Override
	public String actionName() {
		return "rig";
	}

}
