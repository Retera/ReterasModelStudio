package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.*;
import java.util.stream.Collectors;

public class DeleteNodesAction implements UndoAction {
	private final Set<IdObject> selectedObjects;
	private final ModelStructureChangeListener changeListener;
	private final EditableModel model;
	private final ModelView modelView;
	private final Set<Camera> affectedCameras;
	private final Set<CameraNode> selectedCameraNodes;

	private CompoundAction meshLinkDelete;

	private final Map<IdObject, IdObject> baseParentMap = new HashMap<>();
	private final Map<Integer, IdObject> orgIndexToNode = new TreeMap<>();
	private final Map<IdObject, IdObject> topParentMap = new HashMap<>();
	private final Map<IdObject, Set<IdObject>> childMap = new HashMap<>();

	private final boolean relink = true;
	private final int size;

	public DeleteNodesAction(Collection<? extends IdObject> selectedObjects,
	                         Collection<Camera> selectedCameras,
	                         ModelView modelView,
	                         ModelStructureChangeListener changeListener) {
		this.affectedCameras = new HashSet<>(selectedCameras);
		Set<IdObject> affectedIdObject = new HashSet<>(selectedObjects);
		this.changeListener = changeListener;

		this.model = modelView.getModel();
		this.modelView = modelView;

		Set<IdObject> topNodes = new LinkedHashSet<>();

		for (IdObject nodeToRemove : selectedObjects) {
			orgIndexToNode.put(model.getObjectId(nodeToRemove), nodeToRemove);
			if (nodeToRemove.getParent() != null && !selectedObjects.contains(nodeToRemove.getParent())) {
				baseParentMap.put(nodeToRemove, nodeToRemove.getParent());
			}
			if (!selectedObjects.containsAll(nodeToRemove.getChildrenNodes())) {
				topNodes.add(nodeToRemove);
			}
		}

		for (IdObject topNode : topNodes) {
			topParentMap.put(topNode, topParentNotRemoved(topNode, affectedIdObject));
			for (IdObject child : topNode.getChildrenNodes()) {
				if (!selectedObjects.contains(child)) {
					childMap.computeIfAbsent(topNode, k -> new LinkedHashSet<>()).add(child);
//					System.out.println("adding remaining child \"" + child.getName() + "\" of node \"" + nodeToRemove.getName() + "\"");
				}
			}
		}
		this.selectedObjects = selectedObjects.stream().filter(modelView::isSelected).collect(Collectors.toSet());
		this.selectedCameraNodes = new HashSet<>();
		affectedCameras.forEach(c -> {
			if (modelView.isSelected(c.getSourceNode())) selectedCameraNodes.add(c.getSourceNode());
			if (modelView.isSelected(c.getTargetNode())) selectedCameraNodes.add(c.getTargetNode());
		});

		size = selectedObjects.size() + selectedCameras.size();
	}

	public DeleteNodesAction(Collection<? extends IdObject> selectedObjects,
	                         ModelView modelView,
	                         ModelStructureChangeListener changeListener) {
		this(selectedObjects, Collections.emptySet(), modelView, changeListener);
	}

	public DeleteNodesAction(IdObject selectedObject,
	                         ModelView modelView,
	                         ModelStructureChangeListener changeListener) {
		this(Collections.singleton(selectedObject), Collections.emptySet(), modelView, changeListener);
	}

	public DeleteNodesAction(Camera selectedCamera,
	                         ModelView modelView,
	                         ModelStructureChangeListener changeListener) {
		this(Collections.emptySet(), Collections.singleton(selectedCamera), modelView, changeListener);
	}

	private IdObject topParentNotRemoved(IdObject idObject, Set<IdObject> affectedIdObject) {
		IdObject parent = idObject.getParent();
		if (!affectedIdObject.contains(parent)) {
			return parent;
		}
		return topParentNotRemoved(parent, affectedIdObject);
	}

	private void removeFromParent() {
		for (IdObject removedParent : childMap.keySet()) {
			IdObject newParent = topParentMap.get(removedParent);
			for (IdObject affectedChild : childMap.get(removedParent)) {
				if (relink) {
					String parentName = newParent == null ? "null" : newParent.getName();
					System.out.println("changing parent of \"" + affectedChild.getName() + "\" (\"" + removedParent.getName() + "\") to \"" + parentName + "\"");
					affectedChild.setParent(newParent);
				} else {
					affectedChild.setParent(null);
				}
			}
		}
		for (IdObject node : baseParentMap.keySet()) {
			node.setParent(null);
		}
	}
	private void addBackParent() {
		for (IdObject removedParent : childMap.keySet()) {
			for (IdObject affectedChild : childMap.get(removedParent)) {
				affectedChild.setParent(removedParent);
			}
		}
		for (IdObject node : baseParentMap.keySet()) {
			node.setParent(baseParentMap.get(node));
		}
	}

	@Override
	public DeleteNodesAction undo() {
		for (Integer i : orgIndexToNode.keySet()) {
			model.add(orgIndexToNode.get(i), i);
		}
		addBackParent();

		for (Camera camera : affectedCameras) {
			model.add(camera);
		}
		meshLinkDelete.undo();
		modelView.addSelectedCameraNodes(selectedCameraNodes);
		modelView.addSelectedIdObjects(selectedObjects);

		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public DeleteNodesAction redo() {
		for (IdObject object : orgIndexToNode.values()) {
			model.remove(object);
		}
		removeFromParent();
		for (Camera camera : affectedCameras) {
			model.remove(camera);
		}

		getMeshLinkDeleteAction().redo();

		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	private UndoAction getMeshLinkDeleteAction() {
		if (meshLinkDelete == null) {
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
