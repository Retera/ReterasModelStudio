package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.RotatorWidget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.RotateManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public final class RotatorWidgetManipulatorBuilder extends ModelEditorManipulatorBuilder {
	private final RotatorWidget moverWidget = new RotatorWidget(new Vec3(0, 0, 0));

	public RotatorWidgetManipulatorBuilder(ModelEditorManager modelEditorManager, ModelHandler modelHandler, ProgramPreferences programPreferences) {
		super(modelEditorManager, modelHandler, programPreferences);
	}

	@Override
	protected boolean widgetOffersEdit(Vec3 selectionCenter, Point mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		MoveDimension directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem);
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDimension.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(Vec3 selectionCenter, Point mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		MoveDimension directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem);

		moverWidget.setMoveDirection(directionByMouse);
		if (directionByMouse != MoveDimension.NONE) {
			return new RotateManipulator(getModelEditor(), selectionView, directionByMouse);
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(Vec3 selectionCenter, Point mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		return new RotateManipulator(getModelEditor(), selectionView, MoveDimension.XYZ);
	}

	@Override
	protected void renderWidget(Graphics2D graphics, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		moverWidget.render(graphics, coordinateSystem);
	}

}
