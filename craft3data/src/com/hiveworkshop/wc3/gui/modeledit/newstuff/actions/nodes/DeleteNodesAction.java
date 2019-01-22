package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class DeleteNodesAction implements UndoAction {
	private final List<IdObject> objects;
	private final ModelStructureChangeListener changeListener;
	private final ModelView model;
	private final Collection<Vertex> selection;
	private final VertexSelectionHelper vertexSelectionHelper;
	private List<GeosetVertexNodeDeleteOperation> meshLinkDeleteOps;
	private final Set<IdObject> quickHashSetRemovedObjects;

	public DeleteNodesAction(final Collection<? extends Vertex> selection, final List<IdObject> objects,
			final ModelStructureChangeListener changeListener, final ModelView model,
			final VertexSelectionHelper vertexSelectionHelper) {
		this.selection = new ArrayList<>(selection);
		this.objects = objects;
		this.changeListener = changeListener;
		this.model = model;
		this.vertexSelectionHelper = vertexSelectionHelper;
		this.quickHashSetRemovedObjects = new HashSet<>();
		quickHashSetRemovedObjects.addAll(objects);
	}

	@Override
	public void undo() {
		for (final IdObject object : objects) {
			model.getModel().add(object);
		}
		for (int i = meshLinkDeleteOps.size() - 1; i >= 0; i--) {
			meshLinkDeleteOps.get(i).undo();
		}
		changeListener.nodesAdded(objects);
		vertexSelectionHelper.selectVertices(selection);
	}

	@Override
	public void redo() {
		for (final IdObject object : objects) {
			model.getModel().remove(object);
		}
		if (meshLinkDeleteOps == null) {
			meshLinkDeleteOps = new ArrayList<>();
			for (final Geoset geoset : model.getModel().getGeosets()) {
				for (final GeosetVertex geosetVertex : geoset.getVertices()) {
					for (int boneIndex = 0; boneIndex < geosetVertex.getBones().size(); boneIndex++) {
						final Bone bone = geosetVertex.getBones().get(boneIndex);
						if (quickHashSetRemovedObjects.contains(bone)) {
							final GeosetVertexNodeDeleteOperation deleteOp = new GeosetVertexNodeDeleteOperation(
									geosetVertex, bone, boneIndex);
							meshLinkDeleteOps.add(deleteOp);
							deleteOp.redo();
							boneIndex--;
						}
					}
				}
			}
		} else {
			for (final GeosetVertexNodeDeleteOperation op : meshLinkDeleteOps) {
				op.redo();
			}
		}
		changeListener.nodesRemoved(objects);
		vertexSelectionHelper.selectVertices(new ArrayList<Vertex>());
	}

	@Override
	public String actionName() {
		return "delete nodes";
	}

	private static final class GeosetVertexNodeDeleteOperation {
		private final GeosetVertex vertex;
		private final Bone object;
		private final int bonesListIndex;

		public GeosetVertexNodeDeleteOperation(final GeosetVertex vertex, final Bone object, final int bonesListIndex) {
			this.vertex = vertex;
			this.object = object;
			this.bonesListIndex = bonesListIndex;
		}

		public void undo() {
			vertex.getBoneAttachments().add(bonesListIndex, object);
		}

		public void redo() {
			vertex.getBoneAttachments().remove(object);
		}
	}
}
