package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.RotatorWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.ScalerWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.Widget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.*;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.uv.MoveTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.uv.RotateTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.uv.ScaleTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.TVertSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class MultiManipulatorActivity extends ViewportActivity {
	private Manipulator manipulator;
	private Consumer<Cursor> cursorManager;
	private Vec2 mouseStartPoint;
	private Vec2 lastDragPoint;
	protected Widget widget;
	ModelEditorActionType3 currentAction;

	public MultiManipulatorActivity(ModelEditorActionType3 action,
	                                ModelHandler modelHandler,
	                                AbstractModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
		currentAction = action;
		widget = createWidget(action);
	}

	@Override
	public void modelEditorChanged(ModelEditor newModelEditor) {
		modelEditor = newModelEditor;
	}

	@Override
	public void viewportChanged(Consumer<Cursor> cursorManager) {
		this.cursorManager = cursorManager;
	}

	@Override
	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (SwingUtilities.isRightMouseButton(e)) {
			finnishAction(e, coordinateSystem, true);
		} else {
			finnishAction(e, coordinateSystem, !SwingUtilities.isMiddleMouseButton(e));
		}
		System.out.println("Mouse pressed! selectionView: " + selectionManager + " is UV-manager: " + (selectionManager instanceof TVertSelectionManager));
		System.out.println("mouseX: " + e.getX() + "mouseY: " + e.getY());
		manipulator = null;
		int modifiersEx = e.getModifiersEx();
		if ((ProgramGlobals.getPrefs().getModifyMouseButton() & modifiersEx) > 0 && !selectionManager.isEmpty()) {
			Vec2 mousePoint = new Vec2(e.getX(), e.getY());

			Manipulator manipulatorFromWidget = null;
			setWidgetPoint(selectionManager);
			MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, coordinateSystem);

			widget.setMoveDirection(directionByMouse);
			if (directionByMouse != MoveDimension.NONE) {
				manipulatorFromWidget =  getManipulator(directionByMouse);
			}

			if (manipulatorFromWidget != null) {
				manipulator = manipulatorFromWidget;
			}

		} else if ((ProgramGlobals.getPrefs().getSelectMouseButton() & modifiersEx) > 0) {
			manipulator = new SelectManipulator(modelEditor, selectionManager, coordinateSystem);
		}

		if (manipulator != null) {
			mouseStartPoint = new Vec2(coordinateSystem.geomX(e.getX()), coordinateSystem.geomY(e.getY()));
			manipulator.start(e, mouseStartPoint, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
			lastDragPoint = mouseStartPoint;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem) {
		finnishAction(e, coordinateSystem, false);
	}

	private void finnishAction(MouseEvent e, CoordinateSystem coordinateSystem, boolean wasCanceled) {
		if (manipulator != null) {
			Vec2 mouseEnd = new Vec2(coordinateSystem.geomX(e.getX()), coordinateSystem.geomY(e.getY()));
			UndoAction undoAction = manipulator.finish(e, lastDragPoint, mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
			if (wasCanceled && undoAction != null) {
				undoAction.undo();
			} else if (undoAction != null) {
				undoManager.pushAction(undoAction);
			}
			mouseStartPoint = null;
			lastDragPoint = null;
			manipulator = null;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem) {
		Vec2 mousePoint = new Vec2(e.getX(), e.getY());
		if (!selectionManager.isEmpty() && widgetOffersEdit(mousePoint, coordinateSystem, selectionManager)) {
			cursorManager.accept(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		} else if (selectionManager.selectableUnderCursor(mousePoint, coordinateSystem)) {
			cursorManager.accept(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
		cursorManager.accept(null);
	}

	@Override
	public void mouseDragged(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (manipulator != null) {
			Vec2 mouseEnd = new Vec2(coordinateSystem.geomX(e.getX()), coordinateSystem.geomY(e.getY()));
			manipulator.update(e, lastDragPoint, mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
			lastDragPoint = mouseEnd;
		}
	}


	@Override
	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (manipulator != null) {
			finnishAction(e, viewProjectionMatrix, sizeAdj, true);
		}

		manipulator = null;
		int modifiersEx = e.getModifiersEx();
		if ((ProgramGlobals.getPrefs().getModifyMouseButton() & modifiersEx) > 0 && !selectionManager.isEmpty()) {


			Manipulator manipulatorFromWidget = null;
			setWidgetPoint(selectionManager);
			System.out.println("getting manipulator");
			MoveDimension directionByMouse = MoveDimension.XYZ;
			widget.setMoveDirection(directionByMouse);

			manipulatorFromWidget = getManipulator(directionByMouse);

			if (manipulatorFromWidget != null) {
				manipulator = manipulatorFromWidget;
			}
		} else if ((ProgramGlobals.getPrefs().getSelectMouseButton() & modifiersEx) > 0) {
			manipulator = new SelectManipulator(modelEditor, selectionManager, MoveDimension.XYZ);
		}

		if (manipulator != null) {
			mouseStartPoint = getPoint(e);
//			mouseStartPoint = cameraHandler.getPoint_ifYZplane(e.getX(), e.getY());
			manipulator.start(e, mouseStartPoint, viewProjectionMatrix);
			lastDragPoint = mouseStartPoint;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		finnishAction(e, viewProjectionMatrix, sizeAdj, false);
	}

	private void finnishAction(MouseEvent e, Mat4 viewPortAntiRotMat, double sizeAdj, boolean wasCanceled) {
		if (manipulator != null) {
			Vec2 mouseEnd = getPoint(e);
			UndoAction undoAction = manipulator.finish(e, lastDragPoint, mouseEnd, viewPortAntiRotMat, sizeAdj);
			if (wasCanceled && undoAction != null) {
				undoAction.undo();
			} else if (undoAction != null) {
				undoManager.pushAction(undoAction);
			}
			mouseStartPoint = null;
			lastDragPoint = null;
			manipulator = null;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		Vec2 mousePoint = getPoint(e);
		if (!selectionManager.isEmpty() && widgetOffersEdit(mousePoint, null, selectionManager)) {
			cursorManager.accept(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		} else if (selectionManager.selectableUnderCursor(mousePoint, viewProjectionMatrix, sizeAdj)) {
			cursorManager.accept(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
		cursorManager.accept(null);
	}

	@Override
	public void mouseDragged(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (manipulator != null) {
			Vec2 mouseEnd = getPoint(e);
			manipulator.update(e, lastDragPoint, mouseEnd, viewProjectionMatrix);
			lastDragPoint = mouseEnd;
		}
	}

	@Override
	public void render(Graphics2D graphics, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		if (!selectionManager.isEmpty()) {
			setWidgetPoint(selectionManager);
			widget.render(graphics, coordinateSystem);
		}
		if (manipulator != null) {
			manipulator.render(graphics, coordinateSystem);
		}
	}

	@Override
	public boolean isEditing() {
		return manipulator != null;
	}



	protected ModelEditor getModelEditor() {
		return modelEditor;
	}

	protected boolean widgetOffersEdit(Vec2 mousePoint,
	                                   CoordinateSystem coordinateSystem,
	                                   AbstractSelectionManager selectionManager) {
		setWidgetPoint(selectionManager);
		MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, coordinateSystem);
		widget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDimension.NONE;
	}


	public void render(Graphics2D graphics,
	                   CoordinateSystem coordinateSystem,
	                   SelectionManager selectionManager) {
		if (!selectionManager.isEmpty()) {
			setWidgetPoint(selectionManager);
			widget.render(graphics, coordinateSystem);
		}
	}


	protected Manipulator getManipulator(MoveDimension directionByMouse) {
		if(selectionManager instanceof TVertSelectionManager) {
			return switch (currentAction) {
				case TRANSLATION, EXTRUDE, EXTEND -> new MoveTVertexManipulator(modelEditor, selectionManager, directionByMouse);
				case ROTATION, SQUAT -> new RotateTVertexManipulator(modelEditor, selectionManager, directionByMouse);
				case SCALING -> new ScaleTVertexManipulator(modelEditor, selectionManager, directionByMouse);
			};
		} else {
			return switch (currentAction) {
				case TRANSLATION -> new MoveManipulator(modelEditor, selectionManager, directionByMouse);
				case ROTATION -> new RotateManipulator(modelEditor, selectionManager, directionByMouse);
				case SCALING -> new ScaleManipulator(modelEditor, selectionManager, directionByMouse);
				case EXTRUDE -> new ExtrudeManipulator(modelEditor, selectionManager, directionByMouse);
				case EXTEND -> new ExtendManipulator(modelEditor, selectionManager, directionByMouse);
				case SQUAT -> new SquatToolManipulator(modelEditor, selectionManager, directionByMouse);
			};
		}
	}


	private Widget createWidget(ModelEditorActionType3 action) {
		if (action == null) {
			return new MoverWidget();
		} else {
			return switch (action) {
				case TRANSLATION, EXTRUDE, EXTEND -> widget = new MoverWidget();
				case ROTATION, SQUAT -> widget = new RotatorWidget();
				case SCALING -> widget = new ScalerWidget();
			};
		}
	}

	protected void setWidgetPoint(AbstractSelectionManager selectionManager) {
		if(selectionManager instanceof TVertSelectionManager){
//		widget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
			widget.setPoint(selectionManager.getUVCenter(0));
		} else {
			widget.setPoint(selectionManager.getCenter());
		}
	}
}
