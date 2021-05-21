package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.CompoundMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.CompoundRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.CompoundScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.util.SubscriberSetNotifier;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

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
	public UndoAction setSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor modelEditor : listenerSet) {
			actions.add(modelEditor.setSelectedRegion(min, max, coordinateSystem));
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
	public UndoAction removeSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor modelEditor : listenerSet) {
			actions.add(modelEditor.removeSelectedRegion(min, max, coordinateSystem));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction addSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor modelEditor : listenerSet) {
			actions.add(modelEditor.addSelectedRegion(min, max, coordinateSystem));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction hideComponent(List<? extends CheckableDisplayElement<?>> selectableComponents,
	                                EditabilityToggleHandler editabilityToggleHandler,
	                                Runnable refreshGUIRunnable) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor modelEditor : listenerSet) {
			actions.add(modelEditor.hideComponent(selectableComponents, editabilityToggleHandler, refreshGUIRunnable));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction showComponent(EditabilityToggleHandler editabilityToggleHandler) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor modelEditor : listenerSet) {
			actions.add(modelEditor.showComponent(editabilityToggleHandler));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction translate(Vec3 v) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor modelEditor : listenerSet) {
			actions.add(modelEditor.translate(v));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction scale(Vec3 center, Vec3 scale) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor modelEditor : listenerSet) {
			actions.add(modelEditor.scale(center, scale));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction setPosition(Vec3 center, Vec3 v) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor modelEditor : listenerSet) {
			actions.add(modelEditor.setPosition(center, v));
		}
		return mergeActions(actions);
	}

	@Override
	public UndoAction rotate(Vec3 center, Vec3 rotate) {
		List<UndoAction> actions = new ArrayList<>();
		for (ModelEditor modelEditor : listenerSet) {
			actions.add(modelEditor.rotate(center, rotate));
		}
		return mergeActions(actions);
	}

	@Override
	public boolean canSelectAt(Vec2 point, CoordinateSystem axes) {
		boolean canSelect = false;
		for (ModelEditor modelEditor : listenerSet) {
			canSelect = canSelect || modelEditor.canSelectAt(point, axes);
		}
		return canSelect;
	}

	@Override
	public Vec3 getSelectionCenter() {
		Set<Vec3> centers = new HashSet<>();
		for (ModelEditor modelEditor : listenerSet) {
			Vec3 selectionCenter = modelEditor.getSelectionCenter();
			if (Double.isNaN(selectionCenter.x) || Double.isNaN(selectionCenter.y) || Double.isNaN(selectionCenter.z)) {
				continue;
			}
			centers.add(selectionCenter);
		}
		return Vec3.centerOfGroup(centers);
	}

	@Override
	public void selectByVertices(java.util.Collection<? extends Vec3> newSelection) {
		for (ModelEditor modelEditor : listenerSet) {
			modelEditor.selectByVertices(newSelection);
		}
	}

	@Override
	public boolean editorWantsAnimation() {
		for (ModelEditor modelEditor : listenerSet) {
			if (modelEditor.editorWantsAnimation()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public GenericMoveAction beginTranslation() {
		List<GenericMoveAction> actions = new ArrayList<>();
		for (ModelEditor modelEditor : listenerSet) {
			actions.add(modelEditor.beginTranslation());
		}
		return mergeMoveActions(actions);
	}

	@Override
	public GenericRotateAction beginRotation(Vec3 center,
	                                         byte firstXYZ, byte secondXYZ) {
		List<GenericRotateAction> actions = new ArrayList<>();
		for (ModelEditor modelEditor : listenerSet) {
			actions.add(modelEditor.beginRotation(center, firstXYZ, secondXYZ));
		}
		return mergeRotateActions(actions);
	}

	@Override
	public GenericRotateAction beginSquatTool(Vec3 center,
	                                          byte firstXYZ, byte secondXYZ) {
		List<GenericRotateAction> actions = new ArrayList<>();
		for (ModelEditor modelEditor : listenerSet) {
			actions.add(modelEditor.beginRotation(center, firstXYZ, secondXYZ));
		}
		return mergeRotateActions(actions);
	}

	@Override
	public GenericScaleAction beginScaling(Vec3 center) {
		List<GenericScaleAction> actions = new ArrayList<>();
		for (ModelEditor modelEditor : listenerSet) {
			actions.add(modelEditor.beginScaling(center));
		}
		return mergeScaleActions(actions);
	}
}
