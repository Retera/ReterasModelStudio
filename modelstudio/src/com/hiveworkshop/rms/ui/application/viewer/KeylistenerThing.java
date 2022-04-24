package com.hiveworkshop.rms.ui.application.viewer;


import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.CameraManager;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeylistenerThing extends KeyAdapter {
	private final CameraManager cameraHandler;
	private ProgramPreferences prefs;

	public KeylistenerThing(CameraManager cameraHandler, ProgramPreferences programPreferences) {
		this.cameraHandler = cameraHandler;
		this.prefs = programPreferences;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
//        int controlAdj = e.isControlDown() ? -1 : 1;
		int controlAdj = e.getModifiersEx() == prefs.getCameraOppositeKB() ? -1 : 1;
//	    System.out.println("modX: " + e.getModifiersEx() + ", " + KeyEvent.CTRL_DOWN_MASK);
		if (e.getKeyCode() == prefs.getCameraTopKB()) {
			// Top (0,90) / Bottom (0,-90)
			cameraHandler.setCameraRotation(0, controlAdj * 90);
		}
		if (e.getKeyCode() == prefs.getCameraFrontKB()) {
			//Front (0,0) / Back (180,0)
			cameraHandler.setCameraRotation(90 - controlAdj * 90, 0);
		}
		if (e.getKeyCode() == prefs.getCameraSideKB()) {
			//Left (90,0) / Right (-90,0)
			cameraHandler.setCameraRotation(controlAdj * 90, 0);
		}
		if (e.getKeyCode() == prefs.getCameraRotateSideNegKB()) {
			//Left (90,0) / Right (-90,0)
			if(0 < controlAdj){
				cameraHandler.rotate(-15.0, 0.0);
			} else {
				cameraHandler.translate(10.0, 0);
			}
		}
		if (e.getKeyCode() == prefs.getCameraRotateSidePosKB()) {
			//Left (90,0) / Right (-90,0)
			if(0 < controlAdj){
				cameraHandler.rotate(15.0, 0.0);
			} else {
				cameraHandler.translate(-10.0, 0.0);
			}
		}
		if (e.getKeyCode() == prefs.getCameraRotateUpKB()) {
			//Left (90,0) / Right (-90,0)
			if(0 < controlAdj){
				cameraHandler.rotate(0.0, -15.0);
			} else {
				cameraHandler.translate(0.0, 10.0);
			}
		}
		if (e.getKeyCode() == prefs.getCameraRotateDownKB()) {
			//Left (90,0) / Right (-90,0)
			if(0 < controlAdj){
				cameraHandler.rotate(0.0, 15.0);
			} else {
				cameraHandler.translate(0.0, -10.0);
			}
		}
		if (e.getKeyCode() == prefs.getCameraToggleOrtho()) {
			// Orto Mode
			cameraHandler.toggleOrtho();
//	        System.out.println("VK_O");
		}
		if (e.getKeyCode() == prefs.getCameraLocZoomReset()) {
			// resetCamera (ish)
			cameraHandler.resetZoom();
			cameraHandler.setPosition(0, 0);
			cameraHandler.resetCamera();
		}
//	    if (e.getKeyCode() == KeyEvent.VK_X) {
//		    if (e.isControlDown()) {
//			    cameraHandler.rot(-45, 0, 0);
//		    } else {
//			    cameraHandler.rot(45, 0, 0);
//		    }
//	    }
//	    if (e.getKeyCode() == KeyEvent.VK_Y) {
//		    if (e.isControlDown()) {
//			    cameraHandler.rot(0, -45, 0);
//		    } else {
//			    cameraHandler.rot(0, 45, 0);
//		    }
//	    }
//	    if (e.getKeyCode() == KeyEvent.VK_Q) {
//		    if (e.isControlDown()) {
//			    cameraHandler.rot(0, 0, -45);
//		    } else {
//			    cameraHandler.rot(0, 0, 45);
//		    }
//	    }

//		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
//			cameraHandler.translate2(0, -10, 0);
//			System.out.println("Y -10");
//		}
//		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
//			cameraHandler.translate2(0, 10, 0);
//			System.out.println("Y +10");
//		}
//		if (e.getKeyCode() == KeyEvent.VK_UP) {
//			cameraHandler.translate2(0, 0, 10);
//			System.out.println("Z +10");
//		}
//		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//			cameraHandler.translate2(0, 0, -10);
//			System.out.println("Z -10");
//		}
		if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
			cameraHandler.moveTargetDepth(-10);
			System.out.println("X -10");
		}
		if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
			cameraHandler.moveTargetDepth(10);
			System.out.println("X +10");
		}
	}
}
