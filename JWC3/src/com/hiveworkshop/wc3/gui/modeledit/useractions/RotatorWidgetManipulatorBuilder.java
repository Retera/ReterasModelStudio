package com.hiveworkshop.wc3.gui.modeledit.useractions;

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
import com.hiveworkshop.wc3.gui.modeledit.useractions.RotatorWidget.RotateDirection;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public final class RotatorWidgetManipulatorBuilder extends AbstractSelectAndEditManipulatorBuilder {
	private final ModelEditor modelEditor;
	private final RotatorWidget moverWidget = new RotatorWidget(new Vertex(0, 0, 0));

	public RotatorWidgetManipulatorBuilder(final ModelEditor modelEditor,
			final ViewportSelectionHandler viewportSelectionHandler, final ProgramPreferences programPreferences) {
		super(viewportSelectionHandler, programPreferences);
		this.modelEditor = modelEditor;
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
			return new RotateManipulator(modelEditor, selectionView);
		case HORIZONTALLY:
			return new RotateHorizontalManipulator(modelEditor, selectionView);
		case VERTICALLY:
			return new RotateVerticalManipulator(modelEditor, selectionView);
		case SPIN:
			return new RotateManipulator(modelEditor, selectionView);
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		return new RotateManipulator(modelEditor, selectionView);
	}

	@Override
	protected void renderWidget(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final SelectionView selectionView, final ModelView modelView) {
		moverWidget.render(graphics, coordinateSystem);
	}

}
