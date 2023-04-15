package com.hiveworkshop.rms.ui.application.viewer;


import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.AbstractCamera;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.preferences.CameraControlPrefs;
import com.hiveworkshop.rms.ui.preferences.CameraShortCut;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeylistenerThing extends KeyAdapter {
	private final AbstractCamera cameraHandler;
	private ProgramPreferences prefs;

	public KeylistenerThing(AbstractCamera cameraHandler, ProgramPreferences programPreferences) {
		this.cameraHandler = cameraHandler;
		this.prefs = programPreferences;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		int controlAdj = e.getModifiersEx() == prefs.getCameraOppositeKB() ? -1 : 1;
		KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);
//		if(e.getKeyCode() != KeyEvent.VK_CONTROL){
//			System.out.println("e: " + e);
//			System.out.println("e.getKeyCode: " + keyStroke);
////			System.out.println("e.getKeyCode | ctrl: " + (keyStroke | KeyEvent.CTRL_DOWN_MASK));
//			System.out.println("e.getExtendedKeyCode: " + e.getExtendedKeyCode());
//			System.out.println("e.getModifiersEx: " + e.getModifiersEx());
//			System.out.println("e.getID: " + e.getID());
//			KeyStroke keyStrokeForEvent = KeyStroke.getKeyStrokeForEvent(e);
//			System.out.println("keyStrokeForEvent: " + keyStrokeForEvent);
//			System.out.println("keyStrokeForEvent.getKeyCode: " + keyStrokeForEvent.getKeyCode());
//			System.out.println("keyStrokeForEvent.getModifiers: " + keyStrokeForEvent.getModifiers());
//
//		}
		CameraControlPrefs cameraPrefs = prefs.getCameraControlPrefs();
		if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.TOP)) {
			cameraHandler.setCameraRotation(0, -90, 0);
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.BOTTOM)) {
			// Top (0,90) / Bottom (0,-90)
			cameraHandler.setCameraRotation(0,  90, 0);
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.FRONT)) {
			cameraHandler.setCameraRotation(0, 0, 0);
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.BACK)) {
			//Front (0,0) / Back (180,0)
			cameraHandler.setCameraRotation(180, 0, 0);
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.LEFT)) {
			cameraHandler.setCameraRotation(90, 0, 0);
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.RIGHT)) {
			//Left (90,0) / Right (-90,0)
			cameraHandler.setCameraRotation(-90, 0, 0);
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.ROTATE_RIGHT)) {
			//Left (90,0) / Right (-90,0)
			cameraHandler.rotate(-15.0, 0.0);
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.ROTATE_LEFT)) {
			//Left (90,0) / Right (-90,0)
			cameraHandler.rotate(15.0, 0.0);
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.ROTATE_UP)) {
			//Left (90,0) / Right (-90,0)
			cameraHandler.rotate(0.0, -15.0);
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.ROTATE_DOWN)) {
			//Left (90,0) / Right (-90,0)
			cameraHandler.rotate(0.0, 15.0);
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.PAN_RIGHT)) {
			//Left (90,0) / Right (-90,0)
//			cameraHandler.translate(10.0, 0);
			cameraHandler.translate(-0.10, 0);
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.PAN_LEFT)) {
			//Left (90,0) / Right (-90,0)
//			cameraHandler.translate(-10.0, 0.0);
			cameraHandler.translate(.10, 0.0);
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.PAN_UP)) {
			//Left (90,0) / Right (-90,0)
			cameraHandler.translate(0.0, .10);
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.PAN_DOWN)) {
			//Left (90,0) / Right (-90,0)
			cameraHandler.translate(0.0, -.10);
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.TOGGLE_ORTHO)) {
			// Orto Mode
			cameraHandler.toggleOrtho();
//	        System.out.println("VK_O");
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.LOC_ZOOM_RESET)) {
			// resetCamera (ish)
			cameraHandler.resetZoom();
			cameraHandler.resetCamera();
		} else if (keyStroke == KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0)) {
			cameraHandler.moveTargetDepth(-10);
			System.out.println("X -10");
		} else if (keyStroke == KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,0)) {
			cameraHandler.moveTargetDepth(10);
			System.out.println("X +10");
		} else if(keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.FOCUS_SELECTED)){
			ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
			if(modelPanel != null){
				ModelView modelView = modelPanel.getModelView();
				if(!modelView.isEmpty()){
					cameraHandler.setTargetPoint(modelView.getSelectionCenter());
				}
			}
		}
	}
