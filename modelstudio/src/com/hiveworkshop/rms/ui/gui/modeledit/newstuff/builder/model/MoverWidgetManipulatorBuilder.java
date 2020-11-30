package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model;

import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget.MoveDirection;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveXManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveYManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;

public final class MoverWidgetManipulatorBuilder extends AbstractSelectAndEditModelEditorManipulatorBuilder {
	private final MoverWidget moverWidget = new MoverWidget(new Vec3(0, 0, 0));

	public MoverWidgetManipulatorBuilder(final ModelEditor modelEditor,
										 final ViewportSelectionHandler viewportSelectionHandler,
										 final ProgramPreferences programPreferences,
										 final ModelView modelView) {
		super(viewportSelectionHandler, programPreferences, modelEditor, modelView);
	}

	@Override
	protected boolean widgetOffersEdit(final Vec3 selectionCenter,
									   final Point mousePoint,
									   final CoordinateSystem coordinateSystem,
									   final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		final MoveDirection directionByMouse = moverWidget.getDirectionByMouse(
				mousePoint, coordinateSystem,coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDirection.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(final Vec3 selectionCenter,
													  final Point mousePoint,
                                                      final CoordinateSystem coordinateSystem,
													  final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		final MoveDirection directionByMouse = moverWidget.getDirectionByMouse(
				mousePoint, coordinateSystem, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		if (directionByMouse != null) {
			moverWidget.setMoveDirection(directionByMouse);
		}
		if (directionByMouse != null) {
			return switch (directionByMouse) {
				case BOTH -> new MoveManipulator(getModelEditor());
				case RIGHT -> new MoveXManipulator(getModelEditor());
				case UP -> new MoveYManipulator(getModelEditor());
				case NONE -> null;
			};
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(final Vec3 selectionCenter,
												   final Point mousePoint,
												   final CoordinateSystem coordinateSystem,
												   final SelectionView selectionView) {
		return new MoveManipulator(getModelEditor());
	}

	@Override
	protected void renderWidget(final Graphics2D graphics,
								final CoordinateSystem coordinateSystem,
								final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		moverWidget.render(graphics, coordinateSystem);
	}

}
