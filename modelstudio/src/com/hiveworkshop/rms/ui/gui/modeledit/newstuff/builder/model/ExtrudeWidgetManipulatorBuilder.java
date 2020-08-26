package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model;

import com.hiveworkshop.rms.util.Vector3;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget.MoveDirection;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.ExtrudeManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.ExtrudeXManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.ExtrudeYManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;

public final class ExtrudeWidgetManipulatorBuilder extends AbstractSelectAndEditModelEditorManipulatorBuilder {
	private final MoverWidget moverWidget = new MoverWidget(new Vector3(0, 0, 0));

	public ExtrudeWidgetManipulatorBuilder(final ModelEditor modelEditor,
			final ViewportSelectionHandler viewportSelectionHandler, final ProgramPreferences programPreferences,
			final ModelView modelView) {
		super(viewportSelectionHandler, programPreferences, modelEditor, modelView);
	}

	@Override
	protected boolean widgetOffersEdit(final Vector3 selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		final MoveDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem,
				coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDirection.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(final Vector3 selectionCenter, final Point mousePoint,
                                                      final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		final MoveDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem,
				coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		if (directionByMouse != null) {
			moverWidget.setMoveDirection(directionByMouse);
		}
		switch (directionByMouse) {
		case BOTH:
			return new ExtrudeManipulator(getModelEditor());
		case RIGHT:
			return new ExtrudeXManipulator(getModelEditor());
		case UP:
			return new ExtrudeYManipulator(getModelEditor());
		case NONE:
			return null;
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(final Vector3 selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		return new ExtrudeManipulator(getModelEditor());
	}

	@Override
	protected void renderWidget(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		moverWidget.render(graphics, coordinateSystem);
	}

}
