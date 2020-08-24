package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.ManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

public abstract class MultiManipulatorActivity<MANIPULATOR_BUILDER extends ManipulatorBuilder>
		implements ViewportActivity {
	protected final MANIPULATOR_BUILDER manipulatorBuilder;
	private final UndoActionListener undoActionListener;
	private Manipulator manipulator;
	private CursorManager cursorManager;
	private Double mouseStartPoint;
	private Double lastDragPoint;
	private SelectionView selectionView;

	public MultiManipulatorActivity(final MANIPULATOR_BUILDER manipulatorBuilder,
			final UndoActionListener undoActionListener, final SelectionView selectionView) {
		this.manipulatorBuilder = manipulatorBuilder;
		this.undoActionListener = undoActionListener;
		this.selectionView = selectionView;
	}

	@Override
	public void onSelectionChanged(final SelectionView newSelection) {
		this.selectionView = newSelection;
	}

	@Override
	public void viewportChanged(final CursorManager cursorManager) {
		this.cursorManager = cursorManager;
	}

	@Override
	public void mousePressed(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		final ButtonType buttonType;
		if (SwingUtilities.isRightMouseButton(e)) {
			buttonType = ButtonType.RIGHT_MOUSE;
		} else if (SwingUtilities.isMiddleMouseButton(e)) {
			buttonType = ButtonType.MIDDLE_MOUSE;
		} else {
			buttonType = ButtonType.LEFT_MOUSE;
		}
		manipulator = manipulatorBuilder.buildActivityListener(e.getX(), e.getY(), buttonType, coordinateSystem,
				selectionView);
		if (manipulator != null) {
			mouseStartPoint = new Point2D.Double(coordinateSystem.geomX(e.getPoint().getX()),
					coordinateSystem.geomY(e.getPoint().getY()));
			manipulator.start(mouseStartPoint, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
			lastDragPoint = mouseStartPoint;
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		if (manipulator != null) {
			final Point2D.Double mouseEnd = new Point2D.Double(coordinateSystem.geomX(e.getPoint().getX()),
					coordinateSystem.geomY(e.getPoint().getY()));
			undoActionListener.pushAction(manipulator.finish(lastDragPoint, mouseEnd,
					coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ()));
			mouseStartPoint = null;
			lastDragPoint = null;
			manipulator = null;
		}
	}

	@Override
	public void mouseMoved(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		cursorManager.setCursor(manipulatorBuilder.getCursorAt(e.getX(), e.getY(), coordinateSystem, selectionView));
	}

	@Override
	public void mouseDragged(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		if (manipulator != null) {
			final Point2D.Double mouseEnd = new Point2D.Double(coordinateSystem.geomX(e.getPoint().getX()),
					coordinateSystem.geomY(e.getPoint().getY()));
			manipulator.update(lastDragPoint, mouseEnd, coordinateSystem.getPortFirstXYZ(),
					coordinateSystem.getPortSecondXYZ());
			lastDragPoint = mouseEnd;
		}
	}

	@Override
	public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final RenderModel renderModel) {
		manipulatorBuilder.render(graphics, coordinateSystem, selectionView, renderModel);
		if (manipulator != null) {
			manipulator.render(graphics, coordinateSystem);
		}
	}

	@Override
	public void renderStatic(final Graphics2D graphics, final CoordinateSystem coordinateSystem) {
		manipulatorBuilder.renderStatic(graphics, coordinateSystem, selectionView);
		if (manipulator != null) {
			manipulator.render(graphics, coordinateSystem);
		}
	}

	@Override
	public boolean isEditing() {
		return manipulator != null;
	}

	@Override
	public void modelChanged() {
		// TODO Auto-generated method stub

	}

}
