package com.hiveworkshop.wc3.gui.modeledit.manipulator.builder;

import java.awt.Graphics2D;
import java.awt.Point;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.ViewportSelectionHandler;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.activity.Manipulator;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.activity.RotateHorizontalManipulator;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.activity.RotateManipulator;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.activity.RotateVerticalManipulator;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.gui.modeledit.useractions.widgets.RotatorWidget;
import com.hiveworkshop.wc3.gui.modeledit.useractions.widgets.RotatorWidget.RotateDirection;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class RotatorWidgetManipulatorBuilder extends AbstractSelectAndEditManipulatorBuilder {
	private final RotatorWidget moverWidget = new RotatorWidget(new Vertex(0, 0, 0));

	public RotatorWidgetManipulatorBuilder(final ModelEditor modelEditor,
			final ViewportSelectionHandler viewportSelectionHandler, final ProgramPreferences programPreferences) {
		super(viewportSelectionHandler, programPreferences, modelEditor);
	}

	@Override
	protected boolean widgetOffersEdit(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		final RotateDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem);
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != RotateDirection.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		final RotateDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem);
		if (directionByMouse != null) {
			moverWidget.setMoveDirection(directionByMouse);
		}
		switch (directionByMouse) {
		case FREE:
			return new RotateManipulator(getModelEditor(), selectionView);
		case HORIZONTALLY:
			return new RotateHorizontalManipulator(getModelEditor(), selectionView);
		case VERTICALLY:
			return new RotateVerticalManipulator(getModelEditor(), selectionView);
		case SPIN:
			return new RotateManipulator(getModelEditor(), selectionView);
		case NONE:
			return null;
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		return new RotateManipulator(getModelEditor(), selectionView);
	}

	@Override
	public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		moverWidget.render(graphics, coordinateSystem);
	}

}
