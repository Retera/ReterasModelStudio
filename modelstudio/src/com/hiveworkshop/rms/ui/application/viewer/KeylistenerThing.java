package com.hiveworkshop.rms.ui.application.viewer;


import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeylistenerThing extends KeyAdapter {
    private final CameraHandler cameraHandler;
    private final PerspectiveViewport viewport;
    private ProgramPreferences prefs;

    public KeylistenerThing(CameraHandler cameraHandler, ProgramPreferences programPreferences, PerspectiveViewport viewport){
        this.cameraHandler = cameraHandler;
        this.viewport = viewport;
        this.prefs = programPreferences;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        int controlAdj = e.isControlDown() ? -1 : 1;
        if (e.getKeyCode() == KeyEvent.VK_NUMPAD7) {
            // Top (0,90) / Bottom (0,-90)
            cameraHandler.setCameraRotation(0,controlAdj*90);
        }
        if (e.getKeyCode() == KeyEvent.VK_NUMPAD1) {
            //Front (0,0) / Back (180,0)
            cameraHandler.setCameraRotation(90 -controlAdj*90,0);
        }
        if (e.getKeyCode() == KeyEvent.VK_NUMPAD3) {
            //Left (90,0) / Right (-90,0)
            cameraHandler.setCameraRotation(controlAdj*90,0);
        }
        if (e.getKeyCode() == KeyEvent.VK_O) {
            // Orto Mode
            cameraHandler.toggleOrtho();
            System.out.println("VK_O");
        }
        if (e.getKeyCode() == KeyEvent.VK_X) {
            if (e.isControlDown()) {
                cameraHandler.rot(-45, 0, 0);
            } else {
                cameraHandler.rot(45, 0, 0);
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_Y) {
            if (e.isControlDown()) {
                cameraHandler.rot(0, -45, 0);
            } else {
                cameraHandler.rot(0, 45, 0);
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_Q) {
            if (e.isControlDown()) {
                cameraHandler.rot(0, 0, -45);
            } else {
                cameraHandler.rot(0, 0, 45);
            }
        }
    }
}
