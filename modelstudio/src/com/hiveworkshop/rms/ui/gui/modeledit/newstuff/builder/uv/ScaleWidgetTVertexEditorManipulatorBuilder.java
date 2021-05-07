package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.ScalerWidget;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv.ScaleTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;

public final class ScaleWidgetTVertexEditorManipulatorBuilder extends TVertexEditorManipulatorBuilder {
//	private final ScalerWidget widget = new ScalerWidget();

	public ScaleWidgetTVertexEditorManipulatorBuilder(TVertexEditor modelEditor, ViewportSelectionHandler viewportSelectionHandler, ModelView modelView) {
		super(viewportSelectionHandler, modelEditor, modelView);
		widget = new ScalerWidget();
	}

	@Override
	protected Manipulator createManipulatorFromWidget(Vec2 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		widget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, coordinateSystem);

		widget.setMoveDirection(directionByMouse);
		if (directionByMouse != MoveDimension.NONE) {
			return new ScaleTVertexManipulator(getModelEditor(), selectionView, directionByMouse);
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(Vec2 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		return new ScaleTVertexManipulator(getModelEditor(), selectionView, MoveDimension.XYZ);
	}
}
