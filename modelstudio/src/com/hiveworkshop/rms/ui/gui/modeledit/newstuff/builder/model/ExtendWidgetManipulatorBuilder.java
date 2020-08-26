package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model;

import com.hiveworkshop.rms.util.Vertex3;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget.MoveDirection;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.ExtendManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.ExtendXManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.ExtendYManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;

public final class ExtendWidgetManipulatorBuilder extends AbstractSelectAndEditModelEditorManipulatorBuilder {
	private final MoverWidget moverWidget = new MoverWidget(new Vertex3(0, 0, 0));

	public ExtendWidgetManipulatorBuilder(final ModelEditor modelEditor,
			final ViewportSelectionHandler viewportSelectionHandler, final ProgramPreferences programPreferences,
			final ModelView modelView) {
		super(viewportSelectionHandler, programPreferences, modelEditor, modelView);
	}

	@Override
	protected boolean widgetOffersEdit(final Vertex3 selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		final MoveDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem,
				coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDirection.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(final Vertex3 selectionCenter, final Point mousePoint,
                                                      final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		final MoveDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem,
				coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		if (directionByMouse != null) {
			moverWidget.setMoveDirection(directionByMouse);
		}
		switch (directionByMouse) {
		case BOTH:
			return new ExtendManipulator(getModelEditor());
		case RIGHT:
			return new ExtendXManipulator(getModelEditor());
		case UP:
			return new ExtendYManipulator(getModelEditor());
		case NONE:
			return null;
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(final Vertex3 selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		return new ExtendManipulator(getModelEditor());
	}

	@Override
	protected void renderWidget(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		moverWidget.render(graphics, coordinateSystem);
	}

}
