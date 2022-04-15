package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class MouseListenerThing extends MouseAdapter {

	private final CameraHandler cameraHandler;
	private final ProgramPreferences programPreferences;
	private ViewportActivityManager activityManager;

	private Vec2 startP = null;
	private Vec2 endP = null;
	private final Vec2 vec2Temp = new Vec2();
	private final Vec2 endOld = new Vec2();

	private boolean isSelecting = false;
	private boolean isActing = false;

	public MouseListenerThing(CameraHandler cameraHandler, ProgramPreferences programPreferences) {
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
		int modifiersEx = e.getModifiersEx();
		Mat4 viewPortAntiRotMat = cameraHandler.getViewPortAntiRotMat();
		double sizeAdj = cameraHandler.sizeAdj();
		if ((ProgramGlobals.getPrefs().getSelectMouseButton() & modifiersEx) > 0) {
			isSelecting = true;
			if (activityManager != null) {
				activityManager.mousePressed(e, viewPortAntiRotMat, sizeAdj);
			}
		} else if ((ProgramGlobals.getPrefs().getModifyMouseButton() & modifiersEx) > 0) {
			isActing = true;
			if (activityManager != null) {
				activityManager.mousePressed(e, viewPortAntiRotMat, sizeAdj);
			}
		} else {
			isActing = true;

			if (activityManager != null) {
				activityManager.mousePressed(e, viewPortAntiRotMat, sizeAdj);
			}
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (endP != null) {
			endP = setPoint(e, endP);
		}

		if ((isActing || isSelecting) && activityManager != null) {

			Mat4 viewPortAntiRotMat = cameraHandler.getViewPortAntiRotMat();
			double sizeAdj = cameraHandler.sizeAdj();
			activityManager.mouseReleased(e, viewPortAntiRotMat, sizeAdj);
			System.out.println("getStartPGeo: " + getStartPGeo() + " (" + startP + "), " + "getEndPGeo2: " + getEndPGeo2() + " (" + endP + "), ");
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
			if (programPreferences.getThreeDCameraPanMouseEx() == modifiersEx) {
//				System.out.println("transl x: " + (e.getX() - endP.y) + " (" + e.getX() + "-" + endP.y + ")" + ", transl y: " + (e.getY() - endP.z) + " (" + e.getY() + "-" + endP.z + ")");
				cameraHandler.translate(-vec2Temp.x, vec2Temp.y);
			} else if (programPreferences.getThreeDCameraSpinMouseEx() == modifiersEx) {
				cameraHandler.rotate(vec2Temp.x, vec2Temp.y);
			} else if ((isActing || isSelecting) && activityManager != null) {
				Mat4 viewPortAntiRotMat = cameraHandler.getViewPortAntiRotMat();
				double sizeAdj = cameraHandler.sizeAdj();
				activityManager.mouseDragged(e, viewPortAntiRotMat, sizeAdj);
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

	public Vec3 getEndPGeo1() {
		return cameraHandler.getGeoPoint(startP.x, endP.y);
	}

	public Vec3 getEndPGeo2() {
		return cameraHandler.getGeoPoint(endP.x, endP.y);
	}

	public Vec3 getEndPGeo3() {
		return cameraHandler.getGeoPoint(endP.x, startP.y);
	}

	public Vec3 getStartPGeo() {
		return cameraHandler.getGeoPoint(startP.x, startP.y);
	}

	Vec3 vec3Heap = new Vec3();
	public Vec3 getStart(){
		return vec3Heap.set(startP.x, startP.y, 0);
	}
	public Vec3 getEnd(){
		return vec3Heap.set(endP.x, endP.y, 0);
	}

}
