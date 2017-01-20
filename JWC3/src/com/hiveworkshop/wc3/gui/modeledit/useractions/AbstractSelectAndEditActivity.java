package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelChangeNotifier;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItem;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionTypeApplicator;

public abstract class AbstractSelectAndEditActivity implements ViewportActivity {
	private SelectionManager selectionManager;
	private Point2D.Double startingClick;
	private Point2D.Double endingClick;
	private CoordinateSystem coordinateSystem;
	private ActionType actionType;
	private SelectionTypeApplicator selectionApplicator;
	private CursorManager cursorManager;
	private UndoManager undoManager;

	@Override
	public AbstractSelectAndEditActivity reset(final SelectionManager selectionManager,
			final SelectionTypeApplicator selectionListener, final CursorManager cursorManager,
			final CoordinateSystem coordinateSystem, final UndoManager undoManager,
			final ModelChangeNotifier modelChangeNotifier) {
		this.selectionManager = selectionManager;
		this.selectionApplicator = selectionListener;
		this.cursorManager = cursorManager;
		this.coordinateSystem = coordinateSystem;
		this.undoManager = undoManager;
		startingClick = null;
		actionType = null;
		onReset(selectionManager, cursorManager, coordinateSystem, undoManager, modelChangeNotifier);
		return this;
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		startingClick = new Point2D.Double(coordinateSystem.geomX(e.getPoint().getX()),
				coordinateSystem.geomY(e.getPoint().getY()));
		// startingClick = new Point2D.Double(e.getPoint().getX(),
		// e.getPoint().getY());
		if (doStartAction(e, coordinateSystem, selectionManager, startingClick)) {
			actionType = ActionType.EDIT;
		} else {
			if (SwingUtilities.isLeftMouseButton(e)) {
				actionType = ActionType.SELECT;
			}
		}
	}

	@Override
	public final void mouseReleased(final MouseEvent e) {
		endingClick = new Point2D.Double(coordinateSystem.geomX(e.getPoint().getX()),
				coordinateSystem.geomY(e.getPoint().getY()));
		// endingClick = new Point2D.Double(e.getPoint().getX(),
		// e.getPoint().getY());
		if (actionType == ActionType.SELECT) {
			if (startingClick != null) {
				if (endingClick != null) {

					final double minX = Math.min(startingClick.x, endingClick.x);
					final double minY = Math.min(startingClick.y, endingClick.y);
					final double maxX = Math.max(startingClick.x, endingClick.x);
					final double maxY = Math.max(startingClick.y, endingClick.y);
					final Rectangle2D area = new Rectangle2D.Double(minX, minY, (maxX - minX), (maxY - minY));
					final List<SelectionItem> selectedItems = new ArrayList<>();
					for (final SelectionItem item : selectionManager.getSelectableItems()) {
						if (item.hitTest(startingClick, coordinateSystem) || item.hitTest(endingClick, coordinateSystem)
								|| item.hitTest(area, coordinateSystem)) {
							selectedItems.add(item);
						}
					}
					selectionApplicator.chooseGroup(selectedItems);
				}
			}
		} else {
			doEndAction(e, coordinateSystem, selectionManager, startingClick, endingClick);
		}
		startingClick = null;
		endingClick = null;
		if (cursorManager != null) {
			cursorManager.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		if (!SwingUtilities.isMiddleMouseButton(e)) {
			// endingClick = new Point2D.Double(e.getPoint().getX(),
			// e.getPoint().getY());
			endingClick = new Point2D.Double(coordinateSystem.geomX(e.getPoint().getX()),
					coordinateSystem.geomY(e.getPoint().getY()));
			if (actionType == ActionType.SELECT) {
			} else {
				doDrag(e, coordinateSystem, selectionManager, startingClick, endingClick);
			}
		} else {
			if (cursorManager != null) {
				cursorManager.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
		}
	}

	@Override
	public final void mouseMoved(final MouseEvent e) {
		if (coordinateSystem != null) {
			final Point2D.Double mousePoint = new Point2D.Double(coordinateSystem.geomX(e.getPoint().getX()),
					coordinateSystem.geomY(e.getPoint().getY()));
			boolean hitAnItem = false;
			for (final SelectionItem item : selectionManager.getSelectableItems()) {
				if (item.hitTest(mousePoint, coordinateSystem)) {
					hitAnItem = true;
					break;
				}
			}
			if (hitAnItem) {
				cursorManager.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			} else {
				cursorManager.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
		doMouseMove(e, coordinateSystem, selectionManager);
	}

	protected abstract void doDrag(MouseEvent e, CoordinateSystem coordinateSystem, SelectionManager selectionManager,
			Point2D.Double startingClick, Point2D.Double endingClick);

	protected abstract void doMouseMove(MouseEvent e, CoordinateSystem coordinateSystem,
			SelectionManager selectionManager);

	protected abstract void onReset(final SelectionManager selectionManager, final CursorManager cursorManager,
			final CoordinateSystem coordinateSystem, UndoManager undoManager, ModelChangeNotifier modelChangeNotifier);

	protected abstract void onRender(Graphics2D g, CoordinateSystem coordinateSystem);

	protected abstract void doEndAction(MouseEvent e, CoordinateSystem coordinateSystem,
			SelectionManager selectionManager, Point2D.Double startingClick, Point2D.Double endingClick);

	protected abstract boolean doStartAction(MouseEvent e, CoordinateSystem coordinateSystem,
			SelectionManager selectionManager, Point2D.Double startingClick);

	@Override
	public void render(final Graphics2D g) {
		if (startingClick != null && endingClick != null && actionType == ActionType.SELECT) {
			g.setColor(Color.RED);
			final double minX = Math.min(coordinateSystem.convertX(startingClick.x),
					coordinateSystem.convertX(endingClick.x));
			final double minY = Math.min(coordinateSystem.convertY(startingClick.y),
					coordinateSystem.convertY(endingClick.y));
			final double maxX = Math.max(coordinateSystem.convertX(startingClick.x),
					coordinateSystem.convertX(endingClick.x));
			final double maxY = Math.max(coordinateSystem.convertY(startingClick.y),
					coordinateSystem.convertY(endingClick.y));
			// final double minX = Math.min(startingClick.x, endingClick.x);
			// final double minY = Math.min(startingClick.y, endingClick.y);
			// final double maxX = Math.max(startingClick.x, endingClick.x);
			// final double maxY = Math.max(startingClick.y, endingClick.y);
			g.drawRect((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY));
		}
		onRender(g, coordinateSystem);
	}

	private static enum ActionType {
		SELECT, EDIT
	};
}
