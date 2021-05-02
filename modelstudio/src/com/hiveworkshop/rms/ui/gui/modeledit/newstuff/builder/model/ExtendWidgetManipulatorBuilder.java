package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.ExtendManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public final class ExtendWidgetManipulatorBuilder extends ModelEditorManipulatorBuilder {
	private final MoverWidget moverWidget = new MoverWidget(new Vec3(0, 0, 0));

	public ExtendWidgetManipulatorBuilder(ModelEditorManager modelEditorManager, ModelHandler modelHandler,
	                                      ProgramPreferences programPreferences) {
		super(modelEditorManager, modelHandler, programPreferences);
	}

	@Override
	protected boolean widgetOffersEdit(Vec3 selectionCenter, Point mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		MoveDimension directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDimension.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(Vec3 selectionCenter, Point mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		MoveDimension directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());

		moverWidget.setMoveDirection(directionByMouse);
		if (directionByMouse != MoveDimension.NONE) {
			return new ExtendManipulator(getModelEditor(), directionByMouse);
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(Vec3 selectionCenter, Point mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		return new ExtendManipulator(getModelEditor(), MoveDimension.XYZ);
	}

	@Override
	protected void renderWidget(Graphics2D graphics, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		moverWidget.render(graphics, coordinateSystem);
	}

}
