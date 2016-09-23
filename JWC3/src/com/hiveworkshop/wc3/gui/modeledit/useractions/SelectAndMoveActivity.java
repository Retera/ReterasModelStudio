package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;

public class SelectAndMoveActivity implements ViewportActivity {
	private SelectionManager selectionManager;
	private Point2D.Double startingClick;
	private Point2D.Double endingClick;
	private CoordinateSystem coordinateSystem;

	public SelectAndMoveActivity reset(final SelectionManager selectionManager,
			final CoordinateSystem coordinateSystem) {
		this.selectionManager = selectionManager;
		this.coordinateSystem = coordinateSystem;
		startingClick = null;
		return this;
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			startingClick = new Point2D.Double(coordinateSystem.geomX(e.getPoint().getX()),
					coordinateSystem.geomY(e.getPoint().getY()));
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (startingClick != null) {
			if (endingClick != null) {
				endingClick = new Point2D.Double(coordinateSystem.geomX(e.getPoint().getX()),
						coordinateSystem.geomY(e.getPoint().getY()));
			}
		}
		startingClick = null;

	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		if (startingClick != null && endingClick != null) {
			endingClick = new Point2D.Double(coordinateSystem.geomX(e.getPoint().getX()),
					coordinateSystem.geomY(e.getPoint().getY()));
		}
	}

	@Override
	public void render(final Graphics2D g) {
		if (startingClick != null && endingClick != null) {
			g.setColor(Color.RED);
			final double minX = Math.min(startingClick.x, endingClick.x);
			final double minY = Math.min(startingClick.y, endingClick.y);
			final double maxX = Math.max(startingClick.x, endingClick.x);
			final double maxY = Math.max(startingClick.y, endingClick.y);
			g.drawRect((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY));
		}

	}
}
