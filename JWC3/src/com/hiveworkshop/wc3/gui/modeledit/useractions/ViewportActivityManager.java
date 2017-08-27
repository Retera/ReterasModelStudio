package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelChangeNotifier;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItemView;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionTypeApplicator;

public final class ViewportActivityManager implements ViewportActivity {
	private ViewportActivity currentActivity;

	public ViewportActivityManager(final ViewportActivity currentActivity) {
		this.currentActivity = currentActivity;
	}

	public void setCurrentActivity(final ViewportActivity currentActivity) {
		this.currentActivity = currentActivity;
		if (this.currentActivity != null) {
		}
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		currentActivity.mousePressed(e);
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		currentActivity.mouseReleased(e);
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		currentActivity.mouseMoved(e);
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		currentActivity.mouseDragged(e);
	}

	@Override
	public void render(final Graphics2D g) {
		currentActivity.render(g);
	}

	@Override
	public ViewportActivity reset(final SelectionManager selectionManager,
			final SelectionTypeApplicator selectionListener, final CursorManager cursorManager,
			final CoordinateSystem coordinateSystem, final UndoManager undoManager,
			final ModelChangeNotifier modelChangeNotifier) {
		return currentActivity.reset(selectionManager, selectionListener, cursorManager, coordinateSystem, undoManager,
				modelChangeNotifier);
	}

	@Override
	public void onSelectionChanged(final List<? extends SelectionItemView> previousSelection,
			final List<? extends SelectionItemView> newSelection) {
		currentActivity.onSelectionChanged(previousSelection, newSelection);
	}

	@Override
	public void modelChanged() {
		currentActivity.modelChanged();
	}

	@Override
	public boolean isEditing() {
		return currentActivity.isEditing();
	}

}
