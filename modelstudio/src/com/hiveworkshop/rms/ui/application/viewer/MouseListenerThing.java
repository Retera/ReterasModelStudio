package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class MouseListenerThing extends MouseAdapter {

	private final CameraHandler cameraHandler;
	private final ProgramPreferences programPreferences;
	private ViewportActivityManager activityManager;

	private Vec2 startP = null;
	private Vec2 endP = null;

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
		startP = new Vec2(e.getX(), e.getY());
		endP = new Vec2(e.getX(), e.getY());
		int modifiersEx = e.getModifiersEx();
		if ((ProgramGlobals.getPrefs().getSelectMouseButton() & modifiersEx) > 0) {
			isSelecting = true;
			if (activityManager != null) {
				activityManager.mousePressed(e, cameraHandler);
			}
		} else if ((ProgramGlobals.getPrefs().getModifyMouseButton() & modifiersEx) > 0) {
			isActing = true;
			if (activityManager != null) {
				activityManager.mousePressed(e, cameraHandler);
			}
		} else {
			isActing = true;

			if (activityManager != null) {
				activityManager.mousePressed(e, cameraHandler);
			}
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (endP != null) {
			endP.set(e.getX(), e.getY());
		}

		if ((isActing || isSelecting) && activityManager != null) {

			activityManager.mouseReleased(e, cameraHandler);
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
			if (programPreferences.getThreeDCameraPanMouseEx() == modifiersEx) {
//				System.out.println("transl x: " + (e.getX() - endP.y) + " (" + e.getX() + "-" + endP.y + ")" + ", transl y: " + (e.getY() - endP.z) + " (" + e.getY() + "-" + endP.z + ")");
				cameraHandler.translate(-(e.getX() - endP.x), (e.getY() - endP.y));
			} else if (programPreferences.getThreeDCameraSpinMouseEx() == modifiersEx) {
				cameraHandler.rotate((e.getX() - endP.x), (e.getY() - endP.y));
			} else if ((isActing || isSelecting) && activityManager != null) {
				activityManager.mouseDragged(e, cameraHandler);
			}
			endP.set(e.getX(), e.getY());
		}
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

}
