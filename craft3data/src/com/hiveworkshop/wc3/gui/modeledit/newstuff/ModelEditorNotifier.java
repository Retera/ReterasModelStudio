package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.etheller.util.SubscriberSetNotifier;
import com.hiveworkshop.wc3.gui.animedit.WrongModeException;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.editor.CompoundMoveAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.editor.CompoundRotateAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.editor.CompoundScaleAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools.RigAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Vertex;

public class ModelEditorNotifier extends SubscriberSetNotifier<ModelEditor> implements ModelEditor {
	private CloneContextHelper cloneContextHelper;

	public void setCloneContextHelper(final CloneContextHelper cloneContextHelper) {
		this.cloneContextHelper = cloneContextHelper;
	}

	@Override
	public UndoAction setSelectedRegion(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.setSelectedRegion(region, coordinateSystem));
		}
		return mergeActions(actions);
	}

	private CompoundAction mergeActions(final List<UndoAction> actions) {
		return new CompoundAction(actions.get(0).actionName(), (java.util.List<? extends UndoAction>) actions);
	}

	private CompoundMoveAction mergeMoveActions(final List<GenericMoveAction> actions) {
		return new CompoundMoveAction(actions.get(0).actionName(), actions);
	}

	private GenericScaleAction mergeScaleActions(final List<GenericScaleAction> actions) {
		return new CompoundScaleAction(actions.get(0).actionName(), actions);
	}

	private CompoundRotateAction mergeRotateActions(final List<GenericRotateAction> actions) {
		return new CompoundRotateAction(actions.get(0).actionName(), actions);
	}

	@Override
	public UndoAction removeSelectedRegion(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.removeSelectedRegion(region, coordinateSystem));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction addSelectedRegion(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.addSelectedRegion(region, coordinateSystem));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction expandSelection() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.expandSelection());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction invertSelection() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.invertSelection());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction selectAll() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.selectAll());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction hideComponent(final List<? extends SelectableComponent> selectableComponents,
			final EditabilityToggleHandler editabilityToggleHandler, final Runnable refreshGUIRunnable) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.hideComponent(selectableComponents, editabilityToggleHandler, refreshGUIRunnable));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction showComponent(final EditabilityToggleHandler editabilityToggleHandler) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.showComponent(editabilityToggleHandler));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction autoCenterSelectedBones() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			try {
				actions.add(handler.autoCenterSelectedBones());
			} catch (final UnsupportedOperationException e) {
				// don't add actions for unsupported operations
			}
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction setSelectedBoneName(final String name) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			try {
				actions.add(handler.setSelectedBoneName(name));
			} catch (final UnsupportedOperationException e) {
				// don't add actions for unsupported operations
			}
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction addSelectedBoneSuffix(final String name) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			try {
				actions.add(handler.addSelectedBoneSuffix(name));
			} catch (final UnsupportedOperationException e) {
				// don't add actions for unsupported operations
			}
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction setParent(final IdObject parent) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
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
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.addTeamColor());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction splitGeoset() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.splitGeoset());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction translate(final double x, final double y, final double z) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.translate(x, y, z));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction setPosition(final Vertex center, final double x, final double y, final double z) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.setPosition(center, x, y, z));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction rotate(final Vertex center, final double rotateX, final double rotateY, final double rotateZ) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.rotate(center, rotateX, rotateY, rotateZ));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction setMatrix(final java.util.Collection<Bone> bones) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.setMatrix(bones));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction deleteSelectedComponents() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.deleteSelectedComponents());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction snapNormals() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.snapNormals());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction recalcNormals() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.recalcNormals());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction recalcExtents(final boolean onlyIncludeEditableGeosets) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.recalcExtents(onlyIncludeEditableGeosets));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction mirror(final byte dim, final boolean flipModel, final double centerX, final double centerY,
			final double centerZ) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.mirror(dim, flipModel, centerX, centerY, centerZ));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction flipSelectedFaces() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.flipSelectedFaces());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction flipSelectedNormals() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.flipSelectedNormals());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction snapSelectedVertices() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.snapSelectedVertices());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction snapSelectedNormals() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.snapSelectedNormals());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction beginExtrudingSelection() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.beginExtrudingSelection());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction beginExtendingSelection() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.beginExtendingSelection());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction cloneSelectedComponents(final ClonedNodeNamePicker clonedNodeNamePicker) {
		if (cloneContextHelper != null) {
			return cloneContextHelper.cloneSelectedComponents(clonedNodeNamePicker);
		}
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.cloneSelectedComponents(clonedNodeNamePicker));
		}
		return mergeActions(actions);
	}

	@Override
	public void rawTranslate(final double x, final double y, final double z) {
		for (final ModelEditor handler : set) {
			handler.rawTranslate(x, y, z);
		}
	}

	@Override
	public void rawScale(final double centerX, final double centerY, final double centerZ, final double scaleX,
			final double scaleY, final double scaleZ) {
		for (final ModelEditor handler : set) {
			handler.rawScale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
		}
	}

	@Override
	public void rawRotate2d(final double centerX, final double centerY, final double centerZ, final double radians,
			final byte firstXYZ, final byte secondXYZ) {
		for (final ModelEditor handler : set) {
			handler.rawRotate2d(centerX, centerY, centerZ, radians, firstXYZ, secondXYZ);
		}
	}

	@Override
	public void rawRotate3d(final Vertex center, final Vertex axis, final double radians) {
		for (final ModelEditor handler : set) {
			handler.rawRotate3d(center, axis, radians);
		}
	}

	@Override
	public boolean canSelectAt(final Point point, final CoordinateSystem axes) {
		boolean canSelect = false;
		for (final ModelEditor handler : set) {
			canSelect = canSelect || handler.canSelectAt(point, axes);
		}
		return canSelect;
	}

	@Override
	public Vertex getSelectionCenter() {
		final Set<Vertex> centers = new HashSet<>();
		for (final ModelEditor handler : set) {
			final Vertex selectionCenter = handler.getSelectionCenter();
			if (Double.isNaN(selectionCenter.x) || Double.isNaN(selectionCenter.y) || Double.isNaN(selectionCenter.z)) {
				continue;
			}
			centers.add(selectionCenter);
		}
		return Vertex.centerOfGroup(centers);
	}

	@Override
	public CopiedModelData copySelection() {
		final List<Geoset> allGeosetsCreated = new ArrayList<>();
		final List<IdObject> allNodesCreated = new ArrayList<>();
		final List<Camera> allCamerasCreated = new ArrayList<>();
		for (final ModelEditor handler : set) {
			final CopiedModelData copySelection = handler.copySelection();

			allGeosetsCreated.addAll(copySelection.getGeosets());
			allNodesCreated.addAll(copySelection.getIdObjects());
			allCamerasCreated.addAll(copySelection.getCameras());
		}
		return new CopiedModelData(allGeosetsCreated, allNodesCreated, allCamerasCreated);
	}

	@Override
	public void selectByVertices(final java.util.Collection<? extends Vertex> newSelection) {
		for (final ModelEditor handler : set) {
			handler.selectByVertices(newSelection);
		}
	}

	@Override
	public boolean editorWantsAnimation() {
		for (final ModelEditor handler : set) {
			if (handler.editorWantsAnimation()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public GenericMoveAction beginTranslation() {
		final List<GenericMoveAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.beginTranslation());
		}
		return mergeMoveActions(actions);
	}

	@Override
	public GenericRotateAction beginRotation(final double centerX, final double centerY, final double centerZ,
			final byte firstXYZ, final byte secondXYZ) {
		final List<GenericRotateAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.beginRotation(centerX, centerY, centerZ, firstXYZ, secondXYZ));
		}
		return mergeRotateActions(actions);
	}

	@Override
	public GenericRotateAction beginSquatTool(final double centerX, final double centerY, final double centerZ,
			final byte firstXYZ, final byte secondXYZ) {
		final List<GenericRotateAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.beginRotation(centerX, centerY, centerZ, firstXYZ, secondXYZ));
		}
		return mergeRotateActions(actions);
	}

	@Override
	public GenericScaleAction beginScaling(final double centerX, final double centerY, final double centerZ) {
		final List<GenericScaleAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.beginScaling(centerX, centerY, centerZ));
		}
		return mergeScaleActions(actions);
	}

	@Override
	public UndoAction createKeyframe(final ModelEditorActionType actionType) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.createKeyframe(actionType));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction addVertex(final double x, final double y, final double z,
			final Vertex preferredNormalFacingVector) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.addVertex(x, y, z, preferredNormalFacingVector));
		}
		return mergeActions(actions);
	}

	@Override
	public GenericMoveAction addPlane(final double x, final double y, final double x2, final double y2, final byte dim1,
			final byte dim2, final Vertex facingVector, final int numberOfSegmentsX, final int numberOfSegmentsY) {
		final List<GenericMoveAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.addPlane(x, y, x2, y2, dim1, dim2, facingVector, numberOfSegmentsX, numberOfSegmentsY));
		}
		return mergeMoveActions(actions);
	}

	@Override
	public UndoAction createFaceFromSelection(final Vertex preferredFacingVector) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.createFaceFromSelection(preferredFacingVector));
		}
		return mergeActions(actions);
	}

	@Override
	public GenericMoveAction addBox(final double x, final double y, final double x2, final double y2, final byte dim1,
			final byte dim2, final Vertex facingVector, final int numberOfLengthSegments,
			final int numberOfWidthSegments, final int numberOfHeightSegments) {
		final List<GenericMoveAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.addBox(x, y, x2, y2, dim1, dim2, facingVector, numberOfLengthSegments,
					numberOfWidthSegments, numberOfHeightSegments));
		}
		return mergeMoveActions(actions);
	}

	@Override
	public RigAction rig() {
		final List<RigAction> rigActions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			rigActions.add(handler.rig());
		}
		final RigAction rigAction = new RigAction(rigActions.toArray(new RigAction[rigActions.size()]));
		rigAction.redo();
		return rigAction;
	}

	@Override
	public UndoAction addBone(final double x, final double y, final double z) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			try {
				actions.add(handler.addBone(x, y, z));
			} catch (final WrongModeException e) {
				// don't add actions for unsupported operations
			}
		}
		return mergeActions(actions);
	}
}
