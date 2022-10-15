package com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.Widget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.TVertSelectionManager;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.MouseEventHelpers;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class TransformActivity extends ViewportActivity {
	protected final Vec2 mouseStartPoint = new Vec2();
	protected final Vec2 lastDragPoint = new Vec2();
	protected Widget widget;
	protected MoveDimension dir;
	protected final Mat4 viewProjectionMatrix = new Mat4();
	protected final Mat4 inverseViewProjectionMatrix = new Mat4();
	protected boolean isActing = false;
	protected float zDepth = 0;
	protected final Vec3 tempVec3 = new Vec3();
	protected final Vec2 tempVec2 = new Vec2();
	protected final Vec2 vpSelectionCenter = new Vec2();

	public TransformActivity(ModelHandler modelHandler,
	                         AbstractModelEditorManager modelEditorManager, Widget widget) {
		super(modelHandler, modelEditorManager);
		this.widget = widget;
	}

	@Override
	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
		int modifiersEx = e.getModifiersEx();
		ProgramPreferences prefs = ProgramGlobals.getPrefs();
		Integer modifyMouseButton = ProgramGlobals.getPrefs().getModifyMouseButton();
		if (!MouseEventHelpers.isSameMouseButton(e, prefs.getModifyMouseButton())) {
			finnishAction(e, coordinateSystem, true);
		} else {
			finnishAction(e, coordinateSystem, !SwingUtilities.isMiddleMouseButton(e));
		}

//		if ((modifiersEx & modifyMouseButton) == modifyMouseButton ) {
//			finnishAction(e, coordinateSystem, true);
//		} else {
//			finnishAction(e, coordinateSystem, !SwingUtilities.isMiddleMouseButton(e));
//		}

//		if (SwingUtilities.isRightMouseButton(e)) {
//			finnishAction(e, coordinateSystem, true);
//		} else {
//			finnishAction(e, coordinateSystem, !SwingUtilities.isMiddleMouseButton(e));
//		}
		System.out.println("Mouse pressed! selectionView: " + selectionManager + " is UV-manager: " + (selectionManager instanceof TVertSelectionManager));
		System.out.println("mouseX: " + e.getX() + "mouseY: " + e.getY());

		if (MouseEventHelpers.matches(e, prefs.getModifyMouseButton(),prefs.getSnapTransformModifier()) && !selectionManager.isEmpty()) {
//		if ((modifyMouseButton & modifiersEx) > 0 && !selectionManager.isEmpty()) {
			isActing = true;

			Vec2 mousePoint = new Vec2(e.getX(), e.getY());
			setWidgetPoint(selectionManager);
			MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, coordinateSystem);
			dir = directionByMouse;
			widget.setMoveDirection(directionByMouse);

			mouseStartPoint.set(coordinateSystem.geomX(e.getX()), coordinateSystem.geomY(e.getY()));
			lastDragPoint.set(mouseStartPoint);

			startCoord(coordinateSystem);
		}

	}
	protected abstract void startCoord(CoordinateSystem coordinateSystem);

	@Override
	public void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem) {
		finnishAction(e, coordinateSystem, false);
	}

	protected abstract void finnishAction(MouseEvent e, CoordinateSystem coordinateSystem, boolean wasCanceled);

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
			Vec2 mouseEnd = new Vec2(coordinateSystem.geomX(e.getX()), coordinateSystem.geomY(e.getY()));
			updateCoord(e, coordinateSystem, mouseEnd);
			lastDragPoint.set(mouseEnd);
		}
	}

	protected abstract void updateCoord(MouseEvent e, CoordinateSystem coordinateSystem, Vec2 mouseEnd);

	@Override
	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		ProgramPreferences prefs = ProgramGlobals.getPrefs();
		if (isActing) {
			System.out.println("vpMat, canceled");
			finnishAction(e, viewProjectionMatrix, sizeAdj, true);
		}

		int modifiersEx = e.getModifiersEx();
//		if ((ProgramGlobals.getPrefs().getModifyMouseButton() & modifiersEx) > 0 && !selectionManager.isEmpty()) {
		if (MouseEventHelpers.matches(e, prefs.getModifyMouseButton(),prefs.getSnapTransformModifier()) && !selectionManager.isEmpty()) {
			isActing = true;


			setWidgetPoint(selectionManager);
			MoveDimension directionByMouse = MoveDimension.XYZ;
			dir = directionByMouse;
			widget.setMoveDirection(directionByMouse);

			tempVec3.set(selectionManager.getCenter());
			tempVec3.transform(viewProjectionMatrix, 1, true);
			zDepth = tempVec3.z;
			this.viewProjectionMatrix.set(viewProjectionMatrix);
			this.inverseViewProjectionMatrix.set(viewProjectionMatrix).invert();

			mouseStartPoint.set(getPoint(e));
			lastDragPoint.set(mouseStartPoint);

			startMat();
		}
	}

	protected abstract void startMat();

	@Override
	public void mouseReleased(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		finnishAction(e, viewProjectionMatrix, sizeAdj, false);
	}

	abstract protected void finnishAction(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj, boolean wasCanceled);

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
		if (isActing) {
			ProgramPreferences prefs = ProgramGlobals.getPrefs();
			boolean isPrecise = false;
			boolean isSnap = MouseEventHelpers.hasModifier(e, prefs.getSnapTransformModifier());
			boolean isAxisLock = false;
			Vec2 mouseEnd = getPoint(e);
//			updateMat(e, viewProjectionMatrix, mouseEnd);
			updateMat(viewProjectionMatrix, mouseEnd, isPrecise, isSnap, isAxisLock);
			lastDragPoint.set(mouseEnd);
		}
	}

	protected abstract void updateMat(MouseEvent e, Mat4 viewProjectionMatrix, Vec2 mouseEnd);
	protected void updateMat(Mat4 viewProjectionMatrix, Vec2 mouseEnd,
	                         boolean isPrecise, boolean isSnap, boolean isAxisLock){

	}

	@Override
	public void render(Graphics2D graphics, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		if (!selectionManager.isEmpty()) {
			setWidgetPoint(selectionManager);
			widget.render(graphics, coordinateSystem);
		}
	}

	@Override
	public boolean isEditing() {
		return isActing;
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

	protected Vec2 getViewportSelectionCenter(){
//		if(selectionManager instanceof TVertSelectionManager){
//			return selectionManager.getUVCenter(0);
//		} else {
//		}
		return vpSelectionCenter.setAsProjection(selectionManager.getCenter(), viewProjectionMatrix);
	}

	public double getThetaOfDiff(Vec2 v1, Vec2 v2){
		double tX = v1.x - v2.x;
		double tY = v1.y - v2.y;

		return Math.atan2(tY, tX);
	}
}
