package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.AnimatedViewportModelRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;

public class Viewport extends ViewportView implements ModelEditorChangeListener {
	Timer paintTimer;

	private final ViewportModelRenderer viewportModelRenderer;
	private final AnimatedViewportModelRenderer animatedViewportModelRenderer;
	private final LinkRenderingVisitorAdapter linkRenderingVisitorAdapter;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final RenderModel renderModel;
	//	private final ResettableAnimatedIdObjectParentLinkRenderer linkRenderer;
	private ModelEditorManager modelEditorManager;
	private final Vec3 facingVector;
	private View view;

	long totTempRenderTime;
	long renderCount;


	public Viewport(byte d1, byte d2, ModelView modelView, ProgramPreferences programPreferences, ViewportActivity activityListener, ModelStructureChangeListener modelStructureChangeListener, UndoActionListener undoListener, CoordDisplayListener coordDisplayListener, UndoHandler undoHandler, ModelEditorManager modelEditorManager, ViewportTransferHandler viewportTransferHandler, RenderModel renderModel, ViewportListener viewportListener) {
		super(modelView, d1, d2, new Dimension(200, 200), programPreferences, activityListener, viewportListener, undoListener, undoHandler, coordDisplayListener);
		// Dimension 1 and Dimension 2, these specify which dimensions to display.
		// the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z dimensions

		this.modelStructureChangeListener = modelStructureChangeListener;
		this.modelEditorManager = modelEditorManager;
		this.renderModel = renderModel;
		setupCopyPaste(viewportTransferHandler);

//		coordinateSystem = new BasicCoordinateSystem(d1, d2, this);
//		coordinateSystem = this;
		viewport = this;

		contextMenu = new ViewportPopupMenu(this, this.undoListener, this.modelEditorManager, this.modelView);
		add(contextMenu);

		viewportModelRenderer = new ViewportModelRenderer(programPreferences.getVertexSize());
		animatedViewportModelRenderer = new AnimatedViewportModelRenderer(programPreferences.getVertexSize());
//		linkRenderer = new ResettableAnimatedIdObjectParentLinkRenderer(programPreferences.getVertexSize());
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
			renderModel.updateNodes(false);
//			linkRenderer.reset(this, graphics2d, NodeIconPalette.HIGHLIGHT, renderModel);
			linkRenderingVisitorAdapter.reset(coordinateSystem, graphics2d, renderModel);
			modelView.visit(linkRenderingVisitorAdapter);
			graphics2d.setStroke(stroke);
			animatedViewportModelRenderer.reset(graphics2d, programPreferences,this, coordinateSystem, modelView, renderModel);
			modelView.visit(animatedViewportModelRenderer);
			activityListener.render(graphics2d, coordinateSystem, renderModel);
		} else {
			viewportModelRenderer.reset(graphics2d, programPreferences,this, coordinateSystem, modelView);
			modelView.visit(viewportModelRenderer);
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

	@Override
	public void modelEditorChanged(ModelEditor newModelEditor) {
//		modelEditorManager = newModelEditor;
		// TODO call from display panel and above
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
		return modelView;
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