package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ButtonType;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.RotatorWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.ScalerWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.Widget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.ManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.*;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public class ModelEditorManipulatorBuilder implements ManipulatorBuilder {
	private final ModelElementRenderer modelElementRenderer;
	private final ViewportSelectionHandler viewportSelectionHandler;
	private final ModelHandler modelHandler;
	private final ModelView modelView;
	private ModelEditor modelEditor;
	Widget widget;
	ModelEditorActionType3 currentAction;

	public ModelEditorManipulatorBuilder(ModelEditorManager modelEditorManager, ModelHandler modelHandler, ModelEditorActionType3 currentAction) {
		this.viewportSelectionHandler = modelEditorManager.getViewportSelectionHandler();
		this.modelEditor = modelEditorManager.getModelEditor();
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();
		this.currentAction = currentAction;
		modelElementRenderer = new ModelElementRenderer(ProgramGlobals.getPrefs().getVertexSize());
		createWidget(currentAction);
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
		if (!selectionView.isEmpty() && widgetOffersEdit(mousePoint, coordinateSystem, selectionView)) {
			return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		} else if (viewportSelectionHandler.selectableUnderCursor(mousePoint, coordinateSystem)) {
			return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		}
		return null;
	}

	@Override
	public final Manipulator buildActivityListener(int x, int y,
	                                               ButtonType clickedButton,
	                                               CoordinateSystem coordinateSystem,
	                                               SelectionView selectionView) {
		if (clickedButton == ButtonType.RIGHT_MOUSE) {
			return createDefaultManipulator(selectionView);
		} else if (!selectionView.isEmpty()) {
			Vec2 mousePoint = new Vec2(x, y);
			Manipulator manipulatorFromWidget = createManipulatorFromWidget2(mousePoint, coordinateSystem, selectionView, currentAction);
			if (manipulatorFromWidget != null) {
				return manipulatorFromWidget;
			}
		}
		return new SelectManipulator(viewportSelectionHandler, coordinateSystem);
	}

	@Override
	public final void render(Graphics2D graphics,
	                         CoordinateSystem coordinateSystem,
	                         SelectionView selectionView,
	                         boolean isAnimated) {
		modelElementRenderer.reset(graphics, coordinateSystem, modelHandler.getRenderModel(), isAnimated);
//		selectionView.renderSelection(modelElementRenderer, coordinateSystem, modelView);
		if (!selectionView.isEmpty()) {
			renderWidget(graphics, coordinateSystem, selectionView);
		}
	}

	protected boolean widgetOffersEdit(Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		widget.setPoint(selectionView.getCenter());
		MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, coordinateSystem);
		widget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDimension.NONE;
	}

	protected Manipulator createManipulatorFromWidget(Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		return createManipulatorFromWidget2(mousePoint, coordinateSystem, selectionView, currentAction);
	}

	protected Manipulator createDefaultManipulator(SelectionView selectionView) {
		return getBuilder2(selectionView, currentAction, MoveDimension.XYZ);
	}

	private Manipulator getBuilder2(SelectionView selectionView, ModelEditorActionType3 action, MoveDimension directionByMouse) {
		return switch (action) {
			case TRANSLATION -> new MoveManipulator(getModelEditor(), directionByMouse);
			case ROTATION -> new RotateManipulator(getModelEditor(), selectionView, directionByMouse);
			case SCALING -> new ScaleManipulator(getModelEditor(), selectionView, directionByMouse);
			case EXTRUDE -> new ExtrudeManipulator(getModelEditor(), directionByMouse);
			case EXTEND -> new ExtendManipulator(getModelEditor(), directionByMouse);
			case SQUAT -> new SquatToolManipulator(getModelEditor(), selectionView, directionByMouse);
		};
	}

	private void createWidget(ModelEditorActionType3 action) {
		if(action == null){
			widget = new MoverWidget();
		} else {
			switch (action) {
				case TRANSLATION, EXTRUDE, EXTEND -> widget = new MoverWidget();
				case ROTATION, SQUAT -> widget = new RotatorWidget();
				case SCALING -> widget = new ScalerWidget();
			};
		}
	}

	protected Manipulator createManipulatorFromWidget2(Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView, ModelEditorActionType3 action) {
		widget.setPoint(selectionView.getCenter());
		System.out.println("widget point: " + selectionView.getCenter());
		MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, coordinateSystem);

		widget.setMoveDirection(directionByMouse);
		if (directionByMouse != MoveDimension.NONE) {
			return getBuilder2(selectionView, action, directionByMouse);
		}
		return null;
	}

	protected void renderWidget(Graphics2D graphics, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		widget.setPoint(selectionView.getCenter());
		widget.render(graphics, coordinateSystem);
	}

}
