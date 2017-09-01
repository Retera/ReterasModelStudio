package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.actions.AddSelectionAction;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.actions.RemoveSelectionAction;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.actions.SetSelectionAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;

public abstract class AbstractSelectingEventHandler<T> implements SelectingEventHandler {
	protected final SelectionManager<T> selectionManager;

	public AbstractSelectingEventHandler(final SelectionManager<T> selectionManager) {
		this.selectionManager = selectionManager;
	}

	@Override
	public final UndoAction setSelectedRegion(final Rectangle2D region, final byte dim1, final byte dim2) {
		final List<T> newSelection = genericSelect(region, dim1, dim2);
		return setSelectionWithAction(newSelection);
	}

	@Override
	public final UndoAction removeSelectedRegion(final Rectangle2D region, final byte dim1, final byte dim2) {
		final List<T> newSelection = genericSelect(region, dim1, dim2);
		return removeSelectionWithAction(newSelection);
	}

	@Override
	public final UndoAction addSelectedRegion(final Rectangle2D region, final byte dim1, final byte dim2) {
		final List<T> newSelection = genericSelect(region, dim1, dim2);
		return addSelectionWithAction(newSelection);
	}

	protected final UndoAction setSelectionWithAction(final List<T> newSelection) {
		final Set<T> previousSelection = selectionManager.getSelection();
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

	protected abstract List<T> genericSelect(final Rectangle2D region, byte dim1, byte dim2);
}
