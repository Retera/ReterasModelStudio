package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelChangeNotifier;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.MoveComponentAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItem;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItemView;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.selection.edits.UniqueComponentTranslation;
import com.hiveworkshop.wc3.gui.modeledit.useractions.MoverWidget.MoveDirection;
import com.hiveworkshop.wc3.mdl.Vertex;

public class SelectAndMoveActivity extends AbstractSelectAndEditActivity
		implements SelectionListener, ModelChangeListener {
	private MoverWidget moverWidget = null;
	private CursorManager cursorManager;
	private UndoManager undoManager;
	private Vertex moveActionVector;
	private Vertex moveActionPreviousVector;
	private MoveDirection currentDirection = null;
	private SelectionManager selectionManager;

	@Override
	protected void doMouseMove(final MouseEvent e, final CoordinateSystem coordinateSystem,
			final SelectionManager selectionManager) {
		if (moverWidget != null) {
			final MoveDirection directionByMouse = moverWidget.getDirectionByMouse(e.getPoint(), coordinateSystem,
					coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
			if (directionByMouse != MoverWidget.MoveDirection.NONE) {
				cursorManager.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				moverWidget.setMoveDirection(directionByMouse);
			} else {
				// cursorManager.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	@Override
	protected void doDrag(final MouseEvent e, final CoordinateSystem coordinateSystem,
			final SelectionManager selectionManager, final Double startingClick, final Double endingClick) {
		moveActionPreviousVector.setTo(moveActionVector);
		if (currentDirection != MoverWidget.MoveDirection.UP) {
			moveActionVector.setCoord(coordinateSystem.getPortFirstXYZ(), endingClick.x - startingClick.x);
		}
		if (currentDirection != MoverWidget.MoveDirection.RIGHT) {
			moveActionVector.setCoord(coordinateSystem.getPortSecondXYZ(), endingClick.y - startingClick.y);
		}
		final UniqueComponentTranslation callback = new UniqueComponentTranslation(
				(float) (moveActionVector.x - moveActionPreviousVector.x),
				(float) (moveActionVector.y - moveActionPreviousVector.y),
				(float) (moveActionVector.z - moveActionPreviousVector.z));
		for (final SelectionItem item : selectionManager.getSelection()) {
			item.forEachComponent(callback);
		}
	}

	@Override
	protected void doEndAction(final MouseEvent e, final CoordinateSystem coordinateSystem,
			final SelectionManager selectionManager, final Double startingClick, final Double endingClick) {
		moveActionPreviousVector.setTo(moveActionVector);
		if (currentDirection != MoverWidget.MoveDirection.UP) {
			moveActionVector.setCoord(coordinateSystem.getPortFirstXYZ(), endingClick.x - startingClick.x);
			moverWidget.getPoint().setCoord(coordinateSystem.getPortFirstXYZ(),
					moverWidget.getPoint().getCoord(coordinateSystem.getPortFirstXYZ()) + endingClick.x
							- startingClick.x);
		}
		if (currentDirection != MoverWidget.MoveDirection.RIGHT) {
			moveActionVector.setCoord(coordinateSystem.getPortSecondXYZ(), endingClick.y - startingClick.y);
			moverWidget.getPoint().setCoord(coordinateSystem.getPortSecondXYZ(),
					moverWidget.getPoint().getCoord(coordinateSystem.getPortSecondXYZ()) + endingClick.y
							- startingClick.y);
		}
		final UniqueComponentTranslation callback = new UniqueComponentTranslation(
				(float) (moveActionVector.x - moveActionPreviousVector.x),
				(float) (moveActionVector.y - moveActionPreviousVector.y),
				(float) (moveActionVector.z - moveActionPreviousVector.z));
		for (final SelectionItem item : selectionManager.getSelection()) {
			item.forEachComponent(callback);
		}
		undoManager.pushAction(new MoveComponentAction(selectionManager.getSelection(), moveActionVector));
	}

	@Override
	protected boolean doStartAction(final MouseEvent e, final CoordinateSystem coordinateSystem,
			final SelectionManager selectionManager, final Double startingClick) {
		boolean doAction = false;
		if (SwingUtilities.isRightMouseButton(e)) {
			currentDirection = MoveDirection.NONE;
			doAction = true;
		} else if ((moverWidget != null && (currentDirection = moverWidget.getDirectionByMouse(e.getPoint(),
				coordinateSystem, coordinateSystem.getPortFirstXYZ(),
				coordinateSystem.getPortSecondXYZ())) != MoverWidget.MoveDirection.NONE)) {
			doAction = true;
		}
		if (doAction) {
			moveActionVector = new Vertex(0, 0, 0);
			moveActionPreviousVector = new Vertex(0, 0, 0);
			return true;
		}
		return false;
	}

	@Override
	protected void onReset(final SelectionManager selectionManager, final CursorManager cursorManager,
			final CoordinateSystem coordinateSystem, final UndoManager undoManager,
			final ModelChangeNotifier modelChangeNotifier) {
		this.selectionManager = selectionManager;
		modelChangeNotifier.subscribe(this);
		this.cursorManager = cursorManager;
		this.undoManager = undoManager;
		selectionManager.addSelectionListener(this);
	}

	@Override
	protected void onRender(final Graphics2D g, final CoordinateSystem coordinateSystem) {
		if (moverWidget != null) {
			moverWidget.render(g, coordinateSystem);
		}
	}

	@Override
	public void onSelectionChanged(final List<? extends SelectionItemView> previousSelection,
			final List<? extends SelectionItemView> newSelection) {
		if (newSelection.isEmpty()) {
			moverWidget = null;
		} else {
			moverWidget = new MoverWidget(getCenter(newSelection));
		}
	}

	@Override
	public void modelChanged() {
		final List<SelectionItem> selection = selectionManager.getSelection();
		moverWidget = new MoverWidget(getCenter(selection));
	}

	private Vertex getCenter(final List<? extends SelectionItemView> items) {
		final List<Vertex> centers = new ArrayList<>();
		for (final SelectionItemView item : items) {
			centers.add(item.getCenter());
		}
		return Vertex.centerOfGroup(centers);
	}
}
