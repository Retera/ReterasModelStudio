package com.hiveworkshop.rms.ui.gui.modeledit.manipulator;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.event.MouseEvent;

public class SelectManipulator extends Manipulator {
	private final ViewportSelectionHandler viewportSelectionHandler;
	private Vec2 mouseEnd;
	private CoordinateSystem coordinateSystem;
	private byte currentDim1;
	private byte currentDim2;
//	private Mat4 viewPortMat;
//	double zoom;

	public SelectManipulator(ViewportSelectionHandler viewportSelectionHandler, CoordinateSystem coordinateSystem) {
		this.viewportSelectionHandler = viewportSelectionHandler;
		this.coordinateSystem = coordinateSystem;
	}

	public SelectManipulator(ViewportSelectionHandler viewportSelectionHandler, CameraHandler cameraHandler) {
		this.viewportSelectionHandler = viewportSelectionHandler;
//		this.coordinateSystem = coordinateSystem;
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
		currentDim1 = dim1;
		currentDim2 = dim2;
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, CameraHandler cameraHandler) {
//		currentDim1 = dim1;
//		currentDim2 = dim2;
	}

	@Override
	protected void onStart(MouseEvent e, Vec3 mouseStart, CameraHandler cameraHandler) {
//		currentDim1 = dim1;
//		currentDim2 = dim2;
	}

	@Override
	public void update(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		this.mouseEnd = mouseEnd;
	}

	@Override
	public void update(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, Mat4 viewPortAntiRotMat) {
		this.mouseEnd = mouseEnd;
	}

	@Override
	public void update(MouseEvent e, Vec3 mouseStart, Vec3 mouseEnd, Mat4 viewPortAntiRotMat) {
//		this.mouseEnd = mouseEnd;
	}

	@Override
	public UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		Vec2 min = new Vec2(activityStart).minimize(mouseEnd);
		Vec2 max = new Vec2(activityStart).maximize(mouseEnd);
		UndoAction action = viewportSelectionHandler.selectRegion(e, min, max, coordinateSystem);
		if (action != null) {
			action.redo();
		}
		return action;
	}

	@Override
	public UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, Mat4 viewPortAntiRotMat, double sizeAdj) {
		Vec2 min = new Vec2(activityStart).minimize(mouseEnd);
		Vec2 max = new Vec2(activityStart).maximize(mouseEnd);
//		System.out.println("SelectManipulator#finish " + "min: " + min + " max: " + max);
//		return viewportSelectionHandler.selectRegion(e, min, max, cameraHandler).redo();
		UndoAction action = viewportSelectionHandler.selectRegion(e, min, max, viewPortAntiRotMat, sizeAdj);
		if (action != null) {
			action.redo();
		}
		return action;
	}

	@Override
	public UndoAction finish(MouseEvent e, Vec3 mouseStart, Vec3 mouseEnd, CameraHandler cameraHandler) {
		Vec3 min = new Vec3(activityStart1).minimize(mouseEnd);
		Vec3 max = new Vec3(activityStart1).maximize(mouseEnd);
//		System.out.println("SelectManipulator#finish " + "min: " + min + " max: " + max);
//		return viewportSelectionHandler.selectRegion(e, min, max, cameraHandler).redo();
		Mat4 viewPortAntiRotMat = cameraHandler.getViewPortAntiRotMat();
		double sizeAdj = cameraHandler.sizeAdj();
		UndoAction action = viewportSelectionHandler.selectRegion(e, min, max, viewPortAntiRotMat, sizeAdj);
		if (action != null) {
			action.redo();
		}
		return action;
	}

	@Override
	public void render(Graphics2D graphics, CoordinateSystem coordinateSystem) {
		if (mouseEnd == null) {
			return;
		}
		if ((currentDim1 == coordinateSystem.getPortFirstXYZ()) && (currentDim2 == coordinateSystem.getPortSecondXYZ())) {
			double minX = Math.min(coordinateSystem.viewX(activityStart.x), coordinateSystem.viewX(mouseEnd.x));
			double minY = Math.min(coordinateSystem.viewY(activityStart.y), coordinateSystem.viewY(mouseEnd.y));
			double maxX = Math.max(coordinateSystem.viewX(activityStart.x), coordinateSystem.viewX(mouseEnd.x));
			double maxY = Math.max(coordinateSystem.viewY(activityStart.y), coordinateSystem.viewY(mouseEnd.y));
			graphics.setColor(ProgramGlobals.getPrefs().getSelectColor());
			graphics.drawRect((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY));
		}
	}
}
