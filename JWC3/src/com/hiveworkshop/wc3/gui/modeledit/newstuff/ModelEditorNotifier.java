package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import com.etheller.collections.ArrayList;
import com.etheller.collections.Collection;
import com.etheller.collections.List;
import com.etheller.collections.ListView;
import com.etheller.util.SubscriberSetNotifier;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Vertex;

public class ModelEditorNotifier extends SubscriberSetNotifier<ModelEditor> implements ModelEditor {

	@Override
	public UndoAction setSelectedRegion(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.setSelectedRegion(region, coordinateSystem));
		}
		return mergeActions(actions);
	}

	private CompoundAction mergeActions(final List<UndoAction> actions) {
		return new CompoundAction(actions.get(0).actionName(), actions);
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
	public UndoAction hideComponent(final ListView<? extends SelectableComponent> selectableComponents,
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
	public UndoAction addTeamColor(final ModelStructureChangeListener modelStructureChangeListener) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.addTeamColor(modelStructureChangeListener));
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
	public UndoAction deleteSelectedComponents(final ModelStructureChangeListener modelStructureChangeListener) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.deleteSelectedComponents(modelStructureChangeListener));
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
	public UndoAction cloneSelectedComponents(final ModelStructureChangeListener modelStructureChangeListener,
			final ClonedNodeNamePicker clonedNodeNamePicker) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final ModelEditor handler : set) {
			actions.add(handler.cloneSelectedComponents(modelStructureChangeListener, clonedNodeNamePicker));
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
	public void renderSelection(final ModelElementRenderer renderer, final CoordinateSystem coordinateSystem) {
		for (final ModelEditor handler : set) {
			handler.renderSelection(renderer, coordinateSystem);
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
			Collection.Util.addAll(allGeosetsCreated, copySelection.getGeosets());
			Collection.Util.addAll(allNodesCreated, copySelection.getIdObjects());
			Collection.Util.addAll(allCamerasCreated, copySelection.getCameras());
		}
		return new CopiedModelData(allGeosetsCreated, allNodesCreated, allCamerasCreated);
	}

	@Override
	public void selectByVertices(final java.util.Collection<? extends Vertex> newSelection) {
		for (final ModelEditor handler : set) {
			handler.selectByVertices(newSelection);
		}
	}

}
