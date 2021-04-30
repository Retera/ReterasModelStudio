package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public final class MoverWidgetManipulatorBuilder extends ModelEditorManipulatorBuilder {
	private final MoverWidget moverWidget = new MoverWidget(new Vec3(0, 0, 0));

	public MoverWidgetManipulatorBuilder(ModelEditor modelEditor, ViewportSelectionHandler viewportSelectionHandler, ProgramPreferences programPreferences, ModelView modelView) {
		super(viewportSelectionHandler, programPreferences, modelEditor, modelView);
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
			return new MoveManipulator(getModelEditor(), directionByMouse);
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(Vec3 selectionCenter, Point mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		return new MoveManipulator(getModelEditor(), MoveDimension.XYZ);
	}

	@Override
	protected void renderWidget(Graphics2D graphics, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		moverWidget.render(graphics, coordinateSystem);
	}

}
