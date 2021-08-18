package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class MouseListenerThing extends MouseAdapter {

	private final CameraHandler cameraHandler;
	private final ProgramPreferences programPreferences;
	private ViewportActivityManager activityManager;

//	private Timer clickTimer;
//	private boolean mouseInBounds = false;

	//	Vec3 ccc2 = null;
	private Vec3 startP = null;
	private Vec3 endP = null;

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
//		clickTimer.setRepeats(true);
//		clickTimer.start();
//		mouseInBounds = true;
	}

	@Override
	public void mouseExited(final MouseEvent e) {
//		if ((cameraHandler.getCameraSpinStartPoint() == null) && (cameraHandler.getActStart() == null) && (cameraHandler.getCameraPanStartPoint() == null)) {
//			clickTimer.stop();
//		}
//		mouseInBounds = false;
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		startP = new Vec3(0, e.getX(), e.getY());
		endP = new Vec3(0, e.getX(), e.getY());
		int modifiersEx = e.getModifiersEx();
//		if (programPreferences.getThreeDCameraPanButton().isButton(e)) {
		if (programPreferences.getThreeDCameraPanMouseEx() == modifiersEx) {
//			cameraHandler.startPan(e);
//		} else if (programPreferences.getThreeDCameraSpinButton().isButton(e)) {
		} else if (programPreferences.getThreeDCameraSpinMouseEx() == modifiersEx) {
//			cameraHandler.startSpinn(e);
//		} else if (e.getButton() == MouseEvent.BUTTON3) {
		} else if (e.getButton() == MouseEvent.BUTTON3) {
//			cameraHandler.startAct(e);
			isActing = true;
			if (activityManager != null) {
				activityManager.mousePressed(e, cameraHandler);
			}
		} else {
			isActing = true;

			if (activityManager != null) {
				activityManager.mousePressed(e, cameraHandler);
			}
//			cameraHandler.startAct(e);
		}
//		ccc2 = new Vec3();
//				System.out.println("camPos: " + cameraPos + ", invQ: " + inverseCameraRotation + ", invYspin: " + inverseCameraRotationYSpin + ", invZspin: " + inverseCameraRotationZSpin);
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (endP != null) {
			endP.set(0, e.getX(), e.getY());
		}
//		if (programPreferences.getThreeDCameraPanButton().isButton(e) && (cameraHandler.getCameraPanStartPoint() != null)) {
//			cameraHandler.finnishPan(e);
//		} else if (programPreferences.getThreeDCameraSpinButton().isButton(e) && (cameraHandler.getCameraSpinStartPoint() != null)) {
//			cameraHandler.finnishSpinn(e);
//		} else if ((e.getButton() == MouseEvent.BUTTON3) && (cameraHandler.getActStart() != null)) {
//			cameraHandler.finnishAct(e);
//		}
//		if (!mouseInBounds && (cameraHandler.getCameraSpinStartPoint() == null) && (cameraHandler.getActStart() == null) && (cameraHandler.getCameraPanStartPoint() == null)) {
//			clickTimer.stop();
//		}
		if (isActing && activityManager != null) {

			activityManager.mouseReleased(e, cameraHandler);
		}

//		ccc2 = null;
		startP = null;
		endP = null;
		isActing = false;
		/*
		 * if( dispMDL != null ) dispMDL.refreshUndo();
		 */
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
		}
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		cameraHandler.doZoom4(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (endP != null) {
			int modifiersEx = e.getModifiersEx();
//			System.out.println("prefPan: " + programPreferences.getThreeDCameraPanMouseEx() + ", prefSpin: " + programPreferences.getThreeDCameraSpinMouseEx() + ", mouseEx: " + modifiersEx);
//			if (programPreferences.getThreeDCameraPanButton().isButton(e)) {
			if (programPreferences.getThreeDCameraPanMouseEx() == modifiersEx) {
//				System.out.println("transl x: " + (e.getX() - endP.y) + " (" + e.getX() + "-" + endP.y + ")" + ", transl y: " + (e.getY() - endP.z) + " (" + e.getY() + "-" + endP.z + ")");
				cameraHandler.translate(-(e.getX() - endP.y), (e.getY() - endP.z));
//			} else if (programPreferences.getThreeDCameraSpinButton().isButton(e)) {
			} else if (programPreferences.getThreeDCameraSpinMouseEx() == modifiersEx) {
				cameraHandler.rotate((e.getX() - endP.y), (e.getY() - endP.z));
			} else if ((e.getButton() == MouseEvent.BUTTON3)) {
			} else if (isActing && activityManager != null) {
				// "act"? should maybe do geometry transformations
				activityManager.mouseDragged(e, cameraHandler);
			}


//		if (ccc2 != null && cameraHandler.getActStart() != null) {
//			System.out.println("mouseDragged!");
//
//			ccc2.set(cameraHandler.getGeoPoint(e.getX(), e.getY()));
//		}


			endP.set(0, e.getX(), e.getY());
		}
	}

	public boolean isActing() {
		return isActing;
	}

	public Vec3 getEndP() {
		return endP;
	}

	public Vec3 getStartP() {
		return startP;
	}

	public Vec3 getEndPGeo1() {
		return cameraHandler.getGeoPoint(startP.y, endP.z);
	}

	public Vec3 getEndPGeo2() {
		return cameraHandler.getGeoPoint(endP.y, endP.z);
	}

	public Vec3 getEndPGeo3() {
		return cameraHandler.getGeoPoint(endP.y, startP.z);
	}

	public Vec3 getStartPGeo() {
		return cameraHandler.getGeoPoint(startP.y, startP.z);
	}

}
