package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.AddSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.RemoveSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;

import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractSelectingEditor<T> implements ModelEditor {
	protected final SelectionManager<T> selectionManager;

	public AbstractSelectingEditor(SelectionManager<T> selectionManager) {
		this.selectionManager = selectionManager;
	}

	@Override
	public final UndoAction setSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem) {
		List<T> newSelection = genericSelect(region, coordinateSystem);
		return setSelectionWithAction(newSelection);
	}

	@Override
	public final UndoAction removeSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem) {
		List<T> newSelection = genericSelect(region, coordinateSystem);
		return removeSelectionWithAction(newSelection);
	}

	@Override
	public final UndoAction addSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem) {
		List<T> newSelection = genericSelect(region, coordinateSystem);
		return addSelectionWithAction(newSelection);
	}

	protected final UndoAction setSelectionWithAction(List<T> newSelection) {
		Set<T> previousSelection = new HashSet<>(selectionManager.getSelection());
		selectionManager.setSelection(newSelection);
		return (new SetSelectionAction<>(newSelection, previousSelection, selectionManager, "select"));
	}

	protected final UndoAction removeSelectionWithAction(List<T> newSelection) {
		Set<T> previousSelection = new HashSet<>(selectionManager.getSelection());
		selectionManager.removeSelection(newSelection);
		return (new RemoveSelectionAction<>(previousSelection, newSelection, selectionManager));
	}

	protected final UndoAction addSelectionWithAction(List<T> newSelection) {
		Set<T> previousSelection = new HashSet<>(selectionManager.getSelection());
		selectionManager.addSelection(newSelection);
		return (new AddSelectionAction<>(previousSelection, newSelection, selectionManager));
	}

	protected abstract List<T> genericSelect(Rectangle2D region, CoordinateSystem coordinateSystem);

	protected abstract UndoAction buildHideComponentAction(List<? extends SelectableComponent> selectableComponents,
	                                                       EditabilityToggleHandler editabilityToggleHandler,
	                                                       Runnable refreshGUIRunnable);

	@Override
	public UndoAction hideComponent(List<? extends SelectableComponent> selectableComponent,
	                                EditabilityToggleHandler editabilityToggleHandler,
	                                Runnable refreshGUIRunnable) {
		UndoAction hideComponentAction = buildHideComponentAction(selectableComponent, editabilityToggleHandler, refreshGUIRunnable);
		hideComponentAction.redo();
		return hideComponentAction;
	}

	@Override
	public UndoAction showComponent(EditabilityToggleHandler editabilityToggleHandler) {
		editabilityToggleHandler.makeEditable();
		return new MakeEditableAction(editabilityToggleHandler);
	}
}
