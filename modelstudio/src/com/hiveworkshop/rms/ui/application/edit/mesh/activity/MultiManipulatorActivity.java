package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.ManipulatorBuilder;
import com.hiveworkshop.rms.util.Vec2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class MultiManipulatorActivity extends ViewportActivity {
	private final ManipulatorBuilder manipulatorBuilder;
	private Manipulator manipulator;
	private CursorManager cursorManager;
	private Vec2 mouseStartPoint;
	private Vec2 lastDragPoint;

	public MultiManipulatorActivity(ManipulatorBuilder manipulatorBuilder,
	                                ModelHandler modelHandler,
	                                AbstractModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
		this.manipulatorBuilder = manipulatorBuilder;
	}

	@Override
	public void modelEditorChanged(ModelEditor newModelEditor) {
		manipulatorBuilder.modelEditorChanged(newModelEditor);
	}

	@Override
	public void viewportChanged(CursorManager cursorManager) {
		this.cursorManager = cursorManager;
	}

	@Override
	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
		ButtonType buttonType;
		if (SwingUtilities.isRightMouseButton(e)) {
			buttonType = ButtonType.RIGHT_MOUSE;
			finnishAction(e, coordinateSystem, true);
		} else if (SwingUtilities.isMiddleMouseButton(e)) {
			buttonType = ButtonType.MIDDLE_MOUSE;
			finnishAction(e, coordinateSystem, false);
		} else {
			buttonType = ButtonType.LEFT_MOUSE;
			finnishAction(e, coordinateSystem, true);
		}
		System.out.println("Mouse pressed! selectionView: " + selectionManager);
		manipulator = manipulatorBuilder.buildManipulator(e.getX(), e.getY(), buttonType, coordinateSystem, selectionManager);
		if (manipulator != null) {
			mouseStartPoint = new Vec2(coordinateSystem.geomX(e.getPoint().getX()), coordinateSystem.geomY(e.getPoint().getY()));
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
			Vec2 mouseEnd = new Vec2(coordinateSystem.geomX(e.getPoint().getX()), coordinateSystem.geomY(e.getPoint().getY()));
			UndoAction undoAction = manipulator.finish(e, lastDragPoint, mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
			if (wasCanceled) {
				undoAction.undo();
			} else {
				undoManager.pushAction(undoAction);
			}
			mouseStartPoint = null;
			lastDragPoint = null;
			manipulator = null;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem) {
		cursorManager.setCursor(manipulatorBuilder.getCursorAt(e.getX(), e.getY(), coordinateSystem, selectionManager));
	}

	@Override
	public void mouseDragged(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (manipulator != null) {
			Vec2 mouseEnd = new Vec2(coordinateSystem.geomX(e.getPoint().getX()), coordinateSystem.geomY(e.getPoint().getY()));
			manipulator.update(e, lastDragPoint, mouseEnd, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
			lastDragPoint = mouseEnd;
		}
	}


	@Override
	public void mousePressed(MouseEvent e, CameraHandler cameraHandler) {
		ButtonType buttonType;
		if (SwingUtilities.isRightMouseButton(e)) {
			buttonType = ButtonType.RIGHT_MOUSE;
			finnishAction(e, cameraHandler, true);
		} else if (SwingUtilities.isMiddleMouseButton(e)) {
			buttonType = ButtonType.MIDDLE_MOUSE;
			finnishAction(e, cameraHandler, false);
		} else {
			buttonType = ButtonType.LEFT_MOUSE;
			finnishAction(e, cameraHandler, true);
		}
		System.out.println("Mouse pressed! selectionView: " + selectionManager);
		manipulator = manipulatorBuilder.buildManipulator(e.getX(), e.getY(), buttonType, cameraHandler, selectionManager);
		if (manipulator != null) {
			mouseStartPoint = new Vec2(cameraHandler.geomX(e.getPoint().getX()), cameraHandler.geomY(e.getPoint().getY()));
			manipulator.start(e, mouseStartPoint, cameraHandler);
			lastDragPoint = mouseStartPoint;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, CameraHandler cameraHandler) {
		finnishAction(e, cameraHandler, false);
	}

	private void finnishAction(MouseEvent e, CameraHandler cameraHandler, boolean wasCanceled) {
		if (manipulator != null) {
			Vec2 mouseEnd = new Vec2(cameraHandler.geomX(e.getPoint().getX()), cameraHandler.geomY(e.getPoint().getY()));
			UndoAction undoAction = manipulator.finish(e, lastDragPoint, mouseEnd, cameraHandler);
			if (wasCanceled) {
				undoAction.undo();
			} else {
				undoManager.pushAction(undoAction);
			}
			mouseStartPoint = null;
			lastDragPoint = null;
			manipulator = null;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e, CameraHandler cameraHandler) {
		cursorManager.setCursor(manipulatorBuilder.getCursorAt(e.getX(), e.getY(), cameraHandler, selectionManager));
	}

	@Override
	public void mouseDragged(MouseEvent e, CameraHandler cameraHandler) {
		if (manipulator != null) {
			Vec2 mouseEnd = new Vec2(cameraHandler.geomX(e.getPoint().getX()), cameraHandler.geomY(e.getPoint().getY()));
			manipulator.update(e, lastDragPoint, mouseEnd, cameraHandler);
			lastDragPoint = mouseEnd;
		}
	}

	@Override
	public void render(Graphics2D graphics, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		manipulatorBuilder.renderWidget(graphics, coordinateSystem, selectionManager);
		if (manipulator != null) {
			manipulator.render(graphics, coordinateSystem);
		}
	}

	@Override
	public boolean isEditing() {
		return manipulator != null;
	}

}
