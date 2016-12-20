package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;

public final class ViewportActivityManager implements ViewportActivity {
	private ViewportActivity currentActivity;

	public ViewportActivityManager(final ViewportActivity currentActivity) {
		this.currentActivity = currentActivity;
	}

	public void setCurrentActivity(final ViewportActivity currentActivity) {
		this.currentActivity = currentActivity;
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
	public ViewportActivity reset(final SelectionManager selectionManager, final SelectionListener selectionListener,
			final CursorManager cursorManager, final CoordinateSystem coordinateSystem, final Runnable updateListener) {
		return currentActivity.reset(selectionManager, selectionListener, cursorManager, coordinateSystem,
				updateListener);
	}

}
