package com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.uv;

import java.awt.Graphics2D;
import java.awt.Point;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ViewportSelectionHandler;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.uv.MoveTVertexManipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.uv.MoveXTVertexManipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.uv.MoveYTVertexManipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexSelectionView;
import com.hiveworkshop.wc3.gui.modeledit.useractions.widgets.MoverWidget;
import com.hiveworkshop.wc3.gui.modeledit.useractions.widgets.MoverWidget.MoveDirection;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public final class MoverWidgetManipulatorBuilder extends AbstractSelectAndEditTVertexEditorManipulatorBuilder {
	private final MoverWidget moverWidget = new MoverWidget(new Vertex(0, 0, 0));

	public MoverWidgetManipulatorBuilder(final TVertexEditor modelEditor,
			final ViewportSelectionHandler viewportSelectionHandler, final ProgramPreferences programPreferences,
			final ModelView modelView) {
		super(viewportSelectionHandler, programPreferences, modelEditor, modelView);
	}

	@Override
	protected boolean widgetOffersEdit(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final TVertexSelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		final MoveDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem,
				coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDirection.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final TVertexSelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		final MoveDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem,
				coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		if (directionByMouse != null) {
			moverWidget.setMoveDirection(directionByMouse);
		}
		switch (directionByMouse) {
		case BOTH:
			return new MoveTVertexManipulator(getModelEditor());
		case RIGHT:
			return new MoveXTVertexManipulator(getModelEditor());
		case UP:
			return new MoveYTVertexManipulator(getModelEditor());
		case NONE:
			return null;
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(final Vertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final TVertexSelectionView selectionView) {
		return new MoveTVertexManipulator(getModelEditor());
	}

	@Override
	protected void renderWidget(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final TVertexSelectionView selectionView) {
		moverWidget.setPoint(selectionView.getCenter());
		moverWidget.render(graphics, coordinateSystem);
	}

}
