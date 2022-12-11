package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.ViewportModelRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

public class Viewport extends ViewportView {
	Timer paintTimer;

	private final ViewportModelRenderer viewportModelRenderer;
	private final LinkRenderer linkRenderer;
	private final ModelEditorManager modelEditorManager;
	private final Vec3 facingVector;
	private View view;

	long totTempRenderTime;
	long renderCount;

	public Viewport(Vec3 right, Vec3 up, ModelHandler modelHandler,
	                ViewportActivityManager activityListener,
	                BiConsumer<Double, Double> coordDisplayListener2,
	                ModelEditorManager modelEditorManager,
	                ViewportTransferHandler viewportTransferHandler) {
		super(right, up, coordDisplayListener2);
		setModel(modelHandler, activityListener);

		this.modelEditorManager = modelEditorManager;
		setupCopyPaste(viewportTransferHandler);

//		contextMenu = new ViewportPopupMenu(this, this.getRootPane(), this.modelHandler, this.modelEditorManager);
//		add(contextMenu);

		viewportModelRenderer = new ViewportModelRenderer(ProgramGlobals.getPrefs().getVertexSize());
		linkRenderer = new LinkRenderer();

		facingVector = new Vec3(coordinateSystem.getCamBackward());

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
		long renderStart = System.nanoTime();
		if (ProgramGlobals.getPrefs().show2dGrid()) {
			drawGrid(g);
		}

		Graphics2D graphics2d = (Graphics2D) g;

		if (modelEditorManager.getModelEditor().editorWantsAnimation()) {
			Stroke orgStroke = graphics2d.getStroke();
			graphics2d.setStroke(new BasicStroke(3));
			modelHandler.getRenderModel().updateNodes(false);

			linkRenderer.renderLinks(graphics2d, coordinateSystem, modelHandler);

			graphics2d.setStroke(orgStroke);
		}

		viewportModelRenderer.renderModel(graphics2d, coordinateSystem, modelHandler, modelEditorManager.getModelEditor().editorWantsAnimation());
		viewportActivityManager.render(graphics2d, coordinateSystem, modelHandler.getRenderModel(), modelEditorManager.getModelEditor().editorWantsAnimation());

		g.setColor(MoveDimension.getByAxis(coordinateSystem.getCamRight()).getColor());

		Vec2 start = coordinateSystem.viewVN(0,0);
		int startX = Math.round(start.x * getWidth());
		int startY = Math.round(start.y * getHeight());

		Vec2 end = coordinateSystem.viewVN(5,5);
		int endX   = Math.round(end.x * getWidth());
		int endY   = Math.round(end.y * getHeight());
		g.drawLine(startX, startY, endX, startY);

		g.setColor(MoveDimension.getByAxis(coordinateSystem.getCamUp()).getColor());
		g.drawLine(startX, startY, startX, endY);


		adjustAndRunPaintTimer(renderStart);
	}

	public void adjustAndRunPaintTimer(long renderStart) {
		long renderEnd = System.nanoTime();
		long currFrameRenderTime = renderEnd - renderStart;

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

	public void setViewportAxises(String name, Vec3 right, Vec3 up) {
		view.getViewProperties().setTitle(name);
		coordinateSystem.setDimensions(right, up);
	}

	public ModelEditorManager getModelEditorManager() {
		return modelEditorManager;
	}

	public ModelView getModelView() {
		return modelHandler.getModelView();
	}

	public Point getLastMouseMotion() {
		return lastMouseMotion;
	}

	public Vec3 getFacingVector() {
		return facingVector;
	}

}