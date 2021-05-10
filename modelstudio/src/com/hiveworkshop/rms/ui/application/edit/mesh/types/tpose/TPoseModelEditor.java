package com.hiveworkshop.rms.ui.application.edit.mesh.types.tpose;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.AutoCenterBonesAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.RenameBoneAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.SetParentAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.DoNothingAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class TPoseModelEditor extends AbstractModelEditor<IdObject> {

	public TPoseModelEditor(SelectionManager<IdObject> selectionManager,
	                        ModelStructureChangeListener structureChangeListener,
	                        ModelHandler modelHandler) {
		super(selectionManager, structureChangeListener, modelHandler);
	}

	@Override
	public UndoAction setParent(IdObject node) {
		Map<IdObject, IdObject> nodeToOldParent = new HashMap<>();
		for (IdObject b : modelView.getEditableIdObjects()) {
			if (modelView.isSelected(b)) {
				nodeToOldParent.put(b, b.getParent());
			}
		}
		SetParentAction setParentAction = new SetParentAction(nodeToOldParent, node, structureChangeListener);
		setParentAction.redo();
		return setParentAction;
	}

	@Override
	public UndoAction addTeamColor() {
		return new DoNothingAction("add team color");
	}

	@Override
	public UndoAction splitGeoset() {
		return new DoNothingAction("split geoset");
	}

	@Override
	public UndoAction autoCenterSelectedBones() {
		Set<IdObject> selBones = new HashSet<>();
		for (IdObject b : modelView.getEditableIdObjects()) {
			if (modelView.isSelected(b)) {
				selBones.add(b);
			}
		}

		Map<Geoset, Map<Bone, List<GeosetVertex>>> geosetBoneMaps = new HashMap<>();
		for (Geoset geo : modelView.getModel().getGeosets()) {
			geosetBoneMaps.put(geo, geo.getBoneMap());
		}

		Map<Bone, Vec3> boneToOldPosition = new HashMap<>();
		for (IdObject obj : selBones) {
			if (Bone.class.isAssignableFrom(obj.getClass())) {
				Bone bone = (Bone) obj;
				List<GeosetVertex> childVerts = new ArrayList<>();
				for (Geoset geo : modelView.getModel().getGeosets()) {
					List<GeosetVertex> vertices = geosetBoneMaps.get(geo).get(bone);
					if (vertices != null) {
						childVerts.addAll(vertices);
					}
				}
				if (childVerts.size() > 0) {
					Vec3 pivotPoint = bone.getPivotPoint();
					boneToOldPosition.put(bone, new Vec3(pivotPoint));
					pivotPoint.set(Vec3.centerOfGroup(childVerts));
				}
			}
		}
		return new AutoCenterBonesAction(boneToOldPosition);
	}

	@Override
	public UndoAction expandSelection() {
		throw new WrongModeException("Not supported in T-Pose mode");
	}

	@Override
	public UndoAction setSelectedBoneName(String name) {
		if (selectionManager.getSelection().size() != 1) {
			throw new IllegalStateException("Only one bone can be renamed at a time.");
		}
		IdObject node = modelView.getSelectedIdObjects().iterator().next();
		if (node == null) {
			throw new IllegalStateException("Selection is not a node");
		}
		RenameBoneAction renameBoneAction = new RenameBoneAction(node.getName(), name, node);
		renameBoneAction.redo();
		return renameBoneAction;
	}

	@Override
	public UndoAction addSelectedBoneSuffix(String name) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		List<RenameBoneAction> actions = new ArrayList<>();
		for (IdObject bone : selection) {
			RenameBoneAction renameBoneAction = new RenameBoneAction(bone.getName(), bone.getName() + name, bone);
			renameBoneAction.redo();
			actions.add(renameBoneAction);
		}
		return new CompoundAction("add selected bone suffix", actions);
	}

	private void toggleSelection(Set<Vec3> selection, Vec3 position) {
		if (selection.contains(position)) {
			selection.remove(position);
		} else {
			selection.add(position);
		}
	}

	@Override
	public void selectByVertices(Collection<? extends Vec3> newSelection) {
		Set<IdObject> newlySelectedPivots = new HashSet<>();
		for (IdObject object : modelView.getEditableIdObjects()) {
			if (newSelection.contains(object.getPivotPoint())) {
				newlySelectedPivots.add(object);
			}
			if (object instanceof CollisionShape) {
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					if (newSelection.contains(vertex)) {
						newlySelectedPivots.add(object);
					}
				}
			}
		}
		modelView.setSelectedIdObjects(newlySelectedPivots);
	}

	@Override
	protected List<IdObject> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<IdObject> selectedItems = new ArrayList<>();

		for (IdObject object : modelView.getEditableIdObjects()) {
			double vertexSize = object.getClickRadius(coordinateSystem) * coordinateSystem.getZoom();
			if (AbstractModelEditor.hitTest(min, max, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object);
			}
			if (object instanceof CollisionShape) {
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					if (AbstractModelEditor.hitTest(min, max, vertex, coordinateSystem, IdObject.DEFAULT_CLICK_RADIUS)) {
						selectedItems.add(object);
					}
				}
			}
		}
		return selectedItems;
	}

	@Override
	public boolean canSelectAt(Vec2 point, CoordinateSystem axes) {
		for (IdObject object : modelView.getEditableIdObjects()) {
			double vertexSize1 = object.getClickRadius(axes) * axes.getZoom() * 2;
			if (AbstractModelEditor.hitTest(object.getPivotPoint(), CoordSysUtils.geomV2(axes, point), axes, vertexSize1)) {
				return true;
			}

			if (object instanceof CollisionShape) {
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					int vertexSize = IdObject.DEFAULT_CLICK_RADIUS;
					if (AbstractModelEditor.hitTest(vertex, CoordSysUtils.geomV2(axes, point), axes, vertexSize)) {
						return true;
					}
				}
			}
		}
		for (Camera camera : modelView.getEditableCameras()) {
			int vertexSize = ProgramGlobals.getPrefs().getVertexSize();
			if (AbstractModelEditor.hitTest(camera.getPosition(), CoordSysUtils.geomV2(axes, point), axes, vertexSize)) {
				return true;
			}
			if (AbstractModelEditor.hitTest(camera.getTargetPosition(), CoordSysUtils.geomV2(axes, point), axes, vertexSize)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public UndoAction invertSelection() {
		throw new WrongModeException("Not supported in T-Pose mode");
	}

	@Override
	public UndoAction selectAll() {
		throw new WrongModeException("Not supported in T-Pose mode");
	}

	@Override
	protected UndoAction buildHideComponentAction(List<? extends CheckableDisplayElement<?>> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		List<IdObject> previousSelection = new ArrayList<>(modelView.getSelectedIdObjects());
		Runnable truncateSelectionRunnable = () -> modelView.removeSelectedIdObjects(modelView.getModel().getIdObjects());
		Runnable unTruncateSelectionRunnable = () -> modelView.setSelectedIdObjects(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	@Override
	public void rawScale(double centerX, double centerY, double centerZ, double scaleX, double scaleY, double scaleZ) {
		super.rawScale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
		for (IdObject object : modelView.getEditableIdObjects()) {
			if (modelView.isSelected(object)) {
				if (object instanceof Bone) {
					translateBone((Bone) object, scaleX, scaleY, scaleZ);
				} else if (object instanceof CollisionShape) {
					ExtLog extents = ((CollisionShape) object).getExtents();
					if ((extents != null) && (scaleX == scaleY) && (scaleY == scaleZ)) {
						extents.setBoundsRadius(extents.getBoundsRadius() * scaleX);
					}
				}
			}
		}
	}

	public void translateBone(Bone object, double scaleX, double scaleY, double scaleZ) {
		Vec3AnimFlag translation = (Vec3AnimFlag) object.find("Translation");
		if (translation != null) {
			for (int i = 0; i < translation.size(); i++) {
				Vec3 scaleData = translation.getValues().get(i);
				scaleData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
				if (translation.tans()) {
					Vec3 inTanData = translation.getInTans().get(i);
					inTanData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
					Vec3 outTanData = translation.getInTans().get(i);
					outTanData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
				}
			}
		}
	}

	@Override
	public UndoAction deleteSelectedComponents() {
		List<IdObject> deletedIdObjects = new ArrayList<>();
		for (IdObject object : modelView.getEditableIdObjects()) {
			if (modelView.isSelected(object)) {
				deletedIdObjects.add(object);
			}
		}
		List<Camera> deletedCameras = new ArrayList<>();
		for (Camera camera : modelView.getEditableCameras()) {
			if (selectionManager.getSelection().contains(camera.getPosition()) || selectionManager.getSelection().contains(camera.getTargetPosition())) {
				deletedCameras.add(camera);
			}
		}
		DeleteNodesAction deleteNodesAction = new DeleteNodesAction(selectionManager.getSelectedVertices(), deletedIdObjects, deletedCameras, structureChangeListener, modelView, vertexSelectionHelper);
		deleteNodesAction.redo();
		return deleteNodesAction;
	}

	@Override
	public CopiedModelData copySelection() {
		Collection<? extends Vec3> selection = selectionManager.getSelectedVertices();
		Set<IdObject> clonedNodes = new HashSet<>();
		Set<Camera> clonedCameras = new HashSet<>();
		for (IdObject b : modelView.getEditableIdObjects()) {
			if (modelView.isSelected(b)) {
				clonedNodes.add(b.copy());
			}
		}
		for (IdObject obj : clonedNodes) {
			if (!clonedNodes.contains(obj.getParent())) {
				obj.setParent(null);
			}
		}
		for (Camera camera : modelView.getEditableCameras()) {
			if (selection.contains(camera.getTargetPosition()) || selection.contains(camera.getPosition())) {
				clonedCameras.add(camera);
			}
		}
		return new CopiedModelData(new ArrayList<>(), clonedNodes, clonedCameras);
	}

	@Override
	public UndoAction addVertex(double x, double y, double z, Vec3 preferredNormalFacingVector) {
		return new DoNothingAction("add vertex");
	}

	@Override
	public UndoAction createFaceFromSelection(Vec3 preferredFacingVector) {
		return new DoNothingAction("create face");
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
