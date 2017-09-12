package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.AddSelectionAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.RemoveSelectionAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.SetSelectionAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;

public abstract class AbstractSelectingEventHandler<T> implements SelectingEventHandler {
	protected final SelectionManager<T> selectionManager;

	public AbstractSelectingEventHandler(final SelectionManager<T> selectionManager) {
		this.selectionManager = selectionManager;
	}

	@Override
	public final UndoAction setSelectedRegion(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<T> newSelection = genericSelect(region, coordinateSystem);
		return setSelectionWithAction(newSelection);
	}

	@Override
	public final UndoAction removeSelectedRegion(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<T> newSelection = genericSelect(region, coordinateSystem);
		return removeSelectionWithAction(newSelection);
	}

	@Override
	public final UndoAction addSelectedRegion(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<T> newSelection = genericSelect(region, coordinateSystem);
		return addSelectionWithAction(newSelection);
	}

	protected final UndoAction setSelectionWithAction(final List<T> newSelection) {
		final Set<T> previousSelection = new HashSet<>(selectionManager.getSelection());
		selectionManager.setSelection(newSelection);
		return (new SetSelectionAction<>(newSelection, previousSelection, selectionManager, "select"));
	}

	protected final UndoAction removeSelectionWithAction(final List<T> newSelection) {
		selectionManager.removeSelection(newSelection);
		return (new RemoveSelectionAction<>(newSelection, selectionManager));
	}

	protected final UndoAction addSelectionWithAction(final List<T> newSelection) {
		selectionManager.addSelection(newSelection);
		return (new AddSelectionAction<>(newSelection, selectionManager));
	}

	protected abstract List<T> genericSelect(final Rectangle2D region, final CoordinateSystem coordinateSystem);
}
