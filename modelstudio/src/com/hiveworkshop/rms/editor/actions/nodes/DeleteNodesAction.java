package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.BiMap;

import java.util.*;

public class DeleteNodesAction implements UndoAction {
	private final Set<IdObject> selectedObjects;
	private final ModelStructureChangeListener changeListener;
	private final ModelView model;
	private final Set<IdObject> quickHashSetRemovedObjects;
	private final Set<Camera> selectedCameras;

	private CompoundAction meshLinkDelete;

	private BiMap<IdObject, IdObject> parentMap = new BiMap<>();
	private Map<IdObject, IdObject> topParentMap = new HashMap<>();
	private Map<IdObject, Set<IdObject>> childMap = new HashMap<>();

	private boolean relink = true;

	public DeleteNodesAction(Collection<IdObject> selectedObjects,
	                         Collection<Camera> selectedCameras,
	                         ModelStructureChangeListener changeListener,
	                         ModelView modelView) {
		this.selectedObjects = new HashSet<>(selectedObjects);
		this.selectedCameras = new HashSet<>(selectedCameras);
		this.changeListener = changeListener;
		this.model = modelView;

		this.quickHashSetRemovedObjects = new HashSet<>();
		quickHashSetRemovedObjects.addAll(selectedObjects);

		for (IdObject idObject : selectedObjects){
			parentMap.put(idObject, idObject.getParent());
			topParentMap.put(idObject, topParent(idObject));
			childMap.put(idObject, new HashSet<>(idObject.getChildrenNodes()));
		}
	}

	private IdObject topParent(IdObject idObject){
		IdObject parent = idObject.getParent();
		if(selectedObjects.contains(parent)){
			return topParent(parent);
		}
		return parent;
	}

	private void removeFromParent(){
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
	public UndoAction undo() {
		for (IdObject object : selectedObjects) {
			model.getModel().add(object);
		}
		addBackParent();

		for (Camera camera : selectedCameras) {
			model.getModel().add(camera);
		}
		meshLinkDelete.undo();

		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (IdObject object : selectedObjects) {
			model.getModel().remove(object);
		}
		removeFromParent();
		for (Camera camera : selectedCameras) {
			model.getModel().remove(camera);
		}

		getMeshLinkDeleteAction().redo();

		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	private UndoAction getMeshLinkDeleteAction() {
		if(meshLinkDelete == null){
			Set<GeosetVertex> affectedVerts = new HashSet<>();
			Set<Bone> vertBones = new HashSet<>();
			Map<Bone, Set<GeosetVertex>> boneVertMap = new HashMap<>();
			for (Geoset geoset : model.getModel().getGeosets()) {
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
		return "delete nodes";
	}
}
