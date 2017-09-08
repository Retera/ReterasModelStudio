package com.hiveworkshop.wc3.gui.modeledit.manipulator.builder;

import java.awt.Graphics2D;
import java.awt.Point;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.ViewportSelectionHandler;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.activity.Manipulator;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.activity.ScaleManipulator;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.activity.ScaleXManipulator;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.activity.ScaleYManipulator;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.gui.modeledit.useractions.widgets.MoverWidget;
import com.hiveworkshop.wc3.gui.modeledit.useractions.widgets.MoverWidget.MoveDirection;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class ScaleWidgetManipulatorBuilder extends AbstractSelectAndEditManipulatorBuilder {
	private final MoverWidget moverWidget = new MoverWidget(new Vertex(0, 0, 0));

	public ScaleWidgetManipulatorBuilder(final ModelEditor modelEditor,
			final ViewportSelectionHandler viewportSelectionHandler, final ProgramPreferences programPreferences) {
		super(viewportSelectionHandler, programPreferences, modelEditor);
	}

	@Override
	protected boolean widgetOffersEdit(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		final MoveDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem,
				coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDirection.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		final MoveDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem,
				coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		if (directionByMouse != null) {
			moverWidget.setMoveDirection(directionByMouse);
		}
		switch (directionByMouse) {
		case BOTH:
			return new ScaleManipulator(getModelEditor(), selectionView);
		case RIGHT:
			return new ScaleXManipulator(getModelEditor(), selectionView);
		case UP:
			return new ScaleYManipulator(getModelEditor(), selectionView);
		case NONE:
			return null;
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		return new ScaleManipulator(getModelEditor(), selectionView);
	}

	@Override
	public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		moverWidget.render(graphics, coordinateSystem);
	}

}
