package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.*;

public class DeleteNodesAction implements UndoAction {
	private final Set<IdObject> selectedObjects;
	private final ModelStructureChangeListener changeListener;
	private final EditableModel model;
	private final Set<Camera> selectedCameras;

	private CompoundAction meshLinkDelete;

	private final Map<IdObject, IdObject> topParentMap = new HashMap<>();
	private final Map<IdObject, Set<IdObject>> childMap = new HashMap<>();

	private final boolean relink = true;
	private final int size;

	public DeleteNodesAction(Collection<? extends IdObject> selectedObjects,
	                         Collection<Camera> selectedCameras,
	                         ModelStructureChangeListener changeListener,
	                         EditableModel model) {
		this.selectedObjects = new HashSet<>(selectedObjects);
		this.selectedCameras = new HashSet<>(selectedCameras);
		this.changeListener = changeListener;

		this.model = model;

		for (IdObject idObject : selectedObjects) {
			topParentMap.put(idObject, topParent(idObject));
			childMap.put(idObject, new HashSet<>(idObject.getChildrenNodes()));
		}

		size = selectedObjects.size() + selectedCameras.size();
	}
	public DeleteNodesAction(Collection<? extends IdObject> selectedObjects,
	                         ModelStructureChangeListener changeListener,
	                         EditableModel model) {
		this(selectedObjects, Collections.emptySet(), changeListener, model);
	}

	public DeleteNodesAction(IdObject selectedObject,
	                         ModelStructureChangeListener changeListener,
	                         EditableModel model) {
		this(Collections.singleton(selectedObject), Collections.emptySet(), changeListener, model);
	}

	public DeleteNodesAction(Camera selectedCamera,
	                         ModelStructureChangeListener changeListener,
	                         EditableModel model) {
		this(Collections.emptySet(), Collections.singleton(selectedCamera), changeListener, model);
	}

	private IdObject topParent(IdObject idObject) {
		IdObject parent = idObject.getParent();
		if (selectedObjects.contains(parent)) {
			return topParent(parent);
		}
		return parent;
	}

	private void removeFromParent() {
		for(IdObject removedParent : childMap.keySet()){
			for (IdObject affectedChild : childMap.get(removedParent)){
				if (relink){
					affectedChild.setParent(topParent(removedParent));
				} else {
					affectedChild.setParent(null);
				}
			}
		}
	}
	private void addBackParent(){
		for(IdObject removedParent : childMap.keySet()){
			for (IdObject affectedChild : childMap.get(removedParent)){
				affectedChild.setParent(removedParent);
			}
		}
	}

	@Override
	public DeleteNodesAction undo() {
		for (IdObject object : selectedObjects) {
			model.add(object);
		}
		addBackParent();

		for (Camera camera : selectedCameras) {
			model.add(camera);
		}
		meshLinkDelete.undo();

		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public DeleteNodesAction redo() {
		for (IdObject object : selectedObjects) {
			model.remove(object);
		}
		removeFromParent();
		for (Camera camera : selectedCameras) {
			model.remove(camera);
		}

		getMeshLinkDeleteAction().redo();

		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	private UndoAction getMeshLinkDeleteAction() {
		if(meshLinkDelete == null) {
			Set<GeosetVertex> affectedVerts = new HashSet<>();
			Set<Bone> vertBones = new HashSet<>();
			Map<Bone, Set<GeosetVertex>> boneVertMap = new HashMap<>();
			for (Geoset geoset : model.getGeosets()) {
				Map<Bone, List<GeosetVertex>> boneMap = geoset.getBoneMap();
				for (Bone bone : boneMap.keySet()) {
					if (selectedObjects.contains(bone)) {
						boneVertMap.computeIfAbsent(bone, k -> new HashSet<>());
						boneVertMap.get(bone).addAll(boneMap.get(bone));
						affectedVerts.addAll(boneMap.get(bone));
						vertBones.add(bone);
					}
				}
			}
			List<UndoAction> nodeDeleteOperations = new ArrayList<>();
			for (GeosetVertex vertex : affectedVerts) {
				nodeDeleteOperations.add(new GeosetVertexNodeDeleteOperation1(vertex, vertBones, relink, topParentMap));
			}
			meshLinkDelete = new CompoundAction("remove geoset attachments", nodeDeleteOperations);
		}
		return meshLinkDelete;
	}

	@Override
	public String actionName() {
		return "Delete " + size + " Nodes";
	}
}
