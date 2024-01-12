package com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.AbstractCamera;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.Widget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.TVertSelectionManager;
import com.hiveworkshop.rms.ui.util.MouseEventHelpers;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class TransformActivity extends ViewportActivity {
	protected Widget widget;
	protected MoveDimension dir;
	protected boolean isActing = false;
	protected float zDepth = 0;
	protected final Vec2 vpSelectionCenter = new Vec2();
	protected final Vec2 mousePoint = new Vec2();
	protected final Vec2 mouseScreenPoint = new Vec2();
	protected final Vec2 mouseRelPoint = new Vec2();

	public TransformActivity(ModelHandler modelHandler,
	                         AbstractModelEditorManager modelEditorManager, Widget widget) {
		super(modelHandler, modelEditorManager);
		this.widget = widget;
	}
	protected abstract void startMat();


	protected void updateMat(Mat4 viewProjectionMatrix, Vec2 mouseEnd,
	                         boolean isPrecise, boolean isSnap, boolean isAxisLock) {
	}


	@Override
	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (isActing) {
			System.out.println("vpMat, canceled");
			finnishActionUgg(e, viewProjectionMatrix, sizeAdj, true);
		}

		if (MouseEventHelpers.matches(e, getModify(), getSnap()) && !selectionManager.isEmpty()) {
			this.viewProjectionMatrix.set(viewProjectionMatrix);
			this.inverseViewProjectionMatrix.set(viewProjectionMatrix).invert();
			isActing = true;


			mouseStartPoint.set(getPoint(e));
			lastMousePoint.set(mouseStartPoint);

			setWidgetPoint(selectionManager);
			MoveDimension directionByMouse;
			if (selectionManager instanceof TVertSelectionManager) {
				directionByMouse = widget.getDirectionByMouse(mouseStartPoint, viewProjectionMatrix, e.getComponent());
			} else {
				directionByMouse = MoveDimension.XYZ;
			}
			dir = directionByMouse;
			widget.setMoveDirection(directionByMouse);

			tempVec3.set(selectionManager.getCenter());
			tempVec3.transform(viewProjectionMatrix, 1, true);
			zDepth = tempVec3.z;

			startMat();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		finnishActionUgg(e, viewProjectionMatrix, sizeAdj, false);
	}

	protected void finnishActionUgg(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj, boolean wasCanceled) {
		if (isActing && transformAction != null) {
			mousePoint.set(getPoint(e));

			updateMat(null, mousePoint, false, MouseEventHelpers.hasModifier(e, getSnap()), false);

			finnishActionTugg(wasCanceled);
		}
		resetActivity();
	}
	protected void resetActivity() {
	}
	protected void finnishActionTugg(boolean wasCanceled) {
		if (wasCanceled) {
			transformAction.undo();
		} else {
			undoManager.pushAction(transformAction);
		}
		mouseStartPoint.set(0,0);
		lastMousePoint.set(0,0);
		isActing = false;
	}

	@Override
	public void mouseMoved(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		mousePoint.set(getPoint(e));
		if (cursorManager != null) {
			if (!selectionManager.isEmpty() && widgetOffersEdit(mousePoint, viewProjectionMatrix, e.getComponent(), selectionManager)) {
				cursorManager.accept(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			} else if (selectionManager.selectableUnderCursor(mousePoint, viewProjectionMatrix, sizeAdj)) {
				cursorManager.accept(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			} else {
				cursorManager.accept(null);
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (isActing) {
			boolean isPrecise = false;
			boolean isSnap = MouseEventHelpers.hasModifier(e, getSnap());
			boolean isAxisLock = false;
			mousePoint.set(getPoint(e));
			updateMat(viewProjectionMatrix, mousePoint, isPrecise, isSnap, isAxisLock);
			lastMousePoint.set(mousePoint);
		}
	}

	@Override
	public void render(Graphics2D graphics, AbstractCamera coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		if (!selectionManager.isEmpty()) {
			setWidgetPoint(selectionManager);
			widget.render(graphics, coordinateSystem.getViewProjectionMatrix(), coordinateSystem.getInvViewProjectionMat(), coordinateSystem.getComponent());
		}
	}

	@Override
	public boolean isEditing() {
		return isActing;
	}

	protected boolean widgetOffersEdit(Vec2 mousePoint,
	                                   Mat4 vpRotMat, Component parent,
	                                   AbstractSelectionManager selectionManager) {
		setWidgetPoint(selectionManager);
		MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, vpRotMat, parent);
		widget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDimension.NONE;
	}


	public void render(Graphics2D graphics,
	                   AbstractCamera coordinateSystem,
	                   SelectionManager selectionManager) {
		if (!selectionManager.isEmpty()) {
			setWidgetPoint(selectionManager);
			widget.render(graphics, coordinateSystem.getViewProjectionMatrix(), coordinateSystem.getInvViewProjectionMat(), coordinateSystem.getComponent());
		}
	}


	protected void setWidgetPoint(AbstractSelectionManager selectionManager) {
		widget.setPoint(selectionManager.getCenter());
	}

	protected Vec2 getViewportSelectionCenter() {
		return vpSelectionCenter.setAsProjection(selectionManager.getCenter(), viewProjectionMatrix);
	}

	public double getThetaOfDiff(Vec2 v1, Vec2 v2) {
		double tX = v1.x - v2.x;
		double tY = v1.y - v2.y;

		return Math.atan2(tY, tX);
	}
	public double getThetaOfDiff(Vec2 v1, Vec3 v2) {
		double tX = v1.x - v2.x;
		double tY = v1.y - v2.y;

		return Math.atan2(tY, tX);
	}
}
