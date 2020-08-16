package com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.model;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.activity.ButtonType;
import com.hiveworkshop.wc3.gui.modeledit.activity.Graphics2DToAnimatedModelElementRendererAdapter;
import com.hiveworkshop.wc3.gui.modeledit.activity.Graphics2DToModelElementRendererAdapter;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ViewportSelectionHandler;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.SelectManipulator;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public abstract class AbstractSelectAndEditModelEditorManipulatorBuilder implements ModelEditorManipulatorBuilder {
	private final ViewportSelectionHandler viewportSelectionHandler;
	private final ProgramPreferences programPreferences;
	private ModelEditor modelEditor;
	private final Graphics2DToModelElementRendererAdapter graphics2dToModelElementRendererAdapter;
	private final Graphics2DToAnimatedModelElementRendererAdapter graphics2dToAnimatedModelElementRendererAdapter;
	private final ModelView modelView;

	public AbstractSelectAndEditModelEditorManipulatorBuilder(final ViewportSelectionHandler viewportSelectionHandler,
			final ProgramPreferences programPreferences, final ModelEditor modelEditor, final ModelView modelView) {
		this.viewportSelectionHandler = viewportSelectionHandler;
		this.programPreferences = programPreferences;
		this.modelEditor = modelEditor;
		this.modelView = modelView;
		graphics2dToModelElementRendererAdapter = new Graphics2DToModelElementRendererAdapter(
				programPreferences.getVertexSize(), programPreferences);
		graphics2dToAnimatedModelElementRendererAdapter = new Graphics2DToAnimatedModelElementRendererAdapter(
				programPreferences.getVertexSize());
	}

	@Override
	public void modelEditorChanged(final ModelEditor newModelEditor) {
		this.modelEditor = newModelEditor;
	}

	protected final ModelEditor getModelEditor() {
		return modelEditor;
	}

	@Override
	public final Cursor getCursorAt(final int x, final int y, final CoordinateSystem coordinateSystem,
			final SelectionView selectionView) {
		final Point mousePoint = new Point(x, y);
		if (!selectionView.isEmpty()
				&& widgetOffersEdit(selectionView.getCenter(), mousePoint, coordinateSystem, selectionView)) {
			return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		} else if (viewportSelectionHandler.canSelectAt(mousePoint, coordinateSystem)) {
			return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		}
		return null;
	}

	@Override
	public final Manipulator buildActivityListener(final int x, final int y, final ButtonType clickedButton,
			final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
		final Point mousePoint = new Point(x, y);
		if (clickedButton == ButtonType.RIGHT_MOUSE) {
			return createDefaultManipulator(selectionView.getCenter(), mousePoint, coordinateSystem, selectionView);
		} else {
			if (!selectionView.isEmpty()) {
				final Manipulator manipulatorFromWidget = createManipulatorFromWidget(selectionView.getCenter(),
						mousePoint, coordinateSystem, selectionView);
				if (manipulatorFromWidget != null) {
					return manipulatorFromWidget;
				}
			}
			return new SelectManipulator(viewportSelectionHandler, programPreferences, coordinateSystem);
		}
	}

	@Override
	public final void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final SelectionView selectionView, final RenderModel renderModel) {
		selectionView.renderSelection(graphics2dToAnimatedModelElementRendererAdapter.reset(graphics, coordinateSystem,
				renderModel, programPreferences), coordinateSystem, modelView, programPreferences);
		if (!selectionView.isEmpty()) {
			renderWidget(graphics, coordinateSystem, selectionView);
		}
	}

	@Override
	public final void renderStatic(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final SelectionView selectionView) {
		selectionView.renderSelection(graphics2dToModelElementRendererAdapter.reset(graphics, coordinateSystem),
				coordinateSystem, modelView, programPreferences);
		if (!selectionView.isEmpty()) {
			renderWidget(graphics, coordinateSystem, selectionView);
		}
	}

	protected abstract boolean widgetOffersEdit(Vertex selectionCenter, Point mousePoint,
			CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected abstract Manipulator createManipulatorFromWidget(Vertex selectionCenter, Point mousePoint,
			CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected abstract Manipulator createDefaultManipulator(Vertex selectionCenter, Point mousePoint,
			CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected abstract void renderWidget(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final SelectionView selectionView);

}