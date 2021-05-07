package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ButtonType;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.Widget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.ManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.SelectManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public abstract class ModelEditorManipulatorBuilder implements ManipulatorBuilder, ModelEditorChangeListener {
	private final ModelElementRenderer modelElementRenderer;
	ModelEditorManager modelEditorManager;
	private ViewportSelectionHandler viewportSelectionHandler;
	private ModelEditor modelEditor;
	private ModelView modelView;
	private ModelHandler modelHandler;
	Widget widget;

	public ModelEditorManipulatorBuilder(ViewportSelectionHandler viewportSelectionHandler,
	                                     ModelEditor modelEditor,
	                                     ModelView modelView) {
		this.viewportSelectionHandler = viewportSelectionHandler;
		this.modelEditor = modelEditor;
		this.modelView = modelView;
		modelElementRenderer = new ModelElementRenderer(ProgramGlobals.getPrefs().getVertexSize());
	}

	public ModelEditorManipulatorBuilder(ModelEditorManager modelEditorManager,
	                                     ModelHandler modelHandler) {
		this.modelEditorManager = modelEditorManager;
		this.modelHandler = modelHandler;
		this.viewportSelectionHandler = modelEditorManager.getViewportSelectionHandler();
		this.modelEditor = modelEditorManager.getModelEditor();
		this.modelView = modelHandler.getModelView();
		modelElementRenderer = new ModelElementRenderer(ProgramGlobals.getPrefs().getVertexSize());
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
		Vec2 mousePoint = new Vec2(x, y);
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
		Vec2 mousePoint = new Vec2(x, y);
		if (clickedButton == ButtonType.RIGHT_MOUSE) {
			return createDefaultManipulator(selectionView.getCenter(), mousePoint, coordinateSystem, selectionView);
		} else {
			if (!selectionView.isEmpty()) {
				Manipulator manipulatorFromWidget = createManipulatorFromWidget(selectionView.getCenter(), mousePoint, coordinateSystem, selectionView);
				if (manipulatorFromWidget != null) {
					return manipulatorFromWidget;
				}
			}
			return new SelectManipulator(viewportSelectionHandler, coordinateSystem);
		}
	}

	@Override
	public final void render(Graphics2D graphics,
	                         CoordinateSystem coordinateSystem,
	                         SelectionView selectionView,
	                         boolean isAnimated) {
		modelElementRenderer.reset(graphics, coordinateSystem, modelHandler.getRenderModel(), isAnimated);
		selectionView.renderSelection(modelElementRenderer, coordinateSystem, modelView);
		if (!selectionView.isEmpty()) {
			renderWidget(graphics, coordinateSystem, selectionView);
		}
	}

	protected boolean widgetOffersEdit(Vec3 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		widget.setPoint(selectionView.getCenter());
		MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, coordinateSystem);
		widget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDimension.NONE;
	}

	protected abstract Manipulator createManipulatorFromWidget(Vec3 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected abstract Manipulator createDefaultManipulator(Vec3 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected void renderWidget(Graphics2D graphics, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		widget.setPoint(selectionView.getCenter());
		widget.render(graphics, coordinateSystem);
	}

}
