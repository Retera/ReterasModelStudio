package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.RotatorWidget;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv.RotateTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;

public final class RotatorWidgetTVertexEditorManipulatorBuilder extends TVertexEditorManipulatorBuilder {
//	private final RotatorWidget widget = new RotatorWidget();

	public RotatorWidgetTVertexEditorManipulatorBuilder(TVertexEditor modelEditor, ViewportSelectionHandler viewportSelectionHandler, ModelView modelView) {
		super(viewportSelectionHandler, modelEditor, modelView);
		widget = new RotatorWidget();
	}

	@Override
	protected Manipulator createManipulatorFromWidget(Vec2 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		widget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, coordinateSystem);

		widget.setMoveDirection(directionByMouse);
		if (directionByMouse != MoveDimension.NONE) {
			return new RotateTVertexManipulator(getModelEditor(), selectionView, directionByMouse);
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(Vec2 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		return new RotateTVertexManipulator(getModelEditor(), selectionView, MoveDimension.XYZ);
	}
}
