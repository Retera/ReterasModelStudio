package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model;

import com.hiveworkshop.rms.util.Vector3;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.RotatorWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.RotatorWidget.RotateDirection;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.RotateHorizontalManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.RotateManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.RotateVerticalManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;

public final class RotatorWidgetManipulatorBuilder extends AbstractSelectAndEditModelEditorManipulatorBuilder {
	private final RotatorWidget moverWidget = new RotatorWidget(new Vector3(0, 0, 0));

	public RotatorWidgetManipulatorBuilder(final ModelEditor modelEditor,
										   final ViewportSelectionHandler viewportSelectionHandler, final ProgramPreferences programPreferences,
										   final ModelView modelView) {
		super(viewportSelectionHandler, programPreferences, modelEditor, modelView);
	}

	@Override
	protected boolean widgetOffersEdit(final Vector3 selectionCenter, final Point mousePoint,
									   final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		final RotateDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem);
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != RotateDirection.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(final Vector3 selectionCenter, final Point mousePoint,
													  final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
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
	protected Manipulator createDefaultManipulator(final Vector3 selectionCenter, final Point mousePoint,
												   final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		return new RotateManipulator(getModelEditor(), selectionView);
	}

	@Override
	protected void renderWidget(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
								final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		moverWidget.render(graphics, coordinateSystem);
	}

}
