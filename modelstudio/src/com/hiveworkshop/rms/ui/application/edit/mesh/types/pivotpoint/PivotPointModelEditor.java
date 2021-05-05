package com.hiveworkshop.rms.ui.application.edit.mesh.types.pivotpoint;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.DrawBoneAction;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.AutoCenterBonesAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.RenameBoneAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.RigAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.SetParentAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.DoNothingAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.DoNothingMoveActionAdapter;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class PivotPointModelEditor extends AbstractModelEditor<Vec3> {
	private final ProgramPreferences programPreferences;

	public PivotPointModelEditor(ModelView model,
	                             ProgramPreferences programPreferences,
	                             SelectionManager<Vec3> selectionManager,
	                             ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
	}

	private static String getNumberName(String name, int number) {
		return name + String.format("%3s", number).replace(' ', '0');
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
	public GenericMoveAction addPlane(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector,
	                                  int numberOfWidthSegments, int numberOfHeightSegments) {
		return new DoNothingMoveActionAdapter(new DoNothingAction("Add Plane"));
	}

	@Override
	public GenericMoveAction addBox(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector, int numberOfLengthSegments, int numberOfWidthSegments, int numberOfHeightSegments) {
		return new DoNothingMoveActionAdapter(new DoNothingAction("Add Box"));
	}

	@Override
	public UndoAction setParent(IdObject node) {
		Map<IdObject, IdObject> nodeToOldParent = new HashMap<>();
		for (IdObject b : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(b.getPivotPoint())) {
				nodeToOldParent.put(b, b.getParent());
			}
		}
		SetParentAction setParentAction = new SetParentAction(nodeToOldParent, node, structureChangeListener);
		setParentAction.redo();
		return setParentAction;
	}

	@Override
	public UndoAction autoCenterSelectedBones() {
		Set<IdObject> selBones = new HashSet<>();
		for (IdObject b : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(b.getPivotPoint())) {
				selBones.add(b);
			}
		}

		Map<Geoset, Map<Bone, List<GeosetVertex>>> geosetBoneMaps = new HashMap<>();
		for (Geoset geo : model.getModel().getGeosets()) {
			geosetBoneMaps.put(geo, geo.getBoneMap());
		}

		Map<Bone, Vec3> boneToOldPosition = new HashMap<>();
		for (IdObject obj : selBones) {
			if (Bone.class.isAssignableFrom(obj.getClass())) {
				Bone bone = (Bone) obj;
				List<GeosetVertex> childVerts = new ArrayList<>();
				for (Geoset geo : model.getModel().getGeosets()) {
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
	public UndoAction setSelectedBoneName(String name) {
		if (selectionManager.getSelection().size() != 1) {
			throw new IllegalStateException("Only one bone can be renamed at a time.");
		}
		Vec3 selectedVertex = selectionManager.getSelection().iterator().next();
		IdObject node = null;
		for (IdObject bone : model.getEditableIdObjects()) {
			if (bone.getPivotPoint() == selectedVertex) {
				if (node != null) {
					throw new IllegalStateException(
							"Flagrant error. Multiple bones are bound to the same memory addresses. Save your work and restart the application.");
				}
				node = bone;
			}
		}
		if (node == null) {
			throw new IllegalStateException("Selection is not a node");
		}
		RenameBoneAction renameBoneAction = new RenameBoneAction(node.getName(), name, node);
		renameBoneAction.redo();
		return renameBoneAction;
	}

	@Override
	public UndoAction addSelectedBoneSuffix(String name) {
		Set<Vec3> selection = selectionManager.getSelection();
		List<RenameBoneAction> actions = new ArrayList<>();
		for (IdObject bone : model.getEditableIdObjects()) {
			if (selection.contains(bone.getPivotPoint())) {
				RenameBoneAction renameBoneAction = new RenameBoneAction(bone.getName(), bone.getName() + name, bone);
				renameBoneAction.redo();
				actions.add(renameBoneAction);
			}
		}
		return new CompoundAction("add selected bone suffix", actions);
	}

	@Override
	public UndoAction invertSelection() {
		List<Vec3> oldSelection = new ArrayList<>(selectionManager.getSelection());
		Set<Vec3> invertedSelection = new HashSet<>(selectionManager.getSelection());
		for (IdObject object : model.getEditableIdObjects()) {
			toggleSelection(invertedSelection, object.getPivotPoint());
			if (object instanceof CollisionShape) {
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					toggleSelection(invertedSelection, vertex);
				}
			}
		}
		for (Camera object : model.getEditableCameras()) {
			toggleSelection(invertedSelection, object.getPosition());
			toggleSelection(invertedSelection, object.getTargetPosition());
		}
		selectionManager.setSelection(invertedSelection);
		return new SetSelectionAction<>(invertedSelection, oldSelection, selectionManager, "invert selection");
	}

	private void toggleSelection(Set<Vec3> selection, Vec3 position) {
		if (selection.contains(position)) {
			selection.remove(position);
		} else {
			selection.add(position);
		}
	}

	@Override
	public UndoAction selectAll() {
		List<Vec3> oldSelection = new ArrayList<>(selectionManager.getSelection());
		Set<Vec3> allSelection = new HashSet<>();
		for (IdObject object : model.getEditableIdObjects()) {
			allSelection.add(object.getPivotPoint());
			if (object instanceof CollisionShape) {
				allSelection.addAll(((CollisionShape) object).getVertices());
			}
		}
		for (Camera object : model.getEditableCameras()) {
			allSelection.add(object.getPosition());
			allSelection.add(object.getTargetPosition());
		}
		selectionManager.setSelection(allSelection);
		return new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all");
	}

	@Override
	protected List<Vec3> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<Vec3> selectedItems = new ArrayList<>();

		for (IdObject object : model.getEditableIdObjects()) {
			double vertexSize1 = object.getClickRadius(coordinateSystem) * coordinateSystem.getZoom() * 2;
			if (AbstractModelEditor.hitTest(min, max, object.getPivotPoint(), coordinateSystem, vertexSize1)) {
				System.out.println("selected " + object.getName());
				selectedItems.add(object.getPivotPoint());
			}

			if (object instanceof CollisionShape) {
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					int vertexSize = IdObject.DEFAULT_CLICK_RADIUS;
					if (AbstractModelEditor.hitTest(min, max, vertex, coordinateSystem, vertexSize)) {
						selectedItems.add(vertex);
					}
				}
			}
		}
		for (Camera camera : model.getEditableCameras()) {
			int vertexSize = programPreferences.getVertexSize();
			if (AbstractModelEditor.hitTest(min, max, camera.getPosition(), coordinateSystem, vertexSize)) {
				selectedItems.add(camera.getPosition());
			}
			if (AbstractModelEditor.hitTest(min, max, camera.getTargetPosition(), coordinateSystem, vertexSize)) {
				selectedItems.add(camera.getTargetPosition());
			}
		}
		return selectedItems;
	}

	@Override
	public boolean canSelectAt(Vec2 point, CoordinateSystem axes) {
		for (IdObject object : model.getEditableIdObjects()) {
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
		for (Camera camera : model.getEditableCameras()) {
			int vertexSize = programPreferences.getVertexSize();
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
	public void selectByVertices(Collection<? extends Vec3> newSelection) {
		List<Vec3> newlySelectedPivots = new ArrayList<>();
		for (IdObject object : model.getEditableIdObjects()) {
			if (newSelection.contains(object.getPivotPoint())) {
				newlySelectedPivots.add(object.getPivotPoint());
			}
			if (object instanceof CollisionShape) {
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					if (newSelection.contains(vertex)) {
						newlySelectedPivots.add(vertex);
					}
				}
			}
		}
		for (Camera camera : model.getEditableCameras()) {
			if (newSelection.contains(camera.getPosition())) {
				newlySelectedPivots.add(camera.getPosition());
			}
			if (newSelection.contains(camera.getTargetPosition())) {
				newlySelectedPivots.add(camera.getTargetPosition());
			}
		}
		selectionManager.setSelection(newlySelectedPivots);
	}

	@Override
	public UndoAction expandSelection() {
		Set<Vec3> expandedSelection = new HashSet<>(selectionManager.getSelection());
		Set<Vec3> oldSelection = new HashSet<>(selectionManager.getSelection());

		for (IdObject object : model.getEditableIdObjects()) {
			if (object instanceof CollisionShape) {
				boolean selected = false;
				if (oldSelection.contains(object.getPivotPoint())) {
					selected = true;
				}
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					if (oldSelection.contains(vertex)) {
						selected = true;
						break;
					}
				}
				if (selected) {
					expandedSelection.addAll(((CollisionShape) object).getVertices());
					expandedSelection.add(object.getPivotPoint());
				}
			}
		}

		for (Camera camera : model.getEditableCameras()) {
			if (oldSelection.contains(camera.getTargetPosition()) || oldSelection.contains(camera.getPosition())) {
				expandedSelection.add(camera.getPosition());
				expandedSelection.add(camera.getTargetPosition());
			}
		}
		selectionManager.setSelection(expandedSelection);
		return new SetSelectionAction<>(expandedSelection, oldSelection, selectionManager, "expand selection");
	}

	@Override
	protected UndoAction buildHideComponentAction(List<? extends CheckableDisplayElement> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		List<Vec3> previousSelection = new ArrayList<>(selectionManager.getSelection());
		List<Vec3> possibleVerticesToTruncate = new ArrayList<>();
		for (CheckableDisplayElement component : selectableComponents) {
			Object item = component.getItem();
			if (item instanceof Camera) {
				possibleVerticesToTruncate.add(((Camera) item).getPosition());
				possibleVerticesToTruncate.add(((Camera) item).getTargetPosition());
			} else if (item instanceof IdObject) {
				possibleVerticesToTruncate.add(((IdObject) item).getPivotPoint());
				if (item instanceof CollisionShape) {
					possibleVerticesToTruncate.addAll(((CollisionShape) item).getVertices());
				}
			}
		}
		Runnable truncateSelectionRunnable = () -> selectionManager.removeSelection(possibleVerticesToTruncate);
		Runnable unTruncateSelectionRunnable = () -> selectionManager.setSelection(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	@Override
	public CopiedModelData copySelection() {
		Set<Vec3> selection = selectionManager.getSelection();
		Set<IdObject> clonedNodes = new HashSet<>();
		Set<Camera> clonedCameras = new HashSet<>();
		for (IdObject b : model.getEditableIdObjects()) {
			if (selection.contains(b.getPivotPoint())) {
				clonedNodes.add(b.copy());
			}
		}
		for (IdObject obj : clonedNodes) {
			if (!clonedNodes.contains(obj.getParent())) {
				obj.setParent(null);
			}
		}
		for (Camera camera : model.getEditableCameras()) {
			if (selection.contains(camera.getTargetPosition())
					|| selection.contains(camera.getPosition())) {
				clonedCameras.add(camera);
			}
		}
		return new CopiedModelData(new ArrayList<>(), clonedNodes, clonedCameras);
	}

	@Override
	public void rawScale(double centerX, double centerY, double centerZ, double scaleX, double scaleY, double scaleZ) {
		super.rawScale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
		for (IdObject object : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(object.getPivotPoint())) {
				if (object instanceof Bone) {
					scaleBone((Bone) object, scaleX, scaleY, scaleZ);
				} else if (object instanceof CollisionShape) {
					ExtLog extents = ((CollisionShape) object).getExtents();
					if ((extents != null) && (scaleX == scaleY) && (scaleY == scaleZ)) {
						extents.setBoundsRadius(extents.getBoundsRadius() * scaleX);
					}
				}
			}
		}
	}

	public void scaleBone(Bone object, double scaleX, double scaleY, double scaleZ) {
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
		for (IdObject object : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(object.getPivotPoint())) {
				deletedIdObjects.add(object);
			}
		}
		List<Camera> deletedCameras = new ArrayList<>();
		for (Camera camera : model.getEditableCameras()) {
			if (selectionManager.getSelection().contains(camera.getPosition())
					|| selectionManager.getSelection().contains(camera.getTargetPosition())) {
				deletedCameras.add(camera);
			}
		}
		DeleteNodesAction deleteNodesAction = new DeleteNodesAction(selectionManager.getSelection(), deletedIdObjects, deletedCameras, structureChangeListener, model, vertexSelectionHelper);
		deleteNodesAction.redo();
		return deleteNodesAction;
	}

	@Override
	public UndoAction addVertex(double x, double y, double z, Vec3 preferredNormalFacingVector) {
		return new DoNothingAction("add vertex");
	}

	@Override
	public void rawTranslate(double x, double y, double z) {
		super.rawTranslate(x, y, z);
		for (IdObject b : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(b.getPivotPoint())) {
				float[] bindPose = b.getBindPose();
				if (bindPose != null) {
					bindPose[9] += x;
					bindPose[10] += y;
					bindPose[11] += z;
				}
			}
		}
	}

	@Override
	public UndoAction createFaceFromSelection(Vec3 preferredFacingVector) {
		return new DoNothingAction("create face");
	}

	@Override
	public RigAction rig() {
		System.out.println("pivot Rig, sel verts: " + selectionManager.getSelectedVertices().size());
		List<Bone> selectedBones = new ArrayList<>();
		for (IdObject object : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(object.getPivotPoint())) {
				if (object instanceof Bone) {
					selectedBones.add((Bone) object);
				}
			}
		}
		return new RigAction(Collections.emptyList(), selectedBones);
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}

	@Override
	public UndoAction addBone(double x, double y, double z) {
		Set<String> allBoneNames = new HashSet<>();
		for (IdObject object : model.getModel().getIdObjects()) {
			allBoneNames.add(object.getName());
		}
		int nameNumber = 1;
		while (allBoneNames.contains(getNumberName("Bone", nameNumber))) {
			nameNumber++;
		}
		Bone bone = new Bone(getNumberName("Bone", nameNumber));
		bone.setPivotPoint(new Vec3(x, y, z));
		DrawBoneAction drawBoneAction = new DrawBoneAction(model, structureChangeListener, bone);
		drawBoneAction.redo();
		return drawBoneAction;
	}

	@Override
	public String getSelectedHDSkinningDescription() {
		return null;
	}

	@Override
	public String getSelectedMatricesDescription() {
		return null;
	}
}
