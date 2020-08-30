package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

public class DeleteNodesAction implements UndoAction {
	private final List<IdObject> objects;
	private final ModelStructureChangeListener changeListener;
	private final ModelView model;
	private final Collection<Vec3> selection;
	private final VertexSelectionHelper vertexSelectionHelper;
	private List<GeosetVertexNodeDeleteOperation> meshLinkDeleteOps;
	private final Set<IdObject> quickHashSetRemovedObjects;
	private final List<Camera> deletedCameras;

	public DeleteNodesAction(final Collection<? extends Vec3> selection, final List<IdObject> objects,
			final List<Camera> deletedCameras, final ModelStructureChangeListener changeListener, final ModelView model,
			final VertexSelectionHelper vertexSelectionHelper) {
		this.selection = new ArrayList<>(selection);
		this.objects = objects;
		this.deletedCameras = deletedCameras;
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
		for (final Camera camera : deletedCameras) {
			model.getModel().add(camera);
		}
		for (int i = meshLinkDeleteOps.size() - 1; i >= 0; i--) {
			meshLinkDeleteOps.get(i).undo();
		}
		changeListener.nodesAdded(objects);
		changeListener.camerasAdded(deletedCameras);
		vertexSelectionHelper.selectVertices(selection);
	}

	@Override
	public void redo() {
		for (final IdObject object : objects) {
			model.getModel().remove(object);
		}
		for (final Camera camera : deletedCameras) {
			model.getModel().remove(camera);
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
		changeListener.camerasRemoved(deletedCameras);
		vertexSelectionHelper.selectVertices(new ArrayList<>());
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
