package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.nodes;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.util.BiMap;

import java.util.*;

public class DeleteNodesAction implements UndoAction {
	private final Set<IdObject> selectedObjects;
	private final ModelStructureChangeListener changeListener;
	private final ModelView model;
	private List<GeosetVertexNodeDeleteOperation> meshLinkDeleteOps;
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

		changeListener.nodesUpdated();
		changeListener.camerasUpdated();
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

		changeListener.nodesUpdated();
		changeListener.camerasUpdated();
		return this;
	}

	private UndoAction getMeshLinkDeleteAction() {
		if(meshLinkDelete == null){
			Set<GeosetVertex> affectedVerts = new HashSet<>();
			Set<Bone> vertBones = new HashSet<>();
			Map<Bone, Set<GeosetVertex>> boneVertMap = new HashMap<>();
			for (Geoset geoset : model.getModel().getGeosets()) {
				Map<Bone, List<GeosetVertex>> boneMap = geoset.getBoneMap();
				for (Bone bone : boneMap.keySet()){
					if(selectedObjects.contains(bone)){
						boneVertMap.computeIfAbsent(bone, k -> new HashSet<>());
						boneVertMap.get(bone).addAll(boneMap.get(bone));
						affectedVerts.addAll(boneMap.get(bone));
						vertBones.add(bone);
					}
				}
			}
			List<GeosetVertexNodeDeleteOperation> nodeDeleteOperations = new ArrayList<>();
			for (GeosetVertex vertex : affectedVerts){
				nodeDeleteOperations.add(new GeosetVertexNodeDeleteOperation(vertex, vertBones, relink, topParentMap));
			}
			meshLinkDelete = new CompoundAction("remove geoset attachments", nodeDeleteOperations);
		}
		return meshLinkDelete;
	}

	@Override
	public String actionName() {
		return "delete nodes";
	}

	private static class GeosetVertexNodeDeleteOperation implements UndoAction {
		private final GeosetVertex vertex;
		private final Map<Integer, Bone> integerBoneMap;
		boolean relink;
		Map<IdObject, IdObject> topParentMap;


		public GeosetVertexNodeDeleteOperation(GeosetVertex vertex, Set<Bone> bones, boolean relink, Map<IdObject, IdObject> topParentMap) {
			this.vertex = vertex;
			this.relink = relink;
			this.topParentMap = topParentMap;

			integerBoneMap = new TreeMap<>();
			// ToDo SkinBones

			List<Bone> vertexBones = vertex.getBones();
			for (Bone bone : vertexBones){
				if(bones.contains(bone)){
					int key = vertexBones.indexOf(bone);
					if(key != -1){
						integerBoneMap.put(key, bone);
					}
				}
			}
		}

		@Override
		public UndoAction undo() {
			for (Integer i : integerBoneMap.keySet()) {
				Bone oldBone = integerBoneMap.get(i);
				if (relink) {
					IdObject replacedParent = topParentMap.get(oldBone);
					if (replacedParent instanceof Bone) {
						vertex.getBones().remove(replacedParent);
					}
				}
				vertex.getBones().add(i, oldBone);
			}
			return this;
		}

		@Override
		public UndoAction redo() {
			if (relink) {
				for (Integer i : integerBoneMap.keySet()) {
					IdObject potParent = topParentMap.get(integerBoneMap.get(i));
					if (potParent instanceof Bone && !(potParent instanceof Helper)) {
						vertex.getBones().remove(integerBoneMap.get(i));
						vertex.getBones().add(i, (Bone) potParent);
					}
				}
			}
			vertex.getBones().removeAll(integerBoneMap.values());
			return this;
		}

		@Override
		public String actionName() {
			return "remove vertex bone binding";
		}
	}
}
