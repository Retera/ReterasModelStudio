package com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.uv;

import java.awt.Graphics2D;
import java.awt.Point;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ViewportSelectionHandler;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.uv.ScaleTVertexManipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.uv.ScaleTVertexManipulatorUsesYMouseDrag;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.uv.ScaleXTVertexManipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.uv.ScaleXYTVertexManipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.uv.ScaleYTVertexManipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexSelectionView;
import com.hiveworkshop.wc3.gui.modeledit.useractions.widgets.ScalerWidget;
import com.hiveworkshop.wc3.gui.modeledit.useractions.widgets.ScalerWidget.ScaleDirection;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public final class ScaleWidgetManipulatorBuilder extends AbstractSelectAndEditTVertexEditorManipulatorBuilder {
	private final ScalerWidget moverWidget = new ScalerWidget(new Vertex(0, 0, 0));

	public ScaleWidgetManipulatorBuilder(final TVertexEditor modelEditor,
			final ViewportSelectionHandler viewportSelectionHandler, final ProgramPreferences programPreferences,
			final ModelView modelView) {
		super(viewportSelectionHandler, programPreferences, modelEditor, modelView);
	}

	@Override
	protected boolean widgetOffersEdit(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final TVertexSelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		final ScaleDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem,
				coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != ScaleDirection.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final TVertexSelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		final ScaleDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem,
				coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		if (directionByMouse != null) {
			moverWidget.setMoveDirection(directionByMouse);
		}
		switch (directionByMouse) {
		case XYZ:
			return new ScaleTVertexManipulatorUsesYMouseDrag(getModelEditor(), selectionView);
		case FLAT_XY:
			return new ScaleXYTVertexManipulator(getModelEditor(), selectionView);
		case RIGHT:
			return new ScaleXTVertexManipulator(getModelEditor(), selectionView);
		case UP:
			return new ScaleYTVertexManipulator(getModelEditor(), selectionView);
		case NONE:
			return null;
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final TVertexSelectionView selectionView) {
		return new ScaleTVertexManipulator(getModelEditor(), selectionView);
	}

	@Override
	protected void renderWidget(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final TVertexSelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		moverWidget.render(graphics, coordinateSystem);
	}

}
