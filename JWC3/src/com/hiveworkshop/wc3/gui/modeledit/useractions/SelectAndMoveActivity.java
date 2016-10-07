package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;

public class SelectAndMoveActivity extends AbstractSelectAndEditActivity {
	private final MoverWidget moverWidget = null;
	private Point2D.Double startingClick;
	private CursorManager cursorManager;

	@Override
	public void mouseMoved(final MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doDrag(final MouseEvent e, final CoordinateSystem coordinateSystem,
			final SelectionManager selectionManager, final Double startingClick, final Double endingClick) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doAction(final MouseEvent e, final CoordinateSystem coordinateSystem,
			final SelectionManager selectionManager, final Double startingClick, final Double endingClick) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doStartAction(final MouseEvent e, final CoordinateSystem coordinateSystem,
			final SelectionManager selectionManager, final Double startingClick) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onReset(final SelectionManager selectionManager, final CursorManager cursorManager,
			final CoordinateSystem coordinateSystem) {
		this.cursorManager = cursorManager;

	}

	@Override
	protected void onRender(final Graphics2D g, final CoordinateSystem coordinateSystem) {
		moverWidget.render(g, coordinateSystem);
	}

}
