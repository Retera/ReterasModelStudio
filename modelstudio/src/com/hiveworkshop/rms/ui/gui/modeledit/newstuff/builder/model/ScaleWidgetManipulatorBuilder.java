package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model;

import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.ScalerWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.ScalerWidget.ScaleDirection;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.*;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;

public final class ScaleWidgetManipulatorBuilder extends AbstractSelectAndEditModelEditorManipulatorBuilder {
	private final ScalerWidget moverWidget = new ScalerWidget(new Vec3(0, 0, 0));

	public ScaleWidgetManipulatorBuilder(final ModelEditor modelEditor,
			final ViewportSelectionHandler viewportSelectionHandler, final ProgramPreferences programPreferences,
			final ModelView modelView) {
		super(viewportSelectionHandler, programPreferences, modelEditor, modelView);
	}

	@Override
	protected boolean widgetOffersEdit(final Vec3 selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		final ScaleDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem,
				coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != ScaleDirection.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(final Vec3 selectionCenter, final Point mousePoint,
                                                      final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		final ScaleDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem,
				coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		if (directionByMouse != null) {
			moverWidget.setMoveDirection(directionByMouse);
		}
		switch (directionByMouse) {
		case XYZ:
			return new ScaleManipulatorUsesYMouseDrag(getModelEditor(), selectionView);
		case FLAT_XY:
			return new ScaleXYManipulator(getModelEditor(), selectionView);
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
	protected Manipulator createDefaultManipulator(final Vec3 selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		return new ScaleManipulator(getModelEditor(), selectionView);
	}

	@Override
	protected void renderWidget(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		moverWidget.render(graphics, coordinateSystem);
	}

}
