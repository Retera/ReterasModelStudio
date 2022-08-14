package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.ViewportHelpers;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.util.List;

public class ExportFrameViewportCanvas extends ViewportCanvas {
	private TimeEnvironmentImpl timeEnvironment;
	private List<ByteBuffer> byteBuffers;
	private List<Integer> renderTimes;
	private int currFrame = 0;
	private boolean doExp;
	private Dimension expDimension;

	public ExportFrameViewportCanvas(ProgramPreferences programPreferences, boolean portrait) throws LWJGLException {
		super(programPreferences, portrait);
	}


	public ExportFrameViewportCanvas setModel(RenderModel renderModel) {
		if(renderModel != null){
			timeEnvironment = renderModel.getTimeEnvironment();
			ExtLog extents = renderModel.getModel().getExtents();
			cameraManager.loadDefaultCameraFor(ViewportHelpers.getBoundsRadius(renderModel.getTimeEnvironment(), extents));
			bufferFiller = renderModel.getBufferFiller();
		}
		return this;
	}


	@Override
	public void paintGL() {
		if(expDimension != null){
			setSize(expDimension);
		} else {
			setSize(getParent().getSize());
		}
		if(bufferFiller != null){
			if(doExp){
				if(currFrame<renderTimes.size()){
					timeEnvironment.setAnimationTime(renderTimes.get(currFrame));
					bufferFiller.forceUpdate();
					ByteBuffer pixels = bufferFiller.paintGL2(cameraManager, viewportSettings, this.getWidth(), this.getHeight());
					SwingUtilities.invokeLater(() -> byteBuffers.add(pixels));
				} else {
					runOnFramesDone();
					expDimension = null;
					doExp = false;
				}
				currFrame++;
			}
			bufferFiller.paintCanvas(viewportSettings, cameraManager, mouseAdapter, this);
			try {
				swapBuffers();
			} catch (LWJGLException e) {
				e.printStackTrace();
				paintTimer.stop();
				doPaint = false;
				exceptionTimeout = System.currentTimeMillis() + 2000L;
			}
		}

		if (isShowing() && !paintTimer.isRunning()) {
			if(exceptionTimeout < System.currentTimeMillis()){
				doPaint = true;
				paintTimer.restart();
			}
		} else if (!isShowing() && paintTimer.isRunning()) {
			paintTimer.stop();
		}
	}



	public void initGenerateFrames(List<ByteBuffer> byteBuffers, List<Integer> renderTimes, Dimension expDimension) {
		this.byteBuffers = byteBuffers;
		this.renderTimes = renderTimes;
		this.expDimension = expDimension;
		this.currFrame = 0;
		paintTimer.stop();

		timeEnvironment.setAnimationTime(0);
		timeEnvironment.setLive(false);

		repaint();
		timeEnvironment.setLive(true);
		repaint();
		doExp = true;
	}

	private void runOnFramesDone(){
		if(onDoneRunnable != null){
			onDoneRunnable.run();
		}
	}

	Runnable onDoneRunnable;
	public void setOnDoneRunnable(Runnable onDoneRunnable){
		this.onDoneRunnable = onDoneRunnable;
	}
}
