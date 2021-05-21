package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public final class CloneAction2 implements UndoAction {
	private final ModelView modelView;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final Set<GeosetVertex> newVertices = new HashSet<>();
	private final Set<Triangle> newTriangles = new HashSet<>();
	private final Set<IdObject> newBones = new HashSet<>();
	private final Set<Camera> newCameras = new HashSet<>();
	private final Set<GeosetVertex> selectedVertices = new HashSet<>();
	private final Set<IdObject> selectedIdObjects = new HashSet<>();
	private final Set<Camera> selectedCameras = new HashSet<>();
	private final Map<IdObject, IdObject> oldToNewObjMap = new HashMap<>();
	private final Map<GeosetVertex, GeosetVertex> oldToNewVertMap = new HashMap<>();

	public CloneAction2(ModelView modelView,
	                    ModelStructureChangeListener modelStructureChangeListener,
	                    Collection<? extends Vec3> vertices,
	                    Collection<IdObject> idObjects,
	                    Collection<Camera> cameras) {
		this.modelView = modelView;
		this.modelStructureChangeListener = modelStructureChangeListener;

		vertices.forEach(vert -> selectedVertices.add((GeosetVertex) vert));
		selectedIdObjects.addAll(idObjects);
		selectedCameras.addAll(cameras);

		for (IdObject idObject : selectedIdObjects) {
			IdObject copy = idObject.copy();
			copy.setName(getCopyName(copy.getName()));
			oldToNewObjMap.put(idObject, copy);
			newBones.add(copy);
		}
		for (IdObject idObject : newBones) {
			idObject.setParent(oldToNewObjMap.get(idObject.getParent()));
		}

		for (GeosetVertex vertex : selectedVertices) {
			GeosetVertex newVert = new GeosetVertex(vertex);
			newVertices.add(newVert);
			newVert.clearTriangles();
			oldToNewVertMap.put(vertex, newVert);
			if (newVert.getSkinBones() != null) {
				for (GeosetVertex.SkinBone skinBone : newVert.getSkinBones()) {
					if (oldToNewObjMap.containsKey(skinBone.getBone())) {
						skinBone.setBone((Bone) oldToNewObjMap.get(skinBone.getBone()));
					}
				}
			} else if (!newVert.getBones().isEmpty()) {
				List<Bone> newBones = new ArrayList<>();
				for (Bone bone : newVert.getBones()) {
					if (oldToNewObjMap.containsKey(bone)) {
						newBones.add((Bone) oldToNewObjMap.get(bone));
					} else {
						newBones.add(bone);
					}
				}
				newVert.setBones(newBones);
			}
		}
		for (GeosetVertex vertex : selectedVertices) {
			Set<Triangle> triangles = vertex.getTriangles();
			for (Triangle triangle : triangles) {
				if (selectedVertices.containsAll(Arrays.asList(triangle.getVerts()))) {
					Triangle newTriangle = new Triangle(
							oldToNewVertMap.get(triangle.get(0)),
							oldToNewVertMap.get(triangle.get(1)),
							oldToNewVertMap.get(triangle.get(2)));
					newTriangles.add(newTriangle);
				}
			}
		}

		for (Camera camera : selectedCameras) {
			newCameras.add(new Camera(camera.toMdlx()));
		}
	}

	private String getCopyName(String copyName) {
		String name = copyName + " copy";
		if (modelView.getModel().getObject(name) != null) {
			for (int i = 2; i < 100; i++) {
				if (modelView.getModel().getObject(name + i) == null) {
					return name + i;
				}
			}
		}
		return name;
	}

	@Override
	public void undo() {
		for (GeosetVertex gv : newVertices) {
			gv.getGeoset().remove(gv);
		}
		for (Triangle tri : newTriangles) {
			tri.getGeoset().remove(tri);
		}
		for (IdObject b : newBones) {
			modelView.getModel().remove(b);
		}
		for (Camera camera : newCameras) {
			modelView.getModel().remove(camera);
		}
//		vertexSelectionHelper.selectVertices(source);
		modelStructureChangeListener.nodesUpdated();
	}

	@Override
	public void redo() {
		for (GeosetVertex gv : newVertices) {
			gv.getGeoset().add(gv);
		}
		for (Triangle tri : newTriangles) {
			tri.getGeoset().add(tri);
		}
		for (IdObject b : newBones) {
			modelView.getModel().add(b);
		}
		for (Camera camera : newCameras) {
			modelView.getModel().add(camera);
		}
//		vertexSelectionHelper.selectVertices(newSelection);
//		modelStructureChangeListener.nodesAdded(newBones);
		modelStructureChangeListener.nodesUpdated();
	}

	@Override
	public String actionName() {
		return "clone";
	}

}
