package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.ScalerWidget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.ScaleManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public final class ScaleWidgetManipulatorBuilder extends ModelEditorManipulatorBuilder {
//	private final ScalerWidget widget = new ScalerWidget();

	public ScaleWidgetManipulatorBuilder(ModelEditorManager modelEditorManager, ModelHandler modelHandler) {
		super(modelEditorManager, modelHandler);
		widget = new ScalerWidget();
	}

	@Override
	protected Manipulator createManipulatorFromWidget(Vec3 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		widget.setPoint(selectionView.getCenter());
		MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, coordinateSystem);

		widget.setMoveDirection(directionByMouse);
		if (directionByMouse != MoveDimension.NONE) {
			return new ScaleManipulator(getModelEditor(), selectionView, directionByMouse);
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(Vec3 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		return new ScaleManipulator(getModelEditor(), selectionView, MoveDimension.XYZ);
	}
}
