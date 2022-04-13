package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.ManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.TVertSelectionManager;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class MultiManipulatorActivity extends ViewportActivity {
	private final ManipulatorBuilder manipulatorBuilder;
	private Manipulator manipulator;
	private Consumer<Cursor> cursorManager;
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
	public void viewportChanged(Consumer<Cursor> cursorManager) {
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
		System.out.println("Mouse pressed! selectionView: " + selectionManager + " is UV-manager: " + (selectionManager instanceof TVertSelectionManager));
		System.out.println("mouseX: " + e.getX() + "mouseY: " + e.getY());
		manipulator = manipulatorBuilder.buildManipulator(e, e.getX(), e.getY(), buttonType, coordinateSystem, selectionManager);
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
		cursorManager.accept(manipulatorBuilder.getCursorAt(e.getX(), e.getY(), coordinateSystem, selectionManager));
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
	public void mousePressed(MouseEvent e, Mat4 viewPortAntiRotMat, double sizeAdj) {
		if (manipulator != null) {
			finnishAction(e, viewPortAntiRotMat, sizeAdj, true);
		}
		manipulator = manipulatorBuilder.buildManipulator(e, e.getX(), e.getY(), selectionManager);
		if (manipulator != null) {
			mouseStartPoint = getPoint(e);
//			mouseStartPoint = cameraHandler.getPoint_ifYZplane(e.getX(), e.getY());
			manipulator.start(e, mouseStartPoint, viewPortAntiRotMat);
			lastDragPoint = mouseStartPoint;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, Mat4 viewPortAntiRotMat, double sizeAdj) {
		finnishAction(e, viewPortAntiRotMat, sizeAdj, false);
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
	public void mouseMoved(MouseEvent e, Mat4 viewPortAntiRotMat, double sizeAdj) {
		cursorManager.accept(manipulatorBuilder.getCursorAt(e, viewPortAntiRotMat, sizeAdj, selectionManager));
	}

	@Override
	public void mouseDragged(MouseEvent e, Mat4 viewPortAntiRotMat, double sizeAdj) {
		if (manipulator != null) {
			Vec2 mouseEnd = getPoint(e);
			manipulator.update(e, lastDragPoint, mouseEnd, viewPortAntiRotMat);
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
