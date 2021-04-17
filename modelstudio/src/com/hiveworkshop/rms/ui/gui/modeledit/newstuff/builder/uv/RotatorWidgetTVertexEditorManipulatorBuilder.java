package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.application.edit.uv.widgets.TVertexRotatorWidget;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv.RotateTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public final class RotatorWidgetTVertexEditorManipulatorBuilder extends TVertexEditorManipulatorBuilder {
	private final TVertexRotatorWidget moverWidget = new TVertexRotatorWidget(new Vec2(0, 0));

	public RotatorWidgetTVertexEditorManipulatorBuilder(final TVertexEditor modelEditor, final ViewportSelectionHandler viewportSelectionHandler, final ProgramPreferences programPreferences, final ModelView modelView) {
		super(viewportSelectionHandler, programPreferences, modelEditor, modelView);
	}

	@Override
	protected boolean widgetOffersEdit(final Vec2 selectionCenter, final Point mousePoint, final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		final MoveDimension directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem);
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDimension.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(final Vec2 selectionCenter, final Point mousePoint, final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		final MoveDimension directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem);

		moverWidget.setMoveDirection(directionByMouse);
		if (directionByMouse != MoveDimension.NONE) {
			return new RotateTVertexManipulator(getModelEditor(), selectionView, directionByMouse);
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(final Vec2 selectionCenter, final Point mousePoint, final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		return new RotateTVertexManipulator(getModelEditor(), selectionView, MoveDimension.XYZ);
	}

	@Override
	protected void renderWidget(final Graphics2D graphics, final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		moverWidget.render(graphics, coordinateSystem);
	}

}
