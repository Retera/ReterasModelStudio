package com.hiveworkshop.rms.ui.gui.modeledit.manipulator;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.Plane;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.Ray;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.event.MouseEvent;

public class SelectManipulator extends Manipulator {
	private Vec2 mouseEnd;
	private CoordinateSystem coordinateSystem;
	private byte currentDim1;
	private byte currentDim2;
//	private Mat4 viewPortMat;
//	double zoom;

	public SelectManipulator(ModelEditor modelEditor, AbstractSelectionManager selectionManager, CoordinateSystem coordinateSystem) {
		super(modelEditor, selectionManager, null);
		this.coordinateSystem = coordinateSystem;
	}

	public SelectManipulator(ModelEditor modelEditor, AbstractSelectionManager selectionManager, MoveDimension dir) {
		super(modelEditor, selectionManager, dir);
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
		currentDim1 = dim1;
		currentDim2 = dim2;
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
	public UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		Vec2 min = new Vec2(activityStart).minimize(mouseEnd);
		Vec2 max = new Vec2(activityStart).maximize(mouseEnd);
		UndoAction action = selectRegion(e, min, max, coordinateSystem);
		if (action != null) {
			action.redo();
		}
		return action;
	}

	@Override
	public UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, Mat4 viewPortAntiRotMat, double sizeAdj) {
		Vec2 min = new Vec2(activityStart).minimize(mouseEnd);
		Vec2 max = new Vec2(activityStart).maximize(mouseEnd);
		UndoAction action = selectRegion(e, min, max, viewPortAntiRotMat, sizeAdj);
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

	public UndoAction selectRegion(MouseEvent e, Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		SelectionMode tempSelectMode;

		Integer selectMouseButton = ProgramGlobals.getPrefs().getSelectMouseButton();
		Integer addSelectModifier = ProgramGlobals.getPrefs().getAddSelectModifier();
		Integer removeSelectModifier = ProgramGlobals.getPrefs().getRemoveSelectModifier();

		int modBut = e.getModifiersEx();

		if (modBut == addSelectModifier || ProgramGlobals.getSelectionMode() == SelectionMode.ADD && modBut != removeSelectModifier) {
			tempSelectMode = SelectionMode.ADD;
		} else if (modBut == removeSelectModifier || ProgramGlobals.getSelectionMode() == SelectionMode.DESELECT) {
			tempSelectMode = SelectionMode.DESELECT;
		} else {
			tempSelectMode = SelectionMode.SELECT;
		}
		return selectionManager.selectStuff(min, max, tempSelectMode, coordinateSystem);
	}

	public UndoAction selectRegion(MouseEvent e, Vec2 min, Vec2 max, Mat4 viewPortAntiRotMat, double sizeAdj) {
		SelectionMode tempSelectMode;

		Integer selectMouseButton = ProgramGlobals.getPrefs().getSelectMouseButton();
		Integer addSelectModifier = ProgramGlobals.getPrefs().getAddSelectModifier();
		Integer removeSelectModifier = ProgramGlobals.getPrefs().getRemoveSelectModifier();

		int modBut = e.getModifiersEx();

		if (modBut == addSelectModifier || ProgramGlobals.getSelectionMode() == SelectionMode.ADD && modBut != removeSelectModifier) {
			tempSelectMode = SelectionMode.ADD;
		} else if (modBut == removeSelectModifier || ProgramGlobals.getSelectionMode() == SelectionMode.DESELECT) {
			tempSelectMode = SelectionMode.DESELECT;
		} else {
			tempSelectMode = SelectionMode.SELECT;
		}

		return selectionManager.selectStuff(min, max, tempSelectMode, viewPortAntiRotMat, sizeAdj);
	}

//	public UndoAction selectRegion(MouseEvent e, Vec3 min, Vec3 max, Mat4 viewPortAntiRotMat, double sizeAdj) {
//		SelectionMode tempSelectMode;
//
//		Integer selectMouseButton = ProgramGlobals.getPrefs().getSelectMouseButton();
//		Integer addSelectModifier = ProgramGlobals.getPrefs().getAddSelectModifier();
//		Integer removeSelectModifier = ProgramGlobals.getPrefs().getRemoveSelectModifier();
//
//		int modBut = e.getModifiersEx();
//
//		if (modBut == addSelectModifier || ProgramGlobals.getSelectionMode() == SelectionMode.ADD && modBut != removeSelectModifier) {
//			tempSelectMode = SelectionMode.ADD;
//		} else if (modBut == removeSelectModifier || ProgramGlobals.getSelectionMode() == SelectionMode.DESELECT) {
//			tempSelectMode = SelectionMode.DESELECT;
//		} else {
//			tempSelectMode = SelectionMode.SELECT;
//		}
//
//		return selectionManager.selectStuff(min, max, tempSelectMode, viewPortAntiRotMat, sizeAdj);
//	}

	public boolean selectableUnderCursor(Vec2 point, CoordinateSystem axes) {
		return selectionManager.selectableUnderCursor(point, axes);
	}

	public boolean selectableUnderCursor(Vec2 point, Mat4 viewPortAntiRotMat, double sizeAdj) {
		return selectionManager.selectableUnderCursor(point, viewPortAntiRotMat, sizeAdj);
	}

	private final Vec3 camBackward = new Vec3();
	private final Vec3 target = new Vec3();
	private final Vec3 vecHeap = new Vec3();
	private final Ray rayHeap = new Ray();
	private final Plane planeHeap = new Plane();
	private Vec3 getWorldScreenSpaceRay(double viewX, double viewY){
		// https://stackoverflow.com/questions/45893277/is-it-possible-get-which-surface-of-cube-will-be-click-in-opengl
		// https://www.3dgep.com/understanding-the-view-matrix/
		// https://gamedev.stackexchange.com/questions/23395/how-to-convert-screen-space-into-3d-world-space
		// https://stackoverflow.com/questions/7692988/opengl-math-projecting-screen-space-to-world-space-coords
		// https://www.tomdalling.com/blog/modern-opengl/explaining-homogenous-coordinates-and-projective-geometry/

		Mat4 invViewProjectionMat = inverseViewProjectionMatrix;

		// near point
		vecHeap.set(0, 0, -1).transform(invViewProjectionMat, 1, true);
		rayHeap.setPoint(vecHeap);
		// far point
		vecHeap.set(0, 0, 1).transform(invViewProjectionMat, 1, true);
		rayHeap.setDirFromEnd(vecHeap);
		camBackward.set(rayHeap.getDir());

		// near point
		vecHeap.set(viewX, viewY, -1).transform(invViewProjectionMat, 1, true);
		rayHeap.setPoint(vecHeap);
		// far point
		vecHeap.set(viewX, viewY, 1).transform(invViewProjectionMat, 1, true);
		rayHeap.setDirFromEnd(vecHeap);

		// Calculate the ray-plane intersection point.
		planeHeap.set(camBackward, target);
		float intersectP = planeHeap.getIntersect(rayHeap);

		return new Vec3(rayHeap.getPoint()).addScaled(rayHeap.getDir(), intersectP);
	}
}
