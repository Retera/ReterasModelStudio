package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.GeosetVertexBoneLink;
import com.hiveworkshop.wc3.mdl.Vertex;

public class RigAction implements UndoAction {
	private final List<Vertex> selectedVertices;
	private final List<Bone> selectedBones;
	private final Map<Vertex, List<GeosetVertexBoneLink>> vertexToPriorBoneAttachment;

	public RigAction(final Collection<? extends Vertex> selectedVertices,
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
		for (final Vertex vertex : selectedVertices) {
			if (vertex instanceof GeosetVertex) {
				final List<GeosetVertexBoneLink> boneAttachments = ((GeosetVertex) vertex).getLinks();
				vertexToPriorBoneAttachment.put(vertex, new ArrayList<>(boneAttachments));
			}
		}
	}

	@Override
	public void undo() {
		for (final Vertex vertex : selectedVertices) {
			final List<GeosetVertexBoneLink> list = vertexToPriorBoneAttachment.get(vertex);
			if (list != null) {
				if (vertex instanceof GeosetVertex) {
					((GeosetVertex) vertex).setBoneAttachmentsRaw(new ArrayList<>(list));
				}
			}
		}
	}

	@Override
	public void redo() {
		for (final Vertex vertex : selectedVertices) {
			if (vertex instanceof GeosetVertex) {
				((GeosetVertex) vertex).setBoneAttachments(selectedBones);
			}
		}
	}

	@Override
	public String actionName() {
		return "rig";
	}

}
