package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ButtonType;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.Widget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.SelectManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public abstract class ManipulatorBuilder implements ModelEditorChangeListener {
	protected final ViewportSelectionHandler viewportSelectionHandler;
	protected final ModelHandler modelHandler;
	protected final ModelView modelView;
	protected ModelEditor modelEditor;
	protected Widget widget;

	public ManipulatorBuilder(ModelEditor modelEditor, ViewportSelectionHandler viewportSelectionHandler, ModelHandler modelHandler){
		this.viewportSelectionHandler = viewportSelectionHandler;
		this.modelEditor = modelEditor;
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();
	}

	@Override
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

	public Manipulator buildActivityListener(int x, int y,
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

	protected boolean widgetOffersEdit(Vec2 mousePoint,
	                                   CoordinateSystem coordinateSystem,
	                                   AbstractSelectionManager selectionManager) {
		setWidgetPoint(selectionManager);
		MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, coordinateSystem);
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
			return getBuilder(selectionManager, directionByMouse);
		}
		return null;
	}

	protected Manipulator createDefaultManipulator(AbstractSelectionManager selectionManager) {
		return getBuilder(selectionManager, MoveDimension.XYZ);
	}

	protected abstract Manipulator getBuilder(AbstractSelectionManager selectionManager,
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
