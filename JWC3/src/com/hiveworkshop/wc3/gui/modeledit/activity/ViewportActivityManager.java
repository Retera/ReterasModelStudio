package com.hiveworkshop.wc3.gui.modeledit.activity;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;

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
	public void modelChanged() {
		currentActivity.modelChanged();
	}

	@Override
	public boolean isEditing() {
		return currentActivity.isEditing();
	}

	@Override
	public void onSelectionChanged(final SelectionView newSelection) {
		currentActivity.onSelectionChanged(newSelection);
	}

	@Override
	public void viewportChanged(final CursorManager cursorManager, final CoordinateSystem coordinateSystem) {
		currentActivity.viewportChanged(cursorManager, coordinateSystem);
	}

	@Override
	public void modelEditorChanged(final ModelEditor newModelEditor) {
		currentActivity.modelEditorChanged(newModelEditor);
	}

}
