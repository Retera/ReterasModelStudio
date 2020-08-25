package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv;

import java.awt.Graphics2D;
import java.awt.Point;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.uv.widgets.TVertexRotatorWidget;
import com.hiveworkshop.rms.ui.application.edit.uv.widgets.TVertexRotatorWidget.RotateDirection;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv.RotateTVertexHorizontalManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv.RotateTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv.RotateTVertexVerticalManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vertex2;

public final class RotatorWidgetTVertexEditorManipulatorBuilder extends AbstractSelectAndEditTVertexEditorManipulatorBuilder {
	private final TVertexRotatorWidget moverWidget = new TVertexRotatorWidget(new Vertex2(0, 0));

	public RotatorWidgetTVertexEditorManipulatorBuilder(final TVertexEditor modelEditor,
														final ViewportSelectionHandler viewportSelectionHandler, final ProgramPreferences programPreferences,
														final ModelView modelView) {
		super(viewportSelectionHandler, programPreferences, modelEditor, modelView);
	}

	@Override
	protected boolean widgetOffersEdit(final Vertex2 selectionCenter, final Point mousePoint,
									   final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		final RotateDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem);
		moverWidget.setMoveDirection(directionByMouse);
		return directionByMouse != RotateDirection.NONE;
	}

	@Override
	protected Manipulator createManipulatorFromWidget(final Vertex2 selectionCenter, final Point mousePoint,
													  final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		final RotateDirection directionByMouse = moverWidget.getDirectionByMouse(mousePoint, coordinateSystem);
		if (directionByMouse != null) {
			moverWidget.setMoveDirection(directionByMouse);
		}
		switch (directionByMouse) {
			case FREE:
				return new RotateTVertexManipulator(getModelEditor(), selectionView);
			case HORIZONTALLY:
				return new RotateTVertexHorizontalManipulator(getModelEditor(), selectionView);
			case VERTICALLY:
				return new RotateTVertexVerticalManipulator(getModelEditor(), selectionView);
			case SPIN:
				return new RotateTVertexManipulator(getModelEditor(), selectionView);
			case NONE:
				return null;
		}
		return null;
	}

	@Override
	protected Manipulator createDefaultManipulator(final Vertex2 selectionCenter, final Point mousePoint,
												   final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		return new RotateTVertexManipulator(getModelEditor(), selectionView);
	}

	@Override
	protected void renderWidget(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
								final SelectionView selectionView) {
		moverWidget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		moverWidget.render(graphics, coordinateSystem);
	}

}
