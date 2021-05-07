package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv.MoveTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;

public final class MoverWidgetTVertexEditorManipulatorBuilder extends TVertexEditorManipulatorBuilder {
//	private final MoverWidget widget = new MoverWidget();

	public MoverWidgetTVertexEditorManipulatorBuilder(TVertexEditor modelEditor, ViewportSelectionHandler viewportSelectionHandler, ModelView modelView) {
		super(viewportSelectionHandler, modelEditor, modelView);
		widget = new MoverWidget();
	}

	@Override
	protected Manipulator createManipulatorFromWidget(Vec2 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		widget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, coordinateSystem);

		widget.setMoveDirection(directionByMouse);
		if (directionByMouse != MoveDimension.NONE) {
			return new MoveTVertexManipulator(getModelEditor(), directionByMouse);
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(Vec2 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		return new MoveTVertexManipulator(getModelEditor(), MoveDimension.XY);
	}
}