//	public void keyPressed(KeyEvent e) {
//		super.keyPressed(e);
//		int controlAdj = e.getModifiersEx() == prefs.getCameraOppositeKB() ? -1 : 1;
//		if(e.getKeyCode() != KeyEvent.VK_CONTROL){
//			System.out.println("e:" + e);
//			System.out.println("e.getKeyCode:" + e.getKeyCode());
//			System.out.println("e.getKeyCode | ctrl:" + (e.getKeyCode() | KeyEvent.CTRL_DOWN_MASK));
//			System.out.println("e.getExtendedKeyCode:" + e.getExtendedKeyCode());
//			System.out.println("e.getModifiersEx:" + e.getModifiersEx());
//			System.out.println("e.getID:" + e.getID());
//
//		}
//		CameraControlPrefs cameraPrefs = prefs.getCameraControlPrefs();
//		if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.TOP)) {
//			cameraHandler.setCameraRotation(0, controlAdj * 90);
//		} else if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.BOTTOM)) {
//			// Top (0,90) / Bottom (0,-90)
//			cameraHandler.setCameraRotation(0, controlAdj * 90);
//		} else if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.FRONT)) {
//			cameraHandler.setCameraRotation(90 - controlAdj * 90, 0);
//		} else if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.BACK)) {
//			//Front (0,0) / Back (180,0)
//			cameraHandler.setCameraRotation(90 - controlAdj * 90, 0);
//		} else if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.LEFT)) {
//			cameraHandler.setCameraRotation(controlAdj * 90, 0);
//		} else if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.RIGHT)) {
//			//Left (90,0) / Right (-90,0)
//			cameraHandler.setCameraRotation(controlAdj * 90, 0);
//		} else if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.ROTATE_SIDE_NEG)) {
//			//Left (90,0) / Right (-90,0)
//			cameraHandler.rotate(-15.0, 0.0);
//		} else if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.ROTATE_SIDE_POS)) {
//			//Left (90,0) / Right (-90,0)
//			cameraHandler.rotate(15.0, 0.0);
//		} else if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.ROTATE_UP)) {
//			//Left (90,0) / Right (-90,0)
//			cameraHandler.rotate(0.0, -15.0);
//		} else if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.ROTATE_DOWN)) {
//			//Left (90,0) / Right (-90,0)
//			cameraHandler.rotate(0.0, 15.0);
//		} else if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.PAN_SIDE_NEG)) {
//			//Left (90,0) / Right (-90,0)
//			cameraHandler.translate(10.0, 0);
//		} else if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.PAN_SIDE_POS)) {
//			//Left (90,0) / Right (-90,0)
//			cameraHandler.translate(-10.0, 0.0);
//		} else if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.PAN_UP)) {
//			//Left (90,0) / Right (-90,0)
//			cameraHandler.translate(0.0, 10.0);
//		} else if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.PAN_DOWN)) {
//			//Left (90,0) / Right (-90,0)
//			cameraHandler.translate(0.0, -10.0);
//		} else if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.TOGGLE_ORTHO)) {
//			// Orto Mode
//			cameraHandler.toggleOrtho();
////	        System.out.println("VK_O");
//		} else if (e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.LOC_ZOOM_RESET)) {
//			// resetCamera (ish)
//			cameraHandler.resetZoom();
//			cameraHandler.setPosition(0, 0);
//			cameraHandler.resetCamera();
//		} else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
//			cameraHandler.moveTargetDepth(-10);
//			System.out.println("X -10");
//		} else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
//			cameraHandler.moveTargetDepth(10);
//			System.out.println("X +10");
//		} else if(e.getKeyCode() == cameraPrefs.getKeyCode(CameraShortCut.FOCUS_SELECTED)){
//			ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
//			if(modelPanel != null){
//				ModelView modelView = modelPanel.getModelView();
//				if(!modelView.isEmpty()){
//					cameraHandler.setTargetPoint(modelView.getSelectionCenter());
//				}
//			}
//		}
//	}




