package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.ExtendManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public final class ExtendWidgetManipulatorBuilder extends ModelEditorManipulatorBuilder {
//	private final MoverWidget widget = new MoverWidget();

	public ExtendWidgetManipulatorBuilder(ModelEditorManager modelEditorManager, ModelHandler modelHandler) {
		super(modelEditorManager, modelHandler);
		widget = new MoverWidget();
	}

	@Override
	protected Manipulator createManipulatorFromWidget(Vec3 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		widget.setPoint(selectionView.getCenter());
		MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, coordinateSystem);

		widget.setMoveDirection(directionByMouse);
		if (directionByMouse != MoveDimension.NONE) {
			return new ExtendManipulator(getModelEditor(), directionByMouse);
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(Vec3 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		return new ExtendManipulator(getModelEditor(), MoveDimension.XYZ);
	}

}
