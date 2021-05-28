package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.ManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class MultiManipulatorActivity implements ViewportActivity {
	private final ManipulatorBuilder manipulatorBuilder;
	private final UndoManager undoManager;
	private Manipulator manipulator;
	private CursorManager cursorManager;
	private Vec2 mouseStartPoint;
	private Vec2 lastDragPoint;
	private SelectionView selectionView;

	public MultiManipulatorActivity(ManipulatorBuilder manipulatorBuilder,
	                                UndoManager undoManager,
	                                SelectionView selectionView) {
		this.manipulatorBuilder = manipulatorBuilder;
		this.undoManager = undoManager;
		this.selectionView = selectionView;
	}

	@Override
	public void modelEditorChanged(ModelEditor newModelEditor) {
		manipulatorBuilder.modelEditorChanged(newModelEditor);
	}

	@Override
	public void onSelectionChanged(SelectionView newSelection) {
		this.selectionView = newSelection;
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
		System.out.println("Mouse pressed! selectionView: " + selectionView);
		manipulator = manipulatorBuilder.buildActivityListener(e.getX(), e.getY(), buttonType, coordinateSystem, selectionView);
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
		cursorManager.setCursor(manipulatorBuilder.getCursorAt(e.getX(), e.getY(), coordinateSystem, selectionView));
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
	public void render(Graphics2D graphics, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		manipulatorBuilder.render(graphics, coordinateSystem, selectionView, isAnimated);
		if (manipulator != null) {
			manipulator.render(graphics, coordinateSystem);
		}
	}

	@Override
	public boolean isEditing() {
		return manipulator != null;
	}

}