//	public void keyPressed(KeyEvent e) {
//		super.keyPressed(e);
////        int controlAdj = e.isControlDown() ? -1 : 1;
//		int controlAdj = e.getModifiersEx() == prefs.getCameraOppositeKB() ? -1 : 1;
////	    System.out.println("modX: " + e.getModifiersEx() + ", " + KeyEvent.CTRL_DOWN_MASK);
//		if (e.getKeyCode() == prefs.getCameraTopKB()) {
//			// Top (0,90) / Bottom (0,-90)
//			cameraHandler.setCameraRotation(0, controlAdj * 90);
//		}
//		if (e.getKeyCode() == prefs.getCameraFrontKB()) {
//			//Front (0,0) / Back (180,0)
//			cameraHandler.setCameraRotation(90 - controlAdj * 90, 0);
//		}
//		if (e.getKeyCode() == prefs.getCameraSideKB()) {
//			//Left (90,0) / Right (-90,0)
//			cameraHandler.setCameraRotation(controlAdj * 90, 0);
//		}
//		if (e.getKeyCode() == prefs.getCameraRotateSideNegKB()) {
//			//Left (90,0) / Right (-90,0)
//			if(0 < controlAdj){
//				cameraHandler.rotate(-15.0, 0.0);
//			} else {
//				cameraHandler.translate(10.0, 0);
//			}
//		}
//		if (e.getKeyCode() == prefs.getCameraRotateSidePosKB()) {
//			//Left (90,0) / Right (-90,0)
//			if(0 < controlAdj){
//				cameraHandler.rotate(15.0, 0.0);
//			} else {
//				cameraHandler.translate(-10.0, 0.0);
//			}
//		}
//		if (e.getKeyCode() == prefs.getCameraRotateUpKB()) {
//			//Left (90,0) / Right (-90,0)
//			if(0 < controlAdj){
//				cameraHandler.rotate(0.0, -15.0);
//			} else {
//				cameraHandler.translate(0.0, 10.0);
//			}
//		}
//		if (e.getKeyCode() == prefs.getCameraRotateDownKB()) {
//			//Left (90,0) / Right (-90,0)
//			if(0 < controlAdj){
//				cameraHandler.rotate(0.0, 15.0);
//			} else {
//				cameraHandler.translate(0.0, -10.0);
//			}
//		}
//		if (e.getKeyCode() == prefs.getCameraToggleOrtho()) {
//			// Orto Mode
//			cameraHandler.toggleOrtho();
////	        System.out.println("VK_O");
//		}
//		if (e.getKeyCode() == prefs.getCameraLocZoomReset()) {
//			// resetCamera (ish)
//			cameraHandler.resetZoom();
//			cameraHandler.setPosition(0, 0);
//			cameraHandler.resetCamera();
//		}
////	    if (e.getKeyCode() == KeyEvent.VK_X) {
////		    if (e.isControlDown()) {
////			    cameraHandler.rot(-45, 0, 0);
////		    } else {
////			    cameraHandler.rot(45, 0, 0);
////		    }
////	    }
////	    if (e.getKeyCode() == KeyEvent.VK_Y) {
////		    if (e.isControlDown()) {
////			    cameraHandler.rot(0, -45, 0);
////		    } else {
////			    cameraHandler.rot(0, 45, 0);
////		    }
////	    }
////	    if (e.getKeyCode() == KeyEvent.VK_Q) {
////		    if (e.isControlDown()) {
////			    cameraHandler.rot(0, 0, -45);
////		    } else {
////			    cameraHandler.rot(0, 0, 45);
////		    }
////	    }
//
////		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
////			cameraHandler.translate2(0, -10, 0);
////			System.out.println("Y -10");
////		}
////		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
////			cameraHandler.translate2(0, 10, 0);
////			System.out.println("Y +10");
////		}
////		if (e.getKeyCode() == KeyEvent.VK_UP) {
////			cameraHandler.translate2(0, 0, 10);
////			System.out.println("Z +10");
////		}
////		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
////			cameraHandler.translate2(0, 0, -10);
////			System.out.println("Z -10");
////		}
//		if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
//			cameraHandler.moveTargetDepth(-10);
//			System.out.println("X -10");
//		}
//		if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
//			cameraHandler.moveTargetDepth(10);
//			System.out.println("X +10");
//		}
//
////		if(e.getKeyCode() == KeyEvent.VK_COMMA){
//		if(e.getKeyCode() == KeyEvent.VK_DECIMAL){
//			ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
//			if(modelPanel != null){
//				ModelView modelView = modelPanel.getModelView();
//				if(!modelView.isEmpty()){
//					cameraHandler.setTargetPoint(modelView.getSelectionCenter());
//				}
//			}
//		}
//	}
}
