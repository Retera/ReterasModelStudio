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
		KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);

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
		} else if (keyStroke == cameraPrefs.getKeyStroke(CameraShortCut.FOCUS_SELECTED)) {
			ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
			if (modelPanel != null) {
				ModelView modelView = modelPanel.getModelView();
				if (!modelView.isEmpty()) {
					cameraHandler.setTargetPoint(modelView.getSelectionCenter());
				}
			}
		}
	}
}
