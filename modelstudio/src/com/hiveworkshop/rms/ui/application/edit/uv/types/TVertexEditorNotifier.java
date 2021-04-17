package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.ui.application.edit.mesh.CloneContextHelper;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.CompoundMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.CompoundRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.CompoundScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.SubscriberSetNotifier;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TVertexEditorNotifier extends SubscriberSetNotifier<TVertexEditor> implements TVertexEditor {
	private CloneContextHelper cloneContextHelper;

	public void setCloneContextHelper(final CloneContextHelper cloneContextHelper) {
		this.cloneContextHelper = cloneContextHelper;
	}

	@Override
	public UndoAction setSelectedRegion(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.setSelectedRegion(region, coordinateSystem));
		}
		return mergeActions(actions);
	}

	private CompoundAction mergeActions(final List<? extends UndoAction> actions) {
		return new CompoundAction(actions.get(0).actionName(), actions);
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
		for (final TVertexEditor handler : set) {
			actions.add(handler.removeSelectedRegion(region, coordinateSystem));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction addSelectedRegion(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.addSelectedRegion(region, coordinateSystem));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction expandSelection() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.expandSelection());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction invertSelection() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.invertSelection());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction selectAll() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.selectAll());
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction hideComponent(final List<? extends SelectableComponent> selectableComponents,
	                                final EditabilityToggleHandler editabilityToggleHandler,
	                                final Runnable refreshGUIRunnable) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.hideComponent(selectableComponents, editabilityToggleHandler, refreshGUIRunnable));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction showComponent(final EditabilityToggleHandler editabilityToggleHandler) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.showComponent(editabilityToggleHandler));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction translate(final double x, final double y) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.translate(x, y));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction setPosition(final Vec2 center, final double x, final double y) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.setPosition(center, x, y));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction rotate(final Vec2 center, final double rotateRadians) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.rotate(center, rotateRadians));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction mirror(final byte dim, final double centerX, final double centerY) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.mirror(dim, centerX, centerY));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction snapSelectedVertices() {
		final List<UndoAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.snapSelectedVertices());
		}
		return mergeActions(actions);
	}

	@Override
	public void rawTranslate(final double x, final double y) {
		for (final TVertexEditor handler : set) {
			handler.rawTranslate(x, y);
		}
	}

	@Override
	public void rawScale(final double centerX, final double centerY, final double scaleX, final double scaleY) {
		for (final TVertexEditor handler : set) {
			handler.rawScale(centerX, centerY, scaleX, scaleY);
		}
	}

	@Override
	public void rawRotate2d(final double centerX, final double centerY,
	                        final double radians, final byte dim1, final byte dim2) {
		for (final TVertexEditor handler : set) {
			handler.rawRotate2d(centerX, centerY, radians, dim1, dim2);
		}
	}

	@Override
	public boolean canSelectAt(final Point point, final CoordinateSystem axes) {
		boolean canSelect = false;
		for (final TVertexEditor handler : set) {
			canSelect = canSelect || handler.canSelectAt(point, axes);
		}
		return canSelect;
	}

	@Override
	public Vec2 getSelectionCenter() {
		final Set<Vec2> centers = new HashSet<>();
		for (final TVertexEditor handler : set) {
			final Vec2 selectionCenter = handler.getSelectionCenter();
			if (Double.isNaN(selectionCenter.x) || Double.isNaN(selectionCenter.y)) {
				continue;
			}
			centers.add(selectionCenter);
		}
		return Vec2.centerOfGroup(centers);
	}

	@Override
	public void selectByVertices(final java.util.Collection<? extends Vec3> newSelection) {
		for (final TVertexEditor handler : set) {
			handler.selectByVertices(newSelection);
		}
	}

	@Override
	public GenericMoveAction beginTranslation() {
		final List<GenericMoveAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.beginTranslation());
		}
		return mergeMoveActions(actions);
	}

	@Override
	public GenericRotateAction beginRotation(final double centerX, final double centerY,
	                                         final byte dim1, final byte dim2) {
		final List<GenericRotateAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.beginRotation(centerX, centerY, dim1, dim2));
		}
		return mergeRotateActions(actions);
	}

	@Override
	public GenericScaleAction beginScaling(final double centerX, final double centerY) {
		final List<GenericScaleAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.beginScaling(centerX, centerY));
		}
		return mergeScaleActions(actions);
	}

	@Override
	public void setUVLayerIndex(final int uvLayerIndex) {
		for (final TVertexEditor handler : set) {
			handler.setUVLayerIndex(uvLayerIndex);
		}
	}

	@Override
	public UndoAction selectFromViewer(final SelectionView viewerSelectionView) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.selectFromViewer(viewerSelectionView));
		}
		return mergeActions(actions);
	}

	@Override
	public int getUVLayerIndex() {
		int uvLayerIndex = -1;
		for (final TVertexEditor handler : set) {
			if (uvLayerIndex == -1) {
				uvLayerIndex = handler.getUVLayerIndex();
			} else if (uvLayerIndex != handler.getUVLayerIndex()) {
				throw new IllegalStateException("Differing UV Layer Indices between editors: " + uvLayerIndex + " != " + handler.getUVLayerIndex());
			}
		}
		return uvLayerIndex;
	}

	@Override
	public UndoAction remap(final byte xDim, final byte yDim, final UVPanel.UnwrapDirection unwrapDirection) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final TVertexEditor handler : set) {
			actions.add(handler.remap(xDim, yDim, unwrapDirection));
		}
		return mergeActions(actions);
	}
}
