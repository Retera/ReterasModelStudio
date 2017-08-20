package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelChangeNotifier;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ScaleComponentAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItem;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItemView;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.selection.edits.UniqueComponentSpecificScaling;
import com.hiveworkshop.wc3.gui.modeledit.useractions.MoverWidget.MoveDirection;
import com.hiveworkshop.wc3.mdl.Vertex;

public class SelectAndScaleActivity extends AbstractSelectAndEditActivity
		implements SelectionListener, ModelChangeListener {
	private MoverWidget moverWidget = null;
	private CursorManager cursorManager;
	private UndoManager undoManager;
	private Vertex moveActionVector;
	private Vertex moveActionPreviousVector;
	private Point2D.Double previousEndingClick = null;
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
		moveActionVector.setCoord(coordinateSystem.getPortFirstXYZ(), endingClick.x - startingClick.x);
		moveActionVector.setCoord(coordinateSystem.getPortSecondXYZ(), endingClick.y - startingClick.y);
		final Vertex v = getCenter(selectionManager.getSelection());
		double cxs = v.getCoord(coordinateSystem.getPortFirstXYZ());
		double cys = v.getCoord(coordinateSystem.getPortSecondXYZ());
		double czs = 0;
		final Vertex scaleValues = computeScaleFactor(coordinateSystem, previousEndingClick, endingClick, cxs, cys);
		cxs = v.getCoord((byte) 0);
		cys = v.getCoord((byte) 1);
		czs = v.getCoord((byte) 2);
		final UniqueComponentSpecificScaling callback = new UniqueComponentSpecificScaling().resetValues((float) cxs,
				(float) cys, (float) czs, (float) (scaleValues.x), (float) (scaleValues.y), (float) (scaleValues.z));
		for (final SelectionItem item : selectionManager.getSelection()) {
			item.forEachComponent(callback);
		}
		previousEndingClick.x = endingClick.x;
		previousEndingClick.y = endingClick.y;
	}

	@Override
	protected void doEndAction(final MouseEvent e, final CoordinateSystem coordinateSystem,
			final SelectionManager selectionManager, final Double startingClick, final Double endingClick) {
		moveActionPreviousVector.setTo(moveActionVector);
		moveActionVector.setCoord(coordinateSystem.getPortFirstXYZ(), endingClick.x - startingClick.x);
		moveActionVector.setCoord(coordinateSystem.getPortSecondXYZ(), endingClick.y - startingClick.y);
		final Vertex v = getCenter(selectionManager.getSelection());
		final double cxs = v.getCoord(coordinateSystem.getPortFirstXYZ());
		final double cys = v.getCoord(coordinateSystem.getPortSecondXYZ());
		final double czs = 0;
		Vertex scaleValues = computeScaleFactor(coordinateSystem, previousEndingClick, endingClick, cxs, cys);
		final double cx = v.getCoord((byte) 0);
		final double cy = v.getCoord((byte) 1);
		final double cz = v.getCoord((byte) 2);
		final UniqueComponentSpecificScaling callback = new UniqueComponentSpecificScaling().resetValues((float) cx,
				(float) cy, (float) cz, (float) (scaleValues.x), (float) (scaleValues.y), (float) (scaleValues.z));
		for (final SelectionItem item : selectionManager.getSelection()) {
			item.forEachComponent(callback);
		}
		scaleValues = computeScaleFactor(coordinateSystem, startingClick, endingClick, cxs, cys);
		final ScaleComponentAction modifyAction = new ScaleComponentAction(selectionManager.getSelection(), (float) cx,
				(float) cy, (float) cz, (float) (scaleValues.x), (float) (scaleValues.y), (float) (scaleValues.z));
		undoManager.pushAction(modifyAction);
		previousEndingClick = null;
	}

	private Vertex computeScaleFactor(final CoordinateSystem coordinateSystem, final Double startingClick,
			final Double endingClick, final double cxs, final double cys) {
		double dxs = endingClick.x - cxs;
		double dys = endingClick.y - cys;
		final double dzs = 0;
		final double endDist = Math.sqrt(dxs * dxs + dys * dys);
		dxs = startingClick.x - cxs;
		dys = startingClick.y - cys;
		final double startDist = Math.sqrt(dxs * dxs + dys * dys);
		final double distRatio = endDist / startDist;
		Vertex scaleValues;
		if (currentDirection == MoveDirection.BOTH || currentDirection == MoveDirection.NONE) {
			scaleValues = new Vertex(distRatio, distRatio, distRatio);
		} else {
			scaleValues = new Vertex(1, 1, 1);
		}
		if (currentDirection == MoverWidget.MoveDirection.RIGHT) {
			scaleValues.setCoord(coordinateSystem.getPortFirstXYZ(), distRatio);
		}
		if (currentDirection == MoverWidget.MoveDirection.UP) {
			scaleValues.setCoord(coordinateSystem.getPortSecondXYZ(), distRatio);
		}
		return scaleValues;
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
			previousEndingClick = new Point2D.Double(startingClick.x, startingClick.y);
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
		if (selectionManager.getSelection().isEmpty()) {
			moverWidget = null;
		} else {
			moverWidget = new MoverWidget(getCenter(selectionManager.getSelection()));
		}
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
		if (moverWidget == null && selectionManager != null) {
			final List<SelectionItem> selection = selectionManager.getSelection();
			moverWidget = new MoverWidget(getCenter(selection));
		}
	}

	private Vertex getCenter(final List<? extends SelectionItemView> items) {
		final List<Vertex> centers = new ArrayList<>();
		for (final SelectionItemView item : items) {
			centers.add(item.getCenter());
		}
		return Vertex.centerOfGroup(centers);
	}
}
