package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.CameraManager;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.SelectionBoxHelper;
import com.hiveworkshop.rms.ui.preferences.Nav3DMouseAction;
import com.hiveworkshop.rms.ui.preferences.Nav3DMousePrefs;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.MouseEventHelpers;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class MouseListenerThing extends MouseAdapter {

	private final CameraManager cameraHandler;
	private final ProgramPreferences programPreferences;
	private ViewportActivityManager activityManager;

	private Vec2 startP = null;
	private Vec2 endP = null;
	private final Vec2 vec2Temp = new Vec2();
	private final Vec2 endOld = new Vec2();

	private boolean isSelecting = false;
	private boolean isActing = false;

	public MouseListenerThing(CameraManager cameraHandler, ProgramPreferences programPreferences) {
		this.cameraHandler = cameraHandler;
		this.programPreferences = programPreferences;
	}

	public MouseListenerThing setActivityManager(ViewportActivityManager activityManager) {
		this.activityManager = activityManager;
		return this;
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		endP = setPoint(e, endP);
		startP = new Vec2(endP);
		Mat4 viewProjectionMatrix = cameraHandler.getViewProjectionMatrix();
		double sizeAdj = cameraHandler.sizeAdj();


		if (activityManager != null) {
			Vec2 topLeft = new Vec2(startP).maximize(endP);
			Vec2 botRight = new Vec2(endP).minimize(startP);
			SelectionBoxHelper viewBox = cameraHandler.getSelectionBoxHelper(topLeft, botRight);

			activityManager.mousePressed(e, viewProjectionMatrix, viewBox, sizeAdj);
			isSelecting = activityManager.isSelecting();
			isActing = activityManager.isEditing();
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (endP != null) {
			endP = setPoint(e, endP);
		}

		if (activityManager != null) {
			if (isActing) {
				Mat4 viewProjectionMatrix = cameraHandler.getViewProjectionMatrix();
				double sizeAdj = cameraHandler.sizeAdj();
				activityManager.mouseReleased(e, viewProjectionMatrix, sizeAdj);
			} else if (isSelecting && endP != null){
				Vec2 topLeft = new Vec2(startP).maximize(endP);
				Vec2 botRight = new Vec2(endP).minimize(startP);
				SelectionBoxHelper viewBox = cameraHandler.getSelectionBoxHelper(topLeft, botRight);

				double sizeAdj = cameraHandler.sizeAdj();
				activityManager.mouseReleased(e, viewBox, sizeAdj);
			}
		}

		startP = null;
		endP = null;
		isActing = false;
		isSelecting = false;
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
		}
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		cameraHandler.doZoom(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (endP != null) {
			int modifiersEx = e.getModifiersEx();
//			System.out.println("prefPan: " + programPreferences.getThreeDCameraPanMouseEx() + ", prefSpin: " + programPreferences.getThreeDCameraSpinMouseEx() + ", mouseEx: " + modifiersEx);

			endOld.set(endP);
			endP = setPoint(e, endP);
			vec2Temp.set(endP).sub(endOld);
			Nav3DMousePrefs nav3DMousePrefs = programPreferences.getNav3DMousePrefs();
			if (MouseEventHelpers.matches(modifiersEx, nav3DMousePrefs.getKeyStroke(Nav3DMouseAction.CAMERA_PAN))) {
//				System.out.println("transl x: " + (e.getX() - endP.y) + " (" + e.getX() + "-" + endP.y + ")" + ", transl y: " + (e.getY() - endP.z) + " (" + e.getY() + "-" + endP.z + ")");
				cameraHandler.translate(vec2Temp.x, vec2Temp.y);
			} else if (MouseEventHelpers.matches(modifiersEx, nav3DMousePrefs.getKeyStroke(Nav3DMouseAction.CAMERA_SPIN))) {
				vec2Temp.scale((float) Math.toDegrees(1));
				cameraHandler.rotate(vec2Temp.x, vec2Temp.y);
			} else if ((isActing || isSelecting) && activityManager != null) {
				Mat4 viewProjectionMatrix = cameraHandler.getViewProjectionMatrix();
				double sizeAdj = cameraHandler.sizeAdj();
				activityManager.mouseDragged(e, viewProjectionMatrix, sizeAdj);
			}
		}

	}

	private Vec2 setPoint(MouseEvent e, Vec2 point) {
		Component component = e.getComponent();
		float xRatio = 2.0f * ((float) e.getX()) / (float) component.getWidth() - 1.0f;
		float yRatio = 1.0f - 2.0f * ((float) e.getY()) / (float) component.getHeight();
		if(point == null){
			point = new Vec2(xRatio, yRatio);
		} else {
			point.set(xRatio, yRatio);
		}
		return point;
	}

	public boolean isActing() {
		return isActing;
	}

	public boolean isSelecting() {
		return isSelecting;
	}

	public Vec2 getEndP() {
		return endP;
	}

	public Vec2 getStartP() {
		return startP;
	}

	Vec3 vec3Heap = new Vec3();
	public Vec3 getStart(){
		return vec3Heap.set(startP.x, startP.y, 0);
	}
	public Vec3 getEnd(){
		return vec3Heap.set(endP.x, endP.y, 0);
	}

}
