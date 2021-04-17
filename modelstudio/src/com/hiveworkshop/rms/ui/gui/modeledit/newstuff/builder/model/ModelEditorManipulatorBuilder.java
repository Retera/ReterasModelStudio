package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ButtonType;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.Graphics2DToAnimatedModelElementRendererAdapter;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.Graphics2DToModelElementRendererAdapter;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.ManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.SelectManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public abstract class ModelEditorManipulatorBuilder implements ManipulatorBuilder, ModelEditorChangeListener {
	private final ViewportSelectionHandler viewportSelectionHandler;
	private final ProgramPreferences programPreferences;
	private final Graphics2DToModelElementRendererAdapter graphics2dToModelElementRendererAdapter;
	private final Graphics2DToAnimatedModelElementRendererAdapter graphics2dToAnimatedModelElementRendererAdapter;
	private final ModelView modelView;
	private ModelEditor modelEditor;

	public ModelEditorManipulatorBuilder(final ViewportSelectionHandler viewportSelectionHandler,
	                                     final ProgramPreferences programPreferences,
	                                     final ModelEditor modelEditor,
	                                     final ModelView modelView) {
		this.viewportSelectionHandler = viewportSelectionHandler;
		this.programPreferences = programPreferences;
		this.modelEditor = modelEditor;
		this.modelView = modelView;
		graphics2dToModelElementRendererAdapter = new Graphics2DToModelElementRendererAdapter(programPreferences.getVertexSize(), programPreferences);
		graphics2dToAnimatedModelElementRendererAdapter = new Graphics2DToAnimatedModelElementRendererAdapter(programPreferences.getVertexSize());
	}

	@Override
	public void modelEditorChanged(final ModelEditor newModelEditor) {
		modelEditor = newModelEditor;
	}

	protected final ModelEditor getModelEditor() {
		return modelEditor;
	}

	@Override
	public final Cursor getCursorAt(final int x, final int y,
	                                final CoordinateSystem coordinateSystem,
	                                final SelectionView selectionView) {
		final Point mousePoint = new Point(x, y);
		if (!selectionView.isEmpty() && widgetOffersEdit(selectionView.getCenter(), mousePoint, coordinateSystem, selectionView)) {
			return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		} else if (viewportSelectionHandler.canSelectAt(mousePoint, coordinateSystem)) {
			return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		}
		return null;
	}

	@Override
	public final Manipulator buildActivityListener(final int x, final int y,
	                                               final ButtonType clickedButton,
	                                               final CoordinateSystem coordinateSystem,
	                                               final SelectionView selectionView) {
		final Point mousePoint = new Point(x, y);
		if (clickedButton == ButtonType.RIGHT_MOUSE) {
			return createDefaultManipulator(selectionView.getCenter(), mousePoint, coordinateSystem, selectionView);
		} else {
			if (!selectionView.isEmpty()) {
				final Manipulator manipulatorFromWidget = createManipulatorFromWidget(selectionView.getCenter(), mousePoint, coordinateSystem, selectionView);
				if (manipulatorFromWidget != null) {
					return manipulatorFromWidget;
				}
			}
			return new SelectManipulator(viewportSelectionHandler, programPreferences, coordinateSystem);
		}
	}

	@Override
	public final void render(final Graphics2D graphics,
	                         final CoordinateSystem coordinateSystem,
	                         final SelectionView selectionView,
	                         final RenderModel renderModel) {
		selectionView.renderSelection(graphics2dToAnimatedModelElementRendererAdapter.reset(graphics, coordinateSystem, renderModel, programPreferences), coordinateSystem, modelView, programPreferences);
		if (!selectionView.isEmpty()) {
			renderWidget(graphics, coordinateSystem, selectionView);
		}
	}

	@Override
	public final void renderStatic(final Graphics2D graphics,
	                               final CoordinateSystem coordinateSystem,
	                               final SelectionView selectionView) {
		selectionView.renderSelection(graphics2dToModelElementRendererAdapter.reset(graphics, coordinateSystem), coordinateSystem, modelView, programPreferences);
		if (!selectionView.isEmpty()) {
			renderWidget(graphics, coordinateSystem, selectionView);
		}
	}

	protected abstract boolean widgetOffersEdit(Vec3 selectionCenter, Point mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected abstract Manipulator createManipulatorFromWidget(Vec3 selectionCenter, Point mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected abstract Manipulator createDefaultManipulator(Vec3 selectionCenter, Point mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected abstract void renderWidget(final Graphics2D graphics, final CoordinateSystem coordinateSystem, final SelectionView selectionView);

}
