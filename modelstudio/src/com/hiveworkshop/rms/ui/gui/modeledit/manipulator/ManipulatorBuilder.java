package com.hiveworkshop.rms.ui.gui.modeledit.manipulator;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ButtonType;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.Widget;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public abstract class ManipulatorBuilder {
	protected final ViewportSelectionHandler viewportSelectionHandler;
	protected final ModelHandler modelHandler;
	protected final ModelView modelView;
	protected ModelEditor modelEditor;
	protected Widget widget;

	public ManipulatorBuilder(ModelEditor modelEditor, ViewportSelectionHandler viewportSelectionHandler, ModelHandler modelHandler) {
		this.viewportSelectionHandler = viewportSelectionHandler;
		this.modelEditor = modelEditor;
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();
	}

	public void modelEditorChanged(ModelEditor newModelEditor) {
		modelEditor = newModelEditor;
	}

	protected ModelEditor getModelEditor() {
		return modelEditor;
	}

	public Cursor getCursorAt(int x, int y, CoordinateSystem coordinateSystem, AbstractSelectionManager selectionManager) {
		Vec2 mousePoint = new Vec2(x, y);
		if (!selectionManager.isEmpty() && widgetOffersEdit(mousePoint, coordinateSystem, selectionManager)) {
			return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		} else if (viewportSelectionHandler.selectableUnderCursor(mousePoint, coordinateSystem)) {
			return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		}
		return null;
	}

	public Cursor getCursorAt(int x, int y, CameraHandler cameraHandler, AbstractSelectionManager selectionManager) {
		Vec2 mousePoint = new Vec2(x, y);
		if (!selectionManager.isEmpty() && widgetOffersEdit(mousePoint, cameraHandler, selectionManager)) {
			return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		}
//		else if (viewportSelectionHandler.selectableUnderCursor(mousePoint, cameraHandler)) {
//			return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
//		}
		return null;
	}

	public Manipulator buildManipulator(int x, int y,
	                                    ButtonType clickedButton,
	                                    CoordinateSystem coordinateSystem,
	                                    AbstractSelectionManager selectionManager) {
		if (clickedButton == ButtonType.RIGHT_MOUSE) {
			return createDefaultManipulator(selectionManager);
		} else if (!selectionManager.isEmpty()) {
			Vec2 mousePoint = new Vec2(x, y);
			Manipulator manipulatorFromWidget = createManipulatorFromWidget(mousePoint, coordinateSystem, selectionManager);
			if (manipulatorFromWidget != null) {
				return manipulatorFromWidget;
			}
		}
		return new SelectManipulator(viewportSelectionHandler, coordinateSystem);
	}

	public Manipulator buildManipulator(int x, int y,
	                                    ButtonType clickedButton,
	                                    CameraHandler cameraHandler,
	                                    AbstractSelectionManager selectionManager) {
		if (clickedButton == ButtonType.RIGHT_MOUSE && !selectionManager.isEmpty()
//				&& ProgramGlobals.getEditorActionType() == ModelEditorActionType3.TRANSLATION) {
				&& ProgramGlobals.getEditorActionType() == ModelEditorActionType3.ROTATION) {
//			return createDefaultManipulator(selectionManager);
			Vec2 mousePoint = new Vec2(x, y);
			Manipulator manipulatorFromWidget = createManipulatorFromUgg(mousePoint, cameraHandler, selectionManager);
			if (manipulatorFromWidget != null) {
				return manipulatorFromWidget;
			}
		}
		return new SelectManipulator(viewportSelectionHandler, cameraHandler);
	}

	protected boolean widgetOffersEdit(Vec2 mousePoint,
	                                   CoordinateSystem coordinateSystem,
	                                   AbstractSelectionManager selectionManager) {
		setWidgetPoint(selectionManager);
		MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, coordinateSystem);
		widget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDimension.NONE;
	}

	protected boolean widgetOffersEdit(Vec2 mousePoint,
	                                   CameraHandler cameraHandler,
	                                   AbstractSelectionManager selectionManager) {
		setWidgetPoint(selectionManager);
//		MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, cameraHandler);
		MoveDimension directionByMouse = MoveDimension.NONE;
		widget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDimension.NONE;
	}

	protected Manipulator createManipulatorFromWidget(Vec2 mousePoint,
	                                                  CoordinateSystem coordinateSystem,
	                                                  AbstractSelectionManager selectionManager) {
		setWidgetPoint(selectionManager);
		MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, coordinateSystem);

		widget.setMoveDirection(directionByMouse);
		if (directionByMouse != MoveDimension.NONE) {
			return getManipulator(selectionManager, directionByMouse);
		}
		return null;
	}

	protected Manipulator createManipulatorFromUgg(Vec2 mousePoint,
	                                               CameraHandler cameraHandler,
	                                               AbstractSelectionManager selectionManager) {
		setWidgetPoint(selectionManager);
//		if(ProgramGlobals.getEditorActionType() == ModelEditorActionType3.TRANSLATION){
		if (ProgramGlobals.getEditorActionType() == ModelEditorActionType3.ROTATION) {

			MoveDimension directionByMouse = MoveDimension.XYZ;
			widget.setMoveDirection(directionByMouse);
			if (directionByMouse != MoveDimension.NONE) {
				return getManipulator(selectionManager, directionByMouse);
			}
		}

		return null;
	}

	protected Manipulator createDefaultManipulator(AbstractSelectionManager selectionManager) {
		return getManipulator(selectionManager, MoveDimension.XYZ);
	}

	protected abstract Manipulator getManipulator(AbstractSelectionManager selectionManager,
	                                              MoveDimension directionByMouse);

	public void render(Graphics2D graphics,
	                   CoordinateSystem coordinateSystem,
	                   SelectionManager selectionManager) {
		renderWidget(graphics, coordinateSystem, selectionManager);
	}

	public void renderWidget(Graphics2D graphics, CoordinateSystem coordinateSystem, AbstractSelectionManager selectionManager) {
		if (!selectionManager.isEmpty()) {
			setWidgetPoint(selectionManager);
			widget.render(graphics, coordinateSystem);
		}
	}

	protected abstract void setWidgetPoint(AbstractSelectionManager selectionManager);
}
