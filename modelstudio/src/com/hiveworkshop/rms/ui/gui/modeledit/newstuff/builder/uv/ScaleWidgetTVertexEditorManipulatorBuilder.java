package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.application.edit.uv.widgets.TVertexScalerWidget;
import com.hiveworkshop.rms.ui.application.edit.uv.widgets.TVertexScalerWidget.ScaleDirection;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv.*;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public final class ScaleWidgetTVertexEditorManipulatorBuilder extends AbstractSelectAndEditTVertexEditorManipulatorBuilder {
	private final TVertexScalerWidget moverWidget = new TVertexScalerWidget(new Vec2(0, 0));

	public ScaleWidgetTVertexEditorManipulatorBuilder(final TVertexEditor modelEditor, final ViewportSelectionHandler viewportSelectionHandler, final ProgramPreferences programPreferences, final ModelView modelView) {
		super(viewportSelectionHandler, programPreferences, modelEditor, modelView);
	}

	@Override
	protected boolean widgetOffersEdit(final Vec2 selectionCenter, final Point mousePoint, final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		final ScaleDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != ScaleDirection.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(final Vec2 selectionCenter, final Point mousePoint, final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		final ScaleDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		if (directionByMouse != null) {
			moverWidget.setMoveDirection(directionByMouse);
		}
		return switch (directionByMouse) {
			case XYZ -> new ScaleTVertexManipulatorUsesYMouseDrag(getModelEditor(), selectionView);
			case FLAT_XY -> new ScaleXYTVertexManipulator(getModelEditor(), selectionView);
			case RIGHT -> new ScaleXTVertexManipulator(getModelEditor(), selectionView);
			case UP -> new ScaleYTVertexManipulator(getModelEditor(), selectionView);
			case NONE -> null;
		};
	}

	@Override
	protected Manipulator createDefaultManipulator(final Vec2 selectionCenter, final Point mousePoint, final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		return new ScaleTVertexManipulator(getModelEditor(), selectionView);
	}

	@Override
	protected void renderWidget(final Graphics2D graphics, final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		moverWidget.render(graphics, coordinateSystem);
	}

}
