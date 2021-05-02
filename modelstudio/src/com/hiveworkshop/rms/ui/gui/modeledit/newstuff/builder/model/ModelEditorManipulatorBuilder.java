package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ButtonType;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.Graphics2DToModelElementRendererAdapter;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.ManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.SelectManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public abstract class ModelEditorManipulatorBuilder implements ManipulatorBuilder, ModelEditorChangeListener {
	private final ProgramPreferences programPreferences;
	private final Graphics2DToModelElementRendererAdapter graphics2dToModelElementRendererAdapter;
	ModelEditorManager modelEditorManager;
	private ViewportSelectionHandler viewportSelectionHandler;
	private ModelEditor modelEditor;
	private ModelView modelView;
	private ModelHandler modelHandler;

	public ModelEditorManipulatorBuilder(ViewportSelectionHandler viewportSelectionHandler,
	                                     ProgramPreferences programPreferences,
	                                     ModelEditor modelEditor,
	                                     ModelView modelView) {
		this.viewportSelectionHandler = viewportSelectionHandler;
		this.programPreferences = programPreferences;
		this.modelEditor = modelEditor;
		this.modelView = modelView;
		graphics2dToModelElementRendererAdapter = new Graphics2DToModelElementRendererAdapter(programPreferences.getVertexSize());
	}

	public ModelEditorManipulatorBuilder(ModelEditorManager modelEditorManager, ModelHandler modelHandler,
	                                     ProgramPreferences programPreferences) {
		this.modelEditorManager = modelEditorManager;
		this.modelHandler = modelHandler;
		this.viewportSelectionHandler = modelEditorManager.getViewportSelectionHandler();
		this.programPreferences = programPreferences;
		this.modelEditor = modelEditorManager.getModelEditor();
		this.modelView = modelHandler.getModelView();
		graphics2dToModelElementRendererAdapter = new Graphics2DToModelElementRendererAdapter(programPreferences.getVertexSize());
	}

	@Override
	public void modelEditorChanged(ModelEditor newModelEditor) {
		modelEditor = newModelEditor;
	}

	protected final ModelEditor getModelEditor() {
		return modelEditor;
	}

	@Override
	public final Cursor getCursorAt(int x, int y, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		Point mousePoint = new Point(x, y);
		if (!selectionView.isEmpty() && widgetOffersEdit(selectionView.getCenter(), mousePoint, coordinateSystem, selectionView)) {
			return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		} else if (viewportSelectionHandler.canSelectAt(mousePoint, coordinateSystem)) {
			return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		}
		return null;
	}

	@Override
	public final Manipulator buildActivityListener(int x, int y,
	                                               ButtonType clickedButton,
	                                               CoordinateSystem coordinateSystem,
	                                               SelectionView selectionView) {
		Point mousePoint = new Point(x, y);
		if (clickedButton == ButtonType.RIGHT_MOUSE) {
			return createDefaultManipulator(selectionView.getCenter(), mousePoint, coordinateSystem, selectionView);
		} else {
			if (!selectionView.isEmpty()) {
				Manipulator manipulatorFromWidget = createManipulatorFromWidget(selectionView.getCenter(), mousePoint, coordinateSystem, selectionView);
				if (manipulatorFromWidget != null) {
					return manipulatorFromWidget;
				}
			}
			return new SelectManipulator(viewportSelectionHandler, programPreferences, coordinateSystem);
		}
	}

	@Override
	public final void render(Graphics2D graphics,
	                         CoordinateSystem coordinateSystem,
	                         SelectionView selectionView,
	                         RenderModel renderModel) {
		selectionView.renderSelection(graphics2dToModelElementRendererAdapter.reset(graphics, coordinateSystem, modelHandler.getRenderModel(), programPreferences, true), coordinateSystem, modelView, programPreferences);
		if (!selectionView.isEmpty()) {
			renderWidget(graphics, coordinateSystem, selectionView);
		}
	}

	@Override
	public final void renderStatic(Graphics2D graphics,
	                               CoordinateSystem coordinateSystem,
	                               SelectionView selectionView) {
		selectionView.renderSelection(graphics2dToModelElementRendererAdapter.reset(graphics, coordinateSystem, modelHandler.getRenderModel(), programPreferences, false), coordinateSystem, modelView, programPreferences);
		if (!selectionView.isEmpty()) {
			renderWidget(graphics, coordinateSystem, selectionView);
		}
	}

	protected abstract boolean widgetOffersEdit(Vec3 selectionCenter, Point mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected abstract Manipulator createManipulatorFromWidget(Vec3 selectionCenter, Point mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected abstract Manipulator createDefaultManipulator(Vec3 selectionCenter, Point mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected abstract void renderWidget(Graphics2D graphics, CoordinateSystem coordinateSystem, SelectionView selectionView);

}
