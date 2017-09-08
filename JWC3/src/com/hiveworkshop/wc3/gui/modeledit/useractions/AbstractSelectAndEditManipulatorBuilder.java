package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.ViewportSelectionHandler;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.activity.Manipulator;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.activity.SelectManipulator;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public abstract class AbstractSelectAndEditManipulatorBuilder implements ManipulatorBuilder {
	private final ViewportSelectionHandler viewportSelectionHandler;
	private final ProgramPreferences programPreferences;
	private final Graphics2DToModelElementRendererAdapter grAdapter;

	public AbstractSelectAndEditManipulatorBuilder(final ViewportSelectionHandler viewportSelectionHandler,
			final ProgramPreferences programPreferences) {
		this.viewportSelectionHandler = viewportSelectionHandler;
		this.programPreferences = programPreferences;
		grAdapter = new Graphics2DToModelElementRendererAdapter(programPreferences.getVertexSize());
	}

	@Override
	public final Cursor getCursorAt(final int x, final int y, final CoordinateSystem coordinateSystem,
			final SelectionView selectionView) {
		final Point mousePoint = new Point(x, y);
		if (widgetOffersEdit(selectionView.getCenter(), mousePoint, coordinateSystem, selectionView)) {
			return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		} else if (selectionView.canSelectAt(mousePoint, coordinateSystem)) {
			return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		}
		return null;
	}

	@Override
	public final Manipulator buildActivityListener(final int x, final int y, final ButtonType clickedButton,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		final Point mousePoint = new Point(x, y);
		if (clickedButton == ButtonType.RIGHT_MOUSE) {
			return createDefaultManipulator(selectionView.getCenter(), mousePoint, coordinateSystem, selectionView);
		} else {
			final Manipulator manipulatorFromWidget = createManipulatorFromWidget(selectionView.getCenter(), mousePoint,
					coordinateSystem, selectionView);
			if (manipulatorFromWidget != null) {
				return manipulatorFromWidget;
			} else {
				return new SelectManipulator(viewportSelectionHandler, programPreferences);
			}
		}
	}

	@Override
	public final void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final SelectionView selectionView, final ModelView modelView) {
		selectionView.renderSelection(grAdapter.reset(graphics, coordinateSystem), coordinateSystem, modelView,
				programPreferences);
		renderWidget(graphics, coordinateSystem, selectionView, modelView);
	}

	protected abstract boolean widgetOffersEdit(Vertex selectionCenter, Point mousePoint,
			CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected abstract Manipulator createManipulatorFromWidget(Vertex selectionCenter, Point mousePoint,
			CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected abstract Manipulator createDefaultManipulator(Vertex selectionCenter, Point mousePoint,
			CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected abstract void renderWidget(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final SelectionView selectionView, final ModelView modelView);

}
