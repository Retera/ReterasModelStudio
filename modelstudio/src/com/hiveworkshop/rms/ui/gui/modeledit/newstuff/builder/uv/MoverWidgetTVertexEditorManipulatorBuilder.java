package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv;

import com.hiveworkshop.rms.editor.model.TVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.uv.widgets.TVertexMoverWidget;
import com.hiveworkshop.rms.ui.application.edit.uv.widgets.TVertexMoverWidget.MoveDirection;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv.MoveTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv.MoveXTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv.MoveYTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;

public final class MoverWidgetTVertexEditorManipulatorBuilder extends AbstractSelectAndEditTVertexEditorManipulatorBuilder {
	private final TVertexMoverWidget moverWidget = new TVertexMoverWidget(new TVertex(0, 0));

	public MoverWidgetTVertexEditorManipulatorBuilder(final TVertexEditor modelEditor,
			final ViewportSelectionHandler viewportSelectionHandler, final ProgramPreferences programPreferences,
			final ModelView modelView) {
		super(viewportSelectionHandler, programPreferences, modelEditor, modelView);
	}

	@Override
	protected boolean widgetOffersEdit(final TVertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		final MoveDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem,
				coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDirection.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(final TVertex selectionCenter, final Point mousePoint,
                                                      final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
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
	protected Manipulator createDefaultManipulator(final TVertex selectionCenter, final Point mousePoint,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		return new MoveTVertexManipulator(getModelEditor());
	}

	@Override
	protected void renderWidget(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		moverWidget.render(graphics, coordinateSystem);
	}

}
