package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class CanvasTracker {
	private final ArrayList<ViewportCanvas> canvasList = new ArrayList<>();
	private Timer updateTimer;
	private BufferFiller bufferFiller;

	public CanvasTracker(){
		bufferFiller = new BufferFiller();
		updateTimer = new Timer(16, e -> {
//		updateTimer = new Timer(200, e -> {
//		updateTimer = new Timer(500, e -> {
			if(isShowing()){
				boolean showNormals = canvasList.stream().anyMatch(canvas -> canvas.getViewportSettings().isShowNormals());
				boolean show3dVerts = canvasList.stream().anyMatch(canvas -> canvas.getViewportSettings().isShow3dVerts());
				boolean renderTextures = canvasList.stream().anyMatch(canvas -> canvas.getViewportSettings().isRenderTextures());
				bufferFiller.updateBuffers(showNormals, show3dVerts, renderTextures);
			}
			updateTimer.restart();
		});
		updateTimer.start();
	}

	public CanvasTracker addCanvas(ViewportCanvas canvas){
		canvasList.add(canvas);
		canvas.setBufferFiller(bufferFiller);
		return this;
	}

	public CanvasTracker removeCanvas(ViewportCanvas canvas){
		canvasList.remove(canvas);
		return this;
	}

	public CanvasTracker setModelPanel(ModelPanel modelPanel){
		bufferFiller.setModel(modelPanel.getModelView(), modelPanel.getEditorRenderModel(), true);
		return this;
	}

	private void update() {
		if (isShowing() && !updateTimer.isRunning()) {
			updateTimer.restart();
		} else if (!isShowing() && updateTimer.isRunning()) {
			updateTimer.stop();
		}
	}

	private boolean isShowing(){
		return canvasList.stream().anyMatch(Component::isShowing);
	}
}
