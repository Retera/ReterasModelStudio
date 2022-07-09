package com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.Widget;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.Plane;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.Ray;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.SelectionBoxHelper;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.TVertSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class SelectActivity extends ViewportActivity {
	private Consumer<Cursor> cursorManager;
	private final Vec2 mouseStartPoint = new Vec2();
	private final Vec2 lastDragPoint = new Vec2();
	protected Widget widget;
	protected MoveDimension dir;
	protected final Mat4 inverseViewProjectionMatrix = new Mat4();
	private byte currentDim1;
	private byte currentDim2;
	protected boolean isActing = false;

	public SelectActivity(ModelHandler modelHandler,
	                      AbstractModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
		widget = new MoverWidget();
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
		int modifiersEx = e.getModifiersEx();
		if ((ProgramGlobals.getPrefs().getSelectMouseButton() & modifiersEx) > 0) {
			isActing = true;
			mouseStartPoint.set(coordinateSystem.geomX(e.getX()), coordinateSystem.geomY(e.getY()));
			lastDragPoint.set(mouseStartPoint);
			currentDim1 = coordinateSystem.getPortFirstXYZ();
			currentDim2 = coordinateSystem.getPortSecondXYZ();
		}

	}

	@Override
	public void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem) {
		finnishAction(e, coordinateSystem, false);
	}

	private void finnishAction(MouseEvent e, CoordinateSystem coordinateSystem, boolean wasCanceled) {
		if (isActing) {
			Vec2 mouseEnd = new Vec2(coordinateSystem.geomX(e.getX()), coordinateSystem.geomY(e.getY()));
			Vec2 min = new Vec2(mouseStartPoint).minimize(mouseEnd);
			Vec2 max = new Vec2(mouseStartPoint).maximize(mouseEnd);
			UndoAction undoAction = selectRegion(e, min, max, coordinateSystem);
			if (undoAction != null) {
				undoAction.redo();
			}
			if (wasCanceled && undoAction != null) {
				undoAction.undo();
			} else if (undoAction != null) {
				undoManager.pushAction(undoAction);
			}
			mouseStartPoint.set(0,0);
			lastDragPoint.set(0,0);
			isActing = false;
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
		if (isActing) {
			lastDragPoint.set(coordinateSystem.geomX(e.getX()), coordinateSystem.geomY(e.getY()));
		}
	}


	@Override
	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (isActing) {
			finnishAction(e, viewProjectionMatrix, sizeAdj, true);
		}

		int modifiersEx = e.getModifiersEx();
		if ((ProgramGlobals.getPrefs().getSelectMouseButton() & modifiersEx) > 0) {
			isActing = true;
			mouseStartPoint.set(getPoint(e));
			lastDragPoint.set(mouseStartPoint);
			inverseViewProjectionMatrix.set(viewProjectionMatrix).invert();
		}

	}
	public void mousePressed(MouseEvent e, SelectionBoxHelper viewBox, double sizeAdj) {
		if (isActing) {
			finnishAction(e, viewBox, sizeAdj, true);
		}

		int modifiersEx = e.getModifiersEx();
		if ((ProgramGlobals.getPrefs().getSelectMouseButton() & modifiersEx) > 0) {
			isActing = true;
			mouseStartPoint.set(getPoint(e));
			lastDragPoint.set(mouseStartPoint);
			inverseViewProjectionMatrix.setIdentity().invert();
		}

	}

	@Override
	public void mouseReleased(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		finnishAction(e, viewProjectionMatrix, sizeAdj, false);
	}
	public void mouseReleased(MouseEvent e, SelectionBoxHelper viewBox, double sizeAdj) {
		finnishAction(e, viewBox, sizeAdj, false);
	}

	private void finnishAction(MouseEvent e, Mat4 viewPortAntiRotMat, double sizeAdj, boolean wasCanceled) {
		if (isActing) {
			Vec2 mouseEnd = getPoint(e);
			Vec2 min = new Vec2(mouseStartPoint).minimize(mouseEnd);
			Vec2 max = new Vec2(mouseStartPoint).maximize(mouseEnd);
			UndoAction undoAction = selectRegion(e, min, max, viewPortAntiRotMat, sizeAdj);
			if (undoAction != null) {
				undoAction.redo();
			}
			if (wasCanceled && undoAction != null) {
				undoAction.undo();
			} else if (undoAction != null) {
				undoManager.pushAction(undoAction);
			}
			mouseStartPoint.set(0,0);
			lastDragPoint.set(0,0);
			isActing = false;
		}
	}
	private void finnishAction(MouseEvent e, SelectionBoxHelper viewBox, double sizeAdj, boolean wasCanceled) {
		if (isActing) {
			Vec2 mouseEnd = getPoint(e);
			Vec2 min = new Vec2(mouseStartPoint).minimize(mouseEnd);
			Vec2 max = new Vec2(mouseStartPoint).maximize(mouseEnd);
			UndoAction undoAction = selectRegion(e, min, max, viewBox, sizeAdj);
			if (undoAction != null) {
				undoAction.redo();
			}
			if (wasCanceled && undoAction != null) {
				undoAction.undo();
			} else if (undoAction != null) {
				undoManager.pushAction(undoAction);
			}
			mouseStartPoint.set(0,0);
			lastDragPoint.set(0,0);
			isActing = false;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		Vec2 mousePoint = getPoint(e);
		if (!selectionManager.isEmpty() && widgetOffersEdit(mousePoint, null, selectionManager)) {
			cursorManager.accept(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		} else if (selectionManager.selectableUnderCursor(mousePoint, inverseViewProjectionMatrix, sizeAdj)) {
			cursorManager.accept(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
		cursorManager.accept(null);
	}

	@Override
	public void mouseDragged(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (isActing) {
			lastDragPoint.set(getPoint(e));
		}
	}

	@Override
	public void render(Graphics2D graphics, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
//		if (!selectionManager.isEmpty()) {
//			setWidgetPoint(selectionManager);
//			widget.render(graphics, coordinateSystem);
//		}
		if (isActing) {
			if ((currentDim1 == coordinateSystem.getPortFirstXYZ()) && (currentDim2 == coordinateSystem.getPortSecondXYZ())) {
				double minX = Math.min(coordinateSystem.viewX(mouseStartPoint.x), coordinateSystem.viewX(lastDragPoint.x));
				double minY = Math.min(coordinateSystem.viewY(mouseStartPoint.y), coordinateSystem.viewY(lastDragPoint.y));
				double maxX = Math.max(coordinateSystem.viewX(mouseStartPoint.x), coordinateSystem.viewX(lastDragPoint.x));
				double maxY = Math.max(coordinateSystem.viewY(mouseStartPoint.y), coordinateSystem.viewY(lastDragPoint.y));
				graphics.setColor(ProgramGlobals.getPrefs().getSelectColor());
				graphics.drawRect((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY));
			}
		}
	}

	@Override
	public boolean isEditing() {
		return isActing;
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


	protected void setWidgetPoint(AbstractSelectionManager selectionManager) {
		if(selectionManager instanceof TVertSelectionManager){
//		widget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
			widget.setPoint(selectionManager.getUVCenter(0));
		} else {
			widget.setPoint(selectionManager.getCenter());
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

	public UndoAction selectRegion(MouseEvent e, Vec2 min, Vec2 max, SelectionBoxHelper viewBox, double sizeAdj) {
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

		return selectionManager.selectStuff(min, max, tempSelectMode, viewBox, sizeAdj);
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
