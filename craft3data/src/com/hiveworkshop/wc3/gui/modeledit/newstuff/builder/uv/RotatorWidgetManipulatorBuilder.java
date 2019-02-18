package com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.uv;

import java.awt.Graphics2D;
import java.awt.Point;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ViewportSelectionHandler;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.RotateHorizontalManipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.RotateManipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.RotateVerticalManipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexSelectionView;
import com.hiveworkshop.wc3.gui.modeledit.useractions.widgets.RotatorWidget;
import com.hiveworkshop.wc3.gui.modeledit.useractions.widgets.RotatorWidget.RotateDirection;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public final class RotatorWidgetManipulatorBuilder extends AbstractSelectAndEditTVertexEditorManipulatorBuilder {
	private final RotatorWidget moverWidget = new RotatorWidget(new Vertex(0, 0, 0));

	public RotatorWidgetManipulatorBuilder(final TVertexEditor modelEditor,
			final ViewportSelectionHandler viewportSelectionHandler, final ProgramPreferences programPreferences,
			final ModelView modelView) {
		super(viewportSelectionHandler, programPreferences, modelEditor, modelView);
	}

	@Override
	protected boolean widgetOffersEdit(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final TVertexSelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		final RotateDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem);
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != RotateDirection.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final TVertexSelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		final RotateDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem);
		if (directionByMouse != null) {
			moverWidget.setMoveDirection(directionByMouse);
		}
		switch (directionByMouse) {
		case FREE:
			return new RotateManipulator(getModelEditor(), selectionView);
		case HORIZONTALLY:
			return new RotateHorizontalManipulator(getModelEditor(), selectionView);
		case VERTICALLY:
			return new RotateVerticalManipulator(getModelEditor(), selectionView);
		case SPIN:
			return new RotateManipulator(getModelEditor(), selectionView);
		case NONE:
			return null;
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final TVertexSelectionView selectionView) {
		return new RotateManipulator(getModelEditor(), selectionView);
	}

	@Override
	protected void renderWidget(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final TVertexSelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		moverWidget.render(graphics, coordinateSystem);
	}

}
