package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.awt.Rectangle;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.actions.AddSelectionAction;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.actions.RemoveSelectionAction;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.actions.SetSelectionAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.useractions.UndoActionListener;

public abstract class AbstractSelectingEventHandler<T> implements SelectingEventHandler {
	protected final UndoActionListener undoManager;
	protected final SelectionManager<T> selectionManager;

	public AbstractSelectingEventHandler(final UndoActionListener undoManager,
			final SelectionManager<T> selectionManager) {
		this.undoManager = undoManager;
		this.selectionManager = selectionManager;
	}

	@Override
	public final void setSelectedRegion(final Rectangle region, final CoordinateSystem coordinateSystem) {
		final List<T> newSelection = genericSelect(region, coordinateSystem);
		setSelectionWithAction(newSelection);
	}

	@Override
	public final void removeSelectedRegion(final Rectangle region, final CoordinateSystem coordinateSystem) {
		final List<T> newSelection = genericSelect(region, coordinateSystem);
		removeSelectionWithAction(newSelection);
	}

	@Override
	public final void addSelectedRegion(final Rectangle region, final CoordinateSystem coordinateSystem) {
		final List<T> newSelection = genericSelect(region, coordinateSystem);
		addSelectionWithAction(newSelection);
	}

	protected final void setSelectionWithAction(final List<T> newSelection) {
		final Set<T> previousSelection = selectionManager.getSelection();
		selectionManager.setSelection(newSelection);
		undoManager.pushAction(new SetSelectionAction<>(newSelection, previousSelection, selectionManager, "select"));
	}

	protected final void removeSelectionWithAction(final List<T> newSelection) {
		selectionManager.removeSelection(newSelection);
		undoManager.pushAction(new RemoveSelectionAction<>(newSelection, selectionManager));
	}

	protected final void addSelectionWithAction(final List<T> newSelection) {
		selectionManager.addSelection(newSelection);
		undoManager.pushAction(new AddSelectionAction<>(newSelection, selectionManager));
	}

	protected abstract List<T> genericSelect(final Rectangle region, final CoordinateSystem coordinateSystem);
}
