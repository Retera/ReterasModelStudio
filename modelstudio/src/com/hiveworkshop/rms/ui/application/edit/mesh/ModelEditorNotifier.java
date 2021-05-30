package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.CompoundMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.CompoundRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.CompoundScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.RigAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.rms.util.SubscriberSetNotifier;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelEditorNotifier extends SubscriberSetNotifier<ModelEditor> implements ModelEditor {
	private CloneContextHelper cloneContextHelper;

	public void setCloneContextHelper(CloneContextHelper cloneContextHelper) {
		this.cloneContextHelper = cloneContextHelper;
	}

	@Override
	public UndoAction setSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.setSelectedRegion(region, coordinateSystem));
		}
		return mergeActions(actions);
	}

	private CompoundAction mergeActions(List<UndoAction> actions) {
		return new CompoundAction(actions.get(0).actionName(), actions);
	}

	private CompoundMoveAction mergeMoveActions(List<GenericMoveAction> actions) {
		return new CompoundMoveAction(actions.get(0).actionName(), actions);
	}

	private GenericScaleAction mergeScaleActions(List<GenericScaleAction> actions) {
		return new CompoundScaleAction(actions.get(0).actionName(), actions);
	}

	private CompoundRotateAction mergeRotateActions(List<GenericRotateAction> actions) {
		return new CompoundRotateAction(actions.get(0).actionName(), actions);
	}

	@Override
	public UndoAction removeSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.removeSelectedRegion(region, coordinateSystem));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction addSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.addSelectedRegion(region, coordinateSystem));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction expandSelection() {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.expandSelection());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction invertSelection() {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.invertSelection());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction selectAll() {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.selectAll());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction hideComponent(List<? extends SelectableComponent> selectableComponents,
	                                EditabilityToggleHandler editabilityToggleHandler,
	                                Runnable refreshGUIRunnable) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.hideComponent(selectableComponents, editabilityToggleHandler, refreshGUIRunnable));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction showComponent(EditabilityToggleHandler editabilityToggleHandler) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.showComponent(editabilityToggleHandler));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction autoCenterSelectedBones() {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			try {
				actions.add(handler.autoCenterSelectedBones());
			} catch (final UnsupportedOperationException e) {
				// don't add actions for unsupported operations
			}
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction setSelectedBoneName(String name) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			try {
				actions.add(handler.setSelectedBoneName(name));
			} catch (final UnsupportedOperationException e) {
				// don't add actions for unsupported operations
			}
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction addSelectedBoneSuffix(String name) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			try {
				actions.add(handler.addSelectedBoneSuffix(name));
			} catch (final UnsupportedOperationException e) {
				// don't add actions for unsupported operations
			}
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction setParent(IdObject parent) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			try {
				actions.add(handler.setParent(parent));
			} catch (final UnsupportedOperationException e) {
				// don't add actions for unsupported operations
			}
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction addTeamColor() {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.addTeamColor());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction splitGeoset() {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.splitGeoset());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction translate(double x, double y, double z) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.translate(x, y, z));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction translate(Vec3 v) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.translate(v));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction setPosition(Vec3 center, double x, double y, double z) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.setPosition(center, x, y, z));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction setPosition(Vec3 center, Vec3 v) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.setPosition(center, v));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction rotate(Vec3 center, double rotateX, double rotateY, double rotateZ) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.rotate(center, rotateX, rotateY, rotateZ));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction rotate(Vec3 center, Vec3 rotate) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.rotate(center, rotate));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction setMatrix(java.util.Collection<Bone> bones) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.setMatrix(bones));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction setHDSkinning(Bone[] bones, short[] skinWeights) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.setHDSkinning(bones, skinWeights));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction deleteSelectedComponents() {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.deleteSelectedComponents());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction snapNormals() {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.snapNormals());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction recalcNormals(double maxAngle, boolean useTries) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.recalcNormals(maxAngle, useTries));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction recalcExtents(boolean onlyIncludeEditableGeosets) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.recalcExtents(onlyIncludeEditableGeosets));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction mirror(byte dim, boolean flipModel, double centerX, double centerY, double centerZ) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.mirror(dim, flipModel, centerX, centerY, centerZ));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction flipSelectedFaces() {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.flipSelectedFaces());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction flipSelectedNormals() {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.flipSelectedNormals());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction snapSelectedVertices() {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.snapSelectedVertices());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction snapSelectedNormals() {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.snapSelectedNormals());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction beginExtrudingSelection() {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.beginExtrudingSelection());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction beginExtendingSelection() {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.beginExtendingSelection());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction cloneSelectedComponents(ClonedNodeNamePicker clonedNodeNamePicker) {
		if (cloneContextHelper != null) {
			return cloneContextHelper.cloneSelectedComponents(clonedNodeNamePicker);
		}
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.cloneSelectedComponents(clonedNodeNamePicker));
		}
		return mergeActions(actions);
	}

	@Override
	public void rawTranslate(double x, double y, double z) {
		for (ModelEditor handler : set) {
			handler.rawTranslate(x, y, z);
		}
	}

	@Override
	public void rawScale(double centerX, double centerY, double centerZ,
	                     double scaleX, double scaleY, double scaleZ) {
		for (ModelEditor handler : set) {
			handler.rawScale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
		}
	}

	@Override
	public void rawScale(Vec3 center, Vec3 scale) {
		for (ModelEditor handler : set) {
			handler.rawScale(center, scale);
		}
	}

	@Override
	public void rawRotate2d(double centerX, double centerY, double centerZ,
	                        double radians, byte firstXYZ, byte secondXYZ) {
		for (ModelEditor handler : set) {
			handler.rawRotate2d(centerX, centerY, centerZ, radians, firstXYZ, secondXYZ);
		}
	}

	@Override
	public void rawRotate3d(Vec3 center, Vec3 axis, double radians) {
		for (ModelEditor handler : set) {
			handler.rawRotate3d(center, axis, radians);
		}
	}

	@Override
	public boolean canSelectAt(Point point, CoordinateSystem axes) {
		boolean canSelect = false;
		for (ModelEditor handler : set) {
			canSelect = canSelect || handler.canSelectAt(point, axes);
		}
		return canSelect;
	}

	@Override
	public Vec3 getSelectionCenter() {
		Set<Vec3> centers = new HashSet<>();
		for (ModelEditor handler : set) {
			Vec3 selectionCenter = handler.getSelectionCenter();
			if (Double.isNaN(selectionCenter.x) || Double.isNaN(selectionCenter.y) || Double.isNaN(selectionCenter.z)) {
				continue;
			}
			centers.add(selectionCenter);
		}
		return Vec3.centerOfGroup(centers);
	}

	@Override
	public CopiedModelData copySelection() {
		List<Geoset> allGeosetsCreated = new ArrayList<>();
		List<IdObject> allNodesCreated = new ArrayList<>();
		List<Camera> allCamerasCreated = new ArrayList<>();
		for (ModelEditor handler : set) {
			CopiedModelData copySelection = handler.copySelection();

			allGeosetsCreated.addAll(copySelection.getGeosets());
			allNodesCreated.addAll(copySelection.getIdObjects());
			allCamerasCreated.addAll(copySelection.getCameras());
		}
		return new CopiedModelData(allGeosetsCreated, allNodesCreated, allCamerasCreated);
	}

	@Override
	public void selectByVertices(java.util.Collection<? extends Vec3> newSelection) {
		for (ModelEditor handler : set) {
			handler.selectByVertices(newSelection);
		}
	}

	@Override
	public boolean editorWantsAnimation() {
		for (ModelEditor handler : set) {
			if (handler.editorWantsAnimation()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public GenericMoveAction beginTranslation() {
		List<GenericMoveAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.beginTranslation());
		}
		return mergeMoveActions(actions);
	}

	@Override
	public GenericRotateAction beginRotation(double centerX, double centerY, double centerZ,
	                                         byte firstXYZ, byte secondXYZ) {
		List<GenericRotateAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.beginRotation(centerX, centerY, centerZ, firstXYZ, secondXYZ));
		}
		return mergeRotateActions(actions);
	}

	@Override
	public GenericRotateAction beginSquatTool(double centerX, double centerY, double centerZ,
	                                          byte firstXYZ, byte secondXYZ) {
		List<GenericRotateAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.beginRotation(centerX, centerY, centerZ, firstXYZ, secondXYZ));
		}
		return mergeRotateActions(actions);
	}

	@Override
	public GenericScaleAction beginScaling(double centerX, double centerY, double centerZ) {
		List<GenericScaleAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.beginScaling(centerX, centerY, centerZ));
		}
		return mergeScaleActions(actions);
	}

	@Override
	public GenericScaleAction beginScaling(Vec3 center) {
		List<GenericScaleAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.beginScaling(center));
		}
		return mergeScaleActions(actions);
	}

	@Override
	public UndoAction createKeyframe(ModelEditorActionType actionType) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.createKeyframe(actionType));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction addVertex(double x, double y, double z, Vec3 preferredNormalFacingVector) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.addVertex(x, y, z, preferredNormalFacingVector));
		}
		return mergeActions(actions);
	}

	@Override
	public GenericMoveAction addPlane(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector, int numberOfWidthSegments, int numberOfHeightSegments) {
		List<GenericMoveAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			System.out.println(handler);
			actions.add(handler.addPlane(p1, p2, dim1, dim2, facingVector, numberOfWidthSegments, numberOfHeightSegments));
		}
		return mergeMoveActions(actions);
	}

	@Override
	public GenericMoveAction addBox(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector, int numberOfLengthSegments, int numberOfWidthSegments, int numberOfHeightSegments) {
		List<GenericMoveAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.addBox(p1, p2, dim1, dim2, facingVector, numberOfLengthSegments, numberOfWidthSegments, numberOfHeightSegments));
		}
		return mergeMoveActions(actions);
	}

	@Override
	public UndoAction createFaceFromSelection(Vec3 preferredFacingVector) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			actions.add(handler.createFaceFromSelection(preferredFacingVector));
		}
		return mergeActions(actions);
	}

	@Override
	public String getSelectedMatricesDescription() {
		for(ModelEditor editor: set) {
			String selectedMatricesDescription = editor.getSelectedMatricesDescription();
			if(selectedMatricesDescription != null) {
				return selectedMatricesDescription;
			}
		}
		return null;
	}

	@Override
	public String getSelectedHDSkinningDescription() {
		for(ModelEditor editor: set) {
			String selectedMatricesDescription = editor.getSelectedHDSkinningDescription();
			if(selectedMatricesDescription != null) {
				return selectedMatricesDescription;
			}
		}
		return null;
	}

	@Override
	public RigAction rig() {
		System.out.println("modEd rig, set: " + set.size());
		List<RigAction> rigActions = new ArrayList<>();
		for (ModelEditor handler : set) {
			rigActions.add(handler.rig());
		}
		RigAction rigAction = new RigAction(rigActions.toArray(new RigAction[0]));
		rigAction.redo();
		return rigAction;
	}

	@Override
	public UndoAction addBone(double x, double y, double z) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor handler : set) {
			try {
				actions.add(handler.addBone(x, y, z));
			} catch (final WrongModeException e) {
				// don't add actions for unsupported operations
			}
		}
		return mergeActions(actions);
	}
}
