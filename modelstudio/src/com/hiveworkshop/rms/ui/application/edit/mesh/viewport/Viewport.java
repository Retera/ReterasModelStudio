package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.ViewportModelRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;

public class Viewport extends ViewportView {
	Timer paintTimer;

	private final ViewportModelRenderer viewportModelRenderer;
	private final LinkRenderingVisitorAdapter linkRenderingVisitorAdapter;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final ModelEditorManager modelEditorManager;
	private final Vec3 facingVector;
	private View view;

	long totTempRenderTime;
	long renderCount;


	public Viewport(byte d1, byte d2, ModelHandler modelHandler, ProgramPreferences programPreferences, ViewportActivity activityListener, ModelStructureChangeListener modelStructureChangeListener, CoordDisplayListener coordDisplayListener, ModelEditorManager modelEditorManager, ViewportTransferHandler viewportTransferHandler, ViewportListener viewportListener) {
		super(modelHandler, d1, d2, new Dimension(200, 200), programPreferences, activityListener, viewportListener, coordDisplayListener);
		// Dimension 1 and Dimension 2, these specify which dimensions to display.
		// the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z dimensions

		this.modelStructureChangeListener = modelStructureChangeListener;
		this.modelEditorManager = modelEditorManager;
		setupCopyPaste(viewportTransferHandler);

		viewport = this;

		contextMenu = new ViewportPopupMenu(this, this.modelHandler, this.modelEditorManager);
		add(contextMenu);

		viewportModelRenderer = new ViewportModelRenderer(programPreferences.getVertexSize());
		linkRenderingVisitorAdapter = new LinkRenderingVisitorAdapter(programPreferences);

		facingVector = new Vec3(0, 0, 0);
		final byte unusedXYZ = CoordSysUtils.getUnusedXYZ(coordinateSystem);
		facingVector.setCoord(unusedXYZ, unusedXYZ == 0 ? 1 : -1);

		paintTimer = new Timer(16, e -> {
			repaint();
			if (!isShowing()) {
				paintTimer.stop();
			}
		});
		paintTimer.start();
	}

	public void setView(View view) {
		this.view = view;
	}

	public void setupViewportBackground(ProgramPreferences programPreferences) {
		// if (programPreferences.isInvertedDisplay()) {
		// setBackground(Color.DARK_GRAY.darker());
		// } else {setBackground(new Color(255, 255, 255));}
		setBackground(programPreferences.getBackgroundColor());
	}

	private void setupCopyPaste(ViewportTransferHandler viewportTransferHandler) {
		setTransferHandler(viewportTransferHandler);
		ActionMap map = getActionMap();
		map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
		map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
		map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
		setFocusable(true);
	}

	public void paintComponent(Graphics g, int vertexSize) {
//		super.paintComponent(g);
		long renderStart = System.nanoTime();
		if (programPreferences.show2dGrid()) {
			drawGrid(g);
		}

		Graphics2D graphics2d = (Graphics2D) g;

		// dispMDL.drawGeosets(g, this, 1);
		// dispMDL.drawPivots(g, this, 1);
		// dispMDL.drawCameras(g, this, 1);
		if (modelEditorManager.getModelEditor().editorWantsAnimation()) {
			Stroke stroke = graphics2d.getStroke();
			graphics2d.setStroke(new BasicStroke(3));
			modelHandler.getRenderModel().updateNodes(false);

			linkRenderingVisitorAdapter.reset(graphics2d, coordinateSystem, modelHandler);

			graphics2d.setStroke(stroke);

			viewportModelRenderer.reset(graphics2d, programPreferences, coordinateSystem, modelHandler, true);

			activityListener.render(graphics2d, coordinateSystem, modelHandler.getRenderModel());
		} else {
			viewportModelRenderer.reset(graphics2d, programPreferences, coordinateSystem, modelHandler, false);

			activityListener.renderStatic(graphics2d, coordinateSystem);
		}

		getColor(g, coordinateSystem.getPortFirstXYZ());
		g.drawLine((int) Math.round(coordinateSystem.viewX(0)), (int) Math.round(coordinateSystem.viewY(0)), (int) Math.round(coordinateSystem.viewX(5)), (int) Math.round(coordinateSystem.viewY(0)));

		getColor(g, coordinateSystem.getPortSecondXYZ());
		g.drawLine((int) Math.round(coordinateSystem.viewX(0)), (int) Math.round(coordinateSystem.viewY(0)), (int) Math.round(coordinateSystem.viewX(0)), (int) Math.round(coordinateSystem.viewY(5)));


		adjustAndRunPaintTimer(renderStart);
	}

	public void adjustAndRunPaintTimer(long renderStart) {
		long renderEnd = System.nanoTime();
		long currFrameRenderTime = renderEnd - renderStart;

//		minRenderTime = Math.min(currFrameRenderTime, minRenderTime);
//		maxRenderTime = Math.max(currFrameRenderTime, maxRenderTime);
//		totTempRenderTime += currFrameRenderTime;
//		renderCount += 1;
//		if (renderCount >= 100) {
////			long millis = ((totTempRenderTime / renderCount) / 1000000L) + 1;
//			long millis = ((totTempRenderTime/1000000L) / renderCount);
//			System.out.println("millis: " + millis);
//			if (millis > paintTimer.getDelay()) {
//				int millis2 = (int) (millis * 5);
//				System.out.println("min, delay=" + millis2);
//				paintTimer.setDelay(millis2);
//			} else if (millis < paintTimer.getDelay()) {
//				int max2 = Math.max(16, (int) (millis * 5));
//				System.out.println("max, delay=" + max2);
//				paintTimer.setDelay(max2);
//			}
//			System.out.println("min render time: " + (minRenderTime/1000000L) + "ms, max render time: " + (maxRenderTime/1000000L) + "ms");
//			minRenderTime = Long.MAX_VALUE;
//			maxRenderTime = 0;
//		}

		totTempRenderTime += currFrameRenderTime;
		renderCount += 1;
		if (renderCount >= 100) {
			long millis = ((totTempRenderTime / 1000000L) / renderCount) + 1;
			paintTimer.setDelay(Math.max(16, (int) (millis * 5)));
//			System.out.println("delay: " + paintTimer.getDelay());

			totTempRenderTime = 0;
			renderCount = 0;
		}
		boolean showing = isShowing();
		boolean running = paintTimer.isRunning();
		if (showing && !running) {
			paintTimer.start();
		} else if (!showing && running) {
			paintTimer.stop();
		}
	}

	private void getColor(Graphics g, byte dir) {
		switch (dir) {
			case 0 -> g.setColor(new Color(0, 255, 0));
			case 1 -> g.setColor(new Color(255, 0, 0));
			case 2 -> g.setColor(new Color(0, 0, 255));
		}
	}

	public void setViewportAxises(String name, byte dim1, byte dim2) {
		view.getViewProperties().setTitle(name);
		coordinateSystem.setDimensions(dim1, dim2);
	}

	public ModelEditorManager getModelEditorManager() {
		return modelEditorManager;
	}

	public static class DropLocation extends TransferHandler.DropLocation {
		protected DropLocation(Point dropPoint) {
			super(dropPoint);
		}
	}

	public ModelView getModelView() {
		return modelHandler.getModelView();
	}

	public Point getLastMouseMotion() {
		return lastMouseMotion;
	}

	public ModelStructureChangeListener getModelStructureChangeListener() {
		return modelStructureChangeListener;
	}

	public Vec3 getFacingVector() {
		return facingVector;
	}

}