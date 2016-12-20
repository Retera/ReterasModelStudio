package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItem;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;

public abstract class AbstractSelectAndEditActivity implements ViewportActivity {
	private SelectionManager selectionManager;
	private Point2D.Double startingClick;
	private Point2D.Double endingClick;
	private CoordinateSystem coordinateSystem;
	private ActionType actionType;
	private Runnable updateListener;
	private SelectionListener selectionListener;

	@Override
	public AbstractSelectAndEditActivity reset(final SelectionManager selectionManager,
			final SelectionListener selectionListener, final CursorManager cursorManager,
			final CoordinateSystem coordinateSystem, final Runnable updateListener) {
		this.selectionManager = selectionManager;
		this.selectionListener = selectionListener;
		this.coordinateSystem = coordinateSystem;
		this.updateListener = updateListener;
		startingClick = null;
		actionType = null;
		onReset(selectionManager, cursorManager, coordinateSystem);
		return this;
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		// startingClick = new
		// Point2D.Double(coordinateSystem.geomX(e.getPoint().getX()),
		// coordinateSystem.geomY(e.getPoint().getY()));
		startingClick = new Point2D.Double(e.getPoint().getX(), e.getPoint().getY());
		if (SwingUtilities.isLeftMouseButton(e)) {
			actionType = ActionType.SELECT;
		} else if (SwingUtilities.isRightMouseButton(e)) {
			actionType = ActionType.EDIT;
			doStartAction(e, coordinateSystem, selectionManager, startingClick);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (actionType == ActionType.SELECT) {
			if (startingClick != null) {
				if (endingClick != null) {
					endingClick = new Point2D.Double(e.getPoint().getX(), e.getPoint().getY());
					// coordinateSystem.geomX(e.getPoint().getX()),
					// coordinateSystem.geomY(e.getPoint().getY()));
					final double minX = Math.min(startingClick.x, endingClick.x);
					final double minY = Math.min(startingClick.y, endingClick.y);
					final double maxX = Math.max(startingClick.x, endingClick.x);
					final double maxY = Math.max(startingClick.y, endingClick.y);
					final Rectangle area = new Rectangle((int) minX, (int) minY, (int) (maxX - minX),
							(int) (maxY - minY));
					final List<SelectionItem> selectedItems = new ArrayList<>();
					for (final SelectionItem item : selectionManager.getSelectableItems()) {
						if (item.hitTest(startingClick, coordinateSystem) || item.hitTest(endingClick, coordinateSystem)
								|| item.hitTest(area, coordinateSystem)) {
							selectedItems.add(item);
						}
					}
					selectionListener.chooseGroup(selectedItems);
					onSelect(e, coordinateSystem, selectionManager);
				}
			}
		} else {
			doAction(e, coordinateSystem, selectionManager, startingClick, endingClick);
		}
		startingClick = null;
		endingClick = null;
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		if (!SwingUtilities.isMiddleMouseButton(e)) {
			endingClick = new Point2D.Double(e.getPoint().getX(), e.getPoint().getY());
			// endingClick = new
			// Point2D.Double(coordinateSystem.geomX(e.getPoint().getX()),
			// coordinateSystem.geomY(e.getPoint().getY()));
			if (actionType == ActionType.SELECT) {
			} else {
				doDrag(e, coordinateSystem, selectionManager, startingClick, endingClick);
			}
		}
	}

	protected abstract void doDrag(MouseEvent e, CoordinateSystem coordinateSystem, SelectionManager selectionManager,
			Point2D.Double startingClick, Point2D.Double endingClick);

	protected abstract void onReset(final SelectionManager selectionManager, final CursorManager cursorManager,
			final CoordinateSystem coordinateSystem);

	protected abstract void onRender(Graphics2D g, CoordinateSystem coordinateSystem);

	protected abstract void doAction(MouseEvent e, CoordinateSystem coordinateSystem, SelectionManager selectionManager,
			Point2D.Double startingClick, Point2D.Double endingClick);

	protected abstract void onSelect(MouseEvent e, CoordinateSystem coordinateSystem,
			SelectionManager selectionManager);

	protected abstract void doStartAction(MouseEvent e, CoordinateSystem coordinateSystem,
			SelectionManager selectionManager, Point2D.Double startingClick);

	@Override
	public void render(final Graphics2D g) {
		if (startingClick != null && endingClick != null && actionType == ActionType.SELECT) {
			g.setColor(Color.RED);
			// final double minX =
			// Math.min(coordinateSystem.convertX(startingClick.x),
			// coordinateSystem.convertX(endingClick.x));
			// final double minY =
			// Math.min(coordinateSystem.convertY(startingClick.y),
			// coordinateSystem.convertY(endingClick.y));
			// final double maxX =
			// Math.max(coordinateSystem.convertX(startingClick.x),
			// coordinateSystem.convertX(endingClick.x));
			// final double maxY =
			// Math.max(coordinateSystem.convertY(startingClick.y),
			// coordinateSystem.convertY(endingClick.y));
			final double minX = Math.min(startingClick.x, endingClick.x);
			final double minY = Math.min(startingClick.y, endingClick.y);
			final double maxX = Math.max(startingClick.x, endingClick.x);
			final double maxY = Math.max(startingClick.y, endingClick.y);
			g.drawRect((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY));
		}
		onRender(g, coordinateSystem);
	}

	private static enum ActionType {
		SELECT, EDIT
	};
}
